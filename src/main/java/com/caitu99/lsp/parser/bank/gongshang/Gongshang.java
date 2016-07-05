package com.caitu99.lsp.parser.bank.gongshang;

import com.caitu99.lsp.parser.bank.IBank;
import org.springframework.beans.factory.annotation.Value;

public class Gongshang implements IBank {

    @Value("${gongshang.name}")
    private String bank;

    @Override
    public String getBank() {
        return bank;
    }

}
