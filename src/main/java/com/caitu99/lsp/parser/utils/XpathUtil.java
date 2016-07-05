/*
 * Copyright (c) 2015-2020 by caitu99
 * All rights reserved.
 */
package com.caitu99.lsp.parser.utils;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.htmlcleaner.CleanerProperties;
import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.PrettyXmlSerializer;
import org.htmlcleaner.TagNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

/**
 * 
 * @Description: (类职责详细描述,可空)
 * @ClassName: XpathUtil
 * @author lhj
 * @date 2015年12月17日 上午11:38:23
 * @Copyright (c) 2015-2020 by caitu99
 */
public class XpathUtil {

	private final static Logger logger = LoggerFactory
			.getLogger(XpathUtil.class);

	public static String delHead(String html) {

		if (html.indexOf("<html") > 0) {
			return html.substring(html.indexOf("<html"),
					html.lastIndexOf("</html>") + 7);
		} else if (html.indexOf("<HTML") > 0) {
			return html.substring(html.indexOf("<HTML"),
					html.lastIndexOf("</HTML>") + 7);
		} else {
			int position = html.lastIndexOf("</html>") >= 0 ? html
					.lastIndexOf("</html>") : html.lastIndexOf("</HTML>");
			return html.substring(0, position + 7);
		}
	}

	public static Document getCleanHtml(String htmlStr) {
		try {
			HtmlCleaner cleaner = new HtmlCleaner();
			CleanerProperties props = cleaner.getProperties();
			props.setUseCdataForScriptAndStyle(true);
			props.setRecognizeUnicodeChars(true);
			props.setUseEmptyElementTags(true);
			props.setAdvancedXmlEscape(true);
			props.setTranslateSpecialEntities(true);
			props.setBooleanAttributeValues("empty");

			TagNode node = cleaner.clean(htmlStr);

			htmlStr = new PrettyXmlSerializer(props).getAsString(node, "utf-8");
			DocumentBuilder builder = DocumentBuilderFactory.newInstance()
					.newDocumentBuilder();
			InputStream is = new ByteArrayInputStream(htmlStr.getBytes("utf-8"));
			return builder.parse(is);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}
