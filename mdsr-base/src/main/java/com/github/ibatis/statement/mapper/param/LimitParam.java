package com.github.ibatis.statement.mapper.param;

import lombok.Data;

/**
 * limit参数
 * @author X1993
 * @date 2020/08/31
 */
@Data
public class LimitParam {

    /**
     * 起始下标（从0开始）
     */
    private int index;

    /**
     * 截取大小
     */
    private int size;

    public LimitParam() {
    }

    public LimitParam(int size) {
        this(0 ,size);
    }

    public LimitParam(int index, int size) {
        if (index < 0 || size < 0){
            throw new IllegalArgumentException("param [index ,size] must be positive");
        }
        this.index = index;
        this.size = size;
    }

    /**
     * 分页
     * @param pageIndex 下标从0开始计数
     * @param pageSize 分页大小
     * @return
     */
    public static LimitParam page0(int pageIndex ,int pageSize){
        return new LimitParam((pageIndex > 0 ? pageIndex : 0) * pageSize ,pageSize);
    }

    /**
     * 分页
     * @param pageIndex 下标从1开始计数
     * @param pageSize 分页大小
     * @return
     */
    public static LimitParam page1(int pageIndex ,int pageSize){
        return new LimitParam((pageIndex > 0 ? pageIndex - 1 : 0) * pageSize ,pageSize);
    }

}
