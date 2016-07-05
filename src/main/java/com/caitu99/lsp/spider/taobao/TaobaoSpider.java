/*
 * Copyright (c) 2015-2020 by caitu99
 * All rights reserved.
 */
package com.caitu99.lsp.spider.taobao;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.caitu99.lsp.Constant;
import com.caitu99.lsp.exception.SpiderException;
import com.caitu99.lsp.model.spider.MailSpiderEvent;
import com.caitu99.lsp.model.spider.taobao.TaoBaoSpiderEvent;
import com.caitu99.lsp.model.spider.taobao.TaoBaoSpiderState;
import com.caitu99.lsp.spider.AbstractMailSpider;
import com.caitu99.lsp.spider.MailBodySpider;
import com.caitu99.lsp.spider.MailParserTask;
import com.caitu99.lsp.utils.ScriptHelper;
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
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author Hongbo Peng
 * @Description: (类职责详细描述, 可空)
 * @ClassName: TaoBaoNewSpider
 * @date 2015年11月18日 上午10:25:07
 * @Copyright (c) 2015-2020 by caitu99
 */
public class TaobaoSpider extends AbstractMailSpider {

    private final static Logger logger = LoggerFactory.getLogger(TaobaoSpider.class);
    private static final String LOGIN_PAGE_URL = "https://login.m.taobao.com/login.htm?tpl_redirect_url=https%3A%2F%2Fh5.m.taobao.com%2Fmlapp%2Fmytaobao.html%23mlapp-mytaobao";
    private static final String HTTP = "HTTP:";
    private static final String TAOJINBI_URL = "https://api-taojinbi.taobao.com/json/user_info.htm?t=%s&_ksTS=%s_24&callback=jsonp25";
    private static final String EXPIRED_TAOJINBI_URL = "https://ajax-taojinbi.taobao.com/coin/GetUserCoinDetailJson.do?tab=3&page=1&_ksTS=%s_108&callback=jsonp109";
    private static final String TMALL_URL = "http://vip.tmall.com/api/point/MyPointSummary.do?_ksTS=%s_91&callback=jsonp92";
    private static final String TAOLICHEN_URL = "https://ffa.trip.taobao.com/userInfo.htm?callback=ALITRIP.Global._successFn";


    /**
     * @param mailBodySpider
     * @param mailParser
     * @Title:
     * @Description:
     */
    public TaobaoSpider(MailBodySpider mailBodySpider,
                        MailParserTask mailParser) {
        super(mailBodySpider, mailParser);
    }

    /* (non-Javadoc)
     * @see com.caitu99.spider.spider.IMailSpider#onEvent(com.caitu99.spider.model.MailSpiderEvent)
     */
    @Override
    public void onEvent(MailSpiderEvent event) {
        TaoBaoSpiderEvent taoBaoNewEvent = (TaoBaoSpiderEvent) event;
        try {
            switch (taoBaoNewEvent.getState()) {
                case NONE:
                    initUp(taoBaoNewEvent);
                    break;
                case CHECK:
                    checkUp(taoBaoNewEvent);
                    break;
                case LOGIN:
                    loginUp(taoBaoNewEvent);
                    break;
                case LOGIN302:
                    login302Up(taoBaoNewEvent);
                    break;
                case VCODE:
                    vCodeUp(taoBaoNewEvent);
                    break;
                case VCODE302:
                    vCode302Up(taoBaoNewEvent);
                    break;
                case RECHECK:
                    reCheckUp(taoBaoNewEvent);
                    break;
                case RECHECK302:
                    reCheck302Up(taoBaoNewEvent);
                    break;
                case TMALL:
                    tmallUp(taoBaoNewEvent);
                    break;
                case TAOJINBI:
                    taoJinBiUp(taoBaoNewEvent);
                    break;
                case EXPIREDTAOJINBI:
                    expiredTaoJinBiUp(taoBaoNewEvent);
                    break;
                case TAOLICHEN:
                    taoLiChenUp(taoBaoNewEvent);
                    break;
                case ERROR:
                    errorHandle(event);
                    break;
            }
        } catch (Exception e) {
            logger.error("request up error {}", taoBaoNewEvent.getId(), e);
            taoBaoNewEvent.setException(e);
            errorHandle(event);
        }

    }

    /**
     * @param event
     * @Description: (加载登录页面)
     * @Title: initUp
     * @date 2015年11月18日 上午10:50:48
     * @author Hongbo Peng
     */
    private void initUp(TaoBaoSpiderEvent event) {
        logger.debug("do initUp {}", event.getId());
        String s = LOGIN_PAGE_URL;
        HttpGet httpGet = new HttpGet(s);
        httpGet.setHeader("scheme", "https");
        httpGet.setHeader("version", "HTTP/1.1");
        httpGet.setHeader("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
        httpGet.setHeader("accept-language", "zh-CN,zh;q=0.8");
        httpGet.setHeader("referer", "https://h5.m.taobao.com/mlapp/mytaobao.html");
        httpGet.setHeader("upgrade-insecure-requests", "1");
        httpGet.setHeader("user-agent", "Mozilla/5.0 (Windows NT 6.3; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/45.0.2454.93 Safari/537.36");
        httpAsyncClient.execute(httpGet, new HttpAsyncCallback(event));
    }

    /**
     * @param event
     * @Description: (获取验证码二进制流)
     * @Title: checkUp
     * @date 2015年11月18日 上午11:15:07
     * @author Hongbo Peng
     */
    private void checkUp(TaoBaoSpiderEvent event) {
        logger.debug("do checkUp {}", event.getId());
        String s = event.getCodeImgUrl();
        HttpGet httpGet = new HttpGet(s);
        setHeader(s, httpGet, event);
        httpAsyncClient.execute(httpGet, new HttpAsyncCallback(event));
    }

    //登录请求
    private void loginUp(TaoBaoSpiderEvent event) {
        logger.debug("do loginUp {}", event.getId());
        Map<String, String> rsaParams = event.getRsaParams();
        Map<String, String> params = event.getLoginParams();
        try {
            String pwd = ScriptHelper.encryptTaoBaoPassword(rsaParams.get("J_Exponent"), rsaParams.get("J_Module"), event.getPassword());
            params.put("TPL_username", event.getAccount());
            params.put("TPL_checkcode", event.getImgCode());
            params.put("TPL_password2", pwd);
            List<NameValuePair> nvps = converForMap(params);

            HttpPost httpPost = new HttpPost(event.getLoginActionUrl());
            setHeader(event.getLoginActionUrl(), httpPost, event);

            httpPost.setHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
            httpPost.setHeader("Upgrade-Insecure-Requests", "1");

            httpPost.setEntity(new UrlEncodedFormEntity(nvps, "UTF-8"));

            httpAsyncClient.execute(httpPost, new HttpAsyncCallback(event));
        } catch (Exception e) {
            logger.error("taobao error loginUp eventId:{} error {}", event.getId(), e);
            event.setException(e);
            return;
        }
    }

    //登录转向请求
    private void login302Up(TaoBaoSpiderEvent event) {
        logger.debug("do login302Up {}", event.getId());
        String s = event.getLogin302Url();
        HttpGet httpGet = new HttpGet(s);
        setHeader(s, httpGet, event);
        httpAsyncClient.execute(httpGet, new HttpAsyncCallback(event));
    }

    //发送短信验证请求
    private void vCodeUp(TaoBaoSpiderEvent event) {
        try {
            Map<String, String> params = event.getvCodeParams();
            List<NameValuePair> nvps = converForMap(params);
            HttpPost httpPost = new HttpPost(event.getvCodeActionUrl());
            setHeader(event.getLoginActionUrl(), httpPost, event);
            try {
                httpPost.setEntity(new UrlEncodedFormEntity(nvps, "UTF-8"));
            } catch (UnsupportedEncodingException e) {
                logger.error("login eventId:{} error {}", event.getId(), e);
                event.setException(e);
                return;
            }
            httpAsyncClient.execute(httpPost, new HttpAsyncCallback(event));
        } catch (Exception e) {
            logger.error("taobao error vCodeUp eventId:{} error {}", event.getId(), e);
            event.setException(e);
            return;
        }
    }

    //发送短信验证码转向请求
    private void vCode302Up(TaoBaoSpiderEvent event) {
        logger.debug("do vCode302Up {}", event.getId());
        String s = event.getvCode302Url();
        HttpGet httpGet = new HttpGet(s);
        setHeader(s, httpGet, event);
        httpGet.setHeader("scheme", "https");
        httpGet.setHeader("version", "HTTP/1.1");
        httpGet.setHeader("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
//		httpGet.setHeader("accept-encoding", "gzip, deflate, sdch");
        httpGet.setHeader("accept-language", "zh-CN,zh;q=0.8");
        httpGet.setHeader("cache-control", "max-age=0");
//		httpGet.setHeader("referer", "https://login.m.taobao.com/send_code.htm?token=0e8bc8b20c41efa9f07d471cadecf13c&TPL_redirect_url=https%3A%2F%2Fh5.m.taobao.com%2Fmlapp%2Fmytaobao.html%23mlapp-mytaobao&ssottid=&t=IS_NEED_2_CHECK&sid=1c044a76b1b94e7d46012752b5de9e07");
        httpGet.setHeader("upgrade-insecure-requests", "1");

        httpAsyncClient.execute(httpGet, new HttpAsyncCallback(event));
    }

    //短信验证码验证
    private void reCheckUp(TaoBaoSpiderEvent event) {

        try {
            Map<String, String> params = event.getvCodeParams();
            params.put("checkCode", event.getImgCode());
            params.remove("from");
            params.remove("event_submit_do_gen_check_code");

            List<NameValuePair> nvps = converForMap(params);
            String url = event.getvCodeActionUrl();
            HttpPost httpPost = new HttpPost(url);
            setHeader(url, httpPost, event);

            httpPost.setHeader("Connection", "keep-alive");
            httpPost.setHeader("Cache-Control", "max-age=0");
            httpPost.setHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
            httpPost.setHeader("Origin", "https://login.m.taobao.com");
            httpPost.setHeader("Upgrade-Insecure-Requests", "1");
            httpPost.setHeader("Content-Type", "application/x-www-form-urlencoded");
            httpPost.setHeader("Referer", "https://login.m.taobao.com/login_check.htm?_input_charset=utf-8&sid=107ac5963d9ec290ea7daf1442826faa");

            try {
                httpPost.setEntity(new UrlEncodedFormEntity(nvps, "UTF-8"));
            } catch (UnsupportedEncodingException e) {
                logger.error("login eventId:{} error {}", event.getId(), e);
                event.setException(e);
                return;
            }
            httpAsyncClient.execute(httpPost, new HttpAsyncCallback(event));
        } catch (Exception e) {
            logger.error("taobao error reCheckUp eventId:{} error {}", event.getId(), e);
            event.setException(e);
            return;
        }
    }

    //短信验证转向请求
    private void reCheck302Up(TaoBaoSpiderEvent event) {

        logger.debug("do reCheck302Up {}", event.getId());
        String s = event.getReVCode302Url();
        HttpGet httpGet = new HttpGet(s);
        setHeader(s, httpGet, event);
        httpAsyncClient.execute(httpGet, new HttpAsyncCallback(event));

    }

    //获取天猫积分请求
    private void tmallUp(TaoBaoSpiderEvent event) {
        String url = String.format(TMALL_URL, new Date().getTime());
        //"http://vip.tmall.com/api/point/MyPointSummary.do?_ksTS="+new Date().getTime()+"_91&callback=jsonp92";
        HttpGet httpGet = new HttpGet(url);

        setHeader(url, httpGet, event);
        httpGet.setHeader("accept", "text/javascript, application/javascript, application/ecmascript, application/x-ecmascript, */*; q=0.01");
        httpGet.setHeader("accept-encoding", "gzip, deflate, sdch");
        httpGet.setHeader("accept-language", "zh-CN,zh;q=0.8");
        httpGet.setHeader("Cache-Control", "max-age=0");
        httpGet.setHeader("Connection", "keep-alive");
        httpGet.setHeader("referer", "http://vip.tmall.com/point/detail/all?spm=0.0.0.0.2hYSwa&from=top&scm=1027.1.1.4");
        httpGet.setHeader("user-agent", "Mozilla/5.0 (Windows NT 6.3; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/45.0.2454.93 Safari/537.36");
//		httpGet.setHeader("X-Requested-With","XMLHttpRequest");

        httpAsyncClient.execute(httpGet, new HttpAsyncCallback(event));

    }

    //获取淘金币请求
    private void taoJinBiUp(TaoBaoSpiderEvent event) {
        Long t = new Date().getTime();
        String url = String.format(TAOJINBI_URL, t, t + 1);
        HttpGet httpget = new HttpGet(url);
        setHeader(url, httpget, event);
        httpget.setHeader("scheme", "https");
        httpget.setHeader("version", "HTTP/1.1");
        httpget.setHeader("accept", "*/*");
        httpget.setHeader("accept-encoding", "gzip, deflate, sdch");
        httpget.setHeader("accept-language", "zh-CN,zh;q=0.8");
        httpget.setHeader("referer", "https://taojinbi.taobao.com/coin/userCoinDetail.htm?spm=a217e.7256925.1997946877.1.5zwDZs");
        httpget.setHeader("user-agent", "Mozilla/5.0 (Windows NT 6.3; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/45.0.2454.93 Safari/537.36");

        httpAsyncClient.execute(httpget, new HttpAsyncCallback(event));
    }

    //获取淘里程请求
    private void taoLiChenUp(TaoBaoSpiderEvent event) {
        String url = TAOLICHEN_URL;
        //"https://ffa.trip.taobao.com/userInfo.htm?callback=ALITRIP.Global._successFn";
        HttpGet httpget = new HttpGet(url);
        setHeader(url, httpget, event);
        httpget.setHeader("scheme", "https");
        httpget.setHeader("version", "HTTP/1.1");
        httpget.setHeader("accept", "*/*");
//		httpget.setHeader("accept-encoding", "gzip, deflate, sdch");
        httpget.setHeader("accept-language", "zh-CN,zh;q=0.8");
        httpget.setHeader("Cache-Control", "max-age=0");
        httpget.setHeader("referer", "https://www.alitrip.com/mytrip/?spm=181.7091613.a1z68.39.zrFrSZ");
        httpget.setHeader("user-agent", "Mozilla/5.0 (Windows NT 6.3; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/45.0.2454.93 Safari/537.36");

        httpAsyncClient.execute(httpget, new HttpAsyncCallback(event));
    }

    //获取过期淘金币请求
    private void expiredTaoJinBiUp(TaoBaoSpiderEvent event) {

        //Long t = new Date().getTime();
        String url = String.format(EXPIRED_TAOJINBI_URL, new Date().getTime());
        //"https://ajax-taojinbi.taobao.com/coin/GetUserCoinDetailJson.do?tab=3&page=1&_ksTS="
        //		+ t + "_108&callback=jsonp109";

        HttpGet httpget = new HttpGet(url);
        setHeader(url, httpget, event);
        httpget.setHeader("scheme", "https");
        httpget.setHeader("version", "HTTP/1.1");
        httpget.setHeader("accept", "*/*");
//		httpget.setHeader("accept-encoding", "gzip, deflate, sdch");
        httpget.setHeader("accept-language", "zh-CN,zh;q=0.8");
        httpget.setHeader("Cache-Control", "max-age=0");
        httpget.setHeader("referer",
                "https://taojinbi.taobao.com/coin/userCoinDetail.htm?spm=a217e.7256925.1997946877.1.5zwDZs");
        httpget.setHeader("user-agent", "Mozilla/5.0 (Windows NT 6.3; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/45.0.2454.93 Safari/537.36");

        httpAsyncClient.execute(httpget, new HttpAsyncCallback(event));
    }


    private List<NameValuePair> converForMap(Map<String, String> signParams) {
        List<NameValuePair> nvps = new ArrayList<NameValuePair>();
        Set<String> keys = signParams.keySet();
        for (String key : keys) {
            nvps.add(new BasicNameValuePair(key, signParams.get(key)));
        }
        return nvps;
    }

    /* (non-Javadoc)
     * @see com.caitu99.spider.spider.AbstractMailSpider#setHeader(java.lang.String, org.apache.http.HttpMessage, com.caitu99.spider.model.MailSpiderEvent)
     */
    @Override
    protected void setHeader(String uriStr, HttpMessage httpGet,
                             MailSpiderEvent event) {
        httpGet.setHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
        httpGet.setHeader("User-Agent", AbstractMailSpider.USERAGENT_CHROME);
        try {
            CookieHelper.setCookies(uriStr, httpGet, event.getCookieList());
        } catch (URISyntaxException e) {
            logger.error("set cookie fail {}", event.getId());
        }

    }

    /**
     * @author Hongbo Peng
     * @Description: (异步回调处理)
     * @ClassName: HttpAsyncCallback
     * @date 2015年11月18日 上午11:26:19
     * @Copyright (c) 2015-2020 by caitu99
     */
    private class HttpAsyncCallback implements FutureCallback<HttpResponse> {

        private TaoBaoSpiderEvent event;
        private boolean skipNextStep = false;

        /**
         * @param event
         * @Title:
         * @Description:
         */
        public HttpAsyncCallback(TaoBaoSpiderEvent event) {
            super();
            this.event = event;
        }

        /* (non-Javadoc)
         * @see org.apache.http.concurrent.FutureCallback#completed(java.lang.Object)
         */
        @Override
        public void completed(HttpResponse result) {
            try {
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
                    case LOGIN302:
                        login302Down(result);
                        break;
                    case VCODE:
                        vCodeDown(result);
                        break;
                    case VCODE302:
                        vCode302Down(result);
                        break;
                    case RECHECK:
                        reCheckDown(result);
                        break;
                    case RECHECK302:
                        reCheck302Down(result);
                        break;
                    case TMALL:
                        tmallDown(result);
                        break;
                    case TAOJINBI:
                        taojinbiDown(result);
                        break;
                    case EXPIREDTAOJINBI:
                        expiredTaoJinBiDown(result);
                        break;
                    case TAOLICHEN:
                        taoLiChengDown(result);
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

        /* (non-Javadoc)
         * @see org.apache.http.concurrent.FutureCallback#failed(java.lang.Exception)
         */
        @Override
        public void failed(Exception e) {
            logger.debug("request {} failed: {}", event.getId(), e.getMessage());
        }

        /* (non-Javadoc)
         * @see org.apache.http.concurrent.FutureCallback#cancelled()
         */
        @Override
        public void cancelled() {
            logger.debug("request cancelled: {}", event.getId());
        }

        /**
         * @param response
         * @Description: (加载登录页面处理，拿到验证码路径)
         * @Title: initDown
         * @date 2015年11月18日 上午11:08:50
         * @author Hongbo Peng
         */
        private void initDown(HttpResponse response) {
            logger.debug("check down {}", event.getId());
            try {
                //解析登录页面，获取验证码URL和登录必须参数
                HttpEntity entity = response.getEntity();
                String page = EntityUtils.toString(entity);
                String fileDir = XpathHtmlUtils.deleteHeadHtml(page);
                //System.out.println(fileDir);

                String codeImageUrl = HTTP + getCodeImgUrl(fileDir);
                Map<String, String> loginParams = getLoginParam(fileDir);
                String loginActionUrl = getLoginActionUrl(fileDir);
                Map<String, String> rsaParams = getRSAParam(fileDir);

                event.setCodeImgUrl(codeImageUrl);//验证码图片路径
                event.setLoginParams(loginParams);//登录必须参数
                event.setLoginActionUrl(loginActionUrl);//登录请求地址
                event.setRsaParams(rsaParams);//加密参数
                event.setState(TaoBaoSpiderState.CHECK);
                return;
            } catch (Exception e) {
                logger.error("get result exception", e);
                event.setException(e);
                return;
            }
        }

        /**
         * @param response
         * @Description: (验证码二进制流获取处理)
         * @Title: checkDown
         * @date 2015年11月18日 上午11:19:40
         * @author Hongbo Peng
         */
        private void checkDown(HttpResponse response) {
            logger.debug("checkDown {}", event.getId());
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

                // save to file, used for debug
                /*if (appConfig.inDevMode()) {
                    byte[] tbytes = Base64.getDecoder().decode(imgStr);
					FileOutputStream fs = new FileOutputStream("D:\\1.jpg");
					fs.write(tbytes);
					fs.close();
				}*/

                // 记录事件当前步骤
                event.setState(TaoBaoSpiderState.LOGIN); // next step is to login
                // 缓存当前事件内容
                String key = String.format(Constant.TAOBAO_IMPORT_KEY, event.getUserid());
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
         * @param response
         * @Description: (登录结果处理)
         * @Title: loginDown
         * @date 2015年11月18日 下午12:07:04
         * @author Hongbo Peng
         */
        private void loginDown(HttpResponse response) {
            try {
                int status = response.getStatusLine().getStatusCode();
                if (302 == status) {
                    String locationUrl = response.getLastHeader("Location").getValue();
                    //登录成功
                    if (-1 != locationUrl.indexOf("welcome")) {
                        event.setState(TaoBaoSpiderState.TAOLICHEN);
                        return;
                    }
                    event.setLogin302Url(locationUrl);
                    event.setState(TaoBaoSpiderState.LOGIN302);
                    return;
                } else {
                    //logger.info("taobao login error status:", status);
                    event.setException(new SpiderException(1013, "登录失败"));
                    return;
                }
            } catch (Exception e) {
                logger.error("taobao login error", e);
                event.setException(e);
                return;
            }
        }

        /**
         * 1.用户名密码错误
         * 2.验证码错误
         * 3.用户被锁定
         * 4.需要短信验证码
         *
         * @param response
         * @Description: (处理登录后跳转页面)
         * @Title: login302Down
         * @date 2015年11月18日 下午12:26:17
         * @author Hongbo Peng
         */
        private void login302Down(HttpResponse response) {
            try {
                int status = response.getStatusLine().getStatusCode();
                if (302 == status) {
                    String locationUrl = response.getLastHeader("Location").getValue();
                    //登录成功
                    if (-1 != locationUrl.indexOf("welcome")) {
                        event.setState(TaoBaoSpiderState.TMALL);
                        return;
                    }
                    event.setLogin302Url(locationUrl);
                    event.setState(TaoBaoSpiderState.LOGIN302);
                    return;
                }

                HttpEntity entity = response.getEntity();
                String entityStr = EntityUtils.toString(entity);
                String fileDir = XpathHtmlUtils.deleteHeadHtml(entityStr);
                Document document = XpathHtmlUtils.getCleanHtml(fileDir);
                XPath xpath = XPathFactory.newInstance().newXPath();

                //1 验证码错误
                /*String error = "//p[@class='red-box wrong-tip']";
				String errorText = XpathHtmlUtils.getNodeText(error, xpath, document);*/
                if (fileDir.contains("验证码错误，请重新输入")) {
                    //logger.error("taobao login error status: 验证码错误，请重新输入");
                    event.setException(new SpiderException(1044, "验证码错误，请重新输入"));
                    return;
                }

                // 3输入的密码和账户名不匹配
                if (fileDir.contains("输入的密码和账户名不匹配")) {
                    //logger.error("taobao login error status: 输入的密码和账户名不匹配");
                    event.setException(new SpiderException(1045, "输入的密码和账户名不匹配"));
                    return;
                }

                // 5短信验证码
                String needVCode = "//input[@type='submit']";
                String needText = XpathHtmlUtils.getNodeValue(needVCode, xpath, document);
                if ("获取验证码".equals(needText) && entityStr.contains("二次验证")) {
                    Map<String, String> vCodeParam = getLoginParam(fileDir);
                    String vCodeActionUrl = "https:" + getLoginActionUrl(fileDir);
                    event.setvCodeParams(vCodeParam);
                    event.setvCodeActionUrl(vCodeActionUrl);
                    event.setState(TaoBaoSpiderState.VCODE);
                    return;
                }

                // 6失败
                event.setException(new SpiderException(1045, "请重新登录", ""));
                return;
            } catch (Exception e) {
                logger.error("taobao login302 error", e);
                event.setException(e);
                return;
            }
        }


        /**
         * @param response
         * @Description: (发送短信验证码处理结果)
         * @Title: vCodeDown
         * @date 2015年11月18日 下午4:35:48
         * @author Hongbo Peng
         */
        private void vCodeDown(HttpResponse response) {
            try {
                int status = response.getStatusLine().getStatusCode();
                if (302 == status) {
                    //跳转至短信发送
                    String locationUrl = response.getLastHeader("Location").getValue();
                    event.setvCode302Url(locationUrl);
                    event.setState(TaoBaoSpiderState.VCODE302);
                    return;
                } else if (200 == status) {

                    vCode302Down(response);
                } else {
                    //logger.error("taobao vCodeDown error status:", status);
                    event.setException(new SpiderException(1013, "短信验证码发送失败"));
                    return;
                }
            } catch (Exception e) {
                logger.error("taobao vCodeDown error", e);
                event.setException(e);
                return;
            }
        }

        /**
         * 发送短信验证码转向结果处理
         *
         * @param response
         * @Description: (方法职责详细描述, 可空)
         * @Title: vCode302Down
         * @date 2015年11月24日 上午11:19:00
         * @author ws
         */
        private void vCode302Down(HttpResponse response) {
            try {
                int status = response.getStatusLine().getStatusCode();
                if (302 == status) {
                    String locationUrl = response.getLastHeader("Location").getValue();
                    //登录成功
                    if (-1 != locationUrl.indexOf("welcome")) {
                        event.setState(TaoBaoSpiderState.TMALL);
                        return;
                    }
                    event.setLogin302Url(locationUrl);
                    event.setState(TaoBaoSpiderState.LOGIN302);
                    return;
                }

                HttpEntity entity = response.getEntity();
                String entityStr = EntityUtils.toString(entity);
                String fileDir = XpathHtmlUtils.deleteHeadHtml(entityStr);

                Map<String, String> vCodeParam = getLoginParam(fileDir);
                String vCodeActionUrl = "https:" + getLoginActionUrl(fileDir);
                if (vCodeActionUrl.contains("/login_check.htm?_input_charset")) {

                    fileDir = fileDir.replaceAll("\r|\n|\t| ", "");
                    String tValue = fileDir.substring(fileDir.indexOf("vart=\""), fileDir.indexOf("\";if(\""));
                    tValue = tValue.substring(6);
                    //System.out.println(tValue);
                    //fileDir = fileDir.replaceAll( "\"", "\'");
                    //int count = StringUtils.countMatches(fileDir, "t=\"4\"");
//
//					var t = "4";
//					if("-1"!="-1"){
//					          floatNotify.simple("您未绑定手机号码，请去往www.taobao.com设置绑定");
//					       }else{
//					if("")
//					   floatNotify.simple("$message");
//					if(t == "0")
//					          floatNotify.simple("验证码为空,请输入验证码");
//					if(t == "1")
//					           floatNotify.simple("获取验证码失败,请重新获取");
//					if(t == "2")
//					   floatNotify.simple("验证码错误，请重试");
//					if(t == "3")
//					   floatNotify.simple("消息通道忙，请15分钟后再试");
//					if(t == "4")
//					   floatNotify.simple("验证码已发送，请查收短信");
//					 }

                    if ("4".equals(tValue)) {
                        event.setvCodeParams(vCodeParam);
                        event.setvCodeActionUrl(vCodeActionUrl);
                        event.setState(TaoBaoSpiderState.RECHECK);

                        //存入缓存
                        String key = String.format(Constant.TAOBAO_IMPORT_KEY, event.getUserid());
                        redis.set(key, JSON.toJSONString(event), 900); //900秒超时
                        event.setException(new SpiderException(1051, "请输入短信验证码", ""));
                        return;
                    } else {
                        //
						/*
						if("3".equals(tValue)){
							event.setException(new SpiderException(1013, "消息通道忙，请15分钟后再试"));
						}*/
                        event.setException(new SpiderException(1013, "消息通道忙，请15分钟后再试"));
                    }
                } else {

                    event.setException(new SpiderException(1013, "短信验证码发送失败"));
                }
            } catch (Exception e) {
                logger.error("taobao vCodeDown error", e);
                event.setException(e);
                return;
            }
        }

        /**
         * @param response
         * @Description: (二次验证处理结果)
         * @Title: reCheckDown
         * @date 2015年11月18日 下午4:36:35
         * @author Hongbo Peng
         */
        private void reCheckDown(HttpResponse response) {
            try {
                int status = response.getStatusLine().getStatusCode();
                if (302 == status) {
                    String locationUrl = response.getLastHeader("Location").getValue();

                    //登录成功
                    if (-1 != locationUrl.indexOf("welcome")) {
                        event.setState(TaoBaoSpiderState.TMALL);
                        return;
                    }
                    //否则继续跳转
                    event.setReVCode302Url(locationUrl);
                    event.setState(TaoBaoSpiderState.RECHECK302);
                    return;
                } else if (200 == status) {
                    reCheck302Down(response);
                } else {
                    //logger.error("taobao vCodeDown error status:", status);
                    event.setException(new SpiderException(1052, "短信验证码验证失败"));
                    return;
                }
            } catch (Exception e) {
                logger.error("taobao reCheckDown error", e);
                event.setException(e);
                return;
            }
        }

        /**
         * 短信验证码验证
         *
         * @param response
         * @Description: (方法职责详细描述, 可空)
         * @Title: reCheck302Down
         * @date 2015年11月24日 上午11:19:32
         * @author ws
         */
        private void reCheck302Down(HttpResponse response) {

            try {
                int status = response.getStatusLine().getStatusCode();
                if (302 == status) {
                    String locationUrl = response.getLastHeader("Location").getValue();

                    //登录成功
                    if (-1 != locationUrl.indexOf("welcome")) {
                        event.setState(TaoBaoSpiderState.TMALL);
                        return;
                    }
                    //否则继续跳转
                    event.setReVCode302Url(locationUrl);
                    event.setState(TaoBaoSpiderState.RECHECK302);
                    return;
                } else if (200 == status) {

                    HttpEntity entity = response.getEntity();
                    String entityStr = EntityUtils.toString(entity);
                    String fileDir = XpathHtmlUtils.deleteHeadHtml(entityStr);

                    //登录成功
                    if (-1 != fileDir.indexOf("5秒后跳回前页")) {
                        event.setState(TaoBaoSpiderState.TMALL);
                        return;
                    }

                    fileDir = fileDir.replaceAll("\r|\n|\t| ", "");
                    String errMsg = fileDir.substring(fileDir.indexOf("else{if("), fileDir.indexOf("if(t=="));

                    errMsg = errMsg.substring(errMsg.indexOf("simple("), errMsg.indexOf(";"));
                    errMsg = errMsg.substring(8, errMsg.length() - 2);//去掉   simple("  和    ")

                    //存在$message的情况
                    if (StringUtils.isNotBlank(errMsg) && !"$message".equals(errMsg)) {

                        event.setException(new SpiderException(1052, errMsg, ""));
                    } else {
                        event.setException(new SpiderException(1052, "短信验证码错误", ""));
                    }
                    return;
                } else {
                    //logger.error("taobao vCodeDown error status:", status);
                    event.setException(new SpiderException(1052, "短信验证码验证失败"));
                    return;
                }
            } catch (Exception e) {
                logger.error("taobao reCheck302Down error", e);
                event.setException(new SpiderException(1052, "短信验证码验证失败"));
                return;
            }
        }

        /**
         * 天猫积分获取
         *
         * @param response
         * @Description: (方法职责详细描述, 可空)
         * @Title: tmallDown
         * @date 2015年11月24日 上午11:21:36
         * @author ws
         */
        private void tmallDown(HttpResponse response) {
            try {

                HttpEntity entity = response.getEntity();
                String entityStr = StringUtils.trim(EntityUtils.toString(entity));

                Map<String, Object> resultMap = new HashMap<String, Object>();
                String jsonStr = entityStr.substring(8, entityStr.length() - 2);
                logger.debug("tmallinteger json : {}", jsonStr);
                JSONObject object = JSONObject.parseObject(jsonStr);
                if ("true".equals(object.getString("success"))) {

                    resultMap.put("tmallInteger", object.get("model"));
                }

                event.setResultParams(resultMap);
                event.setState(TaoBaoSpiderState.TAOJINBI);
                return;
            } catch (Exception e) {
                logger.error("taobao tmallDown error", e);
                event.setException(e);
                return;
            }
        }

        /**
         * 淘金币获取
         *
         * @param response
         * @Description: (方法职责详细描述, 可空)
         * @Title: taojinbiDown
         * @date 2015年11月24日 上午11:22:02
         * @author ws
         */
        private void taojinbiDown(HttpResponse response) {
            try {
                HttpEntity entity = response.getEntity();
                String entityStr = StringUtils.trim(EntityUtils.toString(entity));

                String jsonStr = entityStr.substring(8, entityStr.length() - 1);
                logger.debug("taojinbi json : {}", jsonStr);
                JSONObject object = JSONObject.parseObject(jsonStr);
                if (null != object && "true".equals(object.getString("isSuccess"))
                        && "true".equals(object.getString("isLogin"))) {
                    event.getResultParams().put("taojinbi", jsonStr);
                }

                event.setState(TaoBaoSpiderState.TAOLICHEN);
                return;
            } catch (Exception e) {
                logger.error("taobao tmallDown error", e);
                event.setException(e);
                return;
            }
        }

        /**
         * 淘里程获取
         *
         * @param response
         * @Description: (方法职责详细描述, 可空)
         * @Title: taoLiChengDown
         * @date 2015年11月24日 上午11:22:13
         * @author ws
         */
        private void taoLiChengDown(HttpResponse response) {
            try {
                HttpEntity entity = response.getEntity();
                String entityStr = StringUtils.trim(EntityUtils.toString(entity));
                String jsonStr = entityStr.substring(30, entityStr.length() - 12);
                logger.debug("taolichen json : {}", jsonStr);
                JSONObject object = JSONObject.parseObject(jsonStr);
                if ("true".equals(object.getString("isLogin"))) {
                    event.getResultParams().put("taolichen", jsonStr);
                }

                event.setState(TaoBaoSpiderState.EXPIREDTAOJINBI);
                return;
            } catch (Exception e) {
                logger.error("taobao tmallDown error", e);
                event.setException(e);
                return;
            }
        }


        private void expiredTaoJinBiDown(HttpResponse response) {
            try {
                HttpEntity entity = response.getEntity();
                String entityStr = StringUtils.trim(EntityUtils.toString(entity));
                //System.out.println(entityStr);

                String jsonStr = entityStr.substring(9, entityStr.length() - 1);
                logger.debug("ExpiredTaojinbi json :{}", jsonStr);
                JSONObject object = JSONObject.parseObject(jsonStr);
                if (null != object && "true".equals(object.getString("success")) && "true".equals(object.getString("isLogin"))) {
                    event.getResultParams().put("expiredTaojinbi", getCoinMonthRecords(object.getString("coinMonthRecords")));
                }

                event.setState(TaoBaoSpiderState.EXPIREDTAOJINBI);
                event.setException(new SpiderException(0, "success", JSON.toJSONString(event.getResultParams())));
                return;
            } catch (Exception e) {
                logger.error("taobao tmallDown error", e);
                event.setException(e);
                return;
            }
        }

        /**
         * 获取图片验证码请求地址
         *
         * @param loginPage
         * @return
         * @Description: (方法职责详细描述, 可空)
         * @Title: getCodeImgUrl
         * @date 2015年11月24日 上午11:23:09
         * @author ws
         */
        private String getCodeImgUrl(String loginPage) {
            try {
                Document document = XpathHtmlUtils.getCleanHtml(loginPage);
                Element img = (Element) document.getElementsByTagName("img").item(0);
                String codeUrl = img.getAttribute("src");
                return codeUrl;
            } catch (Exception e) {
                e.printStackTrace();
                throw e;
            }
        }

        /**
         * 获取登录所需参数
         *
         * @param loginPage
         * @return
         * @Description: (方法职责详细描述, 可空)
         * @Title: getLoginParam
         * @date 2015年11月24日 上午11:23:36
         * @author ws
         */
        private Map<String, String> getLoginParam(String loginPage) {
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
                        params.put(name, e.getAttribute("value"));
                    }
                }
                return params;
            } catch (Exception e) {
                e.printStackTrace();
                throw e;
            }
        }

        /**
         * 获取登录请求地址
         *
         * @param loginPage
         * @return
         * @Description: (方法职责详细描述, 可空)
         * @Title: getLoginActionUrl
         * @date 2015年11月24日 上午11:23:51
         * @author ws
         */
        private String getLoginActionUrl(String loginPage) {
            try {
                Document document = XpathHtmlUtils.getCleanHtml(loginPage);
                Element form = (Element) document.getElementsByTagName("form").item(0);
                String action = form.getAttribute("action");
                return action;
            } catch (Exception e) {
                e.printStackTrace();
                throw e;
            }
        }

        /**
         * 获取登录所需RSA参数
         *
         * @param loginPage
         * @return
         * @throws Exception
         * @Description: (方法职责详细描述, 可空)
         * @Title: getRSAParam
         * @date 2015年11月24日 上午11:24:08
         * @author ws
         */
        private Map<String, String> getRSAParam(String loginPage) throws Exception {
            Map<String, String> params = new HashMap<String, String>();
            try {
                Document document = XpathHtmlUtils.getCleanHtml(loginPage);
                //System.out.println(document.getDocumentElement());
                XPath xpath = XPathFactory.newInstance().newXPath();
                String exp = "//input[@id='J_Exponent']";
                String exponent = XpathHtmlUtils.getNodeValue(exp, xpath, document);
                params.put("J_Exponent", exponent);
                exp = "//input[@id='J_Module']";
                String module = XpathHtmlUtils.getNodeValue(exp, xpath, document);
                params.put("J_Module", module);
                return params;
            } catch (Exception e) {
                throw e;
            }
        }

        /**
         * 淘金币日期处理
         *
         * @param coinMonthRecords
         * @return
         * @throws Exception
         * @Description: (方法职责详细描述, 可空)
         * @Title: getCoinMonthRecords
         * @date 2015年11月24日 上午11:24:25
         * @author ws
         */
        private String getCoinMonthRecords(String coinMonthRecords) throws Exception {
            Map<String, Object> map = new HashMap<String, Object>();
            JSONArray jsonArray = JSONArray.parseArray(coinMonthRecords);
            Long now = new Date().getTime();
            for (int i = 0; i < jsonArray.size(); i++) {
                JSONObject obj = (JSONObject) jsonArray.get(i);
                String timestr = obj.getString("usableTime");
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy年MM月dd日");
                Date d = sdf.parse(timestr);
                if (d.getTime() < now) {
                    continue;
                }
                if (null != map.get(obj.getString("usableTime"))) {
                    int amountFree = Integer.parseInt(map.get("amountFree").toString());
                    amountFree += obj.getInteger("amountFree");
                    map.put("amountFree", amountFree);
                } else {
                    map.put("amountFree", obj.getInteger("amountFree"));
                    map.put("usableTime", obj.getString("usableTime"));
                }
            }
            return JSON.toJSONString(map);
        }


    }

}
