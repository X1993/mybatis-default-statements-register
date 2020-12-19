package com.github.ibatis.statement.mapper;

import java.util.Collection;
import com.github.ibatis.statement.register.factory.*;

/**
 * 表操作mapper
 * @Author: junjie
 * @Date: 2020/3/18
 */
public interface TableMapper<T> extends SelectMapper<T> {

    /**
     * 新增
     * @param t
     * @return
     * @see InsertMappedStatementFactory#INSERT_METHOD_NAME
     */
    int insert(T t);

    /**
     * 条件插入不为空的数据
     * @param t
     * @return
     * @see InsertMappedStatementFactory#INSERT_SELECTIVE_METHOD_NAME
     */
    int insertSelective(T t);

    /**
     * 批量新增
     * @param list
     * @return
     * @see InsertBatchMappedStatementFactory#INSERT_BATCH_METHOD_NAME
     */
    int insertBatch(Collection<T> list);

    /**
     * 批量删除（根据有无逻辑列执行逻辑删除或物理删除）
     * @param condition 利用非空属性作为查询条件，将符合条件的记录删除
     * 条件：1.非空属性，2.有对应的列
     * 注意：基本类型是一定会满足1.非空 条件的
     * @return
     * @see DeleteByPrimaryKeyMappedStatementFactory#DELETE_BY_PRIMARY_KEY
     */
    int deleteSelective(T condition);

    /**
     * 批量物理删除
     * @param condition 利用非空属性作为查询条件，将符合条件的记录删除
     * 条件：1.非空属性，2.有对应的列
     * 注意：基本类型是一定会满足1.非空 条件的
     * @return
     * @see DeleteByPrimaryKeyMappedStatementFactory#DELETE_BY_PRIMARY_KEY_ON_PHYSICAL
     */
    int deleteSelectiveOnPhysical(T condition);

}
