package com.github.ibatis.statement.base.condition;

import java.lang.annotation.*;

/**
 * @Author: junjie
 * @Date: 2020/7/24
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface BanConditions {

    /**
     * 如果为空，默认禁止所有列的默认查询
     */
    BanCondition[] value() default {};

}
