package com.github.ibatis.statement.mapper;

/**
 * 定义实体类型
 * @see T 映射的实体类型
 * @author X1993
 * @date 2020/2/22
 */
public interface EntityType<T> {

    /**
     * 逻辑列存在的情况下，删改查是否只针对逻辑存在行
     * @return
     */
    default boolean defaultLogicalValue()
    {
        return true;
    }

}
