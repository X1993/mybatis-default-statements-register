package com.github.ibatis.statement.mapper;

import com.github.ibatis.statement.mapper.param.ConditionParams;
import com.github.ibatis.statement.mapper.param.DynamicParams;
import org.apache.ibatis.exceptions.TooManyResultsException;
import java.util.List;

/**
 * 动态参数查询接口
 * @Author: junjie
 * @Date: 2020/8/31
 */
public interface DynamicSelectMapper<T> extends EntityType<T> {

    /**
     * 通过自定义规则查询符合条件的数据
     * @param dynamicParams
     * @return
     */
    List<T> selectByDynamicParams(DynamicParams dynamicParams);

    /**
     * 通过自定义规则查询符合条件的数据
     * @param dynamicParams
     * @throws org.apache.ibatis.exceptions.TooManyResultsException 如果存在多个满足条件的结果
     * @return
     */
    default T selectUniqueByDynamicParams(DynamicParams dynamicParams)
    {
        List<T> results = selectByDynamicParams(dynamicParams);
        if (results.size() > 1){
            throw new TooManyResultsException("result count :" + results.size());
        }
        return results.size() > 0 ? results.get(0) : null;
    }

    /**
     * 通过自定义规则查询符合条件的数据
     * @param conditionParams
     * @return
     */
    default List<T> selectByWhereConditions(ConditionParams conditionParams){
        return selectByDynamicParams(conditionParams == null ? null : conditionParams.dynamicParams());
    }

    /**
     * 通过自定义规则查询符合条件的数据
     * @param conditionParams
     * @return
     * @throws org.apache.ibatis.exceptions.TooManyResultsException 如果存在多个满足条件的结果
     */
    default T selectUniqueByWhereConditions(ConditionParams conditionParams){
        return selectUniqueByDynamicParams(conditionParams == null ? null : conditionParams.dynamicParams());
    }

    /**
     * 通过自定义规则查询符合条件的数据条数
     * @param dynamicParams
     * @return
     */
    int countByDynamicParams(DynamicParams dynamicParams);

    /**
     * 通过自定义规则查询符合条件的数据条数
     * @param conditionParams
     * @return
     */
    default int countWhereConditions(ConditionParams conditionParams){
        return countByDynamicParams(conditionParams == null ? null : conditionParams.dynamicParams());
    }

}
