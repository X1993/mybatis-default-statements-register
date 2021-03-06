package com.github.ibatis.statement.mapper.param;

/**
 * 条件过滤规则
 * @author X1993
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
    NOT_LIKE("NOT LIKE"),
    LIKE("LIKE"),
    LIKE_LEFT("LIKE"),
    LIKE_RIGHT("LIKE"),
    NOT_IN("NOT IN"),
    IN("IN"),
    IS_NULL("IS NULL"),
    NOT_NULL("IS NOT NULL"),
    BETWEEN("BETWEEN"),
    NOT_BETWEEN("NOT BETWEEN"),
    NE("<> ''");

    public final String expression;

    ConditionRule(String expression) {
        this.expression = expression;
    }
}