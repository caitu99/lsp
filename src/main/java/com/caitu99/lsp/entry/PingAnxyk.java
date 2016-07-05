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
import com.caitu99.lsp.model.spider.pingan.PingAnxykSpiderEvent;
import com.caitu99.lsp.model.spider.pingan.PingAnxykSpiderState;
import com.caitu99.lsp.spider.SpiderReactor;

/** 
 * 
 * @Description: (类职责详细描述,可空) 
 * @ClassName: PingAnxyk 
 * @author fangjunxiao
 * @date 2016年4月12日 上午10:06:14 
 * @Copyright (c) 2015-2020 by caitu99 
 */
@Controller
public class PingAnxyk extends BaseController{
	
	private static final Logger logger = LoggerFactory.getLogger(PingAn.class);
	
	@Autowired
	RedisOperate redis;
	
	
	@RequestMapping(value = "/api/xyk/pn/getimgcode/1.0", produces = "application/json;charset=utf-8")
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
		PingAnxykSpiderEvent event = new PingAnxykSpiderEvent(userId, deferredResult);
		event.setState(PingAnxykSpiderState.INIT_XYK);
		SpiderReactor.getInstance().process(event);
		return deferredResult;
	}
	
	
	
	@RequestMapping(value = "/api/xyk/pn/login/1.0", produces = "application/json;charset=utf-8")
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
        String key = String.format(Constant.XYK_PINGAN_LOGIN_KEY, userId);
        String content = redis.getStringByKey(key);
        if (StringUtils.isEmpty(content)) {
            SpiderException exception = new SpiderException(1005, "验证码已过期");
            deferredResult.setResult(exception.toString());
            return deferredResult;
        }
        PingAnxykSpiderEvent event = JSON.parseObject(content, PingAnxykSpiderEvent.class);
		event.setState(PingAnxykSpiderState.LOGIN_XYK);
		event.setDeferredResult(deferredResult);
		event.setvCode(imgcode);
		event.setAccount(account);
		event.setPassword(password);
        event.setUserid(userId);
		SpiderReactor.getInstance().process(event);
		return deferredResult;
	}
	

}
