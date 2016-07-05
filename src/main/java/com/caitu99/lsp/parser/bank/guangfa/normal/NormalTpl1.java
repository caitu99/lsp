package com.caitu99.lsp.parser.bank.guangfa.normal;

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
 * 针对广发15年11月账单模板
 */
@Template("guangfa.normal.tpl1")
public class NormalTpl1 extends BaseTpl{
    private final static org.slf4j.Logger logger = LoggerFactory.getLogger(NormalTpl1.class);

    private String  billMonth;      //账单月
    private String name;
    private String billBeginDay;
    private String billEndDay;
    private List<AccountMsg> accountMsgList = new ArrayList<AccountMsg>();     //卡号，额度，还款日，积分
    private MailSrc mailSrc;

    public NormalTpl1(){super(null);}
    public NormalTpl1(ParserContext context) {
        super(context);
    }

    @Override
    public boolean parse() {

        String body = mailSrc.getBody();
        Document doc = Jsoup.parse(body);

        Elements tablestop = doc.getElementsByTag("table");
        Element container = DOMHelper.getMaxContainer(tablestop, "账单信息如下");
        if (container == null) {
            logger.info("解析失败， {}", mailSrc);
            return false;
        }
        Elements tables = container.getElementsByTag("table");
        //获取用户名
        Pattern namePattern = Pattern.compile("(?<=尊敬的).*(?=(先生|女士))");
        Matcher matcher = namePattern.matcher(container.html());
        if (!matcher.find()) {
            logger.info("未能解析到用户名,{}",mailSrc);
            return false;
        }
        name = matcher.group();

        //账单月
        Pattern billPattern = Pattern.compile("(?<=您)\\d{4}年\\d{1,2}月(?=的账单信息)");
        Matcher billMatcher = billPattern.matcher(container.html());
        if (!billMatcher.find()) {
            logger.info("未能解析到账单月信息,{}",mailSrc);
            return false;
        }
        billMonth = billMatcher.group();

        //获取账单起始日和结束日
        Element billPeriodTbl = DOMHelper.getMinContainer(tables, "账单周期");
        if (billPeriodTbl == null) {
            logger.info("未能解析到账单周期，{}", mailSrc);
            return false;
        }
        Elements tds = billPeriodTbl.getElementsByTag("td");
        if (tds.size() != 4) {
            logger.info("未能解析到账单周期，{}", mailSrc);
            return false;
        }
        String beginToEnd = tds.get(1).text().trim().replaceAll(" ", "").replaceAll("\u00A0","");
        Pattern pattern = Pattern.compile("\\d{4}/\\d{2}/\\d{2}(?=-)");
        matcher = pattern.matcher(beginToEnd);
        if (!matcher.find()) {
            logger.info("未能解析到账单周期，{}", mailSrc);
            return false;
        }
        billBeginDay = matcher.group();
        pattern = Pattern.compile("(?<=-)\\d{4}/\\d{2}/\\d{2}");
        matcher = pattern.matcher(beginToEnd);
        if (!matcher.find()) {
            logger.info("未能解析到账单周期，{}", mailSrc);
            return false;
        }
        billEndDay = matcher.group();

        //获取卡号，额度，还款日
        Element accountTbl = DOMHelper.getMinContainer(tables, "账单周期&本期应还总额&********");
        if (accountTbl == null) {
            logger.info("未能解析到卡号，额度，还款日,{}",mailSrc);
            return false;
        }
        Elements trs = accountTbl.child(0).children();
        if (trs.size() != 5) {
            logger.info("未能解析到卡号，额度，还款日,{}",mailSrc);
            return false;
        }
        Elements fonts = trs.get(2).getElementsByTag("font");
        int fontCnt = fonts.size();

        if (fontCnt < 1 || fontCnt % 6 != 0) {
            logger.info("未能解析到卡号，额度，还款日,{}",mailSrc);
            return false;
        }
        int cnt = fontCnt / 6;

        for (int i = 0; i < cnt; i++) {
            AccountMsg accountMsg = new AccountMsg();
            accountMsg.CardNo = fonts.get(i*6).text().trim();
            accountMsg.paymentDueDate = fonts.get(i*6+3).text().trim();
            accountMsg.limit =fonts.get(i*6+5).text().trim();
            accountMsgList.add(accountMsg);
        }
        //获取积分
        Element integralTbl = DOMHelper.getMinContainer(tables, "积分按卡号汇总情况&本期余额&********");
        if(integralTbl == null)
        {
            logger.info("未能解析到积分，{}",mailSrc);
            return false;
        }
        trs = integralTbl.child(0).children();
        if (trs.size() != 2) {
            logger.info("未能解析到积分,{}",mailSrc);
            return false;
        }

        fonts = trs.get(1).getElementsByTag("font");
        fontCnt = fonts.size();

        if (fontCnt < 1 || fontCnt % 6 != 0) {
            logger.info("未能解析到积分,{}",mailSrc);
            return false;
        }

        if (cnt < 1) {
            logger.info("账单格式发生变化，未能解析到积分,{}", mailSrc);
            return false;
        } else {
            for (int i = 0; i < cnt; i++) {
                String cardNo = fonts.get(i*6).text().trim();

                AccountMsg accountMsg = null;
                for (AccountMsg accountMsg1 : accountMsgList) {
                    if (accountMsg1.CardNo != null && cardNo.length()!=0 ) {
                        if (accountMsg1.CardNo.equals(cardNo)) {
                            accountMsg = accountMsg1;
                        }
                    }
                }
                if (accountMsg != null) {
                    accountMsg.integral = fonts.get(i*6+5).text().trim();
                } else {
                    logger.info("未能解析到积分,{}", mailSrc);
                    return false;
                }
            }
        }

        return true;
    }

    class AccountMsg{
        public String CardNo;          //卡号
        public String limit;        //信用额度
        public String paymentDueDate;       //还款日
        public String integral;     // 积分
    }

    @Override
    public boolean check() {
        if (name == null || billEndDay == null || accountMsgList == null ||
                accountMsgList.size() == 0) {
            logger.info("数据校验失败，有不能为空的信息为空,{}", mailSrc);
            return false;
        }
        SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy/MM/dd");
        for (AccountMsg msg : accountMsgList) {
            BankBill bankBill = new BankBill();
            bankBill.setTpl(this);
            bankBill.setAlone(true);
            bankBill.setId(this.getCard().getId());
            bankBill.setName(name);
            try {
                bankBill.setBillDay(sdf2.parse(billEndDay));
            } catch (ParseException e) {
                logger.info("账单月转换失败,{}", mailSrc);
                return false;
            }
            if (msg.CardNo == null || msg.CardNo.length() < 4) {
                logger.info("没有卡号，数据组织失败,{}", mailSrc);
                return false;
            }
            bankBill.setCardNo(msg.CardNo.substring(msg.CardNo.length() - 4));
            bankBill.setIntegral(Long.valueOf(msg.integral));
            Map<String, Object> map = new HashMap<>();
            String limitstr = msg.limit.replaceAll(",", "");
            map.put(Constants.REPAY_CREDIT, Long.valueOf(limitstr.substring(0, limitstr.length() - 3)));
            try {
                map.put(Constants.REPAY_DATE, sdf2.parse(msg.paymentDueDate));
            } catch (ParseException e) {
                logger.info("还款日转换失败,转换格式：yyyy/MM/dd，当前字符串：{},{}", msg.paymentDueDate, mailSrc);
            }
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
}
