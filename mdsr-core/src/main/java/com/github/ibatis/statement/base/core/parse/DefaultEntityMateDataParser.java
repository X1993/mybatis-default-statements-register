package com.github.ibatis.statement.base.core.parse;

import com.github.ibatis.statement.base.condition.ColumnConditionParser;
import com.github.ibatis.statement.base.condition.DefaultColumnConditionParser;
import com.github.ibatis.statement.base.core.MappingStrategy;
import com.github.ibatis.statement.base.core.TableSchemaResolutionStrategy;
import com.github.ibatis.statement.base.core.matedata.*;
import com.github.ibatis.statement.base.dv.ColumnValueParser;
import com.github.ibatis.statement.base.dv.DefaultColumnValueParser;
import com.github.ibatis.statement.base.logical.DefaultLogicalColumnMateDataParser;
import com.github.ibatis.statement.base.logical.LogicalColumnMateDataParser;
import com.github.ibatis.statement.register.database.DefaultTableSchemaQueryRegister;
import com.github.ibatis.statement.register.database.TableSchemaQuery;
import com.github.ibatis.statement.register.database.TableSchemaQueryRegister;
import com.github.ibatis.statement.util.ClassUtils;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.type.JdbcType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.lang.reflect.Field;
import java.text.MessageFormat;
import java.util.*;

/**
 * @Author: X1993
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

    @Override
    public Optional<EntityMateData> parse(Class<?> entityClass, SqlSession sqlSession)
    {
        return Optional.ofNullable(parse0(entityClass ,sqlSession));
    }

    private EntityMateData parse0(Class<?> entityClass ,SqlSession sqlSession)
    {
        TableSourceParser.Source tableSource = tableSourceParser.parse(entityClass).orElse(null);
        if (tableSource == null){
            LOGGER.warn("can't parse table source for entity class [{}]" ,entityClass);
            return null;
        }else {
            TableSchemaResolutionStrategy strategy = tableSource.getTableSchemaResolutionStrategy();
            strategy = strategy == null || TableSchemaResolutionStrategy.GLOBAL.equals(strategy)
                    ? defaultTableSchemaResolutionStrategy : strategy;

            String tableName = tableSource.getTableName();
            if (tableName == null || "".equals(tableName)) {
                LOGGER.warn("can' parse table name from entity class {}", entityClass);
                return null;
            }

            Collection<PropertyMateData> propertyMateDataCollection = parsePropertyMateData(entityClass);

            if (TableSchemaResolutionStrategy.DATA_BASE.equals(strategy)) {
                return parseEntityMateDataByDatabase(entityClass ,tableName ,
                        sqlSession ,propertyMateDataCollection);
            }
            if (TableSchemaResolutionStrategy.DATA_BASE_PRIORITY.equals(strategy)) {
                EntityMateData entityMateData = parseEntityMateDataByDatabase(entityClass ,tableName ,
                        sqlSession ,propertyMateDataCollection);
                if (entityMateData == null){
                    entityMateData = parseEntityMateDataByEntity(entityClass ,tableName ,
                            sqlSession ,propertyMateDataCollection);
                }
                return entityMateData;
            }
            if (TableSchemaResolutionStrategy.ENTITY.equals(strategy)) {
                return parseEntityMateDataByEntity(entityClass ,tableName ,
                        sqlSession ,propertyMateDataCollection);
            }
        }
        return null;
    }

    private Collection<PropertyMateData> parsePropertyMateData(Class<?> entityClass)
    {
        Map<String ,PropertyMateData> columnMappingPropertyMateData = new HashMap<>();
        for (Field field : ClassUtils.getFields(entityClass, false)) {
            PropertyMateData propertyMateData = propertyMateDataParser.parse(entityClass, field).orElse(null);
            if (propertyMateData != null){
                String mappingColumnName = propertyMateData.getMappingColumnName();
                if (columnMappingPropertyMateData.containsKey(mappingColumnName)){
                    throw new IllegalStateException(MessageFormat.format(
                            "[{0}] and [{1}] mapping repeat column [{2}]" ,field ,
                            propertyMateData.getField() ,mappingColumnName));
                }
                columnMappingPropertyMateData.put(propertyMateData.getMappingColumnName() ,propertyMateData);
            }
        }

        return columnMappingPropertyMateData.values();
    }

    private EntityMateData parseEntityMateDataByDatabase(Class entityClass ,
                                                         String tableName ,
                                                         SqlSession sqlSession,
                                                         Collection<PropertyMateData> propertyMateDataSet)
    {
        LOGGER.debug("parse EntityMateData for [{}] from database table [{}]" ,entityClass ,tableName);
        TableSchemaQuery tableSchemaQuery = tableSchemaQueryRegister.getTableSchemaQuery(sqlSession).orElse(null);
        if (tableSchemaQuery != null){
            TableMateData tableMateData = tableSchemaQuery.queryTable(sqlSession, tableName).orElse(null);
            if (tableMateData != null){
                Map<String, ColumnMateData> columnMateDataMap = tableMateData.getColumnMateDataMap();
                final Map<String ,ColumnPropertyMapping> columnPropertyMappings = new HashMap<>();

                for (PropertyMateData propertyMateData : propertyMateDataSet) {
                    String mappingColumnName = propertyMateData.getMappingColumnName();
                    ColumnMateData columnMateData = columnMateDataMap.get(mappingColumnName);
                    if (columnMateData != null)
                    {
                        if (columnMateData.isPrimaryKey()){
                            if (propertyMateData.isIgnore()){
                                throw new IllegalArgumentException("primary key column can't ignore");
                            }
                            propertyMateData.setMappingStrategy(MappingStrategy.PRIMARY_KEY);
                        }else if (propertyMateData.isPrimaryKey()){
                            throw new IllegalArgumentException("property [" + propertyMateData.getField()
                                    + "] mapping column is't primary key");
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

                for (String keyColumnName : tableMateData.getKeyColumnMateDataMap().keySet()) {
                    if (!columnPropertyMappings.containsKey(keyColumnName)){
                        throw new IllegalArgumentException(MessageFormat.format("unable to map entity " +
                                "attributes for primary key column [{0}]" , keyColumnName));
                    }
                }

                tableMateData.setSchemaResolutionStrategy(TableSchemaResolutionStrategy.DATA_BASE);
                return buildEntityMateData(
                        entityClass ,
                        tableMateData ,
                        sqlSession ,
                        columnPropertyMappings);
            }
        }
        return null;
    }

    private EntityMateData parseEntityMateDataByEntity(Class entityClass ,
                                                       String tableName ,
                                                       SqlSession sqlSession,
                                                       Collection<PropertyMateData> propertyMateDataSet)
    {
        LOGGER.debug("parse EntityMateData for [{}] from entity class" ,entityClass);

        final Map<String ,ColumnPropertyMapping> columnPropertyMappings = new HashMap<>();
        Map<String ,String> keyPropertyMappingColumnMap = new HashMap<>();
        List<ColumnMateData> columnMateDataList = new ArrayList<>();

        for (PropertyMateData propertyMateData : propertyMateDataSet) {
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
            if (columnMateData.isPrimaryKey()) {
                keyPropertyMappingColumnMap.put(propertyMateData.getPropertyName() ,mappingColumnName);
            }
            columnMateDataList.add(columnMateData);
        }

        TableMateData tableMateData = new TableMateData();
        tableMateData.setTableName(tableName);
        tableMateData.setType(TableMateData.Type.UNDEFINED);
        tableMateData.setSchemaResolutionStrategy(TableSchemaResolutionStrategy.ENTITY);
        tableMateData.setColumnMateDataList(columnMateDataList);

        List<KeyColumnUsage> keyColumnUsages = new ArrayList<>();
        int i = 1;
        for (Field field : ClassUtils.getFields(entityClass ,false)) {
            String keyColumnName = keyPropertyMappingColumnMap.remove(field.getName());
            if (keyColumnName != null){
                keyColumnUsages.add(new KeyColumnUsage(i++ ,keyColumnName));
            }
            if (keyPropertyMappingColumnMap.isEmpty()){
                break;
            }
        }
        tableMateData.setKeyColumnUsages(keyColumnUsages);

        return buildEntityMateData(
                entityClass ,
                tableMateData ,
                sqlSession ,
                columnPropertyMappings);
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
            Objects.requireNonNull(strategy);
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

