package com.caitu99.lsp.entry;

import com.alibaba.fastjson.JSONObject;
import com.caitu99.lsp.exception.SpiderException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;

public class BaseController {

    private static final Logger logger = LoggerFactory.getLogger(BaseController.class);

    @ExceptionHandler(Exception.class)
    @ResponseBody
    public String exception(Exception e, HttpServletRequest request) {
        // ApiException
        logger.error("have exception", e);
        if (e instanceof SpiderException) {
            return e.toString();
        } else { // unknown exception
            logger.error("unknown exception", e);
            JSONObject error = new JSONObject();
            error.put("code", "-1");
            error.put("message", e.toString());
            return error.toJSONString();
        }
    }
}