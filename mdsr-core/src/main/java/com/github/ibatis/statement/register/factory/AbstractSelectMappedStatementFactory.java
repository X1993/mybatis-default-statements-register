package com.github.ibatis.statement.register.factory;

import com.github.ibatis.statement.register.AbstractMappedStatementFactory;
import com.github.ibatis.statement.base.core.matedata.MappedStatementMateData;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ResultMap;
import org.apache.ibatis.mapping.SqlCommandType;
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
                .resultMaps(Arrays.asList(mappedStatementMateData.resultMapsByReturnType()))
                .useCache(true)
                .cache(mappedStatementMateData.getCacheRef());
    }

    /**
     * 定于{@link ResultMap}
     * @param mappedStatementMateData
     * @return
     */
    protected ResultMap resultMaps(MappedStatementMateData mappedStatementMateData){
        return mappedStatementMateData.resultMapsByReturnType();
    }

    @Override
    protected SqlCommandType sqlCommandType(MappedStatementMateData mappedStatementMateData) {
        return SqlCommandType.SELECT;
    }

}
