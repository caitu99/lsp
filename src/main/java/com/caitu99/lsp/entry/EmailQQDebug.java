package com.caitu99.lsp.entry;


import com.alibaba.fastjson.JSON;
import com.caitu99.lsp.exception.SpiderException;
import com.caitu99.lsp.model.spider.SpiderResult;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Base64;
import java.util.UUID;

@Controller
public class EmailQQDebug extends BaseController {

    private static final Logger logger = LoggerFactory.getLogger(EmailQQDebug.class);

    private static final String localhost = "http://localhost:8080";
    private static final String loginUri = "/api/mail/qq/login";
    private static final String verifyUri = "/api/mail/qq/verify";

    @RequestMapping("/debug/qq")
    public String index(HttpServletRequest request) {
        request.getSession().setAttribute("login", true);
        return "/mailqq/qq";
    }

    @RequestMapping("/debug/qq/login")
    public String login(HttpServletRequest request) throws IOException {
        Object login = request.getSession().getAttribute("login");
        if (login == null) {
            return "redirect:/debug/qq";
        }

        String sessionid;
        if (request.getSession().getAttribute("sessionid") == null) {
            sessionid = UUID.randomUUID().toString();
            request.getSession().setAttribute("sessionid", sessionid);
        } else {
            sessionid = (String) request.getSession().getAttribute("sessionid");
        }

        String account = request.getParameter("account");
        String pwd = request.getParameter("pwd");

        StringBuilder sb = new StringBuilder();
        sb.append(localhost)
                .append(loginUri)
                .append("?")
                .append("sessionid=").append(sessionid)
                .append("&account=").append(account)
                .append("&password=").append(pwd);

        CloseableHttpClient httpclient = HttpClients.createDefault();
        HttpGet httpGet = new HttpGet(sb.toString());

        CloseableHttpResponse httpResponse = httpclient.execute(httpGet);
        String content = EntityUtils.toString(httpResponse.getEntity());
        httpResponse.close();

        SpiderException result = JSON.parseObject(content, SpiderException.class);

        if (result.getCode() == 1001) {
            request.getSession().setAttribute("verifyresult", result);
            return "redirect:/debug/qq/verify";
        }

        request.getSession().setAttribute("result", result.toString());
        return "redirect:/debug/qq/result";
    }

    @RequestMapping("/debug/qq/verify")
    public String verifyIndex(HttpServletRequest request) {
        Object login = request.getSession().getAttribute("login");
        if (login == null) {
            return "redirect:/debug/qq";
        }

        return "/mailqq/verify";
    }

    @RequestMapping(value = "/debug/qq/verify/img", produces = MediaType.IMAGE_JPEG_VALUE)
    @ResponseBody
    public byte[] verifyImg(HttpServletRequest request) {
        SpiderException result = (SpiderException) request.getSession().getAttribute("verifyresult");
        byte[] bytes = Base64.getDecoder().decode(result.getData());
        return bytes;
    }

    @RequestMapping("/debug/qq/verify/post")
    public String verify(HttpServletRequest request) throws IOException {
        Object login = request.getSession().getAttribute("login");
        if (login == null) {
            return "redirect:/debug/qq";
        }

        String sessionid = (String) request.getSession().getAttribute("sessionid");
        String vcode = request.getParameter("vcode");

        StringBuilder sb = new StringBuilder();
        sb.append(localhost)
                .append(verifyUri)
                .append("?")
                .append("sessionid=").append(sessionid)
                .append("&vcode=").append(vcode);

        CloseableHttpClient httpclient = HttpClients.createDefault();
        HttpGet httpGet = new HttpGet(sb.toString());

        CloseableHttpResponse httpResponse = httpclient.execute(httpGet);
        String content = EntityUtils.toString(httpResponse.getEntity());
        httpResponse.close();

        SpiderResult result = JSON.parseObject(content, SpiderResult.class);

        request.getSession().setAttribute("result", result.toString());

        return "redirect:/debug/qq/result";
    }

    @RequestMapping(value = "/debug/qq/result", produces = "application/json;charset=utf-8")
    public String result(HttpServletRequest request, Model model) {
        Object obj = request.getSession().getAttribute("result");
        model.addAttribute("result", obj.toString());
        return "/mailqq/result";
    }

}
