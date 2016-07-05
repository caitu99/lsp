package com.caitu99.lsp.model.spider.unicomshop;

/**
 * Created by Administrator on 2016/1/12.
 */
public enum UnicomShopState {
    GET_LOGIN_PAGE,
    GET_LOGIN_IFRAME,
    GET_VCODE,
    GET_LOGIN_STATE,
    LOGIN,
    GET_NEW_SESSIONID,
    GET_HOME_PAGE_AFTER_LOGIN,
    ORDER,
    SEND_SMS,
    CHECK_SMS_CODE,
    CONFIRM_ORDER,
    SUBMIT_ORDER,
    GET_ORDERNO,
    RECHARGE_TO_SELF,
    OK,
    ERROR

}
