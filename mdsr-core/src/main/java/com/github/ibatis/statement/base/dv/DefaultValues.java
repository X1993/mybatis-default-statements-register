package com.github.ibatis.statement.base.dv;

import java.lang.annotation.*;

/**
 * @Author: junjie
 * @Date: 2020/7/22
 */
@Target({ElementType.TYPE ,ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface DefaultValues {

    DefaultValue[] value();

}
