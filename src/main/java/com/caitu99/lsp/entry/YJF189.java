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
import com.caitu99.lsp.model.spider.tianyi.YJF189SpiderEvent;
import com.caitu99.lsp.model.spider.tianyi.YJF189SpiderState;
import com.caitu99.lsp.spider.SpiderReactor;

/**
 * 天翼积分抓取
 * 
 * @Description: (类职责详细描述,可空) 
 * @ClassName: YJF189 
 * @author ws
 * @date 2016年3月10日 上午9:55:26 
 * @Copyright (c) 2015-2020 by caitu99
 */
@Controller
public class YJF189 {

    private static final Logger logger = LoggerFactory
            .getLogger(YJF189.class);
    @Autowired
    private RedisOperate redis;

    

    /**
     * 非电信用户登录图形验证码获取
     * 	
     * @Description: (方法职责详细描述,可空)  
     * @Title: getImgCode 
     * @param request
     * @return
     * @date 2016年3月10日 上午10:00:16  
     * @author ws
     */
    @RequestMapping(value = "/yjf189/img/1.0", produces = "application/json;charset=utf-8")
    @ResponseBody
    public DeferredResult<Object> getImgCode(HttpServletRequest request) {

        DeferredResult<Object> deferredResult = new DeferredResult<>();
        String userId = request.getParameter("userid");

        if (StringUtils.isEmpty(userId)) {
            SpiderException exception = new SpiderException(1006, "数据不完整");
            deferredResult.setResult(exception.toString());
            return deferredResult;
        }

        YJF189SpiderEvent event = new YJF189SpiderEvent(userId, deferredResult);
        event.setUserid(userId);
        event.setState(YJF189SpiderState.NONE);//首先进入首页，获取cookie

        SpiderReactor.getInstance().process(event);

        return deferredResult;
    }

    
    /**
     * 电信用户登录
     * 	
     * @Description: (方法职责详细描述,可空)  
     * @Title: login 
     * @param request
     * @return
     * @date 2016年3月10日 上午10:01:11  
     * @author ws
     */
    @RequestMapping(value = "/yjf189/login/1.0", produces = "application/json;charset=utf-8")
    @ResponseBody
    public DeferredResult<Object> login(HttpServletRequest request) {
    	DeferredResult<Object> deferredResult = new DeferredResult<>();
        String userid = request.getParameter("userid");
        String account = request.getParameter("account");
        String password = request.getParameter("password");
        String vcode = request.getParameter("vcode");

        if (StringUtils.isEmpty(userid)
                || StringUtils.isEmpty(account)
                || StringUtils.isEmpty(password)
                || StringUtils.isEmpty(vcode)) {
            SpiderException exception = new SpiderException(1006, "数据不完整");
            deferredResult.setResult(exception.toString());
            return deferredResult;
        }

        // 获取缓存事件内容
        String key = String.format(Constant.YJF_189_IMPORT_KEY, userid);
        String content = redis.getStringByKey(key);
        if (StringUtils.isEmpty(content)) {
            SpiderException exception = new SpiderException(1005, "验证码已过期");
            deferredResult.setResult(exception.toString());
            return deferredResult;
        }

        YJF189SpiderEvent event = JSON.parseObject(content, YJF189SpiderEvent.class);
        event.setDeferredResult(deferredResult);
        event.setAccount(account);
        event.setvCode(vcode);
        event.setPassword(password);
        event.setUserid(userid);
        event.setState(YJF189SpiderState.USERINFO); //

        SpiderReactor.getInstance().process(event);

        return deferredResult;
    }
    
    
}
