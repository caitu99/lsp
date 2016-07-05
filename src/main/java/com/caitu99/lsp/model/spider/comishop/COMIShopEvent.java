package com.caitu99.lsp.model.spider.comishop;

import com.alibaba.fastjson.JSONArray;
import com.caitu99.lsp.exception.SpiderException;
import com.caitu99.lsp.model.spider.QueryEvent;
import com.caitu99.lsp.model.spider.ccbshop.CCBShopState;

import org.springframework.web.context.request.async.DeferredResult;

public class COMIShopEvent extends QueryEvent {

    private COMIShopState state;
    private COMIShopResult comiShopResult = new COMIShopResult();
    private String lt;
    private JSONArray jsonArray;
    private String ticketUrl;
    private String ticket;
    private String loginUrl;
    private String jsecurityUrl;
    private String jauthUrl;
    private String prodId;
    private Integer count;
    private String prodName;
    private String originPrice;
    private String cashPrice;
    private Long price;
    private String param;
    private String mobile;
    private String transDataXml;
    private String sessionId;
    private String cardYear;
    private String cardMonth;
    private String cardExpire;
    private String smsCode;
    private String f15;
    private String f20;
    private String f23;
    private String batchNo;
    private String invoiceNo;
    private String orderNo;
    private String integral;
    private String name;

    public String getIntegral() {
        return integral;
    }

    public void setIntegral(String integral) {
        this.integral = integral;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getOrderNo() {
        return orderNo;
    }

    public void setOrderNo(String orderNo) {
        this.orderNo = orderNo;
    }

    public COMIShopEvent() {

    }

    public COMIShopEvent(String userid, DeferredResult<Object> deferredResult) {
        super(userid, deferredResult);
    }

    public String getInvoiceNo() {
        return invoiceNo;
    }

    public void setInvoiceNo(String invoiceNo) {
        this.invoiceNo = invoiceNo;
    }

    public String getBatchNo() {
        return batchNo;
    }

    public void setBatchNo(String batchNo) {
        this.batchNo = batchNo;
    }

    public String getF23() {
        return f23;
    }

    public void setF23(String f23) {
        this.f23 = f23;
    }

    public String getF20() {
        return f20;
    }

    public void setF20(String f20) {
        this.f20 = f20;
    }

    public String getF15() {
        return f15;
    }

    public void setF15(String f15) {
        this.f15 = f15;
    }

    public String getSmsCode() {
        return smsCode;
    }

    public void setSmsCode(String smsCode) {
        this.smsCode = smsCode;
    }

    public String getCardExpire() {
        return this.cardMonth + this.cardYear;
    }

    public String getCardMonth() {
        return cardMonth;
    }

    public void setCardMonth(String cardMonth) {
        this.cardMonth = cardMonth;
    }

    public String getCardYear() {
        return cardYear;
    }

    public void setCardYear(String cardYear) {
        this.cardYear = cardYear;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getTransDataXml() {
        return transDataXml;
    }

    public void setTransDataXml(String transDataXml) {
        this.transDataXml = transDataXml;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getParam() {
        return param;
    }

    public void setParam(String param) {
        this.param = param;
    }

    public Long getPrice() {
        return price;
    }

    public void setPrice(Long price) {
        this.price = price;
    }

    public String getCashPrice() {
        return cashPrice;
    }

    public void setCashPrice(String cashPrice) {
        this.cashPrice = cashPrice;
    }

    public String getOriginPrice() {
        return originPrice;
    }

    public void setOriginPrice(String originPrice) {
        this.originPrice = originPrice;
    }

    public String getProdName() {
        return prodName;
    }

    public void setProdName(String prodName) {
        this.prodName = prodName;
    }

    public String getProdId() {
        return prodId;
    }

    public void setProdId(String prodId) {
        this.prodId = prodId;
    }

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }

    public String getJauthUrl() {
        return jauthUrl;
    }

    public void setJauthUrl(String jauthUrl) {
        this.jauthUrl = jauthUrl;
    }

    public String getJsecurityUrl() {
        return jsecurityUrl;
    }

    public void setJsecurityUrl(String jsecurityUrl) {
        this.jsecurityUrl = jsecurityUrl;
    }

    public String getLoginUrl() {
        return loginUrl;
    }

    public void setLoginUrl(String loginUrl) {
        this.loginUrl = loginUrl;
    }

    public String getTicket() {
        return ticket;
    }

    public void setTicket(String ticket) {
        this.ticket = ticket;
    }

    public String getTicketUrl() {
        return ticketUrl;
    }

    public void setTicketUrl(String ticketUrl) {
        this.ticketUrl = ticketUrl;
    }

    public JSONArray getJsonArray() {
        return jsonArray;
    }

    public void setJsonArray(JSONArray jsonArray) {
        this.jsonArray = jsonArray;
    }

    public String getLt() {
        return lt;
    }

    public void setLt(String lt) {
        this.lt = lt;
    }

    public COMIShopState getState() {
        return state;
    }

    public void setState(COMIShopState state) {
        this.state = state;
    }

    public void setException(Exception exception) {
        this.setState(COMIShopState.ERROR);
        
		if(exception instanceof SpiderException){
	        this.exception = exception;
		}else{
			this.exception = new SpiderException(-1,"交通积分商城系统维护中,请稍后再试");
		}
    }

}
