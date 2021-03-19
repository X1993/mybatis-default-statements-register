package com.github.ibatis.statement.base.core.parse;

import com.github.ibatis.statement.base.core.Column;
import com.github.ibatis.statement.base.core.MappingStrategy;
import com.github.ibatis.statement.base.core.matedata.PropertyMateData;
import com.github.ibatis.statement.util.StringUtils;
import lombok.Data;
import org.apache.ibatis.type.TypeHandler;
import org.apache.ibatis.type.UnknownTypeHandler;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;

/**
 * @Author: X1993
 * @Date: 2020/2/21
 */
@Data
public class DefaultPropertyMateDataParser implements PropertyMateDataParser {

    /**
     * 扩展：自定义解析规则
     */
    private List<PropertyMateDataParser> customParsers = Collections.EMPTY_LIST;

    /**
     * 属性名 -> 列名映射
     */
    private PropertyToColumnNameFunction defaultNameFunction = (propertyName) ->
            StringUtils.camelCaseToUnderscore(propertyName);

    public DefaultPropertyMateDataParser() {
    }

    public DefaultPropertyMateDataParser(List<PropertyMateDataParser> customParsers) {
        this.setCustomParsers(customParsers);
    }

    @Override
    public final Optional<PropertyMateData> parse(Class<?> entityClass ,Field field)
    {
        String propertyName = field.getName();
        Column columnAnnotation = field.getAnnotation(Column.class);
        if (columnAnnotation != null) {
            MappingStrategy strategy = columnAnnotation.mappingStrategy();
            if (MappingStrategy.IGNORE.equals(strategy)){
                return Optional.empty();
            }else {
                String columnName = columnAnnotation.value();
                if (columnName == null || "".equals(columnName)) {
                    columnName = defaultNameFunction.apply(propertyName);
                }
                PropertyMateData propertyMateData = new PropertyMateData(columnName, field);
                propertyMateData.setMappingStrategy(strategy);
                propertyMateData.setCommandTypeMappings(columnAnnotation.commandTypeMappings());
                Class<? extends TypeHandler<?>> typeHandlerClass = columnAnnotation.typeHandler();
                if (!typeHandlerClass.isInterface() && !Modifier.isAbstract(typeHandlerClass.getModifiers())
                        && !UnknownTypeHandler.class.equals(typeHandlerClass))
                {
                    propertyMateData.setTypeHandlerClass(typeHandlerClass);
                }
                return Optional.of(propertyMateData);
            }
        }else {
            for (PropertyMateDataParser customParser : customParsers) {
                Optional<PropertyMateData> optionalPropertyMateData = customParser.parse(entityClass, field);
                if (optionalPropertyMateData.isPresent()){
                    return optionalPropertyMateData;
                }
            }
        }

        return Optional.empty();
    }
}
