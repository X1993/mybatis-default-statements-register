package com.github.ibatis.statement.register;

import com.github.ibatis.statement.base.core.parse.*;
import com.github.ibatis.statement.base.core.matedata.EntityMateData;
import com.github.ibatis.statement.base.core.matedata.MappedStatementMateData;
import com.github.ibatis.statement.base.core.matedata.RootMapperMethodMateData;
import com.github.ibatis.statement.register.factory.*;
import com.github.ibatis.statement.mapper.EntityType;
import com.github.ibatis.statement.register.factory.DeleteSelectiveMappedStatementFactory;
import org.apache.ibatis.mapping.*;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 当mapper接口加载完成后，尝试为实现{@link EntityType}
 * 的mapper接口方法注册默认{@link MappedStatement}，如果没有注册的话
 * @Author: junjie
 * @Date: 2020/2/21
 */
public class DefaultStatementAutoRegister implements StatementAutoRegister {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultStatementAutoRegister.class);

    private final List<MappedStatementFactory> mappedStatementFactories = new ArrayList<>();

    private boolean sortFlag = true;

    private MapperEntityParser mapperEntityParser;

    private EntityMateDataParser entityMateDataParser;

    public DefaultStatementAutoRegister() {
        this(new DefaultMapperEntityParser() ,new DefaultEntityMateDataParser());
    }

    public DefaultStatementAutoRegister(MapperEntityParser mapperEntityParser, EntityMateDataParser entityMateDataParser) {
        this.mapperEntityParser = mapperEntityParser;
        this.entityMateDataParser = entityMateDataParser;
    }

    @Override
    public void registerDefaultMappedStatement(SqlSession sqlSession)
    {
        Collection<Class<?>> mappers = sqlSession.getConfiguration().getMapperRegistry().getMappers()
                .stream()
                .collect(Collectors.toList());

        for (Class<?> mapper : mappers)
        {
            registerDefaultMappedStatement(sqlSession ,mapper);
        }
    }

    @Override
    public void registerDefaultMappedStatement(SqlSession sqlSession, Class<?> mapperClass)
    {
        Class<?> mapperEntityClass = mapperEntityParser.parse(mapperClass).orElse(null);
        if (mapperEntityClass == null){
            LOGGER.debug("can't parse mapper {} mapping entity class ," +
                    "can't auto register default mappedStatement" ,mapperClass);
            return;
        }

        EntityMateData entityMateData = entityMateDataParser.parse(mapperEntityClass, sqlSession).orElse(null);
        if (entityMateData == null){
            LOGGER.warn("can't parse entityMateData for entity class {}" ,mapperEntityClass);
            return;
        }

        Configuration configuration = sqlSession.getConfiguration();
        Collection<String> mappedStatementNames = new ArrayList<>(configuration.getMappedStatementNames());
        for (Method method : mapperClass.getMethods())
        {
            RootMapperMethodMateData statementMateData = new RootMapperMethodMateData(method, mapperClass);
            final String mappedStatementId = statementMateData.getMappedStatementId();
            if (method.isDefault() || method.isBridge() || mappedStatementNames.contains(mappedStatementId)) {
                continue;
            }

            MappedStatementMateData mappedStatementMateData = new MappedStatementMateData(
                    entityMateData, statementMateData, sqlSession);

            MappedStatement mappedStatement = null;
            for (MappedStatementFactory mappedStatementFactory : getMappedStatementFactories())
            {
                //尝试自动注册
                LOGGER.debug("{} try build {}" ,mappedStatementFactory ,mappedStatementId);
                mappedStatement = mappedStatementFactory.tryBuild(mappedStatementMateData).orElse(null);

                if (mappedStatement != null) {
                    configuration.addMappedStatement(mappedStatement);
                    mappedStatementNames.add(mappedStatementId);
                    LOGGER.debug("success register mappedStatement [{}]" ,mappedStatementId);
                    break;
                }
            }

            if (mappedStatement == null){
                LOGGER.warn("can't build mappedStatement for [{}]" ,mappedStatementId);
            }
        }
    }

    @Override
    public void addMappedStatementFactory(MappedStatementFactory mappedStatementFactory) {
        if (mappedStatementFactory == null) {
            return;
        }
        mappedStatementFactories.add(mappedStatementFactory);
        sortFlag = false;
    }

    @Override
    public void addMappedStatementFactories(Collection<MappedStatementFactory> mappedStatementFactories)
    {
        if (mappedStatementFactories != null && mappedStatementFactories.size() >= 0){
            this.mappedStatementFactories.addAll(mappedStatementFactories.stream()
                    .filter(mappedStatementFactory -> mappedStatementFactory != null)
                    .collect(Collectors.toList()));

            sortFlag = false;
        }
    }

    public List<MappedStatementFactory> getMappedStatementFactories() {
        if (!sortFlag){
            Collections.sort(mappedStatementFactories);
            sortFlag = true;
        }
        return mappedStatementFactories;
    }

    /**
     * 默认构造器
     */
    public static class Builder
    {
        private EntityMateDataParser entityMateDataParser;

        private MapperEntityParser mapperEntityParser;

        private List<MappedStatementFactory> mappedStatementFactories = new ArrayList<>();

        public Builder setEntityMateDataParser(EntityMateDataParser entityMateDataParser) {
            Objects.requireNonNull(entityMateDataParser);
            this.entityMateDataParser = entityMateDataParser;
            return this;
        }

        public Builder setMapperEntityParser(MapperEntityParser mapperEntityParser) {
            Objects.requireNonNull(mapperEntityParser);
            this.mapperEntityParser = mapperEntityParser;
            return this;
        }

        public Builder addMappedStatementFactory(MappedStatementFactory mappedStatementFactory)
        {
            mappedStatementFactories.add(mappedStatementFactory);
            return this;
        }

        public Builder addDefaultMappedStatementFactories()
        {
            mappedStatementFactories.add(new SelectByPrimaryKeyMappedStatementFactory());
            mappedStatementFactories.add(new UpdateMappedStatementFactory());
            mappedStatementFactories.add(new InsertMappedStatementFactory());
            mappedStatementFactories.add(new DeleteByPrimaryKeyMappedStatementFactory());
            mappedStatementFactories.add(new SelectSelectiveMappedStatementFactory());
            mappedStatementFactories.add(new DeleteBatchByPrimaryKeyMappedStatementFactory());
            mappedStatementFactories.add(new InsertBatchMappedStatementFactory());
            mappedStatementFactories.add(new UpdateBatchMappedStatementFactory());
            mappedStatementFactories.add(new UpdateSameBatchMappedStatementFactory());
            mappedStatementFactories.add(new DeleteSelectiveMappedStatementFactory());
            mappedStatementFactories.add(new DynamicParamsSelectStatementFactory());
            return this;
        }

        public DefaultStatementAutoRegister build()
        {
            DefaultStatementAutoRegister statementAutoRegister = new DefaultStatementAutoRegister(
                    mapperEntityParser == null ? new DefaultMapperEntityParser() : mapperEntityParser,
                    entityMateDataParser == null ? new DefaultEntityMateDataParser() : entityMateDataParser);

            statementAutoRegister.addMappedStatementFactories(mappedStatementFactories);

            return statementAutoRegister;
        }

    }

}
