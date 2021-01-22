package com.github.ibatis.statement.base.condition;

import com.github.ibatis.statement.base.core.matedata.ColumnMateData;
import com.github.ibatis.statement.base.core.matedata.EntityMateData;
import com.github.ibatis.statement.mapper.param.ConditionRule;
import org.apache.ibatis.mapping.SqlCommandType;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;

/**
 * 使用自定义规则{@link SpecificColumnConditionParser#predicate}解析所有匹配的条件过滤列
 * @see ColumnCondition
 * @Author: X1993
 * @Date: 2020/7/22
 */
public class SpecificColumnConditionParser implements ColumnConditionParser {

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
     * @see ColumnCondition#value
     * @return
     */
    private String value;

    /**
     * 规则
     */
    private ConditionRule rule = ConditionRule.EQ;

    public SpecificColumnConditionParser(Predicate<ColumnMateData> predicate) {
        Objects.requireNonNull(predicate);
        this.predicate = predicate;
    }

    public SpecificColumnConditionParser(Predicate<ColumnMateData> predicate,
                                         SqlCommandType[] sqlCommandTypes,
                                         ConditionRule rule,
                                         String value)
    {
        Objects.requireNonNull(predicate);
        this.predicate = predicate;
        setSqlCommandTypes(sqlCommandTypes);
        setRule(rule);
        setValue(value);
    }

    @Override
    public void parse(EntityMateData entityMateData)
    {
        Map<SqlCommandType ,Map<String ,ColumnCondition>> commandTypeColumnConditions = new HashMap<>();
        if (sqlCommandTypes != null && sqlCommandTypes.length > 0) {
            for (ColumnMateData columnMateData : entityMateData.getTableMateData().getColumnMateDataList()) {
                if (isMatch(columnMateData)) {
                    //默认为条件列
                    for (SqlCommandType sqlCommandType : sqlCommandTypes) {
                        ColumnCondition columnCondition = new ColumnCondition();
                        columnCondition.setColumnName(columnMateData.getColumnName());
                        columnCondition.setSqlCommandType(sqlCommandType);
                        columnCondition.setValue(getValue());
                        columnCondition.setRule(getRule());
                        commandTypeColumnConditions
                                .computeIfAbsent(sqlCommandType ,type -> new HashMap<>())
                                .put(columnMateData.getColumnName() ,columnCondition);
                    }
                }
            }
        }

        entityMateData.setCommandTypeConditionMap(commandTypeColumnConditions);
    }

    private boolean isMatch(ColumnMateData columnMateData){
        return predicate.test(columnMateData);
    }

    public SqlCommandType[] getSqlCommandTypes() {
        return sqlCommandTypes;
    }

    public String getValue() {
        return value;
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

    public void setValue(String value) {
        Objects.requireNonNull(value);
        this.value = value;
    }

    public ConditionRule getRule() {
        return rule;
    }

    public void setRule(ConditionRule rule) {
        Objects.requireNonNull(rule);
        this.rule = rule;
    }

}
