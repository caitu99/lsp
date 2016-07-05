/*
 * Copyright (c) 2015-2020 by caitu99
 * All rights reserved.
 */
package com.caitu99.lsp.model.spider.csair;

import com.alibaba.fastjson.annotation.JSONField;
import com.caitu99.lsp.model.spider.MailSpiderEvent;
import org.springframework.web.context.request.async.DeferredResult;

import javax.servlet.http.HttpServletRequest;

/**
 * @author fangjunxiao
 * @Description: (类职责详细描述, 可空)
 * @ClassName: CsairSpiderEvent
 * @date 2015年11月18日 下午2:14:42
 * @Copyright (c) 2015-2020 by caitu99
 */
public class CsairSpiderEvent extends MailSpiderEvent {


    private String validator;//验证码
    private String type;//登录类型
    private String inCode;
    private String name;


    private CsairSpiderState state = CsairSpiderState.CHECK;


    public CsairSpiderEvent() {

    }

    public CsairSpiderEvent(String userId, DeferredResult<Object> deferredResult, HttpServletRequest request) {
        super(userId, deferredResult, request);
    }

    @JSONField(deserialize = false)
    public void setException(Exception exception) {
        this.setState(CsairSpiderState.ERROR);
        this.exception = exception;
    }


    /**
     * @return the state事件状态
     */
    public CsairSpiderState getState() {
        return state;
    }

    /**
     * @param state the 事件状态 to set
     */
    public void setState(CsairSpiderState state) {
        this.state = state;
    }

    /**
     * @return the inCode
     */
    public String getInCode() {
        return inCode;
    }

    /**
     * @param inCode the inCode to set
     */
    public void setInCode(String inCode) {
        this.inCode = inCode;
    }

    /**
     * @return the 验证码
     */
    public String getValidator() {
        return validator;
    }

    /**
     * @param validator the 验证码 to set
     */
    public void setValidator(String validator) {
        this.validator = validator;
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
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }


}
