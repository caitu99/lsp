/*
 * Copyright (c) 2015-2020 by caitu99
 * All rights reserved.
 */
package com.caitu99.lsp.model.spider.airchina;

/**
 * @author chencheng
 * @Description: (类职责详细描述, 可空)
 * @ClassName: AirChinaSpiderState
 * @date 2015年11月12日 下午12:01:39
 * @Copyright (c) 2015-2020 by caitu99
 */
public enum AirChinaSpiderState {
    NONE,        // init
    CHECK,      // 验证码获取
    KEY,        // 密钥获取
    LOGIN,        // 登录
    GAIN,        // 积分获取
    ERROR        // 错误处理
}
