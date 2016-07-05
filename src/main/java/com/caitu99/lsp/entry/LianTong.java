/*
 * Copyright (c) 2015-2020 by caitu99
 * All rights reserved.
 */
package com.caitu99.lsp.entry;

import com.alibaba.fastjson.JSON;
import com.caitu99.lsp.Constant;
import com.caitu99.lsp.cache.RedisOperate;
import com.caitu99.lsp.exception.SpiderException;
import com.caitu99.lsp.model.spider.liantong.LianTongSpiderEvent;
import com.caitu99.lsp.model.spider.liantong.LianTongSpiderState;
import com.caitu99.lsp.spider.SpiderReactor;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.async.DeferredResult;

import javax.servlet.http.HttpServletRequest;

/**
 * @author chenhl
 * @Description: (类职责详细描述, 可空)
 * @ClassName: LianTong
 * @date 2015年11月18日 上午11:54:13
 * @Copyright (c) 2015-2020 by caitu99
 */
@Controller
public class LianTong extends BaseController {

    //private static final Logger logger = LoggerFactory.getLogger(LianTong.class);

    @Autowired
    private RedisOperate redis;


    /**
     * 登录联通，获取积分
     *
     * @param request{userid,phoneno,password}
     * @return
     * @Description: (方法职责详细描述, 可空)
     * @Title: login
     * @date 2015年11月13日 下午2:16:19
     * @author chenhl
     */
    @RequestMapping(value = "/spider/liantong/login/1.0", produces = "application/json;charset=utf-8")
    @ResponseBody
    public DeferredResult<Object> login(HttpServletRequest request) {
        DeferredResult<Object> deferredResult = new DeferredResult<>();
        String userid = request.getParameter("userid");
        String phoneno = request.getParameter("phoneno");
        String password = request.getParameter("password");

        if (StringUtils.isEmpty(userid)
                || StringUtils.isEmpty(phoneno)
                || StringUtils.isEmpty(password)) {
            SpiderException exception = new SpiderException(1006, "数据不完整");
            deferredResult.setResult(exception.toString());
            return deferredResult;
        }

        LianTongSpiderEvent event = new LianTongSpiderEvent(userid, deferredResult, request);
        event.setDeferredResult(deferredResult);
        event.setUserid(userid);
        event.setPassword(password);
        event.setPhoneno(phoneno);
        event.setState(LianTongSpiderState.First);
        SpiderReactor.getInstance().process(event);

        return deferredResult;
    }


    /**
     * 带着验证码登陆,并获得积分
     *
     * @param request{userid,phoneno,password,yzm}
     * @return
     * @Description: (方法职责详细描述, 可空)
     * @Title: vcodeLogin
     * @date 2015年11月13日 下午2:15:09
     * @author chenhl
     */
    @RequestMapping(value = "/spider/liantong/vcode_login/1.0", produces = "application/json;charset=utf-8")
    @ResponseBody
    public DeferredResult<Object> vcodeLogin(HttpServletRequest request) {
        DeferredResult<Object> deferredResult = new DeferredResult<>();
        String userid = request.getParameter("userid");
        String phoneno = request.getParameter("phoneno");
        String password = request.getParameter("password");
        String yzm = request.getParameter("yzm");

        if (StringUtils.isEmpty(userid)
                || StringUtils.isEmpty(phoneno)
                || StringUtils.isEmpty(password)
                || StringUtils.isEmpty(yzm)) {
            SpiderException exception = new SpiderException(1006, "数据不完整");
            deferredResult.setResult(exception.toString());
            return deferredResult;
        }

        // 获取缓存事件内容
        String key = String.format(Constant.LIAN_TONG_IMPORT_KEY, userid);
        String content = redis.getStringByKey(key);
        if (StringUtils.isEmpty(content)) {
            SpiderException exception = new SpiderException(1005, "账号验证已过期");
            deferredResult.setResult(exception.toString());
            return deferredResult;
        }

        LianTongSpiderEvent event = JSON.parseObject(content, LianTongSpiderEvent.class); //反序列化，把之前保存的event取出来
        event.setDeferredResult(deferredResult);
        event.setPassword(password);
        event.setYzm(yzm);
        event.setUserid(userid);
        event.setState(LianTongSpiderState.VCODELOGIN);

        SpiderReactor.getInstance().process(event);

        return deferredResult;

    }

}
