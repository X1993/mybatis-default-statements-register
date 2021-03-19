package com.github.ibatis.statement.base.core.parse;

import com.github.ibatis.statement.mapper.EntityType;
import com.github.ibatis.statement.util.TypeUtils;
import lombok.Data;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * @Author: X1993
 * @Date: 2020/2/21
 */
@Data
public class DefaultMapperEntityParser implements MapperEntityParser {

    /**
     * 扩展：自定义解析规则
     */
    private List<MapperEntityParser> customParsers = Collections.EMPTY_LIST;

    public DefaultMapperEntityParser() {
    }

    public DefaultMapperEntityParser(List<MapperEntityParser> customParsers) {
        this.setCustomParsers(customParsers);
    }

    @Override
    public final Optional<Class<?>> parse(Class<?> mapperClass)
    {
        Type variableType = TypeUtils.parseSuperTypeVariable(mapperClass, EntityType.class.getTypeParameters()[0]);
        if (variableType != null){
            if (variableType instanceof Class) {
                return Optional.of((Class<?>) variableType);
            }else {
                return Optional.empty();
            }
        }

        //如果默认解析失败再尝试自定义解析
        for (MapperEntityParser customParser : customParsers) {
            Optional<Class<?>> optional = customParser.parse(mapperClass);
            if (optional.isPresent()) {
                return optional;
            }
        }

        return Optional.empty();
    }
}
