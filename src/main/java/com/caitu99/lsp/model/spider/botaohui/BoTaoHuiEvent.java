package com.caitu99.lsp.model.spider.botaohui;

import com.caitu99.lsp.model.spider.QueryEvent;
import org.springframework.web.context.request.async.DeferredResult;

public class BoTaoHuiEvent extends QueryEvent {

    private String appid;
    private String uid;
    private String buid;
    private String sceneid;
    private String captype;
    private String sig;

    private String realSig;

    private String ticket;

    private String ratk;
    private String redirectUrl;
    private String mainPage;
    private BoTaoHuiState state;

    public BoTaoHuiEvent() {
        super();
    }

    public BoTaoHuiEvent(String userid, DeferredResult<Object> deferredResult) {
        super(userid, deferredResult);
    }

    public BoTaoHuiState getState() {
        return state;
    }

    public void setState(BoTaoHuiState state) {
        this.state = state;
    }

    public void setException(Exception exception) {
        this.setState(BoTaoHuiState.ERROR);
        this.exception = exception;
    }

    public String getAppid() {
        return appid;
    }

    public void setAppid(String appid) {
        this.appid = appid;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getBuid() {
        return buid;
    }

    public void setBuid(String buid) {
        this.buid = buid;
    }

    public String getSceneid() {
        return sceneid;
    }

    public void setSceneid(String sceneid) {
        this.sceneid = sceneid;
    }

    public String getCaptype() {
        return captype;
    }

    public void setCaptype(String captype) {
        this.captype = captype;
    }

    public String getSig() {
        return sig;
    }

    public void setSig(String sig) {
        this.sig = sig;
    }

    public String getRealSig() {
        return realSig;
    }

    public void setRealSig(String realSig) {
        this.realSig = realSig;
    }

    public String getTicket() {
        return ticket;
    }

    public void setTicket(String ticket) {
        this.ticket = ticket;
    }

    public String getRatk() {
        return ratk;
    }

    public void setRatk(String ratk) {
        this.ratk = ratk;
    }

    public String getRedirectUrl() {
        return redirectUrl;
    }

    public void setRedirectUrl(String redirectUrl) {
        this.redirectUrl = redirectUrl;
    }

    public String getMainPage() {
        return mainPage;
    }

    public void setMainPage(String mainPage) {
        this.mainPage = mainPage;
    }


}
