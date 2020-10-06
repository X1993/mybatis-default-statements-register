package com.github.ibatis.statement.base.dv;

import com.github.ibatis.statement.base.core.matedata.ColumnMateData;
import com.github.ibatis.statement.base.core.matedata.EntityMateData;
import com.github.ibatis.statement.base.condition.ColumnCondition;
import org.apache.ibatis.mapping.SqlCommandType;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;

/**
 * 使用自定义规则{@link SpecificColumnValueParser#predicate}解析所有匹配的默认赋值列
 * @see ColumnDefaultValue
 * @Author: junjie
 * @Date: 2020/7/22
 */
public class SpecificColumnValueParser implements ColumnValueParser {

    /**
     * 匹配规则
     */
    private final Predicate<ColumnMateData> predicate;

    /**
     * 支持的命令类型
     */
    private SqlCommandType[] sqlCommandTypes = new SqlCommandType[]{SqlCommandType.UPDATE ,SqlCommandType.DELETE};

    /**
     * 默认使用该值作为过滤值，会直接拼接到sql语句上，可以使用支持的数据库函数
     * 注意！如果是字符类型需要加引号
     * @see ColumnDefaultValue#value
     * @return
     */
    private String defaultValue;

    /**
     * 如果有自定义值，是否使用{@link ColumnCondition#value}覆盖声明的值
     * @see ColumnDefaultValue#overwriteCustom
     * @return
     */
    private boolean overwriteCustom = false;

    public SpecificColumnValueParser(Predicate<ColumnMateData> predicate) {
        Objects.requireNonNull(predicate);
        this.predicate = predicate;
    }

    public SpecificColumnValueParser(Predicate<ColumnMateData> predicate,
                                     SqlCommandType[] sqlCommandTypes,
                                     String defaultValue,
                                     boolean overwriteCustom)
    {
        Objects.requireNonNull(predicate);
        this.predicate = predicate;
        setSqlCommandTypes(sqlCommandTypes);
        setDefaultValue(defaultValue);
        setOverwriteCustom(overwriteCustom);
    }

    @Override
    public void parse(EntityMateData entityMateData)
    {
        Map<SqlCommandType ,Map<String ,ColumnDefaultValue>> commandTypeColumnDefaultValueMap = new HashMap<>();
        if (sqlCommandTypes != null && sqlCommandTypes.length > 0) {
            for (ColumnMateData columnMateData : entityMateData.getTableMateData().getColumnMateDataList()) {
                if (isMatch(columnMateData)) {
                    //默认为条件列
                    for (SqlCommandType sqlCommandType : sqlCommandTypes) {
                        ColumnDefaultValue columnDefaultValue = new ColumnDefaultValue();
                        columnDefaultValue.setColumnName(columnMateData.getColumnName());
                        columnDefaultValue.setSqlCommandType(sqlCommandType);
                        columnDefaultValue.setValue(getDefaultValue());
                        columnDefaultValue.setOverwriteCustom(isOverwriteCustom());

                        commandTypeColumnDefaultValueMap
                                .computeIfAbsent(sqlCommandType ,commandType -> new HashMap<>())
                                .put(columnMateData.getColumnName() ,columnDefaultValue);
                    }
                }
            }
        }

        entityMateData.setCommandTypeDefaultValueMap(commandTypeColumnDefaultValueMap);
    }

    private boolean isMatch(ColumnMateData columnMateData){
        return predicate.test(columnMateData);
    }

    public SqlCommandType[] getSqlCommandTypes() {
        return sqlCommandTypes;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public boolean isOverwriteCustom() {
        return overwriteCustom;
    }

    public Predicate<ColumnMateData> getPredicate() {
        return predicate;
    }

    public void setSqlCommandTypes(SqlCommandType[] sqlCommandTypes) {
        if (sqlCommandTypes == null && sqlCommandTypes.length == 0) {
            throw new IllegalArgumentException();
        }
        this.sqlCommandTypes = sqlCommandTypes;
    }

    public void setDefaultValue(String defaultValue) {
        Objects.requireNonNull(defaultValue);
        this.defaultValue = defaultValue;
    }

    public void setOverwriteCustom(boolean overwriteCustom) {
        this.overwriteCustom = overwriteCustom;
    }
}
