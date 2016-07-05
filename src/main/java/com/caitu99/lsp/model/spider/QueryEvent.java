package com.caitu99.lsp.model.spider;

import com.caitu99.lsp.utils.cookie.HttpCookieEx;
import org.springframework.web.context.request.async.DeferredResult;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class QueryEvent {
    protected UUID id = UUID.randomUUID();

    protected String userid;
    protected DeferredResult<Object> deferredResult;
    protected String account;
    protected String password;
    protected String vCode;
    protected List<HttpCookieEx> cookieList = new ArrayList<>();
    protected Exception exception;

    public QueryEvent() {

    }

    public QueryEvent(String userid, DeferredResult<Object> deferredResult) {
        this.userid = userid;
        this.deferredResult = deferredResult;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getUserid() {
        return userid;
    }

    public void setUserid(String userid) {
        this.userid = userid;
    }

    public DeferredResult<Object> getDeferredResult() {
        return deferredResult;
    }

    public void setDeferredResult(DeferredResult<Object> deferredResult) {
        this.deferredResult = deferredResult;
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

    public String getvCode() {
        return vCode;
    }

    public void setvCode(String vCode) {
        this.vCode = vCode;
    }

    public List<HttpCookieEx> getCookieList() {
        return cookieList;
    }

    public void setCookieList(List<HttpCookieEx> cookieList) {
        this.cookieList = cookieList;
    }

    public Exception getException() {
        return exception;
    }
}
