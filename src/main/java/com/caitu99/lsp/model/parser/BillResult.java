/*
 * Copyright (c) 2015-2020 by caitu99
 * All rights reserved.
 */
package com.caitu99.lsp.model.parser;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 
 * @Description: (类职责详细描述,可空)
 * @ClassName: BillResult
 * @author lhj
 * @date 2015年12月24日 下午2:49:25
 * @Copyright (c) 2015-2020 by caitu99
 */
public class BillResult {

	// 账户，如：李先生
	private String name;
	// 账单月，某某月的账单
	private Date billMonth;
	// 积分
	private Long integral;
	// 其它非必要属性
	private Map<String, Object> others = new HashMap<>();
	// 用户id
	private int userId;
	// 是否独立积分
	private boolean isAlone;
	// 卡号
	private String cardNo;
	// 卡片名称
	private String cardName;
	// 卡片类型id
	private Integer cardTypeId;

	/**
	 * @return the cardTypeId
	 */
	public Integer getCardTypeId() {
		return cardTypeId;
	}

	/**
	 * @param cardTypeId
	 *            the cardTypeId to set
	 */
	public void setCardTypeId(Integer cardTypeId) {
		this.cardTypeId = cardTypeId;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name
	 *            the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the billMonth
	 */
	public Date getBillMonth() {
		return billMonth;
	}

	/**
	 * @param billMonth
	 *            the billMonth to set
	 */
	public void setBillMonth(Date billMonth) {
		this.billMonth = billMonth;
	}

	/**
	 * @return the integral
	 */
	public Long getIntegral() {
		return integral;
	}

	/**
	 * @param integral
	 *            the integral to set
	 */
	public void setIntegral(Long integral) {
		this.integral = integral;
	}

	/**
	 * @return the others
	 */
	public Map<String, Object> getOthers() {
		return others;
	}

	/**
	 * @param others
	 *            the others to set
	 */
	public void setOthers(Map<String, Object> others) {
		this.others = others;
	}

	/**
	 * @return the userId
	 */
	public int getUserId() {
		return userId;
	}

	/**
	 * @param userId
	 *            the userId to set
	 */
	public void setUserId(int userId) {
		this.userId = userId;
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
	 * @return the cardName
	 */
	public String getCardName() {
		return cardName;
	}

	/**
	 * @param cardName
	 *            the cardName to set
	 */
	public void setCardName(String cardName) {
		this.cardName = cardName;
	}

}
