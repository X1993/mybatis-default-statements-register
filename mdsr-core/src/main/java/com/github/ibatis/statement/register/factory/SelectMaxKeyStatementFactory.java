package com.github.ibatis.statement.register.factory;

import com.github.ibatis.statement.base.core.MethodSignature;
import com.github.ibatis.statement.base.core.matedata.EntityMateData;
import com.github.ibatis.statement.base.core.matedata.MappedStatementMateData;
import com.github.ibatis.statement.mapper.KeyTableMapper;
import com.github.ibatis.statement.register.AbstractMappedStatementFactory;
import org.apache.ibatis.builder.StaticSqlSource;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.mapping.SqlSource;

/**
 * @see KeyTableMapper#selectMaxPrimaryKey()
 * @Author: X1993
 * @Date: 2020/12/18
 */
public class SelectMaxKeyStatementFactory extends AbstractMappedStatementFactory {

    public static final String SELECT_MAX_PRIMARY_KEY = "selectMaxPrimaryKey";

    @Override
    protected boolean isMatch(MappedStatementMateData mappedStatementMateData)
    {
        MethodSignature methodSignature = mappedStatementMateData.getMapperMethodMateData().getMethodSignature();
        EntityMateData entityMateData = mappedStatementMateData.getEntityMateData();

        return entityMateData.getPrimaryKeyCount() > 0 && methodSignature.isMatch(
                new MethodSignature(entityMateData.getReasonableKeyParameterClass() , SELECT_MAX_PRIMARY_KEY));
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

    @Override
    protected SqlCommandType sqlCommandType(MappedStatementMateData mappedStatementMateData) {
        return SqlCommandType.SELECT;
    }

}
