package com.caitu99.lsp.model.spider.pufabank;

public enum PufaBankState {
    LOGINPAGE, // login page
    GETIMG, // get img
    LOGIN, // do login
    VERIFY, // verify vcode
    USERCAP, // get main page
    ERROR // error
}
