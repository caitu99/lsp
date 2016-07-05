/*
 * Copyright (c) 2015-2020 by caitu99
 * All rights reserved.
 */
package com.caitu99.lsp.model.spider.wumart;

/** 
 * 
 * @Description: (类职责详细描述,可空) 
 * @ClassName: WumartSpiderState 
 * @author fangjunxiao
 * @date 2015年12月11日 下午3:12:17 
 * @Copyright (c) 2015-2020 by caitu99 
 */
public enum WumartSpiderState {
	
	IMGCODE,	// 验证码获取
    LOGIN,		// 登录
    GETN,		//积分获取
    CHECK,      //校验验证码
    GETSMS,		//获取短信验证码
    MODIFY,		//注册或修改
    ERROR,		// 错误处理
    
	

}
