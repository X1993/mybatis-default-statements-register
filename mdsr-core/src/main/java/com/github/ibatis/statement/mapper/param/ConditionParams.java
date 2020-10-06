package com.github.ibatis.statement.mapper.param;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @Author: junjie
 * @Date: 2020/9/1
 */
public class ConditionParams {

    /**
     * 过滤条件
     */
    private final List<ConditionParam> params = new ArrayList<>();

    private boolean or;

    private boolean defaultAllowEmpty;

    /**
     * 两次调用之间的{@link ConditionParams#params}在构建sql时每个条件使用 or 连接，最外层嵌套括号
     * @return
     */
    public ConditionParams or(){
        this.or = !or;
        if (!this.or && params.size() > 0){
            params.get(params.size() - 1).setOr(false);
        }
        return this;
    }

    public ConditionParams eq(String key ,Object value ,boolean allowEmpty){
        if (value != null || allowEmpty) {
            addConditionParam(key,ConditionRule.EQ,value);
        }
        return this;
    }

    public ConditionParams eq(String key ,Object value){
        return eq(key,value ,defaultAllowEmpty);
    }

    public ConditionParams notEq(String key ,Object value ,boolean allowEmpty){
        if (value != null || allowEmpty) {
            addConditionParam(key,ConditionRule.NOT_EQ,value);
        }
        return this;
    }

    public ConditionParams notEq(String key ,Object value){
        return notEq(key,value ,defaultAllowEmpty);
    }

    public ConditionParams lt(String key ,Object value ,boolean allowEmpty){
        if (value != null || allowEmpty) {
            addConditionParam(key,ConditionRule.LT,value);
        }
        return this;
    }

    public ConditionParams lt(String key ,Object value){
        return lt(key, value, defaultAllowEmpty);
    }

    public ConditionParams gt(String key ,Object value ,boolean allowEmpty){
        if (value != null || allowEmpty) {
            addConditionParam(key,ConditionRule.GT,value);
        }
        return this;
    }

    public ConditionParams gt(String key ,Object value){
        return gt(key,value,defaultAllowEmpty);
    }

    public ConditionParams ltEq(String key ,Object value ,boolean allowEmpty){
        if (value != null || allowEmpty) {
            addConditionParam(key,ConditionRule.LE,value);
        }
        return this;
    }

    public ConditionParams ltEq(String key ,Object value){
        return ltEq(key,value, defaultAllowEmpty);
    }

    public ConditionParams gtEq(String key ,Object value,boolean allowEmpty){
        if (value != null || allowEmpty) {
            addConditionParam(key,ConditionRule.GE,value);
        }
        return this;
    }

    public ConditionParams gtEq(String key ,Object value){
        return gtEq(key,value ,defaultAllowEmpty);
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

    public ConditionParams notIn(String key ,Object ... values){
        return this.notIn(key , Arrays.asList(values));
    }

    public ConditionParams notIn(String key ,Iterable iterable)
    {
        if (iterable != null) {
            List list = new ArrayList<>();
            for (Object value : iterable) {
                if (value != null){
                    list.add(value);
                }
            }
            if (list.size() > 0) {
                this.params.add(new ConditionParam(key, ConditionRule.NOT_IN, list));
            }
        }
        return this;
    }

    public ConditionParams in(String key ,Iterable iterable)
    {
        if (iterable != null) {
            List list = new ArrayList<>();
            for (Object value : iterable) {
                if (value != null){
                    list.add(value);
                }
            }
            if (list.size() > 0) {
                this.params.add(new ConditionParam(key, ConditionRule.IN ,list));
            }
        }
        return this;
    }

    public ConditionParams in(String key ,Object ... values)
    {
        return this.in(key ,Arrays.asList(values));
    }

    public ConditionParams isNull(String key){
        this.addConditionParam(key ,ConditionRule.ISNULL ,null);
        return this;
    }

    public ConditionParams notNull(String key){
        this.addConditionParam(key ,ConditionRule.NOT_NULL ,null);
        return this;
    }

    private ConditionParams addConditionParam(ConditionParam conditionParam)
    {
        conditionParam.setOr(or);
        this.params.add(conditionParam);
        return this;
    }

    private ConditionParams addConditionParam(String key ,ConditionRule rule ,Object value)
    {
        addConditionParam(new ConditionParam(key,rule,value));
        return this;
    }

    public ConditionParams setDefaultAllowEmpty(boolean defaultAllowEmpty) {
        this.defaultAllowEmpty = defaultAllowEmpty;
        return this;
    }

    public boolean isDefaultAllowEmpty() {
        return defaultAllowEmpty;
    }

    public List<ConditionParam> getParams() {
        return params;
    }

    public DynamicParams dynamicParams(){
        DynamicParams dynamicParams = new DynamicParams();
        dynamicParams.where(this);
        return dynamicParams;
    }

}
