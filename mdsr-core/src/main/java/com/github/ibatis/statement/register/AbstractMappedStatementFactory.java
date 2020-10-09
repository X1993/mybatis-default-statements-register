package com.github.ibatis.statement.register;

import com.github.ibatis.statement.base.core.MethodSignature;
import com.github.ibatis.statement.base.core.matedata.MappedStatementMateData;
import com.github.ibatis.statement.util.TypeUtils;
import org.apache.ibatis.annotations.CacheNamespaceRef;
import org.apache.ibatis.builder.*;
import org.apache.ibatis.cache.Cache;
import org.apache.ibatis.mapping.*;
import org.apache.ibatis.session.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.lang.reflect.Type;
import java.util.*;

/**
 * 模板类
 * @Author: junjie
 * @Date: 2020/3/5
 */
public abstract class AbstractMappedStatementFactory implements MappedStatementFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractMappedStatementFactory.class);

    @Override
    public Optional<MappedStatement> tryBuild(MappedStatementMateData mappedStatementMateData)
    {
        return Optional.of(mappedStatementMateData)
                .filter(mateData -> isMatch(mateData))
                .map(mateData -> buildMappedStatement(mateData));
    }

    /**
     * 构建流程
     */
    private MappedStatement buildMappedStatement(MappedStatementMateData mappedStatementMateData)
    {
        Configuration configuration = mappedStatementMateData.getConfiguration();

        String mappedStatementId =  mappedStatementMateData.getMapperMethodMateData().getMappedStatementId();
        SqlSource sqlSource = sqlSource(mappedStatementMateData);

        return customBuilder(mappedStatementMateData ,new MappedStatement.Builder(configuration,
                mappedStatementId, sqlSource, sqlCommandType(mappedStatementMateData))
                .resource(resource(mappedStatementMateData))
                .statementType(StatementType.PREPARED)
                .databaseId(configuration.getDatabaseId())
                .resultSetType(configuration.getDefaultResultSetType()))
                .build();
    }

    /**
     * 子类自定义
     * @param mappedStatementMateData
     * @param builder
     * @return
     */
    protected MappedStatement.Builder customBuilder(MappedStatementMateData mappedStatementMateData ,
                                                    MappedStatement.Builder builder){
        return builder;
    }

    /**
     * 判断方法是否匹配
     * @param actual 实际定义的方法签名
     * @param defined 定义的匹配方法签名
     * @return
     */
    protected final boolean isMatchMethodSignature(MethodSignature actual ,MethodSignature defined)
    {
        if (actual.getMethodName().equals(defined.getMethodName())){
            if (TypeUtils.isAssignableFrom(actual.getGenericReturnType() ,defined.getGenericReturnType())
                    && actual.getGenericParameterTypes().length == defined.getGenericParameterTypes().length){
                for (int i = 0; i < actual.getGenericParameterTypes().length; i++) {
                    Type actualParameterType = actual.getGenericParameterTypes()[i];
                    Type definedParameterType = defined.getGenericParameterTypes()[i];
                    if (!TypeUtils.isAssignableFrom(definedParameterType ,actualParameterType)){
                        LOGGER.warn("method signature probably should match {}" ,actual.toString());
                        return false;
                    }
                }
                return true;
            }else {
                LOGGER.warn("method signature probably should match {}" ,actual.toString());
            }
        }
        return false;
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

    /**
     * 来源
     * @param mappedStatementMateData
     * @return
     */
    protected String resource(MappedStatementMateData mappedStatementMateData){
        return mappedStatementMateData.getMapperMethodMateData().getMapperClass().getName();
    }

    /**
     * 获取缓存引用
     * @param mappedStatementMateData
     * @return
     */
    protected final Cache getCacheRef(MappedStatementMateData mappedStatementMateData)
    {
        Class<?> type = mappedStatementMateData.getMapperMethodMateData().getMapperClass();
        Cache cache = null;
        CacheNamespaceRef cacheDomainRef = type.getAnnotation(CacheNamespaceRef.class);
        if (cacheDomainRef != null) {
            Class<?> refType = cacheDomainRef.value();
            String refName = cacheDomainRef.name();
            if (refType == void.class && refName.isEmpty()) {
                throw new BuilderException("Should be specified either value() or name() attribute in the @CacheNamespaceRef");
            }
            if (refType != void.class && !refName.isEmpty()) {
                throw new BuilderException("Cannot use both value() and name() attribute in the @CacheNamespaceRef");
            }
            String namespace = (refType != void.class) ? refType.getName() : refName;
            MapperBuilderAssistant assistant = new MapperBuilderAssistant(
                    mappedStatementMateData.getConfiguration(), resource(mappedStatementMateData));
            try {
                cache = assistant.useCacheRef(namespace);
            } catch (IncompleteElementException e) {
                cache = new CacheRefResolver(assistant, namespace).resolveCacheRef();
            }
        }
        return cache;
    }

}