package com.caitu99.lsp.spider.mailqq;

import org.junit.Test;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class MailQQSpiderTest {

    @Test
    public void testOnEvent() throws Exception {
        String s = "var rsa_n=\"D1F889AF43DC7820989094C00976252A630C3A5519AA73AA3FB7B33770ECDE95407960EBD141370DADEEE11902D0B3BDA5EC4BA6EE27963173A94BD39CFABF56BFA46C5B17F9480AA31F3DB0A22D9BAD0EF98BCD00CDACDDEB575A98564C31EE71DD7F228DE581697836DAEE2AF2FB5C9EA836CA89BC06E12CA5EAD960D1E313\"";
        Pattern pattern = Pattern.compile("(?<=rsa_n=\").*?(?=\")");
        Matcher matcher = pattern.matcher(s);

        if (matcher.find()) {
            System.out.println(matcher.group(0));
        }
    }
}