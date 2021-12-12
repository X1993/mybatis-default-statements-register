package com.github.ibatis.statement.register;

import com.github.ibatis.statement.base.core.matedata.MapperMethodMateData;
import com.github.ibatis.statement.base.core.parse.*;
import com.github.ibatis.statement.base.core.matedata.EntityMateData;
import com.github.ibatis.statement.base.core.matedata.MappedStatementMateData;
import com.github.ibatis.statement.mapper.EntityType;
import com.github.ibatis.statement.register.listener.KeyParameterMapperCheckListener;
import com.github.ibatis.statement.register.listener.TableMapperCheckListener;
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
 * @Author: X1993
 * @Date: 2020/2/21
 */
public class DefaultStatementAutoRegister implements StatementAutoRegister {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultStatementAutoRegister.class);

    private final List<MappedStatementFactory> mappedStatementFactories = new ArrayList<>();

    private MapperEntityParser mapperEntityParser;

    private EntityMateDataParser entityMateDataParser;

    private List<Listener> listeners = Collections.EMPTY_LIST;

    public DefaultStatementAutoRegister() {
        this(new DefaultMapperEntityParser() ,new DefaultEntityMateDataParser() ,true);
    }

    public DefaultStatementAutoRegister(MapperEntityParser mapperEntityParser,
                                        EntityMateDataParser entityMateDataParser,
                                        boolean registerBySPI)
    {
        Objects.requireNonNull(mapperEntityParser);
        Objects.requireNonNull(entityMateDataParser);
        this.mapperEntityParser = mapperEntityParser;
        this.entityMateDataParser = entityMateDataParser;
        if (registerBySPI) {
            loadMappedStatementFactoriesBySPI();
        }
    }

    private final void loadMappedStatementFactoriesBySPI(){
        ServiceLoader<MappedStatementFactory> serviceLoader = ServiceLoader.load(MappedStatementFactory.class);
        for (MappedStatementFactory mappedStatementFactory : serviceLoader) {
            addMappedStatementFactory(mappedStatementFactory);
        }
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

        if (mapperEntityClass.isInterface() || mapperEntityClass.isArray()
                || mapperEntityClass.isPrimitive() || mapperEntityClass.isEnum()){
            throw new IllegalArgumentException(MessageFormat.format("mapper [{0}] entity class [{1}] illegal" ,
                    mapperClass ,mapperEntityClass));
        }

        EntityMateData entityMateData = entityMateDataParser.parse(mapperEntityClass, sqlSession)
                .orElseThrow(() -> new IllegalArgumentException(MessageFormat.format(
                        "unable parse EntityMateData from mapper [{0}] entity class [{1}]" ,
                        mapperClass ,mapperEntityClass)));

        for (Listener listener : listeners) {
            listener.verify(entityMateData ,mapperClass);
        }

        Configuration configuration = sqlSession.getConfiguration();
        Collection<String> mappedStatementNames = new ArrayList<>(configuration.getMappedStatementNames());
        for (Method method : mapperClass.getMethods())
        {
            MapperMethodMateData statementMateData = new MapperMethodMateData(method, mapperClass);
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
                try {
                    mappedStatement = mappedStatementFactory.tryBuild(mappedStatementMateData).orElse(null);
                } catch (Exception e) {
                    LOGGER.error("{} try build mapped statement exception" ,mappedStatementFactory ,e);
                    continue;
                }

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
                    listener.registerMappedStatementFail(mappedStatementMateData);
                }
            }
        }
    }

    /**
     * @see DefaultStatementAutoRegister#registerDefaultMappedStatement(SqlSession, Class) 方法执行监听器
     */
    public interface Listener extends Sorter {

        /**
         * 校验mapper接口与定义的实体类是否兼容
         * @param entityMateData
         * @param mapperClass
         */
        default void verify(EntityMateData entityMateData ,Class mapperClass){}

        /**
         * 成功注册{@link MappedStatement}
         * @param mappedStatement
         */
        default void mappedStatementRegister(MappedStatement mappedStatement){}

        /**
         * 注册{@link MappedStatement}失败
         * @param mappedStatementMateData
         */
        default void registerMappedStatementFail(MappedStatementMateData mappedStatementMateData){}

    }

    @Override
    public void addMappedStatementFactory(MappedStatementFactory mappedStatementFactory) {
        if (mappedStatementFactory == null) {
            return;
        }
        int addIndex = mappedStatementFactories.size();
        for (int i = 0; i < mappedStatementFactories.size(); i++) {
            MappedStatementFactory factory = mappedStatementFactories.get(i);
            if (factory.compareTo(mappedStatementFactory) > 0){
                addIndex = i;
                break;
            }
        }
        mappedStatementFactories.add(addIndex ,mappedStatementFactory);
    }

    @Override
    public void addMappedStatementFactories(Collection<MappedStatementFactory> mappedStatementFactories)
    {
        if (mappedStatementFactories != null && mappedStatementFactories.size() >= 0){
            for (MappedStatementFactory mappedStatementFactory : mappedStatementFactories) {
                if (mappedStatementFactory != null){
                    addMappedStatementFactory(mappedStatementFactory);
                }
            }
        }
    }

    public List<MappedStatementFactory> getMappedStatementFactories() {
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

        private List<Listener> listeners = new ArrayList<>();

        private List<MappedStatementFactory> mappedStatementFactories = new ArrayList<>();

        private boolean registerBySPI = true;

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

        public Builder addListeners(Collection<Listener> listeners) {
            this.listeners.addAll(listeners);
            return this;
        }

        public Builder addListeners(Listener ... listeners){
            for (Listener listener : listeners) {
                this.listeners.add(listener);
            }
            return this;
        }

        public Builder addDefaultListeners(){
            this.addListeners(new KeyParameterMapperCheckListener() ,new TableMapperCheckListener());
            return this;
        }

        public Builder addMappedStatementFactory(MappedStatementFactory mappedStatementFactory)
        {
            mappedStatementFactories.add(mappedStatementFactory);
            return this;
        }

        public Builder registerBySPI(boolean registerBySPI){
            this.registerBySPI = registerBySPI;
            return this;
        }

        public DefaultStatementAutoRegister build()
        {
            DefaultStatementAutoRegister statementAutoRegister = new DefaultStatementAutoRegister(
                    mapperEntityParser == null ? new DefaultMapperEntityParser() : mapperEntityParser,
                    entityMateDataParser == null ? new DefaultEntityMateDataParser() : entityMateDataParser ,
                    registerBySPI);

            statementAutoRegister.addMappedStatementFactories(mappedStatementFactories);

            if (listeners != null) {
                statementAutoRegister.setListeners(listeners);
            }

            return statementAutoRegister;
        }

    }

}
