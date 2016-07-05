package com.caitu99.lsp.model.spider.unicomshop;

/**
 * Created by Administrator on 2016/1/12.
 */
public class UnicomShopResult {
    private String username;
    private String cardno;
    private Integer jifen;
    private String account;

    public String getCardno() {
        return cardno;
    }

    public void setCardno(String cardno) {
        this.cardno = cardno;
    }

    public Integer getJifen() {
        return jifen;
    }

    public void setJifen(Integer jifen) {
        this.jifen = jifen;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
