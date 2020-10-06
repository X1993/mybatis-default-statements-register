package com.github.ibatis.statement.mapper.param;

/**
 * 条件过滤规则
 * @author junjie
 * @date 2020/08/31
 */
public enum ConditionRule
{
    EQ("="),
    NOT_EQ("!="),
    LT("<"),
    GT(">"),
    LE("<="),
    GE(">="),
    NOT_LIKE("not like"),
    LIKE("like"),
    LIKE_LEFT("like"),
    LIKE_RIGHT("like"),
    NOT_IN("not in"),
    IN("in"),
    ISNULL("is null"),
    NOT_NULL("is not null"),
    BETWEEN("between"),
    NOT_BETWEEN("not between"),
    NE("<> ''");

    public final String expression;

    ConditionRule(String expression) {
        this.expression = expression;
    }
}