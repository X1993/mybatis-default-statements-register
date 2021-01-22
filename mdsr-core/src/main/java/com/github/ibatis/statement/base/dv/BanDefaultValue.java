package com.github.ibatis.statement.base.dv;

import com.github.ibatis.statement.base.condition.DefaultColumnConditionParser;
import com.github.ibatis.statement.base.core.matedata.EntityMateData;
import org.apache.ibatis.mapping.SqlCommandType;
import java.lang.annotation.*;

/**
 * 对指定的列禁用默认赋值，优先级大于{@link DefaultValue}
 * @see DefaultColumnConditionParser#parse(EntityMateData)
 * @Author: X1993
 * @Date: 2020/7/24
 */
@Target({ElementType.TYPE ,ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Repeatable(BanDefaultValues.class)
public @interface BanDefaultValue {

    /**
     * 如果定义在列映射的类字段上可以不赋值
     */
    String columnName() default "";

    /**
     * 对哪些命令禁用
     * @return
     */
    SqlCommandType[] commandType() default {SqlCommandType.UPDATE ,SqlCommandType.INSERT};

}
