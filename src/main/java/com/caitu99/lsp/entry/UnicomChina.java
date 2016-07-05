/*
 * Copyright (c) 2015-2020 by caitu99
 * All rights reserved.
 */
package com.caitu99.lsp.entry;

import com.alibaba.fastjson.JSON;
import com.caitu99.lsp.Constant;
import com.caitu99.lsp.cache.RedisOperate;
import com.caitu99.lsp.exception.SpiderException;
import com.caitu99.lsp.model.spider.liantong.UnicomChinaSpiderEvent;
import com.caitu99.lsp.model.spider.liantong.UnicomChinaSpiderState;
import com.caitu99.lsp.spider.SpiderReactor;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.async.DeferredResult;

import javax.servlet.http.HttpServletRequest;

/**
 * 
 * 
 * @Description: (类职责详细描述,可空) 
 * @ClassName: UnicomChina 
 * @author ws
 * @date 2016年3月22日 下午2:32:11 
 * @Copyright (c) 2015-2020 by caitu99
 */
@Controller
public class UnicomChina extends BaseController {

    //private static final Logger logger = LoggerFactory.getLogger(LianTong.class);

    @Autowired
    private RedisOperate redis;


    /**
     * 
     * 	
     * @Description: (方法职责详细描述,可空)  
     * @Title: login 
     * @param request
     * @return
     * @date 2016年3月22日 下午2:32:42  
     * @author ws
     */
    @RequestMapping(value = "/spider/unicomchina/img/1.0", produces = "application/json;charset=utf-8")
    @ResponseBody
    public DeferredResult<Object> img(HttpServletRequest request) {
        DeferredResult<Object> deferredResult = new DeferredResult<>();
        String userid = request.getParameter("userid");

        if (StringUtils.isEmpty(userid)) {
            SpiderException exception = new SpiderException(1006, "数据不完整");
            deferredResult.setResult(exception.toString());
            return deferredResult;
        }

        UnicomChinaSpiderEvent event = new UnicomChinaSpiderEvent(userid, deferredResult);
        event.setDeferredResult(deferredResult);
        event.setUserid(userid);
        event.setState(UnicomChinaSpiderState.NONE);
        SpiderReactor.getInstance().process(event);

        return deferredResult;
    }


    /**
     * 带着验证码登陆,并获得积分
     *
     * @param request{userid,phoneno,password,yzm}
     * @return
     * @Description: (方法职责详细描述, 可空)
     * @Title: vcodeLogin
     * @date 2015年11月13日 下午2:15:09
     * @author chenhl
     */
    @RequestMapping(value = "/spider/unicomchina/login/1.0", produces = "application/json;charset=utf-8")
    @ResponseBody
    public DeferredResult<Object> vcodeLogin(HttpServletRequest request) {
        DeferredResult<Object> deferredResult = new DeferredResult<>();
        String userid = request.getParameter("userid");
        String phoneno = request.getParameter("account");
        String password = request.getParameter("password");
        String vcode = request.getParameter("vcode");

        if (StringUtils.isEmpty(userid)
                || StringUtils.isEmpty(phoneno)
                || StringUtils.isEmpty(password)
                || StringUtils.isEmpty(vcode)) {
            SpiderException exception = new SpiderException(1006, "数据不完整");
            deferredResult.setResult(exception.toString());
            return deferredResult;
        }

        // 获取缓存事件内容
        String key = String.format(Constant.LIAN_TONG_IMPORT_KEY, userid);
        String content = redis.getStringByKey(key);
        if (StringUtils.isEmpty(content)) {
            SpiderException exception = new SpiderException(1005, "账号验证已过期");
            deferredResult.setResult(exception.toString());
            return deferredResult;
        }

        UnicomChinaSpiderEvent event = JSON.parseObject(content, UnicomChinaSpiderEvent.class); //反序列化，把之前保存的event取出来
        event.setDeferredResult(deferredResult);
        event.setAccount(phoneno);
        event.setPassword(password);
        event.setvCode(vcode);
        event.setUserid(userid);
        event.setState(UnicomChinaSpiderState.IMG_CHECK);

        SpiderReactor.getInstance().process(event);

        return deferredResult;

    }

}
