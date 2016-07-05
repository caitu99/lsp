package com.caitu99.lsp.spider.comi;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.caitu99.lsp.AppConfig;
import com.caitu99.lsp.Constant;
import com.caitu99.lsp.cache.RedisOperate;
import com.caitu99.lsp.exception.SpiderException;
import com.caitu99.lsp.model.spider.QueryEvent;
import com.caitu99.lsp.model.spider.comishop.COMIShopEvent;
import com.caitu99.lsp.model.spider.comishop.COMIShopState;
import com.caitu99.lsp.spider.HttpAsyncClient;
import com.caitu99.lsp.spider.QuerySpider;
import com.caitu99.lsp.utils.SpringContext;
import com.caitu99.lsp.utils.cookie.CookieHelper;
import org.apache.http.Header;
import org.apache.http.HttpMessage;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.entity.StringEntity;
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

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.*;

public class COMISpider implements QuerySpider {

    private static final Logger logger = LoggerFactory
            .getLogger(COMISpider.class);

    private static final String userAgent = "Mozilla/5.0 (Windows NT 6.3; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/46.0.2490.86 Safari/537.36";
    private static final String indexUrl = "https://creditcardapp.bankcomm.com/idm/sso/login.html";
    private static final String keyboardUrl = "https://creditcardapp.bankcomm.com/idm/sso/keyboards.json";
    private static final String loginUrl = "https://creditcardapp.bankcomm.com/idm/sso/auth.html";
    private static final String checkUrl = "https://creditcardapp.bankcomm.com/idm/sso/checkLogin.json";
    private static final String jauthUrl = "http://club.bankcomm.com/customer/j_auth.html";
    private static final String queryIntegralUrl = "https://club.bankcomm.com/customer/memberccardinfo/memberCcardList.html";
    private static final String login1Url = "https://creditcardapp.bankcomm.com/idm/sso/login.html";
    private static final String infoUrl = "http://club.bankcomm.com/customer/member/asyMemberInfo.html";
    private static final String orderDetail1Url = "http://club.bankcomm.com/customer/couponactivityrules/vailedByProdRules.html";
    private static final String orderDetail2Url = "https://club.bankcomm.com/customer/cartinfo/gotocounter.html";
    private static final String payDetail1Url = "https://club.bankcomm.com/customer/rushactivityqueue/vaildateProd.html";
    private static final String payDetail2Url = "https://club.bankcomm.com/customer/couponactivityrules/vailedByProdRules.html";
    private static final String payDetail3Url = "https://club.bankcomm.com/customer/cartinfo/gotoOrder.html";
    private static final String payDetail4Url = "https://payment.bankcomm.com/PCCCShopService/purchaseOrder.handler";
    private static final String payDetail5Url = "https://payment.bankcomm.com/PCCCShopService/ShopService/ServiceProcessor.asmx/GetSession";
    private static final String sms1Url = "https://payment.bankcomm.com/PCCCShopService/ShopService/ServiceProcessor.asmx/GetValidCode";
    private static final String sms2Url = "https://payment.bankcomm.com/PCCCShopService/ShopService/ServiceProcessor.asmx/GetScoreByCardNo";
    private static final String pay1Url = "https://payment.bankcomm.com/PCCCShopService/ShopService/ServiceProcessor.asmx/ConfirmPurchase";
    private static final String ordernoUrl = "https://club.bankcomm.com/customer/prodorder/orderDetail.html";

    protected RedisOperate redis = SpringContext.getBean(RedisOperate.class);
    private CloseableHttpAsyncClient httpAsyncClient = HttpAsyncClient.getInstance();
    private AppConfig appConfig = SpringContext.getBean(AppConfig.class);

    @Override
    public void onEvent(QueryEvent event) {
        COMIShopEvent comiShopEvent = (COMIShopEvent) event;
        try {
            switch (comiShopEvent.getState()) {
                case INDEX:
                    indexUp(comiShopEvent);
                    break;
                case KEYBOARD:
                    keyboardUp(comiShopEvent);
                    break;
                case LOGIN:
                    loginUp(comiShopEvent);
                    break;
                case QUERY_INTEGRAL:
                    queryIntegralUp(comiShopEvent);
                    break;
                case TICKET:
                    ticketUp(comiShopEvent);
                    break;
                case CHECK:
                    checkUp(comiShopEvent);
                    break;
                case JAUTH:
                    jauthUp(comiShopEvent);
                    break;
                case LOGIN1:
                    login1Up(comiShopEvent);
                    break;
                case JSECURITY:
                    jsecurityUp(comiShopEvent);
                    break;
                case JAUTH1:
                    jauth1Up(comiShopEvent);
                    break;
                case INFO:
                    infoUp(comiShopEvent);
                    break;
                case ORDERDETAIL1:
                    orderDetail1Up(comiShopEvent);
                    break;
                case ORDERDETAIL2:
                    orderDetail2Up(comiShopEvent);
                    break;
                case PAYDETAIL1:
                    payDetail1Up(comiShopEvent);
                    break;
                case PAYDETAIL2:
                    payDetail2Up(comiShopEvent);
                    break;
                case PAYDETAIL3:
                    payDetail3Up(comiShopEvent);
                    break;
                case PAYDETAIL4:
                    payDetail4Up(comiShopEvent);
                    break;
                case PAYDETAIL5:
                    payDetail5Up(comiShopEvent);
                    break;
                case SMS1:
                    sms1Up(comiShopEvent);
                    break;
                case SMS2:
                    sms2Up(comiShopEvent);
                    break;
                case PAY1:
                    pay1Up(comiShopEvent);
                    break;
                case ORDERNO:
                    ordernoUp(comiShopEvent);
                    break;
                case ERROR:
                    errorHandle(comiShopEvent);
                    break;
            }
        } catch (Exception e) {
            logger.error("request up error {}", event.getId(), e);
            comiShopEvent.setException(e);
            errorHandle(comiShopEvent);
        }
    }

    private void setHeader(String uriStr, HttpMessage httpMessage, QueryEvent event) {
        httpMessage.setHeader("Accept", "*/*");
        httpMessage.setHeader("User-Agent", userAgent);
        try {
            CookieHelper.setCookies(uriStr, httpMessage, event.getCookieList());
        } catch (URISyntaxException e) {
            logger.error("set cookie fail {}", event.getId());
        }
    }

    /**
     * 请求首页
     *
     * @param comiShopEvent
     */
    private void indexUp(COMIShopEvent comiShopEvent) {
        logger.debug("request index up {}", comiShopEvent.getId());
        HttpGet httpGet = new HttpGet(indexUrl + "?_channel=CLUB&service=http://club.bankcomm.com/customer/index.htm");
        setHeader(indexUrl, httpGet, comiShopEvent);
        httpGet.setHeader("Referer", "http://club.bankcomm.com/customer/index.htm");
        httpAsyncClient.execute(httpGet, new HttpAsyncCallback(comiShopEvent));
    }

    /**
     * 请求软键盘
     *
     * @param comiShopEvent
     */
    private void keyboardUp(COMIShopEvent comiShopEvent) {
        logger.debug("request keyboard up {}", comiShopEvent.getId());
        Date d = new Date();
        HttpGet httpGet = new HttpGet(keyboardUrl + "?_=" + d.getTime());
        setHeader(keyboardUrl, httpGet, comiShopEvent);
        httpGet.setHeader("Referer", "https://creditcardapp.bankcomm.com/idm/sso/login.html?_channel=CLUB&service=http://club.bankcomm.com/customer/index.htm");
        httpAsyncClient.execute(httpGet, new HttpAsyncCallback(comiShopEvent));
    }

    /**
     * 请求登录
     *
     * @param comiShopEvent
     */
    private void loginUp(COMIShopEvent comiShopEvent) throws UnsupportedEncodingException {
        logger.debug("request login up {}", comiShopEvent.getId());
        HttpPost httpPost = new HttpPost(loginUrl);

        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("lt", comiShopEvent.getLt()));
        params.add(new BasicNameValuePair("usernametype", "CARD"));
        params.add(new BasicNameValuePair("username", comiShopEvent.getAccount()));
        params.add(new BasicNameValuePair("password", "123456"));

        JSONArray jsonArray = comiShopEvent.getJsonArray();
        StringBuffer passwordseq = new StringBuffer();
        boolean isFirst = true;
        for (char c : comiShopEvent.getPassword().toCharArray()) {
            int index = Integer.valueOf(String.valueOf(c));
            if (isFirst) {
                passwordseq.append(jsonArray.get(index));
                isFirst = false;
                continue;
            }
            passwordseq.append("|").append(jsonArray.get(index));
        }
        logger.debug("passwordseq=" + passwordseq);

        params.add(new BasicNameValuePair("passwordseq", passwordseq.toString()));
        params.add(new BasicNameValuePair("accept", "1"));

        httpPost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));

        setHeader(loginUrl, httpPost, comiShopEvent);
        httpPost.setHeader("Referer", "https://creditcardapp.bankcomm.com/idm/sso/login.html?_channel=CLUB&service=http://club.bankcomm.com/customer/index.htm");
        httpAsyncClient.execute(httpPost, new HttpAsyncCallback(comiShopEvent));
    }

    /**
     * 请求查询积分页面
     *
     * @param comiShopEvent
     */
    private void queryIntegralUp(COMIShopEvent comiShopEvent){
        logger.debug("request queryIntegral up {}", comiShopEvent.getId());
        HttpGet httpGet = new HttpGet(queryIntegralUrl);
        setHeader(queryIntegralUrl, httpGet, comiShopEvent);
        httpAsyncClient.execute(httpGet, new HttpAsyncCallback(comiShopEvent));
    }

    /**
     * 请求ticket
     *
     * @param comiShopEvent
     */
    private void ticketUp(COMIShopEvent comiShopEvent) {
        logger.debug("request ticket up {}", comiShopEvent.getId());
        HttpGet httpGet = new HttpGet(comiShopEvent.getTicketUrl());
        setHeader("http://club.bankcomm.com/customer/index.htm", httpGet, comiShopEvent);
        httpAsyncClient.execute(httpGet, new HttpAsyncCallback(comiShopEvent));
    }

    /**
     * 请求check
     *
     * @param comiShopEvent
     */
    private void checkUp(COMIShopEvent comiShopEvent) {
        logger.debug("request check up {}", comiShopEvent.getId());
        Date d = new Date();
        HttpGet httpGet = new HttpGet(checkUrl + "?loginCallBack=jQuery1720517492342274636_1454470966664&_=" + d.getTime());
        setHeader(checkUrl, httpGet, comiShopEvent);
        httpGet.setHeader("Referer", "http://club.bankcomm.com/customer/index.htm?ticket=" + comiShopEvent.getTicket());
        httpAsyncClient.execute(httpGet, new HttpAsyncCallback(comiShopEvent));
    }

    /**
     * 请求jauth
     *
     * @param comiShopEvent
     */
    private void jauthUp(COMIShopEvent comiShopEvent) {
        logger.debug("request jauth up {}", comiShopEvent.getId());
        HttpGet httpGet = new HttpGet(jauthUrl + "?loginAuth=index.htm");
        setHeader(jauthUrl, httpGet, comiShopEvent);
        httpGet.setHeader("Referer", "http://club.bankcomm.com/customer/index.htm?ticket=" + comiShopEvent.getTicket());
        httpAsyncClient.execute(httpGet, new HttpAsyncCallback(comiShopEvent));
    }

    /**
     * 请求login1
     *
     * @param comiShopEvent
     */
    private void login1Up(COMIShopEvent comiShopEvent) {
        logger.debug("request login1 up {}", comiShopEvent.getId());
        HttpGet httpGet = new HttpGet(comiShopEvent.getLoginUrl());
        setHeader(login1Url, httpGet, comiShopEvent);
        httpGet.setHeader("Referer", "http://club.bankcomm.com/customer/index.htm?ticket=" + comiShopEvent.getTicket());
        httpAsyncClient.execute(httpGet, new HttpAsyncCallback(comiShopEvent));
    }

    /**
     * 请求jsecurity
     *
     * @param comiShopEvent
     */
    private void jsecurityUp(COMIShopEvent comiShopEvent) {
        logger.debug("request jsecurity up {}", comiShopEvent.getId());
        HttpGet httpGet = new HttpGet(comiShopEvent.getJsecurityUrl());
        setHeader("http://club.bankcomm.com/customer/j_security_check", httpGet, comiShopEvent);
        httpGet.setHeader("Referer", "http://club.bankcomm.com/customer/index.htm?ticket=" + comiShopEvent.getTicket());
        httpAsyncClient.execute(httpGet, new HttpAsyncCallback(comiShopEvent));
    }

    /**
     * 请求jauth1
     *
     * @param comiShopEvent
     */
    private void jauth1Up(COMIShopEvent comiShopEvent) {
        logger.debug("request jauth1 up {}", comiShopEvent.getId());
        HttpGet httpGet = new HttpGet(comiShopEvent.getJauthUrl());
        setHeader("http://club.bankcomm.com/customer/j_auth.html", httpGet, comiShopEvent);
        httpGet.setHeader("Referer", "http://club.bankcomm.com/customer/index.htm?ticket=" + comiShopEvent.getTicket());
        httpAsyncClient.execute(httpGet, new HttpAsyncCallback(comiShopEvent));
    }

    /**
     * 请求用户信息1
     *
     * @param comiShopEvent
     */
    private void infoUp(COMIShopEvent comiShopEvent) {
        logger.debug("request info1 up {}", comiShopEvent.getId());
        Date d = new Date();
        HttpGet httpGet = new HttpGet(infoUrl + "?ts=" + d.getTime());
        setHeader(infoUrl, httpGet, comiShopEvent);
        httpGet.setHeader("Referer", "http://club.bankcomm.com/customer/index.htm");
        httpAsyncClient.execute(httpGet, new HttpAsyncCallback(comiShopEvent));
    }

    /**
     * orderDetail1
     *
     * @param comiShopEvent
     */
    private void orderDetail1Up(COMIShopEvent comiShopEvent) {
        logger.debug("request orderDetail1 up {}", comiShopEvent.getId());
        Date d = new Date();
        HttpPost httpPost = new HttpPost(orderDetail1Url + "?prodId=" + comiShopEvent.getProdId() + "&count=" + comiShopEvent.getCount() + "&ts=" + d.getTime());
        setHeader(orderDetail1Url, httpPost, comiShopEvent);
        httpPost.setHeader("Referer", "http://club.bankcomm.com/customer/productinfo/productItemDetail0.html?prodId=" + comiShopEvent.getProdId());
        httpAsyncClient.execute(httpPost, new HttpAsyncCallback(comiShopEvent));
    }

    /**
     * orderDetail2
     *
     * @param comiShopEvent
     */
    private void orderDetail2Up(COMIShopEvent comiShopEvent) {
        logger.debug("request orderDetail2 up {}", comiShopEvent.getId());
        Date d = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String sNow = sdf.format(d);
        String sParams = "{\"prodId\":\"" + comiShopEvent.getProdId() + "\",\"name\":\"" + comiShopEvent.getProdName() + "\",\"originPrice\":\"" + comiShopEvent.getOriginPrice() + "\",\"price\":\"" + comiShopEvent.getCashPrice() + "\",\"instNum\":\"1\",\"oneCash\":\"0.00\",\"oneScoreNum\":\"" + comiShopEvent.getPrice() + "\",\"isOnlyScore\":0,\"onlyScore\":\"\",\"instPrice\":\"\",\"isPutinFamily\":\"\",\"prodAmt\":\"" + comiShopEvent.getCount() + "\",\"putinCartTime\":\"" + sNow + "\",\"isInstallment\":0,\"cartType\":\"01\",\"markupPrice\":\"\",\"markupReason\":\"\",\"markupWhy\":\"\",\"mealId\":\"\",\"memberId\":\"\",\"isHavaSpecStorage\":\"\",\"deliverSpec\":\"\",\"storage\":\"995969\",\"isSel\":1,\"error\":\"0,0\",\"isBrandshopProd\":\"0\",\"instScore\":\"\",\"instTotalPrice\":\"" + comiShopEvent.getCashPrice() + "\"}";
        comiShopEvent.setParam(sParams);
        logger.debug(sParams);
        HttpPost httpPost = new HttpPost(orderDetail2Url);
        try {
            List<NameValuePair> params = new ArrayList<>();
            params.add(new BasicNameValuePair("quickcart", sParams));
            httpPost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
        } catch (Exception e) {
            logger.error("encode body error {}", comiShopEvent.getId(), e);
            comiShopEvent.setException(e);
            return;
        }
        setHeader(orderDetail2Url, httpPost, comiShopEvent);
        httpPost.setHeader("Referer", "http://club.bankcomm.com/customer/productinfo/productItemDetail0.html?prodId=" + comiShopEvent.getProdId());
        httpAsyncClient.execute(httpPost, new HttpAsyncCallback(comiShopEvent));
    }

    /**
     * payDetail1
     *
     * @param comiShopEvent
     */
    private void payDetail1Up(COMIShopEvent comiShopEvent) {
        logger.debug("request payDetail1 up {}", comiShopEvent.getId());
        HttpPost httpPost = new HttpPost(payDetail1Url + "?prodId=" + comiShopEvent.getProdId());
        setHeader(payDetail1Url, httpPost, comiShopEvent);
        httpPost.setHeader("Referer", "https://club.bankcomm.com/customer/cartinfo/gotocounter.html");
        httpAsyncClient.execute(httpPost, new HttpAsyncCallback(comiShopEvent));
    }

    /**
     * payDetail2
     *
     * @param comiShopEvent
     */
    private void payDetail2Up(COMIShopEvent comiShopEvent) {
        logger.debug("request payDetail2 up {}", comiShopEvent.getId());
        Date d = new Date();
        HttpPost httpPost = new HttpPost(payDetail2Url + "?prodId=" + comiShopEvent.getProdId() + "&count=" + comiShopEvent.getCount() + "&ts=" + d.getTime());
        setHeader(payDetail2Url, httpPost, comiShopEvent);
        httpPost.setHeader("Referer", "https://club.bankcomm.com/customer/cartinfo/gotocounter.html");
        httpAsyncClient.execute(httpPost, new HttpAsyncCallback(comiShopEvent));
    }

    /**
     * payDetail3
     *
     * @param comiShopEvent
     */
    private void payDetail3Up(COMIShopEvent comiShopEvent) {
        logger.debug("request payDetail3 up {}", comiShopEvent.getId());
        HttpPost httpPost = new HttpPost(payDetail3Url);
        try {
            List<NameValuePair> params = new ArrayList<>();
            params.add(new BasicNameValuePair("rushQueueId", ""));
            params.add(new BasicNameValuePair("errorPage", "pay_error_new"));
            params.add(new BasicNameValuePair("orderPsfScore", "0"));
            params.add(new BasicNameValuePair("orderPayType", "0"));
            params.add(new BasicNameValuePair("scorePayType", "0"));
            params.add(new BasicNameValuePair("prodHbAndLjStr", ""));
            params.add(new BasicNameValuePair("bonusId", ""));
            params.add(new BasicNameValuePair("psf", "0"));
            params.add(new BasicNameValuePair("cart", comiShopEvent.getParam()));
            params.add(new BasicNameValuePair("consigneeId", ""));
            params.add(new BasicNameValuePair("ccard", "0"));
            params.add(new BasicNameValuePair("invoiceTitle", "个人"));
            params.add(new BasicNameValuePair("invoiceType", "0"));
            params.add(new BasicNameValuePair("scoreCostNum", ""));
            params.add(new BasicNameValuePair("randomCookie", ""));
            params.add(new BasicNameValuePair("payCardFlag", "0"));
            params.add(new BasicNameValuePair("consigneeMobile", comiShopEvent.getMobile()));
            params.add(new BasicNameValuePair("useFavorCode", ""));
            params.add(new BasicNameValuePair("useFavorMobile", ""));
            params.add(new BasicNameValuePair("favorCard", ""));
            params.add(new BasicNameValuePair("isNeedFavorMobile", "0"));
            params.add(new BasicNameValuePair("consigneeList", ""));
            params.add(new BasicNameValuePair("prodkeys", ""));
            params.add(new BasicNameValuePair("hbInfokeys", ""));
            params.add(new BasicNameValuePair("newPayCard", ""));
            params.add(new BasicNameValuePair("paygateCardNum", ""));
            params.add(new BasicNameValuePair("GTPAddrInfor", ""));
            params.add(new BasicNameValuePair("gtpConId", ""));
            params.add(new BasicNameValuePair("gtpConEmail", ""));
            params.add(new BasicNameValuePair("gtpConTel", ""));
            params.add(new BasicNameValuePair("gtpConZip", ""));
            params.add(new BasicNameValuePair("gtpConProvince", ""));
            params.add(new BasicNameValuePair("gtpConCity", ""));
            params.add(new BasicNameValuePair("gtpConCounty", ""));
            params.add(new BasicNameValuePair("gtpConAddress", ""));
            params.add(new BasicNameValuePair("addAccpcheck", "on"));
            params.add(new BasicNameValuePair("orderMemo", ""));
            httpPost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
        } catch (Exception e) {
            logger.error("encode body error {}", comiShopEvent.getId(), e);
            comiShopEvent.setException(e);
            return;
        }
        setHeader(payDetail3Url, httpPost, comiShopEvent);
        httpPost.setHeader("Referer", "https://club.bankcomm.com/customer/cartinfo/gotocounter.html");
        httpAsyncClient.execute(httpPost, new HttpAsyncCallback(comiShopEvent));
    }

    /**
     * payDetail4
     *
     * @param comiShopEvent
     */
    private void payDetail4Up(COMIShopEvent comiShopEvent) {
        logger.debug("request payDetail4 up {}", comiShopEvent.getId());
        HttpPost httpPost = new HttpPost(payDetail4Url);
        try {
            List<NameValuePair> params = new ArrayList<>();
            params.add(new BasicNameValuePair("transDataXml", comiShopEvent.getTransDataXml()));
            httpPost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
        } catch (Exception e) {
            logger.error("encode body error {}", comiShopEvent.getId(), e);
            comiShopEvent.setException(e);
            return;
        }
        setHeader(payDetail4Url, httpPost, comiShopEvent);
        httpPost.setHeader("Referer", "https://club.bankcomm.com/customer/cartinfo/gotoOrder.html");
        httpAsyncClient.execute(httpPost, new HttpAsyncCallback(comiShopEvent));
    }

    /**
     * payDetail5
     *
     * @param comiShopEvent
     */
    private void payDetail5Up(COMIShopEvent comiShopEvent) {
        logger.debug("request payDetail5 up {}", comiShopEvent.getId());
        HttpPost httpPost = new HttpPost(payDetail5Url);
        String parameters = "{ sessionId: '" + comiShopEvent.getSessionId() + "', needAuthData: true }";
        httpPost.setEntity(new StringEntity(parameters, Charset.forName("UTF-8")));
        setHeader(payDetail5Url, httpPost, comiShopEvent);
        httpPost.setHeader("Content-Type", "application/json; charset=UTF-8");
        httpPost.setHeader("Referer", "https://payment.bankcomm.com/PCCCShopService/purchaseOrder.handler");
        httpAsyncClient.execute(httpPost, new HttpAsyncCallback(comiShopEvent));
    }

    /**
     * sms1
     *
     * @param comiShopEvent
     */
    private void sms1Up(COMIShopEvent comiShopEvent) {
        logger.debug("request sms1 up {}", comiShopEvent.getId());
        HttpPost httpPost = new HttpPost(sms1Url);
        String parameters = "{sessionId: '" + comiShopEvent.getSessionId() + "',idLast4: '',cardNo: '" + comiShopEvent.getAccount() + "',cardExpireDate: '" + comiShopEvent.getCardExpire() + "',isPurchase: true,isFirstGetValidCode: true,payType:20}";
        httpPost.setEntity(new StringEntity(parameters, Charset.forName("UTF-8")));
        setHeader(sms1Url, httpPost, comiShopEvent);
        httpPost.setHeader("Content-Type", "application/json; charset=UTF-8");
        httpPost.setHeader("Referer", "https://payment.bankcomm.com/PCCCShopService/purchaseOrder.handler");
        httpAsyncClient.execute(httpPost, new HttpAsyncCallback(comiShopEvent));
    }

    /**
     * sms2
     *
     * @param comiShopEvent
     */
    private void sms2Up(COMIShopEvent comiShopEvent) {
        logger.debug("request sms2 up {}", comiShopEvent.getId());
        HttpPost httpPost = new HttpPost(sms2Url);
        String parameters = "{sessionId: '" + comiShopEvent.getSessionId() + "',cardNo: '" + comiShopEvent.getAccount() + "'}";
        httpPost.setEntity(new StringEntity(parameters, Charset.forName("UTF-8")));
        setHeader(sms2Url, httpPost, comiShopEvent);
        httpPost.setHeader("Content-Type", "application/json; charset=UTF-8");
        httpPost.setHeader("Referer", "https://payment.bankcomm.com/PCCCShopService/purchaseOrder.handler");
        httpAsyncClient.execute(httpPost, new HttpAsyncCallback(comiShopEvent));
    }

    /**
     * pay1
     *
     * @param comiShopEvent
     */
    private void pay1Up(COMIShopEvent comiShopEvent) {
        logger.debug("request pay1 up {}", comiShopEvent.getId());
        HttpPost httpPost = new HttpPost(pay1Url);
        String parameters = "{sessionId: '" + comiShopEvent.getSessionId() + "',cardNo: '" + comiShopEvent.getAccount() + "',cardExpireDate: '" + comiShopEvent.getCardExpire() + "',validType: '02',validCode: '" + comiShopEvent.getSmsCode() + "',f15: '" + comiShopEvent.getF15() + "',f20: '" + comiShopEvent.getF20() + "',f23: '" + comiShopEvent.getF23() + "',reqAuthCode: '      ',reqBatchNo: '" + comiShopEvent.getBatchNo() + "',reqInvoiceNo: '" + comiShopEvent.getInvoiceNo() + "',transPwd:'',chkstate:'true'}";
        logger.debug(parameters);
        httpPost.setEntity(new StringEntity(parameters, Charset.forName("UTF-8")));
        setHeader(pay1Url, httpPost, comiShopEvent);
        httpPost.setHeader("Content-Type", "application/json; charset=UTF-8");
        httpPost.setHeader("Referer", "https://payment.bankcomm.com/PCCCShopService/purchaseOrder.handler");
        httpAsyncClient.execute(httpPost, new HttpAsyncCallback(comiShopEvent));
    }

    /**
     * orderno
     *
     * @param comiShopEvent
     */
    private void ordernoUp(COMIShopEvent comiShopEvent) {
        logger.debug("request orderno up {}", comiShopEvent.getId());
        HttpGet httpGet = new HttpGet(ordernoUrl + "?orderId=" + comiShopEvent.getOrderNo());
        setHeader(ordernoUrl, httpGet, comiShopEvent);
        Date d = new Date();
        httpGet.setHeader("Referer", "https://club.bankcomm.com/customer/prodorder/listCoupon.html?ts=" + d.getTime());
        httpAsyncClient.execute(httpGet, new HttpAsyncCallback(comiShopEvent));
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
        private COMIShopEvent event;
        private boolean skipNextStep = false;

        public HttpAsyncCallback(COMIShopEvent event) {
            this.event = event;
        }

        @Override
        public boolean equals(Object obj) {
            return super.equals(obj);
        }

        @Override
        public void completed(HttpResponse result) {
            try {
                CookieHelper.getCookies(event.getCookieList(), result);
                switch (event.getState()) {
                    case INDEX:
                        indexDown(result);
                        break;
                    case KEYBOARD:
                        keyboardDown(result);
                        break;
                    case LOGIN:
                        loginDown(result);
                        break;
                    case QUERY_INTEGRAL:
                        queryIntegralDown(result);
                        break;
                    case TICKET:
                        ticketDown(result);
                        break;
                    case CHECK:
                        checkDown(result);
                        break;
                    case JAUTH:
                        jauthDown(result);
                        break;
                    case LOGIN1:
                        login1Down(result);
                        break;
                    case JSECURITY:
                        jsecurityDown(result);
                        break;
                    case JAUTH1:
                        jauth1Down(result);
                        break;
                    case INFO:
                        infoDown(result);
                        break;
                    case ORDERDETAIL1:
                        orderDetail1Down(result);
                        break;
                    case ORDERDETAIL2:
                        orderDetail2Down(result);
                        break;
                    case PAYDETAIL1:
                        payDetail1Down(result);
                        break;
                    case PAYDETAIL2:
                        payDetail2Down(result);
                        break;
                    case PAYDETAIL3:
                        payDetail3Down(result);
                        break;
                    case PAYDETAIL4:
                        payDetail4Down(result);
                        break;
                    case PAYDETAIL5:
                        payDetail5Down(result);
                        break;
                    case SMS1:
                        sms1Down(result);
                        break;
                    case SMS2:
                        sms2Down(result);
                        break;
                    case PAY1:
                        pay1Down(result);
                        break;
                    case ORDERNO:
                        ordernoDown(result);
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

        @Override
        public void failed(Exception ex) {

        }

        @Override
        public void cancelled() {

        }

        /**
         * 首页请求完成
         *
         * @param result
         */
        private void indexDown(HttpResponse result) throws IOException {
            logger.debug("request index down {}", event.getId());
            String resultStr = EntityUtils.toString(result.getEntity());
            Document document = Jsoup.parse(resultStr);
            Element form = document.getElementById("tabCardNoForm");
            Elements lts = form.getElementsByAttributeValue("name", "lt");
            String lt = lts.get(0).attr("value");
            event.setState(COMIShopState.KEYBOARD);
        }

        /**
         * 软键盘请求完成
         *
         * @param result
         */
        private void keyboardDown(HttpResponse result) throws IOException {
            logger.debug("request keyboard down {}", event.getId());
            String resultStr = EntityUtils.toString(result.getEntity());
            logger.debug(resultStr);
            JSONObject jsonObject = JSON.parseObject(resultStr);
            JSONArray jsonArray = jsonObject.getJSONArray("keys");
            event.setJsonArray(jsonArray);
            event.setState(COMIShopState.LOGIN);
        }

        /**
         * 请求登录完成
         *
         * @param result
         */
        private void loginDown(HttpResponse result) throws IOException {
            logger.debug("request login down {}", event.getId());
            Header[] headers = result.getHeaders("Location");
            if (headers.length == 0) {
                String resultStr = EntityUtils.toString(result.getEntity());
                if(resultStr.contains("查询密码和信用卡号不匹配")){
                    event.setException(new SpiderException(2113, "查询密码和信用卡号不匹配"));
                    return;
                }else if(resultStr.contains("卡片查询密码已被锁定")){
                    event.setException(new SpiderException(2115, "卡片查询密码已被锁定"));
                    return;
                }else if(resultStr.contains("您的登录密码输入已超过限制，请24小时后再试")){
                    event.setException(new SpiderException(2116, "登录密码输入已超过限制，请24小时后再试"));
                    return;
                }
                event.setException(new SpiderException(2107, "登录失败"));
                return;
            }
            event.setTicketUrl(headers[0].getValue());
            int pos = event.getTicketUrl().indexOf("ticket=");
            String ticket = event.getTicketUrl().substring(pos + 7);
            logger.debug(ticket);
            event.setTicket(ticket);
            event.setState(COMIShopState.TICKET);
        }

        /**
         * queryIntegral
         *
         * @param result
         */
        private void queryIntegralDown(HttpResponse result) throws IOException{
            logger.debug("request queryIntegral down {}", event.getId());
            String resultStr = EntityUtils.toString(result.getEntity());
            Document document = Jsoup.parse(resultStr);
            Elements elements_integarl = document.getElementsByAttributeValue("name", "scoreBalance");
            Long integral = -33L;
            for(int index = 0 ; index < elements_integarl.size() ; index++){
                try{
                    integral = integral + Long.parseLong(elements_integarl.get(index).attr("value"));
                }catch(java.lang.NumberFormatException e){
                    event.setException(new SpiderException(2110, "解析交通银行积分失败"));
                    return;
                }
            }
            if( integral == -33L ){
                event.setException(new SpiderException(2114, "获取交通积分失败"));
                return;
            }
            event.setIntegral(integral.toString());
            Map<String,String> resultMap = new HashMap<>();
            resultMap.put("integral",integral.toString());
            resultMap.put("account",event.getAccount());
            resultMap.put("name",event.getName());
            event.setException(new SpiderException(2111, "获取交通积分成功",JSON.toJSONString(resultMap)));
            return;
        }

        /**
         * ticket
         *
         * @param result
         */
        private void ticketDown(HttpResponse result) {
            logger.debug("request ticket down {}", event.getId());
            event.setState(COMIShopState.CHECK);
        }

        /**
         * check
         *
         * @param result
         */
        private void checkDown(HttpResponse result) {
            logger.debug("request check down {}", event.getId());
            event.setState(COMIShopState.JAUTH);
        }

        /**
         * jauth
         *
         * @param result
         */
        private void jauthDown(HttpResponse result) {
            logger.debug("request jauth down {}", event.getId());
            Header[] headers = result.getHeaders("Location");
            event.setLoginUrl(headers[0].getValue());
            event.setState(COMIShopState.LOGIN1);
        }

        /**
         * login1
         *
         * @param result
         */
        private void login1Down(HttpResponse result) {
            logger.debug("request login1 down {}", event.getId());
            Header[] headers = result.getHeaders("Location");
            event.setJsecurityUrl(headers[0].getValue());
            event.setState(COMIShopState.JSECURITY);
        }

        /**
         * jsecurity
         *
         * @param result
         */
        private void jsecurityDown(HttpResponse result) {
            logger.debug("request jsecurity down {}", event.getId());
            Header[] headers = result.getHeaders("Location");
            event.setJauthUrl(headers[0].getValue());
            event.setState(COMIShopState.JAUTH1);
        }

        /**
         * jauth1
         *
         * @param result
         */
        private void jauth1Down(HttpResponse result) {
            logger.debug("request jauth1 down {}", event.getId());
            event.setState(COMIShopState.INFO);
        }

        /**
         * 获取用户信息完成
         *
         * @param result
         */
        private void infoDown(HttpResponse result) throws IOException {
            logger.debug("request info down {}", event.getId());

            String resultStr = EntityUtils.toString(result.getEntity());
            JSONObject jsonObject = JSON.parseObject(resultStr);
            JSONObject jsonObject_result = jsonObject.getJSONObject("result");
            String name = jsonObject_result.getString("username");
            event.setName(name);

            event.setState(COMIShopState.INFO);
            // 缓存当前事件内容
            String key = String.format(Constant.COM_IMPORT_KEY, event.getUserid());
            redis.set(key, JSON.toJSONString(event), Constant.com_login_state_expire_time);
            event.setException(new SpiderException(0, "登录交通成功"));
            return;
        }

        private void orderDetail1Down(HttpResponse result) throws IOException {
            logger.debug("request orderDetail1 down {}", event.getId());
            String resultStr = EntityUtils.toString(result.getEntity());
            logger.debug(resultStr);
            event.setState(COMIShopState.ORDERDETAIL2);
        }

        private void orderDetail2Down(HttpResponse result) {
            logger.debug("request orderDetail2 down {}", event.getId());
            event.setState(COMIShopState.PAYDETAIL1);
        }

        private void payDetail1Down(HttpResponse result) throws IOException {
            logger.debug("request payDetail1 down {}", event.getId());
            String resultStr = EntityUtils.toString(result.getEntity());
            logger.debug(resultStr);
            event.setState(COMIShopState.PAYDETAIL2);
        }

        private void payDetail2Down(HttpResponse result) throws IOException {
            logger.debug("request payDetail2 down {}", event.getId());
            String resultStr = EntityUtils.toString(result.getEntity());
            logger.debug(resultStr);
            event.setState(COMIShopState.PAYDETAIL3);
        }

        private void payDetail3Down(HttpResponse result) throws IOException {
            logger.debug("request payDetail3 down {}", event.getId());
            String resultStr = EntityUtils.toString(result.getEntity());
            logger.debug(resultStr);
            Document document = Jsoup.parse(resultStr);
            Element element = document.getElementById("textarea");
            event.setTransDataXml(element.html());
            event.setState(COMIShopState.PAYDETAIL4);
        }

        private void payDetail4Down(HttpResponse result) throws IOException {
            logger.debug("request payDetail4 down {}", event.getId());

            String resultStr = EntityUtils.toString(result.getEntity());
            Document document = Jsoup.parse(resultStr);
            Element element = document.getElementById("form");
            if(element==null){
                event.setException(new SpiderException(2109, "获取短信失败"));
                return;
            }
            logger.debug(element.toString());
            String attr = element.attr("action");
            logger.debug(attr);
            int pos = attr.indexOf("si=");
            attr = attr.substring(pos + 3);
            event.setSessionId(attr);
            event.setState(COMIShopState.PAYDETAIL5);
        }

        private void payDetail5Down(HttpResponse result) throws IOException {
            logger.debug("request payDetail5 down {}", event.getId());
            String resultStr = EntityUtils.toString(result.getEntity());
            logger.debug(resultStr);
            /*String key = String.format(Constant.ISHOP_COM_TASK_QUEUE, event.getUserid());
            redis.set(key, JSON.toJSONString(event), 600);
            event.setException(new SpiderException(2100, "请输入卡片有效期"));*/
            event.setState(COMIShopState.SMS1);
        }

        private void sms1Down(HttpResponse result) throws IOException {
            logger.debug("request sms1 down {}", event.getId());
            String resultStr = EntityUtils.toString(result.getEntity());
            logger.debug(resultStr);
            JSONObject jsonObject = JSON.parseObject(resultStr);
            String str = jsonObject.getString("d");
            logger.debug(str);
            JSONObject attr = JSON.parseObject(str);
            event.setF15(attr.getString("f15"));
            event.setF20(attr.getString("traceNo"));
            event.setF23(attr.getString("f23"));
            event.setBatchNo(attr.getString("batchNo"));
            event.setInvoiceNo(attr.getString("invoiceNo"));
            event.setState(COMIShopState.SMS2);
        }

        private void sms2Down(HttpResponse result) throws IOException {
            logger.debug("request sms2 down {}", event.getId());
            String resultStr = EntityUtils.toString(result.getEntity());
            logger.debug(resultStr);
            String key = String.format(Constant.ISHOP_COM_TASK_QUEUE, event.getUserid());
            redis.set(key, JSON.toJSONString(event), 600);
            event.setException(new SpiderException(2102, "请输入短信验证码"));
        }

        private void pay1Down(HttpResponse result) throws IOException {
            logger.debug("request pay1 down {}", event.getId());
            String resultStr = EntityUtils.toString(result.getEntity());
            logger.debug(resultStr);
            if (resultStr.contains("支付成功")) {
                //event.setException(new SpiderException(2104, "支付成功"));
                String[] strs = resultStr.split("orderId=");
                if (strs.length < 1) {
                    event.setException(new SpiderException(2110, "获取券码失败"));
                    return;
                }
                String orderNo = strs[1].substring(0, strs[1].indexOf("&"));
                event.setOrderNo(orderNo);
                event.setState(COMIShopState.ORDERNO);
            } else if (resultStr.contains("您输入的手机动态密码不正确")) {
                event.setException(new SpiderException(2105, "您输入的手机动态密码不正确"));
            } else if (resultStr.contains("您的信用卡帐户积分不足")) {
                event.setException(new SpiderException(2108, "您的信用卡帐户积分不足"));
            } else {
                event.setException(new SpiderException(2106, "支付失败"));
            }
        }

        private void ordernoDown(HttpResponse result) throws IOException {
            logger.debug("request orderno down {}", event.getId());
            String resultStr = EntityUtils.toString(result.getEntity());
            logger.debug(resultStr);
            Document document = Jsoup.parse(resultStr);
            Elements elements = document.getElementsByTag("textarea");
            if (elements == null || elements.size() == 0) {
                event.setException(new SpiderException(2110, "获取券码失败"));
                return;
            }
            Element element = elements.get(0);
            String ticket = element.text().replaceAll("\n", ",");
            String effective = "";
            try {
                effective = element.parent().nextElementSibling().nextElementSibling().text();
            } catch(Exception e) {
                logger.error("获取有效期失败,{}", resultStr);
            }
            Map<String, String> map = new HashMap<>();
            map.put("ticket", ticket);
            map.put("effective",effective);
            map.put("orderno", event.getOrderNo());
            event.setException(new SpiderException(2104, "支付成功", JSON.toJSONString(map)));
        }

        private void errorHandle(COMIShopEvent event) {
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
                    deferredResult.setResult((new SpiderException(-1, exception.getMessage()).toString()));
                }
            }
        }
    }
}