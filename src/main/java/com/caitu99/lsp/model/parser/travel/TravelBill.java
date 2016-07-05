/*
 * Copyright (c) 2015-2020 by caitu99
 * All rights reserved.
 */
package com.caitu99.lsp.model.parser.travel;

import com.caitu99.lsp.model.parser.Bill;

/**
 * @author lhj
 * @Description: (类职责详细描述, 可空)
 * @ClassName: TravelCardParams
 * @date 2015年12月15日 下午4:14:51
 * @Copyright (c) 2015-2020 by caitu99
 */
public class TravelBill extends Bill {
    // 卡号
    private String cardNo;

    /**
     * @return the cardNo
     */
    public String getCardNo() {
        return cardNo;
    }

    /**
     * @param cardNo the cardNo to set
     */
    public void setCardNo(String cardNo) {
        this.cardNo = cardNo;
    }

}
