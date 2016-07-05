package com.caitu99.lsp.entry;

import javax.servlet.http.HttpServletRequest;

import com.caitu99.lsp.model.spider.citybank.CityBankEvent;
import com.caitu99.lsp.model.spider.citybank.CityBankState;
import com.caitu99.lsp.spider.SpiderReactor;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.async.DeferredResult;

import com.caitu99.lsp.cache.RedisOperate;
import com.caitu99.lsp.exception.SpiderException;

@Controller
public class CityBank extends BaseController{
	private static final Logger logger = LoggerFactory.getLogger(CityBank.class);
	@Autowired
	private RedisOperate redis;

	@RequestMapping(value = "/api/citybank/login/1.0", produces = "application/json;charset=utf-8")
	@ResponseBody
	public DeferredResult<Object> login(HttpServletRequest request) {
		logger.debug("get a request for citybank");
		DeferredResult<Object> deferredResult = new DeferredResult<>();

		String userid = request.getParameter("userid");
		String account = request.getParameter("account");
		String password = request.getParameter("password");

		if (StringUtils.isEmpty(userid) || StringUtils.isEmpty(account) || StringUtils.isEmpty(password)) {
			SpiderException exception = new SpiderException(1006, "数据不完整");
			deferredResult.setResult(exception.toString());
			return deferredResult;
		}

		
		CityBankEvent event = new CityBankEvent(userid, deferredResult);
		event.setAccount(account);
		event.setPassword(password);
		event.setDeferredResult(deferredResult);
		event.setState(CityBankState.PRELOGINPAGE);
		
		SpiderReactor.getInstance().process(event);
		logger.info("start processing citybank event: {}", event);
		return deferredResult;
	}
}
