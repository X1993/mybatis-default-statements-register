package com.github.ibatis.statement.base.core.parse;

import com.github.ibatis.statement.base.core.matedata.PropertyMateData;
import com.github.ibatis.statement.util.Sorter;
import java.lang.reflect.Field;
import java.util.Optional;

/**
 * 解析类属性元数据
 * @Author: X1993
 * @Date: 2020/2/21
 */
public interface PropertyMateDataParser extends Sorter {

    /**
     * 解析
     * @param entityClass 实体类
     * @param field 类型属性
     * @return
     */
    Optional<PropertyMateData> parse(Class<?> entityClass ,Field field);

}
