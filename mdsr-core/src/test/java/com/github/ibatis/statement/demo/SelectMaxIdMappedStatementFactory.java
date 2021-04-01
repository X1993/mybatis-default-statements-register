package com.github.ibatis.statement.demo;

import com.github.ibatis.statement.base.core.MethodSignature;
import com.github.ibatis.statement.base.core.matedata.EntityMateData;
import com.github.ibatis.statement.base.core.matedata.MappedStatementMateData;
import com.github.ibatis.statement.base.core.matedata.TableMateData;
import com.github.ibatis.statement.register.AbstractMappedStatementFactory;
import org.apache.ibatis.builder.StaticSqlSource;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.mapping.SqlSource;

/**
 * @author X1993
 * @date 2020/9/27
 */
public class SelectMaxIdMappedStatementFactory extends AbstractMappedStatementFactory {

    @Override
    protected boolean isMatch(MappedStatementMateData mappedStatementMateData)
    {
        MethodSignature methodSignature = mappedStatementMateData.getMapperMethodMateData().getMethodSignature();
        EntityMateData entityMateData = mappedStatementMateData.getEntityMateData();

        if (entityMateData.getPrimaryKeyCount() != 1) {
            return false;
        }

        return methodSignature.isMatch(new MethodSignature(
                entityMateData.getReasonableKeyParameterClass() ,"selectMaxKey"));
    }

    @Override
    protected SqlSource sqlSource(MappedStatementMateData mappedStatementMateData) {
        TableMateData tableMateData = mappedStatementMateData.getEntityMateData().getTableMateData();
        String keyName = tableMateData.getKeyColumnMateDataMap().values()
                .stream()
                .findFirst()
                .get()
                .getEscapeColumnName();
        StringBuilder content = new StringBuilder("select ")
                .append(keyName)
                .append(" from ")
                .append(tableMateData.getEscapeTableName()).append(" order by ")
                .append(keyName)
                .append(" desc limit 1");
        return new StaticSqlSource(mappedStatementMateData.getConfiguration() ,content.toString());
    }

    @Override
    protected SqlCommandType sqlCommandType(MappedStatementMateData mappedStatementMateData) {
        return SqlCommandType.SELECT;
    }

}
