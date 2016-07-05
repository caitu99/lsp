package com.caitu99.lsp.model.spider.taobao;

public class LoginResult {
    private int errcode;
    private boolean needauth;
    private String succcb;

    public int getErrcode() {
        return errcode;
    }

    public void setErrcode(int errcode) {
        this.errcode = errcode;
    }

    public boolean isNeedauth() {
        return needauth;
    }

    public void setNeedauth(boolean needauth) {
        this.needauth = needauth;
    }

    public String getSucccb() {
        return succcb;
    }

    public void setSucccb(String succcb) {
        this.succcb = succcb;
    }

}
