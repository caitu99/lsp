package com.caitu99.lsp.parser.bank.zhongxin;

import com.caitu99.lsp.parser.bank.IBank;
import org.springframework.beans.factory.annotation.Value;

public class Zhongxin implements IBank {

    @Value("${zhongxin.name}")
    private String bank;

    @Override
    public String getBank() {
        return bank;
    }

}
