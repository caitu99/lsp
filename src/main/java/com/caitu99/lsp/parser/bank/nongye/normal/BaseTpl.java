package com.caitu99.lsp.parser.bank.nongye.normal;

import com.caitu99.lsp.model.parser.ParserContext;
import com.caitu99.lsp.parser.BaseConfig;
import com.caitu99.lsp.parser.ICard;
import com.caitu99.lsp.parser.ITpl;
import com.caitu99.lsp.utils.SpringContext;

/**
 * Created by Administrator on 2015/12/20.
 */
public abstract class BaseTpl extends BaseConfig implements ITpl {

    private ICard card;

    public BaseTpl(ParserContext context) {
        super(context);
        card = SpringContext.getBean(NormalCard.class);
    }

    @Override
    public ICard getCard() {
        return card;
    }

    @Override
    public void setCard(ICard card) {
        this.card = card;
    }

    @Override
    public boolean is() {
        return false;
    }
}
