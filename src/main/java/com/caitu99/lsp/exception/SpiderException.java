package com.caitu99.lsp.exception;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.annotation.JSONField;
import com.alibaba.fastjson.serializer.SimplePropertyPreFilter;

public class SpiderException extends RuntimeException {
    private static final SimplePropertyPreFilter filter = new SimplePropertyPreFilter("code", "message", "data");
    private int code;
    private String description;
    private String data;

    public SpiderException() {

    }

    public SpiderException(int code, String description) {
        super(description);
        this.code = code;
        this.description = description;
    }

    public SpiderException(int code, String description, String data) {
        super(description);
        this.code = code;
        this.description = description;
        this.data = data;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    @JSONField(name = "message")
    public String getDescription() {
        return description;
    }

    @JSONField(name = "message")
    public void setDescription(String description) {
        this.description = description;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    @JSONField(serialize = false)
    public String getMessage() {
        return this.description;
    }

    @Override
    public String toString() {

        return JSON.toJSONString(this, filter);
    }
}
