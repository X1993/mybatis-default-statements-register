package com.github.ibatis.statement.base.core.matedata;

import lombok.Data;

/**
 * 主键信息
 * @Author: junjie
 * @Date: 2020/12/18
 */
@Data
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

    @Override
    public int compareTo(KeyColumnUsage o) {
        return ordinalPosition - o.ordinalPosition;
    }

}
