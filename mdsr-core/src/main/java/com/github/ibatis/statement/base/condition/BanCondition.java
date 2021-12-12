package com.github.ibatis.statement.base.condition;

import org.apache.ibatis.mapping.SqlCommandType;
import java.lang.annotation.*;

/**
 * 对指定的列禁用默认查询，优先级大于{@link Condition}
 * @Author: X1993
 * @Date: 2020/7/24
 */
@Target({ElementType.TYPE ,ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
@Repeatable(BanConditions.class)
public @interface BanCondition {

    /**
     * 如果定义在列映射的类字段上可以不赋值
     */
    String columnName() default "";

    /**
     * 对哪些命令禁用
     */
    SqlCommandType[] commandType() default {SqlCommandType.UPDATE ,SqlCommandType.DELETE};

}
