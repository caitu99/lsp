/*
 * Copyright (c) 2015-2020 by caitu99
 * All rights reserved.
 */
package com.caitu99.lsp.spider.wumart;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;

import com.caitu99.lsp.model.spider.MailSpiderEvent;
import com.caitu99.lsp.utils.XpathHtmlUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpMessage;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

import com.alibaba.fastjson.JSON;
import com.caitu99.lsp.Constant;
import com.caitu99.lsp.exception.SpiderException;
import com.caitu99.lsp.model.spider.wumart.WumartSpiderEvent;
import com.caitu99.lsp.model.spider.wumart.WumartSpiderState;
import com.caitu99.lsp.spider.AbstractMailSpider;
import com.caitu99.lsp.spider.MailBodySpider;
import com.caitu99.lsp.spider.MailParserTask;
import com.caitu99.lsp.utils.cookie.CookieHelper;


/** 
 * 
 * @Description: (类职责详细描述,可空) 
 * @ClassName: WucarSpider 
 * @author fangjunxiao
 * @date 2015年12月11日 下午3:46:27 
 * @Copyright (c) 2015-2020 by caitu99 
 */
public class WumartSpider extends AbstractMailSpider{
	
	private final static Logger logger = LoggerFactory.getLogger(WumartSpider.class);
	
	private boolean skipNextStep = false;
	
	
	private static final String YZM_URL = "http://www.wumart.com/login.php/secode?";
	
	private static final String LOGIN_URL = "http://www.wumart.com/login.php";
	
	private static final String NAME_URL = "http://www.wumart.com/Jsearch.php?city=%s";
	
	private static final String CHECK_URL = "http://www.wumart.com/repw.php";
	
	private static final String SMS_URL = "http://www.wumart.com/mescode.php";
	
	private static final String MODIFY_URL = "http://www.wumart.com/repw.php?city=%s";
	
	/** 
	 * @Title:  
	 * @Description:
	 * @param mailBodySpider
	 * @param mailParser 
	 */
	public WumartSpider(MailBodySpider mailBodySpider, MailParserTask mailParser) {
		super(mailBodySpider, mailParser);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void onEvent(MailSpiderEvent event) {
		
		WumartSpiderEvent wumartEvent = (WumartSpiderEvent) event;
		
		try {
			switch (wumartEvent.getState()) {
			case IMGCODE:
				getImgcode(wumartEvent);
				break;
			case LOGIN:
				loginUp(wumartEvent);
				break;
			case GETN:
				getnUp(wumartEvent);
				break;
			case CHECK:
				checkUp(wumartEvent);
				break;
			case GETSMS:
				getSms(wumartEvent);
				break;
			case MODIFY:
				getModify(wumartEvent);
				break;				
			case ERROR:
				errorHandle(wumartEvent);
				break;
			}
		} catch (Exception e) {
			logger.error("request up error {}", wumartEvent.getId(), e);
			wumartEvent.setException(e);
			errorHandle(event);
		}
		
	}

	@Override
	protected void setHeader(String uriStr, HttpMessage httpGet,
			MailSpiderEvent event) {
        httpGet.setHeader("Accept", "*/*");
        httpGet.setHeader("User-Agent", USERAGENT_CHROME);
        try {
            CookieHelper.setCookies(uriStr, httpGet, event.getCookieList());
        } catch (URISyntaxException e) {
            logger.error("set cookie fail {}", event.getId());
        }
		
	}
	
	
	private void getImgcode(WumartSpiderEvent event){
		logger.debug("do getImgcode {}", event.getId());
		String s = String.format(YZM_URL, new Date().getTime());
		HttpGet httpGet = new HttpGet(s);
		setHeader(s, httpGet, event);
		httpAsyncClient.execute(httpGet, new HttpAsyncCallback(event));
		
	}
	
	private void checkUp(WumartSpiderEvent event){
		logger.debug("do checkUp {}", event.getId());
		HttpPost httpPost = new HttpPost(CHECK_URL);
		String vcode = event.getVcode();
		setHeader(CHECK_URL, httpPost, event);
		setHeader(httpPost);
		
		List<NameValuePair> nvps = new ArrayList<NameValuePair>();
		
        nvps.add(new BasicNameValuePair("yzm", vcode));//验证码
        try {
			httpPost.setEntity(new UrlEncodedFormEntity(nvps, "UTF-8"));
		} catch (UnsupportedEncodingException e) {
			logger.error("login eventId:{} error {}", event.getId(), e);
			event.setException(e);
			return;
		}
		httpAsyncClient.execute(httpPost, new HttpAsyncCallback(event));
	}
	
	
	private void getSms(WumartSpiderEvent event){
		logger.debug("do getSms {}", event.getId());
		HttpPost httpPost = new HttpPost(SMS_URL);
		String account = event.getAccount();
		setHeader(SMS_URL, httpPost, event);
		setHeader(httpPost);
		
		List<NameValuePair> nvps = new ArrayList<NameValuePair>();
		
        nvps.add(new BasicNameValuePair("tel", account));//手机号
        try {
			httpPost.setEntity(new UrlEncodedFormEntity(nvps, "UTF-8"));
		} catch (UnsupportedEncodingException e) {
			logger.error("login eventId:{} error {}", event.getId(), e);
			event.setException(e);
			return;
		}
		httpAsyncClient.execute(httpPost, new HttpAsyncCallback(event));
	}
	
	
	private void getModify(WumartSpiderEvent event){
		logger.debug("do getModify {}", event.getId());
	
		String phone = event.getAccount();
		String password = event.getPassword();
		String vcodes = event.getVcode();
		String vcode = event.getVcodes();
		String province = event.getProvince();
		
		String url = String.format(MODIFY_URL, province);
		
		HttpPost httpPost = new HttpPost(url);
		setHeader(url, httpPost, event);
		setHeader(httpPost);
		
		List<NameValuePair> nvps = new ArrayList<NameValuePair>();
		
		nvps.add(new BasicNameValuePair("vcode", vcode));//手机验证码
		nvps.add(new BasicNameValuePair("vcodes", vcodes));//验证码
		
        nvps.add(new BasicNameValuePair("phone", phone));//账号 
        nvps.add(new BasicNameValuePair("password", password));//密码
        nvps.add(new BasicNameValuePair("repassword", password));//重复密码
        try {
			httpPost.setEntity(new UrlEncodedFormEntity(nvps, "UTF-8"));
		} catch (UnsupportedEncodingException e) {
			logger.error("login eventId:{} error {}", event.getId(), e);
			event.setException(e);
			return;
		}
		httpAsyncClient.execute(httpPost, new HttpAsyncCallback(event));
	}
	
	
	
	private void loginUp(WumartSpiderEvent event){
		logger.debug("do loginUp {}", event.getId());
		
		String name = event.getAccount();
		String password = event.getPassword();
		String vcode = event.getVcode();
		
		HttpPost httpPost = new HttpPost(LOGIN_URL);
		setHeader(LOGIN_URL, httpPost, event);
		setHeader(httpPost);
		
		List<NameValuePair> nvps = new ArrayList<NameValuePair>();
		
        nvps.add(new BasicNameValuePair("vcode", vcode));//验证码
        nvps.add(new BasicNameValuePair("name", name));//账号 
        nvps.add(new BasicNameValuePair("password", password));//密码
        try {
			httpPost.setEntity(new UrlEncodedFormEntity(nvps, "UTF-8"));
		} catch (UnsupportedEncodingException e) {
			logger.error("login eventId:{} error {}", event.getId(), e);
			event.setException(e);
			return;
		}
		httpAsyncClient.execute(httpPost, new HttpAsyncCallback(event));
	}
	
	
	private void getnUp(WumartSpiderEvent event){
		logger.debug("do getn up {}", event.getId());
		String province = event.getProvince();
		
		String url = String.format(NAME_URL, province);
		HttpGet httpGet = new HttpGet(url);
		setHeader(url, httpGet, event);
		httpAsyncClient.execute(httpGet, new HttpAsyncCallback(event));
	}
	
	private void setHeader(HttpPost  httpPost){
		httpPost.setHeader("Host","www.wumart.com");
		httpPost.setHeader("Connection","keep-alive");
		httpPost.setHeader("Accept","text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
		httpPost.setHeader("Origin","http://www.wumart.com");
		//httpPost.setHeader("Referer","http://www.wumart.com/index.php/5371c3510a?city=zj");
		httpPost.setHeader("User-Agent","Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/46.0.2490.80 Safari/537.36");
		httpPost.setHeader("Content-Type","application/x-www-form-urlencoded");
		httpPost.setHeader("Accept-Encoding","gzip, deflate");
		httpPost.setHeader("Accept-Language","zh-CN,zh;q=0.8");

	}
	
	
	private class HttpAsyncCallback implements FutureCallback<HttpResponse> {

		private WumartSpiderEvent event;
		
		public HttpAsyncCallback(WumartSpiderEvent event) {
			this.event = event;
		}

		/* (non-Javadoc)
		 * @see org.apache.http.concurrent.FutureCallback#completed(java.lang.Object)
		 */
		@Override
		public void completed(HttpResponse result) {
			try {
				// extract cookie
				CookieHelper.getCookies(event.getCookieList(), result);
				switch (event.getState()) {
				case IMGCODE:
					imgcodeDown(result);
					break;
				case LOGIN:
					loginDown(result);
					break;
				case GETN:
					getnDown(result);
					break;
				case CHECK:
					checkDown(result);
					break;
				case GETSMS:
					smsDown(result);
					break;
				case MODIFY:
					modifyDown(result);
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
		 * failed
		 */
		@Override
		public void failed(Exception e) {
			logger.debug("request {} failed: {}", event.getId(), e.getMessage());
			event.setException(e);
			
			onEvent(event);
		}

		/**
		 * cancelled
		 */
		@Override
		public void cancelled() {
			logger.debug("request cancelled: {}", event.getId());
		}
		
		private void imgcodeDown(HttpResponse result){
			
			try {
				HttpEntity entity = result.getEntity();
				InputStream imgStream = entity.getContent();
				
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				int rd = -1;
				byte[] bytes = new byte[1024];
				while ((rd = imgStream.read(bytes)) != -1) {
					baos.write(bytes, 0, rd);
				}
				byte[] rbyte = baos.toByteArray();
				//图片流字符串
				String imgStr = Base64.getEncoder().encodeToString(rbyte);

				// save to file, used for debug
/*				if (appConfig.inDevMode()) {
					byte[] tbytes = Base64.getDecoder().decode(imgStr);
					// FileOutputStream fs = new
					// FileOutputStream("/Users/bobo/Desktop/vcode.jpg");
					FileOutputStream fs = new FileOutputStream("D:/2.jpg");
					fs.write(tbytes);
					fs.close();
				}*/

				// 记录事件当前步骤
				//event.setState(CsairSpiderState.LOGIN); // next step is to login
				// 缓存当前事件内容
				String key = String.format(Constant.WUMART_IMPORT_KEY, event.getUserid());
				redis.set(key, JSON.toJSONString(event), 300); //300秒超时

				// 返回当前结果
				event.setException(new SpiderException(1045, "图片验证码", imgStr));//统一约定，message赋值0
				return;
				
			} catch (Exception e) {
				logger.error("csair checkDown exception", e);
				event.setException(e);
				return;
			}
			
		}
		
		
		private void loginDown(HttpResponse response){
			try {
				String entityMap = EntityUtils.toString(response.getEntity());

				if(entityMap.contains("验证码错误")){
					event.setException(new SpiderException(2005, "验证码错误"));
					return;
				}else if(entityMap.contains("用户名或密码错误")){
					event.setState(WumartSpiderState.IMGCODE);
					return;
				}else if(entityMap.contains("登陆成功")){
					//登录成功
					event.setState(WumartSpiderState.GETN);
					return;
				}
				
				event.setException(new SpiderException(2004, "登录失败"));
				return;
				
			} catch (Exception e) {
				logger.error("csair loginDown error", e);
				event.setException(e);
				return;
			} 
		}
		
		private void getnDown(HttpResponse response){
			try {
				HttpEntity entity = response.getEntity();
				//获取各项积分
				Map<String,String> data = parserIntegral(entity);
				//返回积分结果	统一约定，message赋值0
				event.setException(new SpiderException(0, "0", JSON.toJSONString(data) ));
				return;
			} catch (Exception e) {
				logger.error("csair gainDown error", e);
				event.setException(new SpiderException(0, "积分解析失败"));
				return;
			}
		}
		
		
		private Map<String, String> parserIntegral(HttpEntity entity)
				throws IOException, Exception {
			String fileDir = XpathHtmlUtils.deleteHeadHtml(EntityUtils.toString(entity));
			Document document = XpathHtmlUtils.getCleanHtml(fileDir);
			XPath xpath = XPathFactory.newInstance().newXPath();
			//  class = check_answer
			////*[@id="nav2_main"]/div[2]/div[2]/p[4]/span
			//会员名
			String exp_name = "//*[@id='nav2_main']/div[2]/div[2]/p[4]/span";
			String value = XpathHtmlUtils.getNodeText(exp_name, xpath, document);
			
			if(!isInteger(value)){
				value = "0";
			}
			
			Map<String,String> data = new HashMap<String, String>();
			data.put("interal", value);
			data.put("account", event.getAccount());
			data.put("password", event.getPassword());
			return data;
		}
		
		private boolean isInteger(String value) {
			  try {
			   Integer.parseInt(value);
			   return true;
			  } catch (NumberFormatException e) {
			   return false;
			  }
		 }
		
		
		
		private void checkDown(HttpResponse response){
			try {
				String entityMap = EntityUtils.toString(response.getEntity());
				if(entityMap.equals("1")){
					String key = String.format(Constant.WUMART_IMPORT_KEY, event.getUserid());
					event.setState(WumartSpiderState.GETSMS);
					redis.set(key, JSON.toJSONString(event), 300); //300秒超时
					return;
				}else if(entityMap.equals("2")){
					event.setException(new SpiderException(2005, "验证码错误"));
					return;
				}
				event.setException(new SpiderException(2004, "登录失败"));
				return;
			} catch (Exception e) {
				logger.error("csair loginDown error", e);
				event.setException(e);
				return;
			} 
		}
		
		
		private void smsDown(HttpResponse response){
			try {
				String entityMap = EntityUtils.toString(response.getEntity());
				event.setException(new SpiderException(0, "0", entityMap ));
				return;
			} catch (Exception e) {
				logger.error("csair loginDown error", e);
				event.setException(e);
				return;
			} 
		}
		
		
		private void modifyDown (HttpResponse response){
			try {
				String entityMap = EntityUtils.toString(response.getEntity());

				//短信验证码信息错误
				//修改成功
				//两次密码不一致
				if(entityMap.contains("修改成功")){
					event.setState(WumartSpiderState.IMGCODE);
					return;
				}else if(entityMap.contains("短信验证码信息错误")){
					event.setException(new SpiderException(2007, "短信验证码信息错误"));
					return;
				}else if(entityMap.contains("图形验证码信息错误")){
					event.setException(new SpiderException(2005, "图形验证码信息错误"));
					return;
				}
				event.setException(new SpiderException(2004, "登录失败"));
				return;
				
			} catch (Exception e) {
				logger.error("csair loginDown error", e);
				event.setException(e);
				return;
			} 
		}
		
	}
}
