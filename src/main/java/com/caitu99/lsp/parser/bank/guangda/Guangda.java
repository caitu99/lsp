package com.caitu99.lsp.parser.bank.guangda;

import com.caitu99.lsp.parser.bank.IBank;
import org.springframework.beans.factory.annotation.Value;

/**
 * Created by Lion on 2015/12/23 0023.
 */
public class Guangda implements IBank{
    @Value("${guangda.name}")
    private String bank;

    @Override
    public String getBank() {
        return bank;
    }
}
