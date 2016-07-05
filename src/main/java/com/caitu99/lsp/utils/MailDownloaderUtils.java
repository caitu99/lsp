package com.caitu99.lsp.utils;

import com.caitu99.lsp.model.spider.Envelope;
import org.apache.commons.lang.StringUtils;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedDeque;

public class MailDownloaderUtils {
    public static Map<String, String> bankMap = new HashMap<String, String>();
    private static Properties props = new Properties();
    private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy,MM,dd,HH,mm,ss");

    static {
        try {
            props.load(MailDownloaderUtils.class.getClassLoader()
                    .getResourceAsStream("properties/cardConfig.properties"));
            Enumeration enu2 = props.propertyNames();
            while (enu2.hasMoreElements()) {
                String key = (String) enu2.nextElement();
                bankMap.put(key, props.getProperty(key));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String getBankCnName(String bankEnName) {
        return MailDownloaderUtils.bankMap.get(bankEnName).split(";")[0];
    }

    public static String getBankInstance(String title) {

        // 信用卡
        for (Map.Entry<String, String> entry : bankMap.entrySet()) {
            String bankCnName = entry.getValue().split(";")[0];
            if (title.indexOf(bankCnName) >= 0) {
                return bankCnName;
            }
        }
        //商旅卡

        // 购物卡

        return "";
    }

    public static String getBankInstanceEmail(String title, String bankEmail) {

        // 信用卡
        for (Map.Entry<String, String> entry : bankMap.entrySet()) {
            String bankCnName = entry.getValue().split(";")[0];
            String bankSender = entry.getValue().split(";")[2];
            if (title.indexOf(bankCnName) >= 0 && bankEmail.indexOf(bankSender) >= 0) {
                return entry.getValue();
            }
            return entry.getValue();
        }
        //商旅卡

        // 购物卡

        return "";
    }

    //格式化时间格式
    public static List<Envelope> parseSendDate(List<Envelope> envelopes) {
        for (int i = 0; i < envelopes.size(); i++) {
            Envelope envelope = envelopes.get(i);
            try {
                envelope.setSentDate(sdf.parse(envelope.getSentDate()).getTime() + "");
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        return envelopes;
    }

    public static ConcurrentLinkedDeque<Envelope> getBankEnvelope(ConcurrentLinkedDeque<Envelope> envelopes, long searchTime) {
        ConcurrentLinkedDeque<Envelope> bankEnvelopes = new ConcurrentLinkedDeque<Envelope>();
        for (Envelope envelope : envelopes) {
            long mailDate = Long.parseLong(envelope.getSentDate()) * 1000;
            String subject = envelope.getSubject();
            subject = XStringUtil.deleteSpace(subject);
            if (subject.indexOf("账单") >= 0 || subject.indexOf("账户概要") >= 0 || subject.indexOf("积分") >= 0 || subject.indexOf("賬戶概要") >= 0) {
                String bankConfigStr = getBankInstanceEmail(envelope.getSubject(), envelope.getFrom());
                if (StringUtils.isNotEmpty(bankConfigStr)) {
                    if ("0".equals(bankConfigStr.split(";")[1])) {
                        if (mailDate >= searchTime) {
                            bankEnvelopes.add(envelope);
                        }
                    } else {
                        if (mailDate >= XStringUtil.getLastSeasonDay().getTime()) {
                            bankEnvelopes.add(envelope);
                        }
                    }
                }
            }
        }
        return bankEnvelopes;
    }
}
