/*
 * Copyright (c) 2015-2020 by caitu99
 * All rights reserved.
 */
package com.caitu99.lsp.mongodb.dao;

import java.util.List;

/**
 * @author ws
 * @Description: (类职责详细描述, 可空)
 * @ClassName: BaseDao
 * @date 2015年11月26日 上午11:34:40
 * @Copyright (c) 2015-2020 by caitu99
 */
public interface IBaseDao {

    <T> T findById(Class<T> entityClass, String id);

    <T> List<T> findAll(Class<T> entityClass);

    void remove(Object obj);

    void add(Object obj);

    void saveOrUpdate(Object obj);
}
