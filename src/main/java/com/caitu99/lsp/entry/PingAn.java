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
import com.caitu99.lsp.model.spider.pingan.PingAnSpiderEvent;
import com.caitu99.lsp.model.spider.pingan.PingAnSpiderState;
import com.caitu99.lsp.spider.SpiderReactor;

/** 
 * 
 * @Description: (类职责详细描述,可空) 
 * @ClassName: PingAn 
 * @author fangjunxiao
 * @date 2016年3月30日 上午11:11:12 
 * @Copyright (c) 2015-2020 by caitu99 
 */
@Controller
public class PingAn extends BaseController{
	private static final Logger logger = LoggerFactory.getLogger(PingAn.class);
	
	@Autowired
	RedisOperate redis;
	
	
	@RequestMapping(value = "/api/shop/pn/getimgcode/1.0", produces = "application/json;charset=utf-8")
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
		PingAnSpiderEvent event = new PingAnSpiderEvent(userId, deferredResult);
		event.setState(PingAnSpiderState.INIT_SP);
		SpiderReactor.getInstance().process(event);
		return deferredResult;
	}
	
	
	
	@RequestMapping(value = "/api/shop/pn/login/1.0", produces = "application/json;charset=utf-8")
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
        PingAnSpiderEvent event = JSON.parseObject(content, PingAnSpiderEvent.class);
		event.setState(PingAnSpiderState.LOGIN_SP);
		event.setDeferredResult(deferredResult);
		event.setvCode(imgcode);
		event.setAccount(account);
		event.setPassword(password);
        event.setUserid(userId);
		SpiderReactor.getInstance().process(event);
		return deferredResult;
	}
	
	
	
	
	@RequestMapping(value = "/api/shop/pn/order/1.0", produces = "application/json;charset=utf-8")
    @ResponseBody
    public DeferredResult<Object> xykorder(HttpServletRequest request) {
		DeferredResult<Object> deferredResult = new DeferredResult<>();
		String userId = request.getParameter("userid");
		
		String goodsId = request.getParameter("goodsId");
		String repositoryId = request.getParameter("repositoryId");
		String orderType = request.getParameter("orderType");

		String province = request.getParameter("province");
		String city = request.getParameter("city");
		String district = request.getParameter("district");
		String address = request.getParameter("address");
		String name = request.getParameter("name");
		String cellphone = request.getParameter("cellphone");
		

		
		if(StringUtils.isBlank(userId)){
			SpiderException exception = new SpiderException(1006, "数据不完整");
            deferredResult.setResult(exception.toString());
            return deferredResult;
		}
		
		// 获取缓存事件内容
        String key = String.format(Constant.SHOP_PINGAN_LOGIN_KEY, userId);
        String content = redis.getStringByKey(key);
        if (StringUtils.isEmpty(content)) {
            SpiderException exception = new SpiderException(1005, "验证码已过期");
            deferredResult.setResult(exception.toString());
            return deferredResult;
        }
        PingAnSpiderEvent event = JSON.parseObject(content, PingAnSpiderEvent.class);
		event.setState(PingAnSpiderState.GOOD_INIT_SP);
		event.setDeferredResult(deferredResult);
        event.setUserid(userId);
        event.setGoodsId(goodsId);
        event.setRepositoryId(repositoryId);
        event.setOrderType(orderType);
        event.setProvince(province);
        event.setCity(city);
        event.setDistrict(district);
        event.setAddress(address);
        event.setName(name);
        event.setCellphone(cellphone);
		SpiderReactor.getInstance().process(event);
		return deferredResult;
	}
	
	
	
	@RequestMapping(value = "/api/shop/pn/sms/1.0", produces = "application/json;charset=utf-8")
    @ResponseBody
    public DeferredResult<Object> sms(HttpServletRequest request) {
		DeferredResult<Object> deferredResult = new DeferredResult<>();
		String userId = request.getParameter("userid");
		
		if(StringUtils.isBlank(userId)){
			SpiderException exception = new SpiderException(1006, "数据不完整");
            deferredResult.setResult(exception.toString());
            return deferredResult;
		}
		
		// 获取缓存事件内容
        String key = String.format(Constant.SHOP_PINGAN_LOGIN_KEY, userId);
        String content = redis.getStringByKey(key);
        if (StringUtils.isEmpty(content)) {
            SpiderException exception = new SpiderException(1005, "验证码已过期");
            deferredResult.setResult(exception.toString());
            return deferredResult;
        }
        PingAnSpiderEvent event = JSON.parseObject(content, PingAnSpiderEvent.class);
		event.setState(PingAnSpiderState.SMS_CODE_SP);
		event.setDeferredResult(deferredResult);
        event.setUserid(userId);
		SpiderReactor.getInstance().process(event);
		return deferredResult;
	}
	
	
	
	@RequestMapping(value = "/api/shop/pn/check/1.0", produces = "application/json;charset=utf-8")
    @ResponseBody
    public DeferredResult<Object> checksms(HttpServletRequest request) {
		DeferredResult<Object> deferredResult = new DeferredResult<>();
		String userId = request.getParameter("userid");
		String smsCode = request.getParameter("smsCode");
		String payPassWord = request.getParameter("payPassWord");
		if(StringUtils.isBlank(userId)){
			SpiderException exception = new SpiderException(1006, "数据不完整");
            deferredResult.setResult(exception.toString());
            return deferredResult;
		}
		
		// 获取缓存事件内容
        String key = String.format(Constant.SHOP_PINGAN_LOGIN_KEY, userId);
        String content = redis.getStringByKey(key);
        if (StringUtils.isEmpty(content)) {
            SpiderException exception = new SpiderException(1005, "验证码已过期");
            deferredResult.setResult(exception.toString());
            return deferredResult;
        }
        PingAnSpiderEvent event = JSON.parseObject(content, PingAnSpiderEvent.class);
		event.setState(PingAnSpiderState.CONTINUE_PAY_SP);
		event.setDeferredResult(deferredResult);
		event.setPayPassWord(payPassWord);
        event.setUserid(userId);
        event.setSmsCode(smsCode);
		SpiderReactor.getInstance().process(event);
		return deferredResult;
	}
	
	
	
	
	
	
	
	
	
	
	
	@RequestMapping(value = "/api/passwd/pn/imgcode/1.0", produces = "application/json;charset=utf-8")
    @ResponseBody
    public DeferredResult<Object> imgcode(HttpServletRequest request) {
		logger.info("平安获取图片验证码请求");
		DeferredResult<Object> deferredResult = new DeferredResult<>();
		String userId = request.getParameter("userid");
		if(StringUtils.isBlank(userId)){
			SpiderException exception = new SpiderException(1006, "数据不完整");
            deferredResult.setResult(exception.toString());
            return deferredResult;
		}
		PingAnSpiderEvent event = new PingAnSpiderEvent(userId, deferredResult);
		event.setState(PingAnSpiderState.VCODEWEB);
		SpiderReactor.getInstance().process(event);
		return deferredResult;
	}
	
	
	
	
	@RequestMapping(value = "/api/passwd/pn/regvcode/1.0", produces = "application/json;charset=utf-8")
    @ResponseBody
    public DeferredResult<Object> regvcode(HttpServletRequest request) {
		logger.info("平安获取图片验证码请求");
		DeferredResult<Object> deferredResult = new DeferredResult<>();
		String userId = request.getParameter("userid");
		String vcode = request.getParameter("imgcode");
		String account = request.getParameter("account");
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
        PingAnSpiderEvent event = JSON.parseObject(content, PingAnSpiderEvent.class);
		event.setState(PingAnSpiderState.VALIDATE_VCODE);
		event.setDeferredResult(deferredResult);
        event.setUserid(userId);
        event.setAccount(account);
        event.setvCode(vcode);
		SpiderReactor.getInstance().process(event);
		return deferredResult;
	}
	
	
	
	@RequestMapping(value = "/api/passwd/pn/thirvcode/1.0", produces = "application/json;charset=utf-8")
    @ResponseBody
    public DeferredResult<Object> thirvcode(HttpServletRequest request) {
		logger.info("平安获取图片验证码请求");
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
        PingAnSpiderEvent event = JSON.parseObject(content, PingAnSpiderEvent.class);
		event.setState(PingAnSpiderState.REGVCODE);
		event.setDeferredResult(deferredResult);
        event.setUserid(userId);
		SpiderReactor.getInstance().process(event);
		return deferredResult;
	}
	
	
	
	@RequestMapping(value = "/api/passwd/pn/sendcode/1.0", produces = "application/json;charset=utf-8")
    @ResponseBody
    public DeferredResult<Object> sendode(HttpServletRequest request) {
		logger.info("平安获取短信验证码请求");
		DeferredResult<Object> deferredResult = new DeferredResult<>();
		String userId = request.getParameter("userid");
		String vcode = request.getParameter("imgcode");
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
        PingAnSpiderEvent event = JSON.parseObject(content, PingAnSpiderEvent.class);
		event.setState(PingAnSpiderState.SEND_CODE);
		event.setDeferredResult(deferredResult);
        event.setUserid(userId);
        event.setvCode(vcode);
		SpiderReactor.getInstance().process(event);
		return deferredResult;
	}
	
	
	
	@RequestMapping(value = "/api/passwd/pn/checkcode/1.0", produces = "application/json;charset=utf-8")
    @ResponseBody
    public DeferredResult<Object> checkcode(HttpServletRequest request) {
		logger.info("平安验证短信验证码请求");
		DeferredResult<Object> deferredResult = new DeferredResult<>();
		String userId = request.getParameter("userid");
		String code = request.getParameter("code");
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
        PingAnSpiderEvent event = JSON.parseObject(content, PingAnSpiderEvent.class);
		event.setState(PingAnSpiderState.CHECK_CODE);
		event.setDeferredResult(deferredResult);
        event.setUserid(userId);
        event.setSmsCode(code);
		SpiderReactor.getInstance().process(event);
		return deferredResult;
	}
	
	
	
	@RequestMapping(value = "/api/passwd/pn/update/1.0", produces = "application/json;charset=utf-8")
    @ResponseBody
    public DeferredResult<Object> update(HttpServletRequest request) {
		logger.info("平安修改密码请求");
		DeferredResult<Object> deferredResult = new DeferredResult<>();
		String userId = request.getParameter("userid");
		String password = request.getParameter("password");
		if(StringUtils.isBlank(userId)){
			SpiderException exception = new SpiderException(1006, "数据不完整");
            deferredResult.setResult(exception.toString());
            return deferredResult;
		}
		
		// 获取缓存事件内容d
        String key = String.format(Constant.SHOP_PINGAN_TASK_QUEUE, userId);
        String content = redis.getStringByKey(key);
        if (StringUtils.isEmpty(content)) {
            SpiderException exception = new SpiderException(1005, "验证码已过期");
            deferredResult.setResult(exception.toString());
            return deferredResult;
        }
        PingAnSpiderEvent event = JSON.parseObject(content, PingAnSpiderEvent.class);
		event.setState(PingAnSpiderState.RESET_THREE);
		event.setDeferredResult(deferredResult);
        event.setUserid(userId);
        event.setPassword(password);
		SpiderReactor.getInstance().process(event);
		return deferredResult;
	}
	
}
