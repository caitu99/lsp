package com.caitu99.lsp.mongodb.parser;

import com.caitu99.lsp.mongodb.dao.MongoDao;
import com.caitu99.lsp.mongodb.model.MGBill;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class MGBillService {

    @Autowired
    MongoDao mgDao;

    /**
     * 插入账单数据
     * @param mgBill bill
     */
    public void insert(MGBill mgBill) {
        mgDao.add(mgBill);
    }

    /**
     * 更新账单数据
     * @param mgBill bill
     */
    public void update(MGBill mgBill) {
        mgDao.saveOrUpdate(mgBill);
    }

    /**
     * 删除账单数据
     * @param mgBill bill
     */
    public void delete(MGBill mgBill) {
        mgDao.remove(mgBill);
    }

    /**
     * 根据id获取账单
     * @param id id
     * @return bill
     */
    public MGBill get(String id) {
        return mgDao.findById(MGBill.class, id);
    }

    /**
     * 筛选账单
     * @param criteriaBill bill
     * @return bill list
     */
    public List<MGBill> get(MGBill criteriaBill) {
        Query query = getQuery(criteriaBill);
        query.fields().exclude("body");
        return mgDao.findByCriteria(MGBill.class, query);
    }

    /**
     * 筛选账单
     * @param criteriaBill bill
     * @return bill list
     */
    public List<MGBill> getWithBody(MGBill criteriaBill) {
        Query query = getQuery(criteriaBill);
        return mgDao.findByCriteria(MGBill.class, query);
    }

    /**
     * 获取最新的账单
     * @param criteriaBill bill
     * @return bill
     */
    public MGBill getLast(MGBill criteriaBill) {
        Query query = getQuery(criteriaBill);
        query.limit(1);
        query.with(new Sort(Sort.Direction.DESC, "date"));
        query.fields().exclude("body");
        return mgDao.findOne(MGBill.class, query);
    }

    /**
     * 获取为解析的账单
     * @param criteriaBill
     * @return
     */
    public MGBill getUnparsed(MGBill criteriaBill) {
        Query query = getQuery(criteriaBill);
        query.limit(1);
        query.fields().include("_id");
        return mgDao.findOne(MGBill.class, query);
    }

    /**
     * 创建查询语句
     * @param mgBill bill
     * @return query
     */
    private Query getQuery(MGBill mgBill) {
        if (mgBill == null) {
            mgBill = new MGBill();
        }
        Query query = new Query();
        if (mgBill.getUserId() != null) {
            Criteria criteria = Criteria.where("userid").is(mgBill.getUserId());
            query.addCriteria(criteria);
        }
        if (StringUtils.isNotBlank(mgBill.getAccount())) {
            Criteria criteria = Criteria.where("account").is(mgBill.getAccount());
            query.addCriteria(criteria);
        }
        if (StringUtils.isNotBlank(mgBill.getTitle())) {
            Criteria criteria = Criteria.where("title").is(mgBill.getTitle());
            query.addCriteria(criteria);
        }
        if (StringUtils.isNotBlank(mgBill.getBank())) {
            Criteria criteria = Criteria.where("bank").is(mgBill.getBank());
            query.addCriteria(criteria);
        }
        if (StringUtils.isNotBlank(mgBill.getCard())) {
            Criteria criteria = Criteria.where("card").is(mgBill.getCard());
            query.addCriteria(criteria);
        }
        if (StringUtils.isNotBlank(mgBill.getTpl())) {
            Criteria criteria = Criteria.where("tpl").is(mgBill.getTpl());
            query.addCriteria(criteria);
        }
        if (StringUtils.isNotBlank(mgBill.getName())) {
            Criteria criteria = Criteria.where("name").is(mgBill.getName());
            query.addCriteria(criteria);
        }
        if (StringUtils.isNotBlank(mgBill.getCardNo())) {
            Criteria criteria = Criteria.where("cardno").is(mgBill.getCardNo());
            query.addCriteria(criteria);
        }
        if (mgBill.getStatus() != null) {
            Criteria criteria = Criteria.where("status").is(mgBill.getStatus());
            query.addCriteria(criteria);
        }

        return query;
    }

}
