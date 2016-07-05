/*
 * Copyright (c) 2015-2020 by caitu99
 * All rights reserved.
 */
package com.caitu99.lsp.model.spider.pingan;

/** 
 * 
 * @Description: (类职责详细描述,可空) 
 * @ClassName: PingAnSpider 
 * @author ws
 * @date 2016年4月1日 上午14:42:23 
 * @Copyright (c) 2015-2020 by caitu99 
 */
public enum PingAnOilSpiderState {
	
	INIT_SP,
	LOGIN_INIT_SP,
	IMG_SP,
	
	LOGIN_SP,
	TO_AUTH_SP,
	SSOLOGIN_SP,
	
	OIL_PAGE,//兑油卡页面
	OIL_VCODE,//图形验证码
	OIL_VERIFY,//验证图形验证码
	OIL_ORDER,//兑油卡下单
	OIL_PAY_PAGE,//兑油卡支付页面
	OIL_BEFOR_PAY,//兑油卡开始支付
	OIL_MSG,	//支付短信验证码
	OIL_SUBMIT_PAY,//兑油卡提交支付
	OIL_SUC_PAY,//兑油卡确认支付

	ERROR	//
}
