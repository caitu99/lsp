package com.caitu99.lsp.model.spider.citybank;

import com.caitu99.lsp.model.spider.QueryEvent;
import org.springframework.web.context.request.async.DeferredResult;

public class CityBankEvent extends QueryEvent {

    private String extra;
    private String jfpToken;
    private String syncToken;
    private String location;
    private CityBankState state;
    private CityBankResult cityBankResult = new CityBankResult();

    public CityBankEvent() {

    }

    public CityBankEvent(String userid, DeferredResult<Object> deferredResult) {
        super(userid, deferredResult);
    }

    public CityBankState getState() {
        return state;
    }

    public void setState(CityBankState state) {
        this.state = state;
    }

    public void setException(Exception exception) {
        this.setState(CityBankState.ERROR);
        this.exception = exception;
    }

    public String getExtra() {
        return extra;
    }

    public void setExtra(String extra) {
        this.extra = extra;
    }

    public String getJfpToken() {
        return jfpToken;
    }

    public void setJfpToken(String jfpToken) {
        this.jfpToken = jfpToken;
    }

    public String getSyncToken() {
        return syncToken;
    }

    public void setSyncToken(String syncToken) {
        this.syncToken = syncToken;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public CityBankResult getCityBankResult() {
        return cityBankResult;
    }

    public void setCityBankResult(CityBankResult cityBankResult) {
        this.cityBankResult = cityBankResult;
    }


}
