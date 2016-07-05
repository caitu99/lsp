/*
 * Copyright (c) 2015-2020 by caitu99
 * All rights reserved.
 */
package com.caitu99.lsp.spider.pingan;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpEntity;
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
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.request.async.DeferredResult;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.caitu99.lsp.AppConfig;
import com.caitu99.lsp.Constant;
import com.caitu99.lsp.cache.RedisOperate;
import com.caitu99.lsp.exception.SpiderException;
import com.caitu99.lsp.model.spider.QueryEvent;
import com.caitu99.lsp.model.spider.pingan.PingAnOilSpiderEvent;
import com.caitu99.lsp.model.spider.pingan.PingAnOilSpiderState;
import com.caitu99.lsp.model.spider.pingan.PingAnSpiderState;
import com.caitu99.lsp.spider.HttpAsyncClient;
import com.caitu99.lsp.spider.QuerySpider;
import com.caitu99.lsp.utils.SpringContext;
import com.caitu99.lsp.utils.XpathHtmlUtils;
import com.caitu99.lsp.utils.cookie.CookieHelper;
import com.caitu99.lsp.utils.cookie.HttpCookieEx;

/** 
 * 
 * @Description: (类职责详细描述,可空) 
 * @ClassName: PingAnSpider 
 * @author ws
 * @date 2016年4月1日 上午14:42:23 
 * @Copyright (c) 2015-2020 by caitu99 
 */
public class PingAnOilSpider implements QuerySpider{

	private static final Logger logger = LoggerFactory
            .getLogger(PingAnOilSpider.class);

	private RedisOperate redis = SpringContext.getBean(RedisOperate.class);
	
	private CloseableHttpAsyncClient httpAsyncClient = HttpAsyncClient.getInstance();
	
    private AppConfig appConfig = SpringContext.getBean(AppConfig.class);
	
	private static final String userAgent = "Mozilla/5.0 (Windows NT 6.3; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/46.0.2490.86 Safari/537.36";
  
	private static final String initpn = "http://www.wanlitong.com/login.do?gURL=http://www.wanlitong.com/";
    private static final String imgpn = "https://member.wanlitong.com/vcodeImage.do?timestamp=%s";
	private static final String loginpn = "https://member.wanlitong.com/getLogin.do";
    private static final String ssologin = "https://www.wanlitong.com/newssologin";
    
    private static final String oilPage = "http://www.wanlitong.com/spendpoints/refuel/spendSinopecOilCardPwd.do";
    private static final String vcodeUrl = "http://www.wanlitong.com/paic/common/vcodeweb.do?timestamp=%s";
    private static final String verifyUrl = "http://www.wanlitong.com/ajaxValidateVcode.do?code=%s&timestamp=%s";
    private static final String orderUrl = "http://www.wanlitong.com/spendpoints/virtual/orderConfirm.do";
    private static final String beforPayUrl = "https://payment.wanlitong.com/payment/wltContinuePay.do";
    private static final String submitPayUrl = "https://payment.wanlitong.com/payment/wltSubmitPay.do";
    private static final String sucPayUrl = "https://payment.wanlitong.com/payment/wltPaySuc.do";
    
    private static final Pattern pattern_payUrl = Pattern.compile("(?<=pin = ').*?(?=';)");
	
    @Override
	public void onEvent(QueryEvent event) {
		PingAnOilSpiderEvent pingAnEvent = (PingAnOilSpiderEvent)event;
		try {
			switch (pingAnEvent.getState()) {
				
			case INIT_SP:
				initspUp(pingAnEvent);
				break;
			case LOGIN_INIT_SP:
				loginInitspUp(pingAnEvent);
				break;
			case IMG_SP:
				imgspUp(pingAnEvent);
				break;
			case LOGIN_SP:
				loginspUp(pingAnEvent);
				break;
			case TO_AUTH_SP:
				toauthUp(pingAnEvent);
				break;
			case SSOLOGIN_SP:
				ssologinUp(pingAnEvent);
				break;	

			case OIL_PAGE:
				oilPageUp(pingAnEvent);
				break;	
			case OIL_VCODE:
				vcodeUp(pingAnEvent);
				break;	
			case OIL_VERIFY:
				verifyUp(pingAnEvent);
				break;	
			case OIL_ORDER:
				oilOrderUp(pingAnEvent);
				break;	
			case OIL_PAY_PAGE:
				oilPayPageUp(pingAnEvent);
				break;	
			case OIL_BEFOR_PAY:
				oilBeforPayUp(pingAnEvent);
				break;	
			case OIL_MSG:
				oilMsgUp(pingAnEvent);
				break;	
			case OIL_SUBMIT_PAY:
				oilSubmitPayUp(pingAnEvent);
				break;		
			case OIL_SUC_PAY:
				oilSucPayUp(pingAnEvent);
				break;	
				
			case ERROR:
                errorHandle(event);
                break;
			}
		} catch (Exception e) {
			logger.warn("request up error {}", event.getId(), e);
			pingAnEvent.setException(e);
            errorHandle(pingAnEvent);
		}
	}
	
	
	
	/**
	 * 	
	 * @Description: (方法职责详细描述,可空)  
	 * @Title: oilMsgUp 
	 * @param pingAnEvent
	 * @date 2016年4月6日 下午12:02:15  
	 * @author ws
	*/
	private void oilMsgUp(PingAnOilSpiderEvent pingAnEvent) {
		logger.info("oilMsgUp {}", pingAnEvent.getAccount());
		
		String url = "https://payment.wanlitong.com/payment/ajaxSendOTP.do";
        HttpPost httpPost = new HttpPost(url);
        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("sendType", "phoneCode"));
        try {
			httpPost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
		} catch (UnsupportedEncodingException e) {
			   logger.error("check account error {}", pingAnEvent.getAccount(), e);
			   pingAnEvent.setException(e);
	            return;
		}
        
        setHeader(url, httpPost, pingAnEvent);
        httpAsyncClient.execute(httpPost, new HttpAsyncCallback(pingAnEvent));	
	}



	/**
	 * 	
	 * @Description: (方法职责详细描述,可空)  
	 * @Title: verifyUp 
	 * @param pingAnEvent
	 * @date 2016年4月5日 上午11:18:03  
	 * @author ws
	*/
	private void verifyUp(PingAnOilSpiderEvent pingAnEvent) {
		logger.info("oilSubmitPayUp {}", pingAnEvent.getAccount());
		String timestamp = pingAnEvent.getTimestamp();
		String url = String.format(verifyUrl, pingAnEvent.getvCode(),timestamp );

        HttpPost httpPost = new HttpPost(url);
        httpPost.setHeader("Referer", "http://www.wanlitong.com/spendpoints/refuel/spendSinopecOilCardPwd.do");
        httpPost.setHeader("Origin", "http://www.wanlitong.com");
        setHeader(url, httpPost, pingAnEvent);
        httpAsyncClient.execute(httpPost, new HttpAsyncCallback(pingAnEvent));
	}



	/**
	 * 	
	 * @Description: (方法职责详细描述,可空)  
	 * @Title: vcodeUp 
	 * @param pingAnEvent
	 * @date 2016年4月5日 上午11:18:00  
	 * @author ws
	*/
	private void vcodeUp(PingAnOilSpiderEvent pingAnEvent) {
		logger.info("oilPageUp {}", pingAnEvent.getAccount());
		String timestamp = PingAnOilUtil.getTimeStamp();
		pingAnEvent.setTimestamp(timestamp);
		
		String url = String.format(vcodeUrl, timestamp);
        HttpGet httpGet = new HttpGet(url);
        setHeader(url, httpGet, pingAnEvent);
        httpAsyncClient.execute(httpGet, new HttpAsyncCallback(pingAnEvent));
	}


	

	/**
	 * 	
	 * @Description: (方法职责详细描述,可空)  
	 * @Title: oilPageUp 
	 * @param pingAnEvent
	 * @date 2016年4月1日 下午3:24:41  
	 * @author ws
	*/
	private void oilPageUp(PingAnOilSpiderEvent pingAnEvent) {
		logger.info("oilPageUp {}", pingAnEvent.getAccount());
		String url = oilPage;
        HttpGet httpGet = new HttpGet(url);
        setHeader(url, httpGet, pingAnEvent);
        httpAsyncClient.execute(httpGet, new HttpAsyncCallback(pingAnEvent));
	}



	/**
	 * 	
	 * @Description: (方法职责详细描述,可空)  
	 * @Title: oilSucPayUp 
	 * @param pingAnEvent
	 * @date 2016年4月1日 下午3:10:41  
	 * @author ws
	 * @throws UnsupportedEncodingException 
	*/
	private void oilSucPayUp(PingAnOilSpiderEvent pingAnEvent) throws UnsupportedEncodingException {
		logger.info("oilSubmitPayUp {}", pingAnEvent.getAccount());
		String url = sucPayUrl;

        HttpPost httpPost = new HttpPost(url);
		Map<String,String> paramMap = pingAnEvent.getParamMap();
		Map<String,String> orderMap = pingAnEvent.getOrderMap();
		
        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("sucForm_orderPoints", paramMap.get("points")));
        params.add(new BasicNameValuePair("sucForm_gUrl", pingAnEvent.getPayGUrl()));//支付时gUrl
        params.add(new BasicNameValuePair("sucForm_retCode", pingAnEvent.getPayRetCode()));//支付时retCode
        params.add(new BasicNameValuePair("sucForm_retMsg", ""));
        params.add(new BasicNameValuePair("sucForm_orderType", paramMap.get("orderType")));
        params.add(new BasicNameValuePair("sucForm_orderCash", paramMap.get("cash")));
        params.add(new BasicNameValuePair("sucForm_orderCoupon", paramMap.get("coupon")));
        params.add(new BasicNameValuePair("sucForm_txnType", "1030"));//固定1030
        params.add(new BasicNameValuePair("sucForm_memberId", orderMap.get("loginUserId")));//下单时订单列中取
        params.add(new BasicNameValuePair("sucForm_id", paramMap.get("id")));
        params.add(new BasicNameValuePair("sucForm_orderId", paramMap.get("orderId")));
        params.add(new BasicNameValuePair("sucForm_merId", paramMap.get("merId")));
        params.add(new BasicNameValuePair("sucForm_reqId", paramMap.get("reqId")));
        params.add(new BasicNameValuePair("sucForm_orderAmt", paramMap.get("totalOrderAmt")));
        params.add(new BasicNameValuePair("sucForm_orderCash", paramMap.get("cash")));
        params.add(new BasicNameValuePair("sucForm_gateId", paramMap.get("gateId")));
        params.add(new BasicNameValuePair("sucForm_parentGateId", ""));
        params.add(new BasicNameValuePair("sucForm_yqbCash", ""));
        params.add(new BasicNameValuePair("sucForm_yqbPayToken", ""));
        params.add(new BasicNameValuePair("sucForm_yqbCash", ""));
        params.add(new BasicNameValuePair("sucForm_yqbPayToken", ""));
        params.add(new BasicNameValuePair("sucForm_sibelRetCode", ""));
        
        httpPost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
        httpPost.setHeader("Referer", "https://payment.wanlitong.com/payment/wltContinuePay.do");
        httpPost.setHeader("Origin", "https://payment.wanlitong.com");
        setHeader(url, httpPost, pingAnEvent);
        httpAsyncClient.execute(httpPost, new HttpAsyncCallback(pingAnEvent));
	}



	/**
	 * 	
	 * @Description: (方法职责详细描述,可空)  
	 * @Title: oilSubmitPayUp 
	 * @param pingAnEvent
	 * @date 2016年4月1日 下午3:10:38  
	 * @author ws
	 * @throws UnsupportedEncodingException 
	*/
	private void oilSubmitPayUp(PingAnOilSpiderEvent pingAnEvent) throws UnsupportedEncodingException {
		logger.info("oilSubmitPayUp {}", pingAnEvent.getAccount());
		String url = submitPayUrl;
		String payMode = pingAnEvent.getPayMod();
        HttpPost httpPost = new HttpPost(url);
		Map<String,String> paramMap = pingAnEvent.getParamMap();
		
        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("orderId", paramMap.get("orderId")));
        params.add(new BasicNameValuePair("orderDate", paramMap.get("orderDate")));
        params.add(new BasicNameValuePair("merId", paramMap.get("merId")));
        params.add(new BasicNameValuePair("reqId", paramMap.get("reqId")));
        params.add(new BasicNameValuePair("totalOrderAmt", paramMap.get("totalOrderAmt")));
        params.add(new BasicNameValuePair("orderAmt", paramMap.get("orderAmt")));
        params.add(new BasicNameValuePair("id", paramMap.get("id")));
        params.add(new BasicNameValuePair("payMode", payMode));
        params.add(new BasicNameValuePair("cash", paramMap.get("cash")));
        params.add(new BasicNameValuePair("points", paramMap.get("points")));
        params.add(new BasicNameValuePair("gateId", paramMap.get("gateId")));
        params.add(new BasicNameValuePair("orderType", paramMap.get("orderType")));
        params.add(new BasicNameValuePair("GiftNo", paramMap.get("GiftNo")));
        params.add(new BasicNameValuePair("GiftNoDis", paramMap.get("GiftNoDis")));
        params.add(new BasicNameValuePair("coupon", paramMap.get("coupon")));
        params.add(new BasicNameValuePair("yqbId", paramMap.get("yqbId")));
        params.add(new BasicNameValuePair("yqbCash", paramMap.get("yqbCash")));
        params.add(new BasicNameValuePair("yqbPayToken", paramMap.get("yqbPayToken")));
        params.add(new BasicNameValuePair("otherGateIndexArray", paramMap.get("otherGateIndexArray")));
        params.add(new BasicNameValuePair("otherGateId_4", paramMap.get("otherGateId_4")));
        params.add(new BasicNameValuePair("gateType_4", paramMap.get("gateType_4")));
        params.add(new BasicNameValuePair("otherGateUsePoints_4", paramMap.get("otherGateUsePoints_4")));
        params.add(new BasicNameValuePair("otherGatePayAmt_4", paramMap.get("otherGatePayAmt_4")));
        
        if("1".equals(payMode)){
       	 	params.add(new BasicNameValuePair("code", pingAnEvent.getMsgCode()));
        }else if("2".equals(payMode)){
       	 	params.add(new BasicNameValuePair("payPassWord", pingAnEvent.getPayPwd()));
        }else if("3".equals(payMode)){
        	params.add(new BasicNameValuePair("code", pingAnEvent.getMsgCode()));
        	params.add(new BasicNameValuePair("payPassWord", pingAnEvent.getPayPwd()));
        }
        
        httpPost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
        httpPost.setHeader("Referer", "https://payment.wanlitong.com/payment/wltContinuePay.do");
        httpPost.setHeader("Origin", "https://payment.wanlitong.com");
        setHeader(url, httpPost, pingAnEvent);
        httpAsyncClient.execute(httpPost, new HttpAsyncCallback(pingAnEvent));
	}



	/**
	 * 	
	 * @Description: (方法职责详细描述,可空)  
	 * @Title: oilBeforPayUp 
	 * @param pingAnEvent
	 * @date 2016年4月1日 下午3:10:36  
	 * @author ws
	 * @throws UnsupportedEncodingException 
	*/
	private void oilBeforPayUp(PingAnOilSpiderEvent pingAnEvent) throws UnsupportedEncodingException {
		logger.info("oilBeforPayUp {}", pingAnEvent.getAccount());
		String url = beforPayUrl;

        HttpPost httpPost = new HttpPost(url);
		Map<String,String> paramMap = pingAnEvent.getParamMap();
		
        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("orderId", paramMap.get("orderId")));
        params.add(new BasicNameValuePair("orderDate", paramMap.get("orderDate")));
        params.add(new BasicNameValuePair("merId", paramMap.get("merId")));
        params.add(new BasicNameValuePair("reqId", paramMap.get("reqId")));
        params.add(new BasicNameValuePair("totalOrderAmt", paramMap.get("totalOrderAmt")));
        params.add(new BasicNameValuePair("orderAmt", paramMap.get("orderAmt")));
        params.add(new BasicNameValuePair("id", paramMap.get("id")));
        params.add(new BasicNameValuePair("platFormGateId", paramMap.get("platFormGateId")));
        params.add(new BasicNameValuePair("gateId", paramMap.get("gateId")));
        params.add(new BasicNameValuePair("parentGateId", paramMap.get("parentGateId")));
        params.add(new BasicNameValuePair("points", pingAnEvent.getTotalPoints().toString()));
        params.add(new BasicNameValuePair("cash", paramMap.get("cash")));
        params.add(new BasicNameValuePair("userPoints", paramMap.get("userPoints")));
        params.add(new BasicNameValuePair("yqbBalance", paramMap.get("yqbBalance")));
        params.add(new BasicNameValuePair("yqbId", paramMap.get("yqbId")));
        params.add(new BasicNameValuePair("yqbUseCash", paramMap.get("yqbUseCash")));
        params.add(new BasicNameValuePair("yqbPayToken", paramMap.get("yqbPayToken")));
        params.add(new BasicNameValuePair("otherGateIndexArray", "4"));//固定为4
        params.add(new BasicNameValuePair("displayName_4", paramMap.get("displayName_4")));//支付总金额
        params.add(new BasicNameValuePair("displayUrl_4", paramMap.get("displayUrl_4")));
        params.add(new BasicNameValuePair("gateType_4", paramMap.get("gateType_4")));
        params.add(new BasicNameValuePair("otherGateId_4", paramMap.get("otherGateId_4")));//接收卡密手机号
        params.add(new BasicNameValuePair("displayName_2", paramMap.get("displayName_2")));
        params.add(new BasicNameValuePair("displayUrl_2", paramMap.get("displayUrl_2")));//图形验证码
        params.add(new BasicNameValuePair("gateType_2", paramMap.get("gateType_2")));
        params.add(new BasicNameValuePair("otherGateId_2", paramMap.get("otherGateId_2")));
        params.add(new BasicNameValuePair("otherGateUsePoints_2", paramMap.get("otherGateUsePoints_2")));
        params.add(new BasicNameValuePair("otherGatePayAmt_2", paramMap.get("otherGatePayAmt_2")));
        params.add(new BasicNameValuePair("displayName_3", paramMap.get("displayName_3")));
        params.add(new BasicNameValuePair("displayUrl_3", paramMap.get("displayUrl_3")));
        params.add(new BasicNameValuePair("gateType_3", paramMap.get("gateType_3")));
        params.add(new BasicNameValuePair("otherGateId_3", paramMap.get("otherGateId_3")));
        params.add(new BasicNameValuePair("otherGateUsePoints_3", paramMap.get("otherGateUsePoints_3")));
        params.add(new BasicNameValuePair("otherGatePayAmt_3", paramMap.get("otherGatePayAmt_3")));

        httpPost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
        httpPost.setHeader("Referer", pingAnEvent.getgUrl());
        httpPost.setHeader("Origin", "https://payment.wanlitong.com");
        setHeader(url, httpPost, pingAnEvent);
        httpAsyncClient.execute(httpPost, new HttpAsyncCallback(pingAnEvent));
	}



	/**
	 * 	
	 * @Description: (方法职责详细描述,可空)  
	 * @Title: oilPayPageUp 
	 * @param pingAnEvent
	 * @date 2016年4月1日 下午3:10:34  
	 * @author ws
	*/
	private void oilPayPageUp(PingAnOilSpiderEvent pingAnEvent) {
		logger.info("oilPayPageUp {}", pingAnEvent.getAccount());
		String url = pingAnEvent.getgUrl();
        HttpGet httpGet = new HttpGet(url);
        setHeader(url, httpGet, pingAnEvent);
        httpGet.setHeader("Referer", "http://www.wanlitong.com/spendpoints/virtual/orderConfirm.do");
        httpAsyncClient.execute(httpGet, new HttpAsyncCallback(pingAnEvent));
	}



	/**
	 * 	
	 * @Description: (方法职责详细描述,可空)  
	 * @Title: oilOrderUp 
	 * @param pingAnEvent
	 * @date 2016年4月1日 下午3:10:31  
	 * @author ws
	 * @throws UnsupportedEncodingException 
	*/
	private void oilOrderUp(PingAnOilSpiderEvent pingAnEvent) throws UnsupportedEncodingException {
		logger.info("oilOrderUp {}", pingAnEvent.getAccount());
		String url = orderUrl;

        HttpPost httpPost = new HttpPost(url);
		Map<String,String> paramMap = pingAnEvent.getOrderMap();
		
        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("oldAmount", " "));//
        params.add(new BasicNameValuePair("cash", paramMap.get("cash")));
        params.add(new BasicNameValuePair("point", paramMap.get("point")));
        params.add(new BasicNameValuePair("allpoint", paramMap.get("allpoint")));
        params.add(new BasicNameValuePair("productName", paramMap.get("productName")));
        params.add(new BasicNameValuePair("sinopecType", paramMap.get("sinopecType")));
        params.add(new BasicNameValuePair("productId", pingAnEvent.getProductId()));
        params.add(new BasicNameValuePair("payType", paramMap.get("payType")));
        params.add(new BasicNameValuePair("mediaID", paramMap.get("mediaID")));
        params.add(new BasicNameValuePair("partnerCode", paramMap.get("partnerCode")));
        params.add(new BasicNameValuePair("fee", paramMap.get("fee")));
        params.add(new BasicNameValuePair("sessionKey", paramMap.get("sessionKey")));
        params.add(new BasicNameValuePair("mobileCode", paramMap.get("mobileCode")));
        params.add(new BasicNameValuePair("isUseCard", paramMap.get("isUseCard")));
        params.add(new BasicNameValuePair("isCheckUser", paramMap.get("isCheckUser")));
        params.add(new BasicNameValuePair("loginUserId", paramMap.get("loginUserId")));
        params.add(new BasicNameValuePair("productArray", pingAnEvent.getProductId()+"@1@01"));
        params.add(new BasicNameValuePair("myToken", paramMap.get("myToken")));
        params.add(new BasicNameValuePair("totalPoints", String.valueOf(pingAnEvent.getTotalPoints())));//支付总金额
        params.add(new BasicNameValuePair("totalCash", paramMap.get("totalCash")));
        params.add(new BasicNameValuePair("avaPoints", paramMap.get("avaPoints")));
        params.add(new BasicNameValuePair("mobilePhoneNum", pingAnEvent.getPhoneNum()));//接收卡密手机号
        params.add(new BasicNameValuePair("radio", "01"));//固定01
        params.add(new BasicNameValuePair("verifyCode", pingAnEvent.getvCode()));//图形验证码
        params.add(new BasicNameValuePair("timestamp", pingAnEvent.getTimestamp()));

        httpPost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
        httpPost.setHeader("Referer", "http://www.wanlitong.com/spendpoints/refuel/spendSinopecOilCardPwd.do");
        httpPost.setHeader("Origin", "http://www.wanlitong.com");
        setHeader(url, httpPost, pingAnEvent);
        httpAsyncClient.execute(httpPost, new HttpAsyncCallback(pingAnEvent));
	}



	private void initspUp(PingAnOilSpiderEvent pingAnEvent){
		logger.info("initspUp {}", pingAnEvent.getAccount());
		String url = initpn;
        HttpGet httpGet = new HttpGet(url);
        setHeader(url, httpGet, pingAnEvent);
        httpAsyncClient.execute(httpGet, new HttpAsyncCallback(pingAnEvent));
	}
	
	private void loginInitspUp(PingAnOilSpiderEvent pingAnEvent){
		logger.info("loginInitspUp {}", pingAnEvent.getAccount());
		String url = pingAnEvent.getLoginUrl();
        HttpGet httpGet = new HttpGet(url);
        setHeader(url, httpGet, pingAnEvent);
        httpAsyncClient.execute(httpGet, new HttpAsyncCallback(pingAnEvent));
	}
	
	private void imgspUp(PingAnOilSpiderEvent pingAnEvent){
		logger.info("loginInitspUp {}", pingAnEvent.getAccount());
		Date date = new Date();
		String url = String.format(imgpn, date.getTime());
        HttpGet httpGet = new HttpGet(url);
        setHeader(url, httpGet, pingAnEvent);
        httpAsyncClient.execute(httpGet, new HttpAsyncCallback(pingAnEvent));
		
	}
	
	private void loginspUp(PingAnOilSpiderEvent pingAnEvent){
		logger.info("loginspUp {}", pingAnEvent.getAccount());
		String url = loginpn;
		Date date = new Date();
        HttpPost httpPost = new HttpPost(url);
        String account = pingAnEvent.getAccount();
        String password =  pingAnEvent.getPassword();
        String imgcode = pingAnEvent.getvCode();
        
        String gURL = pingAnEvent.getgUrl();
        String backURL = pingAnEvent.getBackURL();
        String appId = pingAnEvent.getAppId();
        Long timestamp =date.getTime();
        
        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("gURL", gURL));
        params.add(new BasicNameValuePair("backURL", backURL));
        params.add(new BasicNameValuePair("appId", appId));
        params.add(new BasicNameValuePair("j_username", account));
        params.add(new BasicNameValuePair("j_password", password));
        params.add(new BasicNameValuePair("validCode", imgcode));
        params.add(new BasicNameValuePair("timestamp", timestamp.toString()));
        params.add(new BasicNameValuePair("historyLoginName", "on"));
        
        params.add(new BasicNameValuePair("wltAppId", ""));
        params.add(new BasicNameValuePair("wltMemberId", ""));
        params.add(new BasicNameValuePair("wltEmailAddr", ""));
        params.add(new BasicNameValuePair("wltLoginName", ""));
        params.add(new BasicNameValuePair("wltBackURL", ""));
        params.add(new BasicNameValuePair("mobileNum", ""));
        params.add(new BasicNameValuePair("accountOtpCode", ""));

        try {
			httpPost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
		} catch (UnsupportedEncodingException e) {
			   logger.warn("check account error {}", pingAnEvent.getAccount(), e);
			   pingAnEvent.setException(e);
	            return;
		}
        
        setHeader(url, httpPost, pingAnEvent);
        httpAsyncClient.execute(httpPost, new HttpAsyncCallback(pingAnEvent));	
		
	}
	
	private void toauthUp(PingAnOilSpiderEvent pingAnEvent){
		logger.info("toauthUp {}", pingAnEvent.getAccount());
		String url = pingAnEvent.getIntoUrl();
        HttpGet httpGet = new HttpGet(url);
        setHeader(url, httpGet, pingAnEvent);
        httpAsyncClient.execute(httpGet, new HttpAsyncCallback(pingAnEvent));
	}
	
	private void setHeader(String uriStr, HttpMessage httpMessage, QueryEvent event) {
        httpMessage.setHeader("Accept", "*/*");
        httpMessage.setHeader("User-Agent", userAgent);
        try {
            CookieHelper.setCookies2(uriStr, httpMessage, event.getCookieList());
        } catch (URISyntaxException e) {
            logger.warn("set cookie fail {}", event.getId());
        }
    }

	
	
	
	
	private void ssologinUp(PingAnOilSpiderEvent pingAnEvent){
		logger.info("loginspUp {}", pingAnEvent.getAccount());
		String url = ssologin;
        HttpPost httpPost = new HttpPost(url);
        String param = pingAnEvent.getParamValue();
        
        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("param", param));
        try {
			httpPost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
		} catch (UnsupportedEncodingException e) {
			   logger.warn("check account error {}", pingAnEvent.getAccount(), e);
			   pingAnEvent.setException(e);
	            return;
		}
        
        setHeader(url, httpPost, pingAnEvent);
        httpAsyncClient.execute(httpPost, new HttpAsyncCallback(pingAnEvent));	
		
	}
	
	

	@Override
	public void errorHandle(QueryEvent event) {
		DeferredResult<Object> deferredResult = event.getDeferredResult();
        if (deferredResult == null) {
            return;
        }
        if (deferredResult.isSetOrExpired()) {
            logger.info("a request has been expired: {}", event);
            return;
        }
        Exception exception = event.getException();
        if (exception != null) {
            if (exception instanceof SpiderException) {
                deferredResult.setResult(exception.toString());
            } else {
                logger.warn("unknown exception", exception);
                deferredResult.setResult((new SpiderException(-1, exception
                        .getMessage()).toString()));
            }
        }
		
	}
	
	private class HttpAsyncCallback implements FutureCallback<HttpResponse> {

		private PingAnOilSpiderEvent event;
        private boolean skipNextStep = false;

		public HttpAsyncCallback(PingAnOilSpiderEvent event) {
			super();
			this.event = event;
		}

		
		
		@Override
		public void completed(HttpResponse response) {
			try {
				CookieHelper.getCookiesFresh(event.getCookieList(), response);
				switch (event.getState()) {
				case INIT_SP:
					initspDown(response);
					break;
				case LOGIN_INIT_SP:
					loginInitspDown(response);
					break;
				case IMG_SP:
					imgspDown(response);
					break;
					
				case LOGIN_SP:
					loginspDown(response);
					break;
				case TO_AUTH_SP:
					toauthDown(response);
					break;
				case SSOLOGIN_SP:
					ssologinDown(response);
					break;	

				case OIL_PAGE:
					oilPageDown(response);
					break;	
				case OIL_VCODE:
					vcodeDown(response);
					break;
				case OIL_VERIFY:
					verifyDown(response);
					break;	
				case OIL_ORDER:
					oilOrderDown(response);
					break;	
				case OIL_PAY_PAGE:
					oilPayPageDown(response);
					break;	
				case OIL_BEFOR_PAY:
					oilBeforPayDown(response);
					break;	
				case OIL_MSG:
					oilMsgDown(response);
					break;	
				case OIL_SUBMIT_PAY:
					oilSubmitPayDown(response);
					break;		
				case OIL_SUC_PAY:
					oilSucPayDown(response);
					break;	
					
				default:
					break;
				}
			} catch (Exception e) {
				logger.warn("unexpected error {}", event.getId(), e);
                event.setException(e);
			}
			// next step
            if (skipNextStep)
                return;
            onEvent(event);			
		}
		
		
		/**
		 * 	
		 * @Description: (方法职责详细描述,可空)  
		 * @Title: oilMsgDown 
		 * @param response
		 * @date 2016年4月6日 下午12:04:01  
		 * @author ws
		*/
		private void oilMsgDown(HttpResponse response) {
			try {
				HttpEntity entity = response.getEntity();
	            String entityStr = EntityUtils.toString(entity);
	            JSONObject jobj = JSON.parseObject(entityStr);
	            String retCode = jobj.getString("retCode");
	            
	            String paymode = event.getPayMod();
	    		if("00".equals(retCode)){
		            String key = String.format(Constant.SHOP_PINGAN_TASK_QUEUE, event.getUserid());
		            redis.set(key, JSON.toJSONString(event), 600);
		            if("3".equals(paymode)){
		            	event.setException(new SpiderException(2007,"验证码发送成功"));
		            }else {
		            	event.setException(new SpiderException(2001,"验证码发送成功"));
					}
	    		}else {
	    			String retMsg = jobj.getString("retMsg");
	    			if(StringUtils.isNotBlank(retMsg)){
	    				event.setException(new SpiderException(3003,retMsg));
	    			}else{
	    				event.setException(new SpiderException(3003,"验证码发送失败"));
	    			}
				}
			} catch (Exception e) {
			       logger.error("get loginInitspDown down exception", e);
	               e.printStackTrace();
				   event.setException(new SpiderException(3002,"服务器繁忙"));
			}
		}



		/**
		 * 	
		 * @Description: (方法职责详细描述,可空)  
		 * @Title: verifyDown 
		 * @param response
		 * @date 2016年4月5日 上午11:43:18  
		 * @author ws
		*/
		private void verifyDown(HttpResponse response) {
			logger.info("verifyDown {}", event.getId());
			try {
				String resPage = EntityUtils.toString(response
		                .getEntity());

				JSONObject jsonObj = JSON.parseObject(resPage);
				String resultStr = jsonObj.getString("vresult");
				
				if(StringUtils.isNotBlank(resultStr) && resultStr.equals("success")){

					event.setState(PingAnOilSpiderState.OIL_ORDER);
				}else{
					event.setException(new SpiderException(3004, "下单图形验证码错误"));
				}
			} catch (Exception e) {
				logger.warn("加载登录页面失败", e);
				event.setException(new SpiderException(3002, "系统繁忙"));
			} 
		}



		/**
		 * 	
		 * @Description: (方法职责详细描述,可空)  
		 * @Title: vcodeDown 
		 * @param response
		 * @date 2016年4月5日 上午11:43:15  
		 * @author ws
		*/
		private void vcodeDown(HttpResponse response) {
			logger.info("vcodeDown {}", event.getId());
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
				
                // store event to redis
                String key = String.format(Constant.SHOP_PINGAN_TASK_QUEUE, event.getUserid());
                redis.set(key, JSON.toJSONString(event), 600);
                
                event.setException(new SpiderException(1001, "输入支付图形验证码", imgStr));
                return;
            } catch (Exception e) {
                logger.error("zhaoshang checkDown exception", e);
                event.setException(e);
                return;
            }
		}



		/**
		 * 	
		 * @Description: (方法职责详细描述,可空)  
		 * @Title: oilPageDown 
		 * @param response
		 * @date 2016年4月1日 下午3:25:13  
		 * @author ws
		*/
		private void oilPageDown(HttpResponse response) {
			logger.info("oilPageDown {}", event.getId());
			try {
				
				if(response.getStatusLine().getStatusCode() == 302){
					event.setException(new SpiderException(3006, "请重新登录"));
					return;
				}
				
				String loginPage = EntityUtils.toString(response
		                .getEntity());

				Map<String, String> orderMap = getPageParam(loginPage,"input");
				event.setOrderMap(orderMap);

				event.setState(PingAnOilSpiderState.OIL_VCODE);
			} catch (Exception e) {
				logger.warn("加载登录页面失败", e);
				event.setException(new SpiderException(3002, "系统繁忙"));
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
		 * @throws Exception 
         */
        private Map<String, String> getPageParam(String loginPage,String tagName){
        	logger.info("getLoginParam {}", event.getId());

        	Map<String, String> params = new HashMap<String, String>();
            try {
            	loginPage = XpathHtmlUtils.cleanHtml(loginPage);
            	Document doc = Jsoup.parse(loginPage);
            	Elements elements = doc.getElementsByTag(tagName);
            	for (Element element : elements) {
            		String type = element.attr("type");
                    String name = element.attr("name");
                    if ("hidden".equals(type) && StringUtils.isNotBlank(name)) {
                        params.put(name, element.attr("value"));
                    }
				}
                return params;
            } catch (Exception e) {
				logger.warn("获取登录信息失败", e);
                throw e;
            }
        }


		/**
		 * 	
		 * @Description: (方法职责详细描述,可空)  
		 * @Title: oilOrderDown 
		 * @param response
		 * @date 2016年4月1日 下午3:12:39  
		 * @author ws
		*/
		private void oilOrderDown(HttpResponse response) {
			try {
	            String entityStr = EntityUtils.toString(response.getEntity());
	            Pattern pattern_payUrl = Pattern.compile("vargUrl=\".*?(?=\";)");
	            entityStr = entityStr.replaceAll("\r", "");
	            entityStr = entityStr.replaceAll("\n", "");
	            entityStr = entityStr.replaceAll("\t", "");
	            entityStr = entityStr.replaceAll(" ", "");
	            //System.out.println(entityStr);
	            Matcher gUrlMatcher = pattern_payUrl.matcher(entityStr);
				if (gUrlMatcher.find()) {
					String gUrl = gUrlMatcher.group(0);
					gUrl = gUrl.substring(9);
					gUrl = gUrl.replaceAll("&amp;", "&");
					event.setgUrl(gUrl);
					event.setState(PingAnOilSpiderState.OIL_PAY_PAGE);
					return;
				}else{
					event.setException(new SpiderException(3003, "下单失败"));
					return;
				}
			} catch (Exception e) {
				   logger.warn("get oilOrderDown down exception:()", e);
	               e.printStackTrace();
				   event.setException(new SpiderException(3002,"系统繁忙"));
			}
		}



		/**
		 * 	
		 * @Description: (方法职责详细描述,可空)  
		 * @Title: oilPayPageDown 
		 * @param response
		 * @date 2016年4月1日 下午3:12:37  
		 * @author ws
		*/
		private void oilPayPageDown(HttpResponse response) {
			logger.info("oilPayPageDown {}", event.getId());
			try {
				String loginPage = EntityUtils.toString(response
		                .getEntity());

				Map<String, String> paramMap = getPageParam(loginPage,"input");
				event.setParamMap(paramMap);

				event.setState(PingAnOilSpiderState.OIL_BEFOR_PAY);
			} catch (Exception e) {
				logger.warn("加载支付页面失败:{}", e);
				event.setException(new SpiderException(3002, "系统繁忙"));
			} 
		}


		/**
		 * 	
		 * @Description: (方法职责详细描述,可空)  
		 * @Title: oilBeforPayDown 
		 * @param response
		 * @date 2016年4月1日 下午3:12:34  
		 * @author ws
		*/
		private void oilBeforPayDown(HttpResponse response) {
			logger.info("oilBeforPayDown {}", event.getId());
			try {
				String loginPage = EntityUtils.toString(response
		                .getEntity());

				Map<String, String> paramMap = getPageParam(loginPage,"input");
				event.setParamMap(paramMap);
				
				String payMod = paramMap.get("payMode");
				String orderId = paramMap.get("orderId");
				event.setPayMod(payMod);
				event.setOrderId(orderId);
				if(StringUtils.isBlank(payMod)){
					event.setException(new SpiderException(3003,"下单失败"));
					return;
				}
				
				if(payMod.equals("1") || payMod.equals("3")){//1、短信     3、短信+支付密码
					event.setState(PingAnOilSpiderState.OIL_MSG);
				}else if(payMod.equals("2")){//支付密码
		            String key = String.format(Constant.SHOP_PINGAN_TASK_QUEUE, event.getUserid());
		            redis.set(key, JSON.toJSONString(event), 600);
					event.setException(new SpiderException(2010,"请输入支付密码"));
				}else{
					event.setException(new SpiderException(3003,"下单失败"));
				}
			} catch (Exception e) {
				logger.warn("加载支付页面失败:{}", e);
				event.setException(new SpiderException(3002, "系统繁忙"));
			} 
		}



		/**
		 * 	
		 * @Description: (方法职责详细描述,可空)  
		 * @Title: oilSubmitPayDown 
		 * @param response
		 * @date 2016年4月1日 下午3:12:30  
		 * @author ws
		*/
		private void oilSubmitPayDown(HttpResponse response) {
			logger.info("oilSubmitPayDown {}", event.getId());
			try {
				String loginPage = EntityUtils.toString(response
		                .getEntity());

				JSONObject jsonObj = JSON.parseObject(loginPage);
				String gUrl = jsonObj.getString("gUrl");
				String retCode = jsonObj.getString("retCode");
				String retMsg = jsonObj.getString("retMsg");
				
				if(StringUtils.isNotBlank(retCode) && retCode.equals("10")){
					event.setPayGUrl(gUrl);
					event.setPayRetCode(retCode);
					event.setState(PingAnOilSpiderState.OIL_SUC_PAY);
					return;
				}else{
					if(StringUtils.isNotBlank(retMsg)){
						event.setException(new SpiderException(3003,retMsg));
					}else{
						event.setException(new SpiderException(3003,"支付失败"));
					}
				}
			} catch (Exception e) {
				logger.warn("加载支付页面失败:{}", e);
				event.setException(new SpiderException(3002, "系统繁忙"));
			} 
		}



		/**
		 * 	
		 * @Description: (方法职责详细描述,可空)  
		 * @Title: oilSucPayDown 
		 * @param response
		 * @date 2016年4月1日 下午3:12:27  
		 * @author ws
		*/
		private void oilSucPayDown(HttpResponse response) {
			logger.info("oilSucPayDown {}", event.getId());
			try {
				String pageStr = EntityUtils.toString(response
		                .getEntity());

				if(pageStr.contains("您已成功完成付款")){//支付成功
					event.setException(new SpiderException(0,"购买成功",event.getOrderId()));
				}else{
					event.setException(new SpiderException(3003,"支付失败"));
				}
			} catch (Exception e) {
				logger.warn("支付异常：{}", e);
				event.setException(new SpiderException(3002, "系统繁忙"));
			} 
		}



		private void initspDown(HttpResponse response){
			try {
				HttpEntity entity = response.getEntity();
	            String entityStr = EntityUtils.toString(entity);
	            Document document = Jsoup.parse(entityStr);
	            String retcode = document.select("a").attr("href");
	            event.setLoginUrl(retcode);
				event.setState(PingAnOilSpiderState.LOGIN_INIT_SP);
			} catch (Exception e) {
				   logger.warn("get loginInitspDown down exception：{}", e);
	               e.printStackTrace();
				   event.setException(new SpiderException(3002,"初始化失败"));
			}
		}
		
		private void loginInitspDown(HttpResponse response){
			try {
				HttpEntity entity = response.getEntity();
	            String entityStr = EntityUtils.toString(entity);
	            Document document = Jsoup.parse(entityStr);
	            String gUrl = document.select("#gURL").attr("value");
	            String backURL = document.select("#backURL").attr("value");
	            String appId = document.select("#appId").attr("value");
	            event.setgUrl(gUrl);
	            event.setBackURL(backURL);
	            event.setAppId(appId);
	            
				event.setState(PingAnOilSpiderState.IMG_SP);
			} catch (Exception e) {
			       logger.warn("get loginInitspDown down exception：{}", e);
	               e.printStackTrace();
				   event.setException(new SpiderException(3002,"初始化失败"));
			}
		}
		
		private void imgspDown(HttpResponse response){
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
	                
	                String key = String.format(Constant.SHOP_PINGAN_TASK_QUEUE, event.getUserid());
	                redis.set(key, JSON.toJSONString(event), 600);
	                
	                event.setException(new SpiderException(1001, "输入登录图形验证码", imgStr));
			} catch (Exception e) {
                logger.warn("get img down exception：{}", e);
                e.printStackTrace();
                event.setException(new SpiderException(3002,"系统繁忙"));
			}
		}
		
		
		
		private void loginspDown(HttpResponse response){
			try {
				HttpEntity entity = response.getEntity();
	            String entityStr = EntityUtils.toString(entity);
	            
	            Document document = Jsoup.parse(entityStr);
	            String accountLoginErrorTip = document.select("#accountLoginErrorTip").text();
	            
	            if(accountLoginErrorTip.contains("验证码输入错误")){
	            	  event.setException(new SpiderException(3004, "验证码输入错误"));
	            }else if(accountLoginErrorTip.contains("验证码已过期")){
	            	  event.setException(new SpiderException(3004, "验证码已过期"));
	            }else if(accountLoginErrorTip.contains("用户名和密码不匹配")){
	            	  event.setException(new SpiderException(3005, "用户名和密码不匹配"));
	            }else if(accountLoginErrorTip.contains("请输入您的密码")){
	            	 event.setException(new SpiderException(3005, "请输入您的密码"));
	            }else if(StringUtils.isBlank(accountLoginErrorTip)){
	            	
        			int s = entityStr.indexOf("var gUrl=");
        			int e = entityStr.indexOf("window.location");
        			
        			String script = entityStr.substring(s,e);
	            			
	              	//获得JS脚本引擎
	            	ScriptEngineManager manager = new ScriptEngineManager();
	            	ScriptEngine engine = manager.getEngineByExtension("js");
	            	//设置JS脚本中的userArray、date变量
	            	
	            	engine.eval(script);//执行JS脚本
	            	String intoUrl =  JSONObject.toJSONString(engine.get("gUrl")); 
	            	
	            	event.setIntoUrl(intoUrl.trim().replace("\"", ""));
	                event.setState(PingAnOilSpiderState.TO_AUTH_SP);
	            }else{
	            	  event.setException(new SpiderException(3003, "服务器繁忙"));
	            }
			} catch (Exception e) {
	              logger.error("get img down exception", e);
	              e.printStackTrace();
	              event.setException(new SpiderException(3002,"登录失败"));
			}

		}

		
		private void toauthDown(HttpResponse response){
			try {
				HttpEntity entity = response.getEntity();
	            String entityStr = EntityUtils.toString(entity);
	            Document document = Jsoup.parse(entityStr);
	            String param = document.select("#param").attr("value");
	            
	            event.setParamValue(param);
	            
				event.setState(PingAnOilSpiderState.SSOLOGIN_SP);
			} catch (Exception e) {
			       logger.warn("get loginInitspDown down exception：{}", e);
	               e.printStackTrace();
				   event.setException(new SpiderException(3002,"初始化失败"));
			}
		}
		
		
		private void ssologinDown(HttpResponse response){
			try {
	            String entityStr = EntityUtils.toString(response.getEntity());
	            
	            List<HttpCookieEx> cookie = HttpCookieEx.parse("Set-Cookie:53kf_70722519_{hz6d_keyword}=; path=/;");
	            event.getCookieList().addAll(cookie );
	            
	            cookie =  HttpCookieEx.parse("Set-Cookie:BIGipServerSTATIC_80_PrdPool=204056768.20480.0000; path=/;");
				event.getCookieList().addAll(cookie );
				
				Date date = new Date();
				String guid = date.getTime() + "pyz2eSdhik";

				cookie = HttpCookieEx.parse("Set-Cookie:pa_beacon_wlt_guid="+guid +"; path=/;");
				event.getCookieList().addAll(cookie );

				cookie = HttpCookieEx.parse("Set-Cookie:WLT_HP_VISITER_150722=visited; path=/;");
				event.getCookieList().addAll(cookie );

				cookie = HttpCookieEx.parse("Set-Cookie:wlt.regfrom=0011070H0R030801; path=/;");
				event.getCookieList().addAll(cookie );
				
				String sessionId = PingAnOilUtil.getSessionId();
				cookie = HttpCookieEx.parse("Set-Cookie:pa_beacon_wlt_session="+sessionId+"; path=/;");
				event.getCookieList().addAll(cookie );						//201604050409297427729953975661226

				cookie = HttpCookieEx.parse("Set-Cookie:WT-FPC=id=218.109.149.133-4078694336.30510009:lv="+date.getTime()+":ss="+date.getTime()+":fs="+date.getTime()+":pn=5:vn=2; path=/;");
				event.getCookieList().addAll(cookie );

				cookie = HttpCookieEx.parse("Set-Cookie:CART_COUNT=0; path=/;");
				event.getCookieList().addAll(cookie );
				
				date.setTime(date.getTime() + (30 * 60 * 1000));
				cookie = HttpCookieEx.parse("Set-Cookie:pa_beacon_wlt_expires="+date.getTime()+"; path=/;");
				event.getCookieList().addAll(cookie );
				
				if(entityStr.contains("ChangePasswordInfo.do")){
					event.setException(new SpiderException(3003,"您的密码安全级别较低，请修改密码后重试"));
					return;
				}
				
				event.setException(new SpiderException(0,"登录成功"));
			} catch (Exception e) {
			       logger.warn("get loginInitspDown down exception：{}", e);
	               e.printStackTrace();
				   event.setException(new SpiderException(3002,"初始化失败"));
			}
		}
		
		
		
		
		
		@Override
		public void failed(Exception ex) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void cancelled() {
			// TODO Auto-generated method stub
			
		}
		
		
		
		
		
	}
	
	

}
