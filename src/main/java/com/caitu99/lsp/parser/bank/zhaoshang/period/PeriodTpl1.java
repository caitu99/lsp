package com.caitu99.lsp.parser.bank.zhaoshang.period;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.caitu99.lsp.parser.utils.TplHelper;

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

@Template("zhaoshang.period.tpl1")
public class PeriodTpl1 extends BaseTpl {

	private final static Logger logger = LoggerFactory
			.getLogger(PeriodTpl1.class);

	private List<Bill> billList = new ArrayList<>();

	private MailSrc mailSrc;

	public PeriodTpl1() {
		super(null);
	}

	public PeriodTpl1(ParserContext context) {
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
		String nameReg = this.get("name");// 账户
		String billMonthReg = this.get("month");// 账单月
		String body = mailSrc.getBody();
		Document document = Jsoup.parse(body);
		String account = null;
		Date billMonth = null;
		Long integral = null;
		// 获取账户
		Elements accountElements = document.getElementsByTag("font");
		Element accountElement = DOMHelper.getMinContainer(accountElements,
				"亲爱的&，您好！");
		if (accountElement == null) {
			logger.info("获取账户姓名失败: {}", mailSrc);
			return false;
		}
		account = accountElement.text();
		int pos = account.indexOf("先生") == -1 ? account.indexOf("女士") : account
				.indexOf("先生");
		account = account.substring(account.indexOf("亲爱的") + 3, pos)
				.replaceAll(" ", "");
		// 获取账单月
		Elements monthElements = document.getElementsByTag("table");
		Element monthElement = DOMHelper.getMinContainer(monthElements, "账单周期");
		if (monthElement == null) {
			logger.info("获取账单周期失败: {}", mailSrc);
			return false;
		}
		monthElements = monthElement.getElementsByTag("td");
		String sMonth = monthElements.get(3).text();
		sMonth = sMonth.split("-")[1];
		SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd");
		try {
			billMonth = format.parse(sMonth);
		} catch (ParseException e1) {
			logger.info("账单月转化失败: {}", mailSrc);
			return false;
		}
		// 获取总积分
		Element integralElement = document.getElementById("fixBand33");
		if (integralElement == null)
			integralElement = document.getElementById("ecxfixBand33");
		try {
			integral = Long.parseLong(integralElement.getElementsByTag("table")
					.last().getElementsByTag("td").get(1).text().trim());
		} catch (Exception e) {
			logger.info("获取积分失败: {}", mailSrc);
			return false;
		}
		// 获取卡号
		Set<String> cardNoSet = new HashSet<>();
		try {
			Element listElement = document.getElementById("loopBand2");
			if (listElement == null)
				listElement = document.getElementById("ecxloopBand2");
			Elements elements = listElement.child(0).child(0).children();
			boolean isFirst = true;
			for (Element element : elements) {
				if (isFirst) {
					isFirst = false;
					continue;
				}
				String cardNo = element.getElementsByTag("table").get(2)
						.getElementsByTag("td").get(6).text().trim();
				if (!isCardNo(cardNo)) {
					cardNo = element.getElementsByTag("table").get(2)
							.getElementsByTag("td").get(5).text().trim();
				}
				if (isCardNo(cardNo))
					cardNoSet.add(cardNo);
			}
		} catch (Exception e) {
			logger.info("获取卡号失败: {}", mailSrc);
			return false;
		}
		if (cardNoSet.size() > 0) {
			for (String cardNo : cardNoSet) {
				BankBill bankCard = new BankBill();
				bankCard.setTpl(this);
				bankCard.setName(account);
				bankCard.setBillDay(billMonth);
				bankCard.setCardNo(cardNo);
				bankCard.setIntegral(integral);
				bankCard.setId(this.getCard().getId());
				billList.add(bankCard);
			}
		} else {
			BankBill bankBill = new BankBill();
			bankBill.setTpl(this);
			bankBill.setAlone(false);
			bankBill.setName(account);
			bankBill.setBillDay(billMonth);
			bankBill.setCardNo("--");
			bankBill.setIntegral(integral);
			bankBill.setId(this.getCard().getId());
			billList.add(bankBill);
		}
		return true;
	}

	@Override
	public boolean check() {
		List<Bill> tempBillList = new ArrayList<>();
		for (Bill bill : billList) {
			if (StringUtils.isEmpty(bill.getName())
					|| bill.getBillDay() == null) {
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

	private boolean isCardNo(String cardNo) {
		if (StringUtils.isEmpty(cardNo))
			return false;
		cardNo = cardNo.trim();
		for (int i = 0; i < cardNo.length(); i++) {
			if (48 > cardNo.charAt(i) || cardNo.charAt(i) > 57)
				return false;
		}
		return true;
	}
}
