package com.github.ibatis.statement.base.core.parse;

import com.github.ibatis.statement.base.core.Column;
import com.github.ibatis.statement.base.core.MappingStrategy;
import com.github.ibatis.statement.base.core.matedata.PropertyMateData;
import com.github.ibatis.statement.util.ClassUtils;
import com.github.ibatis.statement.util.StringUtils;
import org.apache.ibatis.type.TypeHandler;
import org.apache.ibatis.type.UnknownTypeHandler;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.text.MessageFormat;
import java.util.*;
import java.util.stream.Collectors;

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
    public final Set<PropertyMateData> parse(Class<?> entityClass)
    {
        Set<PropertyMateData> propertyMateDataSet = new HashSet<>();
        for (PropertyMateDataParser customParser : customParsers) {
            Set<PropertyMateData> customPropertySet = customParser.parse(entityClass);
            if (customPropertySet != null && customPropertySet.size() > 0){
                propertyMateDataSet.addAll(customPropertySet);
            }
        }

        Map<Field, PropertyMateData> propertyMateDataMap = propertyMateDataSet.stream()
                .collect(Collectors.toMap(mateData -> mateData.getField(), mateData -> mateData));

        for (Field field : ClassUtils.getFields(entityClass, false)) {
            String propertyName = field.getName();
            Column columnAnnotation = field.getAnnotation(Column.class);
            if (columnAnnotation != null) {
                MappingStrategy strategy = columnAnnotation.mappingStrategy();
                if (MappingStrategy.IGNORE.equals(strategy)){
                    propertyMateDataMap.remove(field);
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
                    propertyMateDataMap.put(field ,propertyMateData);
                }
            }
        }

        propertyMateDataSet = new HashSet<>(propertyMateDataMap.values());
        Map<String ,Field> columnNameFields = new HashMap<>();
        for (PropertyMateData propertyMateData : propertyMateDataSet) {
            String columnName = propertyMateData.getMappingColumnName();
            Field repeatField = columnNameFields.remove(columnName);
            if (repeatField != null){
                throw new IllegalStateException(MessageFormat.format(
                        "[{0}] and [{1}] mapping repeat column [{2}]" ,repeatField ,
                        propertyMateData.getField() ,columnName));
            }
        }

        return propertyMateDataSet;
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
