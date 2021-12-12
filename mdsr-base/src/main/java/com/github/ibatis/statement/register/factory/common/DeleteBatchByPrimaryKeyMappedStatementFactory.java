package com.github.ibatis.statement.register.factory.common;

import com.github.ibatis.statement.base.core.MethodSignature;
import com.github.ibatis.statement.base.core.matedata.EntityMateData;
import com.github.ibatis.statement.base.core.matedata.MappedStatementMateData;
import com.github.ibatis.statement.base.logical.LogicalColumnMateData;
import com.github.ibatis.statement.mapper.method.MapperMethodEnum;
import com.github.ibatis.statement.register.AbstractMappedStatementFactory;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.mapping.SqlSource;
import org.apache.ibatis.scripting.xmltags.DynamicSqlSource;
import org.apache.ibatis.scripting.xmltags.MixedSqlNode;
import org.apache.ibatis.scripting.xmltags.SqlNode;
import org.apache.ibatis.scripting.xmltags.StaticTextSqlNode;
import java.util.LinkedList;
import java.util.List;

/**
 * @author X1993
 * @date 2020/3/14
 */
public class DeleteBatchByPrimaryKeyMappedStatementFactory extends AbstractMappedStatementFactory
{

    @Override
    protected boolean isMatch(MappedStatementMateData mappedStatementMateData)
    {
        MethodSignature methodSignature = mappedStatementMateData.getMapperMethodMateData().getMethodSignature();
        EntityMateData entityMateData = mappedStatementMateData.getEntityMateData();

        if (entityMateData.getPrimaryKeyCount() < 1) {
            return false;
        }

        return methodSignature.isMatch(MapperMethodEnum.DELETE_BATCH_ON_PHYSICAL.methodSignature(entityMateData))
                || methodSignature.isMatch(MapperMethodEnum.PHYSICAL_DELETE_BATCH_METHOD_NAME.methodSignature(entityMateData));
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
        if (MapperMethodEnum.DELETE_BATCH_ON_PHYSICAL.methodName().equals(
                mappedStatementMateData.getMapperMethodMateData().getMappedMethod().getName())
                && mappedStatementMateData.getEntityMateData().getLogicalColumnMateData() != null)
        {//逻辑删除
            return SqlCommandType.UPDATE;
        }else {
            return SqlCommandType.DELETE;
        }
    }
}
