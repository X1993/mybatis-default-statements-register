package com.github.ibatis.statement.register.database;

import org.apache.ibatis.session.SqlSession;
import java.util.Collection;
import java.util.Optional;

/**
 * 表结构查询接口注册器
 * @author junjie
 * @date 2020/2/22
 */
public interface TableSchemaQueryRegister {

    /**
     * 注册表结构查询器工厂
     * @param tableSchemaQueries
     * @return
     */
    void register(Collection<TableSchemaQuery> tableSchemaQueries);

    /**
     * 注册表结构查询器工厂
     * @param tableSchemaQuery
     * @return
     */
    void register(TableSchemaQuery tableSchemaQuery);

    /**
     * 获取匹配的表结构查询器
     * @param sqlSession
     * @return
     */
    Optional<TableSchemaQuery> getTableSchemaQuery(SqlSession sqlSession);

}
