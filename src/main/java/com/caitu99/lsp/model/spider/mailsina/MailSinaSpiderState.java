package com.caitu99.lsp.model.spider.mailsina;

/**
 * Created by Lion on 2015/11/10 0010.
 */
public enum MailSinaSpiderState {
    NONE,       //init
    PRE_LOGIN,  //pre login
    LOGIN_STEP1,        //first
    GETVERIFY,          //get verify code
    LOGIN_SETP2,        //
    LOGIN_SETP3,
    LOGIN_SETP4,
    CGI_SLA,
    SENDSID,
    CLASSICINDEX,
    GETMAIL_LIST,
    TASKQUEUE,
    MAIL,
    PARSETASKQUEUE,
    PARSE,
    ERROR

}
