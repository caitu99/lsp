package com.caitu99.lsp.entry;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.async.DeferredResult;

import com.alibaba.fastjson.JSON;
import com.caitu99.lsp.Constant;
import com.caitu99.lsp.cache.RedisOperate;
import com.caitu99.lsp.exception.SpiderException;
import com.caitu99.lsp.model.spider.ccbshop.CCBShopEvent;
import com.caitu99.lsp.model.spider.ccbshop.CCBShopState;
import com.caitu99.lsp.spider.SpiderReactor;
import com.caitu99.lsp.utils.ScriptHelper;

@Controller
public class CCBShop extends BaseController {

	private static final Logger logger = LoggerFactory.getLogger(CCBShop.class);
	
	@Autowired
	RedisOperate redis;
	
	/**
	 * @Description: (注册验证)  
	 * @Title: checkaccount 
	 * @param request
	 * @return
	 * @date 2016年1月28日 下午3:18:23  
	 * @author Hongbo Peng
	 * @throws Exception 
	 */
	@RequestMapping(value = "/api/shop/ccb/register/check/1.0", produces = "application/json;charset=utf-8")
    @ResponseBody
    public DeferredResult<Object> checkRegister(HttpServletRequest request) throws Exception {
		logger.info("建设注册验证请求");
		DeferredResult<Object> deferredResult = new DeferredResult<>();
		String mobile = request.getParameter("mobile");
		String account = request.getParameter("account");
		String password = request.getParameter("password");
		String userId = request.getParameter("userId");
		if(StringUtils.isBlank(mobile) || StringUtils.isBlank(account) || StringUtils.isBlank(password) || StringUtils.isBlank(userId)){
			SpiderException exception = new SpiderException(1006 ,"数据不完整");
            deferredResult.setResult(exception.toString());
            return deferredResult;
		}
		CCBShopEvent event = new CCBShopEvent(userId,deferredResult);
		event.setState(CCBShopState.REGISTER_PAGE);
		event.setAccount(account);
		event.setMobile(mobile);
		event.setPassword(password);
		event.setPasswordEnc(ScriptHelper.encryptCCBPasword(password));
		SpiderReactor.getInstance().process(event);
		return deferredResult;
	}
	
	/**
	 * 提交注册
	 * 	
	 * @Description: (方法职责详细描述,可空)  
	 * @Title: submitRegister 
	 * @param request
	 * @return
	 * @throws Exception
	 * @date 2016年2月1日 下午4:46:19  
	 * @author ws
	 */
	@RequestMapping(value = "/api/shop/ccb/register/submit/1.0", produces = "application/json;charset=utf-8")
    @ResponseBody
    public DeferredResult<Object> submitRegister(HttpServletRequest request) throws Exception {
		logger.info("建设注册请求");
		DeferredResult<Object> deferredResult = new DeferredResult<>();
		String smsCode = request.getParameter("smsCode");
		String userId = request.getParameter("userId");
		if(StringUtils.isBlank(smsCode) || StringUtils.isBlank(userId)){
			SpiderException exception = new SpiderException(1006, "数据不完整");
            deferredResult.setResult(exception.toString());
            return deferredResult;
		}
		// 获取缓存事件内容
        String key = String.format(Constant.CCB_SHOP_BUY_KEY, userId);
        String content = redis.getStringByKey(key);
        if (StringUtils.isEmpty(content)) {
            SpiderException exception = new SpiderException(1005,"验证码已过期");
            deferredResult.setResult(exception.toString());
            return deferredResult;
        }
		CCBShopEvent event = JSON.parseObject(content, CCBShopEvent.class);
		event.setDeferredResult(deferredResult);
		event.setState(CCBShopState.CHECK_SMS_CODE);
		event.setSmsCode(smsCode);
		SpiderReactor.getInstance().process(event);
		return deferredResult;
	}
	
	/**
	 * 获取图片验证码
	 * 	
	 * @Description: (方法职责详细描述,可空)  
	 * @Title: getImgCode 
	 * @param request
	 * @return
	 * @date 2016年2月1日 下午4:46:29  
	 * @author ws
	 */
	@RequestMapping(value = "/api/shop/ccb/getimgcode/1.0", produces = "application/json;charset=utf-8")
    @ResponseBody
    public DeferredResult<Object> getImgCode(HttpServletRequest request) {
		logger.info("建设获取图片验证码请求");
		DeferredResult<Object> deferredResult = new DeferredResult<>();
		String userId = request.getParameter("userId");
		if(StringUtils.isBlank(userId)){
			SpiderException exception = new SpiderException(1006, "数据不完整");
            deferredResult.setResult(exception.toString());
            return deferredResult;
		}
		CCBShopEvent event = new CCBShopEvent(userId, deferredResult);
		event.setState(CCBShopState.LOGIN_PAGE);
		SpiderReactor.getInstance().process(event);
		return deferredResult;
	}
	
	/**
	 * 登录
	 * 	
	 * @Description: (方法职责详细描述,可空)  
	 * @Title: order 
	 * @param request
	 * @return
	 * @date 2016年2月1日 下午4:46:40  
	 * @author ws
	 */
	@RequestMapping(value = "/api/shop/ccb/login/1.0", produces = "application/json;charset=utf-8")
    @ResponseBody
    public DeferredResult<Object> loginshop(HttpServletRequest request) {
		logger.info("建设登录并提交订单请求");
		DeferredResult<Object> deferredResult = new DeferredResult<>();
		String userId = request.getParameter("userId");
		String vcode = request.getParameter("vcode");
		String account = request.getParameter("account");
		String password = request.getParameter("password");
		
		
		if(StringUtils.isBlank(userId) || StringUtils.isBlank(vcode) || 
				StringUtils.isBlank(account) || StringUtils.isBlank(password)){
			SpiderException exception = new SpiderException(1006, "数据不完整");
            deferredResult.setResult(exception.toString());
            return deferredResult;
		}
		
		// 获取缓存事件内容
        String key = String.format(Constant.CCB_SHOP_BUY_KEY, userId);
        String content = redis.getStringByKey(key);
        if (StringUtils.isEmpty(content)) {
            SpiderException exception = new SpiderException(1005, "验证码已过期");
            deferredResult.setResult(exception.toString());
            return deferredResult;
        }
        CCBShopEvent event = JSON.parseObject(content, CCBShopEvent.class);
		event.setState(CCBShopState.IMG_CODE_CHECK);
		event.setDeferredResult(deferredResult);
		event.setvCode(vcode);
		event.setAccount(account);
		event.setPassword(password);
        event.setUserid(userId);
		SpiderReactor.getInstance().process(event);
		return deferredResult;
	}
	
	
	
	/**
	 * 提交订单
	 * 	
	 * @Description: (方法职责详细描述,可空)  
	 * @Title: order 
	 * @param request
	 * @return
	 * @date 2016年2月1日 下午4:46:40  
	 * @author ws
	 */
	@RequestMapping(value = "/api/shop/ccb/order/1.0", produces = "application/json;charset=utf-8")
    @ResponseBody
    public DeferredResult<Object> order(HttpServletRequest request) {
		logger.info("建设登录并提交订单请求");
		DeferredResult<Object> deferredResult = new DeferredResult<>();
		String userId = request.getParameter("userId");
/*		String vcode = request.getParameter("vcode");
		String account = request.getParameter("account");
		String password = request.getParameter("password");*/
		String productId = request.getParameter("productId");
		String orderType = request.getParameter("orderType");
		String quantity = request.getParameter("quantity");
		String shopId = request.getParameter("shopId");
		String productPrice = request.getParameter("productPrice");
		String leftMessage = request.getParameter("leftMessage");
		String mobile = request.getParameter("mobile");
		
		String provinceName = "";
		String cityName = "";
		String areaName = "";
		String consigneeName = "";
		String addressDetail = "";
		String postCode = "";
		
		if(StringUtils.isBlank(userId)){
			SpiderException exception = new SpiderException(1006, "数据不完整");
            deferredResult.setResult(exception.toString());
            return deferredResult;
		}
		
		// 获取缓存事件内容
        String key = String.format(Constant.CCB_SHOP_BUY_KEY, userId);
        String content = redis.getStringByKey(key);
        if (StringUtils.isEmpty(content)) {
            SpiderException exception = new SpiderException(1005, "登录超时");
            deferredResult.setResult(exception.toString());
            return deferredResult;
        }
        CCBShopEvent event = JSON.parseObject(content, CCBShopEvent.class);
        if(!"1".equals(event.getLoginType())){
			SpiderException exception = new SpiderException(1005, "登录超时");
            deferredResult.setResult(exception.toString());
            return deferredResult;
        }
        
		if(orderType.equals("1")){//实物订单，需要收货地址信息
			provinceName = request.getParameter("provinceName");
			cityName = request.getParameter("cityName");
			areaName = request.getParameter("areaName");
			consigneeName = request.getParameter("consigneeName");
			addressDetail = request.getParameter("addressDetail");
			postCode = request.getParameter("postCode");
			event.setState(CCBShopState.GET_PROVINCE_GB_ADDRESS_JSON);
		}else{
		    event.setState(CCBShopState.SUBMIT_ORDER_DETAIL);
		}
	
		event.setDeferredResult(deferredResult);
        event.setUserid(userId);
        event.setProductId(productId);
        event.setOrderType(orderType);
        event.setQuantity(quantity);
        event.setShopId(shopId);
        event.setProductPrice(productPrice);
        event.setLeftMessage(leftMessage);
        event.setProvince(provinceName);
        event.setCity(cityName);
        event.setDistinct(areaName);
        event.setConsigneeName(consigneeName);
        event.setAddressDetail(addressDetail);
        event.setPostCode(postCode);
        event.setMobile(mobile);
		SpiderReactor.getInstance().process(event);
		return deferredResult;
	}
	
	/**
	 * 发送支付短信验证码
	 * 	
	 * @Description: (方法职责详细描述,可空)  
	 * @Title: msgSend 
	 * @param request
	 * @return
	 * @date 2016年2月1日 下午4:47:02  
	 * @author ws
	 */
	@RequestMapping(value = "/api/shop/ccb/msgsend/1.0", produces = "application/json;charset=utf-8")
    @ResponseBody
    public DeferredResult<Object> msgSend(HttpServletRequest request) {
		logger.info("建设发送支付短信验证码请求");
		DeferredResult<Object> deferredResult = new DeferredResult<>();
		String userId = request.getParameter("userId");
		String cardNumber = request.getParameter("cardNumber");
		String cardMobile = request.getParameter("cardMobile");
		
		if(StringUtils.isBlank(userId)){
			SpiderException exception = new SpiderException(1006, "数据不完整");
            deferredResult.setResult(exception.toString());
            return deferredResult;
		}
		
		// 获取缓存事件内容
        String key = String.format(Constant.CCB_SHOP_BUY_KEY, userId);
        String content = redis.getStringByKey(key);
        if (StringUtils.isEmpty(content)) {
            SpiderException exception = new SpiderException(1005, "验证码已过期");
            deferredResult.setResult(exception.toString());
            return deferredResult;
        }
        CCBShopEvent event = JSON.parseObject(content, CCBShopEvent.class);
		event.setState(CCBShopState.EPAY_MAIN_PLATGATE);
		event.setDeferredResult(deferredResult);
		event.setCardMobile(cardMobile);
		event.setCardNumber(cardNumber);
        event.setUserid(userId);
		SpiderReactor.getInstance().process(event);
		return deferredResult; 
	}
	
	/**
	 * 支付
	 * @Description: (方法职责详细描述,可空)  
	 * @Title: doPay 
	 * @param request
	 * @return
	 * @date 2016年2月1日 下午4:47:30  
	 * @author ws
	 */
	@RequestMapping(value = "/api/shop/ccb/dopay/1.0", produces = "application/json;charset=utf-8")
    @ResponseBody
    public DeferredResult<Object> doPay(HttpServletRequest request) {
		logger.info("建设支付请求");
		DeferredResult<Object> deferredResult = new DeferredResult<>();
		String userId = request.getParameter("userId");
		String smsCode = request.getParameter("smsCode");
		
		if(StringUtils.isBlank(userId)){
			SpiderException exception = new SpiderException(1006, "数据不完整");
            deferredResult.setResult(exception.toString());
            return deferredResult;
		}
		
		// 获取缓存事件内容
        String key = String.format(Constant.CCB_SHOP_BUY_KEY, userId);
        String content = redis.getStringByKey(key);
        if (StringUtils.isEmpty(content)) {
            SpiderException exception = new SpiderException(1005, "验证码已过期");
            deferredResult.setResult(exception.toString());
            return deferredResult;
        }
        CCBShopEvent event = JSON.parseObject(content, CCBShopEvent.class);
		event.setState(CCBShopState.EPAY_MAIN_B1L1);
		event.setDeferredResult(deferredResult);
		event.setSmsCode(smsCode);
        event.setUserid(userId);
		SpiderReactor.getInstance().process(event);
		return deferredResult;
	}
	
	
	
	@RequestMapping(value = "/api/jf/ccb/sms/1.0", produces = "application/json;charset=utf-8")
    @ResponseBody
    public DeferredResult<Object> sms(HttpServletRequest request) {
		logger.info("建设获取短信验证码请求");
		DeferredResult<Object> deferredResult = new DeferredResult<>();
		//String accoumt = request.getParameter("account");
		
		String aa = request.getParameter("account");
		
		String mobilelastNum = request.getParameter("password");
		String userid = request.getParameter("userId");
		
		if(StringUtils.isBlank(aa) || StringUtils.isBlank(mobilelastNum) 
				|| StringUtils.isBlank(userid)){
			SpiderException exception = new SpiderException(1006, "数据不完整");
            deferredResult.setResult(exception.toString());
            return deferredResult;
		}
		
		String accoumt = "";
		for (int i = 0; i < 4; i++) {
			int x = i*4;
			int y = (i+1)*4;
			accoumt += aa.substring(x,y);
			if(i!=3){
				accoumt+=" ";	
			}
		}
		
		CCBShopEvent event = new CCBShopEvent(userid, deferredResult);
		event.setState(CCBShopState.JF_INIT);
		event.setAccount(accoumt);
		event.setPassword(mobilelastNum);
		
		SpiderReactor.getInstance().process(event);
		return deferredResult;
	}
	
	
	@RequestMapping(value = "/api/jf/ccb/login/1.0", produces = "application/json;charset=utf-8")
    @ResponseBody
    public DeferredResult<Object> login(HttpServletRequest request) {
		DeferredResult<Object> deferredResult = new DeferredResult<>();
		String userId = request.getParameter("userId");
		String smsCode = request.getParameter("smsCode");
		
		if(StringUtils.isBlank(userId) || StringUtils.isBlank(smsCode)){
			SpiderException exception = new SpiderException(1006, "数据不完整");
            deferredResult.setResult(exception.toString());
            return deferredResult;
		}
		
		// 获取缓存事件内容
        String key = String.format(Constant.CCB_JF_SMS_KEY, userId);
        String content = redis.getStringByKey(key);
        if (StringUtils.isEmpty(content)) {
            SpiderException exception = new SpiderException(1005, "验证码已过期");
            deferredResult.setResult(exception.toString());
            return deferredResult;
        }
        CCBShopEvent event = JSON.parseObject(content, CCBShopEvent.class);
		event.setState(CCBShopState.JF_LOGIN);
		event.setDeferredResult(deferredResult);
		event.setSmsCode(smsCode);
        event.setUserid(userId);
		SpiderReactor.getInstance().process(event);
		return deferredResult;
	}
	
	/**
	 * 	获取建设信用卡中心图片验证码
	 * @Description: (方法职责详细描述,可空)  
	 * @Title: getxykimg 
	 * @param request
	 * @return
	 * @date 2016年3月18日 下午10:31:14  
	 * @author 
	 */
	@RequestMapping(value = "/api/shop/ccb/xykimg/1.0", produces = "application/json;charset=utf-8")
    @ResponseBody
    public DeferredResult<Object> getxykimg(HttpServletRequest request) {
		logger.info("建设获取图片验证码请求");
		DeferredResult<Object> deferredResult = new DeferredResult<>();
		String userId = request.getParameter("userId");
		if(StringUtils.isBlank(userId)){
			SpiderException exception = new SpiderException(1006, "数据不完整");
            deferredResult.setResult(exception.toString());
            return deferredResult;
		}
		CCBShopEvent event = new CCBShopEvent(userId, deferredResult);
		event.setState(CCBShopState.XYK_INIT);
		SpiderReactor.getInstance().process(event);
		return deferredResult;
	}
	
	
	/**
	 * 	登录并获取积分
	 * @Description: (方法职责详细描述,可空)  
	 * @Title: xyklogin 
	 * @param request
	 * @return
	 * @date 2016年3月18日 下午10:31:44  
	 * @author 
	 */
	@RequestMapping(value = "/api/shop/ccb/xyklogin/1.0", produces = "application/json;charset=utf-8")
    @ResponseBody
    public DeferredResult<Object> xyklogin(HttpServletRequest request) {
		DeferredResult<Object> deferredResult = new DeferredResult<>();
		String userId = request.getParameter("userId");
		String account = request.getParameter("account");
		String password = request.getParameter("password");
		String imgcode = request.getParameter("imgcode");
		
		if(StringUtils.isBlank(userId) || StringUtils.isBlank(imgcode) ||
			StringUtils.isBlank(account) || StringUtils.isBlank(password)){
			SpiderException exception = new SpiderException(1006, "数据不完整");
            deferredResult.setResult(exception.toString());
            return deferredResult;
		}
		
		// 获取缓存事件内容
        String key = String.format(Constant.CCB_JF_XYK_KEY, userId);
        String content = redis.getStringByKey(key);
        if (StringUtils.isEmpty(content)) {
            SpiderException exception = new SpiderException(1005, "验证码已过期");
            deferredResult.setResult(exception.toString());
            return deferredResult;
        }
        CCBShopEvent event = JSON.parseObject(content, CCBShopEvent.class);
		event.setState(CCBShopState.XYK_LOGIN);
		event.setDeferredResult(deferredResult);
		event.setImgcode(imgcode);
		event.setAccount(account);
		event.setPassword(password);
        event.setUserid(userId);
		SpiderReactor.getInstance().process(event);
		return deferredResult;
	}
	
	
	

	/**
	 * 	获取建设信用卡积分
	 * @Description: (方法职责详细描述,可空)  
	 * @Title: getxykimg 
	 * @param request
	 * @return
	 * @date 2016年3月18日 下午10:31:14  
	 * @author 
	 */
	@RequestMapping(value = "/api/shop/ccb/getJf/1.0", produces = "application/json;charset=utf-8")
    @ResponseBody
    public DeferredResult<Object> getJf(HttpServletRequest request) {
		logger.info("获取建设信用卡积分请求");
		DeferredResult<Object> deferredResult = new DeferredResult<>();
		String userId = request.getParameter("userId");
		String account = request.getParameter("account");
		String password = request.getParameter("password");
		if(StringUtils.isBlank(userId) || StringUtils.isBlank(account) 
			|| StringUtils.isBlank(password)){
				SpiderException exception = new SpiderException(1006, "数据不完整");
	            deferredResult.setResult(exception.toString());
	            return deferredResult;
			}
		CCBShopEvent event = new CCBShopEvent(userId, deferredResult);
		event.setState(CCBShopState.XYK_INIT);
		event.setDeferredResult(deferredResult);
		event.setAccount(account);
		event.setPassword(password);
        event.setUserid(userId);
		SpiderReactor.getInstance().process(event);
		return deferredResult;
	}
	
	
}
