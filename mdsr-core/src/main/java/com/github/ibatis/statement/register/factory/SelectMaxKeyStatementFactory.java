package com.github.ibatis.statement.register.factory;

import com.github.ibatis.statement.base.core.MethodSignature;
import com.github.ibatis.statement.base.core.matedata.EntityMateData;
import com.github.ibatis.statement.base.core.matedata.MappedStatementMateData;
import com.github.ibatis.statement.mapper.KeyTableMapper;
import org.apache.ibatis.builder.StaticSqlSource;
import org.apache.ibatis.mapping.SqlSource;

/**
 * @see KeyTableMapper#selectMaxKey()
 * @Author: junjie
 * @Date: 2020/12/18
 */
public class SelectMaxKeyStatementFactory extends AbstractSelectMappedStatementFactory {

    public static final String SELECT_MAX_KEY = "selectMaxKey";

    @Override
    protected boolean isMatch(MappedStatementMateData mappedStatementMateData)
    {
        MethodSignature methodSignature = mappedStatementMateData.getMapperMethodMateData().getMethodSignature();
        EntityMateData entityMateData = mappedStatementMateData.getEntityMateData();

        return entityMateData.getPrimaryKeyCount() > 0 && super.isMatchMethodSignature(methodSignature ,
                new MethodSignature(entityMateData.getReasonableKeyParameterClass() ,SELECT_MAX_KEY));
    }

    @Override
    protected SqlSource sqlSource(MappedStatementMateData mappedStatementMateData)
    {
        EntityMateData entityMateData = mappedStatementMateData.getEntityMateData();
        String keyColumns = entityMateData.getTableMateData().getKeyColumnUsages()
                .stream()
                .sorted()
                .map(keyColumnUsage -> "`" + keyColumnUsage.getColumnName() + "`")
                .reduce((column1 ,column2) -> column1 + "," + column2)
                .get();

        StringBuilder sqlContext = new StringBuilder("SELECT ")
                .append(keyColumns)
                .append(" FROM `")
                .append(entityMateData.getTableName())
                .append("` ORDER BY ")
                .append(keyColumns)
                .append(" DESC LIMIT 1");

        return new StaticSqlSource(mappedStatementMateData.getConfiguration() ,sqlContext.toString());
    }

}
