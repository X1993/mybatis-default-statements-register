package com.github.ibatis.statement.mapper;

import java.util.Collection;
import com.github.ibatis.statement.register.factory.*;

/**
 * 支持带主键的表
 * @see K 主键类型或实体类型{@link KeyParameterType}
 * @see T 映射的实体类型
 * @author junjie
 * @date 2020/2/22
 */
public interface KeyTableMapper<K ,T> extends KeyParameterType<K>, TableMapper<T>{

    /**
     * 判断指定主键的数据是否存在(如果有逻辑列，只查询逻辑存在的)
     * @param key
     * @return
     * @see SelectByPrimaryKeyMappedStatementFactory#EXIST_BY_PRIMARY_KEY
     */
    boolean existByPrimaryKey(K key);

    /**
     * 判断指定主键的数据是否物理存在
     * @param key
     * @return
     * @see SelectByPrimaryKeyMappedStatementFactory#EXIST_BY_PRIMARY_KEY_ON_PHYSICAL
     */
    boolean existByPrimaryKeyOnPhysical(K key);

    /**
     * 根据主键查询 (如果有逻辑列，只查询逻辑存在的)
     * @param key 主键
     * @return
     * @see SelectByPrimaryKeyMappedStatementFactory#SELECT_BY_PRIMARY_KEY
     */
    T selectByPrimaryKey(K key);

    /**
     * 根据主键物理查询
     * @param key
     * @return
     * @see SelectByPrimaryKeyMappedStatementFactory#SELECT_BY_PRIMARY_KEY_ON_PHYSICAL
     */
    T selectByPrimaryKeyOnPhysical(K key);

    /**
     * 修改属性不为空的数据
     * @param t
     * @return
     * @see UpdateMappedStatementFactory#UPDATE_BY_PRIMARY_KEY_SELECTIVE
     */
    int updateByPrimaryKeySelective(T t);

    /**
     * 修改数据
     * @param t
     * @return
     * @see UpdateMappedStatementFactory#UPDATE_BY_PRIMARY_KEY
     */
    int updateByPrimaryKey(T t);

    /**
     * 批量修改，set值为每个元素不为空的值
     * mysql数据库url添加allowMultiQueries=true，否则会报错
     * @param list
     * @return
     * @see UpdateBatchMappedStatementFactory#UPDATE_BATCH
     * @deprecated 某些数据库（如H2）不支持，适用性比较差
     */
    @Deprecated
    int updateBatch(Collection<? extends T> list);

    /**
     * 对多个主键匹配的数据使用相同的值做修改
     * @param list 主键列表
     * @param updateValue 以为非空属性作为修改的值
     * @see UpdateSameBatchMappedStatementFactory#UPDATE_BATCH_SAME_VALUE
     * @return
     */
    int updateBatchSameValue(Collection<? extends K> list ,T updateValue);

    /**
     * 根据主键删除（如果定义了逻辑列则为逻辑删除，否则物理删除）
     * @param key
     * @return
     * @see DeleteByPrimaryKeyMappedStatementFactory#DELETE_BY_PRIMARY_KEY
     */
    int deleteByPrimaryKey(K key);

    /**
     * 根据主键批量删除（根据有无逻辑列执行逻辑删除或物理删除）
     * @param keys
     * @return
     * @see DeleteByPrimaryKeyMappedStatementFactory#DELETE_BY_PRIMARY_KEY
     */
    int deleteBatchByPrimaryKey(Collection<? extends K> keys);

    /**
     * 根据主键物理删除
     * @param key
     * @return
     * @see DeleteByPrimaryKeyMappedStatementFactory#DELETE_BY_PRIMARY_KEY_ON_PHYSICAL
     */
    int deleteByPrimaryKeyOnPhysical(K key);

    /**
     * 根据主键批量删除 （物理删除）
     * @param keys
     * @return
     * @see DeleteBatchByPrimaryKeyMappedStatementFactory#DELETE_BATCH_METHOD_NAME
     */
    int deleteBatchByPrimaryKeyOnPhysical(Collection<? extends K> keys);

    /**
     * 查询最大的主键
     * @return
     * @see SelectMaxKeyStatementFactory#SELECT_MAX_KEY
     */
    K selectMaxKey();

}
