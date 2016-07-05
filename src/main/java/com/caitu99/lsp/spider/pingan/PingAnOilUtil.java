/*
 * Copyright (c) 2015-2020 by caitu99
 * All rights reserved.
 */
package com.caitu99.lsp.spider.pingan;

import java.nio.channels.SeekableByteChannel;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;

/** 
 * 
 * @Description: (类职责详细描述,可空) 
 * @ClassName: PingAnOilUtil 
 * @author ws
 * @date 2016年4月5日 下午4:13:51 
 * @Copyright (c) 2015-2020 by caitu99 
 */
public class PingAnOilUtil {
	
	public static String getSessionId(){
		Calendar date = Calendar.getInstance();
		String sessionId = PingAnOilUtil.getValue(date.get(Calendar.YEAR)) +
				PingAnOilUtil.getValue(date.get(Calendar.MONTH) + 1) +
                PingAnOilUtil.getValue(date.get(Calendar.DATE)) +
                PingAnOilUtil.getValue(date.get(Calendar.HOUR)) +
                PingAnOilUtil.getValue(date.get(Calendar.MINUTE)) +
                PingAnOilUtil.getValue(date.get(Calendar.SECOND)) +
                PingAnOilUtil.getValue(date.get(Calendar.MILLISECOND))
                + Double.valueOf(Math.random()).toString().substring(2) + "%7C3";
		
		return sessionId;
	}
	
	private static String getValue(Integer a){
		if(a<10){
			return "0" + a;
		}else{
			return "" + a;
		}
	}
	
	

	public static String getTimeStamp() {
		String randomNumber = "";
		Random rd = new Random();
	    for(int i=0;i<6;i++){
	        randomNumber += rd.nextInt(10);
	    }
	    String timestamp = new Date().getTime() + randomNumber;
		return timestamp;
	}


	
}
