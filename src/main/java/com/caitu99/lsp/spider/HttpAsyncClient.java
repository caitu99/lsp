/*
 * Copyright (c) 2015-2020 by caitu99
 * All rights reserved.
 */
package com.caitu99.lsp.spider;

import com.caitu99.lsp.AppConfig;
import com.caitu99.lsp.perf.PerfMonitor;
import com.caitu99.lsp.utils.SpringContext;
import com.caitu99.lsp.utils.cookie.IgnoreCookieStore;
import org.apache.http.HttpHost;
import org.apache.http.client.CookieStore;
import org.apache.http.client.RedirectStrategy;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClients;
import org.apache.http.impl.nio.conn.PoolingNHttpClientConnectionManager;
import org.apache.http.impl.nio.reactor.DefaultConnectingIOReactor;
import org.apache.http.impl.nio.reactor.IOReactorConfig;
import org.apache.http.nio.conn.ssl.SSLIOSessionStrategy;
import org.apache.http.nio.reactor.ConnectingIOReactor;
import org.apache.http.ssl.SSLContexts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLContext;

/**
 * http async client instance singleton
 *
 * @author yukf
 * @Description: (类职责详细描述, 可空)
 * @ClassName: HttpAsyncClient
 * @date 2015年10月24日 下午1:57:04
 * @Copyright (c) 2015-2020 by caitu99
 */
public class HttpAsyncClient {
    private final static Logger logger = LoggerFactory.getLogger(HttpAsyncClient.class);
    private static CloseableHttpAsyncClient httpAsyncClient = null;

    private HttpAsyncClient() {

    }

    public static CloseableHttpAsyncClient getInstance() {
        if (httpAsyncClient == null) {
            synchronized (HttpAsyncClient.class) {
                if (httpAsyncClient == null) {
                    initHttpAsyncClient();
                }
            }
        }
        return httpAsyncClient;
    }

    /**
     * init http async client
     *
     * @Description: (方法职责详细描述, 可空)
     * @Title: initHttpAsyncClient
     * @date 2015年10月24日 下午1:51:50
     * @author yukf
     */
    private static void initHttpAsyncClient() {
        try {

            System.setProperty("jsse.enableSNIExtension", "false");

            RedirectStrategy noRedirect = new IgnoreRedirect();
            CookieStore ignoreCookieStore = new IgnoreCookieStore();
            AppConfig appConfig = SpringContext.getBean(AppConfig.class);
            int threadCount = PerfMonitor.getCpuCount() * 2;
            IOReactorConfig ioReactorConfig = IOReactorConfig.custom().setIoThreadCount(threadCount).build();
            ConnectingIOReactor ioReactor = new DefaultConnectingIOReactor(ioReactorConfig);
            PoolingNHttpClientConnectionManager cm = new PoolingNHttpClientConnectionManager(ioReactor);
            cm.setDefaultMaxPerRoute(10000);
            cm.setMaxTotal(10000);

            RequestConfig requestConfig = RequestConfig.custom()
                    .setCircularRedirectsAllowed(false)
                    .setSocketTimeout(60000)
                    .setConnectTimeout(60000)
                    .setCookieSpec(CookieSpecs.IGNORE_COOKIES)
                    .build();

            SSLContext sslcontext = SSLContexts.createSystemDefault();
            SSLIOSessionStrategy sslSessionStrategy = new SSLIOSessionStrategy(sslcontext, new String[]{"TLS", "SSLv3.0", "TLSv1"}, null,
                    SSLIOSessionStrategy.getDefaultHostnameVerifier());

            if (appConfig.inDevMode()) {
                httpAsyncClient = HttpAsyncClients.custom()
                        .setRedirectStrategy(noRedirect)
                        .setConnectionManager(cm)
                        .setDefaultRequestConfig(requestConfig)
                        .setSSLStrategy(sslSessionStrategy)
//                        .setProxy(new HttpHost("localhost", 8888))
                        .setDefaultCookieStore(ignoreCookieStore)
                        .build();
            } else {
                httpAsyncClient = HttpAsyncClients.custom()
                        .setRedirectStrategy(noRedirect)
                        .setConnectionManager(cm)
                        .setDefaultRequestConfig(requestConfig)
                        .setSSLStrategy(sslSessionStrategy)
                        .setDefaultCookieStore(ignoreCookieStore)
                        .build();
            }
            httpAsyncClient.start();
        } catch (Exception e) {
            logger.error("set io reactor error", e);
        }
    }
}
