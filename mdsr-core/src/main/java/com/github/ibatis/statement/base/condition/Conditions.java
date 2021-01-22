package com.github.ibatis.statement.base.condition;

import java.lang.annotation.*;

/**
 * @Author: X1993
 * @Date: 2020/7/22
 */
@Target({ElementType.FIELD ,ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface Conditions {

    Condition[] value();

}
