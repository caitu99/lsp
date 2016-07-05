/*
 * Copyright (c) 2015-2020 by caitu99
 * All rights reserved.
 */
package com.caitu99.lsp.spider;


import com.caitu99.lsp.AbstractJunit;
import com.caitu99.lsp.model.spider.ccbishop.CCBIAddrQuery;
import com.thoughtworks.xstream.XStream;
import org.junit.Test;

public class MailParserTest1 {

    @Test
    public void testExecute() {
        XStream xStream = new XStream();
        xStream.processAnnotations(CCBIAddrQuery.class);
//        xStream.alias("queryPackup", CCBIAddrQuery.class);
        CCBIAddrQuery ccbiAddrQuery = new CCBIAddrQuery();
        ccbiAddrQuery.setAddrMail("aaa");
        String xml = xStream.toXML(ccbiAddrQuery);
        System.out.println(xml);
    }

}

