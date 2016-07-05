package com.caitu99.lsp.entry;

import com.caitu99.lsp.exception.SpiderException;
import com.caitu99.lsp.model.spider.ihghotel.IHGHotelEvent;
import com.caitu99.lsp.model.spider.ihghotel.IHGHotelState;
import com.caitu99.lsp.spider.SpiderReactor;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.async.DeferredResult;

import javax.servlet.http.HttpServletRequest;

@Controller
public class IHGHotel extends BaseController {

    private static final Logger logger = LoggerFactory
            .getLogger(IHGHotel.class);

    @RequestMapping(value = "/api/ihg/login/1.0", produces = "application/json;charset=utf-8")
    @ResponseBody
    public DeferredResult<Object> login(HttpServletRequest request) {
        logger.debug("get a login request for ihg");
        DeferredResult<Object> deferredResult = new DeferredResult<>();

        String userid = request.getParameter("userid");
        String account = request.getParameter("account");
        String password = request.getParameter("password");

        if (StringUtils.isEmpty(userid) || StringUtils.isEmpty(account)
                || StringUtils.isEmpty(password)) {
            SpiderException exception = new SpiderException(1006, "数据不完整");
            deferredResult.setResult(exception.toString());
            return deferredResult;
        }

        IHGHotelEvent event = new IHGHotelEvent(userid, deferredResult);
        event.setAccount(account);
        event.setPassword(password);
        event.setDeferredResult(deferredResult);
        event.setState(IHGHotelState.LOGINPAGE);

        SpiderReactor.getInstance().process(event);
        logger.info("start processing ihghotel event: {}", event);
        return deferredResult;
    }
}
