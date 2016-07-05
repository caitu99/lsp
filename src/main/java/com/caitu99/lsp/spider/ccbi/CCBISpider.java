package com.caitu99.lsp.spider.ccbi;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.caitu99.lsp.AppConfig;
import com.caitu99.lsp.Constant;
import com.caitu99.lsp.cache.RedisOperate;
import com.caitu99.lsp.exception.SpiderException;
import com.caitu99.lsp.model.spider.QueryEvent;
import com.caitu99.lsp.model.spider.ccbishop.*;
import com.caitu99.lsp.spider.HttpAsyncClient;
import com.caitu99.lsp.spider.QuerySpider;
import com.caitu99.lsp.utils.SpringContext;
import com.caitu99.lsp.utils.cookie.CookieHelper;
import com.caitu99.lsp.utils.cookie.HttpCookieEx;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.naming.NoNameCoder;
import com.thoughtworks.xstream.io.xml.DomDriver;

import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpEntity;
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

import java.io.*;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CCBISpider implements QuerySpider {

    private static final Logger logger = LoggerFactory
            .getLogger(CCBISpider.class);

    private static final String userAgent = "Mozilla/5.0 (Windows NT 6.3; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/46.0.2490.86 Safari/537.36";
    private static final String isLoginUrl = "https://creditcard.ecitic.com/BusinessCityWeb/eshop.do";
    private static final String isLoginCppUrl = "https://creditcard.ecitic.com/BusinessCityWeb/eshop.do";
    private static final String indexUrl = "https://creditcard.ecitic.com/eshop/mainpage_jf/index.htm";
    private static final String getJSessionidUrl = "https://creditcard.ecitic.com/BusinessCityWeb/workJF.do";
    private static final String loginPageUrl = "https://creditcard.ecitic.com/citiccard/cppnew/entry.do";
    private static final String vcodeUrl = "https://creditcard.ecitic.com/citiccard/cppnew/jsp/valicode.jsp";
    private static final String loginUrl = "https://creditcard.ecitic.com/citiccard/cppnew/entry.do";
    private static final String smsUrl = "https://creditcard.ecitic.com/citiccard/cppnew/sms.do";
    private static final String checkUrl = "https://creditcard.ecitic.com/citiccard/cppnew/sms.do";
    private static final String eshopUrl = "https://creditcard.ecitic.com/citiccard/cppnew/eshop.do";
    private static final String eshopLoginUrl = "https://creditcard.ecitic.com/BusinessCityWeb/login.do";
    private static final String getIntegralUrl = "https://creditcard.ecitic.com/BusinessCityWeb/ecity.do";
    private static final String addCartUrl = "https://creditcard.ecitic.com/BusinessCityWeb/workJF.do";
    private static final String queryCartUrl = "https://creditcard.ecitic.com/BusinessCityWeb/workJF.do";
    private static final String addrUrl = "https://creditcard.ecitic.com/BusinessCityWeb/ecity.do";
    private static final String buyConfirmUrl = "https://creditcard.ecitic.com/BusinessCityWeb/workJF.do";
    private static final String queryCardUrl = "https://creditcard.ecitic.com/BusinessCityWeb/workJF.do";
    private static final String orderVcodeUrl = "https://creditcard.ecitic.com/BusinessCityWeb/valicode.jsp";
    private static final String orderUrl = "https://creditcard.ecitic.com/BusinessCityWeb/workJF.do";
    private static final String delCartUrl = "https://creditcard.ecitic.com/BusinessCityWeb/workJF.do";

    private static final Integer redisTime= 600;
    
    protected RedisOperate redis = SpringContext.getBean(RedisOperate.class);
    private CloseableHttpAsyncClient httpAsyncClient = HttpAsyncClient.getInstance();
    private AppConfig appConfig = SpringContext.getBean(AppConfig.class);

    @Override
    public void onEvent(QueryEvent event) {
        CCBIShopEvent ccbiShopEvent = (CCBIShopEvent) event;
        try {
            switch (ccbiShopEvent.getState()) {
                case ISLOGIN:
                    isLoginUp(ccbiShopEvent);
                    break;
                case ISLOGINCPP:
                    isLoginCppUp(ccbiShopEvent);
                    break;
                    
                   //获取登录验证码
                case INDEX:
                    indexUp(ccbiShopEvent);
                    break;
                case GETJSESSIONID:
                    getJSessionidUp(ccbiShopEvent);
                    break;
                case LOGINPAGE:
                    loginPageUp(ccbiShopEvent);
                    break;
                case VCODE:
                    vcodeUp(ccbiShopEvent);  
                    break;
                    
                    //登录
                case LOGIN:
                    loginUp(ccbiShopEvent);
                    break;
                case LOGIN_JF:
                	loginjfUp(ccbiShopEvent);
                	break;
                case QUERY_JF:
                	queryjfUp(ccbiShopEvent);
                	break;
                case SMS:
                    smsUp(ccbiShopEvent);
                    break;
                    
                    
                case CHECK:
                    checkUp(ccbiShopEvent);
                    break;
                case ESHOP:
                    eshopUp(ccbiShopEvent);
                    break;
                case ESHOPLOGIN:
                    eshopLoginUp(ccbiShopEvent);
                    break;
                    
                    //获取下单验证码
                case GETINTEGRAL:
                    getIntegralUp(ccbiShopEvent);
                    break;
                case CLEAR_CART:
                	clearCartUp(ccbiShopEvent);
                    break;
                case ADDCART:
                    addCartUp(ccbiShopEvent);
                    break;
                case QUERYCART:
                    queryCartUp(ccbiShopEvent);
                    break;
                case QUERYCART1:
                    queryCartUp1(ccbiShopEvent);
                    break;
                case INSERTADDR:
                    insertAddrUp(ccbiShopEvent);
                    break;
                case QUERYADDR:
                    queryAddrUp(ccbiShopEvent);
                    break;
                case BUYCONFIRM:
                    buyConfirmUp(ccbiShopEvent);
                    break;
                case ORDERVCODE:
                    orderVcodeUp(ccbiShopEvent);
                    break;
                    
                    
                    //下单
                case QUERYCARD:
                    queryCardUp(ccbiShopEvent);
                    break;
                case ORDER:
                    orderUp(ccbiShopEvent);
                    break;
                case DELADDR:
                    deleteAddrUp(ccbiShopEvent);
                    break;
                case DELCART:
                    delCartUp(ccbiShopEvent);
                    break;
                case ERROR:
                    errorHandle(ccbiShopEvent);
                    break;
            }
        } catch (Exception e) {
            logger.error("request up error {}", event.getId(), e);
            ccbiShopEvent.setException(e);
            errorHandle(ccbiShopEvent);
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
     * 验证是否登录
     *
     * @param ccbiShopEvent
     */
    private void isLoginUp(CCBIShopEvent ccbiShopEvent) throws UnsupportedEncodingException {
        logger.debug("request isLogin up: {}", ccbiShopEvent.getId());
        String queryString = "<request/>";
        StringBuilder sb = new StringBuilder();
        sb.append(isLoginUrl).append("?");
        sb.append("func").append("=").append("isLoginFun").append("&");
        sb.append("dom").append("=");
        sb.append(URLEncoder.encode(queryString, "utf-8"));
        HttpPost httpPost = new HttpPost(sb.toString());
        setHeader(isLoginUrl, httpPost, ccbiShopEvent);
        httpPost.setHeader("Referer", "https://creditcard.ecitic.com/BusinessCityWeb/eshop_jf/scart/payment.htm");
        httpAsyncClient.execute(httpPost, new HttpAsyncCallback(ccbiShopEvent));
    }

    /**
     * 验证是否登录Cpp
     *
     * @param ccbiShopEvent
     */
    private void isLoginCppUp(CCBIShopEvent ccbiShopEvent) throws UnsupportedEncodingException {
        logger.debug("request isLoginCpp up: {}", ccbiShopEvent.getId());
        String queryString = "<request/>";
        StringBuilder sb = new StringBuilder();
        sb.append(isLoginCppUrl).append("?");
        sb.append("func").append("=").append("isLoginCppFun").append("&");
        sb.append("dom").append("=");
        sb.append(URLEncoder.encode(queryString, "utf-8"));
        HttpPost httpPost = new HttpPost(sb.toString());
        setHeader(isLoginCppUrl, httpPost, ccbiShopEvent);
        httpPost.setHeader("Referer", "https://creditcard.ecitic.com/eshop/mainpage_jf/top2.htm");
        httpAsyncClient.execute(httpPost, new HttpAsyncCallback(ccbiShopEvent));
    }

    /**
     * 请求商城首页
     *
     * @param ccbiShopEvent
     */
    private void indexUp(CCBIShopEvent ccbiShopEvent) {
        logger.debug("request index up {}", ccbiShopEvent.getId());
        HttpGet httpGet = new HttpGet(indexUrl);
        setHeader(indexUrl, httpGet, ccbiShopEvent);
        httpAsyncClient.execute(httpGet, new HttpAsyncCallback(ccbiShopEvent));
    }

    /**
     * 请求获取JSessionid
     *
     * @param ccbiShopEvent
     */
    private void getJSessionidUp(CCBIShopEvent ccbiShopEvent) {
        logger.debug("request get jsessionid up {}", ccbiShopEvent.getId());
        HttpPost httpPost = new HttpPost(getJSessionidUrl + "?func=getCartNumFun&dom=%3Crequest/%3E");
        setHeader(getJSessionidUrl, httpPost, ccbiShopEvent);
        httpPost.setHeader("Referer", "https://creditcard.ecitic.com/eshop/mainpage_jf/top.htm");
        String _udh = "124568792";
        String _uu = "" + Math.round(Math.random() * 2147483647);
        Date _udt = new Date();
        String _ust = "" + Math.round(_udt.getTime() / 1000);
        String a = _udh + "." + _uu + "." + _ust + "." + _ust + "." + _ust + ".1";
        String sCookie1 = "Set-Cookie: __utma=" + a + "; path=/";
        String sCookie2 = "Set-Cookie: __utmb=" + _udh + "; path=/";
        String sCookie3 = "Set-Cookie: __utmc=" + _udh + "; path=/";
        String sCookie4 = "Set-Cookie: __utmz=" + _udh + "." + _ust + ".1.1.utmccn=(direct)|utmcsr=(direct)|utmcmd=(none); path=/";
        ccbiShopEvent.getCookieList().addAll(HttpCookieEx.parse(sCookie1));
        ccbiShopEvent.getCookieList().addAll(HttpCookieEx.parse(sCookie2));
        ccbiShopEvent.getCookieList().addAll(HttpCookieEx.parse(sCookie3));
        ccbiShopEvent.getCookieList().addAll(HttpCookieEx.parse(sCookie4));
        httpAsyncClient.execute(httpPost, new HttpAsyncCallback(ccbiShopEvent));
    }

    /**
     * 请求登录页面
     *
     * @param ccbiShopEvent
     */
    private void loginPageUp(CCBIShopEvent ccbiShopEvent) {
        logger.debug("request loginPage up {}", ccbiShopEvent.getId());
        HttpGet httpGet = new HttpGet(loginPageUrl + "?func=entryeshop&page=JF01");
        setHeader(loginPageUrl, httpGet, ccbiShopEvent);
        httpGet.setHeader("Referer", "https://creditcard.ecitic.com/eshop/mainpage_jf/index.htm");
        httpAsyncClient.execute(httpGet, new HttpAsyncCallback(ccbiShopEvent));
    }

    /**
     * 请求验证码
     *
     * @param ccbiShopEvent
     */
    private void vcodeUp(CCBIShopEvent ccbiShopEvent) {
        logger.debug("request vcode up {}", ccbiShopEvent.getId());
        HttpGet httpGet = new HttpGet(vcodeUrl);
        setHeader(vcodeUrl, httpGet, ccbiShopEvent);
        httpGet.setHeader("Referer", "https://creditcard.ecitic.com/citiccard/cppnew/entry.do?func=entryeshop&page=JF01");
        httpAsyncClient.execute(httpGet, new HttpAsyncCallback(ccbiShopEvent));
    }

    /**
     * 请求登录
     *
     * @param ccbiShopEvent
     */
    private void loginUp(CCBIShopEvent ccbiShopEvent) {
        logger.debug("request login up {}", ccbiShopEvent.getId());
        StringEntity entity = null;
        try {
            entity = new StringEntity("<request><logonEshop phonesecretcode=\"" + ccbiShopEvent.getPassword() + "\" logintype=\"02\" idtype=\"1\" idnumber=\"" + ccbiShopEvent.getAccount() + "\" valicode=\"" + ccbiShopEvent.getvCode() + "\" from=\"INNER_SHEQU\" channel=\"11003100608974\" source=\"76603100713223\" /></request>");
        } catch (UnsupportedEncodingException e) {
            logger.error("init entity error", e);
            ccbiShopEvent.setException(e);
        }
        Date d = new Date();
        HttpPost httpPost = new HttpPost(loginUrl + "?func=entryEshopFun&date=" + d.getTime());
        httpPost.setEntity(entity);
        setHeader(loginUrl, httpPost, ccbiShopEvent);
        httpPost.setHeader("Referer", "https://creditcard.ecitic.com/citiccard/cppnew/entry.do?func=entryeshop&page=J020001");
        httpAsyncClient.execute(httpPost, new HttpAsyncCallback(ccbiShopEvent));
    }
    
    private void loginjfUp(CCBIShopEvent ccbiShopEvent){
        logger.debug("request login up {}", ccbiShopEvent.getId());
        StringEntity entity = null;
        try {
            entity = new StringEntity("<request><logonEshop phonesecretcode=\"" + ccbiShopEvent.getPassword() + "\" logintype=\"02\" idtype=\"1\" idnumber=\"" + ccbiShopEvent.getAccount() + "\" valicode=\"" + ccbiShopEvent.getvCode() + "\" from=\"INNER_SHEQU\" channel=\"11003100608974\" source=\"76603100713223\" /></request>");
        } catch (UnsupportedEncodingException e) {
            logger.error("init entity error", e);
            ccbiShopEvent.setException(e);
        }
        Date d = new Date();
        HttpPost httpPost = new HttpPost(loginUrl + "?func=entryEshopFun&date=" + d.getTime());
        httpPost.setEntity(entity);
        setHeader(loginUrl, httpPost, ccbiShopEvent);
        httpPost.setHeader("Referer", "https://creditcard.ecitic.com/citiccard/cppnew/entry.do?func=entryeshop&page=J020001");
        httpAsyncClient.execute(httpPost, new HttpAsyncCallback(ccbiShopEvent));

    }
    
    private void queryjfUp(CCBIShopEvent ccbiShopEvent) throws UnsupportedEncodingException{
        logger.debug("request query integral up: {}", ccbiShopEvent.getId());
        String queryString = "<request><citicmail ordertype_id=\"JF\"/></request>";
        StringBuilder sb = new StringBuilder();
        sb.append(getIntegralUrl).append("?");
        sb.append("func").append("=").append("MyCiticMail").append("&");
        sb.append("dom").append("=");
        sb.append(URLEncoder.encode(queryString, "utf-8"));
        logger.debug("query address: {}, {}", ccbiShopEvent.getId(), sb.toString());
        HttpPost httpPost = new HttpPost(sb.toString());
        setHeader(getIntegralUrl, httpPost, ccbiShopEvent);
        httpPost.setHeader("Referer", "https://creditcard.ecitic.com/BusinessCityWeb/eshop_jf/myciticmail/mymall.htm");
        httpAsyncClient.execute(httpPost, new HttpAsyncCallback(ccbiShopEvent));
        
    }

    /**
     * 请求发送短信验证码
     *
     * @param ccbiShopEvent
     */
    private void smsUp(CCBIShopEvent ccbiShopEvent) {
        logger.debug("request sms up {}", ccbiShopEvent.getId());
        StringEntity entity = null;
        try {
            entity = new StringEntity("<request><logonSendSms/></request>");
        } catch (UnsupportedEncodingException e) {
            logger.error("init entity error", e);
            ccbiShopEvent.setException(e);
        }
        Date d = new Date();
        HttpPost httpPost = new HttpPost(smsUrl + "?func=entrySendSmsFun&date=" + d.getTime());
        httpPost.setEntity(entity);
        setHeader(smsUrl, httpPost, ccbiShopEvent);
        httpPost.setHeader("Referer", "https://creditcard.ecitic.com/citiccard/cppnew/sms.do?func=entrySendSms");
        httpAsyncClient.execute(httpPost, new HttpAsyncCallback(ccbiShopEvent));
    }

    /**
     * 请求短信验证码验证
     *
     * @param ccbiShopEvent
     */
    private void checkUp(CCBIShopEvent ccbiShopEvent) {
        logger.debug("request check up {}", ccbiShopEvent.getId());
        StringEntity entity = null;
        try {
            entity = new StringEntity("<request><checkSms dtsecretcode=\"" + ccbiShopEvent.getSmsCode() + "\" /></request>");
        } catch (UnsupportedEncodingException e) {
            logger.error("init entity error", e);
            ccbiShopEvent.setException(e);
        }
        Date d = new Date();
        HttpPost httpPost = new HttpPost(checkUrl + "?func=entryCheckSmsFun&date=" + d.getTime());
        httpPost.setEntity(entity);
        setHeader(checkUrl, httpPost, ccbiShopEvent);
        httpPost.setHeader("Referer", "https://creditcard.ecitic.com/citiccard/cppnew/sms.do?func=entrySendSms");
        httpAsyncClient.execute(httpPost, new HttpAsyncCallback(ccbiShopEvent));
    }

    /**
     * 请求eshop
     *
     * @param ccbiShopEvent
     */
    private void eshopUp(CCBIShopEvent ccbiShopEvent) {
        logger.debug("request eshop up {}", ccbiShopEvent.getId());
        HttpGet httpGet = new HttpGet(eshopUrl + "?func=eshopLoginFun&eshopRetUrl=JF01");
        setHeader(eshopUrl, httpGet, ccbiShopEvent);
        httpGet.setHeader("Referer", "https://creditcard.ecitic.com/citiccard/cppnew/sms.do?func=entrySendSms");
        httpAsyncClient.execute(httpGet, new HttpAsyncCallback(ccbiShopEvent));
    }

    /**
     * 请求eshopLogin
     *
     * @param ccbiShopEvent
     */
    private void eshopLoginUp(CCBIShopEvent ccbiShopEvent) {
        logger.debug("request eshopLogin up {}", ccbiShopEvent.getId());
        HttpPost httpPost = new HttpPost(eshopLoginUrl);
        try {
            List<NameValuePair> params = new ArrayList<>();
            params.add(new BasicNameValuePair("local_param", ccbiShopEvent.getLocal_param()));
            httpPost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
        } catch (Exception e) {
            logger.error("encode eshopLogin body error {}", ccbiShopEvent.getId(), e);
            ccbiShopEvent.setException(e);
            return;
        }
        httpPost.setHeader("Referer", "https://creditcard.ecitic.com/citiccard/cppnew/eshop.do?func=eshopLoginFun&eshopRetUrl=JF01");
        setHeader(eshopLoginUrl, httpPost, ccbiShopEvent);
        httpAsyncClient.execute(httpPost, new HttpAsyncCallback(ccbiShopEvent));
    }

    /**
     * 请求获取用户积分
     *
     * @param ccbiShopEvent
     */
    private void getIntegralUp(CCBIShopEvent ccbiShopEvent) throws UnsupportedEncodingException {
        logger.debug("request query integral up: {}", ccbiShopEvent.getId());
        String queryString = "<request><citicmail ordertype_id=\"JF\"/></request>";
        StringBuilder sb = new StringBuilder();
        sb.append(getIntegralUrl).append("?");
        sb.append("func").append("=").append("MyCiticMail").append("&");
        sb.append("dom").append("=");
        sb.append(URLEncoder.encode(queryString, "utf-8"));
        logger.debug("query address: {}, {}", ccbiShopEvent.getId(), sb.toString());
        HttpPost httpPost = new HttpPost(sb.toString());
        setHeader(getIntegralUrl, httpPost, ccbiShopEvent);
        httpPost.setHeader("Referer", "https://creditcard.ecitic.com/BusinessCityWeb/eshop_jf/myciticmail/mymall.htm");
        httpAsyncClient.execute(httpPost, new HttpAsyncCallback(ccbiShopEvent));
    }

    /**
	 * 	
	 * @Description: (方法职责详细描述,可空)  
	 * @Title: clearCartUp 
	 * @param ccbiShopEvent
	 * @date 2016年6月3日 下午8:34:43  
	 * @author ws
	*/
	private void clearCartUp(CCBIShopEvent ccbiShopEvent) {
		logger.debug("request clearCartUp: {}", ccbiShopEvent.getId());
        HttpPost httpPost = new HttpPost(delCartUrl);
        try {
            List<NameValuePair> params = new ArrayList<>();
            params.add(new BasicNameValuePair("func", "deleteCartFun"));
            params.add(new BasicNameValuePair("dom", "<request/>"));
            httpPost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
        } catch (Exception e) {
            logger.error("encode buyConfirm body error {}", ccbiShopEvent.getId(), e);
            ccbiShopEvent.setException(e);
            return;
        }
        setHeader(delCartUrl, httpPost, ccbiShopEvent);
        httpPost.setHeader("Referer", "https://creditcard.ecitic.com/BusinessCityWeb/eshop_jf/scart/payment.htm");
        httpAsyncClient.execute(httpPost, new HttpAsyncCallback(ccbiShopEvent));
	}

    /**
     * 请求添加商品到购物车
     *
     * @param ccbiShopEvent
     */
    private void addCartUp(CCBIShopEvent ccbiShopEvent) throws UnsupportedEncodingException {
        logger.debug("request addCardUp up {}", ccbiShopEvent.getId());
        CCBIGoodsQuery goodsQuery = ccbiShopEvent.getGoodsQuery();
        goodsQuery.setGoods_point(String.valueOf(ccbiShopEvent.getPrice()));
        goodsQuery.setGoods_price("0.00");
        goodsQuery.setStages_nm("1");
        goodsQuery.setGoods_size(String.valueOf(ccbiShopEvent.getQuantity()));

        XStream xStream = new XStream(new DomDriver("UTF_8", new NoNameCoder()));
        xStream.processAnnotations(CCBIGoodsQuery.class);
        String domValue = xStream.toXML(goodsQuery);
        //String queryString = "<request><goods goods_id=\"J020001\" goods_payway_id=\"J020001A\" goods_price=\"0.00\" goods_point=\"2250\" stages_nm=\"1\" goods_size=\"1\" goods_nm=\"移动全国流量10M(48小时到账）\" goods_color=\"\" goods_model=\"\" vendor_id=\"GYS057\" vendor_nm=\"江苏奇点网络科技有限公司\" type_id=\"LB0911\" goods_brand=\"\" cardno_benefit=\"\" good_rnd=\"\"/></request>";
        StringBuilder sb = new StringBuilder();
        sb.append(addCartUrl).append("?");
        sb.append("func").append("=").append("addCartFun").append("&");
        sb.append("dom").append("=");
        logger.debug("商品属性：" + domValue);
        sb.append(URLEncoder.encode("<request>" + domValue + "</request>", "GBK"));
        HttpPost httpPost = new HttpPost(sb.toString());
        setHeader(addCartUrl, httpPost, ccbiShopEvent);
        httpPost.setHeader("Referer", "https://creditcard.ecitic.com/eshop/goods/J020001/J020001.htm");
        httpAsyncClient.execute(httpPost, new HttpAsyncCallback(ccbiShopEvent));
    }

    /**
     * 请求查询购物车
     *
     * @param ccbiShopEvent
     */
    private void queryCartUp(CCBIShopEvent ccbiShopEvent) throws UnsupportedEncodingException {
        logger.debug("request queryCart up: {}", ccbiShopEvent.getId());
        String queryString = "<request><querycart celltype=\"0\"/></request>";
        StringBuilder sb = new StringBuilder();
        sb.append(queryCartUrl).append("?");
        sb.append("func").append("=").append("cartGoodsFun").append("&");
        sb.append("dom").append("=");
        sb.append(URLEncoder.encode(queryString, "utf-8"));
        HttpPost httpPost = new HttpPost(sb.toString());
        setHeader(queryCartUrl, httpPost, ccbiShopEvent);
        httpPost.setHeader("Referer", "https://creditcard.ecitic.com/BusinessCityWeb/eshop_jf/scart/cart.htm");
        httpAsyncClient.execute(httpPost, new HttpAsyncCallback(ccbiShopEvent));
    }

    /**
     * 请求查询购物车1
     *
     * @param ccbiShopEvent
     */
    private void queryCartUp1(CCBIShopEvent ccbiShopEvent) throws UnsupportedEncodingException {
        logger.debug("request queryCart1 up: {}", ccbiShopEvent.getId());
        String queryString = "<request><querycart celltype=\"1\"/></request>";
        StringBuilder sb = new StringBuilder();
        sb.append(queryCartUrl).append("?");
        sb.append("func").append("=").append("cartGoodsFun").append("&");
        sb.append("dom").append("=");
        sb.append(URLEncoder.encode(queryString, "utf-8"));
        HttpPost httpPost = new HttpPost(sb.toString());
        setHeader(queryCartUrl, httpPost, ccbiShopEvent);
        httpPost.setHeader("Referer", "https://creditcard.ecitic.com/BusinessCityWeb/eshop_jf/scart/cart.htm");
        httpAsyncClient.execute(httpPost, new HttpAsyncCallback(ccbiShopEvent));
    }

    /**
     * 增加地址
     *
     * @param ccbiShopEvent
     */
    private void insertAddrUp(CCBIShopEvent ccbiShopEvent) throws UnsupportedEncodingException {
        logger.debug("request insert address up: {}", ccbiShopEvent.getId());
        CCBIAddrQuery addrQuery = ccbiShopEvent.getAddrInfo();
        XStream xStream = new XStream(new DomDriver("UTF_8", new NoNameCoder()));
        xStream.processAnnotations(CCBIAddrQuery.class);
        String domValue = xStream.toXML(addrQuery);
        StringBuilder sb = new StringBuilder();
        sb.append(addrUrl).append("?");
        sb.append("func").append("=").append("buyCartFun").append("&");
        sb.append("dom").append("=");
        sb.append(URLEncoder.encode("<request>" + domValue + "</request>", "GBK"));
        logger.debug("insert address: {}, {}", ccbiShopEvent.getId(), sb.toString());
        HttpPost httpPost = new HttpPost(sb.toString());
        setHeader(addrUrl, httpPost, ccbiShopEvent);
        httpPost.setHeader("Referer", "https://creditcard.ecitic.com/BusinessCityWeb/eshop_jf/scart/order.htm");
        httpAsyncClient.execute(httpPost, new HttpAsyncCallback(ccbiShopEvent));
    }

    /**
     * 请求地址查询
     *
     * @param ccbiShopEvent
     */
    private void queryAddrUp(CCBIShopEvent ccbiShopEvent) throws UnsupportedEncodingException {
        logger.debug("request query address up: {}", ccbiShopEvent.getId());
        String queryString = "<request><queryPackup querytype=\"queryaddr\"/></request>";
        StringBuilder sb = new StringBuilder();
        sb.append(addrUrl).append("?");
        sb.append("func").append("=").append("buyCartFun").append("&");
        sb.append("dom").append("=");
        sb.append(URLEncoder.encode(queryString, "utf-8"));
        logger.debug("query address: {}, {}", ccbiShopEvent.getId(), sb.toString());
        HttpPost httpPost = new HttpPost(sb.toString());
        setHeader(addrUrl, httpPost, ccbiShopEvent);
        httpPost.setHeader("Referer", "https://creditcard.ecitic.com/BusinessCityWeb/eshop_jf/scart/order.htm");
        httpAsyncClient.execute(httpPost, new HttpAsyncCallback(ccbiShopEvent));
    }

    /**
     * 请求购买确认
     *
     * @param ccbiShopEvent
     */
    private void buyConfirmUp(CCBIShopEvent ccbiShopEvent) throws UnsupportedEncodingException {
        logger.debug("request buyConfirm up: {}", ccbiShopEvent.getId());
        //String queryString = "<request><addr_desc addr_id=\"" + ccbiShopEvent.getAddrId() + "\" a1=\"\"/></request>";
        String queryString = "<request><addr_desc addr_id=\"\" a1=\"\"/></request>";
        StringBuilder sb = new StringBuilder();
        sb.append(buyConfirmUrl).append("?");
        sb.append("func").append("=").append("buyConfirmFun").append("&");
        sb.append("dom").append("=");
        sb.append(URLEncoder.encode(queryString, "utf-8"));
        HttpPost httpPost = new HttpPost(sb.toString());
        setHeader(buyConfirmUrl, httpPost, ccbiShopEvent);
        httpPost.setHeader("Referer", "https://creditcard.ecitic.com/BusinessCityWeb/eshop_jf/scart/order.htm");
        httpAsyncClient.execute(httpPost, new HttpAsyncCallback(ccbiShopEvent));
    }

    /**
     * 查询卡片
     *
     * @param ccbiShopEvent
     */
    private void queryCardUp(CCBIShopEvent ccbiShopEvent) throws UnsupportedEncodingException {
        logger.debug("request queryCard up: {}", ccbiShopEvent.getId());
        String queryString = "<request><jfinfo type=\"querycard\"/></request>";
        StringBuilder sb = new StringBuilder();
        sb.append(queryCardUrl).append("?");
        sb.append("func").append("=").append("orderJFFun").append("&");
        sb.append("dom").append("=");
        sb.append(URLEncoder.encode(queryString, "utf-8"));
        HttpPost httpPost = new HttpPost(sb.toString());
        setHeader(queryCardUrl, httpPost, ccbiShopEvent);
        httpPost.setHeader("Referer", "https://creditcard.ecitic.com/BusinessCityWeb/eshop_jf/scart/payment.htm");
        httpAsyncClient.execute(httpPost, new HttpAsyncCallback(ccbiShopEvent));
    }

    /**
     * 请求下单验证码
     *
     * @param ccbiShopEvent
     */
    private void orderVcodeUp(CCBIShopEvent ccbiShopEvent) throws UnsupportedEncodingException {
        logger.debug("request orderVcode up: {}", ccbiShopEvent.getId());
        Date d = new Date();
        HttpGet httpGet = new HttpGet(orderVcodeUrl + "?ct=2&time=" + d.getTime());
        setHeader(orderVcodeUrl, httpGet, ccbiShopEvent);
        httpGet.setHeader("Referer", "https://creditcard.ecitic.com/BusinessCityWeb/eshop_jf/scart/payment.htm");
        httpAsyncClient.execute(httpGet, new HttpAsyncCallback(ccbiShopEvent));
    }

    /**
     * 请求下单
     *
     * @param ccbiShopEvent
     */
    private void orderUp(CCBIShopEvent ccbiShopEvent) throws UnsupportedEncodingException {
        logger.debug("request order up: {}", ccbiShopEvent.getId());
        List<CCBICard> cardList = ccbiShopEvent.getCards();
        StringBuilder query = new StringBuilder();
        query.append("<request>");
        for (CCBICard ccbiCard : cardList) {
            if (ccbiCard.getIntegral().compareTo(ccbiShopEvent.getPrice() * ccbiShopEvent.getQuantity()) > 0) {
                query.append("<jfinfo type=\"addorder\" mvd=\"").append(ccbiShopEvent.getOrderVcode()).append("\"/>");
                query.append("<cardinfo sel_card=\"").append(ccbiCard.getCardNo()).append("\"/>");
                query.append("<cardpoint cardpt=\"" + ccbiCard.getIntegral() + "==" + ccbiCard.getCardNo() + "\"/>");
                break;
            }
        }
        query.append("</request>");
        StringBuilder sb = new StringBuilder();
        sb.append(orderUrl).append("?");
        sb.append("func").append("=").append("buyOrderFun").append("&");
        sb.append("dom").append("=");
        sb.append(URLEncoder.encode(query.toString(), "gb2312"));
        HttpPost httpPost = new HttpPost(sb.toString());
        setHeader(orderUrl, httpPost, ccbiShopEvent);
        httpPost.setHeader("Origin", "https://creditcard.ecitic.com");
        httpPost.setHeader("Referer", "https://creditcard.ecitic.com/BusinessCityWeb/eshop_jf/scart/payment.htm");
        httpPost.setHeader("Host", "creditcard.ecitic.com");
        httpPost.setHeader("Content-Type", "application/x-www-form-urlencoded;charset=GBK");
        httpPost.setHeader("Accept-Encoding", "gzip, deflate");
        httpPost.setHeader("Accept-Language", "zh-CN,zh;q=0.8");
        httpAsyncClient.execute(httpPost, new HttpAsyncCallback(ccbiShopEvent));
    }

    /**
     * 请求删除购物车
     *
     * @param ccbiShopEvent
     */
    private void delCartUp(CCBIShopEvent ccbiShopEvent) throws UnsupportedEncodingException {
        logger.debug("request delCart up: {}", ccbiShopEvent.getId());
        HttpPost httpPost = new HttpPost(delCartUrl);
        try {
            List<NameValuePair> params = new ArrayList<>();
            params.add(new BasicNameValuePair("func", "deleteCartFun"));
            params.add(new BasicNameValuePair("dom", "<request/>"));
            httpPost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
        } catch (Exception e) {
            logger.error("encode buyConfirm body error {}", ccbiShopEvent.getId(), e);
            ccbiShopEvent.setException(e);
            return;
        }
        setHeader(delCartUrl, httpPost, ccbiShopEvent);
        httpPost.setHeader("Referer", "https://creditcard.ecitic.com/BusinessCityWeb/eshop_jf/scart/payment.htm");
        httpAsyncClient.execute(httpPost, new HttpAsyncCallback(ccbiShopEvent));
    }

    /**
     * 请求删除地址
     *
     * @param ccbiShopEvent
     */
    private void deleteAddrUp(CCBIShopEvent ccbiShopEvent) throws UnsupportedEncodingException {
        logger.debug("request delete address up: {}", ccbiShopEvent.getId());
        String addrId = ccbiShopEvent.getAddrId();
        String queryString = "<request><csgaddress add_tag=\"delete\" csg_id=\"" + addrId + "\"/></request>";
        StringBuilder sb = new StringBuilder();
        sb.append(addrUrl).append("?");
        sb.append("func").append("=").append("CsgAddressQueryFun").append("&");
        sb.append("dom").append("=");
        sb.append(URLEncoder.encode(queryString, "utf-8"));
        logger.debug("query address: {}, {}", ccbiShopEvent.getId(), sb.toString());
        HttpPost httpPost = new HttpPost(sb.toString());
        setHeader(addrUrl, httpPost, ccbiShopEvent);
        httpPost.setHeader("Referer", "https://creditcard.ecitic.com/BusinessCityWeb/eshop_jf/scart/order.htm");
        httpAsyncClient.execute(httpPost, new HttpAsyncCallback(ccbiShopEvent));
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
        private CCBIShopEvent event;
        private boolean skipNextStep = false;

        public HttpAsyncCallback(CCBIShopEvent event) {
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
                    case ISLOGIN:
                        isLoginDown(result);
                        break;
                    case ISLOGINCPP:
                        isLoginCppDown(result);
                        break;
                    case INDEX:
                        indexDown(result);
                        break;
                    case GETJSESSIONID:
                        getJSessionidDown(result);
                        break;
                    case LOGINPAGE:
                        loginPageDown(result);
                        break;
                    case VCODE:
                        vcodeDown(result);
                        break;
                    case LOGIN:
                        loginDown(result);
                        break;
                    case LOGIN_JF:
                    	loginjfDown(result);
                    	break;
                    case QUERY_JF:
                    	queryjfDown(result);
                    	break;
                    case SMS:
                        smsDown(result);
                        break;
                    case CHECK:
                        checkDown(result);
                        break;
                    case ESHOP:
                        eshopDown(result);
                        break;
                    case ESHOPLOGIN:
                        eshopLoginDown(result);
                        break;
                    case GETINTEGRAL:
                        getIntegralDown(result);
                        break;
                    case CLEAR_CART:
                        clearCartDown(result);
                        break;
                    case ADDCART:
                        addCartDown(result);
                        break;
                    case QUERYCART:
                        queryCartDown(result);
                        break;
                    case QUERYCART1:
                        queryCartDown1(result);
                        break;
                    case INSERTADDR:
                        insertAddrDown(result);
                        break;
                    case QUERYADDR:
                        queryAddrDown(result);
                        break;
                    case BUYCONFIRM:
                        buyConfirmDown(result);
                        break;
                    case QUERYCARD:
                        queryCardDown(result);
                        break;
                    case ORDERVCODE:
                        orderVcodeDown(result);
                        break;
                    case ORDER:
                        orderDown(result);
                        break;
                    case DELADDR:
                        deleteAddrDown(result);
                        break;
                    case DELCART:
                        delCartDown(result);
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
         * 请求完成
         *
         * @param result
         */
        private void isLoginDown(HttpResponse result) {
            logger.debug("request isLogin down {}", event.getId());
            String resultStr = null;
            try {
                resultStr = EntityUtils.toString(result.getEntity());
            } catch (IOException e) {
                logger.error("获取是否登录返回结果失败", e);
            }
            if (StringUtils.isEmpty(resultStr)) {
                event.setException(new SpiderException(1108, "请登录"));
                return;
            }
            Document document = Jsoup.parse(resultStr);
            String retcode = document.select("returninfo").attr("retcode");
            if ("0".equals(retcode)) {//登录成功
                event.setState(CCBIShopState.ISLOGINCPP);
            } else {
                event.setException(new SpiderException(1108, "请登录"));
            }
        }

        /**
         * 请求完成
         *
         * @param result
         */
        private void isLoginCppDown(HttpResponse result) {
            logger.debug("request isLoginCpp down {}", event.getId());
            String resultStr = null;
            try {
                resultStr = EntityUtils.toString(result.getEntity());
            } catch (IOException e) {
                logger.error("获取是否登录返回结果失败", e);
            }
            if (StringUtils.isEmpty(resultStr)) {
                event.setException(new SpiderException(1108, "请登录"));
                return;
            }
            Document document = Jsoup.parse(resultStr);
            String retcode = document.select("returninfo").attr("retcode");
            if ("0".equals(retcode)) {//登录成功
                String key = String.format(Constant.ISHOP_CCB_TASK_QUEUE, event.getUserid());
                String content = redis.getStringByKey(key);
                if (StringUtils.isEmpty(content)) {
                    SpiderException exception = new SpiderException(1081, "用户尚未登录");
                }
                event.setException(new SpiderException(1102, "已登录"));
            } else {
                event.setException(new SpiderException(1108, "请登录"));
            }
        }

        /**
         * 首页请求完成
         *
         * @param result
         */
        private void indexDown(HttpResponse result) {
            logger.debug("request index down {}", event.getId());
            event.setState(CCBIShopState.GETJSESSIONID);
        }

        /**
         * 请求获取JSessionid完成
         *
         * @param result
         */
        private void getJSessionidDown(HttpResponse result) {
            logger.debug("request get jsessionid down {}", event.getId());
            CookieHelper.getCookiesFresh(event.getCookieList(), result);
            event.setState(CCBIShopState.LOGINPAGE);
        }

        /**
         * 请求登录页面完成
         *
         * @param result
         */
        private void loginPageDown(HttpResponse result) {
            logger.debug("request loginPage down {}", event.getId());
            CookieHelper.getCookiesFresh(event.getCookieList(), result);
            event.setState(CCBIShopState.VCODE);
        }

        /**
         * 获取验证码完成
         *
         * @param result
         */
        private void vcodeDown(HttpResponse result) {
            try {
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
                    FileOutputStream fs = new FileOutputStream(
                            appConfig.getUploadPath() + "/" + event.getUserid()
                                    + ".jpg");
                    fs.write(tbytes);
                    fs.close();
                }
                String key = String.format(Constant.ISHOP_CCB_TASK_QUEUE, event.getUserid());
                redis.set(key, JSON.toJSONString(event), 600);
                event.setException(new SpiderException(1001, "输入验证码", imgStr));
            } catch (Exception e) {
                logger.error("get vcode down exception", e);
                e.printStackTrace();
                event.setException(new SpiderException(-1,"获取验证码失败"));
            }
        }

        /**
         * 请求登录完成
         *
         * @param result
         */
        private void loginDown(HttpResponse result) {
            logger.debug("request login down {}", event.getId());
            String resultStr = null;
            try {
                resultStr = EntityUtils.toString(result.getEntity());
            } catch (IOException e) {
                logger.error("获取登录返回结果失败", e);
            }
            if (StringUtils.isEmpty(resultStr)) {
                event.setException(new SpiderException(1013, "登录失败"));
                return;
            }
            Document document = Jsoup.parse(resultStr);
            String retcode = document.select("returninfo").attr("retcode");
            String message = document.select("returninfo").attr("message");
            if ("0".equals(retcode)) {//登录成功
                CookieHelper.getCookiesFresh(event.getCookieList(), result);
                String sendSmsFlag = document.select("re_userinfo").attr("sendSmsFlag");
                
                if ("01".equals(sendSmsFlag)) {

                	logger.info("登录，需要短信验证码");
                    //保存登录后的event，方便重新获取短信验证码
                	String key = String.format(Constant.ISHOP_CCB_TASK_QUEUE, event.getUserid());
                    redis.set(key, JSON.toJSONString(event), redisTime);
                    event.setState(CCBIShopState.SMS);
                } else {
                	logger.info("登录成功");
                	event.setState(CCBIShopState.ESHOP);
                }
            } else if ("1023800".equals(retcode)) {//证件号或密码错误
                event.setException(new SpiderException(1095, "证件号或密码错误"));
            } else if ("1".equals(retcode)) {//错误
            	logger.info("loginDown中信登录错误信息code:{},message：{}",retcode,message);
                if(message.contains("服务繁忙")){
                    event.setException(new SpiderException(1122, "服务繁忙"));
                }else{
                    event.setException(new SpiderException(1094, "验证码错误"));
                }
            } else {
                event.setException(new SpiderException(1093, message));
            }
        }

        
        private void loginjfDown(HttpResponse result){
            logger.debug("request login down {}", event.getId());
            String resultStr = null;
            try {
                resultStr = EntityUtils.toString(result.getEntity());
            } catch (IOException e) {
                logger.error("获取登录返回结果失败", e);
            }
            if (StringUtils.isEmpty(resultStr)) {
                event.setException(new SpiderException(1013, "登录失败"));
                return;
            }
            Document document = Jsoup.parse(resultStr);
            String retcode = document.select("returninfo").attr("retcode");
            String message = document.select("returninfo").attr("message");
            if ("0".equals(retcode)) {//登录成功
                CookieHelper.getCookiesFresh(event.getCookieList(), result);
                String sendSmsFlag = document.select("re_userinfo").attr("sendSmsFlag");
                if ("01".equals(sendSmsFlag)) {
                    //保存登录后的event，方便重新获取短信验证码
                    String key = String.format(Constant.ISHOP_CCB_TASK_QUEUE, event.getUserid());
                    redis.set(key, JSON.toJSONString(event), 600);
                    event.setState(CCBIShopState.SMS);
                } else {
                	event.setState(CCBIShopState.ESHOP);
                }
            } else if ("1023800".equals(retcode)) {//证件号或密码错误
                event.setException(new SpiderException(1095, "证件号或密码错误"));
            } else if ("1".equals(retcode)) {//错误
            	logger.info("loginjfDown中信登录错误信息code:{},message：{}",retcode,message);
                if(message.contains("服务繁忙")){
                    event.setException(new SpiderException(1122, "服务繁忙"));
                }else{
                    event.setException(new SpiderException(1094, "验证码错误"));
                }
            } else {
                event.setException(new SpiderException(1093, message));
            }
        }
        
        private void queryjfDown(HttpResponse result) {
            logger.debug("get jf down {}", event.getId());
            String resultStr = null;
            try {
                resultStr = EntityUtils.toString(result.getEntity());
            } catch (IOException e) {
                logger.error("获取积分返回结果失败", e);
            }

            if (StringUtils.isEmpty(resultStr)) {
                event.setException(new SpiderException(1103, "获取积分失败"));
                return;
            }
            Document document = Jsoup.parse(resultStr);
            String retcode = document.select("returninfo").attr("retcode");
            if ("0".equals(retcode)) {
                Elements cards = document.select("signcardno");
                String username = document.select("userinfo").attr("cust_local_name");
                Long jftotal = 0L;
                for (Element e : cards) {
                    String jfStr = e.attr("point");
                    String cardNo = e.attr("card");
                    
                    if (StringUtils.isNumeric(jfStr)) {
                    	jftotal += Long.parseLong(jfStr);
                    }
                }
                
                Map<String,String> map = new HashMap<String, String>();
                map.put("jf", jftotal.toString());
                map.put("name", username);
                
                event.setException(new SpiderException(0, "查询积分成功",JSON.toJSONString(map)));
            } else {
                event.setException(new SpiderException(1103, "获取积分失败"));
                return;
            }
        }

        
        
        /**
         * 请求验证码完成
         *
         * @param result
         */
        private void smsDown(HttpResponse result) {
            logger.debug("request sms down {}", event.getId());
            String resultStr = null;
            try {
                resultStr = EntityUtils.toString(result.getEntity());
            } catch (IOException e) {
                logger.error("获取发送短信验证码返回结果失败", e);
            }
            if (StringUtils.isEmpty(resultStr)) {
                logger.error("短信验证码发送失败");
                event.setException(new SpiderException(1096, "短信验证码发送失败"));
                return;
            }
            Document document = Jsoup.parse(resultStr);
            String retcode = document.select("returninfo").attr("retcode");
            String message = document.select("returninfo").attr("message");
            if ("0".equals(retcode)) {
                String key = String.format(Constant.ISHOP_CCB_TASK_QUEUE, event.getUserid());
                redis.set(key, JSON.toJSONString(event), redisTime);
                event.setException(new SpiderException(1097, "短信验证码发送成功"));
            } else {
                logger.error("短信验证码发送失败");
                event.setException(new SpiderException(1096, message));
            }
        }

        /**
         * 短信验证码验证完成
         *
         * @param result
         */
        private void checkDown(HttpResponse result) {
            logger.debug("request check down {}", event.getId());
            String resultStr = null;
            try {
                resultStr = EntityUtils.toString(result.getEntity());
            } catch (IOException e) {
                logger.error("获取短信验证返回结果失败", e);
            }
            if (StringUtils.isEmpty(resultStr)) {
                event.setException(new SpiderException(1099, "短信验证失败"));
                return;
            }
            Document document = Jsoup.parse(resultStr);
            String retcode = document.select("returninfo").attr("retcode");
            String message = document.select("returninfo").attr("message");
            if ("0".equals(retcode)) {
            	event.setState(CCBIShopState.ESHOP);
            	logger.info("短信验证码验证成功");
            } else {
                event.setException(new SpiderException(1099, "短信验证失败"));
            }
        }

        /**
         * eshop完成
         *
         * @param result
         */
        private void eshopDown(HttpResponse result) {
            logger.debug("request eshop down {}", event.getId());
            String resultStr = null;
            try {
                resultStr = EntityUtils.toString(result.getEntity());
            } catch (IOException e) {
                logger.error("获取local_param返回结果失败", e);
            }
            if (StringUtils.isEmpty(resultStr)) {
                event.setException(new SpiderException(1116, "获取local_param失败"));
                return;
            }
            Document document = Jsoup.parse(resultStr);
            Elements elements = document.getElementsByTag("input");
            if (elements == null || elements.size() == 0) {
                event.setException(new SpiderException(1116, "获取local_param失败"));
                return;
            }
            String local_param = elements.get(0).attr("value");
            logger.debug(local_param);
            event.setLocal_param(local_param);
        	logger.info("eshop完成");
            event.setState(CCBIShopState.ESHOPLOGIN);
        }

        /**
         * eshopLogin完成
         *
         * @param result
         */
        private void eshopLoginDown(HttpResponse result) {
            logger.debug("request eshopLogin down {}", event.getId());
        	logger.info("eshopLoginDown");
            if("1".equals(event.getQuerytype())){
            	logger.info("eshopLoginDown，积分查询");
            	 event.setState(CCBIShopState.QUERY_JF);
            }else{
            	//登录信息入缓存
            	logger.info("eshopLoginDown，登录成功");
            	event.setLoginType("1");
             	String key = String.format(Constant.ISHOP_CCB_TASK_QUEUE, event.getUserid());
                redis.set(key, JSON.toJSONString(event), redisTime);
            	event.setException(new SpiderException(1001, "登录成功"));
            	  //event.setState(CCBIShopState.GETINTEGRAL);
            }
        }

        /**
         * 请求获取积分完成
         *
         * @param result
         */
        private void getIntegralDown(HttpResponse result) {
            logger.debug("get jf down {}", event.getId());
            logger.info("getIntegralDown");
            String resultStr = null;
            try {
                resultStr = EntityUtils.toString(result.getEntity());
                logger.info("获取积分数据：{}",resultStr);
            } catch (IOException e) {
                logger.error("获取积分返回结果失败", e);
            }

            if (StringUtils.isEmpty(resultStr)) {
                event.setException(new SpiderException(1103, "获取积分失败"));
                logger.info("获取积分失败，返回数据为空！");
                return;
            }
            Document document = Jsoup.parse(resultStr);
            String retcode = document.select("returninfo").attr("retcode");
            logger.info("获取积分，返回retcode值为："+retcode);
            if ("0".equals(retcode)) {
                boolean flag = true;
                Elements cards = document.select("signCardno");
                for (Element e : cards) {
                    String jfStr = e.attr("point");
                    String cardNo = e.attr("card");
                    if (StringUtils.isNumeric(jfStr)) {
                        CCBICard ccbiCard = new CCBICard();
                        ccbiCard.setIntegral(Long.parseLong(jfStr));
                        ccbiCard.setCardNo(cardNo);
                        event.getCards().add(ccbiCard);
                        if (ccbiCard.getIntegral().compareTo(event.getPrice() * event.getQuantity()) > 0) {
                            flag = false;
                        }
                    }
                }
                if (flag) {//积分不足，不能购买商品
                    event.setException(new SpiderException(1109, "您的积分不足"));
                    return;
                }
                //TODO: set next step
                event.setState(CCBIShopState.CLEAR_CART);
            } else {
            	if(resultStr.contains("请登录后再操作")){
	                event.setException(new SpiderException(1005, "请重新登录"));
	                logger.info("获取积分失败，返回retcode不为0！");
	                return;
            	}else{
            		event.setException(new SpiderException(1103, "获取积分失败"));
	                logger.info("获取积分失败，返回retcode不为0！");
	                return;
            	}
            }
        }

        /**
		 * 	
		 * @Description: (方法职责详细描述,可空)  
		 * @Title: clearCartDown 
		 * @param result
		 * @date 2016年6月3日 下午8:36:18  
		 * @author ws
		*/
		private void clearCartDown(HttpResponse result) {
			logger.debug("request delCart down {}", event.getId());
            String resultStr = null;
            try {
                resultStr = EntityUtils.toString(result.getEntity());
            } catch (IOException e) {
                logger.error("请求删除购物车返回结果失败:{}", resultStr, e);
            }
            if (StringUtils.isEmpty(resultStr)) {
                event.setException(new SpiderException(1114, "清理购物车失败"));
                return;
            }
            Document document = Jsoup.parse(resultStr);
            String retcode = document.select("returninfo").attr("retcode");
            if ("0".equals(retcode)) {
                logger.info("下单前清理购物车成功");
                event.setState(CCBIShopState.ADDCART);
            } else {
                logger.info("下单前清理购物车失败，{}", retcode);
                event.setException(new SpiderException(1114, "清理购物车失败"));
            }
		}

        /**
         * 请求添加购物车完成
         *
         * @param result
         */
        private void addCartDown(HttpResponse result) {
            logger.debug("request addCart down {}", event.getId());
            String resultStr = null;
            try {
                resultStr = EntityUtils.toString(result.getEntity());
            } catch (IOException e) {
                logger.error("请求添加到购物车返回结果失败:{}", resultStr, e);
            }
            if (StringUtils.isEmpty(resultStr)) {
                event.setException(new SpiderException(1110, "添加商品到购物车失败"));
                return;
            }
            Document document = Jsoup.parse(resultStr);
            String retcode = document.select("returninfo").attr("retcode");
            if ("0".equals(retcode)) {
                logger.info("添加商品到购物车成功");
                //event.setState(CCBIShopState.INSERTADDR);
                event.setState(CCBIShopState.QUERYCART);
            } else {
                logger.info("添加商品到购物车失败:{}", retcode);
                event.setException(new SpiderException(1110, "添加商品到购物车失败"));
            }
        }

        /**
         * 请求查询购物车完成
         *
         * @param result
         */
        private void queryCartDown(HttpResponse result) {
            logger.debug("request queryCart down {}", event.getId());
            String resultStr = null;
            try {
                resultStr = EntityUtils.toString(result.getEntity());
            } catch (IOException e) {
                logger.error("获取购物车返回结果失败:{}", resultStr, e);
            }
            if (StringUtils.isEmpty(resultStr)) {
                event.setException(new SpiderException(1111, "获取购物车商品失败"));
                return;
            }
            event.setState(CCBIShopState.QUERYCART1);
        }

        private void queryCartDown1(HttpResponse result) {
            logger.debug("request queryCart down {}", event.getId());
            String resultStr = null;
            try {
                resultStr = EntityUtils.toString(result.getEntity());
            } catch (IOException e) {
                logger.error("获取购物车返回结果失败:{}", resultStr, e);
            }
            if (StringUtils.isEmpty(resultStr)) {
                event.setException(new SpiderException(1111, "获取购物车商品失败"));
                return;
            }
            event.setState(CCBIShopState.BUYCONFIRM);
        }

        /**
         * 插入地址
         *
         * @param result
         */
        private void insertAddrDown(HttpResponse result) {
            logger.debug("insert address down: {}", event.getId());
            String resultStr = null;
            try {
                resultStr = EntityUtils.toString(result.getEntity());
            } catch (IOException e) {
                logger.error("新增地址失败:{}", resultStr, e);
            }
            if (StringUtils.isEmpty(resultStr)) {
                event.setException(new SpiderException(1104, "新增地址失败"));
                return;
            }

            Document document = Jsoup.parse(resultStr);
            String retcode = document.select("returninfo").attr("retcode");
            String message = document.select("returninfo").attr("message");
            if (retcode.equals("0")) {
                event.setState(CCBIShopState.QUERYADDR);
                return;
            } else {
                event.setException(new SpiderException(1104, message));
                return;
            }
        }

        /**
         * 地址查询
         *
         * @param result
         */
        private void queryAddrDown(HttpResponse result) {
            logger.debug("query address down: {}", event.getId());
            String resultStr = null;
            try {
                resultStr = EntityUtils.toString(result.getEntity());
            } catch (IOException e) {
                logger.error("地址查询失败:{}", resultStr, e);
            }
            if (StringUtils.isEmpty(resultStr)) {
                event.setException(new SpiderException(1106, "地址查询失败"));
                return;
            }

            Document document = Jsoup.parse(resultStr);
            String retcode = document.select("returninfo").attr("retcode");
            if (retcode.equals("0")) {
                Elements cydzs = document.select("cydz");
                String addrId = null;
                CCBIAddrQuery addrInfo = event.getAddrInfo();
                for (Element cydz : cydzs) {
                    String id = cydz.attr("id");
                    String zip = cydz.attr("csg_postcode");
                    String address = cydz.attr("csg_address");
                    String identity = cydz.attr("csg_id_nbr");
                    String mobile = cydz.attr("csg_mobile");
                    String name = cydz.attr("csg_name");

                    if (zip.equals(addrInfo.getAddrZip())
                            && address.equals(addrInfo.getAddrMail())
                            && identity.equals(addrInfo.getAdd_id_nbr())
                            && mobile.equals(addrInfo.getAddrMobi())
                            && name.equals(addrInfo.getAddrName())) {
                        addrId = id;
                    }
                }

                if (StringUtils.isEmpty(addrId)) {
                    event.setException(new SpiderException(1106, "地址查询失败"));
                    return;
                } else {
                    event.setAddrId(addrId);
                    event.setState(CCBIShopState.QUERYCART);
                }
            } else {
                event.setException(new SpiderException(1106, "地址查询失败"));
                return;
            }
        }

        /**
         * 请求确认购买完成
         *
         * @param result
         */
        private void buyConfirmDown(HttpResponse result) {
            logger.debug("request buyConfirm down {}", event.getId());
            String resultStr = null;
            try {
                resultStr = EntityUtils.toString(result.getEntity());
            } catch (IOException e) {
                logger.error("请求确认购买返回结果失败:{}", resultStr, e);
            }
            if (StringUtils.isEmpty(resultStr)) {
                event.setException(new SpiderException(1112, "确认购买失败"));
                return;
            }
            Document document = Jsoup.parse(resultStr);
            String retcode = document.select("returninfo").attr("retcode");
            if ("0".equals(retcode)) {
                logger.info("确认购买成功");
                event.setState(CCBIShopState.ORDERVCODE);
            } else {
                logger.info("确认购买失败:{}", retcode);
                event.setException(new SpiderException(1112, "确认购买失败"));
            }
        }

        /**
         * 请求下单验证码完成
         *
         * @param result
         */
        private void orderVcodeDown(HttpResponse result) {
            try {
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
                    FileOutputStream fs = new FileOutputStream(
                            appConfig.getUploadPath() + "/" + event.getUserid()
                                    + ".jpg");
                    fs.write(tbytes);
                    fs.close();
                }
                String key = String.format(Constant.ISHOP_CCBI_ORDER_QUEUE, event.getUserid());
                //TODO
               // ISHOP_CCBI_ORDER_QUEUE
                redis.set(key, JSON.toJSONString(event), redisTime);
                event.setException(new SpiderException(1001, "输入验证码", imgStr));
            } catch (Exception e) {
                logger.error("get vcode Down exception", e);
                event.setException(e);
            }
        }

        /**
         * 查询卡片
         *
         * @param result
         */
        private void queryCardDown(HttpResponse result) {
            logger.debug("request queryCard down {}", event.getId());
            event.setState(CCBIShopState.ORDER);
        }

        /**
         * 请求下单完成
         *
         * @param result
         */
        private void orderDown(HttpResponse result) {
            logger.debug("request order down {}", event.getId());
            String resultStr = null;
            try {
                resultStr = EntityUtils.toString(result.getEntity());
            } catch (IOException e) {
                logger.error("获得下单返回结果失败:{}", resultStr, e);
            }
            if (StringUtils.isEmpty(resultStr)) {
                event.setException(new SpiderException(1113, "下单失败"));
                return;
            }
            Document document = Jsoup.parse(resultStr);
            String retcode = document.select("returninfo").attr("retcode");
            String message = document.select("returninfo").attr("message");
            if ("0".equals(retcode)) {
                logger.info("下单成功");
                String orderNo = document.select("Rsobject").attr("order_id");
                logger.debug("订单号为：" + orderNo);
                event.setOrderNo(orderNo);
                event.setState(CCBIShopState.DELCART);
            } else if("1".equals(retcode) && message.contains("请输入正确的验证码")){
                logger.info("下单失败:{}", message);
                event.setException(new SpiderException(1121, message));
            }else {
                logger.info("下单失败:{}", retcode);
                event.setException(new SpiderException(1113, "下单失败"));
            }
        }

        /**
         * 请求删除购物车完成
         *
         * @param result
         */
        private void delCartDown(HttpResponse result) {
            logger.debug("request delCart down {}", event.getId());
            String resultStr = null;
            try {
                resultStr = EntityUtils.toString(result.getEntity());
            } catch (IOException e) {
                logger.error("请求删除购物车返回结果失败:{}", resultStr, e);
            }
            if (StringUtils.isEmpty(resultStr)) {
                event.setException(new SpiderException(1114, "删除购物车失败"));
                return;
            }
            Document document = Jsoup.parse(resultStr);
            String retcode = document.select("returninfo").attr("retcode");
            if ("0".equals(retcode)) {
                logger.info("删除购物车成功");
                event.setException(new SpiderException(1115, "下单成功", event.getOrderNo()));
                //event.setState(CCBIShopState.DELADDR);
            } else {
                logger.info("删除购物车失败:{}", retcode);
                event.setException(new SpiderException(1114, "删除购物车失败"));
            }
        }

        /**
         * 删除地址
         *
         * @param result
         */
        private void deleteAddrDown(HttpResponse result) {
            logger.debug("insert address down: {}", event.getId());
            String resultStr = null;
            try {
                resultStr = EntityUtils.toString(result.getEntity());
            } catch (IOException e) {
                logger.error("删除地址失败", e);
            }
            if (StringUtils.isEmpty(resultStr)) {
                event.setException(new SpiderException(1107, "删除址失败"));
                return;
            }

            Document document = Jsoup.parse(resultStr);
            String retcode = document.select("returninfo").attr("retcode");
            if (retcode.equals("0")) {
                // TODO:删除地址成功
                event.setException(new SpiderException(1115, "下单成功", event.getOrderNo()));
            } else {
                event.setException(new SpiderException(1107, "删除址失败"));
            }
        }

        private void errorHandle(CCBIShopEvent event) {
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