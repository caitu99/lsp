package com.caitu99.lsp.parser.travel.yilong;

import org.springframework.beans.factory.annotation.Value;
import com.caitu99.lsp.parser.bank.IBank;
import com.caitu99.lsp.parser.travel.ITravel;

public class Yilong implements ITravel{
	@Value("${yilong.name}")
    private String travel;

    @Override
    public String getTravel() {
        return travel;
    }

}
