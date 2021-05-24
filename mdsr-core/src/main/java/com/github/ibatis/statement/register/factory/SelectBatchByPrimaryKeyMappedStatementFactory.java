package com.github.ibatis.statement.register.factory;

import com.github.ibatis.statement.base.core.MethodSignature;
import com.github.ibatis.statement.base.core.matedata.ColumnPropertyMapping;
import com.github.ibatis.statement.base.core.matedata.EntityMateData;
import com.github.ibatis.statement.base.core.matedata.MappedStatementMateData;
import com.github.ibatis.statement.base.logical.LogicalColumnMateData;
import com.github.ibatis.statement.register.AbstractMappedStatementFactory;
import com.github.ibatis.statement.util.reflect.ParameterizedTypeImpl;
import com.github.ibatis.statement.mapper.KeyTableMapper;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.mapping.SqlSource;
import org.apache.ibatis.scripting.xmltags.*;
import java.lang.reflect.Type;
import java.util.*;

/**
 * @see KeyTableMapper#countByPrimaryKeys(Collection)
 * @see KeyTableMapper#countByPrimaryKeysOnPhysical(Collection)
 * @see KeyTableMapper#selectBatchByPrimaryKey(Collection)
 * @see KeyTableMapper#selectBatchByPrimaryKeyOnPhysical(Collection)
 * @see KeyTableMapper#getExistPrimaryKeys(Collection)
 * @see KeyTableMapper#getExistPrimaryKeysOnPhysical(Collection)
 * @Author: X1993
 * @Date: 2020/12/29
 */
public class SelectBatchByPrimaryKeyMappedStatementFactory extends AbstractMappedStatementFactory {

    public final static String COUNT_BY_PRIMARY_KEYS = "countByPrimaryKeys";

    public final static String COUNT_BY_PRIMARY_KEYS_ON_PHYSICAL = "countByPrimaryKeysOnPhysical";

    public final static String SELECT_BATCH_BY_PRIMARY_KEY = "selectBatchByPrimaryKey";

    public final static String SELECT_BATCH_BY_PRIMARY_KEY_ON_PHYSICAL = "selectBatchByPrimaryKeyOnPhysical";

    public final static String GET_EXIST_PRIMARY_KEYS = "getExistPrimaryKeys";

    public final static String GET_EXIST_PRIMARY_KEYS_ON_PHYSICAL = "getExistPrimaryKeysOnPhysical";

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

        ParameterizedTypeImpl returnEntityListType = ParameterizedTypeImpl.make(List.class,
                new Type[]{entityMateData.getEntityClass()}, null);

        ParameterizedTypeImpl returnKeySetType = ParameterizedTypeImpl.make(Set.class,
                new Type[]{entityMateData.getReasonableKeyParameterClass()}, null);

        return methodSignature.isMatch(new MethodSignature(int.class ,
                COUNT_BY_PRIMARY_KEYS, parameterizedType))
                || methodSignature.isMatch(new MethodSignature(int.class ,
                COUNT_BY_PRIMARY_KEYS_ON_PHYSICAL, parameterizedType))
                || methodSignature.isMatch(new MethodSignature(returnEntityListType ,
                SELECT_BATCH_BY_PRIMARY_KEY, parameterizedType))
                || methodSignature.isMatch(new MethodSignature(returnEntityListType ,
                SELECT_BATCH_BY_PRIMARY_KEY_ON_PHYSICAL , parameterizedType))
                || methodSignature.isMatch(new MethodSignature(returnKeySetType ,
                GET_EXIST_PRIMARY_KEYS, parameterizedType))
                || methodSignature.isMatch(new MethodSignature(returnKeySetType ,
                GET_EXIST_PRIMARY_KEYS_ON_PHYSICAL, parameterizedType));
    }

    @Override
    protected SqlSource sqlSource(MappedStatementMateData mappedStatementMateData)
    {
        String methodName = mappedStatementMateData.getMapperMethodMateData().getMappedMethod().getName();
        EntityMateData entityMateData = mappedStatementMateData.getEntityMateData();
        LogicalColumnMateData logicalColumnMateData = entityMateData.getLogicalColumnMateData();

        boolean logicalSelect = !methodName.endsWith("Physical") && logicalColumnMateData != null;

        String selectColumnSql = null;
        if (COUNT_BY_PRIMARY_KEYS.equals(methodName) || COUNT_BY_PRIMARY_KEYS_ON_PHYSICAL.equals(methodName)){
            selectColumnSql = "COUNT(0) ";
        } else if (GET_EXIST_PRIMARY_KEYS.equals(methodName) || GET_EXIST_PRIMARY_KEYS_ON_PHYSICAL.equals(methodName)){
            selectColumnSql = entityMateData.getKeyPrimaryColumnPropertyMappings()
                    .values()
                    .stream()
                    .map(ColumnPropertyMapping::getEscapeColumnName)
                    .reduce((keyCol1 ,keyCol2) -> keyCol1 + "," + keyCol2)
                    .get();
        } else {
            selectColumnSql = entityMateData.getBaseColumnListSqlContent().toString();
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

