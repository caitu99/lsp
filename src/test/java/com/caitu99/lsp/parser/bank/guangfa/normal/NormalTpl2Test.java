package com.caitu99.lsp.parser.bank.guangfa.normal;

import com.caitu99.lsp.model.spider.MailSrc;
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
 * Created by Lion on 2015/12/18 0018.
 * 12月账单测试
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"classpath:spring.xml"})
public class NormalTpl2Test {
    private final static Logger logger = LoggerFactory.getLogger(com.caitu99.lsp.parser.bank.jianshe.normal.NormalTpl1.class);

    @Test
    public void testParse() throws Exception {
        String html = readFile("F:\\工作数据\\邮件解析\\guangfa.html");
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
