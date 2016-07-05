package com.caitu99.lsp.parser.bank.pufa.normal;

import com.caitu99.lsp.model.parser.Bill;
import com.caitu99.lsp.model.parser.ParserContext;
import com.caitu99.lsp.model.parser.bank.BankBill;
import com.caitu99.lsp.model.spider.MailSrc;
import com.caitu99.lsp.parser.Template;
import com.caitu99.lsp.parser.utils.DOMHelper;
import com.caitu99.lsp.parser.utils.TplHelper;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;
import org.jsoup.nodes.Document;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by chenhl on 2015/12/29.
 */
@Template("pufa.normal.tpl3")
public class NormalTpl3 extends BaseTpl{

    private final static Logger logger = LoggerFactory
            .getLogger(NormalTpl3.class);

    private String account = null; // 用户名
    private String card_no = null; // 卡号
    private Date bill_month = null; // 账单月
    private Float repay_amount = null; // 本期应还总额
    private Float repay_min = null; // 本期最低还款总额
    private Long integral_balance = null; // 积分
    private Map<String, Object> others = new HashMap<>();
    private List<BankBill> billList = new ArrayList<>();
    private MailSrc mailSrc;

    public NormalTpl3(){
        super(null);
    }

    public NormalTpl3(ParserContext context) {
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
        BankBill billCard = new BankBill();
        String body = mailSrc.getBody();
        Document document = Jsoup.parse(body);
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");

        Elements elements_td = document.getElementsByTag("td");
        Elements elements_tr = document.getElementsByTag("tr");

        // 获取账单月 bill_month
        try {
            Element billMonthInfo = DOMHelper.getMinContainer(elements_td, "年&月");
            Elements billMonthInfoEl = billMonthInfo.getElementsByTag("td");
            String pre_bill_month = billMonthInfoEl.get(0).text().trim();
            pre_bill_month = pre_bill_month.replace("年", "-").replace("(","").replace(")","").replace("月","") + "-01";
            bill_month = format.parse(pre_bill_month);
        } catch (Exception e) {
            logger.info("未能获取账单-浦发银行-账单月: {}", mailSrc, e);
            return false;
        }

        // 获取account
        try {
            Element accountInfo = DOMHelper.getMinContainer(elements_td, "尊敬的");
            Elements accountInfoEl = accountInfo.getElementsByTag("b");
            String pre_account = accountInfoEl.get(0).text().replace("尊敬的", "").replaceAll(" ", "").trim();
            int index = pre_account.contains("女士") ? pre_account.indexOf("女士")
                    : pre_account.indexOf("先生");
            account = pre_account.substring(0, index);
        } catch (Exception e) {
            logger.info("未能获取账单-浦发银行-用户名: {}", mailSrc, e);
            return false;
        }

        //获取integral_balance
        try{
            Element integralInfo = DOMHelper.getMinContainer(elements_tr, "本期积分余额");
            Elements integralInfoEl = integralInfo.getElementsByTag("td");
            String pre_integral = integralInfoEl.get(1).text().trim().replaceAll(",","");
            integral_balance = Long.parseLong(pre_integral);
        }catch(Exception e){
            logger.info("未能获取账单-浦发银行-积分: {}", mailSrc, e);
            return false;
        }

        //获取card_no
        try {
            Element cardNoInfo = DOMHelper.getMinContainer(elements_td, "浦发信用卡尾号");
            Elements cardNoInfoEl = cardNoInfo.getElementsByTag("font");
            String pre_card_no = cardNoInfoEl.get(0).text().trim();
            card_no = pre_card_no.substring( pre_card_no.indexOf("卡尾号为") + 4 , pre_card_no.indexOf("电子对账单") );
        } catch (Exception e) {
            logger.info("未能获取账单-浦发银行-用户名: {}", mailSrc, e);
            return false;
        }

        //获取repay_amount
        try {
            Element repayAmountInfo = DOMHelper.getMinContainer(elements_tr, "本期应还款总额");
            Elements repayAmountInfoEl = repayAmountInfo.getElementsByTag("td");
            String pre_repay_amount = repayAmountInfoEl.get(1).text().trim().replaceAll(",", "");  //RMB:6,974.79
            pre_repay_amount = pre_repay_amount.substring(pre_repay_amount.indexOf("RMB:") + 4
                    , pre_repay_amount.length()).replaceAll(",","");
            repay_amount = Float.parseFloat(pre_repay_amount);
        } catch (Exception e) {
            logger.info("未能获取账单-浦发银行-应还款总额: {}", mailSrc, e);
            return false;
        }

        //获取repay_min
        try {
            Element repayMinInfo = DOMHelper.getMinContainer(elements_tr, "本期最低还款额");
            Elements repayMinInfoEl = repayMinInfo.getElementsByTag("td");
            String pre_repay_min = repayMinInfoEl.get(1).text().trim().replaceAll(",", ""); //RMB:3,202.11
            pre_repay_min = pre_repay_min.substring(pre_repay_min.indexOf("RMB:") + 4
                    , pre_repay_min.length()).replaceAll(",", "");
            repay_min = Float.parseFloat(pre_repay_min);
        } catch (Exception e) {
            logger.info("未能获取账单-浦发银行-最低还款额: {}", mailSrc, e);
            return false;
        }

        //获取integral_balance
        try {
            Element integarInfo = DOMHelper.getMinContainer(elements_tr, "本期积分余额");
            Elements integarInfoEl = integarInfo.getElementsByTag("td");
            String pre_integral_balance = integarInfoEl.get(1).text().trim().replaceAll(",", ""); //17,936
            pre_integral_balance = pre_integral_balance.replaceAll(",", "");
            repay_min = Float.parseFloat(pre_integral_balance);
        } catch (Exception e) {
            logger.info("未能获取账单-浦发银行-积分: {}", mailSrc, e);
        }

        others.put("repay_amount", repay_amount);
        others.put("repay_min", repay_min);

        billCard.setIntegral(integral_balance);
        billCard.setName(account);
        billCard.setCardNo(card_no);
        billCard.setBillDay(bill_month);
        billCard.setOthers(others);
        billCard.setTpl(this);
        billCard.setId(this.getCard().getId());
        billCard.setAlone(true);
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
