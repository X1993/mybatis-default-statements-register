package com.github.ibatis.statement.base.core.matedata;

import com.github.ibatis.statement.base.dv.ColumnDefaultValue;
import com.github.ibatis.statement.base.logical.LogicalColumnMateData;
import com.github.ibatis.statement.util.TypeUtils;
import com.github.ibatis.statement.util.reflect.ParameterizedTypeImpl;
import lombok.Data;
import org.apache.ibatis.mapping.*;
import org.apache.ibatis.scripting.xmltags.*;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSession;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.text.MessageFormat;
import java.util.*;
import java.util.function.Function;

/**
 * 构建{@link org.apache.ibatis.mapping.MappedStatement}的元数据
 * @Author: X1993
 * @Date: 2020/3/5
 */
@Data
public class MappedStatementMateData implements Cloneable{

    private EntityMateData entityMateData;

    private final MapperMethodMateData mapperMethodMateData;

    private final SqlSession sqlSession;

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

    public ResultMap getDefaultMappingResultMap(){
        return entityMateData.getDefaultMappingResultMap(mapperMethodMateData.getMapperClass());
    }

    public Configuration getConfiguration() {
        return sqlSession.getConfiguration();
    }

    @Override
    public String toString() {
        return "MappedStatementMateData{" +
                "mapperMethodMateData=" + mapperMethodMateData +
                '}';
    }

    /**
     * 来源
     * @see MappedStatement#resource
     * @return
     */
    public String resource(){
        return this.getMapperMethodMateData().getMapperClass().getName();
    }

    /**
     * 命名空间
     * @return
     */
    public String namespace(){
        return resource();
    }

    public MappedStatement.Builder mappedStatementBuilder(SqlSource sqlSource ,SqlCommandType sqlCommandType)
    {
        Configuration configuration = getConfiguration();
        String mappedStatementId =  getMapperMethodMateData().getMappedStatementId();
        boolean isSelect = sqlCommandType == SqlCommandType.SELECT;
        boolean hasCache = configuration.hasCache(namespace());

        return new MappedStatement.Builder(configuration,
                mappedStatementId, sqlSource, sqlCommandType)
                .resource(resource())
                .statementType(StatementType.PREPARED)
                .databaseId(configuration.getDatabaseId())
                .resultSetType(configuration.getDefaultResultSetType())
                .resultMaps(Arrays.asList(resultMapsByReturnType()))
                .flushCacheRequired(hasCache && !isSelect)
                .useCache(hasCache && isSelect)
                .cache(hasCache ? configuration.getCache(namespace()) : null);
    }

    /**
     update table
     set
     <if test="propertyName1 != null">
     col1 = #{propertyName1,jdbcType=XXX},
     </if>
     <if test="propertyName2 != null">
     col2 = #{propertyName2,jdbcType=XXX},
     </if>
     ...
     col5 = defaultValue5
     ...
     where
     primaryKey1 = #{keyPropertyName1,jdbcType=XXX}
     and col13 = defaultValue13
     ...
     (and logicalCol = existValue)
     * @param propertyNameFunction
     * @param isSelective
     * @return
     */
    public SqlNode updateSqlNode(Function<String ,String> propertyNameFunction ,
                                        boolean isSelective)
    {
        EntityMateData entityMateData = this.getEntityMateData();
        List<SqlNode> sqlNodes = new ArrayList<>();

        sqlNodes.add(new StaticTextSqlNode(new StringBuilder("UPDATE `")
                .append(entityMateData.getTableMateData().getTableName())
                .append("` ").toString()));
        sqlNodes.add(updateSetSqlNode(propertyNameFunction ,isSelective));
        sqlNodes.add(updateWhereSqlNode(propertyNameFunction));

        return new MixedSqlNode(sqlNodes);
    }

    public SqlSource updateSqlSource(Function<String ,String> propertyNameFunction ,boolean isSelective)
    {
        Configuration configuration = this.getConfiguration();
        return new DynamicSqlSource(configuration ,updateSqlNode(propertyNameFunction ,isSelective));
    }

    public SqlNode updateSetSqlNode(Function<String ,String> propertyNameFunction , boolean isSelective)
    {
        EntityMateData entityMateData = this.getEntityMateData();
        Configuration configuration = this.getConfiguration();
        Map<String ,ColumnPropertyMapping> updateColumnPropertyMappings = entityMateData.getUpdateColumnPropertyMapping();
        Map<String, ColumnDefaultValue> columnDefaultValueMap = entityMateData.filterColumnDefaultValues(SqlCommandType.UPDATE);

        List<SqlNode> setSqlNodes = new ArrayList<>();
        for (ColumnPropertyMapping columnPropertyMapping : updateColumnPropertyMappings.values())
        {
            String columnName = columnPropertyMapping.getColumnName();
            String propertyName = columnPropertyMapping.getPropertyName();
            ColumnDefaultValue columnDefaultValue = columnDefaultValueMap.remove(columnName);

            SqlNode setSqlNode = new StaticTextSqlNode(
                    new StringBuilder(columnPropertyMapping.getEscapeColumnName())
                            .append(" = ")
                            .append(columnPropertyMapping.createPropertyPrecompiledText(propertyNameFunction))
                            .append(",")
                            .toString());

            IfSqlNode ifSqlNode = new IfSqlNode(setSqlNode, propertyNameFunction.apply(propertyName) + " != null");

            if (columnDefaultValue != null){
                SqlNode defaultSqlNode = new StaticTextSqlNode(
                        new StringBuilder(columnPropertyMapping.getEscapeColumnName())
                                .append(" = ")
                                .append(columnDefaultValue.getValue())
                                .append(",")
                                .toString());
                if (columnDefaultValue.isOverwriteCustom()){
                    setSqlNode = defaultSqlNode;
                }else {
                    setSqlNode = new ChooseSqlNode(Arrays.asList(ifSqlNode) ,defaultSqlNode);
                }
            }else if (isSelective){
                setSqlNode = ifSqlNode;
            }
            setSqlNodes.add(setSqlNode);
        }

        for (ColumnDefaultValue columnDefaultValue : columnDefaultValueMap.values()) {
            setSqlNodes.add(new StaticTextSqlNode(new StringBuilder("`")
                    .append(columnDefaultValue.getColumnName())
                    .append("` = ")
                    .append(columnDefaultValue.getValue())
                    .append(",")
                    .toString()));
        }

        return new SetSqlNode(configuration ,
                new TrimSqlNode(configuration ,new MixedSqlNode(setSqlNodes) ,
                        null ,null ,null ,","));
    }

    /**
     primaryKey1 = #{keyPropertyName1,jdbcType=XXX}
     ...
     col6 = #{propertyName6,jdbcType=XXX},
     ...
     <if test="propertyName4 != null">
     col4 = #{propertyName4,jdbcType=XXX},
     </if>
     and col13 = value
     ...
     (and logicalCol = existValue)
     * @param propertyNameFunction
     * @return
     */
    public SqlNode updateWhereSqlNode(Function<String ,String> propertyNameFunction)
    {
        EntityMateData entityMateData = this.getEntityMateData();
        Configuration configuration = this.getConfiguration();
        List<SqlNode> whereSqlNodes = new ArrayList<>();

        //主键,不带if标签
        for (ColumnPropertyMapping columnPropertyMapping : entityMateData.getKeyPrimaryColumnPropertyMappings().values())
        {
            whereSqlNodes.add(new StaticTextSqlNode(
                    columnPropertyMapping.createEqSqlContent(propertyNameFunction)
                            .append(" AND ")
                            .toString()));
        }

        //默认过滤条件
        whereSqlNodes.add(entityMateData.defaultConditionsSqlNode(SqlCommandType.UPDATE ,
                content -> content.append(" AND ")));

        LogicalColumnMateData logicalColumnMateData = entityMateData.getLogicalColumnMateData();
        if (logicalColumnMateData != null){
            whereSqlNodes.add(new StaticTextSqlNode(logicalColumnMateData.equalSqlContent(true).toString()));
        }else {
            whereSqlNodes.add(new StaticTextSqlNode(" 1 = 1 "));
        }

        return new WhereSqlNode(configuration ,new MixedSqlNode(whereSqlNodes));
    }

    /**
     * 根据方法返回类型推测匹配的{@link ResultMap}
     * @return
     */
    public ResultMap resultMapsByReturnType()
    {
        MapperMethodMateData mapperMethodMateData = getMapperMethodMateData();
        Type genericReturnType = mapperMethodMateData.getMethodSignature().getGenericReturnType();
        Class<?> returnType = mapperMethodMateData.getMappedMethod().getReturnType();
        Class<?> entityClass = getEntityMateData().getEntityClass();
        Type resultMapType = genericReturnType;

        if (resultMapType instanceof ParameterizedType){
            ParameterizedType parameterizedType = (ParameterizedType) resultMapType;
            Type rawType = parameterizedType.getRawType();
            if (rawType instanceof Class){
                Class<?> rawClass = (Class<?>) rawType;
                if (Collection.class.isAssignableFrom(rawClass)){
                    resultMapType = parameterizedType.getActualTypeArguments()[0];
                }
            }
        }else if (returnType.isArray()){
            resultMapType = returnType.getComponentType();
        }else if (genericReturnType instanceof GenericArrayType)
        {
            resultMapType = ((GenericArrayType) genericReturnType).getGenericComponentType();
        }

        if (TypeUtils.isAssignableFrom(entityClass ,resultMapType)){
            return getDefaultMappingResultMap();
        }else if (resultMapType instanceof Class){
            return new ResultMap.Builder(
                    getConfiguration(),
                    this.getMapperMethodMateData().getMappedStatementId() + "-ResultMap",
                    Collection.class.isAssignableFrom(returnType) ? Object.class : returnType,
                    Collections.EMPTY_LIST,
                    null).build();
        }

        throw new IllegalStateException(MessageFormat.format("Cannot analyze ResultMapper by method " +
                "return type [{0}]" ,genericReturnType));
    }

    public void insertColumnValueSqlNodes(List<SqlNode> columnSqlNodes ,
                                          List<SqlNode> propertySqlNodes ,
                                          Function<String ,String> propertyNameFunction ,
                                          boolean isSelective)
    {
        EntityMateData entityMateData = this.getEntityMateData();
        Map<String ,ColumnPropertyMapping> columnPropertyMappings = entityMateData.getInsertColumnPropertyMapping();
        Map<String, ColumnDefaultValue> columnDefaultValueMap = entityMateData
                .filterColumnDefaultValues(SqlCommandType.INSERT);

        for (ColumnPropertyMapping columnPropertyMapping : columnPropertyMappings.values())
        {
            ColumnMateData columnMateData = columnPropertyMapping.getColumnMateData();

            String columnName = columnMateData.getColumnName();
            String propertyName = columnPropertyMapping.getPropertyName();

            StaticTextSqlNode columnTextSqlNode = new StaticTextSqlNode(new StringBuilder("`")
                    .append(columnName)
                    .append("`,").toString());

            StaticTextSqlNode propertyTextSqlNode = new StaticTextSqlNode(
                    columnPropertyMapping.createPropertyPrecompiledText(propertyNameFunction)
                            .append(",").toString());

            String test = propertyNameFunction.apply(propertyName) + " != null";

            ColumnDefaultValue columnDefaultValue = columnDefaultValueMap.remove(columnName);
            if (columnDefaultValue != null){
                StaticTextSqlNode defaultSqlNode = new StaticTextSqlNode(columnDefaultValue.getValue() + ",");
                columnSqlNodes.add(columnTextSqlNode);
                if (columnDefaultValue.isOverwriteCustom()) {
                    //直接使用默认值
                    propertySqlNodes.add(defaultSqlNode);
                }else {
                    propertySqlNodes.add(new ChooseSqlNode(Arrays.asList(
                            new IfSqlNode(propertyTextSqlNode, test)) ,defaultSqlNode));
                }
            } else if (isSelective){
                //if标签
                columnSqlNodes.add(new IfSqlNode(columnTextSqlNode, test));
                propertySqlNodes.add(new IfSqlNode(propertyTextSqlNode, test));
            } else {
                columnSqlNodes.add(columnTextSqlNode);
                propertySqlNodes.add(propertyTextSqlNode);
            }
        }

        for (ColumnDefaultValue columnDefaultValue : columnDefaultValueMap.values()) {
            columnSqlNodes.add(new StaticTextSqlNode(new StringBuilder("`")
                    .append(columnDefaultValue.getColumnName())
                    .append("`,")
                    .toString()));
            propertySqlNodes.add(new StaticTextSqlNode(columnDefaultValue.getValue() + ","));
        }

        //为逻辑列赋默认值
        LogicalColumnMateData logicalColumnMateData = entityMateData.getLogicalColumnMateData();
        if (logicalColumnMateData != null){
            columnSqlNodes.add(new StaticTextSqlNode(new StringBuilder("`")
                    .append(logicalColumnMateData.getColumnName())
                    .append("`").toString()));
            propertySqlNodes.add(new StaticTextSqlNode(logicalColumnMateData.getExistValue()));
        }
    }

    /**
     * insert into `tableName` (
     *  col1 ,
     *  <if test="propertyName2 != null">
     *      col2 ,
     *  </if>
     *  col3
     *  ) values (
     *  <choose>
     *      <if test="propertyName1 != null">
     *          #{propertyName1,jdbcType=XXX},
     *      </if>
     *      <otherwise>
     *          defaultValue1
     *      </otherwise>
     *  </choose>
     *  <if test="propertyName2 != null">
     *      #{propertyName2,jdbcType=XXX},
     *  </if>
     *  ...
     *  defaultValue3,
     *  ...
     * )
     * @param propertyNameFunction
     * @param isSelective
     * @return
     */
    public SqlSource insertSqlSource(Function<String ,String> propertyNameFunction ,
                                        boolean isSelective)
    {
        List<SqlNode> columnSqlNodes = new LinkedList<>();
        List<SqlNode> propertySqlNodes = new LinkedList<>();

        this.insertColumnValueSqlNodes(columnSqlNodes ,propertySqlNodes ,propertyNameFunction ,isSelective);

        List<SqlNode> sqlNodes = new LinkedList<>();
        sqlNodes.add(new StaticTextSqlNode(new StringBuilder("INSERT INTO `")
                .append(this.getEntityMateData().getTableName())
                .append("` ")
                .toString()));

        sqlNodes.add(new TrimSqlNode(this.getConfiguration() ,
                new MixedSqlNode(columnSqlNodes) ," (" , null,
                ") " ,","));
        sqlNodes.add(new TrimSqlNode(this.getConfiguration() ,
                new MixedSqlNode(propertySqlNodes) ," VALUES (" , null,
                ")" ,","));

        return new DynamicSqlSource(this.getConfiguration() ,new MixedSqlNode(sqlNodes));
    }

}
