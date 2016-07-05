package com.caitu99.lsp.parser.bank.pufa.normal;

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

@Template("pufa.normal.tpl1")
public class NormalTpl1 extends BaseTpl {

	private final static Logger logger = LoggerFactory
			.getLogger(NormalTpl1.class);

	private String account = null; // 用户名
	private String card_no = null; // 卡号
	private Date bill_month = null; // 账单月
	private Float repay_amount = null; // 本期应还总额
	private Float repay_min = null; // 本期最低还款总额
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
		Elements elements_tr = document.getElementsByTag("tr");

		// 获取账单月 bill_month
		try {
			Element billMonthInfo = DOMHelper.getMinContainer(elements_tr,
					"账单日");
			Elements billMonthInfoEl = billMonthInfo.getElementsByTag("td");
			String pre_bill_month = billMonthInfoEl.get(5).text().trim();
			bill_month = format.parse(pre_bill_month.replace("账单日：", "")
					.replaceAll("/", "-"));
		} catch (Exception e) {
			logger.info("未能获取账单-浦发银行-账单月: {}", mailSrc, e);
			return false;
		}

		// 获取account
		try {
			Element accountInfo = DOMHelper.getMinContainer(elements_td, "尊敬的");
			Elements accountInfoEl = accountInfo.getElementsByTag("td");
			String pre_account = accountInfoEl.get(0).text().trim();
			int index = pre_account.contains("女士") ? pre_account.indexOf("女士")
					: pre_account.indexOf("先生");
			account = pre_account.substring(pre_account.indexOf("尊敬的") + 3,
					index).replaceAll(" ", "").trim();
		} catch (Exception e) {
			logger.info("未能获取账单-浦发银行-用户名: {}", mailSrc, e);
			return false;
		}

		// 获取card_no
		try {
			Element cardNoInfo = DOMHelper.getMinContainer(elements_td, "尾号");
			Elements cardNoInfoEl = cardNoInfo.getElementsByTag("td");
			String pre_card_no = cardNoInfoEl.get(0).text().trim();
			card_no = pre_card_no.substring(pre_card_no.indexOf("尾号") + 2,
					pre_card_no.indexOf("的云账单")).trim();
		} catch (Exception e) {
			logger.info("未能获取账单-浦发银行-卡号: {}", mailSrc, e);
			return false;
		}

		// 获取repay_amount 和 repay_min
		try {
			Element repayInfo = DOMHelper.getMinContainer(elements_tr,
					"本期应还款总额");
			Elements repayInfoEl = repayInfo.getElementsByTag("td");
			String pre_repay_amount = repayInfoEl.get(2).text().trim()
					.replaceAll(",", "");
			repay_amount = Float.parseFloat(pre_repay_amount.substring(
					pre_repay_amount.indexOf("￥") + 1,
					pre_repay_amount.length()));

			String pre_repay_min = repayInfoEl.get(5).text().trim()
					.replaceAll(",", "");
			repay_min = Float.parseFloat(pre_repay_min.substring(
					pre_repay_min.indexOf("￥") + 1, pre_repay_min.length()));
		} catch (Exception e) {
			logger.info("未能获取账单-浦发银行-卡号: {}", mailSrc, e);
		}

		others.put("repay_amount", repay_amount);
		others.put("repay_min", repay_min);

		billCard.setName(account);
		billCard.setCardNo(card_no);
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
