package com.caitu99.lsp.model.spider.hotmail;

/**
 * Created by Lion on 2015/11/14 0014.
 */
public class MailHotmailVerifyResult {
    private int rcode;
    private String rnadstr;
    private String sig;
    private String errmsg;

    public String getErrmsg() {
        return errmsg;
    }

    public void setErrmsg(String errmsg) {
        this.errmsg = errmsg;
    }

    public int getRcode() {
        return rcode;
    }

    public void setRcode(int rcode) {
        this.rcode = rcode;
    }

    public String getRnadstr() {
        return rnadstr;
    }

    public void setRnadstr(String rnadstr) {
        this.rnadstr = rnadstr;
    }

    public String getSig() {
        return sig;
    }

    public void setSig(String sig) {
        this.sig = sig;
    }
}
