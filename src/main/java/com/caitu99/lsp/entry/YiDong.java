package com.caitu99.lsp.entry;

import com.alibaba.fastjson.JSON;
import com.caitu99.lsp.Constant;
import com.caitu99.lsp.cache.RedisOperate;
import com.caitu99.lsp.exception.SpiderException;
import com.caitu99.lsp.model.spider.yidong.YiDongEvent;
import com.caitu99.lsp.model.spider.yidong.YiDongState;
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
public class YiDong extends BaseController {

    private static final Logger logger = LoggerFactory.getLogger(YiDong.class);
    @Autowired
    private RedisOperate redis;

    @RequestMapping(value = "/api/yidong/login/1.0", produces = "application/json;charset=utf-8")
    @ResponseBody
    public DeferredResult<Object> login(HttpServletRequest request) {
        logger.debug("get a request for jingdong");
        DeferredResult<Object> deferredResult = new DeferredResult<>();

        String userid = request.getParameter("userid");
        String account = request.getParameter("account");
        String vcode = request.getParameter("vcode");


        if (StringUtils.isEmpty(userid) || StringUtils.isEmpty(account) || StringUtils.isEmpty(vcode)) {
            SpiderException exception = new SpiderException(1006, "数据不完整");
            deferredResult.setResult(exception.toString());
            return deferredResult;
        }

        String key = String.format(Constant.YIDONG_IMPORT_KEY, userid);
        String content = redis.getStringByKey(key);
        if (StringUtils.isEmpty(content)) {
            SpiderException exception = new SpiderException(1005, "账号验证已过期");
            deferredResult.setResult(exception.toString());
            return deferredResult;
        }


        YiDongEvent event = JSON.parseObject(content, YiDongEvent.class);
        event.setAccount(account);
        event.setUserid(userid);
        event.setvCode(vcode);

        event.setState(YiDongState.PRESENDSMS);
        event.setDeferredResult(deferredResult);

        SpiderReactor.getInstance().process(event);
        logger.info("start processing jingdong event: {}", event);
        return deferredResult;
    }

    @RequestMapping(value = "/api/yidong/verify/1.0", produces = "application/json;charset=utf-8")
    @ResponseBody
    public DeferredResult<Object> verify(HttpServletRequest request) {
        DeferredResult<Object> deferredResult = new DeferredResult<>();

        String passwordType = request.getParameter("passwordtype");
        String account = request.getParameter("account");
        String userid = request.getParameter("userid");
        String password = request.getParameter("password");
        String vCode = request.getParameter("vcode");

        if (StringUtils.isEmpty(userid)
                || StringUtils.isEmpty(password)
                || StringUtils.isEmpty(vCode)
                || StringUtils.isEmpty(account)
                || StringUtils.isEmpty(passwordType)) {
            SpiderException exception = new SpiderException(1006, "数据不完整");
            deferredResult.setResult(exception.toString());
            return deferredResult;
        }


        String key = String.format(Constant.YIDONG_IMPORT_KEY, userid);
        String content = redis.getStringByKey(key);
        if (StringUtils.isEmpty(content)) {
            SpiderException exception = new SpiderException(1005, "账号验证已过期");
            deferredResult.setResult(exception.toString());
            return deferredResult;
        }


        YiDongEvent event = JSON.parseObject(content, YiDongEvent.class);
        if( account != null )
            event.setAccount(account);
        event.setDeferredResult(deferredResult);
        event.setPassword(password);
        event.setvCode(vCode);
        event.setPasswordType(passwordType);
        event.setState(YiDongState.CHECK); // verify vcode

        SpiderReactor.getInstance().process(event);

        logger.info("start processing yidong event: {}", event);
        return deferredResult;
    }

    @RequestMapping(value = "/api/yidong/img/1.0", produces = "application/json;charset=utf-8")
    @ResponseBody
    public DeferredResult<Object> vcode(HttpServletRequest request) {
        DeferredResult<Object> deferredResult = new DeferredResult<>();

        String userid = request.getParameter("userid");
        if (StringUtils.isEmpty(userid)) {
            SpiderException exception = new SpiderException(1006, "数据不完整");
            deferredResult.setResult(exception.toString());
            return deferredResult;
        }

        YiDongEvent event = new YiDongEvent(userid, deferredResult);
        event.setState(YiDongState.GETIMG); // verify vcode
        SpiderReactor.getInstance().process(event);
        logger.info("start processing yidong event: {}", event);
        return deferredResult;
    }


}
