package com.github.ibatis.statement.register.factory.common;

import com.github.ibatis.statement.base.core.MethodSignature;
import com.github.ibatis.statement.base.core.matedata.EntityMateData;
import com.github.ibatis.statement.base.core.matedata.MappedStatementMateData;
import com.github.ibatis.statement.base.logical.LogicalColumnMateData;
import com.github.ibatis.statement.mapper.method.MapperMethodEnum;
import com.github.ibatis.statement.register.AbstractMappedStatementFactory;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.mapping.SqlSource;
import org.apache.ibatis.scripting.xmltags.DynamicSqlSource;
import org.apache.ibatis.scripting.xmltags.MixedSqlNode;
import org.apache.ibatis.scripting.xmltags.SqlNode;
import org.apache.ibatis.scripting.xmltags.StaticTextSqlNode;
import java.util.LinkedList;
import java.util.List;

/**
 * @Author: X1993
 * @Date: 2020/4/27
 */
public class DeleteSelectiveMappedStatementFactory extends AbstractMappedStatementFactory {

    @Override
    protected boolean isMatch(MappedStatementMateData mappedStatementMateData)
    {
        EntityMateData entityMateData = mappedStatementMateData.getEntityMateData();
        MethodSignature methodSignature = mappedStatementMateData.getMapperMethodMateData().getMethodSignature();

        return methodSignature.isMatch(MapperMethodEnum.DELETE_SELECTIVE.methodSignature(entityMateData))
                || methodSignature.isMatch(MapperMethodEnum.DELETE_SELECTIVE_ON_PHYSICAL.methodSignature(entityMateData));
    }

    @Override
    protected SqlSource sqlSource(MappedStatementMateData mappedStatementMateData)
    {
        EntityMateData entityMateData = mappedStatementMateData.getEntityMateData();
        LogicalColumnMateData logicalColumnMateData = entityMateData.getLogicalColumnMateData();

        SqlCommandType sqlCommandType = sqlCommandType(mappedStatementMateData);
        boolean logicalDelete = sqlCommandType == SqlCommandType.UPDATE;

        List<SqlNode> sqlNodes = new LinkedList<>();
        sqlNodes.add(entityMateData.deleteSqlNodeNoWhere(logicalDelete));

        /*
           where 1 = 1
            and columnName3 = defaultValue3
            ...
          <if test = 'propertyName1 != null'>
               and columnName1 = #{propertyName1,jdbcType=XXX}
          </if>
          <if test = 'propertyName2 != null'>
               and columnName2 = #{propertyName2,jdbcType=XXX}
          </if>
            ...
         */

        //where条件
        sqlNodes.add(new StaticTextSqlNode(" WHERE 1 = 1 "));

        //默认过滤条件
        sqlNodes.add(entityMateData.defaultConditionsSqlNode(sqlCommandType ,
                content -> content.insert(0 ," AND ")));

        if (logicalDelete){
            //逻辑存在条件
            sqlNodes.add(new StaticTextSqlNode(logicalColumnMateData.equalSqlContent(true)
                    .insert(0 ," AND ").toString()));
        }

        return new DynamicSqlSource(mappedStatementMateData.getConfiguration() ,new MixedSqlNode(sqlNodes));
    }

    @Override
    protected SqlCommandType sqlCommandType(MappedStatementMateData mappedStatementMateData) {
        if (MapperMethodEnum.DELETE_SELECTIVE.methodName().equals(
                mappedStatementMateData.getMapperMethodMateData().getMappedMethod().getName())
                && mappedStatementMateData.getEntityMateData().getLogicalColumnMateData() != null)
        {//逻辑删除
            return SqlCommandType.UPDATE;
        }else {
            return SqlCommandType.DELETE;
        }
    }

}
