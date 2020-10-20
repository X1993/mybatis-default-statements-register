package com.github.ibatis.statement.register;

import com.github.ibatis.statement.base.core.parse.*;
import com.github.ibatis.statement.base.core.matedata.EntityMateData;
import com.github.ibatis.statement.base.core.matedata.MappedStatementMateData;
import com.github.ibatis.statement.base.core.matedata.RootMapperMethodMateData;
import com.github.ibatis.statement.mapper.KeyParameterType;
import com.github.ibatis.statement.register.factory.*;
import com.github.ibatis.statement.mapper.EntityType;
import com.github.ibatis.statement.register.factory.DeleteSelectiveMappedStatementFactory;
import com.github.ibatis.statement.util.Sorter;
import org.apache.ibatis.mapping.*;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.lang.reflect.Method;
import java.text.MessageFormat;
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

    private List<Listener> listeners = Collections.EMPTY_LIST;

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
            return;
        }

        EntityMateData entityMateData = entityMateDataParser.parse(mapperEntityClass, sqlSession)
                .orElseThrow(() -> new IllegalArgumentException(MessageFormat.format(
                        "unable parse EntityMateData from mapper [{0}] entity class [{1}]" ,
                        mapperClass ,mapperEntityClass)));

        if (KeyParameterType.class.isAssignableFrom(mapperClass) && entityMateData.getPrimaryKeyCount() <= 0){
            throw new IllegalArgumentException(MessageFormat.format("mapper [{0}] implement [{1}] ," +
                    "but mapper entity mapping table [{2}] no primary key" ,
                    mapperClass ,KeyParameterType.class ,entityMateData.getTableName()));
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
                    for (Listener listener : listeners) {
                        listener.mappedStatementRegister(mappedStatement);
                    }
                    break;
                }
            }

            if (mappedStatement == null){
                LOGGER.warn("can't build mappedStatement for [{}]" ,mappedStatementId);
                for (Listener listener : listeners) {
                    listener.cannotRegisterMappedStatement(mappedStatementMateData);
                }
            }
        }
    }

    /**
     * @see DefaultStatementAutoRegister#registerDefaultMappedStatement(SqlSession, Class) 方法执行监听器
     */
    public interface Listener extends Sorter{

        /**
         * 成功注册{@link MappedStatement}
         * @param mappedStatement
         */
        default void mappedStatementRegister(MappedStatement mappedStatement){}

        /**
         * 不能注册缺失{@link MappedStatement}
         * @param mappedStatementMateData
         */
        default void cannotRegisterMappedStatement(MappedStatementMateData mappedStatementMateData){}

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

    public List<Listener> getListeners() {
        return listeners;
    }

    public void setListeners(List<Listener> listeners) {
        Collections.sort(listeners);
        this.listeners = listeners;
    }

    /**
     * 默认构造器
     */
    public static class Builder
    {
        private EntityMateDataParser entityMateDataParser;

        private MapperEntityParser mapperEntityParser;

        private List<Listener> listeners;

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

        public Builder setListeners(List<Listener> listeners) {
            this.listeners = listeners;
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

            if (listeners != null) {
                statementAutoRegister.setListeners(listeners);
            }

            return statementAutoRegister;
        }

    }

}
