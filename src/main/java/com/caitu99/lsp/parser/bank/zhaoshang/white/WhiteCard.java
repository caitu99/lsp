package com.caitu99.lsp.parser.bank.zhaoshang.white;

import java.util.ArrayList;
import java.util.List;

import com.caitu99.lsp.AppConfig;
import com.caitu99.lsp.model.parser.Bill;
import com.caitu99.lsp.model.parser.BillType;
import com.caitu99.lsp.model.parser.bank.BankBill;
import com.caitu99.lsp.mongodb.model.MGBill;
import com.caitu99.lsp.mongodb.parser.MGBillService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.caitu99.lsp.parser.ICard;
import com.caitu99.lsp.parser.bank.zhaoshang.Zhaoshang;
import com.caitu99.lsp.parser.utils.BillHelper;
import com.caitu99.lsp.utils.SpringContext;

@Service("ZhaoshangWhiteCard")
public class WhiteCard extends Zhaoshang implements ICard {
	
	@Autowired
    private AppConfig appConfig;

	@Value("${zhaoshang.white.id}")
	private Integer id;

	@Value("${zhaoshang.white.name}")
	private String name;

	@Value("${zhaoshang.white.bill.type}")
	private int billType;

	@Value("${zhaoshang.white.mail.key}")
	private String titleKey;

	@Value("${zhaoshang.white.mail.sender}")
	private String mailSender;

	@Value("${zhaoshang.white.history.bill}")
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

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public BillType getBillType() {
		return BillType.valueOf(billType);
	}

	@Override
	public boolean is(String title, String sender) {
		if (appConfig.getCheckSender())
			return title.contains(titleKey) && checkSender(sender);//mailSender.equals(sender);
		else
			return title.contains(titleKey);
	}
	/**
	 * 校验发送者
	 * 	
	 * @Description: (方法职责详细描述,可空)  
	 * @Title: checkSender 
	 * @param sender
	 * @return
	 * @date 2016年4月21日 上午11:13:03  
	 * @author ws
	 */
	private boolean checkSender(String sender){
		String[] mailSenders = mailSender.split(",,");
		for (String senderSingle : mailSenders) {
			if(sender.contains(senderSingle)){
				return true;
			}
		}
		
		return false;
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
		if (mgBillDB != null
				&& bankBill.getBillDay().getTime() <= mgBillDB.getDate()
						.getTime())
			return;

		// 插入新的账单
		MGBill newMGBill = BillHelper.createBankBill(bankBill, this,
				this.getBank());
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
