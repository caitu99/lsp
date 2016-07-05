/*
 * Copyright (c) 2015-2020 by caitu99
 * All rights reserved.
 */
package com.caitu99.lsp.spider.liantong;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpMessage;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.request.async.DeferredResult;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.caitu99.lsp.Constant;
import com.caitu99.lsp.cache.RedisOperate;
import com.caitu99.lsp.exception.SpiderException;
import com.caitu99.lsp.model.spider.QueryEvent;
import com.caitu99.lsp.model.spider.liantong.UnicomChinaSpiderEvent;
import com.caitu99.lsp.model.spider.liantong.UnicomChinaSpiderState;
import com.caitu99.lsp.spider.HttpAsyncClient;
import com.caitu99.lsp.spider.QuerySpider;
import com.caitu99.lsp.utils.SpringContext;
import com.caitu99.lsp.utils.XpathHtmlUtils;
import com.caitu99.lsp.utils.cookie.CookieHelper;
import com.caitu99.lsp.utils.cookie.HttpCookieEx;

/**
 * 
 * 
 * @Description: (类职责详细描述,可空) 
 * @ClassName: UnicomChinaSpider 
 * @author ws
 * @date 2016年3月22日 上午10:33:56 
 * @Copyright (c) 2015-2020 by caitu99
 */
public class UnicomChinaSpider implements QuerySpider{

	private CloseableHttpAsyncClient httpAsyncClient = HttpAsyncClient.getInstance();
	private final static Logger logger = LoggerFactory.getLogger(UnicomChinaSpider.class);
	private static final String userAgent = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_10_5) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/45.0.2454.101 Safari/537.36";

	private RedisOperate redis = SpringContext.getBean(RedisOperate.class);
	
	private static final String initUrl = "http://jf.10010.com/login.jsp";
	private static final String jfmalPageUrl = "http://jf.10010.com/servlet/StatisticsServlet?type=PAGE_CLICK&enterTm=1458610419557&outTm=1458610419777&loginSerial=&pageNum=";
	private static final String imgUrl = "http://uac.10010.com/portal/Service/CreateImage?datetime=%s";
	private static final String imgCheckUrl = "http://uac.10010.com/portal/Service/CtaIdyChk?callback=jsonp1458532193842&verifyCode=%s&verifyType=1";
	private static final String loginUrl = "https://uac.10010.com/portal/Service/MallLogin?callback=jsonp1458542334504&req_time=%s&userName=%s&password=%s&pwdType=%s&productType=%s&verifyCode=%s&redirectType=%s&areaCode=%s&arrcity=%s&captchaType=%s&bizCode=%s&uvc=%s";
	private static final String integralUrl = "http://jf.10010.com/user/user_account.jsp";

	/* (non-Javadoc)
	 * @see com.caitu99.spider.spider.IMailSpider#onEvent(com.caitu99.spider.model.MailSpiderEvent)
	 */
	@Override
	public void onEvent(QueryEvent event) {
		UnicomChinaSpiderEvent unicomChinaEvent = (UnicomChinaSpiderEvent) event;
		try {
			switch (unicomChinaEvent.getState()) {
				case NONE:
					initUp(unicomChinaEvent);
					break;
				case PAGE_CLICK:
					pageClickUp(unicomChinaEvent);
					break;
				case IMG:
					imgUp(unicomChinaEvent);
					break;
				case IMG_CHECK:
					imgCheckUp(unicomChinaEvent);
					break;
				case LOGIN_PAGE:
					loginPageUp(unicomChinaEvent);
					break;
				case LOGIN:
					loginUp(unicomChinaEvent);
					break;
				case INTEGRAL:
					integralUp(unicomChinaEvent);
					break;
				case ERROR: // 错误处理
					errorHandle(unicomChinaEvent);
					break;
			}
		} catch (Exception e) {
			logger.warn("request up warn {}", unicomChinaEvent.getId(), e);
			unicomChinaEvent.setException(e);
			errorHandle(unicomChinaEvent);
		}

	}


	/**
	 * 	
	 * @Description: (方法职责详细描述,可空)  
	 * @Title: imgCheckUp 
	 * @param unicomChinaEvent
	 * @date 2016年3月22日 下午12:13:17  
	 * @author ws
	*/
	private void imgCheckUp(UnicomChinaSpiderEvent unicomChinaEvent) {
		logger.info("imgCheckUp {}", unicomChinaEvent.getId());
		String url = String.format(imgCheckUrl, unicomChinaEvent.getvCode());
		HttpGet httpGet = new HttpGet(url);//需要传参
		setHeader(url, httpGet, unicomChinaEvent);
		httpAsyncClient.execute(httpGet, new HttpAsyncCallback(unicomChinaEvent));
	}


	/**
	 * 	
	 * @Description: (方法职责详细描述,可空)  
	 * @Title: integralUp 
	 * @param unicomChinaEvent
	 * @date 2016年3月22日 上午11:03:58  
	 * @author ws
	*/
	private void integralUp(UnicomChinaSpiderEvent unicomChinaEvent) {
		logger.info("integralUp {}", unicomChinaEvent.getId());
		HttpGet httpGet = new HttpGet(integralUrl);
		setHeader(integralUrl, httpGet, unicomChinaEvent);
		httpAsyncClient.execute(httpGet, new HttpAsyncCallback(unicomChinaEvent));
	}


	/**
	 * 	
	 * @Description: (方法职责详细描述,可空)  
	 * @Title: loginUp 
	 * @param unicomChinaEvent
	 * @date 2016年3月22日 上午11:03:37  
	 * @author ws
	*/
	private void loginUp(UnicomChinaSpiderEvent unicomChinaEvent) {
		logger.info("loginUp {}", unicomChinaEvent.getId());
		Date now = new Date();
		Map<String, String> param = unicomChinaEvent.getLoginMap();
		//https://uac.10010.com/portal/Service/MallLogin?callback=jsonp1458542334504
//		&req_time=%s
//		&userName=%s
//		&password=%s
//		&pwdType=%s
//		&productType=%s
//		&verifyCode=%s
//		&redirectType=%s
//		&areaCode=%s
//		&arrcity=%s
//		&captchaType=%s
//		&bizCode=%s
//		&uvc=%s
		String url = String.format(loginUrl, now.getTime()
				,unicomChinaEvent.getAccount()
				,unicomChinaEvent.getPassword()
				,param.get("pwdType")
				,"01"	//param.get("productType")
				,unicomChinaEvent.getvCode()
				,param.get("redirectType")
				,param.get("areaCode")
				,"地区"
				,param.get("captchaType")
				,param.get("bizCode")
				,unicomChinaEvent.getUacverifykey());
		HttpGet httpGet = new HttpGet(url);
		setHeader(url, httpGet, unicomChinaEvent);
		httpAsyncClient.execute(httpGet, new HttpAsyncCallback(unicomChinaEvent));
	}


	/**
	 * 	
	 * @Description: (方法职责详细描述,可空)  
	 * @Title: loginPageUp 
	 * @param unicomChinaEvent
	 * @date 2016年3月22日 上午11:03:21  
	 * @author ws
	*/
	private void loginPageUp(UnicomChinaSpiderEvent unicomChinaEvent) {
		logger.info("loginPageUp {}", unicomChinaEvent.getId());
		
		//设置cookie
		
		
		String url = unicomChinaEvent.getLoginPageUrl();
		HttpGet httpGet = new HttpGet(url);
		setHeader(url, httpGet, unicomChinaEvent);
		httpAsyncClient.execute(httpGet, new HttpAsyncCallback(unicomChinaEvent));
	}


	/**
	 * 	
	 * @Description: (方法职责详细描述,可空)  
	 * @Title: imgUp 
	 * @param unicomChinaEvent
	 * @date 2016年3月22日 上午11:03:05  
	 * @author ws
	*/
	private void imgUp(UnicomChinaSpiderEvent unicomChinaEvent) {
		logger.info("imgUp {}", unicomChinaEvent.getId());
		Date date = new Date();
		String url = String.format(imgUrl, date.getTime());
		HttpGet httpGet = new HttpGet(url);
		setHeader(url, httpGet, unicomChinaEvent);
		httpAsyncClient.execute(httpGet, new HttpAsyncCallback(unicomChinaEvent));
	}


	/**
	 * 	
	 * @Description: (方法职责详细描述,可空)  
	 * @Title: pageClickUp 
	 * @param unicomChinaEvent
	 * @date 2016年3月22日 上午11:02:47  
	 * @author ws
	*/
	private void pageClickUp(UnicomChinaSpiderEvent unicomChinaEvent) {
		logger.info("pageClickUp {}", unicomChinaEvent.getId());
		HttpGet httpGet = new HttpGet(jfmalPageUrl);
		setHeader(jfmalPageUrl, httpGet, unicomChinaEvent);
		httpAsyncClient.execute(httpGet, new HttpAsyncCallback(unicomChinaEvent));
	}


	/**
	 * 	
	 * @Description: (方法职责详细描述,可空)  
	 * @Title: initUp 
	 * @param unicomChinaEvent
	 * @date 2016年3月22日 上午10:57:57  
	 * @author ws
	*/
	private void initUp(UnicomChinaSpiderEvent unicomChinaEvent) {
		logger.info("initUp {}", unicomChinaEvent.getId());
		HttpGet httpGet = new HttpGet(initUrl);
		setHeader(initUrl, httpGet, unicomChinaEvent);
		httpAsyncClient.execute(httpGet, new HttpAsyncCallback(unicomChinaEvent));
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

		private UnicomChinaSpiderEvent event;
		private boolean skipNextStep = false;

		public HttpAsyncCallback(UnicomChinaSpiderEvent event) {
			this.event = event;
		}

		@Override
		public void completed(HttpResponse result) {
			try {
				// extract cookie
				CookieHelper.getCookies(event.getCookieList(), result);

				switch (event.getState()) {
					case NONE:
						initDown(result);
						break;
					case PAGE_CLICK:
						pageClickDown(result);
						break;
					case IMG:
						imgDown(result);
						break;
					case IMG_CHECK:
						imgCheckDown(result);
						break;
					case LOGIN_PAGE:
						loginPageDown(result);
						break;
					case LOGIN:
						loginDown(result);
						break;
					case INTEGRAL:
						integralDown(result);
						break;
					case ERROR:
						errorHandle(event);
						break;
				}
			} catch (Exception e) {
				logger.warn("unexpected warn {}", event.getId(), e);
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
		 * @Title: imgCheckDown 
		 * @param result
		 * @date 2016年3月22日 下午12:14:06  
		 * @author ws
		*/
		private void imgCheckDown(HttpResponse result) {
			try {
				List<HttpCookieEx> cookie = HttpCookieEx.parse("Set-Cookie:_n3fa_cid="
				        		+UnicomChinaUtils.get_n3fa_cid()+"; path=/;");
				event.getCookieList().addAll(cookie );
				
				String entyTime = String.valueOf(Math.round(new Date().getTime() / 1E3));
				
				cookie =  HttpCookieEx.parse("Set-Cookie:_n3fa_lvt_"+UnicomChinaUtils.ID+"="
		        		+entyTime+"; path=/;");
				event.getCookieList().addAll(cookie );
				
				cookie = HttpCookieEx.parse("Set-Cookie:_n3fa_lpvt_"+UnicomChinaUtils.ID+"="
		        		+entyTime+"; path=/;");
				event.getCookieList().addAll(cookie );
				
				cookie = HttpCookieEx.parse("Set-Cookie:_ga=GA1.3.15969404.1458542335; path=/;");
				event.getCookieList().addAll(cookie );

				cookie = HttpCookieEx.parse("Set-Cookie:_dc_gtm_UA-27681312-1=1; path=/;");
				event.getCookieList().addAll(cookie );
				
				String entityStr = EntityUtils.toString(result.getEntity());
				if(entityStr.contains("true")){
					event.setState(UnicomChinaSpiderState.LOGIN);
				}else{
					event.setException(new SpiderException(3004, "验证码错误"));
				}
			} catch (Exception e) {
				logger.warn("验证码验证异常", e);
				event.setException(new SpiderException(3002, "系统繁忙"));
			}
		}

		/**
		 * 	
		 * @Description: (方法职责详细描述,可空)  
		 * @Title: integralDown 
		 * @param result
		 * @date 2016年3月22日 上午11:06:26  
		 * @author ws
		*/
		private void integralDown(HttpResponse result) {
			try {
    			String entityStr = EntityUtils.toString(result.getEntity());
    			String integral = getIntegral(entityStr);
                if(StringUtils.isNotBlank(integral)){
                	event.setException(new SpiderException(0, "获取积分成功",integral));
                }else{
                	event.setException(new SpiderException(3003, "获取积分失败"));
                }
			} catch (Exception e) {
				logger.warn("获取积分异常", e);
				event.setException(new SpiderException(3002, "系统繁忙"));
			}
		}

		private String getIntegral(String entityStr) throws Exception {
			
			entityStr = XpathHtmlUtils.cleanHtml(entityStr);
			//System.out.println(entityStr);
			Document doc = Jsoup.parse(entityStr);
			Element element = doc.getElementById("integral");
			String integral = "";
			
			if(null == element){
				Elements elements = doc.getElementsByTag("h3");
				for (Element el : elements) {
					String str = el.html();
				    str = str.replaceAll(" ", "");
				    str = str.replaceAll("[\\t\\n\\r]","");
				    if(str.indexOf("积分余额：")+5 < str.length()){
				    	integral = str.substring(str.indexOf("积分余额：")+5);
				    	return integral;
				    }
				}
				return integral;
			}else{

				integral = element.html();
			}
			
			return integral;
		}

		/**
		 * 	
		 * @Description: (方法职责详细描述,可空)  
		 * @Title: loginDown 
		 * @param result
		 * @date 2016年3月22日 上午11:06:16  
		 * @author ws
		*/
		private void loginDown(HttpResponse result) {
			
			try {
				String entityStr = EntityUtils.toString(result.getEntity());
    			entityStr = entityStr.substring(entityStr.indexOf("(")+1, entityStr.indexOf(")"));
    			Map entityMap = JSON.parseObject(entityStr);
                if (null == entityMap.get("resultCode")) {
                	//登录失败
                	event.setException(new SpiderException(-1, "登录失败，系统繁忙"));
                }else{
                	String resultCode = String.valueOf(entityMap.get("resultCode"));
                	if(resultCode.equals("0000")){//登录成功
            			event.setState(UnicomChinaSpiderState.INTEGRAL);
            			return;
                	}else if(resultCode.equals("7007") || resultCode.equals("7006") || resultCode.equals("7038")){
                    	//jsonData.resultCode =='7007' || jsonData.resultCode =='7006' || jsonData.resultCode =='7038'
                    	//用户名或密码错误
                    	event.setException(new SpiderException(3005, "用户名或密码错误"));
                	}else{
                		//系统繁忙
                    	event.setException(new SpiderException(3003, "系统繁忙，请稍后再试"));
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
		 * @Title: imgDown 
		 * @param result
		 * @date 2016年3月22日 上午11:05:56  
		 * @author ws
		*/
		private void imgDown(HttpResponse result) {
			logger.info("get img down {}", event.getId());
            try {
            	//uacverifykey  cookie值
            	Header[] headers = result.getHeaders("Set-Cookie");
        		for (Header header : headers) {
        			List<HttpCookieEx> cookies = HttpCookieEx.parse(header.toString());
        			for (HttpCookieEx cookie : cookies) {
        				if (cookie.getName().equals("uacverifykey")) {
        					event.setUacverifykey(cookie.getValue());
        					break;
        				}
        			}
        		}
            	
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

                String key = String.format(Constant.LIAN_TONG_IMPORT_KEY, event.getUserid());
                redis.set(key, JSON.toJSONString(event), 300);

                event.setException(new SpiderException(1001, "请输入验证码", imgStr));
                return;
            } catch (Exception e) {
				logger.warn("获取图形验证码失败", e);
				event.setException(new SpiderException(3002, "系统繁忙"));
            }
		}

		/**
		 * 	
		 * @Description: (方法职责详细描述,可空)  
		 * @Title: loginPageDown 
		 * @param result
		 * @date 2016年3月22日 上午11:06:07  
		 * @author ws
		*/
		private void loginPageDown(HttpResponse result) {
			logger.info("loginPageDown {}", event.getId());
			Map<String, String> params = new HashMap<String, String>();
            try {
    			String loginPage = EntityUtils.toString(result.getEntity());
            	loginPage = XpathHtmlUtils.cleanHtml(loginPage);
            	Document doc = Jsoup.parse(loginPage);
            	Elements elements = doc.getElementsByTag("input");
            	for (Element element : elements) {
            		String type = element.attr("type");
                    String name = element.attr("name");
                    if ("hidden".equals(type) && StringUtils.isNotBlank(name)) {
                        params.put(name, element.attr("value"));
                    	logger.info("getLoginParam name:{},value:{}",name,element.attr("value"));
                    }
				}
            	event.setLoginMap(params);
            	event.setState(UnicomChinaSpiderState.IMG);
			} catch (Exception e) {
				logger.warn("加载登录页面异常", e);
				event.setException(new SpiderException(3002, "系统繁忙"));
			}
		}

		/**
		 * 	
		 * @Description: (方法职责详细描述,可空)  
		 * @Title: pageClickDown 
		 * @param result
		 * @date 2016年3月22日 上午11:05:44  
		 * @author ws
		*/
		private void pageClickDown(HttpResponse result) {
			logger.info("pageClickDown {}", event.getId());
			event.setState(UnicomChinaSpiderState.LOGIN_PAGE);
		}

		/**
		 * 	
		 * @Description: (方法职责详细描述,可空)  
		 * @Title: initDown 
		 * @param result
		 * @date 2016年3月22日 上午11:02:13  
		 * @author ws
		*/
		private void initDown(HttpResponse result) {
			logger.info("initDown {}", event.getId());
			try {
				
		        List<HttpCookieEx> cookie = HttpCookieEx.parse("Set-Cookie:vjuids="
		        		+UnicomChinaUtils.getVjuids()+"; path=/;");
				event.getCookieList().addAll(cookie );
				
				cookie = HttpCookieEx.parse("Set-Cookie:vjlast="
		        		+UnicomChinaUtils.getVjlast()+"; path=/;"); 
				event.getCookieList().addAll(cookie );
				
				cookie = HttpCookieEx.parse("Set-Cookie:WT_FPC="
		        		+UnicomChinaUtils.getWT_FPC()+"; path=/;"); 
				event.getCookieList().addAll(cookie );
				
				Pattern LOGIN_PAGE_PAT = Pattern.compile("(?<=<iframe).*?(?=\">)");
				String resultStr = EntityUtils.toString(result.getEntity());
				resultStr = resultStr.replaceAll(" ", "");
				resultStr = resultStr.replaceAll("[\\t\\n\\r]","");
				Matcher matcher = LOGIN_PAGE_PAT.matcher(resultStr);
				if (matcher.find()) {
					String loginPageUrl = matcher.group(0);
					loginPageUrl = loginPageUrl.substring(loginPageUrl.indexOf("http"), loginPageUrl.length());
					event.setLoginPageUrl(loginPageUrl);
					event.setState(UnicomChinaSpiderState.PAGE_CLICK);
					return;
				} else {
					event.setException(new SpiderException(3003, "系统繁忙"));
					return;
				}
			} catch (Exception e) {
				logger.warn("页面加载异常", e);
				event.setException(new SpiderException(3002, "系统繁忙"));
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

}

