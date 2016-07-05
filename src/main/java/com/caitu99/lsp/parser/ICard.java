package com.caitu99.lsp.parser;


import com.caitu99.lsp.model.parser.Bill;
import com.caitu99.lsp.model.parser.BillType;
import com.caitu99.lsp.model.parser.ParserContext;
import com.caitu99.lsp.parser.ITpl;

import java.util.List;

public interface ICard {

    Integer getId();

    String getName();

    BillType getBillType();

    boolean is(String title, String sender);

    List<Class> getTpls();

    void setTpls(List<Class> tpls);

    void merge(Bill curBill);

}
