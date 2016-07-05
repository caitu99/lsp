/*
 * Copyright (c) 2015-2020 by caitu99
 * All rights reserved.
 */
package com.caitu99.lsp.spider.wumart;

import java.util.Date;
import java.util.concurrent.ConcurrentLinkedDeque;

import com.caitu99.lsp.model.spider.Envelope;
import com.caitu99.lsp.model.spider.MailSpiderEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.caitu99.lsp.spider.MailBodySpider;

/** 
 * 
 * @Description: (类职责详细描述,可空) 
 * @ClassName: WucartBodySpider 
 * @author fangjunxiao
 * @date 2015年12月11日 下午3:46:09 
 * @Copyright (c) 2015-2020 by caitu99 
 */
public class WumartBodySpider extends MailBodySpider{

	private final static Logger logger = LoggerFactory.getLogger(WumartBodySpider.class);

	private static final int INTERVAL_TIME = 2000;

	public void run() {
		while (!isInterrupted()) {
			
			long t = new Date().getTime();
			MailSpiderEvent event = null;
			logger.debug("csair body spider do polling...");
			try {
				event = events.takeFirst();
			} catch (InterruptedException e1) {
				logger.error("csair body spider thread is interrupted");
			}
			if(event == null) continue;
			ConcurrentLinkedDeque<Envelope> envelopes = event.getEnvelopes();
			long lastTime = event.getLastGetTime();
			long interval = t - lastTime;
			if (!envelopes.isEmpty()) {
				if (interval >= INTERVAL_TIME) {
					// 如果上次发送时间和这次差2s以上，则需要发送
					mailSpider.onEvent(event);
					event.setLastGetTime(t);
					events.addLast(event);
				} else {
					events.addFirst(event);
					try {
						Thread.sleep(INTERVAL_TIME - interval);
					} catch (InterruptedException e) {
						logger.error("air china body spider thread is interrupted");
					}
				}
			}
		}
	}
}
