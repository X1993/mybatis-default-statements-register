package com.github.ibatis.statement.base.condition;

import com.github.ibatis.statement.mapper.param.ConditionRule;
import lombok.Data;
import org.apache.ibatis.mapping.SqlCommandType;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 作为过滤条件的列
 * @Author: X1993
 * @Date: 2020/7/22
 */
@Data
public class ColumnCondition implements Cloneable{

    /**
     * 执行哪种命令时生效
     * 当前版本仅支持{@link SqlCommandType#DELETE}或{@link SqlCommandType#UPDATE}或{@link SqlCommandType#SELECT}
     */
    private SqlCommandType sqlCommandType;

    /**
     * 列名
     */
    private String columnName;

    /**
     * 规则
     */
    private ConditionRule rule = ConditionRule.EQ;

    /**
     * 默认使用该值作为过滤值，会直接拼接到sql语句上，可以使用支持的数据库函数
     * 注意！如果是字符类型需要加引号
     */
    private String value = "";

    @Override
    public ColumnCondition clone() throws CloneNotSupportedException {
        return (ColumnCondition) super.clone();
    }

    public StringBuilder fixedValueSqlContent()
    {
        return new StringBuilder(" `")
                .append(getColumnName())
                .append("` ")
                .append(getRule().expression)
                .append(" ")
                .append(getValue())
                .append(" ");
    }

}
