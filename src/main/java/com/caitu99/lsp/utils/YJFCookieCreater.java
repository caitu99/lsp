/*
 * Copyright (c) 2015-2020 by caitu99
 * All rights reserved.
 */
package com.caitu99.lsp.utils;

import java.net.URLEncoder;
import java.util.Calendar;
import java.util.Date;

import javax.script.Invocable;

/** 
 * 
 * @Description: (类职责详细描述,可空) 
 * @ClassName: YJFCookieCreater 
 * @author ws
 * @date 2016年3月11日 上午9:52:19 
 * @Copyright (c) 2015-2020 by caitu99 
 */
public class YJFCookieCreater {
	
	
	public static Long getQcWId(){
		
		Long res = new Date().getTime() % 1000;
		//System.out.println(res);
		return res;
		
	}
	

	public static Long getPgvPvid(){
		
		Calendar cal = Calendar.getInstance();
		
		Long res = (Math.round(Math.random() 
					* 2147483647) 
					* (cal.getTimeInMillis())) 
						% new Long("10000000000");
		//System.out.println(res);
		return res;
		
	}
	
	

	public static String getSPers(){
		
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
		fid = fid+"|"+ Calendar.getInstance().getTimeInMillis();
		//System.out.println(fid);
		return fid;
		
	}
	

	public static String getNvid(){
		
		return "1";
	}
	

	public static String getSSess(){
		
		return URLEncoder.encode("s_cc=true; s_sq=;");
	}

    public static String getECSReqInfoLogin1(String account,String provinceId){
    	
    	String str = account+"$$201$地市（中文/拼音）$"+provinceId+"$$$0";
		return URLEncoder.encode(str);
    	
    }
    
    public static String getTrkHmClickCoords(){
    	String str = "931,379";
		return URLEncoder.encode(str);
    }

    public static String getLoginStatus(){
    	return "non-logined";
    }
    
	public static String getLvid(){
		String a = "";
	    String[] b="abcdef1234567890".split("");
        for(int n=0;n<32;n++){
            a += b[(int) Math.round(Math.random()*(b.length-1))];
        }
        //System.out.println(a);
	    return a;
	}
    
	public static String getECSLoginReq(){
		return "ReqPath=WKTUcaKxENIPN9YZ15CSxnHlCOOcAJo2tDiTJUlpXfU=&ReqQuery=7cq6GDrJwEJ4a/icxOuKTs7cHv0iSjzBfuEqJihP+EPKJl118jOHziNzGIldhd7G8y6JBCGI+Sz1xjOJV35YD9Iu07s5QllrG14WOh1b1xVcDqj3p+ujS1i25IxAcvuwx4hkJqyrmsbeUMzYelWl2eFPHROKGGGSag9GwaN2HxM=&ResPath=";
	}
	
    public static void main(String[] args) {
    	System.out.print("__qc_wId="+YJFCookieCreater.getQcWId()+";");
    	System.out.print("pgv_pvid="+YJFCookieCreater.getPgvPvid()+";");
    	System.out.print("lvid="+YJFCookieCreater.getLvid()+";");
    	System.out.print("nvid="+YJFCookieCreater.getNvid()+";");
    	System.out.print("s_sess="+YJFCookieCreater.getSSess()+";");
    	System.out.print("s_pers="+YJFCookieCreater.getSPers()+";");
    	System.out.print("loginStatus="+YJFCookieCreater.getLoginStatus()+";");
    	System.out.print("ECS_ReqInfo_login1="+YJFCookieCreater.getECSReqInfoLogin1("13325853121", "12")+";");
    	System.out.print("trkHmClickCoords="+YJFCookieCreater.getTrkHmClickCoords()+";");
    	System.out.print("ECSLoginReq="+YJFCookieCreater.getECSLoginReq());
    	
	}
	
	
}
