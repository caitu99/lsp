package com.caitu99.lsp.model.spider.unicomshop;

import com.caitu99.lsp.model.spider.QueryEvent;

import java.util.Date;

/**
 * Created by Administrator on 2016/1/12.
 */
public class UnicomShopEvent extends QueryEvent{
    private UnicomShopState state;
    private UnicomShopResult unicomShopResult = new UnicomShopResult();

    private boolean needVcode;

    private String loginIframe;

    private String productType;
    private String pwdType;
    private String redirectType;
    private String areaCode;
    private String captchaType;
    private String bizCode;
    private String rightCode;
    private String arrcity;
    private String uvc;

    private Boolean self;       //是否给自己充值

    private String indexUrl;

    private String giftId;

    private Integer giftCost;       //商品价格

    private String sms;
    private Integer nums;            //数量

    private String orderNo;

    private Date date;          //上一次请求发送短信的时间

    private String getOrderNoURl;


    public String getGetOrderNoURl() {
        return getOrderNoURl;
    }

    public void setGetOrderNoURl(String getOrderNoURl) {
        this.getOrderNoURl = getOrderNoURl;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getOrderNo() {
        return orderNo;
    }

    public void setOrderNo(String orderNo) {
        this.orderNo = orderNo;
    }

    private String chargeMySelfURL;


    public String getChargeMySelfURL() {
        return chargeMySelfURL;
    }

    public void setChargeMySelfURL(String chargeMySelfURL) {
        this.chargeMySelfURL = chargeMySelfURL;
    }

    public Integer getNums() {
        return nums;
    }

    public void setNums(Integer nums) {
        this.nums = nums;
    }

    public Boolean getSelf() {
        return self;
    }

    public void setSelf(Boolean self) {
        this.self = self;
    }

    public String getSms() {
        return sms;
    }

    public void setSms(String sms) {
        this.sms = sms;
    }

    public Integer getGiftCost() {
        return giftCost;
    }

    public void setGiftCost(Integer giftCost) {
        this.giftCost = giftCost;
    }

    public String getGiftId() {
        return giftId;
    }

    public void setGiftId(String giftId) {
        this.giftId = giftId;
    }

    public UnicomShopResult getUnicomShopResult() {
        return unicomShopResult;
    }

    public void setUnicomShopResult(UnicomShopResult unicomShopResult) {
        this.unicomShopResult = unicomShopResult;
    }

    public String getIndexUrl() {
        return indexUrl;
    }

    public void setIndexUrl(String indexUrl) {
        this.indexUrl = indexUrl;
    }

    public String getUvc() {
        return uvc;
    }

    public void setUvc(String uvc) {
        this.uvc = uvc;
    }

    public String getArrcity() {
        return arrcity;
    }

    public void setArrcity(String arrcity) {
        this.arrcity = arrcity;
    }

    public String getProductType() {
        return productType;
    }

    public void setProductType(String productType) {
        this.productType = productType;
    }

    public String getPwdType() {
        return pwdType;
    }

    public void setPwdType(String pwdType) {
        this.pwdType = pwdType;
    }

    public String getRedirectType() {
        return redirectType;
    }

    public void setRedirectType(String redirectType) {
        this.redirectType = redirectType;
    }

    public String getAreaCode() {
        return areaCode;
    }

    public void setAreaCode(String areaCode) {
        this.areaCode = areaCode;
    }

    public String getCaptchaType() {
        return captchaType;
    }

    public void setCaptchaType(String captchaType) {
        this.captchaType = captchaType;
    }

    public String getBizCode() {
        return bizCode;
    }

    public void setBizCode(String bizCode) {
        this.bizCode = bizCode;
    }

    public String getRightCode() {
        return rightCode;
    }

    public void setRightCode(String rightCode) {
        this.rightCode = rightCode;
    }

    public String getLoginIframe() {
        return loginIframe;
    }

    public void setLoginIframe(String loginIframe) {
        this.loginIframe = loginIframe;
    }

    public boolean isNeedVcode() {
        return needVcode;
    }

    public void setNeedVcode(boolean needVcode) {
        this.needVcode = needVcode;
    }

    public UnicomShopState getState() {
        return state;
    }

    public void setState(UnicomShopState state) {
        this.state = state;
    }

    public void setException(Exception exception) {
        this.setState(UnicomShopState.ERROR);
        this.exception = exception;
    }
}
