package com.caitu99.lsp.parser.bank.nongye;

import com.caitu99.lsp.parser.bank.IBank;
import org.springframework.beans.factory.annotation.Value;

/**
 * Created by Administrator on 2015/12/20.
 */
public class Nongye implements IBank {
    @Value("${nongye.name}")
    private String bank;

    @Override
    public String getBank() {
        return bank;
    }
}
