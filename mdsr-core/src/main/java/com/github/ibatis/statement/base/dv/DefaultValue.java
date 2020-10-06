package com.github.ibatis.statement.base.dv;

import org.apache.ibatis.mapping.SqlCommandType;
import java.lang.annotation.*;

/**
 * 添加该注解的属性映射的列每次执行插入/修改操作的时候会自动赋值
 * @see ColumnDefaultValue
 * @Author: junjie
 * @Date: 2020/7/21
 */
@Target({ElementType.FIELD ,ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Repeatable(DefaultValues.class)
public @interface DefaultValue {

    /**
     * 定义执行插入或修改操作时使用默认值
     * @see ColumnDefaultValue#sqlCommandType
     */
    SqlCommandType[] commandTypes() default {SqlCommandType.INSERT ,SqlCommandType.UPDATE};

    /**
     * 列名
     * 如果该注解添加在类属性上，则该值默认为属性映射的列名
     * @see ColumnDefaultValue#columnName
     */
    String columnName() default "";

    /**
     * 默认使用该值作为插入值/修改值，会直接拼接到sql语句上，可以使用支持的数据库函数
     * 注意！如果是字符类型需要带''
     * @see ColumnDefaultValue#value
     * @return
     */
    String value();

    /**
     * 插入的列如果有自定义插入值/修改值，是否使用{@link ColumnDefaultValue#value}覆盖声明的值
     * @see ColumnDefaultValue#overwriteCustom
     */
    boolean overwriteCustom() default true;

}
