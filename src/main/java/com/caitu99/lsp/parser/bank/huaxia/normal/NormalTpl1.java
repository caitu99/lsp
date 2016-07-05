package com.caitu99.lsp.parser.bank.huaxia.normal;

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
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Created by Lion on 2015/12/17 0017.
 */
@Template("huaxia.normal.tpl1")
public class NormalTpl1 extends BaseTpl {
    private final static Logger logger = LoggerFactory.getLogger(NormalTpl1.class);

    private String name;
    private AccountMsg accountMsg;

    private String integral;

    private List<String> cardNoList;
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
    public boolean parse() {

        String body = mailSrc.getBody();
        Document doc = Jsoup.parse(body);

        Elements elements = doc.getElementsByTag("table");
        Element container = DOMHelper.getMinContainer(elements, get("bodyflag"));
        elements = container.getElementsByTag("table");
        //获取用户名
        Element tableName = DOMHelper.getMinContainer(elements,"尊敬的");
        String nameStr = tableName.text().trim().replaceAll("\u00A0","");
        Pattern namePattern = Pattern.compile(get("name"));
        Matcher matcher = namePattern.matcher(nameStr);

        if (matcher.find()) {
            this.name = matcher.group();
            logger.debug("get client name:");
        } else {
            logger.info("未能获取账单用户名,{}",mailSrc);
            return false;
        }

        //华夏精英·尊尚白金信用卡对账单(2015年11月份)
        Element element = DOMHelper.getMinContainer(elements, get("accountMsgFlag"));
        if (element == null) {
            logger.info("未能获取账单信息,{}",mailSrc);
            return false;
        }
        accountMsg = getAccountMsg(element);

        //获取积分
        element = DOMHelper.getMinContainer(elements, get("integralFlag"));
        if (element == null) {
            logger.info("未能获取到积分信息,{}",mailSrc);
            return false;
        }
        integral = getIntegral(element);

        //获取卡号
        element = DOMHelper.getMinContainer(elements, get("cardFlag"));
        if (element == null) {
            logger.info("未能获取到卡号.{}",mailSrc);
            return false;
        }
        cardNoList = getCardNoList(element);

        return true;
    }

    private List<String> getCardNoList(Element element) {
        Set<String> set = new HashSet<>();
        Elements trs = element.getElementsByTag("tr");
        int num = trs.size();
        if (num < 3) {
            logger.info("交易明细格式发生了改变,{}",mailSrc);
            return null;
        }
        num = num - 2;
        for (int i = 0; i < num; i++) {
            Elements tds = trs.get(i + 2).getElementsByTag("td");
            String str = tds.get(4).text().trim().replaceAll(" ", "").replaceAll("\\u00A0", "");
            set.add(str);
        }
        List<String> list = new ArrayList<String>(set);
        return list;
    }

    private String getIntegral(Element element) {
        Elements elements = element.getElementsByTag("tr");
        if (elements.size() != 2) {
            logger.info("未能获取到积分信息,{}",mailSrc);
            return null;
        }
        Elements tds = elements.get(1).getElementsByTag("td");
        if (tds.size() != 11) {
            logger.info("积分信息数据格式改变,{}",mailSrc);
            return null;
        }
        return tds.get(0).text().trim().replaceAll(" ","").replaceAll("\\u00A0","");
    }

    private AccountMsg getAccountMsg(Element element) {
        AccountMsg accountMsg = new AccountMsg();
        Elements trs = element.getElementsByTag("tr");
        //获取账单月
        Element tr = DOMHelper.getMinContainer(trs, "尊尚白金信用卡对账单");
        Pattern pattern = Pattern.compile("(?<=尊尚白金信用卡对账单\\()\\d{4}年\\d{1,2}月(?=份\\))");
        Matcher matcher = pattern.matcher(tr.text().trim());
        if (!matcher.find()) {
            logger.info("未能获取到账单月,{}",mailSrc);
            return accountMsg;
        } else {
            accountMsg.billMonth = matcher.group();
        }

        //获取账单日
        accountMsg.statementDate = getMsgFromTr(trs, "账单日", "未能完成解析账单，未能获取到账单日,{}");
        //获取还款日
        accountMsg.paymentDueDate = getMsgFromTr(trs, "本期到期还款日", "未能完成解析账单，未能获取到还款日,{}");
        //信用额度
        accountMsg.creditLimit = getMsgFromTr(trs, "信用额度", "未能完成解析账单，未能获取到信用额度,{}");
        //预借现金额度
        accountMsg.cashAdvanceLimit = getMsgFromTr(trs, "预借现金额度", "未能完成解析账单，未能获取到预借现金额度,{}");

        return accountMsg;
    }

    private String getMsgFromTr(Elements trs, String keyWords, String info) {
        Element tr = DOMHelper.getMinContainer(trs, keyWords);
        if (tr == null) {
            logger.info(info,mailSrc);
            return null;
        } else {
            return tr.getElementsByTag("td").get(1).text().trim().replaceAll(" ","").replaceAll("\\u00A0","");
        }
    }

    @Override
    public boolean check() {
        if (name == null || integral == null || accountMsg == null || cardNoList.size() == 0) {
            logger.info("数据校验失败，有不能为空的信息为空,{}",mailSrc);
            return false;
        }
        for(String cardNo:cardNoList)
        {
            BankBill bankBill = new BankBill();
            bankBill.setTpl(this);
            bankBill.setAlone(false);
            bankBill.setName(name);
            bankBill.setId(this.getCard().getId());
            bankBill.setIntegral(Long.valueOf(integral.replaceAll(",", "")));
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy年MM月");

            try {
                bankBill.setBillDay(sdf.parse(accountMsg.billMonth));
            } catch (ParseException e) {
                logger.info("账单月转换失败，解析格式：yyyy年MM月，实际数据：{},{}", accountMsg.billMonth,mailSrc);
                return false;
            }
            bankBill.setCardNo(cardNo);
            Map<String, Object> map = new HashMap<>();
            map.put(Constants.BILL_GENERATE, accountMsg.statementDate);
            sdf = new SimpleDateFormat("yyyy/MM/dd");
            try {
                map.put(Constants.REPAY_DATE, sdf.parse(accountMsg.paymentDueDate));
            } catch (ParseException e) {
                logger.info("还款日转换失败，原格式：yyyy/MM/dd,当前数据：{},{}", accountMsg.paymentDueDate,mailSrc);
            }
            map.put(Constants.REPAY_CREDIT, accountMsg.creditLimit.replaceAll(",", ""));
            map.put(Constants.REPAY_CASH_CREDIT, accountMsg.cashAdvanceLimit.replaceAll(",", ""));
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

    class AccountMsg {
        public String billMonth;           //账单月
        public String statementDate;       //账单日
        public String paymentDueDate;          //本期到期还款日
        public String creditLimit;             //信用额度 （元）
        public String cashAdvanceLimit;        //预借现金额度 （元）
    }
}
