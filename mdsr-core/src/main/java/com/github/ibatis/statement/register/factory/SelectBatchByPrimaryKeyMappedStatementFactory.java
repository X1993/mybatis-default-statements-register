package com.github.ibatis.statement.register.factory;

import com.github.ibatis.statement.base.core.MethodSignature;
import com.github.ibatis.statement.base.core.matedata.EntityMateData;
import com.github.ibatis.statement.base.core.matedata.MappedStatementMateData;
import com.github.ibatis.statement.base.logical.LogicalColumnMateData;
import com.github.ibatis.statement.util.reflect.ParameterizedTypeImpl;
import com.github.ibatis.statement.mapper.KeyTableMapper;
import org.apache.ibatis.mapping.SqlSource;
import org.apache.ibatis.scripting.xmltags.*;
import java.lang.reflect.Type;
import java.util.*;

/**
 * @see KeyTableMapper#countByPrimaryKeys(Collection)
 * @see KeyTableMapper#countByPrimaryKeysOnPhysical(Collection)
 * @see KeyTableMapper#selectBatchByPrimaryKey(Collection)
 * @see KeyTableMapper#selectBatchByPrimaryKeyOnPhysical(Collection)
 * @Author: X1993
 * @Date: 2020/12/29
 */
public class SelectBatchByPrimaryKeyMappedStatementFactory extends AbstractSelectMappedStatementFactory{

    /**
     * 批量删除方法
     */
    public final static String COUNT_BY_PRIMARY_KEYS = "countByPrimaryKeys";

    /**
     * 批量物理删除方法
     */
    public final static String COUNT_BY_PRIMARY_KEYS_ON_PHYSICAL = "countByPrimaryKeysOnPhysical";

    /**
     * 批量删除方法
     */
    public final static String SELECT_BATCH_BY_PRIMARY_KEY = "selectBatchByPrimaryKey";

    /**
     * 批量物理删除方法
     */
    public final static String SELECT_BATCH_BY_PRIMARY_KEY_ON_PHYSICAL = "selectBatchByPrimaryKeyOnPhysical";

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

        ParameterizedTypeImpl returnType = ParameterizedTypeImpl.make(List.class,
                new Type[]{entityMateData.getEntityClass()}, null);

        return super.isMatchMethodSignature(methodSignature ,new MethodSignature(int.class ,
                COUNT_BY_PRIMARY_KEYS, parameterizedType))
                || super.isMatchMethodSignature(methodSignature ,new MethodSignature(int.class ,
                COUNT_BY_PRIMARY_KEYS_ON_PHYSICAL, parameterizedType))
                || super.isMatchMethodSignature(methodSignature ,new MethodSignature(returnType ,
                SELECT_BATCH_BY_PRIMARY_KEY, parameterizedType))
                || super.isMatchMethodSignature(methodSignature ,new MethodSignature(returnType ,
                SELECT_BATCH_BY_PRIMARY_KEY_ON_PHYSICAL , parameterizedType));
    }

    @Override
    protected SqlSource sqlSource(MappedStatementMateData mappedStatementMateData)
    {
        String methodName = mappedStatementMateData.getMapperMethodMateData().getMappedMethod().getName();
        EntityMateData entityMateData = mappedStatementMateData.getEntityMateData();
        LogicalColumnMateData logicalColumnMateData = entityMateData.getLogicalColumnMateData();

        boolean logicalSelect = !methodName.endsWith("Physical") && logicalColumnMateData != null;
        boolean selectEntity = methodName.startsWith("select");

        List<SqlNode> sqlNodes = new LinkedList<>();

        StringBuilder sqlContext = new StringBuilder("SELECT ")
                .append(selectEntity ? entityMateData.getBaseColumnListSqlContent().toString() : "COUNT(0) ")
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

}

