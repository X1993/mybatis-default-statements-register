package com.github.ibatis.statement.base.core.matedata;

import com.github.ibatis.statement.base.core.TableSchemaResolutionStrategy;

import java.util.*;
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
     * 主键信息
     */
    private List<KeyColumnUsage> keyColumnUsages = Collections.EMPTY_LIST;

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
        this.tableName = tableName;
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
        this.columnMateDataList = Collections.unmodifiableList(columnMateDataList);
    }

    public List<KeyColumnUsage> getKeyColumnUsages() {
        return keyColumnUsages;
    }

    public void setKeyColumnUsages(List<KeyColumnUsage> keyColumnUsages) {
        this.keyColumnUsages = Collections.unmodifiableList(keyColumnUsages);
    }

    public void addColumnMateData(ColumnMateData ... columnMateDataList)
    {
        List<ColumnMateData> columnMateDataList1 = new ArrayList<>(this.columnMateDataList);
        for (ColumnMateData columnMateData : columnMateDataList) {
            columnMateDataList1.add(columnMateData);
        }
        setColumnMateDataList(columnMateDataList1);
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
