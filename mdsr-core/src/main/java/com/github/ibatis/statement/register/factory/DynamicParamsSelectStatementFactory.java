package com.github.ibatis.statement.register.factory;

import com.github.ibatis.statement.base.core.MethodSignature;
import com.github.ibatis.statement.base.core.matedata.EntityMateData;
import com.github.ibatis.statement.base.core.matedata.MappedStatementMateData;
import com.github.ibatis.statement.base.logical.LogicalColumnMateData;
import com.github.ibatis.statement.mapper.DynamicSelectMapper;
import com.github.ibatis.statement.mapper.param.DynamicParams;
import com.github.ibatis.statement.register.AbstractMappedStatementFactory;
import com.github.ibatis.statement.util.reflect.ParameterizedTypeImpl;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.mapping.SqlSource;
import org.apache.ibatis.scripting.xmltags.*;
import org.apache.ibatis.session.Configuration;
import java.lang.reflect.Type;
import java.util.*;

/**
 * @see DynamicSelectMapper#selectByDynamicParams(DynamicParams)
 * @see DynamicSelectMapper#countByDynamicParams(DynamicParams)
 *
 * @Author: X1993
 * @Date: 2020/4/5
 */
public class DynamicParamsSelectStatementFactory extends AbstractMappedStatementFactory
{
    public static final String SELECT_ALL_METHOD_NAME = "selectByDynamicParams";

    public static final String SELECT_COUNT_METHOD_NAME = "countByDynamicParams";

    @Override
    protected boolean isMatch(MappedStatementMateData mappedStatementMateData)
    {
        MethodSignature methodSignature = mappedStatementMateData.getMapperMethodMateData().getMethodSignature();
        return methodSignature.isMatch(new MethodSignature(int.class , SELECT_COUNT_METHOD_NAME, DynamicParams.class))
                || methodSignature.isMatch(new MethodSignature(ParameterizedTypeImpl.make(
                List.class ,new Type[]{mappedStatementMateData.getEntityMateData().getEntityClass()} ,null) ,
                SELECT_ALL_METHOD_NAME, DynamicParams.class));
    }
    @Override
    protected SqlCommandType sqlCommandType(MappedStatementMateData mappedStatementMateData) {
        return SqlCommandType.SELECT;
    }

    @Override
    protected SqlSource sqlSource(MappedStatementMateData mappedStatementMateData)
    {
        Configuration configuration = mappedStatementMateData.getConfiguration();
        EntityMateData entityMateData = mappedStatementMateData.getEntityMateData();
        String methodName = mappedStatementMateData.getMapperMethodMateData().getMappedMethod().getName();

        boolean selectCount = SELECT_COUNT_METHOD_NAME.equals(methodName);
        List<SqlNode> sqlNodes = new ArrayList<>();

        sqlNodes.add(new StaticTextSqlNode("SELECT "));
        if (selectCount){
            sqlNodes.add(new StaticTextSqlNode("COUNT(0)"));
        }else {
            sqlNodes.add(new ChooseSqlNode(Arrays.asList(new IfSqlNode(
                    new TextSqlNode("${selectElements}") ,"selectElements != null")) ,
                    new StaticTextSqlNode(entityMateData.getBaseColumnListSqlContent())));
        }
        sqlNodes.add(new StaticTextSqlNode(new StringBuilder(" FROM `")
                .append(entityMateData.getTableMateData().getTableName())
                .append("` WHERE 1 = 1 ")
                .toString()));

        //默认查询条件
        sqlNodes.add(entityMateData.defaultConditionsSqlNode(
                sqlCommandType(mappedStatementMateData) ,
                content -> content.insert(0 ," AND ")));

        //动态where条件
        sqlNodes.add(buildConditionSql(configuration ,"whereConditions"));

        LogicalColumnMateData logicalColumnMateData = entityMateData.getLogicalColumnMateData();
        if (logicalColumnMateData != null) {
            //logical
            sqlNodes.add(new IfSqlNode(new StaticTextSqlNode(new StringBuilder(" AND ")
                    .append(logicalColumnMateData.getEscapeColumnName())
                    .append(" = ")
                    .append(logicalColumnMateData.getExistValue())
                    .toString()), "logical"));
        }

        //group by
        sqlNodes.add(new IfSqlNode(new MixedSqlNode(Arrays.asList(new StaticTextSqlNode(" GROUP BY ") ,
                new ForEachSqlNode(configuration, new TextSqlNode("${groupKey}") ,
                "groupColumns", null, "groupKey",
                        null, null, ","))),
                "groupColumns != null && groupColumns.size() > 0"));

        //having
        sqlNodes.add(new IfSqlNode(new MixedSqlNode(Arrays.asList(new StaticTextSqlNode(" HAVING 1 = 1 ") ,
                this.buildConditionSql(configuration ,"havingConditions"))) ,
                "havingConditions != null"));

        if (!selectCount){
            //order by
            sqlNodes.add(new IfSqlNode(new ForEachSqlNode(configuration ,
                    new TextSqlNode(" ${order.key} ${order.rule} ") ,"orderRules" ,null ,
                    "order" ,"ORDER BY" ,null ,",") ,
                    "orderRules != null and orderRules.size > 0"));

            //limit
            sqlNodes.add(new IfSqlNode(new StaticTextSqlNode(" LIMIT #{limitParam.index}, " +
                    "#{limitParam.size} ") ,"limitParam != null"));
        }

        return new DynamicSqlSource(configuration ,new MixedSqlNode(sqlNodes));
    }

    private IfSqlNode buildConditionSql(Configuration configuration ,String conditionsExpression)
    {
        return new IfSqlNode(new MixedSqlNode(Arrays.asList(
                new VarDeclSqlNode("proCondition" ,"null") ,
                new ForEachSqlNode(configuration, new MixedSqlNode(Arrays.asList(
                        //多个过滤条件连接符
                        new ChooseSqlNode(Arrays.asList(new IfSqlNode(new StaticTextSqlNode(" AND ( "),
                                "(proCondition == null or (proCondition != null and proCondition.isOr() == false)) " +
                                        "and condition.isOr() == true")), new IfSqlNode(new StaticTextSqlNode(" AND "),
                                "proCondition == null or (proCondition != null and proCondition.isOr() == false)")),
                        // column [rule]
                        new TextSqlNode(" ${condition.key} ${condition.rule.expression} "),
                        new ChooseSqlNode(Arrays.asList(
                            new IfSqlNode(new ForEachSqlNode(configuration, new StaticTextSqlNode(" #{data} "),
                                "condition.value", null, "data",
                                "(", ")", ","),
                                "condition.rule.name()=='IN' || condition.rule.name()=='NOT_IN'"),
                            new IfSqlNode(new StaticTextSqlNode(" #{condition.value.minVal} AND #{condition.value.maxVal}"),
                                "condition.rule.name()=='BETWEEN'"),
                            new IfSqlNode(new StaticTextSqlNode(""),
                                " condition.rule.name()=='NE' || condition.rule.name()=='IS_NULL' || condition.rule.name()=='NOT_NULL'")),
                            new StaticTextSqlNode(" #{condition.value} ")),
                        new IfSqlNode(new StaticTextSqlNode(" OR "), "condition.isOr() == true"),
                        new IfSqlNode(new StaticTextSqlNode(") "),
                                "condition.isOr() == false and (proCondition != null and proCondition.isOr() ==true)"),
                        new VarDeclSqlNode("proCondition", "condition")
                )), conditionsExpression + ".getParams()", null, "condition",
                        null, null, null))), conditionsExpression + " != null");
    }

}
