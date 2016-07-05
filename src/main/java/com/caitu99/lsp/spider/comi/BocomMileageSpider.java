package com.caitu99.lsp.spider.comi;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.http.Header;
import org.apache.http.HttpMessage;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.request.async.DeferredResult;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.caitu99.lsp.Constant;
import com.caitu99.lsp.cache.RedisOperate;
import com.caitu99.lsp.exception.SpiderException;
import com.caitu99.lsp.model.spider.QueryEvent;
import com.caitu99.lsp.model.spider.comishop.BocomMileageEvent;
import com.caitu99.lsp.model.spider.comishop.BocomMileageState;
import com.caitu99.lsp.model.spider.comishop.FlightCompany;
import com.caitu99.lsp.spider.HttpAsyncClient;
import com.caitu99.lsp.spider.QuerySpider;
import com.caitu99.lsp.utils.SpringContext;
import com.caitu99.lsp.utils.XpathHtmlUtils;
import com.caitu99.lsp.utils.cookie.CookieHelper;

/**
 * 交通银行航空里程兑换
 * 
 * @Description: (类职责详细描述,可空) 
 * @ClassName: BocomMileageSpider 
 * @author ws
 * @date 2016年4月27日 下午12:23:39 
 * @Copyright (c) 2015-2020 by caitu99
 */
public class BocomMileageSpider implements QuerySpider {

    private static final Logger logger = LoggerFactory
            .getLogger(BocomMileageSpider.class);

    private static final String userAgent = "Mozilla/5.0 (Windows NT 6.3; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/46.0.2490.86 Safari/537.36";
    private static final String indexUrl = "https://creditcardapp.bankcomm.com/idm/sso/login.html";
    private static final String keyboardUrl = "https://creditcardapp.bankcomm.com/idm/sso/keyboards.json";
    private static final String loginUrl = "https://creditcardapp.bankcomm.com/idm/sso/auth.html";
    private static final String checkUrl = "https://creditcardapp.bankcomm.com/idm/sso/checkLogin.json";
    private static final String jauthUrl = "http://club.bankcomm.com/customer/j_auth.html";
    private static final String queryIntegralUrl = "https://club.bankcomm.com/customer/memberccardinfo/memberCcardList.html";
    private static final String login1Url = "https://creditcardapp.bankcomm.com/idm/sso/login.html";

    private static final String convertPageUrl = "http://club.bankcomm.com/customer/flightcompanymemberbinding/list2.html";
    //解绑
    private static final String removeBindUrl = "http://club.bankcomm.com/customer/flightcompanymemberbinding/removeBinding.html?ts=%s&memberId=%s&FlightCompanyCode=%s&id=%s";
    //绑定
    private static final String bindMemberUrl = "http://club.bankcomm.com/customer/flightcompanymemberbinding/insertFcmb.html?ts=%s&memberId=%s&fcmbCode=%s&clubMemberId=%s";
    //校验是否可兑换
    private static final String checkLimitUrl = "http://club.bankcomm.com/customer/bonusconvertmileage/queryUseBonusLimit.html?ajax=true&ts=%s";
    //获取卡片信息
    private static final String queryCardUrl = "http://club.bankcomm.com/customer/bonusconvertmileage/queryCardMessage.html?ajax=true&ts=%s";
    //发送短信验证码
    private static final String sendMsgUrl = "http://club.bankcomm.com/customer/bonusconvertmileage/sendCheckCode.html?ts=%s";
    //兑换里程
    private static final String convertMileageUrl = "http://club.bankcomm.com/customer/bonusconvertmileage/convertMileage.html?ts=%s&flightComMemberId=%s&flightComCode=%s&memberCcard=1&sjCheckCode=%s&useBonus=%s&validMonth=%s&validYear=%s&flightCompanyName=%s&ebsNm1=%s&ebsNm2=%s";
    
    
    protected RedisOperate redis = SpringContext.getBean(RedisOperate.class);
    private CloseableHttpAsyncClient httpAsyncClient = HttpAsyncClient.getInstance();

    @Override
    public void onEvent(QueryEvent event) {
        BocomMileageEvent BocomMileageEvent = (BocomMileageEvent) event;
        try {
            switch (BocomMileageEvent.getState()) {
                case INDEX:
                    indexUp(BocomMileageEvent);
                    break;
                case KEYBOARD:
                    keyboardUp(BocomMileageEvent);
                    break;
                case LOGIN:
                    loginUp(BocomMileageEvent);
                    break;
                case QUERY_INTEGRAL:
                    queryIntegralUp(BocomMileageEvent);
                    break;
                case TICKET:
                    ticketUp(BocomMileageEvent);
                    break;
                case CHECK:
                    checkUp(BocomMileageEvent);
                    break;
                case JAUTH:
                    jauthUp(BocomMileageEvent);
                    break;
                case LOGIN1:
                    login1Up(BocomMileageEvent);
                    break;
                case JSECURITY:
                    jsecurityUp(BocomMileageEvent);
                    break;
                case JAUTH1:
                    jauth1Up(BocomMileageEvent);
                    break;

                case CONVERT_PAGE:
                    convertPageUp(BocomMileageEvent);
                    break;
                case REMOVE_BIND:
                    removeBindUp(BocomMileageEvent);
                    break;
                case BIND_MEMBER:
                    bindMemberUp(BocomMileageEvent);
                    break;
                case CHECK_LIMIT:
                    checkLimitUp(BocomMileageEvent);
                    break;
                case QUERY_CARD:
                    queryCardUp(BocomMileageEvent);
                    break;
                case SEND_MSG:
                    sendMsgUp(BocomMileageEvent);
                    break;
                case CONVERT_MILEAGE:
                    convertMileageUp(BocomMileageEvent);
                    break;

                case ERROR:
                    errorHandle(BocomMileageEvent);
                    break;
            }
        } catch (Exception e) {
            logger.error("request up error {}", event.getId(), e);
            BocomMileageEvent.setException(e);
            errorHandle(BocomMileageEvent);
        }
    }

    /**
	 * 	
	 * @Description: (方法职责详细描述,可空)  
	 * @Title: convertPageUp 
	 * @param bocomMileageEvent
	 * @date 2016年4月27日 上午10:01:03  
	 * @author ws
	*/
	private void convertPageUp(BocomMileageEvent bocomMileageEvent) {

        HttpGet httpGet = new HttpGet(convertPageUrl);
        setHeader(convertPageUrl, httpGet, bocomMileageEvent);
        httpGet.setHeader("Referer", "http://club.bankcomm.com/customer/flightcompanymemberbinding/list2.html");
        httpAsyncClient.execute(httpGet, new HttpAsyncCallback(bocomMileageEvent));
        
	}

	/**
	 * 	
	 * @Description: (方法职责详细描述,可空)  
	 * @Title: convertMileageUp 
	 * @param bocomMileageEvent
	 * @date 2016年4月27日 上午9:33:41  
	 * @author ws
	*/
	private void convertMileageUp(BocomMileageEvent bocomMileageEvent) {

		String url = String.format(convertMileageUrl, new Date().getTime()
						,bocomMileageEvent.getMemberId()
						,bocomMileageEvent.getFlightCompanyCode()
						,bocomMileageEvent.getMsgCode()
						,bocomMileageEvent.getUseBonus()
						,bocomMileageEvent.getValidMonth()
						,bocomMileageEvent.getValidYear()
						,bocomMileageEvent.getFlightCompanyName()
						,bocomMileageEvent.getEbsNm1()
						,bocomMileageEvent.getEbsNm2());
		
        HttpGet httpGet = new HttpGet(url);
        setHeader(url, httpGet, bocomMileageEvent);
        httpGet.setHeader("Referer", "http://club.bankcomm.com/customer/flightcompanymemberbinding/list2.html");
        httpAsyncClient.execute(httpGet, new HttpAsyncCallback(bocomMileageEvent));
        
	}

	/**
	 * 	
	 * @Description: (方法职责详细描述,可空)  
	 * @Title: sendMsgUp 
	 * @param bocomMileageEvent
	 * @date 2016年4月27日 上午9:33:38  
	 * @author ws
	 * @throws UnsupportedEncodingException 
	*/
	private void sendMsgUp(BocomMileageEvent bocomMileageEvent) throws UnsupportedEncodingException {

		String url = String.format(sendMsgUrl, new Date().getTime());
		HttpPost httpPost = new HttpPost(url);

        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("memberCcard", "1"));
        httpPost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
        setHeader(url, httpPost, bocomMileageEvent);
        httpPost.setHeader("Referer", "https://creditcardapp.bankcomm.com/idm/sso/login.html?_channel=CLUB&service=http://club.bankcomm.com/customer/index.htm");
        httpAsyncClient.execute(httpPost, new HttpAsyncCallback(bocomMileageEvent));
		
	}

	/**
	 * 	
	 * @Description: (方法职责详细描述,可空)  
	 * @Title: queryCardUp 
	 * @param bocomMileageEvent
	 * @date 2016年4月27日 上午9:33:35  
	 * @author ws
	 * @throws UnsupportedEncodingException 
	*/
	private void queryCardUp(BocomMileageEvent bocomMileageEvent) throws UnsupportedEncodingException {

		String url = String.format(queryCardUrl, new Date().getTime());
		HttpPost httpPost = new HttpPost(url);

        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("memberCcard", "1"));
        httpPost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
        setHeader(url, httpPost, bocomMileageEvent);
        httpPost.setHeader("Referer", "https://creditcardapp.bankcomm.com/idm/sso/login.html?_channel=CLUB&service=http://club.bankcomm.com/customer/index.htm");
        httpAsyncClient.execute(httpPost, new HttpAsyncCallback(bocomMileageEvent));
		
	}

	/**
	 * 	
	 * @Description: (方法职责详细描述,可空)  
	 * @Title: checkLimitUp 
	 * @param bocomMileageEvent
	 * @date 2016年4月27日 上午9:33:30  
	 * @author ws
	 * @throws UnsupportedEncodingException 
	*/
	private void checkLimitUp(BocomMileageEvent bocomMileageEvent) throws UnsupportedEncodingException {

		String url = String.format(checkLimitUrl, new Date().getTime());
		HttpPost httpPost = new HttpPost(url);

        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("flightCompanyCode", bocomMileageEvent.getFlightCompanyCode()));
        httpPost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
        setHeader(url, httpPost, bocomMileageEvent);
        httpPost.setHeader("Referer", "https://creditcardapp.bankcomm.com/idm/sso/login.html?_channel=CLUB&service=http://club.bankcomm.com/customer/index.htm");
        httpAsyncClient.execute(httpPost, new HttpAsyncCallback(bocomMileageEvent));
		
	}

	/**
	 * 	
	 * @Description: (方法职责详细描述,可空)  
	 * @Title: bindMemberUp 
	 * @param bocomMileageEvent
	 * @date 2016年4月27日 上午9:33:28  
	 * @author ws
	*/
	private void bindMemberUp(BocomMileageEvent bocomMileageEvent) {
		String url = String.format(bindMemberUrl, new Date().getTime(),bocomMileageEvent.getMemberId(),bocomMileageEvent.getFlightCompanyCode(),bocomMileageEvent.getClubMemberId());
		
        HttpGet httpGet = new HttpGet(url);
        setHeader(url, httpGet, bocomMileageEvent);
        httpGet.setHeader("Referer", "http://club.bankcomm.com/customer/flightcompanymemberbinding/list2.html");
        httpAsyncClient.execute(httpGet, new HttpAsyncCallback(bocomMileageEvent));
	}

	/**
	 * 	
	 * @Description: (方法职责详细描述,可空)  
	 * @Title: removeBindUp 
	 * @param bocomMileageEvent
	 * @date 2016年4月27日 上午9:33:26  
	 * @author ws
	*/
	private void removeBindUp(BocomMileageEvent bocomMileageEvent) {
		
		String url = String.format(removeBindUrl, new Date().getTime(),bocomMileageEvent.getMemberId(),bocomMileageEvent.getFlightCompanyCode(),bocomMileageEvent.getRemoveId());
		
        HttpGet httpGet = new HttpGet(url);
        setHeader(url, httpGet, bocomMileageEvent);
        httpGet.setHeader("Referer", "http://club.bankcomm.com/customer/flightcompanymemberbinding/list2.html");
        httpAsyncClient.execute(httpGet, new HttpAsyncCallback(bocomMileageEvent));
	}

	private void setHeader(String uriStr, HttpMessage httpMessage, QueryEvent event) {
        httpMessage.setHeader("Accept", "*/*");
        httpMessage.setHeader("User-Agent", userAgent);
        try {
            CookieHelper.setCookies(uriStr, httpMessage, event.getCookieList());
        } catch (URISyntaxException e) {
            logger.error("set cookie fail {}", event.getId());
        }
    }

    /**
     * 请求首页
     *
     * @param BocomMileageEvent
     */
    private void indexUp(BocomMileageEvent BocomMileageEvent) {
        logger.debug("request index up {}", BocomMileageEvent.getId());
        HttpGet httpGet = new HttpGet(indexUrl + "?_channel=CLUB&service=http://club.bankcomm.com/customer/index.htm");
        setHeader(indexUrl, httpGet, BocomMileageEvent);
        httpGet.setHeader("Referer", "http://club.bankcomm.com/customer/index.htm");
        httpAsyncClient.execute(httpGet, new HttpAsyncCallback(BocomMileageEvent));
    }

    /**
     * 请求软键盘
     *
     * @param BocomMileageEvent
     */
    private void keyboardUp(BocomMileageEvent BocomMileageEvent) {
        logger.debug("request keyboard up {}", BocomMileageEvent.getId());
        Date d = new Date();
        HttpGet httpGet = new HttpGet(keyboardUrl + "?_=" + d.getTime());
        setHeader(keyboardUrl, httpGet, BocomMileageEvent);
        httpGet.setHeader("Referer", "https://creditcardapp.bankcomm.com/idm/sso/login.html?_channel=CLUB&service=http://club.bankcomm.com/customer/index.htm");
        httpAsyncClient.execute(httpGet, new HttpAsyncCallback(BocomMileageEvent));
    }

    /**
     * 请求登录
     *
     * @param BocomMileageEvent
     */
    private void loginUp(BocomMileageEvent BocomMileageEvent) throws UnsupportedEncodingException {
        logger.debug("request login up {}", BocomMileageEvent.getId());
        HttpPost httpPost = new HttpPost(loginUrl);

        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("lt", BocomMileageEvent.getLt()));
        params.add(new BasicNameValuePair("usernametype", "CARD"));
        params.add(new BasicNameValuePair("username", BocomMileageEvent.getAccount()));
        params.add(new BasicNameValuePair("password", "123456"));

        JSONArray jsonArray = BocomMileageEvent.getJsonArray();
        StringBuffer passwordseq = new StringBuffer();
        boolean isFirst = true;
        for (char c : BocomMileageEvent.getPassword().toCharArray()) {
            int index = Integer.valueOf(String.valueOf(c));
            if (isFirst) {
                passwordseq.append(jsonArray.get(index));
                isFirst = false;
                continue;
            }
            passwordseq.append("|").append(jsonArray.get(index));
        }
        logger.debug("passwordseq=" + passwordseq);

        params.add(new BasicNameValuePair("passwordseq", passwordseq.toString()));
        params.add(new BasicNameValuePair("accept", "1"));

        httpPost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));

        setHeader(loginUrl, httpPost, BocomMileageEvent);
        httpPost.setHeader("Referer", "https://creditcardapp.bankcomm.com/idm/sso/login.html?_channel=CLUB&service=http://club.bankcomm.com/customer/index.htm");
        httpAsyncClient.execute(httpPost, new HttpAsyncCallback(BocomMileageEvent));
    }

    /**
     * 请求查询积分页面
     *
     * @param BocomMileageEvent
     */
    private void queryIntegralUp(BocomMileageEvent BocomMileageEvent){
        logger.debug("request queryIntegral up {}", BocomMileageEvent.getId());
        HttpGet httpGet = new HttpGet(queryIntegralUrl);
        setHeader(queryIntegralUrl, httpGet, BocomMileageEvent);
        httpAsyncClient.execute(httpGet, new HttpAsyncCallback(BocomMileageEvent));
    }

    /**
     * 请求ticket
     *
     * @param BocomMileageEvent
     */
    private void ticketUp(BocomMileageEvent BocomMileageEvent) {
        logger.debug("request ticket up {}", BocomMileageEvent.getId());
        HttpGet httpGet = new HttpGet(BocomMileageEvent.getTicketUrl());
        setHeader("http://club.bankcomm.com/customer/index.htm", httpGet, BocomMileageEvent);
        httpAsyncClient.execute(httpGet, new HttpAsyncCallback(BocomMileageEvent));
    }

    /**
     * 请求check
     *
     * @param BocomMileageEvent
     */
    private void checkUp(BocomMileageEvent BocomMileageEvent) {
        logger.debug("request check up {}", BocomMileageEvent.getId());
        Date d = new Date();
        HttpGet httpGet = new HttpGet(checkUrl + "?loginCallBack=jQuery1720517492342274636_1454470966664&_=" + d.getTime());
        setHeader(checkUrl, httpGet, BocomMileageEvent);
        httpGet.setHeader("Referer", "http://club.bankcomm.com/customer/index.htm?ticket=" + BocomMileageEvent.getTicket());
        httpAsyncClient.execute(httpGet, new HttpAsyncCallback(BocomMileageEvent));
    }

    /**
     * 请求jauth
     *
     * @param BocomMileageEvent
     */
    private void jauthUp(BocomMileageEvent BocomMileageEvent) {
        logger.debug("request jauth up {}", BocomMileageEvent.getId());
        HttpGet httpGet = new HttpGet(jauthUrl + "?loginAuth=index.htm");
        setHeader(jauthUrl, httpGet, BocomMileageEvent);
        httpGet.setHeader("Referer", "http://club.bankcomm.com/customer/index.htm?ticket=" + BocomMileageEvent.getTicket());
        httpAsyncClient.execute(httpGet, new HttpAsyncCallback(BocomMileageEvent));
    }

    /**
     * 请求login1
     *
     * @param BocomMileageEvent
     */
    private void login1Up(BocomMileageEvent BocomMileageEvent) {
        logger.debug("request login1 up {}", BocomMileageEvent.getId());
        HttpGet httpGet = new HttpGet(BocomMileageEvent.getLoginUrl());
        setHeader(login1Url, httpGet, BocomMileageEvent);
        httpGet.setHeader("Referer", "http://club.bankcomm.com/customer/index.htm?ticket=" + BocomMileageEvent.getTicket());
        httpAsyncClient.execute(httpGet, new HttpAsyncCallback(BocomMileageEvent));
    }

    /**
     * 请求jsecurity
     *
     * @param BocomMileageEvent
     */
    private void jsecurityUp(BocomMileageEvent BocomMileageEvent) {
        logger.debug("request jsecurity up {}", BocomMileageEvent.getId());
        HttpGet httpGet = new HttpGet(BocomMileageEvent.getJsecurityUrl());
        setHeader("http://club.bankcomm.com/customer/j_security_check", httpGet, BocomMileageEvent);
        httpGet.setHeader("Referer", "http://club.bankcomm.com/customer/index.htm?ticket=" + BocomMileageEvent.getTicket());
        httpAsyncClient.execute(httpGet, new HttpAsyncCallback(BocomMileageEvent));
    }

    /**
     * 请求jauth1
     *
     * @param BocomMileageEvent
     */
    private void jauth1Up(BocomMileageEvent BocomMileageEvent) {
        logger.debug("request jauth1 up {}", BocomMileageEvent.getId());
        HttpGet httpGet = new HttpGet(BocomMileageEvent.getJauthUrl());
        setHeader("http://club.bankcomm.com/customer/j_auth.html", httpGet, BocomMileageEvent);
        httpGet.setHeader("Referer", "http://club.bankcomm.com/customer/index.htm?ticket=" + BocomMileageEvent.getTicket());
        httpAsyncClient.execute(httpGet, new HttpAsyncCallback(BocomMileageEvent));
    }


    @Override
    public void errorHandle(QueryEvent event) {
        DeferredResult<Object> deferredResult = event.getDeferredResult();
        if (deferredResult == null) {
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
                deferredResult.setResult((new SpiderException(-1, exception
                        .getMessage()).toString()));
            }
        }
    }

    private class HttpAsyncCallback implements FutureCallback<HttpResponse> {
        private BocomMileageEvent event;
        private boolean skipNextStep = false;

        public HttpAsyncCallback(BocomMileageEvent event) {
            this.event = event;
        }

        @Override
        public boolean equals(Object obj) {
            return super.equals(obj);
        }

        @Override
        public void completed(HttpResponse result) {
            try {
                CookieHelper.getCookies(event.getCookieList(), result);
                switch (event.getState()) {
                    case INDEX:
                        indexDown(result);
                        break;
                    case KEYBOARD:
                        keyboardDown(result);
                        break;
                    case LOGIN:
                        loginDown(result);
                        break;
                    case TICKET:
                        ticketDown(result);
                        break;
                    case CHECK:
                        checkDown(result);
                        break;
                    case JAUTH:
                        jauthDown(result);
                        break;
                    case LOGIN1:
                        login1Down(result);
                        break;
                    case JSECURITY:
                        jsecurityDown(result);
                        break;
                    case JAUTH1:
                        jauth1Down(result);
                        break;

                    case CONVERT_PAGE:
                        convertPageDown(result);
                        break;
                    case REMOVE_BIND:
                        removeBindDown(result);
                        break;
                    case BIND_MEMBER:
                        bindMemberDown(result);
                        break;
                    case CHECK_LIMIT:
                        checkLimitDown(result);
                        break;
                    case QUERY_CARD:
                        queryCardDown(result);
                        break;
                    case SEND_MSG:
                        sendMsgDown(result);
                        break;
                    case CONVERT_MILEAGE:
                        convertMileageDown(result);
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
		 * 	
		 * @Description: (方法职责详细描述,可空)  
		 * @Title: convertMileageDown 
		 * @param result
		 * @date 2016年4月27日 上午10:06:24  
		 * @author ws
		*/
		private void convertMileageDown(HttpResponse result) {
			try {
				String res = EntityUtils.toString(result.getEntity());
				JSONObject obj = JSON.parseObject(res);
				String key = String.format(Constant.BOCOM_MILEAGE_KEY, event.getUserid());
	            redis.set(key, JSON.toJSONString(event), 600);
				
				if(null != obj.getInteger("success") && 0 == obj.getInteger("success")){//兑换成功
					event.setException(new SpiderException(0, "兑换成功"));
				}else {
					String error = obj.getString("error");
					if(StringUtils.isNotBlank(error)){
						if(error.contains("验证码错误")){//如果是短信验证码错误
							event.setException(new SpiderException(3005, error));
						}else if(error.contains("卡片有效期")){
							event.setException(new SpiderException(3006, error));
						}else if(error.contains("持卡人姓名不符")){
							event.setException(new SpiderException(3004, "您输入的航空会员名与持卡人姓名不符，请重新输入"));
						}else{
							event.setException(new SpiderException(3004, error));
						}
					}else{
						event.setException(new SpiderException(3002, "兑换失败"));
					}
				}
			} catch (Exception e) {
				logger.warn("兑换里程异常,{}", e);
				event.setException(new SpiderException(3002, "系统繁忙"));
			}
		}

		/**
		 * 	
		 * @Description: (方法职责详细描述,可空)  
		 * @Title: sendMsgDown 
		 * @param result
		 * @date 2016年4月27日 上午10:06:21  
		 * @author ws
		*/
		private void sendMsgDown(HttpResponse result) {
			try {
				String res = EntityUtils.toString(result.getEntity());
				JSONObject obj = JSON.parseObject(res);
				
				if(null != obj.getInteger("success") && 0 == obj.getInteger("success")){//发送验证码成功
					
					String resp = obj.getString("resp");
					JSONObject respObj = JSON.parseObject(resp);
					String mobTel = respObj.getString("mobTel");
					
					String key = String.format(Constant.BOCOM_MILEAGE_KEY, event.getUserid());
		            redis.set(key, JSON.toJSONString(event), 600);
					event.setException(new SpiderException(0, "短信验证码发送成功",mobTel));
				}else {
					String error = obj.getString("error");
					if(StringUtils.isNotBlank(error)){
						event.setException(new SpiderException(3004, error));
					}else{
						event.setException(new SpiderException(3002, "短信验证码发送失败"));
					}
				}
			} catch (Exception e) {
				logger.warn("发送短信验证码异常,{}", e);
				event.setException(new SpiderException(3002, "系统繁忙"));
			}
		}

		/**
		 * 	
		 * @Description: (方法职责详细描述,可空)  
		 * @Title: queryCardDown 
		 * @param result
		 * @date 2016年4月27日 上午10:06:19  
		 * @author ws
		*/
		private void queryCardDown(HttpResponse result) {
			try {
				String res = EntityUtils.toString(result.getEntity());
				JSONObject obj = JSON.parseObject(res);
				JSONObject resultObj = JSON.parseObject(obj.getString("result"));
				
				if(null != obj.getInteger("useBonusLimit") && 1 == resultObj.getInteger("useBonusLimit")){
					String key = String.format(Constant.BOCOM_MILEAGE_KEY, event.getUserid());
		            redis.set(key, JSON.toJSONString(event), 600);
					event.setException(new SpiderException(3004, "您当年兑换的里程已超过上限"));
				}else {
					event.setMsgCode("123456");//输入错误的验证码校验其他输入项是否正确
					event.setState(BocomMileageState.CONVERT_MILEAGE);
				}
			} catch (Exception e) {
				logger.warn("验证可兑换里程异常,{}", e);
				event.setException(new SpiderException(3002, "系统繁忙"));
			}
		}

		/**
		 * 	
		 * @Description: (方法职责详细描述,可空)  
		 * @Title: checkLimitDown 
		 * @param result
		 * @date 2016年4月27日 上午10:06:15  
		 * @author ws
		*/
		private void checkLimitDown(HttpResponse result) {
			try {
				String res = EntityUtils.toString(result.getEntity());
				JSONObject obj = JSON.parseObject(res);
				
				if(null != obj.getString("success") && "true".equals(obj.getString("success"))){
					event.setState(BocomMileageState.QUERY_CARD);
				}else {
					String key = String.format(Constant.BOCOM_MILEAGE_KEY, event.getUserid());
		            redis.set(key, JSON.toJSONString(event), 600);
					event.setException(new SpiderException(3004, "对不起，该航空公司本月剩余可兑换里程数不足，暂无法完成兑换！"));
				}
			} catch (Exception e) {
				logger.warn("校验航空公司可兑换里程异常,{}", e);
				event.setException(new SpiderException(3002, "系统繁忙"));
			}
		}

		/**
		 * 	
		 * @Description: (方法职责详细描述,可空)  
		 * @Title: bindMemberDown 
		 * @param result
		 * @date 2016年4月27日 上午10:06:10  
		 * @author ws
		*/
		private void bindMemberDown(HttpResponse result) {
			try {
				String res = EntityUtils.toString(result.getEntity());
				JSONObject obj = JSON.parseObject(res);
				
				if(null != obj.getInteger("isBinding") && 1 == obj.getInteger("isBinding")){
					event.setState(BocomMileageState.CHECK_LIMIT);
				}else if(null != obj.getInteger("isBinding") && 0 == obj.getInteger("isBinding")){
					String key = String.format(Constant.BOCOM_MILEAGE_KEY, event.getUserid());
		            redis.set(key, JSON.toJSONString(event), 600);
					event.setException(new SpiderException(3004, "该航空会员卡已被其他会员绑定，无法重复绑定。"));
				}
			} catch (Exception e) {
				logger.warn("绑定航空会员异常,{}", e);
				event.setException(new SpiderException(3002, "系统繁忙"));
			}

		}

		/**
		 * 	
		 * @Description: (方法职责详细描述,可空)  
		 * @Title: removeBindDown 
		 * @param result
		 * @date 2016年4月27日 上午10:06:08  
		 * @author ws
		*/
		private void removeBindDown(HttpResponse result) {
			try {
				String res = EntityUtils.toString(result.getEntity());
				JSONObject obj = JSON.parseObject(res);
				
				if(null != obj.getInteger("success") && 1 == obj.getInteger("success")){//解绑成功，绑定新号
					event.setState(BocomMileageState.BIND_MEMBER);
				}else {
					logger.warn("解绑失败");
					String key = String.format(Constant.BOCOM_MILEAGE_KEY, event.getUserid());
		            redis.set(key, JSON.toJSONString(event), 600);
					event.setException(new SpiderException(3002, "解绑失败"));
				}
			} catch (Exception e) {
				logger.warn("解绑航空会员异常,{}", e);
				event.setException(new SpiderException(3002, "系统繁忙"));
			}
		}

		/**
		 * 	
		 * @Description: (方法职责详细描述,可空)  
		 * @Title: convertPageDown 
		 * @param result
		 * @date 2016年4月27日 上午10:06:06  
		 * @author ws
		*/
		private void convertPageDown(HttpResponse result) {
			try {
				String resPage = EntityUtils.toString(result
		                .getEntity());
				List<FlightCompany> flights = new ArrayList<FlightCompany>();
				//获取已绑定航空公司
				flights = getFlights(resPage);
				
				//获取交大会员号
				String clubMemberId = getLoginParam(resPage,"input","clubMemberId");
				if(StringUtils.isNotBlank(clubMemberId)){

					event.setClubMemberId(clubMemberId);
					
					for (FlightCompany flightCompany : flights) {
						if(flightCompany.getFlightCode().equals(event.getFlightCompanyCode())){
							if(flightCompany.getMemberId().equals(event.getMemberId())){
								//已绑定，不需绑定，直接兑换
								event.setState(BocomMileageState.CHECK_LIMIT);
								return;
							}else{
								//已绑定相同的航空公司，解绑
								event.setRemoveId(flightCompany.getRemoveId());
								event.setState(BocomMileageState.REMOVE_BIND);
								return;
							}
						}
					}
					//不存在，则绑定
					event.setState(BocomMileageState.BIND_MEMBER);
				}else{
					String key = String.format(Constant.BOCOM_MILEAGE_KEY, event.getUserid());
		            redis.set(key, JSON.toJSONString(event), 600);
					logger.warn("未获取到clubMemberId");
					event.setException(new SpiderException(3002, "未获取到clubMemberId"));
				}
			} catch (Exception e) {
				logger.warn("获取clubMemberId异常,{}", e);
				event.setException(new SpiderException(3002, "系统繁忙"));
			} 
		}

		private List<FlightCompany> getFlights(String resPage) {
			List<FlightCompany> flights = new ArrayList<FlightCompany>();
			String fileDir = XpathHtmlUtils.deleteHeadHtml(resPage);
			Document doc = Jsoup.parse(fileDir);
			Elements es = doc.getElementsByClass("s_guanli_2").get(0)
			      .getElementsByTag("table").get(0)
			      .getElementsByTag("tbody").get(0).getAllElements();
			for (Element e : es) {
			    String onclick = e.attr("onclick");
			    if(onclick.contains("removeBinding")){
			      String params = e.attr("onclick").substring(onclick.indexOf("(")+1, onclick.indexOf(")"));
			      String[] flightParam = params.split(",");
			      if(flightParam.length == 4){
			    	  FlightCompany flight = new FlightCompany();
			    	  flight.setMemberId(flightParam[0].substring(1,flightParam[0].length()-1));
			    	  flight.setFlightCode(flightParam[1].substring(1,flightParam[1].length()-1));
			    	  flight.setRemoveId(flightParam[2].substring(1,flightParam[2].length()-1));
			    	  flight.setFlightName(flightParam[3].substring(1,flightParam[3].length()-1));
			    	  flights.add(flight);
			      }
			    }
			}
			return flights;
		}

		/**
         * 获取登录所需参数
         *
         * @param loginPage
         * @return
         * @Description: (方法职责详细描述, 可空)
         * @Title: getLoginParam
         * @date 2015年11月24日 上午11:23:36
         * @author ws
		 * @throws Exception 
         */
        private String getLoginParam(String loginPage,String tagElement,String tagetName){
        	logger.info("getLoginParam {}", event.getId());

            try {
            	loginPage = XpathHtmlUtils.cleanHtml(loginPage);
            	Document doc = Jsoup.parse(loginPage);
            	Elements elements = doc.getElementsByTag(tagElement);
            	for (Element element : elements) {
            		String type = element.attr("type");
                    String name = element.attr("name");
                    if ("hidden".equals(type) && StringUtils.isNotBlank(name) && name.equals(tagetName)) {
                        return element.attr("value");
                    }
				}
                return "";
            } catch (Exception e) {
				logger.warn("获取登录信息失败", e);
                throw e;
            }
        }
		
		
		@Override
        public void failed(Exception ex) {

        }

        @Override
        public void cancelled() {

        }

        /**
         * 首页请求完成
         *
         * @param result
         */
        private void indexDown(HttpResponse result) throws IOException {
            logger.debug("request index down {}", event.getId());
            event.setState(BocomMileageState.KEYBOARD);
        }

        /**
         * 软键盘请求完成
         *
         * @param result
         */
        private void keyboardDown(HttpResponse result) throws IOException {
            logger.debug("request keyboard down {}", event.getId());
            String resultStr = EntityUtils.toString(result.getEntity());
            logger.debug(resultStr);
            JSONObject jsonObject = JSON.parseObject(resultStr);
            JSONArray jsonArray = jsonObject.getJSONArray("keys");
            event.setJsonArray(jsonArray);
            event.setState(BocomMileageState.LOGIN);
        }

        /**
         * 请求登录完成
         *
         * @param result
         */
        private void loginDown(HttpResponse result) throws IOException {
            logger.debug("request login down {}", event.getId());
            Header[] headers = result.getHeaders("Location");
            if (headers.length == 0) {
                String resultStr = EntityUtils.toString(result.getEntity());
                if(resultStr.contains("查询密码和信用卡号不匹配")){
                    event.setException(new SpiderException(2113, "查询密码和信用卡号不匹配"));
                    return;
                }else if(resultStr.contains("卡片查询密码已被锁定")){
                    event.setException(new SpiderException(2115, "卡片查询密码已被锁定"));
                    return;
                }else if(resultStr.contains("您的登录密码输入已超过限制，请24小时后再试")){
                    event.setException(new SpiderException(2116, "登录密码输入已超过限制，请24小时后再试"));
                    return;
                }
                event.setException(new SpiderException(2107, "登录失败"));
                return;
            }
            event.setTicketUrl(headers[0].getValue());
            int pos = event.getTicketUrl().indexOf("ticket=");
            String ticket = event.getTicketUrl().substring(pos + 7);
            logger.debug(ticket);
            event.setTicket(ticket);
            event.setState(BocomMileageState.TICKET);
        }

        /**
         * ticket
         *
         * @param result
         */
        private void ticketDown(HttpResponse result) {
            logger.debug("request ticket down {}", event.getId());
            event.setState(BocomMileageState.CHECK);
        }

        /**
         * check
         *
         * @param result
         */
        private void checkDown(HttpResponse result) {
            logger.debug("request check down {}", event.getId());
            event.setState(BocomMileageState.JAUTH);
        }

        /**
         * jauth
         *
         * @param result
         */
        private void jauthDown(HttpResponse result) {
            logger.debug("request jauth down {}", event.getId());
            Header[] headers = result.getHeaders("Location");
            event.setLoginUrl(headers[0].getValue());
            event.setState(BocomMileageState.LOGIN1);
        }

        /**
         * login1
         *
         * @param result
         */
        private void login1Down(HttpResponse result) {
            logger.debug("request login1 down {}", event.getId());
            Header[] headers = result.getHeaders("Location");
            event.setJsecurityUrl(headers[0].getValue());
            event.setState(BocomMileageState.JSECURITY);
        }

        /**
         * jsecurity
         *
         * @param result
         */
        private void jsecurityDown(HttpResponse result) {
            logger.debug("request jsecurity down {}", event.getId());
            Header[] headers = result.getHeaders("Location");
            event.setJauthUrl(headers[0].getValue());
            event.setState(BocomMileageState.JAUTH1);
        }

        /**
         * jauth1
         *
         * @param result
         */
        private void jauth1Down(HttpResponse result) {
            logger.debug("request jauth1 down {}", event.getId());
            //绑定航空公司
            event.setState(BocomMileageState.CONVERT_PAGE);
        }

        private void errorHandle(BocomMileageEvent event) {
            DeferredResult<Object> deferredResult = event.getDeferredResult();
            if (deferredResult == null) {
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
    }
}