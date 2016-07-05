/*
 * Copyright (c) 2015-2020 by caitu99
 * All rights reserved.
 */
package com.caitu99.lsp.model.spider.pingan;

import org.springframework.web.context.request.async.DeferredResult;

import com.alibaba.fastjson.annotation.JSONField;
import com.caitu99.lsp.model.spider.QueryEvent;

/** 
 * 
 * @Description: (类职责详细描述,可空) 
 * @ClassName: PingAnxykSpiderEvent 
 * @author fangjunxiao
 * @date 2016年4月11日 下午4:40:50 
 * @Copyright (c) 2015-2020 by caitu99 
 */
public class PingAnxykSpiderEvent extends QueryEvent {

	private PingAnxykSpiderState state;
	
	private String blackBox;
	
	private String directToMenu;
	
	private String userName;
	
	private String maskMasterCardNo;
	
	private String redirectURL;
	
	
	private String key;
	private String hostId;
	private String toaType;
	
	
	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getHostId() {
		return hostId;
	}

	public void setHostId(String hostId) {
		this.hostId = hostId;
	}

	public String getToaType() {
		return toaType;
	}

	public void setToaType(String toaType) {
		this.toaType = toaType;
	}

	public String getRedirectURL() {
		return redirectURL;
	}

	public void setRedirectURL(String redirectURL) {
		this.redirectURL = redirectURL;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getMaskMasterCardNo() {
		return maskMasterCardNo;
	}

	public void setMaskMasterCardNo(String maskMasterCardNo) {
		this.maskMasterCardNo = maskMasterCardNo;
	}

	public String getDirectToMenu() {
		return directToMenu;
	}

	public void setDirectToMenu(String directToMenu) {
		this.directToMenu = directToMenu;
	}

	public String getBlackBox() {
		return blackBox;
	}

	public void setBlackBox(String blackBox) {
		this.blackBox = blackBox;
	}

	public PingAnxykSpiderEvent(String sessionId, DeferredResult<Object> deferredResult) {
		super(sessionId, deferredResult);
        
	}
	
	public PingAnxykSpiderEvent() {
		super();
	}

	@JSONField(deserialize = false)
    public void setException(Exception exception) {
        this.setState(PingAnxykSpiderState.ERROR);
        this.exception = exception;
    }

	public PingAnxykSpiderState getState() {
		return state;
	}

	public void setState(PingAnxykSpiderState state) {
		this.state = state;
	}
	
}
