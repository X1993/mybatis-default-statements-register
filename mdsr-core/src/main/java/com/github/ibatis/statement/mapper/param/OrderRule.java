package com.github.ibatis.statement.mapper.param;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 排序规则
 * @author X1993
 * @date 2020/08/31
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderRule {

    /**
     * 列名
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

}
