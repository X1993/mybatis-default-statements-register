package com.github.ibatis.statement.register.mysql.factory;

import com.github.ibatis.statement.base.core.MethodSignature;
import com.github.ibatis.statement.base.core.matedata.EntityMateData;
import com.github.ibatis.statement.base.core.matedata.MappedStatementMateData;
import com.github.ibatis.statement.register.AbstractMappedStatementFactory;
import com.github.ibatis.statement.register.mysql.AdapterProperties;
import org.apache.ibatis.builder.StaticSqlSource;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.mapping.SqlSource;
import static com.github.ibatis.statement.mapper.method.MapperMethodEnum.SELECT_MAX_PRIMARY_KEY;

/**
 * @Author: X1993
 * @Date: 2020/12/18
 */
public class SelectMaxKeyStatementFactory extends AbstractMappedStatementFactory {

    @Override
    protected boolean isMatch(MappedStatementMateData mappedStatementMateData)
    {
        if (!AdapterProperties.matchDatabase(mappedStatementMateData.getEntityMateData())){
            return false;
        }

        MethodSignature methodSignature = mappedStatementMateData.getMapperMethodMateData().getMethodSignature();
        EntityMateData entityMateData = mappedStatementMateData.getEntityMateData();

        return entityMateData.getPrimaryKeyCount() > 0
                && methodSignature.isMatch(SELECT_MAX_PRIMARY_KEY.methodSignature(entityMateData));
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
