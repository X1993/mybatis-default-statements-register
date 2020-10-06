package com.github.ibatis.statement.base.core.parse;

import com.github.ibatis.statement.util.Sorter;
import java.util.Optional;

/**
 * Mapper接口支持的实体类解析
 * @Author: junjie
 * @Date: 2020/2/21
 */
public interface MapperEntityParser extends Sorter {

    /**
     * 解析
     * @param mapperClass mapper接口类型
     * @return
     */
    Optional<Class<?>> parse(Class<?> mapperClass);

}
