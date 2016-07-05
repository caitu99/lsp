/*
 * Copyright (c) 2015-2020 by caitu99
 * All rights reserved.
 */
package com.caitu99.lsp.utils;

import java.io.IOException;
import java.util.Base64;
import java.util.Base64;
import java.util.Calendar;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;

import org.junit.Test;

import com.caitu99.lsp.model.spider.pufabank.PufaBankResult;

/** 
 * 
 * @Description: (类职责详细描述,可空) 
 * @ClassName: StringUtilTest 
 * @author ws
 * @date 2016年3月10日 下午8:34:19 
 * @Copyright (c) 2015-2020 by caitu99 
 */
public class StringUtilTest {

	@Test
	public void test() {
		String d="0123456789ABCDEF";
		String k="s_fid";
		String fid="";
		String h="";
		String l="";
		int j=0;
		int m=8;
		int n=4;
		Date e=new Date();
		
		for(int i=0;i<16;i++){
			j=(int) Math.floor(Math.random()*m);
			h+=d.substring(j,j+1);
			j=(int) Math.floor(Math.random()*n);
			l+=d.substring(j,j+1);
			m=n=16;
		}
		fid=h+'-'+l;
		
		System.out.println(fid);
	}

	
	@Test
	public void base64Encode(){
		String str = "252406";
		
		System.out.println(str+Base64.getEncoder().encodeToString(str.getBytes()));
		
	}
	
	
	@Test
	public void dxPhoneTest(){
         String reg2 = "1(33|53|80|89|81|77)[0-9]{8}";
         String phoneNum = "15858284090";
         
         if(phoneNum.matches(reg2)){
        	 System.out.println("这是电信用户");
         }else{
        	 System.out.println("这是非电信用户");
         }
	}
	
	@Test
	public void rex(){
		Pattern LOGIN_PAGE_PAT = Pattern.compile("(?<=<iframe).*?(?=\">)");//20151228 chencheng mod RSA_VAL取值变化
		
		String resultStr = "<iframe height=\"205px\" frameborder=\"0\" style=\"background-color: rgb(250,250,250)\" width=\"200px\" scrolling=\"no\" src=\"http://uac.10010.com/portal/jfLogin.html?UniTokenRequest=10010%244Haubt6VJ2uct9Fjv1gKadljHRnXVH%2F1DXUNEeh613SjFFiDLbQbh3rLonW3UGJo\"></iframe></div>";
		Matcher matcher = LOGIN_PAGE_PAT.matcher(resultStr  );
		if (matcher.find()) {
			String rsaValue = matcher.group(0);
			//rsaValue = rsaValue.substring(17, rsaValue.length()-2);//20151228 chencheng mod rsa_val取值变化
			System.out.println(rsaValue);
		} 
	}
	
	
	@Test
	public void XPathTest() throws Exception{
		String resBody = "";
    	
		resBody = FileNIOCommon.readFileToString("D:\\GitSpace\\lsp\\src\\test\\java\\com\\caitu99\\lsp\\imageCode.txt");
		
		// 用户名
		String nameText = XpathHtmlUtils.getTDValue(resBody,6);
		// 用户名
		String cardNoText = XpathHtmlUtils.getTDValue(resBody,9);
		// 积分
		String pointsText = XpathHtmlUtils.getTDValue(resBody,21);
		
		System.out.println("nameText:"+nameText+"cardNoText:"+cardNoText+"pointsText:"+pointsText);
    	
	}
	
	
	@Test
	public void dateTest(){
		Calendar date = Calendar.getInstance();
		String sessionId = this.getValue(date.get(date.YEAR)) +
				this.getValue(date.get(date.MONTH) + 1) +
                this.getValue(date.get(date.DATE)) +
                this.getValue(date.get(date.HOUR)) +
                this.getValue(date.get(date.MINUTE)) +
                this.getValue(date.get(date.SECOND)) +
                this.getValue(date.get(date.MILLISECOND))
                + Double.valueOf(Math.random()).toString().substring(2) + "|3";
		
		System.out.println(sessionId);
	}
	
	public String getValue(Integer a){
		if(a<10){
			return "0" + a;
		}else{
			return "" + a;
		}
	}
	
}
