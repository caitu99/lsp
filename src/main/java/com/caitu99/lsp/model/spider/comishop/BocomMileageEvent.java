package com.caitu99.lsp.model.spider.comishop;

import java.util.List;

import org.springframework.web.context.request.async.DeferredResult;

import com.alibaba.fastjson.JSONArray;
import com.caitu99.lsp.exception.SpiderException;
import com.caitu99.lsp.model.spider.QueryEvent;

public class BocomMileageEvent extends QueryEvent {

    private BocomMileageState state;
    private String lt;
    private JSONArray jsonArray;
    private String ticketUrl;
    private String ticket;
    private String loginUrl;
    private String jsecurityUrl;
    private String jauthUrl;
    
    private String memberId;//航空公司会员编号   输入
    private String flightCompanyCode;//航空公司编号   输入
    private String removeId;//解绑id   
    private String msgCode;//短信验证码    输入
    private String useBonus;//使用积分数   输入
    private String validMonth;//有效月   输入
    private String validYear;//有效年   输入
    private String flightCompanyName;//航空公司名称   输入
    private String ebsNm1;//拼音姓   输入
    private String ebsNm2;//拼音名   输入
    private String clubMemberId;//会员编号   页面爬取
    
    public BocomMileageEvent() {

    }

    public BocomMileageEvent(String userid, DeferredResult<Object> deferredResult) {
        super(userid, deferredResult);
    }

    public String getJauthUrl() {
        return jauthUrl;
    }

    public void setJauthUrl(String jauthUrl) {
        this.jauthUrl = jauthUrl;
    }

    public String getJsecurityUrl() {
        return jsecurityUrl;
    }

    public void setJsecurityUrl(String jsecurityUrl) {
        this.jsecurityUrl = jsecurityUrl;
    }

    public String getLoginUrl() {
        return loginUrl;
    }

    public void setLoginUrl(String loginUrl) {
        this.loginUrl = loginUrl;
    }

    public String getTicket() {
        return ticket;
    }

    public void setTicket(String ticket) {
        this.ticket = ticket;
    }

    public String getTicketUrl() {
        return ticketUrl;
    }

    public void setTicketUrl(String ticketUrl) {
        this.ticketUrl = ticketUrl;
    }

    public JSONArray getJsonArray() {
        return jsonArray;
    }

    public void setJsonArray(JSONArray jsonArray) {
        this.jsonArray = jsonArray;
    }

    public String getLt() {
        return lt;
    }

    public void setLt(String lt) {
        this.lt = lt;
    }

    public BocomMileageState getState() {
        return state;
    }

    public void setState(BocomMileageState state) {
        this.state = state;
    }

    /**
	 * @return the memberId
	 */
	public String getMemberId() {
		return memberId;
	}

	/**
	 * @param memberId the memberId to set
	 */
	public void setMemberId(String memberId) {
		this.memberId = memberId;
	}

	/**
	 * @return the flightCompanyCode
	 */
	public String getFlightCompanyCode() {
		return flightCompanyCode;
	}

	/**
	 * @param flightCompanyCode the flightCompanyCode to set
	 */
	public void setFlightCompanyCode(String flightCompanyCode) {
		this.flightCompanyCode = flightCompanyCode;
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
	 * @return the useBonus
	 */
	public String getUseBonus() {
		return useBonus;
	}

	/**
	 * @param useBonus the useBonus to set
	 */
	public void setUseBonus(String useBonus) {
		this.useBonus = useBonus;
	}

	/**
	 * @return the validMonth
	 */
	public String getValidMonth() {
		return validMonth;
	}

	/**
	 * @param validMonth the validMonth to set
	 */
	public void setValidMonth(String validMonth) {
		this.validMonth = validMonth;
	}

	/**
	 * @return the validYear
	 */
	public String getValidYear() {
		return validYear;
	}

	/**
	 * @param validYear the validYear to set
	 */
	public void setValidYear(String validYear) {
		this.validYear = validYear;
	}

	/**
	 * @return the flightCompanyName
	 */
	public String getFlightCompanyName() {
		return flightCompanyName;
	}

	/**
	 * @param flightCompanyName the flightCompanyName to set
	 */
	public void setFlightCompanyName(String flightCompanyName) {
		this.flightCompanyName = flightCompanyName;
	}

	/**
	 * @return the ebsNm1
	 */
	public String getEbsNm1() {
		return ebsNm1;
	}

	/**
	 * @param ebsNm1 the ebsNm1 to set
	 */
	public void setEbsNm1(String ebsNm1) {
		this.ebsNm1 = ebsNm1;
	}

	/**
	 * @return the ebsNm2
	 */
	public String getEbsNm2() {
		return ebsNm2;
	}

	/**
	 * @param ebsNm2 the ebsNm2 to set
	 */
	public void setEbsNm2(String ebsNm2) {
		this.ebsNm2 = ebsNm2;
	}

	/**
	 * @return the clubMemberId
	 */
	public String getClubMemberId() {
		return clubMemberId;
	}

	/**
	 * @param clubMemberId the clubMemberId to set
	 */
	public void setClubMemberId(String clubMemberId) {
		this.clubMemberId = clubMemberId;
	}

	public void setException(Exception exception) {
        this.setState(BocomMileageState.ERROR);
        
		if(exception instanceof SpiderException){
	        this.exception = exception;
		}else{
			this.exception = new SpiderException(-1,"交通积分商城系统维护中,请稍后再试");
		}
    }

	/**
	 * @return the removeId
	 */
	public String getRemoveId() {
		return removeId;
	}

	/**
	 * @param removeId the removeId to set
	 */
	public void setRemoveId(String removeId) {
		this.removeId = removeId;
	}

}
