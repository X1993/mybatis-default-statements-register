package com.github.ibatis.statement.mapper;

import org.apache.ibatis.exceptions.TooManyResultsException;
import java.util.List;
import com.github.ibatis.statement.register.factory.SelectSelectiveMappedStatementFactory;

/**
 * 支持表或视图查询
 * @Author: junjie
 * @Date: 2020/3/16
 */
public interface SelectMapper<T> extends EntityType<T> ,DynamicSelectMapper<T> {

    /**
     * 将对象属性值作为查询条件，查询总数
     * 条件：1.非空属性，2.有对应的列
     * 注意：基本类型是一定会满足1.非空 条件的
     * @param condition
     * @param logicalExist 是否只查询逻辑存在
     * @return
     * @see SelectSelectiveMappedStatementFactory#TOTAL_SELECTIVE
     */
    int totalSelective(T condition ,boolean logicalExist);

    default int totalSelective(T condition){
        return totalSelective(condition ,defaultLogicalValue());
    }

    /**
     * 查询总数
     * @param logicalExist 是否只查询逻辑存在
     * @return
     */
    default int total(boolean logicalExist){
        return totalSelective(null ,logicalExist);
    }

    default int total(){
        return total(defaultLogicalValue());
    }

    /**
     * 将对象属性值作为查询条件
     * 条件：1.非空属性，2.有对应的列
     * 注意：基本类型是一定会满足1.非空 条件的
     * @param condition
     * @param logicalExist 是否只查询逻辑存在
     * @return
     * @see SelectSelectiveMappedStatementFactory#SELECT_SELECTIVE
     */
    List<T> selectSelective(T condition ,boolean logicalExist);

    default List<T> selectSelective(T condition){
        return selectSelective(condition ,defaultLogicalValue());
    }

    default List<T> selectAll(){
        return selectSelective(null);
    }

    /**
     * 条件查询唯一匹配的数据
     * @throws org.apache.ibatis.exceptions.TooManyResultsException 如果存在多个满足条件的结果
     */
    default T selectUniqueSelective(T condition){
        List<T> results = selectSelective(condition, defaultLogicalValue());
        if (results.size() > 1){
            throw new TooManyResultsException("result count : " + results.size());
        }
        return results.size() == 0 ? null : results.get(0);
    }

}
