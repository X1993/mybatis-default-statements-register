package com.github.ibatis.statement.base.core.matedata;

import com.github.ibatis.statement.base.core.TableSchemaResolutionStrategy;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 表元数据
 * @Author: junjie
 * @Date: 2020/2/21
 */
public class TableMateData implements Cloneable{

    /**
     * 表名
     */
    private String tableName;

    /**
     * 取决于具体数据库
     */
    private String tableType;

    /**
     * 表类型
     */
    private Type type;

    /**
     * 列信息
     */
    private List<ColumnMateData> columnMateDataList = Collections.EMPTY_LIST;

    /**
     * 元数据解析策略
     */
    private TableSchemaResolutionStrategy schemaResolutionStrategy;

    @Override
    public TableMateData clone() throws CloneNotSupportedException {
        return (TableMateData) super.clone();
    }

    public String getTableName() {
        return tableName;
    }

    public String getEscapeTableName(){
        return "`" + tableName + "`";
    }

    public void setTableName(String tableName) {
        this.tableName = tableName.toUpperCase();
    }

    public String getTableType() {
        return tableType;
    }

    public void setTableType(String tableType) {
        this.tableType = tableType;
    }

    public List<ColumnMateData> getColumnMateDataList() {
        return columnMateDataList;
    }

    public void setColumnMateDataList(List<ColumnMateData> columnMateDataList) {
        this.columnMateDataList = columnMateDataList;
    }

    public void addColumnMateData(ColumnMateData columnMateData)
    {
        this.columnMateDataList.add(columnMateData);
    }

    public Map<String ,ColumnMateData> getColumnMateDataMap(){
        return columnMateDataList.stream()
                .collect(Collectors.toMap(columnMateData -> columnMateData.getColumnName() ,
                        columnMateData -> columnMateData));
    }

    public Map<String ,ColumnMateData> getKeyColumnMateDataMap(){
        return columnMateDataList.stream()
                .filter(columnMateData -> columnMateData.isPrimaryKey())
                .collect(Collectors.toMap(columnMateData -> columnMateData.getColumnName() ,
                        columnMateData -> columnMateData));
    }

    public TableSchemaResolutionStrategy getSchemaResolutionStrategy() {
        return schemaResolutionStrategy;
    }

    public void setSchemaResolutionStrategy(TableSchemaResolutionStrategy schemaResolutionStrategy) {
        this.schemaResolutionStrategy = schemaResolutionStrategy;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    /**
     * @Author: junjie
     * @Date: 2020/3/12
     */
    public enum Type {

        /**
         * 基础表
         */
        BASE_TABLE,
        /**
         * 视图
         */
        VIEW,
        /**
         * 系统视图
         */
        SYSTEM_VIEW,
        /**
         * 未定义
         */
        UNDEFINED,

    }

}
