package com.caitu99.lsp.mongodb.parser;

import com.caitu99.lsp.mongodb.dao.MongoDao;
import com.caitu99.lsp.mongodb.model.MGLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.Collection;


@Service
public class MGLogService {

    @Autowired
    MongoDao mgDao;

    /**
     * 插入log
     * @param mgLog mgLog
     */
    public void insert(MGLog mgLog) {
        mgDao.add(mgLog);
    }

    /**
     * 批量插入
     * @param mgLogs mgLogs
     */
    public void batchInsert(Collection<MGLog> mgLogs) {
        mgDao.addCollection(MGLog.class, mgLogs);
    }

    /**
     * 查询
     * @param mailId
     * @return
     */
    public MGLog getMGLogByMailSrcId(String mailId) {
        Query query = new Query();
        if (mailId != null) {
            Criteria criteria = Criteria.where("srcid").is(mailId);
            query.addCriteria(criteria);
            return mgDao.findOne(MGLog.class, query);
        }
        else {
            return null;
        }
    }

}
