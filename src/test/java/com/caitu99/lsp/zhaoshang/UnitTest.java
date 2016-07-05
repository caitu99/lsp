/*
 * Copyright (c) 2015-2020 by caitu99
 * All rights reserved.
 */
package com.caitu99.lsp.zhaoshang;

import org.apache.commons.lang.StringUtils;
import org.junit.Test;

/**
 * @author ws
 * @Description: (类职责详细描述, 可空)
 * @ClassName: UnitTest
 * @date 2015年11月18日 下午12:16:58
 * @Copyright (c) 2015-2020 by caitu99
 */
public class UnitTest {

    @Test
    public void test() {

        String name = "shenme   (刘传新)";

        name = name.substring(name.indexOf("(") + 1, name.indexOf(")"));
        System.out.println(name);


        String str = "sljdlafdlsh很么我  玩意儿   哈哈   额 ";
        String toFind = "玩意啊";
        System.out.println(StringUtils.containsAny(str, toFind));


    }

}
