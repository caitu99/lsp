package com.caitu99.lsp.spider.tianyi;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Base64;
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
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.alibaba.fastjson.JSON;
import com.caitu99.lsp.Constant;
import com.caitu99.lsp.cache.RedisOperate;
import com.caitu99.lsp.exception.SpiderException;
import com.caitu99.lsp.model.spider.QueryEvent;
import com.caitu99.lsp.model.spider.tianyi.YJF189ntSpiderEvent;
import com.caitu99.lsp.model.spider.tianyi.YJF189ntSpiderState;
import com.caitu99.lsp.spider.HttpAsyncClient;
import com.caitu99.lsp.spider.QuerySpider;
import com.caitu99.lsp.utils.SpringContext;
import com.caitu99.lsp.utils.XpathHtmlUtils;
import com.caitu99.lsp.utils.cookie.CookieHelper;

public class YJF189ntSpider implements QuerySpider {

	private static final Logger logger = LoggerFactory
            .getLogger(YJF189ntSpider.class);

	private RedisOperate redis = SpringContext.getBean(RedisOperate.class);
	
	private static final String userAgent = "Mozilla/5.0 (Windows NT 6.3; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/46.0.2490.86 Safari/537.36";
	private static final String URL_indexPage = "http://sso.telefen.com/ssoV2/sso/login.aspx?ProvinceID=12&UserType=nt&service=http%3a%2f%2fy.jf.189.cn%2fselfcenter%2fIndex.aspx";
	private static final String URL_img = "http://sso.telefen.com/ssoV2/sso/ValidationCode.aspx";
	private static final String URL_login = "http://sso.telefen.com/ssoV2/services/loginAction.ashx";
	private static final String URL_logout = "http://jf.189.cn/Home/ServerLogout.aspx";
	
	//重置密码
	private static final String URL_passwordResetPage = "http://sso.telefen.com/ssoV2/sso/login_yw.aspx?service=http%3a%2f%2fy.jf.189.cn%2fselfcenter%2fIndex.aspx";
	private static final String URL_passwordReset = "http://sso.telefen.com/ssoV2/services/loginAction.ashx";
	
	private static final Pattern Sso_Back = Pattern.compile("(http://).*?(?=')");
	private static final Pattern Jf_Back = Pattern.compile("(http://).*?(?=';)");
	private CloseableHttpAsyncClient httpAsyncClient = HttpAsyncClient.getInstance();
	
	
	@Override
	public void onEvent(QueryEvent event) {
		YJF189ntSpiderEvent yjf189Event = (YJF189ntSpiderEvent)event;
		try {
			switch (yjf189Event.getState()) {
			case NONE:
				indexPageUp(yjf189Event);
				break;
			case LOCATION_BEFOR:
				locationBeforUp(yjf189Event);
				break;
			case IMG:
				imgUp(yjf189Event);
				break;
			case LOGIN:
				loginUp(yjf189Event);
				break;
			case LOCATION_AFTER:
				locationAfterUp(yjf189Event);
				break;
			case SSO:
				ssoUp(yjf189Event);
				break;
			case GAIN:
				gainUp(yjf189Event);
				break;
			case LOGOUT:
				logoutUp(yjf189Event);
				break;
			case RESET_PAGE:
				resetPageUp(yjf189Event);
				break;
			case MSG:
				msgUp(yjf189Event);
				break;
			case CHECK:
				checkUp(yjf189Event);
				break;
			case RESET:
				resetUp(yjf189Event);
				break;
			case ERROR:
                errorHandle(event);
                break;
			}
		} catch (Exception e) {
			logger.error("request up error {}", event.getId(), e);
			yjf189Event.setException(e);
            errorHandle(yjf189Event);
		}
	}


	/**
	 * 	
	 * @Description: (方法职责详细描述,可空)  
	 * @Title: resetPageUp 
	 * @param yjf189Event
	 * @date 2016年3月14日 下午5:11:28  
	 * @author ws
	*/
	private void resetPageUp(YJF189ntSpiderEvent yjf189Event) {
		logger.info("do logoutUp {}", yjf189Event.getId());
        HttpGet httpGet = new HttpGet(URL_passwordResetPage);
        setHeader(URL_passwordResetPage, httpGet, yjf189Event);
        httpAsyncClient.execute(httpGet, new HttpAsyncCallback(yjf189Event));
	}


	/**
	 * 	
	 * @Description: (方法职责详细描述,可空)  
	 * @Title: resetUp 
	 * @param yjf189Event
	 * @date 2016年3月14日 下午5:09:27  
	 * @author ws
	 * @throws UnsupportedEncodingException 
	*/
	private void resetUp(YJF189ntSpiderEvent yjf189Event) throws UnsupportedEncodingException {
		logger.debug("checkUp {}", yjf189Event.getAccount());
		String url = URL_passwordReset;
		
        HttpPost httpPost = new HttpPost(url);
        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("actionType", "4"));//重置密码
        params.add(new BasicNameValuePair("newPwd", yjf189Event.getPassword()));
        params.add(new BasicNameValuePair("userID", yjf189Event.getAccount()));
        
        httpPost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
        setHeader(url, httpPost, yjf189Event);
		httpPost.setHeader("Origin", "http://sso.telefen.com");
        httpPost.setHeader("Referer", "http://sso.telefen.com/ssoV2/sso/login_yw.aspx?service=http%3a%2f%2fy.jf.189.cn%2fselfcenter%2fIndex.aspx");
        
        httpAsyncClient.execute(httpPost, new HttpAsyncCallback(yjf189Event));
		
	}


	/**
	 * 	
	 * @Description: (方法职责详细描述,可空)  
	 * @Title: checkUp 
	 * @param yjf189Event
	 * @date 2016年3月14日 下午5:09:25  
	 * @author ws
	 * @throws UnsupportedEncodingException 
	*/
	private void checkUp(YJF189ntSpiderEvent yjf189Event) throws UnsupportedEncodingException {
		logger.debug("checkUp {}", yjf189Event.getAccount());
		String url = URL_passwordReset;
		
        HttpPost httpPost = new HttpPost(url);
        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("actionType", "3"));//验证短信验证码
        params.add(new BasicNameValuePair("msCode", yjf189Event.getMsgCode()));
        params.add(new BasicNameValuePair("userID", yjf189Event.getAccount()));
        
        httpPost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
        setHeader(url, httpPost, yjf189Event);
		httpPost.setHeader("Origin", "http://sso.telefen.com");
        httpPost.setHeader("Referer", "http://sso.telefen.com/ssoV2/sso/login_yw.aspx?service=http%3a%2f%2fy.jf.189.cn%2fselfcenter%2fIndex.aspx");
        
        httpAsyncClient.execute(httpPost, new HttpAsyncCallback(yjf189Event));
		
	}


	/**
	 * 	
	 * @Description: (方法职责详细描述,可空)  
	 * @Title: msgUp 
	 * @param yjf189Event
	 * @date 2016年3月14日 下午5:08:59  
	 * @author ws
	 * @throws UnsupportedEncodingException 
	*/
	private void msgUp(YJF189ntSpiderEvent yjf189Event) throws UnsupportedEncodingException {
		logger.debug("msgUp {}", yjf189Event.getAccount());
		String url = URL_passwordReset;
		
        HttpPost httpPost = new HttpPost(url);
        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("actionType", "2"));//发送短信验证码
        params.add(new BasicNameValuePair("msgType", "2"));
        params.add(new BasicNameValuePair("userID", yjf189Event.getAccount()));
        params.add(new BasicNameValuePair("validateCode", yjf189Event.getvCode()));
        
        httpPost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
        setHeader(url, httpPost, yjf189Event);
		httpPost.setHeader("Origin", "http://sso.telefen.com");
        httpPost.setHeader("Referer", "http://sso.telefen.com/ssoV2/sso/login_yw.aspx?service=http%3a%2f%2fy.jf.189.cn%2fselfcenter%2fIndex.aspx");
        
        httpAsyncClient.execute(httpPost, new HttpAsyncCallback(yjf189Event));
		
	}


	/**
	 * 	
	 * @Description: (方法职责详细描述,可空)  
	 * @Title: logoutUp 
	 * @param yjf189Event
	 * @date 2016年3月14日 下午12:05:03  
	 * @author ws
	*/
	private void logoutUp(YJF189ntSpiderEvent yjf189Event) {
		logger.info("do logoutUp {}", yjf189Event.getId());
        HttpGet httpGet = new HttpGet(URL_logout);
        setHeader(URL_logout, httpGet, yjf189Event);
        httpAsyncClient.execute(httpGet, new HttpAsyncCallback(yjf189Event));
	}


	/**
	 * 	
	 * @Description: (方法职责详细描述,可空)  
	 * @Title: ssoUp 
	 * @param yjf189Event
	 * @date 2016年3月11日 下午5:07:14  
	 * @author ws
	*/
	private void ssoUp(YJF189ntSpiderEvent yjf189Event) {
		logger.info("do ssoUp {}", yjf189Event.getId());
        HttpGet httpGet = new HttpGet(yjf189Event.getSsoUrl());
        setHeader(yjf189Event.getSsoUrl(), httpGet, yjf189Event);
        httpAsyncClient.execute(httpGet, new HttpAsyncCallback(yjf189Event));
		
	}


	/**
	 * 	
	 * @Description: (方法职责详细描述,可空)  
	 * @Title: gainUp 
	 * @param yjf189Event
	 * @date 2016年3月11日 下午5:06:59  
	 * @author ws
	*/
	private void gainUp(YJF189ntSpiderEvent yjf189Event) {
		logger.info("do gainUp {}", yjf189Event.getId());
        HttpGet httpGet = new HttpGet(yjf189Event.getJfUrl());
        setHeader(yjf189Event.getJfUrl(), httpGet, yjf189Event);
        httpGet.setHeader("Referer",yjf189Event.getSsoUrl());
        httpAsyncClient.execute(httpGet, new HttpAsyncCallback(yjf189Event));
	}


	/**
	 * 	
	 * @Description: (方法职责详细描述,可空)  
	 * @Title: imgUp 
	 * @param yjf189Event
	 * @date 2016年3月11日 下午4:48:52  
	 * @author ws
	*/
	private void imgUp(YJF189ntSpiderEvent yjf189Event) {
		logger.info("do imgUp {}", yjf189Event.getId());
        HttpGet httpGet = new HttpGet(URL_img);
        setHeader(URL_img, httpGet, yjf189Event);
        httpAsyncClient.execute(httpGet, new HttpAsyncCallback(yjf189Event));
	}


	/**
	 * 	
	 * @Description: (方法职责详细描述,可空)  
	 * @Title: locationUp 
	 * @param yjf189Event
	 * @date 2016年3月11日 下午4:48:24  
	 * @author ws
	*/
	private void locationAfterUp(YJF189ntSpiderEvent yjf189Event) {
		logger.info("do locationUp {}", yjf189Event.getId());
        HttpGet httpGet = new HttpGet(yjf189Event.getLocationUrl());
        setHeader(yjf189Event.getLocationUrl(), httpGet, yjf189Event);
        httpGet.setHeader("Referer", URL_login);
        httpAsyncClient.execute(httpGet, new HttpAsyncCallback(yjf189Event));
	}



	/**
	 * 	
	 * @Description: (方法职责详细描述,可空)  
	 * @Title: indexPageUp 
	 * @param yjf189Event
	 * @date 2016年3月9日 下午6:11:32  
	 * @author ws
	*/
	private void indexPageUp(YJF189ntSpiderEvent yjf189Event) {
		logger.info("do indexPageUp {}", yjf189Event.getId());
        HttpGet httpGet = new HttpGet(URL_indexPage);
        setHeader(URL_indexPage, httpGet, yjf189Event);
        httpAsyncClient.execute(httpGet, new HttpAsyncCallback(yjf189Event));
		
	}



	/**
	 * 	
	 * @Description: (方法职责详细描述,可空)  
	 * @Title: loginPageUp 
	 * @param yjf189Event
	 * @date 2016年3月9日 下午6:11:29  
	 * @author ws
	*/
	private void locationBeforUp(YJF189ntSpiderEvent yjf189Event) {
		logger.info("do loginPageUp {}", yjf189Event.getId());
        HttpGet httpGet = new HttpGet(yjf189Event.getLocationUrl());
        setHeader(yjf189Event.getLocationUrl(), httpGet, yjf189Event);
		//httpGet.setHeader("Origin", "http://login.189.cn");
//		httpGet.setHeader("Referer", "http://sso.telefen.com/ssoV2/sso/login_uam.aspx?service=http%3a%2f%2fy.jf.189.cn%2fselfcenter%2fIndex.aspx");
        httpAsyncClient.execute(httpGet, new HttpAsyncCallback(yjf189Event));
	}




	/**
	 * 	
	 * @Description: (方法职责详细描述,可空)  
	 * @Title: loginUp 
	 * @param yjf189Event
	 * @date 2016年3月9日 下午6:11:26  
	 * @author ws
	 * @throws Exception 
	*/
	private void loginUp(YJF189ntSpiderEvent yjf189Event) throws Exception {
		logger.debug("loginUp {}", yjf189Event.getAccount());
		String url = URL_login;
		
		Map<String,String > param = yjf189Event.getParamMap();
		
        HttpPost httpPost = new HttpPost(url);
        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("loginType", "1"));//用户密码登录
        params.add(new BasicNameValuePair("userName", yjf189Event.getAccount()));
        params.add(new BasicNameValuePair("userPwd", Base64.getEncoder().encodeToString(yjf189Event.getPassword().getBytes())));
        params.add(new BasicNameValuePair("validateCode", yjf189Event.getvCode()));
        params.add(new BasicNameValuePair("actionType", "1")); //固定为1
        params.add(new BasicNameValuePair("refUrl", param.get("hid_refUrl")));//
        
        httpPost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
        setHeader(url, httpPost, yjf189Event);
		httpPost.setHeader("Origin", "http://sso.telefen.com");
        httpPost.setHeader("Referer", "http://sso.telefen.com/ssoV2/sso/login_yw.aspx?service=http%3a%2f%2fy.jf.189.cn%2fselfcenter%2fIndex.aspx");
        
        httpAsyncClient.execute(httpPost, new HttpAsyncCallback(yjf189Event));
		
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
	
	private void setHeader(String uriStr, HttpMessage httpGet, QueryEvent event) {
//		httpGet.setHeader("User-Agent", userAgent);
//		httpGet.setHeader("Connection", "keep-alive");
        httpGet.setHeader("Accept", "*/*");
        httpGet.setHeader("User-Agent", userAgent);
//		httpGet.setHeader("Accept-Encoding", "gzip, deflate, sdch");
//		httpGet.setHeader("Accept-Language", "zh-CN,zh;q=0.8");
        try {
            CookieHelper.setCookies(uriStr, httpGet, event.getCookieList());
        } catch (URISyntaxException e) {
            logger.error("set cookie fail {}", event.getId());
        }
    }

	private class HttpAsyncCallback implements FutureCallback<HttpResponse> {
		private YJF189ntSpiderEvent event;
        private boolean skipNextStep = false;

		public HttpAsyncCallback(YJF189ntSpiderEvent event) {
			super();
			this.event = event;
		}

		@Override
		public void cancelled() {
			
		}

		@Override
		public void completed(HttpResponse response) {
			try {
				CookieHelper.getCookiesFresh(event.getCookieList(), response);
				switch (event.getState()) {
				case NONE:
					indexPageDown(response);
					break;
				case LOCATION_BEFOR:
					locationBeforDown(response);
					break;
				case IMG:
					imgDown(response);
					break;
				case LOGIN:
					loginDown(response);
					break;
				case LOCATION_AFTER:
					locationAfterDown(response);
					break;
				case SSO:
					ssoDown(response);
					break;
				case GAIN:
					gainDown(response);
					break;
				case LOGOUT:
					logoutDown(response);
					break;
				case RESET_PAGE:
					resetPageDown(response);
					break;
				case MSG:
					msgDown(response);
					break;
				case CHECK:
					checkDown(response);
					break;
				case RESET:
					resetDown(response);
					break;
				case ERROR:
	                errorHandle(event);
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

		/**
		 * 	
		 * @Description: (方法职责详细描述,可空)  
		 * @Title: resetPageDown 
		 * @param response
		 * @date 2016年3月14日 下午5:11:55  
		 * @author ws
		*/
		private void resetPageDown(HttpResponse response) {
			event.setState(YJF189ntSpiderState.IMG);
		}

		/**
		 * 	
		 * @Description: (方法职责详细描述,可空)  
		 * @Title: resetDown 
		 * @param response
		 * @date 2016年3月14日 下午5:10:13  
		 * @author ws
		*/
		private void resetDown(HttpResponse response) {
			try {
				Map entityMap = null;
                entityMap = JSON.parseObject(EntityUtils.toString(response
                        .getEntity()));
				
                if(null != entityMap.get("Success") && entityMap.get("Success").equals("True")){
                	event.setException(new SpiderException(0, "重置成功"));
            		return;
                }else{
                	if(null != entityMap.get("ErrMsg")){
                		event.setException(new SpiderException(3003, String.valueOf(entityMap.get("ErrMsg"))));
                		return;
                	}else{
                		event.setException(new SpiderException(3003, "重置密码失败"));
                		return;
                	}
                }
			} catch (Exception e) {

				logger.warn("登录失败失败", e);
				event.setException(new SpiderException(3002, "系统繁忙"));
			}
		}

		/**
		 * 	
		 * @Description: (方法职责详细描述,可空)  
		 * @Title: checkDown 
		 * @param response
		 * @date 2016年3月14日 下午5:10:10  
		 * @author ws
		*/
		private void checkDown(HttpResponse response) {
			try {
				Map entityMap = null;
                entityMap = JSON.parseObject(EntityUtils.toString(response
                        .getEntity()));
				
                if(null != entityMap.get("Success") && entityMap.get("Success").equals("True")){
                	event.setState(YJF189ntSpiderState.RESET);
            		return;
                }else{
                	if(null != entityMap.get("ErrMsg")){
                		event.setException(new SpiderException(3003, String.valueOf(entityMap.get("ErrMsg"))));
                		return;
                	}else{
                		event.setException(new SpiderException(3003, "短信验证码校验失败"));
                		return;
                	}
                }
			} catch (Exception e) {

				logger.warn("登录失败失败", e);
				event.setException(new SpiderException(3002, "系统繁忙"));
			}
		}

		/**
		 * 	
		 * @Description: (方法职责详细描述,可空)  
		 * @Title: msgDown 
		 * @param response
		 * @date 2016年3月14日 下午5:10:08  
		 * @author ws
		*/
		private void msgDown(HttpResponse response) {
			try {
				Map entityMap = null;
                entityMap = JSON.parseObject(EntityUtils.toString(response
                        .getEntity()));
				
                if(null != entityMap.get("Success") && entityMap.get("Success").equals("True")){
                	String key = String.format(Constant.YJF_189_IMPORT_KEY, event.getUserid());
                    redis.set(key, JSON.toJSONString(event), 300);
                	event.setException(new SpiderException(0, "短信验证码已发送"));
            		return;
                }else{
                	if(null != entityMap.get("ErrMsg")){
                		event.setException(new SpiderException(3003, String.valueOf(entityMap.get("ErrMsg"))));
                		return;
                	}else{
                		event.setException(new SpiderException(3003, "发送短信失败"));
                		return;
                	}
                }
			} catch (Exception e) {

				logger.warn("登录失败失败", e);
				event.setException(new SpiderException(3002, "系统繁忙"));
			}
		}

		/**
		 * 	
		 * @Description: (方法职责详细描述,可空)  
		 * @Title: logoutDown 
		 * @param response
		 * @date 2016年3月14日 下午12:05:59  
		 * @author ws
		*/
		private void logoutDown(HttpResponse response) {
			// 成功
            event.setException(new SpiderException(0, "success", JSON.toJSONString(event.getParamMap())));
            
		}

		/**
		 * 	
		 * @Description: (方法职责详细描述,可空)  
		 * @Title: indexPageDown 
		 * @param response
		 * @date 2016年3月9日 下午6:11:53  
		 * @author ws
		*/
		private void indexPageDown(HttpResponse response) {

			try {
				int status = response.getStatusLine().getStatusCode();
				
				String resBody = EntityUtils.toString(response
	                    .getEntity());
				
	            if (302 == status) {
	                String locationUrl = response.getLastHeader("Location").getValue();
	                
	                event.setLocationUrl(locationUrl);
	    			event.setState(YJF189ntSpiderState.LOCATION_BEFOR);
	                return;
	            } else {
	                event.setException(new SpiderException(3003, "系统繁忙"));
	                return;
	            }
			} catch (Exception e) {
				logger.warn("登录失败失败", e);
				event.setException(new SpiderException(3002, "系统繁忙"));
			}
			
		}
		
		/**
		 * 	
		 * @Description: (方法职责详细描述,可空)  
		 * @Title: loginPageDown 
		 * @param response
		 * @date 2016年3月9日 下午6:11:50  
		 * @author ws
		*/
		private void locationBeforDown(HttpResponse response) {
			try {
				int status = response.getStatusLine().getStatusCode();
				
				String resBody = EntityUtils.toString(response
	                    .getEntity());
				
				if (302 == status) {
	                String locationUrl = response.getLastHeader("Location").getValue();
	                
	                event.setLocationUrl(locationUrl);
	    			event.setState(YJF189ntSpiderState.LOCATION_BEFOR);
	                return;
	            } else {
	            	
	            	Map<String, String> paramMap = getLoginParam(resBody,"input");
	            	event.setParamMap(paramMap);
	    			event.setState(YJF189ntSpiderState.IMG);
	                return;
	            }
			} catch (Exception e) {
				logger.warn("登录失败失败", e);
				event.setException(new SpiderException(3002, "系统繁忙"));
			} 
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
         */
        private Map<String, String> getLoginParam(String loginPage,String tagName) {
            Map<String, String> params = new HashMap<String, String>();
            try {
                Document document = XpathHtmlUtils.getCleanHtml(loginPage);
                NodeList nodeList = document.getElementsByTagName(tagName);
                for (int i = 0; i < nodeList.getLength(); i++) {
                    Node node = nodeList.item(i);
                    Element e = (Element) node;
                    String type = e.getAttribute("type");
                    String name = e.getAttribute("name");
                    if ("hidden".equals(type) && StringUtils.isNotBlank(name)) {
                        params.put(name, e.getAttribute("value"));
                    }
                }
                return params;
            } catch (Exception e) {
				logger.warn("获取登录信息异常", e);
                throw e;
            }
        }
		/**
		 * 	
		 * @Description: (方法职责详细描述,可空)  
		 * @Title: imgDown 
		 * @param response
		 * @date 2016年3月11日 下午4:50:52  
		 * @author ws
		*/
		private void imgDown(HttpResponse response) {
			logger.debug("get img down {}", event.getId());
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
                String key = String.format(Constant.YJF_189_IMPORT_KEY, event.getUserid());
                redis.set(key, JSON.toJSONString(event), 300);

                event.setException(new SpiderException(1001, "输入验证码", imgStr));
                return;
            } catch (Exception e) {
				logger.warn("获取图形验证码异常", e);
				event.setException(new SpiderException(3002, "系统繁忙"));
            }
		}

		
		/**
		 * 	
		 * @Description: (方法职责详细描述,可空)  
		 * @Title: loginDown 
		 * @param response
		 * @date 2016年3月9日 下午6:11:47  
		 * @author ws
		 * @throws IOException 
		 * @throws ParseException 
		*/
		private void loginDown(HttpResponse response) throws ParseException, IOException {
			try {
				Map entityMap = null;
                entityMap = JSON.parseObject(EntityUtils.toString(response
                        .getEntity()));
				
                if(null != entityMap.get("Success")){
                	if(entityMap.get("Success").equals("True") && null != entityMap.get("url")){
                		String url = entityMap.get("url").toString();
                		event.setLocationUrl(URLDecoder.decode(url));
                		event.setState(YJF189ntSpiderState.LOCATION_AFTER);
                		return;
                	}else{
                		if(null != entityMap.get("ErrMsg")){
                			if(String.valueOf(entityMap.get("ErrMsg")).contains("验证码")){//验证码错误
                        		event.setException(new SpiderException(3004, String.valueOf(entityMap.get("ErrMsg"))));
                        		return;
                			}else if(String.valueOf(entityMap.get("ErrMsg")).contains("密码错误")){
                				event.setException(new SpiderException(3005, String.valueOf(entityMap.get("ErrMsg"))));
                        		return;
                			}
                    		event.setException(new SpiderException(3003, String.valueOf(entityMap.get("ErrMsg"))));
                    		return;
                    	}else{
                    		event.setException(new SpiderException(3003, "系统繁忙"));
                    		return;
                    	}
                	}
                }else{
                	event.setException(new SpiderException(3003, "系统繁忙"));
                }
			} catch (Exception e) {
				logger.warn("登录异常", e);
				event.setException(new SpiderException(3002, "系统繁忙"));
			}
		}
		

		/**
		 * 	
		 * @Description: (方法职责详细描述,可空)  
		 * @Title: locationDown 
		 * @param response
		 * @date 2016年3月11日 下午4:50:27  
		 * @author ws
		*/
		private void locationAfterDown(HttpResponse response) {
			try {
				int status = response.getStatusLine().getStatusCode();
				
				String resBody = EntityUtils.toString(response
	                    .getEntity());
				//重定向
				if (302 == status) {
	                String locationUrl = response.getLastHeader("Location").getValue();
	                
	                event.setLocationUrl(locationUrl);
	    			event.setState(YJF189ntSpiderState.LOCATION_AFTER);
	                return;
	            } else {
	            	//成功登录
	            	if(resBody.contains("欢迎回来")){
	            		Map<String, String> gainInfoMap = getJfInfo(resBody);
	    	            if(null == gainInfoMap){
	    	            	event.setException(new SpiderException(3003, "获取积分信息失败"));
	    	            	return;
	    	            }
						event.setParamMap(gainInfoMap);
	    	            event.setState(YJF189ntSpiderState.LOGOUT);
	    	            return;
	            	}
	            	//跳转
	            	Matcher matcher = Jf_Back.matcher(resBody);
					if (matcher.find()) {
						String ssoBack = matcher.group(0);
						event.setSsoUrl(ssoBack);
						event.setState(YJF189ntSpiderState.SSO);
						return;
					}else{
						event.setState(YJF189ntSpiderState.GAIN);
						return;
					}
	            }
			} catch (Exception e) {
				logger.warn("登录异常", e);
				event.setException(new SpiderException(3002, "系统繁忙"));
			} 
		}

		/**
		 * 	
		 * @Description: (方法职责详细描述,可空)  
		 * @Title: ssoDown 
		 * @param response
		 * @date 2016年3月10日 下午2:40:40  
		 * @author ws
		*/
		private void ssoDown(HttpResponse response) {
			try {
				int status = response.getStatusLine().getStatusCode();
				
				String resBody = EntityUtils.toString(response
	                    .getEntity());
				//重定向
				if (302 == status) {
	                String ssoUrl = response.getLastHeader("Location").getValue();
	                event.setSsoUrl(ssoUrl);
	    			event.setState(YJF189ntSpiderState.SSO);
	                return;
	            } else {
	            	//成功登录
	            	if(resBody.contains("欢迎回来")){
	            		Map<String, String> gainInfoMap = getJfInfo(resBody);
	    	            if(null == gainInfoMap){
	    	            	event.setException(new SpiderException(3003, "获取用户积分信息失败"));
	    	            	return;
	    	            }
	    	            
						event.setParamMap(gainInfoMap);
	    	            event.setState(YJF189ntSpiderState.LOGOUT);
	    	            return;
	            	}
	            	//跳转
	            	Matcher matcher = Jf_Back.matcher(resBody);
					if (matcher.find()) {
						String jfBack = matcher.group(0);
						event.setJfUrl(jfBack);
						event.setState(YJF189ntSpiderState.GAIN);
						return;
					}else{
						event.setException(new SpiderException(3003, "系统繁忙"));
					}
	            }
			} catch (Exception e) {
				logger.warn("登录异常", e);
				event.setException(new SpiderException(3002, "系统繁忙"));
			}
		}

		
		/**
		 * 	
		 * @Description: (方法职责详细描述,可空)  
		 * @Title: gainDown 
		 * @param response
		 * @date 2016年3月9日 下午6:11:39  
		 * @author ws
		*/
		private void gainDown(HttpResponse response) {
			try {
				HttpEntity entity = response.getEntity();
	            String entityStr = EntityUtils.toString(entity);
	            
	            Map<String, String> gainInfoMap = getJfInfo(entityStr);
	            if(null == gainInfoMap){
	            	event.setException(new SpiderException(3003, "获取用户积分信息失败"));
	            	return;
	            }
	            
	            event.setParamMap(gainInfoMap);
	            event.setState(YJF189ntSpiderState.LOGOUT);
			} catch (Exception e) {
				logger.warn("登录异常", e);
				event.setException(new SpiderException(3002, "系统繁忙"));
			}
		}

		private Map<String, String> getJfInfo(String entityStr)
				throws Exception {
			String fileDir = XpathHtmlUtils.deleteHeadHtml(entityStr);
			Document document = XpathHtmlUtils.getCleanHtml(fileDir);
			XPath xpath = XPathFactory.newInstance().newXPath();

			// 用户名
			String namePath = "//*[@id='userInfo']/div[1]/div[2]/div[1]/span";
			String nameText = XpathHtmlUtils.getNodeText(namePath, xpath, document);

			// 积分   //*[@id="userInfo"]/div[2]/div[1]/div[1]/p[2]
			String jfPath = "//*[@id='userInfo']/div[2]/div[1]/div[1]/p[2]";
			String jfText = XpathHtmlUtils.getNodeText(jfPath, xpath, document);

			//抵用券   //*[@id="userInfo"]/div[2]/div[1]/div[3]/a/p[2]
			String dyqPath = "//*[@id='userInfo']/div[2]/div[1]/div[3]/a/p[2]";
			String dyqText = XpathHtmlUtils.getNodeText(dyqPath, xpath, document);
			
			if(StringUtils.isBlank(nameText) || 
					StringUtils.isBlank(jfText) ||
					StringUtils.isBlank(dyqText)){
				// 失败
			    return null;
			}
			Map<String,String> gainInfoMap = new HashMap<String, String>();
			gainInfoMap.put("custName", nameText);
            gainInfoMap.put("Integral", jfText);
            gainInfoMap.put("Voucher", dyqText);
			return gainInfoMap;
		}

		@Override
		public void failed(Exception arg0) {
			
		}
		
	}
}
