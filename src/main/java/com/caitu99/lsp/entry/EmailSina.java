package com.caitu99.lsp.entry;

import com.alibaba.fastjson.JSON;
import com.caitu99.lsp.Constant;
import com.caitu99.lsp.cache.RedisOperate;
import com.caitu99.lsp.exception.SpiderException;
import com.caitu99.lsp.model.spider.mailsina.MailSinaSpiderEvent;
import com.caitu99.lsp.model.spider.mailsina.MailSinaSpiderState;
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
 * Created by Lion on 2015/11/9 0009.
 */

@Controller
public class EmailSina extends BaseController {
    private static final Logger logger = LoggerFactory.getLogger(EmailQQ.class);

    @Autowired
    private RedisOperate redis;

    @RequestMapping(value = "/api/mail/sina/login/1.0", produces = "application/json;charset=utf-8")
    @ResponseBody
    public DeferredResult<Object> login(HttpServletRequest request) {
        logger.debug("Sina model: 收到一次请求...");
        DeferredResult<Object> deferredResult = new DeferredResult<>();
        //deferredResult.setResult(JSON.toJSONString(new SpiderResult(0, "success")));
        String userid = request.getParameter("userid");
        String account = request.getParameter("account");
        String password = request.getParameter("password");
        String date = request.getParameter("date");

        if (StringUtils.isEmpty(userid)
                || StringUtils.isEmpty(account)
                || StringUtils.isEmpty(password)
                || StringUtils.isEmpty(date)) {
            SpiderException exception = new SpiderException(1006, "数据不完整");
            deferredResult.setResult(exception.toString());
            return deferredResult;
        }
        String key = String.format(Constant.MAILSINATASKQUEUE, account);
        String value = redis.getStringByKey(key);
        if (StringUtils.isNotBlank(value)) {
            SpiderException exception = new SpiderException(1026, "该邮箱已经在导入中");
            deferredResult.setResult(exception.toString());
            return deferredResult;
        }
        MailSinaSpiderEvent event = new MailSinaSpiderEvent(userid, deferredResult, request);
        event.setAccount(account);
        event.setPassword(password);
        event.setDate(Long.parseLong(date));
        event.setState(MailSinaSpiderState.PRE_LOGIN); // the first step is to pre login

        SpiderReactor.getInstance().process(event);

        logger.info("start processing mail sina event: {}", event);
        return deferredResult;
    }

    @RequestMapping(value = "/api/mail/sina/verify/1.0", produces = "application/json;charset=utf-8")
    @ResponseBody
    public DeferredResult<Object> verify(HttpServletRequest request) {
        DeferredResult<Object> deferredResult = new DeferredResult<>();

        String userid = request.getParameter("userid");
        String vCode = request.getParameter("vcode");

        if (StringUtils.isEmpty(userid)
                || StringUtils.isEmpty(vCode)) {
            SpiderException exception = new SpiderException(1006, "数据不完整");
            deferredResult.setResult(exception.toString());
            return deferredResult;
        }

        String key = String.format(Constant.MAILSINAIMPORTKEY, userid);
        String content = redis.getStringByKey(key);
        if (StringUtils.isEmpty(content)) {
            SpiderException exception = new SpiderException(1005, "账号验证已过期");
            deferredResult.setResult(exception.toString());
            return deferredResult;
        }

        MailSinaSpiderEvent event = JSON.parseObject(content, MailSinaSpiderEvent.class);
        event.setDeferredResult(deferredResult);
        event.setRequest(request);
        event.setvCode(vCode);
        event.setState(MailSinaSpiderState.LOGIN_STEP1); // verify vcode
        event.getCurPage().set(1);
        SpiderReactor.getInstance().process(event);

        logger.info("start processing mail sina event: {}", event);
        return deferredResult;
    }

    @RequestMapping(value = "/api/mail/sina/checkresult/1.0", produces = "application/json;charset=utf-8")
    @ResponseBody
    public DeferredResult<Object> checkResult(HttpServletRequest request) {
        DeferredResult<Object> deferredResult = new DeferredResult<>();

        String userid = request.getParameter("userid");
        if (StringUtils.isEmpty(userid)) {
            SpiderException exception = new SpiderException(1006, "数据不完整");
            deferredResult.setResult(exception.toString());
            return deferredResult;
        }

        String key = String.format(Constant.MAILSINARESULTKEY, userid);
        String content = redis.getStringByKey(key);
        SpiderException exception = new SpiderException(1020, "尚未获得邮件解析结果");
        if (StringUtils.isNotEmpty(content)) {
            exception = new SpiderException(1024, "导入完成并返回结果");
            exception.setData(content);
        }
        deferredResult.setResult(exception.toString());
        return deferredResult;
    }
}
