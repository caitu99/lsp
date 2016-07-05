package com.caitu99.lsp.model.spider.jingdong;

public enum JingDongState {
    NONE, // init
    PRE_LOGINPAGE, // get pre login page
    PASS_PORT,
    LOGIN_PAGE,
    LOGIN, // ready, login
    SUCCCB, // login succcb
    INDEXLOC, // indexloc step
    GO_OPEN, // go open url step
    FANLIYUN, // go www.fanliyun.cn
    UNION, // go union.clock.jd.com
    UNION_SEC, // go union.clock.jd.com again
    MAIN_PAGE, // go jd main page
    GETDOU, // go get jingdou page
    RELOGIN,  // relogin because of vcode
    GETID, // get id
    GETIMG, // get vcode img
    VFY,   // vfy vcode
    NEXT,  // visit next url
    ENTRY,  // visit entry url
    MAILLIST, // spider maillist
    TASKQUEUE, // add event to task
    MAIL, // spider mail
    PARSETASKQUEUE, // add event to mail parser task
    PARSE, // parse mail
    ERROR // error
}
