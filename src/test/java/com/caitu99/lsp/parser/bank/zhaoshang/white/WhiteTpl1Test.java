package com.caitu99.lsp.parser.bank.zhaoshang.white;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.caitu99.lsp.model.spider.MailSrc;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:spring.xml" })
public class WhiteTpl1Test extends TestCase {

	private final static Logger logger = LoggerFactory
			.getLogger(WhiteTpl1Test.class);

	@Test
	public void testParse() throws Exception {
		List<String> list = readFiles("D://test//mail//招商");
		for (String html : list) {
			MailSrc mailSrc = new MailSrc();
			mailSrc.setBody(html);
			new WhiteTpl1().parse();
		}
	}

	private String readFile(String file) throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(file));
		String line = null;
		StringBuilder stringBuilder = new StringBuilder();
		String ls = System.getProperty("line.separator");
		while ((line = reader.readLine()) != null) {
			stringBuilder.append(line);
			stringBuilder.append(ls);
		}
		return stringBuilder.toString();
	}

	private List<String> readFiles(String dir) throws IOException {
		List<String> list = new ArrayList<>();
		File dirPath = new File(dir);
		for (File file : dirPath.listFiles()) {
			list.add(readFile(dir + File.separator + file.getName()));
		}
		return list;
	}
}