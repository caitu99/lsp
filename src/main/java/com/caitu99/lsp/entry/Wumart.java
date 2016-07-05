/*
 * Copyright (c) 2015-2020 by caitu99
 * All rights reserved.
 */
package com.caitu99.lsp.entry;

import javax.servlet.http.HttpServletRequest;

import com.caitu99.lsp.spider.SpiderReactor;
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
import com.caitu99.lsp.model.spider.wumart.WumartSpiderEvent;
import com.caitu99.lsp.model.spider.wumart.WumartSpiderState;
import com.caitu99.lsp.spider.wumart.WumartSpider;

/** 
 * 
 * @Description: (类职责详细描述,可空) 
 * @ClassName: Wumart 
 * @author fangjunxiao
 * @date 2015年12月11日 下午4:49:44 
 * @Copyright (c) 2015-2020 by caitu99 
 */
@Controller	
public class Wumart extends BaseController{
	
	
    @Autowired
    private RedisOperate redis;
	
	private final static Logger logger = LoggerFactory.getLogger(WumartSpider.class);
	
    /**
     * 	物美获取验证码
     * @Description: (方法职责详细描述,可空)  
     * @Title: getImgcode 
     * @param request
     * @return
     * @date 2015年12月11日 下午5:35:04  
     * @author fangjunxiao
     */
    @RequestMapping(value = "/spider/wumart/imgcode/1.0", produces = "application/json;charset=utf-8")
    @ResponseBody
	public DeferredResult<Object> getImgcode(HttpServletRequest request) {
    	 DeferredResult<Object> deferredResult = new DeferredResult<>();
    	 String userId = request.getParameter("userid");
         if (StringUtils.isEmpty(userId)) {
             SpiderException exception = new SpiderException(1006, "数据不完整");
             deferredResult.setResult(exception.toString());
             return deferredResult;
         }
         
         
        WumartSpiderEvent event = new WumartSpiderEvent(userId, deferredResult, request);
         event.setUserid(userId);
         event.setState(WumartSpiderState.IMGCODE);
         
         SpiderReactor.getInstance().process(event);

         return deferredResult;
    }
    
    
    
    
	
    /**
     * 	物美获取验证码
     * @Description: (方法职责详细描述,可空)  
     * @Title: getImgcode 
     * @param request
     * @return
     * @date 2015年12月11日 下午5:35:04  
     * @author fangjunxiao
     */
    @RequestMapping(value = "/spider/wumart/imgcodefinal/1.0", produces = "application/json;charset=utf-8")
    @ResponseBody
	public DeferredResult<Object> getImgcodefinal(HttpServletRequest request) {
    	 DeferredResult<Object> deferredResult = new DeferredResult<>();
    	 String userId = request.getParameter("userid");
         if (StringUtils.isEmpty(userId)) {
             SpiderException exception = new SpiderException(1006, "数据不完整");
             deferredResult.setResult(exception.toString());
             return deferredResult;
         }
         
         // 获取缓存事件内容
         String key = String.format(Constant.WUMART_IMPORT_KEY, userId);
         String content = redis.getStringByKey(key);
         if (StringUtils.isEmpty(content)) {
             SpiderException exception = new SpiderException(1005, "验证码已过期");
             deferredResult.setResult(exception.toString());
             return deferredResult;
         }
         
         WumartSpiderEvent event = JSON.parseObject(content, WumartSpiderEvent.class);
         event.setDeferredResult(deferredResult);
         event.setUserid(userId);
         event.setState(WumartSpiderState.IMGCODE);
        SpiderReactor.getInstance().process(event);

         return deferredResult;
    }
    
	
	/**
	 * 	登录获取积分
	 * @Description: (方法职责详细描述,可空)  
	 * @Title: login 
	 * @param request
	 * @return
	 * @date 2015年12月11日 下午10:47:13  
	 * @author fangjunxiao
	 */
    @RequestMapping(value = "/spider/wumart/login/1.0", produces = "application/json;charset=utf-8")
    @ResponseBody
	public DeferredResult<Object> login(HttpServletRequest request) {
    	 DeferredResult<Object> deferredResult = new DeferredResult<>();
         String userid = request.getParameter("userid");
         String account = request.getParameter("account");
         String password = request.getParameter("password");
         String yzm = request.getParameter("yzm");
         String province = request.getParameter("province");
         
         if (StringUtils.isEmpty(userid)
         		|| StringUtils.isEmpty(account)
                 || StringUtils.isEmpty(password)
                 || StringUtils.isEmpty(yzm)
                 || StringUtils.isEmpty(province)) {
             SpiderException exception = new SpiderException(1006, "数据不完整");
             deferredResult.setResult(exception.toString());
             return deferredResult;
         }
         
         // 获取缓存事件内容
         String key = String.format(Constant.WUMART_IMPORT_KEY, userid);
         String content = redis.getStringByKey(key);
         if (StringUtils.isEmpty(content)) {
             SpiderException exception = new SpiderException(1005, "验证码已过期");
             deferredResult.setResult(exception.toString());
             return deferredResult;
         }
         
         WumartSpiderEvent event = JSON.parseObject(content, WumartSpiderEvent.class);
         event.setDeferredResult(deferredResult);
         event.setAccount(account);
         event.setPassword(password);
         event.setVcode(yzm);
         event.setUserid(userid);
         event.setProvince(province);
         event.setState(WumartSpiderState.LOGIN); 
         
		 redis.set(key, JSON.toJSONString(event), 300); //300秒超时
        SpiderReactor.getInstance().process(event);
         return deferredResult; 
    }
    
	
    
    
	/**
	 * 	登录获取积分
	 * @Description: (方法职责详细描述,可空)  
	 * @Title: login 
	 * @param request
	 * @return
	 * @date 2015年12月11日 下午10:47:13  
	 * @author fangjunxiao
	 */
    @RequestMapping(value = "/spider/wumart/loginsecond/1.0", produces = "application/json;charset=utf-8")
    @ResponseBody
	public DeferredResult<Object> loginsecond(HttpServletRequest request) {
    	 DeferredResult<Object> deferredResult = new DeferredResult<>();
         String userid = request.getParameter("userid");
         String yzm = request.getParameter("yzm");
         
         
         if (StringUtils.isEmpty(userid)
                 || StringUtils.isEmpty(yzm)) {
             SpiderException exception = new SpiderException(1006, "数据不完整");
             deferredResult.setResult(exception.toString());
             return deferredResult;
         }
         
         // 获取缓存事件内容
         String key = String.format(Constant.WUMART_IMPORT_KEY, userid);
         String content = redis.getStringByKey(key);
         if (StringUtils.isEmpty(content)) {
             SpiderException exception = new SpiderException(1005, "验证码已过期");
             deferredResult.setResult(exception.toString());
             return deferredResult;
         }
         
         WumartSpiderEvent event = JSON.parseObject(content, WumartSpiderEvent.class);
         event.setDeferredResult(deferredResult);
         event.setVcode(yzm);
         event.setUserid(userid);
         event.setState(WumartSpiderState.LOGIN); 
         
		 redis.set(key, JSON.toJSONString(event), 300); //300秒超时

        SpiderReactor.getInstance().process(event);

         return deferredResult; 
    }
    
    
    /**
     * 	校验验证码 
     * @Description: (方法职责详细描述,可空)  
     * @Title: checkImgcoke 
     * @param request
     * @return
     * @date 2015年12月11日 下午10:52:42  
     * @author fangjunxiao
     */
    @RequestMapping(value = "/spider/wumart/check/1.0", produces = "application/json;charset=utf-8")
    @ResponseBody
	public DeferredResult<Object> checkImgcoke(HttpServletRequest request) {
    	 DeferredResult<Object> deferredResult = new DeferredResult<>();
    	 String userid = request.getParameter("userid");
    	 String yzm = request.getParameter("yzm");
    	 
         if (StringUtils.isEmpty(userid) ||
        		 StringUtils.isEmpty(yzm)) {
             SpiderException exception = new SpiderException(1006, "数据不完整");
             deferredResult.setResult(exception.toString());
             return deferredResult;
         }
         
         // 获取缓存事件内容
         String key = String.format(Constant.WUMART_IMPORT_KEY, userid);
         String content = redis.getStringByKey(key);
         if (StringUtils.isEmpty(content)) {
             SpiderException exception = new SpiderException(1005, "验证码已过期");
             deferredResult.setResult(exception.toString());
             return deferredResult;
         }
         
         WumartSpiderEvent event = JSON.parseObject(content, WumartSpiderEvent.class);
         event.setDeferredResult(deferredResult);
         event.setVcode(yzm);
         event.setUserid(userid);
         event.setState(WumartSpiderState.CHECK);

        SpiderReactor.getInstance().process(event);

         return deferredResult; 
    }
    
    
    /**
     * 注册或修改密码
     * @Description: (方法职责详细描述,可空)  
     * @Title: modify 
     * @param request
     * @return
     * @date 2015年12月11日 下午10:59:55  
     * @author fangjunxiao
     */
    @RequestMapping(value = "/spider/wumart/modify/1.0", produces = "application/json;charset=utf-8")
    @ResponseBody
	public DeferredResult<Object> modify(HttpServletRequest request) {
    	 DeferredResult<Object> deferredResult = new DeferredResult<>();
    	 String userid = request.getParameter("userid");
    	 String vcodes = request.getParameter("vcodes");
    	 
         if (StringUtils.isEmpty(userid) ||
        		 StringUtils.isEmpty(vcodes)) {
             SpiderException exception = new SpiderException(1006, "数据不完整");
             deferredResult.setResult(exception.toString());
             return deferredResult;
         }
         
         
         // 获取缓存事件内容
         String key = String.format(Constant.WUMART_IMPORT_KEY, userid);
         String content = redis.getStringByKey(key);
         if (StringUtils.isEmpty(content)) {
             SpiderException exception = new SpiderException(1005, "验证码已过期");
             deferredResult.setResult(exception.toString());
             return deferredResult;
         }
         
         WumartSpiderEvent event = JSON.parseObject(content, WumartSpiderEvent.class);
         event.setDeferredResult(deferredResult);
         event.setUserid(userid);
         event.setVcodes(vcodes);
         event.setState(WumartSpiderState.MODIFY);

        SpiderReactor.getInstance().process(event);

         return deferredResult;
    }
}
