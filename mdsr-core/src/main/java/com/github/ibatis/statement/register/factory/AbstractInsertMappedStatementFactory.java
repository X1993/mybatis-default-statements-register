package com.github.ibatis.statement.register.factory;

import com.github.ibatis.statement.base.core.matedata.ColumnMateData;
import com.github.ibatis.statement.base.core.matedata.ColumnPropertyMapping;
import com.github.ibatis.statement.base.core.matedata.EntityMateData;
import com.github.ibatis.statement.base.core.matedata.MappedStatementMateData;
import com.github.ibatis.statement.base.dv.ColumnDefaultValue;
import com.github.ibatis.statement.base.logical.LogicalColumnMateData;
import com.github.ibatis.statement.register.AbstractMappedStatementFactory;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.mapping.SqlSource;
import org.apache.ibatis.scripting.xmltags.*;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * @author junjie
 * @date 2020/9/16
 */
public abstract class AbstractInsertMappedStatementFactory extends AbstractMappedStatementFactory {

    @Override
    protected SqlCommandType sqlCommandType(MappedStatementMateData mappedStatementMateData) {
        return SqlCommandType.INSERT;
    }

    protected void fillSqlNodes(MappedStatementMateData mappedStatementMateData ,
                                List<SqlNode> columnSqlNodes ,List<SqlNode> propertySqlNodes ,
                                Function<String ,String> propertyNameFunction ,boolean isSelective)
    {
        EntityMateData entityMateData = mappedStatementMateData.getEntityMateData();
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
     * @param mappedStatementMateData
     * @param propertyNameFunction
     * @param isSelective
     * @return
     */
    protected SqlSource createSqlSource(MappedStatementMateData mappedStatementMateData ,
                                        Function<String ,String> propertyNameFunction ,
                                        boolean isSelective)
    {
        List<SqlNode> columnSqlNodes = new LinkedList<>();
        List<SqlNode> propertySqlNodes = new LinkedList<>();
        this.fillSqlNodes(mappedStatementMateData ,columnSqlNodes ,propertySqlNodes ,propertyNameFunction ,isSelective);

        List<SqlNode> sqlNodes = new LinkedList<>();
        sqlNodes.add(new StaticTextSqlNode(new StringBuilder("INSERT INTO `")
                .append(mappedStatementMateData.getEntityMateData().getTableName())
                .append("` ")
                .toString()));

        sqlNodes.add(new TrimSqlNode(mappedStatementMateData.getConfiguration() ,
                new MixedSqlNode(columnSqlNodes) ," (" , null,
                ") " ,","));
        sqlNodes.add(new TrimSqlNode(mappedStatementMateData.getConfiguration() ,
                new MixedSqlNode(propertySqlNodes) ," VALUES (" , null,
                ")" ,","));

        return new DynamicSqlSource(mappedStatementMateData.getConfiguration() ,new MixedSqlNode(sqlNodes));
    }

}
