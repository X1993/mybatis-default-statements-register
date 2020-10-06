package com.github.ibatis.statement.mapper.param;

/**
 * limit参数
 * @author junjie
 * @date 2020/08/31
 */
public class LimitParam {

    /**
     * 起始下标（从0开始）
     */
    private int index;

    /**
     * 页大小
     */
    private int size;

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

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }
}
