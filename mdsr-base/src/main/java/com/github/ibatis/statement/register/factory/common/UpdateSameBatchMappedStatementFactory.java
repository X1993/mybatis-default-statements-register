package com.github.ibatis.statement.register.factory.common;

import com.github.ibatis.statement.base.core.MethodSignature;
import com.github.ibatis.statement.base.core.matedata.ColumnPropertyMapping;
import com.github.ibatis.statement.base.core.matedata.EntityMateData;
import com.github.ibatis.statement.base.core.matedata.MappedStatementMateData;
import com.github.ibatis.statement.base.logical.LogicalColumnMateData;
import com.github.ibatis.statement.mapper.KeyTableMapper;
import com.github.ibatis.statement.register.AbstractMappedStatementFactory;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.mapping.SqlSource;
import org.apache.ibatis.scripting.xmltags.*;
import org.apache.ibatis.session.Configuration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import static com.github.ibatis.statement.mapper.method.MapperMethodEnum.UPDATE_BATCH_SAME_VALUE;

/**
 * @Author: X1993
 * @Date: 2020/4/27
 */
public class UpdateSameBatchMappedStatementFactory extends AbstractMappedStatementFactory {

    @Override
    protected boolean isMatch(MappedStatementMateData mappedStatementMateData)
    {
        EntityMateData entityMateData = mappedStatementMateData.getEntityMateData();

        if (entityMateData.getPrimaryKeyCount() < 1) {
            return false;
        }
        MethodSignature methodSignature = mappedStatementMateData.getMapperMethodMateData().getMethodSignature();

        return methodSignature.isMatch(UPDATE_BATCH_SAME_VALUE.methodSignature(entityMateData));
    }

    @Override
    protected SqlSource sqlSource(MappedStatementMateData mappedStatementMateData)
    {
        Configuration configuration = mappedStatementMateData.getConfiguration();
        Function<String ,String> propertyNameFunction = name -> "param2." + name;

        SqlNode updatePrefixSqlNode = new StaticTextSqlNode(new StringBuilder("UPDATE `")
                .append(mappedStatementMateData.getEntityMateData().getTableName())
                .append("` ").toString());

        SqlNode setSqlNode = mappedStatementMateData.updateSetSqlNode(propertyNameFunction, true);

        SqlNode whereSqlNode = this.whereSqlNode(mappedStatementMateData);

        return new DynamicSqlSource(configuration ,
                new MixedSqlNode(Arrays.asList(updatePrefixSqlNode ,setSqlNode ,whereSqlNode)));
    }

    private SqlNode whereSqlNode(MappedStatementMateData mappedStatementMateData)
    {
        EntityMateData entityMateData = mappedStatementMateData.getEntityMateData();
        Configuration configuration = mappedStatementMateData.getConfiguration();

        List<SqlNode> whereSqlNodes = new ArrayList<>();
        //主键,不带if标签
        boolean primaryKeyParameterIsEntity = entityMateData.isPrimaryKeyParameterIsEntity();
        if (primaryKeyParameterIsEntity) {
            List<SqlNode> idEqSqlNodes = new ArrayList<>();
            idEqSqlNodes.add(new StaticTextSqlNode("("));
            for (ColumnPropertyMapping columnPropertyMapping : entityMateData.getKeyPrimaryColumnPropertyMappings().values())
            {
                idEqSqlNodes.add(new StaticTextSqlNode(
                        columnPropertyMapping.createEqSqlContent(name -> "item." + name)
                                .append(" AND ")
                                .toString()));
            }
            idEqSqlNodes.add(new StaticTextSqlNode("1 = 1)"));
            whereSqlNodes.add(new ForEachSqlNode(configuration, new MixedSqlNode(idEqSqlNodes),
                    "param1", "index", "item",
                    "(", ")", " OR "));
        }else {
            whereSqlNodes.add(new StaticTextSqlNode(new StringBuilder(" `")
                    .append(entityMateData.getKeyPrimaryColumnPropertyMappings()
                            .values()
                            .stream()
                            .findFirst()
                            .get()
                            .getColumnName())
                    .append("` IN ")
                    .toString()));

            whereSqlNodes.add(new ForEachSqlNode(configuration, new StaticTextSqlNode("#{item}"),
                    "param1", null, "item",
                    "(", ")", ","));
        }

        //默认过滤条件
        whereSqlNodes.add(entityMateData.defaultConditionsSqlNode(SqlCommandType.UPDATE ,
                content -> content.insert(0 ," AND ")));

        LogicalColumnMateData logicalColumnMateData = entityMateData.getLogicalColumnMateData();
        if (logicalColumnMateData != null){
            whereSqlNodes.add(new StaticTextSqlNode(
                    logicalColumnMateData.equalSqlContent(true)
                            .insert(0 ," AND ")
                            .toString()));
        }else {
            whereSqlNodes.add(new StaticTextSqlNode(" AND 1 = 1 "));
        }

        return new WhereSqlNode(configuration ,new MixedSqlNode(whereSqlNodes));
    }

    @Override
    protected SqlCommandType sqlCommandType(MappedStatementMateData mappedStatementMateData) {
        return SqlCommandType.UPDATE;
    }

}
