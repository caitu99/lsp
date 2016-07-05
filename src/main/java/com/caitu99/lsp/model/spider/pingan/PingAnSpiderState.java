/*
 * Copyright (c) 2015-2020 by caitu99
 * All rights reserved.
 */
package com.caitu99.lsp.model.spider.pingan;

/** 
 * 
 * @Description: (类职责详细描述,可空) 
 * @ClassName: PingAnSpiderState 
 * @author fangjunxiao
 * @date 2016年3月30日 上午11:28:20 
 * @Copyright (c) 2015-2020 by caitu99 
 */
public enum PingAnSpiderState {

	
	ERROR,//
	
	INIT_SP,
	LOGIN_INIT_SP,
	IMG_SP,
	
	LOGIN_SP,
	TO_AUTH_SP,
	SSOLOGIN_SP,
	
	GOOD_INIT_SP,
	GET_MEMBER_INIT_SP,
	GET_INFO_SP,
	LOGIN_AUTH_SP,
	LOGIN_SUCCESS_SP,
	GET_MEMBER_SP,
	
	BUY_CONFIRM_SP,
	COMMIT_ORDER_SP,
	PAYMENT_SP,
	UM_LOGIN_SP,
	FIRST_SP,
	FIRST_DIR_SP,
	TO_PAY_MENT_SP,
	
	
	SMS_CODE_SP,
	
	PAY_YM_SP,
	
	CONTINUE_PAY_SP,
	
	GET_ADDRESS,
	ADD_ADDRESS,
	
	
	
	VCODEWEB,
	VALIDATE_VCODE,
	TORESETPWD,
	TOCOMMITMEMBER,
	
	REGVCODE,
	SEND_CODE,
	CHECK_CODE,
	RESET_THREE,
	CHECK_PASSWORD,
	
	UPDATE_PWD,
	
	
	
}
