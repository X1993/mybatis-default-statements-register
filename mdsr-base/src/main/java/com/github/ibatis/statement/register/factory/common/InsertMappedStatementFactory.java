package com.github.ibatis.statement.register.factory.common;

import com.github.ibatis.statement.base.core.MethodSignature;
import com.github.ibatis.statement.base.core.matedata.EntityMateData;
import com.github.ibatis.statement.base.core.matedata.MappedStatementMateData;
import com.github.ibatis.statement.mapper.method.MapperMethodEnum;
import com.github.ibatis.statement.register.AbstractMappedStatementFactory;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.mapping.SqlSource;

/**
 * @Author: X1993
 * @Date: 2020/3/6
 */
public class InsertMappedStatementFactory extends AbstractMappedStatementFactory {

    @Override
    protected boolean isMatch(MappedStatementMateData mappedStatementMateData)
    {
        MethodSignature methodSignature = mappedStatementMateData.getMapperMethodMateData().getMethodSignature();
        EntityMateData entityMateData = mappedStatementMateData.getEntityMateData();
        return methodSignature.isMatch(MapperMethodEnum.INSERT.methodSignature(entityMateData))
                || methodSignature.isMatch(MapperMethodEnum.INSERT_SELECTIVE.methodSignature(entityMateData));
    }

    @Override
    protected SqlSource sqlSource(MappedStatementMateData mappedStatementMateData) {
        String methodName = mappedStatementMateData.getMapperMethodMateData().getMappedMethod().getName();
        return mappedStatementMateData.insertSqlSource(name -> name ,
                MapperMethodEnum.INSERT_SELECTIVE.methodName().equals(methodName));
    }

    @Override
    protected SqlCommandType sqlCommandType(MappedStatementMateData mappedStatementMateData) {
        return SqlCommandType.INSERT;
    }

}
