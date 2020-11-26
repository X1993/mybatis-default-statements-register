package com.github.ibatis.statement.base.condition;

import com.github.ibatis.statement.base.core.matedata.ColumnMateData;
import com.github.ibatis.statement.base.core.matedata.ColumnPropertyMapping;
import com.github.ibatis.statement.base.core.matedata.EntityMateData;
import com.github.ibatis.statement.base.core.matedata.PropertyMateData;
import com.github.ibatis.statement.base.core.ColumnExpressionParser;
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
 * 默认条件列解析器
 * @Author: junjie
 * @Date: 2020/7/22
 */
public class DefaultColumnConditionParser implements ColumnConditionParser {

    private final static Logger LOGGER = LoggerFactory.getLogger(DefaultColumnConditionParser.class);

    /**
     * 扩展：自定义解析规则
     */
    private List<ColumnConditionParser> customParsers = Collections.EMPTY_LIST;

    /**
     * {@link ColumnCondition#value}表达式解析器
     */
    private ExpressionParser expressionParser = ColumnExpressionParser.INSTANT;

    public DefaultColumnConditionParser() {
    }

    public DefaultColumnConditionParser(List<ColumnConditionParser> customParsers) {
        this.setCustomParsers(customParsers);
    }

    @Override
    public void parse(EntityMateData entityMateData)
    {
        Class<?> entityClass = entityMateData.getEntityClass();
        Map<String, ColumnPropertyMapping> columnPropertyMappings = entityMateData.getColumnPropertyMappings();
        Map<SqlCommandType, Set<String>> noConditionMap = parseBanConditions(entityMateData);
        String tableName = entityMateData.getTableMateData().getTableName();
        Map<String, ColumnMateData> columnMateDataMap = entityMateData.getTableMateData().getColumnMateDataMap();

        Map<SqlCommandType ,Map<String ,ColumnCondition>> conditionMap = new HashMap<>();

        Consumer<Collection<ColumnCondition>> conditionBiConsumer = columnConditions -> {
            for (ColumnCondition columnCondition : columnConditions)
            {
                String columnName = columnCondition.getColumnName();
                SqlCommandType sqlCommandType = columnCondition.getSqlCommandType();

                if (!columnMateDataMap.containsKey(columnName)){
                    LOGGER.warn("table {} invalid column name {}" ,tableName ,columnName);
                    return;
                }

                if (noConditionMap.computeIfAbsent(sqlCommandType ,
                        commandType -> Collections.EMPTY_SET).contains(columnName)){
                    //禁止列使用默认where条件
                    LOGGER.warn("table {} column {} can't use default condition when sqlCommandType is {}",
                            tableName ,columnName ,sqlCommandType);
                    return;
                }

                Map<String, ColumnCondition> columnConditionMap = conditionMap
                        .computeIfAbsent(sqlCommandType, commandType -> new HashMap<>());

                if (columnConditionMap.containsKey(columnName)){
                    LOGGER.warn("table {} column {} defines multiple {} condition rule",
                            tableName, columnName ,sqlCommandType);
                }else {
                    columnCondition.setValue(expressionParser.parse(columnName ,
                            columnCondition.getValue() ,entityMateData));
                    columnConditionMap.put(columnName ,columnCondition);
                }
            }
        };

        Conditions conditionsAnnotation = entityClass.getAnnotation(Conditions.class);
        if (conditionsAnnotation != null){
            conditionBiConsumer.accept(ColumnCondition.build(conditionsAnnotation, null));
        }

        Condition conditionAnnotation = entityClass.getAnnotation(Condition.class);
        if (conditionAnnotation != null){
            conditionBiConsumer.accept(ColumnCondition.build(conditionAnnotation, null));
        }

        for (ColumnPropertyMapping columnPropertyMapping : columnPropertyMappings.values())
        {
            PropertyMateData propertyMateData = columnPropertyMapping.getPropertyMateData();
            Field field = propertyMateData.getField();
            String defaultColumnName = propertyMateData.getMappingColumnName();
            conditionsAnnotation = field.getAnnotation(Conditions.class);
            if (conditionsAnnotation != null){
                conditionBiConsumer.accept(ColumnCondition.build(conditionsAnnotation, defaultColumnName));
            }

            conditionAnnotation = field.getAnnotation(Condition.class);
            if (conditionAnnotation != null){
                conditionBiConsumer.accept(ColumnCondition.build(conditionAnnotation, defaultColumnName));
            }
        }

        for (ColumnConditionParser customParser : customParsers) {
            customParser.parse(entityMateData);
            conditionBiConsumer.accept(entityMateData.getCommandTypeConditionMap().values()
                    .stream()
                    .map(map -> map.values())
                    .flatMap(list -> list.stream())
                    .collect(Collectors.toList()));
        }

        entityMateData.setCommandTypeConditionMap(conditionMap);
    }

    /**
     * 解析所有禁止条件过滤的列
     * @param entityMateData
     * @return key:columnName ,value:columnNames
     */
    private Map<SqlCommandType ,Set<String>> parseBanConditions(EntityMateData entityMateData)
    {
        Class<?> entityClass = entityMateData.getEntityClass();
        Map<SqlCommandType ,Set<String>> noConditionMap = new HashMap<>();
        Set<String> allColumnNames = entityMateData.getTableMateData().getColumnMateDataList()
                .stream()
                .map(columnMateData -> columnMateData.getColumnName())
                .collect(Collectors.toSet());

        BiConsumer<String ,BanCondition> noConditionConsumer = (defaultColumnName , banCondition) -> {
            String columnName = banCondition.columnName();
            columnName = "".equals(columnName) ? defaultColumnName : columnName;
            if (columnName == null || "".equals(columnName) || !allColumnNames.contains(columnName)){
                LOGGER.warn("exist @BanCondition#columnName() on Class {} illegal" ,entityClass);
                return;
            }
            for (SqlCommandType sqlCommandType : banCondition.commandType()) {
                noConditionMap.computeIfAbsent(sqlCommandType ,type -> new HashSet<>()).add(columnName);
            }
        };

        BanConditions banConditionsAnnotation = entityClass.getAnnotation(BanConditions.class);
        if (banConditionsAnnotation != null){
            BanCondition[] banConditionsAnnotations = banConditionsAnnotation.value();
            if (banConditionsAnnotations.length == 0){
                //默认禁用所有列使用默认过滤
                for (SqlCommandType sqlCommandType : SqlCommandType.values()) {
                    noConditionMap.put(sqlCommandType ,allColumnNames);
                }
                return noConditionMap;
            }else {
                for (BanCondition banConditionAnnotation : banConditionsAnnotations) {
                    noConditionConsumer.accept(null , banConditionAnnotation);
                }
            }
        }

        BanCondition banConditionAnnotation = entityClass.getAnnotation(BanCondition.class);
        if (banConditionAnnotation != null){
            String columnName = banConditionAnnotation.columnName();
            if (columnName == null || "".equals(columnName)){
                //默认禁用所有列使用默认过滤
                for (SqlCommandType sqlCommandType : banConditionAnnotation.commandType()) {
                    noConditionMap.put(sqlCommandType ,allColumnNames);
                }
                return noConditionMap;
            }
            noConditionConsumer.accept(null , banConditionAnnotation);
        }

        Map<String, ColumnPropertyMapping> columnPropertyMappings = entityMateData.getColumnPropertyMappings();
        for (ColumnPropertyMapping columnPropertyMapping : columnPropertyMappings.values())
        {
            PropertyMateData propertyMateData = columnPropertyMapping.getPropertyMateData();
            Field field = propertyMateData.getField();
            String columnName = propertyMateData.getMappingColumnName();
            banConditionAnnotation = field.getAnnotation(BanCondition.class);
            if (banConditionAnnotation != null){
                noConditionConsumer.accept(columnName , banConditionAnnotation);
            }
        }

        return noConditionMap;
    }

    public List<ColumnConditionParser> getCustomParsers() {
        return customParsers;
    }

    public void setCustomParsers(List<ColumnConditionParser> customParsers) {
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
