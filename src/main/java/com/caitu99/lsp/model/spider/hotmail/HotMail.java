package com.caitu99.lsp.model.spider.hotmail;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Lion on 2015/11/19 0019.
 */
public class HotMail {
    private Integer mailTotal = 0;      //总邮件数
    private Integer allPages = 0;        //共有多少页
    private Integer curPage = 0;         //当前页
    private Integer curTotal = 0;       //当前已经有多少邮件列表
    private List<HotmailItem> mailList = new ArrayList<>();


    public Integer getMailTotal() {
        return mailTotal;
    }

    public void setMailTotal(Integer mailTotal) {
        this.mailTotal = mailTotal;
    }

    public Integer getAllPages() {
        return allPages;
    }

    public void setAllPages(Integer allPages) {
        this.allPages = allPages;
    }

    public Integer getCurPage() {
        return curPage;
    }

    public void setCurPage(Integer curPage) {
        this.curPage = curPage;
    }

    public List<HotmailItem> getMailList() {
        return mailList;
    }

    public void setMailList(List<HotmailItem> mailList) {
        this.mailList = mailList;
    }

    public Integer getCurTotal() {
        return curTotal;
    }

    public void setCurTotal(Integer curTotal) {
        this.curTotal = curTotal;
    }
}
