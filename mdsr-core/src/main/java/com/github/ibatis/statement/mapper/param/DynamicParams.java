package com.github.ibatis.statement.mapper.param;

import com.github.ibatis.statement.base.core.matedata.EntityMateData;
import lombok.Data;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 动态参数
 * @Author: X1993
 * @Date: 2020/8/31
 */
@Data
public class DynamicParams {

    /**
     * 查询的列
     * 默认值{@link EntityMateData#getBaseColumnListSqlContent()}
     */
    private String selectElements;

    /**
     * where过滤条件
     */
    private ConditionParams whereConditions;

    /**
     * group by 参数
     */
    private final List<String> groupColumns = new ArrayList<>();

    /**
     * having过滤条件
     */
    private ConditionParams havingConditions;

    /**
     * 排序规则
     */
    private final List<OrderRule> orderRules = new ArrayList<>();

    /**
     * limit参数
     */
    private LimitParam limitParam;

    /**
     * 是否只查询逻辑存在
     */
    private boolean logical = true;

    public DynamicParams selectElements(String elements){
        setSelectElements(elements);
        return this;
    }

    /**
     * 添加查询的列
     * @param elements 为了支持更多的语法，不会为每个element添加``，如果是关键字需要自己维护转义符
     * @return
     */
    public DynamicParams addSelectElements(String ... elements){
        if (elements != null && elements.length > 0){
            StringBuilder columnContext = new StringBuilder();
            for (String element : elements) {
                columnContext.append(element).append(",");
            }
            String selectContext = columnContext.deleteCharAt(columnContext.length() - 1).toString();
            setSelectElements(getSelectElements() == null ? selectContext : (getSelectElements() + ", " + selectContext));
        }
        return this;
    }

    public DynamicParams where(ConditionParams conditionParams) {
        setWhereConditions(conditionParams);
        return this;
    }

    public DynamicParams where(Consumer<ConditionParams> consumer){
        setWhereConditions(getWhereConditions() == null ? new ConditionParams() : getWhereConditions());
        consumer.accept(getWhereConditions());
        return this;
    }

    public DynamicParams having(ConditionParams conditionParams) {
        setHavingConditions(conditionParams);
        return this;
    }

    public DynamicParams having(Consumer<ConditionParams> consumer){
        setHavingConditions(getHavingConditions() == null ? new ConditionParams() : getHavingConditions());
        consumer.accept(getHavingConditions());
        return this;
    }

    public DynamicParams groupBy(String ... keys){
        for (String key : keys) {
            groupColumns.add(key);
        }
        return this;
    }

    public DynamicParams addOrderRule(OrderRule orderRule)
    {
        getOrderRules().add(orderRule);
        return this;
    }

    public DynamicParams addOrderRule(List<? extends OrderRule> orderRules)
    {
        getOrderRules().addAll(orderRules);
        return this;
    }

    public DynamicParams addOrderRule(OrderRule ... orderRules){
        if (orderRules != null){
            addOrderRule(Stream.of(orderRules)
                    .collect(Collectors.toList()));
        }
        return this;
    }

    public DynamicParams addOrderRule(OrderRule.Rule rule ,String ... columns){
        if (columns != null){
            addOrderRule(Stream.of(columns)
                    .map(column -> new OrderRule(column ,rule))
                    .collect(Collectors.toList()));
        }
        return this;
    }

    /**
     * 兼容方法
     * @param column
     * @param rule
     * @deprecated 使用{@link #addOrderRule(OrderRule.Rule, String...)}替代
     * @return
     */
    @Deprecated
    public DynamicParams addOrderRule(String column ,OrderRule.Rule rule){
        addOrderRule(new OrderRule(column ,rule));
        return this;
    }

    public DynamicParams asc(String ... keys){
        return addOrderRule(OrderRule.Rule.ASC ,keys);
    }

    public DynamicParams desc(String ... keys){
        return addOrderRule(OrderRule.Rule.DESC ,keys);
    }

    /**
     * 分页
     * @param pageIndex 下标从1开始计数
     * @param pageSize 分页大小
     * @return
     */
    public DynamicParams page1(int pageIndex ,int pageSize){
        setLimitParam(LimitParam.page1(pageIndex, pageSize));
        return this;
    }

    /**
     * 分页
     * @param pageIndex 下标从0开始计数
     * @param pageSize 分页大小
     * @return
     */
    public DynamicParams page0(int pageIndex ,int pageSize){
        setLimitParam(LimitParam.page0(pageIndex, pageSize));
        return this;
    }

    /**
     * limit
     * @param index
     * @param size
     * @return
     */
    public DynamicParams limit(Integer index ,Integer size){
        setLimitParam(new LimitParam(index < 0 ? 0 : index, size));
        return this;
    }

    /**
     * limit
     * @param size
     * @return
     */
    public DynamicParams limit(Integer size){
        setLimitParam(new LimitParam(0, size));
        return this;
    }

    public DynamicParams logical(boolean logical) {
        setLogical(logical);
        return this;
    }

}
