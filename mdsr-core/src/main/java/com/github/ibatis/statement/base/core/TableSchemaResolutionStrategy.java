package com.github.ibatis.statement.base.core;

import com.github.ibatis.statement.base.core.matedata.PropertyMateData;

/**
 * 表结构解析策略
 * @author X1993
 * @date 2020/10/6
 */
public enum TableSchemaResolutionStrategy {

    /**
     * 查询数据库schema
     * 不同的数据需要实现各自的{@link com.github.ibatis.statement.register.schema.TableSchemaQuery}
     * 如果{@link PropertyMateData#getMappingStrategy()} == {@link MappingStrategy#AUTO}，允许类属性映射的列不存在，会忽略
     */
    DATA_BASE_SCHEMA,

    /**
     * 解析实体类
     * 类似 hibernate/jpa
     */
    ENTITY,

    /**
     * 默认使用全局配置
     * @see com.github.ibatis.statement.base.core.parse.DefaultEntityMateDataParser#defaultTableSchemaResolutionStrategy
     */
    GLOBAL,

}