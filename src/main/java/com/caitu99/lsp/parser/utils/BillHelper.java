/*
 * Copyright (c) 2015-2020 by caitu99
 * All rights reserved.
 */
package com.caitu99.lsp.parser.utils;

import java.util.Date;

import com.caitu99.lsp.model.parser.bank.BankBill;
import com.caitu99.lsp.model.spider.MailSrc;
import com.caitu99.lsp.mongodb.model.MGBill;
import com.caitu99.lsp.parser.ICard;

/**
 * 
 * @Description: (类职责详细描述,可空)
 * @ClassName: BillHelper
 * @author lhj
 * @date 2015年12月23日 下午4:29:10
 * @Copyright (c) 2015-2020 by caitu99
 */
public class BillHelper {

	/**
	 * 
	 * 
	 * @Description: (创建银行账单)
	 * @Title: createBankBill
	 * @param bankBill
	 * @param card
	 * @param bank
	 * @return
	 * @date 2015年12月23日 下午4:40:25
	 * @author lhj
	 */
	public static MGBill createBankBill(BankBill bankBill, ICard card,
			String bank) {
		MGBill newMGBill = new MGBill();
		MailSrc mailSrc = bankBill.getTpl().getMailSrc();
		newMGBill.setAccount(bankBill.getTpl().getContext().getAccount());
		newMGBill.setBank(bank);
		newMGBill.setBody(mailSrc.getBody());
		newMGBill.setCard(card.getName());
		newMGBill.setCardNo(bankBill.getCardNo());
		newMGBill.setConfig(bankBill.getTpl().getConfigure());
		newMGBill.setContextId(bankBill.getTpl().getContext().getId());
		newMGBill.setCreated(new Date());
		newMGBill.setDate(bankBill.getBillDay());
		newMGBill.setIntegral(bankBill.getIntegral());
		newMGBill.setName(bankBill.getName());
		newMGBill.setOthers(bankBill.getOthers());
		newMGBill.setsDate(mailSrc.getDate());
		newMGBill.setSrcId(mailSrc.getId());
		newMGBill.setStatus(1);// 成功
		newMGBill.setTitle(mailSrc.getTitle());
		newMGBill.setTpl(bankBill.getTpl().getName());
		newMGBill.setUserId(bankBill.getTpl().getContext().getUserId());

		return newMGBill;
	}

}
