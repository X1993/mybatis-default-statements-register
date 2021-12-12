package com.github.ibatis.statement.register.mysql.factory;

import com.github.ibatis.statement.base.core.MethodSignature;
import com.github.ibatis.statement.base.core.matedata.EntityMateData;
import com.github.ibatis.statement.base.core.matedata.MappedStatementMateData;
import com.github.ibatis.statement.mapper.method.MapperMethodEnum;
import com.github.ibatis.statement.register.AbstractMappedStatementFactory;
import com.github.ibatis.statement.register.mysql.AdapterProperties;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.mapping.SqlSource;
import org.apache.ibatis.scripting.xmltags.*;
import org.apache.ibatis.session.Configuration;

import java.util.LinkedList;
import java.util.List;

/**
 * @Author: X1993
 * @Date: 2020/3/13
 */
public class InsertBatchMappedStatementFactory extends AbstractMappedStatementFactory {

    @Override
    protected boolean isMatch(MappedStatementMateData mappedStatementMateData)
    {
        if (!AdapterProperties.matchDatabase(mappedStatementMateData.getEntityMateData())){
            return false;
        }

        MethodSignature methodSignature = mappedStatementMateData.getMapperMethodMateData().getMethodSignature();
        EntityMateData entityMateData = mappedStatementMateData.getEntityMateData();
        return methodSignature.isMatch(MapperMethodEnum.INSERT_BATCH.methodSignature(entityMateData));
    }

    /**
     * insert into `tableName` (col1 ,col2 ,col3) values
     * <foreach collection="collection" item="item" separator=",">
     *  (#{item.propertyName1,jdbcType=XXX},#{item.propertyName2,jdbcType=XXX},defaultValue3)
     * </foreach>
     * @param mappedStatementMateData
     * @return
     */
    @Override
    protected SqlSource sqlSource(MappedStatementMateData mappedStatementMateData)
    {
        Configuration configuration = mappedStatementMateData.getConfiguration();
        List<SqlNode> sqlNodes = new LinkedList<>();
        sqlNodes.add(new StaticTextSqlNode(new StringBuilder("INSERT INTO `")
                .append(mappedStatementMateData.getEntityMateData().getTableName())
                .append("` ")
                .toString()));

        List<SqlNode> columnSqlNodes = new LinkedList<>();
        List<SqlNode> propertySqlNodes = new LinkedList<>();
        String item = "item";
        mappedStatementMateData.insertColumnValueSqlNodes(columnSqlNodes ,propertySqlNodes ,
                propertyName -> item + "." + propertyName ,false);

        sqlNodes.add(new TrimSqlNode(mappedStatementMateData.getConfiguration() ,
                new MixedSqlNode(columnSqlNodes) ," (" , null,
                ") " ,","));
        sqlNodes.add(new StaticTextSqlNode(" VALUES "));
        sqlNodes.add(new ForEachSqlNode(mappedStatementMateData.getConfiguration() ,
                new TrimSqlNode(configuration ,new MixedSqlNode(propertySqlNodes) ,"(" ,
                        null ,")" ,",") ,
                "collection" ,null ,item ,null ,null ,","));

        return new DynamicSqlSource(mappedStatementMateData.getConfiguration() ,new MixedSqlNode(sqlNodes));
    }

    @Override
    protected SqlCommandType sqlCommandType(MappedStatementMateData mappedStatementMateData) {
        return SqlCommandType.INSERT;
    }

}
