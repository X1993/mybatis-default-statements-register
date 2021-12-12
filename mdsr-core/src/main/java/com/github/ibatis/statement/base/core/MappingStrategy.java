package com.github.ibatis.statement.base.core;

/**
 * 列映射策略
 * @author jie
 * @date 2020-10-06
 */
public enum MappingStrategy {

    /**
     * 忽略
     */
    IGNORE,

    /**
     * 主键
     * REQUIRED
     */
    PRIMARY_KEY,

    /**
     * 如果不存在映射的列自动忽略，适用{@link TableSchemaResolutionStrategy#DATA_BASE_SCHEMA}策略
     */
    AUTO,

    /**
     * 如果不存在匹配的列会报错，适用{@link TableSchemaResolutionStrategy#DATA_BASE_SCHEMA}策略
     */
    REQUIRED,

}