/*
 * Copyright (c) 2015-2020 by caitu99
 * All rights reserved.
 */
package com.caitu99.lsp.entry;

import com.alibaba.fastjson.JSON;
import com.caitu99.lsp.Constant;
import com.caitu99.lsp.cache.RedisOperate;
import com.caitu99.lsp.exception.SpiderException;
import com.caitu99.lsp.model.spider.cmbchina.CmbChinaSpiderEvent;
import com.caitu99.lsp.model.spider.cmbchina.CmbChinaSpiderState;
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

/**
 * 招行信用卡积分抓取
 *
 * @author chencheng
 * @Description: (类职责详细描述, 可空)
 * @ClassName: CmbChina
 * @date 2015年11月18日 下午2:07:27
 * @Copyright (c) 2015-2020 by caitu99
 */
@Controller
public class CmbChina extends BaseController {

    private static final Logger logger = LoggerFactory.getLogger(CmbChina.class);

    @Autowired
    private RedisOperate redis;

    /**
     * 获取图片验证码
     *
     * @Description: (方法职责详细描述, 可空)
     * @Title: getImgCode
     * @param    request {userid}
     * @return 图片字符串
     * @date 2015年11月18日 下午2:07:55
     * @author chencheng
     */
    @RequestMapping(value = "/spider/cmbchina/imgcode/1.0", produces = "application/json;charset=utf-8")
    @ResponseBody
    public DeferredResult<Object> getImgCode(HttpServletRequest request) {

        DeferredResult<Object> deferredResult = new DeferredResult<>();
        String userId = request.getParameter("userid");

        if (StringUtils.isEmpty(userId)) {
            SpiderException exception = new SpiderException(1006, "数据不完整");
            deferredResult.setResult(exception.toString());
            return deferredResult;
        }

        CmbChinaSpiderEvent event = new CmbChinaSpiderEvent(userId, deferredResult, request);
        event.setUserid(userId);
        event.setState(CmbChinaSpiderState.NONE); // the first step is to check

        SpiderReactor.getInstance().process(event);

        return deferredResult;
    }

    /**
     * 登录
     *
     * @param request {userid,account,password,yzm}
     * @return
     * @Description: (方法职责详细描述, 可空)
     * @Title: login
     * @date 2015年11月18日 下午2:17:03
     * @author chencheng
     */
    @RequestMapping(value = "/spider/cmbchina/login/1.0", produces = "application/json;charset=utf-8")
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

        String key = String.format(Constant.CMB_CHINA_IMPORT_KEY, userid);
        String content = redis.getStringByKey(key);
        if (StringUtils.isEmpty(content)) {
            SpiderException exception = new SpiderException(1005, "验证码已过期");
            deferredResult.setResult(exception.toString());
            return deferredResult;
        }


        CmbChinaSpiderEvent event = JSON.parseObject(content, CmbChinaSpiderEvent.class);
        event.setDeferredResult(deferredResult);
        event.setAccount(account);
        event.setPassword(password);
        event.setYzm(yzm);
        event.setUserid(userid);
        event.setState(CmbChinaSpiderState.LOGIN); // the first step is to check

        SpiderReactor.getInstance().process(event);

        return deferredResult;
    }
}
