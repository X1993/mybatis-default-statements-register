package com.github.ibatis.statement.register.factory;

import com.github.ibatis.statement.register.AbstractMappedStatementFactory;
import com.github.ibatis.statement.base.core.matedata.MappedStatementMateData;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.mapping.ResultMap;
import java.util.Arrays;

/**
 * @Author: junjie
 * @Date: 2020/3/5
 */
public abstract class AbstractSelectMappedStatementFactory extends AbstractMappedStatementFactory {

    @Override
    protected MappedStatement.Builder customBuilder(MappedStatementMateData mappedStatementMateData,
                                                    MappedStatement.Builder builder)
    {
        return super.customBuilder(mappedStatementMateData, builder)
                .resultMaps(Arrays.asList(resultMaps(mappedStatementMateData)))
                .useCache(true)
                .cache(getCacheRef(mappedStatementMateData));
    }

    protected ResultMap resultMaps(MappedStatementMateData mappedStatementMateData)
    {
        return mappedStatementMateData.getDefaultMappingResultMap();
    }

    @Override
    protected SqlCommandType sqlCommandType(MappedStatementMateData mappedStatementMateData) {
        return SqlCommandType.SELECT;
    }

}
