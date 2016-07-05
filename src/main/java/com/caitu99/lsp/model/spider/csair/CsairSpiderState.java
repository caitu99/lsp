/*
 * Copyright (c) 2015-2020 by caitu99
 * All rights reserved.
 */
package com.caitu99.lsp.model.spider.csair;

/**
 * @author fangjunxiao
 * @Description: (类职责详细描述, 可空)
 * @ClassName: CsairSpiderState
 * @date 2015年11月18日 下午2:13:39
 * @Copyright (c) 2015-2020 by caitu99
 */
public enum CsairSpiderState {
    CHECK,      // 验证码获取
    LOGIN,        // 登录
    GETN,        //获取会员名
    GAIN,        // 积分获取
    ERROR        // 错误处理
}
