package com.caitu99.lsp.parser.bank.zhongguo.normal;

import com.caitu99.lsp.model.parser.Bill;
import com.caitu99.lsp.model.parser.ParserContext;
import com.caitu99.lsp.model.parser.bank.BankBill;
import com.caitu99.lsp.model.spider.MailSrc;
import com.caitu99.lsp.parser.Template;
import com.caitu99.lsp.parser.utils.DOMHelper;
import com.caitu99.lsp.parser.utils.TplHelper;
import com.caitu99.lsp.utils.XStringUtil;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@Template("zhongguo.normal.tpl1")
public class NormalTpl1 extends BaseTpl {

	private final static Logger logger = LoggerFactory
			.getLogger(NormalTpl1.class);

	private MailSrc mailSrc;

	private List<Bill> billList = new ArrayList<>();

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
		SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd");
		String account = null;
		Date billMonth = null;
		Long integral = null;
		String cardNo = null;
		Map<String, Object> others = new HashMap<>();
		try {
			// 获取卡号
			Elements cardNoElements = document.getElementsByTag("table");
			Element cardNoElement = DOMHelper.getMinContainer(cardNoElements,
					"温馨提示");
			if (cardNoElement == null) {
				logger.info("获取卡号失败: {}", mailSrc);
				return false;
			}
			cardNo = cardNoElement.text();
			int pos = cardNo.indexOf("****");
			cardNo = cardNo.substring(pos + 4, pos + 8);
			// 获取账户
			Element accountElement = cardNoElement.parent().parent().child(0)
					.child(0);
			if (accountElement == null) {
				logger.info("获取账户失败: {}", mailSrc);
				return false;
			}
			// account =
			// accountElement.getElementsByTag("br").get(3).nextSibling()
			// .toString().replaceAll(" ", "").replaceAll("女士", "")
			// .replaceAll("先生", "").replaceAll("&nbsp;", "");
			String[] strs = accountElement.child(0).child(0).child(0).html()
					.toLowerCase().split("<br>");
			account = strs[strs.length - 1];
			account = account.replaceAll(" ", "").replaceAll("女士", "")
					.replaceAll("先生", "").replaceAll("&nbsp;", "");
			// 获取账单月
			Elements monthElements = document.getElementsByTag("table");
			Element monthElement = DOMHelper.getMinContainer(monthElements,
					"到期还款日&账单日期&信用额度");
			if (monthElement == null) {
				logger.info("获取账单周期失败: {}", mailSrc);
				return false;
			}
			monthElements = monthElement.parent().nextElementSibling()
					.getElementsByTag("table");
			monthElement = DOMHelper.getMinContainer(monthElements, null)
					.parent().parent().parent();
			if (monthElement.child(1) != null) {
				billMonth = format.parse(monthElement.child(1).text().trim());
			} else {
				logger.info("获取账单周期失败: {}", mailSrc);
				return false;
			}
			// 获取账单其它属性
			// try {
			//
			// } catch (Exception e) {
			// logger.info("解析账单其它属性失败: {}", mailSrc);
			// }
			String commonAmount = monthElement.child(2).text();
			commonAmount = commonAmount.substring(0, commonAmount.indexOf("."));
			commonAmount = XStringUtil.toNumber(commonAmount);
			others.put("commonAmount", commonAmount);
			// 获取积分数
			Elements integralElements = document.getElementsByTag("table");
			Element integralElement = DOMHelper
					.getMinContainer(integralElements, "您的期末积分余额").parent()
					.nextElementSibling();
			String sIntegral = integralElement.text();
			sIntegral = XStringUtil.toNumber(sIntegral);
			if (StringUtils.isEmpty(sIntegral))
				integral = -1L;
			else
				integral = Long.parseLong(sIntegral);
			BankBill billCard = new BankBill();
			billCard.setName(account);
			billCard.setAlone(true);
			billCard.setBillDay(billMonth);
			billCard.setCardNo(cardNo);
			billCard.setIntegral(integral);
			billCard.setOthers(others);
			billCard.setTpl(this);
			billCard.setId(this.getCard().getId());
			billList.add(billCard);
			return true;
		} catch (Exception e) {
			logger.info("账单解析失败: {}", mailSrc, e);
		}
		return false;
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
