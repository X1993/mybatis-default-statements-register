package com.github.ibatis.statement.base.logical;

import java.lang.annotation.*;

/**
 * 定义逻辑存在列
 * @Author: X1993
 * @Date: 2020/2/21
 */
@Target({ElementType.TYPE ,ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Logical {

    /**
     * 映射的逻辑列名
     * @see LogicalColumnMateData#getColumnName()
     */
    String columnName();

    /**
     * 逻辑存在时的值
     * @see LogicalColumnMateData#getExistValue()
     * @return
     */
    String existValue();

    /**
     * 逻辑删除时的值
     * @see LogicalColumnMateData#getNotExistValue()
     * @return
     */
    String notExistValue();

}
