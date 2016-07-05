package com.caitu99.lsp.model.spider.citybank;

public enum CityBankState {
	/*
	 * 正常步骤
	 * PRELOGINPAGE(获取重定向地址)--->
	 * LOGINPAGE(获取JFP_TOKEN和SYNC_TOKEN)--->
	 * LOGIN(提交表单，成功则获取重定向地址)
	 */

    PRELOGINPAGE, //  https://www.citibank.com.cn/CNGCB/JPS/portal/LocaleSwitch.do?locale=zh_CN
    LOGINPAGE,    //  https://www.citibank.com.cn/CNGCB/JSO/signon/DisplayUsernameSignon.do?JFP_TOKEN=PVLBM4Z1
    LOGIN,        //  https://www.citibank.com.cn/CNGCB/JSO/signon/ProcessUsernameSignon.do
    HOMEPAGE,     //get home page   https://www.citibank.com.cn/CNGCB/JPS/portal/LocaleSwitch.do?locale=zh_CN
    WELCOME,      //get welcome msg https://www.citibank.com.cn/CNGCB/REST/welcome/welcomeMsgContent?JFP_TOKEN=？？？？？？？？？
    JIFEN,        //get jifen       https://www.citibank.com.cn/CNGCB/ICARD/rewhom/displaySummary.do
    LOGOUT,
    ACCOUNTINFO,  //get client account info https://www.citibank.com.cn/CNGCB/REST/accountsPanel/getCustomerAccounts.jws?ttc=742
    ERROR         //error
}
