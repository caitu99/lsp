/*
 * Copyright (c) 2015-2020 by caitu99
 * All rights reserved.
 */
package com.caitu99.lsp.spider.mail163;

import com.caitu99.lsp.model.spider.Envelope;
import com.caitu99.lsp.model.spider.MailSpiderEvent;
import com.caitu99.lsp.spider.MailBodySpider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentLinkedDeque;

/**
 * @author yukf
 * @Description: (类职责详细描述, 可空)
 * @ClassName: Mail163BodySpider
 * @date 2015年10月27日 下午4:23:03
 * @Copyright (c) 2015-2020 by caitu99
 */
public class Mail163BodySpider extends MailBodySpider {
    private final static Logger logger = LoggerFactory.getLogger(Mail163BodySpider.class);


    public void run() {
        while (!isInterrupted()) {
            MailSpiderEvent event = null;
            logger.debug("mail 163 body spider do polling...");
            try {
                event = events.takeFirst();
            } catch (InterruptedException e1) {
                logger.error("mail 163 body spider thread is interrupted");
            }
            ConcurrentLinkedDeque<Envelope> envelopes = event.getEnvelopes();
            if (!envelopes.isEmpty()) {
                mailSpider.onEvent(event);
                events.addLast(event);
            }
        }
    }
}
