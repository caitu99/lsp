/*
 * Copyright (c) 2015-2020 by caitu99
 * All rights reserved.
 */
package com.caitu99.lsp.spider.cmbchina;

import com.alibaba.fastjson.JSON;
import com.caitu99.lsp.Constant;
import com.caitu99.lsp.exception.SpiderException;
import com.caitu99.lsp.model.spider.MailSpiderEvent;
import com.caitu99.lsp.model.spider.cmbchina.CmbChinaSpiderEvent;
import com.caitu99.lsp.model.spider.cmbchina.CmbChinaSpiderState;
import com.caitu99.lsp.spider.AbstractMailSpider;
import com.caitu99.lsp.spider.MailParserTask;
import com.caitu99.lsp.utils.XpathHtmlUtils;
import com.caitu99.lsp.utils.cookie.CookieHelper;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpMessage;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.util.*;

/**
 * 招行信用卡积分抓取
 *
 * @author chencheng
 * @Description: (类职责详细描述, 可空)
 * @ClassName: CmbChinaSpider
 * @date 2015年11月18日 下午12:23:28
 * @Copyright (c) 2015-2020 by caitu99
 */
public class CmbChinaSpider extends AbstractMailSpider {

    private final static Logger logger = LoggerFactory.getLogger(CmbChinaSpider.class);
    private static final String URL_INIT = "https://mobile.cmbchina.com/MobileHtml/Login/LoginC.aspx";
    private static final String URL_CHECK = "https://mobile.cmbchina.com/MobileHtml/Login/ExtraPwd.aspx?ClientNo=%s";
    private static final String URL_LOGIN = "https://mobile.cmbchina.com/MobileHtml/Login/LoginC.aspx";
    private static final String URL_HOME = "https://mobile.cmbchina.com/MobileHtml/creditcard/account/cm_querycustominfo.aspx";
    private static final String URL_GAIN = "https://mobile.cmbchina.com/MobileHtml/CreditCard/CustomerService/CardManage/psm_QueryPoints.aspx";
    private static final String USER_AGENT = "Android";


    public CmbChinaSpider(CmbChinaBodySpider mailBodySpider, MailParserTask mailParser) {
        super(mailBodySpider, mailParser);
    }

    /**
     * event handle
     *
     * @param event event
     */
    @Override
    public void onEvent(MailSpiderEvent event) {
        CmbChinaSpiderEvent cmbChinaEvent = (CmbChinaSpiderEvent) event;
        try {
            switch (cmbChinaEvent.getState()) {
                case NONE:
                    initUp(cmbChinaEvent);
                    break;
                case CHECK:
                    checkUp(cmbChinaEvent);
                    break;
                case LOGIN:
                    loginUp(cmbChinaEvent);
                    break;
                case GAIN:
                    gainUp(cmbChinaEvent);
                    break;
                case HOME:
                    homeUp(cmbChinaEvent);
                    break;
                case ERROR:
                    errorHandle(cmbChinaEvent);
                    break;
            }
        } catch (Exception e) {
            logger.error("request up error {}", cmbChinaEvent.getId(), e);
            cmbChinaEvent.setException(e);
            errorHandle(event);
        }
    }

    @Override
    protected void setHeader(String uriStr, HttpMessage httpGet, MailSpiderEvent event) {
        httpGet.setHeader("Accept", "*/*");
        httpGet.setHeader("User-Agent", AbstractMailSpider.USERAGENT_ANDROID);
        try {
            CookieHelper.setCookies(uriStr, httpGet, event.getCookieList());
        } catch (URISyntaxException e) {
            logger.error("set cookie fail {}", event.getId());
        }
    }

    /**
     * 初始请求
     *
     * @param event
     * @Description: (方法职责详细描述, 可空)
     * @Title: initUp
     * @date 2015年11月18日 下午12:24:13
     * @author chencheng
     */
    public void initUp(CmbChinaSpiderEvent event) {
        logger.debug("do checkup {}", event.getId());
        String s = String.format(URL_INIT);
        HttpGet httpGet = new HttpGet(s);
        setHeader(s, httpGet, event);
        httpAsyncClient.execute(httpGet, new HttpAsyncCallback(event));
    }

    /**
     * 获取验证码
     *
     * @param event
     * @Description: (方法职责详细描述, 可空)
     * @Title: checkUp
     * @date 2015年11月18日 下午12:24:26
     * @author chencheng
     */
    public void checkUp(CmbChinaSpiderEvent event) {
        logger.debug("do checkup {}", event.getId());
        String s = String.format(URL_CHECK, event.getClientNo());

        logger.debug("checkUp userid:{},clientNo:{}", event.getUserid(), event.getClientNo());
        HttpGet httpGet = new HttpGet(s);
        setHeader(s, httpGet, event);

        httpAsyncClient.execute(httpGet, new HttpAsyncCallback(event));
    }


    /**
     * 登录
     *
     * @param event
     * @Description: (方法职责详细描述, 可空)
     * @Title: loginUp
     * @date 2015年11月18日 下午12:24:37
     * @author chencheng
     */
    public void loginUp(CmbChinaSpiderEvent event) {
        logger.debug("do login passwrod up {}", event.getId());

        String account = event.getAccount();
        String password = event.getPassword();
        String yzm = event.getYzm();
        // 构造登录xmlReq
        String xmlReq = createXmlReqStr(account, password, yzm, AbstractMailSpider.USERAGENT_ANDROID);

        HttpPost httpPost = new HttpPost(URL_LOGIN);
        setHeader(URL_LOGIN, httpPost, event);
        httpPost.setHeader("Host", "mobile.cmbchina.com");
        httpPost.setHeader("Connection", "keep-alive");
        httpPost.setHeader("Cache-Control", "max-age=0");
        httpPost.setHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");

        httpPost.setHeader("Origin", "https://mobile.cmbchina.com");
        httpPost.setHeader("Upgrade-Insecure-Requests", "1");
        httpPost.setHeader("Content-Type", "application/x-www-form-urlencoded");
        httpPost.setHeader("Referer", "https://mobile.cmbchina.com/MobileHtml/Login/LoginC.aspx");

        //ClientNo=43631B26587B569E4DEDDE89B85662F9866126610849716200320644&Command=CMD_DOLOGIN&XmlReq=%3CPwdC%3E147258%3C%2FPwdC%3E%3CExtraPwdC%3E0258%3C%2FExtraPwdC%3E%3CLoginMode%3E0%3C%2FLoginMode%3E%3CLoginByCook%3Efalse%3C%2FLoginByCook%3E%3CIDTypeC%3E01%3C%2FIDTypeC%3E%3CIDNoC%3E131182198602066610%3C%2FIDNoC%3E%3CRememberFlag%3Etrue%3C%2FRememberFlag%3E%3CUserAgent%3EMozilla%2F5.0+%28Linux%3B+Android+4.4.4%3B+Nexus+5+Build%2FKTU84P%29+AppleWebKit%2F537.36+%28KHTML%2C+like+Gecko%29+Chrome%2F38.0.2125.114+Mobile+Safari%2F537.36%3C%2FUserAgent%3E%3CscreenW%3E684%3C%2FscreenW%3E%3CscreenH%3E567%3C%2FscreenH%3E%3COS%3EWin32%3C%2FOS%3E
        List<NameValuePair> nvps = new ArrayList<NameValuePair>();
        nvps.add(new BasicNameValuePair("ClientNo", event.getClientNo()));//客户号
        nvps.add(new BasicNameValuePair("Command", "CMD_DOLOGIN"));
        nvps.add(new BasicNameValuePair("XmlReq", xmlReq));//登录密文

        logger.debug("loginUp userid:{},clientNo:{}", event.getUserid(), event.getClientNo());
        try {
            httpPost.setEntity(new UrlEncodedFormEntity(nvps, "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            logger.error("login eventId:{} error {}", event.getId(), e);
            event.setException(e);
            return;
        }
        httpAsyncClient.execute(httpPost, new HttpAsyncCallback(event));
    }

    /**
     * 客户主页获取信用卡信息
     *
     * @param event
     * @Description: (方法职责详细描述, 可空)
     * @Title: homeUp
     * @date 2015年11月18日 下午12:26:39
     * @author chencheng
     */
    public void homeUp(CmbChinaSpiderEvent event) {
        logger.debug("do checkup {}", event.getId());

        HttpPost httpPost = new HttpPost(URL_HOME);
        setHeader(URL_HOME, httpPost, event);

        //ClientNo=178841C2C5C4C75230579A35D24CC571636435883215995100153516&Command=&XmlReq=
        List<NameValuePair> nvps = new ArrayList<NameValuePair>();
        nvps.add(new BasicNameValuePair("ClientNo", event.getClientNo()));//客户号
        nvps.add(new BasicNameValuePair("Command", ""));
        nvps.add(new BasicNameValuePair("XmlReq", ""));//登录密文
        try {
            httpPost.setEntity(new UrlEncodedFormEntity(nvps, "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            logger.error("login eventId:{} error {}", event.getId(), e);
            event.setException(e);
            return;
        }

        httpAsyncClient.execute(httpPost, new HttpAsyncCallback(event));
    }


    /**
     * 获取积分信息
     *
     * @param event
     * @Description: (方法职责详细描述, 可空)
     * @Title: gainUp
     * @date 2015年11月18日 下午12:27:00
     * @author chencheng
     */
    public void gainUp(CmbChinaSpiderEvent event) {
        logger.debug("do gain up {}", event.getId());
        HttpPost httpPost = new HttpPost(URL_GAIN);
        setHeader(URL_GAIN, httpPost, event);

        //Command=&ClientNo=178841C2C5C4C75230579A35D24CC571636435883215995100153516&DeviceTAB=TabDefault
        List<NameValuePair> nvps = new ArrayList<NameValuePair>();
        nvps.add(new BasicNameValuePair("Command", ""));
        nvps.add(new BasicNameValuePair("ClientNo", event.getClientNo()));//客户号
        nvps.add(new BasicNameValuePair("DeviceTAB", "TabDefault"));//登录密文
        try {
            httpPost.setEntity(new UrlEncodedFormEntity(nvps, "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            logger.error("login eventId:{} error {}", event.getId(), e);
            event.setException(e);
            return;
        }
        httpAsyncClient.execute(httpPost, new HttpAsyncCallback(event));
    }

    /**
     * 创建登录XmlReq
     *
     * @param idNo
     * @param pwd
     * @param imgCode
     * @param userAgent
     * @return
     * @Description: (方法职责详细描述, 可空)
     * @Title: createXmlReqStr
     * @date 2015年11月18日 下午12:31:51
     * @author chencheng
     */
    private String createXmlReqStr(String idNo, String pwd, String imgCode, String userAgent) {
        StringBuffer XmlReq = new StringBuffer();

        XmlReq.append("<PwdC>").append(pwd).append("</PwdC>")
                .append("<ExtraPwdC>").append(imgCode).append("</ExtraPwdC>")
                .append("<LoginMode>0</LoginMode>")
                .append("<LoginByCook>false</LoginByCook>")
                .append("<IDTypeC>01</IDTypeC>")
                .append("<IDNoC>").append(idNo).append("</IDNoC>")
                .append("<RememberFlag>true</RememberFlag>")
                .append("<UserAgent>").append(userAgent).append("</UserAgent>")
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
        String xmlReqStr = XmlReq.toString();
        /*String xmlReqStr = XmlReq.toString()
                .replace("<", "%3C").replace(">", "%3E")
				.replace("/", "%2F").replace(";", "%3B")
				.replace(" ", "+").replace("(", "%28")
				.replace(")", "%29").replace(",", "%2C");*/
        //System.out.println(xmlReqStr);

        return xmlReqStr;

    }

    /**
     * async call back handler
     */
    private class HttpAsyncCallback implements FutureCallback<HttpResponse> {

        private CmbChinaSpiderEvent event;
        private boolean skipNextStep = false;

        public HttpAsyncCallback(CmbChinaSpiderEvent event) {
            this.event = event;
        }

        @Override
        public void completed(HttpResponse result) {
            try {
                // extract cookie
                CookieHelper.getCookies(event.getCookieList(), result);
                switch (event.getState()) {
                    case NONE:
                        initDown(result);
                        break;
                    case CHECK:
                        checkDown(result);
                        break;
                    case LOGIN:
                        loginDown(result);
                        break;
                    case GAIN:
                        gainDown(result);
                        break;
                    case HOME:
                        homeDown(result);
                        break;
                    case ERROR:
                        errorHandle(event);
                        break;
                }
            } catch (Exception e) {
                logger.error("unexpected error {}", event.getId(), e);
                event.setException(e);
            }

            // next step
            if (skipNextStep)
                return;

            onEvent(event);
        }

        /**
         * fail
         *
         * @param e exception
         */
        @Override
        public void failed(Exception e) {
            logger.debug("request {} failed: {}", event.getId(), e.getMessage());
            /*if (event.getState() == MailQQSpiderState.MAIL) {
				int done = event.getMailDone().incrementAndGet();
				if (done == event.getTotalMail()) {
					logger.debug("get mail done {}", event.getId());
					event.setState(MailQQSpiderState.PARSETASKQUEUE);
					onEvent(event);
				}
			}*/
        }

        /**
         * cancelled
         */
        @Override
        public void cancelled() {
            logger.debug("request cancelled: {}", event.getId());
        }

        /**
         * 初始页面结果处理
         *
         * @param response
         * @Description: (方法职责详细描述, 可空)
         * @Title: initDown
         * @date 2015年11月18日 下午12:27:35
         * @author chencheng
         */
        public void initDown(HttpResponse response) {
            logger.debug("initDown {}", event.getId());
            try {
                HttpEntity entity = response.getEntity();


                String entityStr = EntityUtils.toString(entity);
                ////System.out.println(entityStr);
                //切掉 ClientNo= 前的数据
                String clientNo = entityStr.substring(entityStr.indexOf("ClientNo="), entityStr.length());
                //A22402AE8023AE1811A0E3929E544AC1965446125176878400324228
                //切掉 "后的数据 得到整个CientNo
                clientNo = clientNo.substring(9, clientNo.indexOf("\""));
                ////System.out.println(clientNo);

                logger.debug("initDown userid:{},clientNo:{}", event.getUserid(), clientNo);

                event.setClientNo(clientNo);
                event.setState(CmbChinaSpiderState.CHECK);
                return;
            } catch (Exception e) {
                logger.error("zhaoshang initDown exception", e);
                event.setException(e);
                return;
            }
        }

        /**
         * 图片验证获取结果处理
         *
         * @param response
         * @Description: (方法职责详细描述, 可空)
         * @Title: checkDown
         * @date 2015年11月18日 下午12:28:10
         * @author chencheng
         */
        public void checkDown(HttpResponse response) {
            logger.debug("zhaoshang check down {}", event.getId());
            try {
                HttpEntity httpEntity = response.getEntity();
                InputStream imgStream = httpEntity.getContent();
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                int rd = -1;
                byte[] bytes = new byte[1024];
                while ((rd = imgStream.read(bytes)) != -1) {
                    baos.write(bytes, 0, rd);
                }
                byte[] rbyte = baos.toByteArray();

                String imgStr = Base64.getEncoder().encodeToString(rbyte);
				/*
				// save to file, used for debug
				if (appConfig.inDevMode()) {
					byte[] tbytes = Base64.getDecoder().decode(imgStr);
					// FileOutputStream fs = new
					// FileOutputStream("/Users/bobo/Desktop/vcode.jpg");
					FileOutputStream fs = new FileOutputStream("D:\\1.jpg");
					fs.write(tbytes);
					fs.close();
				}
				 */
                // store event to redis
                event.setState(CmbChinaSpiderState.LOGIN); // next step is to login

                String key = String.format(Constant.CMB_CHINA_IMPORT_KEY, event.getUserid());
                redis.set(key, JSON.toJSONString(event), 300); //300秒超时

                // return vcode to user
                event.setException(new SpiderException(0, "0", imgStr));
                return;
            } catch (Exception e) {
                logger.error("zhaoshang checkDown exception", e);
                event.setException(e);
                return;
            }
        }

        /**
         * 登录请求结果处理
         *
         * @param response
         * @Description: (方法职责详细描述, 可空)
         * @Title: loginDown
         * @date 2015年11月18日 下午12:28:55
         * @author chencheng
         */
        private void loginDown(HttpResponse response) {
            String entityStr = "";
            try {

                HttpEntity entity = response.getEntity();

                entityStr = EntityUtils.toString(entity);
                logger.debug("zhaoshang loginDown entity:{}", entityStr);

            } catch (Exception e) {
                logger.error("zhaoshang login result error", e);
                event.setException(e);
                return;
            }

            //登出地址，出现此地址说明登录成功
            String searchChars = "https://mobile.cmbchina.com/MobileHtml/Login/Logout.aspx";
            boolean isSuccess = StringUtils.contains(entityStr, searchChars);
            String err = "errMsg";
            boolean hasErr = StringUtils.contains(entityStr, err);

            logger.debug("zhaoshang loginDown isSuccess:{},hasErr:{}", isSuccess, hasErr);

            if (isSuccess && !hasErr) {
                // 登录成功
                event.setState(CmbChinaSpiderState.GAIN);
                logger.debug("zhaoshang loginDown success");
                return;
            } else if (hasErr) {
                String yzm = "无效附加码";
                String tryAgain = "您已经退出手机银行，请重新登录";
                if (StringUtils.contains(entityStr, yzm)) {
                    // 无效附加码
                    logger.debug("zhaoshang loginDown fail:无效附加码");
                    event.setException(new SpiderException(2004, "登录失败,验证码错误", ""));
                    return;
                } else if (StringUtils.contains(entityStr, tryAgain)) {
                    // 您已经退出手机银行，请重新登录--此情况可作为频繁操作处理
                    logger.debug("zhaoshang loginDown fail:您已经退出手机银行，请重新登录");
                    event.setException(new SpiderException(1048, "频繁操作,请刷新验证码再试", ""));
                    return;
                } else {
                    // 登录失败 , 重新获取验证码
                    logger.debug("zhaoshang loginDown fail:其他");
                    event.setException(new SpiderException(2005, "登录失败,用户名或密码错误", ""));
                    return;
                }
            } else {//频繁操作
                logger.debug("zhaoshang loginDown fail:频繁操作,请刷新验证码再试");
                event.setException(new SpiderException(1048, "频繁操作,请刷新验证码再试", ""));
                return;
            }
        }


        /**
         * 积分获取结果处理
         *
         * @param response
         * @Description: (方法职责详细描述, 可空)
         * @Title: gainDown
         * @date 2015年11月18日 下午12:30:00
         * @author chencheng
         */
        private void gainDown(HttpResponse response) {
            try {
                HttpEntity entity = response.getEntity();
                //获取各项积分
                String integral = parserIntegral(entity);
                integral = StringUtils.remove(integral, ",");//去掉千分位
                //System.out.println(integral);

                event.setIntegral(integral);
                event.setState(CmbChinaSpiderState.HOME);
                return;
            } catch (Exception e) {
                logger.error("zhaoshang gainDown error", e);
                event.setException(e);
                return;
            }

        }


        /**
         * 客户信息结果处理
         *
         * @param response
         * @Description: (方法职责详细描述, 可空)
         * @Title: homeDown
         * @date 2015年11月18日 下午12:30:44
         * @author chencheng
         */
        private void homeDown(HttpResponse response) {
            logger.debug("homeDown {}", event.getId());
            try {
                HttpEntity entity = response.getEntity();
                //获取各项积分
                Map<String, String> data = parserCustome(entity);
                data.put("integral", event.getIntegral());
                ////System.out.println(data);
                //返回积分结果	统一约定，message赋值0
                event.setException(new SpiderException(0, "0", JSON.toJSONString(data)));
                return;
            } catch (Exception e) {
                logger.error("zhaoshang homeDown error", e);
                event.setException(e);
                return;
            }
        }


        /**
         * 解析客户信息
         *
         * @param entity
         * @return
         * @throws IOException
         * @throws Exception
         * @Description: (方法职责详细描述, 可空)
         * @Title: parserCustome
         * @date 2015年11月18日 下午12:31:11
         * @author chencheng
         */
        private Map<String, String> parserCustome(HttpEntity entity)
                throws IOException, Exception {

            String entityStr = EntityUtils.toString(entity);
            //System.out.println(entityStr);
            int count = StringUtils.countMatches(entityStr, "ListTitle");//匹配信用卡数

            logger.debug("zhaoshang homeDown entity:{}", entityStr);
            String fileDir = XpathHtmlUtils.deleteHeadHtml(entityStr);
            Document document = XpathHtmlUtils.getCleanHtml(fileDir);
            XPath xpath = XPathFactory.newInstance().newXPath();
            List<String> accounts = new ArrayList<String>();
            if (count == 1) {
                String exp_account = "//*[@id='cphBody_DataContent1']/div[3]/div/table/tbody/tr[1]/td/span";
                String account = XpathHtmlUtils.getNodeText(exp_account, xpath, document);
                if (StringUtils.isNotBlank(account)) {
                    accounts.add(account);
                }
            } else if (count > 1) {
                for (int i = 1; i <= count; i++) {
                    ////*[@id="cphBody_DataContent1"]/div[3]/div/table[1]/tbody/tr[1]/td/span
                    String exp_account = new StringBuffer("//*[@id='cphBody_DataContent1']/div[3]/div/table[")
                            .append(i).append("]/tbody/tr[1]/td/span").toString();
                    String account = XpathHtmlUtils.getNodeText(exp_account, xpath, document);
                    if (StringUtils.isNotBlank(account)) {
                        accounts.add(account);
                    }
                }
            } else {
                logger.error("该用户无信用卡");
                throw new Exception("该用户无信用卡");
            }


            //名称
            String name = "";
            if (count == 1) {
                String exp_name = "//*[@id='cphBody_DataContent1']/div[3]/div/table/tbody/tr[1]/td";
                name = XpathHtmlUtils.getNodeText(exp_name, xpath, document);
            } else if (count > 1) {
                String exp_name = "//*[@id='cphBody_DataContent1']/div[3]/div/table[3]/tbody/tr[1]/td";
                name = XpathHtmlUtils.getNodeText(exp_name, xpath, document);
            }
            //System.out.println(name);
            if (StringUtils.isNotBlank(name)) {
                int firstIndex = name.indexOf("（") + 1;
                int lastIndex = name.indexOf("）");
                if (lastIndex > firstIndex) {
                    name = name.substring(firstIndex, name.indexOf("）"));
                } else {
                    logger.error("用户名解析错误");
                    throw new Exception("用户名解析错误");
                }
                //System.out.println(name);
            }

            //账号
            StringBuffer accountB = new StringBuffer();//多账号使用,分隔
            for (String accountL : accounts) {
                //System.out.println(accountL);
                accountL = accountL.replaceAll("\r|\n|\t| ", "");
                accountB.append(accountL.substring(accountL.length() - 4))
                        .append(",");//取尾号四位，逗号分隔
                //System.out.println(accountL);
            }
            String account = accountB.toString().substring(0, accountB.length() - 1);
            //System.out.println(account);
            Map<String, String> data = new HashMap<String, String>();
            data.put("name", name);
            data.put("account", account);
            return data;
        }

        /**
         * 解析积分信息
         *
         * @param entity
         * @return
         * @throws IOException
         * @throws Exception
         * @Description: (方法职责详细描述, 可空)
         * @Title: parserIntegral
         * @date 2015年11月18日 下午12:31:28
         * @author chencheng
         */
        private String parserIntegral(HttpEntity entity)
                throws IOException, Exception {
            String fileDir = XpathHtmlUtils.deleteHeadHtml(EntityUtils.toString(entity));
            Document document = XpathHtmlUtils.getCleanHtml(fileDir);
            XPath xpath = XPathFactory.newInstance().newXPath();

            logger.debug("zhaoshang gainDown integral:{}", fileDir);
            //积分
            String exp_integral = "//*[@id='cphBody_divTotal']/div/button/table/tbody/tr[2]/td[1]/span";
            String integral = XpathHtmlUtils.getNodeText(exp_integral, xpath, document);

            return StringUtils.isNotBlank(integral) ? integral : "0";
        }

    }


}

