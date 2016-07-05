package com.caitu99.lsp.parser.bank.huaxia;

import com.caitu99.lsp.parser.bank.IBank;
import org.springframework.beans.factory.annotation.Value;

/**
 * Created by Lion on 2015/12/15 0015.
 */
public class Huaxia implements IBank{

    @Value("${huaxia.name}")
    private String bank;

    @Override
    public String getBank() {
        return bank;
    }
}
