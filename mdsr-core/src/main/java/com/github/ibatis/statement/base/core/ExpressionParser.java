package com.github.ibatis.statement.base.core;

import com.github.ibatis.statement.base.core.matedata.EntityMateData;

/**
 * 自定义表达式解析
 * @Author: junjie
 * @Date: 2020/8/3
 */
public interface ExpressionParser {

    /**
     * 解析
     * @param columnName 列名
     * @param expression 表达式
     * @param entityMateData
     * @return
     */
    String parse(String columnName ,String expression ,EntityMateData entityMateData);

}
