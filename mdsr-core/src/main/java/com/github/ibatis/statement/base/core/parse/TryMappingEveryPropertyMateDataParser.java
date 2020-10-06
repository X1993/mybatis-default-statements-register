package com.github.ibatis.statement.base.core.parse;

import com.github.ibatis.statement.base.core.matedata.PropertyMateData;
import com.github.ibatis.statement.util.StringUtils;
import java.lang.reflect.Field;
import java.util.Optional;

/**
 * @Author: junjie
 * @Date: 2020/9/8
 */
public class TryMappingEveryPropertyMateDataParser implements PropertyMateDataParser {

    /**
     * 属性名 -> 列名映射
     */
    private PropertyToColumnNameFunction defaultNameFunction = (propertyName) ->
            StringUtils.camelCaseToUnderscore(propertyName);

    @Override
    public int order() {
        return Integer.MAX_VALUE;
    }

    @Override
    public Optional<PropertyMateData> parse(Field field, Class<?> entityClass)
    {
        return Optional.of(new PropertyMateData(defaultNameFunction.apply(field.getName()) ,field));
    }

    public PropertyToColumnNameFunction getDefaultNameFunction() {
        return defaultNameFunction;
    }

    public void setDefaultNameFunction(PropertyToColumnNameFunction defaultNameFunction) {
        this.defaultNameFunction = defaultNameFunction;
    }
}
