package com.caitu99.lsp.parser.bank.guangda.normal;

import com.caitu99.lsp.model.parser.ParserContext;
import com.caitu99.lsp.model.parser.bank.BankBill;
import com.caitu99.lsp.model.spider.MailSrc;
import com.caitu99.lsp.parser.Constants;
import com.caitu99.lsp.parser.Template;
import com.caitu99.lsp.parser.utils.DOMHelper;
import com.caitu99.lsp.parser.utils.TplHelper;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Lion on 2015/12/23 0023.
 */
@Template("guangda.normal.tpl1")
public class NormalTpl1 extends BaseTpl {
    private final static Logger logger = LoggerFactory.getLogger(NormalTpl1.class);
    private MailSrc mailSrc;
    private String name;
    private String billMonth;   //账单月
    private String paymentdate; //还款日
    private AccountSummary accountSummary = new AccountSummary();
    private List<Account> accountList = new ArrayList<>();


    public NormalTpl1() {
        super(null);
    }

    public NormalTpl1(ParserContext context) {
        super(context);
    }

    @Override
    public boolean parse() {
        String body = mailSrc.getBody();
        Document document = Jsoup.parse(body);
        Elements tables = document.getElementsByTag("table");
        Element tableTop = DOMHelper.getMaxContainer(tables,"感谢您使用中国光大银行信用卡");
        if (tableTop == null) {
            logger.info("未能解析邮件，{}", mailSrc);
            return false;
        }
        tables = tableTop.getElementsByTag("table");
        Element tableHead = DOMHelper.getMinContainer(tables, "感谢您使用中国光大银行信用卡");
        String str = tableHead.text().trim().replaceAll(" ", "").replaceAll("\u00A0","");
        //姓名
        Pattern pattern = Pattern.compile(get("name"));
        Matcher matcher = pattern.matcher(str);
        if (!matcher.find()) {
            logger.info("未能解析到用户名,", mailSrc);
            return false;
        }
        name = matcher.group();

        //账单月
        pattern = Pattern.compile("(?<=至)\\d{4}年\\d{2}月\\d{2}日");
        matcher = pattern.matcher(str);
        if (!matcher.find()) {
            logger.info("未能解析到账单月,{}", mailSrc);
            return false;
        }
        billMonth = matcher.group();

        //还款日
        pattern = Pattern.compile("(?<=最晚于)\\d{4}年\\d{2}月\\d{2}日(?=还款)");
        matcher = pattern.matcher(str);
        if (!matcher.find()) {
            logger.info("未能解析到还款日,{}", mailSrc);
        }
        paymentdate = matcher.group();

        Element table = DOMHelper.getMinContainer(tables, "到期还款日&积分余额");
        if (table == null) {
            logger.info("获取积分信息失败，{}", mailSrc);
            return false;
        }
        Elements trs = table.getElementsByTag("tr");
        if (trs.size() != 3) {
            logger.info("获取积分信息失败，{}", mailSrc);
            return false;
        }
        Elements tds = trs.get(1).getElementsByTag("td");
        if (tds.size() != 6) {
            logger.info("获取积分信息失败，{}", mailSrc);
            return false;
        }
        accountSummary.statementDate = tds.get(0).text().trim();
        accountSummary.paymentDueDate = tds.get(1).text().trim();
        accountSummary.creditLimit = tds.get(2).text().trim();
        accountSummary.currentAmountDue = tds.get(3).text().trim();
        accountSummary.minimumAmountDue = tds.get(4).text().trim();
        accountSummary.rewardsPointsBalance = tds.get(5).text().trim();

        //get card
        Element element = DOMHelper.getMinContainer(tables, "账户&本期余额&本期应还款额");
        trs = element.getElementsByTag("tr");
        if (trs.size() < 3) {
            logger.info("解析卡号失败，{}", mailSrc);
            return false;
        }
        int cnt = trs.size() - 2;
        for (int x = 0; x < cnt; x++) {
            tds = trs.get(x + 1).getElementsByTag("td");
            if (tds.size() != 5) {
                logger.info("卡号解析失败，{}", mailSrc);
                return false;
            }
            Account account = new Account();
            account.accountNumber = tds.get(0).text().trim();
            account.availableCredit = tds.get(2).text().trim();
            account.statementBalance = tds.get(3).text().trim();
            account.minimumPaymentDue = tds.get(4).text().trim();
            accountList.add(account);
        }
        return true;

    }

    @Override
    public boolean check() {
        if (name == null || billMonth == null || paymentdate == null || accountList.size() == 0) {
            logger.info("check failure， {}", mailSrc);
            return false;
        }
        for (Account account : accountList) {
            BankBill bankBill = new BankBill();
            bankBill.setTpl(this);
            String cardNoStr = account.accountNumber;
            bankBill.setCardNo(cardNoStr.substring(cardNoStr.length() - 4));
            bankBill.setName(name);
            bankBill.setId(this.getCard().getId());
            bankBill.setAlone(false);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy年MM月dd日");
            //账单月
            try {
                bankBill.setBillDay(sdf.parse(billMonth));
            } catch (ParseException e) {
                logger.info("账单月转换失败，原格式：yyyy年MM月dd日,当前数据：{},{}", billMonth,mailSrc);
            }
            //积分
            bankBill.setIntegral(Long.valueOf(accountSummary.rewardsPointsBalance));
            Map<String, Object> map = new HashMap<>();
            //信用额度
            map.put(Constants.REPAY_CREDIT, accountSummary.creditLimit.replaceAll("￥", "").replaceAll(",", ""));
            //还款日
            map.put(Constants.REPAY_DATE, accountSummary.paymentDueDate);
            //账单日
            map.put(Constants.BILL_GENERATE, accountSummary.statementDate);
            bankBill.setOthers(map);
            this.getContext().getBills().add(bankBill);
        }


        return true;
    }

    @Override
    public boolean is() {
        return false;
    }

    @Override
    public MailSrc getMailSrc() {
        return mailSrc;
    }

    @Override
    public void setMailSrc(MailSrc mailSrc) {
        this.mailSrc = mailSrc;
    }

    //    @Override
//    public ICard getCard() {
//        return super.getCard();
//    }
//
//    @Override
//    public void setCard(ICard card) {
//        super.setCard(card);
//    }
//
    @Override
    public String getName() {
        return TplHelper.getTplName(this.getClass());
    }

    class AccountSummary {
        public String statementDate;        //账单日
        public String paymentDueDate;       //到期还款日
        public String creditLimit;          //信用额度(人民币)
        public String currentAmountDue;     //人民币本期应还款额
        public String minimumAmountDue;     //人民币最低还款额
        public String rewardsPointsBalance; //积分余额
    }

    class Account {
        public String accountNumber;
        public String availableCredit;
        public String statementBalance;
        public String minimumPaymentDue;
    }
}
