/*
 * Copyright (c) 2015-2020 by caitu99
 * All rights reserved.
 */
package com.caitu99.lsp.model.spider.pingan;

import org.springframework.web.context.request.async.DeferredResult;

import com.alibaba.fastjson.annotation.JSONField;
import com.caitu99.lsp.model.spider.QueryEvent;

/** 
 * 
 * @Description: (类职责详细描述,可空) 
 * @ClassName: PingAnSpiderEvent 
 * @author fangjunxiao
 * @date 2016年3月30日 上午11:20:29 
 * @Copyright (c) 2015-2020 by caitu99 
 */
public class PingAnSpiderEvent extends QueryEvent {

	
	private PingAnSpiderState state;
	
	private String loginUrl;
	
	private String gUrl;
	
	private String backURL;
	
	private String appId;
	
	private String intoUrl;
	
	private String paramValue;
	
	private String memberId;
	
	private String userAuth;
	
	private String cartJsonStr;
	
    private String orderSubmitFormToken;
    private String cashAmt;
    private String salesType;
    private String defaultMobile;
    private String orderType;
    private String pointAmt;
    private String couponAmt;
    
    
    private String topayurl;
	
	
    
    private String orderId;
    private String orderDate;
    private String merId;
    private String reqId;
    private String totalOrderAmt;
    private String orderAmt;
    private String iid;
    private String platFormGateId;
    private String gateId;
    private String parentGateId;
    private String points;
    private String cash;
    private String userPoints;
    
    private String otherGateIndexArray;
    
    
    private String province;
    private String city;
    private String district;
    private String address;
    private String name;
    
    private String cellphone;
    
    private Integer deliveryId;
    
    
    private String goodsId;
    private String repositoryId;
    
    
    private String payPassWord;
    private String payMode;
    private String smsCode;
    
    
    private String salePrice;
    
    
    private String timestamp;
    private String maskMobile;
    private String username;
    
    
    


	public String getMaskMobile() {
		return maskMobile;
	}

	public void setMaskMobile(String maskMobile) {
		this.maskMobile = maskMobile;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(String timestamp) {
		this.timestamp = timestamp;
	}

	public String getSmsCode() {
		return smsCode;
	}

	public void setSmsCode(String smsCode) {
		this.smsCode = smsCode;
	}

	public String getSalePrice() {
		return salePrice;
	}

	public void setSalePrice(String salePrice) {
		this.salePrice = salePrice;
	}

	public Integer getDeliveryId() {
		return deliveryId;
	}

	public void setDeliveryId(Integer deliveryId) {
		this.deliveryId = deliveryId;
	}

	public String getPayPassWord() {
		return payPassWord;
	}

	public void setPayPassWord(String payPassWord) {
		this.payPassWord = payPassWord;
	}

	public String getPayMode() {
		return payMode;
	}

	public void setPayMode(String payMode) {
		this.payMode = payMode;
	}

	public String getCellphone() {
		return cellphone;
	}

	public void setCellphone(String cellphone) {
		this.cellphone = cellphone;
	}

	public String getRepositoryId() {
		return repositoryId;
	}

	public void setRepositoryId(String repositoryId) {
		this.repositoryId = repositoryId;
	}

	public String getGoodsId() {
		return goodsId;
	}

	public void setGoodsId(String goodsId) {
		this.goodsId = goodsId;
	}

	public String getProvince() {
		return province;
	}

	public void setProvince(String province) {
		this.province = province;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public String getDistrict() {
		return district;
	}

	public void setDistrict(String district) {
		this.district = district;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getOrderId() {
		return orderId;
	}

	public void setOrderId(String orderId) {
		this.orderId = orderId;
	}

	public String getOrderDate() {
		return orderDate;
	}

	public void setOrderDate(String orderDate) {
		this.orderDate = orderDate;
	}

	public String getMerId() {
		return merId;
	}

	public void setMerId(String merId) {
		this.merId = merId;
	}

	public String getReqId() {
		return reqId;
	}

	public void setReqId(String reqId) {
		this.reqId = reqId;
	}

	public String getTotalOrderAmt() {
		return totalOrderAmt;
	}

	public void setTotalOrderAmt(String totalOrderAmt) {
		this.totalOrderAmt = totalOrderAmt;
	}

	public String getOrderAmt() {
		return orderAmt;
	}

	public void setOrderAmt(String orderAmt) {
		this.orderAmt = orderAmt;
	}

	public String getIid() {
		return iid;
	}

	public void setIid(String iid) {
		this.iid = iid;
	}

	public String getPlatFormGateId() {
		return platFormGateId;
	}

	public void setPlatFormGateId(String platFormGateId) {
		this.platFormGateId = platFormGateId;
	}

	public String getGateId() {
		return gateId;
	}

	public void setGateId(String gateId) {
		this.gateId = gateId;
	}

	public String getParentGateId() {
		return parentGateId;
	}

	public void setParentGateId(String parentGateId) {
		this.parentGateId = parentGateId;
	}

	public String getPoints() {
		return points;
	}

	public void setPoints(String points) {
		this.points = points;
	}

	public String getCash() {
		return cash;
	}

	public void setCash(String cash) {
		this.cash = cash;
	}

	public String getUserPoints() {
		return userPoints;
	}

	public void setUserPoints(String userPoints) {
		this.userPoints = userPoints;
	}

	public String getOtherGateIndexArray() {
		return otherGateIndexArray;
	}

	public void setOtherGateIndexArray(String otherGateIndexArray) {
		this.otherGateIndexArray = otherGateIndexArray;
	}

	public String getTopayurl() {
		return topayurl;
	}

	public void setTopayurl(String topayurl) {
		this.topayurl = topayurl;
	}

	public String getCouponAmt() {
		return couponAmt;
	}

	public void setCouponAmt(String couponAmt) {
		this.couponAmt = couponAmt;
	}

	public String getOrderSubmitFormToken() {
		return orderSubmitFormToken;
	}

	public void setOrderSubmitFormToken(String orderSubmitFormToken) {
		this.orderSubmitFormToken = orderSubmitFormToken;
	}

	public String getCashAmt() {
		return cashAmt;
	}

	public void setCashAmt(String cashAmt) {
		this.cashAmt = cashAmt;
	}

	public String getSalesType() {
		return salesType;
	}

	public void setSalesType(String salesType) {
		this.salesType = salesType;
	}

	public String getDefaultMobile() {
		return defaultMobile;
	}

	public void setDefaultMobile(String defaultMobile) {
		this.defaultMobile = defaultMobile;
	}

	public String getOrderType() {
		return orderType;
	}

	public void setOrderType(String orderType) {
		this.orderType = orderType;
	}

	public String getPointAmt() {
		return pointAmt;
	}

	public void setPointAmt(String pointAmt) {
		this.pointAmt = pointAmt;
	}

	public String getCartJsonStr() {
		return cartJsonStr;
	}

	public void setCartJsonStr(String cartJsonStr) {
		this.cartJsonStr = cartJsonStr;
	}

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

	public PingAnSpiderEvent(String sessionId, DeferredResult<Object> deferredResult) {
		super(sessionId, deferredResult);
        
	}
	
	public PingAnSpiderEvent() {
		super();
	}

	@JSONField(deserialize = false)
    public void setException(Exception exception) {
        this.setState(PingAnSpiderState.ERROR);
        this.exception = exception;
    }

	public PingAnSpiderState getState() {
		return state;
	}

	public void setState(PingAnSpiderState state) {
		this.state = state;
	}
	
	
	
	
}
