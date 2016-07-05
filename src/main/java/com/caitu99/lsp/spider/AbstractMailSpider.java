/*
 * Copyright (c) 2015-2020 by caitu99
 * All rights reserved.
 */
package com.caitu99.lsp.spider;

import com.caitu99.lsp.AppConfig;
import com.caitu99.lsp.cache.RedisOperate;
import com.caitu99.lsp.exception.SpiderException;
import com.caitu99.lsp.model.spider.MailSpiderEvent;
import com.caitu99.lsp.utils.SpringContext;
import org.apache.http.HttpMessage;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.request.async.DeferredResult;

import java.util.concurrent.LinkedBlockingDeque;

/**
 * @author yukf
 * @Description: (类职责详细描述, 可空)
 * @ClassName: AbstractMailSpider
 * @date 2015年10月26日 下午5:17:31
 * @Copyright (c) 2015-2020 by caitu99
 */
public abstract class AbstractMailSpider implements IMailSpider {

    protected static final String USERAGENT_CHROME = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_10_5) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/45.0.2454.101 Safari/537.36";
    protected static final String USERAGENT_ANDROID = "Android";
    protected static final String USERAGENT_IPHONE = "iphone";
    protected static final String INQUEUE = "inqueue";
    protected final static Logger mailLogger = LoggerFactory.getLogger("mailAppender");
    private final static Logger logger = LoggerFactory.getLogger(AbstractMailSpider.class);
    protected MailBodySpider mailBodySpider;
    protected MailParserTask mailParser;
    protected CloseableHttpAsyncClient httpAsyncClient = HttpAsyncClient.getInstance();
    protected AppConfig appConfig = SpringContext.getBean(AppConfig.class);
    protected RedisOperate redis;

    public AbstractMailSpider(MailBodySpider mailBodySpider, MailParserTask mailParser) {
        this.mailBodySpider = mailBodySpider;
        this.mailParser = mailParser;
        redis = SpringContext.getBean(RedisOperate.class);
    }

    /**
     * add event to mailqq spider and send info back to client
     *
     * @param event
     * @Description: (方法职责详细描述, 可空)
     * @Title: taskQueue
     * @date 2015年10月24日 上午11:05:09
     * @author yukf
     */
    protected void taskQueue(MailSpiderEvent event) {

        // 先返回结果给前端
        DeferredResult<Object> deferredResult = event.getDeferredResult();
        if (deferredResult == null)
            return;

        if (deferredResult.isSetOrExpired()) {
            logger.error("a request has been expired: {}", event);
            return;
        }

        Exception exception = new SpiderException(1019, "开始获取邮件");
        deferredResult.setResult(exception.toString());

        // remove deferred result
        event.setDeferredResult(null);

        LinkedBlockingDeque<MailSpiderEvent> events = mailBodySpider.getEvents();
        events.add(event);// add task to thread
    }

    /**
     * add event to mail parser
     *
     * @param event
     * @Description: (方法职责详细描述, 可空)
     * @Title: parseTaskQueue
     * @date 2015年10月26日 下午2:05:50
     * @author yukf
     */
    protected void parseTaskQueue(MailSpiderEvent event) {
        LinkedBlockingDeque<MailSpiderEvent> events = mailParser.getEvents();
        events.add(event);// add task to thread
    }

    /**
     * error handle
     *
     * @param event event
     */
    @Override
    public void errorHandle(MailSpiderEvent event) {
        DeferredResult<Object> deferredResult = event.getDeferredResult();

        if (deferredResult == null) {
            // the result has benn returned to user
            return;
        }

        if (deferredResult.isSetOrExpired()) {
            logger.debug("a request has been expired: {}", event);
            return;
        }

        Exception exception = event.getException();
        if (exception != null) {
            if (exception instanceof SpiderException) {
                deferredResult.setResult(exception.toString());
            } else {
                logger.error("unknown exception", exception);
                deferredResult.setResult((new SpiderException(-1, exception.getMessage()).toString()));
            }
        }
    }

    /**
     * set http header
     *
     * @param httpGet httpGet
     * @param event   event
     */
    protected abstract void setHeader(String uriStr, HttpMessage httpGet, MailSpiderEvent event);
//	protected void setHeader(CookieHandler cookieHandler, HttpRequestBase request, HttpResponse response) {
//		request.setHeader("Accept", "*/*");
//		request.setHeader("User-Agent", userAgent);
//		CookieHelper.setCookies(cookieHandler, request, );
//	}

}
