package com.caitu99.lsp.parser.bank.zhaoshang;

import com.caitu99.lsp.parser.bank.IBank;
import org.springframework.beans.factory.annotation.Value;

public class Zhaoshang implements IBank {

    @Value("${zhaoshang.name}")
    private String bank;

    @Override
    public String getBank() {
        return bank;
    }

}
