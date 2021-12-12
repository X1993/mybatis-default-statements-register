package com.github.ibatis.statement.register.factory.common;

import com.github.ibatis.statement.base.core.MethodSignature;
import com.github.ibatis.statement.base.core.matedata.EntityMateData;
import com.github.ibatis.statement.base.core.matedata.MappedStatementMateData;
import com.github.ibatis.statement.register.AbstractMappedStatementFactory;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.mapping.SqlSource;
import static com.github.ibatis.statement.mapper.method.MapperMethodEnum.*;

/**
 * @Author: X1993
 * @Date: 2020/3/6
 */
public class UpdateMappedStatementFactory extends AbstractMappedStatementFactory {

    @Override
    protected boolean isMatch(MappedStatementMateData mappedStatementMateData)
    {
        MethodSignature methodSignature = mappedStatementMateData.getMapperMethodMateData().getMethodSignature();
        EntityMateData entityMateData = mappedStatementMateData.getEntityMateData();

        if (entityMateData.getPrimaryKeyCount() < 1) {
            return false;
        }

        return methodSignature.isMatch(UPDATE_BY_PRIMARY_KEY.methodSignature(entityMateData))
                || methodSignature.isMatch(UPDATE_BY_PRIMARY_KEY_SELECTIVE.methodSignature(entityMateData));
    }

    @Override
    protected SqlSource sqlSource(MappedStatementMateData mappedStatementMateData)
    {
        String methodName = mappedStatementMateData.getMapperMethodMateData().getMappedMethod().getName();
        return mappedStatementMateData.updateSqlSource(name -> name ,
                UPDATE_BY_PRIMARY_KEY_SELECTIVE.methodName().equals(methodName));
    }

    @Override
    protected SqlCommandType sqlCommandType(MappedStatementMateData mappedStatementMateData) {
        return SqlCommandType.UPDATE;
    }

}
