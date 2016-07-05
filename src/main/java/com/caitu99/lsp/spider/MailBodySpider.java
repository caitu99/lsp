/*
 * Copyright (c) 2015-2020 by caitu99
 * All rights reserved.
 */
package com.caitu99.lsp.spider;

import com.caitu99.lsp.model.spider.MailSpiderEvent;

import java.util.concurrent.LinkedBlockingDeque;

/**
 * @author yukf
 * @Description: (类职责详细描述, 可空)
 * @ClassName: MailBodySpider
 * @date 2015年10月27日 上午11:34:21
 * @Copyright (c) 2015-2020 by caitu99
 */
public abstract class MailBodySpider extends Thread {
    protected IMailSpider mailSpider;
    protected LinkedBlockingDeque<MailSpiderEvent> events = new LinkedBlockingDeque<>();

    public LinkedBlockingDeque<MailSpiderEvent> getEvents() {
        return events;
    }

    public void setEvents(LinkedBlockingDeque<MailSpiderEvent> events) {
        this.events = events;
    }

    public IMailSpider getMailSpider() {
        return mailSpider;
    }

    public void setMailSpider(IMailSpider mailSpider) {
        this.mailSpider = mailSpider;
    }
}
