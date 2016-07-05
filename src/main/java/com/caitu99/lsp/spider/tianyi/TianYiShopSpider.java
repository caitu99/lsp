/*
 * Copyright (c) 2015-2020 by caitu99
 * All rights reserved.
 */
package com.caitu99.lsp.spider.tianyi;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import com.caitu99.lsp.model.spider.tianyi.TianYiSpiderState;
import org.apache.http.HttpEntity;
import org.apache.http.HttpMessage;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.request.async.DeferredResult;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.caitu99.lsp.AppConfig;
import com.caitu99.lsp.Constant;
import com.caitu99.lsp.cache.RedisOperate;
import com.caitu99.lsp.exception.SpiderException;
import com.caitu99.lsp.model.spider.QueryEvent;
import com.caitu99.lsp.model.spider.tianyi.TianYiShopSpiderEvent;
import com.caitu99.lsp.model.spider.tianyi.TianYiShopSpiderState;
import com.caitu99.lsp.spider.HttpAsyncClient;
import com.caitu99.lsp.spider.QuerySpider;
import com.caitu99.lsp.utils.SpringContext;
import com.caitu99.lsp.utils.cookie.CookieHelper;

/**
 * @author ws
 * @Description: (类职责详细描述, 可空)
 * @ClassName: TianYiSpider
 * @date 2015年11月24日 下午4:56:02
 * @Copyright (c) 2015-2020 by caitu99
 */
public class TianYiShopSpider implements QuerySpider {

    //	private static final String userAgent = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_10_5) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/45.0.2454.101 Safari/537.36";
    private static final String URL_INIT = "http://tyclub.telefen.com/newjf_hgo2/html/HGOIndex_em_qg.html?provinceId=35";
    private static final String URL_IMG = "http://tyclub.telefen.com/newlife/servlet/validateCodeServlet?re=0.022612563567236066";
    private static final String URL_CHECK = "http://tyclub.telefen.com/newlife/jfInterface/imgCodeRON?imgCode=%s";
    private static final String URL_MSG = "http://tyclub.telefen.com/newlife/jfInterface/setMsCode?DeviceNo=%s&SmsCode=12&imgCode=%s";
    private static final String URL_LOGIN = "http://tyclub.telefen.com/newlife/interface/msCodeLogin?Mobile=%s&MsCode=%s";
    private static final String URL_ORDER = "http://tyclub.telefen.com/newlife/interface/createOrder?CustID=%s&DeviceNo=%s&DeviceType=%s&ProvinceID=%s&SystemType=%s&BuyNum=%s&CommodityID=%s&PayVoucher=%s&PayIntegral=%s&Money=%s&PayFlag=%s";
    private static final String URL_PAY = "http://tyclub.telefen.com/newlife/interface/payAllotManage?DeviceNo=%s&OrderID=%s&OrderIntegral=%s&OrderVoucher=%s&OrderMoney=%s&RndCode=%s&CommodityID=%s";
    private static final String URL_MSG_PAY = "http://tyclub.telefen.com/newlife/interface/setMsCode?DeviceNo=%s&SmsCode=13";
    private static final String URL_JF_GET = "http://tyclub.telefen.com/newlife/interface/getJfByPhone?Mobile=%s";
    
    private static final String URL_GETCODE = "http://tyclub.telefen.com/newlife/interface/getTradeList?DeviceType=%s&CustID=%s&DeviceNo=%s&PageIndex=1&Status=&ProvinceID=%s&DataType=0&PageSize=5";
    
    
    private static final Logger logger = LoggerFactory.getLogger(TianYiShopSpider.class);
    private CloseableHttpAsyncClient httpAsyncClient = HttpAsyncClient.getInstance();
    private AppConfig appConfig = SpringContext.getBean(AppConfig.class);
    private RedisOperate redis = SpringContext.getBean(RedisOperate.class);

    @Override
    public void onEvent(QueryEvent event) {
        TianYiShopSpiderEvent tyEvent = (TianYiShopSpiderEvent) event;
        try {
            switch (tyEvent.getState()) {
                case NONE:
                    initUp(tyEvent);
                    break;
                case IMG:
                    imgUp(tyEvent);
                    break;
                case CHECK:
                    checkUp(tyEvent);
                    break;
                case MSG:
                    msgUp(tyEvent);
                    break;
                case LOGIN:
                    loginUp(tyEvent);
                    break;
                case ORDER:
                    orderUp(tyEvent);
                    break;
                case MSG_PAY:
                    msgPayUp(tyEvent);
                    break;
                case PAY:
                    payUp(tyEvent);
                    break;
                case GET_JF:
                    getJfUp(tyEvent);
                    break;
                case GET_CODE:
                	getCode(tyEvent);
                case ERROR:
                    errorHandle(tyEvent);
                    break;
            }
        } catch (Exception e) {
            logger.error("request up error {}", event.getId(), e);
            tyEvent.setException(e);
            errorHandle(tyEvent);
        }
    }


    /**
	 * 	
	 * @Description: (方法职责详细描述,可空)  
	 * @Title: getJfUp 
	 * @param tyEvent
	 * @date 2016年2月16日 上午10:20:32  
	 * @author ws
	*/
	private void getJfUp(TianYiShopSpiderEvent tyEvent) {
		String url = String.format(URL_JF_GET, tyEvent.getAccount());
        HttpGet httpGet = new HttpGet(url);
        setHeader(url, httpGet, tyEvent);
        httpGet.setHeader("X-Requested-With", "XMLHttpRequest");
        httpAsyncClient.execute(httpGet, new HttpAsyncCallback(tyEvent));
	}


	/**
	 * 	
	 * @Description: (方法职责详细描述,可空)  
	 * @Title: msgPayUp 
	 * @param tyEvent
	 * @date 2016年2月14日 下午4:56:01  
	 * @author ws
	*/
	private void msgPayUp(TianYiShopSpiderEvent tyEvent) {
		String url = String.format(URL_MSG_PAY, tyEvent.getAccount());
        HttpGet httpGet = new HttpGet(url);
        setHeader(url, httpGet, tyEvent);
        httpGet.setHeader("X-Requested-With", "XMLHttpRequest");
        httpAsyncClient.execute(httpGet, new HttpAsyncCallback(tyEvent));
	}


	/**
	 * 	支付订单
	 * @Description: (方法职责详细描述,可空)  
	 * @Title: payUp 
	 * @param tyEvent
	 * @date 2016年2月14日 下午4:21:20  
	 * @author ws
	*/
	private void payUp(TianYiShopSpiderEvent tyEvent) {
		String url = String.format(URL_PAY
				, tyEvent.getAccount()
				, tyEvent.getOrderID()
				, tyEvent.getPayIntegral()
				, tyEvent.getPayVoucher()
				, tyEvent.getMoney()
				, tyEvent.getMsgCode()
				, tyEvent.getCommodityID());
		HttpGet httpGet = new HttpGet(url);
        setHeader(url, httpGet, tyEvent);
        httpAsyncClient.execute(httpGet, new HttpAsyncCallback(tyEvent));
		
	}


	/**
	 * 	创建订单
	 * @Description: (方法职责详细描述,可空)  
	 * @Title: orderUp 
	 * @param tyEvent
	 * @date 2016年2月14日 下午4:12:33  
	 * @author ws
	*/
	private void orderUp(TianYiShopSpiderEvent tyEvent) {
		String url = String.format(URL_ORDER
				, tyEvent.getCustID()
				, tyEvent.getAccount()
				, tyEvent.getDeviceType()
				, tyEvent.getProvinceID()
				, tyEvent.getSystemType()
				, tyEvent.getBuyNum()
				, tyEvent.getCommodityID()
				, "0"
				, tyEvent.getPayTotal()
				, "0"
				, tyEvent.getPayFlag());
		HttpGet httpGet = new HttpGet(url);
        setHeader(url, httpGet, tyEvent);
        httpAsyncClient.execute(httpGet, new HttpAsyncCallback(tyEvent));
		
	}


	/**
     * @param tyEvent
     * @Description: (方法职责详细描述, 可空)
     * @Title: initUp
     * @date 2015年11月24日 下午5:03:58
     * @author ws
     */
    private void initUp(TianYiShopSpiderEvent tyEvent) {
        logger.debug("do initUp {}", tyEvent.getId());
        HttpGet httpGet = new HttpGet(URL_INIT);
        setHeader(URL_INIT, httpGet, tyEvent);
        httpAsyncClient.execute(httpGet, new HttpAsyncCallback(tyEvent));
    }


    /**
     * @param tyEvent
     * @Description: (方法职责详细描述, 可空)
     * @Title: checkUp
     * @date 2015年11月24日 下午5:03:56
     * @author ws
     */
    private void imgUp(TianYiShopSpiderEvent tyEvent) {
        logger.debug("do imgUp {}", tyEvent.getId());
        HttpGet httpGet = new HttpGet(URL_IMG);
        setHeader(URL_IMG, httpGet, tyEvent);
        httpAsyncClient.execute(httpGet, new HttpAsyncCallback(tyEvent));
    }


    /**
     * @param tyEvent
     * @Description: (方法职责详细描述, 可空)
     * @Title: msgUp
     * @date 2015年11月24日 下午5:03:53
     * @author ws
     */
    private void checkUp(TianYiShopSpiderEvent tyEvent) {
        logger.debug("do checkUp {}", tyEvent.getId());
        String url = String.format(URL_CHECK, tyEvent.getvCode());
        HttpGet httpGet = new HttpGet(url);
        setHeader(url, httpGet, tyEvent);
        httpGet.setHeader("X-Requested-With", "XMLHttpRequest");
        httpAsyncClient.execute(httpGet, new HttpAsyncCallback(tyEvent));
    }


    /**
     * @param tyEvent
     * @Description: (方法职责详细描述, 可空)
     * @Title: msgUp
     * @date 2015年11月24日 下午5:35:28
     * @author ws
     */
    private void msgUp(TianYiShopSpiderEvent tyEvent) {
        logger.debug("do msgUp {}", tyEvent.getId());
        String url = String.format(URL_MSG, tyEvent.getAccount(), tyEvent.getvCode());
        HttpGet httpGet = new HttpGet(url);
        setHeader(url, httpGet, tyEvent);
        httpGet.setHeader("X-Requested-With", "XMLHttpRequest");
        httpAsyncClient.execute(httpGet, new HttpAsyncCallback(tyEvent));

    }


    /**
     * @param tyEvent
     * @Description: (方法职责详细描述, 可空)
     * @Title: loginUp
     * @date 2015年11月24日 下午5:03:51
     * @author ws
     */
    private void loginUp(TianYiShopSpiderEvent tyEvent) {
        logger.debug("do loginUp {}", tyEvent.getId());
        String url = String.format(URL_LOGIN, tyEvent.getAccount(), tyEvent.getMsgCode());
        HttpGet httpGet = new HttpGet(url);
        setHeader(url, httpGet, tyEvent);
        httpGet.setHeader("X-Requested-With", "XMLHttpRequest");
        httpAsyncClient.execute(httpGet, new HttpAsyncCallback(tyEvent));
    }
    
    
    private void getCode(TianYiShopSpiderEvent tyEvent){
        logger.debug("do getCode {}", tyEvent.getId());
        
		String url = String.format(URL_GETCODE
				, tyEvent.getDeviceType()
				, tyEvent.getCustID()
				, tyEvent.getAccount()
				, tyEvent.getProvinceID());
        
        HttpGet httpGet = new HttpGet(url);
        setHeader(url, httpGet, tyEvent);
        httpGet.setHeader("X-Requested-With", "XMLHttpRequest");
        httpAsyncClient.execute(httpGet, new HttpAsyncCallback(tyEvent));
    	
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

    private void setHeader(String uriStr, HttpMessage httpGet, QueryEvent event) {
//		httpGet.setHeader("User-Agent", userAgent);
//		httpGet.setHeader("Host", "tyclub.telefen.com");
//		httpGet.setHeader("Connection", "keep-alive");
        httpGet.setHeader("Accept", "*/*");
        httpGet.setHeader("User-Agent", "Android");
//		httpGet.setHeader("Referer", "http://tyclub.telefen.com/newjf_hgo2/html/HGOIndex_em.html?provinceId=35");
//		httpGet.setHeader("Accept-Encoding", "gzip, deflate, sdch");
//		httpGet.setHeader("Accept-Language", "zh-CN,zh;q=0.8");
        try {
            CookieHelper.setCookies(uriStr, httpGet, event.getCookieList());
        } catch (URISyntaxException e) {
            logger.error("set cookie fail {}", event.getId());
        }
    }

    private class HttpAsyncCallback implements FutureCallback<HttpResponse> {

        private TianYiShopSpiderEvent event;
        private boolean skipNextStep = false;

        public HttpAsyncCallback(TianYiShopSpiderEvent event) {
            this.event = event;
        }

        @Override
        public void completed(HttpResponse result) {
            try {
                CookieHelper.getCookiesFresh(event.getCookieList(), result);
                switch (event.getState()) {
                    case NONE:
                        initDown(result);
                        break;
                    case IMG:
                        imgDown(result);
                        break;
                    case CHECK:
                        checkDown(result);
                        break;
                    case MSG:
                        msgDown(result);
                        break;
                    case LOGIN:
                        loginDown(result);
                        break;
                    case ORDER:
                        orderDown(result);
                        break;
                    case MSG_PAY:
                        msgPayDown(result);
                        break;
                    case GET_JF:
                        getJfDown(result);
                        break;
                    case PAY:
                        payDown(result);
                        break;
                    case GET_CODE:
                    	getCodeDown(result);
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
        
        
        private void getCodeDown(HttpResponse result){
        	try {
                String entityStr = EntityUtils.toString(result.getEntity());
                JSONObject entityMap = JSON.parseObject(entityStr);
                if (!"0000".equals(entityMap.get("ErrCode"))) {
                	event.setException(new SpiderException(1117, "获取用户兑换卷失败"));
                    return;
                }
                JSONArray orderList = JSONArray.parseArray(entityMap.get("OrderList").toString());
                JSONObject orderFirst =  JSON.parseObject(orderList.get(0).toString());
                JSONArray dummysList = JSONArray.parseArray(orderFirst.get("DummysList").toString());
                
                String carNo = JSONArray.toJSONString(dummysList);
                event.setException(new SpiderException(0, "兑换卷", carNo));
                
			} catch (Exception e) {
			    logger.error("获取用户兑换卷失败", e);
                event.setException(new SpiderException(1117, "获取用户兑换卷失败"));
                return;
			}
        }
        
        

        /**
		 * 	
		 * @Description: (方法职责详细描述,可空)  
		 * @Title: getJfDown 
		 * @param result
		 * @date 2016年2月16日 上午10:21:07  
		 * @author ws
		*/
		private void getJfDown(HttpResponse result) {
			try {
                String entityStr = EntityUtils.toString(result.getEntity());
                JSONObject entityMap = JSON.parseObject(entityStr);
                if (null == entityMap.get("success")) {
                	event.setException(new SpiderException(1117, "获取用户积分数据失败"));
                    return;
                }

                String isSuccess = entityMap.get("success").toString();
                if ("true".equals(isSuccess)) {
                    
                	Long total = Long.valueOf(event.getPayTotal());
                	
                	Long integral = entityMap.getLong("Integral");
                	Long voucher = entityMap.getLong("Voucher");
                	
                	if(total.compareTo(integral) <= 0){
                		event.setPayIntegral(total.toString());
                		event.setPayVoucher("0");
                		event.setMoney("0");
                		event.setState(TianYiShopSpiderState.PAY);
                	}else if(total.compareTo(integral+voucher) <= 0){
                		event.setPayIntegral(integral.toString());
                		event.setPayVoucher(String.valueOf(total - integral));
                		event.setMoney("0");
                		event.setState(TianYiShopSpiderState.PAY);
                	}else{
                		event.setException(new SpiderException(1109, "用户积分不足"));
                	}
                    return;
                } else {
                    event.setException(new SpiderException(1117, "获取用户积分数据失败"));
                    return;
                }
            } catch (Exception e) {
                logger.error("获取用户积分数据失败", e);
                event.setException(new SpiderException(1117, "获取用户积分数据失败"));
                return;
            }
		}

		/**
		 * 	发送支付短信验证码   此步可不要
		 * @Description: (方法职责详细描述,可空)  
		 * @Title: msgPayDown 
		 * @param result
		 * @date 2016年2月14日 下午4:57:44  
		 * @author ws
		*/
		private void msgPayDown(HttpResponse result) {
			try {
                String entityStr = EntityUtils.toString(result.getEntity());
                //System.out.println(entityStr);
                Map entityMap = JSON.parseObject(entityStr);
                if (null == entityMap.get("ErrCode")) {
                	event.setException(new SpiderException(1098, "短信验证码发送失败"));
                    return;
                }

                String reErrCode = entityMap.get("ErrCode").toString();
                if ("0000".equals(reErrCode)) {
                    String key = String.format(Constant.TIAN_YI_SHOP_IMPORT_KEY, event.getUserid());
                    redis.set(key, JSON.toJSONString(event), 600);

                    event.setState(TianYiShopSpiderState.PAY);//支付
                    event.setException(new SpiderException(0, "短信验证码已发送"));
                    return;
                } else {
                	if (null == entityMap.get("ErrMsg")) {
                        event.setException(new SpiderException(1098, "短信验证码发送失败"));
                        return;
                    } else {
                        event.setException(new SpiderException(1098, entityMap.get("ErrMsg").toString()));
                        return;
                    }
                }
            } catch (Exception e) {
                logger.error("天翼短信验证码发送失败", e);
                event.setException(new SpiderException(1098, "短信验证码发送失败"));
                return;
            }
			
		}

		/**
		 * 支付订单	
		 * @Description: (方法职责详细描述,可空)  
		 * @Title: payDown 
		 * @param result
		 * @date 2016年2月14日 下午4:21:37  
		 * @author ws
		*/
		private void payDown(HttpResponse result) {
			try {
                Map entityMap = JSON.parseObject(EntityUtils.toString(result
                        .getEntity()));
                //ErrCode
                String reErrCode = "";
                if (null != entityMap.get("ErrCode")) {
                    reErrCode = entityMap.get("ErrCode").toString();
                } else {
                    event.setException(new SpiderException(1118, "支付失败"));
                    return;
                }

                if ("0000".equals(reErrCode)) {

            	    String key = String.format(Constant.TIAN_YI_SHOP_IMPORT_KEY, event.getUserid());
                    redis.set(key, JSON.toJSONString(event), 600);
                   
                	event.setException(new SpiderException(0, "支付成功",event.getOrderID()));
                	
                } else {
                    if (null == entityMap.get("ErrMsg")) {
                        event.setException(new SpiderException(1118, "支付失败"));
                        return;
                    } else {
                        event.setException(new SpiderException(1118, entityMap.get("ErrMsg").toString()));
                        return;
                    }
                }
            } catch (Exception e) {
                logger.error("天翼订单支付失败", e);
                event.setException(new SpiderException(1118, "支付失败，天翼系统维护中，请稍后再试"));
                return;
            }
		}

		/**
		 * 	创建订单
		 * @Description: (方法职责详细描述,可空)  
		 * @Title: orderDown 
		 * @param result
		 * @date 2016年2月14日 下午4:13:15  
		 * @author ws
		*/
		private void orderDown(HttpResponse result) {
			try {
                Map entityMap = JSON.parseObject(EntityUtils.toString(result
                        .getEntity()));
                //ErrCode
                String reErrCode = "";
                if (null != entityMap.get("ErrCode")) {
                    reErrCode = entityMap.get("ErrCode").toString();
                } else {
                    event.setException(new SpiderException(1113, "生成订单失败"));
                    return;
                }

                if ("0000".equals(reErrCode)) {
                	String orderID = "";
                	if (null != entityMap.get("OrderID")) {
                		orderID = entityMap.get("OrderID").toString();
                		event.setOrderID(orderID);
                		event.setState(TianYiShopSpiderState.GET_JF);//获取积分支付
                		return;
                    } else {
                        event.setException(new SpiderException(1113, "生成订单失败"));
                        return;
                    }
                } else {
                    if (null == entityMap.get("ErrMsg")) {
                        event.setException(new SpiderException(1113, "生成订单失败"));
                        return;
                    } else {
                        event.setException(new SpiderException(-1, entityMap.get("ErrMsg").toString()));
                        return;
                    }
                }
            } catch (Exception e) {
                logger.error("天翼订单生成失败", e);
                event.setException(new SpiderException(1113, "支付失败，天翼系统维护中，请稍后再试"));
                return;
            }
		}

		/**
         * @param result
         * @Description: (方法职责详细描述, 可空)
         * @Title: initDown
         * @date 2015年11月24日 下午5:07:40
         * @author ws
         */
        private void initDown(HttpResponse result) {
            logger.debug("pre login page down {}", event.getId());
            event.setState(TianYiShopSpiderState.IMG);
        }

        /**
         * @param result
         * @Description: (方法职责详细描述, 可空)
         * @Title: checkDown
         * @date 2015年11月24日 下午5:07:37
         * @author ws
         */
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
		/*		if (appConfig.inDevMode()) {
					byte[] tbytes = Base64.getDecoder().decode(imgStr);
					FileOutputStream fs = new FileOutputStream(
							//appConfig.getUploadPath() + "/" + event.getUserid() + ".jpg");
							"D://tianyi.jpg");
					fs.write(tbytes);
					fs.close();
				}
*/
                String key = String.format(Constant.TIAN_YI_SHOP_IMPORT_KEY, event.getUserid());
                redis.set(key, JSON.toJSONString(event), 600);

                event.setException(new SpiderException(0, "输入验证码", imgStr));
                return;
            } catch (Exception e) {
                logger.error("get img exception", e);
                event.setException(e);
            }

        }

        /**
         * @param result
         * @Description: (方法职责详细描述, 可空)
         * @Title: msgDown
         * @date 2015年11月24日 下午5:07:35
         * @author ws
         */
        private void checkDown(HttpResponse result) {
            try {
                Map entityMap = null;
                try {
                    entityMap = JSON.parseObject(EntityUtils.toString(result
                            .getEntity()));
                } catch (IOException e) {

                    logger.error("IOException", e);
                    event.setException(new SpiderException(1004, "图形验证码验证失败"));
                    return;
                }

                if (null == entityMap.get("success")) {
                    event.setException(new SpiderException(1004, "图形验证码验证失败"));
                    return;
                }

                String reErrCode = entityMap.get("success").toString();
                if ("true".equals(reErrCode)) {

                    event.setState(TianYiShopSpiderState.MSG);
                    return;
                } else {
                    event.setException(new SpiderException(1004, "图形验证码错误"));
                    return;
                }
            } catch (Exception e) {
                logger.error("天翼图片验证码验证异常 Exception", e);
                event.setException(new SpiderException(1004, "图形验证码验证失败"));
                return;
            }
        }

        /**
         * @param result
         * @Description: (方法职责详细描述, 可空)
         * @Title: msgDown
         * @date 2015年11月24日 下午5:39:02
         * @author ws
         */
        private void msgDown(HttpResponse result) {
            try {
                String entityStr = EntityUtils.toString(result.getEntity());
                System.out.println(entityStr);
                Map entityMap = JSON.parseObject(entityStr);
                if (null == entityMap.get("ErrCode")) {
                	event.setException(new SpiderException(1098, "短信验证码发送失败"));
                    return;
                }

                String reErrCode = entityMap.get("ErrCode").toString();
                if ("0000".equals(reErrCode)) {
                    String key = String.format(Constant.TIAN_YI_SHOP_IMPORT_KEY, event.getUserid());
                    redis.set(key, JSON.toJSONString(event), 600);

                    event.setState(TianYiShopSpiderState.LOGIN);
                    event.setException(new SpiderException(0, "短信验证码已发送"));
                    return;
                } else {
                	if (null == entityMap.get("ErrMsg")) {
                        event.setException(new SpiderException(1098, "短信验证码发送失败"));
                        return;
                    } else {
                        event.setException(new SpiderException(1098, entityMap.get("ErrMsg").toString()));
                        return;
                    }
                }
            } catch (Exception e) {
                logger.error("天翼短信验证码发送失败", e);
                event.setException(new SpiderException(1098, "短信验证码发送失败"));
                return;
            }
        }


        /**
         * @param result
         * @Description: (方法职责详细描述, 可空)
         * @Title: loginDown
         * @date 2015年11月24日 下午5:07:33
         * @author ws
         */
        private void loginDown(HttpResponse result) {
            try {
                Map entityMap = JSON.parseObject(EntityUtils.toString(result
                        .getEntity()));
                //ErrCode
                String reErrCode = "";
                if (null != entityMap.get("ErrCode")) {
                    reErrCode = entityMap.get("ErrCode").toString();
                } else {
                    event.setException(new SpiderException(1067, "登录失败,请重试"));
                    return;
                }

                if ("0000".equals(reErrCode)) {
                	//CustID
                	event.setCustID(String.valueOf(entityMap.get("CustID")));
                	//ProvinceID
                	event.setProvinceID(String.valueOf(entityMap.get("ProvinceID")));
//                    event.setState(TianYiShopSpiderState.ORDER);//创建订单
                    event.setState(TianYiShopSpiderState.LOGIN);
                    // 缓存当前事件内容
                    String key = String.format(Constant.TIAN_YI_SHOP_IMPORT_KEY, event.getUserid());
                    redis.set(key, JSON.toJSONString(event), Constant.tian_yi_shop_login_state_expire_time);
                    event.setException(new SpiderException(0, "登录天翼商城成功"));
                    return;
                    //测试  爬取订单兑换码
                	//event.setState(TianYiShopSpiderState.GET_CODE);
                } else {
                    if (null == entityMap.get("ErrMsg")) {
                        event.setException(new SpiderException(1067, "登录失败,请重试"));
                        return;
                    } else {
                        event.setException(new SpiderException(1067, entityMap.get("ErrMsg").toString()));
                        return;
                    }
                }
            } catch (Exception e) {
                logger.error("天翼登录失败", e);
                event.setException(new SpiderException(1067, "登录失败,请重试"));
                return;
            }
        }

        @Override
        public void failed(Exception ex) {

        }

        @Override
        public void cancelled() {

        }
    }


}
