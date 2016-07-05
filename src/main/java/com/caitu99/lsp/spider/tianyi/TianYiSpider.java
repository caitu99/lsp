/*
 * Copyright (c) 2015-2020 by caitu99
 * All rights reserved.
 */
package com.caitu99.lsp.spider.tianyi;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.Base64;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.HttpMessage;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.request.async.DeferredResult;

import com.alibaba.fastjson.JSON;
import com.caitu99.lsp.Constant;
import com.caitu99.lsp.cache.RedisOperate;
import com.caitu99.lsp.exception.SpiderException;
import com.caitu99.lsp.model.spider.QueryEvent;
import com.caitu99.lsp.model.spider.tianyi.TianYiSpiderEvent;
import com.caitu99.lsp.model.spider.tianyi.TianYiSpiderState;
import com.caitu99.lsp.spider.HttpAsyncClient;
import com.caitu99.lsp.spider.QuerySpider;
import com.caitu99.lsp.utils.SpringContext;
import com.caitu99.lsp.utils.cookie.CookieHelper;

/**
 * @author ws
 * @Description: (类职责详细描述, 可空)
 * @ClassName: TianYiSpider
 * @date 2015年11月24日 下午4:56:02
 * @Copyright (c) 2015-2020 by caitu99
 */
public class TianYiSpider implements QuerySpider {

    //	private static final String userAgent = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_10_5) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/45.0.2454.101 Safari/537.36";
    private static final String URL_INIT = "http://tyclub.telefen.com/newjf_hgo2/html/HGOIndex_em_qg.html?provinceId=35";
    private static final String URL_IMG = "http://tyclub.telefen.com/newlife/servlet/validateCodeServlet?re=0.022612563567236066";
    private static final String URL_CHECK = "http://tyclub.telefen.com/newlife/jfInterface/imgCodeRON?imgCode=%s";
    private static final String URL_MSG = "http://tyclub.telefen.com/newlife/jfInterface/setMsCode?DeviceNo=%s&SmsCode=12&imgCode=%s";
    private static final String URL_LOGIN = "http://tyclub.telefen.com/newlife/interface/msCodeLogin?Mobile=%s&MsCode=%s";
    private static final String URL_INTEGRAL = "http://tyclub.telefen.com/newlife/interface/getJfByPhone?Mobile=%s";
    private static final Logger logger = LoggerFactory.getLogger(TianYiSpider.class);
    private CloseableHttpAsyncClient httpAsyncClient = HttpAsyncClient.getInstance();
    private RedisOperate redis = SpringContext.getBean(RedisOperate.class);

    @Override
    public void onEvent(QueryEvent event) {
        TianYiSpiderEvent tyEvent = (TianYiSpiderEvent) event;
        try {
            switch (tyEvent.getState()) {
                case NONE:
                    initUp(tyEvent);
                    break;
                case IMG:
                    imgUp(tyEvent);
                    break;
                case CHECK:
                    checkUp(tyEvent);
                    break;
                case MSG:
                    msgUp(tyEvent);
                    break;
                case LOGIN:
                    loginUp(tyEvent);
                    break;
                case GAIN:
                    gainUp(tyEvent);
                    break;
                case ERROR:
                    errorHandle(tyEvent);
                    break;
            }
        } catch (Exception e) {
            logger.error("request up error {}", event.getId(), e);
            tyEvent.setException(e);
            errorHandle(tyEvent);
        }
    }


    /**
     * @param tyEvent
     * @Description: (方法职责详细描述, 可空)
     * @Title: initUp
     * @date 2015年11月24日 下午5:03:58
     * @author ws
     */
    private void initUp(TianYiSpiderEvent tyEvent) {
        logger.debug("do initUp {}", tyEvent.getId());
        HttpGet httpGet = new HttpGet(URL_INIT);
        setHeader(URL_INIT, httpGet, tyEvent);
        httpAsyncClient.execute(httpGet, new HttpAsyncCallback(tyEvent));
    }


    /**
     * @param tyEvent
     * @Description: (方法职责详细描述, 可空)
     * @Title: checkUp
     * @date 2015年11月24日 下午5:03:56
     * @author ws
     */
    private void imgUp(TianYiSpiderEvent tyEvent) {
        logger.debug("do imgUp {}", tyEvent.getId());
        HttpGet httpGet = new HttpGet(URL_IMG);
        setHeader(URL_IMG, httpGet, tyEvent);
        httpAsyncClient.execute(httpGet, new HttpAsyncCallback(tyEvent));
    }


    /**
     * @param tyEvent
     * @Description: (方法职责详细描述, 可空)
     * @Title: msgUp
     * @date 2015年11月24日 下午5:03:53
     * @author ws
     */
    private void checkUp(TianYiSpiderEvent tyEvent) {
        logger.debug("do checkUp {}", tyEvent.getId());
        String url = String.format(URL_CHECK, tyEvent.getvCode());
        HttpGet httpGet = new HttpGet(url);
        setHeader(url, httpGet, tyEvent);
        httpGet.setHeader("X-Requested-With", "XMLHttpRequest");
        httpAsyncClient.execute(httpGet, new HttpAsyncCallback(tyEvent));
    }


    /**
     * @param tyEvent
     * @Description: (方法职责详细描述, 可空)
     * @Title: msgUp
     * @date 2015年11月24日 下午5:35:28
     * @author ws
     */
    private void msgUp(TianYiSpiderEvent tyEvent) {
        logger.debug("do msgUp {}", tyEvent.getId());
        String url = String.format(URL_MSG, tyEvent.getAccount(), tyEvent.getvCode());
        HttpGet httpGet = new HttpGet(url);
        setHeader(url, httpGet, tyEvent);
        httpGet.setHeader("X-Requested-With", "XMLHttpRequest");
        httpAsyncClient.execute(httpGet, new HttpAsyncCallback(tyEvent));

    }


    /**
     * @param tyEvent
     * @Description: (方法职责详细描述, 可空)
     * @Title: loginUp
     * @date 2015年11月24日 下午5:03:51
     * @author ws
     */
    private void loginUp(TianYiSpiderEvent tyEvent) {
        logger.debug("do loginUp {}", tyEvent.getId());
        String url = String.format(URL_LOGIN, tyEvent.getAccount(), tyEvent.getMsgCode());
        HttpGet httpGet = new HttpGet(url);
        setHeader(url, httpGet, tyEvent);
        httpGet.setHeader("X-Requested-With", "XMLHttpRequest");
        httpAsyncClient.execute(httpGet, new HttpAsyncCallback(tyEvent));
    }


    /**
     * @param tyEvent
     * @Description: (方法职责详细描述, 可空)
     * @Title: gainUp
     * @date 2015年11月24日 下午5:03:48
     * @author ws
     */
    private void gainUp(TianYiSpiderEvent tyEvent) {
        logger.debug("do gainUp {}", tyEvent.getId());
        String url = String.format(URL_INTEGRAL, tyEvent.getAccount());
        HttpGet httpGet = new HttpGet(url);
        setHeader(url, httpGet, tyEvent);
        httpGet.setHeader("X-Requested-With", "XMLHttpRequest");
        httpGet.setHeader("Referer", "http://tyclub.telefen.com/newjf_hgo2/html/kamiPage_em.html");
        httpGet.setHeader("Accept-Encoding", "gzip, deflate, sdch");
        httpGet.setHeader("Accept-Language", "zh-CN,zh;q=0.8");
//		Accept-Encoding: gzip, deflate, sdch
//		Accept-Language: zh-CN,zh;q=0.8

        httpAsyncClient.execute(httpGet, new HttpAsyncCallback(tyEvent));
    }


    @Override
    public void errorHandle(QueryEvent event) {
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

    private void setHeader(String uriStr, HttpMessage httpGet, QueryEvent event) {
//		httpGet.setHeader("User-Agent", userAgent);
//		httpGet.setHeader("Host", "tyclub.telefen.com");
//		httpGet.setHeader("Connection", "keep-alive");
        httpGet.setHeader("Accept", "*/*");
        httpGet.setHeader("User-Agent", "Android");
//		httpGet.setHeader("Referer", "http://tyclub.telefen.com/newjf_hgo2/html/HGOIndex_em.html?provinceId=35");
//		httpGet.setHeader("Accept-Encoding", "gzip, deflate, sdch");
//		httpGet.setHeader("Accept-Language", "zh-CN,zh;q=0.8");
        try {
            CookieHelper.setCookies(uriStr, httpGet, event.getCookieList());
        } catch (URISyntaxException e) {
            logger.error("set cookie fail {}", event.getId());
        }
    }

    private class HttpAsyncCallback implements FutureCallback<HttpResponse> {

        private TianYiSpiderEvent event;
        private boolean skipNextStep = false;

        public HttpAsyncCallback(TianYiSpiderEvent event) {
            this.event = event;
        }

        @Override
        public void completed(HttpResponse result) {
            try {
                CookieHelper.getCookiesFresh(event.getCookieList(), result);
                switch (event.getState()) {
                    case NONE:
                        initDown(result);
                        break;
                    case IMG:
                        imgDown(result);
                        break;
                    case CHECK:
                        checkDown(result);
                        break;
                    case MSG:
                        msgDown(result);
                        break;
                    case LOGIN:
                        loginDown(result);
                        break;
                    case GAIN:
                        gainDown(result);
                        break;
                    case ERROR:
                        errorHandle(event);
                        break;
                }
            } catch (Exception e) {
                logger.error("unexpected error {}", event.getId(), e);
                event.setException(e);
            }
            // next step
            if (skipNextStep)
                return;
            onEvent(event);

        }


        /**
         * @param result
         * @Description: (方法职责详细描述, 可空)
         * @Title: initDown
         * @date 2015年11月24日 下午5:07:40
         * @author ws
         */
        private void initDown(HttpResponse result) {
            logger.debug("pre login page down {}", event.getId());
            event.setState(TianYiSpiderState.IMG);
        }

        /**
         * @param result
         * @Description: (方法职责详细描述, 可空)
         * @Title: checkDown
         * @date 2015年11月24日 下午5:07:37
         * @author ws
         */
        private void imgDown(HttpResponse result) {
            logger.debug("get img down {}", event.getId());
            try {
                HttpEntity httpEntity = result.getEntity();
                InputStream imgStream = httpEntity.getContent();
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                int rd = -1;
                byte[] bytes = new byte[1024];
                while ((rd = imgStream.read(bytes)) != -1) {
                    baos.write(bytes, 0, rd);
                }
                byte[] rbyte = baos.toByteArray();
                String imgStr = Base64.getEncoder().encodeToString(rbyte);
/*
                // save to file, used for debug
				if (appConfig.inDevMode()) {
					byte[] tbytes = Base64.getDecoder().decode(imgStr);
					FileOutputStream fs = new FileOutputStream(
							//appConfig.getUploadPath() + "/" + event.getUserid() + ".jpg");
							"D://tianyi.jpg");
					fs.write(tbytes);
					fs.close();
				}
*/
                String key = String.format(Constant.TIAN_YI_IMPORT_KEY, event.getUserid());
                redis.set(key, JSON.toJSONString(event), 300);

                event.setException(new SpiderException(1001, "输入验证码", imgStr));
                return;
            } catch (Exception e) {
                logger.error("get img exception", e);
                event.setException(e);
            }

        }

        /**
         * @param result
         * @Description: (方法职责详细描述, 可空)
         * @Title: msgDown
         * @date 2015年11月24日 下午5:07:35
         * @author ws
         */
        private void checkDown(HttpResponse result) {
            try {
                Map entityMap = null;
                try {
                    entityMap = JSON.parseObject(EntityUtils.toString(result
                            .getEntity()));
                } catch (IOException e) {

                    logger.error("IOException", e);
                    event.setException(new SpiderException(2007, "图形验证码错误"));
                    return;
                }

                if (null == entityMap.get("success")) {
                    event.setException(new SpiderException(2007, "图形验证码错误"));
                    return;
                }

                String reErrCode = entityMap.get("success").toString();
                if ("true".equals(reErrCode)) {

                    event.setState(TianYiSpiderState.MSG);
                    return;
                } else {
                    event.setException(new SpiderException(2004, "图形验证码错误"));
                    return;
                }
            } catch (Exception e) {
                logger.error("天翼图片验证码验证异常 Exception", e);
                event.setException(new SpiderException(2603, "图形验证码验证失败"));
                return;
            }
        }

        /**
         * @param result
         * @Description: (方法职责详细描述, 可空)
         * @Title: msgDown
         * @date 2015年11月24日 下午5:39:02
         * @author ws
         */
        private void msgDown(HttpResponse result) {
            try {
                String entityStr = EntityUtils.toString(result.getEntity());
                System.out.println(entityStr);
                Map entityMap = JSON.parseObject(entityStr);
                if (null == entityMap.get("ErrCode")) {

                }

                String reErrCode = entityMap.get("ErrCode").toString();
                if ("0000".equals(reErrCode)) {
                    String key = String.format(Constant.TIAN_YI_IMPORT_KEY, event.getUserid());
                    redis.set(key, JSON.toJSONString(event), 300);

                    event.setState(TianYiSpiderState.LOGIN);
                    event.setException(new SpiderException(0, "短信验证码已发送"));
                    return;
                } else {
                    event.setException(new SpiderException(2007, "短信验证码发送失败,请刷新验证码重试"));
                    return;
                }
            } catch (Exception e) {
                logger.error("天翼短信验证码发送失败", e);
                event.setException(new SpiderException(2007, "短信验证码发送失败,请刷新验证码重试"));
                return;
            }
        }


        /**
         * @param result
         * @Description: (方法职责详细描述, 可空)
         * @Title: loginDown
         * @date 2015年11月24日 下午5:07:33
         * @author ws
         */
        private void loginDown(HttpResponse result) {
            try {
                Map entityMap = JSON.parseObject(EntityUtils.toString(result
                        .getEntity()));
                //ErrCode
                String reErrCode = "";
                if (null != entityMap.get("ErrCode")) {
                    reErrCode = entityMap.get("ErrCode").toString();
                } else {
                    event.setException(new SpiderException(2005, "登录失败,请重试"));
                    return;
                }
                if ("0000".equals(reErrCode)) {

                    //CustName
                    String custName = "";
                    if (null != entityMap.get("CustName")) {
                        custName = entityMap.get("CustName").toString();
                        event.setCustName(custName);
                        event.setState(TianYiSpiderState.GAIN);
                        return;
                    } else {
                        event.setException(new SpiderException(2049, "积分获取失败,请重新登录"));
                        return;
                    }
                } else {
                    if (null == entityMap.get("ErrMsg")) {
                        event.setException(new SpiderException(2005, "登录失败,请重试"));
                        return;
                    } else {
                        event.setException(new SpiderException(2005, entityMap.get("ErrMsg").toString()));
                        return;
                    }
                }
            } catch (Exception e) {
                logger.error("天翼登录失败", e);
                event.setException(new SpiderException(2005, "登录失败,请重试"));
                return;
            }
        }

        /**
         * @param result
         * @Description: (方法职责详细描述, 可空)
         * @Title: gainDown
         * @date 2015年11月24日 下午5:07:29
         * @author ws
         */
        private void gainDown(HttpResponse result) {
            Map entityMap;
            try {
                entityMap = JSON.parseObject(EntityUtils.toString(result.getEntity()));
                if (null != entityMap.get("success")) {
                    String reErrCode = entityMap.get("success").toString();
                    if ("true".equals(reErrCode)) {
                        entityMap.put("custName", event.getCustName());
                        event.setException(new SpiderException(0, "积分获取成功", JSON.toJSONString(entityMap)));
                        return;
                    } else {
                        event.setException(new SpiderException(2049, "积分获取失败,请重新登录"));
                        return;
                    }
                } else {

                    event.setException(new SpiderException(2049, "积分获取失败,请重新登录"));
                    return;
                }
            } catch (Exception e) {
                logger.error("天翼积分获取失败,请重新登录", e);
                event.setException(new SpiderException(2049, "积分获取失败,请重新登录"));
                return;
            }


        }

        @Override
        public void failed(Exception ex) {

        }

        @Override
        public void cancelled() {

        }
    }


}
