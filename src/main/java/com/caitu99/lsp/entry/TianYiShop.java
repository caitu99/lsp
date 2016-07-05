/*
 * Copyright (c) 2015-2020 by caitu99
 * All rights reserved.
 */
package com.caitu99.lsp.entry;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.async.DeferredResult;

import com.alibaba.fastjson.JSON;
import com.caitu99.lsp.Constant;
import com.caitu99.lsp.cache.RedisOperate;
import com.caitu99.lsp.exception.SpiderException;
import com.caitu99.lsp.model.spider.tianyi.TianYiShopSpiderEvent;
import com.caitu99.lsp.model.spider.tianyi.TianYiShopSpiderState;
import com.caitu99.lsp.spider.SpiderReactor;

/**
 * 天翼积分商城
 * 
 * @Description: (类职责详细描述,可空) 
 * @ClassName: TianYiShop 
 * @author ws
 * @date 2016年2月14日 下午5:06:30 
 * @Copyright (c) 2015-2020 by caitu99
 */
@Controller
public class TianYiShop {

    private static final Logger logger = LoggerFactory
            .getLogger(TianYiShop.class);
    @Autowired
    private RedisOperate redis;


    /**
     * 获取图片验证码
     *
     * @return Map:code,message,data
     * @throws IOException
     * @Description: (方法职责详细描述, 可空)
     * @Title: getImgCode
     * @date 2015年11月10日 上午11:46:33
     * @author chencheng
     */
    @RequestMapping(value = "/tianyishop/imgcode/get/1.0", produces = "application/json;charset=utf-8")
    @ResponseBody
    public DeferredResult<Object> getImgCode(HttpServletRequest request) {

        DeferredResult<Object> deferredResult = new DeferredResult<>();
        String userId = request.getParameter("userid");

        if (StringUtils.isEmpty(userId)) {
            SpiderException exception = new SpiderException(1006, "数据不完整");
            deferredResult.setResult(exception.toString());
            return deferredResult;
        }

        TianYiShopSpiderEvent event = new TianYiShopSpiderEvent(userId, deferredResult);
        event.setUserid(userId);
        event.setState(TianYiShopSpiderState.NONE);//首先进入首页，获取cookie

        SpiderReactor.getInstance().process(event);

        return deferredResult;
    }

    @RequestMapping(value = "/tianyishop/imgcode/check/1.0", produces = "application/json;charset=utf-8")
    @ResponseBody
    public DeferredResult<Object> checkImgCode(HttpServletRequest request) {
        DeferredResult<Object> deferredResult = new DeferredResult<>();
        String userid = request.getParameter("userid");
        String account = request.getParameter("phoneNo");
        String yzm = request.getParameter("imgCode");

        if (StringUtils.isEmpty(userid)
                || StringUtils.isEmpty(account)
                || StringUtils.isEmpty(yzm)) {
            SpiderException exception = new SpiderException(1006, "数据不完整");
            deferredResult.setResult(exception.toString());
            return deferredResult;
        }

        // 获取缓存事件内容
        String key = String.format(Constant.TIAN_YI_SHOP_IMPORT_KEY, userid); 
        String content = redis.getStringByKey(key);
        if (StringUtils.isEmpty(content)) {
            SpiderException exception = new SpiderException(1005, "验证码已过期");
            deferredResult.setResult(exception.toString());
            return deferredResult;
        }

        TianYiShopSpiderEvent event = JSON.parseObject(content, TianYiShopSpiderEvent.class);
        event.setDeferredResult(deferredResult);
        event.setAccount(account);
        event.setvCode(yzm);
        event.setUserid(userid);
        event.setState(TianYiShopSpiderState.CHECK); // 

        SpiderReactor.getInstance().process(event);

        return deferredResult;
    }


    @RequestMapping(value = "/tianyishop/login/1.0", produces = "application/json;charset=utf-8")
    @ResponseBody
    public DeferredResult<Object> login(HttpServletRequest request) {
        DeferredResult<Object> deferredResult = new DeferredResult<>();
        String userid = request.getParameter("userid");
        String account = request.getParameter("phoneNo");
        String msgCode = request.getParameter("msCode");

        if (StringUtils.isEmpty(userid)
                || StringUtils.isEmpty(account)
                || StringUtils.isEmpty(msgCode)) {
            SpiderException exception = new SpiderException(1006, "数据不完整");
            deferredResult.setResult(exception.toString());
            return deferredResult;
        }

        // 获取缓存事件内容
        String key = String.format(Constant.TIAN_YI_SHOP_IMPORT_KEY, userid);
        String content = redis.getStringByKey(key);
        if (StringUtils.isEmpty(content)) {
            SpiderException exception = new SpiderException(1005, "验证码已过期");
            deferredResult.setResult(exception.toString());
            return deferredResult;
        }

        TianYiShopSpiderEvent event = JSON.parseObject(content, TianYiShopSpiderEvent.class);
        event.setDeferredResult(deferredResult);
        event.setAccount(account);
        event.setMsgCode(msgCode);
        event.setUserid(userid);
        event.setState(TianYiShopSpiderState.LOGIN); //

        SpiderReactor.getInstance().process(event);

        return deferredResult;
    }

    @RequestMapping(value = "/tianyishop/order/1.0", produces = "application/json;charset=utf-8")
    @ResponseBody
    public DeferredResult<Object> order(HttpServletRequest request) {
        DeferredResult<Object> deferredResult = new DeferredResult<>();
        String userid = request.getParameter("userid");
        String deviceType = request.getParameter("deviceType");
        String SystemType = request.getParameter("SystemType");
        String buyNum = request.getParameter("buyNum");
        String commodityID = request.getParameter("commodityID");
        String payTotal = request.getParameter("payTotal");
        String payFlag = request.getParameter("payFlag");

        if (StringUtils.isEmpty(userid)
                || StringUtils.isEmpty(deviceType)
                || StringUtils.isEmpty(SystemType)
                || StringUtils.isEmpty(buyNum)
                || StringUtils.isEmpty(commodityID)
                || StringUtils.isEmpty(payTotal)
                || StringUtils.isEmpty(payFlag)) {
            SpiderException exception = new SpiderException(1006, "数据不完整");
            deferredResult.setResult(exception.toString());
            return deferredResult;
        }

        // 获取缓存事件内容
        String key = String.format(Constant.TIAN_YI_SHOP_IMPORT_KEY, userid);
        String content = redis.getStringByKey(key);
        if (StringUtils.isEmpty(content)) {
            SpiderException exception = new SpiderException(1005, "验证码已过期");
            deferredResult.setResult(exception.toString());
            return deferredResult;
        }

        TianYiShopSpiderEvent event = JSON.parseObject(content, TianYiShopSpiderEvent.class);
        event.setDeferredResult(deferredResult);
        event.setUserid(userid);
        event.setDeviceType(deviceType);
        event.setSystemType(SystemType);
        event.setBuyNum(buyNum);
        event.setCommodityID(commodityID);
        event.setPayTotal(payTotal);
        event.setPayFlag(payFlag);
        event.setState(TianYiShopSpiderState.ORDER); //

        SpiderReactor.getInstance().process(event);

        return deferredResult;
    }


    @RequestMapping(value = "/tianyishop/order_old/1.0", produces = "application/json;charset=utf-8")
    @ResponseBody
    public DeferredResult<Object> order_old(HttpServletRequest request) {
        DeferredResult<Object> deferredResult = new DeferredResult<>();
        String userid = request.getParameter("userid");
        String account = request.getParameter("phoneNo");
        String msgCode = request.getParameter("msCode");
        String deviceType = request.getParameter("deviceType");
        String SystemType = request.getParameter("SystemType");
        String buyNum = request.getParameter("buyNum");
        String commodityID = request.getParameter("commodityID");
        String payTotal = request.getParameter("payTotal");
        String payFlag = request.getParameter("payFlag");

        if (StringUtils.isEmpty(userid)
                || StringUtils.isEmpty(account)
                || StringUtils.isEmpty(msgCode)
                || StringUtils.isEmpty(deviceType)
                || StringUtils.isEmpty(SystemType)
                || StringUtils.isEmpty(buyNum)
                || StringUtils.isEmpty(commodityID)
                || StringUtils.isEmpty(payTotal)
                || StringUtils.isEmpty(payFlag)) {
            SpiderException exception = new SpiderException(1006, "数据不完整");
            deferredResult.setResult(exception.toString());
            return deferredResult;
        }

        // 获取缓存事件内容
        String key = String.format(Constant.TIAN_YI_SHOP_IMPORT_KEY, userid);
        String content = redis.getStringByKey(key);
        if (StringUtils.isEmpty(content)) {
            SpiderException exception = new SpiderException(1005, "验证码已过期");
            deferredResult.setResult(exception.toString());
            return deferredResult;
        }

        TianYiShopSpiderEvent event = JSON.parseObject(content, TianYiShopSpiderEvent.class);
        event.setDeferredResult(deferredResult);
        event.setAccount(account);
        event.setMsgCode(msgCode);
        event.setUserid(userid);
        event.setDeviceType(deviceType);
        event.setSystemType(SystemType);
        event.setBuyNum(buyNum);
        event.setCommodityID(commodityID);
        event.setPayTotal(payTotal);
        event.setPayFlag(payFlag);
        event.setState(TianYiShopSpiderState.LOGIN); // 

        SpiderReactor.getInstance().process(event);

        return deferredResult;
    }
    

    @RequestMapping(value = "/tianyishop/pay/1.0", produces = "application/json;charset=utf-8")
    @ResponseBody
    public DeferredResult<Object> pay(HttpServletRequest request) {
        DeferredResult<Object> deferredResult = new DeferredResult<>();
        String userid = request.getParameter("userid");
        String rndCode = request.getParameter("rndCode");

        if (StringUtils.isEmpty(userid)
                || StringUtils.isEmpty(rndCode)) {
            SpiderException exception = new SpiderException(1006, "数据不完整");
            deferredResult.setResult(exception.toString());
            return deferredResult;
        }

        // 获取缓存事件内容
        String key = String.format(Constant.TIAN_YI_SHOP_IMPORT_KEY, userid);
        String content = redis.getStringByKey(key);
        if (StringUtils.isEmpty(content)) {
            SpiderException exception = new SpiderException(1005, "验证码已过期");
            deferredResult.setResult(exception.toString());
            return deferredResult;
        }

        TianYiShopSpiderEvent event = JSON.parseObject(content, TianYiShopSpiderEvent.class);
        event.setDeferredResult(deferredResult);
        event.setRndCode(rndCode);
        event.setUserid(userid);
        event.setState(TianYiShopSpiderState.GET_JF); // 支付

        SpiderReactor.getInstance().process(event);

        return deferredResult;
    }
    

    /**
     * 支付时再次获取短信验证码
     * 	
     * @Description: (方法职责详细描述,可空)  
     * @Title: order 
     * @param request
     * @return
     * @date 2016年2月17日 下午5:19:01  
     * @author ws
     */
    @RequestMapping(value = "/tianyishop/paymsg/1.0", produces = "application/json;charset=utf-8")
    @ResponseBody
    public DeferredResult<Object> paymsg(HttpServletRequest request) {
        DeferredResult<Object> deferredResult = new DeferredResult<>();
        String userid = request.getParameter("userid");
        
        if (StringUtils.isEmpty(userid)) {
            SpiderException exception = new SpiderException(1006, "数据不完整");
            deferredResult.setResult(exception.toString());
            return deferredResult;
        }

        // 获取缓存事件内容
        String key = String.format(Constant.TIAN_YI_SHOP_IMPORT_KEY, userid);
        String content = redis.getStringByKey(key);
        if (StringUtils.isEmpty(content)) {
            SpiderException exception = new SpiderException(1005, "验证码已过期");
            deferredResult.setResult(exception.toString());
            return deferredResult;
        }

        TianYiShopSpiderEvent event = JSON.parseObject(content, TianYiShopSpiderEvent.class);
        event.setDeferredResult(deferredResult);
        event.setUserid(userid);
        event.setState(TianYiShopSpiderState.MSG_PAY); //再次获取短信验证码

        SpiderReactor.getInstance().process(event);

        return deferredResult;
    }
    
    
    /**
     * 	获取订单兑换卷
     * @Description: (方法职责详细描述,可空)  
     * @Title: getOrderCode 
     * @param request
     * @return
     * @date 2016年2月24日 下午12:06:53  
     * @author fangjunxiao
     */
    @RequestMapping(value = "/tianyishop/getcode/1.0", produces = "application/json;charset=utf-8")
    @ResponseBody
    public DeferredResult<Object> getOrderCode(HttpServletRequest request){
    	 DeferredResult<Object> deferredResult = new DeferredResult<>();
         
         String userId = request.getParameter("userid");
         if (StringUtils.isEmpty(userId)) {
             SpiderException exception = new SpiderException(1006, "数据不完整");
             deferredResult.setResult(exception.toString());
             return deferredResult;
         }

         // 获取缓存事件内容
         String key = String.format(Constant.TIAN_YI_SHOP_IMPORT_KEY, userId);
         String content = redis.getStringByKey(key);
         if (StringUtils.isEmpty(content)) {
             SpiderException exception = new SpiderException(1005, "验证码已过期");
             deferredResult.setResult(exception.toString());
             return deferredResult;
         }
         
         TianYiShopSpiderEvent event = JSON.parseObject(content, TianYiShopSpiderEvent.class);
         event.setDeferredResult(deferredResult);
         event.setUserid(userId);
         event.setState(TianYiShopSpiderState.GET_CODE);
         SpiderReactor.getInstance().process(event);
         
    	 return deferredResult;
    }
    
}
