/*
 * Copyright (c) 2015-2020 by caitu99
 * All rights reserved.
 */
package com.caitu99.lsp.model.spider.liantong;

import com.alibaba.fastjson.annotation.JSONField;
import com.caitu99.lsp.model.spider.MailSpiderEvent;
import org.springframework.web.context.request.async.DeferredResult;

import javax.servlet.http.HttpServletRequest;

/**
 * 联通积分抓取
 *
 * @author chenhl
 * @Description: (类职责详细描述, 可空)
 * @ClassName: LianTongSpiderEvent
 * @date 2015年11月18日 上午11:39:52
 * @Copyright (c) 2015-2020 by caitu99
 */
public class LianTongSpiderEvent extends MailSpiderEvent {

    private String phoneno;
    private String yzm;
    private String uvc;
    private int vocdetimes;  //验证码获取的次数
    private LianTongSpiderState state = LianTongSpiderState.NONE;

    public LianTongSpiderEvent() {

    }

    public LianTongSpiderEvent(String sessionId, DeferredResult<Object> deferredResult, HttpServletRequest request) {
        super(sessionId, deferredResult, request);
    }


    @JSONField(deserialize = false)
    public void setException(Exception exception) {
        this.setState(LianTongSpiderState.ERROR);
        this.exception = exception;
    }

    /**
     * @return the state
     */
    public LianTongSpiderState getState() {
        return state;
    }

    /**
     * @param state the state to set
     */
    public void setState(LianTongSpiderState state) {
        this.state = state;
    }

    /**
     * @return the phoneno
     */
    public String getPhoneno() {
        return phoneno;
    }

    /**
     * @param phoneno the phoneno to set
     */
    public void setPhoneno(String phoneno) {
        this.phoneno = phoneno;
    }

    /**
     * @return the yzm
     */
    public String getYzm() {
        return yzm;
    }

    /**
     * @param yzm the yzm to set
     */
    public void setYzm(String yzm) {
        this.yzm = yzm;
    }

    /**
     * @return the uvc
     */
    public String getUvc() {
        return uvc;
    }

    /**
     * @param uvc the uvc to set
     */
    public void setUvc(String uvc) {
        this.uvc = uvc;
    }

    /**
     * @return the vocdetimes
     */
    public int getVocdetimes() {
        return vocdetimes;
    }

    /**
     * @param vocdetimes the vocdetimes to set
     */
    public void setVocdetimes(int vocdetimes) {
        this.vocdetimes = vocdetimes;
    }

}
