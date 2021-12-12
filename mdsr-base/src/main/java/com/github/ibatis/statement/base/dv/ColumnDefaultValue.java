package com.github.ibatis.statement.base.dv;

import lombok.Data;
import org.apache.ibatis.mapping.SqlCommandType;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 列默认值
 * @Author: X1993
 * @Date: 2020/7/22
 */
@Data
public class ColumnDefaultValue implements Cloneable {

    /**
     * 执行哪种命令时生效
     * 当前版本仅支持仅支持{@link SqlCommandType#INSERT}或{@link SqlCommandType#UPDATE}
     */
    private SqlCommandType sqlCommandType;

    /**
     * 列名
     */
    private String columnName;

    /**
     * 默认使用该值作为插入值/修改值，会直接拼接到sql语句上，可以使用支持的数据库函数
     * 注意！如果是字符类型需要加引号
     * @return
     */
    private String value = "";

    /**
     * 插入的列如果有自定义插入值/修改值，是否使用{@link ColumnDefaultValue#value}覆盖声明的值
     * @return
     */
    private boolean overwriteCustom = true;

    @Override
    protected Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    public StringBuilder fixedValueSqlContent(){
        return new StringBuilder(" `")
                .append(getColumnName())
                .append("` = ")
                .append(getValue());
    }

}
