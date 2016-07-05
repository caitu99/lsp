package com.caitu99.lsp.model.spider.mailqq;

import com.alibaba.fastjson.annotation.JSONField;
import com.caitu99.lsp.model.spider.MailSpiderEvent;
import org.springframework.web.context.request.async.DeferredResult;

import javax.servlet.http.HttpServletRequest;

public class MailQQSpiderEvent extends MailSpiderEvent {

    private String extraPwd;
    private String salt;
    private String vCode;
    private String ans;
    private String randStr;
    private String ts;
    private String alonePageUrl;
    private String sid;
    private String capCd;
    private String sig;
    private String checkSigUrl;
    private MailQQSpiderState state = MailQQSpiderState.NONE;

    public MailQQSpiderEvent() {

    }

    public MailQQSpiderEvent(String sessionId, DeferredResult<Object> deferredResult, HttpServletRequest request) {
        super(sessionId, deferredResult, request);
    }

    public String getAlonePageUrl() {
        return alonePageUrl;
    }

    public void setAlonePageUrl(String alonePageUrl) {
        this.alonePageUrl = alonePageUrl;
    }

    public String getSid() {
        return sid;
    }

    public void setSid(String sid) {
        this.sid = sid;
    }

    public String getExtraPwd() {
        return extraPwd;
    }

    public void setExtraPwd(String extraPwd) {
        this.extraPwd = extraPwd;
    }

    public String getSalt() {
        return salt;
    }

    public void setSalt(String salt) {
        this.salt = salt;
    }

    public String getvCode() {
        return vCode;
    }

    public void setvCode(String vCode) {
        this.vCode = vCode;
    }

    public String getAns() {
        return ans;
    }

    public void setAns(String ans) {
        this.ans = ans;
    }

    public String getRandStr() {
        return randStr;
    }

    public void setRandStr(String randStr) {
        this.randStr = randStr;
    }

    public String getCheckSigUrl() {
        return checkSigUrl;
    }

    public void setCheckSigUrl(String checkSigUrl) {
        this.checkSigUrl = checkSigUrl;
    }

    public String getCapCd() {
        return capCd;
    }

    public void setCapCd(String capCd) {
        this.capCd = capCd;
    }

    public String getSig() {
        return sig;
    }

    public void setSig(String sig) {
        this.sig = sig;
    }

    public String getTs() {
        return ts;
    }

    public void setTs(String ts) {
        this.ts = ts;
    }

    public MailQQSpiderState getState() {
        return state;
    }

    public void setState(MailQQSpiderState state) {
        this.state = state;
    }


    @JSONField(deserialize = false)
    public void setException(Exception exception) {
        this.setState(MailQQSpiderState.ERROR);
        this.exception = exception;
    }

}
