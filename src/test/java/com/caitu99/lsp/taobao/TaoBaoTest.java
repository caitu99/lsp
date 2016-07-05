/*
 * Copyright (c) 2015-2020 by caitu99
 * All rights reserved.
 */
package com.caitu99.lsp.taobao;

import com.caitu99.lsp.AbstractJunit;
import com.caitu99.lsp.utils.ScriptHelper;
import org.junit.Test;

/**
 * @author yang
 * @Description: (类职责详细描述, 可空)
 * @ClassName: TaoBaoTest
 * @date 2015年11月16日 下午5:52:22
 * @Copyright (c) 2015-2020 by caitu99
 */
public class TaoBaoTest extends AbstractJunit {

    @Test
    public void encryption() throws Exception {
        String pwd = ScriptHelper.encryptTaoBaoPassword("1111", "22222", "33333");
        System.out.print(pwd);
    }

}
