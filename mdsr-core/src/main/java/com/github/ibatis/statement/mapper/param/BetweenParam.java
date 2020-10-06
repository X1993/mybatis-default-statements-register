package com.github.ibatis.statement.mapper.param;

import java.util.Objects;

/**
 * @Author: junjie
 * @Date: 2020/9/1
 */
public class BetweenParam {

    private Object minVal;

    private Object maxVal;

    public BetweenParam(Object minVal, Object maxVal) {
        Objects.requireNonNull(minVal ,"BetweenParam#minVal is null");
        Objects.requireNonNull(maxVal ,"BetweenParam#maxVal is null");
        this.minVal = minVal;
        this.maxVal = maxVal;
    }

    public Object getMinVal() {
        return minVal;
    }

    public void setMinVal(Object minVal) {
        this.minVal = minVal;
    }

    public Object getMaxVal() {
        return maxVal;
    }

    public void setMaxVal(Object maxVal) {
        this.maxVal = maxVal;
    }
}
