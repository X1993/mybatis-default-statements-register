package com.github.spring.boot.mdsr;

import com.github.ibatis.statement.base.condition.ColumnConditionParser;
import com.github.ibatis.statement.base.condition.DefaultColumnConditionParser;
import com.github.ibatis.statement.base.core.ExpressionParser;
import com.github.ibatis.statement.base.core.TableSchemaResolutionStrategy;
import com.github.ibatis.statement.base.dv.ColumnValueParser;
import com.github.ibatis.statement.base.dv.DefaultColumnValueParser;
import com.github.ibatis.statement.base.logical.DefaultLogicalColumnMateDataParser;
import com.github.ibatis.statement.base.logical.LogicalColumnMateDataParser;
import com.github.ibatis.statement.register.DefaultStatementAutoRegister;
import com.github.ibatis.statement.register.MappedStatementFactory;
import com.github.ibatis.statement.register.StatementAutoRegister;
import com.github.ibatis.statement.register.schema.*;
import com.github.ibatis.statement.base.core.parse.*;
import com.github.ibatis.statement.register.schema.TableSchemaQueryRegister;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * @Author: X1993
 * @Date: 2020/4/15
 */
@EnableConfigurationProperties(MappedStatementProperties.class)
@Configuration
public class DefaultStatementConfiguration implements ApplicationContextAware{

    @Autowired
    private MappedStatementProperties mappedStatementProperties;

    @Autowired
    private ApplicationContext applicationContext;

    @Bean
    @Primary
    public MapperEntityParser mapperEntityParser(
            @Autowired(required = false) List<MapperEntityParser> mapperEntityParsers)
    {
        DefaultMapperEntityParser defaultMapperEntityParser = new DefaultMapperEntityParser();
        if (mapperEntityParsers != null && mapperEntityParsers.size() > 0) {
            defaultMapperEntityParser.setCustomParsers(mapperEntityParsers);
        }
        return defaultMapperEntityParser;
    }

    @Bean
    @Primary
    public TableSourceParser tableNameParser(@Autowired(required = false) List<TableSourceParser> tableSourceParsers)
    {
        DefaultTableSourceParser defaultTableNameParser = new DefaultTableSourceParser();
        if (tableSourceParsers != null && tableSourceParsers.size() > 0) {
            defaultTableNameParser.setCustomParsers(tableSourceParsers);
        }
        defaultTableNameParser.setDefaultMappingTable(mappedStatementProperties.isDefaultMappingTable());
        getBean(mappedStatementProperties.getTableNameFunctionClass())
                .ifPresent(fun -> defaultTableNameParser.setDefaultTableNameFunction(fun));
        return defaultTableNameParser;
    }

    @Bean
    @Primary
    public LogicalColumnMateDataParser logicalColumnMateDataParser(
            @Autowired(required = false) List<LogicalColumnMateDataParser> logicalColumnMateDataParsers)
    {
        DefaultLogicalColumnMateDataParser defaultLogicalColumnMateDataParser = new DefaultLogicalColumnMateDataParser();
        if (logicalColumnMateDataParsers != null && logicalColumnMateDataParsers.size() > 0){
            defaultLogicalColumnMateDataParser.setCustomParsers(logicalColumnMateDataParsers);
        }
        return defaultLogicalColumnMateDataParser;
    }

    @Bean
    public TryMappingEveryPropertyMateDataParser tryMappingEveryPropertyMateDataParser()
    {
        TryMappingEveryPropertyMateDataParser propertyMateDataParser = new TryMappingEveryPropertyMateDataParser();
        propertyMateDataParser.setEachPropertyMappingColumn(mappedStatementProperties.isEachPropertyMappingColumn());
        getBean(mappedStatementProperties.getColumnNameFunctionClass()).ifPresent(fun -> propertyMateDataParser.setDefaultNameFunction(fun));
        return propertyMateDataParser;
    }

    @Bean
    @Primary
    public PropertyMateDataParser propertyMateDataParser(
            @Autowired(required = false) List<PropertyMateDataParser> propertyMateDataParsers)
    {
        DefaultPropertyMateDataParser propertyMateDataParser = new DefaultPropertyMateDataParser();
        getBean(mappedStatementProperties.getColumnNameFunctionClass())
                .ifPresent(fun -> propertyMateDataParser.setDefaultNameFunction(fun));

        if (propertyMateDataParsers != null && propertyMateDataParsers.size() > 0) {
            propertyMateDataParser.setCustomParsers(propertyMateDataParsers);
        }
        return propertyMateDataParser;
    }

    private <T> Optional<T> getBean(Class<? extends T> clazz)
    {
        T t = null;
        if (clazz != null){
            t = Stream.of(applicationContext
                    .getBeanNamesForType(clazz))
                    .findFirst()
                    .map(beanName -> applicationContext.getBean(beanName, clazz))
                    .orElse(null);
            if (t == null){
                t = BeanUtils.instantiate(clazz);
            }
        }
        return Optional.ofNullable(t);
    }

    @Bean
    @Primary
    public ColumnValueParser columnValueParser(
            @Autowired(required = false) ExpressionParser expressionParser,
            @Autowired(required = false) List<ColumnValueParser> columnValueParsers)
    {
        DefaultColumnValueParser defaultColumnValueParser = new DefaultColumnValueParser();
        if (columnValueParsers != null && columnValueParsers.size() > 0){
            defaultColumnValueParser.setCustomParsers(columnValueParsers);
        }
        if (expressionParser != null){
            defaultColumnValueParser.setExpressionParser(expressionParser);
        }
        return defaultColumnValueParser;
    }

    @Bean
    @Primary
    public ColumnConditionParser columnConditionParser(
            @Autowired(required = false) ExpressionParser expressionParser,
            @Autowired(required = false) List<ColumnConditionParser> columnConditionParsers)
    {
        DefaultColumnConditionParser defaultColumnConditionParser = new DefaultColumnConditionParser();
        if (columnConditionParsers != null && columnConditionParsers.size() > 0){
            defaultColumnConditionParser.setCustomParsers(columnConditionParsers);
        }
        if (expressionParser != null){
            defaultColumnConditionParser.setExpressionParser(expressionParser);
        }
        return defaultColumnConditionParser;
    }

    @Bean
    @ConditionalOnMissingBean(value = TableSchemaQueryRegister.class)
    public DefaultTableSchemaQueryRegister defaultTableSchemaQueryRegister(
            @Autowired(required = false) List<TableSchemaQuery> tableSchemaQueries)
    {
        DefaultTableSchemaQueryRegister defaultTableSchemaQueryRegister = new DefaultTableSchemaQueryRegister();
        if (tableSchemaQueries != null && tableSchemaQueries.size() > 0){
            defaultTableSchemaQueryRegister.register(tableSchemaQueries.toArray(new TableSchemaQuery[tableSchemaQueries.size()]));
        }
        return defaultTableSchemaQueryRegister;
    }

    @Bean
    @ConditionalOnMissingBean(value = EntityMateDataParser.class)
    public DefaultEntityMateDataParser defaultEntityMateDataParser(
            @Autowired TableSourceParser tableSourceParser,
            @Autowired LogicalColumnMateDataParser logicalColumnMateDataParser,
            @Autowired PropertyMateDataParser propertyMateDataParser,
            @Autowired ColumnConditionParser columnConditionParser,
            @Autowired ColumnValueParser columnValueParser,
            @Autowired TableSchemaQueryRegister tableSchemaQueryRegister)
    {
        DefaultEntityMateDataParser.Builder builder = new DefaultEntityMateDataParser.Builder()
                .setColumnConditionParser(columnConditionParser)
                .setColumnValueParser(columnValueParser)
                .setLogicalColumnMateDataParser(logicalColumnMateDataParser)
                .setPropertyMateDataParser(propertyMateDataParser)
                .setTableSchemaQueryRegister(tableSchemaQueryRegister)
                .setTableSourceParser(tableSourceParser);

        TableSchemaResolutionStrategy strategy = mappedStatementProperties.getTableSchemaResolutionStrategy();
        if (strategy != null && !TableSchemaResolutionStrategy.GLOBAL.equals(strategy)){
            builder.setTableSchemaResolutionStrategy(strategy);
        }

        return builder.build();
    }

    @Bean
    @ConditionalOnMissingBean(value = StatementAutoRegister.class)
    public DefaultStatementAutoRegister defaultStatementAutoRegister(
            MapperEntityParser defaultMapperEntityParser ,
            EntityMateDataParser defaultEntityMateDataParser ,
            @Autowired(required = false) List<DefaultStatementAutoRegister.Listener> listeners,
            @Autowired(required = false) List<MappedStatementFactory> mappedStatementFactories)
    {
        DefaultStatementAutoRegister.Builder builder = new DefaultStatementAutoRegister.Builder()
                .setEntityMateDataParser(defaultEntityMateDataParser)
                .setMapperEntityParser(defaultMapperEntityParser);

        if (mappedStatementFactories != null) {
            for (MappedStatementFactory mappedStatementFactory : mappedStatementFactories) {
                builder.addMappedStatementFactory(mappedStatementFactory);
            }
        }

        if (mappedStatementProperties.isAddDefaultListeners()) {
            builder.addDefaultListeners();
        }

        if (listeners != null) {
            builder.addListeners(listeners);
        }

        return builder.build();
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

}
