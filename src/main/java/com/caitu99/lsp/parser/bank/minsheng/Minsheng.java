package com.caitu99.lsp.parser.bank.minsheng;

import com.caitu99.lsp.parser.bank.IBank;
import org.springframework.beans.factory.annotation.Value;

/**
 * Created by Lion on 2015/12/19 0019.
 */
public class Minsheng implements IBank{
    @Value("${minsheng.name}")
    private String bank;

    @Override
    public String getBank() {
        return bank;
    }
}
