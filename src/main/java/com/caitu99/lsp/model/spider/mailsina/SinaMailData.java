package com.caitu99.lsp.model.spider.mailsina;

import java.util.ArrayList;

/**
 * Created by Lion on 2015/11/11 0011.
 */
public class SinaMailData {
    private String fname;
    private String fid;
    private String pagenum;
    private String currentpage;
    private String pagesize;
    private ArrayList<String[]> mailList;
    private SinaMailTotal total;
    private String sortby;
    private String sort_ascdesc;
    private String timestamp;

    public SinaMailTotal getTotal() {
        return total;
    }

    public void setTotal(SinaMailTotal total) {
        this.total = total;
    }

    public String getSortby() {
        return sortby;
    }

    public void setSortby(String sortby) {
        this.sortby = sortby;
    }

    public String getSort_ascdesc() {
        return sort_ascdesc;
    }

    public void setSort_ascdesc(String sort_ascdesc) {
        this.sort_ascdesc = sort_ascdesc;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getFname() {
        return fname;
    }

    public void setFname(String fname) {
        this.fname = fname;
    }

    public String getFid() {
        return fid;
    }

    public void setFid(String fid) {
        this.fid = fid;
    }

    public String getPagenum() {
        return pagenum;
    }

    public void setPagenum(String pagenum) {
        this.pagenum = pagenum;
    }

    public String getCurrentpage() {
        return currentpage;
    }

    public void setCurrentpage(String currentpage) {
        this.currentpage = currentpage;
    }

    public String getPagesize() {
        return pagesize;
    }

    public void setPagesize(String pagesize) {
        this.pagesize = pagesize;
    }

    public ArrayList<String[]> getMailList() {
        return mailList;
    }

    public void setMailList(ArrayList<String[]> mailList) {
        this.mailList = mailList;
    }
}
