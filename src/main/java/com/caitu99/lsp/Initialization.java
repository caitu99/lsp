package com.caitu99.lsp;

import com.caitu99.lsp.parser.ParserReactor;
import com.caitu99.lsp.spider.SpiderReactor;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;


public class Initialization implements ApplicationListener<ContextRefreshedEvent> {

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {

        if (event.getApplicationContext().getParent() == null) {
            SpiderReactor.getInstance().start();
            ParserReactor.getInstance().start();
        }
    }

}
