package com.github.ibatis.statement.base.condition;

import com.github.ibatis.statement.base.core.matedata.EntityMateData;
import com.github.ibatis.statement.util.Sorter;

/**
 * 解析使用默认条件过滤的列
 * @see EntityMateData#commandTypeConditionMap
 * @Author: X1993
 * @Date: 2020/7/22
 */
public interface ColumnConditionParser extends Sorter {

    /**
     * 解析
     * @param entityMateData
     */
    void parse(EntityMateData entityMateData);

}
