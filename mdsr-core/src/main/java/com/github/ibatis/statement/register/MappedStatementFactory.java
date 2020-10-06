package com.github.ibatis.statement.register;

import com.github.ibatis.statement.base.core.matedata.MappedStatementMateData;
import com.github.ibatis.statement.util.Sorter;
import org.apache.ibatis.mapping.MappedStatement;
import java.util.Optional;

/**
 * {@link MappedStatement}构建工厂
 * @Author: junjie
 * @Date: 2020/2/21
 */
public interface MappedStatementFactory extends Sorter {

    /**
     * 尝试构建
     * @param mappedStatementMateData
     * @return
     * @throws Exception
     */
    Optional<MappedStatement> tryBuild(MappedStatementMateData mappedStatementMateData);

}
