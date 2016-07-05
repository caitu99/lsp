package com.caitu99.lsp.spider.botaohui;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.caitu99.lsp.model.spider.QueryEvent;
import com.caitu99.lsp.model.spider.botaohui.BoTaoHuiEvent;
import com.caitu99.lsp.model.spider.botaohui.BoTaoHuiResult;
import com.caitu99.lsp.model.spider.botaohui.BoTaoHuiState;
import com.caitu99.lsp.spider.HttpAsyncClient;
import org.apache.http.Header;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.request.async.DeferredResult;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.caitu99.lsp.AppConfig;
import com.caitu99.lsp.Constant;
import com.caitu99.lsp.cache.RedisOperate;
import com.caitu99.lsp.exception.SpiderException;
import com.caitu99.lsp.spider.QuerySpider;
import com.caitu99.lsp.utils.SpringContext;
import com.caitu99.lsp.utils.cookie.CookieHelper;

public class BoTaoHuiSpider implements QuerySpider {

	private static final Logger logger = LoggerFactory.getLogger(BoTaoHuiSpider.class);

	private CloseableHttpAsyncClient httpAsyncClient = HttpAsyncClient.getInstance();
	private RedisOperate redis = SpringContext.getBean(RedisOperate.class);
	private AppConfig appConfig = SpringContext.getBean(AppConfig.class);

	private static final String userAgent = "Mozilla/5.0 (Windows NT 6.3; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/46.0.2490.86 Safari/537.36";
	private static final String loginPageUrl = "http://www.plateno.com/login.html";
	private static final String nloginUrl = "http://www.plateno.com/p/nlogin.html?_=%s";
	private static final String realSigUrl = "http://captcha.guard.qcloud.com/getsig?appid=%s&uid=%s&buid=%s&sceneid=%s&captype=%s&sig=%s&%s";
	private static final String imgUrl = "http://captcha.guard.qcloud.com/getcap?appid=%s&uid=%s&buid=%s&sceneid=%s&captype=%s&sig=%s&%s";
	private static final String verifyUrl = "http://captcha.guard.qcloud.com/verify?appid=%s&uid=%s&buid=%s&sceneid=%s&captype=%s&sig=%s&ans=%s&%s";
	private static final String smsUrl = "http://www.plateno.com/member/sendVerificationCode";
	private static final String loginUrl = "http://www.plateno.com/ajax_authentication";
	private static final String usercapUrl = "http://www.plateno.com/member/usercaup?_=%s";

	private static final Pattern FUNC = Pattern.compile("(?<=eval\\(').*?(?='\\);)", Pattern.DOTALL);
	private static final Pattern REALSIG = Pattern.compile("(?<=onGetSig\\(\").*?(?=\"\\);)", Pattern.DOTALL);
	private static final Pattern JSONSTR = Pattern.compile("(?<=\\().*?(?=\\))", Pattern.DOTALL);
	private static final Pattern RATK = Pattern.compile("(?<=ratk\" value=\").*?(?=\">)", Pattern.DOTALL);

	private static final Pattern APPID = Pattern.compile("(?<=var G=\").*?(?=\";)", Pattern.DOTALL);
	private static final Pattern UID = Pattern.compile("(?<=var E=\").*?(?=\";)", Pattern.DOTALL);
	private static final Pattern BUID = Pattern.compile("(?<=var I=\").*?(?=\";)", Pattern.DOTALL);
	private static final Pattern SCENEID = Pattern.compile("(?<=var C=\").*?(?=\";)", Pattern.DOTALL);
	private static final Pattern CAPTYPE = Pattern.compile("(?<=var D=\").*?(?=\";)", Pattern.DOTALL);
	private static final Pattern SIG = Pattern.compile("(?<=var A=\").*?(?=\";)", Pattern.DOTALL);

	@Override
	public void onEvent(QueryEvent event) {
		BoTaoHuiEvent bthEvent = (BoTaoHuiEvent) event;
		try {
			switch (bthEvent.getState()) {
			case LOGINPAGE:
				loginPageUp(bthEvent);
				break;
			case NLOGIN:
				nloginUp(bthEvent);
				break;
			case REALSIG:
				realSigUp(bthEvent);
				break;
			case GETIMG:
				getImgUp(bthEvent);
				break;
			case VERIFY:
				verifyUp(bthEvent);
				break;
			case SENDSMS:
				sendSmsUp(bthEvent);
				break;
			case LOGIN:
				loginUp(bthEvent);
				break;
			case AFTERLOGIN:
				afterLoginUp(bthEvent);
				break;
			case USERCAP:
				userCapUp(bthEvent);
				break;
			case ERROR:
				errorHandle(bthEvent);
				break;
			}
		} catch (Exception e) {
			logger.error("request up error {}", event.getId(), e);
			bthEvent.setException(e);
			errorHandle(bthEvent);
		}
	}

	private void loginPageUp(BoTaoHuiEvent bthEvent) {
		logger.debug("do login page up {}", bthEvent.getId());
		HttpGet httpGet = new HttpGet(loginPageUrl);
		setHeader(loginPageUrl, httpGet, bthEvent);
		httpAsyncClient.execute(httpGet, new HttpAsyncCallback(bthEvent));
	}

	private void nloginUp(BoTaoHuiEvent bthEvent) {
		logger.debug("do get img sig up {}", bthEvent.getId());
		String s = String.format(nloginUrl, new Date().getTime());
		HttpGet httpGet = new HttpGet(s);
		setHeader(s, httpGet, bthEvent);
		httpAsyncClient.execute(httpGet, new HttpAsyncCallback(bthEvent));
	}

	private void realSigUp(BoTaoHuiEvent bthEvent) {
		logger.debug("do get real img sig up {}", bthEvent.getId());
		String s = String.format(realSigUrl, bthEvent.getAppid(), bthEvent.getUid(), bthEvent.getBuid(),
				bthEvent.getSceneid(), bthEvent.getCaptype(), bthEvent.getSig(), Math.random());
		HttpGet httpGet = new HttpGet(s);
		setHeader(s, httpGet, bthEvent);
		httpAsyncClient.execute(httpGet, new HttpAsyncCallback(bthEvent));
	}

	private void getImgUp(BoTaoHuiEvent bthEvent) {
		logger.debug("do get img up {}", bthEvent.getId());
		String s = String.format(imgUrl, bthEvent.getAppid(), bthEvent.getUid(), bthEvent.getBuid(),
				bthEvent.getSceneid(), bthEvent.getCaptype(), bthEvent.getRealSig(), Math.random());
		HttpGet httpGet = new HttpGet(s);
		setHeader(s, httpGet, bthEvent);
		httpGet.setHeader("Referer", "http://www.plateno.com/login.html");
		httpAsyncClient.execute(httpGet, new HttpAsyncCallback(bthEvent));
	}

	private void verifyUp(BoTaoHuiEvent bthEvent) {
		logger.debug("do verify vcode up {}", bthEvent.getId());
		String s = String.format(verifyUrl, bthEvent.getAppid(), bthEvent.getUid(), bthEvent.getBuid(),
				bthEvent.getSceneid(), bthEvent.getCaptype(), bthEvent.getRealSig(), bthEvent.getvCode(),
				Math.random());
		HttpGet httpGet = new HttpGet(s);
		setHeader(s, httpGet, bthEvent);
		httpAsyncClient.execute(httpGet, new HttpAsyncCallback(bthEvent));
	}

	private void sendSmsUp(BoTaoHuiEvent bthEvent) {
		logger.debug("do send sms up {}", bthEvent.getId());
		String s = String.format(smsUrl);
		HttpPost httpPost = new HttpPost(s);
		try {
			httpPost.setEntity(new UrlEncodedFormEntity(createSmsFormEntity(bthEvent), "UTF-8"));
		} catch (Exception e) {
			logger.error("encode sms form body error {}", bthEvent.getId(), e);
			bthEvent.setException(e);
			return;
		}
		setHeader(s, httpPost, bthEvent);
		httpAsyncClient.execute(httpPost, new HttpAsyncCallback(bthEvent));
	}

	private void loginUp(BoTaoHuiEvent bthEvent) {
		logger.debug("do login up {}", bthEvent.getId());
		HttpPost httpPost = new HttpPost(loginUrl);
		try {
			httpPost.setEntity(new UrlEncodedFormEntity(createLoginFormEntity(bthEvent), "UTF-8"));
		} catch (Exception e) {
			logger.error("encode login form body error {}", bthEvent.getId(), e);
			bthEvent.setException(e);
			return;
		}
		setHeader(loginUrl, httpPost, bthEvent);
		httpAsyncClient.execute(httpPost, new HttpAsyncCallback(bthEvent));
	}
	
	private void afterLoginUp(BoTaoHuiEvent bthEvent) {
		logger.debug("do after login up {}", bthEvent.getId());
		HttpGet httpGet = new HttpGet(bthEvent.getRedirectUrl());
		setHeader(bthEvent.getRedirectUrl(), httpGet, bthEvent);
		httpAsyncClient.execute(httpGet, new HttpAsyncCallback(bthEvent));
	}
	
	private void userCapUp(BoTaoHuiEvent bthEvent) {
		logger.debug("do get user info up {}", bthEvent.getId());
		String s = String.format(usercapUrl, new Date().getTime());
		HttpGet httpGet = new HttpGet(s);
		setHeader(s, httpGet, bthEvent);
		httpAsyncClient.execute(httpGet, new HttpAsyncCallback(bthEvent));
	}

	private class HttpAsyncCallback implements FutureCallback<HttpResponse> {

		private BoTaoHuiEvent event;
		private boolean skipNextStep = false;

		public HttpAsyncCallback(BoTaoHuiEvent event) {
			this.event = event;
		}

		@Override
		public void completed(HttpResponse result) {
			try {
				CookieHelper.getCookies(event.getCookieList(), result);
				switch (event.getState()) {
				case LOGINPAGE:
					loginPageDown(result);
					break;
				case NLOGIN:
					nloginDown(result);
					break;
				case REALSIG:
					realSigDown(result);
					break;
				case GETIMG:
					getImgDown(result);
					break;
				case VERIFY:
					verifyDown(result);
					break;
				case SENDSMS:
					sendSmsDown(result);
					break;
				case LOGIN:
					loginDown(result);
					break;
				case AFTERLOGIN:
					afterLoginDown(result);
					break;
				case USERCAP:
					userCapDown(result);
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

		private void loginPageDown(HttpResponse result) {
			logger.debug("login page down {}", event.getId());
			event.setState(BoTaoHuiState.NLOGIN);
		}

		private void nloginDown(HttpResponse result) {
			logger.debug("do get img sig down {}", event.getId());
			try {
				String resultStr = EntityUtils.toString(result.getEntity());
				Matcher funcMatcher = FUNC.matcher(resultStr);
				Matcher ratkMatcher = RATK.matcher(resultStr);
				if (funcMatcher.find() && ratkMatcher.find()) {
					String ans = funcMatcher.group(0);
					Matcher appidMatcher = APPID.matcher(ans);
					Matcher uidMatcher = UID.matcher(ans);
					Matcher buidMatcher = BUID.matcher(ans);
					Matcher sceneidMatcher = SCENEID.matcher(ans);
					Matcher captypeMatcher = CAPTYPE.matcher(ans);
					Matcher sigMatcher = SIG.matcher(ans);
					appidMatcher.find();
					uidMatcher.find();
					buidMatcher.find();
					sceneidMatcher.find();
					captypeMatcher.find();
					sigMatcher.find();
					event.setAppid(appidMatcher.group(0));
					event.setUid(uidMatcher.group(0));
					event.setBuid(buidMatcher.group(0));
					event.setSceneid(sceneidMatcher.group(0));
					event.setCaptype(captypeMatcher.group(0));
					event.setSig(sigMatcher.group(0));

					String ratk = ratkMatcher.group(0);
					event.setRatk(ratk);
				} else {
					logger.error("get ratk and other info error when nlogin down {}", event.getId());
					event.setException(new SpiderException(1073, "获取ratk等信息错误"));
				}
				event.setState(BoTaoHuiState.REALSIG);
			} catch (Exception e) {
				logger.error("get nlogin Down exception", e);
				event.setException(e);
			}
		}

		private void realSigDown(HttpResponse result) {
			logger.debug("do get real img sig down {}", event.getId());
			try {
				String resultStr = EntityUtils.toString(result.getEntity());
				Matcher sigMatcher = REALSIG.matcher(resultStr);
				sigMatcher.find();
				String ans = sigMatcher.group(0);
				event.setRealSig(ans);
				event.setState(BoTaoHuiState.GETIMG);
			} catch (Exception e) {
				logger.error("get real sig Down exception", e);
				event.setException(e);
			}
		}

		private void getImgDown(HttpResponse result) {
			logger.debug("do get img down {}", event.getId());
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
				
				// save to file, used for debug
//				if (appConfig.inDevMode()) {
//					byte[] tbytes = Base64.getDecoder().decode(imgStr);
//					FileOutputStream fs = new FileOutputStream("C:\\Users\\zhouxi\\Desktop\\vcode.jpg");
//					fs.write(tbytes);
//					fs.close();
//				}

				String key = String.format(Constant.BOTAOHUI_IMPORT_KEY, event.getUserid());
				redis.set(key, JSON.toJSONString(event), 300);
				logger.debug("need vfy code {}", event.getId());
				// return vcode to user
				event.setException(new SpiderException(1001, "输入验证码", imgStr));
				return;
			} catch (Exception e) {
				logger.error("get img Down exception", e);
				event.setException(e);
			}
		}

		private void verifyDown(HttpResponse result) {
			logger.debug("do verify vcode down {}", event.getId());
			try {
				String resultStr = EntityUtils.toString(result.getEntity());
				Matcher matcher = JSONSTR.matcher(resultStr);
				if (matcher.find()) {
					String ans = matcher.group(0);
					JSONObject jsonObj = JSONObject.parseObject(ans);
					int errorCode = jsonObj.getIntValue("errorCode");
					if (errorCode == 0) {
						String ticket = jsonObj.getString("ticket");
						event.setTicket(ticket);
						event.setState(BoTaoHuiState.SENDSMS);
					} else {
						logger.debug("vcode or account incorrect, vode:{} account:{}", event.getvCode(), event.getAccount());
						event.setException(new SpiderException(1071, "验证码或手机号错误"));
					}
				} else {
					logger.error("get ticket error, vode:{} account:{}", event.getId());
					event.setException(new SpiderException(1072, "获取ticket错误"));
				}
			} catch (Exception e) {
				logger.error("verify Down exception", e);
				event.setException(e);
			}
		}

		private void sendSmsDown(HttpResponse result) {
			logger.debug("do send sms down {}", event.getId());
			try {
				String resultStr = EntityUtils.toString(result.getEntity());
				JSONObject jsonObj = JSONObject.parseObject(resultStr);
				String rt = jsonObj.getString("result");
				String msg = jsonObj.getString("message");
				if ("failed".equals(rt)) {
					logger.debug("send sms error {}", event.getId());
					event.setException(new SpiderException(1074, msg));
				} else {
					String key = String.format(Constant.BOTAOHUI_IMPORT_KEY, event.getUserid());
					redis.set(key, JSON.toJSONString(event), 300);
					logger.debug("need vfy code {}", event.getId());
					// return vcode to user
					event.setException(new SpiderException(1051, "请输入短信验证码"));
				}
			} catch (Exception e) {
				logger.error("send sms Down exception", e);
				event.setException(e);
			}
		}

		private void loginDown(HttpResponse result) {
			logger.debug("do login down {}", event.getId());
			try {
				
				HttpEntity httpEntity = result.getEntity();
				InputStream inputStream = httpEntity.getContent();
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
                int rd = -1;
                byte[] bytes = new byte[1024];
                while ((rd = inputStream.read(bytes)) != -1) {
                    baos.write(bytes, 0, rd);
                }
//				String resultStr = EntityUtils.toString(result.getEntity());
				JSONObject jsonObj = JSONObject.parseObject(baos.toString());
				String rt = jsonObj.getString("result");
				String mes = jsonObj.getString("message");
				if ("failed".equals(rt)) {
					if( mes.indexOf("用户名或动态密码有误") != -1){
						event.setException(new SpiderException(1053, "用户名或动态密码有误,请返回重试"));
						return;
					}
					logger.debug("login failed, password:{} account:{}", event.getPassword(), event.getAccount());
					event.setException(new SpiderException(1053, "登录失败,请返回重试"));
					return;
				} else {
					String redirectUrl = jsonObj.getString("redirectUrl");
					event.setRedirectUrl(redirectUrl);
					event.setState(BoTaoHuiState.AFTERLOGIN);
				}
			} catch (Exception e) {
				logger.error("login Down exception", e);
				event.setException(e);
			}
		}

		private void afterLoginDown(HttpResponse result) {
			logger.debug("do after login down {}", event.getId());
			try {
				Header header = result.getFirstHeader("Location");
				String mainPage = header.getValue();
				event.setMainPage(mainPage);
				event.setState(BoTaoHuiState.USERCAP);
			} catch (Exception e) {
				logger.error("after login Down exception", e);
				event.setException(e);
			}
		}
		
		private void userCapDown(HttpResponse result) {
			logger.debug("get user info down {}", event.getId());
			try {
				String resultStr = EntityUtils.toString(result.getEntity());
				JSONObject jsonObj = JSON.parseObject(resultStr);
				String name = jsonObj.getString("memberName");
				int point = jsonObj.getIntValue("usablePoint");
				BoTaoHuiResult retResult = new BoTaoHuiResult();
				retResult.setName(name);
				retResult.setPoint(String.valueOf(point));
				event.setException(new SpiderException(0, JSON.toJSONString(retResult)));
			} catch (Exception e) {
				logger.error("user cap Down exception", e);
				event.setException(e);
			}
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

	private List<? extends NameValuePair> createSmsFormEntity(BoTaoHuiEvent event) {
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("mobilePhone", event.getAccount()));
		params.add(new BasicNameValuePair("checkcode", event.getTicket()));
		return params;
	}

	private List<? extends NameValuePair> createLoginFormEntity(BoTaoHuiEvent event) {
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("fromType", "0"));
		params.add(new BasicNameValuePair("username", event.getAccount()));
		params.add(new BasicNameValuePair("password", event.getPassword()));
		params.add(new BasicNameValuePair("lgCheckcode", event.getvCode()));
		params.add(new BasicNameValuePair("atk", event.getRatk()));
		params.add(new BasicNameValuePair("j_expiredays", "0"));
		params.add(new BasicNameValuePair("passwordType", "1"));
		return params;
	}
}
