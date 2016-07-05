/*
 * Copyright (c) 2015-2020 by caitu99
 * All rights reserved.
 */
package com.caitu99.lsp.model.spider.taobao;

import com.alibaba.fastjson.annotation.JSONField;
import com.caitu99.lsp.model.spider.MailSpiderEvent;
import com.caitu99.lsp.utils.cookie.HttpCookieEx;
import org.springframework.web.context.request.async.DeferredResult;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

/**
 * @author Hongbo Peng
 * @Description: (类职责详细描述, 可空)
 * @ClassName: TaoBaoNewEvent
 * @date 2015年11月18日 上午10:21:11
 * @Copyright (c) 2015-2020 by caitu99
 */
public class TaoBaoSpiderEvent extends MailSpiderEvent {
    /**
     * 验证码路径
     */
    private String codeImgUrl;
    /**
     * 登录必须参数（隐藏域的）
     */
    private Map<String, String> loginParams;
    /**
     * 登录请求路径
     */
    private String loginActionUrl;
    /**
     * 用户密码加密参数
     */
    private Map<String, String> rsaParams;
    /**
     * 登录后的跳转页面路径
     */
    private String login302Url;
    /**
     * 短信验证码请求参数
     */
    private Map<String, String> vCodeParams;
    /**
     * 短信验证码请求路径
     */
    private String vCodeActionUrl;

    /**
     * 验证码
     */
    private String imgCode;

    /**
     * 验证码发送完跳转页面
     */
    private String vCode302Url;

    /**
     * 验证码验证后跳转路径
     */
    private String reVCode302Url;

    /**
     * 积分结果集
     */
    private Map<String, Object> resultParams;

    private TaoBaoSpiderState state = TaoBaoSpiderState.NONE;

    /**
     * @Title:
     * @Description:
     */
    public TaoBaoSpiderEvent() {
        super();
    }

    public TaoBaoSpiderEvent(String sessionId, DeferredResult<Object> deferredResult, HttpServletRequest request) {
        super(sessionId, deferredResult, request);
        List<HttpCookieEx> cookieExList = HttpCookieEx.parse("Set-Cookie:cna=SOPZDsbYThECATpknpbhY1e6; path=/; domain=.taobao.com");
        this.getCookieList().addAll(cookieExList);
        /*List<HttpCookieEx> cookieExList2 = HttpCookieEx.parse("Set-Cookie:isg=5ED52889A3066F0182A96A0E499F69D6");
        this.getCookieList().addAll(cookieExList2);*/
    }

    @JSONField(deserialize = false)
    public void setException(Exception exception) {
        this.setState(TaoBaoSpiderState.ERROR);
        this.exception = exception;
    }

    /**
     * @return the codeImgUrl
     */
    public String getCodeImgUrl() {
        return codeImgUrl;
    }

    /**
     * @param codeImgUrl the codeImgUrl to set
     */
    public void setCodeImgUrl(String codeImgUrl) {
        this.codeImgUrl = codeImgUrl;
    }

    /**
     * @return the loginParams
     */
    public Map<String, String> getLoginParams() {
        return loginParams;
    }

    /**
     * @param loginParams the loginParams to set
     */
    public void setLoginParams(Map<String, String> loginParams) {
        this.loginParams = loginParams;
    }

    /**
     * @return the loginActionUrl
     */
    public String getLoginActionUrl() {
        return loginActionUrl;
    }

    /**
     * @param loginActionUrl the loginActionUrl to set
     */
    public void setLoginActionUrl(String loginActionUrl) {
        this.loginActionUrl = loginActionUrl;
    }

    /**
     * @return the rsaParams
     */
    public Map<String, String> getRsaParams() {
        return rsaParams;
    }

    /**
     * @param rsaParams the rsaParams to set
     */
    public void setRsaParams(Map<String, String> rsaParams) {
        this.rsaParams = rsaParams;
    }

    /**
     * @return the login302Url
     */
    public String getLogin302Url() {
        return login302Url;
    }

    /**
     * @param login302Url the login302Url to set
     */
    public void setLogin302Url(String login302Url) {
        this.login302Url = login302Url;
    }

    /**
     * @return the vCodeParams
     */
    public Map<String, String> getvCodeParams() {
        return vCodeParams;
    }

    /**
     * @param vCodeParams the vCodeParams to set
     */
    public void setvCodeParams(Map<String, String> vCodeParams) {
        this.vCodeParams = vCodeParams;
    }

    /**
     * @return the vCodeActionUrl
     */
    public String getvCodeActionUrl() {
        return vCodeActionUrl;
    }

    /**
     * @param vCodeActionUrl the vCodeActionUrl to set
     */
    public void setvCodeActionUrl(String vCodeActionUrl) {
        this.vCodeActionUrl = vCodeActionUrl;
    }

    /**
     * @return the imgCode
     */
    public String getImgCode() {
        return imgCode;
    }

    /**
     * @param imgCode the imgCode to set
     */
    public void setImgCode(String imgCode) {
        this.imgCode = imgCode;
    }

    /**
     * @return the vCode302Url
     */
    public String getvCode302Url() {
        return vCode302Url;
    }

    /**
     * @param vCode302Url the vCode302Url to set
     */
    public void setvCode302Url(String vCode302Url) {
        this.vCode302Url = vCode302Url;
    }

    /**
     * @return the reVCode302Url
     */
    public String getReVCode302Url() {
        return reVCode302Url;
    }

    /**
     * @param reVCode302Url the reVCode302Url to set
     */
    public void setReVCode302Url(String reVCode302Url) {
        this.reVCode302Url = reVCode302Url;
    }

    /**
     * @return the state
     */
    public TaoBaoSpiderState getState() {
        return state;
    }

    /**
     * @param state the state to set
     */
    public void setState(TaoBaoSpiderState state) {
        this.state = state;
    }

    /**
     * @return the resultParams
     */
    public Map<String, Object> getResultParams() {
        return resultParams;
    }

    /**
     * @param resultParams the resultParams to set
     */
    public void setResultParams(Map<String, Object> resultParams) {
        this.resultParams = resultParams;
    }
}
