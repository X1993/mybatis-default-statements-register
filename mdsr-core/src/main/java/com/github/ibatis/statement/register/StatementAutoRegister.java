package com.github.ibatis.statement.register;

import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSession;
import java.util.Collection;

/**
 * 缺省的{@link MappedStatement}注册器
 * @Author: X1993
 * @Date: 2020/2/21
 */
public interface StatementAutoRegister {

    /**
     * 为{@link Configuration}中扫描的所有mapper自动注册缺省{@link MappedStatement}
     * @param sqlSession
     */
    void registerDefaultMappedStatement(SqlSession sqlSession);

    /**
     * 为指定mapper自动注册缺省{@link MappedStatement}
     * @param sqlSession
     * @param mapperClass
     */
    void registerDefaultMappedStatement(SqlSession sqlSession, Class<?> mapperClass);

    /**
     * 注册生成默认{@link MappedStatement}的工厂
     * @param mappedStatementFactory
     * @return
     */
    void addMappedStatementFactory(MappedStatementFactory mappedStatementFactory);

    /**
     * 注册生成默认{@link MappedStatement}的工厂
     * @param mappedStatementFactories
     * @return
     */
    void addMappedStatementFactories(Collection<MappedStatementFactory> mappedStatementFactories);

}
