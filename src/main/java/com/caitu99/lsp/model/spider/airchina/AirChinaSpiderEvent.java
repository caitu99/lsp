/*
 * Copyright (c) 2015-2020 by caitu99
 * All rights reserved.
 */
package com.caitu99.lsp.model.spider.airchina;

import com.alibaba.fastjson.annotation.JSONField;
import com.caitu99.lsp.model.spider.MailSpiderEvent;
import com.caitu99.lsp.utils.cookie.HttpCookieEx;
import org.springframework.web.context.request.async.DeferredResult;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 国航积分抓取
 *
 * @author chencheng
 * @Description: (类职责详细描述, 可空)
 * @ClassName: AirChinaSpiderEvent
 * @date 2015年11月12日 上午11:08:23
 * @Copyright (c) 2015-2020 by caitu99
 */
public class AirChinaSpiderEvent extends MailSpiderEvent {

    private String key;//密钥
    private String yzm;//验证码
    private String type;//登录类型

    private AirChinaSpiderState state = AirChinaSpiderState.NONE;

    public AirChinaSpiderEvent() {

    }

    public AirChinaSpiderEvent(String sessionId, DeferredResult<Object> deferredResult, HttpServletRequest request) {
        super(sessionId, deferredResult, request);
        List<HttpCookieEx> cookieExList = HttpCookieEx.parse("Set-Cookie:215_vq=1; path=/;");
        this.getCookieList().addAll(cookieExList);
    }


    @JSONField(deserialize = false)
    public void setException(Exception exception) {
        this.setState(AirChinaSpiderState.ERROR);
        this.exception = exception;
    }


    /**
     * @return 密钥
     */
    public String getKey() {
        return key;
    }


    /**
     * @param 密钥 to set
     */
    public void setKey(String key) {
        this.key = key;
    }


    /**
     * @return 事件状态
     */
    public AirChinaSpiderState getState() {
        return state;
    }


    /**
     * @param 事件状态 to set
     */
    public void setState(AirChinaSpiderState state) {
        this.state = state;
    }


    /**
     * @return 验证码
     */
    public String getYzm() {
        return yzm;
    }


    /**
     * @param 验证码 to set
     */
    public void setYzm(String yzm) {
        this.yzm = yzm;
    }

    /**
     * @return the type
     */
    public String getType() {
        return type;
    }

    /**
     * @param type the type to set
     */
    public void setType(String type) {
        this.type = type;
    }

}
