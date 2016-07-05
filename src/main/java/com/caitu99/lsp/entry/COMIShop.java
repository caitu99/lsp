package com.caitu99.lsp.entry;


import com.alibaba.fastjson.JSON;
import com.caitu99.lsp.Constant;
import com.caitu99.lsp.cache.RedisOperate;
import com.caitu99.lsp.exception.SpiderException;
import com.caitu99.lsp.model.spider.comishop.COMIShopEvent;
import com.caitu99.lsp.model.spider.comishop.COMIShopState;
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
public class COMIShop extends BaseController {

    private static final Logger logger = LoggerFactory.getLogger(COMIShop.class);

    @Autowired
    private RedisOperate redis;

    /**
     * 验证用户是否可以下单
     *
     * @param request
     * @return
     */
    @RequestMapping(value = "/api/ishop/com/validate/1.0", produces = "application/json;charset=utf-8")
    @ResponseBody
    public DeferredResult<Object> validate(HttpServletRequest request) {
        return null;
    }

    /**
     * 登录
     *
     * @param request
     * @return
     */
    @RequestMapping(value = "/api/ishop/com/login/1.0", produces = "application/json;charset=utf-8")
    @ResponseBody
    public DeferredResult<Object> login(HttpServletRequest request) {
        logger.debug("请求登录交通银行积分乐园。。。");
        DeferredResult<Object> deferredResult = new DeferredResult<>(0);
        String userid = request.getParameter("userid");
        String account = request.getParameter("account");
        String password = request.getParameter("password");
        if (StringUtils.isEmpty(userid)
                || StringUtils.isEmpty(account)
                || StringUtils.isEmpty(password)) {
            SpiderException exception = new SpiderException(1006, "数据不完整");
            deferredResult.setResult(exception.toString());
            return deferredResult;
        }
        COMIShopEvent comiShopEvent = new COMIShopEvent();
        comiShopEvent.setDeferredResult(deferredResult);
        comiShopEvent.setUserid(userid);
        comiShopEvent.setAccount(account);
        comiShopEvent.setPassword(password);
        comiShopEvent.setState(COMIShopState.INDEX);
        SpiderReactor.getInstance().process(comiShopEvent);
        return deferredResult;
    }

    /**
     * 下单
     *
     * @param request
     * @return
     */
    @RequestMapping(value = "/api/ishop/com/order/1.0", produces = "application/json;charset=utf-8")
    @ResponseBody
    public DeferredResult<Object> order(HttpServletRequest request) {
        logger.debug("请求下单-交通。。。");
        DeferredResult<Object> deferredResult = new DeferredResult<>(0);
        String userid = request.getParameter("userid");
        String prodid = request.getParameter("prodid");
        String prodname = request.getParameter("prodname");
        String originprice = request.getParameter("originprice");
        String cashprice = request.getParameter("cashprice");
        String price = request.getParameter("price");
        String count = request.getParameter("count");
        String mobile = request.getParameter("mobile");
        String cardMonth = request.getParameter("cardMonth");
        String cardYear = request.getParameter("cardYear");
        if (StringUtils.isEmpty(userid)
                || StringUtils.isEmpty(prodid)
                || StringUtils.isEmpty(prodname)
                || StringUtils.isEmpty(originprice)
                || StringUtils.isEmpty(cashprice)
                || StringUtils.isEmpty(price)
                || StringUtils.isEmpty(count)
                || StringUtils.isEmpty(mobile)
                || StringUtils.isEmpty(cardMonth)
                || StringUtils.isEmpty(cardYear)) {
            SpiderException exception = new SpiderException(1006, "数据不完整");
            deferredResult.setResult(exception.toString());
            return deferredResult;
        }
        String key = String.format(Constant.COM_IMPORT_KEY, userid);
        String content = redis.getStringByKey(key);
        if (StringUtils.isEmpty(content)) {
            SpiderException exception = new SpiderException(1005, "账号验证已过期");
            deferredResult.setResult(exception.toString());
            return deferredResult;
        }

        COMIShopEvent comiShopEvent = JSON.parseObject(content,COMIShopEvent.class);
        comiShopEvent.setDeferredResult(deferredResult);
        comiShopEvent.setUserid(userid);
        comiShopEvent.setProdId(prodid);
        comiShopEvent.setProdName(prodname);
        comiShopEvent.setOriginPrice(originprice);
        comiShopEvent.setCashPrice(cashprice);
        comiShopEvent.setPrice(Long.valueOf(price));
        comiShopEvent.setCount(Integer.valueOf(count));
        comiShopEvent.setMobile(mobile);
        comiShopEvent.setCardMonth(cardMonth);
        comiShopEvent.setCardYear(cardYear);
        comiShopEvent.setState(COMIShopState.ORDERDETAIL1);
        SpiderReactor.getInstance().process(comiShopEvent);
        return deferredResult;
    }

    /**
     * 查积分
     *
     * @param request
     * @return
     */
    @RequestMapping(value = "/api/ishop/com/integral/1.0", produces = "application/json;charset=utf-8")
    @ResponseBody
    public DeferredResult<Object> ingetral(HttpServletRequest request) {
        logger.debug("请求查询交通积分。。。");
        DeferredResult<Object> deferredResult = new DeferredResult<>(0);
        String userid = request.getParameter("userid");
        if (StringUtils.isEmpty(userid)) {
            SpiderException exception = new SpiderException(1006, "数据不完整");
            deferredResult.setResult(exception.toString());
            return deferredResult;
        }

        String key = String.format(Constant.COM_IMPORT_KEY, userid);
        String content = redis.getStringByKey(key);
        if (StringUtils.isEmpty(content)) {
            SpiderException exception = new SpiderException(1005, "账号验证已过期");
            deferredResult.setResult(exception.toString());
            return deferredResult;
        }

        COMIShopEvent comiShopEvent = JSON.parseObject(content,COMIShopEvent.class);
        comiShopEvent.setDeferredResult(deferredResult);
        comiShopEvent.setUserid(userid);
        comiShopEvent.setState(COMIShopState.QUERY_INTEGRAL);
        SpiderReactor.getInstance().process(comiShopEvent);
        return deferredResult;
    }

    /**
     * 登录
     *
     * @param request
     * @return
     */
    @RequestMapping(value = "/api/ishop/com/login_old/1.0", produces = "application/json;charset=utf-8")
    @ResponseBody
    public DeferredResult<Object> login_old(HttpServletRequest request) {
        logger.debug("请求登录交通银行积分乐园。。。");
        DeferredResult<Object> deferredResult = new DeferredResult<>(0);
        String userid = request.getParameter("userid");
        String account = request.getParameter("account");
        String password = request.getParameter("password");
        String prodid = request.getParameter("prodid");
        String prodname = request.getParameter("prodname");
        String originprice = request.getParameter("originprice");
        String cashprice = request.getParameter("cashprice");
        String price = request.getParameter("price");
        String count = request.getParameter("count");
        String mobile = request.getParameter("mobile");
        String cardMonth = request.getParameter("cardMonth");
        String cardYear = request.getParameter("cardYear");
        if (StringUtils.isEmpty(userid)
                || StringUtils.isEmpty(account)
                || StringUtils.isEmpty(password)
                || StringUtils.isEmpty(prodid)
                || StringUtils.isEmpty(prodname)
                || StringUtils.isEmpty(originprice)
                || StringUtils.isEmpty(cashprice)
                || StringUtils.isEmpty(price)
                || StringUtils.isEmpty(count)
                || StringUtils.isEmpty(mobile)
                || StringUtils.isEmpty(cardMonth)
                || StringUtils.isEmpty(cardYear)) {
            SpiderException exception = new SpiderException(1006, "数据不完整");
            deferredResult.setResult(exception.toString());
            return deferredResult;
        }
        COMIShopEvent comiShopEvent = new COMIShopEvent();
        comiShopEvent.setDeferredResult(deferredResult);
        comiShopEvent.setUserid(userid);
        comiShopEvent.setAccount(account);
        comiShopEvent.setPassword(password);
        comiShopEvent.setProdId(prodid);
        comiShopEvent.setProdName(prodname);
        comiShopEvent.setOriginPrice(originprice);
        comiShopEvent.setCashPrice(cashprice);
        comiShopEvent.setPrice(Long.valueOf(price));
        comiShopEvent.setCount(Integer.valueOf(count));
        comiShopEvent.setMobile(mobile);
        comiShopEvent.setCardMonth(cardMonth);
        comiShopEvent.setCardYear(cardYear);
        comiShopEvent.setState(COMIShopState.INDEX);
        SpiderReactor.getInstance().process(comiShopEvent);
        return deferredResult;
    }


    /**
     * 获取短信验证码
     *
     * @param request
     * @return
     */
    @RequestMapping(value = "/api/ishop/com/sms/1.0", produces = "application/json;charset=utf-8")
    @ResponseBody
    public DeferredResult<Object> sms(HttpServletRequest request) {
        logger.debug("获取短信验证码。。。");
        DeferredResult<Object> deferredResult = new DeferredResult<>();
        String userid = request.getParameter("userid");
        if (StringUtils.isEmpty(userid)) {
            SpiderException exception = new SpiderException(1006, "数据不完整");
            deferredResult.setResult(exception.toString());
            return deferredResult;
        }
        String key = String.format(Constant.ISHOP_COM_TASK_QUEUE, userid);
        String value = redis.getStringByKey(key);
        if (StringUtils.isEmpty(value)) {
            SpiderException exception = new SpiderException(2101, "请先登录");
            deferredResult.setResult(exception.toString());
            return deferredResult;
        }
        COMIShopEvent event = JSON.parseObject(value, COMIShopEvent.class);
        //event.setState(COMIShopState.SMS1);
        event.setState(COMIShopState.PAYDETAIL3);
        event.setDeferredResult(deferredResult);
        SpiderReactor.getInstance().process(event);
        return deferredResult;
    }

    /**
     * 支付
     *
     * @param request
     * @return
     */
    @RequestMapping(value = "/api/ishop/com/pay/1.0", produces = "application/json;charset=utf-8")
    @ResponseBody
    public DeferredResult<Object> pay(HttpServletRequest request) {
        logger.debug("支付。。。");
        DeferredResult<Object> deferredResult = new DeferredResult<>();
        String userid = request.getParameter("userid");
        String smsCode = request.getParameter("smscode");
        if (StringUtils.isEmpty(userid)
                || StringUtils.isEmpty(smsCode)) {
            SpiderException exception = new SpiderException(1006, "数据不完整");
            deferredResult.setResult(exception.toString());
            return deferredResult;
        }
        String key = String.format(Constant.ISHOP_COM_TASK_QUEUE, userid);
        String value = redis.getStringByKey(key);
        if (StringUtils.isEmpty(value)) {
            SpiderException exception = new SpiderException(2103, "短信验证码失效");
            deferredResult.setResult(exception.toString());
            return deferredResult;
        }
        COMIShopEvent event = JSON.parseObject(value, COMIShopEvent.class);
        event.setSmsCode(smsCode);
        event.setState(COMIShopState.PAY1);
        event.setDeferredResult(deferredResult);
        SpiderReactor.getInstance().process(event);
        return deferredResult;
    }
}