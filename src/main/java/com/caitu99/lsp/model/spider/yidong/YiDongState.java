package com.caitu99.lsp.model.spider.yidong;

public enum YiDongState {
    /*
     * 1.0(短信验证码登录) 正常请求流程：  (有错都要返回第一步全部重来)
     * /api/yidong/img/1.0 (input:userid   output:imgcode)
     * 		GETIMG
     *
     * /api/yidong/login/1.0 (input:userid,account,vcode   output:sms)
     * 		PRESENDSMS（验证图片码是否正确）>>>SENDSMS(请求发送短信)
     *
     * /api/yidong/verify/1.0 (input:userid,password,vcode,passwordType,account)
     * 		CHECK(验证图片码是否正确)>>>LOGIN(验证账号密码)>>>GETTOKEN>>>GETINTEGRAL
     *
     *
     *2.0(服务密码登录)
     *  /api/yidong/img/1.0 (input:userid   output:imgcode)
     * 		GETIMG
     * 	/api/yidong/verify/1.0 (input:userid,password,vcode,passwordType,account)
     * 		CHECK(验证图片码是否正确)>>>LOGIN(验证账号密码)>>>GETTOKEN>>>GETINTEGRAL
     */
    GETIMG,      //	https://login.10086.cn/captchazh.htm?type=05
    CHECK,       //	https://login.10086.cn/verifyCaptcha?inputCode=xxxxxx
    PRESENDSMS,  //	https://login.10086.cn/verifyCaptcha?inputCode=xxxxxx   用于验证验证码是否正确
    SENDSMS,     // https://login.10086.cn/sendRandomCodeAction.action
    LOGIN,       // https://login.10086.cn/login.htm?accountType=01&account=%s&password=%s&pwdType=02&inputCode=%s
    GETTOKEN,    // http://shop.10086.cn/i/v1/auth/getArtifact?backUrl=http://shop.10086.cn/i/&artifact=
    GETINTEGRAL, // http://shop.10086.cn/i/v1/point/sum/
    ERROR        // error
}
