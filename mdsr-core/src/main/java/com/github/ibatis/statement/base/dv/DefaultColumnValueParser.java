package com.github.ibatis.statement.base.dv;

import com.github.ibatis.statement.base.core.ColumnExpressionParser;
import com.github.ibatis.statement.base.core.matedata.ColumnPropertyMapping;
import com.github.ibatis.statement.base.core.matedata.EntityMateData;
import com.github.ibatis.statement.base.core.matedata.PropertyMateData;
import com.github.ibatis.statement.base.core.ExpressionParser;
import org.apache.ibatis.mapping.SqlCommandType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.lang.reflect.Field;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * @Author: junjie
 * @Date: 2020/7/22
 */
public class DefaultColumnValueParser implements ColumnValueParser {

    private final static Logger LOGGER = LoggerFactory.getLogger(DefaultColumnValueParser.class);

    /**
     * 扩展：自定义解析规则
     */
    private List<ColumnValueParser> customParsers = Collections.EMPTY_LIST;

    /**
     * {@link ColumnDefaultValue#value}表达式解析器
     */
    private ExpressionParser expressionParser = ColumnExpressionParser.INSTANT;

    public DefaultColumnValueParser() {
    }

    public DefaultColumnValueParser(List<ColumnValueParser> customParsers) {
        setCustomParsers(customParsers);
    }

    @Override
    public void parse(EntityMateData entityMateData)
    {
        Class<?> entityClass = entityMateData.getEntityClass();
        Map<String, ColumnPropertyMapping> columnPropertyMappings = entityMateData.getColumnPropertyMappings();
        Map<SqlCommandType, Set<String>> noDefaultValueMap = parseBanDefaultValues(entityMateData);
        String tableName = entityMateData.getTableMateData().getTableName();
        Set<String> tableColumnNames = entityMateData.getTableMateData()
                .getColumnMateDataList()
                .stream()
                .map(columnMateData -> columnMateData.getColumnName())
                .collect(Collectors.toSet());

        Map<SqlCommandType ,Map<String ,ColumnDefaultValue>> commandTypeColumnDefaultValueMap = new HashMap<>();

        Consumer<Collection<ColumnDefaultValue>> defaultValueBiConsumer = columnDefaultValues -> {
            for (ColumnDefaultValue columnDefaultValue : columnDefaultValues)
            {
                String columnName = columnDefaultValue.getColumnName();
                SqlCommandType sqlCommandType = columnDefaultValue.getSqlCommandType();

                if (!tableColumnNames.contains(columnName)){
                    LOGGER.warn("table {} invalid column name {}" ,tableName ,columnName);
                }else if (!columnDefaultValue.isOverwriteCustom() && !columnPropertyMappings.containsKey(columnName)){
                    //使用自定义值但是不存在列映射的类字段
                    LOGGER.warn("table {} column {} can't overwrite custom value and not exist " +
                                    "mapping class-property", tableName ,columnName);
                }else if (noDefaultValueMap.computeIfAbsent(sqlCommandType ,
                        commandType -> Collections.EMPTY_SET).contains(columnName)){
                    //禁止列使用默认值
                    LOGGER.warn("table {} column {} can't use default value when sqlCommandType is {}",
                            tableName ,columnName ,sqlCommandType);
                }else {
                    Map<String, ColumnDefaultValue> columnDefaultValueMap = commandTypeColumnDefaultValueMap
                            .computeIfAbsent(sqlCommandType, commandType -> new HashMap<>());
                    if (columnDefaultValueMap.containsKey(columnName)){
                        LOGGER.warn("table {} column {} defines multiple {} value rule",
                                tableName, columnName ,sqlCommandType);
                    }else {
                        columnDefaultValue.setValue(expressionParser.parse(columnName ,
                                columnDefaultValue.getValue() ,entityMateData));
                        columnDefaultValueMap.put(columnName ,columnDefaultValue);
                    }
                }
            }
        };

        DefaultValues defaultValuesAnnotation = entityClass.getAnnotation(DefaultValues.class);
        if (defaultValuesAnnotation != null){
            defaultValueBiConsumer.accept(ColumnDefaultValue.build(defaultValuesAnnotation, null));
        }

        DefaultValue defaultValueAnnotation = entityClass.getAnnotation(DefaultValue.class);
        if (defaultValueAnnotation != null){
            defaultValueBiConsumer.accept(ColumnDefaultValue.build(defaultValueAnnotation, null));
        }

        for (ColumnPropertyMapping columnPropertyMapping : columnPropertyMappings.values())
        {
            PropertyMateData propertyMateData = columnPropertyMapping.getPropertyMateData();
            Field field = propertyMateData.getField();
            String defaultColumnName = propertyMateData.getMappingColumnName();
            defaultValuesAnnotation = field.getAnnotation(DefaultValues.class);
            if (defaultValuesAnnotation != null){
                defaultValueBiConsumer.accept(ColumnDefaultValue.build(defaultValuesAnnotation, defaultColumnName));
            }

            defaultValueAnnotation = field.getAnnotation(DefaultValue.class);
            if (defaultValueAnnotation != null){
                defaultValueBiConsumer.accept(ColumnDefaultValue.build(defaultValueAnnotation, defaultColumnName));
            }
        }

        for (ColumnValueParser customParser : customParsers) {
            customParser.parse(entityMateData);
            defaultValueBiConsumer.accept(entityMateData.getCommandTypeDefaultValueMap()
                    .values()
                    .stream()
                    .map(map -> map.values())
                    .flatMap(list -> list.stream())
                    .collect(Collectors.toList()));
        }

        entityMateData.setCommandTypeDefaultValueMap(commandTypeColumnDefaultValueMap);
    }

    /**
     * 解析所有禁止使用默认值的列
     * @param entityMateData
     * @return key:columnName ,value:columnNames
     */
    private Map<SqlCommandType ,Set<String>> parseBanDefaultValues(EntityMateData entityMateData)
    {
        Class<?> entityClass = entityMateData.getEntityClass();
        Map<SqlCommandType ,Set<String>> noDefaultValueMap = new HashMap<>();
        Set<String> allColumnNames = entityMateData.getTableMateData().getColumnMateDataList()
                .stream()
                .map(columnMateData -> columnMateData.getColumnName())
                .collect(Collectors.toSet());

        BiConsumer<String ,BanDefaultValue> noDefaultValueConsumer = (defaultColumnName , banDefaultValue) -> {
            String name = banDefaultValue.columnName();
            name = "".equals(name) ? defaultColumnName : name;
            if (name == null || "".equals(name) || !allColumnNames.contains(name)){
                LOGGER.warn("exist @BanDefaultValue#columnName() on Class {} illegal" ,entityClass);
                return;
            }
            for (SqlCommandType sqlCommandType : banDefaultValue.commandType()) {
                noDefaultValueMap.computeIfAbsent(sqlCommandType ,type -> new HashSet<>()).add(name);
            }
        };

        BanDefaultValues banDefaultValuesAnnotation = entityClass.getAnnotation(BanDefaultValues.class);
        if (banDefaultValuesAnnotation != null){
            BanDefaultValue[] banDefaultValueAnnotations = banDefaultValuesAnnotation.value();
            if (banDefaultValueAnnotations.length == 0){
                //默认禁用所有列使用默认值
                for (SqlCommandType sqlCommandType : SqlCommandType.values()) {
                    noDefaultValueMap.put(sqlCommandType ,allColumnNames);
                }
                return noDefaultValueMap;
            }else {
                for (BanDefaultValue banDefaultValue : banDefaultValueAnnotations) {
                    noDefaultValueConsumer.accept(null , banDefaultValue);
                }
            }
        }

        BanDefaultValue banDefaultValueAnnotation = entityClass.getAnnotation(BanDefaultValue.class);
        if (banDefaultValueAnnotation != null){
            String columnName = banDefaultValueAnnotation.columnName();
            if (columnName == null || "".equals(columnName)){
                //默认禁用所有列使用默认值
                for (SqlCommandType sqlCommandType : banDefaultValueAnnotation.commandType()) {
                    noDefaultValueMap.put(sqlCommandType ,allColumnNames);
                }
                return noDefaultValueMap;
            }
            noDefaultValueConsumer.accept(null , banDefaultValueAnnotation);
        }

        Map<String, ColumnPropertyMapping> columnPropertyMappings = entityMateData.getColumnPropertyMappings();
        for (ColumnPropertyMapping columnPropertyMapping : columnPropertyMappings.values())
        {
            PropertyMateData propertyMateData = columnPropertyMapping.getPropertyMateData();
            Field field = propertyMateData.getField();
            String columnName = propertyMateData.getMappingColumnName();
            banDefaultValueAnnotation = field.getAnnotation(BanDefaultValue.class);
            if (banDefaultValueAnnotation != null){
                noDefaultValueConsumer.accept(columnName , banDefaultValueAnnotation);
            }
        }

        return noDefaultValueMap;
    }

    public List<ColumnValueParser> getCustomParsers() {
        return customParsers;
    }

    public void setCustomParsers(List<ColumnValueParser> customParsers) {
        Collections.sort(customParsers);
        this.customParsers = customParsers;
    }

    public ExpressionParser getExpressionParser() {
        return expressionParser;
    }

    public void setExpressionParser(ExpressionParser expressionParser) {
        Objects.requireNonNull(expressionParser);
        this.expressionParser = expressionParser;
    }
}
