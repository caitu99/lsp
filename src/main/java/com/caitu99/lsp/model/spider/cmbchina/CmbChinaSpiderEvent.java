/*
 * Copyright (c) 2015-2020 by caitu99
 * All rights reserved.
 */
package com.caitu99.lsp.model.spider.cmbchina;

import com.alibaba.fastjson.annotation.JSONField;
import com.caitu99.lsp.model.spider.MailSpiderEvent;
import org.springframework.web.context.request.async.DeferredResult;

import javax.servlet.http.HttpServletRequest;

/**
 * 招行积分抓取
 *
 * @author chencheng
 * @Description: (类职责详细描述, 可空)
 * @ClassName: AirChinaSpiderEvent
 * @date 2015年11月12日 上午11:08:23
 * @Copyright (c) 2015-2020 by caitu99
 */
public class CmbChinaSpiderEvent extends MailSpiderEvent {

    private String clientNo;//客户编号
    private String yzm;//验证码
    private String type;//登录类型
    private String integral;//积分

    private CmbChinaSpiderState state = CmbChinaSpiderState.NONE;

    public CmbChinaSpiderEvent() {

    }

    public CmbChinaSpiderEvent(String sessionId, DeferredResult<Object> deferredResult, HttpServletRequest request) {
        super(sessionId, deferredResult, request);
    }


    @JSONField(deserialize = false)
    public void setException(Exception exception) {
        this.setState(CmbChinaSpiderState.ERROR);
        this.exception = exception;
    }


    /**
     * @return 事件状态
     */
    public CmbChinaSpiderState getState() {
        return state;
    }


    /**
     * @param 事件状态 to set
     */
    public void setState(CmbChinaSpiderState state) {
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

    /**
     * @return the clientNo
     */
    public String getClientNo() {
        return clientNo;
    }

    /**
     * @param clientNo the clientNo to set
     */
    public void setClientNo(String clientNo) {
        this.clientNo = clientNo;
    }

    /**
     * @return the integral
     */
    public String getIntegral() {
        return integral;
    }

    /**
     * @param integral the integral to set
     */
    public void setIntegral(String integral) {
        this.integral = integral;
    }

}
