package com.github.ibatis.statement.base.logical;

import com.github.ibatis.statement.base.core.matedata.ColumnMateData;
import lombok.Data;

/**
 * 逻辑存在标识列元数据
 * @Author: X1993
 * @Date: 2020/3/4
 */
@Data
public class LogicalColumnMateData extends ColumnMateData implements Cloneable{

    /**
     * 逻辑存在时的值
     */
    private String existValue;

    /**
     * 逻辑不存在时的值
     */
    private String notExistValue;

    @Override
    public LogicalColumnMateData clone() throws CloneNotSupportedException {
        return (LogicalColumnMateData) super.clone();
    }

    /**
     * `logicalColumn` = existValue
     * @return
     */
    public StringBuilder equalSqlContent(boolean exist){
        return new StringBuilder(getEscapeColumnName())
                .append(" = ")
                .append(exist ? getExistValue() : getNotExistValue());
    }

}
