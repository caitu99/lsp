/*
 * Copyright (c) 2015-2020 by caitu99
 * All rights reserved.
 */
package com.caitu99.lsp.model.spider.taobao;

;

/**
 * @author Hongbo Peng
 * @Description: (类职责详细描述, 可空)
 * @ClassName: TaoBaoNewSpiderState
 * @date 2015年11月18日 上午9:31:01
 * @Copyright (c) 2015-2020 by caitu99
 */
public enum TaoBaoSpiderState {
    NONE,                //初始
    CHECK,                //获取验证码
    LOGIN,                //登录
    LOGIN302,            //登录请求后跳转
    VCODE,                //短信验证码验证
    VCODE302,            //发送短信验证码后跳转页面
    RECHECK,            //二次验证
    RECHECK302,            //二次验证提交跳转
    TMALL,                //获取天猫积分
    TAOJINBI,            //获取淘金币
    EXPIREDTAOJINBI,    //获取即将过期的淘金币和过期时间
    TAOLICHEN,            //获取淘里程积分
    ERROR                //错误处理
}
