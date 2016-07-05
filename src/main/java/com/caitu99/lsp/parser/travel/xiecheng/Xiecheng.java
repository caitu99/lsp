package com.caitu99.lsp.parser.travel.xiecheng;

import com.caitu99.lsp.parser.travel.ITravel;
import org.springframework.beans.factory.annotation.Value;

public class Xiecheng implements ITravel{
	@Value("${xiecheng.name}")
    private String travel;

    @Override
    public String getTravel() {
        return travel;
    }
}
