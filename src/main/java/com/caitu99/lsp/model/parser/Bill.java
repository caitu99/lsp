package com.caitu99.lsp.model.parser;

import com.caitu99.lsp.parser.ITpl;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class Bill {
	// 卡片类型id
	private Integer id;
	// 账户，如：李先生
	private String name;
	// 账单月，某某月的账单
	private Date billDay;
	// 积分
	private Long integral;
	// 其它非必要属性
	private Map<String, Object> others = new HashMap<>();
	// 模板
	private ITpl tpl;

	/**
	 * @return the id
	 */
	public Integer getId() {
		return id;
	}

	/**
	 * @param id
	 *            the id to set
	 */
	public void setId(Integer id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Date getBillDay() {
		return billDay;
	}

	public void setBillDay(Date billDay) {
		this.billDay = billDay;
	}

	public Long getIntegral() {
		return integral;
	}

	public void setIntegral(Long integral) {
		this.integral = integral;
	}

	public Map<String, Object> getOthers() {
		return others;
	}

	public void setOthers(Map<String, Object> others) {
		this.others = others;
	}

	public ITpl getTpl() {
		return tpl;
	}

	public void setTpl(ITpl tpl) {
		this.tpl = tpl;
	}

	@Override
	public String toString() {
		return "Bill{" +
				"id=" + id +
				", name='" + name + '\'' +
				", billDay=" + billDay +
				", integral=" + integral +
				", others=" + others +
				", tpl=" + tpl +
				'}';
	}
}
