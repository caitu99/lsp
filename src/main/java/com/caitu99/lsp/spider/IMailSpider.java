package com.caitu99.lsp.spider;


import com.caitu99.lsp.model.spider.MailSpiderEvent;

public interface IMailSpider {

    void onEvent(MailSpiderEvent event);

    void errorHandle(MailSpiderEvent event);

}
