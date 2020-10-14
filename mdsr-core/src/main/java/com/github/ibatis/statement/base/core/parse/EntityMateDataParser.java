package com.github.ibatis.statement.base.core.parse;

import com.github.ibatis.statement.base.core.matedata.EntityMateData;
import com.github.ibatis.statement.util.Sorter;
import org.apache.ibatis.session.SqlSession;
import java.util.Optional;

/**
 * 实体类元数据解析器
 * @Author: junjie
 * @Date: 2020/2/24
 */
public interface EntityMateDataParser extends Sorter {

    /**
     * 解析
     * @param entityClazz 实体类
     * @param sqlSession
     * @return
     */
    Optional<EntityMateData> parse(Class<?> entityClazz, SqlSession sqlSession);

}
