package com.caitu99.lsp.model.spider.mailqq;


public enum MailQQSpiderState {
    NONE,        //init
    CHECK,      // the first state
    SHOW,       // show cap page and get sig
    CAP,        // get cap code
    VFY,        // verify cap code
    NEWSIG,     // get new sig
    LOGIN,      // ready, login
    CHECKSIG,   // visit after login
    SID,        // visit to get sid
    ALONEPAGE,  // visit alonepage
    PWDALONE,   // input pwdalone if needed
    MAILLIST,   // spider maillist
    TASKQUEUE,  // add event to task
    MAIL,       // spider mail
    PARSETASKQUEUE, //add event to mail parser task
    PARSE,    // parse mail
    ERROR      // error

}
