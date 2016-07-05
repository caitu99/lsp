/*
 * Copyright (c) 2015-2020 by caitu99
 * All rights reserved.
 */
package com.caitu99.lsp.model.spider;

import com.alibaba.fastjson.annotation.JSONField;
import com.caitu99.lsp.perf.TicTac;
import com.caitu99.lsp.utils.cookie.HttpCookieEx;
import org.springframework.web.context.request.async.DeferredResult;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author yukf
 * @Description: (类职责详细描述, 可空)
 * @ClassName: MailSpiderEvent
 * @date 2015年10月26日 下午5:39:37
 * @Copyright (c) 2015-2020 by caitu99
 */
public class MailSpiderEvent {

    protected UUID id = UUID.randomUUID();

    protected String userid;
    protected DeferredResult<Object> deferredResult;
    protected HttpServletRequest request;
    protected String account;
    protected String password;
    protected String vCode;
    protected String needCode;
    protected AtomicInteger curPage = new AtomicInteger(-1);
    protected int maxPage;
    protected int totalMail;
    protected long date;

    protected TicTac ticTac = new TicTac();
    protected AtomicInteger mailDone = new AtomicInteger(0);
    protected List<HttpCookieEx> cookieList = new ArrayList<>();

    protected ConcurrentLinkedDeque<Envelope> envelopes = new ConcurrentLinkedDeque<>();
    protected ConcurrentLinkedQueue<MailSrc> mailSrcs = new ConcurrentLinkedQueue<>();

    protected long lastGetTime = 0l;
    protected Exception exception;

    public MailSpiderEvent() {

    }

    public MailSpiderEvent(String userid, DeferredResult<Object> deferredResult, HttpServletRequest request) {
        this.userid = userid;
        this.deferredResult = deferredResult;
        this.request = request;
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

    public void setUserid(String sessionId) {
        this.userid = sessionId;
    }

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }

    @JSONField(serialize = false)
    public ConcurrentLinkedQueue<MailSrc> getMailSrcs() {
        return mailSrcs;
    }

    @JSONField(deserialize = false)
    public void setMailSrcs(ConcurrentLinkedQueue<MailSrc> mailSrcs) {
        this.mailSrcs = mailSrcs;
    }

    public int getTotalMail() {
        return totalMail;
    }

    public void setTotalMail(int totalMail) {
        this.totalMail = totalMail;
    }

    @JSONField(serialize = false)
    public int getMaxPage() {
        return maxPage;
    }

    @JSONField(deserialize = false)
    public void setMaxPage(int maxPage) {
        this.maxPage = maxPage;
    }

    @JSONField(serialize = false)
    public ConcurrentLinkedDeque<Envelope> getEnvelopes() {
        return envelopes;
    }

    @JSONField(deserialize = false)
    public void setEnvelopes(ConcurrentLinkedDeque<Envelope> envelopes) {
        this.envelopes = envelopes;
    }

    @JSONField(serialize = false)
    public AtomicInteger getCurPage() {
        return curPage;
    }

    @JSONField(deserialize = false)
    public void setCurPage(AtomicInteger curPage) {
        this.curPage = curPage;
    }

    @JSONField(serialize = false)
    public DeferredResult<Object> getDeferredResult() {
        return deferredResult;
    }

    @JSONField(deserialize = false)
    public void setDeferredResult(DeferredResult<Object> deferredResult) {
        this.deferredResult = deferredResult;
    }

    @JSONField(serialize = false)
    public HttpServletRequest getRequest() {
        return request;
    }

    @JSONField(deserialize = false)
    public void setRequest(HttpServletRequest request) {
        this.request = request;
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

    @JSONField(serialize = false)
    public String getvCode() {
        return vCode;
    }

    @JSONField(deserialize = false)
    public void setvCode(String vCode) {
        this.vCode = vCode;
    }

    @JSONField(serialize = false)
    public long getLastGetTime() {
        return lastGetTime;
    }

    @JSONField(deserialize = false)
    public void setLastGetTime(long lastGetTime) {
        this.lastGetTime = lastGetTime;
    }

    @JSONField(serialize = false)
    public TicTac getTicTac() {
        return ticTac;
    }

    @JSONField(deserialize = false)
    public void setTicTac(TicTac ticTac) {
        this.ticTac = ticTac;
    }

    @JSONField(serialize = false)
    public AtomicInteger getMailDone() {
        return mailDone;
    }

    @JSONField(deserialize = false)
    public void setMailDone(AtomicInteger mailDone) {
        this.mailDone = mailDone;
    }

    public List<HttpCookieEx> getCookieList() {
        return cookieList;
    }

    public void setCookieList(List<HttpCookieEx> cookieList) {
        this.cookieList = cookieList;
    }

    public String getNeedCode() {
        return needCode;
    }

    public void setNeedCode(String needCode) {
        this.needCode = needCode;
    }

    @JSONField(serialize = false)
    public Exception getException() {
        return exception;
    }
}
