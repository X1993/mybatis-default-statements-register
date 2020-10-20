package com.github.ibatis.statement.base.core.parse;

import com.github.ibatis.statement.base.core.MappingStrategy;
import com.github.ibatis.statement.base.core.TableSchemaResolutionStrategy;
import com.github.ibatis.statement.base.core.matedata.*;
import com.github.ibatis.statement.base.condition.ColumnConditionParser;
import com.github.ibatis.statement.base.condition.DefaultColumnConditionParser;
import com.github.ibatis.statement.base.dv.ColumnValueParser;
import com.github.ibatis.statement.base.dv.DefaultColumnValueParser;
import com.github.ibatis.statement.base.logical.DefaultLogicalColumnMateDataParser;
import com.github.ibatis.statement.base.logical.LogicalColumnMateDataParser;
import com.github.ibatis.statement.register.database.DefaultTableSchemaQueryRegister;
import com.github.ibatis.statement.register.database.TableSchemaQuery;
import com.github.ibatis.statement.register.database.TableSchemaQueryRegister;
import com.github.ibatis.statement.util.ClassUtils;
import org.apache.ibatis.mapping.ResultFlag;
import org.apache.ibatis.mapping.ResultMap;
import org.apache.ibatis.mapping.ResultMapping;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.TypeHandler;
import org.apache.ibatis.type.TypeHandlerRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.sql.DataSource;
import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @Author: junjie
 * @Date: 2020/2/24
 */
public class DefaultEntityMateDataParser implements EntityMateDataParser{

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultEntityMateDataParser.class);

    private TableSourceParser tableSourceParser;

    private PropertyMateDataParser propertyMateDataParser;

    private ColumnConditionParser columnConditionParser;

    private ColumnValueParser columnValueParser;

    private LogicalColumnMateDataParser logicalColumnMateDataParser;

    private TableSchemaQueryRegister tableSchemaQueryRegister;

    private TableSchemaResolutionStrategy defaultTableSchemaResolutionStrategy = TableSchemaResolutionStrategy.DATA_BASE_PRIORITY;

    public static final String AUTO_MAPPING_RESULT_MAP_ID_SUFFIX = "autoMappingResultMap";

    /**
     * key : databaseId {@link Configuration#getDatabaseId()}
     * value :
     *  key : entity class
     */
    private final Map<DataSource, Map<Class<?> ,EntityMateData>> databaseEntityMateDataCache = new HashMap<>();

    public DefaultEntityMateDataParser() {
        this(new DefaultTableSourceParser() ,
            new DefaultPropertyMateDataParser() ,
            new DefaultLogicalColumnMateDataParser(),
            new DefaultColumnConditionParser() ,
            new DefaultColumnValueParser() ,
            new DefaultTableSchemaQueryRegister());
    }

    public DefaultEntityMateDataParser(TableSourceParser tableSourceParser,
                                       PropertyMateDataParser propertyMateDataParser,
                                       LogicalColumnMateDataParser logicalColumnMateDataParser,
                                       ColumnConditionParser columnConditionParser,
                                       ColumnValueParser columnValueParser,
                                       TableSchemaQueryRegister tableSchemaQueryRegister)
    {
        setTableSourceParser(tableSourceParser);
        setLogicalColumnMateDataParser(logicalColumnMateDataParser);
        setPropertyMateDataParser(propertyMateDataParser);
        setColumnConditionParser(columnConditionParser);
        setColumnValueParser(columnValueParser);
        setTableSchemaQueryRegister(tableSchemaQueryRegister);
    }

    final EntityMateData NULL = new EntityMateData();

    @Override
    public Optional<EntityMateData> parse(Class<?> entityClazz, SqlSession sqlSession)
    {
        Map<Class<?>, EntityMateData> databaseEntityMateData = databaseEntityMateDataCache
                .computeIfAbsent(sqlSession.getConfiguration().getEnvironment().getDataSource(), dataSource ->  new HashMap<>());

        EntityMateData entityMateData = databaseEntityMateData.get(entityClazz);

        if (entityMateData == null) {
            TableSourceParser.Source tableSource = tableSourceParser.parse(entityClazz).orElse(null);
            if (tableSource == null){
                LOGGER.warn("can't parse table source for entity class [{}]" ,entityClazz);
            }else {
                TableSchemaResolutionStrategy strategy = tableSource.getTableSchemaResolutionStrategy();
                strategy = strategy == null || TableSchemaResolutionStrategy.GLOBAL.equals(strategy)
                        ? defaultTableSchemaResolutionStrategy : strategy;

                String tableName = tableSource.getTableName();
                if (tableName == null || "".equals(tableName)) {
                    LOGGER.warn("can' parse table name from entity class {}", entityClazz);
                } else if (TableSchemaResolutionStrategy.DATA_BASE.equals(strategy)) {
                    entityMateData = parseEntityMateDataByDatabase(entityClazz ,tableName ,strategy ,sqlSession);
                } else if (TableSchemaResolutionStrategy.DATA_BASE_PRIORITY.equals(strategy)) {
                    strategy = TableSchemaResolutionStrategy.DATA_BASE;
                    entityMateData = parseEntityMateDataByDatabase(entityClazz ,tableName ,strategy ,sqlSession);
                    if (entityMateData == null){
                        strategy = TableSchemaResolutionStrategy.ENTITY;
                        entityMateData = parseEntityMateDataByEntity(entityClazz ,tableName ,strategy ,sqlSession);
                    }
                    if (entityMateData != null){
                        entityMateData.getTableMateData().setSchemaResolutionStrategy(strategy);
                    }
                } else if (TableSchemaResolutionStrategy.ENTITY.equals(strategy)) {
                    entityMateData = parseEntityMateDataByEntity(entityClazz ,tableName ,strategy ,sqlSession);
                }
            }
            entityMateData = entityMateData == null ? NULL : entityMateData;
            databaseEntityMateData.put(entityClazz, entityMateData);
        }

        return Optional.ofNullable(entityMateData == NULL ? null : entityMateData);
    }

    private EntityMateData parseEntityMateDataByDatabase(Class entityClazz ,
                                                         String tableName ,
                                                         TableSchemaResolutionStrategy strategy ,
                                                         SqlSession sqlSession)
    {
        TableSchemaQuery tableSchemaQuery = tableSchemaQueryRegister.getTableSchemaQuery(sqlSession).orElse(null);
        if (tableSchemaQuery != null){
            TableMateData tableMateData = tableSchemaQuery.queryTable(sqlSession, tableName).orElse(null);
            if (tableMateData != null){
                tableMateData.setSchemaResolutionStrategy(strategy);
                return buildEntityMateData(
                        entityClazz ,
                        tableMateData ,
                        sqlSession ,
                        parseColumnPropertyMappings(entityClazz ,tableMateData));
            }
        }
        return null;
    }

    private EntityMateData parseEntityMateDataByEntity(Class entityClazz ,
                                                       String tableName ,
                                                       TableSchemaResolutionStrategy strategy ,
                                                       SqlSession sqlSession)
    {
        Map<String, ColumnPropertyMapping> columnPropertyMappings = parseColumnPropertyMappings(entityClazz);

        TableMateData tableMateData = new TableMateData();
        tableMateData.setTableName(tableName);
        tableMateData.setType(TableMateData.Type.UNDEFINED);
        tableMateData.setSchemaResolutionStrategy(strategy);
        tableMateData.setColumnMateDataList(columnPropertyMappings
                .values()
                .stream()
                .map(columnPropertyMapping -> columnPropertyMapping.getColumnMateData())
                .collect(Collectors.toList()));

        return buildEntityMateData(
                entityClazz ,
                tableMateData ,
                sqlSession ,
                columnPropertyMappings);
    }

    private void registerAutoMappingResultMap(Configuration configuration , EntityMateData entityMateData)
    {
        Class<?> entityClass = entityMateData.getEntityClass();
        String autoMappingResultMappingId = entityClass.getName() + "." + AUTO_MAPPING_RESULT_MAP_ID_SUFFIX;
        if (!configuration.hasResultMap(autoMappingResultMappingId))
        {
            Set<String> keyPrimaryPropertyNameSet = entityMateData.getKeyPrimaryColumnPropertyMappings()
                    .values()
                    .stream()
                    .map(mapping -> mapping.getPropertyName())
                    .collect(Collectors.toSet());

            ResultMap autoMappingResultMap = new ResultMap.Builder(configuration, autoMappingResultMappingId,
                    entityClass, entityMateData.getColumnPropertyMappings()
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

            configuration.addResultMap(autoMappingResultMap);
            entityMateData.setAutoMappingResultMap(autoMappingResultMap);
        }
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

    private Map<String ,ColumnPropertyMapping> parseColumnPropertyMappings(Class<?> entityClass ,TableMateData tableMateData)
    {
        Map<String, ColumnMateData> columnMateDataMap = tableMateData.getColumnMateDataMap();
        final Map<String ,ColumnPropertyMapping> columnPropertyMappings = new HashMap<>();

        for (Field field : ClassUtils.getBaseNonStaticFields(entityClass))
        {
            PropertyMateData propertyMateData = propertyMateDataParser.parse(field, entityClass).orElse(null);
            if (propertyMateData != null){
                String mappingColumnName = propertyMateData.getMappingColumnName();
                ColumnMateData columnMateData = columnMateDataMap.get(mappingColumnName);
                if (columnMateData != null)
                {
                    if (columnMateData.isPrimaryKey()){
                        MappingStrategy mappingStrategy = propertyMateData.getMappingStrategy();
                        if (MappingStrategy.IGNORE.equals(mappingStrategy)){
                            throw new IllegalArgumentException("primary key column can't ignore");
                        }
                        propertyMateData.setMappingStrategy(MappingStrategy.PRIMARY_KEY);
                    }else if (propertyMateData.isPrimaryKey()){
                        throw new IllegalArgumentException("property [" + propertyMateData.getField() + "] mapping column is't primary key");
                    }

                    ColumnPropertyMapping repeat = columnPropertyMappings.put(mappingColumnName,
                            new ColumnPropertyMapping(propertyMateData, columnMateDataMap.get(mappingColumnName)));
                    if (repeat != null)
                    {
                        throw new IllegalArgumentException("entity [" + entityClass + "] multiple property mapping to "
                                + "the same column "  + mappingColumnName);
                    }
                }else if (propertyMateData.isRequiredMappingColumn()){
                    throw new IllegalArgumentException("property [" + propertyMateData.getField().toGenericString()
                            + "] mapping column " + mappingColumnName + " is " + propertyMateData.getMappingStrategy()
                            + " ,but don't exist match column on table " + tableMateData.getTableName());
                }
            }
        }

        for (String keyColumnName : tableMateData.getKeyColumnMateDataMap().keySet()) {
            if (!columnPropertyMappings.containsKey(keyColumnName)){
                throw new IllegalArgumentException("unable to map entity attributes for primary key column :" + keyColumnName);
            }
        }

        return columnPropertyMappings;
    }

    private Map<String ,ColumnPropertyMapping> parseColumnPropertyMappings(Class<?> entityClass)
    {
        final Map<String ,ColumnPropertyMapping> columnPropertyMappings = new HashMap<>();

        for (Field field : ClassUtils.getBaseNonStaticFields(entityClass)) {
            PropertyMateData propertyMateData = propertyMateDataParser.parse(field, entityClass).orElse(null);
            if (propertyMateData == null || propertyMateData.isIgnore()){
                continue;
            }

            String mappingColumnName = propertyMateData.getMappingColumnName();
            if (columnPropertyMappings.containsKey(mappingColumnName))
            {
                throw new IllegalArgumentException("entity [" + entityClass + "] multiple property " +
                        "mapping to the same column "  + mappingColumnName);
            }

            ColumnMateData columnMateData = new ColumnMateData();
            columnMateData.setColumnName(propertyMateData.getMappingColumnName());
            columnMateData.setJdbcType(JdbcType.UNDEFINED);
            columnMateData.setPrimaryKey(propertyMateData.isPrimaryKey());

            columnPropertyMappings.put(mappingColumnName, new ColumnPropertyMapping(propertyMateData, columnMateData));
        }

        return columnPropertyMappings;
    }

    private EntityMateData buildEntityMateData(Class<?> entityClass ,
                                              TableMateData tableMateData ,
                                              SqlSession sqlSession ,
                                              Map<String ,ColumnPropertyMapping> columnPropertyMappings)
    {
        Configuration configuration = sqlSession.getConfiguration();

        EntityMateData entityMateData = new EntityMateData();
        entityMateData.setConfiguration(configuration);
        entityMateData.setEntityClass(entityClass);
        entityMateData.setTableMateData(tableMateData);
        entityMateData.setColumnPropertyMappings(columnPropertyMappings);

        registerAutoMappingResultMap(configuration ,entityMateData);

        logicalColumnMateDataParser.parse(entityMateData);
        columnConditionParser.parse(entityMateData);
        columnValueParser.parse(entityMateData);

        return entityMateData;
    }

    public TableSourceParser getTableSourceParser() {
        return tableSourceParser;
    }

    public void setTableSourceParser(TableSourceParser tableSourceParser) {
        Objects.requireNonNull(tableSourceParser);
        this.tableSourceParser = tableSourceParser;
    }

    public PropertyMateDataParser getPropertyMateDataParser() {
        return propertyMateDataParser;
    }

    public void setPropertyMateDataParser(PropertyMateDataParser propertyMateDataParser) {
        Objects.requireNonNull(propertyMateDataParser);
        this.propertyMateDataParser = propertyMateDataParser;
    }

    public LogicalColumnMateDataParser getLogicalColumnMateDataParser() {
        return logicalColumnMateDataParser;
    }

    public void setLogicalColumnMateDataParser(LogicalColumnMateDataParser logicalColumnMateDataParser) {
        Objects.requireNonNull(logicalColumnMateDataParser);
        this.logicalColumnMateDataParser = logicalColumnMateDataParser;
    }

    public ColumnConditionParser getColumnConditionParser() {
        return columnConditionParser;
    }

    public void setColumnConditionParser(ColumnConditionParser columnConditionParser) {
        Objects.requireNonNull(columnConditionParser);
        this.columnConditionParser = columnConditionParser;
    }

    public ColumnValueParser getColumnValueParser() {
        return columnValueParser;
    }

    public void setColumnValueParser(ColumnValueParser columnValueParser) {
        Objects.requireNonNull(columnValueParser);
        this.columnValueParser = columnValueParser;
    }

    public TableSchemaQueryRegister getTableSchemaQueryRegister() {
        return tableSchemaQueryRegister;
    }

    public void setTableSchemaQueryRegister(TableSchemaQueryRegister tableSchemaQueryRegister) {
        Objects.requireNonNull(columnValueParser);
        this.tableSchemaQueryRegister = tableSchemaQueryRegister;
    }

    public TableSchemaResolutionStrategy getDefaultTableSchemaResolutionStrategy() {
        return defaultTableSchemaResolutionStrategy;
    }

    public void setDefaultTableSchemaResolutionStrategy(TableSchemaResolutionStrategy defaultTableSchemaResolutionStrategy) {
        if (defaultTableSchemaResolutionStrategy == null || defaultTableSchemaResolutionStrategy.equals(TableSchemaResolutionStrategy.GLOBAL)){
            throw new IllegalArgumentException();
        }
        this.defaultTableSchemaResolutionStrategy = defaultTableSchemaResolutionStrategy;
    }

    /**
     * 默认构造器
     */
    public static class Builder
    {
        private TableSourceParser tableSourceParser;

        private PropertyMateDataParser propertyMateDataParser;

        private LogicalColumnMateDataParser logicalColumnMateDataParser;

        private ColumnConditionParser columnConditionParser;

        private ColumnValueParser columnValueParser;

        private TableSchemaQueryRegister tableSchemaQueryRegister;

        private TableSchemaResolutionStrategy tableSchemaResolutionStrategy;

        public Builder setTableSourceParser(TableSourceParser tableSourceParser) {
            Objects.requireNonNull(tableSourceParser);
            this.tableSourceParser = tableSourceParser;
            return this;
        }

        public Builder setPropertyMateDataParser(PropertyMateDataParser propertyMateDataParser) {
            Objects.requireNonNull(propertyMateDataParser);
            this.propertyMateDataParser = propertyMateDataParser;
            return this;
        }

        public Builder setLogicalColumnMateDataParser(LogicalColumnMateDataParser logicalColumnMateDataParser) {
            Objects.requireNonNull(logicalColumnMateDataParser);
            this.logicalColumnMateDataParser = logicalColumnMateDataParser;
            return this;
        }

        public Builder setColumnConditionParser(ColumnConditionParser columnConditionParser) {
            Objects.requireNonNull(columnConditionParser);
            this.columnConditionParser = columnConditionParser;
            return this;
        }

        public Builder setColumnValueParser(ColumnValueParser columnValueParser) {
            Objects.requireNonNull(columnValueParser);
            this.columnValueParser = columnValueParser;
            return this;
        }

        public Builder setTableSchemaQueryRegister(TableSchemaQueryRegister tableSchemaQueryRegister) {
            Objects.requireNonNull(tableSchemaQueryRegister);
            this.tableSchemaQueryRegister = tableSchemaQueryRegister;
            return this;
        }

        public Builder setTableSchemaResolutionStrategy(TableSchemaResolutionStrategy strategy){
            this.tableSchemaResolutionStrategy = strategy;
            return this;
        }

        public DefaultEntityMateDataParser build()
        {
            DefaultEntityMateDataParser defaultEntityMateDataParser = new DefaultEntityMateDataParser(
                    tableSourceParser == null ? new DefaultTableSourceParser() : tableSourceParser,
                    propertyMateDataParser == null ? new DefaultPropertyMateDataParser() : propertyMateDataParser,
                    logicalColumnMateDataParser == null ? new DefaultLogicalColumnMateDataParser() : logicalColumnMateDataParser,
                    columnConditionParser == null ? new DefaultColumnConditionParser() : columnConditionParser,
                    columnValueParser == null ? new DefaultColumnValueParser() : columnValueParser,
                    tableSchemaQueryRegister == null ? new DefaultTableSchemaQueryRegister() : tableSchemaQueryRegister);

            if (tableSchemaResolutionStrategy != null){
                defaultEntityMateDataParser.setDefaultTableSchemaResolutionStrategy(tableSchemaResolutionStrategy);
            }

            return defaultEntityMateDataParser;
        }
    }

}
