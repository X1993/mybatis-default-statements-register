package com.github.ibatis.statement.register.factory;

import com.github.ibatis.statement.register.AbstractMappedStatementFactory;
import com.github.ibatis.statement.base.core.MethodSignature;
import com.github.ibatis.statement.base.core.matedata.ColumnPropertyMapping;
import com.github.ibatis.statement.base.core.matedata.EntityMateData;
import com.github.ibatis.statement.base.core.matedata.MappedStatementMateData;
import com.github.ibatis.statement.base.logical.LogicalColumnMateData;
import com.github.ibatis.statement.mapper.KeyTableMapper;
import com.github.ibatis.statement.util.reflect.ParameterizedTypeImpl;
import org.apache.ibatis.mapping.*;
import org.apache.ibatis.scripting.xmltags.*;
import org.apache.ibatis.session.Configuration;
import java.lang.reflect.Type;
import java.util.*;

/**
 * @see KeyTableMapper#deleteBatchByPrimaryKey(Collection)
 * @see KeyTableMapper#deleteBatchByPrimaryKeyOnPhysical(Collection)
 * @author junjie
 * @date 2020/3/14
 */
public class DeleteBatchByPrimaryKeyMappedStatementFactory extends AbstractMappedStatementFactory
{
    /**
     * 批量删除方法
     */
    public final static String DELETE_BATCH_ON_PHYSICAL = "deleteBatchByPrimaryKey";

    /**
     * 批量物理删除方法
     */
    public final static String PHYSICAL_DELETE_BATCH_METHOD_NAME = "deleteBatchByPrimaryKeyOnPhysical";

    @Override
    protected boolean isMatch(MappedStatementMateData mappedStatementMateData)
    {
        MethodSignature methodSignature = mappedStatementMateData.getMapperMethodMateData().getMethodSignature();
        EntityMateData entityMateData = mappedStatementMateData.getEntityMateData();

        if (entityMateData.getPrimaryKeyCount() < 1) {
            return false;
        }
        ParameterizedTypeImpl parameterizedType = ParameterizedTypeImpl.make(Collection.class,
            new Type[]{entityMateData.getReasonableKeyParameterClass()}, null);

        return super.isMatchMethodSignature(methodSignature ,new MethodSignature(int.class ,
                DELETE_BATCH_ON_PHYSICAL, parameterizedType))
                || super.isMatchMethodSignature(methodSignature ,new MethodSignature(int.class ,
                PHYSICAL_DELETE_BATCH_METHOD_NAME , parameterizedType))
                && entityMateData.getPrimaryKeyCount() > 0;
    }

    @Override
    protected SqlSource sqlSource(MappedStatementMateData mappedStatementMateData)
    {
        EntityMateData entityMateData = mappedStatementMateData.getEntityMateData();
        LogicalColumnMateData logicalColumnMateData = entityMateData.getLogicalColumnMateData();

        List<SqlNode> sqlNodes = new LinkedList<>();
        boolean logicalDelete = sqlCommandType(mappedStatementMateData) == SqlCommandType.UPDATE;

        sqlNodes.add(DeleteByPrimaryKeyMappedStatementFactory.deleteSqlNodeNoWhere(logicalDelete ,entityMateData));

        Configuration configuration = mappedStatementMateData.getConfiguration();
        Map<String ,ColumnPropertyMapping> keyPrimaryColumnPropertyMappings = entityMateData
                .getKeyPrimaryColumnPropertyMappings();

        boolean primaryKeyParameterIsEntity = entityMateData.isPrimaryKeyParameterIsEntity();

        sqlNodes.add(new StaticTextSqlNode(" WHERE "));
        if (primaryKeyParameterIsEntity)
        {
            /*
            <foreach collection="collectionExpression" item="item" index="index" open="(" close=")" separator="or">
               1 = 1
               and key1 = #{item.keyPropertyName1,jdbcType=XXX}
               and key2 = #{item.keyPropertyName2,jdbcType=XXX}
            </foreach>
             */

            StringBuilder whereConditions = new StringBuilder("(");

            //主键的查询条件
            for (ColumnPropertyMapping columnPropertyMapping : keyPrimaryColumnPropertyMappings.values()) {
                whereConditions.append(columnPropertyMapping.createEqSqlContent(name -> "item." + name))
                        .append(" AND ");
            }

            whereConditions.append(" 1 = 1 )");

            sqlNodes.add(new ForEachSqlNode(configuration ,new StaticTextSqlNode(whereConditions.toString()) ,
                    "collection" ,"index" , "item" ,
                    "(" ,")" ," OR "));

        }else {
            /*
                参数类型一定是唯一主键

                primaryKeyColName in (
             <foreach collection="collectionExpression" item="item" separator=",">
                #{item}
             </foreach>
             )
             */

            //主键多值查询
            ColumnPropertyMapping keyColumnPropertyMapping = keyPrimaryColumnPropertyMappings.values()
                    .stream()
                    .findFirst()
                    .get();

            sqlNodes.add(new StaticTextSqlNode(new StringBuilder(
                    keyColumnPropertyMapping.getColumnMateData().getEscapeColumnName())
                    .append(" in ")
                    .toString()));

            sqlNodes.add(new ForEachSqlNode(configuration ,new StaticTextSqlNode("#{item}") ,
                    "collection" ,null, "item" ,
                    "(" ,")" ,","));
        }

        //值固定的查询条件
        StringBuilder fixedValueConditions = entityMateData.defaultConditionsContent(
                sqlCommandType(mappedStatementMateData) ,content -> content.insert(0 ," AND "));

        if (logicalDelete){
            //逻辑存在条件
            fixedValueConditions.append(" AND ").append(logicalColumnMateData.equalSqlContent(true));
        }

        sqlNodes.add(new StaticTextSqlNode(fixedValueConditions.toString()));

        return new DynamicSqlSource(mappedStatementMateData.getConfiguration() ,new MixedSqlNode(sqlNodes));
    }

    @Override
    protected SqlCommandType sqlCommandType(MappedStatementMateData mappedStatementMateData) {
        if (DELETE_BATCH_ON_PHYSICAL.equals(mappedStatementMateData.getMapperMethodMateData().getMappedMethod()
                .getName()) && mappedStatementMateData.getEntityMateData().getLogicalColumnMateData() != null)
        {//逻辑删除
            return SqlCommandType.UPDATE;
        }else {
            return SqlCommandType.DELETE;
        }
    }
}
