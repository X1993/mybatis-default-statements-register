package com.github.ibatis.statement.base.dv;

import com.github.ibatis.statement.base.core.matedata.EntityMateData;
import com.github.ibatis.statement.util.Sorter;

/**
 * 解析使用默认赋值的列
 * @see EntityMateData#commandTypeDefaultValueMap
 * @Author: X1993
 * @Date: 2020/7/22
 */
public interface ColumnValueParser extends Sorter{

    /**
     * 解析
     * @param entityMateData
     */
    void parse(EntityMateData entityMateData);

}
