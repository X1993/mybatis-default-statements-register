package com.github.ibatis.statement.base.core.matedata;

/**
 * 主键信息
 * @Author: junjie
 * @Date: 2020/12/18
 */
public class KeyColumnUsage implements Comparable<KeyColumnUsage>{

    /**
     * 主键顺序
     */
    private int ordinalPosition;

    /**
     * 主键列名
     */
    private String columnName;

    public KeyColumnUsage() {
    }

    public KeyColumnUsage(int ordinalPosition, String columnName) {
        this.ordinalPosition = ordinalPosition;
        this.columnName = columnName;
    }

    public int getOrdinalPosition() {
        return ordinalPosition;
    }

    public void setOrdinalPosition(int ordinalPosition) {
        this.ordinalPosition = ordinalPosition;
    }

    public String getColumnName() {
        return columnName;
    }

    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }

    @Override
    public int compareTo(KeyColumnUsage o) {
        return ordinalPosition - o.ordinalPosition;
    }

}
