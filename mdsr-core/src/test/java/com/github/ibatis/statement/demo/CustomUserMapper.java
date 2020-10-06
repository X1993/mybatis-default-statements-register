package com.github.ibatis.statement.demo;

import com.github.ibatis.statement.mapper.EntityType;
import com.github.ibatis.statement.mapper.KeyParameterType;
import com.github.ibatis.statement.mapper.param.DynamicParams;

import java.util.Collection;

/**
 * @Author: junjie
 * @Date: 2020/9/14
 */
public interface CustomUserMapper extends KeyParameterType<Integer> ,EntityType<User>{

    /**
     * 与{@link com.github.ibatis.statement.mapper.KeyTableMapper#selectByPrimaryKey(Object)}方法签名兼容
     * @param key
     * @return
     */
    User selectByPrimaryKey(Integer key);

    /**
     * 与{@link com.github.ibatis.statement.mapper.SelectMapper#selectSelective(Object)}方法签名兼容
     * @param condition
     * @param logicalExist
     * @return
     */
    Collection<? extends User> selectSelective(User condition , boolean logicalExist);

    Integer countByDynamicParams(DynamicParams dynamicParams);

}
