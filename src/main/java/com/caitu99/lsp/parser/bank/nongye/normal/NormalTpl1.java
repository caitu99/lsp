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
@Template("nongye.normal.tpl1")
public class NormalTpl1 extends BaseTpl {
    private final static Logger logger = LoggerFactory.getLogger(NormalTpl1.class);
    private String name;
    private String cardNo;          //卡号
    private String billDate;        //账单日
    private String paymentDueDate;  //还款日
    private String integral;        //积分
    private String creditLimited;       //额度
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

        Elements divs = doc.getElementsByTag("div");
        //找姓名
        Element divname = DOMHelper.getMinContainer(divs, get("mailFlag"));
        if (divname == null) {
            logger.info("未能解析到姓名,{}",mailSrc);
            return false;
        }
        String nameStr = divname.text().trim();
        Pattern pattern = Pattern.compile(get("nameReg"));
        Matcher matcher = pattern.matcher(nameStr);
        if (!matcher.find()) {
            logger.info("未能解析到姓名,{}",mailSrc);
            return false;
        }
        name = matcher.group();

        //获取账单日，信用额度
        Elements tables = doc.getElementsByTag("table");
        Element table = DOMHelper.getMinContainer(tables, "信用卡账户信息");
        Element tabtop = table.parent().parent().parent().parent()
                .parent().parent().parent().parent()
                .parent().parent().parent().parent();
        if (tabtop.children().size() != 2) {
            logger.info("邮件格式发生了变化,{}",mailSrc);
            return false;
        }
        Elements fonts = tabtop.child(0).getElementsByTag("font");
        if (fonts.size() != 15) {
            logger.info("邮件格式发生了变化，未能解析到账单日，信用度,{}",mailSrc);
        }
        billDate = fonts.get(2).text().trim();
        creditLimited = fonts.get(4).text().trim();
        fonts = tabtop.child(1).getElementsByTag("font");
        if (fonts.size() != 5) {
            logger.info("未能解析到卡号,{}",mailSrc);
            return false;
        }
        cardNo = fonts.get(0).text().trim();
        paymentDueDate = fonts.get(4).text().trim();

        //积分
        Element div = DOMHelper.getMinContainer(divs, "积分明细");
        Element tbody = div.parent().parent().parent().parent().parent()
                .parent().parent();
        if (tbody.children().size() != 6) {
            logger.info("邮件格式发生了变化，未能解析到积分信息,{}",mailSrc);
            return false;
        }
        fonts = tbody.child(4).getElementsByTag("font");
        if (fonts.size() != 2) {
            logger.info("邮件格式发生了变化，未能解析到积分,{}",mailSrc);
            return false;
        }
        integral = fonts.get(1).text().trim();
        return true;
    }

    @Override
    public boolean check() {
        if (name == null || cardNo == null || billDate == null || integral == null) {
            logger.info("check failure!{}",mailSrc);
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
            logger.info("未能解析到账单月，数据出错：{},{}", billDate,mailSrc);
            return false;
        }
        Map<String, Object> map = new HashMap<>();
        map.put(Constants.REPAY_CREDIT, creditLimited);
        try {
            map.put(Constants.REPAY_DATE, sdf.parse(paymentDueDate));
        } catch (ParseException e) {
            logger.info("未能转换还款日, 可解析格式：yyyyMMdd,当前数据：{},{}",paymentDueDate,mailSrc);
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
