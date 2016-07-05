/*
 * Copyright (c) 2015-2020 by caitu99
 * All rights reserved.
 */
package com.caitu99.lsp.model.spider.pingan;

import java.util.Map;

import org.springframework.web.context.request.async.DeferredResult;

import com.alibaba.fastjson.annotation.JSONField;
import com.caitu99.lsp.model.spider.QueryEvent;
import com.caitu99.lsp.model.spider.ccbshop.CCBShopState;

/** 
 * 
 * @Description: (类职责详细描述,可空) 
 * @ClassName: PingAnSpider 
 * @author ws
 * @date 2016年4月1日 上午14:42:23 
 * @Copyright (c) 2015-2020 by caitu99 
 */
public class PingAnOilSpiderEvent extends QueryEvent {

	
	private PingAnOilSpiderState state;
	
	private String loginUrl;
	
	private String gUrl;
	
	private String backURL;
	
	private String appId;
	
	private String intoUrl;
	
	private String paramValue;
	
	private String memberId;
	
	private String userAuth;
	
	private Map<String,String> paramMap;
	
	private Map<String,String> orderMap;
	
	private String phoneNum;
	
	private Long totalPoints;
	
	private String orderId;
	
	private String payPwd;
	
	private String payGUrl;//支付时gUrl
	
	private String payRetCode;//支付返回Code
	
	private String timestamp;
	
	private String productId;
	
	private String payMod;
	
	private String msgCode;//支付短信验证码
	
	public String getMemberId() {
		return memberId;
	}

	public void setMemberId(String memberId) {
		this.memberId = memberId;
	}

	public String getUserAuth() {
		return userAuth;
	}

	public void setUserAuth(String userAuth) {
		this.userAuth = userAuth;
	}

	public String getParamValue() {
		return paramValue;
	}

	public void setParamValue(String paramValue) {
		this.paramValue = paramValue;
	}

	public String getIntoUrl() {
		return intoUrl;
	}

	public void setIntoUrl(String intoUrl) {
		this.intoUrl = intoUrl;
	}

	public String getgUrl() {
		return gUrl;
	}

	public void setgUrl(String gUrl) {
		this.gUrl = gUrl;
	}

	public String getBackURL() {
		return backURL;
	}

	public void setBackURL(String backURL) {
		this.backURL = backURL;
	}

	public String getAppId() {
		return appId;
	}

	public void setAppId(String appId) {
		this.appId = appId;
	}

	public String getLoginUrl() {
		return loginUrl;
	}

	public void setLoginUrl(String loginUrl) {
		this.loginUrl = loginUrl;
	}

	public PingAnOilSpiderEvent(String sessionId, DeferredResult<Object> deferredResult) {
		super(sessionId, deferredResult);
        
	}
	
	public PingAnOilSpiderEvent() {
		super();
	}

	@JSONField(deserialize = false)
    public void setException(Exception exception) {
        this.setState(PingAnOilSpiderState.ERROR);
        this.exception = exception;
    }

	public PingAnOilSpiderState getState() {
		return state;
	}

	public void setState(PingAnOilSpiderState state) {
		this.state = state;
	}

	/**
	 * @return the paramMap
	 */
	public Map<String,String> getParamMap() {
		return paramMap;
	}

	/**
	 * @param paramMap the paramMap to set
	 */
	public void setParamMap(Map<String,String> paramMap) {
		this.paramMap = paramMap;
	}

	/**
	 * @return the phoneNum
	 */
	public String getPhoneNum() {
		return phoneNum;
	}

	/**
	 * @param phoneNum the phoneNum to set
	 */
	public void setPhoneNum(String phoneNum) {
		this.phoneNum = phoneNum;
	}

	/**
	 * @return the totalPoints
	 */
	public Long getTotalPoints() {
		return totalPoints;
	}

	/**
	 * @param totalPoints the totalPoints to set
	 */
	public void setTotalPoints(Long totalPoints) {
		this.totalPoints = totalPoints;
	}

	/**
	 * @return the orderId
	 */
	public String getOrderId() {
		return orderId;
	}

	/**
	 * @param orderId the orderId to set
	 */
	public void setOrderId(String orderId) {
		this.orderId = orderId;
	}

	/**
	 * @return the payPwd
	 */
	public String getPayPwd() {
		return payPwd;
	}

	/**
	 * @param payPwd the payPwd to set
	 */
	public void setPayPwd(String payPwd) {
		this.payPwd = payPwd;
	}

	/**
	 * @return the payGUrl
	 */
	public String getPayGUrl() {
		return payGUrl;
	}

	/**
	 * @param payGUrl the payGUrl to set
	 */
	public void setPayGUrl(String payGUrl) {
		this.payGUrl = payGUrl;
	}

	/**
	 * @return the payRetCode
	 */
	public String getPayRetCode() {
		return payRetCode;
	}

	/**
	 * @param payRetCode the payRetCode to set
	 */
	public void setPayRetCode(String payRetCode) {
		this.payRetCode = payRetCode;
	}

	/**
	 * @return the orderMap
	 */
	public Map<String,String> getOrderMap() {
		return orderMap;
	}

	/**
	 * @param orderMap the orderMap to set
	 */
	public void setOrderMap(Map<String,String> orderMap) {
		this.orderMap = orderMap;
	}

	/**
	 * @return the timestamp
	 */
	public String getTimestamp() {
		return timestamp;
	}

	/**
	 * @param timestamp the timestamp to set
	 */
	public void setTimestamp(String timestamp) {
		this.timestamp = timestamp;
	}

	/**
	 * @return the productId
	 */
	public String getProductId() {
		return productId;
	}

	/**
	 * @param productId the productId to set
	 */
	public void setProductId(String productId) {
		this.productId = productId;
	}

	/**
	 * @return the payMod
	 */
	public String getPayMod() {
		return payMod;
	}

	/**
	 * @param payMod the payMod to set
	 */
	public void setPayMod(String payMod) {
		this.payMod = payMod;
	}

	/**
	 * @return the msgCode
	 */
	public String getMsgCode() {
		return msgCode;
	}

	/**
	 * @param msgCode the msgCode to set
	 */
	public void setMsgCode(String msgCode) {
		this.msgCode = msgCode;
	}
	
	
	
	
}
