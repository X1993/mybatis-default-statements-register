package com.github.ibatis.statement.mapper.param;

import lombok.Data;
import java.util.Objects;

/**
 * @Author: junjie
 * @Date: 2020/9/1
 */
@Data
public class BetweenParam<T> {

    private T minVal;

    private T maxVal;

    public BetweenParam(T minVal, T maxVal) {
        Objects.requireNonNull(minVal ,"BetweenParam#minVal is null");
        Objects.requireNonNull(maxVal ,"BetweenParam#maxVal is null");
        this.minVal = minVal;
        this.maxVal = maxVal;
    }

}
