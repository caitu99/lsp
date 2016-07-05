package com.caitu99.lsp.parser.travel.yilong.normal;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.caitu99.lsp.AppConfig;
import com.caitu99.lsp.model.parser.Bill;
import com.caitu99.lsp.model.parser.BillType;
import com.caitu99.lsp.model.parser.travel.TravelBill;
import com.caitu99.lsp.model.spider.MailSrc;
import com.caitu99.lsp.mongodb.model.MGBill;
import com.caitu99.lsp.mongodb.parser.MGBillService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.caitu99.lsp.parser.ICard;
import com.caitu99.lsp.parser.travel.yilong.Yilong;
import com.caitu99.lsp.utils.SpringContext;

@Service("YilongNormalCard")
public class NormalCard extends Yilong implements ICard{
	
	@Autowired
    private AppConfig appConfig;

    @Value("${yilong.normal.id}")
    private Integer id;

	@Value("${yilong.normal.name}")
    private String name;

    @Value("${yilong.normal.bill.type}")
    private int billType;

    @Value("${yilong.normal.mail.key}")
    private String titleKey;

    @Value("${yilong.normal.mail.sender}")
    private String mailSender;

    @Value("${yilong.normal.history.bill}")
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
            return title.contains("艺龙积分");
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
    	TravelBill travelBill = (TravelBill) curBill;
    	MGBillService mgBillService = SpringContext
				.getBean(MGBillService.class);
    	MGBill mgBill = new MGBill();
    	mgBill.setName(travelBill.getName());
    	mgBill.setUserId(travelBill.getTpl().getContext().getUserId());
    	mgBill.setCard(name);
    	MGBill mgBillDB = mgBillService.getLast(mgBill);
    	
    	// 判断是否是新账单
    	if (mgBillDB != null
    					&& travelBill.getBillDay().getTime() <= mgBillDB.getDate()
    							.getTime())
    		return;
    	
    	// 插入新的账单
    	MGBill newMGBill = new MGBill();
    	MailSrc mailSrc = travelBill.getTpl().getMailSrc();
    	newMGBill.setAccount(travelBill.getTpl().getContext().getAccount());
    	newMGBill.setBank(super.getTravel());
    	newMGBill.setBody(mailSrc.getBody());
    	newMGBill.setCard(name);
    	newMGBill.setConfig(travelBill.getTpl().getConfigure());
    	newMGBill.setContextId(travelBill.getTpl().getContext().getId());
    	newMGBill.setCreated(new Date());
    	newMGBill.setDate(travelBill.getBillDay());
    	newMGBill.setIntegral(travelBill.getIntegral());
    	newMGBill.setName(travelBill.getName());
    	newMGBill.setOthers(travelBill.getOthers());
    	newMGBill.setsDate(mailSrc.getDate());
		newMGBill.setSrcId(mailSrc.getId());
		newMGBill.setStatus(1);
		newMGBill.setTitle(mailSrc.getTitle());
		newMGBill.setTpl(travelBill.getTpl().getName());
		newMGBill.setUserId(travelBill.getTpl().getContext().getUserId());
    	
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
