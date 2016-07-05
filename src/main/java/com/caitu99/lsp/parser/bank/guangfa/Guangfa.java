package com.caitu99.lsp.parser.bank.guangfa;

import com.caitu99.lsp.parser.bank.IBank;
import org.springframework.beans.factory.annotation.Value;

/**
 * Created by Lion on 2015/12/18 0018.
 */
public class Guangfa implements IBank{
    @Value("${guangfa.name}")
    private String bank;

    @Override
    public String getBank() {
        return bank;
    }
}
