/*
 * Copyright (c) 2015-2020 by caitu99
 * All rights reserved.
 */
package com.caitu99.lsp.entry;

import com.alibaba.fastjson.JSON;
import com.caitu99.lsp.Constant;
import com.caitu99.lsp.cache.RedisOperate;
import com.caitu99.lsp.exception.SpiderException;
import com.caitu99.lsp.model.spider.tianyi.TianYiSpiderEvent;
import com.caitu99.lsp.model.spider.tianyi.TianYiSpiderState;
import com.caitu99.lsp.spider.SpiderReactor;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.async.DeferredResult;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * 天翼积分获取类
 *
 * @author chencheng
 * @Description: (类职责详细描述, 可空)
 * @ClassName: TyClub
 * @date 2015年11月9日 下午7:23:28
 * @Copyright (c) 2015-2020 by caitu99
 */
@Controller
public class TianYiClub {

    private static final Logger logger = LoggerFactory
            .getLogger(TianYiClub.class);
    @Autowired
    private RedisOperate redis;


    /**
     * 获取图片验证码
     *
     * @return Map:code,message,data
     * @throws IOException
     * @Description: (方法职责详细描述, 可空)
     * @Title: getImgCode
     * @date 2015年11月10日 上午11:46:33
     * @author chencheng
     */
    @RequestMapping(value = "/imgcode/get/1.0", produces = "application/json;charset=utf-8")
    @ResponseBody
    public DeferredResult<Object> getImgCode(HttpServletRequest request) {

        DeferredResult<Object> deferredResult = new DeferredResult<>();
        String userId = request.getParameter("userid");

        if (StringUtils.isEmpty(userId)) {
            SpiderException exception = new SpiderException(1006, "数据不完整");
            deferredResult.setResult(exception.toString());
            return deferredResult;
        }

        TianYiSpiderEvent event = new TianYiSpiderEvent(userId, deferredResult);
        event.setUserid(userId);
        event.setState(TianYiSpiderState.NONE);//首先进入首页，获取cookie

        SpiderReactor.getInstance().process(event);

        return deferredResult;
    }

    @RequestMapping(value = "/imgcode/check/1.0", produces = "application/json;charset=utf-8")
    @ResponseBody
    public DeferredResult<Object> checkImgCode(HttpServletRequest request) {
        DeferredResult<Object> deferredResult = new DeferredResult<>();
        String userid = request.getParameter("userid");
        String account = request.getParameter("phoneNo");
        String yzm = request.getParameter("imgCode");

        if (StringUtils.isEmpty(userid)
                || StringUtils.isEmpty(account)
                || StringUtils.isEmpty(yzm)) {
            SpiderException exception = new SpiderException(1006, "数据不完整");
            deferredResult.setResult(exception.toString());
            return deferredResult;
        }

        // 获取缓存事件内容
        String key = String.format(Constant.TIAN_YI_IMPORT_KEY, userid);
        String content = redis.getStringByKey(key);
        if (StringUtils.isEmpty(content)) {
            SpiderException exception = new SpiderException(1005, "验证码已过期");
            deferredResult.setResult(exception.toString());
            return deferredResult;
        }

        TianYiSpiderEvent event = JSON.parseObject(content, TianYiSpiderEvent.class);
        event.setDeferredResult(deferredResult);
        event.setAccount(account);
        event.setvCode(yzm);
        event.setUserid(userid);
        event.setState(TianYiSpiderState.CHECK); // 接下来获取密钥，登录获取积分

        SpiderReactor.getInstance().process(event);

        return deferredResult;
    }

    @RequestMapping(value = "/login/1.0", produces = "application/json;charset=utf-8")
    @ResponseBody
    public DeferredResult<Object> login(HttpServletRequest request) {
        DeferredResult<Object> deferredResult = new DeferredResult<>();
        String userid = request.getParameter("userid");
        String account = request.getParameter("phoneNo");
        String msgCode = request.getParameter("msCode");

        if (StringUtils.isEmpty(userid)
                || StringUtils.isEmpty(account)
                || StringUtils.isEmpty(msgCode)) {
            SpiderException exception = new SpiderException(1006, "数据不完整");
            deferredResult.setResult(exception.toString());
            return deferredResult;
        }

        // 获取缓存事件内容
        String key = String.format(Constant.TIAN_YI_IMPORT_KEY, userid);
        String content = redis.getStringByKey(key);
        if (StringUtils.isEmpty(content)) {
            SpiderException exception = new SpiderException(1005, "验证码已过期");
            deferredResult.setResult(exception.toString());
            return deferredResult;
        }

        TianYiSpiderEvent event = JSON.parseObject(content, TianYiSpiderEvent.class);
        event.setDeferredResult(deferredResult);
        event.setAccount(account);
        event.setMsgCode(msgCode);
        event.setUserid(userid);
        event.setState(TianYiSpiderState.LOGIN); // 接下来获取密钥，登录获取积分

        SpiderReactor.getInstance().process(event);

        return deferredResult;
    }
}
