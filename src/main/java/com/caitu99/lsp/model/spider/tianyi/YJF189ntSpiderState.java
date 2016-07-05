/*
 * Copyright (c) 2015-2020 by caitu99
 * All rights reserved.
 */
package com.caitu99.lsp.model.spider.tianyi;

/**
 * @author chencheng
 * @Description: (类职责详细描述, 可空)
 * @ClassName: AirChinaSpiderState
 * @date 2015年11月12日 下午12:01:39
 * @Copyright (c) 2015-2020 by caitu99
 */
public enum YJF189ntSpiderState {
    NONE,        // init
    IMG,      // 验证码获取
    LOCATION_BEFOR,	//登录页面
    LOGIN,        // 登录
    LOCATION_AFTER,	//登录
    SSO,	
    GAIN,        // 积分获取
    LOGOUT,	//登出
    RESET_PAGE,	//密码重置界面
    MSG,	//发送短信验证码
    CHECK,	//验证短信验证码
    RESET,	//重置密码
    ERROR        // 错误处理
}
