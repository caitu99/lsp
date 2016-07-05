package com.caitu99.lsp.utils;

import javax.script.*;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URLEncoder;

public class ScriptHelper {

    private static ScriptEngine engine;
    private static String cityBank = "%s = {}; %s.f = function() { %s return \"\"; }; %s.f();";

    static {
        ScriptEngineManager manager = new ScriptEngineManager();
        engine = manager.getEngineByName("nashorn");
        Bindings engineScope = engine.getBindings(ScriptContext.ENGINE_SCOPE);
        engineScope.put("window", engineScope);
        engineScope.put("navigator", engineScope);
    }

    static {
        InputStream commonStr = ScriptHelper.class.getClassLoader().getResourceAsStream("script/common.js");
        BufferedReader commonBr = new BufferedReader(new InputStreamReader(commonStr));

        InputStream streamQQ = ScriptHelper.class.getClassLoader().getResourceAsStream("script/mailqq.js");
        BufferedReader qqBr = new BufferedReader(new InputStreamReader(streamQQ));

        InputStream aloneStream = ScriptHelper.class.getClassLoader().getResourceAsStream("script/mailqqalone.js");
        BufferedReader aloneBr = new BufferedReader(new InputStreamReader(aloneStream));

        InputStream sinaStream = ScriptHelper.class.getClassLoader().getResourceAsStream("script/sinassologin.js");
        BufferedReader sinaBr = new BufferedReader(new InputStreamReader(sinaStream));

        InputStream jingdongStr = ScriptHelper.class.getClassLoader().getResourceAsStream("script/jingdong.js");
        BufferedReader jingdongBr = new BufferedReader(new InputStreamReader(jingdongStr));

        InputStream taobaoStr = ScriptHelper.class.getClassLoader().getResourceAsStream("script/taobaoRSA.js");
        BufferedReader taobaoBr = new BufferedReader(new InputStreamReader(taobaoStr));

        InputStream ccbStr = ScriptHelper.class.getClassLoader().getResourceAsStream("script/ccb_enc.js");
        BufferedReader ccbBr = new BufferedReader(new InputStreamReader(ccbStr));

        InputStream yjf189Str = ScriptHelper.class.getClassLoader().getResourceAsStream("script/yjf189_enc.js");
        BufferedReader yjf189Br = new BufferedReader(new InputStreamReader(yjf189Str));

        InputStream uCStr = ScriptHelper.class.getClassLoader().getResourceAsStream("script/unicomChina.js");
        BufferedReader uCBr = new BufferedReader(new InputStreamReader(uCStr));
        try {
            engine.eval(commonBr);
            engine.eval(qqBr);
            engine.eval(aloneBr);
            engine.eval(sinaBr);
            engine.eval(jingdongBr);
            engine.eval(taobaoBr);
            engine.eval(ccbBr);
            engine.eval(yjf189Br);
            engine.eval(uCBr);
        } catch (ScriptException ignored) {
            ignored.printStackTrace();
        }
    }

    public static String encryptQQPassword(String password, String salt, String randStr)
            throws ScriptException, NoSuchMethodException {
        Invocable invocable = (Invocable) engine;
        Object result = invocable.invokeFunction("getQQEncryption", password, salt, randStr);
        return result.toString();
    }

    public static String encryptQQAlonePwd(String pwdalone, String ts) throws Exception {
        Invocable invocable = (Invocable) engine;
        Object result = invocable.invokeFunction("getQQEncryptionAlone", pwdalone, ts);
        return result.toString();
    }

    public static String encryptSinaPassword(String password, String key, Long serverTime, String nonce) throws Exception {
        Invocable invocable = (Invocable) engine;
        Object result = invocable.invokeFunction("getSinaEncryption", password, key, serverTime, nonce);
        return result.toString();
    }

    public static String encryptSinaUsername(String username) throws Exception {
        Invocable invocable = (Invocable) engine;
        Object result = invocable.invokeFunction("getSinaEncodeUsername", URLEncoder.encode(username, "utf-8"));
        return result.toString();
    }

    public static String encryptJDPassword(String password, String rsaValue) throws Exception {
        Invocable invocable = (Invocable) engine;
        Object result = invocable.invokeFunction("getJDEncryption", password, rsaValue);
        return result.toString();
    }

    public static String encryptTaoBaoPassword(String exponent, String module, String password) throws Exception {
        Invocable invocable = (Invocable) engine;
        Object result = invocable.invokeFunction("getTaoBaoEncryption", exponent, module, password);
        return result.toString();
    }

    public static String getCityBankExtraXXX(String userid, String script) throws ScriptException, NoSuchMethodException {
        String area = "a" + userid;
        script = String.format(cityBank, area, area, script, area);
        String ans = engine.eval(script).toString();
        String delete = "delete " + area + " ;";
        engine.eval(delete);
        return ans;
    }
    
    public static String encryptCCBPasword(String password) throws Exception{
    	Invocable invocable = (Invocable) engine;
        Object result = invocable.invokeFunction("calcMD5",  password);
        return result.toString();
    }
    

    public static String encryptYJF189Pasword(String password) throws Exception{
    	Invocable invocable = (Invocable) engine;
        Object result = invocable.invokeFunction("aesDecrypt",  password);
        return result.toString();
    }
    

    public static String aesEncryptYJF189(String password) throws Exception{
    	Invocable invocable = (Invocable) engine;
        Object result = invocable.invokeFunction("AESEncrypt",  password);
        return result.toString();
    }

    public static String aesDecryptYJF189(String password) throws Exception{
    	Invocable invocable = (Invocable) engine;
        Object result = invocable.invokeFunction("AESDecrypt",  password);
        return result.toString();
    }

    public static String vjHash(String c) throws Exception{
    	Invocable invocable = (Invocable) engine;
        Object result = invocable.invokeFunction("vjHash",  c);
        return result.toString();
    }
    
    public static String getuuid() throws Exception{
    	Invocable invocable = (Invocable) engine;
        Object result = invocable.invokeFunction("uuid");
        return result.toString();
    }
    
    public static String getCof() throws Exception{
    	Invocable invocable = (Invocable) engine;
        Object result = invocable.invokeFunction("getCof");
        return result.toString();
    }

    public static String toString16(String v) throws Exception{
    	Invocable invocable = (Invocable) engine;
        Object result = invocable.invokeFunction("toString16",v);
        return result.toString();
    }

    public static void main(String[] args) {
        /*try {
            String rsaValue = "D1F889AF43DC7820989094C00976252A630C3A5519AA73AA3FB7B33770ECDE95407960EBD141370DADEEE11902D0B3BDA5EC4BA6EE27963173A94BD39CFABF56BFA46C5B17F9480AA31F3DB0A22D9BAD0EF98BCD00CDACDDEB575A98564C31EE71DD7F228DE581697836DAEE2AF2FB5C9EA836CA89BC06E12CA5EAD960D1E313";
            System.out.println(encryptJDPassword("yukaifeng2a", rsaValue));
            //System.out.println(encryptQQAlonePwd("yukaifeng2a", "1445308038"));
        } catch (Exception e) {
            e.printStackTrace();
        }*/
    	String str = "U2FsdGVkX18CmV3oHX13cB9enmvROULxYhzqOh225AjYdM1aSutPXNCEobS9kwwE4aFHSbAhVHiFPz+Lc7Y3rb1EXjiuQTBlEMwziufUWBE=";
    	String str2 = "13325853121$$201$地市（中文/拼音）$12$$$0";
    	try {

    		System.out.println(getuuid());
    		
    		/*String enStr2 =  aesEncryptYJF189(str2);
    		String deStr2 = aesDecryptYJF189(enStr2);
    		String deStr = aesDecryptYJF189(str);
			System.out.println("加密："+enStr2);
			System.out.println("解密："+deStr2);
			System.out.println("解密："+deStr);*/
			//mR3+nmF0aQO6gmL98Hg3dA==
			//ciY/j3tfg8zBxkpQblrqcg==
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    }

}
