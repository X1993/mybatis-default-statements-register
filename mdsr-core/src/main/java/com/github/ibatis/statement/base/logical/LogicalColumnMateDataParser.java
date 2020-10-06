package com.github.ibatis.statement.base.logical;

import com.github.ibatis.statement.base.core.matedata.EntityMateData;
import com.github.ibatis.statement.util.Sorter;

/**
 * 逻辑列元数据解析
 * @Author: junjie
 * @Date: 2020/3/4
 */
public interface LogicalColumnMateDataParser extends Sorter{

    /**
     * 解析
     * @param entityMateData
     */
    void parse(EntityMateData entityMateData);

}
