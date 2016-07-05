/*
 * Copyright (c) 2015-2020 by caitu99
 * All rights reserved.
 */
package com.caitu99.lsp.taobao;

import com.caitu99.lsp.utils.XpathHtmlUtils;
import com.caitu99.lsp.utils.ScriptHelper;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.*;

/**
 * @author Hongbo Peng
 * @Description: (淘宝 淘金币 天猫积分 淘里程查询)
 * @ClassName: TaoBao
 * @date 2015年11月16日 下午5:53:56
 * @Copyright (c) 2015-2020 by caitu99
 */
public class TaoBao {
    private HttpClient httpClient;

    public TaoBao() {
        httpClient = HttpClientBuilder.create().build();
    }

    public static void main(String[] args) {
        try {
            TaoBao tb = new TaoBao();
            String loginPage = tb.getLoginPage();
            Map<String, String> rsaParam = tb.getRSAParam(loginPage);

            String codeUrl = tb.getCheckCodeImg(loginPage);
            System.out.println(codeUrl);
            Map<String, String> params = tb.getLoginParam(loginPage);
            System.out.println("请输入验证码：");
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            String authCode = reader.readLine();
            //登录
            tb.taobaoLogin(rsaParam, params, "justphb", "Penghb0818", authCode);
            //淘金币
//			tb.getTaojinbi();
            //过期淘金币
//			tb.getExpiredTaojinbi();
            //天猫积分
            tb.getTmallInteger();
            //淘里程
//			tb.getTaolicheng();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * @param loginPage
     * @return
     * @Description: (获取登录页面内容)
     * @Title: getLoginParam
     * @date 2015年11月17日 上午9:57:56
     * @author Hongbo Peng
     */
    public String getLoginPage() {
        String url = "https://login.m.taobao.com/login.htm?tpl_redirect_url=https%3A%2F%2Fh5.m.taobao.com%2Fmlapp%2Fmytaobao.html%23mlapp-mytaobao";
        HttpGet httpget = new HttpGet(url);
        httpget.setHeader("scheme", "https");
        httpget.setHeader("version", "HTTP/1.1");
        httpget.setHeader("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
        httpget.setHeader("accept-encoding", "gzip, deflate, sdch");
        httpget.setHeader("accept-language", "zh-CN,zh;q=0.8");
        httpget.setHeader("cookie", "");
        httpget.setHeader("referer", "https://h5.m.taobao.com/mlapp/mytaobao.html");
        httpget.setHeader("upgrade-insecure-requests", "1");
        httpget.setHeader("user-agent", "Mozilla/5.0 (Windows NT 6.3; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/45.0.2454.93 Safari/537.36");
        try {
            HttpResponse response = httpClient.execute(httpget);
            HttpEntity entity = response.getEntity();
            String entityStr = EntityUtils.toString(entity);
            System.out.println(entityStr);
            String fileDir = XpathHtmlUtils.deleteHeadHtml(entityStr);
//			System.out.println(fileDir);
            return fileDir;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * @param loginPage
     * @return
     * @Description: (获取登录隐藏域参数)
     * @Title: getLoginParam
     * @date 2015年11月17日 上午9:57:56
     * @author Hongbo Peng
     */
    public Map<String, String> getLoginParam(String loginPage) {
        Map<String, String> params = new HashMap<String, String>();
        try {
            Document document = XpathHtmlUtils.getCleanHtml(loginPage);
            NodeList nodeList = document.getElementsByTagName("input");
            for (int i = 0; i < nodeList.getLength(); i++) {
                Node node = nodeList.item(i);
                Element e = (Element) node;
                String type = e.getAttribute("type");
                String name = e.getAttribute("name");
                if ("hidden".equals(type) && StringUtils.isNotBlank(name)) {
//					System.out.println(name +"="+e.getAttribute("value"));
                    params.put(name, e.getAttribute("value"));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return params;
    }

    public Map<String, String> getRSAParam(String loginPage) {
        Map<String, String> params = new HashMap<String, String>();
        try {
            Document document = XpathHtmlUtils.getCleanHtml(loginPage);
            System.out.println(document.getDocumentElement());
            XPath xpath = XPathFactory.newInstance().newXPath();
            String exp = "//input[@id='J_Exponent']";
            String exponent = XpathHtmlUtils.getNodeValue(exp, xpath, document);
            params.put("J_Exponent", exponent);
            exp = "//input[@id='J_Module']";
            String module = XpathHtmlUtils.getNodeValue(exp, xpath, document);
            params.put("J_Module", module);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return params;
    }

    /**
     * @param loginPage
     * @return
     * @Description: (获取验证码图片路径)
     * @Title: getCheckCodeImg
     * @date 2015年11月17日 上午10:05:47
     * @author Hongbo Peng
     */
    public String getCheckCodeImg(String loginPage) {
        try {
            Document document = XpathHtmlUtils.getCleanHtml(loginPage);
            Element img = (Element) document.getElementsByTagName("img").item(0);
            String codeUrl = img.getAttribute("src");
            return codeUrl;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public void taobaoLogin(Map<String, String> rsaParams, Map<String, String> params, String username, String password, String authCode) {
        try {
            String url = "https://login.m.taobao.com/login.htm?_input_charset=utf-8&amp;sid=1cdba779853fc7f6ed36cd797ed097f5";

            String pwd = ScriptHelper.encryptTaoBaoPassword(rsaParams.get("J_Exponent"), rsaParams.get("J_Module"), password);
            System.out.println("===============");
            System.out.println(pwd);
            System.out.println("===============");
            params.put("TPL_username", username);
            params.put("TPL_checkcode", authCode);
            params.put("TPL_password2", pwd);
            List<NameValuePair> nvps = converForMap(params);
            HttpPost post = new HttpPost(url);
            post.setHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
            post.setHeader("Referer", "https://login.m.taobao.com/login.htm?token=bd4d28c03b625e8c2900b1d3a496fea1&TPL_redirect_url=https%3A%2F%2Fh5.m.taobao.com%2Fmlapp%2Fmytaobao.html%23mlapp-mytaobao&ssottid=&sid=1cdba779853fc7f6ed36cd797ed097f5");
            post.setHeader("Upgrade-Insecure-Requests", "1");
            post.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 6.3; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/45.0.2454.93 Safari/537.36");
            post.setEntity(new UrlEncodedFormEntity(nvps, "utf-8"));
            HttpResponse httpResponse = httpClient.execute(post);
            int status = httpResponse.getStatusLine().getStatusCode();
            System.out.println("status =" + status);
            if (status == 302) {
                String locationUrl = httpResponse.getLastHeader("Location").getValue();
                System.out.println(locationUrl);
                HttpGet get = new HttpGet(locationUrl);
                HttpResponse response = httpClient.execute(get);
                HttpEntity entity = response.getEntity();
                String entityStr = EntityUtils.toString(entity);
                System.out.println(entityStr);
                //5秒后跳回前页, 如未跳转, 请点击下面链接继续先前的操作
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public List<NameValuePair> converForMap(Map<String, String> signParams) {
        List<NameValuePair> nvps = new ArrayList<NameValuePair>();
        Set<String> keys = signParams.keySet();
        for (String key : keys) {
            nvps.add(new BasicNameValuePair(key, signParams.get(key)));
        }
        return nvps;
    }

    public Integer getTaojinbi() {
        Long t = new Date().getTime();
        String url = "https://api-taojinbi.taobao.com/json/user_info.htm?t=" + t + "&_ksTS=" + (t + 1) + "_24&callback=jsonp25";
        HttpGet httpget = new HttpGet(url);
        httpget.setHeader("scheme", "https");
        httpget.setHeader("version", "HTTP/1.1");
        httpget.setHeader("accept", "*/*");
        httpget.setHeader("accept-encoding", "gzip, deflate, sdch");
        httpget.setHeader("accept-language", "zh-CN,zh;q=0.8");
        httpget.setHeader("referer", "https://taojinbi.taobao.com/coin/userCoinDetail.htm?spm=a217e.7256925.1997946877.1.5zwDZs");
        httpget.setHeader("user-agent", "Mozilla/5.0 (Windows NT 6.3; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/45.0.2454.93 Safari/537.36");
        try {
            HttpResponse response = httpClient.execute(httpget);
            HttpEntity entity = response.getEntity();
            String entityStr = EntityUtils.toString(entity);
            System.out.println(entityStr);
            return null;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public void getExpiredTaojinbi() {
        Long t = new Date().getTime();
        String url = "https://ajax-taojinbi.taobao.com/coin/GetUserCoinDetailJson.do?tab=2&page=1&_ksTS=" + t + "_38&callback=jsonp39";
        HttpGet httpget = new HttpGet(url);
        httpget.setHeader("scheme", "https");
        httpget.setHeader("version", "HTTP/1.1");
        httpget.setHeader("accept", "*/*");
        httpget.setHeader("accept-encoding", "gzip, deflate, sdch");
        httpget.setHeader("accept-language", "zh-CN,zh;q=0.8");
        httpget.setHeader("referer", "https://taojinbi.taobao.com/coin/userCoinDetail.htm?spm=a217e.7256925.1997946877.1.5zwDZs");
        httpget.setHeader("user-agent", "Mozilla/5.0 (Windows NT 6.3; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/45.0.2454.93 Safari/537.36");
        try {
            HttpResponse response = httpClient.execute(httpget);
            HttpEntity entity = response.getEntity();
            String entityStr = EntityUtils.toString(entity);
            System.out.println(entityStr);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void getTmallInteger() {
        Long t = new Date().getTime();
        String url = "http://vip.tmall.com/api/point/MyPointSummary.do?_ksTS=" + t + "_91&callback=jsonp92";
        HttpGet httpget = new HttpGet(url);
        httpget.setHeader("accept", "text/javascript, application/javascript, application/ecmascript, application/x-ecmascript, */*; q=0.01");
        httpget.setHeader("accept-encoding", "gzip, deflate, sdch");
        httpget.setHeader("accept-language", "zh-CN,zh;q=0.8");
        httpget.setHeader("Cache-Control", "max-age=0");
        httpget.setHeader("referer", "http://vip.tmall.com/point/detail/all?spm=0.0.0.0.2hYSwa&from=top&scm=1027.1.1.4");
        httpget.setHeader("user-agent", "Mozilla/5.0 (Windows NT 6.3; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/45.0.2454.93 Safari/537.36");
        try {
            HttpResponse response = httpClient.execute(httpget);
            HttpEntity entity = response.getEntity();
            String entityStr = EntityUtils.toString(entity);
            System.out.println(entityStr);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void getTaolicheng() {
        String url = "https://ffa.trip.taobao.com/userInfo.htm?callback=ALITRIP.Global._successFn";
        HttpGet httpget = new HttpGet(url);
        httpget.setHeader("scheme", "https");
        httpget.setHeader("version", "HTTP/1.1");
        httpget.setHeader("accept", "*/*");
        httpget.setHeader("accept-encoding", "gzip, deflate, sdch");
        httpget.setHeader("accept-language", "zh-CN,zh;q=0.8");
        httpget.setHeader("Cache-Control", "max-age=0");
        httpget.setHeader("referer", "https://www.alitrip.com/mytrip/?spm=181.7091613.a1z68.39.zrFrSZ");
        httpget.setHeader("user-agent", "Mozilla/5.0 (Windows NT 6.3; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/45.0.2454.93 Safari/537.36");
        try {
            HttpResponse response = httpClient.execute(httpget);
            HttpEntity entity = response.getEntity();
            String entityStr = EntityUtils.toString(entity);
            System.out.println(entityStr);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
