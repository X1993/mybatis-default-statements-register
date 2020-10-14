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
import com.github.ibatis.statement.register.database.DefaultTableSchemaQueryRegister;
import com.github.ibatis.statement.register.database.MysqlTableSchemaQuery;
import com.github.ibatis.statement.base.core.parse.*;
import com.github.ibatis.statement.register.database.TableSchemaQuery;
import com.github.ibatis.statement.register.database.TableSchemaQueryRegister;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
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
 * @Author: junjie
 * @Date: 2020/4/15
 */
@EnableConfigurationProperties(MappedStatementProperties.class)
@Configuration
public class DefaultStatementConfiguration implements ApplicationContextAware{

    @Autowired
    private MappedStatementProperties properties;

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
    public TableSourceParser tableNameParser(@Autowired(required = false) List<TableSourceParser> tableSourceParsers){
        DefaultTableSourceParser defaultTableNameParser = new DefaultTableSourceParser();
        if (tableSourceParsers != null && tableSourceParsers.size() > 0) {
            defaultTableNameParser.setCustomParsers(tableSourceParsers);
        }
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
    @ConditionalOnProperty(matchIfMissing = true ,prefix = MappedStatementProperties.PREFIX ,
            name = "each-property-mapping-column" ,havingValue = "true")
    public TryMappingEveryPropertyMateDataParser tryMappingEveryPropertyMateDataParser()
    {
        final TryMappingEveryPropertyMateDataParser propertyMateDataParser = new TryMappingEveryPropertyMateDataParser();
        this.getPropertyToColumnNameFunction().ifPresent(columnNameFunction ->
                propertyMateDataParser.setDefaultNameFunction(columnNameFunction));
        return propertyMateDataParser;
    }

    @Bean
    @Primary
    public PropertyMateDataParser propertyMateDataParser(
            @Autowired(required = false) List<PropertyMateDataParser> propertyMateDataParsers)
    {
        DefaultPropertyMateDataParser propertyMateDataParser = new DefaultPropertyMateDataParser();
        this.getPropertyToColumnNameFunction().ifPresent(columnNameFunction ->
                propertyMateDataParser.setDefaultNameFunction(columnNameFunction));

        if (propertyMateDataParsers != null && propertyMateDataParsers.size() > 0) {
            propertyMateDataParser.setCustomParsers(propertyMateDataParsers);
        }
        return propertyMateDataParser;
    }

    private Optional<PropertyToColumnNameFunction> getPropertyToColumnNameFunction(){
        Class<? extends PropertyToColumnNameFunction> columnNameFunctionClass = properties.getColumnNameFunctionClass();
        PropertyToColumnNameFunction columnNameFunction = null;
        if (columnNameFunctionClass != null){
            columnNameFunction = Stream.of(applicationContext
                    .getBeanNamesForType(columnNameFunctionClass))
                    .findFirst()
                    .map(beanName -> applicationContext.getBean(beanName, columnNameFunctionClass))
                    .orElse(null);
            if (columnNameFunction == null){
                columnNameFunction = BeanUtils.instantiate(columnNameFunctionClass);
            }
        }
        return Optional.ofNullable(columnNameFunction);
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
    public MysqlTableSchemaQuery mysqlTableMateDataQueryRegister()
    {
        return new MysqlTableSchemaQuery();
    }

    @Bean
    @ConditionalOnMissingBean(value = TableSchemaQueryRegister.class)
    public DefaultTableSchemaQueryRegister defaultTableSchemaQueryRegister(@Autowired(required = false) List<TableSchemaQuery> tableSchemaQueries){
        DefaultTableSchemaQueryRegister defaultTableSchemaQueryRegister = new DefaultTableSchemaQueryRegister();
        if (tableSchemaQueries != null){
            defaultTableSchemaQueryRegister.register(tableSchemaQueries);
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

        TableSchemaResolutionStrategy strategy = properties.getTableSchemaResolutionStrategy();
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

        if (properties.isAddDefaultMappedStatementFactories()){
            builder.addDefaultMappedStatementFactories();
        }

        if (mappedStatementFactories != null) {
            for (MappedStatementFactory mappedStatementFactory : mappedStatementFactories) {
                builder.addMappedStatementFactory(mappedStatementFactory);
            }
        }

        if (listeners != null) {
            builder.setListeners(listeners);
        }

        return builder.build();
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

}
