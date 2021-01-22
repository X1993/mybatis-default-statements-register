package com.github.ibatis.statement.base.logical;

import java.lang.annotation.*;

/**
 * 标记表不存在逻辑列，优先级大于{@link Logical}
 * @see LogicalColumnMateDataParser
 * @Author: X1993
 * @Date: 2020/3/10
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface NoLogical {

}
