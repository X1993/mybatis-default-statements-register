package com.github.ibatis.statement.register.factory.common;

import com.github.ibatis.statement.base.core.MethodSignature;
import com.github.ibatis.statement.base.core.matedata.ColumnPropertyMapping;
import com.github.ibatis.statement.base.core.matedata.EntityMateData;
import com.github.ibatis.statement.base.core.matedata.MappedStatementMateData;
import com.github.ibatis.statement.base.logical.LogicalColumnMateData;
import com.github.ibatis.statement.mapper.param.ConditionRule;
import com.github.ibatis.statement.register.AbstractMappedStatementFactory;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.mapping.SqlSource;
import org.apache.ibatis.scripting.xmltags.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;
import static com.github.ibatis.statement.mapper.method.MapperMethodEnum.*;

/**
 * @Author: X1993
 * @Date: 2020/3/13
 */
public class SelectSelectiveMappedStatementFactory extends AbstractMappedStatementFactory {

    @Override
    protected boolean isMatch(MappedStatementMateData mappedStatementMateData)
    {
        MethodSignature methodSignature = mappedStatementMateData.getMapperMethodMateData().getMethodSignature();
        EntityMateData entityMateData = mappedStatementMateData.getEntityMateData();
        return methodSignature.isMatch(SELECT_SELECTIVE.methodSignature(entityMateData))
                || methodSignature.isMatch(TOTAL_SELECTIVE.methodSignature(entityMateData));
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
        return SELECT_SELECTIVE.methodName().equals(methodName);
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
