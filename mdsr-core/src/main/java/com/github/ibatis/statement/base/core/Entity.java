package com.github.ibatis.statement.base.core;

import java.lang.annotation.*;

/**
 * @Author: X1993
 * @Date: 2020/2/21
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface Entity {

    /**
     * 表名，映射的表名，缺省时使用
     * @return
     */
    String tableName() default "";

    /**
     * 表结构解析策略
     * @return
     */
    TableSchemaResolutionStrategy resolutionStrategy() default TableSchemaResolutionStrategy.GLOBAL;

}
