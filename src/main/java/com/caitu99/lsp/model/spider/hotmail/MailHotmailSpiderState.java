package com.caitu99.lsp.model.spider.hotmail;

/**
 * Created by Lion on 2015/11/14 0014.
 */
public enum MailHotmailSpiderState {
    NONE,
    DEFAULT,
    GETLOGINURL,
    LOGIN,
    BEGINGETVERIFYCODE,
    GETVERIFYIMG,
    GOTORRU_INBOX,
    CHANGEVIEW,
    CHANGEVIEW_NEXT,
    GETNEXTPAGE,
    TASKQUEUE,
    GETMAIL,
    MAIL,
    PARSETASKQUEUE,
    PARSE,
    ERROR
}
