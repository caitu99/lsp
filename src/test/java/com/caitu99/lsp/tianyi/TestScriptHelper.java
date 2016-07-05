package com.caitu99.lsp.tianyi;

import com.caitu99.lsp.utils.JsHelper;
import com.caitu99.lsp.utils.ScriptHelper;
import org.junit.Test;

public class TestScriptHelper {

    @Test
    public void testEncryptPassword() throws Exception {

        String key = "EB2A38568661887FA180BDDB5CABD5F21C7BFD59C090CB2D245A87AC253062882729293E5506350508E7F9AA3BB77F4333231490F915F6D63C55FE2F08A49B353F444AD3993CACC02DB784ABBB8E42A9B1BBFFFB38BE18D78E87A0E41B9B8F73A928EE0CCEE1F6739884B9777E4FE9E88A1BBE495927AC4A799B3181D6442443";
        String opassword = "";
        Long serverTime = 1447156507L;
        String nonce = "DJ9UNO";
        String password = ScriptHelper.encryptSinaPassword(opassword, key, serverTime, nonce);
        System.out.println(password);
    }


    @Test
    public void testEncryptSinaUsername() throws Exception {
        String username = "huang_yue_28@sina.com";
        username = ScriptHelper.encryptSinaUsername(username);
        System.out.println(username);

    }

    @Test
    public void testGetCityBankExtraXXX() throws Exception {
        String f1 = "s = {};\n" +
                "s.f = function() {\n" +
                "\tvar eK1Ks6DHOFP16eNPsXv;\n" +
                "\tvar a3iPo9xOlnDQq4CC4=1;\n" +
                "\tvar aN0wIAdrPnZcbizNg='YNXw2F5WmaGHUZY';\n" +
                "\tvar O753ddYkQqtmTXZ3a='ba3389e23f9da2f';\n" +
                "\tif(O753ddYkQqtmTXZ3a!=null && O753ddYkQqtmTXZ3a!=\"\" && O753ddYkQqtmTXZ3a!=\"null\")\n" +
                "\t{\n" +
                "\t\tvar RoHDGZAszhPnmO2n='6';\n" +
                "\t\tvar ue7Ey3fktt5TSW31='9';\n" +
                "\t\treturn O753ddYkQqtmTXZ3a.substring(O753ddYkQqtmTXZ3a.length-RoHDGZAszhPnmO2n);\n" +
                "\t}\n" +
                "\treturn \"\";\n" +
                "}; s.f();";
        System.out.println(f1);
        String extraXXX = JsHelper.getCityBankExtraXXX(f1);
        f1 = "delete s;";
        extraXXX = JsHelper.getCityBankExtraXXX(f1);
        f1 = "delete s";
        extraXXX = JsHelper.getCityBankExtraXXX(f1);
        System.out.println(extraXXX);
    }

    @Test
    public void testEncryptQQPassword() throws Exception {

    }

    @Test
    public void testEncryptQQAlonePwd() throws Exception {

    }

    @Test
    public void testEncryptSinaPassword() throws Exception {

    }

    @Test
    public void testEncryptJDPassword() throws Exception {

    }

    @Test
    public void testEncryptQQPassword1() throws Exception {

    }

    @Test
    public void testMain() throws Exception {

    }
}