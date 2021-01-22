package com.github.ibatis.statement.base.core.parse;

import com.github.ibatis.statement.base.core.matedata.PropertyMateData;
import com.github.ibatis.statement.util.ClassUtils;
import com.github.ibatis.statement.util.StringUtils;
import java.lang.annotation.*;
import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 默认为每一个属性需要尝试映射列
 * @Author: X1993
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
    public Set<PropertyMateData> parse(Class<?> entityClass)
    {
        return entityClass.getAnnotation(Prohibit.class) == null ?
                ClassUtils.getFields(entityClass ,false)
                .stream()
                .map(field -> new PropertyMateData(defaultNameFunction.apply(field.getName()) ,field))
                .collect(Collectors.toSet()) : Collections.EMPTY_SET;
    }

    public PropertyToColumnNameFunction getDefaultNameFunction() {
        return defaultNameFunction;
    }

    public void setDefaultNameFunction(PropertyToColumnNameFunction defaultNameFunction) {
        Objects.requireNonNull(defaultNameFunction);
        this.defaultNameFunction = defaultNameFunction;
    }

    /**
     * 禁止解析
     */
    @Target({ElementType.TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    @Inherited
    public @interface Prohibit{

    }

}
