package com.github.mdsr.sample.env;

import com.github.ibatis.statement.base.core.MethodSignature;
import com.github.ibatis.statement.base.core.matedata.EntityMateData;
import com.github.ibatis.statement.base.core.matedata.MappedStatementMateData;
import com.github.ibatis.statement.base.core.matedata.TableMateData;
import com.github.ibatis.statement.register.factory.AbstractSelectMappedStatementFactory;
import org.apache.ibatis.builder.StaticSqlSource;
import org.apache.ibatis.mapping.ResultMap;
import org.apache.ibatis.mapping.SqlSource;
import org.springframework.stereotype.Component;
import java.util.Collections;

/**
 * @author junjie
 * @date 2020/9/27
 */
@Component
public class SelectMaxIdMappedStatementFactory extends AbstractSelectMappedStatementFactory {

    @Override
    protected boolean isMatch(MappedStatementMateData mappedStatementMateData)
    {
        MethodSignature methodSignature = mappedStatementMateData.getMapperMethodMateData().getMethodSignature();
        EntityMateData entityMateData = mappedStatementMateData.getEntityMateData();

        if (entityMateData.getPrimaryKeyCount() != 1) {
            return false;
        }

        return super.isMatchMethodSignature(methodSignature ,new MethodSignature(
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
    protected ResultMap resultMaps(MappedStatementMateData mappedStatementMateData) {
        return new ResultMap.Builder(mappedStatementMateData.getConfiguration(),
                mappedStatementMateData.getMapperMethodMateData().getMappedStatementId() + "-ResultMap",
                Integer.class,
                Collections.EMPTY_LIST,
                null).build();
    }
}
