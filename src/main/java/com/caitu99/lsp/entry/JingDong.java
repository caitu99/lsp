package com.caitu99.lsp.entry;

import com.alibaba.fastjson.JSON;
import com.caitu99.lsp.Constant;
import com.caitu99.lsp.cache.RedisOperate;
import com.caitu99.lsp.exception.SpiderException;
import com.caitu99.lsp.model.spider.jingdong.JingDongEvent;
import com.caitu99.lsp.model.spider.jingdong.JingDongState;
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
public class JingDong extends BaseController {
    private static final Logger logger = LoggerFactory.getLogger(JingDong.class);
    @Autowired
    private RedisOperate redis;

    @RequestMapping(value = "/api/jingdong/login/1.0", produces = "application/json;charset=utf-8")
    @ResponseBody
    public DeferredResult<Object> login(HttpServletRequest request) {
        logger.debug("get a request for jingdong");
        DeferredResult<Object> deferredResult = new DeferredResult<>();

        String userid = request.getParameter("userid");
        String account = request.getParameter("account");
        String password = request.getParameter("password");
        String vCode = request.getParameter("vcode");

        if (StringUtils.isEmpty(userid) || StringUtils.isEmpty(account) || StringUtils.isEmpty(password) || StringUtils.isEmpty(vCode)) {
            SpiderException exception = new SpiderException(1006, "数据不完整");
            deferredResult.setResult(exception.toString());
            return deferredResult;
        }


        String key = String.format(Constant.JINGDONG_IMPORT_KEY, userid);
        String content = redis.getStringByKey(key);
        if (StringUtils.isEmpty(content)) {
            SpiderException exception = new SpiderException(1005, "账号验证已过期");
            deferredResult.setResult(exception.toString());
            return deferredResult;
        }

        JingDongEvent event = JSON.parseObject(content, JingDongEvent.class);
        event.setAccount(account);
        event.setPassword(password);
        event.setvCode(vCode);
        event.setDeferredResult(deferredResult);
        event.setState(JingDongState.LOGIN);


        SpiderReactor.getInstance().process(event);
        logger.info("start processing jingdong event: {}", event);
        return deferredResult;
    }

    @RequestMapping(value = "/api/jingdong/loginpage/1.0", produces = "application/json;charset=utf-8")
    @ResponseBody
    public DeferredResult<Object> verify(HttpServletRequest request) {
        DeferredResult<Object> deferredResult = new DeferredResult<>();

        String userid = request.getParameter("userid");

        if (StringUtils.isEmpty(userid)) {
            SpiderException exception = new SpiderException(1006, "数据不完整");
            deferredResult.setResult(exception.toString());
            return deferredResult;
        }
        JingDongEvent event = new JingDongEvent(userid, deferredResult);
        event.setState(JingDongState.PRE_LOGINPAGE);
        SpiderReactor.getInstance().process(event);
        logger.info("start processing jingdong event: {}", event);
        return deferredResult;
    }
}
