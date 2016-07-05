/*
 * Copyright (c) 2015-2020 by caitu99
 * All rights reserved.
 */
package com.caitu99.lsp.spider.pingan;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.Map;

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
import com.caitu99.lsp.model.spider.pingan.PingAnSpiderEvent;
import com.caitu99.lsp.model.spider.pingan.PingAnSpiderState;
import com.caitu99.lsp.spider.HttpAsyncClient;
import com.caitu99.lsp.spider.QuerySpider;
import com.caitu99.lsp.utils.SpringContext;
import com.caitu99.lsp.utils.cookie.CookieHelper;

/** 
 * 
 * @Description: (类职责详细描述,可空) 
 * @ClassName: PingAnSpider 
 * @author fangjunxiao
 * @date 2016年3月30日 上午11:13:23 
 * @Copyright (c) 2015-2020 by caitu99 
 */
public class PingAnSpider implements QuerySpider{

	private static final Logger logger = LoggerFactory
            .getLogger(PingAnSpider.class);

	private RedisOperate redis = SpringContext.getBean(RedisOperate.class);
	
	private CloseableHttpAsyncClient httpAsyncClient = HttpAsyncClient.getInstance();
	
    private AppConfig appConfig = SpringContext.getBean(AppConfig.class);
	
	private static final String userAgent = "Mozilla/5.0 (Windows NT 6.3; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/46.0.2490.86 Safari/537.36";
  
	private static final String initpn = "http://www.wanlitong.com/login.do?gURL=http://www.wanlitong.com/";
    private static final String imgpn = "https://member.wanlitong.com/vcodeImage.do?timestamp=%s";
	private static final String loginpn = "https://member.wanlitong.com/getLogin.do";
    private static final String ssologin = "https://www.wanlitong.com/newssologin";
    
    
    private static final Integer redistime = 600;
    
    private static final String goodinit = "http://jf.wanlitong.com/product_%s_%s";
    
    private static final String getmember = "http://jf.wanlitong.com/member/getMemberInfo.do";
    private static final String buyconfirm = "http://jf.wanlitong.com/buy/confirm.do";
    private static final String commitOrder = "http://jf.wanlitong.com/commitOrder.do";
    private static final String continuepay = "https://payment.wanlitong.com/payment/submitPay.do";
    
    
    
    
    
    
	@Override
	public void onEvent(QueryEvent event) {
		PingAnSpiderEvent pingAnEvent = (PingAnSpiderEvent)event;
		try {
			switch (pingAnEvent.getState()) {
				//初始化并获取图片验证码
			case INIT_SP:
				initspUp(pingAnEvent);
				break;
			case LOGIN_INIT_SP:
				loginInitspUp(pingAnEvent);
				break;
			case IMG_SP:
				imgspUp(pingAnEvent);
				break;
			//登录
			case LOGIN_SP:
				loginspUp(pingAnEvent);
				break;
			case TO_AUTH_SP:
				toauthUp(pingAnEvent);
				break;
			case SSOLOGIN_SP:
				ssologinUp(pingAnEvent);
				break;	
			//商品初始化并登录积分商城
			case GOOD_INIT_SP:
				goodInitUp(pingAnEvent);
				break;
			case GET_MEMBER_INIT_SP:
				getMemberInitUp(pingAnEvent);
				break;	
			case GET_INFO_SP:
				getInfoUp(pingAnEvent);
				break;
			case LOGIN_AUTH_SP:
				loginAuthUp(pingAnEvent);
				break;				
			case LOGIN_SUCCESS_SP:
				loginSuccessUp(pingAnEvent);
				break;	
			case GET_MEMBER_SP:
				getMemberUp(pingAnEvent);
				break;	
			//下单
			case BUY_CONFIRM_SP:
				buyConfirmUp(pingAnEvent);
				break;
			case COMMIT_ORDER_SP:
				commitOrderUp(pingAnEvent);
				break;	
			case PAYMENT_SP:
				paymentUp(pingAnEvent);
				break;	
			case PAY_YM_SP:
				payYmUp(pingAnEvent);
				break;	
			//购买
			case CONTINUE_PAY_SP:
				continuePayUp(pingAnEvent);
				break;				
				
				
				
			//首次购买同意条款
			case UM_LOGIN_SP:
				umLoginUp(pingAnEvent);
				break;	
			case FIRST_SP:
				firstUp(pingAnEvent);
				break;		
			case FIRST_DIR_SP:
				firstDirUp(pingAnEvent);
				break;
			case TO_PAY_MENT_SP:
				topaymentUp(pingAnEvent);
				break;
				
				
				
			
			case SMS_CODE_SP:
				smsCodeUp(pingAnEvent);
				break;		
			
			case ADD_ADDRESS:
				addAddressUp(pingAnEvent);
				break;
			case GET_ADDRESS:
				getAddressUp(pingAnEvent);
				break;
				
				
				
				
				
				
				
				//修改密码
				
				
				
			case VCODEWEB:
				vcodewebUp(pingAnEvent);
				break;
				
			case VALIDATE_VCODE:
				validateVcodeUp(pingAnEvent);
				break;
			case TORESETPWD:
				toresetpwdUp(pingAnEvent);
				break;
			case TOCOMMITMEMBER:
				tocommitmemberUp(pingAnEvent);
				break;	
			case REGVCODE:
				regvcodeUp(pingAnEvent);
				break;
				
				
			case SEND_CODE:
				sendCodeUp(pingAnEvent);
				break;
				
				
				
			case CHECK_CODE:
				checkCodeUp(pingAnEvent);
				break;
				
			case RESET_THREE:
				resetThreeUp(pingAnEvent);
				break;		
			case CHECK_PASSWORD:
				checkPassUp(pingAnEvent);
				break;	
			case UPDATE_PWD:
				updatePwdUp(pingAnEvent);
				break;	
				
				
			case ERROR:
                errorHandle(event);
                break;
			}
		} catch (Exception e) {
			logger.error("request up error {}", event.getId(), e);
			pingAnEvent.setException(e);
            errorHandle(pingAnEvent);
		}
	}
	
	
	
	private void initspUp(PingAnSpiderEvent pingAnEvent){
		logger.debug("initspUp {}", pingAnEvent.getAccount());
		String url = initpn;
        HttpGet httpGet = new HttpGet(url);
        setHeader(url, httpGet, pingAnEvent);
        httpAsyncClient.execute(httpGet, new HttpAsyncCallback(pingAnEvent));
	}
	
	private void loginInitspUp(PingAnSpiderEvent pingAnEvent){
		logger.debug("loginInitspUp {}", pingAnEvent.getAccount());
		String url = pingAnEvent.getLoginUrl();
        HttpGet httpGet = new HttpGet(url);
        setHeader(url, httpGet, pingAnEvent);
        httpAsyncClient.execute(httpGet, new HttpAsyncCallback(pingAnEvent));
	}
	
	private void imgspUp(PingAnSpiderEvent pingAnEvent){
		logger.debug("loginInitspUp {}", pingAnEvent.getAccount());
		Date date = new Date();
		String url = String.format(imgpn, date.getTime());
        HttpGet httpGet = new HttpGet(url);
        setHeader(url, httpGet, pingAnEvent);
        httpAsyncClient.execute(httpGet, new HttpAsyncCallback(pingAnEvent));
		
	}
	
	private void loginspUp(PingAnSpiderEvent pingAnEvent){
		logger.debug("loginspUp {}", pingAnEvent.getAccount());
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
			   logger.error("check account error {}", pingAnEvent.getAccount(), e);
			   pingAnEvent.setException(e);
	            return;
		}
        
        setHeader(url, httpPost, pingAnEvent);
        httpAsyncClient.execute(httpPost, new HttpAsyncCallback(pingAnEvent));	
		
	}
	
	private void toauthUp(PingAnSpiderEvent pingAnEvent){
		logger.debug("toauthUp {}", pingAnEvent.getAccount());
		String url = pingAnEvent.getIntoUrl();
        HttpGet httpGet = new HttpGet(url);
        setHeader(url, httpGet, pingAnEvent);
        httpAsyncClient.execute(httpGet, new HttpAsyncCallback(pingAnEvent));
	}
	
	
	private void goodInitUp(PingAnSpiderEvent pingAnEvent){
		logger.debug("goodInitUp {}", pingAnEvent.getAccount());
		String url = String.format(goodinit, pingAnEvent.getGoodsId(),pingAnEvent.getRepositoryId());
        HttpGet httpGet = new HttpGet(url);
        setHeader(url, httpGet, pingAnEvent);
        httpAsyncClient.execute(httpGet, new HttpAsyncCallback(pingAnEvent));
	}
	
	
	private void getMemberInitUp(PingAnSpiderEvent pingAnEvent){
		logger.debug("goodInitUp {}", pingAnEvent.getAccount());
		String url = getmember;
        HttpGet httpGet = new HttpGet(url);
        setHeader(url, httpGet, pingAnEvent);
        httpAsyncClient.execute(httpGet, new HttpAsyncCallback(pingAnEvent));
	}
	
	
	
	private void getInfoUp(PingAnSpiderEvent pingAnEvent){
		logger.debug("getInfoUp {}", pingAnEvent.getAccount());
		String url = "http://jf.wanlitong.com/login.do?backUrl=/member/getMemberInfo.do";
        HttpGet httpGet = new HttpGet(url);
        setHeader(url, httpGet, pingAnEvent);
        httpAsyncClient.execute(httpGet, new HttpAsyncCallback(pingAnEvent));
	}
	
	private void loginAuthUp(PingAnSpiderEvent pingAnEvent){
		logger.debug("loginAuthUp {}", pingAnEvent.getAccount());
		
		String url = "http://www.wanlitong.com/um/loginAuth.do";
        HttpPost httpPost = new HttpPost(url);
        String param = pingAnEvent.getParamValue();
        
        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("param", param));
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
	
	
	
	private void loginSuccessUp(PingAnSpiderEvent pingAnEvent){
		logger.debug("loginSuccessUp {}", pingAnEvent.getAccount());
		
		String url = "http://jf.wanlitong.com/loginSuccess.do?backUrl=/member/getMemberInfo.do";
        HttpPost httpPost = new HttpPost(url);
        String param = pingAnEvent.getParamValue();
        
        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("param", param));
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
	
	private void getMemberUp(PingAnSpiderEvent pingAnEvent){
		logger.debug("getMemberUp {}", pingAnEvent.getAccount());
		
		String url = "http://jf.wanlitong.com/member/getMemberInfo.do";
        HttpGet httpGet = new HttpGet(url);
        setHeader(url, httpGet, pingAnEvent);
        httpAsyncClient.execute(httpGet, new HttpAsyncCallback(pingAnEvent));
	}
	
	
	private void buyConfirmUp(PingAnSpiderEvent pingAnEvent){
		logger.debug("loginSuccessUp {}", pingAnEvent.getAccount());
		
		String url = buyconfirm;
        HttpPost httpPost = new HttpPost(url);
        String userAuth = pingAnEvent.getUserAuth();
        String orderType = pingAnEvent.getOrderType();
        String cartJsonStr = pingAnEvent.getCartJsonStr();
        
        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("userAuth", userAuth));
        params.add(new BasicNameValuePair("orderType", orderType));
        params.add(new BasicNameValuePair("cartJsonStr", cartJsonStr));
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
	
	private void commitOrderUp(PingAnSpiderEvent pingAnEvent){
		logger.debug("commitOrderUp {}", pingAnEvent.getAccount());
		
		String url = commitOrder;
        HttpPost httpPost = new HttpPost(url);
        String cartJsonStr = pingAnEvent.getCartJsonStr();
        String cashAmt = pingAnEvent.getCashAmt();
        String defaultMobile = pingAnEvent.getDefaultMobile();
        String orderSubmitFormToken = pingAnEvent.getOrderSubmitFormToken();
        String orderType = pingAnEvent.getOrderType();
        String salesType = pingAnEvent.getSalesType();
        String userAuth = pingAnEvent.getUserAuth();
        String pointAmt = pingAnEvent.getPointAmt();
        String couponAmt = pingAnEvent.getCouponAmt();
        String newMobile = pingAnEvent.getCellphone();
        String province = pingAnEvent.getProvince();
        String city = pingAnEvent.getCity();
        String district = pingAnEvent.getDistrict();
        String street = pingAnEvent.getAddress();
        String fullName = pingAnEvent.getName();
        
        String deliveryId = String.valueOf(pingAnEvent.getDeliveryId());
        
        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("cartJsonStr", cartJsonStr));
        params.add(new BasicNameValuePair("cashAmt", cashAmt));
        params.add(new BasicNameValuePair("orderSubmitFormToken", orderSubmitFormToken));
        params.add(new BasicNameValuePair("orderType", orderType));
        params.add(new BasicNameValuePair("salesType", salesType));
        params.add(new BasicNameValuePair("userAuth", userAuth));
        params.add(new BasicNameValuePair("pointAmt", pointAmt));
        params.add(new BasicNameValuePair("couponAmt", couponAmt));
        params.add(new BasicNameValuePair("couponNo", ""));
        params.add(new BasicNameValuePair("couponType", ""));
        params.add(new BasicNameValuePair("relatedId", ""));
        params.add(new BasicNameValuePair("useCouponItemId", ""));
        
        
        if("001001".equals(orderType)){
        	params.add(new BasicNameValuePair("address", "on"));
            params.add(new BasicNameValuePair("postcode", ""));
            params.add(new BasicNameValuePair("province", province));
            params.add(new BasicNameValuePair("street", street));
            params.add(new BasicNameValuePair("city", city));
            params.add(new BasicNameValuePair("deliveryComment", ""));
            params.add(new BasicNameValuePair("deliveryId", deliveryId));
            params.add(new BasicNameValuePair("delivery-time", "01"));
            params.add(new BasicNameValuePair("deliveryTime", "01"));
            params.add(new BasicNameValuePair("district", district));
            params.add(new BasicNameValuePair("fullName", fullName));
            params.add(new BasicNameValuePair("mobile", newMobile));
            params.add(new BasicNameValuePair("phone", "--"));
        }else{
        	//002002
            params.add(new BasicNameValuePair("defaultMobile", defaultMobile));
            params.add(new BasicNameValuePair("mobile", "on"));
            params.add(new BasicNameValuePair("newMobile", newMobile));
        }
        
        
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
	
	private void paymentUp(PingAnSpiderEvent pingAnEvent){
		logger.debug("paymentUp {}", pingAnEvent.getAccount());
		
		String url = pingAnEvent.getTopayurl();
        HttpGet httpGet = new HttpGet(url);
        setHeader(url, httpGet, pingAnEvent);
        httpAsyncClient.execute(httpGet, new HttpAsyncCallback(pingAnEvent));
	}
	
	
	private void umLoginUp(PingAnSpiderEvent pingAnEvent){
		logger.debug("umLoginUp {}", pingAnEvent.getAccount());
		
		String url = pingAnEvent.getIntoUrl();
        HttpGet httpGet = new HttpGet(url);
        setHeader(url, httpGet, pingAnEvent);
        httpAsyncClient.execute(httpGet, new HttpAsyncCallback(pingAnEvent));
	}
	
	
	
	private void firstUp(PingAnSpiderEvent pingAnEvent){
		logger.debug("firstUp {}", pingAnEvent.getAccount());
		
		String url = "https://www.wanlitong.com/wangguan/wangguanRedirect.do";
        HttpPost httpPost = new HttpPost(url);
        String param = pingAnEvent.getParamValue();
        
        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("param", param));
        
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
	
	
	
	private void firstDirUp(PingAnSpiderEvent pingAnEvent){
		logger.debug("firstDirUp {}", pingAnEvent.getAccount());
		
		String url = "https://www.wanlitong.com/wangguan/wangguanGoRedirect.do";
        HttpPost httpPost = new HttpPost(url);
        String param = pingAnEvent.getParamValue();
        
        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("checkProtocol", "true"));
        params.add(new BasicNameValuePair("param", param));
        
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
	
	
	
	private void topaymentUp(PingAnSpiderEvent pingAnEvent){
		logger.debug("topaymentUp {}", pingAnEvent.getAccount());
		
		String url = pingAnEvent.getIntoUrl();
        HttpGet httpGet = new HttpGet(url);
        setHeader(url, httpGet, pingAnEvent);
        httpAsyncClient.execute(httpGet, new HttpAsyncCallback(pingAnEvent));
	}
	
	
	
	private void payYmUp(PingAnSpiderEvent pingAnEvent){
		logger.debug("payYmUp {}", pingAnEvent.getAccount());
		
		String url = "https://payment.wanlitong.com/payment/continuePay.do";
        HttpPost httpPost = new HttpPost(url);
        String orderId = pingAnEvent.getOrderId();
        String orderDate = pingAnEvent.getOrderDate();
        String merId = pingAnEvent.getMerId();
        String reqId = pingAnEvent.getReqId();
        String totalOrderAmt = pingAnEvent.getTotalOrderAmt();
        String orderAmt = pingAnEvent.getOrderAmt();
        String id = pingAnEvent.getIid();
        String points = pingAnEvent.getPoints();
        String cash = pingAnEvent.getCash();
        String userPoints = pingAnEvent.getUserPoints();
        
        
        if(StringUtils.isBlank(cash)){
        	cash = "0";
        }
        
        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("orderId", orderId));
        params.add(new BasicNameValuePair("orderDate", orderDate));
        params.add(new BasicNameValuePair("merId", merId));
        params.add(new BasicNameValuePair("reqId", reqId));
        params.add(new BasicNameValuePair("totalOrderAmt", totalOrderAmt));
        params.add(new BasicNameValuePair("orderAmt", orderAmt));
        params.add(new BasicNameValuePair("id", id));
        params.add(new BasicNameValuePair("points", points));
        params.add(new BasicNameValuePair("cash", cash));
        

        params.add(new BasicNameValuePair("otherGateIndexArray", "4"));
        params.add(new BasicNameValuePair("otherGateId_4", "000100"));
        params.add(new BasicNameValuePair("otherGateId_3", "000901"));
        params.add(new BasicNameValuePair("otherGateId_2", "000902"));
        params.add(new BasicNameValuePair("gateType_4", "1"));
        params.add(new BasicNameValuePair("gateType_2", "9"));
        params.add(new BasicNameValuePair("gateType_3", "9"));
        params.add(new BasicNameValuePair("userPoints", userPoints));
        params.add(new BasicNameValuePair("displayUrl_3", "image/cqhk.png"));
        params.add(new BasicNameValuePair("displayUrl_2", "image/mgw.png"));
        params.add(new BasicNameValuePair("displayUrl_4", "image/wlt-logo.png"));
        params.add(new BasicNameValuePair("displayName_3", "春秋绿翼"));
        params.add(new BasicNameValuePair("displayName_2", "芒果网"));
        params.add(new BasicNameValuePair("displayName_4", "万里通积分"));

        params.add(new BasicNameValuePair("yqbUseCash", ""));
        params.add(new BasicNameValuePair("yqbPayToken", ""));
        params.add(new BasicNameValuePair("yqbId", ""));
        params.add(new BasicNameValuePair("yqbBalance", ""));
        params.add(new BasicNameValuePair("gateId", ""));
        params.add(new BasicNameValuePair("GiftCode", ""));
        params.add(new BasicNameValuePair("GiftCodeDis", ""));
        params.add(new BasicNameValuePair("GiftPoint", ""));
        
        params.add(new BasicNameValuePair("platFormGateId", ""));
        params.add(new BasicNameValuePair("parentGateId", ""));
        params.add(new BasicNameValuePair("otherGateUsePoints_3", ""));
        params.add(new BasicNameValuePair("otherGateUsePoints_2", ""));
        params.add(new BasicNameValuePair("otherGatePayAmt_3", ""));
        params.add(new BasicNameValuePair("otherGatePayAmt_2", ""));
        params.add(new BasicNameValuePair("GiftType", ""));
        
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
	
	
	private void continuePayUp(PingAnSpiderEvent pingAnEvent){
		logger.debug("continuePayUp {}", pingAnEvent.getAccount());
		
		String url = continuepay;
        HttpPost httpPost = new HttpPost(url);
        String orderId = pingAnEvent.getOrderId();
        String orderDate = pingAnEvent.getOrderDate();
        String merId = pingAnEvent.getMerId();
        String reqId = pingAnEvent.getReqId();
        String totalOrderAmt = pingAnEvent.getTotalOrderAmt();
        String orderAmt = pingAnEvent.getOrderAmt();
        String id = pingAnEvent.getIid();
        String points = pingAnEvent.getPoints();
        String cash = pingAnEvent.getCash();
        String code = pingAnEvent.getSmsCode();
        
        if(StringUtils.isBlank(cash)){
        	cash = "0";
        }
        
        String payPassWord = pingAnEvent.getPayPassWord();
        String payMode = pingAnEvent.getPayMode();
        
        String otherGatePayAmt_4 = pingAnEvent.getSalePrice();
        
        
        List<NameValuePair> params = new ArrayList<>();
        if("1".equals(payMode)){
        	 params.add(new BasicNameValuePair("code", code));
        }else if("2".equals(payMode)){
        	 params.add(new BasicNameValuePair("payPassWord", payPassWord));
        }else if("3".equals(payMode)){
        	params.add(new BasicNameValuePair("code", code));
        	params.add(new BasicNameValuePair("payPassWord", payPassWord));
        }
        
        params.add(new BasicNameValuePair("orderId", orderId));
        params.add(new BasicNameValuePair("orderDate", orderDate));
        params.add(new BasicNameValuePair("merId", merId));
        params.add(new BasicNameValuePair("reqId", reqId));
        params.add(new BasicNameValuePair("totalOrderAmt", totalOrderAmt));
        params.add(new BasicNameValuePair("orderAmt", orderAmt));
        params.add(new BasicNameValuePair("id", id));
        params.add(new BasicNameValuePair("points", points));
        params.add(new BasicNameValuePair("cash", cash));
        
        params.add(new BasicNameValuePair("payMode", payMode));
        

        params.add(new BasicNameValuePair("otherGateIndexArray", "4"));
        params.add(new BasicNameValuePair("otherGateId_4", "000100"));
        params.add(new BasicNameValuePair("gateType_4", "1"));
        params.add(new BasicNameValuePair("otherGateUsePoints_4", points));
        params.add(new BasicNameValuePair("otherGatePayAmt_4", otherGatePayAmt_4));
        
        
        params.add(new BasicNameValuePair("gateId", ""));
        params.add(new BasicNameValuePair("GiftNo", ""));
        params.add(new BasicNameValuePair("GiftNoDis", ""));
        params.add(new BasicNameValuePair("coupon", ""));
        params.add(new BasicNameValuePair("GiftCardType", ""));
        params.add(new BasicNameValuePair("yqbId", ""));
        params.add(new BasicNameValuePair("yqbCash", ""));
        params.add(new BasicNameValuePair("yqbPayToken", ""));
        
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
	
	private void setHeader(String uriStr, HttpMessage httpMessage, QueryEvent event) {
        httpMessage.setHeader("Accept", "*/*");
        httpMessage.setHeader("User-Agent", userAgent);
        try {
            CookieHelper.setCookies2(uriStr, httpMessage, event.getCookieList());
        } catch (URISyntaxException e) {
            logger.error("set cookie fail {}", event.getId());
        }
    }

	
	
	private void smsCodeUp(PingAnSpiderEvent pingAnEvent){
		logger.debug("smsCodeUp {}", pingAnEvent.getAccount());
		
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
	
	
	private void getAddressUp(PingAnSpiderEvent pingAnEvent){
		logger.debug("getAddressUp {}", pingAnEvent.getAccount());

		Date date = new Date();

		String ads = "http://jf.wanlitong.com/getDeliverInfo.do";
		String jq = "jQuery1112020096355117857456_1459819292088";
		StringBuilder sb = new StringBuilder();
		sb.append(ads)
		.append("?callback=").append(jq)
		.append("&_=").append(date.getTime());
		
		String url = sb.toString();
		System.out.println(url);
        HttpGet httpGet = new HttpGet(url);
        setHeader(url, httpGet, pingAnEvent);
        httpAsyncClient.execute(httpGet, new HttpAsyncCallback(pingAnEvent));
	}
	
	private void addAddressUp(PingAnSpiderEvent pingAnEvent){
		logger.debug("addAddressUp {}", pingAnEvent.getAccount());

		Date date = new Date();
		String ads = "http://jf.wanlitong.com/addDeliverInfo.do";
		String jq = "jQuery1112020096355117857456_1459819292088";
		String province;
		String city;
		String district;
		String address;
		String name;
		try {
			province = this.seEncode(pingAnEvent.getProvince());
			city = this.seEncode(pingAnEvent.getCity());
			district = this.seEncode(pingAnEvent.getDistrict());
			address = this.seEncode(pingAnEvent.getAddress());
			name = this.seEncode(pingAnEvent.getName());
		} catch (UnsupportedEncodingException e) {
			   logger.error("check account error {}", pingAnEvent.getAccount(), e);
			   pingAnEvent.setException(e);
	           return;
		}
		StringBuilder sb = new StringBuilder();
		sb.append(ads)
		.append("?callback=").append(jq)
		.append("&province=").append(province)
		.append("&city=").append(city)
		.append("&district=").append(district)
		.append("&address=").append(address)
		.append("&zipCode=").append("")
		.append("&name=").append(name)
		.append("&phone=").append("--")
		.append("&cellphone=").append(pingAnEvent.getCellphone())
		.append("&defaultFlag=true")
		.append("&_=").append(date.getTime());
		
		String url = sb.toString();
		System.out.println(url);
        HttpGet httpGet = new HttpGet(url);
        setHeader(url, httpGet, pingAnEvent);
        httpAsyncClient.execute(httpGet, new HttpAsyncCallback(pingAnEvent));
	}
	
	
	private String seEncode(String code) throws UnsupportedEncodingException{
			code = URLEncoder.encode(code, "utf-8");
			code = URLEncoder.encode(code, "utf-8");
		return code;
	}
	
	private void ssologinUp(PingAnSpiderEvent pingAnEvent){
		logger.debug("loginspUp {}", pingAnEvent.getAccount());
		String url = ssologin;
        HttpPost httpPost = new HttpPost(url);
        String param = pingAnEvent.getParamValue();
        
        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("param", param));
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
	
	private void vcodewebUp(PingAnSpiderEvent pingAnEvent){
		logger.debug("vcodewebUp {}", pingAnEvent.getAccount());
		Date date = new Date();
		String weburl = "http://www.wanlitong.com/paic/common/vcodeweb.do?timestamp=%s";
		String timestamp = String.valueOf(date.getTime());
		pingAnEvent.setTimestamp(timestamp);
		String url = String.format(weburl,timestamp );
        HttpGet httpGet = new HttpGet(url);
        setHeader(url, httpGet, pingAnEvent);
        httpAsyncClient.execute(httpGet, new HttpAsyncCallback(pingAnEvent));
		
	}
	
	private void validateVcodeUp(PingAnSpiderEvent pingAnEvent){
		logger.debug("validateVcodeUp {}", pingAnEvent.getAccount());
		String url = "http://www.wanlitong.com/ajaxValidateVcode.do";
        HttpPost httpPost = new HttpPost(url);
        
        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("code", pingAnEvent.getvCode()));
        params.add(new BasicNameValuePair("timestamp", pingAnEvent.getTimestamp()));
        
        
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
	

	private void toresetpwdUp(PingAnSpiderEvent pingAnEvent){
		logger.debug("toresetpwdUp {}", pingAnEvent.getAccount());
		String url = "http://www.wanlitong.com/resetPwd/toresetpwd2.do";
        HttpPost httpPost = new HttpPost(url);
        
        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("verifyCode", pingAnEvent.getvCode()));
        params.add(new BasicNameValuePair("username", pingAnEvent.getAccount()));
        params.add(new BasicNameValuePair("timestamp", pingAnEvent.getTimestamp()));
        
        
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
	
	private void tocommitmemberUp(PingAnSpiderEvent pingAnEvent){
		logger.debug("tocommitmemberUp {}", pingAnEvent.getAccount());
		String url = "http://www.wanlitong.com/resetPwd/tocommitmember.do";
        
        HttpPost httpPost = new HttpPost(url);
        
        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("appid", "P59000"));
        params.add(new BasicNameValuePair("backURL", "http://jf.wanlitong.com/loginSuccess.do?backUrl=http://jf.wanlitong.com/product_3170_2"));
        params.add(new BasicNameValuePair("appName", "新积分商城"));
        params.add(new BasicNameValuePair("appDesc", "新积分商城"));
        params.add(new BasicNameValuePair("appLink", "http://jf.wanlitong.com"));
        params.add(new BasicNameValuePair("appLogo", "新积分商城.png"));
        params.add(new BasicNameValuePair("user-name", pingAnEvent.getAccount()));
        params.add(new BasicNameValuePair("verifyCode", pingAnEvent.getvCode()));
        params.add(new BasicNameValuePair("timestamp", pingAnEvent.getTimestamp()));
        
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
	
	
	
	private void regvcodeUp(PingAnSpiderEvent pingAnEvent){
		logger.debug("regvcodeUp {}", pingAnEvent.getAccount());
		Date date = new Date();
		String weburl = "http://www.wanlitong.com/paic/common/regVcode.do?timestamp=%s";
		String timestamp = String.valueOf(date.getTime());
		pingAnEvent.setTimestamp(timestamp);
		String url = String.format(weburl,timestamp );
        HttpGet httpGet = new HttpGet(url);
        setHeader(url, httpGet, pingAnEvent);
        httpAsyncClient.execute(httpGet, new HttpAsyncCallback(pingAnEvent));
		
	}
	
	
	
	private void sendCodeUp(PingAnSpiderEvent pingAnEvent){
		logger.debug("sendCodeUp {}", pingAnEvent.getAccount());
		Date date = new Date();
		String weburl = "http://www.wanlitong.com/otp/sendOtpDynamicCode.do?t=%s";
		String timetem = String.valueOf(date.getTime());
		String url = String.format(weburl,timetem);
        HttpPost httpPost = new HttpPost(url);
        
        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("mobile", pingAnEvent.getMaskMobile()));
        params.add(new BasicNameValuePair("vCodeValue", pingAnEvent.getvCode()));
        params.add(new BasicNameValuePair("vCodeKey", pingAnEvent.getTimestamp()));
        params.add(new BasicNameValuePair("sendType", "01"));
        params.add(new BasicNameValuePair("loginName", pingAnEvent.getUsername()));
        params.add(new BasicNameValuePair("functionContext", "ValidatewltWithMobile"));
        
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
	
	
	private void checkCodeUp(PingAnSpiderEvent pingAnEvent){
		logger.debug("checkCodeUp {}", pingAnEvent.getAccount());
		Date date = new Date();
		String weburl = "http://www.wanlitong.com/resetPwd/checkcode.do?t=%s";
		String timetem = String.valueOf(date.getTime());
		String url = String.format(weburl,timetem);
        HttpPost httpPost = new HttpPost(url);
        
        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("phone", pingAnEvent.getMaskMobile()));
        params.add(new BasicNameValuePair("code", pingAnEvent.getSmsCode()));
        params.add(new BasicNameValuePair("name", pingAnEvent.getUsername()));
        
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
	
	
	private void resetThreeUp(PingAnSpiderEvent pingAnEvent){
		logger.debug("resetThreeUp {}", pingAnEvent.getAccount());
		String url = "http://www.wanlitong.com/resetPwd/toResetThree.do";
        HttpPost httpPost = new HttpPost(url);
        Date date = new Date();
        
        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("timestamp", String.valueOf(date.getTime())));
        params.add(new BasicNameValuePair("appid", "P59000"));
        params.add(new BasicNameValuePair("backURL", "http://jf.wanlitong.com/loginSuccess.do?backUrl=http://jf.wanlitong.com/product_3170_2"));
        params.add(new BasicNameValuePair("appName", "新积分商城"));
        params.add(new BasicNameValuePair("appDesc", "新积分商城"));
        params.add(new BasicNameValuePair("appLink", "http://jf.wanlitong.com"));
        params.add(new BasicNameValuePair("appLogo", "新积分商城.png"));
        params.add(new BasicNameValuePair("maskMobile", pingAnEvent.getMaskMobile()));
        params.add(new BasicNameValuePair("username", pingAnEvent.getUsername()));
        params.add(new BasicNameValuePair("validCode", pingAnEvent.getvCode()));
        params.add(new BasicNameValuePair("r-phone", pingAnEvent.getMaskMobile()));
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
	
	private void checkPassUp(PingAnSpiderEvent pingAnEvent){
		logger.debug("checkPassUp {}", pingAnEvent.getAccount());
		String url = "http://www.wanlitong.com/member/checkPassword.do";
        HttpPost httpPost = new HttpPost(url);
        
        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("password", pingAnEvent.getPassword()));
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
	
	
	private void updatePwdUp(PingAnSpiderEvent pingAnEvent){
		logger.debug("updatePwdUp {}", pingAnEvent.getAccount());
		String url = "http://www.wanlitong.com/resetPwd/updatepwd.do";
        HttpPost httpPost = new HttpPost(url);
        
        List<NameValuePair> params = new ArrayList<>();
        
        params.add(new BasicNameValuePair("memberId", pingAnEvent.getMemberId()));
        params.add(new BasicNameValuePair("password", pingAnEvent.getPassword()));
        params.add(new BasicNameValuePair("repassword", pingAnEvent.getPassword()));
        params.add(new BasicNameValuePair("backURL", "http://jf.wanlitong.com/loginSuccess.do"));
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

		private PingAnSpiderEvent event;
        private boolean skipNextStep = false;

		public HttpAsyncCallback(PingAnSpiderEvent event) {
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
					
				case GOOD_INIT_SP:
					goodInitDown(response);
					break;
				case GET_MEMBER_INIT_SP:
					getMemberInitDown(response);
					break;	
					
				case GET_INFO_SP:
					getInfoDown(response);
					break;
				case LOGIN_AUTH_SP:
					loginAuthDown(response);
					break;	
				case LOGIN_SUCCESS_SP:
					loginSuccessDown(response);
					break;	
					
					
				case GET_MEMBER_SP:
					getMemberDown(response);
					break;	
				case BUY_CONFIRM_SP:
					buyConfirmDown(response);
					break;
				case COMMIT_ORDER_SP:
					commitOrderDown(response);
					break;
				case PAYMENT_SP:
					paymentDown(response);
					break;	
				case PAY_YM_SP:
					payYmDown(response);
					break;
				case CONTINUE_PAY_SP:
					continuePayDown(response);
					break;				
					
					
				case UM_LOGIN_SP:
					umLoginDown(response);
					break;	
				case FIRST_SP:
					firstDown(response);
					break;		
				case FIRST_DIR_SP:
					firstDirDown(response);
					break;	
				case TO_PAY_MENT_SP:
					topaymentDown(response);
					break;
					
					
				
				case SMS_CODE_SP:
					smsCodeDown(response);
					break;	
					
				case GET_ADDRESS:
					getAddressDown(response);
					break;
				case ADD_ADDRESS:
					addAddressDown(response);
					break;		
					
					
					
				case VCODEWEB:
					vcodewebDown(response);
					break;
					
					
				case VALIDATE_VCODE:
					validateVcodeDown(response);
					break;
				case TORESETPWD:
					toresetpwdDown(response);
					break;
				case TOCOMMITMEMBER:
					tocommitmemberDown(response);
					break;	
				case REGVCODE:
					regvcodeDown(response);
					break;
					
					
				case SEND_CODE:
					sendCodeDown(response);
					break;
					
					
				case CHECK_CODE:
					checkCodeDown(response);
					break;
				case RESET_THREE:
					resetThreeDown(response);
					break;		
				case CHECK_PASSWORD:
					checkPassDown(response);
					break;	
				case UPDATE_PWD:
					updatePwdDown(response);
					break;	
					
				default:
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
		
		
		private void initspDown(HttpResponse response){
			try {
			HttpEntity entity = response.getEntity();
            String entityStr = EntityUtils.toString(entity);
            Document document = Jsoup.parse(entityStr);
            String retcode = document.select("a").attr("href");
            event.setLoginUrl(retcode);
			event.setState(PingAnSpiderState.LOGIN_INIT_SP);
			} catch (Exception e) {
				   logger.error("get loginInitspDown down exception", e);
	               e.printStackTrace();
				   event.setException(new SpiderException(-1,"初始化失败"));
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
	            
				event.setState(PingAnSpiderState.IMG_SP);
			} catch (Exception e) {
			       logger.error("get loginInitspDown down exception", e);
	               e.printStackTrace();
				   event.setException(new SpiderException(-1,"初始化失败"));
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
	                if (appConfig.inDevMode()) {
	                    byte[] tbytes = Base64.getDecoder().decode(imgStr);
	                    FileOutputStream fs = new FileOutputStream(
	                            appConfig.getUploadPath() + "/" + event.getUserid()
	                                    + ".jpg");
	                    fs.write(tbytes);
	                    fs.close();
	                }
	                
	                String key = String.format(Constant.SHOP_PINGAN_TASK_QUEUE, event.getUserid());
	                redis.set(key, JSON.toJSONString(event), 600);
	                
	                event.setException(new SpiderException(1001, "输入验证码", imgStr));
			} catch (Exception e) {
                logger.error("get img down exception", e);
                e.printStackTrace();
                event.setException(new SpiderException(-1,"获取验证码失败"));
			}
		}
		
		
		
		private void loginspDown(HttpResponse response){
			try {
				HttpEntity entity = response.getEntity();
	            String entityStr = EntityUtils.toString(entity);
	            
	            Document document = Jsoup.parse(entityStr);
	            String accountLoginErrorTip = document.select("#accountLoginErrorTip").text();
	            
	            if(accountLoginErrorTip.contains("验证码输入错误")){
	            	  event.setException(new SpiderException(1010, "验证码输入错误"));
	            }else if(accountLoginErrorTip.contains("验证码已过期")){
	            	  event.setException(new SpiderException(1011, "验证码已过期"));
	            }else if(accountLoginErrorTip.contains("用户名和密码不匹配")){
	            	  event.setException(new SpiderException(1012, "用户名和密码不匹配"));
	            }else if(accountLoginErrorTip.contains("请输入您的密码")){
	            	 event.setException(new SpiderException(1013, "请输入您的密码"));
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
	                event.setState(PingAnSpiderState.TO_AUTH_SP);
	            }else{
	            	  event.setException(new SpiderException(1006, "服务器繁忙"));
	            }
			} catch (Exception e) {
	              logger.error("get img down exception", e);
	              e.printStackTrace();
	              event.setException(new SpiderException(-1,"登录失败"));
			}

		}

		
		private void toauthDown(HttpResponse response){
			try {
				HttpEntity entity = response.getEntity();
	            String entityStr = EntityUtils.toString(entity);
	            Document document = Jsoup.parse(entityStr);
	            String param = document.select("#param").attr("value");
	            event.setParamValue(param);
				event.setState(PingAnSpiderState.SSOLOGIN_SP);
			} catch (Exception e) {
			       logger.error("get loginInitspDown down exception", e);
	               e.printStackTrace();
				   event.setException(new SpiderException(-1,"登录失败"));
			}
		}
		
		
		private void ssologinDown(HttpResponse response){
			try {
	            String key = String.format(Constant.SHOP_PINGAN_LOGIN_KEY, event.getUserid());
	            redis.set(key, JSON.toJSONString(event), redistime);
	            
	            String entityStr = EntityUtils.toString(response.getEntity());
	            
				if(entityStr.contains("ChangePasswordInfo.do")){
					event.setException(new SpiderException(3003,"您的密码安全级别较低，请修改密码后重试"));
					return;
				}
				
	            
				event.setException(new SpiderException(0,"登录成功"));
			} catch (Exception e) {
			       logger.error("get loginInitspDown down exception", e);
	               e.printStackTrace();
				   event.setException(new SpiderException(-1,"登录失败"));
			}
		}
		
		
		private void goodInitDown(HttpResponse response){
			try {
				HttpEntity entity = response.getEntity();
	            String entityStr = EntityUtils.toString(entity);
	            Document document = Jsoup.parse(entityStr);
	            String goodsId = document.select("#goodsId").attr("value");
	            String goodsCode = document.select("#goodsCode").attr("value");
	            String repositoryId = document.select("#repositoryId").attr("value");
	            String salePrice = document.select("#salePrice").attr("value");
	            String goodsName = document.select("#goodsName").attr("value");
	            String costPrice = document.select("#costPrice").attr("value");
	            String productImagePath = document.select("#productImagePath").attr("value");
	            String goodsProperty = document.select("#goodsProperty").attr("value");
	            String freeTaxPrice = document.select("#freeTaxPrice").attr("value");
	            
	            event.setSalePrice(salePrice);
	            
	            JSONObject job = new JSONObject();
	            job.put("itemId", goodsId);
	            job.put("itemCode", goodsCode);
	            job.put("productName", goodsName);
	            job.put("itemChannel", repositoryId);
	            job.put("costPrice", costPrice);
	            job.put("salePrice", salePrice);
	            job.put("sumPurePointsPrice", 0);
	            job.put("sumStablePointsPrice", 0);
	            job.put("sumStableCashPrice", 0);
	            
	            job.put("productImagePath", productImagePath);
	            job.put("goodsProperty", goodsProperty);
	            job.put("freeTaxPrice", freeTaxPrice);
	            job.put("cnt", 1);
	            job.put("url", "/product_4068_2");
	            job.put("purePointsPrice", 0);
	            job.put("stablePointsPrice", 0);
	            job.put("stableCashPrice", 0);
	            job.put("purchaseType", "02");
	            
	            String jsoncar = job.toJSONString();
	            
	            jsoncar = "[" + jsoncar + "]";
	            
	    		String cartJsonStr = URLEncoder.encode(jsoncar, "utf-8");
	            
	            System.out.println(cartJsonStr);
	            event.setCartJsonStr(cartJsonStr);
				event.setState(PingAnSpiderState.GET_MEMBER_INIT_SP);
			} catch (Exception e) {
			       logger.error("get loginInitspDown down exception", e);
	               e.printStackTrace();
				   event.setException(new SpiderException(-1,"下单失败"));
			}
		}
		
		private void getMemberInitDown(HttpResponse response){
				event.setState(PingAnSpiderState.GET_INFO_SP);
		}
		
		
		private void getInfoDown(HttpResponse response){
			try {
				HttpEntity entity = response.getEntity();
	            String entityStr = EntityUtils.toString(entity);
	           
	            Document document = Jsoup.parse(entityStr);
	            String param = document.select("input").first().attr("value");
	            if(StringUtils.isBlank(param)){
	            	logger.info("PingAnSpider:param is null");
	            	 event.setException(new SpiderException(1020,"系统繁忙"));
	            }
	            
	            event.setParamValue(param);
	            
                event.setState(PingAnSpiderState.LOGIN_AUTH_SP);
			} catch (Exception e) {
			       logger.error("get loginInitspDown down exception", e);
	               e.printStackTrace();
				   event.setException(new SpiderException(-1,"下单失败"));
			}
		}
		
		
		
		private void loginAuthDown(HttpResponse response){
			try {
				HttpEntity entity = response.getEntity();
	            String entityStr = EntityUtils.toString(entity);
	            Document document = Jsoup.parse(entityStr);
	            String param = document.select("#param").attr("value");
	            
	            event.setParamValue(param);
				event.setState(PingAnSpiderState.LOGIN_SUCCESS_SP);
			} catch (Exception e) {
			       logger.error("get loginInitspDown down exception", e);
	               e.printStackTrace();
				   event.setException(new SpiderException(-1,"下单失败"));
			}
		}
		
		private void loginSuccessDown(HttpResponse response){
				event.setState(PingAnSpiderState.GET_MEMBER_SP);
		}

		private void getMemberDown(HttpResponse response){
			try {
				HttpEntity entity = response.getEntity();
	            String entityStr = EntityUtils.toString(entity);
	        	
    			int s = entityStr.indexOf("<script>");
    			int e = entityStr.indexOf("</script>");
    			
    			String script = entityStr.substring(s+8,e);
	            
              	//获得JS脚本引擎
            	ScriptEngineManager manager = new ScriptEngineManager();
            	ScriptEngine engine = manager.getEngineByExtension("js");
            	//设置JS脚本中的userArray、date变量
            	
            	engine.eval(script);//执行JS脚本
	            Map<String,String> map = (Map<String, String>) engine.get("memberInfoData");
	            event.setUserAuth(map.get("userAuth"));
            	
	            String orderType = event.getOrderType();
	            
	            if("001001".equals(orderType)){
	            	 event.setState(PingAnSpiderState.ADD_ADDRESS);
	            }else {
	            	 event.setState(PingAnSpiderState.BUY_CONFIRM_SP);
				}
	            
	           
			} catch (Exception e) {
			       logger.error("get loginInitspDown down exception", e);
	               e.printStackTrace();
				   event.setException(new SpiderException(-1,"下单失败"));
			}
		}

		
		private void buyConfirmDown(HttpResponse response){
			try {
				HttpEntity entity = response.getEntity();
	            String entityStr = EntityUtils.toString(entity);
	            Document document = Jsoup.parse(entityStr);
	            String orderSubmitFormToken = document.select("input[name=orderSubmitFormToken]").attr("value");
	            String cashAmt = document.select("#cashAmt").attr("value");
	            String salesType = document.select("#salesType").attr("value");
	            String defaultMobile = document.select("#defaultMobile").attr("value");
	            String orderType = document.select("#orderType").attr("value");
	            String pointAmt = document.select("#pointAmt").attr("value");
	            String couponAmt = document.select("#couponAmt").attr("value");
	            //div class = info-address current
	            //input name="address  id =
	            //默认   address
	            String modifyAddress = document.select("#couponAmt").attr("value");
	            
	            event.setOrderSubmitFormToken(orderSubmitFormToken);
	            event.setCashAmt(cashAmt);
	            event.setSalesType(salesType);
	            event.setDefaultMobile(defaultMobile);
	           // event.setOrderType(orderType);
	            event.setPointAmt(pointAmt);
	            event.setCouponAmt(couponAmt);
	            if(StringUtils.isBlank(orderSubmitFormToken)){
	            	logger.info("PingAnSpider:orderSubmitFormToken is null");
	            	 event.setException(new SpiderException(1020,"系统繁忙"));
	            }else {
	            	  event.setState(PingAnSpiderState.COMMIT_ORDER_SP);	
				}
			} catch (Exception e) {
			       logger.error("get loginInitspDown down exception", e);
	               e.printStackTrace();
				   event.setException(new SpiderException(-1,"下单失败"));
			}
		}

		
		private void commitOrderDown(HttpResponse response){
			try {
				HttpEntity entity = response.getEntity();
	            String entityStr = EntityUtils.toString(entity);
	   
	            Document document = Jsoup.parse(entityStr);
	            String scriptstring = document.select("div[class=payment-result]").html();
	            
	            if(scriptstring.contains("您的订单提交失败")){
	            	 event.setException(new SpiderException(1021,"订单提交失败"));
	            }else {
	            	int s =  scriptstring.indexOf("//payment");
	   	            int e =  scriptstring.indexOf("\";");
	   	            
	   	            String topayurl =  scriptstring.substring(s-6, e);
	   	            event.setTopayurl(topayurl);;
	   	            
	               	event.setState(PingAnSpiderState.PAYMENT_SP);
				}
	         
			} catch (Exception e) {
			       logger.error("get loginInitspDown down exception", e);
	               e.printStackTrace();
				   event.setException(new SpiderException(-1,"下单失败"));
			}
		}
		
		
		
		private void paymentDown(HttpResponse response){
			try {
				HttpEntity entity = response.getEntity();
	            String entityStr = EntityUtils.toString(entity);
	   
	            Document document = Jsoup.parse(entityStr);
	            
	            String loginIFrame = document.select("#loginIFrame").attr("src");
	            if(!StringUtils.isBlank(loginIFrame)){
	            	event.setIntoUrl(loginIFrame);
	            	event.setState(PingAnSpiderState.UM_LOGIN_SP);
	            }else{
	            	
	            String orderId = document.select("#orderId").attr("value");
	            String orderDate = document.select("#orderDate").attr("value");
	            String merId = document.select("#merId").attr("value");
	            String reqId = document.select("#reqId").attr("value");
	            String totalOrderAmt = document.select("#totalOrderAmt").attr("value");
	            String orderAmt = document.select("#orderAmt").attr("value");
	            String id = document.select("#id").attr("value");
	            String platFormGateId = document.select("#platFormGateId").attr("value");
	            String gateId = document.select("#gateId").attr("value");
	            String parentGateId = document.select("#parentGateId").attr("value");
	            String cash = document.select("#cash").attr("value");
	            String userPoints = document.select("#userPoints").attr("value");
	            String points = document.select("#points").attr("value");

	            String otherGateIndexArray = document.select("#otherGateIndexArray").attr("value");
	            
	            if(StringUtils.isBlank(points)){
	            	 String defaultPoints = document.select("#defaultPoints").attr("value");
	            	 event.setPoints(defaultPoints);
	            }else {
	            	 event.setPoints(points);
				}
	            
	            event.setOrderId(orderId);
	            event.setOrderDate(orderDate);
	            event.setMerId(merId);
	            event.setReqId(reqId);
	            event.setTotalOrderAmt(totalOrderAmt);
	            event.setOrderAmt(orderAmt);
	            event.setIid(id);
	            event.setPlatFormGateId(platFormGateId);
	            event.setGateId(gateId);
	            event.setParentGateId(parentGateId);
	            event.setCash(cash);
	            event.setUserPoints(userPoints);
	            event.setOtherGateIndexArray(otherGateIndexArray);
	            
	            event.setState(PingAnSpiderState.PAY_YM_SP);
	           	
	            }
			} catch (Exception e) {
			       logger.error("get loginInitspDown down exception", e);
	               e.printStackTrace();
				   event.setException(new SpiderException(-1,"下单失败"));
			}
		}
		
		
		private void umLoginDown(HttpResponse response){
			try {
				HttpEntity entity = response.getEntity();
	            String entityStr = EntityUtils.toString(entity);
	            Document document = Jsoup.parse(entityStr);
	            String param = document.select("#param").attr("value");
	            event.setParamValue(param);
	            event.setState(PingAnSpiderState.FIRST_SP);
			} catch (Exception e) {
			       logger.error("get loginInitspDown down exception", e);
	               e.printStackTrace();
				   event.setException(new SpiderException(-1,"同意条款跳转失败"));
			}
		}
		
		
		private void firstDown(HttpResponse response){
			try {
				HttpEntity entity = response.getEntity();
	            String entityStr = EntityUtils.toString(entity);
	            Document document = Jsoup.parse(entityStr);
	            String param = document.select("#param").attr("value");
	            event.setParamValue(param);
	            event.setState(PingAnSpiderState.FIRST_DIR_SP);
			} catch (Exception e) {
			       logger.error("get loginInitspDown down exception", e);
	               e.printStackTrace();
				   event.setException(new SpiderException(-1,"同意条款跳转失败"));
			}
		}
		
		
		private void firstDirDown(HttpResponse response){
			try {
				HttpEntity entity = response.getEntity();
	            String entityStr = EntityUtils.toString(entity);
	            
				int s = entityStr.indexOf("https://");
    			int e = entityStr.indexOf("';");
    			
    		    String dd =  entityStr.substring(s, e);
	            String tourl = dd.trim().replace("amp;", "").trim();
	            
	            event.setIntoUrl(tourl);
	            event.setState(PingAnSpiderState.TO_PAY_MENT_SP);
			} catch (Exception e) {
			       logger.error("get loginInitspDown down exception", e);
	               e.printStackTrace();
				   event.setException(new SpiderException(-1,"同意条款跳转失败"));
			}
		}
		
		
		private void topaymentDown(HttpResponse response){
			try {
				HttpEntity entity = response.getEntity();
	            String entityStr = EntityUtils.toString(entity);
	            Document document = Jsoup.parse(entityStr);
	          	
	            String orderId = document.select("#orderId").attr("value");
	            String orderDate = document.select("#orderDate").attr("value");
	            String merId = document.select("#merId").attr("value");
	            String reqId = document.select("#reqId").attr("value");
	            String totalOrderAmt = document.select("#totalOrderAmt").attr("value");
	            String orderAmt = document.select("#orderAmt").attr("value");
	            String id = document.select("#id").attr("value");
	            String platFormGateId = document.select("#platFormGateId").attr("value");
	            String gateId = document.select("#gateId").attr("value");
	            String parentGateId = document.select("#parentGateId").attr("value");
	            String cash = document.select("#cash").attr("value");
	            String userPoints = document.select("#userPoints").attr("value");
	            String points = document.select("#points").attr("value");

	            String otherGateIndexArray = document.select("#otherGateIndexArray").attr("value");
	            
	            if(StringUtils.isBlank(points)){
	            	 String defaultPoints = document.select("#defaultPoints").attr("value");
	            	 event.setPoints(defaultPoints);
	            }else {
	            	 event.setPoints(points);
				}
	            
	            event.setOrderId(orderId);
	            event.setOrderDate(orderDate);
	            event.setMerId(merId);
	            event.setReqId(reqId);
	            event.setTotalOrderAmt(totalOrderAmt);
	            event.setOrderAmt(orderAmt);
	            event.setIid(id);
	            event.setPlatFormGateId(platFormGateId);
	            event.setGateId(gateId);
	            event.setParentGateId(parentGateId);
	            event.setCash(cash);
	            event.setUserPoints(userPoints);
	            event.setOtherGateIndexArray(otherGateIndexArray);
	            
	            
	            event.setState(PingAnSpiderState.PAY_YM_SP);
			} catch (Exception e) {
			       logger.error("get loginInitspDown down exception", e);
	               e.printStackTrace();
				   event.setException(new SpiderException(-1,"同意条款跳转失败"));
			}
		}
		
		
		
		private void payYmDown(HttpResponse response){
			try {
				HttpEntity entity = response.getEntity();
	            String entityStr = EntityUtils.toString(entity);
	   
	            Document document = Jsoup.parse(entityStr);
	            String payMode = document.select("#payMode").attr("value");
	            
	            event.setPayMode(payMode);
	            if("1".equals(payMode) || "3".equals(payMode)){
	            	 event.setState(PingAnSpiderState.SMS_CODE_SP);
	            }else if("2".equals(payMode)){
		            String key = String.format(Constant.SHOP_PINGAN_LOGIN_KEY, event.getUserid());
		            redis.set(key, JSON.toJSONString(event), redistime);
		            event.setException(new SpiderException(2010,"下单成功"));
	            	// event.setState(PingAnSpiderState.CONTINUE_PAY_SP);
	            }else{
	            	 event.setException(new SpiderException(-1,"下单失败"));
	            }
	           
			} catch (Exception e) {
			       logger.error("get loginInitspDown down exception", e);
	               e.printStackTrace();
				   event.setException(new SpiderException(-1,"下单失败"));
			}
		}
		
		
		
		private void continuePayDown(HttpResponse response){
			try {
				HttpEntity entity = response.getEntity();
	            String entityStr = EntityUtils.toString(entity);
	         
	            JSONObject jobj = JSON.parseObject(entityStr);
	            String retCode = (String) jobj.get("retCode");
	           // retMsg":"手机动态码验证失败","retCode":"10000003"
	            if("10".equals(retCode)){
	            	event.setException(new SpiderException(0,"交易完成"));
	            }else if("10000003".equals(retCode)){
	            	String retMsg = (String) jobj.get("retMsg");
	            	event.setException(new SpiderException(-1,retMsg));
				}else{
					String retMsg = (String) jobj.get("retMsg");
	            	event.setException(new SpiderException(-1,retMsg));
				}
	            
	            //"orderType":"00","retCode":"10
			} catch (Exception e) {
			       logger.error("get loginInitspDown down exception", e);
	               e.printStackTrace();
				   event.setException(new SpiderException(-1,"交易失败"));
			}
		}
		
		
		private void smsCodeDown(HttpResponse response){
			try {
				HttpEntity entity = response.getEntity();
	            String entityStr = EntityUtils.toString(entity);
	            JSONObject jobj = JSON.parseObject(entityStr);
	            String retCode = (String) jobj.get("retCode");
	            
	            String paymode = event.getPayMode();
	    		if("00".equals(retCode)){
		            String key = String.format(Constant.SHOP_PINGAN_LOGIN_KEY, event.getUserid());
		            redis.set(key, JSON.toJSONString(event), redistime);
		            if("3".equals(paymode)){
		            	event.setException(new SpiderException(2007,"验证码发送成功"));
		            }else {
		            	event.setException(new SpiderException(2001,"验证码发送成功"));
					}
	    		    
	    		}else {
	    			event.setException(new SpiderException(2005,"验证码发送失败"));
				}
			} catch (Exception e) {
			       logger.error("get loginInitspDown down exception", e);
	               e.printStackTrace();
				   event.setException(new SpiderException(-1,"服务器繁忙"));
			}
		}
		
		private void getAddressDown(HttpResponse response){
			try {
				
				//jQuery111202785740054678172_1459911688360({"respCode":0,"deliveryList":[{"phone":"--","deliveryId":4122857,"isDefault":1,"address":"天目山路226号网新大厦3310","cellphone":"13588313856","zipCode":"","name":"财途积分生活","province":"浙江","memberId":"010000185550889","district":"西湖区","city":"杭州"}],"memberId":"010000185550889"})
				HttpEntity entity = response.getEntity();
	            String entityStr = EntityUtils.toString(entity);
	    		
			} catch (Exception e) {
			       logger.error("get loginInitspDown down exception", e);
	               e.printStackTrace();
				   event.setException(new SpiderException(-1,"初始化失败"));
			}
		}
		
		
		
		private void addAddressDown(HttpResponse response){
			try {
				HttpEntity entity = response.getEntity();
	            String entityStr = EntityUtils.toString(entity);
	    		
	            int s = entityStr.indexOf("respCode\\\":");
	            int e = entityStr.indexOf(",\\\"deliveryList");
	             
	            String dd =  entityStr.substring(s+11, e);
	          	
	            
	            if(StringUtils.isBlank(dd) || !"0".equals(dd)){
	            	event.setException(new SpiderException(2002,"新增地址失败"));
	            }
	            
	            int r = entityStr.indexOf("{\"result");
	            String gg =  entityStr.substring(r);
	            String cc = gg.trim().substring(0, gg.length()-1);
	            JSONObject jobj = JSON.parseObject(cc);
	            Integer deliveryId =  (Integer) jobj.get("deliveryId");
	            
	            event.setDeliveryId(deliveryId);
	            
	            //"orderType":"00","retCode":"10
	            event.setState(PingAnSpiderState.BUY_CONFIRM_SP);
			} catch (Exception e) {
			       logger.error("get loginInitspDown down exception", e);
	               e.printStackTrace();
				   event.setException(new SpiderException(-1,"初始化失败"));
			}
		}
		
		
		private void vcodewebDown(HttpResponse response){
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
	                if (appConfig.inDevMode()) {
	                    byte[] tbytes = Base64.getDecoder().decode(imgStr);
	                    FileOutputStream fs = new FileOutputStream(
	                            appConfig.getUploadPath() + "/" + event.getUserid()
	                                    + ".jpg");
	                    fs.write(tbytes);
	                    fs.close();
	                }
	                
	                String key = String.format(Constant.SHOP_PINGAN_TASK_QUEUE, event.getUserid());
	                redis.set(key, JSON.toJSONString(event), 600);
	                
	                event.setException(new SpiderException(0, "输入验证码", imgStr));
			} catch (Exception e) {
                logger.error("get img down exception", e);
                e.printStackTrace();
                event.setException(new SpiderException(-1,"获取验证码失败"));
			}
		}
		
		
		
		private void validateVcodeDown(HttpResponse response){
			try {
				HttpEntity entity = response.getEntity();
	            String entityStr = EntityUtils.toString(entity);
	            JSONObject jobj = JSON.parseObject(entityStr);
	            String vresult = (String) jobj.get("vresult");
	            if("success".equals(vresult)){
	            	 event.setState(PingAnSpiderState.TORESETPWD);
	            }else{
	            	event.setException(new SpiderException(3301,"验证失败"));
	            }
	              
			} catch (Exception e) {
                logger.error("get img down exception", e);
                e.printStackTrace();
                event.setException(new SpiderException(-1,"获取验证码失败"));
			}
		}
		
		
		private void toresetpwdDown(HttpResponse response){
			try {
				HttpEntity entity = response.getEntity();
	            String entityStr = EntityUtils.toString(entity);
	            JSONObject jobj = JSON.parseObject(entityStr);
	            String messagecode = (String) jobj.get("messagecode");
	            if("00".equals(messagecode)){
	            	 event.setState(PingAnSpiderState.TOCOMMITMEMBER);
	            }else{
	            	event.setException(new SpiderException(3301,"验证失败"));
	            }
	              
			} catch (Exception e) {
                logger.error("get img down exception", e);
                e.printStackTrace();
                event.setException(new SpiderException(-1,"获取验证码失败"));
			}
		}
		
		
		private void tocommitmemberDown(HttpResponse response){
			try {
				HttpEntity entity = response.getEntity();
	            String entityStr = EntityUtils.toString(entity);

	            Document document = Jsoup.parse(entityStr);
	            String maskMobile = document.select("#maskMobile").attr("value");
	            String username = document.select("#username").attr("value");
	            
	            
	            event.setMaskMobile(maskMobile);
	            event.setUsername(username);
	            event.setState(PingAnSpiderState.REGVCODE);
	              
			} catch (Exception e) {
                logger.error("get img down exception", e);
                e.printStackTrace();
                event.setException(new SpiderException(-1,"获取验证码失败"));
			}
		}
		
		
		
		private void regvcodeDown(HttpResponse response){
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
	                if (appConfig.inDevMode()) {
	                    byte[] tbytes = Base64.getDecoder().decode(imgStr);
	                    FileOutputStream fs = new FileOutputStream(
	                            appConfig.getUploadPath() + "/" + event.getUserid()
	                                    + ".jpg");
	                    fs.write(tbytes);
	                    fs.close();
	                }
	                
	                String key = String.format(Constant.SHOP_PINGAN_TASK_QUEUE, event.getUserid());
	                redis.set(key, JSON.toJSONString(event), 600);
	                
	                //event.setState(PingAnSpiderState.SEND_CODE);
	                event.setException(new SpiderException(0, "输入验证码", imgStr));
			} catch (Exception e) {
                logger.error("get img down exception", e);
                e.printStackTrace();
                event.setException(new SpiderException(-1,"获取验证码失败"));
			}
		}
		
		
		private void sendCodeDown(HttpResponse response){
			try {
				HttpEntity entity = response.getEntity();
	            String entityStr = EntityUtils.toString(entity);
	            JSONObject jobj = JSON.parseObject(entityStr);
	            String ErrorCode = (String) jobj.get("ErrorCode");
	            if("00".equals(ErrorCode)){
	                String key = String.format(Constant.SHOP_PINGAN_TASK_QUEUE, event.getUserid());
	                redis.set(key, JSON.toJSONString(event), 600);
	                event.setException(new SpiderException(0,"发送验证码成功"));
	            }else if("02".equals(ErrorCode)){
	            	event.setException(new SpiderException(3302,"图形验证码错误"));
	            }else{
	            	event.setException(new SpiderException(3303,"发送验证码失败"));
	            }
	              //02 图片验证码错误
			} catch (Exception e) {
                logger.error("get img down exception", e);
                e.printStackTrace();
                event.setException(new SpiderException(-1,"获取验证码失败"));
			}
		}
		
		
		private void checkCodeDown(HttpResponse response){
			try {
				HttpEntity entity = response.getEntity();
	            String entityStr = EntityUtils.toString(entity);
	            JSONObject jobj = JSON.parseObject(entityStr);
	            String message = (String) jobj.get("message");
	            if("success".equals(message)){
	            	
	                String key = String.format(Constant.SHOP_PINGAN_TASK_QUEUE, event.getUserid());
	                redis.set(key, JSON.toJSONString(event), 600);
	                event.setException(new SpiderException(0,"验证成功"));
	            }else{
	            	event.setException(new SpiderException(3304,"验证失败"));
	            }
	              
			} catch (Exception e) {
                logger.error("get img down exception", e);
                e.printStackTrace();
                event.setException(new SpiderException(-1,"验证失败"));
			}
		}
		
		
		
		private void resetThreeDown(HttpResponse response){
			try {
				HttpEntity entity = response.getEntity();
	            String entityStr = EntityUtils.toString(entity);
	            Document document = Jsoup.parse(entityStr);
	            String memberId = document.select("#memberid").attr("value");
	            event.setMemberId(memberId);
	            event.setState(PingAnSpiderState.CHECK_PASSWORD);
	            
			} catch (Exception e) {
                logger.error("get img down exception", e);
                e.printStackTrace();
                event.setException(new SpiderException(-1,"验证失败"));
			}
		}
		
		private void checkPassDown(HttpResponse response){
			try {
				HttpEntity entity = response.getEntity();
	            String entityStr = EntityUtils.toString(entity);
	            JSONObject jobj = JSON.parseObject(entityStr);
	            String message = (String) jobj.get("errorCode");
	            
	            if("00".equals(message)){
	            	event.setState(PingAnSpiderState.UPDATE_PWD);
	            }else{
	            	String errorMessage = (String) jobj.get("errorMessage");
	            	event.setException(new SpiderException(3305,errorMessage));
	            }
	            
	            
			} catch (Exception e) {
                logger.error("get img down exception", e);
                e.printStackTrace();
                event.setException(new SpiderException(-1,"验证失败"));
			}
		}
		
		
		private void updatePwdDown(HttpResponse response){
			try {
				HttpEntity entity = response.getEntity();
	            String entityStr = EntityUtils.toString(entity);
	            JSONObject jobj = JSON.parseObject(entityStr);
	            String message = (String) jobj.get("message");
	            if("success".equals(message)){
	            	event.setException(new SpiderException(0,"修改成功"));
	            }else{
	            	event.setException(new SpiderException(3310,"修改密码失败"));
	            }
	              
			} catch (Exception e) {
                logger.error("get img down exception", e);
                e.printStackTrace();
                event.setException(new SpiderException(-1,"获取验证码失败"));
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
