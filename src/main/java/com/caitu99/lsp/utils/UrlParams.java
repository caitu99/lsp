/*
 * Copyright (c) 2015-2020 by caitu99
 * All rights reserved.
 */
package com.caitu99.lsp.utils;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

/** 
 * 
 * @Description: (类职责详细描述,可空) 
 * @ClassName: UrlParams 
 * @author fangjunxiao
 * @date 2016年4月13日 上午10:53:48 
 * @Copyright (c) 2015-2020 by caitu99 
 */
public class UrlParams {
	
	 
	private String url;
	
	private String params;
	 
	private Map<String,String> map;
	 
	 
	public UrlParams(String url) {
		this.url = url;
        init();
	}

	private void init(){
	 	if(!params())
	 		return;
		if(StringUtils.isBlank(params))
 			return;
 		valofmap(params.split("&"));
	}
	

	 private boolean params (){
	 	if(StringUtils.isBlank(url))
	 		return false;
	 	try {
	 		this.params = new URL(url).getQuery();
	 	} catch (MalformedURLException e) {
	 		return false;
	 	}
	 	return true;
	 }


	 private void valofmap(String[] val){
	 	Map<String, String> map = new HashMap<String, String>();
	 	for (String string : val) {
	 		 String[] keyval = string.split("=");
	 		if(keyval.length >1){
	 			 map.put(keyval[0], keyval[1]);
	 		}else{
	 		     map.put(keyval[0], "");
	 		}
	 	}
	 	 this.map = map;
	 }
	 
	 public String getVal(String key){
		 if(null == params)
			 return null;
		 
		 if(null == map)
			return "";
		 
		 return map.get(key);
	 }



}
