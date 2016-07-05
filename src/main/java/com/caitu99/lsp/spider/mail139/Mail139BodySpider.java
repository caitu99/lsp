package com.caitu99.lsp.spider.mail139;

import com.caitu99.lsp.model.spider.Envelope;
import com.caitu99.lsp.model.spider.MailSpiderEvent;
import com.caitu99.lsp.spider.MailBodySpider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentLinkedDeque;

/**
 * @author yukf
 * @date 2015年11月9日 上午10:43:03
 */
public class Mail139BodySpider extends MailBodySpider {
    private final static Logger logger = LoggerFactory.getLogger(Mail139BodySpider.class);


    public void run() {
        while (!isInterrupted()) {
            MailSpiderEvent event = null;
            logger.debug("mail 139 body spider do polling...");
            try {
                event = events.takeFirst();
            } catch (InterruptedException e1) {
                logger.error("mail 139 body spider thread is interrupted");
            }
            ConcurrentLinkedDeque<Envelope> envelopes = event.getEnvelopes();
            if (!envelopes.isEmpty()) {
                mailSpider.onEvent(event);
                events.addLast(event);
            }
        }
    }
}
