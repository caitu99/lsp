package com.caitu99.lsp.parser.bank.gongshang.normal;

import com.caitu99.lsp.AppConfig;
import com.caitu99.lsp.model.parser.Bill;
import com.caitu99.lsp.model.parser.BillType;
import com.caitu99.lsp.model.parser.bank.BankBill;
import com.caitu99.lsp.mongodb.model.MGBill;
import com.caitu99.lsp.mongodb.parser.MGBillService;
import com.caitu99.lsp.parser.ICard;
import com.caitu99.lsp.parser.bank.gongshang.Gongshang;
import com.caitu99.lsp.parser.utils.BillHelper;
import com.caitu99.lsp.utils.SpringContext;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service("GongshangNormalCard")
public class NormalCard extends Gongshang implements ICard {

	@Autowired
    private AppConfig appConfig;

	@Value("${gongshang.normal.id}")
	private Integer id;

	@Value("${gongshang.normal.name}")
	private String name;

	@Value("${gongshang.normal.bill.type}")
	private int billType;

	@Value("${gongshang.normal.mail.key}")
	private String titleKey;

	@Value("${gongshang.normal.mail.sender}")
	private String mailSender;

	@Value("${gongshang.normal.history.bill}")
	private boolean parseHistory;

	private List<Class> tpls = new ArrayList<>();

	@Override
	public Integer getId() {
		return id;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public BillType getBillType() {
		return BillType.valueOf(billType);
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public boolean is(String title, String sender) {
		if (appConfig.getCheckSender())
			return title.contains(titleKey) && sender.contains(mailSender);//checkSender(sender);//mailSender.equals(sender);
		else
			return title.contains(titleKey);
	}

	@Override
	public List<Class> getTpls() {
		return tpls;
	}

	@Override
	public void setTpls(List<Class> tpls) {
		this.tpls = tpls;
	}

	@Override
	public void merge(Bill curBill) {

		BankBill bankBill = (BankBill) curBill;

		MGBillService mgBillService = SpringContext
				.getBean(MGBillService.class);
		MGBill mgBill = new MGBill();
		mgBill.setName(bankBill.getName());
		mgBill.setCard(name);
		mgBill.setUserId(bankBill.getTpl().getContext().getUserId());
		MGBill mgBillDB = mgBillService.getLast(mgBill);

		// 判断是否是新账单
		if (mgBillDB != null &&
                bankBill.getBillDay().getTime() <= mgBillDB.getDate().getTime())
			return;

		// 插入新的账单
		MGBill newMGBill = BillHelper.createBankBill(bankBill, this, this.getBank());
		mgBillService.insert(newMGBill);
	}

	public String getTitleKey() {
		return titleKey;
	}

	public void setTitleKey(String titleKey) {
		this.titleKey = titleKey;
	}

	public String getMailSender() {
		return mailSender;
	}

	public void setMailSender(String mailSender) {
		this.mailSender = mailSender;
	}

	public boolean isParseHistory() {
		return parseHistory;
	}

	public void setParseHistory(boolean parseHistory) {
		this.parseHistory = parseHistory;
	}

}
