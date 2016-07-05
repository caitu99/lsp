/*
 * Copyright (c) 2015-2020 by caitu99
 * All rights reserved.
 */
package com.caitu99.lsp.spider;

import com.caitu99.lsp.model.spider.MailSpiderEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.LinkedBlockingDeque;

/**
 * mail parser
 *
 * @author yukf
 * @Description: (类职责详细描述, 可空)
 * @ClassName: MailParser
 * @date 2015年10月24日 下午2:29:05
 * @Copyright (c) 2015-2020 by caitu99
 */
public class MailParserTask extends Thread {
    private final static Logger logger = LoggerFactory.getLogger(MailParserTask.class);
    private IMailSpider mailSpider;
    private LinkedBlockingDeque<MailSpiderEvent> events = new LinkedBlockingDeque<>();

    /* (non-Javadoc)
     * @see java.lang.Thread#run()
     */
    @Override
    public void run() {
        while (!isInterrupted()) {
            try {
                MailSpiderEvent event = events.take();
                logger.debug("mail parser do polling...");
                mailSpider.onEvent(event);
            } catch (InterruptedException e) {
                logger.error("mail parse thread is interrupted");
            }
        }
    }

    public IMailSpider getMailSpider() {
        return mailSpider;
    }

    public void setMailSpider(IMailSpider mailSpider) {
        this.mailSpider = mailSpider;
    }

    public LinkedBlockingDeque<MailSpiderEvent> getEvents() {
        return events;
    }

    public void setEvents(LinkedBlockingDeque<MailSpiderEvent> events) {
        this.events = events;
    }
}
