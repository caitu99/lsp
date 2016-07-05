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
public enum YJF189SpiderState {
    NONE,        // init
    IMG,      // 验证码获取
    LOGIN_PAGE,	//登录页面
    USERINFO,	//获取用户信息
    LOGIN,        // 登录
    LOCATION,	//登录
    SSO,	
    GAIN,        // 积分获取
    LOGOUT,	//登出
    ERROR        // 错误处理
}
