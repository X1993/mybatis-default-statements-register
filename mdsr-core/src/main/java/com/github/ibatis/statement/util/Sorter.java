package com.github.ibatis.statement.util;

/**
 * 排序接口
 * @Author: X1993
 * @Date: 2020/2/21
 */
public interface Sorter extends Comparable<Sorter>{

    /**
     * 排序值，值越小优先级越高
     * @return
     */
    default int order(){
        return 100;
    }

    /**
     * 比较规则
     * @param o
     * @return
     */
    @Override
    default int compareTo(Sorter o){
        return this.order() - o.order();
    }

}
