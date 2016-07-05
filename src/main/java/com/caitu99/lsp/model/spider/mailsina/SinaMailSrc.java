package com.caitu99.lsp.model.spider.mailsina;

/**
 * Created by Lion on 2015/11/12 0012.
 */
public class SinaMailSrc {
    private String result;
    private String errno;
    private String msg;
    private SinaMailContent data;

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public String getErrno() {
        return errno;
    }

    public void setErrno(String errno) {
        this.errno = errno;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public SinaMailContent getData() {
        return data;
    }

    public void setData(SinaMailContent data) {
        this.data = data;
    }
}
