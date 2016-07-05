package com.caitu99.lsp.parser.bank.xingye.normal;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
import com.caitu99.lsp.parser.utils.DOMHelper;
import com.caitu99.lsp.parser.Template;

@Template("xingye.normal.tpl1")
public class NormalTpl1 extends BaseTpl {

	private final static Logger logger = LoggerFactory.getLogger(NormalTpl1.class);

	private String account = null; // 用户名
	private String card_no = null; // 卡号
	private Date bill_month = null; // 账单月
	private Long integral_balance = null; // 积分
	private int repayment_day; // 还款日
	private Float repay_amount = null; // 本期应还总额
	private Float repay_credit = null; // 信用额度
	private Float repay_min = null; // 本期最低还款总额
	private Float repay_cash_credit = null; // 预借现金额度
	private Map<String, Object> others = new HashMap<>();
	private List<BankBill> billList = new ArrayList<>();
	private MailSrc mailSrc;

	public NormalTpl1() {
		super(null);
	}

	public NormalTpl1(ParserContext context) {
		super(context);
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
	public boolean is() {
		return false;
	}

	@Override
	public boolean parse() {
		// 初始化
		BankBill billCard = new BankBill();
		String body = mailSrc.getBody();
		Document document = Jsoup.parse(body);
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");

		Elements elements_td = document.getElementsByTag("td");
		Elements elements_font = document.getElementsByTag("font");

		// 获取account
		Element nameInfo = DOMHelper.getMinContainer(elements_td, "尊敬的&您好");

		String nameTxt = nameInfo.text().trim().replaceAll(" ","").replaceAll("\\u00A0", "");
		Pattern namePattern = Pattern.compile(get("name"));
		Matcher matcher = namePattern.matcher(nameTxt);
		if (matcher.find()) {
			this.account = matcher.group();
		} else {
			logger.info("未能解析-兴业银行-用户名: {}", mailSrc);
			return false;
		}

		// 获取卡号
		try{
			Element cardNoInfo = DOMHelper.getMinContainer(elements_td, "感谢您选择兴业银行信用卡");
			Elements cardNoInfoEl = cardNoInfo.getElementsByTag("b");
			String pre_card_no = cardNoInfoEl.get(0).text().trim();
			card_no = pre_card_no.substring(pre_card_no.length() - 5, pre_card_no.length() - 1);
		}catch(Exception e){
			logger.info("未能解析-兴业银行-卡号: {}", mailSrc, e);
			return false;
		}


		// 获取integral_balance 本期积分余额
		Element integralInfo = DOMHelper.getMinContainer(elements_td, "本期积分余额");
		Elements integralInfoEl = integralInfo.getElementsByTag("td");
		String[] pre_integral = integralInfoEl.get(0).text().trim().split(" ");
		try {
			integral_balance = Long.parseLong(pre_integral[pre_integral.length - 1]);
		} catch (java.lang.NumberFormatException e) {
			logger.info("未能解析-兴业银行-积分: {}", mailSrc, e);
			return false;
		}

		// 获取BillMonth
		Element dateInfo = DOMHelper.getMinContainer(elements_td, "账单周期");
		Elements dateEl = dateInfo.getElementsByTag("span");
		if (dateEl.size() == 0) {
			try {
				Elements dateEl2 = dateInfo.getElementsByTag("td");
				Pattern billMonthPattern = Pattern.compile(get("bill_month"));
				Matcher bill_month_matcher = billMonthPattern.matcher(dateEl2.toString());
				if (bill_month_matcher.find()) {
					bill_month = format.parse(bill_month_matcher.group().trim().replaceAll("/", "-"));
				}
			} catch (Exception e) {
				logger.info("未能解析-兴业银行-账单月: {}", mailSrc, e);
				return false;
			}
		} else {
			try {
				bill_month = format.parse(dateEl.get(1).text().trim().replaceAll("/", "-"));
			} catch (ParseException e) {
				logger.info("未能解析-兴业银行-账单月: {}", mailSrc, e);
				return false;
			}
		}

		// 获取repayment_day
		Element repaymentDayInfo = DOMHelper.getMinContainer(elements_font, "到期还款日");
		Elements repaymentDayInfoEl = repaymentDayInfo.getElementsByTag("span");
		if (repaymentDayInfoEl.size() == 0) {
			repaymentDayInfoEl = repaymentDayInfo.getElementsByTag("b");
		}
		String pre_repayment_day = repaymentDayInfoEl.get(0).text().trim();
		try {
			repayment_day = Integer.parseInt(
					pre_repayment_day.substring(pre_repayment_day.indexOf("月") + 1, pre_repayment_day.indexOf("日")));
		} catch (Exception e) {
			logger.info("未能解析-兴业银行-还款日: {}", mailSrc, e);
		}

		// 获取repay_amount
		try {
			Element totalBlanceInfo = DOMHelper.getMinContainer(elements_font, "本期应还款总额");
			Elements totalBlanceInfoEl = totalBlanceInfo.getElementsByTag("b");
			String pre_total_balance = totalBlanceInfoEl.get(0).text().trim();
			repay_amount = Float.parseFloat(
					pre_total_balance.replace("RMB", "").replaceAll(" ", "").replace(",", "").replaceAll(" ", ""));
		} catch (Exception e) {
			logger.info("未能解析-兴业银行-本期应还总额: {}", mailSrc, e);
		}

		// 获取repay_credit Credit Limit()</font> 33,000 <br>
		try {
			Element maxAmountInfo = DOMHelper.getMinContainer(elements_td, "信用额度");
			Elements maxAmountInfoEl = maxAmountInfo.getElementsByTag("td");
			Pattern maxAmountPattern = Pattern.compile(get("repay_credit1"));
			Pattern maxAmountPattern2 = Pattern.compile(get("repay_credit2"));
			Matcher max_amount_matcher = maxAmountPattern.matcher(maxAmountInfoEl.toString());
			Matcher max_amount_matcher2 = maxAmountPattern2.matcher(maxAmountInfoEl.toString());
			if (max_amount_matcher.find()) {
				String pre_max_amount = max_amount_matcher.group().replace("RMB", "").replace(",", "").trim(); // ()</font>
																												// 33000
				repay_credit = Float
						.parseFloat(pre_max_amount.substring(pre_max_amount.indexOf(">") + 1, pre_max_amount.length())
								.trim().replaceAll(",", ""));
			} else if (max_amount_matcher2.find()) {
				String pre_max_amount = max_amount_matcher2.group().trim();
				repay_credit = Float.parseFloat(pre_max_amount
						.substring(pre_max_amount.indexOf("font>") + 5, pre_max_amount.length()).replaceAll(",", ""));
			} else {
				logger.info("未能解析-兴业银行-信用额度: {}", mailSrc);
			}
		} catch (Exception e) {
			logger.info("未能解析-兴业银行-信用额度: {}", mailSrc, e);
		}

		// 获取repay_min Minimum Payment</font> RMB 1,603.20<font>
		Element minAmountInfo = DOMHelper.getMinContainer(elements_td, "本期最低还款额");
		Elements minAmountInfoEl = minAmountInfo.getElementsByTag("td");
		Pattern minAmountPattern = Pattern.compile(get("repay_min1"));
		Pattern minAmountPattern2 = Pattern.compile(get("repay_min2"));
		Matcher min_amount_matcher2 = minAmountPattern2.matcher(minAmountInfoEl.toString());
		Matcher min_amount_matcher = minAmountPattern.matcher(minAmountInfoEl.toString());
		try {
			if (min_amount_matcher.find()) {
				repay_min = Float.parseFloat(min_amount_matcher.group().replace("RMB", "").replace(",", "").trim());
			} else if (min_amount_matcher2.find()) {
				repay_min = Float.parseFloat(min_amount_matcher2.group().replace("RMB", "").replace(",", "").trim());
			} else {
				logger.info("未能解析-兴业银行-最小还款额: {}", mailSrc);
			}
		} catch (Exception e) {
			logger.info("未能解析-兴业银行-最小还款额: {}", mailSrc, e);
		}

		// 获取repay_cash_credit      //Cash Advance Limit(RMB)</font> 16,500 </td>
		try {
			Element cashAmountInfo = DOMHelper.getMinContainer(elements_td, "预借现金额度(人民币)");
			Elements cashAmountInfoEl = cashAmountInfo.getElementsByTag("td");
			Pattern cashAmountPattern = Pattern.compile(get("cash_amount1"));
			Pattern cashAmountPattern2 = Pattern.compile(get("cash_amount2"));
			Matcher cash_amount_matcher = cashAmountPattern.matcher(cashAmountInfoEl.toString());
			Matcher cash_amount_matcher2 = cashAmountPattern2.matcher(cashAmountInfoEl.toString());
			if (cash_amount_matcher.find()) {
				String pre_cash_amount = cash_amount_matcher.group().replace("RMB", "").replace(",", "").trim(); // ()</font>
				repay_cash_credit = Float.parseFloat(
						pre_cash_amount.substring(pre_cash_amount.indexOf(">") + 1, pre_cash_amount.length()).trim());
			} else if (cash_amount_matcher2.find()) {
				String pre_cash_amount = cash_amount_matcher2.group().trim().replaceAll(",", "");
				int index = pre_cash_amount.contains("<br") ? pre_cash_amount.indexOf("<br"):pre_cash_amount.length();
				repay_cash_credit = Float.parseFloat(
						pre_cash_amount.substring(pre_cash_amount.indexOf("font>") + 5, index));
			} else {
				logger.info("未能解析-兴业银行-预借现金额度: {}", mailSrc);
			}
		} catch (Exception e) {
			logger.info("未能解析-兴业银行-预借现金额度: {}", mailSrc, e);
		}

		others.put("repayment_day", repayment_day);
		others.put("repay_amount", repay_amount);
		others.put("repay_credit", repay_credit);
		others.put("repay_min", repay_min);
		others.put("repay_cash_credit", repay_cash_credit);

		billCard.setName(account);
		billCard.setCardNo(card_no);
		billCard.setIntegral(integral_balance);
		billCard.setBillDay(bill_month);
		billCard.setOthers(others);
		billCard.setTpl(this);
		billCard.setAlone(true);
		billCard.setId(this.getCard().getId());
		billList.add(billCard);

		return true;
	}

	@Override
	public boolean check() {
		List<Bill> tempBillList = new ArrayList<>();
		for (Bill bill : billList) {
			BankBill bankBill = (BankBill) bill;
			if (StringUtils.isEmpty(bankBill.getName()) 
					|| bankBill.getBillDay() == null
					|| StringUtils.isEmpty(bankBill.getCardNo()) 
					|| bankBill.getIntegral() == null) {
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
