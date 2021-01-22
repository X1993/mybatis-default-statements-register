package com.github.ibatis.statement.mapper.param;

import lombok.Data;

/**
 * 排序规则
 * @author X1993
 * @date 2020/08/31
 */
@Data
public class OrderRule {

    /**
     * 列名 or 列映射的属性名 ，优先级 列名 > 列映射的属性名
     */
    private final String key;

    /**
     * 是否升序
     */
    private final Rule rule;

    public enum Rule{
        ASC,
        DESC
    }

    public OrderRule(String key, Rule rule) {
        this.key = key;
        this.rule = rule;
    }

}
