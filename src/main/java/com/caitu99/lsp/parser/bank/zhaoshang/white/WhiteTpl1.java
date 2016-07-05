package com.caitu99.lsp.parser.bank.zhaoshang.white;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.caitu99.lsp.parser.utils.DOMHelper;
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

@Template("zhaoshang.white.tpl1")
public class WhiteTpl1 extends BaseTpl {

	private final static Logger logger = LoggerFactory
			.getLogger(WhiteTpl1.class);

	private MailSrc mailSrc;

	private List<Bill> billList = new ArrayList<>();

	public WhiteTpl1() {
		super(null);
	}

	public WhiteTpl1(ParserContext context) {
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
		Pattern pattern = Pattern.compile(nameReg);
		Matcher matcher = pattern.matcher(body);
		if (matcher.find()) {
			account = matcher.group();
		} else {
			logger.info("未能获取账单用户名: {}", mailSrc);
			return false;
		}
		// 获取账户账单月
		pattern = Pattern.compile(billMonthReg);
		matcher = pattern.matcher(DOMHelper.filterHtml(body));
		if (matcher.find()) {
			String group = matcher.group();
			group = group.substring(group.lastIndexOf("您") + 1,
					group.lastIndexOf("信用卡"));
			SimpleDateFormat format = new SimpleDateFormat("yyyy年MM月");
			try {
				billMonth = format.parse(group);
			} catch (ParseException e) {
				// logger.error("将账单月【{}】转为日期时报错", group, e);
				logger.info("将账单月转为日期时报错: {}", mailSrc);
			}
		} else {
			logger.info("未能获取账单月: {}", mailSrc);
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
				Elements s = element.getElementsByTag("table");
				cardNoSet.add(s.last().getElementsByTag("td").get(5).text());
			}
		} catch (Exception e) {
			logger.info("获取卡号失败: {}", mailSrc);
			return false;
		}
		if (cardNoSet.size() > 0) {
			for (String cardNo : cardNoSet) {
				BankBill bankBill = new BankBill();
				bankBill.setTpl(this);
				bankBill.setName(account);
				bankBill.setBillDay(billMonth);
				bankBill.setCardNo(cardNo);
				bankBill.setIntegral(integral);
				bankBill.setId(this.getCard().getId());
				billList.add(bankBill);
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
}
