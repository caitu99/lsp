package com.caitu99.lsp.parser.bank.pufa;

import org.springframework.beans.factory.annotation.Value;

import com.caitu99.lsp.parser.bank.IBank;

public class Pufa implements IBank{
	
	@Value("${pufa.name}")
    private String bank;

    @Override
    public String getBank() {
        return bank;
    }

}
