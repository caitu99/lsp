package com.caitu99.lsp.model.spider.mailsina;

import com.alibaba.fastjson.annotation.JSONField;
import com.caitu99.lsp.model.spider.MailSpiderEvent;
import org.springframework.web.context.request.async.DeferredResult;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by Lion on 2015/11/9 0009.
 */
public class MailSinaSpiderEvent extends MailSpiderEvent {

    private String serverTime;
    private String pcid;
    private String nonce;
    private String pubkey;
    private String rsakv;
    private String is_openlock;
    private String exectime;

    private String url_step2;
    private String url_step3;
    private String url_step4;

    private String location;
    private boolean verisy;
    private MailSinaSpiderState state = MailSinaSpiderState.NONE;

    public MailSinaSpiderEvent() {

    }

    public MailSinaSpiderEvent(String sessionId, DeferredResult<Object> deferredResult, HttpServletRequest request) {
        super(sessionId, deferredResult, request);
        curPage.set(1);
    }

    public String getServerTime() {
        return serverTime;
    }

    public void setServerTime(String serverTime) {
        this.serverTime = serverTime;
    }

    public String getPcid() {
        return pcid;
    }

    public void setPcid(String pcid) {
        this.pcid = pcid;
    }

    public String getNonce() {
        return nonce;
    }

    public void setNonce(String nonce) {
        this.nonce = nonce;
    }

    public String getPubkey() {
        return pubkey;
    }

    public void setPubkey(String pubkey) {
        this.pubkey = pubkey;
    }

    public String getRsakv() {
        return rsakv;
    }

    public void setRsakv(String rsakv) {
        this.rsakv = rsakv;
    }

    public String getIs_openlock() {
        return is_openlock;
    }

    public void setIs_openlock(String is_openlock) {
        this.is_openlock = is_openlock;
    }

    public String getExectime() {
        return exectime;
    }

    public void setExectime(String exectime) {
        this.exectime = exectime;
    }

    public String getUrl_step2() {
        return url_step2;
    }

    public void setUrl_step2(String url_step2) {
        this.url_step2 = url_step2;
    }

    public String getUrl_step3() {
        return url_step3;
    }

    public void setUrl_step3(String url_step3) {
        this.url_step3 = url_step3;
    }

    public String getUrl_step4() {
        return url_step4;
    }

    public void setUrl_step4(String url_step4) {
        this.url_step4 = url_step4;
    }

    public MailSinaSpiderState getState() {
        return state;
    }

    public void setState(MailSinaSpiderState state) {
        this.state = state;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public boolean isVerisy() {
        return verisy;
    }

    public void setVerisy(boolean verisy) {
        this.verisy = verisy;
    }

    @JSONField(deserialize = false)
    public void setException(Exception exception) {
        this.setState(MailSinaSpiderState.ERROR);
        this.exception = exception;
    }
}