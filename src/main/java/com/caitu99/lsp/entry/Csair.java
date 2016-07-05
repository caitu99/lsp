/*
 * Copyright (c) 2015-2020 by caitu99
 * All rights reserved.
 */
package com.caitu99.lsp.entry;

import com.alibaba.fastjson.JSON;
import com.caitu99.lsp.Constant;
import com.caitu99.lsp.cache.RedisOperate;
import com.caitu99.lsp.exception.SpiderException;
import com.caitu99.lsp.model.spider.csair.CsairSpiderEvent;
import com.caitu99.lsp.model.spider.csair.CsairSpiderState;
import com.caitu99.lsp.spider.SpiderReactor;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.async.DeferredResult;

import javax.servlet.http.HttpServletRequest;

/**
 * @author fangjunxiao
 * @Description: (类职责详细描述, 可空)
 * @ClassName: Csair
 * @date 2015年11月18日 下午2:03:57
 * @Copyright (c) 2015-2020 by caitu99
 */
@Controller
public class Csair extends BaseController {


    @Autowired
    private RedisOperate redis;

    /**
     * 获取南航验证码
     *
     * @param request
     * @return
     * @Description: (方法职责详细描述, 可空)
     * @Title: getImgCode
     * @date 2015年11月18日 下午2:07:00
     * @author fangjunxiao
     */
    @RequestMapping(value = "/spider/csair/imgcode/1.0", produces = "application/json;charset=utf-8")
    @ResponseBody
    public DeferredResult<Object> getImgCode(HttpServletRequest request) {
        DeferredResult<Object> deferredResult = new DeferredResult<>();
        String userId = request.getParameter("userid");
        if (StringUtils.isEmpty(userId)) {
            SpiderException exception = new SpiderException(1006, "数据不完整");
            deferredResult.setResult(exception.toString());
            return deferredResult;
        }

        CsairSpiderEvent event = new CsairSpiderEvent(userId, deferredResult, request);
        event.setUserid(userId);
        event.setState(CsairSpiderState.CHECK);

        SpiderReactor.getInstance().process(event);

        return deferredResult;
    }


    /**
     * 登录南航,获取积分
     *
     * @param request
     * @return
     * @Description: (方法职责详细描述, 可空)
     * @Title: login
     * @date 2015年11月19日 上午9:36:45
     * @author fangjunxiao
     */
    @RequestMapping(value = "/spider/csair/login/1.0", produces = "application/json;charset=utf-8")
    @ResponseBody
    public DeferredResult<Object> login(HttpServletRequest request) {
        DeferredResult<Object> deferredResult = new DeferredResult<>();
        String userid = request.getParameter("userid");
        String account = request.getParameter("account");
        String password = request.getParameter("password");
        String yzm = request.getParameter("yzm");
        String type = request.getParameter("type");
        String inCode = request.getParameter("inCode");

        if (StringUtils.isEmpty(userid)
                || StringUtils.isEmpty(account)
                || StringUtils.isEmpty(password)
                || StringUtils.isEmpty(yzm)
                || StringUtils.isEmpty(inCode)
                || StringUtils.isEmpty(type)) {
            SpiderException exception = new SpiderException(1006, "数据不完整");
            deferredResult.setResult(exception.toString());
            return deferredResult;
        }

        // 获取缓存事件内容
        String key = String.format(Constant.CSAIR_IMPORT_KEY, userid);
        String content = redis.getStringByKey(key);
        if (StringUtils.isEmpty(content)) {
            SpiderException exception = new SpiderException(1005, "验证码已过期");
            deferredResult.setResult(exception.toString());
            return deferredResult;
        }

        CsairSpiderEvent event = JSON.parseObject(content, CsairSpiderEvent.class);
        event.setDeferredResult(deferredResult);
        event.setAccount(account);
        event.setPassword(password);
        event.setValidator(yzm);
        event.setType(type);
        event.setInCode(inCode);
        event.setUserid(userid);
        event.setState(CsairSpiderState.LOGIN);

        SpiderReactor.getInstance().process(event);

        return deferredResult;

    }


}
