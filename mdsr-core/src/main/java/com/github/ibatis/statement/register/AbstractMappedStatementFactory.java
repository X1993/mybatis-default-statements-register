package com.github.ibatis.statement.register;

import com.github.ibatis.statement.base.core.matedata.MappedStatementMateData;
import org.apache.ibatis.mapping.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.*;

/**
 * 模板类
 * @Author: X1993
 * @Date: 2020/3/5
 */
public abstract class AbstractMappedStatementFactory implements MappedStatementFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractMappedStatementFactory.class);

    @Override
    public Optional<MappedStatement> tryBuild(MappedStatementMateData mappedStatementMateData)
    {
        return Optional.of(mappedStatementMateData)
                .filter(mateData -> isMatch(mateData))
                .map(mateData -> getBuilder(mateData).build());
    }

    /**
     * 默认MappedStatement.Builder，子类重写
     * @param mateData
     * @return
     */
    protected MappedStatement.Builder getBuilder(MappedStatementMateData mateData)
    {
        return mateData.mappedStatementBuilder(sqlSource(mateData) ,sqlCommandType(mateData));
    }

    /**
     * 判断接口方法是否支持构建{@link MappedStatement}
     * @param mappedStatementMateData 元数据
     * @return
     */
    protected abstract boolean isMatch(MappedStatementMateData mappedStatementMateData);

    /**
     * sql源
     * @param mappedStatementMateData
     * @return
     */
    protected abstract SqlSource sqlSource(MappedStatementMateData mappedStatementMateData);

    /**
     * 命令类型
     * @param mappedStatementMateData
     * @return
     */
    protected abstract SqlCommandType sqlCommandType(MappedStatementMateData mappedStatementMateData);

}
