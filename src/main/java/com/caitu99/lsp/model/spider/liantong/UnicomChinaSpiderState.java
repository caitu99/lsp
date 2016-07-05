/*
 * Copyright (c) 2015-2020 by caitu99
 * All rights reserved.
 */
package com.caitu99.lsp.model.spider.liantong;


/**
 * 
 * 
 * @Description: (类职责详细描述,可空) 
 * @ClassName: UnicomChinaSpiderState 
 * @author ws
 * @date 2016年3月22日 上午10:43:40 
 * @Copyright (c) 2015-2020 by caitu99
 */
public enum UnicomChinaSpiderState {
    NONE,		 // init
    PAGE_CLICK,      //
    IMG,    		//图形验证码获取
    IMG_CHECK,		//验证图形验证码
    LOGIN_PAGE,   	//登录页面加载
    LOGIN,   	//登录
    INTEGRAL,    //抓取积分
    ERROR		 // 错误处理
}
