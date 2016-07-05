package com.caitu99.lsp.utils;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;

import org.htmlcleaner.CleanerProperties;
import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.PrettyXmlSerializer;
import org.htmlcleaner.TagNode;
import org.jsoup.Jsoup;
import org.jsoup.select.Elements;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class XpathHtmlUtils {
	
    public static Float getNumberFromStr(String nodeStr) {
        String[] balances = nodeStr.trim().split(",");
        Float balance = 0f;
        int increment = 1;
        for (int k = balances.length - 1; k >= 0; k--) {
            // if(k==balances.length-1) {
            // String[] decimalStrs = balances[k].split(".");
            // if(decimalStrs.length>1) {
            // int decimalInc = 1;
            // for (int i = 0; i < decimalStrs[0].length(); i++) {
            // decimalInc *= 0.1;
            // }
            // balance += Float.valueOf(decimalStrs[1]) +
            // Float.valueOf(decimalStrs[0])*decimalInc;
            // } else {
            // balance += Float.valueOf(decimalStrs[0]);
            // }
            // } else {
            // balance += Float.valueOf(balances[k])*increment;
            // }
            balance += Float.valueOf(balances[k].trim()) * increment;
            increment *= 1000;
        }
        return balance;
    }

    public static Integer getNumberFromStrInteger(String nodeStr) {
        String[] balances = nodeStr.trim().split(",");
        int balance = 0;
        int increment = 1;
        for (int k = balances.length - 1; k >= 0; k--) {
            balance += Float.valueOf(balances[k]) * increment;
            increment *= 1000;
        }
        return balance;
    }

    public static String getNodeText(String exp, XPath xpath, Document document)
            throws Exception {
        Node node = (Node) xpath.evaluate(exp, document, XPathConstants.NODE);
        if (null != node) {
            return node.getTextContent().trim();
        }
        return "";
    }

    public static List<String> getNodeListText(String exp, String exp1,
                                               String exp2, XPath xpath, Document document) throws Exception {
        List<String> list = new ArrayList<String>();
        NodeList nodeList = (NodeList) xpath.evaluate(exp, document,
                XPathConstants.NODESET);
        for (int i = 1; i < nodeList.getLength(); i++) {
            Node node = (Node) xpath.evaluate(exp1 + (i + 1) + exp2, document,
                    XPathConstants.NODE);
            if (null != node) {
                list.add(node.getTextContent().trim());
            }
        }
        return list;
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

            // TagNode node = cleaner.clean(new
            // File("C:\\Users\\yang\\Desktop\\testXPath\\zhaoshangwhite02.html"));
            TagNode node = cleaner.clean(htmlStr);
            // new PrettyXmlSerializer(props).writeToFile(node,
            // this.getClass().getResource("/").getPath()+"tempXml.xml",
            // "utf-8");

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

    public static String deleteHeadHtml(String fileDir) {
        if (fileDir.indexOf("<html") >= 0) {
            return fileDir.substring(fileDir.indexOf("<html"));
        } else if (fileDir.indexOf("<HTML") >= 0) {
            return fileDir.substring(fileDir.indexOf("<HTML"));
        } else {
            return fileDir;
        }
    }

    public static void main(String[] args) {
        System.out.println(XpathHtmlUtils.getNumberFromStr("59,486.32   "
                .trim()));
    }

    public static String getNodeValue(String exp, XPath xpath, Document document)
            throws Exception {
        Node node = (Node) xpath.evaluate(exp, document, XPathConstants.NODE);
        if (null != node) {
            return ((Element) node).getAttribute("value");
        }
        return "";
    }
    

    public static String getTDValue(String body,int index)
            throws Exception {
    	
    	org.jsoup.nodes.Document document = Jsoup.parse(body);
        Elements elements = document.getElementsByTag("td");
        
        /*for(int i = 0; i< elements.size(); i++){
        	System.out.print(elements.get(i).html());
			System.out.println("    index:"+i);
        }*/
        
        if(null != elements.get(index)){
        	return elements.get(index).html();
        }
        return "";
    }
    
    public static String cleanHtml(String html){
    	HtmlCleaner cleaner = new HtmlCleaner();
        CleanerProperties props = cleaner.getProperties();
        props.setUseCdataForScriptAndStyle(true);
        props.setRecognizeUnicodeChars(true);
        props.setUseEmptyElementTags(true);
        props.setAdvancedXmlEscape(true);
        props.setTranslateSpecialEntities(true);
        props.setBooleanAttributeValues("empty");

        TagNode node = cleaner.clean(html);

        html = new PrettyXmlSerializer(props).getAsString(node, "utf-8");
        return html;
    }

}
