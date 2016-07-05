/*
 * Copyright (c) 2015-2020 by caitu99
 * All rights reserved.
 */
package com.caitu99.lsp.model.spider.tianyi;

import com.alibaba.fastjson.annotation.JSONField;
import com.caitu99.lsp.model.spider.QueryEvent;
import org.springframework.web.context.request.async.DeferredResult;

/**
 * 天翼积分抓取
 *
 * @author chencheng
 * @Description: (类职责详细描述, 可空)
 * @ClassName: TianYiSpiderEvent
 * @date 2015年11月12日 上午11:08:23
 * @Copyright (c) 2015-2020 by caitu99
 */
public class TianYiSpiderEvent extends QueryEvent {

    private String msgCode;//短信验证码
    private String custName;//用户名称

    private TianYiSpiderState state = TianYiSpiderState.NONE;

    public TianYiSpiderEvent() {

    }

    public TianYiSpiderEvent(String userId, DeferredResult<Object> deferredResult) {
        super(userId, deferredResult);
    }


    @JSONField(deserialize = false)
    public void setException(Exception exception) {
        this.setState(TianYiSpiderState.ERROR);
        this.exception = exception;
    }

    /**
     * @return 事件状态
     */
    public TianYiSpiderState getState() {
        return state;
    }


    /**
     * @param 事件状态 to set
     */
    public void setState(TianYiSpiderState state) {
        this.state = state;
    }

    /**
     * @return the msgCode
     */
    public String getMsgCode() {
        return msgCode;
    }

    /**
     * @param msgCode the msgCode to set
     */
    public void setMsgCode(String msgCode) {
        this.msgCode = msgCode;
    }

    /**
     * @return the custName
     */
    public String getCustName() {
        return custName;
    }

    /**
     * @param custName the custName to set
     */
    public void setCustName(String custName) {
        this.custName = custName;
    }

}
