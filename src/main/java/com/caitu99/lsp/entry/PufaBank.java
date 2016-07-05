/*
 * Copyright (c) 2015-2020 by caitu99
 * All rights reserved.
 */
package com.caitu99.lsp.entry;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.async.DeferredResult;

import com.alibaba.fastjson.JSON;
import com.caitu99.lsp.Constant;
import com.caitu99.lsp.cache.RedisOperate;
import com.caitu99.lsp.exception.SpiderException;
import com.caitu99.lsp.model.spider.pufabank.PufaBankEvent;
import com.caitu99.lsp.model.spider.pufabank.PufaBankState;
import com.caitu99.lsp.spider.SpiderReactor;

/**
 * 浦发信用卡
 * 
 * @Description: (类职责详细描述,可空) 
 * @ClassName: PufaBank 
 * @author ws
 * @date 2016年3月30日 下午6:10:52 
 * @Copyright (c) 2015-2020 by caitu99
 */
@Controller
public class PufaBank extends BaseController {

    //private static final Logger logger = LoggerFactory.getLogger(LianTong.class);

    @Autowired
    private RedisOperate redis;


    /**
     * 获取浦发图形验证码
     * 	
     * @Description: (方法职责详细描述,可空)  
     * @Title: login 
     * @param request
     * @return
     * @date 2016年3月22日 下午2:32:42  
     * @author ws
     */
    @RequestMapping(value = "/spider/pufabank/img/1.0", produces = "application/json;charset=utf-8")
    @ResponseBody
    public DeferredResult<Object> img(HttpServletRequest request) {
        DeferredResult<Object> deferredResult = new DeferredResult<>();
        String userid = request.getParameter("userid");

        if (StringUtils.isEmpty(userid)) {
            SpiderException exception = new SpiderException(1006, "数据不完整");
            deferredResult.setResult(exception.toString());
            return deferredResult;
        }

        PufaBankEvent event = new PufaBankEvent(userid, deferredResult);
        event.setDeferredResult(deferredResult);
        event.setUserid(userid);
        event.setState(PufaBankState.LOGINPAGE);
        SpiderReactor.getInstance().process(event);

        return deferredResult;
    }


    /**
     * 浦发登录，并请求短信验证码
     * 	
     * @Description: (方法职责详细描述,可空)  
     * @Title: login 
     * @param request
     * @return
     * @date 2016年3月30日 下午6:12:24  
     * @author ws
     */
    @RequestMapping(value = "/spider/pufabank/login/1.0", produces = "application/json;charset=utf-8")
    @ResponseBody
    public DeferredResult<Object> login(HttpServletRequest request) {
        DeferredResult<Object> deferredResult = new DeferredResult<>();
        String userid = request.getParameter("userid");
        String phoneno = request.getParameter("account");
        String password = request.getParameter("password");
        String vcode = request.getParameter("vcode");

        if (StringUtils.isEmpty(userid)
                || StringUtils.isEmpty(phoneno)
                || StringUtils.isEmpty(password)
                || StringUtils.isEmpty(vcode)) {
            SpiderException exception = new SpiderException(1006, "数据不完整");
            deferredResult.setResult(exception.toString());
            return deferredResult;
        }

        // 获取缓存事件内容
        String key = String.format(Constant.PUFABANK_IMPORT_KEY, userid);
        String content = redis.getStringByKey(key);
        if (StringUtils.isEmpty(content)) {
            SpiderException exception = new SpiderException(1005, "账号验证已过期");
            deferredResult.setResult(exception.toString());
            return deferredResult;
        }

        PufaBankEvent event = JSON.parseObject(content, PufaBankEvent.class); //反序列化，把之前保存的event取出来
        event.setDeferredResult(deferredResult);
        event.setAccount(phoneno);
        event.setPassword(password);
        event.setvCode(vcode);
        event.setUserid(userid);
        event.setState(PufaBankState.LOGIN);

        SpiderReactor.getInstance().process(event);

        return deferredResult;

    }


    /**
     * 浦发短信验证码验证
     * 	
     * @Description: (方法职责详细描述,可空)  
     * @Title: verify 
     * @param request
     * @return
     * @date 2016年3月30日 下午6:12:35  
     * @author ws
     */
    @RequestMapping(value = "/spider/pufabank/verify/1.0", produces = "application/json;charset=utf-8")
    @ResponseBody
    public DeferredResult<Object> verify(HttpServletRequest request) {
        DeferredResult<Object> deferredResult = new DeferredResult<>();
        String userid = request.getParameter("userid");
        String msmCode = request.getParameter("msmCode");

        if (StringUtils.isEmpty(userid)
                || StringUtils.isEmpty(msmCode)) {
            SpiderException exception = new SpiderException(1006, "数据不完整");
            deferredResult.setResult(exception.toString());
            return deferredResult;
        }

        // 获取缓存事件内容
        String key = String.format(Constant.PUFABANK_IMPORT_KEY, userid);
        String content = redis.getStringByKey(key);
        if (StringUtils.isEmpty(content)) {
            SpiderException exception = new SpiderException(1005, "账号验证已过期");
            deferredResult.setResult(exception.toString());
            return deferredResult;
        }

        PufaBankEvent event = JSON.parseObject(content, PufaBankEvent.class); //反序列化，把之前保存的event取出来
        event.setDeferredResult(deferredResult);
        event.setMsmCode(msmCode);
        event.setUserid(userid);
        event.setState(PufaBankState.VERIFY);

        SpiderReactor.getInstance().process(event);

        return deferredResult;

    }

    
}
