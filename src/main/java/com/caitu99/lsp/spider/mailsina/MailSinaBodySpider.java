package com.caitu99.lsp.spider.mailsina;

import com.caitu99.lsp.model.spider.Envelope;
import com.caitu99.lsp.model.spider.MailSpiderEvent;
import com.caitu99.lsp.spider.MailBodySpider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentLinkedDeque;

/**
 * Created by Lion on 2015/11/10 0010.
 */
public class MailSinaBodySpider extends MailBodySpider {
    private final static Logger logger = LoggerFactory.getLogger(MailSinaBodySpider.class);


    public void run() {
        while (!isInterrupted()) {
            MailSpiderEvent event = null;
            logger.debug("mail sina body spider do polling...");
            try {
                event = events.takeFirst();
            } catch (InterruptedException e1) {
                logger.error("mail sina body spider thread is interrupted");
            }
            ConcurrentLinkedDeque<Envelope> envelopes = event.getEnvelopes();
            if (!envelopes.isEmpty()) {
                mailSpider.onEvent(event);
                events.addLast(event);
            }
        }
    }
}
