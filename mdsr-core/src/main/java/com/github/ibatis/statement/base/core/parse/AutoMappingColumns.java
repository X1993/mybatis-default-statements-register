package com.github.ibatis.statement.base.core.parse;

import java.lang.annotation.*;

/**
 * 默认所有属性都有映射的列
 * @see TryMappingEveryPropertyMateDataParser
 * @Author: X1993
 * @Date: 2021/6/10
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface AutoMappingColumns {

    /**
     * 是否启用
     * @return
     */
    boolean enable() default true;

}
