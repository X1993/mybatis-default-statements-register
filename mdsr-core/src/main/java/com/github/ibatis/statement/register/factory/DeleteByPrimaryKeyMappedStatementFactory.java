package com.github.ibatis.statement.register.factory;

import com.github.ibatis.statement.register.AbstractMappedStatementFactory;
import com.github.ibatis.statement.base.core.MethodSignature;
import com.github.ibatis.statement.base.core.matedata.ColumnPropertyMapping;
import com.github.ibatis.statement.base.core.matedata.EntityMateData;
import com.github.ibatis.statement.base.core.matedata.MappedStatementMateData;
import com.github.ibatis.statement.base.core.matedata.PropertyMateData;
import com.github.ibatis.statement.base.logical.LogicalColumnMateData;
import com.github.ibatis.statement.mapper.KeyTableMapper;
import org.apache.ibatis.builder.StaticSqlSource;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.mapping.SqlSource;
import org.apache.ibatis.session.Configuration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @see KeyTableMapper#deleteByPrimaryKey(Object)
 * @see KeyTableMapper#deleteByPrimaryKeyOnPhysical(Object)
 * @Author: X1993
 * @Date: 2020/3/9
 */
public class DeleteByPrimaryKeyMappedStatementFactory extends AbstractMappedStatementFactory
{
    public final static String DELETE_BY_PRIMARY_KEY = "deleteByPrimaryKey";

    public final static String DELETE_BY_PRIMARY_KEY_ON_PHYSICAL = "deleteByPrimaryKeyOnPhysical";

    @Override
    protected boolean isMatch(MappedStatementMateData mappedStatementMateData)
    {
        MethodSignature methodSignature = mappedStatementMateData.getMapperMethodMateData().getMethodSignature();
        EntityMateData entityMateData = mappedStatementMateData.getEntityMateData();

        if (entityMateData.getPrimaryKeyCount() < 1) {
            return false;
        }
        Class<?> reasonableKeyParameterClass = entityMateData.getReasonableKeyParameterClass();

        return super.isMatchMethodSignature(methodSignature ,new MethodSignature(int.class ,
                DELETE_BY_PRIMARY_KEY,reasonableKeyParameterClass))
                || super.isMatchMethodSignature(methodSignature ,new MethodSignature(int.class ,
                DELETE_BY_PRIMARY_KEY_ON_PHYSICAL,reasonableKeyParameterClass))
                && entityMateData.getPrimaryKeyCount() > 0;
    }

    @Override
    protected SqlSource sqlSource(MappedStatementMateData mappedStatementMateData)
    {
        EntityMateData entityMateData = mappedStatementMateData.getEntityMateData();

        LogicalColumnMateData logicalColumnMateData = entityMateData.getLogicalColumnMateData();
        SqlCommandType sqlCommandType = sqlCommandType(mappedStatementMateData);
        boolean logicalDelete = sqlCommandType == SqlCommandType.UPDATE;

        StringBuilder sqlBuilder = new StringBuilder();
        sqlBuilder.append(entityMateData.deleteSqlContentNoWhere(logicalDelete));

        Map<String ,ColumnPropertyMapping> keyPrimaryColumnPropertyMappings = entityMateData
                .getKeyPrimaryColumnPropertyMappings();

        /*
           where 1 = 1
           and key1 = #{keyPropertyName1,jdbcType=XXX}
           and key2 = #{keyPropertyName2,jdbcType=XXX}
           and col5 = value
           (and logicalCol = true)
         */
        StringBuilder whereConditionContent = new StringBuilder(" WHERE ");
        List<ParameterMapping> parameterMappings = new ArrayList<>();
        Configuration configuration = mappedStatementMateData.getConfiguration();

        //主键的查询条件
        for (ColumnPropertyMapping columnPropertyMapping : keyPrimaryColumnPropertyMappings.values()) {
            PropertyMateData propertyMateData = columnPropertyMapping.getPropertyMateData();
            String columnName = propertyMateData.getMappingColumnName();
            whereConditionContent.append(" `")
                    .append(columnName)
                    .append("` = ? AND ");

            parameterMappings.add(columnPropertyMapping.buildParameterMapping(configuration));
        }

        //默认过滤条件
        whereConditionContent.append(entityMateData.defaultConditionsContent(
                sqlCommandType ,content -> content.append(" AND ")));

        if (logicalDelete){
            //逻辑存在条件
            whereConditionContent.append(logicalColumnMateData.equalSqlContent(true));
        }else {
            whereConditionContent.append(" 1 = 1 ");
        }

        return new StaticSqlSource(mappedStatementMateData.getConfiguration() ,
                sqlBuilder.append(whereConditionContent).toString() ,parameterMappings);
    }

    @Override
    protected SqlCommandType sqlCommandType(MappedStatementMateData mappedStatementMateData) {
        if (DELETE_BY_PRIMARY_KEY.equals(mappedStatementMateData.getMapperMethodMateData().getMappedMethod()
                .getName()) && mappedStatementMateData.getEntityMateData().getLogicalColumnMateData() != null)
        {//逻辑删除
            return SqlCommandType.UPDATE;
        }else {
            return SqlCommandType.DELETE;
        }
    }

}
