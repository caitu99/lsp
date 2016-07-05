package com.caitu99.lsp.model.spider.comishop;

public enum BocomMileageState {
    INDEX,
    KEYBOARD,
    LOGIN,
    QUERY_INTEGRAL,
    TICKET,
    CHECK,
    JAUTH,
    LOGIN1,
    JSECURITY,
    JAUTH1,
    
    CONVERT_PAGE,//兑里程页面
    REMOVE_BIND,//解除绑定
    BIND_MEMBER,//绑定
    CHECK_LIMIT,//校验兑换限制
    QUERY_CARD,//获取会员卡积分
    SEND_MSG,//发送短信验证码
    CONVERT_MILEAGE,//兑换里程
    
    ERROR
}
