package com.github.ibatis.statement.register.factory;

import com.github.ibatis.statement.base.core.MethodSignature;
import com.github.ibatis.statement.base.core.matedata.MappedStatementMateData;
import com.github.ibatis.statement.mapper.TableMapper;
import com.github.ibatis.statement.util.reflect.ParameterizedTypeImpl;
import org.apache.ibatis.mapping.*;
import org.apache.ibatis.scripting.xmltags.*;
import org.apache.ibatis.session.Configuration;
import java.lang.reflect.Type;
import java.util.*;

/**
 * @see TableMapper#insertBatch(Collection)
 * @Author: junjie
 * @Date: 2020/3/13
 */
public class InsertBatchMappedStatementFactory extends AbstractInsertMappedStatementFactory {

    public final static String INSERT_BATCH_METHOD_NAME = "insertBatch";

    @Override
    protected boolean isMatch(MappedStatementMateData mappedStatementMateData)
    {
        return super.isMatchMethodSignature(mappedStatementMateData.getMapperMethodMateData().getMethodSignature() ,
                new MethodSignature(int.class ,INSERT_BATCH_METHOD_NAME , ParameterizedTypeImpl.make(Collection.class ,
                new Type[]{mappedStatementMateData.getEntityMateData().getEntityClass()} ,null)));
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
        this.fillSqlNodes(mappedStatementMateData ,columnSqlNodes ,propertySqlNodes ,
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

}
