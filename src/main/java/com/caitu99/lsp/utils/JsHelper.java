package com.caitu99.lsp.utils;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

public class JsHelper {
    private static ScriptEngine jsEngine;

    static {
        ScriptEngineManager manager = new ScriptEngineManager();
        jsEngine = manager.getEngineByName("javascript");
    }

    public static String getCityBankExtraXXX(String script) throws ScriptException, NoSuchMethodException {
        //System.out.println(script);
        return jsEngine.eval(script).toString();
    }

    public static String getJSEXEResult(String script) throws ScriptException,NoSuchMethodException{
        return jsEngine.eval(script).toString();
    }

    public static void main(String[] args) throws NoSuchMethodException, ScriptException {
        System.out.println(getCityBankExtraXXX("function(){if(!navigator.cookieEnabled){this.p+=\"&WT.vt_f=2\"; return  }var F=\"2\"; var E=new Date(); var D=new Date(E.getTime()+315360000000); var C=new Date(E.getTime()); if(document.cookie.indexOf(\"WT_FPC=\")!=-1){this.p+=\"&WT.vt_f=3\"; F=document.cookie.substring(document.cookie.indexOf(\"WT_FPC=\")+10); if(F.indexOf(\";\")!=-1){F=F.substring(0,F.indexOf(\";\")) }if(E.getTime()<((new Date(parseInt(F.substring(F.indexOf(\":lv=\")+4,F.indexOf(\":ss=\"))))).getTime()+1800000)){C.setTime((new Date(parseInt(F.substring(F.indexOf(\":ss=\")+4)))).getTime()) }else{this.p+=\"&WT.entry=2\" }F=F.substring(0,F.indexOf(\":lv=\")) }if(F.length<10){this.p+=\"&WT.vt_f=1&WT.entry=1\"; var B=E.getTime().toString(); for(var A=2; A<=(32-B.length); A++){F+=Math.floor(Math.random()*16).toString(16) }F+=B }F=encodeURIComponent(F); this.p+=\"&WT.co_f=\"+F; return \"WT_FPC=id=\"+F+\":lv=\"+E.getTime().toString()+\":ss=\"+C.getTime().toString()+\"; expires=\"+D.toGMTString()+\"; path=/; domain=.10086.cn\"; };"));
    }
}
