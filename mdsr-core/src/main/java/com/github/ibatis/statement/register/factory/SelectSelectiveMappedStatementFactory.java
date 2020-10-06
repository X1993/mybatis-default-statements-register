package com.github.ibatis.statement.register.factory;

import com.github.ibatis.statement.base.core.MethodSignature;
import com.github.ibatis.statement.base.core.matedata.EntityMateData;
import com.github.ibatis.statement.base.core.matedata.MappedStatementMateData;
import com.github.ibatis.statement.base.logical.LogicalColumnMateData;
import com.github.ibatis.statement.mapper.SelectMapper;
import com.github.ibatis.statement.util.reflect.ParameterizedTypeImpl;
import org.apache.ibatis.mapping.ResultMap;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.mapping.SqlSource;
import org.apache.ibatis.scripting.xmltags.*;
import java.lang.reflect.Type;
import java.util.*;
import java.util.function.Function;

/**
 * @see {@link SelectMapper#selectSelective(Object, boolean)}
 * @Author: junjie
 * @Date: 2020/3/13
 */
public class SelectSelectiveMappedStatementFactory extends AbstractSelectMappedStatementFactory {

    public final static String SELECT_SELECTIVE = "selectSelective";

    public final static String TOTAL_SELECTIVE = "totalSelective";

    @Override
    protected boolean isMatch(MappedStatementMateData mappedStatementMateData)
    {
        MethodSignature methodSignature = mappedStatementMateData.getMapperMethodMateData().getMethodSignature();
        EntityMateData entityMateData = mappedStatementMateData.getEntityMateData();
        Class<?> entityClass = entityMateData.getEntityClass();
        ParameterizedTypeImpl returnType = ParameterizedTypeImpl.make(List.class, new Type[]{entityClass}, null);
        return super.isMatchMethodSignature(methodSignature ,new MethodSignature(returnType ,
                SELECT_SELECTIVE ,entityClass ,boolean.class))
                || super.isMatchMethodSignature(methodSignature ,new MethodSignature(int.class ,
                TOTAL_SELECTIVE ,entityClass ,boolean.class));
    }

    /**
     * 查询的是实体
     * @param mappedStatementMateData
     * @return
     */
    private boolean selectEntity(MappedStatementMateData mappedStatementMateData){
        String methodName = mappedStatementMateData.getMapperMethodMateData().getMappedMethod().getName();
        return SELECT_SELECTIVE.equals(methodName);
    }

    @Override
    protected ResultMap resultMaps(MappedStatementMateData mappedStatementMateData)
    {
        return selectEntity(mappedStatementMateData) ? super.resultMaps(mappedStatementMateData) :
                new ResultMap.Builder(mappedStatementMateData.getConfiguration(),
                        mappedStatementMateData.getMapperMethodMateData().getMappedStatementId() + "-ResultMap",
                        int.class,
                        Collections.EMPTY_LIST,
                        null).build();
    }

    @Override
    protected SqlSource sqlSource(MappedStatementMateData mappedStatementMateData)
    {
        EntityMateData entityMateData = mappedStatementMateData.getEntityMateData();
        List<SqlNode> sqlNodes = new LinkedList<>();

        String selectContent = selectEntity(mappedStatementMateData)
                ? entityMateData.getBaseColumnListSqlContent().toString() : " COUNT(0) ";

        sqlNodes.add(new StaticTextSqlNode(new StringBuilder("SELECT ")
                .append(selectContent)
                .append(" FROM `")
                .append(entityMateData.getTableName())
                .append("` WHERE 1 = 1 ")
                .toString()));

        SqlCommandType sqlCommandType = sqlCommandType(mappedStatementMateData);
        Function<String ,String> propertyNameFunction = name -> "param1." + name;
        Function<StringBuilder ,StringBuilder> sqlContentFunction = content -> content.insert(0 ," AND ");
        sqlNodes.add(new ChooseSqlNode(Arrays.asList(new IfSqlNode(new MixedSqlNode(
                entityMateData.selectiveConditionSqlNodes(sqlCommandType, propertyNameFunction, sqlContentFunction)) ,
                "param1 != null")),
                //如果param1为null，使用默认值
                entityMateData.noCustomConditionsSqlNode(sqlCommandType ,sqlContentFunction)));

        LogicalColumnMateData logicalColumnMateData = entityMateData.getLogicalColumnMateData();
        if (logicalColumnMateData != null){
            sqlNodes.add(new IfSqlNode(new StaticTextSqlNode(new StringBuilder(" AND ")
                    .append(logicalColumnMateData.equalSqlContent(true))
                    .toString()) ,"param2"));
        }

        return new DynamicSqlSource(mappedStatementMateData.getConfiguration() ,new MixedSqlNode(sqlNodes));
    }

}
