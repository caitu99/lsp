package com.caitu99.lsp.entry;


import com.alibaba.fastjson.JSON;
import com.caitu99.lsp.Constant;
import com.caitu99.lsp.cache.RedisOperate;
import com.caitu99.lsp.exception.SpiderException;
import com.caitu99.lsp.model.spider.botaohui.BoTaoHuiEvent;
import com.caitu99.lsp.model.spider.botaohui.BoTaoHuiState;
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

@Controller
public class BoTaoHui extends BaseController {
    private static final Logger logger = LoggerFactory.getLogger(AirChina.class);

    @Autowired
    private RedisOperate redis;

    /**
     * 2015年11月24日
     * yukf
     */
    @RequestMapping(value = "/api/botaohui/loginpage/1.0", produces = "application/json;charset=utf-8")
    @ResponseBody
    public DeferredResult<Object> loginPage(HttpServletRequest request) {

        DeferredResult<Object> deferredResult = new DeferredResult<>();
        String userId = request.getParameter("userid");

        if (StringUtils.isEmpty(userId)) {
            SpiderException exception = new SpiderException(1006, "数据不完整");
            deferredResult.setResult(exception.toString());
            return deferredResult;
        }

        BoTaoHuiEvent event = new BoTaoHuiEvent(userId, deferredResult);
        event.setUserid(userId);
        event.setState(BoTaoHuiState.LOGINPAGE);//首先进入首页，获取cookie

        SpiderReactor.getInstance().process(event);
        logger.info("start processing botaohui event: {}", event);
        return deferredResult;
    }

    @RequestMapping(value = "/api/botaohui/sendsms/1.0", produces = "application/json;charset=utf-8")
    @ResponseBody
    public DeferredResult<Object> sendSms(HttpServletRequest request) {
        DeferredResult<Object> deferredResult = new DeferredResult<>();

        String userid = request.getParameter("userid");
        String account = request.getParameter("account");
        String vCode = request.getParameter("vcode");

        if (StringUtils.isEmpty(userid) || StringUtils.isEmpty(account) || StringUtils.isEmpty(vCode)) {
            SpiderException exception = new SpiderException(1006, "数据不完整");
            deferredResult.setResult(exception.toString());
            return deferredResult;
        }


        String key = String.format(Constant.BOTAOHUI_IMPORT_KEY, userid);
        String content = redis.getStringByKey(key);
        if (StringUtils.isEmpty(content)) {
            SpiderException exception = new SpiderException(1005, "账号验证已过期");
            deferredResult.setResult(exception.toString());
            return deferredResult;
        }

        BoTaoHuiEvent event = JSON.parseObject(content, BoTaoHuiEvent.class);
        event.setAccount(account);
        event.setvCode(vCode);
        event.setDeferredResult(deferredResult);
        event.setState(BoTaoHuiState.VERIFY);


        SpiderReactor.getInstance().process(event);
        logger.info("start processing botaohui event: {}", event);
        return deferredResult;
    }

    @RequestMapping(value = "/api/botaohui/login/1.0", produces = "application/json;charset=utf-8")
    @ResponseBody
    public DeferredResult<Object> login(HttpServletRequest request) {
        DeferredResult<Object> deferredResult = new DeferredResult<>();

        String userid = request.getParameter("userid");
        String password = request.getParameter("password");

        if (StringUtils.isEmpty(userid) || StringUtils.isEmpty(password)) {
            SpiderException exception = new SpiderException(1006, "数据不完整");
            deferredResult.setResult(exception.toString());
            return deferredResult;
        }


        String key = String.format(Constant.BOTAOHUI_IMPORT_KEY, userid);
        String content = redis.getStringByKey(key);
        if (StringUtils.isEmpty(content)) {
            SpiderException exception = new SpiderException(1005, "账号验证已过期");
            deferredResult.setResult(exception.toString());
            return deferredResult;
        }

        BoTaoHuiEvent event = JSON.parseObject(content, BoTaoHuiEvent.class);
        event.setPassword(password);
        event.setDeferredResult(deferredResult);
        event.setState(BoTaoHuiState.LOGIN);


        SpiderReactor.getInstance().process(event);
        logger.info("start processing botaohui event: {}", event);
        return deferredResult;
    }

}
