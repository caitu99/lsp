/*
 * Copyright (c) 2015-2020 by caitu99
 * All rights reserved.
 */
package com.caitu99.lsp.model.spider.tianyi;

import com.alibaba.fastjson.annotation.JSONField;
import com.caitu99.lsp.model.spider.QueryEvent;
import org.springframework.web.context.request.async.DeferredResult;

/**
 * 天翼积分商城
 * 
 * @Description: (类职责详细描述,可空) 
 * @ClassName: TianYiShopSpiderEvent 
 * @author ws
 * @date 2016年2月4日 上午11:53:10 
 * @Copyright (c) 2015-2020 by caitu99
 */
public class TianYiShopSpiderEvent extends QueryEvent {

    private String msgCode;//短信验证码 
    private String custID;//用户id
    //private String deviceNo;//手机号
    private String deviceType;//固定为7
    private String provinceID;//账户对应的省份id
    private String SystemType;//固定为2
    private String buyNum;//购买数量
    private String commodityID;//商品编号
    private String payTotal;//支付总积分数
    private String payVoucher;//积分支付数   OrderVoucher
    private String payIntegral;//翼积分支付数  OrderIntegral
    private String money;//现金支付数   OrderMoney
    private String payFlag;//支付方式   x:现金，j积分，y翼积分
    private String orderID;//订单号
    private String rndCode;//支付短信验证码
    
    private String csolCode;//兑换卷

    private TianYiShopSpiderState state = TianYiShopSpiderState.NONE;

    public TianYiShopSpiderEvent() {

    }

    public TianYiShopSpiderEvent(String userId, DeferredResult<Object> deferredResult) {
        super(userId, deferredResult);
    }


    @JSONField(deserialize = false)
    public void setException(Exception exception) {
        this.setState(TianYiShopSpiderState.ERROR);
        this.exception = exception;
    }

    /**
     * @return 事件状态
     */
    public TianYiShopSpiderState getState() {
        return state;
    }


    public String getCsolCode() {
		return csolCode;
	}

	public void setCsolCode(String csolCode) {
		this.csolCode = csolCode;
	}

	/**
     * @param 事件状态 to set
     */
    public void setState(TianYiShopSpiderState state) {
        this.state = state;
    }

    /**
     * @return the msgCode
     */
    public String getMsgCode() {
        return msgCode;
    }

    /**
     * @param msgCode the msgCode to set
     */
    public void setMsgCode(String msgCode) {
        this.msgCode = msgCode;
    }

	/**
	 * @return the custID
	 */
	public String getCustID() {
		return custID;
	}

	/**
	 * @param custID the custID to set
	 */
	public void setCustID(String custID) {
		this.custID = custID;
	}

	/**
	 * @return the deviceType
	 */
	public String getDeviceType() {
		return deviceType;
	}

	/**
	 * @param deviceType the deviceType to set
	 */
	public void setDeviceType(String deviceType) {
		this.deviceType = deviceType;
	}

	/**
	 * @return the provinceID
	 */
	public String getProvinceID() {
		return provinceID;
	}

	/**
	 * @param provinceID the provinceID to set
	 */
	public void setProvinceID(String provinceID) {
		this.provinceID = provinceID;
	}

	/**
	 * @return the systemType
	 */
	public String getSystemType() {
		return SystemType;
	}

	/**
	 * @param systemType the systemType to set
	 */
	public void setSystemType(String systemType) {
		SystemType = systemType;
	}

	/**
	 * @return the buyNum
	 */
	public String getBuyNum() {
		return buyNum;
	}

	/**
	 * @param buyNum the buyNum to set
	 */
	public void setBuyNum(String buyNum) {
		this.buyNum = buyNum;
	}

	/**
	 * @return the commodityID
	 */
	public String getCommodityID() {
		return commodityID;
	}

	/**
	 * @param commodityID the commodityID to set
	 */
	public void setCommodityID(String commodityID) {
		this.commodityID = commodityID;
	}

	/**
	 * @return the payVoucher
	 */
	public String getPayVoucher() {
		return payVoucher;
	}

	/**
	 * @param payVoucher the payVoucher to set
	 */
	public void setPayVoucher(String payVoucher) {
		this.payVoucher = payVoucher;
	}

	/**
	 * @return the payIntegral
	 */
	public String getPayIntegral() {
		return payIntegral;
	}

	/**
	 * @param payIntegral the payIntegral to set
	 */
	public void setPayIntegral(String payIntegral) {
		this.payIntegral = payIntegral;
	}

	/**
	 * @return the money
	 */
	public String getMoney() {
		return money;
	}

	/**
	 * @param money the money to set
	 */
	public void setMoney(String money) {
		this.money = money;
	}

	/**
	 * @return the payFlag
	 */
	public String getPayFlag() {
		return payFlag;
	}

	/**
	 * @param payFlag the payFlag to set
	 */
	public void setPayFlag(String payFlag) {
		this.payFlag = payFlag;
	}

	/**
	 * @return the orderID
	 */
	public String getOrderID() {
		return orderID;
	}

	/**
	 * @param orderID the orderID to set
	 */
	public void setOrderID(String orderID) {
		this.orderID = orderID;
	}

	/**
	 * @return the rndCode
	 */
	public String getRndCode() {
		return rndCode;
	}

	/**
	 * @param rndCode the rndCode to set
	 */
	public void setRndCode(String rndCode) {
		this.rndCode = rndCode;
	}

	/**
	 * @return the payTotal
	 */
	public String getPayTotal() {
		return payTotal;
	}

	/**
	 * @param payTotal the payTotal to set
	 */
	public void setPayTotal(String payTotal) {
		this.payTotal = payTotal;
	}

}
