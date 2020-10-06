package com.github.ibatis.statement.base.dv;

import java.lang.annotation.*;

/**
 * @Author: junjie
 * @Date: 2020/7/24
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface BanDefaultValues {

    /**
     * 如果为空，默认禁止所有列的默认赋值
     */
    BanDefaultValue[] value() default {};

}
