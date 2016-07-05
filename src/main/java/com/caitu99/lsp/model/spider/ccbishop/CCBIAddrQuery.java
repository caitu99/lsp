package com.caitu99.lsp.model.spider.ccbishop;


import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;


@XStreamAlias("queryPackup")
public class CCBIAddrQuery {

    @XStreamAsAttribute
    private String querytype;
    @XStreamAsAttribute
    private String addrZip;
    @XStreamAsAttribute
    private String addrMail;
    @XStreamAsAttribute
    private String addrName;
    @XStreamAsAttribute
    private String addrPhone;
    @XStreamAsAttribute
    private String addrMobi;
    @XStreamAsAttribute
    private String add_id_type;
    @XStreamAsAttribute
    private String add_id_nbr;

    public CCBIAddrQuery() {
        querytype = addrZip = addrMail = addrName = addrPhone = addrMobi =
        add_id_type = add_id_nbr = "";
    }

    public String getQuerytype() {
        return querytype;
    }

    public void setQuerytype(String querytype) {
        this.querytype = querytype;
    }

    public String getAddrZip() {
        return addrZip;
    }

    public void setAddrZip(String addrZip) {
        this.addrZip = addrZip;
    }

    public String getAddrMail() {
        return addrMail;
    }

    public void setAddrMail(String addrMail) {
        this.addrMail = addrMail;
    }

    public String getAddrName() {
        return addrName;
    }

    public void setAddrName(String addrName) {
        this.addrName = addrName;
    }

    public String getAddrPhone() {
        return addrPhone;
    }

    public void setAddrPhone(String addrPhone) {
        this.addrPhone = addrPhone;
    }

    public String getAddrMobi() {
        return addrMobi;
    }

    public void setAddrMobi(String addrMobi) {
        this.addrMobi = addrMobi;
    }

    public String getAdd_id_type() {
        return add_id_type;
    }

    public void setAdd_id_type(String add_id_type) {
        this.add_id_type = add_id_type;
    }

    public String getAdd_id_nbr() {
        return add_id_nbr;
    }

    public void setAdd_id_nbr(String add_id_nbr) {
        this.add_id_nbr = add_id_nbr;
    }
}
