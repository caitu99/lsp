package com.caitu99.lsp.parser.bank.zhongxin.normal;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

@Template("zhongxin.normal.tpl1")
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
		
		SimpleDateFormat format = new SimpleDateFormat("yyyy年MM月dd日");
		String account = null;
		Date billMonth = null;
		Long integral = null;
		String cardNo = null;
		Map<String, String> others = new HashMap<>();
		// 获取账户
		Elements accountElements = document.getElementsByTag("font");
		Element accountElement = DOMHelper.getMinContainer(accountElements,
				"尊敬的&：");
		if (accountElement == null) {
			logger.info("获取账户失败: {}", mailSrc);
			return false;
		}
		account = accountElement.text().trim();
		account = account.replaceAll("尊敬的", "").replaceAll("：", "")
				.replaceAll("先生", "").replaceAll("女士", "")
				.replaceAll("      ", "");
		// 获取账单日期
		Elements billMonthElements = document.getElementsByTag("font");
		Element billMonthElement = DOMHelper.getMinContainer(billMonthElements,
				"您好！&记录了您&现为您诚意奉上");
		if (billMonthElement == null) {
			logger.info("获取账单日期失败: {}", mailSrc);
			return false;
		}
		String sBillMonth = billMonthElement.text().trim();
		int pos = sBillMonth.indexOf("账户变动信息，现为您诚意奉上，仅供您参考");
		sBillMonth = sBillMonth.substring(pos - 11, pos);
		try {
			billMonth = format.parse(sBillMonth);
		} catch (ParseException e) {
			logger.info("账单日期转换失败: {}", mailSrc);
			return false;
		}
		// 获取卡号及积分
		Element recordElement = document.getElementById("loopHeader1");
		if (recordElement == null) {
			logger.info("获取卡号及积分信息失败: {}", mailSrc);
			return false;
		}
		recordElement = recordElement.parent().parent().parent().parent()
				.parent().parent().parent();
		if (!"tbody".equalsIgnoreCase(recordElement.tagName())) {
			logger.info("获取卡号及积分信息失败: {}", mailSrc);
			return false;
		}
		Elements records = recordElement.children();
		for (int i = 1; i < records.size(); i++) {
			Elements tables = records.get(i).getElementsByTag("table");
			Element table = DOMHelper.getMinContainer(tables, null);
			Elements tds = table.getElementsByTag("td");
			BankBill bankBill = new BankBill();
			bankBill.setName(account);
			bankBill.setAlone(true);
			bankBill.setBillDay(billMonth);
			String sIntegral = "-1";
			if (tds.get(1).text().indexOf("****") >= 0) {
				String sCardNo = tds.get(1).text().trim();
				bankBill.setCardNo("**"
						+ sCardNo.substring(sCardNo.length() - 2));
				sIntegral = tds.get(6).text();
			} else if (tds.get(2).text().indexOf("****") >= 0) {
				String sCardNo = tds.get(2).text().trim();
				bankBill.setCardNo("**"
						+ sCardNo.substring(sCardNo.length() - 2));
				sIntegral = tds.get(7).text();
			} else if (tds.get(3).text().indexOf("****") >= 0) {
				String sCardNo = tds.get(3).text().trim();
				bankBill.setCardNo("**"
						+ sCardNo.substring(sCardNo.length() - 2));
				sIntegral = tds.get(8).text();
			}
			bankBill.setIntegral(Long.valueOf(XStringUtil.toNumber(sIntegral)));
			bankBill.setTpl(this);
			bankBill.setId(this.getCard().getId());
			billList.add(bankBill);
		}
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
