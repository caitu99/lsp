/*
 * Copyright (c) 2015-2020 by caitu99
 * All rights reserved.
 */
package com.caitu99.lsp.model.spider.liantong;


/**
 *
 * @Description: (类职责详细描述,可空)
 * @ClassName: LianTongSpiderState
 * @author chenhl
 * @date 2015年11月18日 上午11:38:00
 * @Copyright (c) 2015-2020 by caitu99
 */
public enum LianTongSpiderState {
    NONE,		 // init
    First,       // 访问主页，http://wap.10010.com/t/home.htm
    PRELOGIN,    // http://wap.10010.com/t/loginCallzz.htm?time=1447923569280
    LOGINPAGE,   // https://uac.10010.com/oauth2/loginWeb06?display=wap&page_type=05&app_code=ECS-YH-SD%20&redirect_uri=http://wap.10010.com/t/loginCallBack.htm?version=sd&state=http://wap.10010.com/t/home.htm&channel_code=113000002&real_ip=219.82.157.125 HTTP/1.1
    LOGINPOST,   // https://uac.10010.com/oauth2/loginWeb06
    HOMEPAGE,    // http://wap.10010.com/t/loginCallBack.htm?version=sd&code=vyb59m48c4db305ab5829b57dc36c71f1c23019bgdolugic
    HOMEPAGE2,   // http://wap.10010.com/t/home.htm
    TOUCHSCREEN, // http://wap.10010.com/t/versionSwitch.htm?version=sd
    QUERYPAGE,   // http://wap.10010.com/t/siteMap.htm?menuId=query
    GAIN,		 // 积分获取  http://wap.10010.com/t/points/queryPoint.htm?menuId=000200040001
    GAINSCREEN,  // 积分获取 http://wap.10010.com/t/points/queryPointFourg.htm?menuId=000200040006
    VCODEPIC,    // 获取验证码图片 https://uac.10010.com/oauth2/webSDCapcha?uvc=jeg294322308f1e264a403f1f71c84eac8cuom
    VCODELOGIN,  // 用验证码登陆
    ERROR		 // 错误处理
}
