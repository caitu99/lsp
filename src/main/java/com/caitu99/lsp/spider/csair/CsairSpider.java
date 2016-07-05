/*
 * Copyright (c) 2015-2020 by caitu99
 * All rights reserved.
 */
package com.caitu99.lsp.spider.csair;

import com.alibaba.fastjson.JSON;
import com.caitu99.lsp.Constant;
import com.caitu99.lsp.exception.SpiderException;
import com.caitu99.lsp.model.spider.MailSpiderEvent;
import com.caitu99.lsp.model.spider.csair.CsairSpiderEvent;
import com.caitu99.lsp.model.spider.csair.CsairSpiderState;
import com.caitu99.lsp.spider.AbstractMailSpider;
import com.caitu99.lsp.spider.MailParserTask;
import com.caitu99.lsp.utils.XpathHtmlUtils;
import com.caitu99.lsp.utils.cookie.CookieHelper;
import org.apache.http.*;
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
 * @author fangjunxiao
 * @Description: (类职责详细描述, 可空)
 * @ClassName: CsairSpider
 * @date 2015年11月18日 下午3:18:21
 * @Copyright (c) 2015-2020 by caitu99
 */
public class CsairSpider extends AbstractMailSpider {


    private final static Logger logger = LoggerFactory.getLogger(CsairSpider.class);
    private static final String YZM_URL = "http://skypearl.csair.com/skypearl/cn/validatorAction.action?d=";
    private static final String LOGIN_URL = "https://skypearl.csair.com/skypearl/cn/loginAction.action";
    private static final String NAME_URL = "http://skypearl.csair.com/skypearl/cn/skypearlbaseinfo.action?NXXS=";
    private static final String CARD_URL = "http://skypearl.csair.com/skypearl/cn/integralquery.action?NXXS=";
    private boolean skipNextStep = false;


    /**
     * @param CsairBodySpider
     * @param mailParser
     * @Title:
     * @Description:
     */
    public CsairSpider(CsairBodySpider mailBodySpider, MailParserTask mailParser) {
        super(mailBodySpider, mailParser);
    }

    /* (non-Javadoc)
     * @see com.caitu99.spider.spider.IMailSpider#onEvent(com.caitu99.spider.model.MailSpiderEvent)
     */
    @Override
    public void onEvent(MailSpiderEvent event) {

        CsairSpiderEvent csairEvent = (CsairSpiderEvent) event;

        try {
            switch (csairEvent.getState()) {
                case CHECK:
                    checkUp(csairEvent);
                    break;
                case LOGIN:
                    loginUp(csairEvent);
                    break;
                case GETN:
                    getnUp(csairEvent);
                    break;
                case GAIN:
                    gainUp(csairEvent);
                    break;
                case ERROR:
                    errorHandle(csairEvent);
                    break;
            }
        } catch (Exception e) {
            logger.error("request up error {}", csairEvent.getId(), e);
            csairEvent.setException(e);
            errorHandle(event);
        }
    }

    @Override
    protected void setHeader(String uriStr, HttpMessage httpGet, MailSpiderEvent event) {
        httpGet.setHeader("Accept", "*/*");
        httpGet.setHeader("User-Agent", USERAGENT_CHROME);
        try {
            CookieHelper.setCookies(uriStr, httpGet, event.getCookieList());
        } catch (URISyntaxException e) {
            logger.error("set cookie fail {}", event.getId());
        }
    }

    /**
     * 获取验证码和cookie
     *
     * @param event
     * @Description: (方法职责详细描述, 可空)
     * @Title: checkUp
     * @date 2015年11月18日 下午3:50:20
     * @author fangjunxiao
     */

    private void checkUp(CsairSpiderEvent event) {
        logger.debug("do initUp {}", event.getId());
        String s = String.format(YZM_URL, new Date().getTime());
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
     * @date 2015年11月18日 下午3:50:20
     * @author fangjunxiao
     */

    private void loginUp(CsairSpiderEvent event) {
        logger.debug("do loginUp {}", event.getId());

        String username = event.getAccount();
        String password = event.getPassword();
        String validator = event.getValidator();
        String inCode = event.getInCode();
        String type = event.getType();

        HttpPost httpPost = new HttpPost(LOGIN_URL);
        setHeader(LOGIN_URL, httpPost, event);
        setHeader(httpPost);

        List<NameValuePair> nvps = new ArrayList<NameValuePair>();

        nvps.add(new BasicNameValuePair("type", type));
        nvps.add(new BasicNameValuePair("inCode", inCode));
        nvps.add(new BasicNameValuePair("validator", validator));//验证码
        nvps.add(new BasicNameValuePair("username", username));//账号 
        nvps.add(new BasicNameValuePair("password", password));//密码
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
     * 获取会员名
     *
     * @param event
     * @Description: (方法职责详细描述, 可空)
     * @Title: getnUp
     * @date 2015年11月18日 下午3:50:20
     * @author fangjunxiao
     */
    private void getnUp(CsairSpiderEvent event) {
        logger.debug("do getn up {}", event.getId());
        String url = String.format(NAME_URL, new Date().getTime());
        HttpGet httpGet = new HttpGet(url);
        setHeader(url, httpGet, event);
        httpAsyncClient.execute(httpGet, new HttpAsyncCallback(event));
    }

    /**
     * 获取积分及会员号
     *
     * @param event
     * @Description: (方法职责详细描述, 可空)
     * @Title: getnUp
     * @date 2015年11月18日 下午3:50:20
     * @author fangjunxiao
     */
    private void gainUp(CsairSpiderEvent event) {
        logger.debug("do gain up {}", event.getId());
        String url = String.format(CARD_URL, new Date().getTime());
        HttpGet httpGet = new HttpGet(url);
        setHeader(url, httpGet, event);
        httpAsyncClient.execute(httpGet, new HttpAsyncCallback(event));
    }


    private void setHeader(HttpPost httpPost) {
        httpPost.setHeader("Host", "skypearl.csair.com");
        httpPost.setHeader("Connection", "keep-alive");
        httpPost.setHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
        httpPost.setHeader("Origin", "http://skypearl.csair.com");
        httpPost.setHeader("Referer", "http://skypearl.csair.com/skypearl/cn/loginPage.action");
        httpPost.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/46.0.2490.80 Safari/537.36");
        httpPost.setHeader("Content-Type", "application/x-www-form-urlencoded");
        httpPost.setHeader("Accept-Encoding", "gzip, deflate");
        httpPost.setHeader("Accept-Language", "zh-CN,zh;q=0.8");

    }


    private class HttpAsyncCallback implements FutureCallback<HttpResponse> {

        private CsairSpiderEvent event;

        public HttpAsyncCallback(CsairSpiderEvent event) {
            this.event = event;
        }

        /* (non-Javadoc)
         * @see org.apache.http.concurrent.FutureCallback#completed(java.lang.Object)
         */
        @Override
        public void completed(HttpResponse result) {
            try {
                // extract cookie
                CookieHelper.getCookies(event.getCookieList(), result);
                switch (event.getState()) {
                    case CHECK:
                        checkDown(result);
                        break;
                    case LOGIN:
                        loginDown(result);
                        break;
                    case GETN:
                        getnDown(result);
                        break;
                    case GAIN:
                        gainDown(result);
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
         * failed
         */
        @Override
        public void failed(Exception e) {
            logger.debug("request {} failed: {}", event.getId(), e.getMessage());
            event.setException(e);

            onEvent(event);
        }

        /**
         * cancelled
         */
        @Override
        public void cancelled() {
            logger.debug("request cancelled: {}", event.getId());
        }

        /**
         * 获取验证码结果处理
         *
         * @param result
         * @Description: (方法职责详细描述, 可空)
         * @Title: checkDown
         * @date 2015年11月18日 下午4:24:23
         * @author fangjunxiao
         */
        private void checkDown(HttpResponse result) {
            try {
                HttpEntity entity = result.getEntity();
                InputStream imgStream = entity.getContent();

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                int rd = -1;
                byte[] bytes = new byte[1024];
                while ((rd = imgStream.read(bytes)) != -1) {
                    baos.write(bytes, 0, rd);
                }
                byte[] rbyte = baos.toByteArray();
                //图片流字符串
                String imgStr = Base64.getEncoder().encodeToString(rbyte);

                // save to file, used for debug
        /*		if (appConfig.inDevMode()) {
                    byte[] tbytes = Base64.getDecoder().decode(imgStr);
					// FileOutputStream fs = new
					// FileOutputStream("/Users/bobo/Desktop/vcode.jpg");
					FileOutputStream fs = new FileOutputStream("D:/2.jpg");
					fs.write(tbytes);
					fs.close();
				}*/

                // 记录事件当前步骤
                //event.setState(CsairSpiderState.LOGIN); // next step is to login
                // 缓存当前事件内容
                String key = String.format(Constant.CSAIR_IMPORT_KEY, event.getUserid());
                redis.set(key, JSON.toJSONString(event), 300); //300秒超时

                // 返回当前结果
                event.setException(new SpiderException(0, "0", imgStr));//统一约定，message赋值0
                return;

            } catch (Exception e) {
                logger.error("csair checkDown exception", e);
                event.setException(e);
                return;
            }

        }

        /**
         * 登录结果处理
         *
         * @param response
         * @Description: (方法职责详细描述, 可空)
         * @Title: loginDown
         * @date 2015年11月19日 上午11:59:30
         * @author fangjunxiao
         */
        private void loginDown(HttpResponse response) {
            try {

                //	Location: http://skypearl.csair.com/skypearl/cn/loginPage.action?uri=&ms=skypearlweb.actions.login.pwd.no
                //	Location: http://skypearl.csair.com/skypearl/cn/loginPage.action?uri=&ms=skypearlweb.actions.login.validate.no
                //  Location: http://skypearl.csair.com/skypearl/cn/memberArea.action?&NOSSL&rem=true&ms=skypearlweb.actions.login.info.error.address

                Header[] headers = response.getHeaders("Location");
                if (null == headers || 0 == headers.length) {
                    event.setException(new SpiderException(2005, "登录失败"));
                    return;
                }
                String location = headers[0].toString();
                if (location.contains("loginPage.action")) {
                    if (location.contains("pwd.no")) {
                        event.setException(new SpiderException(2005, "您输入的用户名、密码不匹配哟，请您检查后再试试"));
                        return;
                    } else if (location.contains("validate.no")) {
                        event.setException(new SpiderException(2005, "验证码不正确，重新输入试试"));
                        return;
                    }
                } else if (location.contains("memberArea.action")) {
                    //登录成功
                    event.setState(CsairSpiderState.GETN);
                    return;
                }

                event.setException(new SpiderException(2005, "登录失败"));
                return;

            } catch (Exception e) {
                logger.error("csair loginDown error", e);
                event.setException(e);
                return;
            }
        }

        /**
         * 获取会员名结果处理
         *
         * @param response
         * @Description: (方法职责详细描述, 可空)
         * @Title: getnDown
         * @date 2015年11月19日 上午11:59:54
         * @author fangjunxiao
         */
        private void getnDown(HttpResponse response) {

            try {
                HttpEntity entity = response.getEntity();
                //获取各项积分
                String name = parserIntegral(entity);
                event.setName(name);
                event.setState(CsairSpiderState.GAIN);
                return;
            } catch (Exception e) {
                logger.error("csair getnDown error", e);
                event.setException(new SpiderException(0, "积分解析失败"));
                return;
            }

        }

        /**
         * 获取积分结果处理
         *
         * @param response
         * @Description: (方法职责详细描述, 可空)
         * @Title: gainDown
         * @date 2015年11月19日 下午12:00:25
         * @author fangjunxiao
         */
        private void gainDown(HttpResponse response) {
            try {
                HttpEntity entity = response.getEntity();
                //获取各项积分
                Map<String, String> data = getIntegralInfo(entity);
                data.put("name", event.getName());
                //返回积分结果	统一约定，message赋值0
                event.setException(new SpiderException(0, "0", JSON.toJSONString(data)));
                return;
            } catch (Exception e) {
                logger.error("csair gainDown error", e);
                event.setException(new SpiderException(0, "积分解析失败"));
                return;
            }
        }

        private String parserIntegral(HttpEntity entity)
                throws IOException, Exception {
            String fileDir = XpathHtmlUtils.deleteHeadHtml(EntityUtils.toString(entity));
            Document document = XpathHtmlUtils.getCleanHtml(fileDir);
            XPath xpath = XPathFactory.newInstance().newXPath();

            //会员名
            String exp_name = "//*[@id='searchbox']/div/div/table/tbody/tr[4]/td[2]";
            return XpathHtmlUtils.getNodeText(exp_name, xpath, document);

        }


        private Map<String, String> getIntegralInfo(HttpEntity entity)
                throws IOException, Exception {
            String entityStr = EntityUtils.toString(entity);
            String fileDir = XpathHtmlUtils.deleteHeadHtml(entityStr);
            Document document = XpathHtmlUtils.getCleanHtml(fileDir);
            XPath xpath = XPathFactory.newInstance().newXPath();

            //卡号
            String exp_card = "//*[@id='searchbox']/div[1]";
            String card = XpathHtmlUtils.getNodeText(exp_card, xpath, document);
            String cardrep = card.replace("卡号", "").trim();


            //积分
            String exp_integral = "//*[@id='searchbox']/div[2]/div[1]/div[2]/div/table/tbody/tr[1]/td[2]";
            String integral = XpathHtmlUtils.getNodeText(exp_integral, xpath, document);
            String ireplace = integral.replace(",", "").replace("公里", "").trim();
            Map<String, String> data = new HashMap<String, String>();
            data.put("card", cardrep);
            data.put("integral", ireplace);

            return data;
        }

    }


}
