package com.github.ibatis.statement.base.core.matedata;

import com.github.ibatis.statement.base.condition.ColumnCondition;
import com.github.ibatis.statement.base.core.TableSchemaResolutionStrategy;
import com.github.ibatis.statement.base.dv.ColumnDefaultValue;
import com.github.ibatis.statement.base.logical.LogicalColumnMateData;
import org.apache.ibatis.mapping.ResultMap;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.scripting.xmltags.SqlNode;
import org.apache.ibatis.scripting.xmltags.StaticTextSqlNode;
import org.apache.ibatis.session.Configuration;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

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

    public Map<String ,ColumnCondition> filterColumnConditions(SqlCommandType sqlCommandType)
    {
        return Optional.ofNullable(commandTypeConditionMap.get(sqlCommandType))
                .map(map -> map.entrySet()
                    .stream()
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
     * 使用默认值的条件过滤
     * @param sqlCommandType
     * @param sqlContentFunction
     * @return
     */
    public StringBuilder defaultConditionsContent(SqlCommandType sqlCommandType ,
                                                  Function<StringBuilder ,StringBuilder> sqlContentFunction)
    {
        return this.filterColumnConditions(sqlCommandType)
                .values()
                .stream()
                .map(columnCondition -> sqlContentFunction.apply(columnCondition.fixedValueSqlContent()))
                .reduce(new StringBuilder() ,(content1 ,content2) -> content1.append(content2));
    }

    /**
     * 使用默认值的条件过滤
     * @param sqlCommandType
     * @param sqlContentFunction
     * @return
     */
    public SqlNode defaultConditionsSqlNode(SqlCommandType sqlCommandType ,
                                            Function<StringBuilder ,StringBuilder> sqlContentFunction)
    {
        return new StaticTextSqlNode(defaultConditionsContent(sqlCommandType ,sqlContentFunction).toString());
    }

}
