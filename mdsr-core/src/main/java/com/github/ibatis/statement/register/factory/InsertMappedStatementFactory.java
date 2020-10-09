package com.github.ibatis.statement.register.factory;

import com.github.ibatis.statement.base.core.matedata.*;
import com.github.ibatis.statement.base.core.MethodSignature;
import com.github.ibatis.statement.mapper.TableMapper;
import org.apache.ibatis.mapping.SqlSource;

/**
 * @see TableMapper#insert(Object)
 * @see TableMapper#insertSelective(Object)
 * @Author: junjie
 * @Date: 2020/3/6
 */
public class InsertMappedStatementFactory extends AbstractInsertMappedStatementFactory {

    public static final String INSERT_METHOD_NAME = "insert";

    public static final String INSERT_SELECTIVE_METHOD_NAME = "insertSelective";

    @Override
    protected boolean isMatch(MappedStatementMateData mappedStatementMateData)
    {
        MethodSignature methodSignature = mappedStatementMateData.getMapperMethodMateData().getMethodSignature();
        Class<?> entityClass = mappedStatementMateData.getEntityMateData().getEntityClass();
        return super.isMatchMethodSignature(methodSignature ,new MethodSignature(
                int.class ,INSERT_METHOD_NAME ,entityClass))
                || super.isMatchMethodSignature(methodSignature ,new MethodSignature(
                        int.class ,INSERT_SELECTIVE_METHOD_NAME, entityClass));
    }

    @Override
    protected SqlSource sqlSource(MappedStatementMateData mappedStatementMateData) {
        String methodName = mappedStatementMateData.getMapperMethodMateData().getMappedMethod().getName();
        return super.createSqlSource(mappedStatementMateData ,name -> name ,INSERT_SELECTIVE_METHOD_NAME.equals(methodName));
    }
}