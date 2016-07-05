package com.caitu99.lsp.utils;

import org.apache.commons.lang.StringUtils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;

public class XStringUtil {

    // 获得当天开始时间
    public static Date getBeginOfToday() {
        Calendar currentDate = Calendar.getInstance();
        currentDate.set(Calendar.HOUR_OF_DAY, 0);
        currentDate.set(Calendar.MINUTE, 0);
        currentDate.set(Calendar.SECOND, 0);
        return currentDate.getTime();
    }

    // 将日期转化为字符串
    public static String dateToString(Date date, String... strs) {
        SimpleDateFormat sdf = new SimpleDateFormat(strs == null || strs.length == 0 ? "yyyy-MM-dd" : strs[0]);
        return sdf.format(date);
    }

    // 得到订单编号，如：yyMMddHHmmss
    public static String getOrder() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyMMddHHmmss");
        Random random = new Random(100);
        Date date = new Date();
        int randomNum = random.nextInt(1000);
        return "" + sdf.format(date) + (randomNum < 0 ? -randomNum : randomNum);
    }

    public static String toNumber(String str) {
        if (StringUtils.isEmpty(str))
            return null;
        str = str.trim();
        StringBuffer temp = new StringBuffer();
        for (int i = 0; i < str.length(); i++) {
            if ((48 <= str.charAt(i) && str.charAt(i) <= 57) || str.charAt(i) == 46) {
                temp.append(str.charAt(i));
            }
        }
        if (StringUtils.isEmpty(temp.toString()))
            return null;
        return temp.toString();
    }

    public static String toNumberPlus(String str) {
        if (StringUtils.isEmpty(str))
            return null;
        str = str.trim();
        StringBuffer temp = new StringBuffer();
        for (int i = 0; i < str.length(); i++) {
            if ((48 <= str.charAt(i) && str.charAt(i) <= 57) || str.charAt(i) == 46 || str.charAt(i) == 45) {
                temp.append(str.charAt(i));
            }
        }
        if (StringUtils.isEmpty(temp.toString()))
            return null;
        return temp.toString();
    }

    public static String deleteSpace(String str) {
        return str.replaceAll(" ", "").replaceAll(" ", "");
    }

    public static String deleteSpacePlus(String str) {
        return str.replace("\n", "").replace("\t", "").replaceAll(" ", "").replaceAll(" ", "");
    }

    // 获得当天往前三个月的日期
    public static Date getLastSeasonDay() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, -50);
        return calendar.getTime();
    }

}
