package com.caitu99.lsp.model.spider.cmishop;

public enum CMIShopState {
	INIT,//init
    LOGINPAGE, // login page
  //  VCODE,//vcode
    
    SMSLOGIN,//sms login
    
    LOGIN, // do login
    HOMEPAGE, // get home page
    SMS,//sms
    ORDERDETAIL,//orderDetail
    ORDER,//order
    ERROR // error
}
