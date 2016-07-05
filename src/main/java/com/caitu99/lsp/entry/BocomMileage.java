package com.caitu99.lsp.entry;


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
import com.caitu99.lsp.model.spider.comishop.BocomMileageEvent;
import com.caitu99.lsp.model.spider.comishop.BocomMileageState;
import com.caitu99.lsp.model.spider.comishop.COMIShopEvent;
import com.caitu99.lsp.model.spider.comishop.COMIShopState;
import com.caitu99.lsp.spider.SpiderReactor;

/**
 * 交通银行航空里程兑换
 * 
 * @Description: (类职责详细描述,可空) 
 * @ClassName: BocomMileage 
 * @author ws
 * @date 2016年4月27日 下午12:27:49 
 * @Copyright (c) 2015-2020 by caitu99
 */
@Controller
public class BocomMileage extends BaseController {

    private static final Logger logger = LoggerFactory.getLogger(BocomMileage.class);

    @Autowired
    private RedisOperate redis;

    /**
     * 登录
     *
     * @param request
     * @return
     */
    @RequestMapping(value = "/bocom/login/1.0", produces = "application/json;charset=utf-8")
    @ResponseBody
    public DeferredResult<Object> login(HttpServletRequest request) {
        logger.debug("请求登录交通银行积分乐园。。。");
        DeferredResult<Object> deferredResult = new DeferredResult<>(0);
        String userid = request.getParameter("userid");
        String account = request.getParameter("account");
        String password = request.getParameter("password");
        
        String memberId = request.getParameter("memberId");//航空公司会员编号   输入
        String flightCompanyCode = request.getParameter("flightCompanyCode");//航空公司编号   输入
        String useBonus = request.getParameter("useBonus");//使用积分数   输入
        String validMonth = request.getParameter("validMonth");//有效月   输入
        String validYear = request.getParameter("validYear");//有效年   输入
        String flightCompanyName = request.getParameter("flightCompanyName");//航空公司名称   输入
        String ebsNm1 = request.getParameter("ebsNm1");//拼音姓   输入
        String ebsNm2 = request.getParameter("ebsNm2");//拼音名   输入
        
        if (StringUtils.isEmpty(userid)
                || StringUtils.isEmpty(account)
                || StringUtils.isEmpty(password)
                || StringUtils.isEmpty(memberId)
                || StringUtils.isEmpty(flightCompanyCode)
                || StringUtils.isEmpty(useBonus)
                || StringUtils.isEmpty(validMonth)
                || StringUtils.isEmpty(validYear)
                || StringUtils.isEmpty(flightCompanyName)
                || StringUtils.isEmpty(ebsNm1)
                || StringUtils.isEmpty(ebsNm2)) {
            SpiderException exception = new SpiderException(1006, "数据不完整");
            deferredResult.setResult(exception.toString());
            return deferredResult;
        }
        BocomMileageEvent comiShopEvent = new BocomMileageEvent();
        comiShopEvent.setDeferredResult(deferredResult);
        comiShopEvent.setUserid(userid);
        comiShopEvent.setAccount(account);
        comiShopEvent.setPassword(password);
        comiShopEvent.setEbsNm1(ebsNm1);
        comiShopEvent.setEbsNm2(ebsNm2);
        comiShopEvent.setFlightCompanyCode(flightCompanyCode);
        comiShopEvent.setFlightCompanyName(flightCompanyName);
        comiShopEvent.setMemberId(memberId);
        comiShopEvent.setUseBonus(useBonus);
        comiShopEvent.setValidMonth(validMonth);
        comiShopEvent.setValidYear(validYear);
        comiShopEvent.setState(BocomMileageState.INDEX);
        SpiderReactor.getInstance().process(comiShopEvent);
        return deferredResult;
    }


    /**
     * 提交兑换(登录态保持)
     *
     * @param request
     * @return
     */
    @RequestMapping(value = "/bocom/submit/1.0", produces = "application/json;charset=utf-8")
    @ResponseBody
    public DeferredResult<Object> submit(HttpServletRequest request) {
        DeferredResult<Object> deferredResult = new DeferredResult<>();
        String userid = request.getParameter("userid");
        String account = request.getParameter("account");
        String password = request.getParameter("password");
        
        String memberId = request.getParameter("memberId");//航空公司会员编号   输入
        String flightCompanyCode = request.getParameter("flightCompanyCode");//航空公司编号   输入
        String useBonus = request.getParameter("useBonus");//使用积分数   输入
        String validMonth = request.getParameter("validMonth");//有效月   输入
        String validYear = request.getParameter("validYear");//有效年   输入
        String flightCompanyName = request.getParameter("flightCompanyName");//航空公司名称   输入
        String ebsNm1 = request.getParameter("ebsNm1");//拼音姓   输入
        String ebsNm2 = request.getParameter("ebsNm2");//拼音名   输入
        
        if (StringUtils.isEmpty(userid)
                || StringUtils.isEmpty(account)
                || StringUtils.isEmpty(password)
                || StringUtils.isEmpty(memberId)
                || StringUtils.isEmpty(flightCompanyCode)
                || StringUtils.isEmpty(useBonus)
                || StringUtils.isEmpty(validMonth)
                || StringUtils.isEmpty(validYear)
                || StringUtils.isEmpty(flightCompanyName)
                || StringUtils.isEmpty(ebsNm1)
                || StringUtils.isEmpty(ebsNm2)) {
            SpiderException exception = new SpiderException(1006, "数据不完整");
            deferredResult.setResult(exception.toString());
            return deferredResult;
        }
        BocomMileageEvent event = null;
        String key = String.format(Constant.BOCOM_MILEAGE_KEY, userid);
        String value = redis.getStringByKey(key);
        if (StringUtils.isEmpty(value)) {
        	event = new BocomMileageEvent();
        	event.setAccount(account);
        	event.setPassword(password);
        	event.setState(BocomMileageState.INDEX);
        }else{
        	event = JSON.parseObject(value, BocomMileageEvent.class);
        	if(account.equals(event.getAccount())){//如果是同一个账户，直接兑换
                event.setState(BocomMileageState.CONVERT_PAGE);
            }else{//如果不是同一个账户，要求重新登录
            	event = new BocomMileageEvent();
            	event.setAccount(account);
            	event.setPassword(password);
            	event.setState(BocomMileageState.INDEX);
            }
        }
        
        event.setUserid(userid);
        event.setEbsNm1(ebsNm1);
        event.setEbsNm2(ebsNm2);
        event.setFlightCompanyCode(flightCompanyCode);
        event.setFlightCompanyName(flightCompanyName);
        event.setMemberId(memberId);
        event.setUseBonus(useBonus);
        event.setValidMonth(validMonth);
        event.setValidYear(validYear);
        event.setDeferredResult(deferredResult);
        
        SpiderReactor.getInstance().process(event);
        return deferredResult;
    }

    

    /**
     * 获取短信验证码
     *
     * @param request
     * @return
     */
    @RequestMapping(value = "/bocom/sms/1.0", produces = "application/json;charset=utf-8")
    @ResponseBody
    public DeferredResult<Object> sms(HttpServletRequest request) {
        DeferredResult<Object> deferredResult = new DeferredResult<>();
        String userid = request.getParameter("userid");
        if (StringUtils.isEmpty(userid)) {
            SpiderException exception = new SpiderException(1006, "数据不完整");
            deferredResult.setResult(exception.toString());
            return deferredResult;
        }
        String key = String.format(Constant.BOCOM_MILEAGE_KEY, userid);
        String value = redis.getStringByKey(key);
        if (StringUtils.isEmpty(value)) {
            SpiderException exception = new SpiderException(2101, "请先登录");
            deferredResult.setResult(exception.toString());
            return deferredResult;
        }
        BocomMileageEvent event = JSON.parseObject(value, BocomMileageEvent.class);
        //event.setState(COMIShopState.SMS1);
        event.setState(BocomMileageState.SEND_MSG);
        event.setDeferredResult(deferredResult);
        SpiderReactor.getInstance().process(event);
        return deferredResult;
    }

    /**
     * 兑换
     *
     * @param request
     * @return
     */
    @RequestMapping(value = "/bocom/convert/1.0", produces = "application/json;charset=utf-8")
    @ResponseBody
    public DeferredResult<Object> convert(HttpServletRequest request) {
        DeferredResult<Object> deferredResult = new DeferredResult<>();
        String userid = request.getParameter("userid");
        String smsCode = request.getParameter("smscode");
        if (StringUtils.isEmpty(userid)
                || StringUtils.isEmpty(smsCode)) {
            SpiderException exception = new SpiderException(1006, "数据不完整");
            deferredResult.setResult(exception.toString());
            return deferredResult;
        }
        String key = String.format(Constant.BOCOM_MILEAGE_KEY, userid);
        String value = redis.getStringByKey(key);
        if (StringUtils.isEmpty(value)) {
            SpiderException exception = new SpiderException(2103, "短信验证码失效");
            deferredResult.setResult(exception.toString());
            return deferredResult;
        }
        BocomMileageEvent event = JSON.parseObject(value, BocomMileageEvent.class);
        event.setMsgCode(smsCode);
        event.setState(BocomMileageState.CONVERT_MILEAGE);
        event.setDeferredResult(deferredResult);
        SpiderReactor.getInstance().process(event);
        return deferredResult;
    }
    
    
    
    
}