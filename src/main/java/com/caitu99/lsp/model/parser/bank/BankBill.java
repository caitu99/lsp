/*
 * Copyright (c) 2015-2020 by caitu99
 * All rights reserved.
 */
package com.caitu99.lsp.model.parser.bank;

import com.caitu99.lsp.model.parser.Bill;

/**
 * @author lhj
 * @Description: (类职责详细描述, 可空)
 * @ClassName: BankCardParams
 * @date 2015年12月15日 下午4:12:50
 * @Copyright (c) 2015-2020 by caitu99
 */
public class BankBill extends Bill {

	// 卡号
	private String cardNo;

	// 账单是否独立
	private boolean isAlone;

	/**
	 * @return the cardNo
	 */
	public String getCardNo() {
		return cardNo;
	}

	/**
	 * @param cardNo
	 *            the cardNo to set
	 */
	public void setCardNo(String cardNo) {
		this.cardNo = cardNo;
	}

	/**
	 * @return the isAlone
	 */
	public boolean isAlone() {
		return isAlone;
	}

	/**
	 * @param isAlone
	 *            the isAlone to set
	 */
	public void setAlone(boolean isAlone) {
		this.isAlone = isAlone;
	}

	@Override
	public String toString() {

		return "BankBill{" +super.toString()+
				"cardNo='" + cardNo + '\'' +
				", isAlone=" + isAlone +
				'}';
	}
}
