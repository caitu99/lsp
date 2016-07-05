/*
 * Copyright (c) 2015-2020 by caitu99
 * All rights reserved.
 */
package com.caitu99.lsp.spider.liantong;

import java.util.Date;

import com.caitu99.lsp.utils.ScriptHelper;

/** 
 * 
 * @Description: (类职责详细描述,可空) 
 * @ClassName: UnicomChinaUtils 
 * @author ws
 * @date 2016年3月22日 上午11:20:02 
 * @Copyright (c) 2015-2020 by caitu99 
 */
public class UnicomChinaUtils {
	public static String ID = "a9e72dfe4a54a20c3d6e671b3bad01d9";
	
	public static String getVjuids(){
		/*try {
			String A = ScriptHelper.vjHash("");
			Date B = new Date();
			
			return A + "." + ScriptHelper.toString16(String.valueOf(B.getTime())) + "." + ScriptHelper.toString16(String.valueOf(Math.random()));
		} catch (Exception e) {
			return "288fbe1ab.1539dd1c334.0.cc2ff08f";
		}*/
		return "288fbe1ab.1539dd1c334.0.cc2ff08f";
	}

	public static String getVjlast(){
		Date F = new Date();
		String U = "30";
	    String E = String.valueOf(Math.round(F.getTime() / 1000));//vjGetTimestamp(F.getTime());
	    String R = E + "." + E + ".30";
	    return R;
		//return "1458641683.1458641683.30";
	}
	
	public static String getWT_FPC(){
		try {
			String cof = ScriptHelper.getCof();
			Date dCur=new Date();
			Long offset = 8L;
			Long adj = (dCur.getTimezoneOffset()*60000)+(offset *3600000);
			dCur.setTime(dCur.getTime()+adj);
			Date dExp = new Date(dCur.getTime()+315360000000L);
			Date dSes=new Date(dCur.getTime());
			
			return "id="+cof+":lv="+dCur.getTime()+":ss="+dSes.getTime();
		} catch (Exception e) {

			return "id=2c03626538a9223238d1458641683271:lv=1458641683274:ss=1458641683271";
		}
	}

	public static String get_n3fa_cid(){
		
		try {
			return ScriptHelper.getuuid();
		} catch (Exception e) {

			return "";
		}
	}

	public static Long get_n3fa_ext(){
		Date now = new Date();
		return now.getTime();
	}
	
}
