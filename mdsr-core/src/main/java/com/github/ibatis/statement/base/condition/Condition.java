package com.github.ibatis.statement.base.condition;

import com.github.ibatis.statement.base.core.ExpressionParser;
import com.github.ibatis.statement.mapper.param.ConditionRule;
import org.apache.ibatis.mapping.SqlCommandType;
import java.lang.annotation.*;

/**
 * 声明列作为过滤条件
 * @Author: junjie
 * @Date: 2020/7/22
 */
@Target({ElementType.FIELD ,ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
@Repeatable(Conditions.class)
public @interface Condition {

    /**
     * 定义执行哪些操作（修改/删除/查询）时使用条件过滤
     * @see ColumnCondition#sqlCommandType
     */
    SqlCommandType[] commandTypes() default {SqlCommandType.UPDATE ,SqlCommandType.DELETE};

    /**
     * 列名
     * 如果该注解添加在类属性上，则该值默认为属性映射的列名
     * @see ColumnCondition#columnName
     */
    String columnName() default "";

    /**
     * 规则
     * @return
     */
    ConditionRule rule() default ConditionRule.EQ;

    /**
     * 默认使用该值作为过滤值，会直接拼接到sql语句上，可以使用支持的数据库函数
     * 注意！如果是字符类型需要带''
     * 支持{@link ExpressionParser}解析
     * @see ColumnCondition#value
     */
    String value() default "";

}
