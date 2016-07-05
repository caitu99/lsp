/*
 * Copyright (c) 2015-2020 by caitu99
 * All rights reserved.
 */
package com.caitu99.lsp.entry;

import com.alibaba.fastjson.JSON;
import com.caitu99.lsp.Constant;
import com.caitu99.lsp.cache.RedisOperate;
import com.caitu99.lsp.exception.SpiderException;
import com.caitu99.lsp.model.spider.airchina.AirChinaSpiderEvent;
import com.caitu99.lsp.model.spider.airchina.AirChinaSpiderState;
import com.caitu99.lsp.spider.SpiderReactor;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.async.DeferredResult;

import javax.servlet.http.HttpServletRequest;

/**
 * @author chencheng
 * @Description: (类职责详细描述, 可空)
 * @ClassName: AirChina
 * @date 2015年11月12日 上午10:55:40
 * @Copyright (c) 2015-2020 by caitu99
 */
@Controller
public class AirChina extends BaseController {


    //private static final Logger logger = LoggerFactory.getLogger(AirChina.class);

    @Autowired
    private RedisOperate redis;

    /**
     * 获取国航图片验证码
     *
     * @param request{userid}
     * @return
     * @Description: (方法职责详细描述, 可空)
     * @Title: getImgCode
     * @date 2015年11月13日 下午2:15:09
     * @author chencheng
     */
    @RequestMapping(value = "/spider/airchina/imgcode/1.0", produces = "application/json;charset=utf-8")
    @ResponseBody
    public DeferredResult<Object> getImgCode(HttpServletRequest request) {

        DeferredResult<Object> deferredResult = new DeferredResult<>();
        String userId = request.getParameter("userid");

        if (StringUtils.isEmpty(userId)) {
            SpiderException exception = new SpiderException(1006, "数据不完整");
            deferredResult.setResult(exception.toString());
            return deferredResult;
        }

        AirChinaSpiderEvent event = new AirChinaSpiderEvent(userId, deferredResult, request);
        event.setUserid(userId);
        event.setState(AirChinaSpiderState.NONE);//首先进入首页，获取cookie

        SpiderReactor.getInstance().process(event);

        return deferredResult;
    }

    /**
     * 登录国航，获取积分
     *
     * @param request{userid,account,password,yzm,type}
     * @param request{userid,account,password,yzm}
     * @return
     * @Description: (方法职责详细描述, 可空)
     * @Title: login
     * @date 2015年11月13日 下午2:16:19
     * @author chencheng
     */
    @RequestMapping(value = "/spider/airchina/login/1.0", produces = "application/json;charset=utf-8")
    @ResponseBody
    public DeferredResult<Object> login(HttpServletRequest request) {
        DeferredResult<Object> deferredResult = new DeferredResult<>();
        String userid = request.getParameter("userid");
        String account = request.getParameter("account");
        String password = request.getParameter("password");
        String yzm = request.getParameter("yzm");
        String type = request.getParameter("type");

        if (StringUtils.isEmpty(userid)
                || StringUtils.isEmpty(account)
                || StringUtils.isEmpty(password)
                || StringUtils.isEmpty(yzm)) {
            SpiderException exception = new SpiderException(1006, "数据不完整");
            deferredResult.setResult(exception.toString());
            return deferredResult;
        }

        // 获取缓存事件内容
        String key = String.format(Constant.AIR_CHINA_IMPORT_KEY, userid);
        String content = redis.getStringByKey(key);
        if (StringUtils.isEmpty(content)) {
            SpiderException exception = new SpiderException(1005, "验证码已过期");
            deferredResult.setResult(exception.toString());
            return deferredResult;
        }

        AirChinaSpiderEvent event = JSON.parseObject(content, AirChinaSpiderEvent.class);
        event.setDeferredResult(deferredResult);
        event.setAccount(account);
        event.setPassword(password);
        event.setYzm(yzm);
        event.setType(type);
        event.setUserid(userid);
        event.setState(AirChinaSpiderState.KEY); // 接下来获取密钥，登录获取积分

        SpiderReactor.getInstance().process(event);

        return deferredResult;
    }
}
