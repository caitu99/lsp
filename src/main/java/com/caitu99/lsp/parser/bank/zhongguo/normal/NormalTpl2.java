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
public class NormalTpl2 extends BaseTpl {

	private final static Logger logger = LoggerFactory
			.getLogger(NormalTpl2.class);

	private List<Bill> billList = new ArrayList<>();

	private MailSrc mailSrc;

	public NormalTpl2() {
		super(null);
	}

	public NormalTpl2(ParserContext context) {
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
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
		String account = null;
		Date billMonth = null;
		Long integral = null;
		String cardNo = null;
		Map<String, Object> others = new HashMap<>();
		try {
			// 获取卡号
			Elements cardNoElements = document.getElementsByTag("table");
			Element cardNoElement = DOMHelper.getMinContainer(cardNoElements,
					"信用卡号&账单日期&到期还款日").child(0);
			if (cardNoElement == null) {
				logger.info("获取卡号失败: {}", mailSrc);
				return false;
			}
			cardNo = cardNoElement.child(0).child(1).text().trim();
			cardNo = cardNo.substring(cardNo.length() - 4);
			// 获取账单日期
			String sBillMonth = cardNoElement.child(1).child(1).text().trim();
			// try {
			//
			// } catch (ParseException e) {
			// logger.info("账单日期转换错误: {}", mailSrc);
			// return false;
			// }
			billMonth = format.parse(sBillMonth);
			// 获取账户
			Element accountElement = cardNoElement.parent().parent().parent()
					.parent().parent().parent().previousElementSibling()
					.child(0).child(0).child(4);
			if (accountElement == null) {
				logger.info("获取账户失败: {}", mailSrc);
				return false;
			}
			account = accountElement.text().replaceAll(" ", "")
					.replace(" ", "").replaceAll("女士", "").replaceAll("先生", "");
			// 获取积分数
			Elements integralElements = document.getElementsByTag("table");
			Element integralElement = DOMHelper.getMinContainer(
					integralElements, "总计&序号&上月积分余额&本期累计积分&本期赢取积分").child(0);
			String sIntegral = integralElement.children().last()
					.previousElementSibling().child(5).text();
			sIntegral = XStringUtil.toNumber(sIntegral);
			if (StringUtils.isEmpty(sIntegral))
				integral = -1L;
			else
				integral = Long.parseLong(sIntegral);
			BankBill bankCard = new BankBill();
			bankCard.setName(account);
			bankCard.setAlone(true);
			bankCard.setBillDay(billMonth);
			bankCard.setCardNo(cardNo);
			bankCard.setIntegral(integral);
			bankCard.setOthers(others);
			bankCard.setTpl(this);
			bankCard.setId(this.getCard().getId());
			billList.add(bankCard);
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
