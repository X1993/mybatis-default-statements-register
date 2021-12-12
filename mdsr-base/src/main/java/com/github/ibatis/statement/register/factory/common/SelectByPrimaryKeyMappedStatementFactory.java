package com.github.ibatis.statement.register.factory.common;

import com.github.ibatis.statement.base.core.MethodSignature;
import com.github.ibatis.statement.base.core.matedata.ColumnPropertyMapping;
import com.github.ibatis.statement.base.core.matedata.EntityMateData;
import com.github.ibatis.statement.base.core.matedata.MappedStatementMateData;
import com.github.ibatis.statement.base.logical.LogicalColumnMateData;
import com.github.ibatis.statement.register.AbstractMappedStatementFactory;
import org.apache.ibatis.builder.StaticSqlSource;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.mapping.SqlSource;
import org.apache.ibatis.session.Configuration;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import static com.github.ibatis.statement.mapper.method.MapperMethodEnum.*;

/**
 * @author X1993
 * @date 2020/2/22
 */
public class SelectByPrimaryKeyMappedStatementFactory extends AbstractMappedStatementFactory {

    @Override
    protected boolean isMatch(MappedStatementMateData mappedStatementMateData)
    {
        MethodSignature methodSignature = mappedStatementMateData.getMapperMethodMateData().getMethodSignature();
        EntityMateData entityMateData = mappedStatementMateData.getEntityMateData();

        if (entityMateData.getPrimaryKeyCount() < 1) {
            return false;
        }

        return methodSignature.isMatch(SELECT_BY_PRIMARY_KEY.methodSignature(entityMateData))
                || methodSignature.isMatch(SELECT_BY_PRIMARY_KEY_ON_PHYSICAL.methodSignature(entityMateData))
                || methodSignature.isMatch(EXIST_BY_PRIMARY_KEY.methodSignature(entityMateData))
                || methodSignature.isMatch(EXIST_BY_PRIMARY_KEY_ON_PHYSICAL.methodSignature(entityMateData));
    }

    @Override
    protected SqlSource sqlSource(MappedStatementMateData mappedStatementMateData)
    {
        EntityMateData entityMateData = mappedStatementMateData.getEntityMateData();
        String methodName = mappedStatementMateData.getMapperMethodMateData().getMappedMethod().getName();

        boolean logicalConditional = SELECT_BY_PRIMARY_KEY.methodName().equals(methodName)
                || EXIST_BY_PRIMARY_KEY.methodName().equals(methodName);
        boolean selectEntity = SELECT_BY_PRIMARY_KEY.methodName().equals(methodName)
                || SELECT_BY_PRIMARY_KEY_ON_PHYSICAL.methodName().equals(methodName);

        Configuration configuration = mappedStatementMateData.getConfiguration();

        StringBuilder sqlContext = new StringBuilder("SELECT ")
                .append(selectEntity ? entityMateData.getBaseColumnListSqlContent() : "COUNT(0)")
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

        return new StaticSqlSource(mappedStatementMateData.getConfiguration() ,sqlContext.toString() ,parameterMappings);
    }

    @Override
    protected SqlCommandType sqlCommandType(MappedStatementMateData mappedStatementMateData) {
        return SqlCommandType.SELECT;
    }

}
