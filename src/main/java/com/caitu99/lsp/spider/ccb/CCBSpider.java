package com.caitu99.lsp.spider.ccb;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
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
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.request.async.DeferredResult;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.caitu99.lsp.AppConfig;
import com.caitu99.lsp.Constant;
import com.caitu99.lsp.cache.RedisOperate;
import com.caitu99.lsp.exception.SpiderException;
import com.caitu99.lsp.model.spider.QueryEvent;
import com.caitu99.lsp.model.spider.ccbshop.CCBShopEvent;
import com.caitu99.lsp.model.spider.ccbshop.CCBShopState;
import com.caitu99.lsp.spider.HttpAsyncClient;
import com.caitu99.lsp.spider.QuerySpider;
import com.caitu99.lsp.utils.DateUtil;
import com.caitu99.lsp.utils.ScriptHelper;
import com.caitu99.lsp.utils.SpringContext;
import com.caitu99.lsp.utils.XpathHtmlUtils;
import com.caitu99.lsp.utils.cookie.CookieHelper;
import com.caitu99.lsp.utils.cookie.HttpCookieEx;

public class CCBSpider implements QuerySpider {

	private static final Logger logger = LoggerFactory
            .getLogger(CCBSpider.class);

	private RedisOperate redis = SpringContext.getBean(RedisOperate.class);
	
	private static final String ctx = "http://jf.ccb.com";
	private static final String webType = ".jhtml";
	private static final String userAgent = "Mozilla/5.0 (Windows NT 6.3; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/46.0.2490.86 Safari/537.36";
	private static final String checkAccount = "/exchangecenter/register/ajaxExistAccount";
	private static final String sendSmsCodeForRegister = "/exchangecenter/sendSmsCodeForRegister";
	private static final String ajaxExistMobile = "/exchangecenter/register/ajaxExistMobile";
	private static final String registerHtml = "/exchangecenter/register";
	private static final String checkSmsCodeForAjax = "/exchangecenter/register/checkSmsCodeForAjax";
	private static final String registersubmit = "/exchangecenter/registersubmit";
	private static final String loginPageHtml = "http://jf.ccb.com/exchangecenter/login/showLogin.jhtml";
	private static final String getImgCodeHtml = "http://jf.ccb.com/exchangecenter/RandomCodeAction/makeImageCodeForCust.jhtml?q=%s";
	private static final String checkImgCodeHtml = "http://jf.ccb.com/exchangecenter/login/checkRandomCodeForAjax.jhtml";
	private static final String loginHtml = "http://jf.ccb.com/exchangecenter/login/loginForAjax.jhtml";
	private static final String submitOrderDetailHtml = "http://jf.ccb.com/exchangecenter/order/submitOrderdetail.jhtml?productId=%s&orderType=%s&quantity=%s&shopId=%s";
	private static final String submitOrderHtml = "http://jf.ccb.com/exchangecenter/order/submit.jhtml";
	
	private static final String provinceGBAddressJSon = "/customercenter/account/getProvinceGBAddressJSon";
	private static final String cityGBAddressJSon = "/customercenter/account/getCityGBAddressJSon";
	private static final String distinctGBAddressJSon = "/customercenter/account/getDistinctGBAddressJSon";
	private static final String payMsgSendUrl = "https://epay.ccb.com/epay/EPAYMainB1L1?CCB_IBSVersion=V5&SERVLET_NAME=EPAYMainB1L1&TXCODE=EP0005&BRANCHID=310000000&USERID=&SKEY=";
	
	private static final String jflogin = "http://jf.ccb.com/exchangecenter/account/viewScoreResult.jhtml";
	private static final String getSms = "http://jf.ccb.com/exchangecenter/account/viewScoreVerifyCode.jhtml";
	private static final String getJFquery = "http://jf.ccb.com/exchangecenter/account/getScore.jhtml?time=%s&scoreCardNo=%s";
	
	
	private static final String getxykimg = "https://ibsbjstar.ccb.com.cn/NCCB_Encoder/Encoder?CODE=%s";
	private static final String xyklogin = "https://ibsbjstar.ccb.com.cn/app/B2CMainB1L1?CCB_IBSVersion=V5&SERVLET_NAME=B2CMainB1L1";
	private static final String xykgetjf = "http://creditcard.ccb.com/tran/WCCMainPlatV5?CCB_IBSVersion=V5&SERVLET_NAME=WCCMainPlatV5&TXCODE=E13012&ACCT_NO=%s&BRANCHID=310000000&isAjaxRequest=true";
	
	
	private static final String creditLoginPage = "http://creditcard.ccb.com/cn/creditcard/jf_query_login.html";
	private static final String creditTranPage = "http://creditcard.ccb.com/tran/WCCMainPlatV5?CCB_IBSVersion=V5&SERVLET_NAME=WCCMainPlatV5&TXCODE=NE3050";
	
	private static final String creditMainPage = "https://ibsbjstar.ccb.com.cn/CCBIS/B2CMainPlatGate?ccbParam=pdRU%2CQCmEy9M53TzuNvGecCqYuHF9XlUC7EoPcuqcAYOVUTdtCTAcd9ke5yuk1NeRSAMMJmZHCNl%0A%2ChoHKKYhVJcy%2Cu0lElAWUz6Im6E2xO%2FygE99lFYM436tIQw6hx4if%2CG9311s0O7YmqYlUrlz8cNw%0AQjFi0myUGzLP7f7wza9QNxUGqwWUDTH44V7rd%2CyY9OospcwXvlJ5ddV22m7oGv3CyrzS9M1Ke%2CWG%0AznwYyVDksNQR%2CbBW7c7N6qQAgIk4JtrFktqhCga4Yy8FNRmtaG9y8V4QP4e50nu65tSoKgNpHCYv%0Ar3oRUs1ln5SIfmKChfGUC4kVFA7UdfwzQoFHwoyUp7l7dOkx2rT2cp9V4XOMkKFxmZ2H8X8LdT5x%0Aaoie7EytyWbZWRt2WU903y4KLvHqTmPcC4fz1xDmt6C8G4iY2yse5XNQQpLkjWi5gDpdECA3lpV4%0AEe9a4MEuOjV8oR%2Fw03UeD%2C3zy3BSy0%2FdwjzyudwJDJH71i0KFrKLxrX0KjDCgw9%2CH%2Ck%2FOSh5Cuk4%0AYH3NRA9aVPUYK%2CNdIJkcNgsqtc12XXUsUAmvkqqxkKwp1RIs4u2dKXc%3D000000000000005";
	private static final String creditLogin = "https://ibsbjstar.ccb.com.cn/CCBIS/B2CMainPlat_13?SERVLET_NAME=B2CMainPlat_13&CCB_IBSVersion=V6&PT_STYLE=1";
	
	
	private static final Integer redisTime = 600;
	
	
	private CloseableHttpAsyncClient httpAsyncClient = HttpAsyncClient.getInstance();
    private AppConfig appConfig = SpringContext.getBean(AppConfig.class);
	
	
	@Override
	public void onEvent(QueryEvent event) {
		CCBShopEvent ccbShopEvent = (CCBShopEvent)event;
		try {
			switch (ccbShopEvent.getState()) {
			case REGISTER_PAGE:
				registerPageUp(ccbShopEvent);
				break;
			case CHECK_ACCOUNT:
				checkAccountUp(ccbShopEvent);
				break;
			case CHECK_EXISTS_MOBILE:
				checkExistsMobileUp(ccbShopEvent);
				break;
			case REGISTER_VERIFY_CODE:
				sendSmsCodeForRegisterUp(ccbShopEvent);
				break;
			case CHECK_SMS_CODE:
				checkSmsCodeForAjaxUp(ccbShopEvent);
				break;
			case REGISTER:
				registerSubmitUp(ccbShopEvent);
				break;

			case LOGIN_PAGE://登录页面
				loginPageUp(ccbShopEvent);
				break;
			case IMG_CODE_GET://获取验证码
				imgCodeGetUp(ccbShopEvent);
				break;
				
			case IMG_CODE_CHECK://验证验证码
				imgCodeCheckUp(ccbShopEvent);
				break;
			case LOGIN://登录
				loginUp(ccbShopEvent);
				break;
				
			case SUBMIT_ORDER_DETAIL://订单详情页
				submitOrderDetailUp(ccbShopEvent);
				break;
			case SUBMIT_ORDER://提交订单
				submitOrderUp(ccbShopEvent);
				break;
				
				
			case EPAY_MAIN_PLATGATE://支付页面
				epayMainPlatGateUp(ccbShopEvent);
				break;
			case EPAY_MSG_CODE://支付短信验证码发送
				epayMsgCodeUp(ccbShopEvent);
				break;
			case EPAY_MAIN_B1L1://支付
				epayMainB1l1Up(ccbShopEvent);
				break;
				
				//收货地址
			case GET_PROVINCE_GB_ADDRESS_JSON:
				getProvinceGBAddressJSonUp(ccbShopEvent);
				break;
			case GET_CITY_GB_ADDRESS_JSON:
				getCityGBAddressJSonUp(ccbShopEvent);
				break;
			case GET_DISTINCT_GB_ADDRESS_JSON:
				getDistinctGBAddressJSonUp(ccbShopEvent);
				break;
			
			//获取积分
			case JF_INIT:
				InitUp(ccbShopEvent);
				break;
			case JF_SMS_SEND:
				getSmsUp(ccbShopEvent);
				break;
			case JF_LOGIN:
				getJFLoginUp(ccbShopEvent);
				break;				
			case JF_QUERY:
				getJFQueryUp(ccbShopEvent);
				break;	
				
			//信用卡中心获取积分
			case XYK_INIT:
				xykInitUp(ccbShopEvent);
				break;	
			case XYK_GETKEY:
				xykGetkeyUp(ccbShopEvent);
				break;
			case XYK_IMG:
				xykImgUp(ccbShopEvent);
				break;
			case XYK_KEY:
				xykKeyUp(ccbShopEvent);
				break;
				
			case XYK_LOGIN:
				xykloginUp(ccbShopEvent);
				break;
			case XYK_GOTOURL:
				xykgotourlUp(ccbShopEvent);
				break;
			case XYK_GETJF:
				xykgetjfUp(ccbShopEvent);
				break;
				
			case ERROR:
                errorHandle(event);
                break;
			}
		} catch (Exception e) {
			logger.error("request up error {},{}", event.getId(), e);
			ccbShopEvent.setException(e);
            errorHandle(ccbShopEvent);
		}
	}
	
	
	/**
	 * 	
	 * @Description: (方法职责详细描述,可空)  
	 * @Title: xykKeyUp 
	 * @param ccbShopEvent
	 * @date 2016年5月31日 下午3:57:09  
	 * @author ws
	*/
	private void xykKeyUp(CCBShopEvent ccbShopEvent) {
		logger.info("xykKeyUp {}", ccbShopEvent.getAccount());
		String url = creditLogin;
        HttpPost httpPost = new HttpPost(url);
        
        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("TXCODE", ccbShopEvent.getTXCODE()));
        params.add(new BasicNameValuePair("BRANCHID", ccbShopEvent.getBRANCHID()));
        params.add(new BasicNameValuePair("DATE", ccbShopEvent.getDATE()));
        params.add(new BasicNameValuePair("LOGONTYPE", "1"));
        params.add(new BasicNameValuePair("MAC", ""));
        params.add(new BasicNameValuePair("MERCHANTID", ccbShopEvent.getMERCHANTID()));
        params.add(new BasicNameValuePair("SERIALNO", ccbShopEvent.getSERIALNO()));
        params.add(new BasicNameValuePair("TIME", ccbShopEvent.getTIME()));
        params.add(new BasicNameValuePair("T_TXCODE", ccbShopEvent.getT_TXCODE()));
        params.add(new BasicNameValuePair("resType", "jsp"));

        try {
			httpPost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
		} catch (UnsupportedEncodingException e) {
			   logger.error("check account error {}", ccbShopEvent.getAccount(), e);
	            ccbShopEvent.setException(e);
	            return;
		}
        
        setHeader(url, httpPost, ccbShopEvent);
        httpAsyncClient.execute(httpPost, new HttpAsyncCallback(ccbShopEvent));	
	}


	private void xykInitUp(CCBShopEvent ccbShopEvent){
		logger.info("xykInitUp {}", ccbShopEvent.getAccount());
		//String url = "http://creditcard.ccb.com/tran/WCCMainPlatV5?CCB_IBSVersion=V5&SERVLET_NAME=WCCMainPlatV5&TXCODE=NE3050";
        String url = creditTranPage;
		HttpGet httpGet = new HttpGet(url);
        setHeader(url, httpGet, ccbShopEvent);
        httpAsyncClient.execute(httpGet, new HttpAsyncCallback(ccbShopEvent));
	}
	
	private void xykGetkeyUp(CCBShopEvent ccbShopEvent) {
		logger.info("xykGetkeyUp {}", ccbShopEvent.getAccount());
		String url = ccbShopEvent.getGetkeyurl();
		HttpGet httpGet = new HttpGet(url);
        setHeader(url, httpGet, ccbShopEvent);
        httpAsyncClient.execute(httpGet, new HttpAsyncCallback(ccbShopEvent));
	}
	
	
	private void xykImgUp(CCBShopEvent ccbShopEvent){
		logger.info("xykImgUp {}", ccbShopEvent.getAccount());
		String code = ccbShopEvent.getCOOKIES();
		String url = String.format(getxykimg,code.trim());
		HttpGet httpGet = new HttpGet(url);
        setHeader(url, httpGet, ccbShopEvent);
        httpAsyncClient.execute(httpGet, new HttpAsyncCallback(ccbShopEvent));
	}
	
	
	public void xykloginUp(CCBShopEvent ccbShopEvent){
		logger.info("xykloginUp {}", ccbShopEvent.getAccount());
		String url = creditLogin;
        HttpPost httpPost = new HttpPost(url);
        String account = ccbShopEvent.getAccount();
        String password =  ccbShopEvent.getPassword();
        
        String a = ccbShopEvent.getGigest();
        String b = ccbShopEvent.getLogpass();
        
        a = a.replaceAll("\"","");
        
        StringBuilder ACC_NO_temp = new StringBuilder();
        char[] acc = account.toCharArray();
        for (int i = 0; i < acc.length; i++) {
        	ACC_NO_temp.append(acc[i]);
			if((i+1)%4 == 0 && i!=acc.length-1){
				ACC_NO_temp.append(" ");
			}
		}
        
      	Object ob = JSONObject.parse(b);
    	Map<String, String> ss = (Map<String, String>)ob;
    	int m = ss.size();
	    String[] bstring = new String[m];
	    for (String key : ss.keySet()) {
	    	int intkey = Integer.parseInt(key);
	    	bstring[intkey] = ss.get(key);
	    }
    	
	    String logpass = this.getLogpass(password, bstring);
        //String gigest = this.getjsparam(a);
        
        String MERCHANTID = ccbShopEvent.getMERCHANTID();
        String BRANCHID = ccbShopEvent.getBRANCHID();
        String SERIALNO = ccbShopEvent.getSERIALNO();
        String TXCODE = ccbShopEvent.getTXCODE();
        String T_TXCODE = ccbShopEvent.getT_TXCODE();
        String DATE = ccbShopEvent.getDATE();
        String TIME = ccbShopEvent.getTIME();
        String SYS_TYPE = ccbShopEvent.getSYS_TYPE();
        String COOKIES = ccbShopEvent.getCOOKIES();
        String errURL = ccbShopEvent.getErrURL();
        
        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("ACC_NO_temp", ACC_NO_temp.toString()));
        params.add(new BasicNameValuePair("ACC_NO", account));
        params.add(new BasicNameValuePair("LOGPASS", logpass));
        params.add(new BasicNameValuePair("TXCODE", TXCODE));
        params.add(new BasicNameValuePair("CCB_PWD_MAP_GIGEST", a+"|LOGPASS"));
        params.add(new BasicNameValuePair("COOKIES", COOKIES));
        params.add(new BasicNameValuePair("SYS_TYPE", SYS_TYPE));
        params.add(new BasicNameValuePair("BRANCHID", BRANCHID));
        params.add(new BasicNameValuePair("MERCHANTID", MERCHANTID));
        params.add(new BasicNameValuePair("SERIALNO", SERIALNO));
        params.add(new BasicNameValuePair("TIME", TIME));
        params.add(new BasicNameValuePair("T_TXCODE", T_TXCODE));
        params.add(new BasicNameValuePair("DATE", DATE));
        params.add(new BasicNameValuePair("errURL", errURL));

        try {
			httpPost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
		} catch (UnsupportedEncodingException e) {
			   logger.error("check account error {}", ccbShopEvent.getAccount(), e);
	            ccbShopEvent.setException(e);
	            return;
		}
        
        setHeader(url, httpPost, ccbShopEvent);
        httpAsyncClient.execute(httpPost, new HttpAsyncCallback(ccbShopEvent));	
	}
	
	private void xykgotourlUp(CCBShopEvent ccbShopEvent){
		try {
			logger.info("xykgotourlUp {}", ccbShopEvent.getAccount());
			String url = ccbShopEvent.getResulturl();
			String uuu = url.replaceAll("\\^", "%5E");
			//System.out.println(uuu);
			HttpGet httpGet = new HttpGet(uuu);
	        setHeader(uuu, httpGet, ccbShopEvent);
	        httpAsyncClient.execute(httpGet, new HttpAsyncCallback(ccbShopEvent));
		} catch (Exception e) {
			   logger.error("xykgotourlUp error {}", ccbShopEvent.getAccount(), e);
	            ccbShopEvent.setException(e);
	            return;
		}
	
	}
	
	
	private void xykgetjfUp(CCBShopEvent ccbShopEvent){
		logger.info("xykgetjf {}", ccbShopEvent.getAccount());
		String carno = ccbShopEvent.getAccount();
		StringBuilder sb = new StringBuilder();
		String a = carno.substring(0, 7);
		String b = carno.substring(12, 16);
		sb.append(a).append("*****").append(b);
		String url = String.format(xykgetjf,sb.toString());
		HttpGet httpGet = new HttpGet(url);
        setHeader(url, httpGet, ccbShopEvent);
        httpAsyncClient.execute(httpGet, new HttpAsyncCallback(ccbShopEvent));
	}
	
	
	private String getLogpass(String logpass,String[] b){
		
		
		String result = "";
		char[] newValue = logpass.toCharArray();
		Integer specialChar = 0;
		String everyone = "";
		String afterPass = "";

		for (int i=0;i<newValue.length;i++ ) {
			if (specialChar == 1) {
				break;
			}

			everyone = String.valueOf(newValue[i]);

			for (int j =0;j<((b.length)/2);j++) {
				if (everyone.equals(b[2*j])) {
					afterPass = afterPass + b[2*j+1];
					break;
				}

				if (j == (b.length)/2 - 1) {
					if (everyone != b[2*j]) {
						specialChar = 1;
						break;
					}
				}
		}
	}

	if (specialChar == 0) {					
		result = afterPass;
	} else {							
		String ret = "";
		afterPass = "";
		for(int i=0;i<newValue.length;i++) {
			String c = String.valueOf(newValue[i]);
			String ts = escape(c);
		//	String ts = "";
			if("%u".equals(ts.substring(0,2))) {
				ret = ret + ts.replace("%u","(^?)");
			} else {
				ret = ret + c;
			}
		}

		result = ret;
		
		char[] temt = ret.toCharArray();

		for (int n=0;n<temt.length;n++ ) {
			everyone = String.valueOf(temt[n]);
			for (int w =0;w<((b.length)/2);w++) {
				if (everyone == b[2*w]) {
					afterPass = afterPass + b[2*w+1];
					break;
				}
			}
		}
			result = afterPass;
		}
		//System.out.println(result);
		return result;
	}
	
	
	public String escape(String src) {
		  int i;
		  char j;
		  StringBuffer tmp = new StringBuffer();
		  tmp.ensureCapacity(src.length() * 6);
		  for (i = 0; i < src.length(); i++) {
		   j = src.charAt(i);
		   if (Character.isDigit(j) || Character.isLowerCase(j)
		     || Character.isUpperCase(j))
		    tmp.append(j);
		   else if (j < 256) {
		    tmp.append("%");
		    if (j < 16)
		     tmp.append("0");
		    tmp.append(Integer.toString(j, 16));
		   } else {
		    tmp.append("%u");
		    tmp.append(Integer.toString(j, 16));
		   }
		  }
		  return tmp.toString();
	 }
	
	private String getjsparam(String aa){
     	String[] bb = aa.replace("{", "").replace("}", "").replaceAll("\"", "").split(",");
    	StringBuilder ss = new StringBuilder();
    	for (String b : bb) {
    		String[] cc = b.split(":");
    		ss.append(cc[1]);
    	}
    	
		return ss.toString();
	}
	
	
	
	private void InitUp(CCBShopEvent ccbShopEvent){
		logger.info("check InitUp {}", ccbShopEvent.getUserid());
		String url = jflogin;
        HttpPost httpPost = new HttpPost(url);
        
        setHeader(url, httpPost, ccbShopEvent);
        httpAsyncClient.execute(httpPost, new HttpAsyncCallback(ccbShopEvent));
		
	}
	
	private void getSmsUp(CCBShopEvent ccbShopEvent) throws UnsupportedEncodingException{
		logger.info("getSmsUp {}", ccbShopEvent.getAccount());
		String url = getSms;
		
		String account = ccbShopEvent.getAccount();
		String passord = ccbShopEvent.getPassword();
		
        HttpPost httpPost = new HttpPost(url);
        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("accoumt", account));
        params.add(new BasicNameValuePair("mobilelastNum", passord));
        //params.add(new BasicNameValuePair("accessCode", "7J2P03F76YEROE04MG0ZAITG09E97QGO"));
        params.add(new BasicNameValuePair("state", "1"));

        httpPost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
        
        setHeader(url, httpPost, ccbShopEvent);
        httpPost.setHeader("Accept-Language", "zh-CN,zh;q=0.8");
        httpPost.setHeader("X-Requested-With", "XMLHttpRequest");
        httpPost.setHeader("Referer", "http://jf.ccb.com/exchangecenter/account/viewScoreResult.jhtml");
        httpPost.setHeader("Origin", "http://jf.ccb.com");
        httpPost.setHeader("Content-Type", "application/x-www-form-urlencoded");
        httpAsyncClient.execute(httpPost, new HttpAsyncCallback(ccbShopEvent));
	}
	
	private void getJFLoginUp(CCBShopEvent ccbShopEvent) throws UnsupportedEncodingException{
		logger.info("getJFLoginUp {}", ccbShopEvent.getAccount());
		String url = jflogin;
		
		String account = ccbShopEvent.getAccount();
		String passord = ccbShopEvent.getPassword();
		String smscode = ccbShopEvent.getSmsCode();
		
        HttpPost httpPost = new HttpPost(url);
        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("hidden_bankCard", account));
        params.add(new BasicNameValuePair("mobilelastNum", passord));
        params.add(new BasicNameValuePair("hidden_mcode", smscode));
        
        httpPost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
        setHeader(url, httpPost, ccbShopEvent);

 		httpPost.setHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
        httpPost.setHeader("Origin", "http://jf.ccb.com");
        httpPost.setHeader("Upgrade-Insecure-Requests", "1");
        httpPost.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 6.3; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/44.0.2403.157 Safari/537.36");
        httpPost.setHeader("Content-Type", "application/x-www-form-urlencoded");
        httpPost.setHeader("Referer", "http://jf.ccb.com/exchangecenter/account/viewScoreResult.jhtml");
        httpPost.setHeader("Accept-Encoding", "gzip, deflate");
        httpPost.setHeader("Accept-Language", "zh-CN,zh;q=0.8");
        
        httpAsyncClient.execute(httpPost, new HttpAsyncCallback(ccbShopEvent));
	}
	
	private void getJFQueryUp(CCBShopEvent ccbShopEvent){
		logger.info("getJFQueryUp {}", ccbShopEvent.getAccount());
		
		String aa = ccbShopEvent.getAccount();
		String[] bb = aa.split(" ");
		bb[2]="****";
		String account = "";
		for (int i = 0; i < bb.length; i++) {
			account += bb[i];
			if(i!=3){
				account+="%20";
			}
		}
		
		
		String url = String.format(getJFquery, new Date().getTime(),account);
		 
        HttpGet httpGet = new HttpGet(url);

        setHeader(url, httpGet, ccbShopEvent);
        httpAsyncClient.execute(httpGet, new HttpAsyncCallback(ccbShopEvent));
	}

	/**
	 * 发送支付短信验证码
	 * 	
	 * @Description: (方法职责详细描述,可空)  
	 * @Title: epayMsgCodeUp 
	 * @param ccbShopEvent
	 * @date 2016年1月30日 上午11:22:39  
	 * @author ws
	 * @throws UnsupportedEncodingException 
	*/
	private void epayMsgCodeUp(CCBShopEvent ccbShopEvent) throws UnsupportedEncodingException {
		logger.info("epayMsgCodeUp {}", ccbShopEvent.getAccount());
		String url = payMsgSendUrl;
		
		String mobile = ccbShopEvent.getCardMobile();
		
        HttpPost httpPost = new HttpPost(url);
        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("MOBILELAST4", mobile.substring(mobile.length()-4)));
        params.add(new BasicNameValuePair("TXCODE_PARAM", "EP5042"));
        params.add(new BasicNameValuePair("ACCT_NO", ccbShopEvent.getCardNumber()));
        params.add(new BasicNameValuePair("AMT", "6000"));

        httpPost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
        setHeader(url, httpPost, ccbShopEvent);
        httpPost.setHeader("Referer", ccbShopEvent.getPayParamsUrl());
        httpPost.setHeader("X-Requested-With", "XMLHttpRequest");
        httpPost.setHeader("Origin", "https://epay.ccb.com");
        httpPost.setHeader("Content-Type", "application/x-www-form-urlencoded");
        httpAsyncClient.execute(httpPost, new HttpAsyncCallback(ccbShopEvent));
		
	}


	/**
	 * 支付
	 * 	
	 * @Description: (方法职责详细描述,可空)  
	 * @Title: epayMainB1l1Up 
	 * @param ccbShopEvent
	 * @date 2016年1月29日 下午12:21:49  
	 * @author ws
	 * @throws UnsupportedEncodingException 
	*/
	private void epayMainB1l1Up(CCBShopEvent ccbShopEvent) throws UnsupportedEncodingException {
		
        
        logger.info("epayMainB1l1Up {}", ccbShopEvent.getAccount());
		String url = "https://epay.ccb.com"+ccbShopEvent.getPayUrl();
		
		Map<String,Object> orderMap = new HashMap<String, Object>();
		orderMap = ccbShopEvent.getOrderMap();

        HttpPost httpPost = new HttpPost(url);
        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("SKEY", String.valueOf(orderMap.get("SKEY"))));
        params.add(new BasicNameValuePair("USERID", String.valueOf(orderMap.get("USERID"))));
        params.add(new BasicNameValuePair("BRANCHID", String.valueOf(orderMap.get("BRANCHID"))));
        params.add(new BasicNameValuePair("ORDER_ID", String.valueOf(orderMap.get("ORDER_ID"))));
        params.add(new BasicNameValuePair("TXCODE", String.valueOf(orderMap.get("TXCODE"))));
        params.add(new BasicNameValuePair("ACCT_NO", ccbShopEvent.getCardNumber()));
        params.add(new BasicNameValuePair("VER_NO", ccbShopEvent.getSmsCode()));
        params.add(new BasicNameValuePair("MOBILE_CODE", ccbShopEvent.getCardMobile()));
        params.add(new BasicNameValuePair("errURL", String.valueOf(orderMap.get("errURL"))));
        
        httpPost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
        setHeader(url, httpPost, ccbShopEvent);
        httpPost.setHeader("Referer", ccbShopEvent.getPayParamsUrl().replaceAll(" ", "%20"));
        httpPost.setHeader("Upgrade-Insecure-Requests", "1");
        httpPost.setHeader("Origin", "https://epay.ccb.com");
        httpPost.setHeader("Cache-Control", "max-age=0");
        httpPost.setHeader("Content-Type", "application/x-www-form-urlencoded");
        httpAsyncClient.execute(httpPost, new HttpAsyncCallback(ccbShopEvent));
		
	}

	/**
	 * 加载支付界面
	 * 	
	 * @Description: (方法职责详细描述,可空)  
	 * @Title: epayMainPlatGateUp 
	 * @param ccbShopEvent
	 * @date 2016年1月29日 下午12:21:47  
	 * @author ws
	 * @throws UnsupportedEncodingException 
	*/
	private void epayMainPlatGateUp(CCBShopEvent ccbShopEvent) throws UnsupportedEncodingException {
		logger.info("epayMainPlatGateUp {}", ccbShopEvent.getAccount());
		String url = ccbShopEvent.getPayParamsUrl().replaceAll(" ", "%20");
		//String url = URLDecoder.decode(ccbShopEvent.getPayParamsUrl(),"UTF-8");
        HttpGet httpGet = new HttpGet(url);
        
        //System.out.println(url);
        setHeader(url, httpGet, ccbShopEvent);
        httpAsyncClient.execute(httpGet, new HttpAsyncCallback(ccbShopEvent));
		
		
	}

	/**
	 * 提交订单
	 * 	
	 * @Description: (方法职责详细描述,可空)  
	 * @Title: submitOrderUp 
	 * @param ccbShopEvent
	 * @date 2016年1月29日 下午12:21:45  
	 * @author ws
	 * @throws UnsupportedEncodingException 
	*/
	private void submitOrderUp(CCBShopEvent ccbShopEvent) throws UnsupportedEncodingException {
		logger.info("imgCodeUp {}", ccbShopEvent.getAccount());
		String url = submitOrderHtml;
		
		Map<String,Object> orderMap = new HashMap<String, Object>();
		orderMap = ccbShopEvent.getOrderMap();
		
        HttpPost httpPost = new HttpPost(url);
        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("orderType", String.valueOf(orderMap.get("orderType"))));
        params.add(new BasicNameValuePair("vo.t", String.valueOf(orderMap.get("voT"))));
        params.add(new BasicNameValuePair("vo.pid", String.valueOf(orderMap.get("voPid"))));
        params.add(new BasicNameValuePair("productId", ccbShopEvent.getProductId()));
        params.add(new BasicNameValuePair("inventoryId", String.valueOf(orderMap.get("inventoryId"))));
        params.add(new BasicNameValuePair("struts.token.name", String.valueOf(orderMap.get("strutsTokenName"))));
        params.add(new BasicNameValuePair("token", String.valueOf(orderMap.get("token"))));
        params.add(new BasicNameValuePair("formVerifyToken", String.valueOf(orderMap.get("formVerifyToken"))));
        params.add(new BasicNameValuePair("limitedFlag", String.valueOf(orderMap.get("limitedFlag"))));
        params.add(new BasicNameValuePair("vo.q", ccbShopEvent.getQuantity()));
        params.add(new BasicNameValuePair("productPrice", String.valueOf(ccbShopEvent.getProductPrice())));
        params.add(new BasicNameValuePair("vo.leftmsg", String.valueOf(ccbShopEvent.getLeftMessage())));
        if(ccbShopEvent.getOrderType().equals("1")){//实物
            params.add(new BasicNameValuePair("mvo.consigneeName", ccbShopEvent.getConsigneeName()));
            params.add(new BasicNameValuePair("mvo.consigneePhone", ccbShopEvent.getMobile()));
            params.add(new BasicNameValuePair("mvo.mobileSubmited", ccbShopEvent.getMobile()));
            params.add(new BasicNameValuePair("mvo.consigneeAddressSubmited", ccbShopEvent.getAddressDetail()));
            params.add(new BasicNameValuePair("mvo.consigneePostCode", ccbShopEvent.getPostCode()));
            params.add(new BasicNameValuePair("mvo.consigneeProvinceId", ccbShopEvent.getProvinceCode()));
            params.add(new BasicNameValuePair("mvo.consigneeCityId", ccbShopEvent.getCityCode()));
            params.add(new BasicNameValuePair("mvo.consigneeAreaId", ccbShopEvent.getDistinctCode()));
            params.add(new BasicNameValuePair("mvo.consigneeProvinceName", ccbShopEvent.getProvince()));
            params.add(new BasicNameValuePair("mvo.consigneeCityName", ccbShopEvent.getCity()));
            params.add(new BasicNameValuePair("mvo.consigneeAreaName", ccbShopEvent.getDistinct()));
            
        }else{//虚拟物品
        	params.add(new BasicNameValuePair("vvo.mobile", ccbShopEvent.getMobile()));
            params.add(new BasicNameValuePair("vvo.secmobile", ccbShopEvent.getMobile()));
            
        }
        
        httpPost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
        setHeader(url, httpPost, ccbShopEvent);
        httpPost.setHeader("Referer", "http://jf.ccb.com/exchangecenter/order/submitOrderdetail.jhtml?productId=1170028&orderType=2&quantity=1&shopId=undefined");
        httpPost.setHeader("Upgrade-Insecure-Requests", "1");
        httpPost.setHeader("Origin", "http://jf.ccb.com");
        httpPost.setHeader("Cache-Control", "max-age=0");
        httpPost.setHeader("Content-Type", "application/x-www-form-urlencoded");
        httpAsyncClient.execute(httpPost, new HttpAsyncCallback(ccbShopEvent));
		
	}

	/**
	 * 加载订单详情页
	 * 	
	 * @Description: (方法职责详细描述,可空)  
	 * @Title: submitOrderDetailUp 
	 * @param ccbShopEvent
	 * @date 2016年1月29日 下午12:21:42  
	 * @author ws
	*/
	private void submitOrderDetailUp(CCBShopEvent ccbShopEvent) {
		logger.info("imgCodeUp {}", ccbShopEvent.getAccount());
		String productId = ccbShopEvent.getProductId();
		String orderType = String.valueOf(ccbShopEvent.getOrderType());
		String quantity = ccbShopEvent.getQuantity();
		String shopId = ccbShopEvent.getShopId();
		String url = String.format(submitOrderDetailHtml,productId,orderType,quantity,shopId);
        HttpGet httpGet = new HttpGet(url);

        setHeader(url, httpGet, ccbShopEvent);
        httpAsyncClient.execute(httpGet, new HttpAsyncCallback(ccbShopEvent));
		
	}

	/**
	 * 验证图片验证码
	 * 	
	 * @Description: (方法职责详细描述,可空)  
	 * @Title: imgCodeCheckUp 
	 * @param ccbShopEvent
	 * @date 2016年1月28日 下午7:52:56  
	 * @author ws
	 * @throws UnsupportedEncodingException 
	*/
	private void imgCodeCheckUp(CCBShopEvent ccbShopEvent) throws UnsupportedEncodingException {
		logger.info("imgCodeUp {}", ccbShopEvent.getAccount());
		String url = checkImgCodeHtml;
        HttpPost httpPost = new HttpPost(url);
        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("randomCode", ccbShopEvent.getvCode()));//图片验证码
        params.add(new BasicNameValuePair("type", "0"));
        httpPost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
        setHeader(url, httpPost, ccbShopEvent);
        httpAsyncClient.execute(httpPost, new HttpAsyncCallback(ccbShopEvent));
		
	}

	/**
	 * 登录
	 * 	
	 * @Description: (方法职责详细描述,可空)  
	 * @Title: login 
	 * @param ccbShopEvent
	 * @date 2016年1月28日 下午7:46:04  
	 * @author ws
	 * @throws Exception 
	*/
	private void loginUp(CCBShopEvent ccbShopEvent) throws Exception {
		logger.info("imgCodeUp {}", ccbShopEvent.getAccount());
		String url = loginHtml;
        HttpPost httpPost = new HttpPost(url);
        
        //type=0&userBase.userAccount=kcheng&userBase.password=e10adc3949ba59abbe56e057f20f883e&randomCode=94t6g&remember=1
        String passwordEnc = ScriptHelper.encryptCCBPasword(ccbShopEvent.getPassword());
    	//System.out.println(passwordEnc);
        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("type", "0"));
        params.add(new BasicNameValuePair("userBase.userAccount", ccbShopEvent.getAccount()));
        params.add(new BasicNameValuePair("userBase.password", passwordEnc));
        params.add(new BasicNameValuePair("randomCode", ccbShopEvent.getvCode()));
        params.add(new BasicNameValuePair("remember", "1"));//1记住，0不记住
        httpPost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
        
        
        setHeader(url, httpPost, ccbShopEvent);
        httpAsyncClient.execute(httpPost, new HttpAsyncCallback(ccbShopEvent));
		
	}

	/**
	 * 获取图片验证码
	 * 	
	 * @Description: (方法职责详细描述,可空)  
	 * @Title: imgCodeUp 
	 * @param ccbShopEvent
	 * @date 2016年1月28日 下午7:45:50  
	 * @author ws
	 * @throws UnsupportedEncodingException 
	*/
	private void imgCodeGetUp(CCBShopEvent ccbShopEvent) throws UnsupportedEncodingException {
		logger.info("imgCodeUp {}", ccbShopEvent.getAccount());
		String url = String.format(getImgCodeHtml,new Date().getTime());
        HttpGet httpGet = new HttpGet(url);

        setHeader(url, httpGet, ccbShopEvent);
        httpAsyncClient.execute(httpGet, new HttpAsyncCallback(ccbShopEvent));
		
		
	}

	/**
	 * 加载登录页面
	 * 	
	 * @Description: (方法职责详细描述,可空)  
	 * @Title: loginPageUp 
	 * @param ccbShopEvent
	 * @date 2016年1月28日 下午7:45:35  
	 * @author ws
	*/
	private void loginPageUp(CCBShopEvent ccbShopEvent) {
		logger.info("loginPageUp {}", ccbShopEvent.getAccount());
		String url = loginPageHtml;
        HttpPost httpPost = new HttpPost(url);
        
        setHeader(url, httpPost, ccbShopEvent);
        httpAsyncClient.execute(httpPost, new HttpAsyncCallback(ccbShopEvent));
		
	}

	private void setHeader(String uriStr, HttpMessage httpMessage, QueryEvent event) {
        httpMessage.setHeader("Accept", "*/*");
        httpMessage.setHeader("User-Agent", userAgent);
        try {
            CookieHelper.setCookies2(uriStr, httpMessage, event.getCookieList());
        } catch (URISyntaxException e) {
            logger.error("set cookie fail {}", e);
        }
    }

	/**
	 * 	注册页面加载
	 * @Description: (方法职责详细描述,可空)  
	 * @Title: registerPageUp 
	 * @param ccbShopEvent
	 * @return
	 * @date 2016年1月28日 下午6:38:33  
	 * @author ws
	*/
	private void registerPageUp(CCBShopEvent ccbShopEvent) {
		logger.info("check account up {}", ccbShopEvent.getUserid());
		String url = new StringBuffer(ctx).append(registerHtml).append(webType).toString();
        HttpPost httpPost = new HttpPost(url);
        
        setHeader(url, httpPost, ccbShopEvent);
        httpAsyncClient.execute(httpPost, new HttpAsyncCallback(ccbShopEvent));
	}

	/**
	 * @Description: (验证用户名是否存在)  
	 * @Title: checkAccountUp 
	 * @param ccbShopEvent
	 * @date 2016年1月28日 下午2:51:26  
	 * @author Hongbo Peng
	 */
	private void checkAccountUp(CCBShopEvent ccbShopEvent) {
		logger.info("check account up {}", ccbShopEvent.getUserid());
		String url = new StringBuffer(ctx).append(checkAccount).append(webType).toString();
        HttpPost httpPost = new HttpPost(url);
        try {
            List<NameValuePair> params = new ArrayList<>();
            params.add(new BasicNameValuePair("account", ccbShopEvent.getAccount()));
            httpPost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
        } catch (Exception e) {
            logger.error("check account error {}", ccbShopEvent.getAccount(), e);
            ccbShopEvent.setException(e);
            return;
        }
        setHeader(url, httpPost, ccbShopEvent);
        httpAsyncClient.execute(httpPost, new HttpAsyncCallback(ccbShopEvent));
	}

	/**
	 * @Description: (验证手机号码是否存在)  
	 * @Title: checkExistsMobile 
	 * @param ccbShopEvent
	 * @date 2016年1月28日 下午2:51:59  
	 * @author Hongbo Peng
	 */
	private void checkExistsMobileUp(CCBShopEvent ccbShopEvent){
		logger.info("check exitst mobile up {}",ccbShopEvent.getUserid());
		String url = new StringBuffer(ctx).append(ajaxExistMobile).append(webType).toString();
        HttpPost httpPost = new HttpPost(url);
        try {
            List<NameValuePair> params = new ArrayList<>();
            params.add(new BasicNameValuePair("phone_num", ccbShopEvent.getMobile()));
            params.add(new BasicNameValuePair("account", ccbShopEvent.getAccount()));
            params.add(new BasicNameValuePair("passwordEnc", ccbShopEvent.getPasswordEnc()));
            params.add(new BasicNameValuePair("registerToken", ccbShopEvent.getRegisterToken()));
            httpPost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
        } catch (Exception e) {
            logger.error("check exists mobile error {}", ccbShopEvent.getMobile(), e);
            ccbShopEvent.setException(e);
            return;
        }
        setHeader(url, httpPost, ccbShopEvent);
        httpAsyncClient.execute(httpPost, new HttpAsyncCallback(ccbShopEvent));
	}
	/**
	 * @Description: (发送注册短信验证码)  
	 * @Title: sendSmsCodeForRegister 
	 * @param ccbShopEvent
	 * @date 2016年1月28日 下午2:53:17  
	 * @author Hongbo Peng
	 */
	private void sendSmsCodeForRegisterUp(CCBShopEvent ccbShopEvent){
		logger.info("send Sms Code For Register up {}",ccbShopEvent.getUserid());
		String url = new StringBuffer(ctx).append(sendSmsCodeForRegister).append(webType).toString();
        HttpPost httpPost = new HttpPost(url);
        try {
            List<NameValuePair> params = new ArrayList<>();
            params.add(new BasicNameValuePair("phone_num", ccbShopEvent.getMobile()));
            params.add(new BasicNameValuePair("account", ccbShopEvent.getAccount()));
            params.add(new BasicNameValuePair("passwordEnc", ccbShopEvent.getPasswordEnc()));
            params.add(new BasicNameValuePair("registerToken", ccbShopEvent.getRegisterToken()));
            httpPost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
        } catch (Exception e) {
            logger.error("send Sms Code For Register error {}", ccbShopEvent.getMobile(), e);
            ccbShopEvent.setException(e);
            return;
        }
        setHeader(url, httpPost, ccbShopEvent);
        httpAsyncClient.execute(httpPost, new HttpAsyncCallback(ccbShopEvent));
	}
	
	/**
	 * @Description: (校验短信验证码)  
	 * @Title: checkSmsCodeForAjaxUp 
	 * @param ccbShopEvent
	 * @date 2016年2月1日 下午4:39:32  
	 * @author ws
	 */
	private void checkSmsCodeForAjaxUp(CCBShopEvent ccbShopEvent) {
		logger.info("check sms code for ajax up {}",ccbShopEvent.getUserid());
		String url = new StringBuffer(ctx).append(checkSmsCodeForAjax).append(webType).toString();
        HttpPost httpPost = new HttpPost(url);
        try {
            List<NameValuePair> params = new ArrayList<>();
            params.add(new BasicNameValuePair("smsCode", ccbShopEvent.getSmsCode()));
            httpPost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
        } catch (Exception e) {
            logger.error("send Sms Code For Register error {}", ccbShopEvent.getMobile(), e);
            ccbShopEvent.setException(e);
            return;
        }
        setHeader(url, httpPost, ccbShopEvent);
        httpAsyncClient.execute(httpPost, new HttpAsyncCallback(ccbShopEvent));
	}
	/**	
	 * @Description: (注册提交)  
	 * @Title: registerSubmitUp 
	 * @param ccbShopEvent
	 * @date 2016年2月1日 下午4:40:03  
	 * @author ws
	 */
	private void registerSubmitUp(CCBShopEvent ccbShopEvent) {
		logger.info("register up {}",ccbShopEvent.getUserid());
		String url = new StringBuffer(ctx).append(registersubmit).append(webType).toString();
        HttpPost httpPost = new HttpPost(url);
        try {
            List<NameValuePair> params = new ArrayList<>();
            params.add(new BasicNameValuePair("account", ccbShopEvent.getAccount()));
            params.add(new BasicNameValuePair("passwordEnc", ccbShopEvent.getPasswordEnc()));
            params.add(new BasicNameValuePair("phone_num", ccbShopEvent.getMobile()));
            params.add(new BasicNameValuePair("smsCode", ccbShopEvent.getSmsCode()));
            params.add(new BasicNameValuePair("registerToken", ccbShopEvent.getRegisterToken()));
            httpPost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
        } catch (Exception e) {
            logger.error("register error {}", ccbShopEvent.getMobile(), e);
            ccbShopEvent.setException(e);
            return;
        }
        setHeader(url, httpPost, ccbShopEvent);
        httpAsyncClient.execute(httpPost, new HttpAsyncCallback(ccbShopEvent));
	}
	/**
	 * 获取省份地址Json
	 * 	
	 * @Description: (方法职责详细描述,可空)  
	 * @Title: getProvinceGBAddressJSonUp 
	 * @param ccbShopEvent
	 * @date 2016年2月1日 下午4:40:23  
	 * @author ws
	 */
	private void getProvinceGBAddressJSonUp(CCBShopEvent ccbShopEvent) {
		logger.info("get Province GB Address JSon up {}",ccbShopEvent.getUserid());
		String url = new StringBuffer(ctx).append(provinceGBAddressJSon).append(webType).toString();
        HttpPost httpPost = new HttpPost(url);
        setHeader(url, httpPost, ccbShopEvent);
        httpAsyncClient.execute(httpPost, new HttpAsyncCallback(ccbShopEvent));
	}
	/**
	 * 获取城市地址Json
	 * 	
	 * @Description: (方法职责详细描述,可空)  
	 * @Title: getCityGBAddressJSonUp 
	 * @param ccbShopEvent
	 * @date 2016年2月1日 下午4:40:50  
	 * @author ws
	 */
	private void getCityGBAddressJSonUp(CCBShopEvent ccbShopEvent) {
		logger.info("city GB Address JSon up {}",ccbShopEvent.getUserid());
		String url = new StringBuffer(ctx).append(cityGBAddressJSon).append(webType).toString();
        HttpPost httpPost = new HttpPost(url);
        try {
            List<NameValuePair> params = new ArrayList<>();
            params.add(new BasicNameValuePair("provinceCode", ccbShopEvent.getProvinceCode()));
            httpPost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
        } catch (Exception e) {
            logger.error("getCityGBAddressJSonUp error {}", ccbShopEvent.getMobile(), e);
            ccbShopEvent.setException(e);
            return;
        }
        setHeader(url, httpPost, ccbShopEvent);
        httpAsyncClient.execute(httpPost, new HttpAsyncCallback(ccbShopEvent));
	}
	/**
	 * 获取区域地址Json
	 * 	
	 * @Description: (方法职责详细描述,可空)  
	 * @Title: getDistinctGBAddressJSonUp 
	 * @param ccbShopEvent
	 * @date 2016年2月1日 下午4:41:06  
	 * @author ws
	 */
	private void getDistinctGBAddressJSonUp(CCBShopEvent ccbShopEvent) {
		logger.info("get Distinct GB Address JSon up {}",ccbShopEvent.getUserid());
		String url = new StringBuffer(ctx).append(distinctGBAddressJSon).append(webType).toString();
        HttpPost httpPost = new HttpPost(url);
        try {
            List<NameValuePair> params = new ArrayList<>();
            params.add(new BasicNameValuePair("cityCode", ccbShopEvent.getCityCode()));
            httpPost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
        } catch (Exception e) {
            logger.error("getDistinctGBAddressJSon error {}", ccbShopEvent.getMobile(), e);
            ccbShopEvent.setException(e);
            return;
        }
        setHeader(url, httpPost, ccbShopEvent);
        httpAsyncClient.execute(httpPost, new HttpAsyncCallback(ccbShopEvent));
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

	private class HttpAsyncCallback implements FutureCallback<HttpResponse> {
		private CCBShopEvent event;
        private boolean skipNextStep = false;

		public HttpAsyncCallback(CCBShopEvent event) {
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
				case REGISTER_PAGE:
					registerPageDown(response);
					break;
				case CHECK_ACCOUNT:
					checkAccountDown(response);
					break;
				case CHECK_EXISTS_MOBILE:
					checkExistsMobileDown(response);
					break;
				case REGISTER_VERIFY_CODE:
					sendSmsCodeForRegisterDown(response);
					break;
				case CHECK_SMS_CODE:
					checkSmsCodeForAjaxDown(response);
					break;
				case REGISTER:
					registerSubmitDown(response);
					break;
				case LOGIN_PAGE://登录页面 
					loginPageDown(response);
					break;
				case IMG_CODE_GET://获取验证码
					imgCodeGetDown(response);
					break;
				case IMG_CODE_CHECK://验证验证码
					imgCodeCheckDown(response);
					break;
				case LOGIN://登录
					loginDown(response);
					break;
				case SUBMIT_ORDER_DETAIL://订单详情页
					submitOrderDetailDown(response);
					break;
				case SUBMIT_ORDER://生成订单
					submitOrderDown(response);
					break;
				case EPAY_MAIN_PLATGATE://支付页面
					eapyMainPlatGateDown(response);
					break;
				case EPAY_MSG_CODE://支付短信验证码发送
					epayMsgCodeDown(response);
					break;
				case EPAY_MAIN_B1L1://支付
					epayMainB1l1Down(response);
					break;
				case GET_PROVINCE_GB_ADDRESS_JSON:
					getProvinceGBAddressJSonDown(response);
					break;
				case GET_CITY_GB_ADDRESS_JSON:
					getCityGBAddressJSonDown(response);
					break;
				case GET_DISTINCT_GB_ADDRESS_JSON:
					getDistinctGBAddressJSonDown(response);
					break;
					
				case JF_INIT:
					InitDown(response);
					break;
				case JF_SMS_SEND:
					getSmsDown(response);
					break;
				case JF_LOGIN:
					getJFLoginDown(response);
					break;		
				case JF_QUERY:
					getJFQueryDown(response);
					break;			
					
				case XYK_INIT:
					xykInitDown(response);
					break;	
				case XYK_GETKEY:
					xykGetkeyDown(response);
					break;
				case XYK_IMG:
					xykImgDown(response);
					break;
				case XYK_KEY:
					xykKeyDown(response);
					break;
					
					
				case XYK_LOGIN:
					xykloginDown(response);
					break;
				case XYK_GOTOURL:
					xykgotourlDown(response);
					break;
				case XYK_GETJF:
					xykgetDown(response);
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
		 * @Title: xykKeyDown 
		 * @param response
		 * @date 2016年5月31日 下午4:04:04  
		 * @author ws
		 * @throws IOException 
		 * @throws ParseException 
		*/
		private void xykKeyDown(HttpResponse response){
			try {
				HttpEntity entity = response.getEntity();
	            String entityStr = EntityUtils.toString(entity);
				
	    		int s = entityStr.indexOf("var ifUseYinshe");
				int e = entityStr.indexOf("var jiami = 0;");
				
				String script = entityStr.substring(s, e);
	        	//获得JS脚本引擎
	        	ScriptEngineManager manager = new ScriptEngineManager();
	        	ScriptEngine engine = manager.getEngineByExtension("js");
	        	//设置JS脚本中的userArray、date变量
	        	
	        	engine.eval(script);//执行JS脚本
	        	String aa =  JSONObject.toJSONString(engine.get("a")); 
	        	String bb =  JSONObject.toJSONString(engine.get("b")); 
	        	
	        	event.setGigest(aa);
	        	event.setLogpass(bb);
	        	
	        	Map<String, String> paramMap = getPageParam(entityStr,"input");
				if(null == paramMap){
	                event.setException(new SpiderException(1006, "建设银行系统维护中"));
				}
				
              	event.setCOOKIES(paramMap.get("COOKIES"));
              	event.setMERCHANTID(paramMap.get("MERCHANTID"));
              	event.setBRANCHID(paramMap.get("BRANCHID"));
              	event.setSERIALNO(paramMap.get("SERIALNO"));
              	event.setTXCODE(paramMap.get("TXCODE"));
              	event.setSYS_TYPE(paramMap.get("SYS_TYPE"));
              	event.setDATE(paramMap.get("DATE"));
              	event.setTIME(paramMap.get("TIME"));
              	event.setT_TXCODE(paramMap.get("T_TXCODE"));
              	event.setErrURL(paramMap.get("errURL"));
	        	
	        	event.setState(CCBShopState.XYK_LOGIN);
			} catch (Exception e) {
				logger.error("xykKeyDown exception {} ",e);
				event.setException(e);
			}
		}

		private void xykInitDown(HttpResponse response){
			try {
				HttpEntity entity = response.getEntity();
                String entityStr = EntityUtils.toString(entity);
				//System.out.println(entityStr);
				
		        String result = getKeyUrl(entityStr);
		        
		        event.setGetkeyurl(result);
		        
				event.setState(CCBShopState.XYK_GETKEY);
			} catch (Exception e) {
				logger.error("xykInitDown exception {} ",e);
				event.setException(e);
			}
		}

		private String getKeyUrl(String entityStr) {
			int s = entityStr.indexOf("action=\"");
			entityStr = entityStr.substring(s+8);
			//System.out.println(entityStr);
			int e = entityStr.indexOf("\"");
			String result = entityStr.substring(0, e);
			//System.out.println(result);
			return result;
		}
		
		
		private void xykGetkeyDown(HttpResponse response){
			try {
				HttpEntity entity = response.getEntity();
                String entityStr = EntityUtils.toString(entity);
				Map<String, String> paramMap = getPageParam(entityStr,"input");
              	
				if(null == paramMap){

	                event.setException(new SpiderException(1006, "建设银行系统维护中"));
				}
				
              	event.setCOOKIES(paramMap.get("COOKIES"));
              	event.setMERCHANTID(paramMap.get("MERCHANTID"));
              	event.setBRANCHID(paramMap.get("BRANCHID"));
              	event.setSERIALNO(paramMap.get("SERIALNO"));
              	event.setTXCODE(paramMap.get("TXCODE"));
              	event.setSYS_TYPE(paramMap.get("SYS_TYPE"));
              	event.setDATE(paramMap.get("DATE"));
              	event.setTIME(paramMap.get("TIME"));
              	event.setT_TXCODE(paramMap.get("T_TXCODE"));
              	//event.setErrURL(paramMap.get("resType"));
              	
				event.setState(CCBShopState.XYK_KEY);
			} catch (Exception e) {
				logger.error("xykGetkeyDown exception {} ",e);
				event.setException(e);
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
        private Map<String, String> getPageParam(String loginPage,String tagName){
        	logger.info("getLoginParam {}", event.getId());

        	Map<String, String> params = new HashMap<String, String>();
            try {
            	loginPage = XpathHtmlUtils.cleanHtml(loginPage);
            	org.jsoup.nodes.Document doc = Jsoup.parse(loginPage);
            	org.jsoup.select.Elements elements = doc.getElementsByTag(tagName);
            	for (org.jsoup.nodes.Element element : elements) {
            		String type = element.attr("type");
                    String name = element.attr("name");
                    if ("hidden".equals(type) && StringUtils.isNotBlank(name)) {
                        params.put(name, element.attr("value"));
                    }
				}
                return params;
            } catch (Exception e) {
				logger.warn("获取登录信息失败", e);
                throw e;
            }
        }

		
		private String getnotebystring(String result,String node){
			int s = result.indexOf(node);
			int e = result.indexOf("\">");
			result = result.substring(s, e);
			s = result.indexOf("value=\"");
			result = result.substring(s+7);
			//System.out.println(result);
			return result;
		}
		
		private String getnoteUN(String result,String node){
			int s = result.indexOf(node);
			int e = result.indexOf("\">");
			result = result.substring(s, e);
			s = result.indexOf("VALUE=\"");
			result = result.substring(s+7);
			//System.out.println(result);
			return result;
		}
		
		private void xykImgDown(HttpResponse response){
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
                if (appConfig.inDevMode()) {
                    byte[] tbytes = Base64.getDecoder().decode(imgStr);
                    FileOutputStream fs = new FileOutputStream(
                            appConfig.getUploadPath() + "/" + event.getUserid()
                                    + ".jpg");
                    fs.write(tbytes);
                    fs.close();
                }
                String key = String.format(Constant.CCB_JF_XYK_KEY, event.getUserid());
                redis.set(key, JSON.toJSONString(event), 600); //600秒超时
                event.setException(new SpiderException(1001, "输入验证码", imgStr));
            } catch (Exception e) {
                logger.error("xykImgDown exception {}", e);
                event.setException(new SpiderException(1006,"获取验证码失败"));
            }
		}
		

		
		private void xykloginDown(HttpResponse response){
			try {
				HttpEntity entity = response.getEntity();
				String entityStr = EntityUtils.toString(entity);
				
				
		        int s = entityStr.indexOf("window.location='");
		        if(s == -1){
					event.setException(new SpiderException(1006, "服务器繁忙"));
		        }else{
		            int e = entityStr.indexOf("';");
	                
			        String result = entityStr.substring(s+17, e);
					
			        event.setResulturl(result);
					
					event.setState(CCBShopState.XYK_GOTOURL);	
		        }
		 
				
			} catch (Exception e) {
				logger.error("xykloginDown exception {} ",e);
				event.setException(e);
			}
		}
		
		private void xykgotourlDown(HttpResponse response){
			try {
				HttpEntity entity = response.getEntity();
				String entityString = EntityUtils.toString(entity);
                
				Map<String, String> map = getJfInfo(entityString);
				
				event.setException(new SpiderException(0, "获取积分成功",JSON.toJSONString(map)));
                	
			} catch (Exception e) {
				logger.error("xykgotourlDown exception {} ",e);
				event.setException(e);
			}
		}

		private Map<String, String> getJfInfo(String entityString)
				throws Exception {
			String fileDir = XpathHtmlUtils.deleteHeadHtml(entityString);
			Document document = XpathHtmlUtils.getCleanHtml(fileDir);
			XPath xpath = XPathFactory.newInstance().newXPath();
			
			String exp_card = "/html/body/div/div/h3/span[1]";
			String card = XpathHtmlUtils.getNodeText(exp_card, xpath, document);
			card = card.substring(3);
			//System.out.println(card);
			String exp_jf = "/html/body/div/div/div/ul/li[7]/dl/dd";
			String jf = XpathHtmlUtils.getNodeText(exp_jf, xpath, document);
			jf = jf.substring(6);
			//System.out.println(jf);
			
			Map<String,String> map = new HashMap<String, String>();
			map.put("jf", jf);
			map.put("name", card);
			return map;
		}
		
		private void xykgetDown(HttpResponse response){
			try {
				HttpEntity entity = response.getEntity();
				String result = EntityUtils.toString(entity);
				
				int errcode = result.indexOf("ErrCode");
				if(errcode == -1){
					String[] jf = result.trim().split("\\|");
					
					String username = "";
					if(!StringUtils.isBlank(event.getUserName())){
						username = event.getUserName();
					}
					
					
					Map<String,String> map = new HashMap<String, String>();
					map.put("jf", jf[3]);
					map.put("name", username);
					
					event.setException(new SpiderException(0, "获取积分成功",JSON.toJSONString(map)));
				}else{
					event.setException(new SpiderException(1006, "服务器繁忙"));
				}
				
	
				
			} catch (Exception e) {
				logger.error("xykgetDown exception {} ",e);
				event.setException(e);
			}
		}
		
		
		private void InitDown(HttpResponse response){
			event.setState(CCBShopState.JF_SMS_SEND);
		}

		private void getSmsDown(HttpResponse response){
			try {
				HttpEntity entity = response.getEntity();
                String entityStr = EntityUtils.toString(entity);
                
                JSONObject res = JSON.parseObject(entityStr);
                if("0".equals(res.get("code"))){

                    String key = String.format(Constant.CCB_JF_SMS_KEY, event.getUserid());
                    redis.set(key, JSON.toJSONString(event), 600); //600秒超时
                    
                	event.setException(new SpiderException(0, "请接收短信验证码"));
                }else{
                    event.setException(new SpiderException(1001, res.getString("message")));
                }
			} catch (Exception e) {
				logger.error("getSmsDown exception {} ",e);
				event.setException(e);
			}
		}
		
		private void getJFLoginDown(HttpResponse response){
			try {
				HttpEntity entity = response.getEntity();
                
				String fileDir = XpathHtmlUtils.deleteHeadHtml(EntityUtils.toString(entity));
				Document document = XpathHtmlUtils.getCleanHtml(fileDir);
				XPath xpath = XPathFactory.newInstance().newXPath();
				
                String exp_card = "/html/body/div[2]/div/div/div[1]/div[2]/ul/li[7]/span[2]";
                String card = XpathHtmlUtils.getNodeText(exp_card, xpath, document);
                if(card.contains("验证码不匹配")){
                	 event.setException(new SpiderException(1004, "手机短信验证码不匹配，请重新输入。"));
                }else if(card.contains("验证码已超过有效期")){
                	event.setException(new SpiderException(1005, "很抱歉，验证码已超过有效期"));
                }else{
                	String resultJF = "/html/body/div[2]/div/div/div[1]/div[2]/div/div/span[3]";
                	String jfString = XpathHtmlUtils.getNodeText(resultJF, xpath, document);
                	if(jfString.contains("剩余积分")){
                		 event.setState(CCBShopState.JF_QUERY);
                	}else{
                		event.setException(new SpiderException(1006, "登录失败"));
                	}
                }
			} catch (Exception e) {
				logger.error("getJFLoginDown exception {} ",e);
				event.setException(e);
			}
		}
		
		private void getJFQueryDown(HttpResponse response){
			try {
				HttpEntity entity = response.getEntity();
                String entityStr = EntityUtils.toString(entity);
                
                JSONObject res = JSON.parseObject(entityStr);
                
                if("true".equals(res.getString("msg"))){

                	String score = res.getString("score");
                	
                	event.setException(new SpiderException(0, "success",score));
                }else{
                    event.setException(new SpiderException(1007, res.getString("message")));
                }
			} catch (Exception e) {
				logger.error("getJFQueryDown exception {} ",e);
				event.setException(e);
			}
		}
		
		
		/**
		 * 	发送短信验证码
		 * @Description: (方法职责详细描述,可空)  
		 * @Title: epayMsgCodeDown 
		 * @param response
		 * @date 2016年1月30日 上午11:23:21  
		 * @author ws
		*/
		private void epayMsgCodeDown(HttpResponse response) {
			try {
				HttpEntity entity = response.getEntity();
                String entityStr = EntityUtils.toString(entity);
                
                JSONObject res = JSON.parseObject(entityStr);
                if(null != res && null != res.getString("status") && "true".equals(res.get("status"))){

                    String key = String.format(Constant.CCB_SHOP_BUY_KEY, event.getUserid());
                    redis.set(key, JSON.toJSONString(event), 600); //600秒超时
                    
                	event.setException(new SpiderException(0, "请接收短信验证码"));
                }else{
                	if(null != res && null != res.getString("msg")){

                    	event.setException(new SpiderException(1306, res.getString("msg")));
                	}else{
                		
                		event.setException(new SpiderException(1068, "系统繁忙，请稍后重试"));
                	}
                }
			} catch (Exception e) {
				logger.error("epayMsgCodeDown exception {} ",e);
				event.setException(e);
			}
		}

		/**
		 * 	支付
		 * @Description: (方法职责详细描述,可空)  
		 * @Title: epayMainB1l1Down 
		 * @param response
		 * @date 2016年1月29日 下午12:23:43  
		 * @author ws
		*/
		private void epayMainB1l1Down(HttpResponse response) {
			try {
				HttpEntity entity = response.getEntity();
                String entityStr = EntityUtils.toString(entity);
                //System.out.println(entityStr);
                //恭喜您！积分支付成功！您已成功支付
                //         /html/body/div/div[2]/div[2]/div[1]   
                //  输入的卡号的当前可用积分小于要兑换的分数！
                //  验证码输入错误,请重新输入。
                if(entityStr.contains("您已成功支付") || entityStr.contains("积分支付成功")){
                	String orderNo = "";
                	Map<String,Object> orderMap = event.getOrderMap();
                	if(null != orderMap){
                		orderNo = (String)orderMap.get("ORDER_ID");
                	}
                    event.setException(new SpiderException(0, "支付成功" , orderNo));
                }else{
                	if(entityStr.contains("可用积分小于")){
                		event.setException(new SpiderException(1307, "输入的卡号的当前可用积分小于要兑换的分数！"));
                	}else if(entityStr.contains("验证码输入错误")){
                		event.setException(new SpiderException(1090, "短信验证码错误"));
                	}else{
                		event.setException(new SpiderException(1300, "支付失败"));
                	}
                }
			} catch (Exception e) {
				logger.error("epayMainB1l1Down exception {} ",e);
				event.setException(e);
			}
		}

		/**
		 * 	加载支付页面
		 * @Description: (方法职责详细描述,可空)  
		 * @Title: eapyMainPlatGateDown 
		 * @param response
		 * @date 2016年1月29日 下午12:23:41  
		 * @author ws
		*/
		private void eapyMainPlatGateDown(HttpResponse response) {
			try {
				HttpEntity entity = response.getEntity();
                String entityStr = EntityUtils.toString(entity);
                
                String fileDir = XpathHtmlUtils.deleteHeadHtml(entityStr);
                Document document = XpathHtmlUtils.getCleanHtml(fileDir);
                XPath xpath = XPathFactory.newInstance().newXPath();
                
                Map<String,Object> payMap = new HashMap<String, Object>();
                
                //*[@id="jhform"]/input[1]
                String SKEYXp = "//*[@id='jhform']/input[@name='SKEY']";
                String SKEY = XpathHtmlUtils.getNodeValue(SKEYXp, xpath, document);
                
                String USERIDXp = "//*[@id='jhform']/input[@name='USERID']";
                String USERID = XpathHtmlUtils.getNodeValue(USERIDXp, xpath, document);

                String BRANCHIDXp = "//*[@id='jhform']/input[@name='BRANCHID']";
                String BRANCHID = XpathHtmlUtils.getNodeValue(BRANCHIDXp, xpath, document);

                String ORDER_IDXp = "//*[@id='jhform']/input[@name='ORDER_ID']";
                String ORDER_ID = XpathHtmlUtils.getNodeValue(ORDER_IDXp, xpath, document);

                String TXCODEXp = "//*[@id='jhform']/input[@name='TXCODE']";
                String TXCODE = XpathHtmlUtils.getNodeValue(TXCODEXp, xpath, document);

                String errURLXp = "//*[@id='jhform']/input[@name='errURL']";
                String errURL = XpathHtmlUtils.getNodeValue(errURLXp, xpath, document);
                
                
                NodeList nodeList = document.getElementsByTagName("form");
                if(null == nodeList || nodeList.getLength() < 1){
                	event.setException(new SpiderException(1300, "支付失败"));
                	return;
                }
                
                Node node = nodeList.item(0);
                Element e = (Element) node;
                String payUrl = e.getAttribute("action");
                
                payMap.put("SKEY", SKEY);
                payMap.put("USERID", USERID);
                payMap.put("BRANCHID", BRANCHID);
                payMap.put("ORDER_ID", ORDER_ID);
                payMap.put("TXCODE", TXCODE);
                payMap.put("errURL", errURL);
                event.setPayUrl(payUrl);
                event.setOrderMap(payMap);
                event.setState(CCBShopState.EPAY_MSG_CODE);
                
			} catch (Exception e) {
				logger.error("eapyMainPlatGateDown exception {} ",e);
				event.setException(e);
			}
			
		}

		/**
		 * 	提交订单
		 * @Description: (方法职责详细描述,可空)  
		 * @Title: submitOrderDown 
		 * @param response
		 * @date 2016年1月29日 下午12:23:39  
		 * @author ws
		*/
		private void submitOrderDown(HttpResponse response) {
			try {
				HttpEntity entity = response.getEntity();
                String entityStr = EntityUtils.toString(entity);
                
                String fileDir = XpathHtmlUtils.deleteHeadHtml(entityStr);
                Document document = XpathHtmlUtils.getCleanHtml(fileDir);
                
                //System.out.println(fileDir);
                
                String payParamsUrl = "";
                NodeList nodeList = document.getElementsByTagName("iframe");
                if(null == nodeList || nodeList.getLength() < 1){
                	event.setException(new SpiderException(1301, "订单生成失败"));
                	return;
                }
                
                Node node = nodeList.item(0);
                Element e = (Element) node;
                payParamsUrl = e.getAttribute("src");
                
                event.setPayParamsUrl(payParamsUrl);
                
                String key = String.format(Constant.CCB_SHOP_BUY_KEY, event.getUserid());
                redis.set(key, JSON.toJSONString(event), 600); //600秒超时
                
                event.setException(new SpiderException(0, "订单生成成功"));
			} catch (Exception e) {
				logger.error("submitOrderDown exception {} ",e);
				event.setException(e);
			}
			
		}

		/**
		 * 	加载订单详情页
		 * @Description: (方法职责详细描述,可空)  
		 * @Title: submitOrderDetailDown 
		 * @param response
		 * @date 2016年1月29日 下午12:23:36  
		 * @author ws
		*/
		private void submitOrderDetailDown(HttpResponse response) {
			try {
				HttpEntity entity = response.getEntity();
                String entityStr = EntityUtils.toString(entity);

                Map<String,Object> orderMap = new HashMap<String,Object>();
                
                String fileDir = XpathHtmlUtils.deleteHeadHtml(entityStr);
                Document document = XpathHtmlUtils.getCleanHtml(fileDir);
                XPath xpath = XPathFactory.newInstance().newXPath();
                //*[@id="orderform"]/input[1]
                String orderTypeXp = "//*[@id='orderform']/input[@name='orderType']";
                String orderType = XpathHtmlUtils.getNodeValue(orderTypeXp, xpath, document);
                
                String voTXp = "//*[@id='orderform']/input[@name='vo.t']";
                String voT = XpathHtmlUtils.getNodeValue(voTXp, xpath, document);

                String voPidXp = "//*[@id='orderform']/input[@name='vo.pid']";
                String voPid = XpathHtmlUtils.getNodeValue(voPidXp, xpath, document);

                String inventoryIdXp = "//*[@id='orderform']/input[@name='inventoryId']";
                String inventoryId = XpathHtmlUtils.getNodeValue(inventoryIdXp, xpath, document);

                String strutsTokenNameXp = "//*[@id='orderform']/input[@name='struts.token.name']";
                String strutsTokenName = XpathHtmlUtils.getNodeValue(strutsTokenNameXp, xpath, document);

                String tokenXp = "//*[@id='orderform']/input[@name='token']";
                String token = XpathHtmlUtils.getNodeValue(tokenXp, xpath, document);

                String formVerifyTokenXp = "//*[@id='orderform']/input[@name='formVerifyToken']";
                String formVerifyToken = XpathHtmlUtils.getNodeValue(formVerifyTokenXp, xpath, document);

                String limitedFlagXp = "//*[@id='orderform']/input[@name='limitedFlag']";
                String limitedFlag = XpathHtmlUtils.getNodeValue(limitedFlagXp, xpath, document);
                
                orderMap.put("orderType", orderType);
                orderMap.put("voT", voT);
                orderMap.put("voPid", voPid);
                orderMap.put("inventoryId", inventoryId);
                orderMap.put("strutsTokenName", strutsTokenName);
                orderMap.put("token", token);
                orderMap.put("formVerifyToken", formVerifyToken);
                orderMap.put("limitedFlag", limitedFlag);
                
                event.setOrderMap(orderMap);
                event.setState(CCBShopState.SUBMIT_ORDER);
                //event.setException(new SpiderException(0, "success", entityStr));
			} catch (Exception e) {
				logger.error("submitOrderDetailDown exception {} ",e);
				event.setException(e);
			}
			
		}

		/**
		 * 	验证图片验证码
		 * @Description: (方法职责详细描述,可空)  
		 * @Title: imgCodeCheckDown 
		 * @param response
		 * @date 2016年1月28日 下午7:52:32  
		 * @author ws
		*/
		private void imgCodeCheckDown(HttpResponse response) {
			try {
				HttpEntity entity = response.getEntity();
                String entityStr = EntityUtils.toString(entity);
                
                //System.out.println(entityStr);
                
                JSONObject object = JSONObject.parseObject(entityStr);
                
                if(null == object.get("result")){
                	event.setException(new SpiderException(1302, "验证图片验证码错误"));
                	return;
                }
                if(object.get("result").toString().equals("true")){

                    event.setState(CCBShopState.LOGIN);
                }else{
                	event.setException(new SpiderException(1052, "图片验证码错误"));
                	return;
                }
			} catch (Exception e) {
				logger.error("imgCodeCheckDown exception {} ",e);
				event.setException(e);
			}
		}

		/**
		 * 	登录
		 * @Description: (方法职责详细描述,可空)  
		 * @Title: loginDown 
		 * @param response
		 * @date 2016年1月28日 下午7:47:13  
		 * @author ws
		*/
		private void loginDown(HttpResponse response) {
			try {
				HttpEntity entity = response.getEntity();
                String entityStr = EntityUtils.toString(entity);
                
                JSONObject object = JSONObject.parseObject(entityStr);
                
                ////*[@id="registerTip0"]
                
                if(null == object.get("result")){//登录失败
                	event.setException(new SpiderException(1060, "用户名或密码错误"));
                	return;
                }
                if(object.get("result").toString().equals("true")){
                    // 缓存当前事件内容
                	event.setLoginType("1");//登录成功
                	
                    String key = String.format(Constant.CCB_SHOP_BUY_KEY, event.getUserid());
                    redis.set(key, JSON.toJSONString(event), redisTime); //600秒超时
                	event.setException(new SpiderException(0,"登录成功"));
                	
                }else{
                	String errTip = String.valueOf(object.get("errTip"));
                	if(StringUtils.isBlank(errTip)){
                		event.setException(new SpiderException(-1, "建行积分商城系统维护中,请稍后再试"));
                	}else{
                		event.setException(new SpiderException(1060, errTip));
                	}
                	return;
                }
			} catch (Exception e) { 
				logger.error("loginDown exception {} ",e);
				event.setException(e);
			}
			
		}

		/**获取图片验证码
		 * 	
		 * @Description: (方法职责详细描述,可空)  
		 * @Title: imgCodeDown 
		 * @param response
		 * @date 2016年1月28日 下午7:47:10  
		 * @author ws
		*/
		private void imgCodeGetDown(HttpResponse response) {
			logger.info("imgCodeGetDown {}", event.getId());
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

                // 缓存当前事件内容
                String key = String.format(Constant.CCB_SHOP_BUY_KEY, event.getUserid());
                redis.set(key, JSON.toJSONString(event), redisTime); //600秒超时
                // 返回当前结果
                event.setException(new SpiderException(0, "0", imgStr));//统一约定，message赋值0
                return;
            } catch (Exception e) {
                logger.error("imgCodeGetDown exception:{}", e);
                event.setException(e);
            }
			
		}

		/**加载登录页面
		 * 	
		 * @Description: (方法职责详细描述,可空)  
		 * @Title: loginPageDown 
		 * @param response
		 * @date 2016年1月28日 下午7:47:07  
		 * @author ws
		*/
		private void loginPageDown(HttpResponse response) {
			event.setState(CCBShopState.IMG_CODE_GET);
		}

		/**
		 * 	加载注册页面
		 * @Description: (方法职责详细描述,可空)  
		 * @Title: registerPageDown 
		 * @param response
		 * @date 2016年1月28日 下午6:41:16  
		 * @author ws
		*/
		private void registerPageDown(HttpResponse response) {
			try {
				HttpEntity entity = response.getEntity();
                String entityStr = EntityUtils.toString(entity);

                String fileDir = XpathHtmlUtils.deleteHeadHtml(entityStr);
                Document document = XpathHtmlUtils.getCleanHtml(fileDir);
                XPath xpath = XPathFactory.newInstance().newXPath();

                String registerToken = "//*[@id='registerToken']";
                String needText = XpathHtmlUtils.getNodeValue(registerToken, xpath, document);
                
                event.setRegisterToken(needText);
                event.setState(CCBShopState.CHECK_ACCOUNT);
			} catch (Exception e) {
				logger.error("registerPageDown exception {} ",e);
				event.setException(e);
			}
			
		}
		/**
		 * 校验用户名
		 * @Description: (方法职责详细描述,可空)  
		 * @Title: checkAccountDown 
		 * @param response
		 * @date 2016年2月1日 下午4:43:56  
		 * @author ws
		 */
		private void checkAccountDown(HttpResponse response) {
			try {
				HttpEntity entity = response.getEntity();
                String entityStr = StringUtils.trim(EntityUtils.toString(entity));
                logger.info("Method checkExistsMobileDown:{}",entityStr);
                if("false".equals(entityStr)){
                	event.setState(CCBShopState.CHECK_EXISTS_MOBILE);
                	return;
                }
                event.setException(new SpiderException(1303, "用户名已存在"));
			} catch (Exception e) {
				logger.error("checkAccountDown exception {} ",e);
				event.setException(e);
			}
		}
		/**
		 * 校验手机号码
		 * 	
		 * @Description: (方法职责详细描述,可空)  
		 * @Title: checkExistsMobileDown 
		 * @param response
		 * @date 2016年2月1日 下午4:44:16  
		 * @author ws
		 */
		private void checkExistsMobileDown(HttpResponse response) {
			try {
				HttpEntity entity = response.getEntity();
                String entityStr = StringUtils.trim(EntityUtils.toString(entity));
                logger.info("Method checkExistsMobileDown:{}",entityStr);
                JSONObject object = JSONObject.parseObject(entityStr);
                if("false".equals(object.getString("resultStr"))){
                	event.setState(CCBShopState.REGISTER_VERIFY_CODE);
                	return ;
                }
                String flag = flagMemo(object);
                event.setException(new SpiderException(1304, flag));
			} catch (Exception e) {
				logger.error("checkExistsMobileDown exception {} ",e);
				event.setException(e);
			}
		}
		/**
		 * 发送注册短信验证码
		 * 	
		 * @Description: (方法职责详细描述,可空)  
		 * @Title: sendSmsCodeForRegisterDown 
		 * @param response
		 * @date 2016年2月1日 下午4:44:35  
		 * @author ws
		 */
		private void sendSmsCodeForRegisterDown(HttpResponse response) {
			try {
				HttpEntity entity = response.getEntity();
                String entityStr = StringUtils.trim(EntityUtils.toString(entity));
                logger.info("Method sendSmsCodeForRegisterDown:{}",entityStr);
                JSONObject object = JSONObject.parseObject(entityStr);
                if("false".equals(object.getString("resultStr"))){
                	event.setException(new SpiderException(0, "验证码发送成功"));
                	
                	String key = String.format(Constant.CCB_SHOP_BUY_KEY, event.getUserid());
                    redis.set(key, JSON.toJSONString(event), 600); //600秒超时
                	return;
                }
                String flag = flagMemo(object);
                event.setException(new SpiderException(1304, flag));
			} catch (Exception e) {
				logger.error("sendSmsCodeForRegisterDown exception {} ",e);
				event.setException(e);
			}
		}
		/**
		 * 验证短信验证码
		 * 	
		 * @Description: (方法职责详细描述,可空)  
		 * @Title: checkSmsCodeForAjaxDown 
		 * @param response
		 * @date 2016年2月1日 下午4:44:46  
		 * @author ws
		 */
		private void checkSmsCodeForAjaxDown(HttpResponse response) {
			try {
				HttpEntity entity = response.getEntity();
                String entityStr = StringUtils.trim(EntityUtils.toString(entity));
                logger.info("Method sendSmsCodeForRegisterDown:{}",entityStr);
                JSONObject object = JSONObject.parseObject(entityStr);
                if("true".equals(object.getString("result"))){
                	event.setState(CCBShopState.REGISTER);
                	return;
                }
                event.setException(new SpiderException(1090, "短信验证码错误!"));
			} catch (Exception e) {
				logger.error("checkSmsCodeForAjaxDown exception {} ",e);
				event.setException(e);
			}
		}
		/**
		 * 提交注册
		 * 	
		 * @Description: (方法职责详细描述,可空)  
		 * @Title: registerSubmitDown 
		 * @param response
		 * @date 2016年2月1日 下午4:44:58  
		 * @author ws
		 */
		private void registerSubmitDown(HttpResponse response) {
			try {
				HttpEntity entity = response.getEntity();
                String entityStr = StringUtils.trim(EntityUtils.toString(entity));
                logger.info("Method sendSmsCodeForRegisterDown:{}",entityStr);
                if(entityStr.contains("恭喜您注册成功")){
                	event.setException(new SpiderException(0, "注册成功"));
                }else{
                	event.setException(new SpiderException(1305, "注册失败"));
                }
                return;
			} catch (Exception e) {
				logger.error("registerSubmitDown exception {} ",e);
				event.setException(e);
			}
		}
		/**
		 * 获取省份地址Json
		 * 	
		 * @Description: (方法职责详细描述,可空)  
		 * @Title: getProvinceGBAddressJSonDown 
		 * @param response
		 * @date 2016年2月1日 下午4:45:09  
		 * @author ws
		 */
		private void getProvinceGBAddressJSonDown(HttpResponse response) {
			try {
				HttpEntity entity = response.getEntity();
                String entityStr = StringUtils.trim(EntityUtils.toString(entity));
                logger.info("Method getProvinceGBAddressJSonDown:{}",entityStr);
                JSONObject object = JSON.parseObject(entityStr);
                Map<String,Object> map = (Map<String, Object>) object;
                for (String key : map.keySet()) {
					if(map.get(key).toString().contains(event.getProvince())){
						event.setProvinceCode(key);
					}
				}
                event.setState(CCBShopState.GET_CITY_GB_ADDRESS_JSON);
			} catch (Exception e) {
				logger.error("getProvinceGBAddressJSonDown exception {} ",e);
				event.setException(e);
			}
		}
		/**
		 * 获取城市地址Json
		 * 	
		 * @Description: (方法职责详细描述,可空)  
		 * @Title: getCityGBAddressJSonDown 
		 * @param response
		 * @date 2016年2月1日 下午4:45:32  
		 * @author ws
		 */
		private void getCityGBAddressJSonDown(HttpResponse response) {
			try {
				HttpEntity entity = response.getEntity();
                String entityStr = StringUtils.trim(EntityUtils.toString(entity));
                logger.info("Method getCityGBAddressJSonDown:{}",entityStr);
                JSONObject object = JSON.parseObject(entityStr);
                Map<String,Object> map = (Map<String, Object>) object;
                for (String key : map.keySet()) {
					if(map.get(key).toString().contains(event.getCity())){
						event.setCityCode(key);
					}
				}
                event.setState(CCBShopState.GET_DISTINCT_GB_ADDRESS_JSON);
			} catch (Exception e) {
				logger.error("getCityGBAddressJSonDown exception {} ",e);
				event.setException(e);
			}
		}
		/**
		 * 获取区域地址Json 
		 * 	
		 * @Description: (方法职责详细描述,可空)  
		 * @Title: getDistinctGBAddressJSonDown 
		 * @param response
		 * @date 2016年2月1日 下午4:45:48  
		 * @author ws
		 */
		private void getDistinctGBAddressJSonDown(HttpResponse response) {
			try {
				HttpEntity entity = response.getEntity();
                String entityStr = StringUtils.trim(EntityUtils.toString(entity));
                logger.info("Method getDistinctGBAddressJSonDown:{}",entityStr);
                JSONObject object = JSON.parseObject(entityStr);
                Map<String,Object> map = (Map<String, Object>) object;
                for (String key : map.keySet()) {
					if(map.get(key).toString().contains(event.getDistinct())){
						event.setDistinctCode(key);
					}
				}
                event.setState(CCBShopState.SUBMIT_ORDER_DETAIL);
			} catch (Exception e) {
				logger.error("getCityGBAddressJSonDown exception {} ",e);
				event.setException(e);
			}
		}
		
		private String flagMemo(JSONObject object){
			String flag = "";
            if("true".equals(object.getString("resultStr"))){
            	flag = "该手机号码已被使用，请重新输入";
            } else 
        	if("accountBlank".equals(object.getString("resultStr"))){
            	flag = "用户名不能为空，请填写用户名";
            } else 
        	if("passwordBlank".equals(object.getString("resultStr"))){
            	flag = "密码不能为空，请填写密码";
            } else 
        	if("phoneNumBlank".equals(object.getString("resultStr"))){
            	flag = "手机号码不能为空，请填写手机号码";
            } else 
        	if("accountFormatError".equals(object.getString("resultStr"))){
            	flag = "用户名格式不正确，请重新填写";
            } else 
        	if("phoneNumberFormatError".equals(object.getString("resultStr"))){
            	flag = "手机号码格式不正确，请重新填写";
            } else {
            	flag = "系统异常，请重试";
            }
            return flag;
		}

		@Override
		public void failed(Exception arg0) {
			
		}
		
	}
}
