package com.caitu99.lsp.entry;

import com.alibaba.fastjson.JSON;
import com.caitu99.lsp.Constant;
import com.caitu99.lsp.cache.RedisOperate;
import com.caitu99.lsp.exception.SpiderException;
import com.caitu99.lsp.model.spider.unicomshop.UnicomShopEvent;
import com.caitu99.lsp.model.spider.unicomshop.UnicomShopState;
import com.caitu99.lsp.spider.SpiderReactor;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.async.DeferredResult;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;

/**
 * 联通商城
 * Created by Administrator on 2016/1/12.
 */
@Controller
public class UnicomShop extends BaseController {
    private static final Logger logger = LoggerFactory.getLogger(UnicomShop.class);

    @Autowired
    private RedisOperate redis;

    /**
     *
     * @param request
     * @return
     */
    //登录初始化
    @RequestMapping(value = "/api/shop/unicom/init/1.0", produces = "application/json;charset=utf-8")
    @ResponseBody
    public DeferredResult<Object> init(HttpServletRequest request) {
        logger.debug("in get login page api");
        DeferredResult<Object> deferredResult = new DeferredResult<>();
        String userid = request.getParameter("userid");     //用户id
        
        if(StringUtils.isEmpty(userid)
                ) {
            SpiderException spiderException = new SpiderException(1006, "数据不完整");
            deferredResult.setResult(spiderException.toString());
            return deferredResult;
        }
        
        String key = String.format(Constant.UNICOM_KEY, userid);
        String json = redis.getStringByKey(key);
        if (!StringUtils.isEmpty(json)) {
            //如果已经登录，那么更新一下信息
            UnicomShopEvent event = JSON.parseObject(json, UnicomShopEvent.class);
            if(event.getState() == UnicomShopState.RECHARGE_TO_SELF ||//判断之前是否已经完成过订单
            event.getState() == UnicomShopState.OK)
            {
                logger.debug("之前已经完成过订单，可以直接获取信息并下单");
                event.setState(UnicomShopState.GET_HOME_PAGE_AFTER_LOGIN);  //要重新获取信息，主要是积分
                event.setDeferredResult(deferredResult);
                SpiderReactor.getInstance().process(event);
                logger.info("开始获取账户信息 : {}", event);
                return deferredResult;
            }

        }
        //
        logger.debug("需要登录联通商城");
        UnicomShopEvent event = new UnicomShopEvent();
        event.setUserid(userid);
        event.setDeferredResult(deferredResult);
        event.setState(UnicomShopState.GET_LOGIN_PAGE);
        SpiderReactor.getInstance().process(event);
        logger.debug("开始登录联通商城");
        return deferredResult;
    }

    //获取登录验证码
    /*@RequestMapping(value = "/api/shop/unicom/getvcode/1.0", produces = "application/json;charset=utf-8")
    @ResponseBody
    public DeferredResult<Object> getvcode(HttpServletRequest request) {
        logger.debug("in get vcode api");
        DeferredResult<Object> deferredResult = new DeferredResult<>();
        String userid = request.getParameter("userid");


        if(StringUtils.isEmpty(userid)) {
            SpiderException spiderException = new SpiderException(1006, "数据不完整");
            deferredResult.setResult(spiderException.toString());
            return deferredResult;
        }

        String key = String.format(Constant.UNICOM_KEY, userid);
        String json = redis.getStringByKey(key);
        if (StringUtils.isEmpty(json)) {
            SpiderException exception = new SpiderException(1005, "账号验证已过期");
            deferredResult.setResult(exception.toString());
            return deferredResult;
        }
        UnicomShopEvent event = JSON.parseObject(json, UnicomShopEvent.class);
        if((event.getState() != UnicomShopState.GET_VCODE) && (event.getState() != UnicomShopState.LOGIN))
        {
            logger.error("状态值不对，{}",event.getState());
            SpiderException exception = new SpiderException(1203, "状态值不对");
            redis.del(key);
            deferredResult.setResult(exception.toString());
            return deferredResult;
        }
        event.setState(UnicomShopState.GET_VCODE);
        event.setDeferredResult(deferredResult);
        SpiderReactor.getInstance().process(event);
        logger.info("start get vcode : {}", event);
        return deferredResult;
    }*/
    
    @RequestMapping(value = "/api/shop/unicom/getvcode/1.0", produces = "application/json;charset=utf-8")
    @ResponseBody
    public DeferredResult<Object> getvcode(HttpServletRequest request) {
        logger.debug("in get vcode api");
        DeferredResult<Object> deferredResult = new DeferredResult<>();
        String userid = request.getParameter("userid");


        if(StringUtils.isEmpty(userid)) {
            SpiderException spiderException = new SpiderException(1006, "数据不完整");
            deferredResult.setResult(spiderException.toString());
            return deferredResult;
        }

       
        UnicomShopEvent event = new UnicomShopEvent();
        event.setUserid(userid);
        event.setDeferredResult(deferredResult);
        event.setState(UnicomShopState.GET_LOGIN_PAGE);
        SpiderReactor.getInstance().process(event);
        logger.info("start get vcode : {}", event);
        return deferredResult;
    }

    //向服务器请求检查是否已经登录
    @RequestMapping(value = "/api/shop/unicom/get/login/state/1.0", produces = "application/json;charset=utf-8")
    @ResponseBody
    public DeferredResult<Object> getLoginState(HttpServletRequest request) {
        logger.debug("get login state api");
        DeferredResult<Object> deferredResult = new DeferredResult<>();
        String userid = request.getParameter("userid");

        if(StringUtils.isEmpty(userid)) {
            SpiderException spiderException = new SpiderException(1006, "数据不完整");
            deferredResult.setResult(spiderException.toString());
            return deferredResult;
        }

        String key = String.format(Constant.UNICOM_KEY, userid);
        String json = redis.getStringByKey(key);
        if (StringUtils.isEmpty(json)) {
            SpiderException exception = new SpiderException(1005, "账号验证已过期");
            deferredResult.setResult(exception.toString());
            return deferredResult;
        }
        UnicomShopEvent event = JSON.parseObject(json, UnicomShopEvent.class);
        if(event.getState() != UnicomShopState.GET_LOGIN_STATE)
        {
            SpiderException exception = new SpiderException(1203, "状态值不对");
            redis.del(key);
            deferredResult.setResult(exception.toString());
            return deferredResult;
        }
        event.setDeferredResult(deferredResult);
        SpiderReactor.getInstance().process(event);
        logger.info("start get login state: {}", event);
        return deferredResult;
    }

    //登录
    @RequestMapping(value="/api/shop/unicom/login/1.0",
            produces = "application/json;charset=utf-8",
            method={RequestMethod.GET,RequestMethod.POST})
    @ResponseBody
    public DeferredResult<Object> login(HttpServletRequest request){
        logger.debug("in login api");
        DeferredResult<Object> deferredResult = new DeferredResult<>();
        String userid = request.getParameter("userid");
        String account = request.getParameter("account");
        String password = request.getParameter("password");
        String vcode = request.getParameter("vcode");
        String self = request.getParameter("self");

        if (StringUtils.isEmpty(userid)||
                StringUtils.isEmpty(account)||
                StringUtils.isEmpty(password)||
                StringUtils.isEmpty(self)||
                StringUtils.isEmpty(vcode) //todo 待确定是否每次都需要验证码
                ) {
            SpiderException spiderException = new SpiderException(1006, "数据不完整");
            deferredResult.setResult(spiderException.toString());
            return deferredResult;
        }
        String key = String.format(Constant.UNICOM_KEY,userid);
        String json = redis.getStringByKey(key);
        if (StringUtils.isEmpty(json)) {
            SpiderException exception = new SpiderException(1005, "账号验证已过期");
            deferredResult.setResult(exception.toString());
            return deferredResult;
        }
        UnicomShopEvent event = JSON.parseObject(json, UnicomShopEvent.class);
        if((event.getState() != UnicomShopState.LOGIN) && (event.getState() !=UnicomShopState.GET_VCODE))
        {
            SpiderException exception = new SpiderException(1203, "状态值不对");
            redis.del(key);
            deferredResult.setResult(exception.toString());
            return deferredResult;
        }
        try {
            event.setSelf(Boolean.valueOf(self));
        } catch (Exception e) {
            SpiderException spiderException = new SpiderException(1204, "给自己充值参数无法转换为布尔值");
            deferredResult.setResult(spiderException.toString());
            return deferredResult;
        }
        event.setState(UnicomShopState.LOGIN);
        event.setDeferredResult(deferredResult);
        event.setAccount(account);
        event.setPassword(password);
        event.setvCode(vcode);
        SpiderReactor.getInstance().process(event);
        logger.info("start login : {}", event);
        return deferredResult;
    }

    //获取短信验证码
    @RequestMapping(value="/api/shop/unicom/getsmscode/1.0",
            produces = "application/json;charset=utf-8",
            method={RequestMethod.GET,RequestMethod.POST})
    @ResponseBody
    public DeferredResult<Object> getSmsCode(HttpServletRequest request){
        logger.debug("in get sms code api");
        DeferredResult<Object> deferredResult = new DeferredResult<>();
        String userid = request.getParameter("userid");
        String giftId = request.getParameter("giftid");     //商品编号
        String price = request.getParameter("price"); //商品价格
        String nums = request.getParameter("nums");         //购买数量
        
        if (StringUtils.isEmpty(userid)||
        		StringUtils.isEmpty(giftId)||
                StringUtils.isEmpty(price)||
                StringUtils.isEmpty(nums)
                ){
            SpiderException spiderException = new SpiderException(1006, "数据不完整");
            deferredResult.setResult(spiderException.toString());
            return deferredResult;
        }
        Integer giftCostint = null;
        try {
            giftCostint = Integer.valueOf(price);
        } catch (Exception e) {
            SpiderException spiderException = new SpiderException(1200, "商品价格无法转换为数字");
            deferredResult.setResult(spiderException.toString());
            return deferredResult;
        }
        Integer numsint = null;
        try {
            numsint = Integer.valueOf(nums);
        } catch (Exception e) {
            SpiderException spiderException = new SpiderException(1202, "购买数量无法转换为数字");//todo
            deferredResult.setResult(spiderException.toString());
            return deferredResult;
        }
        if(numsint ==0 || numsint == null)
        {
            SpiderException spiderException = new SpiderException(1201, "购买数量不能为0");//todo
            deferredResult.setResult(spiderException.toString());
            return deferredResult;
        }
        
        String key = String.format(Constant.UNICOM_KEY,userid);
        String json = redis.getStringByKey(key);
        if (StringUtils.isEmpty(json)) {
            SpiderException exception = new SpiderException(1005, "账号验证已过期");
            deferredResult.setResult(exception.toString());
            return deferredResult;
        }
        UnicomShopEvent event = JSON.parseObject(json, UnicomShopEvent.class);
//        if((event.getState() != UnicomShopState.SEND_SMS) && (event.getState()!= UnicomShopState.CHECK_SMS_CODE))
//        {
//            SpiderException exception = new SpiderException(1203, "状态值不对");
//            deferredResult.setResult(exception.toString());
//            return deferredResult;
//        }
        if(StringUtils.isEmpty(event.getIndexUrl())){
        	 SpiderException exception = new SpiderException(1005, "账号验证已过期");
             deferredResult.setResult(exception.toString());
             return deferredResult;
        }
        event.setGiftId(giftId);
        event.setNums(numsint);
        event.setGiftCost(giftCostint);
//        event.setState(UnicomShopState.ORDER);
        event.setState(UnicomShopState.GET_HOME_PAGE_AFTER_LOGIN);
        if(event.getDate() != null && (new Date().getTime()-event.getDate().getTime() < 60*1000))
        {
            SpiderException exception = new SpiderException(1226, "请60秒后再请求发送短信");
            deferredResult.setResult(exception.toString());
            return deferredResult;
        }
        event.setDeferredResult(deferredResult);
        SpiderReactor.getInstance().process(event);
        logger.info("start get sms code : {}", event);
        return deferredResult;
    }

    //验证短信，提交订单
    @RequestMapping(value="/api/shop/unicom/submit/1.0",
            produces = "application/json;charset=utf-8",
            method={RequestMethod.GET,RequestMethod.POST})
    @ResponseBody
    public DeferredResult<Object> submit(HttpServletRequest request){
        logger.debug("in login api");
        DeferredResult<Object> deferredResult = new DeferredResult<>();
        String userid = request.getParameter("userid");
        String smscode = request.getParameter("smscode");
        if (StringUtils.isEmpty(userid)||
            StringUtils.isEmpty(smscode)
                ) {
            SpiderException spiderException = new SpiderException(1006, "数据不完整");
            deferredResult.setResult(spiderException.toString());
            return deferredResult;
        }
        String key = String.format(Constant.UNICOM_KEY, userid);
        String json = redis.getStringByKey(key);
        if (StringUtils.isEmpty(json)) {
            SpiderException exception = new SpiderException(1005, "账号验证已过期");
            deferredResult.setResult(exception.toString());
            return deferredResult;
        }
        UnicomShopEvent event = JSON.parseObject(json, UnicomShopEvent.class);
        if((event.getState() != UnicomShopState.CHECK_SMS_CODE) &&(event.getState() !=UnicomShopState.SEND_SMS))
        {
            SpiderException exception = new SpiderException(1203, "状态值不对");
            deferredResult.setResult(exception.toString());
            return deferredResult;
        }
        event.setState(UnicomShopState.CHECK_SMS_CODE);
        event.setDeferredResult(deferredResult);
        event.setSms(smscode);
        SpiderReactor.getInstance().process(event);
        logger.info("start submit : {}", event);
        return deferredResult;
    }
}
