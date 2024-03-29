package com.github.spring.boot.mdsr;

import com.github.ibatis.statement.base.core.TableSchemaResolutionStrategy;
import com.github.ibatis.statement.base.core.parse.DefaultPropertyMateDataParser;
import com.github.ibatis.statement.base.core.parse.PropertyToColumnNameFunction;
import com.github.ibatis.statement.base.core.parse.TryMappingEveryPropertyMateDataParser;
import com.github.ibatis.statement.register.DefaultStatementAutoRegister;
import org.springframework.boot.context.properties.ConfigurationProperties;
import java.util.function.Function;

/**
 * @Author: X1993
 * @Date: 2020/3/17
 */
@ConfigurationProperties(MappedStatementProperties.PREFIX)
public class MappedStatementProperties {

    static final String PREFIX = "mybatis.mapped-statement.auto-register";

    /**
     * 默认为每一个属性需要尝试映射列
     * @see {@link TryMappingEveryPropertyMateDataParser}
     */
    private boolean eachPropertyMappingColumn = true;

    /**
     * 如果没有指定属性映射的列名，默认通过属性名函数获取对应列名
     * @see DefaultPropertyMateDataParser#defaultNameFunction
     */
    private Class<? extends PropertyToColumnNameFunction> columnNameFunctionClass;

    /**
     * 默认每个实体类都存在映射的表
     * @see {@link com.github.ibatis.statement.base.core.parse.DefaultTableSourceParser#defaultMappingTable}
     */
    private boolean defaultMappingTable = true;

    /**
     * 默认实体类解析表名规则
     * @see com.github.ibatis.statement.base.core.parse.DefaultTableSourceParser#defaultTableNameFunction
     */
    private Class<? extends Function<Class<?> ,String>> tableNameFunctionClass;

    /**
     * 默认table schema解析策略
     */
    private TableSchemaResolutionStrategy tableSchemaResolutionStrategy = TableSchemaResolutionStrategy.DATA_BASE_SCHEMA;

    /**
     * 注册默认监听
     * @see DefaultStatementAutoRegister.Builder#addDefaultListeners()
     */
    private boolean addDefaultListeners = true;

    public boolean isEachPropertyMappingColumn() {
        return eachPropertyMappingColumn;
    }

    public void setEachPropertyMappingColumn(boolean eachPropertyMappingColumn) {
        this.eachPropertyMappingColumn = eachPropertyMappingColumn;
    }

    public Class<? extends PropertyToColumnNameFunction> getColumnNameFunctionClass() {
        return columnNameFunctionClass;
    }

    public void setColumnNameFunctionClass(Class<? extends PropertyToColumnNameFunction> columnNameFunctionClass) {
        this.columnNameFunctionClass = columnNameFunctionClass;
    }

    public TableSchemaResolutionStrategy getTableSchemaResolutionStrategy() {
        return tableSchemaResolutionStrategy;
    }

    public void setTableSchemaResolutionStrategy(TableSchemaResolutionStrategy tableSchemaResolutionStrategy) {
        this.tableSchemaResolutionStrategy = tableSchemaResolutionStrategy;
    }

    public boolean isAddDefaultListeners() {
        return addDefaultListeners;
    }

    public void setAddDefaultListeners(boolean addDefaultListeners) {
        this.addDefaultListeners = addDefaultListeners;
    }

    public boolean isDefaultMappingTable() {
        return defaultMappingTable;
    }

    public void setDefaultMappingTable(boolean defaultMappingTable) {
        this.defaultMappingTable = defaultMappingTable;
    }

    public Class<? extends Function<Class<?>, String>> getTableNameFunctionClass() {
        return tableNameFunctionClass;
    }

    public void setTableNameFunctionClass(Class<? extends Function<Class<?>, String>> tableNameFunctionClass) {
        this.tableNameFunctionClass = tableNameFunctionClass;
    }
}
