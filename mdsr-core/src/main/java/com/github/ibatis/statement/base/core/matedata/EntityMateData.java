package com.github.ibatis.statement.base.core.matedata;

import com.github.ibatis.statement.base.condition.ColumnCondition;
import com.github.ibatis.statement.base.condition.Strategy;
import com.github.ibatis.statement.base.core.TableSchemaResolutionStrategy;
import com.github.ibatis.statement.base.dv.ColumnDefaultValue;
import com.github.ibatis.statement.base.logical.LogicalColumnMateData;
import org.apache.ibatis.mapping.ResultMap;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.scripting.xmltags.ChooseSqlNode;
import org.apache.ibatis.scripting.xmltags.IfSqlNode;
import org.apache.ibatis.scripting.xmltags.SqlNode;
import org.apache.ibatis.scripting.xmltags.StaticTextSqlNode;
import org.apache.ibatis.session.Configuration;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 实体类元数据
 * @author junjie
 * @date 2020/2/22
 */
public class EntityMateData implements Cloneable{

    public EntityMateData() {
    }

    /**
     * 实体类
     */
    private Class<?> entityClass;

    /**
     * 映射的表元数据
     */
    private TableMateData tableMateData;

    /**
     * 表列名与实体类字段的映射
     * key columnName
     */
    private Map<String ,ColumnPropertyMapping> columnPropertyMappings = Collections.EMPTY_MAP;

    /**
     * 逻辑列
     */
    private LogicalColumnMateData logicalColumnMateData;

    /**
     * 执行不同类型sql命令时列默认条件过滤项
     */
    private Map<SqlCommandType ,Map<String ,ColumnCondition>> commandTypeConditionMap = Collections.EMPTY_MAP;

    /**
     * 执行不同类型sql命令时列默认条件过滤项
     */
    private Map<SqlCommandType ,Map<String ,ColumnDefaultValue>> commandTypeDefaultValueMap =  Collections.EMPTY_MAP;

    /**
     * 默认ResultMap
     */
    private ResultMap autoMappingResultMap;

    private Configuration configuration;

    @Override
    public EntityMateData clone() throws CloneNotSupportedException
    {
        EntityMateData cloneEntityMateData = (EntityMateData) super.clone();
        cloneEntityMateData.setTableMateData(tableMateData.clone());
        cloneEntityMateData.setLogicalColumnMateData(logicalColumnMateData == null ? null : logicalColumnMateData.clone());
        return cloneEntityMateData;
    }

    public String getTableName(){
        return tableMateData.getTableName();
    }

    public Class<?> getEntityClass() {
        return entityClass;
    }

    public void setEntityClass(Class<?> entityClass) {
        this.entityClass = entityClass;
    }

    public TableMateData getTableMateData() {
        return tableMateData;
    }

    public void setTableMateData(TableMateData tableMateData) {
        this.tableMateData = tableMateData;
    }

    public TableSchemaResolutionStrategy getSchemaResolutionStrategy() {
        return tableMateData.getSchemaResolutionStrategy();
    }

    public Configuration getConfiguration() {
        return configuration;
    }

    public void setConfiguration(Configuration configuration) {
        this.configuration = configuration;
    }

    public Map<String, ColumnPropertyMapping> getColumnPropertyMappings() {
        return columnPropertyMappings;
    }

    public void setColumnPropertyMappings(Map<String, ColumnPropertyMapping> columnPropertyMappings) {
        if (columnPropertyMappings == null || columnPropertyMappings.size() == 0){
            this.commandTypeConditionMap = Collections.EMPTY_MAP;
            return;
        }
        this.columnPropertyMappings = Collections.unmodifiableMap(columnPropertyMappings);
    }

    public Map<String, ColumnPropertyMapping> getKeyPrimaryColumnPropertyMappings() {
        return columnPropertyMappings.values()
                .stream()
                .filter(columnPropertyMapping -> columnPropertyMapping.isPrimaryKey())
                .collect(Collectors.toMap(mapping -> mapping.getColumnName() ,mapping -> mapping));
    }

    public LogicalColumnMateData getLogicalColumnMateData() {
        return logicalColumnMateData;
    }

    public void setLogicalColumnMateData(LogicalColumnMateData logicalColumnMateData) {
        this.logicalColumnMateData = logicalColumnMateData;
    }

    public int getPrimaryKeyCount() {
        return getKeyPrimaryColumnPropertyMappings().size();
    }

    public ResultMap getAutoMappingResultMap() {
        return autoMappingResultMap;
    }

    public Map<SqlCommandType, Map<String, ColumnCondition>> getCommandTypeConditionMap() {
        return commandTypeConditionMap;
    }

    public void setCommandTypeConditionMap(Map<SqlCommandType, Map<String, ColumnCondition>> commandTypeConditionMap) {
        if (commandTypeConditionMap == null || commandTypeConditionMap.size() == 0){
            this.commandTypeConditionMap = Collections.EMPTY_MAP;
            return;
        }
        this.commandTypeConditionMap = Collections.unmodifiableMap(commandTypeConditionMap.entrySet()
                .stream()
                .collect(Collectors.toMap(entry -> entry.getKey() ,
                        entry -> Collections.unmodifiableMap(entry.getValue()))));
    }

    public Map<SqlCommandType, Map<String, ColumnDefaultValue>> getCommandTypeDefaultValueMap() {
        return commandTypeDefaultValueMap;
    }

    public void setCommandTypeDefaultValueMap(Map<SqlCommandType, Map<String, ColumnDefaultValue>> commandTypeDefaultValueMap) {
        if (commandTypeDefaultValueMap == null || commandTypeDefaultValueMap.size() == 0){
            this.commandTypeDefaultValueMap = Collections.EMPTY_MAP;
            return;
        }
        this.commandTypeDefaultValueMap = Collections.unmodifiableMap(commandTypeDefaultValueMap.entrySet()
                .stream()
                .collect(Collectors.toMap(entry -> entry.getKey() ,
                        entry -> Collections.unmodifiableMap(entry.getValue()))));
    }

    public void setAutoMappingResultMap(ResultMap autoMappingResultMap) {
        this.autoMappingResultMap = autoMappingResultMap;
    }

    public boolean isPrimaryKeyParameterIsEntity(){
        return getEntityClass().isAssignableFrom(getReasonableKeyParameterClass());
    }

    public Class<?> getReasonableKeyParameterClass()
    {
        //复合主键默认直接使用实体类型作为主键参数类型
        Class primaryKeyParameterClass = entityClass;
        int primaryKeyCount = getPrimaryKeyCount();
        if (primaryKeyCount == 0){
            throw new IllegalArgumentException("entity [" + entityClass
                    + "] mapping table [" + getTableName() + "] don't exist primary key");
        }
        if (primaryKeyCount == 1){
            primaryKeyParameterClass = getKeyPrimaryColumnPropertyMappings().values()
                    .stream()
                    .findFirst()
                    .get()
                    .getPropertyMateData()
                    .getField()
                    .getType();
        }
        return primaryKeyParameterClass;
    }

    public Map<String ,ColumnPropertyMapping> getInsertColumnPropertyMapping()
    {
        return getColumnPropertyMappings().entrySet()
                .stream()
                .filter(entry -> entry.getValue().getPropertyMateData().isInsertMapping() && (
                        logicalColumnMateData == null || !logicalColumnMateData.getColumnName().equals(entry.getKey())
                ))
                .collect(Collectors.toMap(entry -> entry.getKey() ,entry -> entry.getValue()));
    }

    public Map<String ,ColumnPropertyMapping> getSelectColumnPropertyMapping(){
        return getColumnPropertyMappings().entrySet()
                .stream()
                .filter(entry -> entry.getValue().getPropertyMateData().isSelectMapping())
                .collect(Collectors.toMap(entry -> entry.getKey() ,entry -> entry.getValue()));
    }

    public Map<String ,ColumnPropertyMapping> getUpdateColumnPropertyMapping()
    {
        Set<String> keyColumnNames = getKeyPrimaryColumnPropertyMappings().keySet();
        return getColumnPropertyMappings().entrySet()
                .stream()
                .filter(entry -> entry.getValue().getPropertyMateData().isUpdateMapping() && (
                        logicalColumnMateData == null || !logicalColumnMateData.getColumnName().equals(entry.getKey())
                ) && !keyColumnNames.contains(entry.getKey()))
                .collect(Collectors.toMap(entry -> entry.getKey() ,entry -> entry.getValue()));
    }

    public Map<String ,ColumnDefaultValue> filterColumnDefaultValues(SqlCommandType sqlCommandType ,boolean overwriteCustom)
    {
        return Optional.ofNullable(getCommandTypeDefaultValueMap().get(sqlCommandType))
                .map(map -> map.entrySet()
                        .stream()
                        .filter(entry -> logicalColumnMateData == null
                                || !entry.getKey().equals(logicalColumnMateData.getColumnName()))
                        .filter(entry -> overwriteCustom == entry.getValue().isOverwriteCustom())
                        .collect(Collectors.toMap(entry -> entry.getKey() ,entry -> entry.getValue())))
                .orElse(Collections.EMPTY_MAP);
    }

    public Map<String ,ColumnDefaultValue> filterColumnDefaultValues(SqlCommandType sqlCommandType)
    {
        return Optional.ofNullable(getCommandTypeDefaultValueMap().get(sqlCommandType))
                .map(map -> map.entrySet()
                        .stream()
                        .filter(entry -> logicalColumnMateData == null
                                || !entry.getKey().equals(logicalColumnMateData.getColumnName()))
                        .collect(Collectors.toMap(entry -> entry.getKey() ,entry -> entry.getValue())))
                .orElse(Collections.EMPTY_MAP);
    }

    public Map<String ,ColumnCondition> filterColumnConditions(SqlCommandType sqlCommandType ,Strategy ... strategies)
    {
        if (strategies == null || strategies.length == 0){
            return commandTypeConditionMap.getOrDefault(sqlCommandType ,Collections.EMPTY_MAP);
        }
        return Optional.ofNullable(commandTypeConditionMap.get(sqlCommandType))
                .map(map -> map.entrySet()
                    .stream()
                    .filter(entry -> Stream.of(strategies)
                            .anyMatch(strategy -> strategy.equals(entry.getValue().getStrategy())))
                    .collect(Collectors.toMap(entry -> entry.getKey() ,entry -> entry.getValue())))
                .orElse(Collections.EMPTY_MAP);
    }

    /**
     * 构建查询的列
     * @return
     */
    public StringBuilder getBaseColumnListSqlContent()
    {
        StringBuilder sqlContext = new StringBuilder();
        for (String columnName : getSelectColumnPropertyMapping().keySet())
        {
            sqlContext.append("`")
                    .append(columnName)
                    .append("`,");
        }

        if (sqlContext.length() > 0) {
            sqlContext.delete(sqlContext.length() - 1, sqlContext.length());
        }
        return sqlContext;
    }

    /**
     * 根据过滤条件构建SqlNode
     *

     -  GLOBAL:
        `columnName` = defaultValue

     -  CUSTOM:
        `columnName` = #{propertyName,jdbcType=XXX}

     -  CUSTOM_MISS_SKIP:
         <if test="propertyName != null">
            `columnName` = #{propertyName,jdbcType=XXX}
         </if>

     -  CUSTOM_MISS_DEFAULT:
         <choose>
             <if test="propertyName5 != null">
                `col5` = #{propertyName5,jdbcType=XXX}
             </if>
             <otherwise>
                `col5` = defaultValue5
             </otherwise>
         </choose>

     * @param columnCondition
     * @param propertyNameFunction
     * @param sqlContentFunction
     * @return
     */
    public SqlNode createConditionSqlNode(ColumnCondition columnCondition ,
                                          Function<String ,String> propertyNameFunction,
                                          Function<StringBuilder ,StringBuilder> sqlContentFunction)
    {
        Strategy strategy = columnCondition.getStrategy();
        String columnName = columnCondition.getColumnName();
        ColumnPropertyMapping columnPropertyMapping = columnPropertyMappings.get(columnName);

        StaticTextSqlNode defaultCondition = new StaticTextSqlNode(
                sqlContentFunction.apply(columnCondition.fixedValueSqlContent()).toString());

        StaticTextSqlNode customSqlNode = new StaticTextSqlNode(
                sqlContentFunction.apply(columnPropertyMapping.createConditionSqlContent(
                        columnCondition.getRule() ,propertyNameFunction)).toString());

        IfSqlNode equalIfSqlNode = new IfSqlNode(customSqlNode ,
                propertyNameFunction.apply(columnPropertyMapping.getPropertyName()) + " != null");

        switch (strategy){
            case DEFAULT:
                return defaultCondition;
            case CUSTOM:
                return customSqlNode;
            case CUSTOM_MISS_DEFAULT:
                return new ChooseSqlNode(Arrays.asList(equalIfSqlNode) ,defaultCondition);
            case CUSTOM_MISS_SKIP:
                return equalIfSqlNode;
            default:
                throw new IllegalArgumentException("Temporarily does not support [" + strategy + "] strategy");
        }
    }

    /**
     * 配置带默认值的条件过滤
     * @param sqlCommandType
     * @param sqlContentFunction
     * @return
     */
    public StringBuilder noCustomConditionsContent(SqlCommandType sqlCommandType ,
                                                    Function<StringBuilder ,StringBuilder> sqlContentFunction)
    {
        return this.filterColumnConditions(
                sqlCommandType ,Strategy.CUSTOM_MISS_DEFAULT ,Strategy.DEFAULT)
                .values()
                .stream()
                .map(columnCondition -> {
                    try {
                        return columnCondition.clone();
                    } catch (CloneNotSupportedException e) {
                        throw new IllegalStateException(e);
                    }
                })
                .peek(columnCondition -> columnCondition.setStrategy(Strategy.DEFAULT))
                .map(columnCondition -> sqlContentFunction.apply(columnCondition.fixedValueSqlContent()))
                .reduce(new StringBuilder() ,(content1 ,content2) -> content1.append(content2));
    }

    /**
     * 配置带默认值的条件过滤
     * @param sqlCommandType
     * @param sqlContentFunction
     * @return
     */
    public SqlNode noCustomConditionsSqlNode(SqlCommandType sqlCommandType ,
                                             Function<StringBuilder ,StringBuilder> sqlContentFunction)
    {
        return new StaticTextSqlNode(noCustomConditionsContent(sqlCommandType ,sqlContentFunction).toString());
    }

    /**
     * 自定义非空匹配条件过滤
     * @param sqlCommandType
     * @param propertyNameFunction
     * @param sqlContentFunction
     * @return
     */
    public List<SqlNode> selectiveConditionSqlNodes(SqlCommandType sqlCommandType  ,
                                                    Function<String ,String> propertyNameFunction,
                                                    Function<StringBuilder ,StringBuilder> sqlContentFunction)
    {
        Map<String, ColumnCondition> defaultColumnConditions = this.filterColumnConditions(
                sqlCommandType ,Strategy.CUSTOM_MISS_DEFAULT ,Strategy.DEFAULT);

        List<ColumnPropertyMapping> missColumnConditions = this.getColumnPropertyMappings()
                .values()
                .stream()
                .filter(mapping -> !defaultColumnConditions.containsKey(mapping.getColumnName()))
                .collect(Collectors.toList());

        List<ColumnCondition> list = new ArrayList<>();
        list.addAll(defaultColumnConditions.values());
        for (ColumnPropertyMapping missColumnCondition : missColumnConditions) {
            ColumnCondition columnCondition = new ColumnCondition();
            columnCondition.setColumnName(missColumnCondition.getColumnName());
            columnCondition.setStrategy(Strategy.CUSTOM_MISS_SKIP);
            list.add(columnCondition);
        }

        return list.stream()
                .map(columnCondition -> this.createConditionSqlNode(
                        columnCondition ,propertyNameFunction ,sqlContentFunction))
                .collect(Collectors.toList());
    }

}
