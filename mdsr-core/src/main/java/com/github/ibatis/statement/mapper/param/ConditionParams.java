package com.github.ibatis.statement.mapper.param;

import lombok.Data;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * @Author: X1993
 * @Date: 2020/9/1
 */
@Data
public class ConditionParams {

    /**
     * 过滤条件
     */
    private final List<ConditionParam> params = new ArrayList<>();

    /**
     * 下一个添加的过滤条件 分隔符
     */
    private boolean or;

    /**
     * 语法上允许值为NULL的规则，如果值为NULL时，默认情况下是否略过
     */
    private boolean defaultAllowNull;

    /**
     * 自定义条件
     * such as : count(0) as `name`
     */
    private String customCondition;

    /**
     * 添加自定义条件
     * @return
     */
    public ConditionParams customCondition(String customCondition){
        setCustomCondition(customCondition);
        return this;
    }

    /**
     * 两次调用之间的{@link ConditionParams#params}在构建sql时每个条件使用 or 连接，最外层嵌套括号
     * @return
     */
    public ConditionParams or(){
        this.or = !or;
        if (!this.or && params.size() > 0){
            setOr(params.size() - 1 ,false);
        }
        return this;
    }

    public ConditionParams setOr(int index ,boolean isOr){
        if (index < params.size()) {
            params.get(index).setOr(isOr);
        }
        return this;
    }

    public ConditionParams eq(String key ,Object value ,boolean allowNull){
        if (value != null || allowNull) {
            addConditionParam(key ,ConditionRule.EQ ,value);
        }
        return this;
    }

    public ConditionParams eq(String key ,Object value){
        return eq(key ,value ,defaultAllowNull);
    }

    public ConditionParams notEq(String key ,Object value ,boolean allowNull){
        if (value != null || allowNull) {
            addConditionParam(key,ConditionRule.NOT_EQ,value);
        }
        return this;
    }

    public ConditionParams notEq(String key ,Object value){
        return notEq(key,value , defaultAllowNull);
    }

    public ConditionParams lt(String key ,Object value ,boolean allowNull){
        if (value != null || allowNull) {
            addConditionParam(key,ConditionRule.LT,value);
        }
        return this;
    }

    public ConditionParams lt(String key ,Object value){
        return lt(key, value, defaultAllowNull);
    }

    public ConditionParams gt(String key ,Object value ,boolean allowNull){
        if (value != null || allowNull) {
            addConditionParam(key, ConditionRule.GT, value);
        }
        return this;
    }

    public ConditionParams gt(String key ,Object value){
        return gt(key, value, defaultAllowNull);
    }

    public ConditionParams ltEq(String key ,Object value ,boolean allowNull){
        if (value != null || allowNull) {
            addConditionParam(key,ConditionRule.LE,value);
        }
        return this;
    }

    public ConditionParams ltEq(String key ,Object value){
        return ltEq(key,value, defaultAllowNull);
    }

    public ConditionParams gtEq(String key ,Object value,boolean allowNull){
        if (value != null || allowNull) {
            addConditionParam(key,ConditionRule.GE,value);
        }
        return this;
    }

    public ConditionParams gtEq(String key ,Object value){
        return gtEq(key,value , defaultAllowNull);
    }

    public ConditionParams ne(String key){
        this.addConditionParam(key, ConditionRule.NE ,null);
        return this;
    }

    public ConditionParams like(String key ,String value){
        if (value != null) {
            addConditionParam(key,ConditionRule.LIKE, "%" + value + "%");
        }
        return this;
    }

    public ConditionParams likeLeft(String key ,String value){
        if (value != null) {
            addConditionParam(key, ConditionRule.LIKE, "%" + value);
        }
        return this;
    }

    public ConditionParams likeRight(String key ,String value){
        if (value != null) {
            this.addConditionParam(key, ConditionRule.LIKE, value + "%");
        }
        return this;
    }

    public ConditionParams notLike(String key ,String value){
        if (value != null) {
            this.addConditionParam(key ,ConditionRule.LIKE, "%" + value + "%");
        }
        return this;
    }

    public ConditionParams between(String key ,Object minVal ,Object maxVal){
        if (minVal != null && maxVal != null) {
            this.addConditionParam(key ,ConditionRule.BETWEEN ,new BetweenParam(minVal ,maxVal));
        }
        return this;
    }

    public ConditionParams notIn(String key ,Collection collection)
    {
        if (collection != null && collection.size() > 0){
            this.params.add(new ConditionParam(key, ConditionRule.NOT_IN, collection));
        }
        return this;
    }

    public ConditionParams notIn(String key ,Object ... values){
        return values == null || values.length == 0 ? this : this.notIn(key ,Arrays.asList(values));
    }

    public ConditionParams in(String key ,Collection collection)
    {
        if (collection != null && collection.size() > 0){
            this.params.add(new ConditionParam(key, ConditionRule.IN, collection));
        }
        return this;
    }

    public ConditionParams in(String key ,Object ... values)
    {
        return values == null || values.length == 0 ? this : this.in(key ,Arrays.asList(values));
    }

    public ConditionParams isNull(String key){
        this.addConditionParam(key ,ConditionRule.IS_NULL,null);
        return this;
    }

    public ConditionParams notNull(String key){
        this.addConditionParam(key ,ConditionRule.NOT_NULL ,null);
        return this;
    }

    public ConditionParams addConditionParam(ConditionParam conditionParam)
    {
        conditionParam.setOr(or);
        this.params.add(conditionParam);
        return this;
    }

    public ConditionParams addConditionParam(String key ,ConditionRule rule ,Object value)
    {
        addConditionParam(new ConditionParam(key, rule, value));
        return this;
    }

    public DynamicParams dynamicParams(){
        DynamicParams dynamicParams = new DynamicParams();
        dynamicParams.where(this);
        return dynamicParams;
    }

    public int size() {
        return params.size();
    }

}
