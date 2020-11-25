package com.github.ibatis.statement.base.logical;

import com.github.ibatis.statement.base.core.matedata.ColumnMateData;
import com.github.ibatis.statement.base.core.matedata.EntityMateData;
import java.util.Objects;
import java.util.function.Predicate;

/**
 * 使用自定义规则{@link SpecificLogicalColumnMateDataParser#predicate}解析所有匹配的逻辑列
 * @see LogicalColumnMateData
 * @Author: junjie
 * @Date: 2020/3/10
 */
public class SpecificLogicalColumnMateDataParser implements LogicalColumnMateDataParser {

    /**
     * 匹配规则
     */
    private final Predicate<ColumnMateData> predicate;

    /**
     * 逻辑存在时的值
     * @see LogicalColumnMateData#existValue
     */
    private final String existValue;

    /**
     * 逻辑不存在时的值
     * @see LogicalColumnMateData#notExistValue
     */
    private final String notExistValue;

    public SpecificLogicalColumnMateDataParser(Predicate<ColumnMateData> predicate, String existValue, String notExistValue) {
        Objects.requireNonNull("construction parameters [logicalColumnName] is null");
        Objects.requireNonNull("construction parameters [existValue] is null");
        Objects.requireNonNull("construction parameters [notExistValue] is null");
        this.predicate = predicate;
        this.existValue = existValue;
        this.notExistValue = notExistValue;
    }

    public SpecificLogicalColumnMateDataParser(String logicalColumnName, String existValue, String notExistValue) {
        Objects.requireNonNull("construction parameters [logicalColumnName] is null");
        Objects.requireNonNull("construction parameters [existValue] is null");
        Objects.requireNonNull("construction parameters [notExistValue] is null");
        final String matchColumnName = logicalColumnName;
        predicate = columnMateData -> matchColumnName.equals(columnMateData.getColumnName());
        this.existValue = existValue;
        this.notExistValue = notExistValue;
    }

    @Override
    public void parse(EntityMateData entityMateData) {
        if (entityMateData.getLogicalColumnMateData() != null) {
            return;
        }
        for (ColumnMateData columnMateData : entityMateData.getTableMateData().getColumnMateDataList()) {
            if (isMatch(columnMateData)){
                //默认为逻辑字段
                LogicalColumnMateData logicalColumnMateData = new LogicalColumnMateData();
                logicalColumnMateData.setColumnName(columnMateData.getColumnName());
                logicalColumnMateData.setPrimaryKey(columnMateData.isPrimaryKey());
                logicalColumnMateData.setDataType(columnMateData.getDataType());
                logicalColumnMateData.setJdbcType(columnMateData.getJdbcType());
                logicalColumnMateData.setExistValue(existValue);
                logicalColumnMateData.setNotExistValue(notExistValue);
                entityMateData.setLogicalColumnMateData(logicalColumnMateData);
                return;
            }
        }
    }

    private boolean isMatch(ColumnMateData columnMateData){
        return predicate.test(columnMateData);
    }

    public String getExistValue() {
        return existValue;
    }

    public String getNotExistValue() {
        return notExistValue;
    }

}
