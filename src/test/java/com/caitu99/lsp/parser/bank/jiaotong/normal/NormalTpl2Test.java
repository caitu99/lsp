package com.caitu99.lsp.parser.bank.jiaotong.normal;

import com.caitu99.lsp.model.spider.MailSrc;
import junit.framework.TestCase;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Created by Lion on 2015/12/19 0019.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"classpath:spring.xml"})
public class NormalTpl2Test extends TestCase {

    private final static Logger logger = LoggerFactory.getLogger(NormalTpl2Test.class);

    @Test
    public void testParse() throws Exception {
        String html = readFile("/home/hy/work/data/forlsp/jiaotong/jiaotong_1.html");
        MailSrc mailSrc = new MailSrc();
        mailSrc.setBody(html);
        NormalTpl2 normalTpl2 = new NormalTpl2();
        normalTpl2.setMailSrc(mailSrc);
        normalTpl2.parse();
        normalTpl2.check();
    }

    private String readFile( String file ) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), "utf-8"));//new BufferedReader( new FileReader(file));
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
