package com.caitu99.lsp.parser.utils;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DOMHelper {

	private final static Logger logger = LoggerFactory.getLogger(DOMHelper.class);
    private static final String regEx_script = "<script[^>]*?>[\\s\\S]*?<\\/script>"; // 定义script的正则表达式
    private static final String regEx_style = "<style[^>]*?>[\\s\\S]*?<\\/style>"; // 定义style的正则表达式
    private static final String regEx_html = "<[^>]+>"; // 定义HTML标签的正则表达式
    private static final String regEx_space = "\\s*|\t|\r|\n";// 定义空格回车换行符

    public static Element getMaxContainer(Elements elements, String keyStr) {
        Elements newElements = null;
        if (StringUtils.isEmpty(keyStr)) {
            newElements = elements;
        } else {
            String[] keys = keyStr.split("&");
            newElements = new Elements();
            for (Element element : elements) {
                boolean flag = true;
                for (String key : keys) {
                    if (!element.html().contains(key)) {
                        flag = false;
                    }
                }
                if (flag) {
                    newElements.add(element);
                }
            }
        }
        int len = Integer.MIN_VALUE;
        Element e = null;
        for (Element element : newElements) {
            if (element.html().length() > len) {
                len = element.html().length();
                e = element;
            }
        }
        return e;
    }

    public static Element getMinContainer(Elements elements, String keyStr) {
        Elements newElements = filter(elements, keyStr);
        int len = Integer.MAX_VALUE;
        Element e = null;
        for (Element element : newElements) {
            if (element.html().length() < len) {
                len = element.html().length();
                e = element;
            }
        }
        return e;
    }

	public static Elements filter(Elements elements, String keyStr) {

		Elements newElements = null;
		if (StringUtils.isEmpty(keyStr)) {
			newElements = elements;
		} else {
			String[] keys = keyStr.split("&");
			newElements = new Elements();
			for (Element element : elements) {
				boolean flag = true;
				for (String key : keys) {
					if (!element.html().contains(key)) {
						flag = false;
					}
				}
				if (flag) {
					newElements.add(element);
				}
			}
		}

        return newElements;
    }

    // 去除html
    public static String filterHtml(String str) {
        Pattern p_script = Pattern.compile(regEx_script,
                Pattern.CASE_INSENSITIVE);
        Matcher m_script = p_script.matcher(str);
        str = m_script.replaceAll(""); // 过滤script标签

        Pattern p_style = Pattern
                .compile(regEx_style, Pattern.CASE_INSENSITIVE);
        Matcher m_style = p_style.matcher(str);
        str = m_style.replaceAll(""); // 过滤style标签

        Pattern p_html = Pattern.compile(regEx_html, Pattern.CASE_INSENSITIVE);
        Matcher m_html = p_html.matcher(str);
        str = m_html.replaceAll(""); // 过滤html标签

        Pattern p_space = Pattern
                .compile(regEx_space, Pattern.CASE_INSENSITIVE);
        Matcher m_space = p_space.matcher(str);
        str = m_space.replaceAll(""); // 过滤空格回车标签
        return str.trim(); // 返回文本字符串
    }
    

}
