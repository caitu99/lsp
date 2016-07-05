package com.caitu99.lsp.parser.bank.pingan;

import com.caitu99.lsp.parser.bank.IBank;
import org.springframework.beans.factory.annotation.Value;

public class Pingan implements IBank {

    @Value("${pingan.name}")
    private String bank;

    @Override
    public String getBank() {
        return bank;
    }

}
