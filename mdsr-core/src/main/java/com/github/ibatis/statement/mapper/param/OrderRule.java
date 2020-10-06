package com.github.ibatis.statement.mapper.param;

/**
 * 排序规则
 * @author junjie
 * @date 2020/08/31
 */
public class OrderRule {

    /**
     * 列名 or 列映射的属性名 ，优先级 列名 > 列映射的属性名
     */
    private String key;

    /**
     * 是否升序
     */
    private Rule rule;

    public enum Rule{
        ASC,
        DESC
    }

    public OrderRule(String key, Rule rule) {
        this.key = key;
        this.rule = rule;
    }

    public String getKey() {
        return key;
    }

    public Rule getRule() {
        return rule;
    }

    @Override
    public String toString() {
        return "OrderRule{" +
                "key='" + key + '\'' +
                ", rule=" + rule +
                '}';
    }
}
