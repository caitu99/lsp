/*
 * Copyright (c) 2015-2020 by caitu99
 * All rights reserved.
 */
package com.caitu99.lsp;

import com.caitu99.lsp.utils.SingleHttpClient;
import org.apache.commons.io.FileUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.message.BasicHeader;

import java.io.File;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author lawrence
 * @Description: (类职责详细描述, 可空)
 * @ClassName: Test
 * @date 2015年11月11日 下午3:43:35
 * @Copyright (c) 2015-2020 by caitu99
 */
public class AirChinaLoginTest {

    public static final String charset = "utf-8";

    private static Header[] headers = null;

    static {
        headers = new Header[]{
                new BasicHeader("Host", "mobile.cmbchina.com"),
                new BasicHeader("Connection", "keep-alive"),
                new BasicHeader("Cache-Control", "max-age=0"),
                new BasicHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8"),
                new BasicHeader("Origin", "https://mobile.cmbchina.com"),
                new BasicHeader("Upgrade-Insecure-Requests", "1"),
                new BasicHeader("User-Agent",
                        "Mozilla/5.0 (Linux; Android 4.4.4; Nexus 5 Build/KTU84P) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/38.0.2125.114 Mobile Safari/537.36"),
                new BasicHeader("Content-Type",
                        "application/x-www-form-urlencoded;charset=utf-8"),
                new BasicHeader("Cookie",
                        "ASP.NET_SessionId=wkrfvlbkl1p4rjasxpyf5lzs; LoginMode=3UFntqbNDa8_; $CLientIP$=219.82.142.229; Version=HTML; LoginType=C; DeviceType=H; _MobileAppVersion=1.0.0")};
    }

    public static void main(String[] args) throws Exception {
//		 getyzm();
        String txt = ":120%;\">鲍和映先生，您好！<";
        Pattern pattern = Pattern.compile("(?<=\\>).*(?=，您好)");
        Matcher matcher = pattern.matcher(txt);
        if(matcher.find())
            System.out.print(matcher.group());
    }

    public static void login(String idNo, String pwd, String imgCode,
                             String clientNo) throws Exception {
        String xmlReq = createXmlReqStr(idNo, pwd, imgCode);
        String url = "https://mobile.cmbchina.com/MobileHtml/Login/LoginC.aspx";
        Map<String, String> paramMap = new HashMap<String, String>();
        paramMap.put("ClientNo", clientNo);
        paramMap.put("Command", "CMD_DOLOGIN");
        paramMap.put("XmlReq", xmlReq);
        String result = SingleHttpClient.getInstances().post(url, charset,
                paramMap, headers);
        System.out.println(result);
    }

    private static String createXmlReqStr(String idNo, String pwd,
                                          String imgCode) {
        StringBuffer XmlReq = new StringBuffer();

        XmlReq.append("<PwdC>")
                .append(pwd)
                .append("</PwdC>")
                .append("<ExtraPwdC>")
                .append(imgCode)
                .append("</ExtraPwdC>")
                .append("<LoginMode>0</LoginMode>")
                .append("<LoginByCook>false</LoginByCook>")
                .append("<IDTypeC>01</IDTypeC>")
                .append("<IDNoC>")
                .append(idNo)
                .append("</IDNoC>")
                .append("<RememberFlag>true</RememberFlag>")
                .append("<UserAgent>Mozilla/5.0 (Linux; Android 4.4.4; Nexus 5 Build/KTU84P) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/38.0.2125.114 Mobile Safari/537.36</UserAgent>")
                .append("<screenW>684</screenW>")
                .append("<screenH>567</screenH>").append("<OS>Win32</OS>");

        /**
         * 做如下对应转换 %3C : < %3E : > %2F : / %3B : ; + : (空格) %28 : ( %29 : ) %2C
         * : ,
         */

        String xmlReqStr = XmlReq.toString().replace("<", "%3C")
                .replace(">", "%3E").replace("/", "%2F").replace(";", "%3B")
                .replace(" ", "+").replace("(", "%28").replace(")", "%29")
                .replace(",", "%2C");
        System.out.println(xmlReqStr);

        return xmlReqStr;

    }

    public static void getyzm() throws Exception {

        String url = "https://mobile.cmbchina.com/MobileHtml/Login/LoginC.aspx";
        String entityStr = SingleHttpClient.getInstances().get(url,
                charset, headers);
        //System.out.println(entityStr);
        /*
		 * System.out.println(entityStr);
		 * System.out.println(entityStr.length());
		 */
        // int start = entityStr.indexOf("ClientNo=");

        // 切掉 ClientNo= 前的数据
        String clientNo = entityStr.substring(entityStr.indexOf("ClientNo="),
                entityStr.length());
        // A22402AE8023AE1811A0E3929E544AC1965446125176878400324228
        // 切掉 "后的数据 得到整个CientNo
        clientNo = clientNo.substring(0, clientNo.indexOf("\""));
        System.out.println(clientNo);
        getImgCode(clientNo);

    }

    /**
     * @param clientNo
     * @throws Exception
     * @Description: (方法职责详细描述, 可空)
     * @Title: getImgCode
     * @date 2015年11月11日 下午4:32:17
     * @author ws
     */
    private static void getImgCode(String clientNo) throws Exception {
        // 初始化
        StringBuffer url = new StringBuffer(
                "https://mobile.cmbchina.com/MobileHtml/Login/ExtraPwd.aspx?")
                .append(clientNo).append("&random=").append(Math.random());
        HttpEntity result = SingleHttpClient.getInstances().getEntity(
                url.toString(), charset, headers);
        FileUtils.copyInputStreamToFile(result.getContent(), new File(
                "d:/1.png"));

    }
}
