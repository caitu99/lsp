package com.caitu99.lsp.model.spider.cmishop;

import com.caitu99.lsp.model.spider.QueryEvent;
import org.springframework.web.context.request.async.DeferredResult;

public class CMIShopEvent extends QueryEvent {

    private CMIShopState state;
    private CMIShopResult cmiShopResult = new CMIShopResult();
    private Boolean isLogin = false;
    private String smsCode;
    private String hisProductv3;
    private String dirKey;
    private String wareIds;
    private Integer amount;
    private String WanlitongAccount;
    private String curAllIntegral;

    
    
    public CMIShopResult getCmiShopResult() {
		return cmiShopResult;
	}

	public void setCmiShopResult(CMIShopResult cmiShopResult) {
		this.cmiShopResult = cmiShopResult;
	}

	public String getCurAllIntegral() {
		return curAllIntegral;
	}

	public void setCurAllIntegral(String curAllIntegral) {
		this.curAllIntegral = curAllIntegral;
	}

	public CMIShopEvent() {

    }

    public String getWanlitongAccount() {
        return WanlitongAccount;
    }

    public void setWanlitongAccount(String wanlitongAccount) {
        WanlitongAccount = wanlitongAccount;
    }

    public Integer getAmount() {
        return amount;
    }

    public void setAmount(Integer amount) {
        this.amount = amount;
    }

    public void setWareIds(String wareIds) {
        this.wareIds = wareIds;
    }

    public String getWareIds() {
        return wareIds;
    }

    public void setDirKey(String dirKey) {
        this.dirKey = dirKey;
    }

    public String getDirKey() {
        return dirKey;
    }

    public void setSmsCode(String smsCode) {
        this.smsCode = smsCode;
    }

    public String getSmsCode() {
        return smsCode;
    }

    public String getHisProductv3() {
        return hisProductv3;
    }

    public void setHisProductv3(String hisProductv3) {
        this.hisProductv3 = hisProductv3;
    }

    public CMIShopEvent(String userid, DeferredResult<Object> deferredResult) {
        super(userid, deferredResult);
    }

    public Boolean getIsLogin() {
        return isLogin;
    }

    public void setIsLogin(Boolean isLogin) {
        this.isLogin = isLogin;
    }

    public CMIShopState getState() {
        return state;
    }

    public void setState(CMIShopState state) {
        this.state = state;
    }


    public void setException(Exception exception) {
        this.setState(CMIShopState.ERROR);
        this.exception = exception;
    }

}
