package com.caitu99.lsp.spider.cmi;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;
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
import com.alibaba.fastjson.JSONObject;
import com.caitu99.lsp.AppConfig;
import com.caitu99.lsp.Constant;
import com.caitu99.lsp.cache.RedisOperate;
import com.caitu99.lsp.exception.SpiderException;
import com.caitu99.lsp.model.spider.QueryEvent;
import com.caitu99.lsp.model.spider.cmishop.CMIShopEvent;
import com.caitu99.lsp.model.spider.cmishop.CMIShopState;
import com.caitu99.lsp.spider.HttpAsyncClient;
import com.caitu99.lsp.spider.QuerySpider;
import com.caitu99.lsp.utils.JsHelper;
import com.caitu99.lsp.utils.SpringContext;
import com.caitu99.lsp.utils.cookie.CookieHelper;
import com.caitu99.lsp.utils.cookie.HttpCookieEx;

public class CMISpider implements QuerySpider {

    private static final Logger logger = LoggerFactory
            .getLogger(CMISpider.class);

    private static final String initUrl = "http://jf.10086.cn/asynorder/user/web/UserOrderQueryAction?action=initOrderQuery&queryType=0&timePrior=30";
    private static final String userAgent = "Mozilla/5.0 (Windows NT 6.3; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/46.0.2490.86 Safari/537.36";
    private static final String loginPageUrl = "https://jf.10086.cn/login/login.jsp";
    private static final String vcodeUrl = "https://jf.10086.cn/imageRand";
    
    private static final String smsloginUrl = "https://jf.10086.cn/asynlogin/ngves.asiainfo.portal.user.web.UserLoginAction?action=sendSmsConfirmCode&mobileNo=%s";
    
    private static final String loginUrl = "https://jf.10086.cn/asynlogin/ngves.asiainfo.portal.user.web.UserLoginAction?action=login";
    private static final String loginRef = "https://jf.10086.cn/login/login.jsp";
    private static final String homePageUrl = "http://jf.10086.cn/asynlogin/user/web/UserLoginAction";
    private static final String orderDetailUrl = "http://jf.10086.cn/portal/order/web/UserOrderAction?action=directExchangeWare";
    private static final String smsUrl = "http://jf.10086.cn/asynorder/order/web/UserOrderAction?action=sendConfirmCode";
    private static final String orderUrl = "http://jf.10086.cn/asynorder/order/web/UserOrderAction?action=orderDirBusinessInfo";
    private static final String orderRef = "http://jf.10086.cn/portal/order/web/UserOrderAction?action=directExchangeWare";
    protected RedisOperate redis = SpringContext.getBean(RedisOperate.class);
    private CloseableHttpAsyncClient httpAsyncClient = HttpAsyncClient.getInstance();
    private AppConfig appConfig = SpringContext.getBean(AppConfig.class);

    private List<NameValuePair> orderParams = new ArrayList<>();

    @Override
    public void onEvent(QueryEvent event) {
        CMIShopEvent cmiShopEvent = (CMIShopEvent) event;
        try {
            switch (cmiShopEvent.getState()) {
         		case INIT:
         			initUp(cmiShopEvent);
         			break;
                case LOGINPAGE:
                    loginPageUp(cmiShopEvent);
                    break;
                case SMSLOGIN:
                    smsLoginUp(cmiShopEvent);
                    break;   
                    
                    
                case LOGIN:
                    loginUp(cmiShopEvent);
                    break;
                case HOMEPAGE:
                    homePageUp(cmiShopEvent);
                    break;
                case ORDERDETAIL:
                    orderDetailUp(cmiShopEvent);
                    break;
                case SMS:
                    smsUp(cmiShopEvent);
                    break;
                case ORDER:
                    orderUp(cmiShopEvent);
                    break;
                case ERROR:
                    errorHandle(cmiShopEvent);
                    break;
            }
        } catch (Exception e) {
            logger.error("request up error {}", event.getId(), e);
            cmiShopEvent.setException(e);
            errorHandle(cmiShopEvent);
        }
    }

    private void setHeader(String uriStr, HttpMessage httpMessage, QueryEvent event) {
        httpMessage.setHeader("Accept", "*/*");
        httpMessage.setHeader("User-Agent", userAgent);
        try {
            CookieHelper.setCookies(uriStr, httpMessage, event.getCookieList());
        } catch (URISyntaxException e) {
            logger.error("set cookie fail {}", event.getId());
        }
    }

    /**
     * 构造登录表单数据
     *
     * @param event
     * @return
     */
    private List<? extends NameValuePair> createLoginFormEntity(CMIShopEvent event) {
        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("mobilePhone", event.getAccount()));
        params.add(new BasicNameValuePair("password", event.getPassword()));
        params.add(new BasicNameValuePair("code", ""));
        params.add(new BasicNameValuePair("isRememberMe", "1"));
        params.add(new BasicNameValuePair("smsConfirmCode", event.getvCode()));
        return params;
    }

    
    private void initUp(CMIShopEvent cmiShopEvent){
        logger.debug("request init up {}", cmiShopEvent.getId());
        HttpGet httpGet = new HttpGet(initUrl);
        setHeader(initUrl, httpGet, cmiShopEvent);
        httpAsyncClient.execute(httpGet, new HttpAsyncCallback(cmiShopEvent));
    }
    
    /**
     * 请求登录页面
     *
     * @param cmiShopEvent
     */
    private void loginPageUp(CMIShopEvent cmiShopEvent) {
        logger.debug("request loginPage up {}", cmiShopEvent.getId());
        HttpGet httpGet = new HttpGet(loginPageUrl);
        setHeader(loginPageUrl, httpGet, cmiShopEvent);
        httpAsyncClient.execute(httpGet, new HttpAsyncCallback(cmiShopEvent));
    }

    /**
     * 请求验证码
     *
     * @param cmiShopEvent
     */
/*    private void vcodeUp(CMIShopEvent cmiShopEvent) {
        logger.debug("request vcode up {}", cmiShopEvent.getId());
        HttpPost httpPost = new HttpPost(vcodeUrl);
        try {
            List<NameValuePair> params = new ArrayList<>();
            params.add(new BasicNameValuePair("mobilePhone", cmiShopEvent.getAccount()));
            httpPost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
        } catch (Exception e) {
            logger.error("encode vcode body error {}", cmiShopEvent.getId(), e);
            cmiShopEvent.setException(e);
            return;
        }
        setHeader(vcodeUrl, httpPost, cmiShopEvent);
        httpAsyncClient.execute(httpPost, new HttpAsyncCallback(cmiShopEvent));
    }*/
    
    /**
     * 发送登录短信验证码
     * @Description: (方法职责详细描述,可空)  
     * @Title: smsLoginUp 
     * @param cmiShopEvent
     * @date 2016年5月24日 上午10:06:25  
     * @author fangjunxiao
     */
    private void smsLoginUp(CMIShopEvent cmiShopEvent) {
        logger.debug("request sms login up {}", cmiShopEvent.getId());
        String url = String.format(smsloginUrl, cmiShopEvent.getAccount());
        HttpPost httpPost = new HttpPost(url);
        setHeader(smsloginUrl, httpPost, cmiShopEvent);
        httpAsyncClient.execute(httpPost, new HttpAsyncCallback(cmiShopEvent));
    }
    

    /**
     * 请求登录
     *
     * @param cmiShopEvent
     */
    private void loginUp(CMIShopEvent cmiShopEvent) {
        logger.debug("request login up {}", cmiShopEvent.getId());
        HttpPost httpPost = new HttpPost(loginUrl);
        try {
            cmiShopEvent.setPassword(JsHelper.getJSEXEResult("var VdKy1 = \"ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/=\";function encodeForPwd(V2) {var s3 = \"\";var fapXPYTDM4, Se5, LFpuqfCc6 = \"\";var fFkKqMU7, Atgh8, cQOqgsbl9, cYHoFcL10 = \"\";var mVKze11 = 0;do {fapXPYTDM4 = V2.charCodeAt(mVKze11++);Se5 = V2.charCodeAt(mVKze11++);LFpuqfCc6 = V2.charCodeAt(mVKze11++);fFkKqMU7 = fapXPYTDM4 >> 2;Atgh8 = (fapXPYTDM4 & 3) << 4 | Se5 >> 4;cQOqgsbl9 = (Se5 & 15) << 2 | LFpuqfCc6 >> 6;cYHoFcL10 = LFpuqfCc6 & 63;if (isNaN(Se5)) {cQOqgsbl9 = cYHoFcL10 = 64;} else if (isNaN(LFpuqfCc6)) {cYHoFcL10 = 64;}s3 = s3 + VdKy1.charAt(fFkKqMU7) + VdKy1.charAt(Atgh8) + VdKy1.charAt(cQOqgsbl9) + VdKy1.charAt(cYHoFcL10);fapXPYTDM4 = Se5 = LFpuqfCc6 = \"\";fFkKqMU7 = Atgh8 = cQOqgsbl9 = cYHoFcL10 = \"\";} while (mVKze11 < V2.length);return s3;} encodeForPwd('" + cmiShopEvent.getPassword() + "');"));
            String infos = JsHelper.getJSEXEResult("function infos(){var F=\"2\"; var E=new Date(); var D=new Date(E.getTime()+315360000000); var C=new Date(E.getTime()); if(F.length<10){this.p+=\"&WT.vt_f=1&WT.entry=1\"; var B=E.getTime().toString(); for(var A=2; A<=(32-B.length); A++){F+=Math.floor(Math.random()*16).toString(16) }F+=B }F=encodeURIComponent(F); this.p+=\"&WT.co_f=\"+F; return \"WT_FPC=id=\"+F+\":lv=\"+E.getTime().toString()+\":ss=\"+C.getTime().toString()+\"; expires=\"+D.toGMTString()+\"; path=/; domain=.10086.cn\"; }; infos();");
            String[] aInfos = infos.split(";");
            String sCookie1 = "Set-Cookie: " + aInfos[0] + ";" + aInfos[2];
            String sCookie2 = "Set-Cookie: AWSUSER_ID=22131223883479835000; path=/";
            String sCookie3 = "Set-Cookie: AWSSESSION_ID=59314604196697470000; path=/";
            
            
            //String sCookie4 = "Set-Cookie: JFUSERID1=fe544c9334fd48e7f71392116a34050f223bb4761452305780402; path=/";
            cmiShopEvent.getCookieList().addAll(HttpCookieEx.parse(sCookie1));
            cmiShopEvent.getCookieList().addAll(HttpCookieEx.parse(sCookie2));
            cmiShopEvent.getCookieList().addAll(HttpCookieEx.parse(sCookie3));
            //event.getCookieList().addAll(HttpCookieEx.parse(sCookie4));
            httpPost.setEntity(new UrlEncodedFormEntity(createLoginFormEntity(cmiShopEvent), "UTF-8"));
        } catch (Exception e) {
            logger.error("encode login body error {}", cmiShopEvent.getId(), e);
            cmiShopEvent.setException(e);
            return;
        }
        setHeader(loginUrl, httpPost, cmiShopEvent);
        httpPost.setHeader("Referer", loginRef);
        httpAsyncClient.execute(httpPost, new HttpAsyncCallback(cmiShopEvent));
    }

    /**
     * 请求主页
     *
     * @param cmiShopEvent
     */
    private void homePageUp(CMIShopEvent cmiShopEvent) {
        logger.debug("request homePage up {}", cmiShopEvent.getId());
        HttpPost httpPost = new HttpPost(homePageUrl);
        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("action", "isUserLogin"));
        params.add(new BasicNameValuePair("time", ""));
        try {
            httpPost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
        } catch (Exception e) {
            logger.error("encode userinfo body error {}", cmiShopEvent.getId(), e);
            cmiShopEvent.setException(e);
            return;
        }
        setHeader(homePageUrl, httpPost, cmiShopEvent);
        httpAsyncClient.execute(httpPost, new HttpAsyncCallback(cmiShopEvent));
    }

    /**
     * 获取短信
     *
     * @param cmiShopEvent
     */
    private void smsUp(CMIShopEvent cmiShopEvent) {
        logger.debug("request sms up {}", cmiShopEvent.getId());
        HttpPost httpPost = new HttpPost(smsUrl);
//        List<NameValuePair> params = new ArrayList<>();
//        params.add(new BasicNameValuePair("action", "sendConfirmCode"));
        if( cmiShopEvent.getWanlitongAccount() == null ){
            String sCookie = "Set-Cookie: hisProductv3=" + cmiShopEvent.getHisProductv3() + "; path=/";
            cmiShopEvent.getCookieList().addAll(HttpCookieEx.parse(sCookie));
        }
//        try {
//            httpPost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
//        } catch (Exception e) {
//            logger.error("encode sms body error {}", cmiShopEvent.getId(), e);
//            cmiShopEvent.setException(e);
//            return;
//        }
        setHeader(smsUrl, httpPost, cmiShopEvent);
        httpAsyncClient.execute(httpPost, new HttpAsyncCallback(cmiShopEvent));
    }

    /**
     * 请求订单详细页
     *
     * @param cmiShopEvent
     */
    private void orderDetailUp(CMIShopEvent cmiShopEvent) {
        logger.debug("request order up {}", cmiShopEvent.getId());
        HttpPost httpPost = new HttpPost(orderDetailUrl);
        List<NameValuePair> params = new ArrayList<>();
        if ( cmiShopEvent.getWanlitongAccount() == null ){
            params.add(new BasicNameValuePair("wareIds", cmiShopEvent.getWareIds()));
            params.add(new BasicNameValuePair("magIds", ""));
            params.add(new BasicNameValuePair("amount", "1"));
            params.add(new BasicNameValuePair("BMobile", ""));
            params.add(new BasicNameValuePair("typeCode", ""));
            params.add(new BasicNameValuePair("selectPayType", "01"));
            params.add(new BasicNameValuePair("selectPayType", ""));
            params.add(new BasicNameValuePair("proCode", "zj"));
            params.add(new BasicNameValuePair("cityCode", "576"));
            params.add(new BasicNameValuePair("disCode", "57608"));
        }else{
        	params.add(new BasicNameValuePair("action","directExchangeWare"));
            params.add(new BasicNameValuePair("amount", cmiShopEvent.getAmount()+""));
            params.add(new BasicNameValuePair("wareIds", cmiShopEvent.getWareIds()));
            params.add(new BasicNameValuePair("selectPayType", "01"));
            params.add(new BasicNameValuePair("curAllIntegral", cmiShopEvent.getCurAllIntegral()));
        }

        try {
            httpPost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
        } catch (Exception e) {
            logger.error("encode order detail body error {}", cmiShopEvent.getId(), e);
            cmiShopEvent.setException(e);
            return;
        }
        httpPost.setHeader("Referer", orderRef);
        setHeader(orderDetailUrl, httpPost, cmiShopEvent);
        httpAsyncClient.execute(httpPost, new HttpAsyncCallback(cmiShopEvent));
    }

    /**
     * 下单
     *
     * @param cmiShopEvent
     */
    private void orderUp(CMIShopEvent cmiShopEvent) {
        logger.debug("request order up {}", cmiShopEvent.getId());
        HttpPost httpPost = new HttpPost(orderUrl);
        Date date = new Date();
        //http://jf.10086.cn/asynorder/order/web/UserOrderAction?action=orderDirBusinessInfo

        
        orderParams.add(new BasicNameValuePair("confirmCode", cmiShopEvent.getSmsCode()));
        if( cmiShopEvent.getWanlitongAccount() != null )
            orderParams.add(new BasicNameValuePair("dataDIY", cmiShopEvent.getWanlitongAccount()));
        
        orderParams.add(new BasicNameValuePair("receiveName", ""));
        orderParams.add(new BasicNameValuePair("sendProvinceCode", ""));
        orderParams.add(new BasicNameValuePair("sendCityCode", ""));
        orderParams.add(new BasicNameValuePair("sendDistrictCode", ""));
        orderParams.add(new BasicNameValuePair("receiveAddress", ""));
        orderParams.add(new BasicNameValuePair("receivePostCode", ""));
        orderParams.add(new BasicNameValuePair("receiveTelphone", ""));
        orderParams.add(new BasicNameValuePair("receiveTime", ""));
        orderParams.add(new BasicNameValuePair("nbafp", ""));
        orderParams.add(new BasicNameValuePair("magIds", ""));
        orderParams.add(new BasicNameValuePair("bMobile", ""));
        orderParams.add(new BasicNameValuePair("typeCode", ""));
        orderParams.add(new BasicNameValuePair("ticketReserve", ""));
        orderParams.add(new BasicNameValuePair("payType", ""));
        orderParams.add(new BasicNameValuePair("invoiceTitle", ""));
        
        orderParams.add(new BasicNameValuePair("singleCurPayCash", "0"));
        orderParams.add(new BasicNameValuePair("singleCurPayIntegral", "0"));
        orderParams.add(new BasicNameValuePair("isDynamicPrice", "0"));
        orderParams.add(new BasicNameValuePair("wareDeliType", "sms"));
        orderParams.add(new BasicNameValuePair("addrId", "0"));
        orderParams.add(new BasicNameValuePair("wareType", "0"));
        
        orderParams.add(new BasicNameValuePair("amount", cmiShopEvent.getAmount()+""));
        orderParams.add(new BasicNameValuePair("wareIds", cmiShopEvent.getWareIds()));
        orderParams.add(new BasicNameValuePair("packageCount", "1"));
        
        orderParams.add(new BasicNameValuePair("delivType", "15"));
        orderParams.add(new BasicNameValuePair("dirKey", String.valueOf(date.getTime())));
        
        
       // wareIds	100000045987633
        try {
            httpPost.setEntity(new UrlEncodedFormEntity(orderParams, "UTF-8"));
        } catch (Exception e) {
            logger.error("encode userinfo body error {}", cmiShopEvent.getId(), e);
            cmiShopEvent.setException(e);
            return;
        }
        setHeader(orderUrl, httpPost, cmiShopEvent);
        httpAsyncClient.execute(httpPost, new HttpAsyncCallback(cmiShopEvent));
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

    private class HttpAsyncCallback implements FutureCallback<HttpResponse> {
        private CMIShopEvent event;
        private boolean skipNextStep = false;

        public HttpAsyncCallback(CMIShopEvent event) {
            this.event = event;
        }

        @Override
        public boolean equals(Object obj) {
            return super.equals(obj);
        }

        @Override
        public void completed(HttpResponse result) {
            try {
            	CookieHelper.getCookiesFresh(event.getCookieList(), result);
                switch (event.getState()) {
                	case INIT:
                		initDown(result);
                		break;
                    case LOGINPAGE:
                        loginPageDown(result);
                        break;
                    case SMSLOGIN:
                        smsLoginDown(result);
                        break;    
                        
                        
                    case LOGIN:
                        loginDown(result);
                        break;
                    case HOMEPAGE:
                        homePageDown(result);
                        break;
                    case ORDERDETAIL:
                        orderDetailDown(result);
                        break;
                    case SMS:
                        smsDown(result);
                        break;
                    case ORDER:
                        orderDown(result);
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

        @Override
        public void failed(Exception ex) {

        }

        @Override
        public void cancelled() {

        }

        private void initDown(HttpResponse result){
            logger.debug("init page down {}", event.getId());
            event.setState(CMIShopState.LOGINPAGE);
        }
        
        
        /**
         * 登录页面请求完成
         *
         * @param result
         */
        private void loginPageDown(HttpResponse result) {
            logger.debug("login page down {}", event.getId());
            try {
                if (event.getIsLogin()) {
                    event.setState(CMIShopState.LOGIN);
                } else {
                    event.setState(CMIShopState.SMSLOGIN);
                }
            } catch (Exception e) {
                logger.error("get login Page Down exception", e);
                event.setException(e);
            }
        }

        /**
         * 请求获取验证码完成
         *
         * @param result
         */
/*        private void vcodeDown(HttpResponse result) {
            logger.debug("request vcode down {}", event.getId());
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
                    // FileOutputStream fs = new
                    // FileOutputStream("/Users/bobo/Desktop/vcode.jpg");
                    FileOutputStream fs = new FileOutputStream(
                            appConfig.getUploadPath() + "/" + event.getUserid()
                                    + ".jpg");
                    fs.write(tbytes);
                    fs.close();
                }
                String key = String.format(Constant.ISHOP_CM_TASK_QUEUE, event.getAccount());
                redis.set(key, JSON.toJSONString(event), 600);
                event.setException(new SpiderException(1001, "输入验证码", imgStr));
            } catch (Exception e) {
                logger.error("get vcode Down exception", e);
                event.setException(e);
            }
        }*/
        
        
        private void smsLoginDown(HttpResponse result) {
            try {
                logger.debug("request sms login down {}", event.getId());
                String resultStr = null;
                try {
                    resultStr = EntityUtils.toString(result.getEntity());
                } catch (IOException e) {
                    logger.error("get result exception", e);
                    event.setException(e);
                }
                if ("SUCCESS".equals(resultStr)) {
                    String key = String.format(Constant.ISHOP_CM_TASK_QUEUE, event.getAccount());
                    redis.set(key, JSON.toJSONString(event),480);
                    event.setException(new SpiderException(1082, "短信验证码获取成功"));
                } else {
                    logger.error("get sms fail" + resultStr);
                    event.setException(new SpiderException(1086, "短信验证码获取失败"));
                }
            } catch (Exception e) {
                logger.error("get result exception", e);
                event.setException(e);
            }
        }
        
        
        
        /**
         * 登录请求完成
         *
         * @param result
         */
        private void loginDown(HttpResponse result) {
            logger.debug("login down {}", event.getId());
            try {
                String resultStr = EntityUtils.toString(result.getEntity());
                JSONObject jsonObject = JSON.parseObject(resultStr);
                String msg = jsonObject.getString("msg");
                if ("SYS_MSG_AUTHENTICATE_SUCCESS".equals(msg)) {
                    CookieHelper.getCookiesFresh(event.getCookieList(), result);
                    event.setState(CMIShopState.HOMEPAGE);
                } else if ("SYS_MSG_LOGIN_SMS_CONFIRM_CODE_WRONG".equals(msg)){
                    event.setException(new SpiderException(1087, "验证码错误"));
                } else if ("SYS_MSG_LOGIN_MOBILE_CODE_WRONG".equals(msg)) {
                    event.setException(new SpiderException(1089, "验证手机号码空或者长度不正确"));
                } else if ("SYS_MSG_LOGIN_CODE_LENGTH_WRONG".equals(msg)) {
                    event.setException(new SpiderException(1088, "服务密码错误"));
                } else if ("22001".equals(msg)) {
                    event.setException(new SpiderException(1092, "您的手机已被系统锁定，请咨询当地10086"));
                } else if ("42010".equals(msg)){
                    event.setException(new SpiderException(1119, "尊敬的客户，您当前已使用移动手机号码在积分商城登录，如需登录此手机号码请将原登录号码退出后再操作，谢谢"));
                }else {
                    event.setException(new SpiderException(1093, "未知错误"));
                }
            } catch (IOException e) {
                logger.error("get result exception", e);
                event.setException(e);
            }
        }

        /**
         * 请求主页完成
         *
         * @param result
         */
        private void homePageDown(HttpResponse result) {
            logger.debug("request homePage down {}", event.getId());
            try {
                String resultStr = EntityUtils.toString(result.getEntity());
                JSONObject jsonObject = JSON.parseObject(resultStr);
                String status = jsonObject.getString("status");
                if (!"USER_LOGIN_STATUS_LOGIN".equals(status)) {
                    event.setException(new SpiderException(1081, "用户尚未登录"));
                } else {
                    //保存积分信息
//                    JSONObject info_jsonObject = JSON.parseObject(jsonObject.getString("info"));
//                    Integer integral = info_jsonObject.getInteger("totalPoint");
                    String key = String.format(Constant.ISHOP_CM_LOGIN_EVENT, event.getUserid());
                    CookieHelper.getCookiesFresh(event.getCookieList(), result);
                    redis.set(key, JSON.toJSONString(event),480);
                    event.setException(new SpiderException(1019, "登录成功", resultStr));
                }
            } catch (IOException e) {
                logger.error("get result exception", e);
                event.setException(e);
            }
        }

        /**
         * 获取订单详情完成
         *
         * @param result
         */
        private void orderDetailDown(HttpResponse result) {
            logger.debug("request order detail down {}", event.getId());
            String resultStr = null;
            try {
                resultStr = EntityUtils.toString(result.getEntity());
            } catch (IOException e) {
                logger.error("get result exception", e);
                event.setException(e);
            }
            Document document = Jsoup.parse(resultStr);
            Element element = document.getElementById("orderForm");
            orderParams.clear();
            if(element != null &&  element.children().size() > 0){
                for (Element child : element.children()) {
                    String name = child.attr("name");
                    String value = child.attr("value");
                    if (name == null || "confirmCode".equals(name)||"dataDIY".equals(name)) {
                        continue;
                    }
                    if ("packageCount".equals(name)) {
                        orderParams.add(new BasicNameValuePair("packageCount", "1"));
                        continue;
                    }
                    orderParams.add(new BasicNameValuePair(name, value == null ? "" : value));
                }
            }
            CookieHelper.getCookiesFresh(event.getCookieList(), result);
            event.setState(CMIShopState.SMS);
        }

        /**
         * 获取短信完成
         *
         * @param result
         */
        private void smsDown(HttpResponse result) {
            logger.debug("request sms down {}", event.getId());
            String resultStr = null;
            try {
                resultStr = EntityUtils.toString(result.getEntity());
            } catch (IOException e) {
                logger.error("get result exception", e);
                event.setException(e);
            }
            if ("SUCCESS".equals(resultStr)) {
                String key = String.format(Constant.ISHOP_CM_LOGIN_EVENT, event.getUserid());
                redis.set(key, JSON.toJSONString(event),480);
                event.setException(new SpiderException(1082, "短信验证码获取成功"));
            } else {
                logger.error("get sms fail" + resultStr);
                event.setException(new SpiderException(1086, "短信验证码获取失败"));
            }
        }

        /**
         * 下单完成
         *
         * @param result
         */
        private void orderDown(HttpResponse result) {
            logger.debug("request order down {}", event.getId());
            String resultStr = null;
            String orderNo = null;
            try {
                resultStr = EntityUtils.toString(result.getEntity());
                Document document = Jsoup.parse(resultStr);
                Elements errorEles = document.getElementsByClass("wrong-r-msg-01");
                if (errorEles != null && errorEles.size() > 0) {
                    Element element = errorEles.get(0);
                    String errorMsg = element.text();
                    if (errorMsg.contains("短信确认码已经失效") || errorMsg.contains("短信确认码错误")) {
                        event.setException(new SpiderException(1090, "短信验证码错误"));
                        return;
                    }else if(errorMsg.contains("号码已经被加锁")){
                        event.setException(new SpiderException(1120, "移动号码被锁，请联系10086"));
                        return;
                    }else if (errorMsg.contains("该类型礼品每个用户每月限兑1款")) {
                        event.setException(new SpiderException(1091, "该类型礼品每个用户每月限兑1款"));
                        return;
                    }else if(errorMsg.contains("不允许重复提交")){
                        event.setException(new SpiderException(1094, "订单重复提交"));
                        return;
                    }
                } else {
                    Elements elements = document.getElementsByClass("order-msg-table");
                    if (elements != null && elements.size() > 0) {
                        Element element = elements.get(0);
                        Elements trs = element.getElementsByTag("tr");
                        orderNo = trs.get(1).child(0).text();
                    } else {
                        logger.error("get order no exception");
                    }
                }
            } catch (IOException e) {
                logger.error("get order result exception", e);
                event.setException(e);
            }
            if (StringUtils.isEmpty(orderNo)) {
                event.setException(new SpiderException(1089, "下单失败"));
            } else {
                event.setException(new SpiderException(1083, "下单成功", orderNo));
            }
        }

        public void errorHandle(CMIShopEvent event) {
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
                    deferredResult.setResult((new SpiderException(-1, exception.getMessage()).toString()));
                }
            }
        }

    }
}