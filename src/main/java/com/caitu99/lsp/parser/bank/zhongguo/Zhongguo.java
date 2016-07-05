package com.caitu99.lsp.parser.bank.zhongguo;

import com.caitu99.lsp.parser.bank.IBank;
import org.springframework.beans.factory.annotation.Value;

public class Zhongguo implements IBank {

    @Value("${zhongguo.name}")
    private String bank;

    @Override
    public String getBank() {
        return bank;
    }

}
