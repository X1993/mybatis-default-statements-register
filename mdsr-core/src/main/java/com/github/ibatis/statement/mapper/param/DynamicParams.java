package com.github.ibatis.statement.mapper.param;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

/**
 * 动态参数
 * @Author: junjie
 * @Date: 2020/8/31
 */
public class DynamicParams {

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

    public DynamicParams where(ConditionParams conditionParams) {
        this.whereConditions = conditionParams;
        return this;
    }

    public DynamicParams where(Consumer<ConditionParams> consumer){
        this.whereConditions = this.whereConditions == null ? new ConditionParams() : this.whereConditions;
        consumer.accept(this.whereConditions);
        return this;
    }

    public DynamicParams having(ConditionParams conditionParams) {
        this.havingConditions = conditionParams;
        return this;
    }

    public DynamicParams having(Consumer<ConditionParams> consumer){
        this.havingConditions = this.havingConditions == null ? new ConditionParams() : this.havingConditions;
        consumer.accept(this.havingConditions);
        return this;
    }

    public DynamicParams groupBy(String ... keys){
        for (String key : keys) {
            groupColumns.add(key);
        }
        return this;
    }

    public DynamicParams asc(String key){
        return addOrderRule(key ,OrderRule.Rule.ASC);
    }

    public DynamicParams desc(String key){
        return addOrderRule(key ,OrderRule.Rule.DESC);
    }

    public DynamicParams addOrderRule(OrderRule ... orderRules){
        if (orderRules != null){
            for (OrderRule orderRule : orderRules) {
                this.orderRules.add(orderRule);
            }
        }
        return this;
    }

    public DynamicParams addOrderRule(Collection<? extends OrderRule> orderRules){
        if (orderRules != null){
            for (OrderRule orderRule : orderRules) {
                this.orderRules.add(orderRule);
            }
        }
        return this;
    }

    public DynamicParams addOrderRule(String key ,OrderRule.Rule rule){
        this.orderRules.add(new OrderRule(key ,rule));
        return this;
    }

    public DynamicParams addOrderRule(OrderRule.Rule rule ,String ... keys){
        for (String key : keys) {
            this.orderRules.add(new OrderRule(key ,rule));
        }
        return this;
    }

    public DynamicParams addOrderRules(Iterable<OrderRule> orderRules){
        if (orderRules != null){
            for (OrderRule orderRule : orderRules) {
                this.orderRules.add(orderRule);
            }
        }
        return this;
    }

    /**
     * 分页
     * @param pageIndex 下标从1开始计数
     * @param pageSize 分页大小
     * @return
     */
    public DynamicParams page1(int pageIndex ,int pageSize){
        this.limitParam = LimitParam.page1(pageIndex, pageSize);
        return this;
    }

    /**
     * 分页
     * @param pageIndex 下标从0开始计数
     * @param pageSize 分页大小
     * @return
     */
    public DynamicParams page0(int pageIndex ,int pageSize){
        this.limitParam = LimitParam.page0(pageIndex, pageSize);
        return this;
    }

    /**
     * limit
     * @param index
     * @param size
     * @return
     */
    public DynamicParams limit(Integer index ,Integer size){
        this.limitParam = new LimitParam(index < 0 ? 0 : index, size);
        return this;
    }

    /**
     * limit
     * @param size
     * @return
     */
    public DynamicParams limit(Integer size){
        this.limitParam = new LimitParam(0, size);
        return this;
    }

    public ConditionParams getWhereConditions() {
        return whereConditions;
    }

    public ConditionParams getHavingConditions() {
        return havingConditions;
    }

    public List<String> getGroupColumns() {
        return groupColumns;
    }

    public DynamicParams logical(boolean logical) {
        this.logical = logical;
        return this;
    }

    public List<OrderRule> getOrderRules() {
        return orderRules;
    }

    public LimitParam getLimitParam() {
        return limitParam;
    }

    public boolean isLogical() {
        return logical;
    }
}
