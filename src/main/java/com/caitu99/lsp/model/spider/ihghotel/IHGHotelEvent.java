package com.caitu99.lsp.model.spider.ihghotel;

import com.caitu99.lsp.model.spider.QueryEvent;
import org.springframework.web.context.request.async.DeferredResult;

public class IHGHotelEvent extends QueryEvent {

    private String extra;
    private String jfpToken;
    private String syncToken;
    private String location;
    private IHGHotelState state;
    private IHGHotelResult ihgHotelResult = new IHGHotelResult();

    public IHGHotelEvent() {

    }

    public IHGHotelEvent(String userid, DeferredResult<Object> deferredResult) {
        super(userid, deferredResult);
    }

    public IHGHotelState getState() {
        return state;
    }

    public void setState(IHGHotelState state) {
        this.state = state;
    }

    public void setException(Exception exception) {
        this.setState(IHGHotelState.ERROR);
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

    public IHGHotelResult getIhgHotelResult() {
        return ihgHotelResult;
    }

    public void setIhgHotelResult(IHGHotelResult ihgHotelResult) {
        this.ihgHotelResult = ihgHotelResult;
    }
}
