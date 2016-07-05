package com.caitu99.lsp.entry;


import com.alibaba.fastjson.JSON;
import com.caitu99.lsp.Constant;
import com.caitu99.lsp.cache.RedisOperate;
import com.caitu99.lsp.exception.SpiderException;
import com.caitu99.lsp.model.spider.ccbishop.CCBIAddrQuery;
import com.caitu99.lsp.model.spider.ccbishop.CCBIGoodsQuery;
import com.caitu99.lsp.model.spider.ccbishop.CCBIShopEvent;
import com.caitu99.lsp.model.spider.ccbishop.CCBIShopState;
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
public class CCBIShop extends BaseController {

    private static final Logger logger = LoggerFactory.getLogger(CCBIShop.class);

    @Autowired
    private RedisOperate redis;

    /**
     * 验证用户是否可以下单
     *
     * @param request
     * @return
     */
    @RequestMapping(value = "/api/ishop/ccb/validate/1.0", produces = "application/json;charset=utf-8")
    @ResponseBody
    public DeferredResult<Object> validate(HttpServletRequest request) {
        logger.info("开始验证用户是否可以下单。。。");
        DeferredResult<Object> deferredResult = new DeferredResult<>();
        String userid = request.getParameter("userid");
        String price = request.getParameter("price");
        if (StringUtils.isEmpty(userid) || StringUtils.isEmpty(price)) {
            SpiderException exception = new SpiderException(1006, "数据不完整");
            deferredResult.setResult(exception.toString());
            return deferredResult;
        }
        String key = String.format(Constant.ISHOP_CCB_TASK_QUEUE, userid);
        String content = redis.getStringByKey(key);
        if (StringUtils.isEmpty(content)) {
            SpiderException exception = new SpiderException(1081, "用户尚未登录");
            deferredResult.setResult(exception.toString());
            return deferredResult;
        }
        CCBIShopEvent ccbiShopEvent = JSON.parseObject(content, CCBIShopEvent.class);
        logger.debug("验证用户是否真的处于登录状态。。。");
        ccbiShopEvent.setState(CCBIShopState.ISLOGIN);
        ccbiShopEvent.setDeferredResult(deferredResult);
        SpiderReactor.getInstance().process(ccbiShopEvent);
        logger.debug("数据返回。。。");
        return deferredResult;
    }

    /**
     * 获取验证码
     *
     * @param request
     * @return
     */
    @RequestMapping(value = "/api/ishop/ccb/vcode/get/1.0", produces = "application/json;charset=utf-8")
    @ResponseBody
    public DeferredResult<Object> getVcode(HttpServletRequest request) {
        logger.info("获取验证码。。。");
        DeferredResult<Object> deferredResult = new DeferredResult<>();
        String userid = request.getParameter("userid");
        if (StringUtils.isEmpty(userid)) {
            SpiderException exception = new SpiderException(1006, "数据不完整");
            deferredResult.setResult(exception.toString());
            return deferredResult;
        }
        CCBIShopEvent event = new CCBIShopEvent(userid, deferredResult);
        event.setState(CCBIShopState.INDEX);
        SpiderReactor.getInstance().process(event);
        return deferredResult;
    }
    
    @RequestMapping(value = "/api/ishop/ccb/loginjf/1.0", produces = "application/json;charset=utf-8")
    @ResponseBody
    public DeferredResult<Object> loginjf(HttpServletRequest request) {
        logger.debug("请求登录loginjf。。。");
        DeferredResult<Object> deferredResult = new DeferredResult<>(0);
        String userid = request.getParameter("userid");
        String account = request.getParameter("account");
        String password = request.getParameter("password");
        String vcode = request.getParameter("vcode");
        String querytype = request.getParameter("querytype");
        if (StringUtils.isEmpty(userid)
                || StringUtils.isEmpty(account)
                || StringUtils.isEmpty(password)
                || StringUtils.isEmpty(vcode)) {
            SpiderException exception = new SpiderException(1006, "数据不完整");
            deferredResult.setResult(exception.toString());
            return deferredResult;
        }
        String key = String.format(Constant.ISHOP_CCB_TASK_QUEUE, userid);
        String value = redis.getStringByKey(key);
        if (StringUtils.isEmpty(value)) {
            SpiderException exception = new SpiderException(1092, "验证码失效");
            deferredResult.setResult(exception.toString());
            return deferredResult;
        }
        CCBIShopEvent ccbiShopEvent = JSON.parseObject(value, CCBIShopEvent.class);
        ccbiShopEvent.setDeferredResult(deferredResult);
        ccbiShopEvent.setAccount(account);
        ccbiShopEvent.setPassword(password);
        ccbiShopEvent.setvCode(vcode);
        ccbiShopEvent.setQuerytype(querytype);
   
        ccbiShopEvent.setState(CCBIShopState.LOGIN_JF);
        SpiderReactor.getInstance().process(ccbiShopEvent);
        return deferredResult;
    }
    
    

    /**
     * 登录
     *
     * @param request
     * @return
     */
    @RequestMapping(value = "/api/ishop/ccb/login/1.0", produces = "application/json;charset=utf-8")
    @ResponseBody
    public DeferredResult<Object> login(HttpServletRequest request) {
        logger.info("请求登录中信。。。");
        DeferredResult<Object> deferredResult = new DeferredResult<>(0);
        String userid = request.getParameter("userid");
        String account = request.getParameter("account");
        String password = request.getParameter("password");
        String vcode = request.getParameter("vcode");
        if (StringUtils.isEmpty(userid)
                || StringUtils.isEmpty(account)
                || StringUtils.isEmpty(password)
                || StringUtils.isEmpty(vcode)) {
            SpiderException exception = new SpiderException(1006, "数据不完整");
            deferredResult.setResult(exception.toString());
            return deferredResult;
        }
        String key = String.format(Constant.ISHOP_CCB_TASK_QUEUE, userid);
        String value = redis.getStringByKey(key);
        if (StringUtils.isEmpty(value)) {
            SpiderException exception = new SpiderException(1092, "验证码失效");
            deferredResult.setResult(exception.toString());
            return deferredResult;
        }
        CCBIShopEvent ccbiShopEvent = JSON.parseObject(value, CCBIShopEvent.class);
        ccbiShopEvent.setDeferredResult(deferredResult);
        ccbiShopEvent.setAccount(account);
        ccbiShopEvent.setPassword(password);
        ccbiShopEvent.setvCode(vcode);

        ccbiShopEvent.setState(CCBIShopState.LOGIN);
        SpiderReactor.getInstance().process(ccbiShopEvent);
        return deferredResult;
    }

    /**
     * 重新获取登录短信
     *
     * @param request
     * @return
     */
    @RequestMapping(value = "/api/ishop/ccb/resms/get/1.0", produces = "application/json;charset=utf-8")
    @ResponseBody
    public DeferredResult<Object> reSms(HttpServletRequest request) {
        logger.info("重新获取登录短信。。。");
        DeferredResult<Object> deferredResult = new DeferredResult<>(0);
        String userid = request.getParameter("userid");
        if (StringUtils.isEmpty(userid)) {
            SpiderException exception = new SpiderException(1006, "数据不完整");
            deferredResult.setResult(exception.toString());
            return deferredResult;
        }
        String key = String.format(Constant.ISHOP_CCB_TASK_QUEUE, userid);
        String value = redis.getStringByKey(key);
        if (StringUtils.isEmpty(value)) {
            SpiderException exception = new SpiderException(1108, "请重新登录");
            deferredResult.setResult(exception.toString());
            return deferredResult;
        }
        CCBIShopEvent ccbiShopEvent = JSON.parseObject(value, CCBIShopEvent.class);
        ccbiShopEvent.setDeferredResult(deferredResult);
        ccbiShopEvent.setState(CCBIShopState.SMS);
        SpiderReactor.getInstance().process(ccbiShopEvent);
        return deferredResult;
    }

    /**
     * 短信验证码验证
     *
     * @param request
     * @return
     */
    @RequestMapping(value = "/api/ishop/ccb/check/1.0", produces = "application/json;charset=utf-8")
    @ResponseBody
    public DeferredResult<Object> check(HttpServletRequest request) {
        logger.info("请求进行短信验证。。。");
        DeferredResult<Object> deferredResult = new DeferredResult<>(0);
        String userid = request.getParameter("userid");
        String smscode = request.getParameter("smscode");
        String querytype = request.getParameter("querytype");
        if (StringUtils.isEmpty(userid)
                || StringUtils.isEmpty(smscode)) {
            SpiderException exception = new SpiderException(1006, "数据不完整");
            deferredResult.setResult(exception.toString());
            return deferredResult;
        }
        String key = String.format(Constant.ISHOP_CCB_TASK_QUEUE, userid);
        String value = redis.getStringByKey(key);
        if (StringUtils.isEmpty(value)) {
            SpiderException exception = new SpiderException(1092, "短信验证码失效");
            deferredResult.setResult(exception.toString());
            return deferredResult;
        }
        CCBIShopEvent ccbiShopEvent = JSON.parseObject(value, CCBIShopEvent.class);
        ccbiShopEvent.setDeferredResult(deferredResult);
        ccbiShopEvent.setSmsCode(smscode);
        ccbiShopEvent.setState(CCBIShopState.CHECK);
        ccbiShopEvent.setQuerytype(querytype);
        SpiderReactor.getInstance().process(ccbiShopEvent);
        return deferredResult;
    }

    /**
     * 重新获取下单时的验证码
     *
     * @param request
     * @return
     */
    @RequestMapping(value = "/api/ishop/ccb/revcode/1.0", produces = "application/json;charset=utf-8")
    @ResponseBody
    public DeferredResult<Object> reVcode(HttpServletRequest request) {
        logger.info("重新获取下单时的验证码。。。");
        DeferredResult<Object> deferredResult = new DeferredResult<>(0);
        String userid = request.getParameter("userid");
        
        String price = request.getParameter("price");
        String addrZip = request.getParameter("addrzip");
        String addr = request.getParameter("addr");
        String name = request.getParameter("name");
        String cardId = request.getParameter("cardId");
        String mobile = request.getParameter("mobile");
        String quantity = request.getParameter("quantity");
        String goods_id = request.getParameter("goods_id");
        String goods_payway_id = request.getParameter("goods_payway_id");
        String vendor_id = request.getParameter("vendor_id");
        String vendor_nm = request.getParameter("vendor_nm");
        String type_id = request.getParameter("type_id");
        String goods_nm = request.getParameter("goods_nm");
        
        String imgType = request.getParameter("imgType");
        
        
        if (StringUtils.isEmpty(userid)) {
            SpiderException exception = new SpiderException(1006, "数据不完整");
            deferredResult.setResult(exception.toString());
            return deferredResult;
        }
        String key = String.format(Constant.ISHOP_CCB_TASK_QUEUE, userid);
        String value = redis.getStringByKey(key);
        if (StringUtils.isEmpty(value)) {
            SpiderException exception = new SpiderException(1005, "请重新登录");
            deferredResult.setResult(exception.toString());
            return deferredResult;
        }
        CCBIShopEvent ccbiShopEvent = JSON.parseObject(value, CCBIShopEvent.class);
        
        if(!"1".equals(ccbiShopEvent.getLoginType())){
    		SpiderException exception = new SpiderException(1005, "登录超时");
            deferredResult.setResult(exception.toString());
            return deferredResult;
        }
        
        if("1".equals(imgType)){
        	
       
	        ccbiShopEvent.setPrice(Long.parseLong(price));
	        ccbiShopEvent.setQuantity(Long.valueOf(quantity));
	
	        //创建收货地址对象
	        CCBIAddrQuery ccbiAddrQuery = new CCBIAddrQuery();
	        ccbiAddrQuery.setAddrZip(addrZip);
	        ccbiAddrQuery.setAddrMail(addr);
	        ccbiAddrQuery.setAddrName(name);
	        ccbiAddrQuery.setAdd_id_nbr(cardId);
	        ccbiAddrQuery.setAddrMobi(mobile);
	        ccbiAddrQuery.setAdd_id_type("1");
	        ccbiAddrQuery.setQuerytype("insertaddr");
	        ccbiShopEvent.setAddrInfo(ccbiAddrQuery);
	
	        //创建商品属性对象
	        CCBIGoodsQuery ccbiGoodsQuery = new CCBIGoodsQuery();
	        ccbiGoodsQuery.setGoods_id(goods_id);
	        ccbiGoodsQuery.setGoods_payway_id(goods_payway_id);
	        ccbiGoodsQuery.setVendor_id(vendor_id);
	        ccbiGoodsQuery.setVendor_nm(vendor_nm);
	        ccbiGoodsQuery.setType_id(type_id);
	        ccbiGoodsQuery.setGoods_nm(goods_nm);
	        ccbiShopEvent.setGoodsQuery(ccbiGoodsQuery);
        
        }
        ccbiShopEvent.setDeferredResult(deferredResult);
        //ccbiShopEvent.setState(CCBIShopState.ORDERVCODE);
       
        
        ccbiShopEvent.setState(CCBIShopState.GETINTEGRAL);
        SpiderReactor.getInstance().process(ccbiShopEvent);
        return deferredResult;
    }

    /**
     * 下单
     *
     * @param request
     * @return
     */
    @RequestMapping(value = "/api/ishop/ccb/order/1.0", produces = "application/json;charset=utf-8")
    @ResponseBody
    public DeferredResult<Object> order(HttpServletRequest request) {
        logger.info("请求下单。。。");
        DeferredResult<Object> deferredResult = new DeferredResult<>(0);
        String userid = request.getParameter("userid");
        String orderVcode = request.getParameter("ordervcode");
        if (StringUtils.isEmpty(userid)
                || StringUtils.isEmpty(orderVcode)) {
            SpiderException exception = new SpiderException(1006, "数据不完整");
            deferredResult.setResult(exception.toString());
            return deferredResult;
        }
        String key = String.format(Constant.ISHOP_CCBI_ORDER_QUEUE, userid);
        String value = redis.getStringByKey(key);
        if (StringUtils.isEmpty(value)) {
            SpiderException exception = new SpiderException(1092, "验证码失效");
            deferredResult.setResult(exception.toString());
            return deferredResult;
        }
        CCBIShopEvent ccbiShopEvent = JSON.parseObject(value, CCBIShopEvent.class);
        ccbiShopEvent.setDeferredResult(deferredResult);
        ccbiShopEvent.setOrderVcode(orderVcode);
        ccbiShopEvent.setState(CCBIShopState.QUERYCARD);
        SpiderReactor.getInstance().process(ccbiShopEvent);
        return deferredResult;
    }
}