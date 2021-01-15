package com.github.ibatis.statement.base.core.matedata;

import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSession;

/**
 * 构建{@link org.apache.ibatis.mapping.MappedStatement}的元数据
 * @Author: junjie
 * @Date: 2020/3/5
 */
public class MappedStatementMateData implements Cloneable{

    private EntityMateData entityMateData;

    private final MapperMethodMateData mapperMethodMateData;

    private final SqlSession sqlSession;

    public MappedStatementMateData(EntityMateData entityMateData,
                                   MapperMethodMateData mapperMethodMateData,
                                   SqlSession sqlSession)
    {
        this.entityMateData = entityMateData;
        this.mapperMethodMateData = mapperMethodMateData;
        this.sqlSession = sqlSession;
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    public EntityMateData getEntityMateData() {
        return entityMateData;
    }

    public MapperMethodMateData getMapperMethodMateData() {
        return mapperMethodMateData;
    }

    public SqlSession getSqlSession() {
        return sqlSession;
    }

    public Configuration getConfiguration() {
        return sqlSession.getConfiguration();
    }

    @Override
    public String toString() {
        return "MappedStatementMateData{" +
                "mapperMethodMateData=" + mapperMethodMateData +
                '}';
    }
}
