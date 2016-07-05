package com.caitu99.lsp.parser.bank.xingye;

import org.springframework.beans.factory.annotation.Value;

import com.caitu99.lsp.parser.bank.IBank;

public class Xingye implements IBank{
	@Value("${xingye.name}")
    private String bank;

    @Override
    public String getBank() {
        return bank;
    }

}
