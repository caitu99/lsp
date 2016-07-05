/*
 * Copyright (c) 2015-2020 by caitu99
 * All rights reserved.
 */
package com.caitu99.lsp.zhaoshang;

import com.caitu99.lsp.AbstractJunit;
import com.caitu99.lsp.entry.ZhaoShang;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author ws
 * @Description: (类职责详细描述, 可空)
 * @ClassName: ZhaoShangTest
 * @date 2015年11月11日 下午2:33:16
 * @Copyright (c) 2015-2020 by caitu99
 */
public class ZhaoShangTest extends AbstractJunit {

    @Autowired
    ZhaoShang zs;

    /**
     * Test method for {@link com.caitu99.lsp.entry.ZhaoShang#getImgCode()}.
     */
    @Test
    public void testGetImgCode() {
        try {
            System.out.println(zs.getImgCode());
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * Test method for {@link com.caitu99.lsp.entry.ZhaoShang#loginAndGetIntegral(java.lang.String, java.lang.String, java.lang.String, java.lang.String)}.
     */
    @Test
    public void testLoginAndGetIntegral() {
        String idNo = "131182198602066610";
        String pwd = "147258";
        String imgCode = "8083";
        String clientNo = "ClientNo=A22402AE8023AE1811A0E3929E544AC1680302436379915000420345";
        zs.loginAndGetIntegral(idNo, pwd, imgCode, clientNo);
    }

    /**
     * Test method for {@link com.caitu99.lsp.entry.ZhaoShang#loginAndGetIntegral(java.lang.String, java.lang.String, java.lang.String, java.lang.String)}.
     */
    @Test
    public void testIntegral() {
        String idNo = "131182198602066610";
        String pwd = "147258";
        String imgCode = "1017";
        String clientNo = "ClientNo=A22402AE8023AE1811A0E3929E544AC1953621370208267000330029";
        zs.loginAndGetIntegral(idNo, pwd, imgCode, clientNo);
    }


}
