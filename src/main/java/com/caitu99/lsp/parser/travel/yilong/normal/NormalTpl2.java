package com.caitu99.lsp.parser.travel.yilong.normal;

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

@Template("yilong.normal.tpl2")
public class NormalTpl2 extends BaseTpl {

	private final static Logger logger = LoggerFactory.getLogger(NormalTpl2.class);

	private String account = null;
	private String card_no = null;
	private Date bill_month = null;
	private Long integral_balance = null;
	private List<TravelBill> billList = new ArrayList<>();

	private MailSrc mailSrc;

	public NormalTpl2() {
		super(null);
	}

	public NormalTpl2(ParserContext context) {
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

	@Override
	public boolean parse() {
		// 初始化
		TravelBill bill = new TravelBill();
		String body = mailSrc.getBody();
		Document document = Jsoup.parse(body);
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");

		Elements elements_td = document.getElementsByTag("td");
		Elements elements_tr = document.getElementsByTag("tr");





		//获取积分
		try {
			Element integarInfo = DOMHelper.getMinContainer(elements_tr, "可用积分");
			Elements integarInfoEl = integarInfo.getElementsByTag("td");
			String pre_integral_balance = integarInfoEl.get(2).text().trim().replaceAll(",", "");
			integral_balance = Long.parseLong(pre_integral_balance);
		} catch (Exception e) {
			logger.info("未能解析-艺龙-积分: {}", mailSrc, e);
			return false;
		}

		//获取账单月
		try {
			Element billMonthInfo = DOMHelper.getMinContainer(elements_tr, "统计日期");
			Elements billMonthInfoEl = billMonthInfo.getElementsByTag("td");
			String pre_bill_month = billMonthInfoEl.get(2).text().trim();
			bill_month = format.parse(pre_bill_month);
		} catch (Exception e) {
			logger.info("未能解析-艺龙-账单月: {}", mailSrc, e);
			return false;
		}

		try {
			Elements elements2 = document.getElementsByTag("a");
			Element nameInfo = DOMHelper.getMinContainer(elements2, "尊敬的");
			account = nameInfo.getElementsByTag("font").get(0).text().trim(); // 如果是
																				// 尊敬的艺龙会员
																				// ，就用card_no代替
			if ("尊敬的艺龙会员".equals(account)) {
				Element accountInfo = DOMHelper.getMinContainer(elements_tr, "艺龙账户");
				Elements accountInfoEl = accountInfo.getElementsByTag("td");
				card_no = accountInfoEl.get(2).text().trim();
				account = card_no;
			}
			if (account.getBytes("gbk").length > 12) { // 账户名长度大于12个字节，要截取
				int num = trimGBK(account.getBytes("gbk"), 12);
				bill.setName(account.substring(0, num) + "...");
			}
		} catch (Exception e) {
			logger.info("未能解析-艺龙-姓名: {}", mailSrc, e);
			return false;
		}
		bill.setBillDay(bill_month);
		bill.setIntegral(integral_balance);
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
					|| StringUtils.isEmpty(travelBill.getIntegral())) {
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
