package com.caitu99.lsp.parser.bank.pingan.normal;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import com.caitu99.lsp.model.parser.Bill;
import com.caitu99.lsp.model.parser.ParserContext;
import com.caitu99.lsp.model.parser.bank.BankBill;
import com.caitu99.lsp.model.spider.MailSrc;
import com.caitu99.lsp.parser.Template;
import com.caitu99.lsp.parser.utils.DOMHelper;
import com.caitu99.lsp.parser.utils.TplHelper;
import com.caitu99.lsp.utils.XStringUtil;

@Template("pingan.normal.tpl1")
public class NormalTpl1 extends BaseTpl {

	private final static Logger logger = LoggerFactory
			.getLogger(NormalTpl1.class);

	private List<Bill> billList = new ArrayList<>();
	private MailSrc mailSrc;

	public NormalTpl1() {
		super(null);
	}

	public NormalTpl1(ParserContext context) {
		super(context);
	}

	@Override
	public boolean is() {
		return false;
	}

	@Override
	public void setContext(ParserContext context) {
		super.setContext(context);
	}

	@Override
	public ParserContext getContext() {
		return super.getContext();
	}

	@Override
	public boolean parse() {
		// 初始化
		String body = mailSrc.getBody();
		Document document = Jsoup.parse(body);
		String account = null;
		Date billMonth = null;
		Long integral = null;
		String cardNo = null;
		Map<String, String> others = new HashMap<>();
		// 获取账户
		Elements accountElements = document.getElementsByTag("strong");
		Element accountElement = DOMHelper.getMinContainer(accountElements,
				"尊敬的&先生");
		if (accountElement == null) {
			logger.info("获取账户姓名失败: {}", mailSrc);
			return false;
		}
		account = accountElement.text();
		int pos = account.indexOf("先生") == -1 ? account.indexOf("女士") : account
				.indexOf("先生");
		account = account.substring(account.indexOf("尊敬的") + 3, pos)
				.replaceAll(" ", "");
		// 获取账单月
		Elements monthElements = document.getElementsByTag("table");
		Element monthElement = DOMHelper.getMinContainer(monthElements,
				"本期账单日&本期还款日&信用额度&取现额度");
		if (monthElement == null) {
			logger.info("获取账单周期失败: {}", mailSrc);
			return false;
		}
		Elements attrElements = monthElement.getElementsByTag("tr");
		if (attrElements == null || attrElements.size() == 0) {
			logger.info("获取账单周期失败: {}", mailSrc);
			return false;
		}
		for (int i = 0; i < attrElements.size(); i++) {
			Element attr = attrElements.get(i).getElementsByTag("td").last();
			String sBillMonth = null;
			try {
				if (i == 0) {
					sBillMonth = attr.text();
					SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
					billMonth = format.parse(sBillMonth);
				} else if (i == 1) {// 还款日，暂不打开
					// others.put("", attr.child(0).child(0).child(0).text()
					// .trim());
				} else if (i == 2) {
					String commonAmount = attr.text();
					// ¥ 50,000.00
					commonAmount = commonAmount.substring(0,
							commonAmount.indexOf("."));
					commonAmount = XStringUtil.toNumber(commonAmount);
					others.put("commonAmount", commonAmount);
				} else if (i == 3) {
					String cashAmount = attr.text();
					cashAmount = cashAmount.substring(0,
							cashAmount.indexOf("."));
					cashAmount = XStringUtil.toNumber(cashAmount);
					others.put("cashAmount", cashAmount);
				}
			} catch (ParseException e) {
				logger.info("账单日转换错误: {}", mailSrc);
				return false;
			} catch (Exception e) {
				logger.info("获取其它属性失败: {}", mailSrc);
			}
		}
		// 获取总积分
		Elements integralElements = document.getElementsByTag("table");
		Element integralElement = DOMHelper.getMinContainer(integralElements,
				"积分账户&万里通积分");
		integralElement = integralElement.child(0).child(2);
		try {
			integral = Long.parseLong(XStringUtil.toNumber(integralElement
					.child(0).text()));
		} catch (Exception e) {
			logger.info("获取积分失败: {}", mailSrc);
			return false;
		}
		// 获取卡号
		Set<String> cardNoSet = new HashSet<>();
		try {
			Elements cardNoElements = document.getElementsByTag("table");
			cardNo = DOMHelper.getMinContainer(integralElements, "交易种类&交易日期")
					.child(0).child(1).child(1).child(0).text().trim();
			cardNo = cardNo.substring(cardNo.length() - 4);
		} catch (Exception e) {
			logger.info("获取卡号失败: {}", mailSrc);
			return false;
		}
		BankBill bankBill = new BankBill();
		bankBill.setName(account);
		bankBill.setBillDay(billMonth);
		bankBill.setCardNo(cardNo);
		bankBill.setIntegral(integral);
		bankBill.setTpl(this);
		bankBill.setAlone(false);
		bankBill.setId(this.getCard().getId());
		billList.add(bankBill);
		return true;
	}

	@Override
	public boolean check() {
		List<Bill> tempBillList = new ArrayList<>();
		for (Bill bill : billList) {
			BankBill bankBill = (BankBill) bill;
			if (StringUtils.isEmpty(bankBill.getName())
					|| bankBill.getBillDay() == null
					|| StringUtils.isEmpty(bankBill.getCardNo())) {
				continue;
			}
			tempBillList.add(bill);
		}

		if (tempBillList.size() == 0)
			return false;

		this.getContext().getBills().addAll(tempBillList);

		return true;
	}

	@Override
	public MailSrc getMailSrc() {
		return mailSrc;
	}

	@Override
	public void setMailSrc(MailSrc mailSrc) {
		this.mailSrc = mailSrc;
	}

	@Override
	public String getName() {
		return TplHelper.getTplName(this.getClass());
	}
}
