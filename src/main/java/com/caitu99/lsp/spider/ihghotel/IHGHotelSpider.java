package com.caitu99.lsp.spider.ihghotel;

import com.alibaba.fastjson.JSON;
import com.caitu99.lsp.exception.SpiderException;
import com.caitu99.lsp.model.spider.QueryEvent;
import com.caitu99.lsp.model.spider.ihghotel.IHGHotelEvent;
import com.caitu99.lsp.model.spider.ihghotel.IHGHotelState;
import com.caitu99.lsp.spider.HttpAsyncClient;
import com.caitu99.lsp.spider.QuerySpider;
import com.caitu99.lsp.utils.XpathHtmlUtils;
import com.caitu99.lsp.utils.cookie.CookieHelper;
import org.apache.commons.lang.StringUtils;
import org.apache.http.Header;
import org.apache.http.HttpMessage;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.request.async.DeferredResult;
import org.w3c.dom.Document;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

public class IHGHotelSpider implements QuerySpider {

    private static final Logger logger = LoggerFactory
            .getLogger(IHGHotelSpider.class);
    private static final String userAgent = "Mozilla/5.0 (Windows NT 6.3; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/46.0.2490.86 Safari/537.36";
    private static final String loginPageUrl = "http://cn.ihg.com/ihg";
    private static final String loginUrl = "https://cn.ihg.com/ihg/pcrlogin";
    private static final String loginPageRef = "http://cn.ihg.com/ihg";
    private CloseableHttpAsyncClient httpAsyncClient = HttpAsyncClient
            .getInstance();

    @Override
    public void onEvent(QueryEvent event) {
        IHGHotelEvent ihgEvent = (IHGHotelEvent) event;
        try {
            switch (ihgEvent.getState()) {
                case LOGINPAGE:
                    loginPageUp(ihgEvent);
                    break;
                case LOGIN:
                    loginUp(ihgEvent);
                    break;
                case HOMEPAGE:
                    homePageUp(ihgEvent);
                    break;
                case ERROR:
                    errorHandle(ihgEvent);
                    break;
            }
        } catch (Exception e) {
            logger.error("request up error {}", event.getId(), e);
            ihgEvent.setException(e);
            errorHandle(ihgEvent);
        }
    }

    private void loginPageUp(IHGHotelEvent ihgEvent) {
        logger.debug("do login page up {}", ihgEvent.getId());
        HttpGet httpGet = new HttpGet(loginPageUrl);
        setHeader(loginPageUrl, httpGet, ihgEvent);
        httpAsyncClient.execute(httpGet, new HttpAsyncCallback(ihgEvent));
    }

    private void loginUp(IHGHotelEvent ihgEvent) {
        logger.debug("do login up {}", ihgEvent.getId());
        HttpPost httpPost = new HttpPost(loginUrl);
        try {
            httpPost.setEntity(new UrlEncodedFormEntity(
                    createLoginFormEntity(ihgEvent), "UTF-8"));
        } catch (Exception e) {
            logger.error("encode login body error {}", ihgEvent.getId(), e);
            ihgEvent.setException(e);
            return;
        }
        setHeader(loginUrl, httpPost, ihgEvent);
        httpPost.setHeader("Referer", loginPageRef);
        httpAsyncClient.execute(httpPost, new HttpAsyncCallback(ihgEvent));
    }

    private void homePageUp(IHGHotelEvent ihgEvent) {
        logger.debug("do home page up {}", ihgEvent.getId());
        HttpGet httpGet = new HttpGet(ihgEvent.getLocation());
        setHeader(ihgEvent.getLocation(), httpGet, ihgEvent);
        httpGet.setHeader("Referer", loginPageRef);
        httpAsyncClient.execute(httpGet, new HttpAsyncCallback(ihgEvent));
    }

    private void setHeader(String uriStr, HttpMessage httpGet, QueryEvent event) {
        httpGet.setHeader("Accept", "*/*");
        httpGet.setHeader("User-Agent", userAgent);
        try {
            CookieHelper.setCookies(uriStr, httpGet, event.getCookieList());
        } catch (URISyntaxException e) {
            logger.error("set cookie fail {}", event.getId());
        }
    }

    @Override
    public void errorHandle(QueryEvent event) {
        DeferredResult<Object> deferredResult = event.getDeferredResult();

        if (deferredResult == null) {
            // the result has bean returned to user
            return;
        }

        if (deferredResult.isSetOrExpired()) {
            logger.debug("a request has been expired: {}", event);
            return;
        }

        Exception exception = event.getException();
        if (exception != null) {
            if (exception instanceof SpiderException) {
                deferredResult.setResult(exception.toString());
            } else {
                logger.error("unknown exception", exception);
                deferredResult.setResult((new SpiderException(-1, exception
                        .getMessage()).toString()));
            }
        }
    }

    private List<? extends NameValuePair> createLoginFormEntity(
            IHGHotelEvent event) {
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("pcrLoginFromPopup", "true"));
        params.add(new BasicNameValuePair("pcrAccountId", event.getAccount()));
        params.add(new BasicNameValuePair("pcr_login_pwd_help", "密码"));
        params.add(new BasicNameValuePair("pcrAccountPin", event.getPassword()));
        params.add(new BasicNameValuePair("submit_pcr_signin_form", "登录"));
        params.add(new BasicNameValuePair("loginRedirectUrl",
                "http://cn.ihg.com/ihg"));
        return params;
    }

    private class HttpAsyncCallback implements FutureCallback<HttpResponse> {

        private IHGHotelEvent event;
        private boolean skipNextStep = false;

        public HttpAsyncCallback(IHGHotelEvent event) {
            this.event = event;
        }

        @Override
        public void completed(HttpResponse result) {
            try {
                CookieHelper.getCookies(event.getCookieList(), result);
                switch (event.getState()) {
                    case LOGINPAGE:
                        loginPageDown(result);
                        break;
                    case LOGIN:
                        loginDown(result);
                        break;
                    case HOMEPAGE:
                        homePageDown(result);
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

        private void loginPageDown(HttpResponse result) {
            logger.debug("login page down {}", event.getId());
            event.setState(IHGHotelState.LOGIN);
        }

        private void loginDown(HttpResponse result) {
            logger.debug("login page down {}", event.getId());
            try {
                if (result.getStatusLine().getStatusCode() == 302) {// 登录请求发送成功
                    event.setLocation(result.getFirstHeader("Location")
                            .getValue());
                    event.setState(IHGHotelState.HOMEPAGE);
                } else {
                    logger.info("username or password error exception {}",
                            event.getId());
                    event.setException(new SpiderException(1060, "用户名或密码错误"));
                }
            } catch (Exception e) {
                logger.error("get login Down exception", e);
                event.setException(e);
            }
        }

        private void homePageDown(HttpResponse result) {
            logger.debug("home page down {}", event.getId());
            Header[] headers = result.getHeaders("Set-Cookie");
            String body;
            try {
                body = XpathHtmlUtils.deleteHeadHtml(EntityUtils.toString(
                        result.getEntity(), "utf-8"));
                Document document = XpathHtmlUtils.getCleanHtml(body);
                XPath xpath = XPathFactory.newInstance().newXPath();
                String exp = "//*[@id='header']/div[1]/div/ul/li[4]/div/div/p[2]";
                String nodeStr = XpathHtmlUtils.getNodeText(exp, xpath,
                        document);
                event.getIhgHotelResult().setJifen(
                        nodeStr.replace("您的积分 : ", ""));
                exp = "//*[@id='header']/div[1]/div/ul/li[4]/div/span[2]";
                nodeStr = XpathHtmlUtils.getNodeText(exp, xpath, document);
                event.getIhgHotelResult().setUsername(
                        StringUtils.isEmpty(nodeStr) ? event.getAccount()
                                : nodeStr);
                event.getIhgHotelResult().setAccount(event.getAccount());
                logger.info("get ihg integral success {}", event.getId());
                event.setException(new SpiderException(0, "获取IHG积分成功", JSON
                        .toJSONString(event.getIhgHotelResult())));
            } catch (Exception e) {
                logger.error("get home Page Down exception", e);
                event.setException(e);
            }
        }

        @Override
        public void failed(Exception ex) {
        }

        @Override
        public void cancelled() {
        }
    }
}
