package com.caitu99.lsp.parser.bank.zhaoshang.normal;

import java.text.ParseException;
import java.text.SimpleDateFormat;
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

import com.caitu99.lsp.model.parser.ParserContext;
import com.caitu99.lsp.model.parser.bank.BankBill;
import com.caitu99.lsp.model.spider.MailSrc;
import com.caitu99.lsp.parser.Template;

@Template("zhaoshang.normal.tpl1")
public class NormalTpl1 extends BaseTpl {

	private final static Logger logger = LoggerFactory
			.getLogger(NormalTpl1.class);

	private BankBill bankBill = new BankBill();
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
		String nameReg = this.get("name");// 账户
		String billMonthReg = this.get("month");// 账单月
		String body = mailSrc.getBody();
		SimpleDateFormat format = new SimpleDateFormat("yyyy年MM月");
		// 获取账户
		Pattern pattern = Pattern.compile(nameReg);
		Matcher matcher = pattern.matcher(body);
		if (matcher.find()) {
			String group = matcher.group();
			bankBill.setName(group.replaceAll("&nbsp;", ""));
		} else {
			logger.info("未能获取账单用户名: {}", mailSrc);
			return false;
		}
		// 获取账户账单月
		pattern = Pattern.compile(billMonthReg);
		Document document = Jsoup.parse(body);
		// 获取账单月
		Elements monthElements = document.getElementsByTag("table");
		Element monthElement = DOMHelper.getMinContainer(monthElements, "账单周期");
		if (monthElement == null) {
			Element element = DOMHelper.getMinContainer(monthElements,
					"信用卡&还款日");
			String filterHtml = DOMHelper.filterHtml(element.html());
			matcher = pattern.matcher(filterHtml);
			if (matcher.find()) {
				String group = matcher.group();
				try {
					bankBill.setBillDay(format.parse(group));
				} catch (ParseException e) {
					logger.info("转为日期时报错: {}", mailSrc);
				}
			} else {
				logger.info("未能获取账单月: {}", mailSrc);
				return false;
			}
		} else {
			monthElements = monthElement.getElementsByTag("td");
			String sMonth = monthElements.get(3).text();
			sMonth = sMonth.split("-")[1];
			SimpleDateFormat format1 = new SimpleDateFormat("yyyy/MM/dd");
			try {
				bankBill.setBillDay(format1.parse(sMonth));
			} catch (ParseException e1) {
				logger.info("账单月转化失败: {}", mailSrc);
				return false;
			}
		}
		bankBill.setIntegral(-1L);
		bankBill.setId(this.getCard().getId());
		bankBill.setAlone(false);
		bankBill.setTpl(this);
		return true;
	}

	@Override
	public boolean check() {
		if (StringUtils.isEmpty(bankBill.getName())
				|| bankBill.getBillDay() == null) {
			logger.info("账户或账单日期为空: {}", mailSrc);
			return false;
		}
		this.getContext().getBills().add(bankBill);
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
