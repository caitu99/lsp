package com.caitu99.lsp.parser.travel.yazhouwanlitong;

import org.springframework.beans.factory.annotation.Value;

import com.caitu99.lsp.parser.bank.IBank;
import com.caitu99.lsp.parser.travel.ITravel;

public class Yazhouwanlitong implements ITravel{
	@Value("${yazhouwanlitong.name}")
    private String travel;

    @Override
    public String getTravel() {
        return travel;
    }
}

