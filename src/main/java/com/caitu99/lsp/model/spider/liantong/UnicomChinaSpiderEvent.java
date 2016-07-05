/*
 * Copyright (c) 2015-2020 by caitu99
 * All rights reserved.
 */
package com.caitu99.lsp.model.spider.liantong;

import java.util.Map;

import org.springframework.web.context.request.async.DeferredResult;

import com.alibaba.fastjson.annotation.JSONField;
import com.caitu99.lsp.model.spider.QueryEvent;

/**
 * 
 * 
 * @Description: (类职责详细描述,可空) 
 * @ClassName: UnicomChinaSpiderEvent 
 * @author ws
 * @date 2016年3月22日 上午10:43:49 
 * @Copyright (c) 2015-2020 by caitu99
 */
public class UnicomChinaSpiderEvent extends QueryEvent {

	private String loginPageUrl;
	private Map<String, String> loginMap;
	private String uacverifykey;
    private UnicomChinaSpiderState state = UnicomChinaSpiderState.NONE;

    public UnicomChinaSpiderEvent() {

    }

    public UnicomChinaSpiderEvent(String userid, DeferredResult<Object> deferredResult) {
    	super(userid, deferredResult);
    }


    @JSONField(deserialize = false)
    public void setException(Exception exception) {
        this.setState(UnicomChinaSpiderState.ERROR);
        this.exception = exception;
    }

    /**
     * @return the state
     */
    public UnicomChinaSpiderState getState() {
        return state;
    }

    /**
     * @param state the state to set
     */
    public void setState(UnicomChinaSpiderState state) {
        this.state = state;
    }

	/**
	 * @return the loginPageUrl
	 */
	public String getLoginPageUrl() {
		return loginPageUrl;
	}

	/**
	 * @param loginPageUrl the loginPageUrl to set
	 */
	public void setLoginPageUrl(String loginPageUrl) {
		this.loginPageUrl = loginPageUrl;
	}

	/**
	 * @return the loginMap
	 */
	public Map<String, String> getLoginMap() {
		return loginMap;
	}

	/**
	 * @param loginMap the loginMap to set
	 */
	public void setLoginMap(Map<String, String> loginMap) {
		this.loginMap = loginMap;
	}

	/**
	 * @return the uacverifykey
	 */
	public String getUacverifykey() {
		return uacverifykey;
	}

	/**
	 * @param uacverifykey the uacverifykey to set
	 */
	public void setUacverifykey(String uacverifykey) {
		this.uacverifykey = uacverifykey;
	}


}
