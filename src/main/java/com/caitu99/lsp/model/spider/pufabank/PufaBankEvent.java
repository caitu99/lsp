package com.caitu99.lsp.model.spider.pufabank;

import java.util.Map;

import com.caitu99.lsp.model.spider.QueryEvent;

import org.springframework.web.context.request.async.DeferredResult;

public class PufaBankEvent extends QueryEvent {

    private String msmCode;
    private Map<String,String> loginParam;
    private Map<String,String> verifyParam;

    private PufaBankState state;

    public PufaBankEvent() {
        super();
    }

    public PufaBankEvent(String userid, DeferredResult<Object> deferredResult) {
        super(userid, deferredResult);
    }

    public PufaBankState getState() {
        return state;
    }

    public void setState(PufaBankState state) {
        this.state = state;
    }

    public void setException(Exception exception) {
        this.setState(PufaBankState.ERROR);
        this.exception = exception;
    }

	/**
	 * @return the msmCode
	 */
	public String getMsmCode() {
		return msmCode;
	}

	/**
	 * @param msmCode the msmCode to set
	 */
	public void setMsmCode(String msmCode) {
		this.msmCode = msmCode;
	}

	/**
	 * @return the loginParam
	 */
	public Map<String, String> getLoginParam() {
		return loginParam;
	}

	/**
	 * @param loginParam the loginParam to set
	 */
	public void setLoginParam(Map<String, String> loginParam) {
		this.loginParam = loginParam;
	}

	/**
	 * @return the verifyParam
	 */
	public Map<String,String> getVerifyParam() {
		return verifyParam;
	}

	/**
	 * @param verifyParam the verifyParam to set
	 */
	public void setVerifyParam(Map<String,String> verifyParam) {
		this.verifyParam = verifyParam;
	}

    
    
}
