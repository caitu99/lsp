package com.caitu99.lsp.parser.travel.yazhouwanlitong.normal;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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
import com.caitu99.lsp.model.parser.travel.TravelBill;
import com.caitu99.lsp.model.spider.MailSrc;
import com.caitu99.lsp.parser.utils.DOMHelper;
import com.caitu99.lsp.parser.Template;

@Template("yazhouwanlitong.normal.tpl1")
public class NormalTpl1 extends BaseTpl {

	private final static Logger logger = LoggerFactory.getLogger(NormalTpl1.class);

	private String account = null;
	private String card_no = null;
	private Date bill_month = null;
	private Long integral_balance = null;
	private List<TravelBill> billList = new ArrayList<>();
	private MailSrc mailSrc;

	public NormalTpl1() {
		super(null);
	}

	public NormalTpl1(ParserContext context) {
		super(context);
	}

	@Override
	public boolean is() {
		return mailSrc.getBody().contains(get("body_key"));
	}

	@Override
	public void setContext(ParserContext context) {
		super.setContext(context);
	}

	@Override
	public ParserContext getContext() {
		return super.getContext();
	}

	public boolean parse() {
		// 初始化
		TravelBill bill = new TravelBill();
		String body = mailSrc.getBody();
		Document document = Jsoup.parse(body);
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");

		Elements elements_td = document.getElementsByTag("td");

		// 获取account和card_no
		try {
			Element nameInfo = DOMHelper.getMinContainer(elements_td, "会员号码");
			if (nameInfo == null) {
				nameInfo = DOMHelper.getMinContainer(elements_td, "會員號碼");
			}
			Elements nameEl = nameInfo.getElementsByTag("b");
			account = nameEl.get(0).text().trim().replaceAll(" ", "");
			String pre_card_no = nameEl.get(1).text().trim().replaceAll(" ", "");
			card_no = pre_card_no.substring(pre_card_no.length() - 4, pre_card_no.length());

			if (account.getBytes("gbk").length > 12) { // 账户名长度大于12个字节，要截取
				int num = trimGBK(account.getBytes("gbk"), 12);
				account = account.substring(0, num) + "...";
			}
		} catch (Exception e) {
			logger.info("未能解析-亚洲万里通-姓名或卡号: {}", mailSrc, e);
			return false;
		}

		// 获取integral_balance
		try {
			Element integralInfo = DOMHelper.getMinContainer(elements_td, "帐户余额");
			if (integralInfo == null) {
				integralInfo = DOMHelper.getMinContainer(elements_td, "帳戶餘額");
			}
			Elements integralEl = integralInfo.getElementsByTag("b");
			integral_balance = Long.parseLong(integralEl.get(1).text().trim().replaceAll(",", ""));
		} catch (Exception e) {
			logger.info("未能解析-亚洲万里通-积分: {}", mailSrc, e);
			return false;
		}

		// 获取bill_month
		try {
			Element dateInfo = DOMHelper.getMinContainer(elements_td, "年&月&日");
			Elements dateEl = dateInfo.getElementsByTag("td");
			bill_month = format.parse(dateEl.get(0).text().trim().replace("年", "-").replace("月", "-").replace("日", ""));
		} catch (Exception e) {
			logger.info("未能解析-亚洲万里通-账单月: {}", mailSrc, e);
			return false;
		}

		bill.setName(account);
		bill.setIntegral(integral_balance);
		bill.setCardNo(card_no);
		bill.setBillDay(bill_month);
		bill.setTpl(this);
		bill.setId(this.getCard().getId());
		billList.add(bill);

		return true;
	}

	@Override
	public boolean check() {
		List<Bill> tempBillList = new ArrayList<>();
		for (Bill bill : billList) {
			TravelBill travelBill = (TravelBill) bill;
			if (StringUtils.isEmpty(travelBill.getName()) 
					|| travelBill.getBillDay() == null
					|| StringUtils.isEmpty(travelBill.getIntegral()) 
					|| StringUtils.isEmpty(travelBill.getCardNo())) {
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

	public static int trimGBK(byte[] buf, int n) {
		int num = 0;
		boolean bChineseFirstHalf = false;
		for (int i = 0; i < n; i++) {
			// 一个字节如果小于0那么这个字符肯定就是存储汉字的一半了
			if (buf[i] < 0 && !bChineseFirstHalf) {
				bChineseFirstHalf = true;
			} else {
				num++;
				bChineseFirstHalf = false;
			}
		}
		return num;
	}

}
