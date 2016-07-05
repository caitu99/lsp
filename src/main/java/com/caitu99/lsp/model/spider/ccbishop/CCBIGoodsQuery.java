package com.caitu99.lsp.model.spider.ccbishop;


import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;


@XStreamAlias("goods")
public class CCBIGoodsQuery {

    @XStreamAsAttribute
    private String goods_id;
    @XStreamAsAttribute
    private String goods_payway_id;
    @XStreamAsAttribute
    private String goods_price;
    @XStreamAsAttribute
    private String goods_point;
    @XStreamAsAttribute
    private String stages_nm;
    @XStreamAsAttribute
    private String goods_size;
    @XStreamAsAttribute
    private String goods_nm;
    @XStreamAsAttribute
    private String goods_color;
    @XStreamAsAttribute
    private String goods_model;
    @XStreamAsAttribute
    private String vendor_id;
    @XStreamAsAttribute
    private String vendor_nm;
    @XStreamAsAttribute
    private String type_id;
    @XStreamAsAttribute
    private String goods_brand;
    @XStreamAsAttribute
    private String cardno_benefit;
    @XStreamAsAttribute
    private String good_rnd;

    public CCBIGoodsQuery() {
        goods_id = goods_payway_id = goods_price = goods_point = stages_nm
                = goods_size = goods_nm = goods_color = goods_model = vendor_id
                = vendor_nm = type_id = goods_brand = cardno_benefit = good_rnd = "";
    }

    public String getGood_rnd() {
        return good_rnd;
    }

    public void setGood_rnd(String good_rnd) {
        this.good_rnd = good_rnd;
    }

    public String getCardno_benefit() {
        return cardno_benefit;
    }

    public void setCardno_benefit(String cardno_benefit) {
        this.cardno_benefit = cardno_benefit;
    }

    public String getGoods_brand() {
        return goods_brand;
    }

    public void setGoods_brand(String goods_brand) {
        this.goods_brand = goods_brand;
    }

    public String getType_id() {
        return type_id;
    }

    public void setType_id(String type_id) {
        this.type_id = type_id;
    }

    public String getVendor_nm() {
        return vendor_nm;
    }

    public void setVendor_nm(String vendor_nm) {
        this.vendor_nm = vendor_nm;
    }

    public String getVendor_id() {
        return vendor_id;
    }

    public void setVendor_id(String vendor_id) {
        this.vendor_id = vendor_id;
    }

    public String getGoods_model() {
        return goods_model;
    }

    public void setGoods_model(String goods_model) {
        this.goods_model = goods_model;
    }

    public String getGoods_color() {
        return goods_color;
    }

    public void setGoods_color(String goods_color) {
        this.goods_color = goods_color;
    }

    public String getGoods_nm() {
        return goods_nm;
    }

    public void setGoods_nm(String goods_nm) {
        this.goods_nm = goods_nm;
    }

    public String getGoods_size() {
        return goods_size;
    }

    public void setGoods_size(String goods_size) {
        this.goods_size = goods_size;
    }

    public String getStages_nm() {
        return stages_nm;
    }

    public void setStages_nm(String stages_nm) {
        this.stages_nm = stages_nm;
    }

    public String getGoods_point() {
        return goods_point;
    }

    public void setGoods_point(String goods_point) {
        this.goods_point = goods_point;
    }

    public String getGoods_price() {
        return goods_price;
    }

    public void setGoods_price(String goods_price) {
        this.goods_price = goods_price;
    }

    public String getGoods_id() {
        return goods_id;
    }

    public void setGoods_id(String goods_id) {
        this.goods_id = goods_id;
    }

    public String getGoods_payway_id() {
        return goods_payway_id;
    }

    public void setGoods_payway_id(String goods_payway_id) {
        this.goods_payway_id = goods_payway_id;
    }
}
