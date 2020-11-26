package com.github.ibatis.statement.register.factory;

import com.github.ibatis.statement.register.AbstractMappedStatementFactory;
import com.github.ibatis.statement.base.core.MethodSignature;
import com.github.ibatis.statement.base.core.matedata.EntityMateData;
import com.github.ibatis.statement.base.core.matedata.MappedStatementMateData;
import com.github.ibatis.statement.base.logical.LogicalColumnMateData;
import com.github.ibatis.statement.mapper.TableMapper;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.mapping.SqlSource;
import org.apache.ibatis.scripting.xmltags.*;
import java.util.LinkedList;
import java.util.List;

/**
 * @see  TableMapper#deleteSelectiveOnPhysical(Object)
 * @see  TableMapper#deleteSelective(Object)
 * @Author: junjie
 * @Date: 2020/4/27
 */
public class DeleteSelectiveMappedStatementFactory extends AbstractMappedStatementFactory {

    public static final String DELETE_SELECTIVE_METHOD_NAME = "deleteSelective";

    public static final String PHYSICAL_DELETE_SELECTIVE_METHOD_NAME = "deleteSelectiveOnPhysical";

    @Override
    protected boolean isMatch(MappedStatementMateData mappedStatementMateData)
    {
        EntityMateData entityMateData = mappedStatementMateData.getEntityMateData();
        Class<?> entityClass = entityMateData.getEntityClass();

        return super.isMatchMethodSignature(mappedStatementMateData.getMapperMethodMateData().getMethodSignature() ,
                new MethodSignature(int.class ,DELETE_SELECTIVE_METHOD_NAME, entityClass))
                || super.isMatchMethodSignature(mappedStatementMateData.getMapperMethodMateData().getMethodSignature() ,
                new MethodSignature(int.class ,PHYSICAL_DELETE_SELECTIVE_METHOD_NAME, entityClass));
    }

    @Override
    protected SqlSource sqlSource(MappedStatementMateData mappedStatementMateData)
    {
        EntityMateData entityMateData = mappedStatementMateData.getEntityMateData();
        LogicalColumnMateData logicalColumnMateData = entityMateData.getLogicalColumnMateData();

        SqlCommandType sqlCommandType = sqlCommandType(mappedStatementMateData);
        boolean logicalDelete = sqlCommandType == SqlCommandType.UPDATE;

        List<SqlNode> sqlNodes = new LinkedList<>();
        sqlNodes.add(DeleteByPrimaryKeyMappedStatementFactory.deleteSqlNodeNoWhere(logicalDelete ,entityMateData));

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

        sqlNodes.add(entityMateData.defaultConditionsSqlNode(sqlCommandType(mappedStatementMateData) ,
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
        if (DELETE_SELECTIVE_METHOD_NAME.equals(mappedStatementMateData.getMapperMethodMateData().getMappedMethod()
                .getName()) && mappedStatementMateData.getEntityMateData().getLogicalColumnMateData() != null)
        {//逻辑删除
            return SqlCommandType.UPDATE;
        }else {
            return SqlCommandType.DELETE;
        }
    }

}
