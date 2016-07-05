/*
 * Copyright (c) 2015-2020 by caitu99
 * All rights reserved.
 */
package com.caitu99.lsp.spider.pingan;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.script.ScriptException;

import org.apache.http.HttpEntity;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.request.async.DeferredResult;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.caitu99.lsp.AppConfig;
import com.caitu99.lsp.Constant;
import com.caitu99.lsp.cache.RedisOperate;
import com.caitu99.lsp.exception.SpiderException;
import com.caitu99.lsp.model.spider.QueryEvent;
import com.caitu99.lsp.model.spider.pingan.PingAnxykSpiderEvent;
import com.caitu99.lsp.model.spider.pingan.PingAnxykSpiderState;
import com.caitu99.lsp.spider.HttpAsyncClient;
import com.caitu99.lsp.spider.QuerySpider;
import com.caitu99.lsp.utils.ScriptHelper;
import com.caitu99.lsp.utils.SpringContext;
import com.caitu99.lsp.utils.UrlParams;
import com.caitu99.lsp.utils.cookie.CookieHelper;
import com.caitu99.lsp.utils.cookie.HttpCookieEx;

/** 
 * 
 * @Description: (类职责详细描述,可空) 
 * @ClassName: PingAnxykSpider 
 * @author fangjunxiao
 * @date 2016年4月11日 下午4:37:49 
 * @Copyright (c) 2015-2020 by caitu99 
 */
public class PingAnxykSpider implements QuerySpider{

	private static final Logger logger = LoggerFactory
            .getLogger(PingAnSpider.class);

	private RedisOperate redis = SpringContext.getBean(RedisOperate.class);
	
	private CloseableHttpAsyncClient httpAsyncClient = HttpAsyncClient.getInstance();
	
    private AppConfig appConfig = SpringContext.getBean(AppConfig.class);
	
	private static final String userAgent = "Mozilla/5.0 (Windows NT 6.3; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/46.0.2490.86 Safari/537.36";
	  
	
    
	@Override
	public void onEvent(QueryEvent event) {
		PingAnxykSpiderEvent pingAnEvent = (PingAnxykSpiderEvent)event;
		try {
			switch (pingAnEvent.getState()) {
				
			case INIT_XYK:
				initUp(pingAnEvent);
				break;
			case IMG_XYK:
				imgUp(pingAnEvent);
				break;
				
				
			case LOGIN_XYK:
				loginUp(pingAnEvent);
				break;	
			case DIRECT_XYK:
				directUp(pingAnEvent);
				break;		
			case CAR_NAME_XYK:
				carNmaeUp(pingAnEvent);
				break;	
			case QUERY_JF_XYK:
				queryJfUp(pingAnEvent);
				break;	
				
				
				
			case ERROR:
                errorHandle(event);
                break;
			}
		} catch (Exception e) {
			logger.error("request up error {}", event.getId(), e);
			pingAnEvent.setException(e);
            errorHandle(pingAnEvent);
		}
	}
	
	
	
	private void initUp(PingAnxykSpiderEvent pingAnEvent){
		logger.debug("initspUp {}", pingAnEvent.getAccount());
		String url = "https://m.pingan.com/xinyongka/index.screen";
        HttpGet httpGet = new HttpGet(url);
        setHeader(url, httpGet, pingAnEvent);
        httpAsyncClient.execute(httpGet, new HttpAsyncCallback(pingAnEvent));
	}
	
	
	private void imgUp(PingAnxykSpiderEvent pingAnEvent){
		logger.debug("initspUp {}", pingAnEvent.getAccount());
		String imgpn = "https://m.pingan.com/xinyongka/ImageGif.do?rd=%s";
		double  t = new Random().nextDouble();
		String url = String.format(imgpn, String.valueOf(t));
		
        HttpGet httpGet = new HttpGet(url);
        setHeader(url, httpGet, pingAnEvent);
        httpAsyncClient.execute(httpGet, new HttpAsyncCallback(pingAnEvent));
	}
	
	

	private void loginUp(PingAnxykSpiderEvent pingAnEvent){
		logger.debug("loginUp {}", pingAnEvent.getAccount());
		String path = "https://m.pingan.com/xinyongka/toLogin.do?menuType=accountInfo&random=%s";
		double  t = new Random().nextDouble();
		String url = String.format(path, String.valueOf(t));
        HttpPost httpPost = new HttpPost(url);
        String account = pingAnEvent.getAccount();
        String password = pingAnEvent.getPassword();
        String imgcode = pingAnEvent.getvCode();
        try {
        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("userId", account));
        params.add(new BasicNameValuePair("pwd", password));
        params.add(new BasicNameValuePair("rndCode", imgcode));
        params.add(new BasicNameValuePair("rmbUserId", ""));

    
		httpPost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
		} catch (Exception e) {
			   logger.error("loginUp error {}", pingAnEvent.getAccount(), e);
			   pingAnEvent.setException(e);
	            return;
		} 
        
        setHeader(url, httpPost, pingAnEvent);
        httpAsyncClient.execute(httpPost, new HttpAsyncCallback(pingAnEvent));	
	}
	
	private void directUp(PingAnxykSpiderEvent pingAnEvent){
		logger.debug("directUp {}", pingAnEvent.getAccount());
		String url = pingAnEvent.getRedirectURL();
        HttpGet httpGet = new HttpGet(url);
        setHeader(url, httpGet, pingAnEvent);
        httpAsyncClient.execute(httpGet, new HttpAsyncCallback(pingAnEvent));
	}
	
	private void carNmaeUp(PingAnxykSpiderEvent pingAnEvent){
		logger.debug("carNmaeUp {}", pingAnEvent.getAccount());
		String path = "https://wap-ebank.pingan.com/xinyongka/index.do?key=%s&hostId=%s&toaType=%s&operationType=getOriginalCardAcctInfoList&random=%s&menuName=accountInfo";
       
		String key = pingAnEvent.getKey();
		String hostId = pingAnEvent.getHostId();
		String toaType = pingAnEvent.getToaType();
		double  t = new Random().nextDouble();
		
		String url = String.format(path,key,hostId,toaType,String.valueOf(t));
		
		HttpGet httpGet = new HttpGet(url);
        setHeader(url, httpGet, pingAnEvent);
        httpAsyncClient.execute(httpGet, new HttpAsyncCallback(pingAnEvent));
	}
	
	
	private void queryJfUp(PingAnxykSpiderEvent pingAnEvent){
		logger.debug("loginDirectUp {}", pingAnEvent.getAccount());
		String path = "https://wap-ebank.pingan.com/xinyongka/index.do?key=%s&hostId=%s&toaType=%s&operationType=getWanLiTongPoint&random=%s";
		String key = pingAnEvent.getKey();
		String hostId = pingAnEvent.getHostId();
		String toaType = pingAnEvent.getToaType();
		double  t = new Random().nextDouble();
		
		String url = String.format(path,key,hostId,toaType,String.valueOf(t));
		
		HttpGet httpGet = new HttpGet(url);
        setHeader(url, httpGet, pingAnEvent);
        httpAsyncClient.execute(httpGet, new HttpAsyncCallback(pingAnEvent));
	}
	
	private void setHeader(String uriStr, HttpMessage httpMessage, QueryEvent event) {
        httpMessage.setHeader("Accept", "*/*");
        httpMessage.setHeader("User-Agent", userAgent);
        try {
            CookieHelper.setCookies2(uriStr, httpMessage, event.getCookieList());
        } catch (URISyntaxException e) {
            logger.error("set cookie fail {}", event.getId());
        }
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

		private PingAnxykSpiderEvent event;
        private boolean skipNextStep = false;

		public HttpAsyncCallback(PingAnxykSpiderEvent event) {
			super();
			this.event = event;
		}

		
		
		@Override
		public void completed(HttpResponse response) {
			try {
				CookieHelper.getCookiesFresh(event.getCookieList(), response);
				switch (event.getState()) {
				case INIT_XYK:
					initDown(response);
					break;
				case IMG_XYK:
					imgDown(response);
					break;
					
					
				case LOGIN_XYK:
					loginDown(response);
					break;	
				case DIRECT_XYK:
					directDown(response);
					break;	
				case CAR_NAME_XYK:
					carNmaeDown(response);
					break;	
				case QUERY_JF_XYK:
					queryJfDown(response);
					break;	
			
				default:
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
		
		
		private void initDown(HttpResponse response){
	            event.setState(PingAnxykSpiderState.IMG_XYK);
		}
		
		
		private void imgDown(HttpResponse response){
			try {
		            HttpEntity httpEntity = response.getEntity();
	                InputStream imgStream = httpEntity.getContent();
	                ByteArrayOutputStream baos = new ByteArrayOutputStream();
	                int rd = -1;
	                byte[] bytes = new byte[1024];
	                while ((rd = imgStream.read(bytes)) != -1) {
	                    baos.write(bytes, 0, rd);
	                }
	                byte[] rbyte = baos.toByteArray();
	                String imgStr = Base64.getEncoder().encodeToString(rbyte);
	                if (appConfig.inDevMode()) {
	                    byte[] tbytes = Base64.getDecoder().decode(imgStr);
	                    FileOutputStream fs = new FileOutputStream(
	                            appConfig.getUploadPath() + "/" + event.getUserid()
	                                    + ".jpg");
	                    fs.write(tbytes);
	                    fs.close();
	                }
	                
	                String key = String.format(Constant.XYK_PINGAN_LOGIN_KEY, event.getUserid());
	                redis.set(key, JSON.toJSONString(event), 600);
	                
	                event.setException(new SpiderException(1001, "输入验证码", imgStr));
			} catch (Exception e) {
              logger.error("get img down exception", e);
              e.printStackTrace();
              event.setException(new SpiderException(2517,"获取验证码失败"));
			}
		}
		
		
		private void loginDown(HttpResponse response){
			try {
				HttpEntity entity = response.getEntity();
	            String entityStr = EntityUtils.toString(entity);
	            
	            JSONObject jobj = JSON.parseObject(entityStr);
	            String ret_code = (String) jobj.get("ret_code");
	            if("0000".equals(ret_code)){
	            	String redirectURL = (String) jobj.get("redirectURL");
	            	UrlParams up= new UrlParams(redirectURL);
	            	String key = up.getVal("key");
	            	String hostId = up.getVal("hostId");
	            	String toaType = up.getVal("toaType");
	            	
	            	event.setKey(key);
	            	event.setHostId(hostId);
	            	event.setToaType(toaType);
	            	event.setRedirectURL(redirectURL);
	            	event.setState(PingAnxykSpiderState.DIRECT_XYK);
	            }else if("1111".equals(ret_code)){
	            	event.setException(new SpiderException(1111, "用户名密码错误"));
	            }else if("1112".equals(ret_code)){
	            	event.setException(new SpiderException(1112, "验证码错误"));
	            }else{
	            	String msg = (String) jobj.get("msg");
	            	event.setException(new SpiderException(2001, msg));
	            }
			} catch (Exception e) {
			       logger.error("get loginInitspDown down exception", e);
	               e.printStackTrace();
				   event.setException(new SpiderException(-1,"服务器繁忙"));
			}
		}
		
		
		
		
		private void directDown(HttpResponse response){
	            event.setState(PingAnxykSpiderState.CAR_NAME_XYK);
		}
		
	
		private void carNmaeDown(HttpResponse response){
			try {
				HttpEntity entity = response.getEntity();
	            String entityStr = EntityUtils.toString(entity);
	            
	            JSONObject jobj = JSON.parseObject(entityStr);
	            String ret_code = (String) jobj.get("ret_code");
	            if("000".equals(ret_code)){
	            	JSONObject customer =  (JSONObject) jobj.get("CUSTOMER");
	            	String userName = (String) customer.get("userName");
	            	JSONObject detail =  (JSONObject) jobj.get("detail");
	            	String maskMasterCardNo = (String) detail.get("maskMasterCardNo");
	            	
	            	event.setUserName(userName);
	            	event.setMaskMasterCardNo(maskMasterCardNo);
	            	event.setState(PingAnxykSpiderState.QUERY_JF_XYK);
	            }else{
	            	String ret_message = (String) jobj.get("ret_message");
	            	event.setException(new SpiderException(2003, ret_message));
	            }
	            
			} catch (Exception e) {
			       logger.error("get loginInitspDown down exception", e);
	               e.printStackTrace();
				   event.setException(new SpiderException(-1,"服务器繁忙"));
			}
		}
		
		
		private void queryJfDown(HttpResponse response){
			try {
				HttpEntity entity = response.getEntity();
	            String entityStr = EntityUtils.toString(entity);
	            JSONObject jobj = JSON.parseObject(entityStr);
	            String ret_code = (String) jobj.get("ret_code");
	            if("000".equals(ret_code)){
	            	String jf = (String) jobj.get("wanLiTongCurrentPoint");
	            	
	                Map<String,String> map = new HashMap<String, String>();
	                map.put("jf", jf);
	                map.put("name", event.getUserName());
	                map.put("cardNo", event.getMaskMasterCardNo());
	                event.setException(new SpiderException(0, "查询积分成功",JSON.toJSONString(map)));
	            	
	            }else{
	            	String ret_message = (String) jobj.get("ret_message");
	            	event.setException(new SpiderException(2004, ret_message));
	            }
	         
			} catch (Exception e) {
			       logger.error("get loginInitspDown down exception", e);
	               e.printStackTrace();
				   event.setException(new SpiderException(-1,"服务器繁忙"));
			}
		}
		
		
		
		
		
		@Override
		public void failed(Exception ex) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void cancelled() {
			// TODO Auto-generated method stub
			
		}
		
		
		
		
	}
	
	

}
