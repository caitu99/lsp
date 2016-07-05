/*
 * Copyright (c) 2015-2020 by caitu99
 * All rights reserved.
 */
package com.caitu99.lsp.model.spider.mail163;

/**
 * @author yukf
 * @Description: (类职责详细描述, 可空)
 * @ClassName: Mail163SpiderState
 * @date 2015年10月27日 下午4:15:43
 * @Copyright (c) 2015-2020 by caitu99
 */
public enum Mail163SpiderState {
    NONE, // init
    LOGIN, // ready, login
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
