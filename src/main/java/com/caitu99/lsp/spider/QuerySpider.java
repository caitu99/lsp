package com.caitu99.lsp.spider;


import com.caitu99.lsp.model.spider.QueryEvent;

public interface QuerySpider {

    void onEvent(QueryEvent event);

    void errorHandle(QueryEvent event);

}
