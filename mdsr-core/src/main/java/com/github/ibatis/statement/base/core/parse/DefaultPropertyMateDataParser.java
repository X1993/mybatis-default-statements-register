package com.github.ibatis.statement.base.core.parse;

import com.github.ibatis.statement.base.core.Column;
import com.github.ibatis.statement.base.core.MappingStrategy;
import com.github.ibatis.statement.base.core.matedata.PropertyMateData;
import com.github.ibatis.statement.util.StringUtils;
import org.apache.ibatis.type.TypeHandler;
import org.apache.ibatis.type.UnknownTypeHandler;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;

/**
 * @Author: junjie
 * @Date: 2020/2/21
 */
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
    public final Optional<PropertyMateData> parse(Field nonStaticField ,Class<?> entityClass)
    {
        String propertyName = nonStaticField.getName();
        Column columnAnnotation = nonStaticField.getAnnotation(Column.class);
        if (columnAnnotation != null) {
            MappingStrategy strategy = columnAnnotation.mappingStrategy();
            if (MappingStrategy.IGNORE.equals(strategy)){
                return Optional.empty();
            }else {
                String columnName = columnAnnotation.value();
                if (columnName == null || "".equals(columnName)) {
                    columnName = defaultNameFunction.apply(propertyName);
                }
                PropertyMateData propertyMateData = new PropertyMateData(columnName, nonStaticField);
                propertyMateData.setMappingStrategy(strategy);
                propertyMateData.setCommandTypeMappings(columnAnnotation.commandTypeMappings());
                Class<? extends TypeHandler<?>> typeHandlerClass = columnAnnotation.typeHandler();
                if (!typeHandlerClass.isInterface()
                        && !Modifier.isAbstract(typeHandlerClass.getModifiers())
                        && !UnknownTypeHandler.class.equals(typeHandlerClass))
                {
                    propertyMateData.setTypeHandlerClass(typeHandlerClass);
                }
                return Optional.of(propertyMateData);
            }
        }

        //如果默认解析失败再尝试自定义解析
        for (PropertyMateDataParser parser : customParsers) {
            Optional optional = parser.parse(nonStaticField ,entityClass);
            if (optional.isPresent()) {
                return optional;
            }
        }

        return Optional.empty();
    }

    public PropertyToColumnNameFunction getDefaultNameFunction() {
        return defaultNameFunction;
    }

    public void setDefaultNameFunction(PropertyToColumnNameFunction defaultNameFunction) {
        Objects.requireNonNull(defaultNameFunction);
        this.defaultNameFunction = defaultNameFunction;
    }

    public List<PropertyMateDataParser> getCustomParsers() {
        return customParsers;
    }

    public void setCustomParsers(List<PropertyMateDataParser> customParsers) {
        Collections.sort(customParsers);
        this.customParsers = customParsers;
    }
}