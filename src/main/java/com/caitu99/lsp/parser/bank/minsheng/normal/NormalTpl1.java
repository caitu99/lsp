package com.caitu99.lsp.parser.bank.minsheng.normal;

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
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Lion on 2015/12/19 0019.
 */
@Template("minsheng.normal.tpl1")
public class NormalTpl1 extends BaseTpl {
    private final static Logger logger = LoggerFactory.getLogger(NormalTpl1.class);

    private String name;
    private String billMonth;
    private String statementDate;//账单日
    private String paymentDueDate;//还款日
    private String integral;
    private Set<String> set = new HashSet<>();
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

        Elements tablesAll = doc.getElementsByTag("table");
        Element tableTop = DOMHelper.getMaxContainer(tablesAll,"您的信用卡账户信息&本期账单日");
        if (tableTop == null||tableTop.child(0).children().size()!=15) {
            logger.info("未能解析账单,{}",mailSrc);
            return false;
        }
        Elements trs = tableTop.child(0).children();

        //姓名
        String nameStr = trs.get(2).text().trim().replaceAll(" ","").replaceAll("\u00A0","");
        Pattern pattern = Pattern.compile(get("nameReg"));
        Matcher matcher = pattern.matcher(nameStr);
        if (!matcher.find()) {
            logger.info("未能解析到用户名，解析失败,{}", mailSrc);
            return false;
        }
        name = matcher.group();

        //账单日
        Elements tds = trs.get(4).getElementsByTag("td");
        Element td = DOMHelper.getMinContainer(tds, "本期账单日&Statement&Date");
        if (td == null) {
            logger.info("未能解析到账单日，解析失败,{}",mailSrc);
            return false;
        }
        Elements tdsTemp = td.parent().children();
        if (tdsTemp == null || tdsTemp.size() != 3) {
            logger.info("未能解析到账单日，解析失败，{}",mailSrc);
            return false;
        }
        statementDate = tdsTemp.get(2).text().trim();

        //还款日
        td = DOMHelper.getMinContainer(tds, "本期最后还款日&Payment&Due&Date");
        if (td == null) {
            logger.info("未能解析到还款日,解析失败，:{}", mailSrc);
            return false;
        }
        tdsTemp = td.parent().children();
        if (tdsTemp == null || tdsTemp.size() != 3) {
            logger.info("未能解析到还款日,解析失败,{}", mailSrc);
            return false;
        }
        paymentDueDate = tdsTemp.get(2).text().trim();

        //积分
        Elements elements = trs.get(6).getElementsByTag("table");
        Element table = DOMHelper.getMinContainer(elements, "人民币/美元账户&RMB");
        if (table == null) {
            logger.info("未能解析到积分信息，解析失败,{}", mailSrc);
            return false;
        }
        elements = table.parent().parent().parent().children();
        if (elements.size() != 5) {
            logger.info("未能解析到积分信息，解析失败，{}", mailSrc);
            return false;
        }
        integral = elements.get(4).text().trim();
        //拿卡号
        elements = trs.get(9).getElementsByTag("table");
        table = DOMHelper.getMinContainer(elements, "本期应还款金额&交易日&交易金额");
        if (table == null) {
            logger.info("未能解析到卡号，解析失败，{}", mailSrc);
            return false;
        }
        elements = table.child(0).children();
        if (elements.size() != 4) {
            logger.info("未能解析到卡片，解析失败，{}", mailSrc);
            return false;
        }
        Element tr = elements.get(3);
        try {
            tds = tr.child(0).child(0).child(0).child(0).child(1).child(0).child(0).child(0).children();
        } catch (Exception e) {
            logger.info("未能解析到卡号,{}", mailSrc);
            return false;
        }
        //拿到有多少行记录
        int cnt = tds.size();
        if (cnt < 1) {
            logger.info("未能解析到卡号,{}", mailSrc);
            return false;
        }
        for (int x = 0; x < cnt; x++) {
            tr = tds.get(x);
            try {
                tdsTemp = tr.child(0).child(0).child(0).child(0).child(1).child(0).child(0).child(0).child(0).child(0).child(0).child(0).child(0).children();
            } catch (Exception e) {
                logger.info("未能解析到卡号,{}", mailSrc);
                return false;
            }
            if (tdsTemp.size() != 6) {
                logger.info("未能解析到卡号,{}", mailSrc);
                return false;
            }
            set.add(tdsTemp.get(5).text().trim());
        }

        return true;
    }

    @Override
    public boolean check() {
        if (name == null || statementDate == null || integral == null || set.size() == 0) {
            logger.info("check failure！");
            return false;
        }
        for (String cardNo : set) {
            BankBill bankBill = new BankBill();
            bankBill.setTpl(this);
            bankBill.setAlone(false);
            bankBill.setName(name);
            bankBill.setId(this.getCard().getId());
            bankBill.setIntegral(Long.valueOf(integral));
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
            try {
                bankBill.setBillDay(sdf.parse(statementDate));
            } catch (ParseException e) {
                logger.info("账单月转换出错，原格式：yyyy年MM月,数据：{},{}", statementDate, mailSrc);
                return false;
            }
            Map<String, Object> map = new HashMap<>();
//            sdf = new SimpleDateFormat("yyyy/MM/dd");
            try {
                map.put(Constants.REPAY_DATE, sdf.parse(paymentDueDate));
            } catch (ParseException e) {
                logger.info("还款日转换出错，原格式：yyyy/MM/dd，当前数据：{},{}", paymentDueDate, mailSrc);
                return false;
            }
            try {
                map.put(Constants.BILL_GENERATE, sdf.parse(statementDate));
            } catch (ParseException e) {
                logger.info("账单日转换出错，原格式：yyyy/MM/dd，当前数据：{},{}", statementDate, mailSrc);
                return false;
            }
            bankBill.setOthers(map);

            bankBill.setCardNo(cardNo);
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
