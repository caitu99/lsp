/*
 * Copyright (c) 2015-2020 by caitu99
 * All rights reserved.
 */
package com.caitu99.lsp.model.spider.tianyi;

import java.util.Map;

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
public class YJF189SpiderEvent extends QueryEvent {

	private String pId;//省份id
	private String areaname;//省份名称
	private String msgCode;//短信验证码
	private String locationUrl;//重定向
	private String ssoUrl;
	private String jfUrl;
	
	private Map<String,String> paramMap;

    private YJF189SpiderState state = YJF189SpiderState.NONE;

    public YJF189SpiderEvent() {

    }

    public YJF189SpiderEvent(String userId, DeferredResult<Object> deferredResult) {
        super(userId, deferredResult);
    }


    @JSONField(deserialize = false)
    public void setException(Exception exception) {
        this.setState(YJF189SpiderState.ERROR);
        this.exception = exception;
    }

    /**
     * @return 事件状态
     */
    public YJF189SpiderState getState() {
        return state;
    }


    /**
     * @param 事件状态 to set
     */
    public void setState(YJF189SpiderState state) {
        this.state = state;
    }

	/**
	 * @return the pId
	 */
	public String getpId() {
		return pId;
	}

	/**
	 * @param pId the pId to set
	 */
	public void setpId(String pId) {
		this.pId = pId;
	}

	/**
	 * @return the areaname
	 */
	public String getAreaname() {
		return areaname;
	}

	/**
	 * @param areaname the areaname to set
	 */
	public void setAreaname(String areaname) {
		this.areaname = areaname;
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
	 * @return the locationUrl
	 */
	public String getLocationUrl() {
		return locationUrl;
	}

	/**
	 * @param locationUrl the locationUrl to set
	 */
	public void setLocationUrl(String locationUrl) {
		this.locationUrl = locationUrl;
	}

	/**
	 * @return the ssoUrl
	 */
	public String getSsoUrl() {
		return ssoUrl;
	}

	/**
	 * @param ssoUrl the ssoUrl to set
	 */
	public void setSsoUrl(String ssoUrl) {
		this.ssoUrl = ssoUrl;
	}

	/**
	 * @return the jfUrl
	 */
	public String getJfUrl() {
		return jfUrl;
	}

	/**
	 * @param jfUrl the jfUrl to set
	 */
	public void setJfUrl(String jfUrl) {
		this.jfUrl = jfUrl;
	}

	/**
	 * @return the paramMap
	 */
	public Map<String,String> getParamMap() {
		return paramMap;
	}

	/**
	 * @param paramMap the paramMap to set
	 */
	public void setParamMap(Map<String,String> paramMap) {
		this.paramMap = paramMap;
	}


}
