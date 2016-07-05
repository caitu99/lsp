package com.caitu99.lsp.spider.yidong;

import java.io.*;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.caitu99.lsp.model.spider.QueryEvent;
import com.caitu99.lsp.model.spider.yidong.*;
import com.caitu99.lsp.spider.HttpAsyncClient;
import org.apache.http.HttpEntity;
import org.apache.http.HttpMessage;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.ParseException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.request.async.DeferredResult;

import com.alibaba.fastjson.JSON;
import com.caitu99.lsp.AppConfig;
import com.caitu99.lsp.Constant;
import com.caitu99.lsp.cache.RedisOperate;
import com.caitu99.lsp.exception.SpiderException;
import com.caitu99.lsp.spider.QuerySpider;
import com.caitu99.lsp.utils.SpringContext;
import com.caitu99.lsp.utils.cookie.CookieHelper;

public class YiDongSpider implements QuerySpider {

	private CloseableHttpAsyncClient httpAsyncClient = HttpAsyncClient.getInstance();

	private AppConfig appConfig = SpringContext.getBean(AppConfig.class);
	private RedisOperate redis = SpringContext.getBean(RedisOperate.class);

	private static final Logger logger = LoggerFactory.getLogger(YiDongSpider.class);

	private static final String userAgent = "Mozilla/5.0 (Windows NT 6.3; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/43.0.2357.134 Safari/537.36";
	private static final String geiCookUrl = "https://login.10086.cn/checkUidAvailable.action";
	private static final String tologinUrl = "https://login.10086.cn/";
	private static final String preLoginPageUrl = "https://login.10086.cn/html/login/login.html?channelID=12002&backUrl=http%3A%2F%2Fshop.10086.cn%2Fmall_571_571.html%3Fforcelogin%3D1";
	private static final String passwordUrl = "https://login.10086.cn/sendRandomCodeAction.action？type=01&channelID=12003";
	private static final String getTokenUrl = "http://shop.10086.cn/i/v1/auth/getArtifact?backUrl=http://shop.10086.cn/i/&artifact=";
	private static final String getIntegralUrl = "http://shop.10086.cn/i/v1/point/sum/";
	private static final String imgUrl = "https://login.10086.cn/captchazh.htm?type=05";
	private static final String checkUrl = "https://login.10086.cn/verifyCaptcha?inputCode=";
	private static final String sendSmsUrl = "https://login.10086.cn/sendRandomCodeAction.action";
	private static final String loginUrl = "https://login.10086.cn/login.htm?accountType=01&account=%s&password=%s&pwdType=02&inputCode=%s";
	private static final String loginUrl_service = "https://login.10086.cn/login.htm?accountType=01&account=%s&password=%s&pwdType=01&inputCode=%s";

	private static final String getdouUrl = "http://home.m.jd.com/wallet/wallet.action?functionId=wodeqianbao&sid=%s";

	@Override
	public void onEvent(QueryEvent event) {

		YiDongEvent yiEvent = (YiDongEvent) event;

		try {
			switch (yiEvent.getState()) {
			case GETIMG:
				imgUp(yiEvent);
				break;
			case CHECK:
				checkUp(yiEvent);
				break;
			case PRESENDSMS:
				preSendSmsUp(yiEvent);
				break;
			case SENDSMS:
				sendSmsUp(yiEvent);
				break;
			case LOGIN:
				loginUp(yiEvent);
				break;
			case GETTOKEN:
				getToken(yiEvent);
				break;
			case GETINTEGRAL:
				getintegralUp(yiEvent);
				break;
			case ERROR:
				errorHandle(event);
				break;
			}
		} catch (Exception e) {
			logger.error("request up error {}", event.getId(), e);
			yiEvent.setException(e);
			errorHandle(yiEvent);
		}
	}

	/**
	 * 
	 * @Description: (方法职责详细描述,可空)
	 * @Title: preSendSmsUp
	 * @param yiEvent
	 * @date 2015年11月26日 下午8:33:36
	 * @author zhouxi
	 */
	private void preSendSmsUp(YiDongEvent yiEvent) {
		logger.debug("preSendSmsUp{}", yiEvent.getId());
		String s = checkUrl + yiEvent.getvCode();
		HttpGet httpGet = new HttpGet(s);
		setHeader(s, httpGet, yiEvent);
		httpAsyncClient.execute(httpGet, new HttpAsyncCallback(yiEvent));
	}

	private void getCooks(YiDongEvent yiEvent) {
		HttpGet httpGet = new HttpGet(geiCookUrl);
		setHeader(geiCookUrl, httpGet, yiEvent);
		httpAsyncClient.execute(httpGet, new HttpAsyncCallback(yiEvent));
	}

	private void toLongin(YiDongEvent yiEvent) {
		logger.debug("do pre tologin page up {}", yiEvent.getId());
		HttpGet httpGet = new HttpGet(geiCookUrl);
		setHeader(geiCookUrl, httpGet, yiEvent);
		httpAsyncClient.execute(httpGet, new HttpAsyncCallback(yiEvent));
	}

	private void imgUp(YiDongEvent yiEvent) {
		logger.debug("do pre login page up {}", yiEvent.getId());
		HttpGet httpGet = new HttpGet(imgUrl);
		setHeader(imgUrl, httpGet, yiEvent);
		httpAsyncClient.execute(httpGet, new HttpAsyncCallback(yiEvent));
	}

	private void checkUp(YiDongEvent yiEvent) {
		logger.debug("do image check {}", yiEvent.getId());
		String s = checkUrl + yiEvent.getvCode();
		HttpGet httpGet = new HttpGet(s);
		setHeader(s, httpGet, yiEvent);
		httpAsyncClient.execute(httpGet, new HttpAsyncCallback(yiEvent));
	}

	private void sendSmsUp(YiDongEvent yiEvent) {
		logger.debug("do send port up {}", yiEvent.getId());
		HttpPost httpPost = new HttpPost(sendSmsUrl);
		try {
			httpPost.setEntity(new UrlEncodedFormEntity(createSendSmsFormEntity(yiEvent), "UTF-8"));
		} catch (Exception e) {
			logger.error("encode body error {}", yiEvent.getId(), e);
			yiEvent.setException(e);
			return;
		}
		setHeader(sendSmsUrl, httpPost, yiEvent);
		httpAsyncClient.execute(httpPost, new HttpAsyncCallback(yiEvent));
	}

	private void loginUp(YiDongEvent yiEvent) {
		logger.debug("do pre login page up {}", yiEvent.getId());
		String s = null;
		if(  "service".equals(yiEvent.getPasswordType()) ){
			 s = String.format(loginUrl_service, yiEvent.getAccount(), yiEvent.getPassword(), yiEvent.getvCode());
		}else{
			s = String.format(loginUrl, yiEvent.getAccount(), yiEvent.getPassword(), yiEvent.getvCode());
		}
		s = s + "&backUrl=http%3A%2F%2Fshop.10086.cn%2Fi%2F&rememberMe=0&channelID=12003&protocol=https%3A";
		HttpGet httpGet = new HttpGet(s);
		httpGet.setHeader("Referer", "https://login.10086.cn/");
		setHeader(s, httpGet, yiEvent);
		httpAsyncClient.execute(httpGet, new HttpAsyncCallback(yiEvent));
	}

	private void getintegralUp(YiDongEvent yiEvent) {
		logger.debug("do pre login page up {}", yiEvent.getId());
		String s = getIntegralUrl + yiEvent.getAccount();
		HttpGet httpGet = new HttpGet(s);
		httpGet.setHeader("Referer", "http://shop.10086.cn/i/?welcome=1447918022279");
		setHeader(s, httpGet, yiEvent);
		httpAsyncClient.execute(httpGet, new HttpAsyncCallback(yiEvent));
	}

	private void getToken(YiDongEvent yiDongEvent) {
		String url = getTokenUrl + yiDongEvent.getBackUrl();
		HttpGet httpGet = new HttpGet(url);
		setHeader(url, httpGet, yiDongEvent);
		httpAsyncClient.execute(httpGet, new HttpAsyncCallback(yiDongEvent));
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

	private class HttpAsyncCallback implements FutureCallback<HttpResponse> {

		private YiDongEvent event;
		private boolean skipNextStep = false;

		public HttpAsyncCallback(YiDongEvent event) {
			this.event = event;
		}

		@Override
		public void completed(HttpResponse result) {
			try {
				CookieHelper.getCookies(event.getCookieList(), result);

				switch (event.getState()) {
				case GETIMG:
					imgDown(result);
					break;
				case CHECK:
					checkDown(result);
					break;
				case PRESENDSMS:
					preSendSmsDown(result);
					break;
				case SENDSMS:
					sendSmsDown(result);
					break;
				case LOGIN:
					loginDown(result);
					break;
				case GETTOKEN:
					getToken(result);
					break;
				case GETINTEGRAL:
					getintegralDown(result);
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
		 * @Title: preSendSmsDown 
		 * @param result
		 * @date 2015年11月26日 下午8:47:56  
		 * @author chenhl
		*/
		private void preSendSmsDown(HttpResponse result) {
			logger.debug("preSendSmsDown ", event.getId());
			try {
				String resultStr = EntityUtils.toString(result.getEntity());
				if (resultStr.contains("0")) {         //图片验证码正确的情况
					event.setState(YiDongState.SENDSMS);
					return;
				} else if (resultStr.contains("1")) {
					logger.debug("checkDown 验证码输入错误", event.getId());
					event.setException(new SpiderException(1052, "验证码错误"));
					return;
				} else {
					logger.debug("checkDown 验证码未知错误", event.getId());
					event.setException(new SpiderException(1052, "验证码错误"));
					return;
				}
			} catch (ParseException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		private void getCooks(HttpResponse result) {
			event.setState(YiDongState.GETIMG);
		}

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
				// 图片流字符串
				String imgStr = Base64.getEncoder().encodeToString(rbyte);

				// save to file, used for debug
//				if (appConfig.inDevMode()) {
//					byte[] tbytes = Base64.getDecoder().decode(imgStr);
//					// FileOutputStream fs = new
//					// FileOutputStream("/Users/bobo/Desktop/vcode.jpg");
//					FileOutputStream fs = new FileOutputStream("D:\\1.jpg");
//					fs.write(tbytes);
//					fs.close();
//				}

				// 记录事件当前步骤
				event.setState(YiDongState.GETIMG); // next step is to login
				// 缓存当前事件内容
				String key = String.format(Constant.YIDONG_IMPORT_KEY, event.getUserid());
				redis.set(key, JSON.toJSONString(event), 300); // 300秒超时

				// 返回当前结果
				event.setException(new SpiderException(0, "输入验证码", imgStr));// 统一约定，message赋值0
				return;
			} catch (Exception e) {
				logger.error("air china checkDown exception", e);
				event.setException(new SpiderException(1049, "图片验证码获取错误"));
				return;
			}
		}

		private void checkDown(HttpResponse result) {
			logger.debug("checkDown ", event.getId());
			try {
				String resultStr = EntityUtils.toString(result.getEntity());
				if (resultStr.contains("0")) {
					event.setState(YiDongState.LOGIN);
					return;
				} else if (resultStr.contains("1")) {
					logger.debug("checkDown 验证码输入错误", event.getId());
					event.setException(new SpiderException(1052, "验证码错误"));
					return;
				} else {
					logger.debug("checkDown 验证码未知错误", event.getId());
					event.setException(new SpiderException(1052, "验证码错误"));
					return;
				}
			} catch (ParseException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		private void sendSmsDown(HttpResponse result) {
			logger.debug("get img down {}", event.getId());
			try {
				String resultStr = EntityUtils.toString(result.getEntity());
				if (!resultStr.equals("0")) {
					event.setException(new SpiderException(1050, "获取短信验证码失败，请稍后再试"));
				} else {
					// 缓存当前事件内容
					String key = String.format(Constant.YIDONG_IMPORT_KEY, event.getUserid());
					redis.set(key, JSON.toJSONString(event), 300); // 300秒超时
					event.setException(new SpiderException(1051, "请输入短信验证码"));// 统一约定，message赋值0
					return;
				}
			} catch (Exception e) {
				logger.error("air china checkDown exception", e);
				event.setException(e);
				return;
			}
		}

		private void loginDown(HttpResponse result) {
			logger.debug("login down {}", event.getId());
			try {
				String resultStr = EntityUtils.toString(result.getEntity());
				LoginResult loginResult = JSON.parseObject(resultStr, LoginResult.class);
				if (loginResult.getCode().equals("0000")) {
					event.setBackUrl(loginResult.getArtifact());
					event.setState(YiDongState.GETTOKEN);

				} else if (loginResult.getCode().equals("3012") || loginResult.getCode().equals("2036")) {
					event.setException(new SpiderException(2112, "服务密码错误"));
				} else if (loginResult.getCode().equals("8051")) {
					event.setException(new SpiderException(1055, "非移动用户请注册互联网用户登录"));
				} else if (loginResult.getCode().equals("6001") || loginResult.getCode().equals("6002")) {
					event.setException(new SpiderException(1075, "短信随机码不正确或已经过期！"));
				} else {
					event.setException(new SpiderException(1053, "登录失败"));
				}

			} catch (Exception e) {
				logger.error("login Down exception", e);
				event.setException(e);
				return;
			}
		}

		private void getToken(HttpResponse result) {
			event.setState(YiDongState.GETINTEGRAL);
		}

		private void getintegralDown(HttpResponse result) {
			logger.debug("get integral down {}", event.getId());
			try {
				String resultStr = EntityUtils.toString(result.getEntity());
				IntegralResult integralResult = JSON.parseObject(resultStr, IntegralResult.class);
				if (integralResult.getRetCode().equals("000000")) {
					IntegralResultData integralResultData = JSON.parseObject(integralResult.getData(),
							IntegralResultData.class);
					Map<String, String> data = new HashMap<String, String>();
					data.put("account", event.getAccount());
					data.put("integral", integralResultData.getPointValue());
					event.setSuccb(resultStr);
					event.setException(new SpiderException(0, "抓取成功", JSON.toJSONString(data)));
				} else {
					event.setException(new SpiderException(1054, "抓取失败"));
				}

			} catch (IOException e) {
				logger.error("yidong checkDown exception", e);
			}
			return;
		}

		@Override
		public void failed(Exception ex) {

		}

		@Override
		public void cancelled() {

		}
	}

	private void setHeader(String uriStr, HttpMessage httpGet, QueryEvent event) {
		httpGet.setHeader("Accept", "*/*");
		httpGet.setHeader("User-Agent", userAgent);
		try {
			CookieHelper.setCookies(uriStr, httpGet, event.getCookieList());
		} catch (URISyntaxException e) {
			logger.error("set cookie fail {}", event.getId());
		}
	}

	private List<? extends NameValuePair> createSendSmsFormEntity(YiDongEvent event) {
		List<NameValuePair> params = new ArrayList<>();
		params.add(new BasicNameValuePair("channelID", "12003"));
		params.add(new BasicNameValuePair("type", "01"));
		params.add(new BasicNameValuePair("userName", event.getAccount()));
		return params;
	}
}
