/*
 * Copyright (c) 2015-2020 by caitu99
 * All rights reserved.
 */
package com.caitu99.lsp.spider.airchina;

import com.alibaba.fastjson.JSON;
import com.caitu99.lsp.Constant;
import com.caitu99.lsp.exception.SpiderException;
import com.caitu99.lsp.model.spider.MailSpiderEvent;
import com.caitu99.lsp.model.spider.airchina.AirChinaSpiderEvent;
import com.caitu99.lsp.model.spider.airchina.AirChinaSpiderState;
import com.caitu99.lsp.spider.AbstractMailSpider;
import com.caitu99.lsp.spider.MailParserTask;
import com.caitu99.lsp.utils.XpathHtmlUtils;
import com.caitu99.lsp.utils.cookie.CookieHelper;
import org.apache.commons.lang.StringUtils;
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
 * @author chencheng
 * @Description: (类职责详细描述, 可空)
 * @ClassName: AirChinaSpider
 * @date 2015年11月12日 下午12:13:54
 * @Copyright (c) 2015-2020 by caitu99
 */
public class AirChinaSpider extends AbstractMailSpider {

    private final static Logger logger = LoggerFactory.getLogger(AirChinaSpider.class);
    private static final String URL_INIT = "http://ffp.airchina.com.cn/cn/main.jsp";
    private static final String URL_CHECK = "http://ffp.airchina.com.cn/codeImg.jsp?%s";
    private static final String URL_KEY = "http://ffp.airchina.com.cn/mailpost.action?method=userFindAesKey";
    private static final String URL_LOGIN = "http://ffp.airchina.com.cn/mailpost.action?method=goUser";
    private static final String URL_GAIN = "http://ffp.airchina.com.cn/cn/member/main.jsp";


    public AirChinaSpider(AirChinaBodySpider mailBodySpider, MailParserTask mailParser) {
        super(mailBodySpider, mailParser);
    }

    /**
     * event handle
     *
     * @param event event
     */
    @Override
    public void onEvent(MailSpiderEvent event) {
        AirChinaSpiderEvent airChinaEvent = (AirChinaSpiderEvent) event;
        try {
            switch (airChinaEvent.getState()) {
                case NONE:
                    initUp(airChinaEvent);
                    break;
                case CHECK:
                    checkUp(airChinaEvent);
                    break;
                case KEY:
                    keyUp(airChinaEvent);
                    break;
                case LOGIN:
                    loginUp(airChinaEvent);
                    break;
                case GAIN:
                    gainUp(airChinaEvent);
                    break;
                case ERROR:
                    errorHandle(airChinaEvent);
                    break;
            }
        } catch (Exception e) {
            logger.error("request up error {}", airChinaEvent.getId(), e);
            airChinaEvent.setException(e);
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
     * 首次进入国航首页，获取cookie
     *
     * @param event
     * @Description: (方法职责详细描述, 可空)
     * @Title: initUp
     * @date 2015年11月12日 下午2:16:36
     * @author chencheng
     */
    public void initUp(AirChinaSpiderEvent event) {
        logger.debug("do initUp {}", event.getId());
        String s = String.format(URL_INIT);
        HttpGet httpGet = new HttpGet(s);
        //httpGet.setHeaders(headers);
        setHeader(s, httpGet, event);
        httpAsyncClient.execute(httpGet, new HttpAsyncCallback(event));
    }

    /**
     * 获取图片验证码
     *
     * @param event
     * @Description: (方法职责详细描述, 可空)
     * @Title: checkUp
     * @date 2015年11月12日 下午2:16:36
     * @author chencheng
     */
    public void checkUp(AirChinaSpiderEvent event) {
        logger.debug("do checkup {}", event.getId());
        String s = String.format(URL_CHECK, new Date().getTime());
        HttpGet httpGet = new HttpGet(s);
        //httpGet.setHeaders(headers);
        setHeader(s, httpGet, event);

        httpAsyncClient.execute(httpGet, new HttpAsyncCallback(event));
    }

    /**
     * 获取解密密钥
     *
     * @param event
     * @Description: (方法职责详细描述, 可空)
     * @Title: keyUp
     * @date 2015年11月13日 下午2:29:04
     * @author chencheng
     */
    public void keyUp(AirChinaSpiderEvent event) {
        logger.debug("do keyUp {}", event.getId());
        HttpGet httpGet = new HttpGet(URL_KEY);
        setHeader(URL_KEY, httpGet, event);
        //httpGet.setHeaders(headers);
        httpAsyncClient.execute(httpGet, new HttpAsyncCallback(event));
    }

    /**
     * 登录国航
     *
     * @param event
     * @Description: (方法职责详细描述, 可空)
     * @Title: loginUp
     * @date 2015年11月13日 下午2:29:27
     * @author chencheng
     */
    public void loginUp(AirChinaSpiderEvent event) {
        logger.debug("do loginUp {}", event.getId());

        String account = event.getAccount();
        String password = event.getPassword();
        String yzm = event.getYzm();
        String key = event.getKey();
        String type = event.getType();
        String inpin = "";
        try {
            //密钥解密
            inpin = AirChinaAES.encrypt(password, key);
        } catch (Exception e) {
            logger.error("air china error loginUp eventId:{} error {}", event.getId(), e);
            event.setException(e);
            return;
        }

        HttpPost httpPost = new HttpPost(URL_LOGIN);
        setHeader(URL_KEY, httpPost, event);
        //httpPost.setHeaders(headers);
        List<NameValuePair> nvps = new ArrayList<NameValuePair>();

        //MOBILE-手机、NUMBER-证件号
        nvps.add(new BasicNameValuePair("type", type));
        nvps.add(new BasicNameValuePair("yzm", yzm));//验证码
        nvps.add(new BasicNameValuePair("INMID", account));//账号 
        nvps.add(new BasicNameValuePair("INPIN", inpin));//密码
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
     * 抓取积分
     *
     * @param event
     * @Description: (方法职责详细描述, 可空)
     * @Title: gainUp
     * @date 2015年11月13日 下午2:31:01
     * @author chencheng
     */
    public void gainUp(AirChinaSpiderEvent event) {
        logger.debug("do gain up {}", event.getId());
        HttpGet httpGet = new HttpGet(URL_GAIN);
        //httpGet.setHeaders(headers);
        setHeader(URL_GAIN, httpGet, event);
        httpAsyncClient.execute(httpGet, new HttpAsyncCallback(event));
    }

    /**
     * 结果处理
     *
     * @author chencheng
     * @Description: (类职责详细描述, 可空)
     * @ClassName: HttpAsyncCallback
     * @date 2015年11月13日 下午2:31:40
     * @Copyright (c) 2015-2020 by caitu99
     */
    private class HttpAsyncCallback implements FutureCallback<HttpResponse> {

        private AirChinaSpiderEvent event;
        private boolean skipNextStep = false;

        public HttpAsyncCallback(AirChinaSpiderEvent event) {
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
                    case KEY:
                        keyDown(result);
                        break;
                    case LOGIN:
                        loginDown(result);
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
         * 进入国航首页结果处理
         *
         * @param response
         * @Description: (方法职责详细描述, 可空)
         * @Title: initDown
         * @date 2015年11月13日 下午2:33:08
         * @author chencheng
         */
        public void initDown(HttpResponse response) {
            logger.debug("check down {}", event.getId());
            try {
                event.setState(AirChinaSpiderState.CHECK);
                return;
            } catch (Exception e) {
                logger.error("get result exception", e);
                event.setException(e);
                return;
            }
        }

        /**
         * 获取验证码结果处理
         *
         * @param response
         * @Description: (方法职责详细描述, 可空)
         * @Title: checkDown
         * @date 2015年11月13日 下午2:33:43
         * @author chencheng
         */
        public void checkDown(HttpResponse response) {
            logger.debug("check down {}", event.getId());
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
                //图片流字符串
                String imgStr = Base64.getEncoder().encodeToString(rbyte);

				/*// save to file, used for debug
                if (appConfig.inDevMode()) {
					byte[] tbytes = Base64.getDecoder().decode(imgStr);
					// FileOutputStream fs = new
					// FileOutputStream("/Users/bobo/Desktop/vcode.jpg");
					FileOutputStream fs = new FileOutputStream("D:\\1.jpg");
					fs.write(tbytes);
					fs.close();
				}*/

                // 记录事件当前步骤
                event.setState(AirChinaSpiderState.KEY); // next step is to login
                // 缓存当前事件内容
                String key = String.format(Constant.AIR_CHINA_IMPORT_KEY, event.getUserid());
                redis.set(key, JSON.toJSONString(event), 300); //300秒超时

                // 返回当前结果
                event.setException(new SpiderException(0, "0", imgStr));//统一约定，message赋值0
                return;
            } catch (Exception e) {
                logger.error("air china checkDown exception", e);
                event.setException(e);
                return;
            }
        }

        /**
         * 获取密钥结果处理
         *
         * @param response
         * @Description: (方法职责详细描述, 可空)
         * @Title: keyDown
         * @date 2015年11月13日 下午2:38:14
         * @author chencheng
         */
        private void keyDown(HttpResponse response) {
            logger.debug("keyDown {}", event.getId());
            try {
                HttpEntity httpEntity = response.getEntity();
                String key = EntityUtils.toString(httpEntity);
                event.setKey(key);
                event.setState(AirChinaSpiderState.LOGIN);
                return;
            } catch (Exception e) {
                logger.error("keyDown result error", e);
                event.setException(e);
                return;
            }
        }


        /**
         * 国航登录结果处理
         *
         * @param response
         * @Description: (方法职责详细描述, 可空)
         * @Title: loginDown
         * @date 2015年11月13日 下午2:38:50
         * @author chencheng
         */
        private void loginDown(HttpResponse response) {

            Map entityMap = null;
            try {
                entityMap = JSON.parseObject(EntityUtils.toString(response.getEntity()));


            } catch (ParseException e) {
                logger.error("air china loginDown error", e);
                event.setException(e);
                return;
            } catch (IOException e) {
                logger.error("air china loginDown error", e);
                event.setException(e);
                return;
            }

//          2004 {message:'验证码错误'}
//
//          1046{errorMessage:'会员卡不存在或未激活'}
//
//          2005 {errorMessage:'卡号/密码不正确<br>您还有4次机会使用网站登录，超过6次密码输入错误后，您的网上登录功能将被锁定并于60分钟后再次开放。'}
//
//          1068{errorMessage:'您登录时密码错误次数已经达到6次，为保障您的账户安全，您的账户被暂时锁定，请于60分钟后再试。'}


            if (null == entityMap.get("success")) {
                if (null != entityMap.get("message")) {
                    if ("验证码错误".equals(entityMap.get("message").toString())) {
                        event.setException(new SpiderException(2004, "验证码错误"));
                        return;
                    } else {
                        event.setException(new SpiderException(2005
                                , entityMap.get("message").toString()));
                        return;
                    }
                }
                if (null != entityMap.get("errorMessage")) {
                    if ("不正确".equals(entityMap.get("errorMessage").toString())) {
                        event.setException(new SpiderException(2005
                                , entityMap.get("errorMessage").toString()));
                        return;
                    } else if ("已经达到6次".equals(entityMap.get("errorMessage").toString())) {
                        event.setException(new SpiderException(1068
                                , entityMap.get("errorMessage").toString()));
                        return;
                    } else if ("未激活".equals(entityMap.get("errorMessage").toString())) {
                        event.setException(new SpiderException(1046
                                , entityMap.get("errorMessage").toString()));
                        return;
                    } else {
                        event.setException(new SpiderException(2005
                                , entityMap.get("errorMessage").toString()));
                        return;
                    }
                } else {

                    event.setException(new SpiderException(2005, "登录失败，用户名密码错误"));
                    return;
                }
            }
            String result = entityMap.get("success").toString();
            if (StringUtils.isNotBlank(result) && "true".equals(result)) {
                // 登录成功
                event.setState(AirChinaSpiderState.GAIN);
                return;
            } else {
                // 登录失败 , 重新获取验证码
                event.setException(new SpiderException(2005, "登录失败 , 重新获取验证码"));
                return;
            }
        }


        /**
         * 积分获取
         *
         * @param response
         * @Description: (方法职责详细描述, 可空)
         * @Title: gainDown
         * @date 2015年11月13日 下午2:41:21
         * @author chencheng
         */
        private void gainDown(HttpResponse response) {
            try {
                HttpEntity entity = response.getEntity();
                //获取各项积分
                Map<String, String> data = parserIntegral(entity);
                //System.out.println(data);
                //返回积分结果	统一约定，message赋值0
                event.setException(new SpiderException(0, "0", JSON.toJSONString(data)));
                return;
            } catch (Exception e) {
                logger.error("air china gainDown error", e);
                event.setException(new SpiderException(1029, "积分解析失败"));
                return;
            }

        }

        /**
         * 解析积分结果
         *
         * @param entity
         * @return
         * @throws IOException
         * @throws Exception
         * @Description: (方法职责详细描述, 可空)
         * @Title: parserIntegral
         * @date 2015年11月13日 下午2:41:41
         * @author chencheng
         */
        private Map<String, String> parserIntegral(HttpEntity entity)
                throws IOException, Exception {
            String fileDir = XpathHtmlUtils.deleteHeadHtml(EntityUtils.toString(entity));
            Document document = XpathHtmlUtils.getCleanHtml(fileDir);
            XPath xpath = XPathFactory.newInstance().newXPath();

            //名称
            String exp_name = "/html/body/div/div[3]/div/div[1]/span[1]";
            String name = XpathHtmlUtils.getNodeText(exp_name, xpath, document);

            //账号
            String exp_account = "/html/body/div/div[3]/div/div[1]";
            String accountMore = XpathHtmlUtils.getNodeText(exp_account, xpath, document);
            String account = accountMore
                    .substring(accountMore.indexOf("您的卡号是："), accountMore.length()).trim();
            //System.out.println(account);
            account = account.replaceAll("\r|\n|\t| ", "");
            //System.out.println(account);
            account = account.substring(6, account.indexOf("会员等级："));

            //可用里程
            String exp_available = "/html/body/div/div[3]/div/div[4]/dl/dd[1]/span";
            String available = XpathHtmlUtils.getNodeText(exp_available, xpath, document);

            //下月作废里程
            String exp_this_invalid = "//*[@id='span_nowMonthExpire']";
            String thisInvalid = XpathHtmlUtils.getNodeText(exp_this_invalid, xpath, document);

            //本期作废里程
            String exp_next_invalid = "//*[@id='span_nextMonthExpire']";
            String nextInvalid = XpathHtmlUtils.getNodeText(exp_next_invalid, xpath, document);

            Map<String, String> data = new HashMap<String, String>();
            data.put("name", name);
            data.put("account", account);
            data.put("available", StringUtils.isNotBlank(available) ? available : "0");
            data.put("thisInvalid", StringUtils.isNotBlank(thisInvalid) ? thisInvalid : "0");
            data.put("nextInvalid", StringUtils.isNotBlank(nextInvalid) ? nextInvalid : "0");
            return data;
        }

    }

}
