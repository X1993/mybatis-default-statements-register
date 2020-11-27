package com.github.ibatis.statement.register.factory;

import com.github.ibatis.statement.base.core.matedata.ColumnPropertyMapping;
import com.github.ibatis.statement.base.core.matedata.EntityMateData;
import com.github.ibatis.statement.base.core.matedata.MappedStatementMateData;
import com.github.ibatis.statement.base.dv.ColumnDefaultValue;
import com.github.ibatis.statement.base.logical.LogicalColumnMateData;
import com.github.ibatis.statement.register.AbstractMappedStatementFactory;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.mapping.SqlSource;
import org.apache.ibatis.scripting.xmltags.*;
import org.apache.ibatis.session.Configuration;
import java.util.*;
import java.util.function.Function;

/**
 * @author junjie
 * @date 2020/9/16
 */
public abstract class AbstractUpdateMappedStatementFactory extends AbstractMappedStatementFactory {

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
     * @param mappedStatementMateData
     * @param propertyNameFunction
     * @param isSelective
     * @return
     */
    protected SqlNode createSqlNode(MappedStatementMateData mappedStatementMateData ,
                                        Function<String ,String> propertyNameFunction ,
                                        boolean isSelective)
    {
        EntityMateData entityMateData = mappedStatementMateData.getEntityMateData();
        List<SqlNode> sqlNodes = new ArrayList<>();

        StringBuilder sqlContent = new StringBuilder("UPDATE `")
                .append(entityMateData.getTableMateData().getTableName())
                .append("` ");
        sqlNodes.add(new StaticTextSqlNode(sqlContent.toString()));

        sqlNodes.add(getSetSqlNode(mappedStatementMateData ,propertyNameFunction ,isSelective));
        sqlNodes.add(getWhereSqlNode(mappedStatementMateData ,propertyNameFunction));

        return new MixedSqlNode(sqlNodes);
    }

    protected SqlSource createSqlSource(MappedStatementMateData mappedStatementMateData ,
                                    Function<String ,String> propertyNameFunction ,
                                    boolean isSelective)
    {
        Configuration configuration = mappedStatementMateData.getConfiguration();
        return new DynamicSqlSource(configuration ,createSqlNode(mappedStatementMateData ,propertyNameFunction ,isSelective));
    }

    protected SqlNode getSetSqlNode(MappedStatementMateData mappedStatementMateData ,
                                    Function<String ,String> propertyNameFunction ,
                                    boolean isSelective)
    {
        EntityMateData entityMateData = mappedStatementMateData.getEntityMateData();
        Configuration configuration = mappedStatementMateData.getConfiguration();
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
     * @param mappedStatementMateData
     * @param propertyNameFunction
     * @return
     */
    protected SqlNode getWhereSqlNode(MappedStatementMateData mappedStatementMateData,
                                      Function<String ,String> propertyNameFunction)
    {
        EntityMateData entityMateData = mappedStatementMateData.getEntityMateData();
        Configuration configuration = mappedStatementMateData.getConfiguration();
        List<SqlNode> whereSqlNodes = new ArrayList<>();

        //主键,不带if标签
        for (ColumnPropertyMapping columnPropertyMapping : entityMateData.getKeyPrimaryColumnPropertyMappings().values())
        {
            whereSqlNodes.add(new StaticTextSqlNode(
                    columnPropertyMapping.createEqSqlContent(propertyNameFunction)
                            .append(" AND ")
                            .toString()));
        }

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

    @Override
    protected SqlCommandType sqlCommandType(MappedStatementMateData mappedStatementMateData) {
        return SqlCommandType.UPDATE;
    }

}
