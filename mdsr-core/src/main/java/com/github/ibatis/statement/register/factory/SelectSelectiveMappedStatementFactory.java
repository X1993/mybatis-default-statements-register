package com.github.ibatis.statement.register.factory;

import com.github.ibatis.statement.base.core.MethodSignature;
import com.github.ibatis.statement.base.core.matedata.ColumnPropertyMapping;
import com.github.ibatis.statement.base.core.matedata.EntityMateData;
import com.github.ibatis.statement.base.core.matedata.MappedStatementMateData;
import com.github.ibatis.statement.base.logical.LogicalColumnMateData;
import com.github.ibatis.statement.mapper.SelectMapper;
import com.github.ibatis.statement.mapper.param.ConditionRule;
import com.github.ibatis.statement.register.AbstractMappedStatementFactory;
import sun.reflect.generics.reflectiveObjects.ParameterizedTypeImpl;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.mapping.SqlSource;
import org.apache.ibatis.scripting.xmltags.*;
import java.lang.reflect.Type;
import java.util.*;
import java.util.function.Function;

/**
 * @Author: X1993
 * @Date: 2020/3/13
 */
public class SelectSelectiveMappedStatementFactory extends AbstractMappedStatementFactory {

    /**
     * @see {@link SelectMapper#selectSelective(Object, boolean)}
     */
    public final static String SELECT_SELECTIVE = "selectSelective";

    /**
     * @see {@link SelectMapper#totalSelective(Object, boolean)}
     */
    public final static String TOTAL_SELECTIVE = "totalSelective";

    @Override
    protected boolean isMatch(MappedStatementMateData mappedStatementMateData)
    {
        MethodSignature methodSignature = mappedStatementMateData.getMapperMethodMateData().getMethodSignature();
        EntityMateData entityMateData = mappedStatementMateData.getEntityMateData();
        Class<?> entityClass = entityMateData.getEntityClass();
        ParameterizedTypeImpl returnType = ParameterizedTypeImpl.make(List.class, new Type[]{entityClass}, null);
        return methodSignature.isMatch(new MethodSignature(returnType ,SELECT_SELECTIVE ,entityClass ,boolean.class))
                || methodSignature.isMatch(new MethodSignature(int.class ,TOTAL_SELECTIVE ,entityClass ,boolean.class));
    }

    @Override
    protected SqlCommandType sqlCommandType(MappedStatementMateData mappedStatementMateData) {
        return SqlCommandType.SELECT;
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
    protected SqlSource sqlSource(MappedStatementMateData mappedStatementMateData)
    {
        EntityMateData entityMateData = mappedStatementMateData.getEntityMateData();
        List<SqlNode> sqlNodes = new LinkedList<>();

        String selectContent = selectEntity(mappedStatementMateData)
                ? entityMateData.getBaseColumnListSqlContent() : " COUNT(0) ";

        sqlNodes.add(new StaticTextSqlNode(new StringBuilder("SELECT ")
                .append(selectContent)
                .append(" FROM `")
                .append(entityMateData.getTableName())
                .append("` WHERE 1 = 1 ")
                .toString()));

        SqlCommandType sqlCommandType = sqlCommandType(mappedStatementMateData);

        Function<String ,String> propertyNameFunction = name -> "param1." + name;
        Function<StringBuilder ,StringBuilder> sqlContentFunction = content -> content.insert(0 ," AND ");

        List<SqlNode> conditionSqlNodes = new ArrayList<>();
        for (ColumnPropertyMapping columnPropertyMapping : entityMateData.getColumnPropertyMappings().values())
        {
            StaticTextSqlNode customSqlNode = new StaticTextSqlNode(
                    sqlContentFunction.apply(columnPropertyMapping.createConditionSqlContent(
                            ConditionRule.EQ ,propertyNameFunction)).toString());

            IfSqlNode equalIfSqlNode = new IfSqlNode(customSqlNode ,
                    propertyNameFunction.apply(columnPropertyMapping.getPropertyName()) + " != null");

            conditionSqlNodes.add(equalIfSqlNode);
        }

        sqlNodes.add(new IfSqlNode(new MixedSqlNode(conditionSqlNodes) , "param1 != null"));
        //默认查询条件
        sqlNodes.add(entityMateData.defaultConditionsSqlNode(sqlCommandType ,sqlContentFunction));

        LogicalColumnMateData logicalColumnMateData = entityMateData.getLogicalColumnMateData();
        if (logicalColumnMateData != null){
            sqlNodes.add(new IfSqlNode(new StaticTextSqlNode(new StringBuilder(" AND ")
                    .append(logicalColumnMateData.equalSqlContent(true))
                    .toString()) ,"param2"));
        }

        return new DynamicSqlSource(mappedStatementMateData.getConfiguration() ,new MixedSqlNode(sqlNodes));
    }

}
