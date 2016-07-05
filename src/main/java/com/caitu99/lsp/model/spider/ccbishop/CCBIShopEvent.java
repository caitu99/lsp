package com.caitu99.lsp.model.spider.ccbishop;

import com.caitu99.lsp.model.spider.QueryEvent;
import org.springframework.web.context.request.async.DeferredResult;

import java.util.ArrayList;
import java.util.List;

public class CCBIShopEvent extends QueryEvent {

    private CCBIShopState state;
    private String smsCode;
    private CCBIAddrQuery addrInfo;
    private CCBIGoodsQuery goodsQuery;
    private String addrId;
    private String orderNo;
    private Long price;
    private String orderVcode;
    private String local_param;
    private Long quantity;
    private String querytype;
    
    private String loginType;
    
    


	public String getLoginType() {
		return loginType;
	}

	public void setLoginType(String loginType) {
		this.loginType = loginType;
	}

	public String getQuerytype() {
		return querytype;
	}

	public void setQuerytype(String querytype) {
		this.querytype = querytype;
	}

	public void setQuantity(Long quantity) {
        this.quantity = quantity;
    }

    public Long getQuantity() {
        return quantity;
    }

    public void setGoodsQuery(CCBIGoodsQuery goodsQuery) {
        this.goodsQuery = goodsQuery;
    }

    public CCBIGoodsQuery getGoodsQuery() {
        return goodsQuery;
    }

    public void setLocal_param(String local_param) {
        this.local_param = local_param;
    }

    public String getLocal_param() {
        return local_param;
    }

    public void setOrderVcode(String orderVcode) {
        this.orderVcode = orderVcode;
    }

    public String getOrderVcode() {
        return orderVcode;
    }

    public void setPrice(Long price) {
        this.price = price;
    }

    public Long getPrice() {
        return price;
    }

    public void setOrderNo(String orderNo) {
        this.orderNo = orderNo;
    }

    public String getOrderNo() {
        return orderNo;
    }
    private List<CCBICard> cards = new ArrayList<>();

    public CCBIShopEvent() {
    }

    public CCBIShopEvent(String userid, DeferredResult<Object> deferredResult) {
        super(userid, deferredResult);
    }

    public String getSmsCode() {
        return smsCode;
    }

    public void setSmsCode(String smsCode) {
        this.smsCode = smsCode;
    }

    public CCBIShopState getState() {
        return state;
    }

    public void setState(CCBIShopState state) {
        this.state = state;
    }

    public CCBIAddrQuery getAddrInfo() {
        return addrInfo;
    }

    public void setAddrInfo(CCBIAddrQuery addrInfo) {
        this.addrInfo = addrInfo;
    }

    public String getAddrId() {
        return addrId;
    }

    public void setAddrId(String addrId) {
        this.addrId = addrId;
    }

    public List<CCBICard> getCards() {
        return cards;
    }

    public void setCards(List<CCBICard> cards) {
        this.cards = cards;
    }

    public void setException(Exception exception) {
        this.setState(CCBIShopState.ERROR);
        this.exception = exception;
    }

}
