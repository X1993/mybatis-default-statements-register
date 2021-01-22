package com.github.ibatis.statement.register.factory;

import com.github.ibatis.statement.base.core.matedata.*;
import com.github.ibatis.statement.base.core.MethodSignature;
import com.github.ibatis.statement.mapper.TableMapper;
import com.github.ibatis.statement.register.AbstractMappedStatementFactory;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.mapping.SqlSource;

/**
 * @see TableMapper#insert(Object)
 * @see TableMapper#insertSelective(Object)
 * @Author: X1993
 * @Date: 2020/3/6
 */
public class InsertMappedStatementFactory extends AbstractMappedStatementFactory {

    public static final String INSERT = "insert";

    public static final String INSERT_SELECTIVE = "insertSelective";

    @Override
    protected boolean isMatch(MappedStatementMateData mappedStatementMateData)
    {
        MethodSignature methodSignature = mappedStatementMateData.getMapperMethodMateData().getMethodSignature();
        Class<?> entityClass = mappedStatementMateData.getEntityMateData().getEntityClass();
        return super.isMatchMethodSignature(methodSignature ,new MethodSignature(
                int.class , INSERT,entityClass))
                || super.isMatchMethodSignature(methodSignature ,new MethodSignature(
                        int.class , INSERT_SELECTIVE, entityClass));
    }

    @Override
    protected SqlSource sqlSource(MappedStatementMateData mappedStatementMateData) {
        String methodName = mappedStatementMateData.getMapperMethodMateData().getMappedMethod().getName();
        return mappedStatementMateData.insertSqlSource(name -> name , INSERT_SELECTIVE.equals(methodName));
    }

    @Override
    protected SqlCommandType sqlCommandType(MappedStatementMateData mappedStatementMateData) {
        return SqlCommandType.INSERT;
    }

}
