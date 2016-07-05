/*
 * Copyright (c) 2015-2020 by caitu99
 * All rights reserved.
 */
package com.caitu99.lsp.perf;

import com.caitu99.lsp.utils.cookie.HttpCookieEx;

import java.util.List;

/**
 * @author yukf
 * @Description: (类职责详细描述, 可空)
 * @ClassName: Test
 * @date 2015年10月28日 下午5:15:00
 * @Copyright (c) 2015-2020 by caitu99
 */
public class Test {

    public static void main(String[] args) {
        String s = "Coremail.sid=WBAffWgukPUjhgIKZRuuQAacGFSzxYZg; Path=/";
        List<HttpCookieEx> cookies = HttpCookieEx.parse(s.toString());
        for (HttpCookieEx cookie : cookies) {
            System.out.println(cookie.toString());
        }
    }
}

class Boo {
    /* (non-Javadoc)
     * @see java.lang.Object#finalize()
     */
    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        System.out.println("super class finalize");
    }
}

class Foo extends Boo {
    private final Object guard = new Object() {
        protected void finalize() throws Throwable {
            System.out.println("sub class finalize");
        }

        ;
    };
    /* (non-Javadoc)
	 * @see com.caitu99.spider.executor.Boo#finalize()
	 */
}
