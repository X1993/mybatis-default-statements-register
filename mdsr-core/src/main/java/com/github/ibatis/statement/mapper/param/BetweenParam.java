package com.github.ibatis.statement.mapper.param;

import java.util.Objects;

/**
 * @Author: junjie
 * @Date: 2020/9/1
 */
public class BetweenParam<T> {

    private T minVal;

    private T maxVal;

    public BetweenParam(T minVal, T maxVal) {
        Objects.requireNonNull(minVal ,"BetweenParam#minVal is null");
        Objects.requireNonNull(maxVal ,"BetweenParam#maxVal is null");
        this.minVal = minVal;
        this.maxVal = maxVal;
    }

    public T getMinVal() {
        return minVal;
    }

    public void setMinVal(T minVal) {
        this.minVal = minVal;
    }

    public T getMaxVal() {
        return maxVal;
    }

    public void setMaxVal(T maxVal) {
        this.maxVal = maxVal;
    }
}
