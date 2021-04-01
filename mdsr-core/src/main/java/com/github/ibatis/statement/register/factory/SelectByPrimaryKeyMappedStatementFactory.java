package com.github.ibatis.statement.register.factory;

import com.github.ibatis.statement.base.core.MethodSignature;
import com.github.ibatis.statement.base.core.matedata.ColumnPropertyMapping;
import com.github.ibatis.statement.base.core.matedata.EntityMateData;
import com.github.ibatis.statement.base.core.matedata.MappedStatementMateData;
import com.github.ibatis.statement.base.logical.LogicalColumnMateData;
import com.github.ibatis.statement.mapper.KeyTableMapper;
import com.github.ibatis.statement.register.AbstractMappedStatementFactory;
import org.apache.ibatis.builder.*;
import org.apache.ibatis.mapping.*;
import org.apache.ibatis.session.Configuration;
import java.util.*;

/**
 * @see KeyTableMapper#selectByPrimaryKey(Object)
 * @see KeyTableMapper#selectByPrimaryKeyOnPhysical(Object)
 * @see KeyTableMapper#existByPrimaryKey(Object)
 * @see KeyTableMapper#existByPrimaryKeyOnPhysical(Object)
 * @author X1993
 * @date 2020/2/22
 */
public class SelectByPrimaryKeyMappedStatementFactory extends AbstractMappedStatementFactory {

    public static final String SELECT_BY_PRIMARY_KEY = "selectByPrimaryKey";

    public static final String SELECT_BY_PRIMARY_KEY_ON_PHYSICAL = "selectByPrimaryKeyOnPhysical";

    public static final String EXIST_BY_PRIMARY_KEY = "existByPrimaryKey";

    public static final String EXIST_BY_PRIMARY_KEY_ON_PHYSICAL = "existByPrimaryKeyOnPhysical";

    @Override
    protected boolean isMatch(MappedStatementMateData mappedStatementMateData)
    {
        MethodSignature methodSignature = mappedStatementMateData.getMapperMethodMateData().getMethodSignature();
        EntityMateData entityMateData = mappedStatementMateData.getEntityMateData();
        Class<?> entityClass = entityMateData.getEntityClass();

        if (entityMateData.getPrimaryKeyCount() < 1) {
            return false;
        }
        Class<?> reasonableKeyParameterClass = entityMateData.getReasonableKeyParameterClass();

        return methodSignature.isMatch(new MethodSignature(entityClass ,SELECT_BY_PRIMARY_KEY, reasonableKeyParameterClass))
                || methodSignature.isMatch(new MethodSignature(entityClass ,SELECT_BY_PRIMARY_KEY_ON_PHYSICAL, reasonableKeyParameterClass))
                || methodSignature.isMatch(new MethodSignature(boolean.class ,EXIST_BY_PRIMARY_KEY, reasonableKeyParameterClass))
                || methodSignature.isMatch(new MethodSignature(boolean.class ,EXIST_BY_PRIMARY_KEY_ON_PHYSICAL, reasonableKeyParameterClass))
                && entityMateData.getPrimaryKeyCount() > 0;
    }

    /**
     * 查询的是实体
     * @param mappedStatementMateData
     * @return
     */
    private boolean selectEntity(MappedStatementMateData mappedStatementMateData){
        String methodName = mappedStatementMateData.getMapperMethodMateData().getMappedMethod().getName();
        return SELECT_BY_PRIMARY_KEY.equals(methodName) || SELECT_BY_PRIMARY_KEY_ON_PHYSICAL.equals(methodName);
    }

    @Override
    protected SqlSource sqlSource(MappedStatementMateData mappedStatementMateData)
    {
        EntityMateData entityMateData = mappedStatementMateData.getEntityMateData();
        String methodName = mappedStatementMateData.getMapperMethodMateData().getMappedMethod().getName();

        boolean logicalConditional = SELECT_BY_PRIMARY_KEY.equals(methodName) || EXIST_BY_PRIMARY_KEY.equals(methodName);
        boolean selectEntity = selectEntity(mappedStatementMateData);

        Configuration configuration = mappedStatementMateData.getConfiguration();

        StringBuilder sqlContext = new StringBuilder("SELECT ")
                .append(selectEntity ? entityMateData.getBaseColumnListSqlContent().toString() : "COUNT(0)")
                .append(" FROM `")
                .append(entityMateData.getTableName())
                .append("` WHERE ");

        List<ParameterMapping> parameterMappings = new LinkedList<>();
        LogicalColumnMateData logicalColumnMateData = entityMateData.getLogicalColumnMateData();
        Map<String, ColumnPropertyMapping> keyPrimaryColumnPropertyMappings = entityMateData.getKeyPrimaryColumnPropertyMappings();

        for (ColumnPropertyMapping columnPropertyMapping : keyPrimaryColumnPropertyMappings.values())
        {
            String columnName = columnPropertyMapping.getColumnMateData().getColumnName();
            sqlContext.append(" `")
                    .append(columnName)
                    .append("` = ? AND ");

            parameterMappings.add(columnPropertyMapping.buildParameterMapping(configuration));
        }

        //默认过滤条件
        sqlContext.append(entityMateData.defaultConditionsContent(
                sqlCommandType(mappedStatementMateData) ,
                content -> content.append(" AND ")));

        if (logicalConditional && logicalColumnMateData != null){
            sqlContext.append(logicalColumnMateData.equalSqlContent(true));
        }else {
            sqlContext.delete(sqlContext.length() - 4,sqlContext.length());
        }

        if (!selectEntity) {
            sqlContext.append(" LIMIT 1");
        }

        return new StaticSqlSource(mappedStatementMateData.getConfiguration() ,sqlContext.toString() ,parameterMappings);
    }

    @Override
    protected SqlCommandType sqlCommandType(MappedStatementMateData mappedStatementMateData) {
        return SqlCommandType.SELECT;
    }

}
