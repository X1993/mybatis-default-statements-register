package com.github.ibatis.statement.register.factory.common;

import com.github.ibatis.statement.base.core.MethodSignature;
import com.github.ibatis.statement.base.core.matedata.ColumnPropertyMapping;
import com.github.ibatis.statement.base.core.matedata.EntityMateData;
import com.github.ibatis.statement.base.core.matedata.MappedStatementMateData;
import com.github.ibatis.statement.base.core.matedata.PropertyMateData;
import com.github.ibatis.statement.base.logical.LogicalColumnMateData;
import com.github.ibatis.statement.mapper.method.MapperMethodEnum;
import com.github.ibatis.statement.register.AbstractMappedStatementFactory;
import org.apache.ibatis.builder.StaticSqlSource;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.mapping.SqlSource;
import org.apache.ibatis.session.Configuration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @Author: X1993
 * @Date: 2020/3/9
 */
public class DeleteByPrimaryKeyMappedStatementFactory extends AbstractMappedStatementFactory
{

    @Override
    protected boolean isMatch(MappedStatementMateData mappedStatementMateData)
    {
        MethodSignature methodSignature = mappedStatementMateData.getMapperMethodMateData().getMethodSignature();
        EntityMateData entityMateData = mappedStatementMateData.getEntityMateData();

        if (entityMateData.getPrimaryKeyCount() < 1) {
            return false;
        }

        return methodSignature.isMatch(MapperMethodEnum.DELETE_BY_PRIMARY_KEY.methodSignature(entityMateData))
                || methodSignature.isMatch(MapperMethodEnum.DELETE_BY_PRIMARY_KEY_ON_PHYSICAL.methodSignature(entityMateData));
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
        if (MapperMethodEnum.DELETE_BY_PRIMARY_KEY.methodName().equals(
                mappedStatementMateData.getMapperMethodMateData().getMappedMethod().getName())
                && mappedStatementMateData.getEntityMateData().getLogicalColumnMateData() != null)
        {//逻辑删除
            return SqlCommandType.UPDATE;
        }else {
            return SqlCommandType.DELETE;
        }
    }

}
