package com.caitu99.lsp.entry;


import javax.servlet.http.HttpServletRequest;

import com.alibaba.fastjson.JSONObject;
import com.caitu99.lsp.model.spider.mailqq.MailQQSpiderEvent;
import com.caitu99.lsp.model.spider.mailqq.MailQQSpiderState;
import com.caitu99.lsp.spider.SpiderReactor;
import com.caitu99.lsp.utils.cookie.CookieHelper;
import com.caitu99.lsp.utils.cookie.HttpCookieEx;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.async.DeferredResult;

import com.alibaba.fastjson.JSON;
import com.caitu99.lsp.Constant;
import com.caitu99.lsp.cache.RedisOperate;
import com.caitu99.lsp.exception.SpiderException;

import java.util.Arrays;
import java.util.List;


@Controller
public class EmailQQ extends BaseController{

    private static final Logger logger = LoggerFactory.getLogger(EmailQQ.class);

    @Autowired
    private RedisOperate redis;

    @RequestMapping(value = "/api/mail/qq/login/1.0", produces = "application/json;charset=utf-8")
    @ResponseBody
    public DeferredResult<Object> login(HttpServletRequest request) {
    	logger.debug("收到一次请求。。。");
        DeferredResult<Object> deferredResult = new DeferredResult<>();
        //deferredResult.setResult(JSON.toJSONString(new SpiderResult(0, "success")));
        String userid = request.getParameter("userid");
        String account = request.getParameter("account");
        String password = request.getParameter("password");
        String extraPwd = request.getParameter("pwdalone");
        String date = request.getParameter("date");

        String cookie = request.getParameter("cookie");

        if (StringUtils.isNotEmpty(cookie)) { // 前端登陆
            logger.info("use front login spider: {}, {}", userid, account);
            if (StringUtils.isEmpty(userid) || StringUtils.isEmpty(account)) {
                SpiderException exception = new SpiderException(1006, "数据不完整");
                deferredResult.setResult(exception.toString());
                return deferredResult;
            }

            String[] strings = cookie.split("\\|");
            List<String> cookieStrs = Arrays.asList(strings);
            List<HttpCookieEx> cookies = CookieHelper.getCookies(cookieStrs);

            MailQQSpiderEvent event = new MailQQSpiderEvent();
            event.setDeferredResult(deferredResult);
            event.setUserid(userid);
            event.setAccount(account);
            event.setPassword(password);
            event.setExtraPwd(extraPwd);
            event.setDate(Long.parseLong(date));
            event.setRequest(request);
            event.setCookieList(cookies);

            String sid = CookieHelper.getSpecCookieValue("msid", event.getCookieList());
            event.setSid(sid);

            event.setState(MailQQSpiderState.MAILLIST);

            SpiderReactor.getInstance().process(event);
        } else { // 后端登陆
            if (StringUtils.isEmpty(userid)
                    || StringUtils.isEmpty(account)
                    || StringUtils.isEmpty(password)
                    ||StringUtils.isEmpty(date)) {
                SpiderException exception = new SpiderException(1006, "数据不完整");
                deferredResult.setResult(exception.toString());
                return deferredResult;
            }
            String key = String.format(Constant.MAILQQTASKQUEUE, account);
            String value = redis.getStringByKey(key);
            if(StringUtils.isNotBlank(value)) {
                SpiderException exception = new SpiderException(1026, "该邮箱已经在导入中");
                deferredResult.setResult(exception.toString());
                return deferredResult;
            }

            MailQQSpiderEvent event = new MailQQSpiderEvent(userid, deferredResult, request);
            event.setAccount(account);
            event.setPassword(password);
            event.setExtraPwd(extraPwd);
            event.setDate(Long.parseLong(date));
            event.setState(MailQQSpiderState.CHECK); // the first step is to check

            SpiderReactor.getInstance().process(event);

            logger.info("start processing mail qq event: {}", event);
        }

        return deferredResult;
    }

    @RequestMapping(value = "/api/mail/qq/verify/1.0", produces = "application/json;charset=utf-8")
    @ResponseBody
    public DeferredResult<Object> verify(HttpServletRequest request) {
        DeferredResult<Object> deferredResult = new DeferredResult<>();

        String userid = request.getParameter("userid");
        String vCode = request.getParameter("vcode");
        vCode = StringUtils.trim(vCode);

        if (StringUtils.isEmpty(userid)
                || StringUtils.isEmpty(vCode)) {
            SpiderException exception = new SpiderException(1006, "数据不完整");
            deferredResult.setResult(exception.toString());
            return deferredResult;
        }

        String key = String.format(Constant.mailqqImportKey, userid);
        String content = redis.getStringByKey(key);
        if (StringUtils.isEmpty(content)) {
            SpiderException exception = new SpiderException(1005, "账号验证已过期");
            deferredResult.setResult(exception.toString());
            return deferredResult;
        }

        MailQQSpiderEvent event = JSON.parseObject(content, MailQQSpiderEvent.class);
        event.setDeferredResult(deferredResult);
        event.setRequest(request);
        event.setvCode(vCode);
        event.setState(MailQQSpiderState.VFY); // verify vcode

        SpiderReactor.getInstance().process(event);

        logger.info("start processing mail qq event: {}", event);
        return deferredResult;
    }

    @RequestMapping(value = "/api/mail/qq/pwdalone/1.0", produces = "application/json;charset=utf-8")
    @ResponseBody
    public DeferredResult<Object> pwdalone(HttpServletRequest request) {
        DeferredResult<Object> deferredResult = new DeferredResult<>();

        String userid = request.getParameter("userid");
        String pwdalone = request.getParameter("pwdalone");

        if (StringUtils.isEmpty(userid)
                || StringUtils.isEmpty(pwdalone)) {
            SpiderException exception = new SpiderException(1006, "数据不完整");
            deferredResult.setResult(exception.toString());
            return deferredResult;
        }

        String key = String.format(Constant.mailqqImportKey, userid);
        String content = redis.getStringByKey(key);
        if (StringUtils.isEmpty(content)) {
            SpiderException exception = new SpiderException(1005, "账号验证已过期");
            deferredResult.setResult(exception.toString());
            return deferredResult;
        }

        MailQQSpiderEvent event = JSON.parseObject(content, MailQQSpiderEvent.class);
        event.setDeferredResult(deferredResult);
        event.setRequest(request);
        event.setExtraPwd(pwdalone);
        event.setState(MailQQSpiderState.PWDALONE); // verify pwdalone

        SpiderReactor.getInstance().process(event);

        logger.info("start processing mail qq event: {}", event);
        return deferredResult;
    }
    
    @RequestMapping(value = "/api/mail/qq/checkresult/1.0", produces = "application/json;charset=utf-8")
    @ResponseBody
    public DeferredResult<Object> checkResult(HttpServletRequest request) {
        DeferredResult<Object> deferredResult = new DeferredResult<>();

        String userid = request.getParameter("userid");
        if (StringUtils.isEmpty(userid)) {
            SpiderException exception = new SpiderException(1006, "数据不完整");
            deferredResult.setResult(exception.toString());
            return deferredResult;
        }

        String key = String.format(Constant.mailqqResultKey, userid);
        String content = redis.getStringByKey(key);
        SpiderException exception = new SpiderException(1020, "尚未获得邮件解析结果");
        if (StringUtils.isNotEmpty(content)) {
        	exception = new SpiderException(1024, "导入完成并返回结果");
        	exception.setData(content);
        } 
        deferredResult.setResult(exception.toString());
        return deferredResult;
    }

    @RequestMapping(value = "/api/mail/qq/front/login/1.0", produces = "application/json;charset=utf-8")
    @ResponseBody
    public DeferredResult<Object> frontLogin(HttpServletRequest request, Model model) {
        DeferredResult<Object> deferredResult = new DeferredResult<>();
        List<HttpCookieEx> cookies = null;

        String userid = request.getParameter("userid");
        String userAccount = request.getParameter("account");
        String cookie = request.getParameter("cookie");

        if (StringUtils.isEmpty(userid) || StringUtils.isEmpty(userAccount)) {
            SpiderException exception = new SpiderException(1006, "数据不完整");
            deferredResult.setResult(exception.toString());
            return deferredResult;
        }

        if (StringUtils.isEmpty(cookie)) {
            String key = String.format(Constant.COOKIE_STORE_KEY, userid, userAccount);
            String redisString = redis.getStringByKey(key);
            if (StringUtils.isNotEmpty(redisString)) {
                cookies = JSONObject.parseArray(redisString, HttpCookieEx.class);
            } else {
                SpiderException exception = new SpiderException(1080, "请先登录");
                deferredResult.setResult(exception);
                return deferredResult;
            }
        } else {
            String[] strings = cookie.split("\\|");
            List<String> cookieStrs = Arrays.asList(strings);
            cookies = CookieHelper.getCookies(cookieStrs);
            String redisString = JSONObject.toJSONString(cookies);
            String key = String.format(Constant.COOKIE_STORE_KEY, userid, userAccount);
            redis.set(key, redisString);
        }

        MailQQSpiderEvent event = new MailQQSpiderEvent();
        event.setDeferredResult(deferredResult);
        event.setUserid(userid);
        event.setRequest(request);
        event.setCookieList(cookies);

        String sid = CookieHelper.getSpecCookieValue("msid", event.getCookieList());
        event.setSid(sid);

        event.setState(MailQQSpiderState.MAILLIST);

        SpiderReactor.getInstance().process(event);
        return deferredResult;
    }

}
