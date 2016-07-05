package com.caitu99.lsp.spider.unicomshopspider;

import com.alibaba.fastjson.JSON;
import com.caitu99.lsp.AppConfig;
import com.caitu99.lsp.Constant;
import com.caitu99.lsp.cache.RedisOperate;
import com.caitu99.lsp.exception.SpiderException;
import com.caitu99.lsp.model.spider.QueryEvent;
import com.caitu99.lsp.model.spider.unicomshop.UnicomShopEvent;
import com.caitu99.lsp.model.spider.unicomshop.UnicomShopState;
import com.caitu99.lsp.parser.utils.DOMHelper;
import com.caitu99.lsp.spider.HttpAsyncClient;
import com.caitu99.lsp.spider.QuerySpider;
import com.caitu99.lsp.utils.SpringContext;
import com.caitu99.lsp.utils.cookie.CookieHelper;
import com.caitu99.lsp.utils.cookie.HttpCookieEx;
import org.apache.commons.lang.StringUtils;
import org.apache.http.*;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.request.async.DeferredResult;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * for 联通商城
 * Created by Administrator on 2016/1/12.
 */
public class UnicomShopSpider implements QuerySpider {
    private static final Logger logger = LoggerFactory.getLogger(UnicomShopSpider.class);

    private static final String userAgent = "Mozilla/5.0 (Windows NT 6.3; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/46.0.2490.86 Safari/537.36";
    private static final String loginPageURL = "http://jf.10010.com/login.jsp";
    private static final String vcodeURL = "http://uac.10010.com/portal/Service/CreateImage?datetime=";
    private static final String orderURL = "http://jf.10010.com/order/userEcardAction.do?command=submitUserCart&giftId=%s&tm=%s";
    private static final String sendSMSURL = "http://jf.10010.com/order/userEcardAction.do?command=getConfirmCode";
    private static final String checkSmsURL = "http://jf.10010.com/order/userEcardAction.do?command=checkRandomNum";
    private static final String getLoginStateURL = "http://jf.10010.com/login/userLogin.do?command=isLogined";
    private static final String nextStepAfterCheckSmsCodeURL = "http://jf.10010.com/order/userEcardAction.do?command=confirmOrder&giftId=%s&num=%s";
    //private static final String nextStepAfterCheckSmsCodeURL2 = "http://jf.10010.com/order/userEcardAction.do?command=includeOrderInfo";
    private static final String submitOrderURL = "http://jf.10010.com/order/userEcardAction.do?command=submitOrder";

    protected RedisOperate redis = SpringContext.getBean(RedisOperate.class);
    private CloseableHttpAsyncClient httpAsyncClient = HttpAsyncClient.getInstance();
    private AppConfig appConfig = SpringContext.getBean(AppConfig.class);

    @Override
    public void onEvent(QueryEvent event) {
        UnicomShopEvent unicomShopEvent = (UnicomShopEvent) event;

        try {
            switch (unicomShopEvent.getState()) {
                case GET_LOGIN_PAGE:
                    get_login_page_up(unicomShopEvent);
                    break;
                case GET_LOGIN_IFRAME:
                    get_login_iframe_up(unicomShopEvent);
                    break;
                case GET_VCODE:
                    get_vcode_up(unicomShopEvent);
                    break;
                case LOGIN:
                    login_up(unicomShopEvent);
                    break;
                case GET_NEW_SESSIONID:
                    get_new_sessionid_up(unicomShopEvent);
                    break;
                case GET_HOME_PAGE_AFTER_LOGIN:
                    getHomePageAfterLoginUp(unicomShopEvent);
                    break;
                case ORDER:
                    order_up(unicomShopEvent);
                    break;
                case GET_LOGIN_STATE:
                    get_login_state_up(unicomShopEvent);
                    break;
                case SEND_SMS:
                    send_sms_up(unicomShopEvent);
                    break;
                case CHECK_SMS_CODE:
                    check_sms_code_up(unicomShopEvent);
                    break;
                case CONFIRM_ORDER:
                    confirm_order_up(unicomShopEvent);
                    break;
                case SUBMIT_ORDER:
                    submit_order_up(unicomShopEvent);
                    break;
                case GET_ORDERNO:
                    get_orderno_up(unicomShopEvent);
                    break;
                case RECHARGE_TO_SELF:
                    recharge_to_self_up(unicomShopEvent);
                    break;
                case ERROR:
                    errorHandle(unicomShopEvent);
                    break;
            }
        } catch (Exception e) {
            logger.error("request up error {}", event.getId(), e);
            unicomShopEvent.setException(e);
        }
    }

    private void get_orderno_up(UnicomShopEvent unicomShopEvent) {
        logger.debug("get orderno up{}", unicomShopEvent.getId());
        try {
            String url = unicomShopEvent.getGetOrderNoURl();
            HttpGet httpGet = new HttpGet(url);
            setHeader(url, httpGet, unicomShopEvent);
            httpAsyncClient.execute(httpGet, new HttpAsyncCallback(unicomShopEvent));
        } catch (Exception e) {
            logger.error("get orderno request has exception", e);
        }
    }

    private void submit_order_up(UnicomShopEvent unicomShopEvent) {
        logger.debug("submit up{}", unicomShopEvent.getId());
        try {
            String url = submitOrderURL;
            HttpPost httpPost = new HttpPost(url);
            List<NameValuePair> nvps = new ArrayList<>();
            nvps.add(new BasicNameValuePair("giftId", unicomShopEvent.getGiftId()));
            nvps.add(new BasicNameValuePair("num", unicomShopEvent.getNums().toString()));
            nvps.add(new BasicNameValuePair("deviId", ""));
            httpPost.setEntity(new UrlEncodedFormEntity(nvps));

            setHeader(url, httpPost, unicomShopEvent);
            httpAsyncClient.execute(httpPost, new HttpAsyncCallback(unicomShopEvent));
        } catch (Exception e) {
            logger.error("请求登录状态失败", e);
        }
    }

    private void confirm_order_up(UnicomShopEvent unicomShopEvent) {
        logger.debug("next step after check sms code up{}", unicomShopEvent.getId());
        try {
            String url = String.format(nextStepAfterCheckSmsCodeURL, unicomShopEvent.getGiftId(), unicomShopEvent.getNums());
            HttpGet httpGet = new HttpGet(url);
            setHeader(url, httpGet, unicomShopEvent);
            httpAsyncClient.execute(httpGet, new HttpAsyncCallback(unicomShopEvent));
        } catch (Exception e) {
            logger.error("next step after check sms code has exception", e);
        }
    }

    private void get_login_state_up(UnicomShopEvent unicomShopEvent) {
        logger.debug("submit up{}", unicomShopEvent.getId());
        try {
            String url = getLoginStateURL;
            HttpPost httpPost = new HttpPost(url);
            List<NameValuePair> nvps = new ArrayList<>();
            nvps.add(new BasicNameValuePair("date", unicomShopEvent.getSms()));
            httpPost.setEntity(new UrlEncodedFormEntity(nvps));

            setHeader(url, httpPost, unicomShopEvent);
            httpAsyncClient.execute(httpPost, new HttpAsyncCallback(unicomShopEvent));
        } catch (Exception e) {
            logger.error("请求登录状态失败", e);
        }
    }

    private void recharge_to_self_up(UnicomShopEvent unicomShopEvent) {
        logger.debug("recharge to self up{}", unicomShopEvent.getId());
        try {
            String url = unicomShopEvent.getChargeMySelfURL();
            logger.debug("给自己充值的url："+url);
            HttpGet httpGet = new HttpGet(url);
            setHeader(url, httpGet, unicomShopEvent);
            httpAsyncClient.execute(httpGet, new HttpAsyncCallback(unicomShopEvent));
        } catch (Exception e) {
            logger.error("发送给自己充值请求发生异常", e);
        }
    }

    private void check_sms_code_up(UnicomShopEvent unicomShopEvent) {
        logger.debug("submit up{}", unicomShopEvent.getId());
        try {
            String url = checkSmsURL;
            HttpPost httpPost = new HttpPost(url);
            List<NameValuePair> nvps = new ArrayList<>();
            nvps.add(new BasicNameValuePair("randomNum", unicomShopEvent.getSms()));
            nvps.add(new BasicNameValuePair("tm", Long.toString(new Date().getTime())));
            httpPost.setEntity(new UrlEncodedFormEntity(nvps));
            setHeader(url, httpPost, unicomShopEvent);
            httpAsyncClient.execute(httpPost, new HttpAsyncCallback(unicomShopEvent));
        } catch (Exception e) {
            logger.error("发送验证信息出错", e);
        }
    }

    private void send_sms_up(UnicomShopEvent unicomShopEvent) {
        logger.debug("get sms {}", unicomShopEvent.getId());
        try {
            String url = sendSMSURL;
            HttpGet httpGet = new HttpGet(url);
            setHeader(url, httpGet, unicomShopEvent);
            httpAsyncClient.execute(httpGet, new HttpAsyncCallback(unicomShopEvent));
        } catch (Exception e) {
            logger.error("请求验证码出错", e);
        }
    }

    private void order_up(UnicomShopEvent unicomShopEvent) {
        logger.debug("order gift {}", unicomShopEvent.getId());
        try {
            String url = String.format(orderURL, unicomShopEvent.getGiftId(), new Date().getTime());
            HttpGet httpGet = new HttpGet(url);
            setHeader(url, httpGet, unicomShopEvent);
            httpAsyncClient.execute(httpGet, new HttpAsyncCallback(unicomShopEvent));
        } catch (Exception e) {
            logger.error("发送立即购买请求出错", e);
        }
    }

    private void get_new_sessionid_up(UnicomShopEvent unicomShopEvent) {
        logger.debug("request new session id {}", unicomShopEvent.getId());
        try {
            String url = "http://jf.10010.com/login.jsp?target=";
            HttpGet httpGet = new HttpGet(url);
            setHeader(url, httpGet, unicomShopEvent);
            httpAsyncClient.execute(httpGet, new HttpAsyncCallback(unicomShopEvent));
        } catch (Exception e) {
            logger.error("请求new session id出错", e);
        }
    }

    private void getHomePageAfterLoginUp(UnicomShopEvent unicomShopEvent) {
        logger.debug("request home page {}", unicomShopEvent.getId());
        try {
            String url = unicomShopEvent.getIndexUrl();
            HttpGet httpGet = new HttpGet(unicomShopEvent.getIndexUrl());
            setHeader(url, httpGet, unicomShopEvent);
            httpAsyncClient.execute(httpGet, new HttpAsyncCallback(unicomShopEvent));
        } catch (Exception e) {
            logger.error("请求home page出错", e);
        }
    }

    private void get_login_iframe_up(UnicomShopEvent unicomShopEvent) {
        logger.debug("request login page {}", unicomShopEvent.getId());
        try {
            HttpGet httpGet = new HttpGet(unicomShopEvent.getLoginIframe());
            setHeader(loginPageURL, httpGet, unicomShopEvent);
            httpAsyncClient.execute(httpGet, new HttpAsyncCallback(unicomShopEvent));
        } catch (Exception e) {
            logger.error("请求iframe页面出错", e);
        }
    }

    private void login_up(UnicomShopEvent unicomShopEvent) {
        logger.debug("request login page {}", unicomShopEvent.getId());
        try {
            String url = "https://uac.10010.com/portal/Service/MallLogin?callback=jsonp" + (new Date().getTime() - 200000) +
                    "&req_time=" + new Date().getTime() +
                    "&userName=" + unicomShopEvent.getAccount() +
                    "&password=" + unicomShopEvent.getPassword() +
                    "&pwdType=" + (unicomShopEvent.getPwdType() != null ? unicomShopEvent.getPwdType() : "") +
                    "&productType=" + (unicomShopEvent.getProductType() != null ? unicomShopEvent.getProductType() : "") +
                    "&verifyCode=" + unicomShopEvent.getvCode() +
                    "&redirectType=" + (unicomShopEvent.getRedirectType() != null ? unicomShopEvent.getRedirectType() : "") +
                    "&areaCode=" + (unicomShopEvent.getAreaCode() != null ? unicomShopEvent.getAreaCode() : "") +
                    "&arrcity=" + (unicomShopEvent.getArrcity() != null ? URLEncoder.encode(unicomShopEvent.getArrcity()) : "") +
                    "&captchaType=" + (unicomShopEvent.getCaptchaType() != null ? unicomShopEvent.getCaptchaType() : "") +
                    "&bizCode=" + (unicomShopEvent.getBizCode() != null ? unicomShopEvent.getBizCode() : "") +
                    "&uvc=" + (unicomShopEvent.getUvc() != null ? unicomShopEvent.getUvc() : "");
            HttpGet httpGet = new HttpGet(url);
            setHeader(url, httpGet, unicomShopEvent);

            httpAsyncClient.execute(httpGet, new HttpAsyncCallback(unicomShopEvent));
        } catch (Exception e) {
            logger.error("请求登录页面出错", e);
        }
    }

    private void get_login_page_up(UnicomShopEvent unicomShopEvent) {
        logger.debug("request get login page for status {}", unicomShopEvent.getId());
        try {
            HttpGet httpGet = new HttpGet(loginPageURL);
            setHeader(loginPageURL, httpGet, unicomShopEvent);
            httpAsyncClient.execute(httpGet, new HttpAsyncCallback(unicomShopEvent));
        } catch (Exception e) {
            logger.error("请求登录页面出错", e);
        }
    }

    private void setHeader(String loginPageURL, HttpMessage httpMessage, QueryEvent event) {
        httpMessage.setHeader("Accept", "*/*");
        httpMessage.setHeader("User-Agent", userAgent);
        try {
            CookieHelper.setCookies(loginPageURL, httpMessage, event.getCookieList());
        } catch (URISyntaxException e) {
            logger.error("set cookie fail {}", event.getId(), e);
        }
    }

    private void get_vcode_up(UnicomShopEvent unicomShopEvent) {
        logger.debug("request vcode {}", unicomShopEvent.getId());
        try {
            HttpGet httpGet = new HttpGet(vcodeURL + new Date().getTime());
            setHeader(loginPageURL, httpGet, unicomShopEvent);
            httpAsyncClient.execute(httpGet, new HttpAsyncCallback(unicomShopEvent));
        } catch (Exception e) {
            logger.error("request vcode error", e);
        }
    }

    @Override
    public void errorHandle(QueryEvent event) {
        DeferredResult<Object> deferredResult = event.getDeferredResult();
        if (deferredResult == null) {
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

    private class HttpAsyncCallback implements FutureCallback<HttpResponse> {
        private UnicomShopEvent event;
        private boolean skipNextStep = false;

        public HttpAsyncCallback(UnicomShopEvent event) {
            this.event = event;
        }

        @Override
        public void completed(HttpResponse result) {
            try {
                CookieHelper.getCookiesFresh(event.getCookieList(), result);
                switch (event.getState()) {
                    case GET_LOGIN_PAGE:
                        get_login_page_down(result);
                        break;
                    case GET_LOGIN_IFRAME:
                        get_login_iframe_down(result);
                        break;
                    case GET_VCODE:
                        get_vcode_down(result);
                        break;
                    case LOGIN:
                        login_down(result);
                        break;
                    case GET_NEW_SESSIONID:
                        get_new_sessionid_down(result);
                        break;
                    case GET_HOME_PAGE_AFTER_LOGIN:
                        getHomePageAfterLoginDown(result);
                        break;
                    case ORDER:
                        order_down(result);
                        break;
                    case SEND_SMS:
                        get_sms_down(result);
                        break;
                    case CHECK_SMS_CODE:
                        check_sms_code_down(result);
                        break;
                    case CONFIRM_ORDER:
                        confirm_order_down(result);
                        break;
                    case SUBMIT_ORDER:
                        submit_order_down(result);
                        break;
                    case GET_ORDERNO:
                        get_orderno_down(result);
                        break;
                    case RECHARGE_TO_SELF:
                        recharge_to_self_down(result);
                        break;
                }
            } catch (Exception e) {
                logger.error("unexpected error {}", event.getId(), e);
                event.setException(e);
            }
            //skip next
            if (skipNextStep) {
                return;
            }
            onEvent(event);
        }

        //获取订单号
        private void get_orderno_down(HttpResponse result) {
            logger.debug("get_orderno_down ");
            try {
                String resultStr = EntityUtils.toString(result.getEntity(), "utf-8");

                if (appConfig.inDevMode()) {
                    byte[] tbytes = resultStr.getBytes();
                    FileOutputStream fs = new FileOutputStream(appConfig.getUploadPath() + "/txt/" + "order_no_page" + new Date().getTime() + "_" + event.getUserid() + ".html");
                    fs.write(tbytes);
                    fs.close();
                }
                Element doc = Jsoup.parse(resultStr);
                Elements tds = doc.getElementsByTag("td");
                Element td = DOMHelper.getMinContainer(tds, "您的订单提交成功&您的订单号为");
                if (td == null) {
                    logger.error("未能获取到订单提交结果信息,{}", resultStr);
                    event.setException(new SpiderException(1229, "未能获取到订单提交结果信息", resultStr));//todo
                    return;
                }
                String txt = td.text().trim().replaceAll(" ", "").replaceAll("\u00A0", "");

                Pattern patternForOrderNo = Pattern.compile("(?<=您的订单号为).*");              //todo
                Matcher matcherForOrderNo = patternForOrderNo.matcher(txt);
                if (!matcherForOrderNo.find()) {
                    logger.error("未能获取到订单号,{}", resultStr);
                    event.setException(new SpiderException(1223, "未能获取到订单号", resultStr));//todo
                    return;
                }
                String orderNo = matcherForOrderNo.group();
                event.setOrderNo(orderNo);

                Pattern pattern = Pattern.compile("(?<=window\\.location\\.href=')/order/userEcardAction\\.do\\?command=chargeMySelfOnLine.*?(?=')");
                Matcher matcher = pattern.matcher(resultStr);


                if (event.getSelf()) {       //判断是否是给自己充值
                    if (matcher.find()) {
                        String url = "http://jf.10010.com" + matcher.group();
                        event.setChargeMySelfURL(url);
                        event.setState(UnicomShopState.RECHARGE_TO_SELF);
                        return;
                    } else {
                        event.setState(UnicomShopState.OK); //订单完成
                        String key = String.format(Constant.UNICOM_KEY, event.getUserid());
                        redis.set(key, JSON.toJSONString(event), Constant.unicom_expire_time);
                        logger.info("购买联通商城商品成功,但未找到给自己充值的url {}", event.getId());
                        event.setException(new SpiderException(1228, "购买联通商城商品成功，但给自己充值未成功", orderNo));
                        return;
                    }
                }

                event.setState(UnicomShopState.OK); //订单完成
                String key = String.format(Constant.UNICOM_KEY, event.getUserid());
                redis.set(key, JSON.toJSONString(event), Constant.unicom_expire_time);
                logger.debug("购买联通商城商品成功 {}", event.getId());
                event.setException(new SpiderException(1222, "购买联通商城商品成功", orderNo));
                return;
            } catch (Exception e) {
                logger.error("recharge to self has exception", e);
                event.setException(e);
            }
        }

        private void submit_order_down(HttpResponse result) {
            logger.debug("recharge to self down");
            try {
                String resultStr = EntityUtils.toString(result.getEntity(), "utf-8");

                if (appConfig.inDevMode()) {
                    byte[] tbytes = resultStr.getBytes();
                    FileOutputStream fs = new FileOutputStream(appConfig.getUploadPath() + "/txt/" + new Date().getTime() + "_" + event.getUserid() + ".html");
                    fs.write(tbytes);
                    fs.close();
                }

                //抓取积分不足的情况
                Pattern patternx = Pattern.compile("您的积分余额不足");
                Matcher matcherx = patternx.matcher(resultStr);
                if (matcherx.find()) {
                    logger.error("您的积分余额不足");
                    event.setException(new SpiderException(1225, "您的积分余额不足", resultStr));
                    return;
                }

                Header location = result.getFirstHeader("Location");
                if (location == null) {
                    logger.error("无法获取到订单号页面url");
                    event.setException(new SpiderException(1227, "无法获取到订单号页面url", resultStr));
                    return;
                } else {
                    event.setGetOrderNoURl(location.getValue());
                    event.setState(UnicomShopState.GET_ORDERNO);
                }
            } catch (Exception e) {
                logger.error("recharge to self has exception", e);
                event.setException(e);
            }
        }

        //短信验证后的提交
        private void confirm_order_down(HttpResponse result) {
            logger.debug("recharge to self down");
            try {
                String resultStr = EntityUtils.toString(result.getEntity(), "utf-8");
                if (result.getStatusLine().getStatusCode() == 200) {
                    event.setState(UnicomShopState.SUBMIT_ORDER);
                } else {
                    logger.error("短信验证后的提交返回有误,{},{}", resultStr, result);
                    event.setException(new SpiderException(1221, "短信验证后的提交返回有误", result.toString()));//todo
                }
            } catch (Exception e) {
                logger.error("recharge to self has exception", e);
                event.setException(e);
            }
        }

        private void recharge_to_self_down(HttpResponse result) {
            logger.debug("recharge to self down");
            try {
                String resultStr = EntityUtils.toString(result.getEntity(), "utf-8");

                if(result.getStatusLine().getStatusCode()==200)
                {
                    logger.debug(resultStr);
                    event.setState(UnicomShopState.OK); //订单完成
                    String key = String.format(Constant.UNICOM_KEY, event.getUserid());
                    redis.set(key, JSON.toJSONString(event), Constant.unicom_expire_time);
                    logger.debug("购买联通商城商品成功 {}", event.getId());
                    event.setException(new SpiderException(1222, "购买联通商城商品成功", event.getOrderNo()));
                }
                else {
                    logger.error("给自己充值返回数据异常：{}",resultStr);
                    event.setException(new SpiderException(1223, "给自己充值返回异常"));
                }
            } catch (Exception e) {
                logger.error("recharge to self has exception", e);
                event.setException(e);
            }
        }

        private void check_sms_code_down(HttpResponse result) {
            logger.debug("check sms code down");
            try {
                String resultStr = EntityUtils.toString(result.getEntity(), "utf-8");
                if (!"1".equals(resultStr)) {
                    if ("0".equals(resultStr)) {
                        event.setState(UnicomShopState.SEND_SMS);       //到重新获取短信
                        String key = String.format(Constant.UNICOM_KEY, event.getUserid());
                        redis.set(key, JSON.toJSONString(event), Constant.unicom_expire_time);
                        logger.info("联通商城提交订单失败，原因：您输入的随机码不正确，请重新获取随机码,date:{}", resultStr);
                        event.setException(new SpiderException(1219, "联通商城提交订单失败，原因：您输入的随机码不正确，请重新获取随机码", resultStr));//todo
                        return;
                    } else {
                        event.setState(UnicomShopState.SEND_SMS);       //到重新获取短信
                        String key = String.format(Constant.UNICOM_KEY, event.getUserid());
                        redis.set(key, JSON.toJSONString(event), Constant.unicom_expire_time);
                        logger.error("联通商城提交订单短信验证失败,date:{}", resultStr);
                        event.setException(new SpiderException(1218, "联通商城提交订单短信验证失败", resultStr));//todo
                        return;
                    }
                }

                event.setState(UnicomShopState.CONFIRM_ORDER);      //确认订单

            } catch (Exception e) {
                logger.error("in order down has exception", e);
                event.setException(e);
            }
        }

        //发送请求短信验证码返回处理
        private void get_sms_down(HttpResponse result) {
            logger.debug("get sms down");
            try {
                String resultStr = EntityUtils.toString(result.getEntity(), "utf-8");
                if (!"1".equals(resultStr)) {
                    logger.error("请求发送短信验证码出错,code:{}", resultStr);
                    event.setException(new SpiderException(1216, "请求发送短信验证码出错", resultStr));//todo
                    return;
                }
                event.setDate(new Date());
                event.setState(UnicomShopState.CHECK_SMS_CODE); //
                // vCode
                String key = String.format(Constant.UNICOM_KEY, event.getUserid());
                redis.set(key, JSON.toJSONString(event), Constant.unicom_expire_time); // expire in 5 minutes
                logger.debug("need sms verify code {}", event.getId());
                event.setException(new SpiderException(1215, "请输入短信验证码"));//todo
                return;
            } catch (Exception e) {
                logger.error("in order down has exception", e);
                event.setException(e);
            }
        }

        private void order_down(HttpResponse result) {
            logger.debug("order down");
            try {
                String resultStr = EntityUtils.toString(result.getEntity(), "utf-8");
                if (!resultStr.contains("请输入短信验证码")) {
                    event.setException(new SpiderException(1213, "返回的数据有误"));
                    logger.error("数据格式有误");
                    return;
                }

                event.setState(UnicomShopState.SEND_SMS);       //等待请求发送短信
                String key = String.format(Constant.UNICOM_KEY, event.getUserid());
                redis.set(key, JSON.toJSONString(event), Constant.unicom_expire_time); // expire in 5 minutes
                logger.debug("next requestion is get sms code {}", event.getId());
//                event.setException(new SpiderException(1214, "请获取短信验证码"));
            } catch (Exception e) {
                logger.error("in order down has exception", e);
                event.setException(e);
            }
        }

        private void get_new_sessionid_down(HttpResponse result) {
            logger.debug("get new session id down");
            try {
                String resultStr = EntityUtils.toString(result.getEntity(), "utf-8");
                Pattern pattern = Pattern.compile("(?<=href=\").*?(?=\")");
                Matcher matcher = pattern.matcher(resultStr);
                if (!matcher.find()) {
                    event.setException(new SpiderException(1224, "获取新的SESSIONID时无法获取到重定向url"));
                    logger.error("获取新的SESSIONID时无法获取到重定向url");
                    return;
                }
                String url = matcher.group();
                event.setIndexUrl(url);
//                event.setState(UnicomShopState.GET_HOME_PAGE_AFTER_LOGIN);
                String key = String.format(Constant.UNICOM_KEY, event.getUserid());
                redis.set(key, JSON.toJSONString(event), 480);
                event.setException(new SpiderException(1214, "请获取短信验证码"));
                
            } catch (Exception e) {
                logger.error("in get new session id down has exception", e);
                event.setException(e);
            }
        }

        private void getHomePageAfterLoginDown(HttpResponse result) {
            logger.debug("get home page down");
            try {
                String resultStr = EntityUtils.toString(result.getEntity(), "utf-8");
                Document doc = Jsoup.parse(resultStr);
                Elements elements = doc.getElementsByTag("table");
                if (elements.size() == 0) {
                    logger.error("登录失败，数据格式不对");
                    event.setException(new SpiderException(1213, "返回的数据有误"));
                    return;
                }
                Element table = DOMHelper.getMinContainer(elements, "欢迎您登录&我的账户&安全退出");
                Element jifentab = DOMHelper.getMinContainer(elements, "积分余额");
                if (table == null || jifentab == null) {
                    logger.error("登录失败，数据格式不对");
                    event.setException(new SpiderException(1213, "返回的数据有误"));
                    return;
                }
                elements = table.getElementsByTag("td");
                if (elements.size() == 0) {
                    logger.error("登录失败，数据格式不对");
                    event.setException(new SpiderException(1213, "返回的数据有误"));
                    return;
                }
                Element td = DOMHelper.getMinContainer(elements, "欢迎您登录");
                if (td == null) {
                    logger.error("登录失败，数据格式不对");
                    event.setException(new SpiderException(1213, "返回的数据有误"));
                    return;
                }
                String accountStr = td.text().trim().replaceAll(" ", "").replaceAll("\u00A0", "");
                Pattern pattern = Pattern.compile("(?<=欢迎您登录！).*");
                Matcher matcher = pattern.matcher(accountStr);
                if (!matcher.find()) {
                    logger.error("登录失败，数据格式不对");
                    event.setException(new SpiderException(1213, "返回的数据有误"));
                    return;
                }
                event.getUnicomShopResult().setAccount(matcher.group());
                //积分
                elements = jifentab.getElementsByTag("td");
                if (elements.size() == 0) {
                    logger.error("登录失败，数据格式不对");
                    event.setException(new SpiderException(1213, "返回的数据有误"));
                }
                td = DOMHelper.getMinContainer(elements, "积分余额");
                if (td == null) {
                    logger.error("登录失败，数据格式不对");
                    event.setException(new SpiderException(1213, "返回的数据有误"));
                    return;
                }
                String jifenStr = td.text().trim().replaceAll(" ", "").replaceAll("\u00A0", "");
                Pattern patternJifen = Pattern.compile("(?<=积分余额：)\\d+");
                Matcher matcherJifen = patternJifen.matcher(jifenStr);
                if (!matcherJifen.find()) {
                    logger.error("登录失败，数据格式不对");
                    event.setException(new SpiderException(1213, "返回的数据有误", jifenStr));//todo 待确定代码
                    return;
                }
                String string = matcherJifen.group();
                try {
                    event.getUnicomShopResult().setJifen(Integer.valueOf(string));
                } catch (Exception e) {
                    logger.error("积分数据异常，无法转换");
                    event.setException(new SpiderException(1212, "积分数据异常，无法转换", string));
                    return;
                }
//                if (event.getGiftCost() * event.getNums() > event.getUnicomShopResult().getJifen()) {
//                    logger.debug("积分不足以兑换该商品");
//                    event.setException(new SpiderException(1211, "积分不足以兑换该商品", "需要的积分："
//                            + event.getGiftCost() * event.getNums() + "，当前积分：" + event.getUnicomShopResult().getJifen()));
//                    return;
//                }
                String key = String.format(Constant.UNICOM_KEY, event.getUserid());
                redis.set(key, JSON.toJSONString(event), 480);
                event.setState(UnicomShopState.ORDER);
            } catch (Exception e) {
                logger.error("in get home page down has exception", e);
                event.setException(e);
            }
        }

        private void login_down(HttpResponse result) {
            logger.debug("login down");
            try {
                String resultStr = EntityUtils.toString(result.getEntity(), "utf-8");
                Pattern pattern = Pattern.compile("\\{.*?}");
                Matcher matcher = pattern.matcher(resultStr);
                if (!matcher.find()) {
                    event.setException(new SpiderException(1210, "登录返回的数据无法解析", resultStr));
                    return;
                }

                String json = matcher.group();
                Map map = (Map) JSON.parse(json);
                String code = (String) map.get("resultCode");
                if (!"0000".equals(code)) {
                    logger.info("登录失败："+event.getId()+" "+resultStr);
                    if ("7007".equals(code)) {
                        event.setException(new SpiderException(1067, "用户名或密码错误"));
                        return;
                    }
                    if ("7002".equals(code)) {
                        event.setException(new SpiderException(1234, "不支持简单密码登录"));
                        return;
                    }
                    if ("7001".equals(code)) {
                        event.setException(new SpiderException(1052, "验证码错误", resultStr));
                        return;
                    }
                    if("7209".equals(code))
                    {
                        event.setException(new SpiderException(1230, "账号登录异常，请稍后再试", resultStr));
                        return;
                    }
                    if("7008".equals(code))
                    {
                        event.setException(new SpiderException(1232, "您的账号登录受限，请联系客服", resultStr));
                        return;
                    }
                    if("7208".equals(code))
                    {
                        event.setException(new SpiderException(1231, "登录过于频繁，为保障您的账号安全，请稍后再试", resultStr));
                        return;
                    }
                    event.setException(new SpiderException(1013, "登录失败", resultStr));//todo 后续补充错误情况
                    return;
                }
                String redirectURL = (String) map.get("redirectURL");
                if (StringUtils.isBlank(redirectURL)) {
                    event.setException(new SpiderException(1209, "无法解析到重定向url", resultStr));
                    return;
                }
                event.setState(UnicomShopState.GET_NEW_SESSIONID);
//                event.setException(new SpiderException(1214, "请获取短信验证码"));
            } catch (Exception e) {
                logger.error("login  down exception", e);
                event.setException(e);
            }
        }

        private void get_login_iframe_down(HttpResponse result) {
            logger.debug("get login iframe down");
            try {
                InputStream in = result.getEntity().getContent();

                String resultStr = EntityUtils.toString(result.getEntity(), "utf-8");
                Document doc = Jsoup.parse(resultStr);
                if (doc == null) {
                    event.setException(new SpiderException(1208, "获取登录页面失败"));//todo 待确定代码
                }
//                event.setProductType(doc.getElementById("productType").attr("value"));
                event.setProductType("01");     //动态生成，这里固定为01
                event.setPwdType(doc.getElementById("pwdType").attr("value"));
                event.setRedirectType(doc.getElementById("redirectType").attr("value"));
                event.setAreaCode(doc.getElementById("areaCode").attr("value"));
                event.setCaptchaType(doc.getElementById("captchaType").attr("value"));
                event.setBizCode(doc.getElementById("bizCode").attr("value"));
                event.setRightCode(doc.getElementById("rightCode").attr("value"));
                event.setArrcity(doc.getElementById("arrcity").attr("value"));

                event.setState(UnicomShopState.GET_VCODE);
                String key = String.format(Constant.UNICOM_KEY, event.getUserid());
                redis.set(key, JSON.toJSONString(event), Constant.unicom_expire_time); // expire in 5 minutes

                logger.debug("need verify code {}", event.getId());
//                event.setException(new SpiderException(1207, "登录初始化完成"));       //todo
                return;

            } catch (Exception e) {
                logger.error("get login iframe down exception", e);
                event.setException(e);
            }
        }

        private void get_vcode_down(HttpResponse result) {
            logger.debug("get vcode down {}", event.getId());
            try {
                List<HttpCookieEx> cookieList = event.getCookieList();
                boolean flag = false;
                for (HttpCookieEx cookieEx : cookieList) {
                    String name = cookieEx.getName();
                    if ("uacverifykey".equals(name)) {
                        event.setUvc(cookieEx.getValue());
                        flag = true;
                        break;
                    }
                }
                if (!flag) {
                    logger.error("未接收到uvc cookie");
                    event.setException(new SpiderException(1206, "未接收到uvc cookie"));
                    return;
                }

                HttpEntity httpEntity = result.getEntity();
                InputStream imgStream = httpEntity.getContent();
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                int rd = -1;
                byte[] bytes = new byte[1024];
                while ((rd = imgStream.read(bytes)) != -1) {
                    baos.write(bytes, 0, rd);
                }
                byte[] rbyte = baos.toByteArray();

                String imgStr = Base64.getEncoder().encodeToString(rbyte);

                if (appConfig.inDevMode()) {
                    byte[] tbytes = Base64.getDecoder().decode(imgStr);
                    FileOutputStream fs = new FileOutputStream(appConfig.getUploadPath() + "/" + UUID.randomUUID() + "_" + new Date().getTime() + event.getUserid() + ".jpg");
                    fs.write(tbytes);
                    fs.close();
                }
                event.setState(UnicomShopState.LOGIN); //
                //event.setNeedVcode(true);
                // vCode
                String key = String.format(Constant.UNICOM_KEY, event.getUserid());
                redis.set(key, JSON.toJSONString(event), Constant.unicom_expire_time); // expire in 5 minutes

                logger.debug("need verify code {}", event.getId());
                // return vcode to user
                event.setException(new SpiderException(1001, "输入验证码", imgStr));
                return;
            } catch (Exception e) {
                logger.error("get vcode Down exception", e);
                event.setException(e);
            }
        }

        private void get_login_page_down(HttpResponse result) {
            logger.debug("login page down {}", event.getId());
            try {
                String resultStr = EntityUtils.toString(result.getEntity());
                Pattern pattern = Pattern.compile("(?<=<iframe[^>]{0,1001}src=\").*(?=\"></iframe>)");
                Matcher matcher = pattern.matcher(resultStr);
                if (matcher.find()) {
                    String url = matcher.group();
                    event.setLoginIframe(url);
                    event.setState(UnicomShopState.GET_LOGIN_IFRAME);
                } else {
                    logger.error("无法获取到登录URL");
                    event.setException(new SpiderException(1205, "无法获取到登录URL"));
                    return;
                }
                //这里要区分，是否已经登录
                //      event.setState(UnicomShopState.GET_VCODE);
            } catch (Exception e) {
                logger.error("get login Page Down exception", e);
                event.setException(e);
            }
        }

        @Override
        public void failed(Exception ex) {
            logger.debug("request failed");
        }

        @Override
        public void cancelled() {
            logger.debug("request cancelled");
        }
    }
}
