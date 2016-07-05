package com.caitu99.lsp.parser.bank.jiaotong.normal;

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
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Lion on 2015/12/19 0019.
 */
@Template("jiaotong.normal.tpl2")
public class NormalTpl2 extends BaseTpl{
    private final static Logger logger = LoggerFactory.getLogger(NormalTpl2.class);

    private String name;
    private String billFrom;
    private String billTo;
    private String billMonth;
    private String cardNo;
    private String paymentDueDate;
    private String limited;
    private String cashLimited;     //取现额度
    private String integral_cny;    //人民币积分
    private String integral_doller; //美元积分
    private MailSrc mailSrc;

    public NormalTpl2(){super(null);}
    public NormalTpl2(ParserContext context) {
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

        Elements tables = doc.getElementsByTag("table");
        Element tableTop = DOMHelper.getMinContainer(tables, "感谢您使用交通银行信用卡&账单周期");
        if (tableTop == null) {
            logger.info("未能解析账单，{}", mailSrc.getId());
            return false;
        }

        Element trCont = null;
        try {
            trCont = tableTop.child(0).child(1);
        } catch (Exception e) {
            logger.info("未能解析账单，{}", mailSrc.getId());
            return false;
        }
        if (trCont == null) {
            logger.info("未能解析账单，{}", mailSrc.getId());
            return false;
        }
        Elements divs = null;
        try {
            divs = trCont.child(0).child(0).child(0).children();
        }
        catch (Exception e) {
            logger.info("未能解析账单，{}", mailSrc.getId());
            return false;
        }
        if (divs== null || divs.size() < 8) {
            logger.info("未能解析账单，{}", mailSrc.getId());
            return false;
        }

        Element div_titlemsg = divs.get(0);
        if (div_titlemsg == null) {
            logger.info("未能解析账单，{}", mailSrc.getId());
            return false;
        }
        Elements ps = div_titlemsg.getElementsByTag("p");
        String titleName =ps.get(0).text().trim().replaceAll("\u00A0","");
        Pattern pattern = Pattern.compile(get("nameReg"));
        Matcher matcher = pattern.matcher(titleName);
        if (!matcher.find()) {
            logger.info("未能解析到用户名,{}", mailSrc);
            return false;
        }
        name =matcher.group();

        //from
        String billPeriod =ps.get(3).text().trim();
        Pattern patternFrom = Pattern.compile("\\d{4}/\\d{1,2}/\\d{1,2}(?=-)");
        Matcher matcherFrom = patternFrom.matcher(billPeriod);
        if (!matcherFrom.find()) {
            logger.info("未能解析到账单起始时间,{}", mailSrc);
        }
        else
        {
            billFrom = matcherFrom.group();
        }
        //to
        Pattern patternTo = Pattern.compile("(?<=-)\\d{4}/\\d{1,2}/\\d{1,2}");
        Matcher matcherTo = patternTo.matcher(billPeriod);
        if (!matcherTo.find()) {
            logger.info("未能解析到账单结束时间,{}", mailSrc);
            return false;
        }
        billTo = matcherTo.group();

        //卡号
        String cardNoText = ps.get(2).text().trim();
        pattern = Pattern.compile("(?<=卡号:).*");
        matcher = pattern.matcher(cardNoText);
        if (!matcher.find()) {
            logger.info("未能解析到卡号,{}", mailSrc);
            return false;
        }
        cardNo = matcher.group();

        //到期还款日 信用额度 取现额度
        Element billInfo1 = divs.get(4);
        Elements trs = billInfo1.getElementsByTag("tr");
        if(trs.size()!=5)
        {
            logger.info("未能解析到还款日与额度信息,{}", mailSrc);
        }
        else {

            Elements tds = trs.get(0).getElementsByTag("td");
            if(tds.size() !=2)
            {
                logger.info("未能解析到还款日,{}", mailSrc);
            }
            else {
                paymentDueDate = tds.get(0).text().trim();
            }

            tds = trs.get(3).getElementsByTag("td");
            if(tds.size() !=3)
            {
                logger.info("未能解析到信用额度,{}", mailSrc);
            }
            else {
                limited = tds.get(0).text().trim();
            }

            tds = trs.get(4).getElementsByTag("td");
            if(tds.size() !=2)
            {
                logger.info("未能解析到取现额度,{}", mailSrc);
            }
            else {
                cashLimited = tds.get(0).text().trim();
            }
        }
        //取积分
        Element billInfo20 = divs.get(8);
        tables = billInfo20.getElementsByTag("table");
        trs = tables.get(0).getElementsByTag("tr");
        if (trs.size() != 3) {
            logger.info("未能解析到积分信息，格式发生了变化,{}", mailSrc);
            return false;
        }
        Elements tds = trs.get(1).getElementsByTag("td");
        if (tds.size() != 5) {
            logger.info("未能解析到积分信息，格式发生了变化,{}", mailSrc);
            return false;
        }
        integral_cny = tds.get(1).text().trim();
         tds = trs.get(2).getElementsByTag("td");
        if (tds.size() != 5) {
            logger.info("未能解析到积分信息，格式发生了变化,{}", mailSrc);
            return false;
        }
        integral_doller = tds.get(1).text().trim();

        return true;
    }

    @Override
    public boolean check() {
        if (name == null || billTo == null || cardNo == null || integral_cny == null) {
            logger.info("check fails,{}",mailSrc);
            return false;
        }

        BankBill bankBill = new BankBill();
        bankBill.setTpl(this);
        bankBill.setAlone(true);
        bankBill.setName(name);
        bankBill.setId(this.getCard().getId());
        bankBill.setCardNo(cardNo.substring(cardNo.length()-4));
        Long cny  = 0L;
        try {
            cny = Long.valueOf(integral_cny);
        } catch (Exception e) {
            logger.info("人民币积分值转换发生异常,待转换积分值：{}",integral_cny);
        }
        Long doller = 0L;
        try {
            doller = Long.valueOf(integral_doller);
        } catch (Exception e) {
            logger.info("美元积分值转换发生异常，待转换积分值：{}",integral_doller);
        }
        bankBill.setIntegral(cny + doller);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
        try {
            bankBill.setBillDay(sdf.parse(billTo));
        } catch (ParseException e) {
            logger.info("未能转换账单月,原数据格式：yyyy/MM/dd，当前数据：{},{}", billTo, mailSrc);
            return  false;
        }
        Map<String, Object> map = new HashMap<>();
        map.put(Constants.REPAY_CREDIT, limited);
        map.put(Constants.REPAY_CASH_CREDIT, cashLimited);

        try {
            map.put(Constants.REPAY_DATE, sdf.parse(paymentDueDate));
        } catch (ParseException e) {
            logger.error("还款日发生了变化，未能转换:{},{}", paymentDueDate,mailSrc);
        }
        bankBill.setOthers(map);
        this.getContext().getBills().add(bankBill);
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
