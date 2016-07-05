package com.caitu99.lsp.spider.pufabank;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.lang.StringUtils;
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
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.request.async.DeferredResult;

import com.alibaba.fastjson.JSON;
import com.caitu99.lsp.AppConfig;
import com.caitu99.lsp.Constant;
import com.caitu99.lsp.cache.RedisOperate;
import com.caitu99.lsp.exception.SpiderException;
import com.caitu99.lsp.model.spider.QueryEvent;
import com.caitu99.lsp.model.spider.jingdong.JingDongResult;
import com.caitu99.lsp.model.spider.pufabank.PufaBankEvent;
import com.caitu99.lsp.model.spider.pufabank.PufaBankResult;
import com.caitu99.lsp.model.spider.pufabank.PufaBankState;
import com.caitu99.lsp.spider.HttpAsyncClient;
import com.caitu99.lsp.spider.QuerySpider;
import com.caitu99.lsp.utils.SpringContext;
import com.caitu99.lsp.utils.XpathHtmlUtils;
import com.caitu99.lsp.utils.cookie.CookieHelper;


public class PufaBankSpider implements QuerySpider {

	private static final Logger logger = LoggerFactory.getLogger(PufaBankSpider.class);

	private CloseableHttpAsyncClient httpAsyncClient = HttpAsyncClient.getInstance();
	private RedisOperate redis = SpringContext.getBean(RedisOperate.class);
	private AppConfig appConfig = SpringContext.getBean(AppConfig.class);

	private static final String userAgent = "Mozilla/5.0 (Linux; U; Android 6.0.1; zh-cn; MI 4LTE Build/MMB29M) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/42.0.2311.153 Mobile Safari/537.36 XiaoMi/MiuiBrowser/2.1.1";
	private static final String loginPageUrl = "https://cardsonline.spdbccc.com.cn/icard/icardlogin.do?_locale=zh_CN";
	private static final String imgUrl = "https://cardsonline.spdbccc.com.cn/icard/CaptchaImg?T=%s";
	private static final String loginUrl = "https://cardsonline.spdbccc.com.cn/icard/login.do";
	private static final String verifyUrl = "https://cardsonline.spdbccc.com.cn/icard/checkMobilePwd.do";
	private static final String usercapUrl = "https://cardsonline.spdbccc.com.cn/icard/rewardPointsQuery.do?transName=&_locale=zh_CN&SelectedMenuId=menu5_1_1_1&_viewReferer=defaultError&CardNo=&CardClass=&CardType=&IsJoinCard=&ChangeCardFlag=&htmlType=";

	private static final Pattern FUNC = Pattern.compile("(?<=eval\\(').*?(?='\\);)", Pattern.DOTALL);

	@Override
	public void onEvent(QueryEvent event) {
		PufaBankEvent bthEvent = (PufaBankEvent) event;
		try {
			switch (bthEvent.getState()) {
			case LOGINPAGE:
				loginPageUp(bthEvent);
				break;
			case GETIMG:
				getImgUp(bthEvent);
				break;
			case LOGIN:
				loginUp(bthEvent);
				break;
			case VERIFY:
				verifyUp(bthEvent);
				break;
			case USERCAP:
				userCapUp(bthEvent);
				break;
			case ERROR:
				errorHandle(bthEvent);
				break;
			}
		} catch (Exception e) {
			logger.warn("request up error {}", event.getId(), e);
			bthEvent.setException(e);
			errorHandle(bthEvent);
		}
	}

	private void loginPageUp(PufaBankEvent bthEvent) {
		logger.info("do login page up {}", bthEvent.getId());
		HttpGet httpGet = new HttpGet(loginPageUrl);
		setHeader(loginPageUrl, httpGet, bthEvent);
		httpAsyncClient.execute(httpGet, new HttpAsyncCallback(bthEvent));
	}

	private void getImgUp(PufaBankEvent bthEvent) {
		logger.info("do get img up {}", bthEvent.getId());
		String s = String.format(imgUrl, new Date().getTime());
		HttpGet httpGet = new HttpGet(s);
		httpGet.setHeader("Referer", "https://cardsonline.spdbccc.com.cn/icard/icardlogin.do?_locale=zh_CN");
		setHeader(s, httpGet, bthEvent);
		httpAsyncClient.execute(httpGet, new HttpAsyncCallback(bthEvent));
	}

	private void loginUp(PufaBankEvent bthEvent) throws UnsupportedEncodingException {
		logger.info("do loginUp {}", bthEvent.getId());
		HttpPost httpPost = new HttpPost(loginUrl);
        List<NameValuePair> params = new ArrayList<>();
		Map<String,String> loginParam = bthEvent.getLoginParam();
		
        params.add(new BasicNameValuePair("_viewReferer", loginParam.get("_viewReferer")));
        params.add(new BasicNameValuePair("hiddenPkName", loginParam.get("hiddenPkName")));
        params.add(new BasicNameValuePair("_locale", loginParam.get("_locale")));
        params.add(new BasicNameValuePair("GoodsId", loginParam.get("GoodsId")));
        params.add(new BasicNameValuePair("GoodsTypeFlag", loginParam.get("GoodsTypeFlag")));
        params.add(new BasicNameValuePair("GiftId", loginParam.get("GiftId")));
        params.add(new BasicNameValuePair("trsName", loginParam.get("trsName")));
        params.add(new BasicNameValuePair("ProductId", loginParam.get("ProductId")));
        params.add(new BasicNameValuePair("LoginType", loginParam.get("LoginType")));
        params.add(new BasicNameValuePair("SubmitType", loginParam.get("SubmitType")));
        params.add(new BasicNameValuePair("_mobileSendToken", loginParam.get("_mobileSendToken")));
        params.add(new BasicNameValuePair("IdType", "01"));
        params.add(new BasicNameValuePair("IdNo", bthEvent.getAccount()));
        params.add(new BasicNameValuePair("Password", bthEvent.getPassword()));
        params.add(new BasicNameValuePair("Passwordkeytype", "IcardPublicKey"));
        params.add(new BasicNameValuePair("navigator", "msie"));
        params.add(new BasicNameValuePair("Token", bthEvent.getvCode()));

        httpPost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
        httpPost.setHeader("Origin", "https://cardsonline.spdbccc.com.cn");
        httpPost.setHeader("Referer", "https://cardsonline.spdbccc.com.cn/icard/icardlogin.do?_locale=zh_CN");
        setHeader(loginUrl, httpPost, bthEvent);
        
		httpAsyncClient.execute(httpPost, new HttpAsyncCallback(bthEvent));
	}
	
	private void verifyUp(PufaBankEvent bthEvent) throws UnsupportedEncodingException {
		logger.info("do verify vcode up {}", bthEvent.getId());
		HttpPost httpPost = new HttpPost(verifyUrl);
		
		Map<String,String> verifyParam = bthEvent.getVerifyParam();
		
        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("_viewReferer", "mobileCheck"));
        params.add(new BasicNameValuePair("_locale", "zh_CN"));
        params.add(new BasicNameValuePair("MobilePasswd", bthEvent.getMsmCode()));
        params.add(new BasicNameValuePair("LoginedFlag", "1"));
        params.add(new BasicNameValuePair("_ajaxToken", verifyParam.get("_ajaxToken")));
        
        httpPost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
        httpPost.setHeader("Origin", "https://cardsonline.spdbccc.com.cn");
        httpPost.setHeader("Referer", "https://cardsonline.spdbccc.com.cn/icard/login.do");
        setHeader(verifyUrl, httpPost, bthEvent);
        
		httpAsyncClient.execute(httpPost, new HttpAsyncCallback(bthEvent));
	}
	
	private void userCapUp(PufaBankEvent bthEvent) {
		logger.info("do get user info up {}", bthEvent.getId());
		HttpGet httpGet = new HttpGet(usercapUrl);
		httpGet.setHeader("Referer", "https://cardsonline.spdbccc.com.cn/icard/checkMobilePwd.do");
		setHeader(usercapUrl, httpGet, bthEvent);
		httpAsyncClient.execute(httpGet, new HttpAsyncCallback(bthEvent));
	}

	private class HttpAsyncCallback implements FutureCallback<HttpResponse> {

		private PufaBankEvent event;
		private boolean skipNextStep = false;

		public HttpAsyncCallback(PufaBankEvent event) {
			this.event = event;
		}

		@Override
		public void completed(HttpResponse result) {
			try {
				CookieHelper.getCookiesFresh(event.getCookieList(), result);
				switch (event.getState()) {
				case LOGINPAGE:
					loginPageDown(result);
					break;
				case GETIMG:
					getImgDown(result);
					break;
				case LOGIN:
					loginDown(result);
					break;
				case VERIFY:
					verifyDown(result);
					break;
				case USERCAP:
					userCapDown(result);
					break;
				case ERROR:
					errorHandle(event);
					break;
				}
			} catch (Exception e) {
				logger.warn("unexpected error {}", event.getId(), e);
				event.setException(e);
			}
			// next step
			if (skipNextStep)
				return;
			onEvent(event);

		}

		private void loginPageDown(HttpResponse result) {
			logger.info("login page down {}", event.getId());
			try {
				String loginPage = EntityUtils.toString(result
		                .getEntity());

				Map<String, String> paramMap = getInputParam(loginPage,"input");
				event.setLoginParam(paramMap);

				event.setState(PufaBankState.GETIMG);
			} catch (Exception e) {
				logger.warn("加载登录页面失败,{}", e);
				event.setException(new SpiderException(3002, "系统繁忙，浦发银行系统维护中，请稍后再试"));
			} 
		}

		private void getImgDown(HttpResponse result) {
			logger.info("do get img down {}", event.getId());
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
				
				String key = String.format(Constant.PUFABANK_IMPORT_KEY, event.getUserid());
				redis.set(key, JSON.toJSONString(event), 300);
				event.setException(new SpiderException(1001, "输入验证码", imgStr));
				return;
			} catch (Exception e) {
				logger.warn("get img Down exception:{}", e);
				event.setException(new SpiderException(3002, "系统繁忙，浦发银行系统维护中，请稍后再试"));
			}
		}

		private void loginDown(HttpResponse result) {
			logger.info("do login down {}", event.getId());
			try {
				String resBody = EntityUtils.toString(result
	                    .getEntity());
				
				if(resBody.contains("手机动态密码") || resBody.contains("网银登录动态验证码")){
					//登录成功，需要手机动态密码

					Map<String, String> paramMap = getInputParam(resBody,"input");
					event.setVerifyParam(paramMap);

					String key = String.format(Constant.PUFABANK_IMPORT_KEY, event.getUserid());
					redis.set(key, JSON.toJSONString(event), 300);
					event.setException(new SpiderException(0, "请输入短信验证码"));
				}else if(resBody.contains("您还没有设置您的登录密码，请点击")){
					//登录失败
					event.setException(new SpiderException(3003, "您还没有设置您的登录密码，请设置查询密码后查询"));
				}else if(resBody.contains("校验码输入不正确")){
					//登录失败
					event.setException(new SpiderException(3004, "校验码输入不正确"));
				}else if(resBody.contains("非常抱歉，目前我们无法完成您的请求")){
					//登录失败
					event.setException(new SpiderException(3005, "账号或密码错误"));
				}else{
					//登录失败
					event.setException(new SpiderException(3002, "登录失败"));
				}
			} catch (Exception e) {
				logger.warn("login Down exception：{}", e);
				event.setException(new SpiderException(3002, "系统繁忙，浦发银行系统维护中，请稍后再试"));
			}
		}

		private void verifyDown(HttpResponse result) {
			logger.info("do verify vcode down {}", event.getId());
			try {
				String resBody = EntityUtils.toString(result
                    .getEntity());
			
				//成功的情况     有 安全退出 按钮
				if(resBody.contains("安全退出")){
					//登录成功
					event.setState(PufaBankState.USERCAP);
				}else if(resBody.contains("动态密码输入错误")){
					//登录失败
					event.setException(new SpiderException(3006, "动态密码输入错误"));
				}else{
					//登录失败
					event.setException(new SpiderException(3002, "系统繁忙，浦发银行系统维护中，请稍后再试"));
				}
			} catch (Exception e) {
				logger.warn("verify Down exception：{}", e);
				event.setException(new SpiderException(3002, "系统繁忙，浦发银行系统维护中，请稍后再试"));
			}
		}

		private void userCapDown(HttpResponse result) {
			logger.info("get user info down {}", event.getId());
			try {
				String resBody = EntityUtils.toString(result
	                    .getEntity());
				
				PufaBankResult userInfo = parseUserInfo(resBody);
				
				if(StringUtils.isBlank(userInfo.getCardNo())
						|| StringUtils.isBlank(userInfo.getName())
						|| StringUtils.isBlank(userInfo.getPoint())){
					logger.warn("浦发信用卡用户积分爬取失败,userName:{},cardNo:{},point:{}", userInfo.getName(),userInfo.getCardNo(),userInfo.getPoint());
					event.setException(new SpiderException(3002, "用户积分爬取失败"));
					return;
				}
				
	            event.setException(new SpiderException(0, "success",JSON.toJSONString(userInfo)));
			} catch (Exception e) {
				logger.warn("user cap Down exception：{}", e);
				event.setException(new SpiderException(3002, "系统繁忙，浦发银行系统维护中，请稍后再试"));
			}
		}

		private PufaBankResult parseUserInfo(String resBody) throws Exception {
			PufaBankResult userInfo = new PufaBankResult();
			
			resBody = XpathHtmlUtils.deleteHeadHtml(resBody);
			
			// 用户名
			String nameText = XpathHtmlUtils.getTDValue(resBody,6);
			// 用户名
			String cardNoText = XpathHtmlUtils.getTDValue(resBody,9);
			// 积分
			String pointsText = XpathHtmlUtils.getTDValue(resBody,21);
			
			userInfo.setName(nameText);
			userInfo.setCardNo(cardNoText);
			userInfo.setPoint(pointsText);
			return userInfo;
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
	    private Map<String, String> getInputParam(String loginPage,String tagName){
	    	logger.info("getLoginParam {}", event.getId());

	    	Map<String, String> params = new HashMap<String, String>();
	        try {
	        	loginPage = XpathHtmlUtils.cleanHtml(loginPage);
	        	Document doc = Jsoup.parse(loginPage);
	        	Elements elements = doc.getElementsByTag(tagName);
	        	for (Element element : elements) {
	        		String type = element.attr("type");
	                String name = element.attr("name");
	                if ("hidden".equals(type) && StringUtils.isNotBlank(name)) {
	                    params.put(name, element.attr("value"));
	                	//logger.info("getLoginParam name:{},value:{}",name,element.attr("value"));
	                }
				}
	            return params;
	        } catch (Exception e) {
				logger.warn("获取登录信息失败:{}", e);
	            throw e;
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
			logger.warn("set cookie fail {}", event.getId());
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
			logger.info("a request has been expired: {}", event);
			return;
		}

		Exception exception = event.getException();
		if (exception != null) {
			if (exception instanceof SpiderException) {
				deferredResult.setResult(exception.toString());
			} else {
				logger.warn("unknown exception", exception);
				deferredResult.setResult((new SpiderException(-1, exception.getMessage()).toString()));
			}
		}
	}
	
}
