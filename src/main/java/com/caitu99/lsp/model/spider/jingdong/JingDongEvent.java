package com.caitu99.lsp.model.spider.jingdong;

import com.caitu99.lsp.model.spider.QueryEvent;
import org.springframework.web.context.request.async.DeferredResult;

public class JingDongEvent extends QueryEvent {

    private String loginPage;
    private String rsaValue;
    private String stoken;
    private String succb;
    private String indexLoc;
    private String goOpenUrl;
    private String fanliyunUrl;
    private String unionUrl;
    private String unionSecUrl;
    private String mainPageUrl;
    private String sid;
    private JingDongState state;

    public JingDongEvent() {

    }

    public JingDongEvent(String userid, DeferredResult<Object> deferredResult) {
        super(userid, deferredResult);
    }

    public JingDongState getState() {
        return state;
    }

    public void setState(JingDongState state) {
        this.state = state;
    }

    public void setException(Exception exception) {
        this.setState(JingDongState.ERROR);
        this.exception = exception;
    }

    public String getLoginPage() {
        return loginPage;
    }

    public void setLoginPage(String loginPage) {
        this.loginPage = loginPage;
    }

    public String getRsaValue() {
        return rsaValue;
    }

    public void setRsaValue(String rsaValue) {
        this.rsaValue = rsaValue;
    }

    public String getStoken() {
        return stoken;
    }

    public void setStoken(String stoken) {
        this.stoken = stoken;
    }

    public String getSuccb() {
        return succb;
    }

    public void setSuccb(String succb) {
        this.succb = succb;
    }

    public String getIndexLoc() {
        return indexLoc;
    }

    public void setIndexLoc(String indexLoc) {
        this.indexLoc = indexLoc;
    }

    public String getGoOpenUrl() {
        return goOpenUrl;
    }

    public void setGoOpenUrl(String goOpenUrl) {
        this.goOpenUrl = goOpenUrl;
    }

    public String getFanliyunUrl() {
        return fanliyunUrl;
    }

    public void setFanliyunUrl(String fanliyunUrl) {
        this.fanliyunUrl = fanliyunUrl;
    }

    public String getUnionUrl() {
        return unionUrl;
    }

    public void setUnionUrl(String unionUrl) {
        this.unionUrl = unionUrl;
    }

    public String getUnionSecUrl() {
        return unionSecUrl;
    }

    public void setUnionSecUrl(String unionSecUrl) {
        this.unionSecUrl = unionSecUrl;
    }

    public String getMainPageUrl() {
        return mainPageUrl;
    }

    public void setMainPageUrl(String mainPageUrl) {
        this.mainPageUrl = mainPageUrl;
    }

    public String getSid() {
        return sid;
    }

    public void setSid(String sid) {
        this.sid = sid;
    }


}
