package com.caitu99.lsp.spider.hotmail;

import com.caitu99.lsp.model.spider.MailSpiderEvent;
import com.caitu99.lsp.model.spider.hotmail.HotmailItem;
import com.caitu99.lsp.model.spider.hotmail.MailHotmailSpiderEvent;
import com.caitu99.lsp.spider.MailBodySpider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentLinkedDeque;

/**
 * Created by Lion on 2015/11/14 0014.
 */
public class MailHotmailBodySpider extends MailBodySpider {
    //    private final static Logger logger = LoggerFactory.getLogger(MailHotmailBodySpider.class);
//
//    private static final int INTERVAL_TIME = 1000;
//
//    public void run() {
//        while (!isInterrupted()) {
//
//            long t = new Date().getTime();
//            MailSpiderEvent event = null;
//            logger.debug("mail qq body spider do polling...");
//            try {
//                event = events.takeFirst();
//            } catch (InterruptedException e1) {
//                logger.error("mail qq body spider thread is interrupted");
//            }
//            if(event == null) continue;
//            MailHotmailSpiderEvent event1 = (MailHotmailSpiderEvent) event;
//            ConcurrentLinkedDeque<HotmailItem> hotmailItems = event1.getHotmailItems();
//            long lastTime = event1.getLastGetTime();
//            long interval = t - lastTime;
//            // 如果不是获取邮件状态，就丢弃
//            if (!hotmailItems.isEmpty()) {
//                if (interval >= INTERVAL_TIME) {
//                    // 如果上次发送时间和这次差2s以上，则需要发送
//                    mailSpider.onEvent(event1);
//                    event1.setLastGetTime(t);
//                    events.addLast(event1);
//                } else {
//                    events.addFirst(event1);
//                    try {
//                        Thread.sleep(INTERVAL_TIME - interval);
//                    } catch (InterruptedException e) {
//                        logger.error("mail qq body spider thread is interrupted");
//                    }
//                }
//            }
//        }
//    }
    private final static Logger logger = LoggerFactory.getLogger(MailHotmailBodySpider.class);


    public void run() {
        while (!isInterrupted()) {
            MailSpiderEvent event = null;
            logger.debug("mail hotmail body spider do polling...");
            try {
                event = events.takeFirst();
            } catch (InterruptedException e1) {
                logger.error("mail hotmail body spider thread is interrupted");
            }
            MailHotmailSpiderEvent event1 = (MailHotmailSpiderEvent) event;
            ConcurrentLinkedDeque<HotmailItem> hotmailItems = event1.getHotmailItems();
            if (!hotmailItems.isEmpty()) {
                mailSpider.onEvent(event);
                events.addLast(event);
            }
        }
    }
}
