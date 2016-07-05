/*
 * Copyright (c) 2015-2020 by caitu99
 * All rights reserved.
 */
package com.caitu99.lsp.model.spider.tianyi;

/**
 * 天翼积分商城
 * 
 * @Description: (类职责详细描述,可空) 
 * @ClassName: TianYiShopSpiderState 
 * @author ws
 * @date 2016年2月4日 上午11:52:48 
 * @Copyright (c) 2015-2020 by caitu99
 */
public enum TianYiShopSpiderState {
    NONE,        // init
    IMG,      // 验证码获取
    CHECK,        // 验证验证码
    MSG,        //下发短信
    LOGIN,        // 登录
    ORDER,        // 创建订单
    MSG_PAY,	//支付短信验证码
    PAY,        // 支付订单
    GET_JF,		//获取用户积分信息
    GET_CODE,   //获取用户订单兑换码
    ERROR        // 错误处理
}
