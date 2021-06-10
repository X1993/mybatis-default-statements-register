package com.github.ibatis.statement.base.core.parse;

import com.github.ibatis.statement.base.core.matedata.PropertyMateData;
import com.github.ibatis.statement.util.StringUtils;
import lombok.Data;
import java.lang.reflect.Field;
import java.util.Objects;
import java.util.Optional;

/**
 * 默认为每一个属性需要尝试映射列
 * @Author: X1993
 * @Date: 2020/9/8
 */
@Data
public class TryMappingEveryPropertyMateDataParser implements PropertyMateDataParser {

    /**
     * 属性名 -> 列名映射
     */
    private PropertyToColumnNameFunction defaultNameFunction = (propertyName) ->
            StringUtils.camelCaseToUnderscore(propertyName);

    /**
     * 默认每个实体类每个属性都需要尝试映射列
     */
    private boolean eachPropertyMappingColumn = true;

    @Override
    public int order() {
        return Integer.MAX_VALUE - 100;
    }

    @Override
    public Optional<PropertyMateData> parse(Class<?> entityClass ,Field field)
    {
        AutoMappingColumns autoMappingColumns = entityClass.getAnnotation(AutoMappingColumns.class);
        if (autoMappingColumns != null){
            if (autoMappingColumns.enable()){
                return Optional.of(new PropertyMateData(defaultNameFunction.apply(field.getName()) ,field));
            }else {
                return Optional.empty();
            }
        }
        if (isEachPropertyMappingColumn()){
            return Optional.of(new PropertyMateData(defaultNameFunction.apply(field.getName()) ,field));
        }
        return Optional.empty();
    }

    public void setDefaultNameFunction(PropertyToColumnNameFunction defaultNameFunction) {
        Objects.requireNonNull(defaultNameFunction);
        this.defaultNameFunction = defaultNameFunction;
    }

}
