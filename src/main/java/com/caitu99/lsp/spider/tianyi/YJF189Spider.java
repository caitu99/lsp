package com.caitu99.lsp.spider.tianyi;

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
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.request.async.DeferredResult;

import com.alibaba.fastjson.JSON;
import com.caitu99.lsp.Constant;
import com.caitu99.lsp.cache.RedisOperate;
import com.caitu99.lsp.exception.SpiderException;
import com.caitu99.lsp.model.spider.QueryEvent;
import com.caitu99.lsp.model.spider.tianyi.YJF189SpiderEvent;
import com.caitu99.lsp.model.spider.tianyi.YJF189SpiderState;
import com.caitu99.lsp.spider.HttpAsyncClient;
import com.caitu99.lsp.spider.QuerySpider;
import com.caitu99.lsp.utils.SpringContext;
import com.caitu99.lsp.utils.XpathHtmlUtils;
import com.caitu99.lsp.utils.cookie.CookieHelper;

public class YJF189Spider implements QuerySpider {

	private static final Logger logger = LoggerFactory
            .getLogger(YJF189Spider.class);

	private RedisOperate redis = SpringContext.getBean(RedisOperate.class);
	
	private static final String userAgent = "Mozilla/5.0 (Windows NT 6.3; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/46.0.2490.86 Safari/537.36";
	private static final String URL_indexPage = "http://sso.telefen.com/ssoV2/sso/login_uam.aspx?service=http%3a%2f%2fy.jf.189.cn%2fselfcenter%2fIndex.aspx";
	private static final String URL_loginPage = "https://uam.ct10000.com/ct10000uam/login?service=http%3a%2f%2fsso.telefen.com%2fssoV2%2fsso%2fuamback.aspx%3fservice%3daHR0cDovL3kuamYuMTg5LmNuL3NlbGZjZW50ZXIvSW5kZXguYXNweA==&serviceId=35000&pl=01002&UserIp=172.17.56.1&register=registerMB";
	private static final String URL_img = "https://uam.ct10000.com/ct10000uam/validateImg.jsp?rand=0.726239068623494";
	private static final String URL_userInfo = "https://uam.ct10000.com/ct10000uam/FindPhoneAreaServlet";
	private static final String URL_login = "https://uam.ct10000.com/ct10000uam/login?service=http%3a%2f%2fsso.telefen.com%2fssoV2%2fsso%2fuamback.aspx%3fservice%3daHR0cDovL3kuamYuMTg5LmNuL3NlbGZjZW50ZXIvSW5kZXguYXNweA==&serviceId=35000&pl=01002&UserIp=172.17.56.1&register=registerMB";
	//private static final String URL_jf = "http://y.jf.189.cn/selfcenter/Index.aspx?ticket=telefen-ST-ef3f575d8dc042c1ba567e2f3919773c";
	private static final String URL_loginAfter = "http://sso.telefen.com/ssoV2/sso/uamback.aspx?service=aHR0cDovL3kuamYuMTg5LmNuL3NlbGZjZW50ZXIvSW5kZXguYXNweA==&UATicket=3535000ST--212351-HHrClGXeP5fJmnL3tkeN-ct10000uam";
	private static final String URL_logout = "http://jf.189.cn/Home/ServerLogout.aspx";
	
	
	
	private static final Pattern Sso_Back = Pattern.compile("(http://).*?(?=')");
	private static final Pattern Jf_Back = Pattern.compile("(http://).*?(?=';)");
	private CloseableHttpAsyncClient httpAsyncClient = HttpAsyncClient.getInstance();
	
	
	@Override
	public void onEvent(QueryEvent event) {
		YJF189SpiderEvent yjf189Event = (YJF189SpiderEvent)event;
		try {
			switch (yjf189Event.getState()) {
			case NONE:
				indexPageUp(yjf189Event);
				break;
			case LOGIN_PAGE:
				loginPageUp(yjf189Event);
				break;
			case IMG:
				imgUp(yjf189Event);
				break;
			case USERINFO:
				userinfoUp(yjf189Event);
				break;
			case LOGIN:
				loginUp(yjf189Event);
				break;
			case LOCATION:
				locationUp(yjf189Event);
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
	 * @Title: userinfoUp 
	 * @param yjf189Event
	 * @date 2016年3月15日 上午9:39:28  
	 * @author ws
	 * @throws UnsupportedEncodingException 
	*/
	private void userinfoUp(YJF189SpiderEvent yjf189Event) throws UnsupportedEncodingException {
		logger.info("userinfoUp {}", yjf189Event.getAccount());
		String url = URL_userInfo;
		
		Map<String,String > param = yjf189Event.getParamMap();
		
        HttpPost httpPost = new HttpPost(url);
        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("username", yjf189Event.getAccount()));

        httpPost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
        setHeader(url, httpPost, yjf189Event);
		httpPost.setHeader("Origin", "https://uam.ct10000.com");
        httpPost.setHeader("Referer", "https://uam.ct10000.com/ct10000uam/login?service=http%3a%2f%2fsso.telefen.com%2fssoV2%2fsso%2fuamback.aspx%3fservice%3daHR0cDovL3kuamYuMTg5LmNuL3NlbGZjZW50ZXIvSW5kZXguYXNweA==&serviceId=35000&pl=01002&UserIp=172.17.56.1&register=registerMB");
        
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
	private void logoutUp(YJF189SpiderEvent yjf189Event) {
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
	private void ssoUp(YJF189SpiderEvent yjf189Event) {
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
	private void gainUp(YJF189SpiderEvent yjf189Event) {
		logger.info("do gainUp {}", yjf189Event.getId());
        HttpGet httpGet = new HttpGet(yjf189Event.getJfUrl());
        setHeader(yjf189Event.getJfUrl(), httpGet, yjf189Event);
        httpGet.setHeader("Cookie", "");
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
	private void imgUp(YJF189SpiderEvent yjf189Event) {
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
	private void locationUp(YJF189SpiderEvent yjf189Event) {
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
	private void indexPageUp(YJF189SpiderEvent yjf189Event) {
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
	private void loginPageUp(YJF189SpiderEvent yjf189Event) {
		logger.info("do loginPageUp {}", yjf189Event.getId());
        HttpGet httpGet = new HttpGet(URL_loginPage);
        setHeader(URL_loginPage, httpGet, yjf189Event);
		//httpGet.setHeader("Origin", "http://login.189.cn");
		httpGet.setHeader("Referer", "http://sso.telefen.com/ssoV2/sso/login_uam.aspx?service=http%3a%2f%2fy.jf.189.cn%2fselfcenter%2fIndex.aspx");
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
	private void loginUp(YJF189SpiderEvent yjf189Event) throws Exception {
		logger.info("loginUp {}", yjf189Event.getAccount());
		String url = URL_login;
		
		Map<String,String > param = yjf189Event.getParamMap();
		
        HttpPost httpPost = new HttpPost(url);
        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("forbidpass", param.get("forbidpass")));
        params.add(new BasicNameValuePair("forbidaccounts", param.get("forbidaccounts")));
        params.add(new BasicNameValuePair("authtype", param.get("authtype")));
        params.add(new BasicNameValuePair("open_no", param.get("open_no")));
        params.add(new BasicNameValuePair("customFileld02", yjf189Event.getpId())); //
        params.add(new BasicNameValuePair("areaname", yjf189Event.getAreaname()));//
        params.add(new BasicNameValuePair("submitBtn1", "正在提交....."));
        params.add(new BasicNameValuePair("lt", param.get("lt")));//
        params.add(new BasicNameValuePair("_eventId", param.get("_eventId")));
        //params.add(new BasicNameValuePair("c2000004RmbMe", "on"));
        params.add(new BasicNameValuePair("username", yjf189Event.getAccount()));//
        params.add(new BasicNameValuePair("randomId", yjf189Event.getvCode()));//
        params.add(new BasicNameValuePair("customFileld01", "3"));//
        params.add(new BasicNameValuePair("password", yjf189Event.getPassword()));
        params.add(new BasicNameValuePair("password_r", ""));
        params.add(new BasicNameValuePair("password_c", ""));

        httpPost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
        setHeader(url, httpPost, yjf189Event);
		httpPost.setHeader("Origin", "https://uam.ct10000.com");
        httpPost.setHeader("Referer", "https://uam.ct10000.com/ct10000uam/login?service=http%3a%2f%2fsso.telefen.com%2fssoV2%2fsso%2fuamback.aspx%3fservice%3daHR0cDovL3kuamYuMTg5LmNuL3NlbGZjZW50ZXIvSW5kZXguYXNweA==&serviceId=35000&pl=01002&UserIp=172.17.56.1&register=registerMB");
        
        httpAsyncClient.execute(httpPost, new HttpAsyncCallback(yjf189Event));
		
	}

	@Override
	public void errorHandle(QueryEvent event) {
		DeferredResult<Object> deferredResult = event.getDeferredResult();
        if (deferredResult == null) {
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
		private YJF189SpiderEvent event;
        private boolean skipNextStep = false;

		public HttpAsyncCallback(YJF189SpiderEvent event) {
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
				case LOGIN_PAGE:
					loginPageDown(response);
					break;
				case IMG:
					imgDown(response);
					break;
				case USERINFO:
					userinfoDown(response);
					break;
				case LOGIN:
					loginDown(response);
					break;
				case LOCATION:
					locationDown(response);
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
		 * @Title: userinfoDown 
		 * @param response
		 * @date 2016年3月15日 上午9:59:14  
		 * @author ws
		*/
		private void userinfoDown(HttpResponse response) {
        	logger.info("userinfoDown {}", event.getId());
			try {
				String loginPage = EntityUtils.toString(response
		                .getEntity());
				
				String[] strs = loginPage.split("\\|");
				if(strs.length == 2){
					event.setpId(strs[0]);
					event.setAreaname(strs[1]);
					event.setState(YJF189SpiderState.LOGIN);
				}else{
					event.setException(new SpiderException(3001, "用户不存在"));
				}
			} catch (Exception e) {
				logger.warn("获取用户信息失败", e);
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
			logger.info("indexPageDown {}", event.getId());
			event.setState(YJF189SpiderState.LOGIN_PAGE);
		}
		
		/**
		 * 	
		 * @Description: (方法职责详细描述,可空)  
		 * @Title: loginPageDown 
		 * @param response
		 * @date 2016年3月9日 下午6:11:50  
		 * @author ws
		*/
		private void loginPageDown(HttpResponse response) {
        	logger.info("loginPageDown {}", event.getId());
			try {
				String loginPage = EntityUtils.toString(response
		                .getEntity());

				Map<String, String> paramMap = getLoginParam(loginPage,"input");
				event.setParamMap(paramMap);

				event.setState(YJF189SpiderState.IMG);
			} catch (Exception e) {
				logger.warn("加载登录页面失败", e);
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
		 * @throws Exception 
         */
        private Map<String, String> getLoginParam(String loginPage,String tagName){
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

                    	logger.info("getLoginParam name:{},value:{}",name,element.attr("value"));
                    }
				}
            	
            	/*logger.info("getLoginParam getElementsByTagName");
                NodeList nodeList = document.getElementsByTagName(tagName);
            	logger.info("getLoginParam foreach");
                for (int i = 0; i < nodeList.getLength(); i++) {
                    Node node = nodeList.item(i);
                    Element e = (Element) node;
                    String type = e.getAttribute("type");
                    String name = e.getAttribute("name");
                    if ("hidden".equals(type) && StringUtils.isNotBlank(name)) {
                        params.put(name, e.getAttribute("value"));

                    	logger.info("getLoginParam name:{},value:{}",name,e.getAttribute("value"));
                    }
                }*/
                return params;
            } catch (Exception e) {
				logger.warn("获取登录信息失败", e);
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
			logger.info("get img down {}", event.getId());
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
				logger.warn("获取图形验证码失败", e);
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
        	logger.info("loginDown {}", event.getId());
			try {
				int status = response.getStatusLine().getStatusCode();
				
				String resBody = EntityUtils.toString(response
	                    .getEntity());
				
	            if (302 == status) {
	                String locationUrl = response.getLastHeader("Location").getValue();
	                
	                event.setLocationUrl(locationUrl);
	                event.setState(YJF189SpiderState.LOCATION);
	                return;
	            } else {
	            	String fileDir = XpathHtmlUtils.deleteHeadHtml(resBody);
		            org.w3c.dom.Document document = XpathHtmlUtils.getCleanHtml(fileDir);
		            XPath xpath = XPathFactory.newInstance().newXPath();
		            // 用户名
		            String remindXpath = "//*[@id='remind']";
		            String remindText = XpathHtmlUtils.getNodeText(remindXpath, xpath, document);
	            	
		            if(remindText.contains("验证码")){
		                event.setException(new SpiderException(3004, remindText));
		                return;
		            }else if(remindText.contains("账号或密码输入错误")){
		            	event.setException(new SpiderException(3005, remindText));
		                return;
		            }else if(remindText.contains("密码简单")){
		            	event.setException(new SpiderException(3005, "账号或密码输入错误"));
		                return;
		            }else{
		                event.setException(new SpiderException(3003, remindText));
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
		 * @Title: locationDown 
		 * @param response
		 * @date 2016年3月11日 下午4:50:27  
		 * @author ws
		*/
		private void locationDown(HttpResponse response) {
			logger.info("locationDown {}", event.getId());
			try {
				String resBody = EntityUtils.toString(response
	                    .getEntity());
				
				Matcher matcher = Sso_Back.matcher(resBody);
				if (matcher.find()) {
					String ssoBack = matcher.group(0);
					logger.info("locationUrl {}", ssoBack);
					event.setSsoUrl(ssoBack);
					event.setState(YJF189SpiderState.SSO);
					return;
				}
				
				event.setException(new SpiderException(3003, "系统繁忙"));
			} catch (Exception e) {
				logger.warn("登录失败失败", e);
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
			logger.info("ssoDown {}", event.getId());
			try {
				String resBody = EntityUtils.toString(response
	                    .getEntity());
				
				Matcher matcher = Jf_Back.matcher(resBody);
				if (matcher.find()) {
					String jfBack = matcher.group(0);
					logger.info("ssoUrl {}", jfBack);
					event.setJfUrl(jfBack);
					event.setState(YJF189SpiderState.GAIN);
					return;
				}

				event.setException(new SpiderException(3003, "系统繁忙"));
			} catch (Exception e) {
				logger.warn("登录失败失败", e);
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
			logger.info("gainDown {}", event.getId());
			try {
				HttpEntity entity = response.getEntity();
	            String entityStr = EntityUtils.toString(entity);
	            
	            String fileDir = XpathHtmlUtils.deleteHeadHtml(entityStr);
	            org.w3c.dom.Document document = XpathHtmlUtils.getCleanHtml(fileDir);
	            XPath xpath = XPathFactory.newInstance().newXPath();

	            // 用户名
	            String namePath = "//*[@id='userInfo']/div[1]/div[2]/div[1]/span";
	            String nameText = XpathHtmlUtils.getNodeText(namePath, xpath, document);

	            // 积分
	            String jfPath = "//*[@id='userInfo']/div[2]/div[1]/div[1]/a/p[2]";
	            String jfText = XpathHtmlUtils.getNodeText(jfPath, xpath, document);

	            //抵用券
	            String dyqPath = "//*[@id='userInfo']/div[2]/div[1]/div[3]/a/p[2]";
	            String dyqText = XpathHtmlUtils.getNodeText(dyqPath, xpath, document);
	            
	            if(StringUtils.isBlank(nameText) || 
	            		StringUtils.isBlank(jfText) ||
	            		StringUtils.isBlank(dyqText)){
	            	// 失败
		            event.setException(new SpiderException(3003, "未获取到用户积分信息"));
		            return;
	            }
	            Map<String,String> gainInfoMap = new HashMap<String, String>();
	            gainInfoMap.put("custName", nameText);
	            gainInfoMap.put("Integral", jfText);
	            gainInfoMap.put("Voucher", dyqText);
	            event.setParamMap(gainInfoMap);
	            event.setState(YJF189SpiderState.LOGOUT);
			} catch (Exception e) {
				logger.warn("登录失败失败", e);
				event.setException(new SpiderException(3002, "系统繁忙"));
			}
		}

		@Override
		public void failed(Exception arg0) {
			
		}
		
	}
}
