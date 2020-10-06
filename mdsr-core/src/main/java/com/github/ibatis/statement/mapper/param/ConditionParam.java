package com.github.ibatis.statement.mapper.param;

/**
 * 过滤条件
 * @author junjie
 * @date 2020/08/31
 */
public class ConditionParam
{
    /**
     * 列名 or 列映射的属性名 ，优先级 列名 > 列映射的属性名
     */
    private String key;

    /**
     * 比较规则
     */
    private ConditionRule rule;

    /**
     * 值
     */
    private Object value;

    /**
     * 多个规则间的连接符 是否使用`OR` ,默认`AND`
     */
    private boolean or = false;

    public ConditionParam(String key, ConditionRule rule, Object value)
    {
        this.key = key;
        this.rule = rule;
        this.value = value;
    }

    public String getKey() {
        return this.key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public ConditionRule getRule() {
        return rule;
    }

    public void setRule(ConditionRule rule) {
        this.rule = rule;
    }

    public Object getValue() {
        return this.value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public void setOr(boolean or) {
        this.or = or;
    }

    public boolean isOr() {
        return this.or;
    }

    @Override
    public String toString() {
        return "ConditionParam{" +
                "key='" + key + '\'' +
                ", rule=" + rule +
                ", value=" + value +
                ", or=" + or +
                '}';
    }
}
