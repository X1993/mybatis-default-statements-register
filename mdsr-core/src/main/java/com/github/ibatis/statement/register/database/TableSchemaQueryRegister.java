package com.github.ibatis.statement.register.database;

import org.apache.ibatis.session.SqlSession;
import java.util.Collection;
import java.util.Optional;

/**
 * 表结构查询接口注册器
 * @author X1993
 * @date 2020/2/22
 */
public interface TableSchemaQueryRegister {

    /**
     * 注册表结构查询器工厂
     * @param tableSchemaQueries
     * @return
     */
    void register(TableSchemaQuery ... tableSchemaQueries);

    /**
     * 获取匹配的表结构查询器
     * @param sqlSession
     * @return
     */
    Optional<TableSchemaQuery> getTableSchemaQuery(SqlSession sqlSession);

}
