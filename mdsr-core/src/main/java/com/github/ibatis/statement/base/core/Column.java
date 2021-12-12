package com.github.ibatis.statement.base.core;

import com.github.ibatis.statement.base.core.matedata.PropertyMateData;
import com.github.ibatis.statement.base.core.parse.DefaultPropertyMateDataParser;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.TypeHandler;
import org.apache.ibatis.type.UnknownTypeHandler;
import java.lang.annotation.*;

/**
 * 实体类属性映射的列信息
 * @Author: X1993
 * @Date: 2020/2/21
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Column {

    /**
     * 列名，缺省时默认通过{@link DefaultPropertyMateDataParser#getDefaultNameFunction()}获取
     * @see PropertyMateData#mappingColumnName
     * @return
     */
    String value() default "";

    /**
     * 列匹配策略
     * @return
     */
    MappingStrategy mappingStrategy() default MappingStrategy.AUTO;

    /**
     * 如果不包含{@link SqlCommandType#INSERT},则自动注入的插入语句会过滤该列的插入
     * 如果不包含{@link SqlCommandType#UPDATE}, ...修改...过滤该列的修改
     * 如果不包含{@link SqlCommandType#SELECT}, ...查询...过滤该列的查询
     * （具体实现逻辑由每个{@link MappedStatementFactory}自定义，也可不实现）
     * @see PropertyMateData#commandTypeMappings
     * @return
     */
    SqlCommandType[] commandTypeMappings() default {SqlCommandType.INSERT ,SqlCommandType.UPDATE ,SqlCommandType.SELECT};

    /**
     * 类型转换器类型
     * @see PropertyMateData#typeHandlerClass
     * @return
     */
    Class<? extends TypeHandler<?>> typeHandler() default UnknownTypeHandler.class;

    /**
     * 映射的列JdbcType
     * @see PropertyMateData#jdbcType
     * @return
     */
    JdbcType jdbcType() default JdbcType.NULL;

}
