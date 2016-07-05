package com.caitu99.lsp.parser.bank.nongye.normal;

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
 * Created by Administrator on 2015/12/20.
 */
@Template("nongye.normal.tpl2")
public class NormalTpl2 extends BaseTpl {
    private final static Logger logger = LoggerFactory.getLogger(NormalTpl2.class);
    private String name;
    private String cardNo;
    private String billDate;
    private String paymentDueDate;
    private String integral;
    private MailSrc mailSrc;

    public NormalTpl2() {
        super(null);
    }

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
        //找姓名
        Element tableName = DOMHelper.getMinContainer(tables, get("mailFlag"));
        if (tableName == null) {
            logger.info("未能解析到姓名,{}", mailSrc);
            return false;
        }
        String nameStr = tableName.text().trim();
        Pattern pattern = Pattern.compile(get("nameReg"));
        Matcher matcher = pattern.matcher(nameStr);
        if (!matcher.find()) {
            logger.info("未能解析到姓名,{}", mailSrc);
            return false;
        }
        name = matcher.group();

        //获取卡号,账单日，还款日
        Element table = DOMHelper.getMinContainer(tables, "到期还款日&账单日&卡号");
        if (table == null) {
            logger.info("邮件格式发生了变化,未解析到卡号,账单日，还款日，{}", mailSrc);
            return false;
        }

        Elements tds = table.getElementsByTag("td");
        if (tds.size() != 7) {
            logger.info("邮件格式发生了变化，未能解析到卡号,{}", mailSrc);
            return false;
        }
        cardNo = tds.get(2).text().trim();
        billDate = tds.get(4).text().trim();
        paymentDueDate = tds.get(6).text().trim();

        //积分
        table = DOMHelper.getMinContainer(tables, "本期积分余额");
        Element tbody = null;
        try {
            tbody = table.parent().parent().parent().parent().parent().parent().parent().parent().parent().parent().parent().parent();
        } catch (Exception e) {
            logger.info("邮件格式发生了变化，未能解析到积分,{}", mailSrc);
            return false;
        }
        if (tbody == null) {
            logger.info("邮件格式发生了变化，未能解析到积分,{}", mailSrc);
            return false;
        }

        if (tbody.children().size() != 2) {
            logger.info("邮件格式发生了变化，未能解析到积分,{}", mailSrc);
            return false;
        }
        Element tr = tbody.child(1);
        tables = tr.getElementsByTag("table");
        if (tables.size() != 3) {
            logger.info("未能解析到积分,{}", mailSrc);
            return false;
        }
        try {
            integral = tables.get(2).child(0).child(0).child(3).text().trim();
        } catch (Exception e) {
            logger.info("未能解析到积分,{}", mailSrc);
            return false;
        }
        return true;
    }

    @Override
    public boolean check() {
        if (name == null || cardNo == null || billDate == null || integral == null) {
            logger.info("check failure!,{}", mailSrc);
            return false;
        }
        BankBill bankBill = new BankBill();
        bankBill.setTpl(this);
        bankBill.setAlone(false);
        bankBill.setName(name);
        bankBill.setId(this.getCard().getId());
        bankBill.setCardNo(cardNo.substring(cardNo.length() - 4));
        bankBill.setIntegral(Long.valueOf(integral));
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");

        try {
            bankBill.setBillDay(sdf.parse(billDate));
        } catch (ParseException e) {
            logger.info("未能解析到账单月，数据出错：{},{}", billDate, mailSrc);
            return false;
        }
        Map<String, Object> map = new HashMap<>();

        try {
            map.put(Constants.REPAY_DATE, sdf.parse(paymentDueDate));
        } catch (ParseException e) {
            logger.info("未能转换还款日, 可解析格式：yyyyMMdd,当前数据：{},{}", paymentDueDate, mailSrc);
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
