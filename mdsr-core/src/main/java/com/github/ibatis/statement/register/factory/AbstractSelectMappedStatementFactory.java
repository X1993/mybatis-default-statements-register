package com.github.ibatis.statement.register.factory;

import com.github.ibatis.statement.base.core.matedata.MapperMethodMateData;
import com.github.ibatis.statement.register.AbstractMappedStatementFactory;
import com.github.ibatis.statement.base.core.matedata.MappedStatementMateData;
import com.github.ibatis.statement.util.TypeUtils;
import com.github.ibatis.statement.util.reflect.ParameterizedTypeImpl;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.mapping.ResultMap;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Type;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

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

    /**
     * 定于{@link ResultMap}
     * @param mappedStatementMateData
     * @return
     */
    protected ResultMap resultMaps(MappedStatementMateData mappedStatementMateData)
    {
        MapperMethodMateData mapperMethodMateData = mappedStatementMateData.getMapperMethodMateData();
        Type genericReturnType = mapperMethodMateData.getMethodSignature().getGenericReturnType();
        Class<?> returnType = mapperMethodMateData.getMappedMethod().getReturnType();

        Class<?> entityClass = mappedStatementMateData.getEntityMateData().getEntityClass();
        if (TypeUtils.isAssignableFrom(entityClass ,genericReturnType) || TypeUtils.isAssignableFrom(
                ParameterizedTypeImpl.make(Collection.class ,new Type[]{entityClass} ,null) ,genericReturnType)
                || (returnType.isArray() && TypeUtils.isAssignableFrom(entityClass ,returnType.getComponentType()))
                || (genericReturnType instanceof GenericArrayType && TypeUtils.isAssignableFrom(entityClass ,
                ((GenericArrayType) genericReturnType).getGenericComponentType()))) {
            return mappedStatementMateData.getDefaultMappingResultMap();
        }else if (genericReturnType instanceof Class){
            return new ResultMap.Builder(
                    mappedStatementMateData.getConfiguration(),
                    mappedStatementMateData.getMapperMethodMateData().getMappedStatementId() + "-ResultMap",
                    (Class<?>) genericReturnType,
                    Collections.EMPTY_LIST,
                    null).build();
        }

        throw new IllegalStateException(MessageFormat.format("Cannot analyze ResultMapper by method " +
                "return type [{0}]" ,genericReturnType));
    }

    @Override
    protected SqlCommandType sqlCommandType(MappedStatementMateData mappedStatementMateData) {
        return SqlCommandType.SELECT;
    }

}
