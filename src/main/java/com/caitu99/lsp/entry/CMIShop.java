package com.caitu99.lsp.entry;


import com.alibaba.fastjson.JSON;
import com.caitu99.lsp.Constant;
import com.caitu99.lsp.cache.RedisOperate;
import com.caitu99.lsp.exception.SpiderException;
import com.caitu99.lsp.model.spider.cmishop.CMIShopEvent;
import com.caitu99.lsp.model.spider.cmishop.CMIShopState;
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
public class CMIShop extends BaseController {

    private static final Logger logger = LoggerFactory.getLogger(CMIShop.class);

    @Autowired
    private RedisOperate redis;

    /**
     * 验证用户是否可以下单
     *
     * @param request
     * @return
     */
    @RequestMapping(value = "/api/ishop/cm/validate/1.0", produces = "application/json;charset=utf-8")
    @ResponseBody
    public DeferredResult<Object> validate(HttpServletRequest request) {
        logger.debug("开始验证用户是否可以下单。。。");
        DeferredResult<Object> deferredResult = new DeferredResult<>();
        String userid = request.getParameter("userid");
        String price = request.getParameter("price");
        String wanlitongAccount = request.getParameter("wanlitongAccount");
        if (StringUtils.isEmpty(userid) || StringUtils.isEmpty(price)) {
            SpiderException exception = new SpiderException(1006, "数据不完整");
            deferredResult.setResult(exception.toString());
            return deferredResult;
        }
        String key = String.format(Constant.ISHOP_CM_LOGIN_EVENT, userid);
        String content = redis.getStringByKey(key);
        if (StringUtils.isEmpty(content)) {
            SpiderException exception = new SpiderException(1081, "用户尚未登录");
            deferredResult.setResult(exception.toString());
            return deferredResult;
        }
        CMIShopEvent cmiShopEvent = JSON.parseObject(content, CMIShopEvent.class);
        logger.debug("验证用户是否真的处于登录状态。。。");
        cmiShopEvent.setState(CMIShopState.HOMEPAGE);
        cmiShopEvent.setDeferredResult(deferredResult);
        cmiShopEvent.setWanlitongAccount(wanlitongAccount);
        SpiderReactor.getInstance().process(cmiShopEvent);
        logger.debug("数据返回。。。");
        return deferredResult;
    }

    /**
     * 获取短信验证码
     *
     * @param request
     * @return
     */
    @RequestMapping(value = "/api/ishop/cm/vcode/get/1.0", produces = "application/json;charset=utf-8")
    @ResponseBody
    public DeferredResult<Object> getVcode(HttpServletRequest request) {
        logger.info("获取验证码。。。");
        DeferredResult<Object> deferredResult = new DeferredResult<>();
        String userid = request.getParameter("userid");
        String account = request.getParameter("account");
        if (StringUtils.isEmpty(userid)
                || StringUtils.isEmpty(account)) {
            SpiderException exception = new SpiderException(1006, "数据不完整");
            deferredResult.setResult(exception.toString());
            return deferredResult;
        }

        CMIShopEvent event = new CMIShopEvent(userid, deferredResult);
        event.setAccount(account);
        event.setState(CMIShopState.INIT);
        event.setDeferredResult(deferredResult);
        SpiderReactor.getInstance().process(event);
        logger.info("start get sms login: {}", event);
        return deferredResult;
    }

    /**
     * 登录
     *
     * @param request
     * @return
     */
    @RequestMapping(value = "/api/ishop/cm/login/1.0", produces = "application/json;charset=utf-8")
    @ResponseBody
    public DeferredResult<Object> login(HttpServletRequest request) {
        logger.debug("请求登录CM。。。");
        DeferredResult<Object> deferredResult = new DeferredResult<>(0);
        String userid = request.getParameter("userid");
        String account = request.getParameter("account");
        String password = request.getParameter("password");
        String vcode = request.getParameter("vcode");
        if (StringUtils.isEmpty(userid)
                || StringUtils.isEmpty(account)
                || StringUtils.isEmpty(password)) {
            SpiderException exception = new SpiderException(1006, "数据不完整");
            deferredResult.setResult(exception.toString());
            return deferredResult;
        }
        String key = String.format(Constant.ISHOP_CM_TASK_QUEUE, account);
        String value = redis.getStringByKey(key);
        CMIShopEvent cmiShopEvent;
        if (StringUtils.isNotBlank(value)) {
            if (StringUtils.isEmpty(vcode)) {
                SpiderException exception = new SpiderException(1006, "数据不完整");
                deferredResult.setResult(exception.toString());
                return deferredResult;
            }
            cmiShopEvent = JSON.parseObject(value, CMIShopEvent.class);
            cmiShopEvent.setvCode(vcode);
            cmiShopEvent.setState(CMIShopState.LOGIN);//直接请求登录
        } else {
            cmiShopEvent = new CMIShopEvent(userid, deferredResult);
            cmiShopEvent.setAccount(account);
            cmiShopEvent.setState(CMIShopState.LOGINPAGE);//从请求登录页面开始
        }
        cmiShopEvent.setDeferredResult(deferredResult);
        cmiShopEvent.setPassword(password);
        cmiShopEvent.setIsLogin(true);
        SpiderReactor.getInstance().process(cmiShopEvent);
        logger.info("start processing login: {}", cmiShopEvent);
        return deferredResult;
    }

    /**
     * 获取短信
     *
     * @param request
     * @return
     */
    @RequestMapping(value = "/api/ishop/cm/sms/1.0", produces = "application/json;charset=utf-8")
    @ResponseBody
    public DeferredResult<Object> sms(HttpServletRequest request) {
        logger.debug("获取移动商城下单短信。。。");
        DeferredResult<Object> deferredResult = new DeferredResult<>(0);
        String userid = request.getParameter("userid");
        String hisProductv3 = request.getParameter("hisProductv3");
        String wareIds = request.getParameter("wareIds");
        String amount = request.getParameter("amount");
        String curAllIntegral =  request.getParameter("price");
        if(StringUtils.isEmpty(amount)){
            amount = "1";
        }
        String wanlitongAccount = request.getParameter("wanlitongAccount");
        //hisProductv3 = "%5B%7B%22wareid%22%3A%22100000000503699%22%2C%22warename%22%3A%2230%E5%85%83%E8%AF%9D%E8%B4%B9%E7%9B%B4%E5%85%85%22%2C%22warepic%22%3A%2299%2F99%2F346031_1307503976361_240.jpg%22%2C%22base_value%22%3A%222500%22%2C%22payType%22%3A%2201%2C02%22%7D%5D";
        if (StringUtils.isEmpty(userid) || StringUtils.isEmpty(wareIds)) {
            SpiderException exception = new SpiderException(1006, "数据不完整");
            deferredResult.setResult(exception.toString());
            return deferredResult;
        }
        String key = String.format(Constant.ISHOP_CM_LOGIN_EVENT, userid);
        String content = redis.getStringByKey(key);
        if (StringUtils.isEmpty(content)) {
            SpiderException exception = new SpiderException(1081, "用户尚未登录");
            deferredResult.setResult(exception.toString());
            return deferredResult;
        }
        CMIShopEvent cmiShopEvent = JSON.parseObject(content, CMIShopEvent.class);
        cmiShopEvent.setHisProductv3(hisProductv3);
        cmiShopEvent.setWareIds(wareIds);
        cmiShopEvent.setDeferredResult(deferredResult);
        cmiShopEvent.setAmount(Integer.parseInt(amount));
        cmiShopEvent.setWanlitongAccount(wanlitongAccount);
        cmiShopEvent.setCurAllIntegral(curAllIntegral);
        cmiShopEvent.setState(CMIShopState.ORDERDETAIL);
        SpiderReactor.getInstance().process(cmiShopEvent);
        logger.info("start get sms: {}", cmiShopEvent);
        return deferredResult;
    }

    /**
     * 下单
     *
     * @param request
     * @return
     */
    @RequestMapping(value = "/api/ishop/cm/order/1.0", produces = "application/json;charset=utf-8")
    @ResponseBody
    public DeferredResult<Object> order(HttpServletRequest request) {
        logger.debug("移动积分商城下单。。。");
        DeferredResult<Object> deferredResult = new DeferredResult<>(0);
        String userid = request.getParameter("userid");
        String smsCode = request.getParameter("smsCode");
        String wareIds = request.getParameter("wareIds");
        if (StringUtils.isEmpty(userid)
                || StringUtils.isEmpty(smsCode)
                || StringUtils.isEmpty(wareIds)) {
            SpiderException exception = new SpiderException(1006, "数据不完整");
            deferredResult.setResult(exception.toString());
            return deferredResult;
        }
        String key = String.format(Constant.ISHOP_CM_LOGIN_EVENT, userid);
        String content = redis.getStringByKey(key);
        if (StringUtils.isEmpty(content)) {
            SpiderException exception = new SpiderException(1081, "用户尚未登录");
            deferredResult.setResult(exception.toString());
            return deferredResult;
        }
        CMIShopEvent cmiShopEvent = JSON.parseObject(content, CMIShopEvent.class);
        cmiShopEvent.setSmsCode(smsCode);
        cmiShopEvent.setWareIds(wareIds);
        cmiShopEvent.setState(CMIShopState.ORDER);
        cmiShopEvent.setDeferredResult(deferredResult);
        SpiderReactor.getInstance().process(cmiShopEvent);
        logger.info("start order: {}", cmiShopEvent);
        return deferredResult;
    }

}