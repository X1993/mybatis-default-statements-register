package com.github.ibatis.statement.mapper;

import java.util.Collection;

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
     */
    boolean existByPrimaryKey(K key);

    /**
     * 判断指定主键的数据是否物理存在
     * @param key
     * @return
     */
    boolean existByPrimaryKeyOnPhysical(K key);

    /**
     * 根据主键查询 (如果有逻辑列，只查询逻辑存在的)
     * @param key 主键
     * @return
     */
    T selectByPrimaryKey(K key);

    /**
     * 根据主键物理查询
     * @param key
     * @return
     */
    T selectByPrimaryKeyOnPhysical(K key);

    /**
     * 修改属性不为空的数据
     * @param t
     * @return
     */
    int updateByPrimaryKeySelective(T t);

    /**
     * 修改数据
     * @param t
     * @return
     */
    int updateByPrimaryKey(T t);

    /**
     * 批量修改，set值为每个元素不为空的值
     * mysql数据库url添加allowMultiQueries=true，否则会报错
     * @param list
     * @return
     * @deprecated 某些数据库（如H2）不支持，适用性比较差
     */
    @Deprecated
    int updateBatch(Collection<T> list);

    /**
     * 对多个主键匹配的数据使用相同的值做修改
     * @param list 主键列表
     * @param updateValue 以为非空属性作为修改的值
     * @return
     */
    int updateBatchSameValue(Collection<K> list ,T updateValue);

    /**
     * 根据主键删除（如果定义了逻辑列则为逻辑删除，否则物理删除）
     * @param key
     * @return
     */
    int deleteByPrimaryKey(K key);

    /**
     * 根据主键物理删除
     * @param key
     * @return
     */
    int deleteByPrimaryKeyOnPhysical(K key);

    /**
     * 根据主键批量删除（根据有无逻辑列执行逻辑删除或物理删除）
     * @param keys
     * @return
     */
    int deleteBatchByPrimaryKey(Collection<K> keys);

    /**
     * 根据主键批量删除 （物理删除）
     * @param keys
     * @return
     */
    int deleteBatchByPrimaryKeyOnPhysical(Collection<K> keys);

}
