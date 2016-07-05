package com.caitu99.lsp.parser;


import com.caitu99.lsp.model.parser.ParserContext;
import com.caitu99.lsp.model.spider.MailSrc;

import java.util.Map;


public interface ITpl {

    boolean parse();

    boolean check();

    boolean is();

    void setContext(ParserContext context);

    ParserContext getContext();

    MailSrc getMailSrc();

    void setMailSrc(MailSrc mailSrc);

    ICard getCard();

    void setCard(ICard card);

    String getName();

    Map<String, String> getConfigure();

}
