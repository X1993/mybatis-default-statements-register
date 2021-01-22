package com.github.ibatis.statement.register.factory;

import com.github.ibatis.statement.register.AbstractMappedStatementFactory;
import com.github.ibatis.statement.base.core.MethodSignature;
import com.github.ibatis.statement.base.core.matedata.EntityMateData;
import com.github.ibatis.statement.base.core.matedata.MappedStatementMateData;
import com.github.ibatis.statement.base.logical.LogicalColumnMateData;
import com.github.ibatis.statement.mapper.KeyTableMapper;
import com.github.ibatis.statement.util.reflect.ParameterizedTypeImpl;
import org.apache.ibatis.mapping.*;
import org.apache.ibatis.scripting.xmltags.*;
import java.lang.reflect.Type;
import java.util.*;

/**
 * @see KeyTableMapper#deleteBatchByPrimaryKey(Collection)
 * @see KeyTableMapper#deleteBatchByPrimaryKeyOnPhysical(Collection)
 * @author X1993
 * @date 2020/3/14
 */
public class DeleteBatchByPrimaryKeyMappedStatementFactory extends AbstractMappedStatementFactory
{
    /**
     * 批量删除方法
     */
    public final static String DELETE_BATCH_ON_PHYSICAL = "deleteBatchByPrimaryKey";

    /**
     * 批量物理删除方法
     */
    public final static String PHYSICAL_DELETE_BATCH_METHOD_NAME = "deleteBatchByPrimaryKeyOnPhysical";

    @Override
    protected boolean isMatch(MappedStatementMateData mappedStatementMateData)
    {
        MethodSignature methodSignature = mappedStatementMateData.getMapperMethodMateData().getMethodSignature();
        EntityMateData entityMateData = mappedStatementMateData.getEntityMateData();

        if (entityMateData.getPrimaryKeyCount() < 1) {
            return false;
        }
        ParameterizedTypeImpl parameterizedType = ParameterizedTypeImpl.make(Collection.class,
            new Type[]{entityMateData.getReasonableKeyParameterClass()}, null);

        return super.isMatchMethodSignature(methodSignature ,new MethodSignature(int.class ,
                DELETE_BATCH_ON_PHYSICAL, parameterizedType))
                || super.isMatchMethodSignature(methodSignature ,new MethodSignature(int.class ,
                PHYSICAL_DELETE_BATCH_METHOD_NAME , parameterizedType))
                && entityMateData.getPrimaryKeyCount() > 0;
    }

    @Override
    protected SqlSource sqlSource(MappedStatementMateData mappedStatementMateData)
    {
        EntityMateData entityMateData = mappedStatementMateData.getEntityMateData();

        List<SqlNode> sqlNodes = new LinkedList<>();

        LogicalColumnMateData logicalColumnMateData = entityMateData.getLogicalColumnMateData();
        SqlCommandType sqlCommandType = sqlCommandType(mappedStatementMateData);
        boolean logicalDelete = sqlCommandType == SqlCommandType.UPDATE;

        sqlNodes.add(entityMateData.deleteSqlNodeNoWhere(logicalDelete));

        sqlNodes.add(new StaticTextSqlNode(" WHERE "));
        sqlNodes.add(entityMateData.multivaluedKeyConditionSqlNode());

        //默认过滤条件
        StringBuilder fixedValueConditions = entityMateData.defaultConditionsContent(
                sqlCommandType ,content -> content.insert(0 ," AND "));

        if (logicalDelete){
            //逻辑存在条件
            fixedValueConditions.append(" AND ").append(logicalColumnMateData.equalSqlContent(true));
        }

        sqlNodes.add(new StaticTextSqlNode(fixedValueConditions.toString()));

        return new DynamicSqlSource(mappedStatementMateData.getConfiguration() ,new MixedSqlNode(sqlNodes));
    }

    @Override
    protected SqlCommandType sqlCommandType(MappedStatementMateData mappedStatementMateData) {
        if (DELETE_BATCH_ON_PHYSICAL.equals(mappedStatementMateData.getMapperMethodMateData().getMappedMethod()
                .getName()) && mappedStatementMateData.getEntityMateData().getLogicalColumnMateData() != null)
        {//逻辑删除
            return SqlCommandType.UPDATE;
        }else {
            return SqlCommandType.DELETE;
        }
    }
}
