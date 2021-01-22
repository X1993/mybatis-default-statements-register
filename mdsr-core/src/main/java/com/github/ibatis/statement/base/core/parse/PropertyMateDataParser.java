package com.github.ibatis.statement.base.core.parse;

import com.github.ibatis.statement.base.core.matedata.PropertyMateData;
import com.github.ibatis.statement.util.Sorter;

import java.util.Set;


/**
 * 解析类属性映射的列名
 * @Author: X1993
 * @Date: 2020/2/21
 */
public interface PropertyMateDataParser extends Sorter {

    /**
     * 解析
     * @param entityClass 实体类
     * @return
     */
    Set<PropertyMateData> parse(Class<?> entityClass);

}
