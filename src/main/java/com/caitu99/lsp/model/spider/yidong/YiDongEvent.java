package com.caitu99.lsp.model.spider.yidong;

import com.caitu99.lsp.model.spider.QueryEvent;
import org.springframework.web.context.request.async.DeferredResult;

public class YiDongEvent extends QueryEvent {
    private String accountType;
    private String account;
    private String password;
    private String pwdType;
    private String inputCode;
    private String backUrl;
    private String rememberMe;
    private String channelID;
    private String protocol;
    private String succb;
    private YiDongState state;
    private String passwordType;  //密码类型：随机短信密码(sms) 服务密码(service)

    public String getPasswordType() {
        return passwordType;
    }

    public void setPasswordType(String passwordType) {
        this.passwordType = passwordType;
    }

    public YiDongEvent() {

    }

    public YiDongEvent(String userid, DeferredResult<Object> deferredResult) {
        super(userid, deferredResult);
    }

    public YiDongState getState() {
        return state;
    }

    public void setState(YiDongState state) {
        this.state = state;
    }

    public void setException(Exception exception) {
        this.setState(YiDongState.ERROR);
        this.exception = exception;
    }

    public String getAccountType() {
        return accountType;
    }

    public void setAccountType(String accountType) {
        this.accountType = accountType;
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

    public String getPwdType() {
        return pwdType;
    }

    public void setPwdType(String pwdType) {
        this.pwdType = pwdType;
    }

    public String getInputCode() {
        return inputCode;
    }

    public void setInputCode(String inputCode) {
        this.inputCode = inputCode;
    }

    public String getBackUrl() {
        return backUrl;
    }

    public void setBackUrl(String backUrl) {
        this.backUrl = backUrl;
    }

    public String getRememberMe() {
        return rememberMe;
    }

    public void setRememberMe(String rememberMe) {
        this.rememberMe = rememberMe;
    }

    public String getChannelID() {
        return channelID;
    }

    public void setChannelID(String channelID) {
        this.channelID = channelID;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public String getSuccb() {
        return succb;
    }

    public void setSuccb(String succb) {
        this.succb = succb;
    }


}
