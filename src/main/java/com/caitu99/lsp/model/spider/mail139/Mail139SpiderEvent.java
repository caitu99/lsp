package com.caitu99.lsp.model.spider.mail139;

import com.alibaba.fastjson.annotation.JSONField;
import com.caitu99.lsp.model.spider.MailSpiderEvent;
import org.springframework.web.context.request.async.DeferredResult;

import javax.servlet.http.HttpServletRequest;

/**
 * @author yukf
 * @date 2015年11月10日 上午11:10:23
 */
public class Mail139SpiderEvent extends MailSpiderEvent {
    private Mail139SpiderState state = Mail139SpiderState.NONE;

    private String sid;
    private String client;

    public Mail139SpiderEvent() {

    }

    public Mail139SpiderEvent(String sessionId, DeferredResult<Object> deferredResult, HttpServletRequest request) {
        super(sessionId, deferredResult, request);
    }

    public Mail139SpiderState getState() {
        return state;
    }

    public void setState(Mail139SpiderState state) {
        this.state = state;
    }

    @JSONField(deserialize = false)
    public void setException(Exception exception) {
        this.setState(Mail139SpiderState.ERROR);
        this.exception = exception;
    }

    public String getSid() {
        return sid;
    }

    public void setSid(String sid) {
        this.sid = sid;
    }

    public String getClient() {
        return client;
    }

    public void setClient(String client) {
        this.client = client;
    }


}
