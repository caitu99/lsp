package com.caitu99.lsp.spider.jingdong;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.caitu99.lsp.model.spider.QueryEvent;
import com.caitu99.lsp.model.spider.jingdong.JingDongEvent;
import com.caitu99.lsp.model.spider.jingdong.JingDongResult;
import com.caitu99.lsp.model.spider.jingdong.JingDongState;
import com.caitu99.lsp.model.spider.jingdong.LoginResult;
import com.caitu99.lsp.spider.HttpAsyncClient;
import org.apache.commons.lang.StringUtils;
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
import com.caitu99.lsp.AppConfig;
import com.caitu99.lsp.Constant;
import com.caitu99.lsp.cache.RedisOperate;
import com.caitu99.lsp.exception.SpiderException;
import com.caitu99.lsp.spider.QuerySpider;
import com.caitu99.lsp.utils.ScriptHelper;
import com.caitu99.lsp.utils.SpringContext;
import com.caitu99.lsp.utils.cookie.CookieHelper;

public class JingDongSpider implements QuerySpider {

	private CloseableHttpAsyncClient httpAsyncClient = HttpAsyncClient.getInstance();
	private AppConfig appConfig = SpringContext.getBean(AppConfig.class);
	private RedisOperate redis = SpringContext.getBean(RedisOperate.class);
	private static final Logger logger = LoggerFactory.getLogger(JingDongSpider.class);
	private static final String userAgent = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_10_5) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/45.0.2454.101 Safari/537.36";
	private static final String preLoginPageUrl = "http://m.jd.com/";
	private static final String passPortUrl = "https://passport.m.jd.com/user/login.action?returnurl=http://m.jd.com?indexloc=1";
	private static final String imgUrl = "https://plogin.m.jd.com/cgi-bin/m/authcode?mod=login";
	private static final String loginUrl = "https://plogin.m.jd.com/cgi-bin/m/domlogin";
	private static final String getdouUrl = "http://home.m.jd.com/wallet/wallet.action?functionId=wodeqianbao&sid=%s";

	//private static final Pattern RSA_VAL = Pattern.compile("(?<=rsa_n=\").*?(?=\")");
	private static final Pattern RSA_VAL = Pattern.compile("str_rsaString = '\\w*';");//20151228 chencheng mod RSA_VAL取值变化
	private static final Pattern STOKEN = Pattern.compile("str_kenString = '\\w*';");//20151228 chencheng mod STOKEN取值变化
	private static final Pattern GO_OPEN = Pattern.compile("(?<=u = \").*?(?=\";)");
	private static final Pattern FANLIYUN = Pattern.compile("(?<=window.location.href=').*?(?=';)");
	private static final Pattern UNION_SEC = Pattern.compile("(?<=hrl=').*?(?=' ;)");
	private static final Pattern JINDOU = Pattern.compile("(?<=<span>).*?(?=</span>京豆)");
	private static final Pattern NAME = Pattern.compile("(?<=pin = ').*?(?=';)");

	@Override
	public void onEvent(QueryEvent event) {
		JingDongEvent jdEvent = (JingDongEvent) event;
		try {
			switch (jdEvent.getState()) {
			case PRE_LOGINPAGE:
				preLoginPageUp(jdEvent);
				break;
			case PASS_PORT:
				passPortUp(jdEvent);
				break;
			case LOGIN_PAGE:
				loginPageUp(jdEvent);
				break;
			case GETIMG:
				imgUp(jdEvent);
				break;
			case LOGIN:
				loginUp(jdEvent);
				break;
			case SUCCCB:
				succcbUp(jdEvent);
				break;
			case INDEXLOC:
				indexLocUp(jdEvent);
				break;
			case GO_OPEN:
				goOpenUp(jdEvent);
				break;
			case FANLIYUN:
				fanliyunUp(jdEvent);
				break;
			case UNION:
				unionUp(jdEvent);
				break;
			case UNION_SEC:
				unionSecUp(jdEvent);
				break;
			case MAIN_PAGE:
				mainPageUp(jdEvent);
				break;
			case GETDOU:
				getdouUp(jdEvent);
				break;
			case ERROR:
				errorHandle(jdEvent);
				break;
			}
		} catch (Exception e) {
			logger.error("request up error {}", event.getId(), e);
			jdEvent.setException(e);
			errorHandle(jdEvent);
		}
	}

	private void preLoginPageUp(JingDongEvent jdEvent) {
		logger.debug("do pre login page up {}", jdEvent.getId());
		HttpGet httpGet = new HttpGet(preLoginPageUrl);
		setHeader(preLoginPageUrl, httpGet, jdEvent);
		httpAsyncClient.execute(httpGet, new HttpAsyncCallback(jdEvent));
	}

	private void passPortUp(JingDongEvent jdEvent) {
		logger.debug("do pass port up {}", jdEvent.getId());
		HttpGet httpGet = new HttpGet(passPortUrl);
		setHeader(passPortUrl, httpGet, jdEvent);
		httpAsyncClient.execute(httpGet, new HttpAsyncCallback(jdEvent));
	}

	private void loginPageUp(JingDongEvent jdEvent) {
		logger.debug("do login page up {}", jdEvent.getId());
		String s = jdEvent.getLoginPage();
		HttpGet httpGet = new HttpGet(s);
		setHeader(s, httpGet, jdEvent);
		httpAsyncClient.execute(httpGet, new HttpAsyncCallback(jdEvent));
	}

	private void imgUp(JingDongEvent jdEvent) {
		logger.debug("do get img up {}", jdEvent.getId());
		HttpGet httpGet = new HttpGet(imgUrl);
		setHeader(imgUrl, httpGet, jdEvent);
		httpAsyncClient.execute(httpGet, new HttpAsyncCallback(jdEvent));
	}

	private void loginUp(JingDongEvent jdEvent) {
		logger.debug("do login up {}", jdEvent.getId());
		HttpPost httpPost = new HttpPost(loginUrl);
		try {
			String password = ScriptHelper.encryptJDPassword(jdEvent.getPassword(), jdEvent.getRsaValue());
			jdEvent.setPassword(password);
			httpPost.setEntity(new UrlEncodedFormEntity(createLoginFormEntity(jdEvent), "UTF-8"));
		} catch (Exception e) {
			logger.error("encode body error {}", jdEvent.getId(), e);
			jdEvent.setException(e);
			return;
		}
		setHeader(loginUrl, httpPost, jdEvent);
		httpAsyncClient.execute(httpPost, new HttpAsyncCallback(jdEvent));
	}

	private void succcbUp(JingDongEvent jdEvent) {
		logger.debug("do succcb up {}", jdEvent.getId());
		HttpGet httpGet = new HttpGet(jdEvent.getSuccb());
		setHeader(jdEvent.getSuccb(), httpGet, jdEvent);
		httpAsyncClient.execute(httpGet, new HttpAsyncCallback(jdEvent));
	}

	private void indexLocUp(JingDongEvent jdEvent) {
		logger.debug("do indexloc up {}", jdEvent.getId());
		HttpGet httpGet = new HttpGet(jdEvent.getIndexLoc());
		setHeader(jdEvent.getIndexLoc(), httpGet, jdEvent);
		httpAsyncClient.execute(httpGet, new HttpAsyncCallback(jdEvent));
	}

	private void goOpenUp(JingDongEvent jdEvent) {
		logger.debug("do go open up {}", jdEvent.getId());
		HttpGet httpGet = new HttpGet(jdEvent.getGoOpenUrl());
		setHeader(jdEvent.getGoOpenUrl(), httpGet, jdEvent);
		httpAsyncClient.execute(httpGet, new HttpAsyncCallback(jdEvent));
	}

	private void fanliyunUp(JingDongEvent jdEvent) {
		logger.debug("do go fanliyun up {}", jdEvent.getId());
		HttpGet httpGet = new HttpGet(jdEvent.getFanliyunUrl());
		setHeader(jdEvent.getFanliyunUrl(), httpGet, jdEvent);
		httpAsyncClient.execute(httpGet, new HttpAsyncCallback(jdEvent));
	}

	private void unionUp(JingDongEvent jdEvent) {
		logger.debug("do union up {}", jdEvent.getId());
		HttpGet httpGet = new HttpGet(jdEvent.getUnionUrl());
		setHeader(jdEvent.getUnionUrl(), httpGet, jdEvent);
		httpGet.setHeader("Referer", jdEvent.getFanliyunUrl());
		httpAsyncClient.execute(httpGet, new HttpAsyncCallback(jdEvent));
	}

	private void unionSecUp(JingDongEvent jdEvent) {
		logger.debug("do union sec up {}", jdEvent.getId());
		HttpGet httpGet = new HttpGet(jdEvent.getUnionSecUrl());
		setHeader(jdEvent.getUnionSecUrl(), httpGet, jdEvent);
		httpAsyncClient.execute(httpGet, new HttpAsyncCallback(jdEvent));
	}

	private void mainPageUp(JingDongEvent jdEvent) {
		logger.debug("do main page up {}", jdEvent.getId());
		HttpGet httpGet = new HttpGet(jdEvent.getMainPageUrl());
		setHeader(jdEvent.getMainPageUrl(), httpGet, jdEvent);
		httpAsyncClient.execute(httpGet, new HttpAsyncCallback(jdEvent));
	}

	private void getdouUp(JingDongEvent jdEvent) {
		logger.debug("do get dou up {}", jdEvent.getId());
		String s = String.format(getdouUrl, jdEvent.getSid());
		HttpGet httpGet = new HttpGet(s);
		setHeader(s, httpGet, jdEvent);
		httpAsyncClient.execute(httpGet, new HttpAsyncCallback(jdEvent));
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

		private JingDongEvent event;
		private boolean skipNextStep = false;

		public HttpAsyncCallback(JingDongEvent event) {
			this.event = event;
		}

		@Override
		public void completed(HttpResponse result) {
			try {
				//CookieHelper.getCookies(event.getCookieList(), result);
				CookieHelper.getCookiesFresh(event.getCookieList(), result);//20151228 chencheng mod 采用正确的逻辑获取cookie
				switch (event.getState()) {
				case PRE_LOGINPAGE:
					preLoginPageDown(result);
					break;
				case PASS_PORT:
					passPortDown(result);
					break;
				case LOGIN_PAGE:
					loginPageDown(result);
					break;
				case GETIMG:
					imgDown(result);
					break;
				case LOGIN:
					loginDown(result);
					break;
				case SUCCCB:
					succcbDown(result);
					break;
				case INDEXLOC:
					indexLocDown(result);
					break;
				case GO_OPEN:
					goOpenDown(result);
					break;
				case FANLIYUN:
					fanliyunDown(result);
					break;
				case UNION:
					unionDown(result);
					break;
				case UNION_SEC:
					unionSecDown(result);
					break;
				case MAIN_PAGE:
					mainPageDown(result);
					break;
				case GETDOU:
					getdouDown(result);
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

		private void preLoginPageDown(HttpResponse result) {
			logger.debug("pre login page down {}", event.getId());
			String sid = CookieHelper.getSpecCookieValue("sid", event.getCookieList());
			event.setSid(sid);
			event.setState(JingDongState.PASS_PORT);
		}

		private void passPortDown(HttpResponse result) {
			logger.debug("pass port down {}", event.getId());
			Header header = result.getFirstHeader("Location");
			event.setLoginPage(header.getValue());
			event.setState(JingDongState.LOGIN_PAGE);
		}

		private void loginPageDown(HttpResponse result) {
			logger.debug("login page down {}", event.getId());
			try {
				String resultStr = EntityUtils.toString(result.getEntity());
				Matcher matcher = RSA_VAL.matcher(resultStr);
				if (matcher.find()) {
					String rsaValue = matcher.group(0);
					rsaValue = rsaValue.substring(17, rsaValue.length()-2);//20151228 chencheng mod rsa_val取值变化
					event.setRsaValue(rsaValue);
					event.setState(JingDongState.GETIMG);
				} else {
					logger.error("get rsa value error: {}", event.getId());
					event.setException(new SpiderException(1040, "获取登录页面rsa错误"));
					return;
				}
				matcher = STOKEN.matcher(resultStr);
				if (matcher.find()) {
					String stoken = matcher.group(0);
					stoken = stoken.substring(17, stoken.length()-2);//20151228 chencheng mod stoken取值变化
					event.setStoken(stoken);
					event.setState(JingDongState.GETIMG);
				} else {
					logger.error("get rsa value error: {}", event.getId());
					event.setException(new SpiderException(1041, "获取登录页面stoken错误"));
					return;
				}
			} catch (Exception e) {
				logger.error("get login Page Down exception", e);
				event.setException(e);
			}
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
				String imgStr = Base64.getEncoder().encodeToString(rbyte);

				// save to file, used for debug
				if (appConfig.inDevMode()) {
					byte[] tbytes = Base64.getDecoder().decode(imgStr);
					FileOutputStream fs = new FileOutputStream(
							appConfig.getUploadPath() + "/" + event.getUserid() + ".jpg");
					fs.write(tbytes);
					fs.close();
				}

				String key = String.format(Constant.JINGDONG_IMPORT_KEY, event.getUserid());
				redis.set(key, JSON.toJSONString(event), 300);
				logger.debug("need vfy code {}", event.getId());
				// return vcode to user
				event.setException(new SpiderException(1001, "输入验证码", imgStr));
				return;

			} catch (Exception e) {
				logger.error("get img exception", e);
				event.setException(e);
			}
		}

		private void loginDown(HttpResponse result) {
			logger.debug("login down {}", event.getId());
			try {
				String resultStr = EntityUtils.toString(result.getEntity());
				LoginResult loginResult = JSON.parseObject(resultStr, LoginResult.class);
				if (loginResult.getErrcode() == 0) {
					// 登录成功
					logger.debug("password correct {}", event.getId());
					event.setSuccb(loginResult.getSucccb());
					event.setState(JingDongState.SUCCCB);
				} else if (loginResult.getErrcode() == 257) {
					logger.debug("vcode incorrect {}", event.getId());
					event.setException(new SpiderException(1044, "验证码输入错误"));
				} else if (loginResult.getErrcode() == 6) {
					logger.debug("password incorrect {}", event.getId());
					event.setException(new SpiderException(1045, "账户名或密码不正确"));
				}else if(loginResult.getErrcode() == 7) { 
					logger.debug("password incorrect {}", event.getId());
					event.setException(new SpiderException(1046, "账户名不存在"));
				}else {
					logger.debug("unknow login error {}", event.getId());
					event.setException(new SpiderException(1013, "登录失败"));
				}
			} catch (Exception e) {
				logger.error("login down exception", e);
				event.setException(e);
			}
		}

		private void succcbDown(HttpResponse result) {
			logger.debug("succcb down {}", event.getId());
			try {
				Header header = result.getFirstHeader("Location");
				String indexLoc = header.getValue();
				event.setIndexLoc(indexLoc);
				event.setState(JingDongState.GETDOU);//20151228 chencheng mod 不需要走indexLoc步骤
			} catch (Exception e) {
				logger.error("get succcb Down exception", e);
				event.setException(e);
			}
		}

		private void indexLocDown(HttpResponse result) {
			logger.debug("indexloc down {}", event.getId());
			try {
				// 首先判断是否可以直接获取京豆
				String sid = CookieHelper.getSpecCookieValue("sid", event.getCookieList());
				if (StringUtils.isNotEmpty(sid)) {
					event.setState(JingDongState.GETDOU);
					return;
				}
				String resultStr = EntityUtils.toString(result.getEntity());
				Matcher matcher = GO_OPEN.matcher(resultStr);
				if (matcher.find()) {
					String goOpenUrl = matcher.group(0);
					event.setGoOpenUrl(goOpenUrl);
					event.setState(JingDongState.GO_OPEN);
				} else {
					logger.error("get go open url exception {}", event.getId());
					event.setException(new SpiderException(1031, "获取jd go open url错误"));
				}
			} catch (Exception e) {
				logger.error("get login Page Down exception", e);
				event.setException(e);
			}
		}

		private void goOpenDown(HttpResponse result) {
			logger.debug("go open down {}", event.getId());
			try {
				String resultStr = EntityUtils.toString(result.getEntity());
				Matcher matcher = FANLIYUN.matcher(resultStr);
				if (matcher.find()) {
					String fanliyunUrl = matcher.group(0);
					event.setFanliyunUrl(fanliyunUrl);
					event.setState(JingDongState.FANLIYUN);
				} else {
					logger.error("get fanliyun url exception {}", event.getId());
					event.setException(new SpiderException(1032, "获取fanliyun url错误"));
				}
			} catch (Exception e) {
				logger.error("get go open down exception", e);
				event.setException(e);
			}
		}

		private void fanliyunDown(HttpResponse result) {
			logger.debug("go fanliyun down {}", event.getId());
			try {
				String resultStr = EntityUtils.toString(result.getEntity());
				Matcher matcher = FANLIYUN.matcher(resultStr);
				if (matcher.find()) {
					String unionUrl = matcher.group(0);
					event.setUnionUrl(unionUrl);
					event.setState(JingDongState.UNION);
				} else {
					logger.error("get union url exception {}", event.getId());
					event.setException(new SpiderException(1033, "获取union url错误"));
				}
			} catch (Exception e) {
				logger.error("get fanliyun down exception", e);
				event.setException(e);
			}
		}

		private void unionDown(HttpResponse result) {
			logger.debug("go union down {}", event.getId());
			try {
				String resultStr = EntityUtils.toString(result.getEntity());
				Matcher matcher = UNION_SEC.matcher(resultStr);
				if (matcher.find()) {
					String unionSecUrl = matcher.group(0);
					event.setUnionSecUrl(unionSecUrl);
					event.setState(JingDongState.UNION_SEC);
				} else {
					logger.error("get union sec url exception {}", event.getId());
					event.setException(new SpiderException(1034, "获取union sec url错误"));
				}
			} catch (Exception e) {
				logger.error("get union down exception", e);
				event.setException(e);
			}
		}

		private void unionSecDown(HttpResponse result) {
			logger.debug("go union sec down {}", event.getId());
			try {
				Header header = result.getFirstHeader("Location");
				String mainPageUrl = header.getValue();
				event.setMainPageUrl(mainPageUrl);
				event.setState(JingDongState.MAIN_PAGE);
			} catch (Exception e) {
				logger.error("get union sec down exception", e);
				event.setException(e);
			}
		}

		private void mainPageDown(HttpResponse result) {
			logger.debug("go main page down {}", event.getId());
			try {

				String sid = CookieHelper.getSpecCookieValue("sid", event.getCookieList());
				if (StringUtils.isNotEmpty(sid)) {
					event.setSid(sid);
					event.setState(JingDongState.GETDOU);
				} else {
					logger.error("get jd sid exception {}", event.getId());
					event.setException(new SpiderException(1035, "获取jd sid错误"));
				}
			} catch (Exception e) {
				logger.error("get union sec down exception", e);
				event.setException(e);
			}
		}

		private void getdouDown(HttpResponse result) {
			logger.debug("do get dou down {}", event.getId());
			try {
				String resultStr = EntityUtils.toString(result.getEntity());
				Matcher matcher = JINDOU.matcher(resultStr);
				Matcher nameMatcher = NAME.matcher(resultStr);
				if (matcher.find() && nameMatcher.find()) {
					String jindou = matcher.group(0);
					String name = nameMatcher.group(0);
					event.setException(
							new SpiderException(0, "获取京豆成功", JSON.toJSONString(new JingDongResult(jindou, name))));
				} else {
					logger.error("get jindou exception {}", event.getId());
					event.setException(new SpiderException(1036, "获取京豆错误"));
				}
			} catch (Exception e) {
				logger.error("get union sec down exception", e);
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

	private List<? extends NameValuePair> createLoginFormEntity(JingDongEvent event) {
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("username", event.getAccount()));
		params.add(new BasicNameValuePair("pwd", event.getPassword()));
		params.add(new BasicNameValuePair("remember", "false"));
		params.add(new BasicNameValuePair("s_token", event.getStoken()));
		params.add(new BasicNameValuePair("authcode", event.getvCode()));
		return params;
	}
}
