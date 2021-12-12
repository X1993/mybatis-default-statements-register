package com.github.ibatis.statement.mapper.param;

import lombok.Data;

/**
 * 过滤条件
 * @author X1993
 * @date 2020/08/31
 */
@Data
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

}
