package com.github.ibatis.statement;

/**
 * 映射 mybatis SqlMapConfig <environment id="h2">
 * @Author: X1993
 * @Date: 2020/10/21
 */
public enum DataSourceEnvironment {

    H2,

    MYSQL;

    public static DataSourceEnvironment defaultDatabase(){
        return H2;
    }

}
