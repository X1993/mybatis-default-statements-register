package com.github.ibatis.statement.mapper;

import com.github.ibatis.statement.base.logical.Logical;
import java.lang.annotation.*;

/**
 * @Author: junjie
 * @Date: 2020/3/9
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Logical(columnName = "removed" ,existValue = "0", notExistValue = "1")
public @interface Removed {
}
