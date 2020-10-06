package com.github.ibatis.statement.base.core.parse;

import com.github.ibatis.statement.base.core.matedata.PropertyMateData;
import com.github.ibatis.statement.util.Sorter;
import java.lang.reflect.Field;
import java.util.Optional;

/**
 * 解析类属性对应列名
 * @Author: junjie
 * @Date: 2020/2/21
 */
public interface PropertyMateDataParser extends Sorter {

    /**
     * 解析
     * @param field 字段
     * @param entityClass 实体类
     * @return
     */
    Optional<PropertyMateData> parse(Field field , Class<?> entityClass);

}
