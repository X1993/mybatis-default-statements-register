package com.github.ibatis.statement.register.factory.common;

import com.github.ibatis.statement.base.core.MethodSignature;
import com.github.ibatis.statement.base.core.matedata.ColumnPropertyMapping;
import com.github.ibatis.statement.base.core.matedata.EntityMateData;
import com.github.ibatis.statement.base.core.matedata.MappedStatementMateData;
import com.github.ibatis.statement.base.logical.LogicalColumnMateData;
import com.github.ibatis.statement.register.AbstractMappedStatementFactory;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.mapping.SqlSource;
import org.apache.ibatis.scripting.xmltags.DynamicSqlSource;
import org.apache.ibatis.scripting.xmltags.MixedSqlNode;
import org.apache.ibatis.scripting.xmltags.SqlNode;
import org.apache.ibatis.scripting.xmltags.StaticTextSqlNode;
import java.util.LinkedList;
import java.util.List;
import static com.github.ibatis.statement.mapper.method.MapperMethodEnum.*;

/**
 * @Author: X1993
 * @Date: 2020/12/29
 */
public class SelectBatchByPrimaryKeyMappedStatementFactory extends AbstractMappedStatementFactory {

    @Override
    protected boolean isMatch(MappedStatementMateData mappedStatementMateData)
    {
        MethodSignature methodSignature = mappedStatementMateData.getMapperMethodMateData().getMethodSignature();
        EntityMateData entityMateData = mappedStatementMateData.getEntityMateData();

        if (entityMateData.getPrimaryKeyCount() < 1) {
            return false;
        }

        return methodSignature.isMatch(COUNT_BY_PRIMARY_KEYS.methodSignature(entityMateData))
                || methodSignature.isMatch(COUNT_BY_PRIMARY_KEYS_ON_PHYSICAL.methodSignature(entityMateData))
                || methodSignature.isMatch(SELECT_BATCH_BY_PRIMARY_KEY.methodSignature(entityMateData))
                || methodSignature.isMatch(SELECT_BATCH_BY_PRIMARY_KEY_ON_PHYSICAL.methodSignature(entityMateData))
                || methodSignature.isMatch(GET_EXIST_PRIMARY_KEYS.methodSignature(entityMateData))
                || methodSignature.isMatch(GET_EXIST_PRIMARY_KEYS_ON_PHYSICAL.methodSignature(entityMateData));
    }

    @Override
    protected SqlSource sqlSource(MappedStatementMateData mappedStatementMateData)
    {
        String methodName = mappedStatementMateData.getMapperMethodMateData().getMappedMethod().getName();
        EntityMateData entityMateData = mappedStatementMateData.getEntityMateData();
        LogicalColumnMateData logicalColumnMateData = entityMateData.getLogicalColumnMateData();

        boolean logicalSelect = !methodName.endsWith("Physical") && logicalColumnMateData != null;

        String selectColumnSql = null;
        if (COUNT_BY_PRIMARY_KEYS.methodName().equals(methodName)
                || COUNT_BY_PRIMARY_KEYS_ON_PHYSICAL.methodName().equals(methodName)){
            selectColumnSql = "COUNT(0) ";
        } else if (GET_EXIST_PRIMARY_KEYS.methodName().equals(methodName)
                || GET_EXIST_PRIMARY_KEYS_ON_PHYSICAL.methodName().equals(methodName)){
            selectColumnSql = entityMateData.getKeyPrimaryColumnPropertyMappings()
                    .values()
                    .stream()
                    .map(ColumnPropertyMapping::getEscapeColumnName)
                    .reduce((keyCol1 ,keyCol2) -> keyCol1 + "," + keyCol2)
                    .get();
        } else {
            selectColumnSql = entityMateData.getBaseColumnListSqlContent();
        }

        List<SqlNode> sqlNodes = new LinkedList<>();

        StringBuilder sqlContext = new StringBuilder("SELECT ")
                .append(selectColumnSql)
                .append(" FROM `")
                .append(entityMateData.getTableName())
                .append("` WHERE ");

        sqlNodes.add(new StaticTextSqlNode(sqlContext.toString()));
        sqlNodes.add(entityMateData.multivaluedKeyConditionSqlNode());

        //默认过滤条件
        StringBuilder fixedValueConditions = entityMateData.defaultConditionsContent(
                sqlCommandType(mappedStatementMateData) ,content -> content.insert(0 ," AND "));

        if (logicalSelect){
            //逻辑存在条件
            fixedValueConditions.append(" AND ").append(logicalColumnMateData.equalSqlContent(true));
        }

        sqlNodes.add(new StaticTextSqlNode(fixedValueConditions.toString()));

        return new DynamicSqlSource(mappedStatementMateData.getConfiguration() ,new MixedSqlNode(sqlNodes));
    }

    @Override
    protected SqlCommandType sqlCommandType(MappedStatementMateData mappedStatementMateData) {
        return SqlCommandType.SELECT;
    }

}

