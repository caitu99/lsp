package com.caitu99.lsp.parser.bank.xingye.normal;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.caitu99.lsp.model.spider.MailSrc;

import junit.framework.TestCase;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"classpath:spring.xml"})
public class NormalTpl1Test extends TestCase {
	
	private final static Logger logger = LoggerFactory.getLogger(NormalTpl1.class);
	
	@Test
    public void testParse() throws Exception {
        String html = readFile("d:\\xingye2.html");
        MailSrc mailSrc = new MailSrc();
        mailSrc.setBody(html);
        new NormalTpl1().parse();
    }
	
	private String readFile( String file1 ) throws IOException {
		
		File file = new File(file1);
		String fileDir = "";
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), "gbk"));
//			reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
			String tempString = null;
			// 一次读入一行，直到读入null为文件结束
			while ((tempString = reader.readLine()) != null) {
				// 显示行号
				fileDir = fileDir + tempString + "\n";
			}
			reader.close();
		} catch (IOException e) {
			System.out.println("oo");
		}
		MailSrc mailSrc = new MailSrc();
		return fileDir;
    }

}
