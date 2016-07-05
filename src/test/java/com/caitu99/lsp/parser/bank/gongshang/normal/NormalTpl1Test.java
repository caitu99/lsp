package com.caitu99.lsp.parser.bank.gongshang.normal;

import com.caitu99.lsp.model.spider.MailSrc;
import junit.framework.TestCase;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"classpath:spring.xml"})
public class NormalTpl1Test extends TestCase {

    private final static Logger logger = LoggerFactory.getLogger(NormalTpl1.class);

    @Test
    public void testParse() throws Exception {
        String html = readFile("/Users/bobo/Desktop/bill/gongshang_normal.html");
        MailSrc mailSrc = new MailSrc();
        mailSrc.setBody(html);
        new NormalTpl1().parse();
    }

    private String readFile( String file ) throws IOException {
        BufferedReader reader = new BufferedReader( new FileReader(file));
        String         line = null;
        StringBuilder  stringBuilder = new StringBuilder();
        String         ls = System.getProperty("line.separator");

        while( ( line = reader.readLine() ) != null ) {
            stringBuilder.append( line );
            stringBuilder.append( ls );
        }

        return stringBuilder.toString();
    }
}