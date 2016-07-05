package com.caitu99.lsp.spider.citybank;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.caitu99.lsp.model.spider.QueryEvent;
import com.caitu99.lsp.model.spider.citybank.CityBankEvent;
import com.caitu99.lsp.model.spider.citybank.CityBankState;
import com.caitu99.lsp.spider.HttpAsyncClient;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.request.async.DeferredResult;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.caitu99.lsp.exception.SpiderException;
import com.caitu99.lsp.spider.QuerySpider;
import com.caitu99.lsp.utils.JsHelper;
import com.caitu99.lsp.utils.cookie.CookieHelper;

public class CityBankSpider implements QuerySpider {

    private static final Logger logger = LoggerFactory.getLogger(CityBankSpider.class);

	private CloseableHttpAsyncClient httpAsyncClient = HttpAsyncClient.getInstance();

	private static final String userAgent = "iphone";
	private static final String preLoginPageUrl ="https://www.citibank.com.cn/CNGCB/JPS/portal/LocaleSwitch.do?locale=zh_CN";
	private static final String loginUrl = "https://www.citibank.com.cn/CNGCB/JSO/signon/ProcessUsernameSignon.do";
	private static final String accountInfoUrl = "https://www.citibank.com.cn/CNGCB/REST/accountsPanel/getCustomerAccounts.jws?ttc=742";
	private static final String loginPageRef = "https://www.citibank.com.cn/CNGCB/JPS/portal/LocaleSwitch.do?locale=zh_CN";
	private static final String jifenUrl = "https://www.citibank.com.cn/CNGCB/ICARD/rewhom/displaySummary.do";
	private static final String welcomeUrl = "https://www.citibank.com.cn/CNGCB/REST/welcome/welcomeMsgContent?JFP_TOKEN=%s";
	private static final String functionName = "function %s(){";
	private static final Pattern FUNCTION = Pattern.compile("(?<=jQuery\\(\"input\\.extraField\"\\)\\.remove\\(\\);).*?(?=;\\})");
	private static final Pattern PARAMS = Pattern.compile("(?<=ProcessUsernameSignon\\.do\\?).*?(?=\")");
	private static final Pattern CARDNO = Pattern.compile("(?<=\"cA-rewHom-displaySummaryColumn1\">).*?(?=</div>)", Pattern.DOTALL);
	private static final Pattern JIFEN = Pattern.compile("(?<=\"cA-rewHom-displaySummaryColumn2\" >).*?(?=</div>)", Pattern.DOTALL);
	private static String loginPageUrl;
	private static String logoutUrl;
	private static String JFP_TOKEN;
	@Override
	public void onEvent(QueryEvent event) {
		CityBankEvent cbEvent = (CityBankEvent) event;
		try {
			switch (cbEvent.getState()) {
			case PRELOGINPAGE:
				preLoginPageUp(cbEvent);
				break;
			case LOGINPAGE:
				loginPageUp(cbEvent);
				break;
			case LOGIN:
				loginUp(cbEvent);
				break;
			case HOMEPAGE:
				homePageUp(cbEvent);
				break;
			case WELCOME:
				welcomeUp(cbEvent);
				break;
			case JIFEN:
				jifenUp(cbEvent);
				break;
			case LOGOUT:
				logoutUp(cbEvent);
				break;
			case ACCOUNTINFO:
				accountInfoUp(cbEvent);
				break;
			case ERROR:
				errorHandle(cbEvent);
				break;
			}
		} catch (Exception e) {
			logger.error("request up error {}", event.getId(), e);
			cbEvent.setException(e);
			errorHandle(cbEvent);
		}
	}
	
	/**
	 * 	
	 * @Description: (方法职责详细描述,可空)  
	 * @Title: logoutUp 
	 * @param cbEvent
	 * @date 2015年12月1日 下午7:31:25  
	 * @author zhouxi
	*/
	private void logoutUp(CityBankEvent cbEvent) {
		logger.debug("do logout up {}", cbEvent.getId());
		logoutUrl = "https://www.citibank.com.cn/CNGCB/JSO/signoff/SummaryRecord.do?logOff=true&JFP_TOKEN=" + JFP_TOKEN;
		HttpGet httpGet = new HttpGet(logoutUrl);
		setHeader(logoutUrl, httpGet, cbEvent);
		httpAsyncClient.execute(httpGet, new HttpAsyncCallback(cbEvent));
	}

	private void preLoginPageUp(CityBankEvent cbEvent) {
		logger.debug("do prelogin page up {}", cbEvent.getId());
		HttpGet httpGet = new HttpGet(preLoginPageUrl);
		setHeader(preLoginPageUrl, httpGet, cbEvent);
		httpAsyncClient.execute(httpGet, new HttpAsyncCallback(cbEvent));
		
	}

	private void loginPageUp(CityBankEvent cbEvent) {
		logger.debug("do login page up {}", cbEvent.getId());
		HttpGet httpGet = new HttpGet(loginPageUrl);
		setHeader(loginPageUrl, httpGet, cbEvent);
		httpAsyncClient.execute(httpGet, new HttpAsyncCallback(cbEvent));
	}

	private void loginUp(CityBankEvent cbEvent) {
		logger.debug("do login up {}", cbEvent.getId());
		HttpPost httpPost = new HttpPost(loginUrl);
		try {
			httpPost.setEntity(new UrlEncodedFormEntity(createLoginFormEntity(cbEvent), "UTF-8"));
		} catch (Exception e) {
			logger.error("encode login body error {}", cbEvent.getId(), e);
			cbEvent.setException(e);
			return;
		}
		setHeader(loginUrl, httpPost, cbEvent);
		httpPost.setHeader("Referer", loginPageRef);
		httpAsyncClient.execute(httpPost, new HttpAsyncCallback(cbEvent));
	}
	
	private void homePageUp(CityBankEvent cbEvent) {
		logger.debug("do home page up {}", cbEvent.getId());
		HttpGet httpGet = new HttpGet(cbEvent.getLocation());
		setHeader(cbEvent.getLocation(), httpGet, cbEvent);
		httpGet.setHeader("Referer", loginPageRef);
		httpAsyncClient.execute(httpGet, new HttpAsyncCallback(cbEvent));
	}
	
	private void welcomeUp(CityBankEvent cbEvent) {
		logger.debug("do welcome up {}", cbEvent.getId());
		String s = String.format(welcomeUrl, cbEvent.getJfpToken());
		HttpGet httpGet = new HttpGet(s);
		httpGet.setHeader("X-Requested-With", "XMLHttpRequest");
		setHeader(s, httpGet, cbEvent);
		httpAsyncClient.execute(httpGet, new HttpAsyncCallback(cbEvent));
	}
	
	private void jifenUp(CityBankEvent cbEvent) {
		logger.debug("do get jifen up {}", cbEvent.getId());
		HttpPost httpGet = new HttpPost(jifenUrl);
		setHeader(jifenUrl, httpGet, cbEvent);
		httpGet.setHeader("X-Requested-With", "XMLHttpRequest");
		httpAsyncClient.execute(httpGet, new HttpAsyncCallback(cbEvent));
	}
	
	private void accountInfoUp(CityBankEvent cbEvent) {
		logger.debug("do account info up {}", cbEvent.getId());
		HttpPost httpGet = new HttpPost(accountInfoUrl);
		setHeader(accountInfoUrl, httpGet, cbEvent);
		httpGet.setHeader("X-Requested-With", "XMLHttpRequest");
		httpAsyncClient.execute(httpGet, new HttpAsyncCallback(cbEvent));
	}

	private class HttpAsyncCallback implements FutureCallback<HttpResponse> {

		private CityBankEvent event;
		private boolean skipNextStep = false;

		public HttpAsyncCallback(CityBankEvent event) {
			this.event = event;
		}

		@Override
		public void completed(HttpResponse result) {
			try {
				CookieHelper.getCookiesFresh(event.getCookieList(), result);
				switch (event.getState()) {
				case PRELOGINPAGE:
					preLoginPageDown(result);
					break;
				case LOGINPAGE:
					loginPageDown(result);
					break;
				case LOGIN:
					loginDown(result);
					break;
				case HOMEPAGE:
					homePageDown(result);
					break;
				case WELCOME:
					welcomeDown(result);
					break;
				case JIFEN:
					jifenDown(result);
					break;
				case LOGOUT:
					logoutDown(result);
					break;
				case ACCOUNTINFO:
					accountInfoDown(result);
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
			logger.debug("od prelogin page down {}", event.getId());
			try {
				loginPageUrl = result.getFirstHeader("Location").toString().replaceFirst("Location: ", "");
			} catch (NullPointerException e) {
				event.setException(new SpiderException(1076, "访问官网官网失败"));
				return;
			}
			if( loginPageUrl == null || loginPageUrl.equals("") ){
				event.setException(new SpiderException(1076, "访问官网官网失败"));
				return;
			}
			event.setState(CityBankState.LOGINPAGE);
		}
		
		private void loginPageDown(HttpResponse result) {
			logger.debug("login page down {}", event.getId());
			try {
				String resultStr = EntityUtils.toString(result.getEntity());
				Matcher function = FUNCTION.matcher(resultStr);
				if(function.find()) {
					String funName = function.group(0);
					funName = funName.substring(0, funName.length() - 2);
					String start = String.format(functionName, funName);
					int stIndex = resultStr.indexOf(start);
					int endIndex = stIndex;
					for(int i = 0; i < 7; i++) {
						endIndex = resultStr.indexOf(";", endIndex) + 1;
					}
					String funAll = resultStr.substring(stIndex + start.length(), endIndex) + "}";
					int lastVar = funAll.lastIndexOf("var");
					int lastE = funAll.lastIndexOf("=");
					funAll = funAll.substring(0, lastVar) + funAll.substring(lastE + 1);
					String extra = JsHelper.getCityBankExtraXXX(funAll);
					event.setExtra(extra);
					Matcher paramsMatcher = PARAMS.matcher(resultStr);
					if(paramsMatcher.find()) {
						String paramsStr = paramsMatcher.group(0);
						String[] ans = paramsStr.split("&");
                        for (String an : ans) {
                            String name = an.split("=")[0];
                            String value = an.split("=")[1];
                            if (name.equals("JFP_TOKEN")) {
                                event.setJfpToken(value);
                                JFP_TOKEN = value;
                            } else if (name.equals("SYNC_TOKEN")) {
                                event.setSyncToken(value);
                            }
                        }
						event.setState(CityBankState.LOGIN);
					} else {
						logger.error("get SYNC_TOKEN and JFP_TOKEN exception {}", event.getId());
						event.setException(new SpiderException(1057, "获取SYNC_TOKEN and JFP_TOKEN错误"));
					}
					
				} else {
					logger.error("do not get function of extra_xxx exception {}", event.getId());
					event.setException(new SpiderException(1056, "获取extra function错误"));
				}
				
			} catch (Exception e) {
				logger.error("get login Page Down exception", e);
				event.setException(e);
			}
		}
		
		private void loginDown(HttpResponse result) {
			logger.debug("login page down {}", event.getId());
			try {
				String resultStr = EntityUtils.toString(result.getEntity());
				String value = result.getFirstHeader("Location").getValue();
				event.setLocation(value);
				event.setState(CityBankState.HOMEPAGE);
//				if(value.indexOf("HomePage") != -1) {
//					event.setLocation(value);
//					event.setState(CityBankState.HOMEPAGE);
//				} else {
//					logger.error("username or password error exception {}", event.getId());
//					event.setException(new SpiderException(1060, "用户名或密码错误"));
//				}
				
			} catch (Exception e) {
				logger.error("get login Down exception", e);
				event.setException(e);
			}
		}
		
		private void homePageDown(HttpResponse result) {
			logger.debug("home page down {}", event.getId());
			if(!event.getLocation().contains("HomePage")) {  //登陆失败
				try {
					String resultStr = EntityUtils.toString(result.getEntity());
					if(resultStr.contains("请等待5分钟后可再次登录") || resultStr.contains("若您不进行登录时，请勿停留在登录页面")){
						logger.error("花旗登录过于频繁  exception {}", event.getId());
						event.setException(new SpiderException(1077, "您已登录，请五分钟后重试"));
						return;
					}else if(resultStr.contains("请点击此链接重设网上银行密码")){
						logger.error("花旗用户名或密码错误  exception {}", event.getId());
						event.setException(new SpiderException(1060, "用户名或密码错误"));
					}
				} catch (Exception e) {
					logger.error("homePageDown exception", e);
					event.setException(e);
				}
				logger.error("username or password error exception {}", event.getId());
				event.setException(new SpiderException(1060, "登录时未知错误"));
				return;
			}
			Header[] headers = result.getHeaders("Set-Cookie");
			boolean find = false;
			if(headers != null) {
				for(Header header : headers) {
					String value = header.getValue();
					if(value.contains("JSESSIONID")) {
						find = true;
						break;
					}
				}
				
			}
			if(find) {
				event.setState(CityBankState.WELCOME);
			} else {
				event.setState(CityBankState.LOGINPAGE);
			}
		}
		
		private void welcomeDown(HttpResponse result) {
			logger.debug("welcome down {}", event.getId());
			try {
				String resultStr = EntityUtils.toString(result.getEntity(), "utf-8");
				JSONObject jsonObj = JSON.parseObject(resultStr);
				String lastName = jsonObj.getString("USERLASTNAME");
				String firstName = jsonObj.getString("USERFIRSTNAME");
				event.getCityBankResult().setName(lastName + firstName);
				event.setState(CityBankState.JIFEN);
			} catch (Exception e) {
				logger.error("get welcome Down exception", e);
				event.setException(e);
			}

		}
		
		private void jifenDown(HttpResponse result) {
			logger.debug("get jifen down {}", event.getId());
			try {
				String resultStr = EntityUtils.toString(result.getEntity());
				Matcher cardNoMathcer = CARDNO.matcher(resultStr);
				if(cardNoMathcer.find()) {
					String ans = cardNoMathcer.group(0).trim();
					int stIndex = ans.lastIndexOf(";") + 1;
					String cardno = ans.substring(stIndex);
					event.getCityBankResult().setCardno(cardno.substring(cardno.length() - 4));
				} else {
					logger.error("get cardno error {}", event.getId());
					event.setException(new SpiderException(1058, "获取卡号错误"));
					return ;
				}
				Matcher jifenMatcher = JIFEN.matcher(resultStr);
				if(jifenMatcher.find()) {
					String ans = jifenMatcher.group(0).trim();
					int pointIndex = ans.indexOf(".");
					ans = ans.substring(0, pointIndex).replaceAll(",", "");
					event.getCityBankResult().setJifen(ans);
				} else {
					logger.error("get SYNC_TOKEN and JFP_TOKEN exception {}", event.getId());
					event.setException(new SpiderException(1059, "获取花旗积分错误"));
					return ;
				}
				logger.info("get citybank jifen success {}", event.getId());
				event.setState(CityBankState.LOGOUT);
//				event.setException(new SpiderException(0, "获取花旗积分成功", JSON.toJSONString(event.getCityBankResult())));
			} catch (Exception e) {
				logger.error("get home Page Down exception", e);
				event.setException(e);
			}
		}
		
		private void logoutDown(HttpResponse result) {
			logger.debug("get logout down {}", event.getId());
			event.setException(new SpiderException(0, "获取花旗积分成功", JSON.toJSONString(event.getCityBankResult())));
		}
		
		private void accountInfoDown(HttpResponse result) {
			logger.debug("account info down {}", event.getId());
			try {
				//String resultStr = EntityUtils.toString(result.getEntity());
				event.setState(CityBankState.JIFEN);
			} catch (Exception e) {
				logger.error("get home Page Down exception", e);
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

	private List<? extends NameValuePair> createLoginFormEntity(CityBankEvent event) {
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("username", event.getAccount()));
		params.add(new BasicNameValuePair("password", event.getPassword()));
		params.add(new BasicNameValuePair("JFP_TOKEN", event.getJfpToken()));
		params.add(new BasicNameValuePair("SYNC_TOKEN", event.getSyncToken()));
		params.add(new BasicNameValuePair("XXX_Extra", event.getExtra()));
		return params;
	}
}
