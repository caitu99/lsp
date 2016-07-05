/*
 * Copyright (c) 2015-2020 by caitu99
 * All rights reserved.
 */
package com.caitu99.lsp.spider.liantong;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

import org.apache.http.HttpEntity;
import org.apache.http.HttpMessage;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.ParseException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.htmlparser.http.Cookie;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.caitu99.lsp.Constant;
import com.caitu99.lsp.exception.SpiderException;
import com.caitu99.lsp.model.spider.MailSpiderEvent;
import com.caitu99.lsp.model.spider.liantong.LianTongSpiderEvent;
import com.caitu99.lsp.model.spider.liantong.LianTongSpiderState;
import com.caitu99.lsp.spider.AbstractMailSpider;
import com.caitu99.lsp.spider.MailParserTask;
import com.caitu99.lsp.utils.cookie.CookieHelper;
import com.caitu99.lsp.utils.cookie.HttpCookieEx;

/**
 *
 * @Description: (类职责详细描述,可空)
 * @ClassName: LianTongSpider
 * @author chenhl
 * @date 2015年11月18日 上午11:52:28 
 * @Copyright (c) 2015-2020 by caitu99 
 */
public class LianTongSpider extends AbstractMailSpider{

	private final static Logger logger = LoggerFactory.getLogger(LianTongSpider.class);

	private static final String firstUrl = "http://wap.10010.com/t/home.htm";
	private static final String loginPostUrl = "https://uac.10010.com/oauth2/loginWeb06";
	private static final String homePageUrl2 = "http://wap.10010.com/t/home.htm";
	private static final String integralUrl = "http://wap.10010.com/t/points/queryPoint.htm?menuId=000200040001";
	private static final String integralScreenUrl = "http://wap.10010.com/t/points/queryPointFourg.htm?menuId=000200040006";
	private static final String touchScreenUrl = "http://wap.10010.com/t/versionSwitch.htm?version=sd";
	private static final String queryPageUrl = "http://wap.10010.com/t/siteMap.htm?menuId=query";
	private String loginPageUrl;
	private String uvcString;
	private String homePageUrl;
	private String vcodePicUrl;
	private int    vcodetimes = 0;   //验证码获取的次数


	public LianTongSpider(LianTongBodySpider mailBodySpider, MailParserTask mailParser) {
		super(mailBodySpider, mailParser);
	}

	/* (non-Javadoc)
	 * @see com.caitu99.spider.spider.IMailSpider#onEvent(com.caitu99.spider.model.MailSpiderEvent)
	 */
	@Override
	public void onEvent(MailSpiderEvent event) {
		LianTongSpiderEvent lianTongEvent = (LianTongSpiderEvent) event;
		try {
			switch (lianTongEvent.getState()) {
				case First:
					firstUp(lianTongEvent);
					break;
				case PRELOGIN:
					preLoginUp(lianTongEvent);
					break;
				case LOGINPAGE:
					loginPageUp(lianTongEvent);
					break;
				case LOGINPOST:
					loginPostUp(lianTongEvent);
					break;
				case HOMEPAGE:
					homePageUp(lianTongEvent);
					break;
				case HOMEPAGE2:
					homePage2Up(lianTongEvent);
					break;
				case TOUCHSCREEN:
					touchScreenUp(lianTongEvent);
					break;
				case QUERYPAGE:
					queryPageUp(lianTongEvent);
					break;
				case GAIN:
					gainUp(lianTongEvent);
					break;
				case GAINSCREEN:
					gainScreenUp(lianTongEvent);
					break;
				case VCODEPIC:
					vcodePicUp(lianTongEvent);
					break;
				case VCODELOGIN:
					vcodeLoginUp(lianTongEvent);
					break;
				case ERROR: // 错误处理
					errorHandle(lianTongEvent);
					break;
			}
		} catch (Exception e) {
			logger.error("request up error {}", lianTongEvent.getId(), e);
			lianTongEvent.setException(e);
			errorHandle(event);
		}

	}





	/**
	 *
	 * @Description: (方法职责详细描述,可空)
	 * @Title: vcodePicUp
	 * @param lianTongEvent
	 * @date 2015年11月19日 下午9:05:33  
	 * @author chenhl
	 */
	private void vcodePicUp(LianTongSpiderEvent event) {
		logger.debug("do vcodePicUp {}", event.getId());
		HttpGet httpGet = new HttpGet(vcodePicUrl);
		setHeader(vcodePicUrl, httpGet, event);
		httpAsyncClient.execute(httpGet, new HttpAsyncCallback(event));
	}

	/**
	 *
	 * @Description: (方法职责详细描述,可空)
	 * @Title: firstUp
	 * @param lianTongEvent
	 * @date 2015年11月19日 下午4:35:30  
	 * @author chenhl
	 */
	private void firstUp(LianTongSpiderEvent event) {
		logger.debug("do firstUp {}", event.getId());
		HttpGet httpGet = new HttpGet(firstUrl);
		setHeader(firstUrl, httpGet, event);
		httpAsyncClient.execute(httpGet, new HttpAsyncCallback(event));
	}

	/**
	 *
	 * @Description: (方法职责详细描述,可空)
	 * @Title: homePage2Up
	 * @param lianTongEvent
	 * @date 2015年11月19日 下午4:26:34  
	 * @author chenhl
	 */
	private void homePage2Up(LianTongSpiderEvent event) {
		logger.debug("do homePage2Up {}", event.getId());
		HttpGet httpGet = new HttpGet(homePageUrl2);
		setHeader(homePageUrl2, httpGet, event);
		httpAsyncClient.execute(httpGet, new HttpAsyncCallback(event));
	}

	/**
	 *
	 * @Description: (方法职责详细描述,可空)
	 * @Title: touchScreenUp
	 * @param lianTongEvent
	 * @date 2015年11月19日 下午4:26:34  
	 * @author chenhl
	 */
	private void touchScreenUp(LianTongSpiderEvent event){
		logger.debug("do touchScreenUp {}", event.getId());
		HttpGet httpGet = new HttpGet(touchScreenUrl);
		setHeader(touchScreenUrl, httpGet, event);
		httpAsyncClient.execute(httpGet, new HttpAsyncCallback(event));
	}

	private void queryPageUp(LianTongSpiderEvent event) {
		logger.debug("do queryPageUp {}", event.getId());
		HttpGet httpGet = new HttpGet(queryPageUrl);
		setHeader1(queryPageUrl, httpGet, event);
		httpAsyncClient.execute(httpGet, new HttpAsyncCallback(event));
	}

	/**
	 *
	 * @Description: (方法职责详细描述,可空)
	 * @Title: preLoginUp
	 * @param lianTongEvent
	 * @date 2015年11月19日 下午4:12:36  
	 * @author chenhl
	 */
	private void preLoginUp(LianTongSpiderEvent event) {
		logger.debug("do preLoginUp {}", event.getId());
		String home_url = String.format("http://wap.10010.com/t/loginCallzz.htm?time="+System.currentTimeMillis());
		HttpGet httpGet = new HttpGet(home_url);
		setHeader(home_url, httpGet, event);
		httpAsyncClient.execute(httpGet, new HttpAsyncCallback(event));
	}

	/**
	 *
	 * @Description: (方法职责详细描述,可空)
	 * @Title: gainUp
	 * @param lianTongEvent
	 * @date 2015年11月19日 上午9:18:25  
	 * @author chenhl
	 */
	private void gainUp(LianTongSpiderEvent event) {
		logger.debug("do gainUp {}", event.getId());
		HttpGet httpGet = new HttpGet(integralUrl);

		//添加cookies
		List<HttpCookieEx> cookieExs = new ArrayList<>();

		HttpCookieEx CookieEx3 = new HttpCookieEx("clientid", "36|360");
		CookieEx3.setPath("/");
		CookieEx3.setSecure(false);
		CookieEx3.setHttpOnly(true);
		CookieEx3.setVersion(0);

		cookieExs.add(CookieEx3);

		httpGet.setHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
		httpGet.setHeader("Accept-Encoding", "gzip, deflate, sdch");
		httpGet.setHeader("Accept-Language", "zh-CN,zh;q=0.8");
		httpGet.setHeader("Connection", "keep-alive");
		httpGet.setHeader("Host", "wap.10010.com");
		httpGet.setHeader("Referer", "http://wap.10010.com/t/home.htm");
		httpGet.setHeader("Upgrade-Insecure-Requests", "1");
		getCookies(event.getCookieList(),cookieExs);
		setHeader(integralUrl, httpGet, event);
		httpAsyncClient.execute(httpGet, new HttpAsyncCallback(event));
	}

	private void gainScreenUp(LianTongSpiderEvent event) {
		logger.debug("do gainUp {}", event.getId());
		HttpGet httpGet = new HttpGet(integralScreenUrl);

		//添加cookies
		List<HttpCookieEx> cookieExs = new ArrayList<>();

		HttpCookieEx CookieEx3 = new HttpCookieEx("clientid", "36|360");
		CookieEx3.setPath("/");
		CookieEx3.setSecure(false);
		CookieEx3.setHttpOnly(true);
		CookieEx3.setVersion(0);

		cookieExs.add(CookieEx3);

		httpGet.setHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
		httpGet.setHeader("Accept-Encoding", "gzip, deflate, sdch");
		httpGet.setHeader("Accept-Language", "zh-CN,zh;q=0.8");
		httpGet.setHeader("Connection", "keep-alive");
		httpGet.setHeader("Host", "wap.10010.com");
		httpGet.setHeader("Referer", "http://wap.10010.com/t/home.htm");
		httpGet.setHeader("Upgrade-Insecure-Requests", "1");
		getCookies(event.getCookieList(),cookieExs);
		setHeader(integralScreenUrl, httpGet, event);
		httpAsyncClient.execute(httpGet, new HttpAsyncCallback(event));
	}

	/**
	 *
	 * @Description: (方法职责详细描述,可空)
	 * @Title: homePageUp
	 * @param lianTongEvent
	 * @date 2015年11月18日 下午9:28:50  
	 * @author chenhl
	 */
	private void homePageUp(LianTongSpiderEvent event) {
		logger.debug("do homePageUp {}", event.getId());
		if(homePageUrl == null || homePageUrl.equals("")){
			logger.error("loginPostUp 联通获取homePageUrl失败", event.getId());
			event.setException(new SpiderException(1064, "联通获取homePageUrl失败"));
			return;
		}
		HttpGet httpGet = new HttpGet(homePageUrl);
		setHeader(homePageUrl, httpGet, event);
		httpAsyncClient.execute(httpGet, new HttpAsyncCallback(event));
	}

	/**
	 *
	 * @Description: (方法职责详细描述,可空)
	 * @Title: vcodeLoginUp
	 * @param lianTongEvent
	 * @date 2015年11月20日 上午9:21:50  
	 * @author chenhl
	 */
	private void vcodeLoginUp(LianTongSpiderEvent event) {
		logger.debug("do vcodeLoginUp {}", event.getId());
		uvcString = event.getUvc();
		String password = event.getPassword();
		String phoneno = event.getPhoneno();
		String yzm = event.getYzm();

		HttpPost httpPost = new HttpPost(loginPostUrl);
		setHeader(loginPostUrl, httpPost, event);
		List<NameValuePair> nvps = new ArrayList<NameValuePair>();
		nvps.add(new BasicNameValuePair("app_code", "ECS-YH-SD"));
		nvps.add(new BasicNameValuePair("redirect_uri", "http://wap.10010.com/t/loginCallBack.htm?version=sd"));
		nvps.add(new BasicNameValuePair("state", "http://wap.10010.com/t/home.htm"));
		nvps.add(new BasicNameValuePair("submitType", "01"));
		nvps.add(new BasicNameValuePair("uvc", uvcString));
		nvps.add(new BasicNameValuePair("user_id", phoneno));
		nvps.add(new BasicNameValuePair("user_pwd", password));
		nvps.add(new BasicNameValuePair("verify_code", yzm));

		try {
			httpPost.setEntity(new UrlEncodedFormEntity(nvps));
		} catch (UnsupportedEncodingException e) {
			logger.error("login eventId:{} error {}", event.getId(), e);
			event.setException(e);
			return;
		}
		httpAsyncClient.execute(httpPost, new HttpAsyncCallback(event));

	}

	/**
	 *
	 * @Description: (方法职责详细描述,可空)
	 * @Title: loginPostUp
	 * @param lianTongEvent
	 * @date 2015年11月18日 下午9:23:57  
	 * @author chenhl
	 */
	private void loginPostUp(LianTongSpiderEvent event) {
		logger.debug("do loginPostUp {}", event.getId());
		if (uvcString == null || uvcString.equals("")) {
			event.setException(new SpiderException(1063, "联通获取uvc失败"));
			logger.error("loginPostUp 联通获取uvc失败", event.getId());
			return;
		}
		String password = event.getPassword();
		String phoneno = event.getPhoneno();
		HttpPost httpPost = new HttpPost(loginPostUrl);
		setHeader(loginPostUrl, httpPost, event);
		List<NameValuePair> nvps = new ArrayList<NameValuePair>();
		nvps.add(new BasicNameValuePair("app_code", "ECS-YH-SD"));
		nvps.add(new BasicNameValuePair("redirect_uri", "http://wap.10010.com/t/loginCallBack.htm?version=sd"));
		nvps.add(new BasicNameValuePair("state", "http://wap.10010.com/t/home.htm"));
		nvps.add(new BasicNameValuePair("submitType", "01"));
		nvps.add(new BasicNameValuePair("uvc", uvcString));
		nvps.add(new BasicNameValuePair("user_id", phoneno));
		nvps.add(new BasicNameValuePair("user_pwd", password));

		try {
			httpPost.setEntity(new UrlEncodedFormEntity(nvps));
		} catch (UnsupportedEncodingException e) {
			logger.error("login eventId:{} error {}", event.getId(), e);
			event.setException(e);
			return;
		}
		httpAsyncClient.execute(httpPost, new HttpAsyncCallback(event));

	}

	/**
	 *
	 * @Description: (方法职责详细描述,可空)
	 * @Title: loginPageUp
	 * @param lianTongEvent
	 * @date 2015年11月18日 下午9:15:59  
	 * @author chenhl
	 */
	private void loginPageUp(LianTongSpiderEvent event) {
		logger.debug("do loginPageUp {}", event.getId());
		if(loginPageUrl == null || loginPageUrl.equals("")){
			logger.error("loginPostUp 联通获取loginPageUrl失败", event.getId());
			event.setException(new SpiderException(1062, "联通获取loginPageUrl失败"));
			return;
		}
		HttpGet httpGet = new HttpGet(loginPageUrl);
		setHeader(loginPageUrl, httpGet, event);
		httpAsyncClient.execute(httpGet, new HttpAsyncCallback(event));
	}


	/**
	 * 结果处理
	 *
	 * @Description: (类职责详细描述,可空)
	 * @ClassName: HttpAsyncCallback
	 * @author chenhl
	 * @date 2015年11月13日 下午2:31:40 
	 * @Copyright (c) 2015-2020 by caitu99
	 */
	private class HttpAsyncCallback implements FutureCallback<HttpResponse> {

		private LianTongSpiderEvent event;
		private boolean skipNextStep = false;

		public HttpAsyncCallback(LianTongSpiderEvent event) {
			this.event = event;
		}

		@Override
		public void completed(HttpResponse result) {
			try {
				// extract cookie
				CookieHelper.getCookies(event.getCookieList(), result);

				switch (event.getState()) {
					case First:
						firstDown(result);
						break;
					case PRELOGIN:
						preLoginDown(result);
						break;
					case LOGINPAGE:
						loginPageDown(result);
						break;
					case LOGINPOST:
						loginPostDown(result);
						break;
					case HOMEPAGE:
						homePageDown(result);
						break;
					case HOMEPAGE2:
						homePage2Down(result);
						break;
					case GAIN:
						gainDown(result);
						break;
					case TOUCHSCREEN:
						touchScreenDown(result);
						break;
					case QUERYPAGE:
						queryPageDown(result);
						break;
					case GAINSCREEN:
						gainScreenDown(result);
						break;
					case VCODEPIC:
						vcodePicDown(result);
						break;
					case VCODELOGIN:
						loginPostDown(result);
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
		 * @Title: vcodePicDown
		 * @param result
		 * @date 2015年11月19日 下午9:08:24  
		 * @author chenhl
		 */
		private void vcodePicDown(HttpResponse response) {
			logger.debug("do vcodePicDown {}", event.getId());
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
				//图片流字符串
				String imgStr = Base64.getEncoder().encodeToString(rbyte);

				// save to file, used for debug
//				if (appConfig.inDevMode()) {
//					byte[] tbytes = Base64.getDecoder().decode(imgStr);
//					FileOutputStream fs = new FileOutputStream("C:\\Users\\zhouxi\\Desktop\\vcode.jpg");
//					fs.write(tbytes);
//					fs.close();
//				}

				// 记录事件当前步骤
				event.setState(LianTongSpiderState.VCODELOGIN); // next step is to login
				event.setUvc(uvcString);
				event.setVocdetimes(++vcodetimes);
				// 缓存当前事件内容
				String key = String.format(Constant.LIAN_TONG_IMPORT_KEY, event.getUserid());
				redis.set(key, JSON.toJSONString(event), 3000); //3000秒超时

				// 返回当前结果
				Map<String, String> data = new HashMap<String, String>();
				data.put("phone", event.getPhoneno());
				data.put("imgstr", imgStr);
				if( vcodetimes == 1){  //第一次获取验证码输出这个code
					event.setException(new SpiderException(1001, "请输入验证码",JSON.toJSONString(data)));
					return;
				}
				event.setException(new SpiderException(1069, "验证码不正确,请再次输入",JSON.toJSONString(data)));
				return;
			} catch (Exception e) {
				logger.error("liantong vcodePicDown exception", e);
				event.setException(e);
				return;
			}
		}

		/**
		 *
		 * @Description: (方法职责详细描述,可空)
		 * @Title: firstDown
		 * @param result
		 * @date 2015年11月19日 下午4:36:22  
		 * @author chenhl
		 */
		private void firstDown(HttpResponse response) {
			logger.debug("do firstDown {}", event.getId());
			event.setState(LianTongSpiderState.PRELOGIN);
		}

		/**
		 *
		 * @Description: (方法职责详细描述,可空)
		 * @Title: homePage2Down
		 * @param result
		 * @date 2015年11月19日 下午4:27:31  
		 * @author chenhl
		 */
		private void homePage2Down(HttpResponse response) {
			logger.debug("do homePage2Down {}", event.getId());
			try {
				String homePage2 = EntityUtils.toString(response.getEntity());
				Pattern pattern = Pattern.compile("退出");
				Matcher matcher = pattern.matcher(homePage2);
				if( matcher.find()){
					event.setState(LianTongSpiderState.GAIN);
					logger.debug("homePage2Down() 登陆成功   ", event.getId());
					return;
				}
			} catch (ParseException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			logger.debug("homePage2Down() 登陆失败  ", event.getId());
			event.setException(new SpiderException(1013, "登陆失败"));
		}

		private void touchScreenDown(HttpResponse response){
			logger.debug("do touchScreenDown {}", event.getId());
			event.setState(LianTongSpiderState.QUERYPAGE);
		}


		private void queryPageDown(HttpResponse response){
			logger.debug("do touchScreenDown {}", event.getId());
			try {
				String queryPage = EntityUtils.toString(response.getEntity());
			} catch (ParseException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			event.setState(LianTongSpiderState.GAINSCREEN);
		}

		/**
		 *
		 * @Description: (方法职责详细描述,可空)
		 * @Title: preLoginDown
		 * @param result
		 * @date 2015年11月19日 下午4:14:56  
		 * @author chenhl
		 */
		private void preLoginDown(HttpResponse response) {
			logger.debug("do preLoginDown {}", event.getId());
			try{
				loginPageUrl = response.getFirstHeader("Location").getValue().replaceAll(" ", "");
			}catch(NullPointerException e){
				logger.debug("loginPageUrl() 联通获取loginPageUrl失败  ", event.getId());
				event.setException(new SpiderException(1062, "联通获取loginPageUrl失败"));
				return;
			}

			if(loginPageUrl == null){
				logger.debug("loginPageUrl() 联通获取loginPageUrl失败  ", event.getId());
				event.setException(new SpiderException(1062, "联通获取loginPageUrl失败"));
				return;
			}
			event.setState(LianTongSpiderState.LOGINPAGE);
		}

		/**
		 *
		 * @Description: (方法职责详细描述,可空)
		 * @Title: gainScreenDown
		 * @param result
		 * @date 2015年11月19日 上午9:24:07  
		 * @author chenhl
		 */
		private void gainScreenDown(HttpResponse response) {
			logger.debug("do gainScreenDown {}", event.getId());
			String integralPage;  //积分在这个页面显示 传来的是压缩格式
			try {
				//解压缩
				HttpEntity httpEntity = response.getEntity();
				InputStream inputStream = httpEntity.getContent();
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				int rd = -1;
				byte[] bytes = new byte[1024];
				while ((rd = inputStream.read(bytes)) != -1) {
					baos.write(bytes, 0, rd);
				}
				byte[] rbyte = baos.toByteArray();
				integralPage = uncompressToString(rbyte,"utf-8");
				Pattern pattern = Pattern.compile("(?<=当前总积分)[\\s\\S]*?(?=</em>)");
				Matcher matcher = pattern.matcher(integralPage);
				if(matcher.find()){
					String jifen = matcher.group();
					jifen = jifen.substring(jifen.lastIndexOf(">") + 1, jifen.length());
					Long.parseLong(jifen);
					Map<String, String> data = new HashMap<String, String>();
					data.put("phone", event.getPhoneno());
					data.put("integral", jifen);
					event.setException(new SpiderException(1070, "联通获得积分成功",JSON.toJSONString(data)));
					return;
				}else{
					logger.debug("gainScreenDown() 积分解析失败  ", event.getId());
					event.setException(new SpiderException(1061, "积分解析失败"));
					return;
				}
			} catch (ParseException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (java.lang.NumberFormatException e) {
				logger.debug("gainScreenDown() 积分解析失败  ", event.getId());
				event.setException(new SpiderException(1061, "积分解析失败"));
				return;
			}
		}

		private void gainDown(HttpResponse response) {
			logger.debug("do gainDown {}", event.getId());
			String integralPage;  //积分在这个页面显示 传来的是压缩格式
			try {
				//解压缩
				HttpEntity httpEntity = response.getEntity();
				InputStream inputStream = httpEntity.getContent();
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				int rd = -1;
				byte[] bytes = new byte[1024];
				while ((rd = inputStream.read(bytes)) != -1) {
					baos.write(bytes, 0, rd);
				}
				byte[] rbyte = baos.toByteArray();
				integralPage = uncompressToString(rbyte, "utf-8");
				if( integralPage == null ){
					event.setState(LianTongSpiderState.TOUCHSCREEN);
					return;
				}
				Pattern pattern = Pattern.compile("(?<=当前可用积分：<span class=\"color-red\">)\\S*(?=</span>)");
				Matcher matcher = pattern.matcher(integralPage);
				if (matcher.find()) {
					Map<String, String> data = new HashMap<String, String>();
					data.put("phone", event.getPhoneno());
					data.put("integral", matcher.group());
					event.setException(new SpiderException(1070, "联通获得积分成功", JSON.toJSONString(data)));
					return;
				} else {
					//老版本的界面查询不到，转到新版本查询
					event.setState(LianTongSpiderState.TOUCHSCREEN);
				}
			} catch (java.lang.NumberFormatException e) {
				logger.debug("gainScreenDown() 积分解析失败  ", event.getId());
				event.setException(new SpiderException(1061, "积分解析失败"));
				return;
			} catch(java.util.zip.ZipException e){
				event.setState(LianTongSpiderState.TOUCHSCREEN);
				return;
			}catch (Exception e) {
				event.setState(LianTongSpiderState.TOUCHSCREEN);
				return;
			}
		}



		/**
		 *
		 * @Description: (方法职责详细描述,可空)
		 * @Title: loginPostDown
		 * @param result
		 * @date 2015年11月18日 下午9:31:08  
		 * @author chenhl
		 */
		private void loginPostDown(HttpResponse response) {
			logger.debug("do loginPostDown {}", event.getId());
			String errorMsgPage;  //在这里提示异常信息     请输入验证码
			try {
				errorMsgPage = EntityUtils.toString(response.getEntity());
				Pattern pattern = Pattern.compile("请输入正确的密码");
				Matcher matcher = pattern.matcher(errorMsgPage);
				if(matcher.find()){
					event.setException(new SpiderException(1065, "密码错误"));
					return;
				}
				pattern = Pattern.compile("请输入验证码");
				matcher = pattern.matcher(errorMsgPage);
				if(matcher.find()){
					Pattern patternUvc = Pattern.compile("(?<=name=\"uvc\" value=\")\\S*(?=\"/>)");
					Matcher matcherUvc = patternUvc.matcher(errorMsgPage);
					if(matcherUvc.find()){
						uvcString = matcherUvc.group();
						vcodePicUrl = "https://uac.10010.com/oauth2/webSDCapcha?uvc="+uvcString;
						event.setState(LianTongSpiderState.VCODEPIC);
						return;
					}
					event.setException(new SpiderException(1069, "请输入验证码"));
					return;
				}
				pattern = Pattern.compile("系统繁忙，请稍后再试");
				matcher = pattern.matcher(errorMsgPage);
				if(matcher.find()){
					event.setException(new SpiderException(1068, "系统繁忙，请稍后再试"));
					return;
				}
				pattern = Pattern.compile("用户名或密码错误");
				matcher = pattern.matcher(errorMsgPage);
				if(matcher.find()){
					event.setException(new SpiderException(1067, "用户名或密码错误"));
					return;
				}
				pattern = Pattern.compile("请输入正确的用户名");
				matcher = pattern.matcher(errorMsgPage);
				if(matcher.find()){
					event.setException(new SpiderException(1066, "账号错误"));
					return;
				}
				pattern = Pattern.compile("验证码不正确");
				matcher = pattern.matcher(errorMsgPage);
				if(matcher.find()){
					Pattern patternUvc = Pattern.compile("(?<=name=\"uvc\" value=\")\\S*(?=\"/>)");
					Matcher matcherUvc = patternUvc.matcher(errorMsgPage);
					if(matcherUvc.find()){
						uvcString = matcherUvc.group();
						vcodePicUrl = "https://uac.10010.com/oauth2/webSDCapcha?uvc="+uvcString;
						event.setState(LianTongSpiderState.VCODEPIC);
						return;
					}
					event.setException(new SpiderException(1069, "此验证码不正确"));
					return;
				}
				pattern = Pattern.compile("验证码已过期");
				matcher = pattern.matcher(errorMsgPage);
				if(matcher.find()){
					Pattern patternUvc = Pattern.compile("(?<=name=\"uvc\" value=\")\\S*(?=\"/>)");
					Matcher matcherUvc = patternUvc.matcher(errorMsgPage);
					if(matcherUvc.find()){
						uvcString = matcherUvc.group();
						vcodePicUrl = "https://uac.10010.com/oauth2/webSDCapcha?uvc="+uvcString;
						event.setState(LianTongSpiderState.VCODEPIC);
						return;
					}
					event.setException(new SpiderException(1069, "此验证码已过期"));
					return;
				}
			} catch (ParseException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}

			try{
				homePageUrl = response.getFirstHeader("Location").getValue();
			}catch(NullPointerException e){
				logger.debug("do loginPostDown {}   联通获取homePageUrl失败", event.getId());
				event.setException(new SpiderException(1064, "联通获取homePageUrl失败"));
				return;
			}

			if(homePageUrl == null){
				logger.debug("do loginPostDown {}   联通获取homePageUrl失败", event.getId());
				event.setException(new SpiderException(1064, "联通获取homePageUrl失败"));
				return;
			}
			event.setState(LianTongSpiderState.HOMEPAGE);
		}

		/**
		 *
		 * @Description: (方法职责详细描述,可空)
		 * @Title: homePageDown
		 * @param result
		 * @date 2015年11月18日 下午9:28:10  
		 * @author chenhl
		 */
		private void homePageDown(HttpResponse response) {
			logger.debug("do homePageDown {}", event.getId());
			event.setState(LianTongSpiderState.HOMEPAGE2);
		}

		/**
		 *
		 * @Description: (方法职责详细描述,可空)
		 * @Title: loginPageUp
		 * @param result
		 * @date 2015年11月18日 下午9:20:14  
		 * @author chenhl
		 */
		private void loginPageDown(HttpResponse response) {
			logger.debug("do loginPageDown {}", event.getId());
			String uvcPage;  //获取uvc的页面
			try {
				uvcPage = EntityUtils.toString(response.getEntity());
				Pattern pattern = Pattern.compile("(?<=name=\"uvc\" value=\")\\S*(?=\"/>)");
				Matcher matcher = pattern.matcher(uvcPage);
				if (matcher.find()) {
					uvcString = matcher.group();
					event.setState(LianTongSpiderState.LOGINPOST);
				}else{
					event.setException(new SpiderException(1063, "联通获取uvc失败"));
					logger.error("联通获取uvc失败");
					return;
				}
			} catch (ParseException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}


		/* (non-Javadoc)
		 * @see org.apache.http.concurrent.FutureCallback#failed(java.lang.Exception)
		 */
		@Override
		public void failed(Exception e) {
			logger.debug("request {} failed: {}", event.getId(), e.getMessage());

		}

		/* (non-Javadoc)
		 * @see org.apache.http.concurrent.FutureCallback#cancelled()
		 */
		@Override
		public void cancelled() {
			logger.debug("request cancelled: {}", event.getId());
		}

	}

	public static void getCookies(List<HttpCookieEx> cookieList, List<HttpCookieEx> list) {
		//for Set-Cookie

		for (HttpCookieEx cookieEx : list) {
			if (!cookieEx.hasExpired() && !cookieList.contains(cookieEx)) {
				cookieList.add(cookieEx);
			}
		}
	}

	//解压缩gzip格式
	public static String uncompressToString(byte[] b, String encoding) {
		if (b == null || b.length == 0) {
			return null;
		}
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		ByteArrayInputStream in = new ByteArrayInputStream(b);

		try {
			GZIPInputStream gunzip = new GZIPInputStream(in);
			byte[] buffer = new byte[256];
			int n;
			while ((n = gunzip.read(buffer)) >= 0) {
				out.write(buffer, 0, n);
			}
			return out.toString(encoding);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see com.caitu99.spider.spider.AbstractMailSpider#setHeader(java.lang.String, org.apache.http.HttpMessage, com.caitu99.spider.model.MailSpiderEvent)
	 */
	@Override
	protected void setHeader(String uriStr, HttpMessage http, MailSpiderEvent event) {
		http.setHeader("Accept", "*/*");
		http.setHeader("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_10_5) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/45.0.2454.101 Safari/537.36");
		try {
			CookieHelper.setCookies(uriStr, http, event.getCookieList());
		} catch (URISyntaxException e) {
			logger.error("set cookie fail {}", event.getId());
		}
	}

	protected void setHeader1(String uriStr, HttpMessage http, MailSpiderEvent event) {
		http.setHeader("Accept", "*/*");
		http.setHeader("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_10_5) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/45.0.2454.101 Safari/537.36");
		List<HttpCookieEx> list = event.getCookieList();
		HttpCookieEx Cvertion = new HttpCookieEx("vertion", "1");
		Cvertion.setVersion(0);
		Cvertion.setPath("/");
		Cvertion.setSecure(false);
		list.add(Cvertion);
		event.setCookieList(list);
		try {
			CookieHelper.setCookies(uriStr, http, event.getCookieList());
		} catch (URISyntaxException e) {
			logger.error("set cookie fail {}", event.getId());
		}
	}
}

