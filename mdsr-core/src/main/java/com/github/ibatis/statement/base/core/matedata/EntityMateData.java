package com.github.ibatis.statement.base.core.matedata;

import com.github.ibatis.statement.base.condition.ColumnCondition;
import com.github.ibatis.statement.base.core.TableSchemaResolutionStrategy;
import com.github.ibatis.statement.base.dv.ColumnDefaultValue;
import com.github.ibatis.statement.base.logical.LogicalColumnMateData;
import org.apache.ibatis.mapping.ResultFlag;
import org.apache.ibatis.mapping.ResultMap;
import org.apache.ibatis.mapping.ResultMapping;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.scripting.xmltags.ForEachSqlNode;
import org.apache.ibatis.scripting.xmltags.MixedSqlNode;
import org.apache.ibatis.scripting.xmltags.SqlNode;
import org.apache.ibatis.scripting.xmltags.StaticTextSqlNode;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.type.TypeHandler;
import org.apache.ibatis.type.TypeHandlerRegistry;
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

    private Configuration configuration;

    public static final String DEFAULT_MAPPING_RESULT_MAP_ID_SUFFIX = "default";

    /**
     * 获取默认ResultMapper
     * @return
     */
    public ResultMap getDefaultMappingResultMap(Class<?> mapperClass)
    {
        Configuration configuration = getConfiguration();
        Class<?> entityClass = this.getEntityClass();
        String mappingResultMappingId = mapperClass.getName() + "." + DEFAULT_MAPPING_RESULT_MAP_ID_SUFFIX;
        if (!configuration.hasResultMap(mappingResultMappingId))
        {
            Set<String> keyPrimaryPropertyNameSet = this.getKeyPrimaryColumnPropertyMappings()
                    .values()
                    .stream()
                    .map(mapping -> mapping.getPropertyName())
                    .collect(Collectors.toSet());

            ResultMap autoMappingResultMapping = new ResultMap.Builder(configuration, mappingResultMappingId,
                    entityClass, this.getColumnPropertyMappings()
                    .values()
                    .stream()
                    .map(columnPropertyMapping -> new ResultMapping.Builder(configuration,
                            columnPropertyMapping.getPropertyName(),
                            columnPropertyMapping.getColumnName(),
                            columnPropertyMapping.getPropertyMateData().getType())
                            .jdbcType(columnPropertyMapping.getColumnMateData().getJdbcType())
                            .flags(keyPrimaryPropertyNameSet.contains(columnPropertyMapping.getPropertyMateData()
                                    .getField().getName()) ? Arrays.asList(ResultFlag.ID) : new ArrayList<>())
                            .typeHandler(resolveTypeHandler(configuration ,
                                    columnPropertyMapping.getPropertyMateData().getType(),
                                    columnPropertyMapping.getPropertyMateData().getTypeHandlerClass()))
                            .build())
                    .collect(Collectors.toList()), null)
                    .build();

            configuration.addResultMap(autoMappingResultMapping);
        }

        return configuration.getResultMap(mappingResultMappingId);
    }

    private TypeHandler<?> resolveTypeHandler(Configuration configuration ,
                                              Class<?> javaType,
                                              Class<? extends TypeHandler<?>> typeHandlerType)
    {
        if (typeHandlerType == null) {
            return null;
        }
        TypeHandlerRegistry typeHandlerRegistry = configuration.getTypeHandlerRegistry();
        // javaType ignored for injected handlers see issue #746 for full detail
        TypeHandler<?> handler = typeHandlerRegistry.getMappingTypeHandler(typeHandlerType);
        if (handler == null) {
            // not in registry, create a new one
            handler = typeHandlerRegistry.getInstance(javaType, typeHandlerType);
        }
        return handler;
    }

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

    public Map<SqlCommandType, Map<String, ColumnCondition>> getCommandTypeConditionMap() {
        return commandTypeConditionMap;
    }

    public void setCommandTypeConditionMap(Map<SqlCommandType, Map<String, ColumnCondition>> commandTypeConditionMap) {
        this.commandTypeConditionMap = Collections.unmodifiableMap(commandTypeConditionMap.entrySet()
                .stream()
                .collect(Collectors.toMap(entry -> entry.getKey() ,
                        entry -> Collections.unmodifiableMap(entry.getValue()))));
    }

    public Map<SqlCommandType, Map<String, ColumnDefaultValue>> getCommandTypeDefaultValueMap() {
        return commandTypeDefaultValueMap;
    }

    public void setCommandTypeDefaultValueMap(Map<SqlCommandType, Map<String, ColumnDefaultValue>> commandTypeDefaultValueMap) {
        this.commandTypeDefaultValueMap = Collections.unmodifiableMap(commandTypeDefaultValueMap.entrySet()
                .stream()
                .collect(Collectors.toMap(entry -> entry.getKey() ,
                        entry -> Collections.unmodifiableMap(entry.getValue()))));
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

    /**
     * 多主键过滤sqlNode
     * @return
     */
    public SqlNode multivaluedKeyConditionSqlNode()
    {
        Map<String ,ColumnPropertyMapping> keyPrimaryColumnPropertyMappings = this.getKeyPrimaryColumnPropertyMappings();
        if (this.isPrimaryKeyParameterIsEntity())
        {
            /*
            <foreach collection="collectionExpression" item="item" index="index" open="(" close=")" separator="or">
               1 = 1
               and key1 = #{item.keyPropertyName1,jdbcType=XXX}
               and key2 = #{item.keyPropertyName2,jdbcType=XXX}
            </foreach>
             */

            StringBuilder whereConditions = new StringBuilder("(");

            //主键的查询条件
            for (ColumnPropertyMapping columnPropertyMapping : keyPrimaryColumnPropertyMappings.values()) {
                whereConditions.append(columnPropertyMapping.createEqSqlContent(name -> "item." + name))
                        .append(" AND ");
            }

            whereConditions.append(" 1 = 1 )");

            return new ForEachSqlNode(configuration ,new StaticTextSqlNode(whereConditions.toString()) ,
                    "collection" ,"index" , "item" ,
                    "(" ,")" ," OR ");

        }else {
            /*
                参数类型一定是唯一主键

                `primaryKeyColName` in (
             <foreach collection="collectionExpression" item="item" separator=",">
                #{item}
             </foreach>
             )
             */

            //主键多值查询
            ColumnPropertyMapping keyColumnPropertyMapping = keyPrimaryColumnPropertyMappings.values()
                    .stream()
                    .findFirst()
                    .get();

            List<SqlNode> sqlNodes = new ArrayList<>();

            sqlNodes.add(new StaticTextSqlNode(new StringBuilder(
                    keyColumnPropertyMapping.getColumnMateData().getEscapeColumnName())
                    .append(" in ")
                    .toString()));

            sqlNodes.add(new ForEachSqlNode(configuration ,new StaticTextSqlNode("#{item}") ,
                    "collection" ,null, "item" ,
                    "(" ,")" ,","));

            return new MixedSqlNode(sqlNodes);
        }
    }

}
