package com.caitu99.lsp.entry;

import com.alibaba.fastjson.JSON;
import com.caitu99.lsp.Constant;
import com.caitu99.lsp.cache.RedisOperate;
import com.caitu99.lsp.exception.SpiderException;
import com.caitu99.lsp.model.spider.hotmail.MailHotmailSpiderEvent;
import com.caitu99.lsp.model.spider.hotmail.MailHotmailSpiderState;
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
 * Created by Lion on 2015/11/14 0014.
 */

@Controller
public class EamilHotmail extends BaseController {
    private static final Logger logger = LoggerFactory.getLogger(EamilHotmail.class);

    @Autowired
    private RedisOperate redis;

    @RequestMapping(value = "/api/mail/hotmail/login/1.0", produces = "application/json;charset=utf-8")
    @ResponseBody
    public DeferredResult<Object> login(HttpServletRequest request) {
        logger.debug("hotmail model: 收到一次请求...");
        DeferredResult<Object> deferredResult = new DeferredResult<>();

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
        String key = String.format(Constant.MAILHOTMAILTASKQUEUE, account);
        String value = redis.getStringByKey(key);
        if (StringUtils.isNotBlank(value)) {
            SpiderException exception = new SpiderException(1026, "该邮箱已经在导入中");
            deferredResult.setResult(exception.toString());
            return deferredResult;
        }
        MailHotmailSpiderEvent event = new MailHotmailSpiderEvent(userid, deferredResult, request);
        event.setAccount(account);
        event.setPassword(password);
        event.setDate(Long.parseLong(date));
        event.setState(MailHotmailSpiderState.DEFAULT);

        SpiderReactor.getInstance().process(event);

        logger.info("start processing hotmail event: {}", event);
        return deferredResult;
    }

    @RequestMapping(value = "/api/mail/hotmail/verify/1.0", produces = "application/json;charset=utf-8")
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

        String key = String.format(Constant.MAILHOTMAILIMPORTKEY, userid);
        String content = redis.getStringByKey(key);
        if (StringUtils.isEmpty(content)) {
            SpiderException exception = new SpiderException(1005, "账号验证已过期");
            deferredResult.setResult(exception.toString());
            return deferredResult;
        }

        MailHotmailSpiderEvent event = JSON.parseObject(content, MailHotmailSpiderEvent.class);
        event.setDeferredResult(deferredResult);
        event.setRequest(request);
        event.setvCode(vCode);
//        event.setState(); // verify vcode
        event.getCurPage().set(1);
        SpiderReactor.getInstance().process(event);

        logger.info("start processing hotmail event: {}", event);
        return deferredResult;
    }

    @RequestMapping(value = "/api/mail/hotmail/checkresult/1.0", produces = "application/json;charset=utf-8")
    @ResponseBody
    public DeferredResult<Object> checkResult(HttpServletRequest request) {
        DeferredResult<Object> deferredResult = new DeferredResult<>();

        String userid = request.getParameter("userid");
        if (StringUtils.isEmpty(userid)) {
            SpiderException exception = new SpiderException(1006, "数据不完整");
            deferredResult.setResult(exception.toString());
            return deferredResult;
        }

        String key = String.format(Constant.MAILHOTMAILRESLUTKEY, userid);
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
