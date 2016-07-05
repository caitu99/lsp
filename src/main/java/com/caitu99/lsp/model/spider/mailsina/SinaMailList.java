package com.caitu99.lsp.model.spider.mailsina;

/**
 * Created by Lion on 2015/11/11 0011.
 */
public class SinaMailList {
    private String result;
    private String error;
    private String msg;
    private SinaMailData data;


    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public SinaMailData getData() {
        return data;
    }

    public void setData(SinaMailData data) {
        this.data = data;
    }


}
