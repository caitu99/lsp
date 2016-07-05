package com.caitu99.lsp.model.spider.botaohui;

public enum BoTaoHuiState {
    LOGINPAGE, // login page
    NLOGIN, // get img sig
    REALSIG, // to get real sig
    GETIMG, // get img
    VERIFY, // verify vcode
    SENDSMS, // send sms
    LOGIN, // do login
    AFTERLOGIN, // do after login
    USERCAP, // get main page
    ERROR // error
}
