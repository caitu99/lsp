package com.caitu99.lsp.model.spider.ccbshop;

import java.util.Map;

import org.springframework.web.context.request.async.DeferredResult;

import com.alibaba.fastjson.annotation.JSONField;
import com.caitu99.lsp.exception.SpiderException;
import com.caitu99.lsp.model.spider.QueryEvent;

public class CCBShopEvent extends QueryEvent {

	private CCBShopState state;
	
	private String initCode;
	
	private String getkeyurl;
	
	private String logpass;
	
	private String gigest;
	
	private String imgcode;
	
	private String resulturl;
	private String userName;
	
	private String MERCHANTID;
	private String BRANCHID;
	private String SERIALNO;
	private String TXCODE;
	private String COOKIES;
	private String SYS_TYPE;
	private String DATE;
	private String TIME;
	private String T_TXCODE;
	private String errURL;
	
	private String loginType;
	
	//用户名
	private String account;
	//密码
	private String password;
	//确认密码
	private String passwordEnc;
	//手机号
	private String mobile;
	//短信验证码
	private String smsCode;
	//防刷TOKEN
	private String registerToken;
	//产品id
	private String productId;
	//产品类型   1位实物  2为虚拟
	private String orderType;
	//购买数量
	private String quantity;
	//默认值为 'undefined'
	private String shopId;
	//单价
	private String productPrice;
	//留言
	private String leftMessage;
	
	private Map<String,Object> orderMap;
	//省
	private String province;
	//省code
	private String provinceCode;
	//市
	private String city;
	//市code
	private String cityCode;
	//区
	private String distinct;
	//区code
	private String distinctCode;
	//收货人姓名
	private String consigneeName;
	//详细地址
	private String addressDetail;
	//邮编
	private String postCode;
	//支付信息路径
	private String payParamsUrl;
	//支付路径
	private String payUrl;
	//信用卡号
	private String cardNumber;
	/**
	 * @return the payParamsUrl
	 */
	
	
	public String getPayParamsUrl() {
		return payParamsUrl;
	}

	public String getLoginType() {
		return loginType;
	}

	public void setLoginType(String loginType) {
		this.loginType = loginType;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getMERCHANTID() {
		return MERCHANTID;
	}

	public void setMERCHANTID(String mERCHANTID) {
		MERCHANTID = mERCHANTID;
	}

	public String getBRANCHID() {
		return BRANCHID;
	}

	public void setBRANCHID(String bRANCHID) {
		BRANCHID = bRANCHID;
	}

	public String getSERIALNO() {
		return SERIALNO;
	}

	public void setSERIALNO(String sERIALNO) {
		SERIALNO = sERIALNO;
	}

	public String getTXCODE() {
		return TXCODE;
	}

	public void setTXCODE(String tXCODE) {
		TXCODE = tXCODE;
	}

	public String getCOOKIES() {
		return COOKIES;
	}

	public void setCOOKIES(String cOOKIES) {
		COOKIES = cOOKIES;
	}

	public String getSYS_TYPE() {
		return SYS_TYPE;
	}

	public void setSYS_TYPE(String sYS_TYPE) {
		SYS_TYPE = sYS_TYPE;
	}

	public String getDATE() {
		return DATE;
	}

	public void setDATE(String dATE) {
		DATE = dATE;
	}

	public String getTIME() {
		return TIME;
	}

	public void setTIME(String tIME) {
		TIME = tIME;
	}

	public String getT_TXCODE() {
		return T_TXCODE;
	}

	public void setT_TXCODE(String t_TXCODE) {
		T_TXCODE = t_TXCODE;
	}

	public String getErrURL() {
		return errURL;
	}

	public void setErrURL(String errURL) {
		this.errURL = errURL;
	}

	public String getResulturl() {
		return resulturl;
	}

	public void setResulturl(String resulturl) {
		this.resulturl = resulturl;
	}

	public String getImgcode() {
		return imgcode;
	}

	public void setImgcode(String imgcode) {
		this.imgcode = imgcode;
	}

	public String getLogpass() {
		return logpass;
	}

	public void setLogpass(String logpass) {
		this.logpass = logpass;
	}

	public String getGigest() {
		return gigest;
	}

	public void setGigest(String gigest) {
		this.gigest = gigest;
	}

	public String getGetkeyurl() {
		return getkeyurl;
	}

	public void setGetkeyurl(String getkeyurl) {
		this.getkeyurl = getkeyurl;
	}

	public String getInitCode() {
		return initCode;
	}

	public void setInitCode(String initCode) {
		this.initCode = initCode;
	}

	/**
	 * @param payParamsUrl the payParamsUrl to set
	 */
	public void setPayParamsUrl(String payParamsUrl) {
		this.payParamsUrl = payParamsUrl;
	}

	/**
	 * @return the payUrl
	 */
	public String getPayUrl() {
		return payUrl;
	}

	/**
	 * @param payUrl the payUrl to set
	 */
	public void setPayUrl(String payUrl) {
		this.payUrl = payUrl;
	}

	//预留手机号
	private String cardMobile;
	
	
	
	/** 
	 * @Title:  
	 * @Description:
	 * @param userId
	 * @param deferredResult
	 * @param request 
	 */
	public CCBShopEvent(String sessionId, DeferredResult<Object> deferredResult) {
		super(sessionId, deferredResult);
        
	}
	
	public CCBShopEvent() {
		super();
	}

	@JSONField(deserialize = false)
    public void setException(Exception exception) {
        this.setState(CCBShopState.ERROR);
        
		if(exception instanceof SpiderException){
	        this.exception = exception;
		}else{
			this.exception = new SpiderException(-1,"建行积分商城系统维护中,请稍后再试");
		}
    }

	public CCBShopState getState() {
		return state;
	}

	public void setState(CCBShopState state) {
		this.state = state;
	}

	public String getAccount() {
		return account;
	}

	public void setAccount(String account) {
		this.account = account;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getPasswordEnc() {
		return passwordEnc;
	}

	public void setPasswordEnc(String passwordEnc) {
		this.passwordEnc = passwordEnc;
	}

	public String getMobile() {
		return mobile;
	}

	public void setMobile(String mobile) {
		this.mobile = mobile;
	}

	public String getSmsCode() {
		return smsCode;
	}

	public void setSmsCode(String smsCode) {
		this.smsCode = smsCode;
	}

	/**
	 * @return the registerToken
	 */
	public String getRegisterToken() {
		return registerToken;
	}

	/**
	 * @param registerToken the registerToken to set
	 */
	public void setRegisterToken(String registerToken) {
		this.registerToken = registerToken;
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
	 * @return the orderType
	 */
	public String getOrderType() {
		return orderType;
	}

	/**
	 * @param orderType the orderType to set
	 */
	public void setOrderType(String orderType) {
		this.orderType = orderType;
	}

	/**
	 * @return the quantity
	 */
	public String getQuantity() {
		return quantity;
	}

	/**
	 * @param quantity the quantity to set
	 */
	public void setQuantity(String quantity) {
		this.quantity = quantity;
	}

	/**
	 * @return the shopId
	 */
	public String getShopId() {
		return shopId;
	}

	/**
	 * @param shopId the shopId to set
	 */
	public void setShopId(String shopId) {
		this.shopId = shopId;
	}
	
	/**
	 * @return the productPrice
	 */
	public String getProductPrice() {
		return productPrice;
	}

	/**
	 * @param productPrice the productPrice to set
	 */
	public void setProductPrice(String productPrice) {
		this.productPrice = productPrice;
	}

	/**
	 * @return the leftMessage
	 */
	public String getLeftMessage() {
		return leftMessage;
	}

	/**
	 * @param leftMessage the leftMessage to set
	 */
	public void setLeftMessage(String leftMessage) {
		this.leftMessage = leftMessage;
	}

	/**
	 * @return the orderMap
	 */
	public Map<String,Object> getOrderMap() {
		return orderMap;
	}

	/**
	 * @param orderMap the orderMap to set
	 */
	public void setOrderMap(Map<String,Object> orderMap) {
		this.orderMap = orderMap;
	}
	
	public String getProvince() {
		return province;
	}

	public void setProvince(String province) {
		this.province = province;
	}

	public String getProvinceCode() {
		return provinceCode;
	}

	public void setProvinceCode(String provinceCode) {
		this.provinceCode = provinceCode;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public String getCityCode() {
		return cityCode;
	}

	public void setCityCode(String cityCode) {
		this.cityCode = cityCode;
	}

	public String getDistinct() {
		return distinct;
	}

	public void setDistinct(String distinct) {
		this.distinct = distinct;
	}

	public String getDistinctCode() {
		return distinctCode;
	}

	public void setDistinctCode(String distinctCode) {
		this.distinctCode = distinctCode;
	}

	/**
	 * @return the consigneeName
	 */
	public String getConsigneeName() {
		return consigneeName;
	}

	/**
	 * @param consigneeName the consigneeName to set
	 */
	public void setConsigneeName(String consigneeName) {
		this.consigneeName = consigneeName;
	}

	/**
	 * @return the addressDetail
	 */
	public String getAddressDetail() {
		return addressDetail;
	}

	/**
	 * @param addressDetail the addressDetail to set
	 */
	public void setAddressDetail(String addressDetail) {
		this.addressDetail = addressDetail;
	}

	/**
	 * @return the postCode
	 */
	public String getPostCode() {
		return postCode;
	}

	/**
	 * @param postCode the postCode to set
	 */
	public void setPostCode(String postCode) {
		this.postCode = postCode;
	}

	/**
	 * @return the cardNumber
	 */
	public String getCardNumber() {
		return cardNumber;
	}

	/**
	 * @param cardNumber the cardNumber to set
	 */
	public void setCardNumber(String cardNumber) {
		this.cardNumber = cardNumber;
	}

	/**
	 * @return the cardMobile
	 */
	public String getCardMobile() {
		return cardMobile;
	}

	/**
	 * @param cardMobile the cardMobile to set
	 */
	public void setCardMobile(String cardMobile) {
		this.cardMobile = cardMobile;
	}
	
}
