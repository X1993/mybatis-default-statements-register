package com.github.ibatis.statement.base.core;

/**
 * 列映射策略
 */
public enum MappingStrategy {

    /**
     * 如果不存在匹配的列，报错
     */
    REQUIRED,

    /**
     * 忽略
     */
    IGNORE,

    /**
     * 如果不存在映射的列自动忽略，适用{@link TableSchemaResolutionStrategy#DATA_BASE}策略
     */
    AUTO,

    /**
     * 主键
     * REQUIRED
     */
    PRIMARY_KEY,

}