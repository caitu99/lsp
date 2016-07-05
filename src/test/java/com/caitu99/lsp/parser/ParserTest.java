package com.caitu99.lsp.parser;

import com.caitu99.lsp.model.parser.ParserContext;
import com.caitu99.lsp.model.spider.MailSrc;
import com.caitu99.lsp.parser.utils.TplHelper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"classpath:spring.xml"})
public class ParserTest {

    private static final Logger logger = LoggerFactory.getLogger(ParserTest.class);

    @Test
    public void testReactor() throws Exception {
        String html = readFile("/Users/bobo/Desktop/bill/gongshang_normal.html");

        MailSrc mailSrc = new MailSrc();
        mailSrc.setTitle("中国工商银行客户对账");
        mailSrc.setBody(html);
        mailSrc.setDate(new Date());
        mailSrc.setBank("xxoo");

        List<MailSrc> mailSrcList = new ArrayList<>();
        mailSrcList.add(mailSrc);

        ParserContext context = new ParserContext();
        context.setUserId(1);
        context.setAccount("bobo@qq.com");
        context.setMailSrcs(mailSrcList);

        ParserReactor.getInstance().processDebug(context);
        logger.info("");
    }

    @Test
    public void testPattern() throws IOException {
        String html = readFile("/Users/bobo/Desktop/bill/zhaoshang_normal.html");
        Pattern pattern = Pattern.compile("(?<=>)[^><]*?(?=，您好)", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(html);
        if(matcher.find())
            System.out.print(matcher.group());
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
