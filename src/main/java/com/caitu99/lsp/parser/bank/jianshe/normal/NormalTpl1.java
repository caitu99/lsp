package com.caitu99.lsp.parser.bank.jianshe.normal;

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
 * Created by Lion on 2015/12/15 0015.
 */
@Template("jianshe.normal.tpl1")
public class NormalTpl1  extends BaseTpl{
    private final static Logger logger = LoggerFactory.getLogger(NormalTpl1.class);

    private MailSrc mailSrc;
    private String name;

    private String billPeriodFrom;  //账单起始日
    private String billPeriodTo;    //账单结束日
    private AccountInformation accountInformation;
    private List<PointInfo> pointInfoList;  //积分信息

    public NormalTpl1(){super(null);}

    public NormalTpl1(ParserContext context) {
        super(context);
    }

    @Override
    public boolean is() {
        return false;
    }

    @Override
    public boolean parse() {
        String body = mailSrc.getBody();

        Document document = Jsoup.parse(body);
        Elements elements = document.getElementsByTag("table");
        Element tab1 = DOMHelper.getMinContainer(elements, "龙卡信用卡对账单");

        if(tab1== null)
        {
            logger.info("未能解析账单,{}",mailSrc);
            return false;
        }
        Element mailContainer = tab1.parent().parent().parent();

        //获取用户姓名
        Pattern namePattern = Pattern.compile(get("name"));
        String str =mailContainer.html();
        Matcher matcher = namePattern.matcher(mailContainer.html());
        if (matcher.find()) {
            name = matcher.group();
            logger.debug("get client name: {}", name);
        } else {
            logger.info("未能获取账单用户名,{}",mailSrc);
            return false;
        }

        //获取账单周期
        Element billPeriodEl = DOMHelper.getMinContainer(elements, "账单周期");
        if (billPeriodEl == null) {
            logger.info("未能获取账单周期,{}",mailSrc);
            return false;
        }
        billPeriodFrom = getPillPeriodFrom(billPeriodEl);
        billPeriodTo = getPillPeriodTo(billPeriodEl);
        //获取账单日，信用额度，积分余额
        Element accountEle = DOMHelper.getMinContainer(elements, "账户信息&账单日");
        if (accountEle == null) {
            logger.info("未能获取账单信息,{}",mailSrc);
            return false;
        }
        accountInformation = getAccountInfo(accountEle);

        //积分明细
        Element pointInfoEle = DOMHelper.getMinContainer(elements, "积分明细");
        if(pointInfoEle == null)
        {
            logger.info("未能获取账单信息,{}",mailSrc);
            return false;
        }
        pointInfoList = getPointInfo(pointInfoEle);

        return true;
    }

    private List<PointInfo> getPointInfo(Element pointInfoEle) {

        Elements elements = pointInfoEle.getElementsByTag("tr");
        List<PointInfo> list = new ArrayList<>();
        if (elements.size() < 3) {
            logger.info("未能获取积分信息,{}",mailSrc);
            return null;
        }
        int cardNum = elements.size() - 3;
        for (int i = 0; i < cardNum; i++) {
            Elements eletds = elements.get(i + 3).getElementsByTag("td");
            PointInfo pointInfol = new PointInfo();
            if(eletds.size()!=7)
            {
                logger.info("积分信息发生变化，未能获取信息,{}",mailSrc);
                return null;
            }
            //读取卡号
            pointInfol.cardNo = eletds.get(0).text().trim().replaceAll(" ", "").replaceAll("\\u00A0", "");

            //读取起初积分
            pointInfol.previousPoints = eletds.get(1).text().trim().replaceAll(" ", "").replaceAll("\\u00A0", "");

            //读取本期消费积分
            pointInfol.consumedPoints = eletds.get(2).text().trim().replaceAll(" ", "").replaceAll("\\u00A0", "");

            //读取本期奖励积分
            pointInfol.bonusPoints = eletds.get(3).text().trim().replaceAll(" ", "").replaceAll("\\u00A0", "");

            //读取本期调整积分
            pointInfol.adjustedPoints = eletds.get(4).text().trim().replaceAll(" ", "").replaceAll("\\u00A0", "");

            //读取兑换余额
            pointInfol.redeemedPoints = eletds.get(5).text().trim().replaceAll(" ","").replaceAll("\\u00A0","");

            //读取积分余额
            pointInfol.totalPoints = eletds.get(6).text().trim().replaceAll(" ","").replaceAll("\\u00A0","");
            list.add(pointInfol);
        }


        return list;
    }


    private AccountInformation getAccountInfo(Element accountEle) {
        AccountInformation accountInfo = new AccountInformation();
        Elements elements = accountEle.getElementsByTag("tr");
        if (elements.size() != 7) {
            logger.info("账单格式发生了变化，未能解析账单信息,{}",mailSrc);
            return null;
        }

        //账单日
        Elements eletds = elements.get(1).getElementsByTag("td");
        if (eletds.size() != 2) {
            logger.info("账单格式发生了变化，未能解析账单信息,{}",mailSrc);
            return null;
        }
        accountInfo.statementDate = eletds.get(1).text().trim().replaceAll(" ","").replaceAll("\\u00A0","");

        //到期还款日
        eletds = elements.get(2).getElementsByTag("td");
        if (eletds.size() != 2) {
            logger.info("账单格式发生了变化，未能解析账单信息,{}",mailSrc);
            return null;

        }
        accountInfo.paymentDueDate = eletds.get(1).text().trim().replaceAll(" ","").replaceAll("\\u00A0","");

        //信用额度
        eletds = elements.get(3).getElementsByTag("td");
        if (eletds.size() != 2) {
            logger.info("账单格式发生了变化，未能解析账单信息,{}",mailSrc);
            return null;
        }
        accountInfo.creditLimit = eletds.get(1).text().trim().replaceAll(" ","").replaceAll("\\u00A0","");//格式：CNY 50,000

        //取现额度
        eletds = elements.get(4).getElementsByTag("td");
        if (eletds.size() != 2) {
            logger.info("账单格式发生了变化，未能解析账单信息,{}",mailSrc);
            return null;
        }
        accountInfo.cashAdvanceLimit = eletds.get(1).text().trim().replaceAll(" ","").replaceAll("\\u00A0",""); //字符串，CNY 25,000

        //积分余额
        eletds = elements.get(5).getElementsByTag("td");
        if (eletds.size() != 2) {
            logger.info("账单格式发生了变化，未能解析账单信息,{}",mailSrc);
            return null;
        }
        accountInfo.availablePoints = eletds.get(1).text().trim();
        return accountInfo;
    }

    private String getPillPeriodFrom(Element billPeriodEl) {
        Elements elements = billPeriodEl.getElementsByTag("td");
        Element billPeriodtd = DOMHelper.getMinContainer(elements, "账单周期");
        if (billPeriodtd == null) {
            return null;
        }
        String str = billPeriodtd.text().trim();
        Pattern pattern = Pattern.compile("\\d{4}年\\d{1,2}月\\d{1,2}日(?=-)");
        Matcher matcher = pattern.matcher(str);
        if (!matcher.find()) {
            logger.info("账单月解析失败,{},{}",str,mailSrc);
            return null;
        }
        return matcher.group();

    }

    private String getPillPeriodTo(Element billPeriodEl) {
        Elements elements = billPeriodEl.getElementsByTag("td");
        Element billPeriodtd = DOMHelper.getMinContainer(elements, "账单周期");
        if (billPeriodtd == null) {
            return null;
        }
        String str = billPeriodtd.text().trim();
        Pattern pattern = Pattern.compile("(?<=-)\\d{4}年\\d{1,2}月\\d{1,2}日");
        Matcher matcher = pattern.matcher(str);
        if (!matcher.find()) {
            logger.info("账单月解析失败,{},{}",str,mailSrc);
            return null;
        }
        return matcher.group();
    }


    @Override
    public boolean check() {
        //检查姓名
        if (name == null || billPeriodFrom == null || accountInformation == null
                || pointInfoList == null || pointInfoList.size() == 0) {
            logger.info("check 失败,{}",mailSrc);
            return false;
        }
        for (PointInfo pointInfo : pointInfoList) {
            BankBill bankBill = new BankBill();
            bankBill.setTpl(this);
            bankBill.setAlone(true);
            bankBill.setId(this.getCard().getId());
            bankBill.setName(name);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy年MM月dd日");
            try {
                bankBill.setBillDay(sdf.parse(billPeriodTo));
            } catch (ParseException e) {
               logger.info("账单月转换失败,原格式：yyyy年MM月dd日，现数据：{},{}", billPeriodTo,mailSrc);
                return false;
            }
            String cardStr = pointInfo.cardNo;
            bankBill.setCardNo(cardStr.substring(cardStr.length() - 4));
            String pointStr = pointInfo.totalPoints.replaceAll(",", "");
            pointStr = pointStr.replaceAll(" ", "");
            bankBill.setIntegral(Long.valueOf(pointStr.substring(0,pointStr.length()-3)));

            Map<String, Object> map = new HashMap<>();
            SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd");
            try {
                map.put(Constants.REPAY_DATE, sdf2.parse(accountInformation.paymentDueDate));
            } catch (ParseException e) {
                logger.info("还款日转换失败，原格式：yyyy-MM-dd,现格式：{},{}",accountInformation.paymentDueDate,mailSrc);
            }
            try {
                map.put(Constants.BILL_GENERATE, sdf2.parse(accountInformation.statementDate));
            } catch (ParseException e) {
                logger.info("账单日转换失败，原格式：yyyy-MM-dd,现格式：{},{}", accountInformation.statementDate,mailSrc);
            }
            map.put(Constants.REPAY_CREDIT, accountInformation.creditLimit);
            map.put(Constants.REPAY_CASH_CREDIT, accountInformation.cashAdvanceLimit);
            bankBill.setOthers(map);
            this.getContext().getBills().add(bankBill);
        }

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


    private class PointInfo{
        public String cardNo;                   //卡号
        public String previousPoints;           //本期起初积分
        public String consumedPoints;           //本期消费积分
        public String bonusPoints;              //本期奖励积分
        public String adjustedPoints;           //本期调整积分
        public String redeemedPoints;           //本期兑换余额
        public String totalPoints;              //本期积分余额
    }

    private class AccountInformation {
        public String statementDate;        //账单日
        public String paymentDueDate;       //到期还款日
        public String creditLimit;          //信用额度
        public String cashAdvanceLimit;     //取现额度
        public String availablePoints;      //积分余额
        public String currency;             //账户币种
        public String newBalance;           //本期应还款额
    }
}
