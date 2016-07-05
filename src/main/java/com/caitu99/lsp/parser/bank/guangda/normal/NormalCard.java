package com.caitu99.lsp.parser.bank.guangda.normal;

import com.caitu99.lsp.AppConfig;
import com.caitu99.lsp.model.parser.Bill;
import com.caitu99.lsp.model.parser.BillType;
import com.caitu99.lsp.model.parser.bank.BankBill;
import com.caitu99.lsp.mongodb.model.MGBill;
import com.caitu99.lsp.mongodb.parser.MGBillService;
import com.caitu99.lsp.parser.ICard;
import com.caitu99.lsp.parser.bank.guangda.Guangda;
import com.caitu99.lsp.parser.utils.BillHelper;
import com.caitu99.lsp.utils.SpringContext;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Lion on 2015/12/23 0023.
 */
@Service("GuangdaNormalCard")
public class NormalCard extends Guangda implements ICard {
	
	@Autowired
    private AppConfig appConfig;

    @Value("${guangda.normal.id}")
    private Integer id;

    @Value("${guangda.normal.name}")
    private String name;

    @Value("${guangda.normal.bill.type}")
    private int billType;

    @Value("${guangda.normal.mail.key}")
    private String titleKey;

    @Value("${guangda.normal.mail.sender}")
    private String mailSender;

    @Value("${guangda.normal.history.bill}")
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

    @Override
    public boolean is(String title, String sender) {
        if (appConfig.getCheckSender())
            return title.contains(titleKey) && checkSender(sender);//mailSender.equals(sender);
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
        if (mgBillDB != null
                && bankBill.getBillDay().getTime() <= mgBillDB.getDate()
                .getTime())
            return;

        // 插入新的账单
        MGBill newMGBill = BillHelper.createBankBill(bankBill, this,
                this.getBank());
        mgBillService.insert(newMGBill);
    }


    public void setId(Integer id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setBillType(int billType) {
        this.billType = billType;
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
}
