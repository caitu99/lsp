/*
 * Copyright (c) 2015-2020 by caitu99
 * All rights reserved.
 */
package com.caitu99.lsp.entry;

import com.alibaba.fastjson.JSON;
import com.caitu99.lsp.Constant;
import com.caitu99.lsp.cache.RedisOperate;
import com.caitu99.lsp.exception.SpiderException;
import com.caitu99.lsp.model.spider.taobao.TaoBaoSpiderEvent;
import com.caitu99.lsp.model.spider.taobao.TaoBaoSpiderState;
import com.caitu99.lsp.spider.SpiderReactor;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.async.DeferredResult;

import javax.servlet.http.HttpServletRequest;


/**
 * @author Hongbo Peng
 * @Description: (淘宝 淘金币 天猫积分 淘里程查询)
 * @ClassName: TaoBao
 * @date 2015年11月16日 下午5:53:56
 * @Copyright (c) 2015-2020 by caitu99
 */
@Controller
public class TaoBao extends BaseController {

    @Autowired
    private RedisOperate redis;

    /**
     * @param request
     * @return
     * @Description: (获取验证码)
     * @Title: getImgCode
     * @date 2015年11月18日 上午10:00:23
     * @author Hongbo Peng
     */
    @RequestMapping(value = "/spider/taobao/imgcode/1.0", produces = "application/json;charset=utf-8")
    @ResponseBody
    public DeferredResult<Object> getImgCode(HttpServletRequest request) {
        DeferredResult<Object> deferredResult = new DeferredResult<>();
        String userId = request.getParameter("userid");

        if (StringUtils.isEmpty(userId)) {
            SpiderException exception = new SpiderException(1006, "数据不完整");
            deferredResult.setResult(exception.toString());
            return deferredResult;
        }

        TaoBaoSpiderEvent event = new TaoBaoSpiderEvent(userId, deferredResult, request);
        event.setUserid(userId);
        event.setState(TaoBaoSpiderState.NONE);

        SpiderReactor.getInstance().process(event);
        return deferredResult;
    }

    /**
     * @param request
     * @return
     * @Description: (登录)
     * @Title: login
     * @date 2015年11月18日 上午10:04:09
     * @author Hongbo Peng
     */
    @RequestMapping(value = "/spider/taobao/login/1.0", produces = "application/json;charset=utf-8")
    @ResponseBody
    public DeferredResult<Object> login(HttpServletRequest request) {
        DeferredResult<Object> deferredResult = new DeferredResult<>();
        String userid = request.getParameter("userid");
        String account = request.getParameter("account");
        String password = request.getParameter("password");
        String yzm = request.getParameter("yzm");

        if (StringUtils.isEmpty(userid)
                || StringUtils.isEmpty(account)
                || StringUtils.isEmpty(password)
                || StringUtils.isEmpty(yzm)) {
            SpiderException exception = new SpiderException(1006, "数据不完整");
            deferredResult.setResult(exception.toString());
            return deferredResult;
        }
        // 获取缓存事件内容
        String key = String.format(Constant.TAOBAO_IMPORT_KEY, userid);
        String content = redis.getStringByKey(key);
        if (StringUtils.isEmpty(content)) {
            SpiderException exception = new SpiderException(1005, "验证码已过期");
            deferredResult.setResult(exception.toString());
            return deferredResult;
        }
        TaoBaoSpiderEvent event = JSON.parseObject(content, TaoBaoSpiderEvent.class);
        event.setDeferredResult(deferredResult);
        event.setAccount(account);
        event.setPassword(password);
        event.setImgCode(yzm);
        event.setUserid(userid);
        event.setState(TaoBaoSpiderState.LOGIN);

        SpiderReactor.getInstance().process(event);
        return deferredResult;
    }

    /**
     * @param request
     * @return
     * @Description: (短信验证码)
     * @Title: vcode
     * @date 2015年11月18日 上午10:04:44
     * @author Hongbo Peng
     */
    @RequestMapping(value = "/spider/taobao/vcode/1.0", produces = "application/json;charset=utf-8")
    @ResponseBody
    public DeferredResult<Object> vcode(HttpServletRequest request) {
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
        String key = String.format(Constant.TAOBAO_IMPORT_KEY, userid);
        String content = redis.getStringByKey(key);
        if (StringUtils.isEmpty(content)) {
            SpiderException exception = new SpiderException(1005, "验证码已过期");
            deferredResult.setResult(exception.toString());
            return deferredResult;
        }
        TaoBaoSpiderEvent event = JSON.parseObject(content, TaoBaoSpiderEvent.class);
        event.setDeferredResult(deferredResult);
        event.setImgCode(yzm);
        event.setUserid(userid);
        event.setState(TaoBaoSpiderState.RECHECK);

        SpiderReactor.getInstance().process(event);
        return deferredResult;
    }
}
