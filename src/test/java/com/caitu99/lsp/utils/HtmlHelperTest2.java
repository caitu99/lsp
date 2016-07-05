package com.caitu99.lsp.utils;

import junit.framework.TestCase;
import org.junit.Test;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;


public class HtmlHelperTest2 extends TestCase {

    @Test
    public void test() throws IOException {
        FileInputStream fileInputStream = new FileInputStream("E:\\a.html");
        InputStreamReader stream = new InputStreamReader(fileInputStream);
        StringBuilder stringBuilder = HtmlHelper2.xHex2Html(stream);

        stringBuilder = HtmlHelper2.unicodeDecoded(stringBuilder);
        stringBuilder = HtmlHelper2.htmlDecoded(stringBuilder);
        FileOutputStream fileOutputStream = new FileOutputStream("E:\\b.html");
        fileOutputStream.write(stringBuilder.toString().getBytes());
    }

}