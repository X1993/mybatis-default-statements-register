package com.github.ibatis.statement.base.core.matedata;

import org.apache.ibatis.mapping.ResultFlag;
import org.apache.ibatis.mapping.ResultMap;
import org.apache.ibatis.mapping.ResultMapping;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.type.TypeHandler;
import org.apache.ibatis.type.TypeHandlerRegistry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 构建{@link org.apache.ibatis.mapping.MappedStatement}的元数据
 * @Author: junjie
 * @Date: 2020/3/5
 */
public class MappedStatementMateData implements Cloneable{

    private EntityMateData entityMateData;

    private final MapperMethodMateData mapperMethodMateData;

    /**
     * 默认ResultMap
     */
    private ResultMap defaultMappingResultMap;

    private final SqlSession sqlSession;

    public static final String DEFAULT_MAPPING_RESULT_MAP_ID_SUFFIX = "defaultMappingResultMap";

    public MappedStatementMateData(EntityMateData entityMateData,
                                   MapperMethodMateData mapperMethodMateData,
                                   SqlSession sqlSession)
    {
        this.entityMateData = entityMateData;
        this.mapperMethodMateData = mapperMethodMateData;
        this.sqlSession = sqlSession;
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    public EntityMateData getEntityMateData() {
        return entityMateData;
    }

    public MapperMethodMateData getMapperMethodMateData() {
        return mapperMethodMateData;
    }

    public SqlSession getSqlSession() {
        return sqlSession;
    }

    public Configuration getConfiguration() {
        return sqlSession.getConfiguration();
    }

    public ResultMap getDefaultMappingResultMap() {
        if (defaultMappingResultMap == null){
            this.defaultMappingResultMap = getMappingResultMap();
        }
        return defaultMappingResultMap;
    }

    private ResultMap getMappingResultMap()
    {
        Configuration configuration = getConfiguration();
        Class<?> entityClass = entityMateData.getEntityClass();
        String mappingResultMappingId = getMapperMethodMateData().getMapperClass().getName()
                + "." + DEFAULT_MAPPING_RESULT_MAP_ID_SUFFIX;
        if (!configuration.hasResultMap(mappingResultMappingId))
        {
            Set<String> keyPrimaryPropertyNameSet = entityMateData.getKeyPrimaryColumnPropertyMappings()
                    .values()
                    .stream()
                    .map(mapping -> mapping.getPropertyName())
                    .collect(Collectors.toSet());

            ResultMap autoMappingResultMapping = new ResultMap.Builder(configuration, mappingResultMappingId,
                    entityClass, entityMateData.getColumnPropertyMappings()
                    .values()
                    .stream()
                    .map(columnPropertyMapping -> new ResultMapping.Builder(configuration,
                            columnPropertyMapping.getPropertyName(),
                            columnPropertyMapping.getColumnName(),
                            columnPropertyMapping.getPropertyMateData().getType())
                            .jdbcType(columnPropertyMapping.getColumnMateData().getJdbcType())
                            .flags(keyPrimaryPropertyNameSet.contains(columnPropertyMapping.getPropertyMateData()
                                    .getField().getName()) ? Arrays.asList(ResultFlag.ID) : new ArrayList<>())
                            .typeHandler(resolveTypeHandler(configuration ,
                                    columnPropertyMapping.getPropertyMateData().getType(),
                                    columnPropertyMapping.getPropertyMateData().getTypeHandlerClass()))
                            .build())
                    .collect(Collectors.toList()), null)
                    .build();

            configuration.addResultMap(autoMappingResultMapping);
        }

        return configuration.getResultMap(mappingResultMappingId);
    }

    private TypeHandler<?> resolveTypeHandler(Configuration configuration ,
                                              Class<?> javaType,
                                              Class<? extends TypeHandler<?>> typeHandlerType)
    {
        if (typeHandlerType == null) {
            return null;
        }
        TypeHandlerRegistry typeHandlerRegistry = configuration.getTypeHandlerRegistry();
        // javaType ignored for injected handlers see issue #746 for full detail
        TypeHandler<?> handler = typeHandlerRegistry.getMappingTypeHandler(typeHandlerType);
        if (handler == null) {
            // not in registry, create a new one
            handler = typeHandlerRegistry.getInstance(javaType, typeHandlerType);
        }
        return handler;
    }

}
