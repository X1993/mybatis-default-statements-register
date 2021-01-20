package com.github.ibatis.statement.register.factory;

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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.lang.reflect.Type;
import java.util.*;
import java.util.function.Function;

/**
 * @see KeyTableMapper#updateBatchSameValue(Collection, Object)
 * @Author: junjie
 * @Date: 2020/4/27
 */
public class UpdateSameBatchMappedStatementFactory extends AbstractUpdateMappedStatementFactory {

    private final static Logger LOGGER = LoggerFactory.getLogger(UpdateSameBatchMappedStatementFactory.class);

    public static final String UPDATE_BATCH_SAME_VALUE = "updateBatchSameValue";

    @Override
    protected boolean isMatch(MappedStatementMateData mappedStatementMateData)
    {
        EntityMateData entityMateData = mappedStatementMateData.getEntityMateData();
        Class<?> entityClass = entityMateData.getEntityClass();

        if (entityMateData.getPrimaryKeyCount() < 1) {
            return false;
        }
        Class<?> reasonableKeyParameterClass = entityMateData.getReasonableKeyParameterClass();

        return super.isMatchMethodSignature(mappedStatementMateData.getMapperMethodMateData().getMethodSignature() ,
                new MethodSignature(int.class , UPDATE_BATCH_SAME_VALUE, ParameterizedTypeImpl.make(Collection.class ,
                        new Type[]{reasonableKeyParameterClass} ,null) ,entityClass))
                && entityMateData.getPrimaryKeyCount() > 0;
    }

    @Override
    protected SqlSource sqlSource(MappedStatementMateData mappedStatementMateData)
    {
        Configuration configuration = mappedStatementMateData.getConfiguration();
        Function<String ,String> propertyNameFunction = name -> "param2." + name;

        SqlNode updatePrefixSqlNode = new StaticTextSqlNode(new StringBuilder("UPDATE `")
                .append(mappedStatementMateData.getEntityMateData().getTableName())
                .append("` ").toString());

        SqlNode setSqlNode = super.getSetSqlNode(mappedStatementMateData, propertyNameFunction, true);

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

}
