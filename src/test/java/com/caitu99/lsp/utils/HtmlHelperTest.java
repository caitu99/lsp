package com.caitu99.lsp.utils;

import junit.framework.TestCase;
import org.apache.commons.lang.StringEscapeUtils;
import org.junit.Test;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;


public class HtmlHelperTest extends TestCase {

    @Test
    public void test() throws IOException {
        FileInputStream fileInputStream = new FileInputStream("E:\\a.html");
        StringBuilder stringBuilder = HtmlHelper.xHex2Html(fileInputStream);
        stringBuilder = HtmlHelper.html2UHex(stringBuilder);
        FileOutputStream fileOutputStream = new FileOutputStream("E:\\b.html");
        fileOutputStream.write(StringEscapeUtils.unescapeJava(stringBuilder.toString()).getBytes());
    }

}