/*
 * Copyright (c) 2015-2020 by caitu99
 * All rights reserved.
 */
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
import com.caitu99.lsp.model.spider.pingan.PingAnOilSpiderEvent;
import com.caitu99.lsp.model.spider.pingan.PingAnOilSpiderState;
import com.caitu99.lsp.spider.SpiderReactor;

/** 
 * 平安油卡兑换
 * 
 * @Description: (类职责详细描述,可空) 
 * @ClassName: PingAnOil 
 * @author ws
 * @date 2016年4月5日 下午12:08:54 
 * @Copyright (c) 2015-2020 by caitu99
 */
@Controller
public class PingAnOil extends BaseController{
	private static final Logger logger = LoggerFactory.getLogger(PingAnOil.class);
	
	@Autowired
	RedisOperate redis;
	
	
	@RequestMapping(value = "/pingan/oil/getimgcode/1.0", produces = "application/json;charset=utf-8")
    @ResponseBody
    public DeferredResult<Object> getImgCode(HttpServletRequest request) {
		logger.info("平安获取图片验证码请求");
		DeferredResult<Object> deferredResult = new DeferredResult<>();
		String userId = request.getParameter("userid");
		if(StringUtils.isBlank(userId)){
			SpiderException exception = new SpiderException(1006, "数据不完整");
            deferredResult.setResult(exception.toString());
            return deferredResult;
		}
		PingAnOilSpiderEvent event = new PingAnOilSpiderEvent(userId, deferredResult);
		event.setState(PingAnOilSpiderState.INIT_SP);
		SpiderReactor.getInstance().process(event);
		return deferredResult;
	}
	
	
	
	@RequestMapping(value = "/pingan/oil/login/1.0", produces = "application/json;charset=utf-8")
    @ResponseBody
    public DeferredResult<Object> xyklogin(HttpServletRequest request) {
		DeferredResult<Object> deferredResult = new DeferredResult<>();
		String userId = request.getParameter("userid");
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
        String key = String.format(Constant.SHOP_PINGAN_TASK_QUEUE, userId);
        String content = redis.getStringByKey(key);
        if (StringUtils.isEmpty(content)) {
            SpiderException exception = new SpiderException(1005, "验证码已过期");
            deferredResult.setResult(exception.toString());
            return deferredResult;
        }
        PingAnOilSpiderEvent event = JSON.parseObject(content, PingAnOilSpiderEvent.class);
		event.setState(PingAnOilSpiderState.LOGIN_SP);
		event.setDeferredResult(deferredResult);
		event.setvCode(imgcode);
		event.setAccount(account);
		event.setPassword(password);
        event.setUserid(userId);
		SpiderReactor.getInstance().process(event);
		return deferredResult;
	}
	

	@RequestMapping(value = "/pingan/oil/payvcode/1.0", produces = "application/json;charset=utf-8")
    @ResponseBody
    public DeferredResult<Object> payVcode(HttpServletRequest request) {
		DeferredResult<Object> deferredResult = new DeferredResult<>();
		String userId = request.getParameter("userid");
		
		if(StringUtils.isBlank(userId)){
			SpiderException exception = new SpiderException(1006, "数据不完整");
            deferredResult.setResult(exception.toString());
            return deferredResult;
		}
		
		// 获取缓存事件内容
        String key = String.format(Constant.SHOP_PINGAN_TASK_QUEUE, userId);
        String content = redis.getStringByKey(key);
        if (StringUtils.isEmpty(content)) {
            SpiderException exception = new SpiderException(1005, "验证码已过期");
            deferredResult.setResult(exception.toString());
            return deferredResult;
        }
        PingAnOilSpiderEvent event = JSON.parseObject(content, PingAnOilSpiderEvent.class);
		event.setState(PingAnOilSpiderState.OIL_PAGE);
		event.setDeferredResult(deferredResult);
        event.setUserid(userId);
		SpiderReactor.getInstance().process(event);
		return deferredResult;
	}
	

	@RequestMapping(value = "/pingan/oil/submitpay/1.0", produces = "application/json;charset=utf-8")
    @ResponseBody
    public DeferredResult<Object> submitPay(HttpServletRequest request) {
		DeferredResult<Object> deferredResult = new DeferredResult<>();
		String userId = request.getParameter("userid");
		String totalPoints = request.getParameter("totalPoints");
		String phoneNum = request.getParameter("phoneNum");
		String vcode = request.getParameter("vcode");
		String productId = request.getParameter("productId");
		
		if(StringUtils.isBlank(userId) || StringUtils.isBlank(totalPoints) 
				|| StringUtils.isBlank(phoneNum) || StringUtils.isBlank(vcode)
				|| StringUtils.isBlank(productId)){
			SpiderException exception = new SpiderException(1006, "数据不完整");
            deferredResult.setResult(exception.toString());
            return deferredResult;
		}
		
		// 获取缓存事件内容
        String key = String.format(Constant.SHOP_PINGAN_TASK_QUEUE, userId);
        String content = redis.getStringByKey(key);
        if (StringUtils.isEmpty(content)) {
            SpiderException exception = new SpiderException(1005, "验证码已过期");
            deferredResult.setResult(exception.toString());
            return deferredResult;
        }
        PingAnOilSpiderEvent event = JSON.parseObject(content, PingAnOilSpiderEvent.class);
		event.setState(PingAnOilSpiderState.OIL_VERIFY);
		event.setDeferredResult(deferredResult);
		event.setTotalPoints(Long.valueOf(totalPoints));
		event.setvCode(vcode);
		event.setPhoneNum(phoneNum);
		event.setProductId(productId);
        event.setUserid(userId);
		SpiderReactor.getInstance().process(event);
		return deferredResult;
	}
	
	@RequestMapping(value = "/pingan/oil/msg/1.0", produces = "application/json;charset=utf-8")
    @ResponseBody
    public DeferredResult<Object> msg(HttpServletRequest request) {
		DeferredResult<Object> deferredResult = new DeferredResult<>();
		String userId = request.getParameter("userid");
		
		if(StringUtils.isBlank(userId)){
			SpiderException exception = new SpiderException(1006, "数据不完整");
            deferredResult.setResult(exception.toString());
            return deferredResult;
		}
		
		// 获取缓存事件内容
        String key = String.format(Constant.SHOP_PINGAN_TASK_QUEUE, userId);
        String content = redis.getStringByKey(key);
        if (StringUtils.isEmpty(content)) {
            SpiderException exception = new SpiderException(1005, "验证码已过期");
            deferredResult.setResult(exception.toString());
            return deferredResult;
        }
        PingAnOilSpiderEvent event = JSON.parseObject(content, PingAnOilSpiderEvent.class);
		event.setState(PingAnOilSpiderState.OIL_MSG);
		event.setDeferredResult(deferredResult);
        event.setUserid(userId);
		SpiderReactor.getInstance().process(event);
		return deferredResult;
	}
	
	
	@RequestMapping(value = "/pingan/oil/dopay/1.0", produces = "application/json;charset=utf-8")
    @ResponseBody
    public DeferredResult<Object> doPay(HttpServletRequest request) {
		DeferredResult<Object> deferredResult = new DeferredResult<>();
		String userId = request.getParameter("userid");
		String msgCode = request.getParameter("msgCode");
		String payPwd = request.getParameter("payPwd");
		
		if(StringUtils.isBlank(userId)){
			SpiderException exception = new SpiderException(1006, "数据不完整");
            deferredResult.setResult(exception.toString());
            return deferredResult;
		}
		
		// 获取缓存事件内容
        String key = String.format(Constant.SHOP_PINGAN_TASK_QUEUE, userId);
        String content = redis.getStringByKey(key);
        if (StringUtils.isEmpty(content)) {
            SpiderException exception = new SpiderException(1005, "验证码已过期");
            deferredResult.setResult(exception.toString());
            return deferredResult;
        }
        PingAnOilSpiderEvent event = JSON.parseObject(content, PingAnOilSpiderEvent.class);
		event.setState(PingAnOilSpiderState.OIL_SUBMIT_PAY);
		event.setDeferredResult(deferredResult);
		event.setPayPwd(payPwd);
		event.setMsgCode(msgCode);
        event.setUserid(userId);
		SpiderReactor.getInstance().process(event);
		return deferredResult;
	}
	
	
	
	
}
