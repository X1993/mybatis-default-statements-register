package com.github.ibatis.statement.base.core.matedata;

import lombok.Data;
import org.apache.ibatis.type.JdbcType;

/**
 * 列元数据
 * @author junjie
 * @date 2020/2/22
 */
@Data
public class ColumnMateData implements Cloneable{

    /**
     * 列名
     */
    private String columnName;

    /**
     * 是否主键
     */
    private boolean primaryKey;

    /**
     * 数据库-表-列数据类型（不同数据库值可能不同）
     */
    private String dataType;

    /**
     * jdbc类型
     */
    private JdbcType jdbcType;

    @Override
    public ColumnMateData clone() throws CloneNotSupportedException {
        return (ColumnMateData) super.clone();
    }

    /**
     * 获取转义列名，`{@link ColumnMateData#columnName}`
     * @return
     */
    public String getEscapeColumnName(){
        return "`" + columnName + "`";
    }

}
