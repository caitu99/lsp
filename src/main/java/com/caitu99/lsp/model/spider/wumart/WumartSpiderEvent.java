/*
 * Copyright (c) 2015-2020 by caitu99
 * All rights reserved.
 */
package com.caitu99.lsp.model.spider.wumart;

import javax.servlet.http.HttpServletRequest;

import com.caitu99.lsp.model.spider.MailSpiderEvent;
import org.springframework.web.context.request.async.DeferredResult;

import com.alibaba.fastjson.annotation.JSONField;

/**
 * 
 * @Description: (类职责详细描述,可空)
 * @ClassName: WumartSpiderEvent
 * @author fangjunxiao
 * @date 2015年12月11日 下午2:47:37
 * @Copyright (c) 2015-2020 by caitu99
 */
public class WumartSpiderEvent extends MailSpiderEvent {
	
	
	private String vcode;
	
	private String vcodes;
	
	private String province;
	

	private WumartSpiderState state = WumartSpiderState.IMGCODE;

	/**
	 * @Title:
	 * @Description:
	 */
	public WumartSpiderEvent() {
		super();
	}

	/**
	 * @Title:
	 * @Description:
	 * @param userid
	 * @param deferredResult
	 * @param request
	 */
	public WumartSpiderEvent(String userid,
			DeferredResult<Object> deferredResult, HttpServletRequest request) {
		super(userid, deferredResult, request);
	}

	@JSONField(deserialize = false)
	public void setException(Exception exception) {
		this.setState(WumartSpiderState.ERROR);
		this.exception = exception;
	}
	
	
	public WumartSpiderState getState() {
		return state;
	}

	public void setState(WumartSpiderState state) {
		this.state = state;
	}

	public String getVcode() {
		return vcode;
	}

	public void setVcode(String vcode) {
		this.vcode = vcode;
	}

	public String getProvince() {
		return province;
	}

	public void setProvince(String province) {
		this.province = province;
	}


	public String getVcodes() {
		return vcodes;
	}

	public void setVcodes(String vcodes) {
		this.vcodes = vcodes;
	}
	
	
}
