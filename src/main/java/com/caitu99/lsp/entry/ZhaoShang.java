/*
 * Copyright (c) 2015-2020 by caitu99
 * All rights reserved.
 */
package com.caitu99.lsp.entry;

import com.alibaba.fastjson.JSON;
import com.caitu99.lsp.utils.XpathHtmlUtils;
import com.caitu99.lsp.utils.HttpClientUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.conn.ssl.X509HostnameVerifier;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.BasicClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.w3c.dom.Document;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
 * @author ws
 * @Description: (类职责详细描述, 可空)
 * @ClassName: ZhaoShang
 * @date 2015年11月11日 上午10:58:37
 * @Copyright (c) 2015-2020 by caitu99
 */
@Controller
public class ZhaoShang extends BaseController {

    /**
     * 接口一：1、首页请求登录，获取ClientNo
     * 2、使用ClientNo获取验证码
     * 接口二：1、使用身份证号+验证码+密码，生产XmlReq字符串
     * 2、使用XmlReq字符串进行登录
     * 3、登录成功后访问获取积分页面获取积分
     */
    private static final Logger logger = LoggerFactory
            .getLogger(ZhaoShang.class);

    private static String createXmlReqStr(String idNo, String pwd, String imgCode) {
        StringBuffer XmlReq = new StringBuffer();

        XmlReq.append("<PwdC>").append(pwd).append("</PwdC>")
                .append("<ExtraPwdC>").append(imgCode).append("</ExtraPwdC>")
                .append("<LoginMode>0</LoginMode>")
                .append("<LoginByCook>false</LoginByCook>")
                .append("<IDTypeC>01</IDTypeC>")
                .append("<IDNoC>").append(idNo).append("</IDNoC>")
                .append("<RememberFlag>true</RememberFlag>")
                .append("<UserAgent>Mozilla/5.0 (Linux; Android 4.4.4; Nexus 5 Build/KTU84P) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/38.0.2125.114 Mobile Safari/537.36</UserAgent>")
                .append("<screenW>684</screenW>")
                .append("<screenH>567</screenH>")
                .append("<OS>Win32</OS>");

        /**    做如下对应转换
         %3C	:	<
         %3E	:	>
         %2F	:	/
         %3B	:	;
         +	:	(空格)
         %28	:	(
         %29	:	)
         %2C	:	,
         */

        String xmlReqStr = XmlReq.toString()
                .replace("<", "%3C").replace(">", "%3E")
                .replace("/", "%2F").replace(";", "%3B")
                .replace(" ", "+").replace("(", "%28")
                .replace(")", "%29").replace(",", "%2C");
        System.out.println(xmlReqStr);

        return xmlReqStr;

    }

    public static void main(String[] args) {
        String idNo = "131182198602066610";
        String pwd = "147258";
        String imgCode = "4238";
        createXmlReqStr(idNo, pwd, imgCode);
    }

    public String getImgCode() throws Exception {

        // 初始化
        String url = "https://mobile.cmbchina.com/MobileHtml/Login/LoginC.aspx";
        String referer = "https://mobile.cmbchina.com/portal.aspx";
        HttpClient httpCLient = HttpClientBuilder.create().build();
        HttpGet httpget = createCommonRequest(url, referer, "");

        //
        try {
            HttpResponse response = httpCLient.execute(httpget);

            HttpEntity entity = response.getEntity();


            String entityStr = EntityUtils.toString(entity);
            System.out.println(entityStr);

            String fileDir = XpathHtmlUtils.deleteHeadHtml(entityStr);
            System.out.println(fileDir);

            //  "//*[@id=\"InputInfoTable\"]/table[5]/tbody/tr/td[3]/input"


            Document document = XpathHtmlUtils.getCleanHtml(fileDir);
            System.out.println(document.getDocumentElement());

            XPath xpath = XPathFactory.newInstance().newXPath();
            String exp = "//*[@id='InputInfoTable']/table[5]/tbody/tr/td[1]/table/tbody/tr/td[2]";


            String nodeStr = XpathHtmlUtils.getNodeText(exp, xpath, document);

            System.out.println(nodeStr);
            /*System.out.println(entityStr);
			System.out.println(entityStr.length());*/
            //int start = entityStr.indexOf("ClientNo=");

            //切掉 ClientNo= 前的数据
            String clientNo = entityStr.substring(entityStr.indexOf("ClientNo="), entityStr.length());
            //A22402AE8023AE1811A0E3929E544AC1965446125176878400324228
            //切掉 "后的数据 得到整个CientNo
            clientNo = clientNo.substring(0, clientNo.indexOf("\""));

            String imgCodeStr = getImgCode(clientNo);

            //返回图片流串+setCookie
            return this.getJsonStr(0, imgCodeStr, clientNo);
        } catch (ClientProtocolException e) {
            logger.error("IOException", e);
            return this.getJsonStr(1010, "获取图片验证码失败", "");
        } catch (IOException e) {
            logger.error("IOException", e);
            return this.getJsonStr(1010, "获取图片验证码失败", "");
        }

    }

    public String loginAndGetIntegral(String idNo, String pwd, String imgCode, String clientNo) {

        String xmlReq = createXmlReqStr(idNo, pwd, imgCode);
		/*StringBuffer postBodyBf = new StringBuffer(clientNo)
			.append("&Command=CMD_DOLOGIN&XmlReq=")
			.append(xmlReq);*/


        Map<String, String> paramMap = new HashMap<String, String>();
        String url = "https://mobile.cmbchina.com/MobileHtml/Login/LoginC.aspx";
        clientNo = clientNo.substring(9);
        paramMap.put("ClientNo", clientNo);
        paramMap.put("Command", "CMD_DOLOGIN");
        paramMap.put("XmlReq", xmlReq);
        String result;
        try {
            result = HttpClientUtils.getInstances().doSSLPost(url, "UTF-8", JSON.toJSONString(paramMap));
//			result =SingleHttpClient.getInstances().post(url, "UTF-8", paramMap, headers);
            System.out.println(result);

        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }


        return null;

    }

    /**
     * @param clientNo
     * @throws IOException
     * @Description: (方法职责详细描述, 可空)
     * @Title: getImgCode
     * @date 2015年11月11日 下午4:32:17
     * @author ws
     */
    private String getImgCode(String clientNo) throws IOException {
        // 初始化
        String referer = "https://mobile.cmbchina.com/MobileHtml/Login/LoginC.aspx";
        StringBuffer url = new StringBuffer("https://mobile.cmbchina.com/MobileHtml/Login/ExtraPwd.aspx?")
                .append(clientNo)
                .append("&random=")
                .append(Math.random());
        HttpClient httpCLient = HttpClientBuilder.create().build();
        HttpGet httpget = createCommonRequest(url.toString(), referer, "");

        HttpResponse response;
        try {
            response = httpCLient.execute(httpget);

            HttpEntity entity = response.getEntity();

            InputStream imgStream = entity.getContent();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            int rd = -1;
            byte[] bytes = new byte[1024];
            while ((rd = imgStream.read(bytes)) != -1) {
                baos.write(bytes, 0, rd);
            }
            byte[] rbyte = baos.toByteArray();
            String imgStr = Base64.getEncoder().encodeToString(rbyte);

			/*//生成图片，测试用
			if (true) {
				byte[] tbytes = Base64.getDecoder().decode(imgStr);
				FileOutputStream fs = new FileOutputStream("D://imgCode.jpg");
				fs.write(tbytes);
				fs.close();
			}*/

            return imgStr;
        } catch (ClientProtocolException e) {
            logger.error("ClientProtocolException", e);
            throw e;
        } catch (IOException e) {
            logger.error("IOException", e);
            throw e;
        }

    }

    /**
     * @param setCookies
     * @return
     * @Description: (方法职责详细描述, 可空)
     * @Title: createCookieStr
     * @date 2015年11月11日 下午3:00:28
     * @author ws
     */
    private String createCookieStr(Header[] setCookies) {
        StringBuffer cookieStr = new StringBuffer();
        boolean isFirst = true;
        for (Header cookie : setCookies) {
            String value = cookie.getValue();
			/*if(null == value){
				continue;
			}
			if(isFirst){
				cookieStr.append(value.substring(0, value.indexOf(";")));
				isFirst = false;
			}else{
				cookieStr.append(";").append(cookie.getValue());
			}*/

            String[] splitStr = value.split(";");
            for (String str : splitStr) {

                cookieStr.append(str).append(";");
            }

            //cookieStr.append(value.substring(0, value.indexOf(";")));
        }
        return cookieStr.toString();
    }

    private HttpGet createCommonRequest(String httpUrl, String Referer, String cookie) {


        HttpGet httpget = new HttpGet(httpUrl);
        //设置统一报文头
        httpget.setHeader("Host", "mobile.cmbchina.com");
        httpget.setHeader("Connection", "keep-alive");
        httpget.setHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
        httpget.setHeader("Origin", "https://mobile.cmbchina.com");
        httpget.setHeader("Upgrade-Insecure-Requests", "1");
        httpget.setHeader("Referer", "https://mobile.cmbchina.com/MobileHtml/User/Navigation/NV_FuncSearch.aspx");
//        post.setHeader("Accept-Encoding", "gzip, deflate");
//        post.setHeader("Accept-Language", "zh-CN,zh;q=0.8");
        httpget.setHeader("Accept-Encoding", "gzip, deflate");
        httpget.setHeader("Cookie", "ASP.NET_SessionId=wkrfvlbkl1p4rjasxpyf5lzs; LoginMode=3UFntqbNDa8_; $CLientIP$=219.82.142.229; Version=HTML; LoginType=C; DeviceType=H; _MobileAppVersion=1.0.0");

        //增加Set-Cookie头信息
        if (StringUtils.isNotBlank(cookie)) {
            httpget.setHeader("Cookie", cookie);
        } else {
            httpget.setHeader("Cookie", "ASP.NET_SessionId=wkrfvlbkl1p4rjasxpyf5lzs; LoginMode=3UFntqbNDa8_; $CLientIP$=219.82.142.229; Version=HTML; DeviceType=H; _MobileAppVersion=1.0.0; LoginType=C");

        }
        return httpget;
    }

    /**
     * 构造返回Map
     *
     * @param i
     * @param message
     * @param data
     * @return Map:code,message,data
     * @Description: (方法职责详细描述, 可空)
     * @Title: setResultMap
     * @date 2015年11月10日 下午3:58:22
     * @author chencheng
     */
    private String getJsonStr(int code, String message,
                              Object data) {
        Map<String, Object> resultMap = new HashMap<String, Object>();
        resultMap.put("code", code);
        resultMap.put("message", message);
        resultMap.put("data", data);
        return JSON.toJSONString(resultMap);
    }

    public String doSSLPost(String url, String charset, Map<String, String> paramMap) throws Exception {
        HostnameVerifier hostnameVerifier = org.apache.http.conn.ssl.SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER;
        try {
            HttpClient httpClient = HttpClientBuilder.create().build();
            SchemeRegistry registry = new SchemeRegistry();
            SSLSocketFactory socketFactory = SSLSocketFactory.getSocketFactory();
            socketFactory.setHostnameVerifier((X509HostnameVerifier) hostnameVerifier);
            registry.register(new Scheme("https", 443, socketFactory));
            httpClient.getParams().setParameter(HttpProtocolParams.HTTP_CONTENT_CHARSET, charset);
            BasicClientConnectionManager bccm = new BasicClientConnectionManager(registry);
            DefaultHttpClient client = new DefaultHttpClient(bccm, httpClient.getParams());
            HttpsURLConnection.setDefaultHostnameVerifier(hostnameVerifier);
            HttpPost post = new HttpPost(url);
            post.setHeader("User-Agent", "Mozilla/5.0 (Linux; Android 4.4.4; Nexus 5 Build/KTU84P) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/38.0.2125.114 Mobile Safari/537.36");
            post.setHeader("Content-Type", "application/x-www-form-urlencoded");
            List<NameValuePair> nvps = converForMap(paramMap);
            post.setEntity(new UrlEncodedFormEntity(nvps, charset));
            HttpResponse httpResponse = client.execute(post);
            int status = httpResponse.getStatusLine().getStatusCode();
            System.out.println("status =" + status);
            String returnString = EntityUtils
                    .toString(httpResponse.getEntity());
            return returnString;
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        } finally {

        }
    }

    /**
     * 参数转换接口 Map转换
     *
     * @param signParams
     * @return
     * @Title: converForMap
     * @Description: (这里用一句话描述这个方法的作用)
     * @date 2014年4月8日 上午11:53:09
     * @author dzq
     */
    public List<NameValuePair> converForMap(Map<String, String> signParams) {
        List<NameValuePair> nvps = new ArrayList<NameValuePair>();
        Set<String> keys = signParams.keySet();
        for (String key : keys) {
            nvps.add(new BasicNameValuePair(key, signParams.get(key)));
        }
        return nvps;
    }

}
