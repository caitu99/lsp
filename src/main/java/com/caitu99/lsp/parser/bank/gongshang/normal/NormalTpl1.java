package com.caitu99.lsp.parser.bank.gongshang.normal;

import com.caitu99.lsp.model.parser.ParserContext;
import com.caitu99.lsp.model.parser.bank.BankBill;
import com.caitu99.lsp.model.spider.MailSrc;
import com.caitu99.lsp.parser.Constants;
import com.caitu99.lsp.parser.utils.DOMHelper;
import com.caitu99.lsp.parser.Template;
import com.caitu99.lsp.parser.utils.TplHelper;
import org.apache.commons.lang.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Template("gongshang.normal.tpl1")
public class NormalTpl1 extends BaseTpl {

    private final static Logger logger = LoggerFactory.getLogger(NormalTpl1.class);

    private MailSrc mailSrc;

    private String name;
    private BillDate billDate;
    private RepayDetail repayDetail;
    private Integer balance;
    private String last4No;

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
    public void setContext(ParserContext context) {
        super.setContext(context);
    }

    @Override
    public ParserContext getContext() {
        return super.getContext();
    }

    @Override
    public boolean parse() {
        String body = mailSrc.getBody();
        body = body.replaceAll("&nbsp;", "");
        Document document = Jsoup.parse(body);
        Elements elements = document.getElementsByTag("table");
        Element mailContainer = DOMHelper.getMaxContainer(elements, get("container"));

        if (mailContainer == null) {
            logger.info("can not get main container: {}", mailSrc);
            return false;
        }

        // 获取用户姓名
        Pattern namePattern = Pattern.compile(get("name"));
        Matcher matcher = namePattern.matcher(mailContainer.html());
        if (matcher.find()) {
            this.name = matcher.group();
            logger.debug("get client name: {}", this.name);
        } else {
            logger.info("未能获取账单用户名: {}", mailSrc);
            return false;
        }

        // 获取账单周期
        Element billDateEl = DOMHelper.getMinContainer(elements, get("period"));
        if (billDateEl == null) {
            logger.info("未能获取账单周期: {}", mailSrc);
            return false;
        }
        try {
            billDate = getBillDate(billDateEl);
        } catch (Exception e) {
            logger.info("未能解析账单日期: {}", mailSrc, e);
            return false;
        }

        // 获取卡号
        String tranDetail = get("trans");
        Element tranDetailEl = DOMHelper.getMinContainer(elements, tranDetail);
        if (tranDetailEl != null) {
            last4No = getLast4No(tranDetailEl);
        } else {
            logger.info("未能获卡号: {}", mailSrc);
        }

        // 获取还款明细
        String billDetail = get("detail");
        Element repayDetailEl = DOMHelper.getMinContainer(elements, billDetail);
        if (repayDetailEl != null) {
            repayDetail = getRepayDetail(repayDetailEl);
        } else {
            logger.info("未能获取还款明细: {}", mailSrc);
        }

        // 获取个人积分
        Element personalIntegralEl = DOMHelper.getMinContainer(elements, get("integral"));
        if (personalIntegralEl == null) {
            logger.info("未能获取个人综合积分: {}", mailSrc);
            return false;
        }
        balance = getBalance(personalIntegralEl);

        return true;
    }

    @Override
    public boolean check() {
        if(this.name == null || billDate == null || last4No == null || balance == null)
            return false;

        BankBill bill = new BankBill();
        bill.setTpl(this); // set tpl
        bill.setName(name); // set name
        bill.setId(this.getCard().getId());
        bill.setTpl(this); // set tpl
        bill.setAlone(true);
        
        if(StringUtils.isBlank(last4No))
            logger.info("can not get card no: {}", mailSrc);

        bill.setCardNo(last4No); // set card no
		bill.setBillDay(billDate.billDate); // set bill date
        bill.setIntegral(Long.valueOf(balance)); // set integral

        // set other attribute
        Map<String, String> repayDetailMap = new HashMap<>();
        if (repayDetail != null) {
            repayDetailMap.put(Constants.REPAY_CARD_NO, repayDetail.last4);
            repayDetailMap.put(Constants.REPAY_MONEY_TYPE, repayDetail.mType);
            repayDetailMap.put(Constants.REPAY_AMOUNT, repayDetail.amount);
            repayDetailMap.put(Constants.REPAY_MIN, repayDetail.mAmount);
            repayDetailMap.put(Constants.REPAY_CREDIT, repayDetail.credit);
        }

        bill.getOthers().put(Constants.REPAY_DETAIL, repayDetailMap);

        this.getContext().getBills().add(bill);

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

    private BillDate getBillDate(Element element) throws Exception {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(get("date.format"));
        Pattern datePatten = Pattern.compile(get("date"));
        BillDate billDate = new BillDate();

        Elements elements = element.getElementsByTag("td");

        //获取账单周期
        Element billPeriodEl = DOMHelper.getMinContainer(elements, "周期");
        if(billPeriodEl == null)
            return null;

        Matcher billPeriodMatcher = datePatten.matcher(billPeriodEl.text());

        if (billPeriodMatcher.find()) // get the first group
            billDate.billFrom = simpleDateFormat.parse(billPeriodMatcher.group());
        else
            return null;

        if (billPeriodMatcher.find()) // get the second group
            billDate.billTo = simpleDateFormat.parse(billPeriodMatcher.group());
        else
            return null;

        //获取账单日
        Element billDateEl = DOMHelper.getMinContainer(elements, "生成日");
        if(billDateEl == null)
            return null;
        Matcher billDateMatcher = datePatten.matcher(billDateEl.text());
        if (billDateMatcher.find()) { // get the first group
            billDate.billDate = simpleDateFormat.parse(billDateMatcher.group());
        }
        return billDate;
    }

    private String getLast4No(Element element) {
        Elements elements = element.getElementsByTag("tr");
        if (elements.size() < 4) {
            logger.info("model has been changed: {}", mailSrc);
            return null;
        }

        Element tr0 = elements.get(0);
        Elements tds = tr0.getElementsByTag("td");
        Integer index = null;
        for (int i = 0; i < tds.size(); ++i) {
            if (tds.get(i).text().contains("后四位")) {
                index = i;
            }
        }

        if (index == null) {
            logger.info("model has been changed: {}", mailSrc);
            return null;
        }

        Element noTd = elements.get(2).getElementsByTag("td").get(index);
        String noStr = noTd.text();

        Pattern cardNoPattern = Pattern.compile("\\d+");
        Matcher matcher = cardNoPattern.matcher(noStr);
        if (matcher.find())
            return matcher.group();

        return null;
    }

    private RepayDetail getRepayDetail(Element element) {
        RepayDetail detail = new RepayDetail();
        Elements elements = element.getElementsByTag("tr");
        if (elements.size() < 2) {
            logger.info("model has been changed: {}", mailSrc);
            return null;
        }

        elements = elements.get(1).getElementsByTag("td");
        if (elements.size() != 5) {
            logger.info("model has been changed: {}", mailSrc);
            return null;
        }

        detail.last4 = elements.get(0).text().trim();

        Pattern cardNoPattern = Pattern.compile("\\d+");
        Matcher matcher = cardNoPattern.matcher(detail.last4);
        if (matcher.find())
            detail.last4 = matcher.group();
        else
            detail.last4 = "";

        detail.mType = elements.get(1).text().trim();
        detail.amount = elements.get(2).text().trim();
        detail.mAmount = elements.get(3).text().trim();
        detail.credit = elements.get(4).text().trim();

        return detail;
    }

    private Integer getBalance(Element element) {
        Elements elements = element.getElementsByTag("td");
        Element balanceEl = DOMHelper.getMinContainer(elements, "余额");
        if (balanceEl == null) {
            logger.error("cannot get balance: {}", mailSrc);
            return null;
        }

        String balanceStr = balanceEl.text();
        balanceStr = balanceStr.replaceAll(",", "");

        Pattern cardNoPattern = Pattern.compile("\\d+");
        Matcher matcher = cardNoPattern.matcher(balanceStr);

        if (matcher.find()) {
            return Integer.valueOf(matcher.group());
        }

        return null;
    }

    private class BillDate {
        public Date billFrom;
        public Date billTo;
        public Date billDate;
    }

    private class RepayDetail {
        public String last4;
        public String mType;
        public String amount;
        public String mAmount;
        public String credit;
    }

}
