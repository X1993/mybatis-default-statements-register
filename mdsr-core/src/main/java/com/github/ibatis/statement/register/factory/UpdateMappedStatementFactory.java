package com.github.ibatis.statement.register.factory;

import com.github.ibatis.statement.base.core.MethodSignature;
import com.github.ibatis.statement.base.core.matedata.EntityMateData;
import com.github.ibatis.statement.base.core.matedata.MappedStatementMateData;
import com.github.ibatis.statement.mapper.KeyTableMapper;
import com.github.ibatis.statement.register.AbstractMappedStatementFactory;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.mapping.SqlSource;

/**
 * @see  KeyTableMapper#updateByPrimaryKey(Object)
 * @see KeyTableMapper#updateByPrimaryKeySelective(Object)
 * @Author: X1993
 * @Date: 2020/3/6
 */
public class UpdateMappedStatementFactory extends AbstractMappedStatementFactory {

    public static final String UPDATE_BY_PRIMARY_KEY = "updateByPrimaryKey";

    public static final String UPDATE_BY_PRIMARY_KEY_SELECTIVE = "updateByPrimaryKeySelective";

    @Override
    protected boolean isMatch(MappedStatementMateData mappedStatementMateData)
    {
        MethodSignature methodSignature = mappedStatementMateData.getMapperMethodMateData().getMethodSignature();
        EntityMateData entityMateData = mappedStatementMateData.getEntityMateData();
        Class<?> entityClass = entityMateData.getEntityClass();

        return super.isMatchMethodSignature(methodSignature ,new MethodSignature(int.class ,
                UPDATE_BY_PRIMARY_KEY,entityClass))
                || super.isMatchMethodSignature(methodSignature ,new MethodSignature(int.class ,
                UPDATE_BY_PRIMARY_KEY_SELECTIVE,entityClass))
                && entityMateData.getPrimaryKeyCount() > 0;
    }

    @Override
    protected SqlSource sqlSource(MappedStatementMateData mappedStatementMateData)
    {
        String methodName = mappedStatementMateData.getMapperMethodMateData().getMappedMethod().getName();
        return mappedStatementMateData.updateSqlSource(name -> name ,
                UPDATE_BY_PRIMARY_KEY_SELECTIVE.equals(methodName));
    }

    @Override
    protected SqlCommandType sqlCommandType(MappedStatementMateData mappedStatementMateData) {
        return SqlCommandType.UPDATE;
    }

}
