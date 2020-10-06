package com.github.ibatis.statement.base.core.parse;

import java.util.function.Function;

/**
 * 实体类属性名对应的数据库表列名
 * @Author: junjie
 * @Date: 2020/3/17
 */
public interface PropertyToColumnNameFunction extends Function<String ,String> {
}
