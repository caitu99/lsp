package com.caitu99.lsp.model.spider.ccbshop;

public enum CCBShopState {
	ERROR,//
	REGISTER_PAGE,//加载注册页面
	REGISTER,//注册
	CHECK_ACCOUNT,//验证账户是否存在
	CHECK_EXISTS_MOBILE,//验证手机号码是否存在
	REGISTER_VERIFY_CODE,//注册短信验证码
	CHECK_SMS_CODE,//验证短信验证码是否正确
	LOGIN_PAGE,//加载登录页面
	IMG_CODE_GET,//获取图片验证码
	IMG_CODE_CHECK,//验证图片验证码
	LOGIN,//登录
	SUBMIT_ORDER_DETAIL,//进入订单详情页
	SUBMIT_ORDER,//提交生成订单
	EPAY_MAIN_PLATGATE,//支付页面加载
	EPAY_MAIN_B1L1,//支付
	EPAY_MSG_CODE,//支付短信验证码发送
	
	
	JF_INIT,//初始化
	JF_SMS_SEND,//获取登录短信验证码
	JF_LOGIN,//登录
	JF_QUERY,//查询积分
	
	XYK_INIT,//初始化
	XYK_GETKEY,//获取登录参数
	XYK_IMG,//获取验证码
	XYK_KEY,//获取加密密钥
	XYK_LOGIN,//登录
	XYK_GOTOURL,//登录回结果
	XYK_GETJF,//获取积分

	
	
	
	GET_PROVINCE_GB_ADDRESS_JSON,//获取省JSON
	GET_CITY_GB_ADDRESS_JSON,//获取市JSON
	GET_DISTINCT_GB_ADDRESS_JSON,//获取区JSON
}
