package com.caitu99.lsp.model.spider.ccbishop;

public enum CCBIShopState {
    ISLOGIN,//isLogin
    ISLOGINCPP,//isLoginCpp
    INDEX, // index page
    GETJSESSIONID,//getJSessionid
    LOGINPAGE, // loginPage
    VCODE, // get vcode
    LOGIN, //login
    LOGIN_JF,//login for jf
    QUERY_JF,//query for jf
    SMS,//sms
    CHECK,//order
    ESHOP,//eshop
    ESHOPLOGIN,//eshopLogin
    GETINTEGRAL,//getIntegral
    CLEAR_CART,
    ADDCART,//addCart
    QUERYCART,//queryCart
    QUERYCART1,//queryCart1
    INSERTADDR,// insert new address
    QUERYADDR,// query address
    BUYCONFIRM,//buyConfirm
    QUERYCARD,//queryCard
    ORDERVCODE,//orderVcode
    ORDER,//order
    DELCART,//delCart
    DELADDR,// delete address
    ERROR // error
}
