package com.github.ibatis.statement.register.factory;

import com.github.ibatis.statement.base.core.MethodSignature;
import com.github.ibatis.statement.base.core.matedata.MappedStatementMateData;
import com.github.ibatis.statement.mapper.KeyTableMapper;
import com.github.ibatis.statement.util.reflect.ParameterizedTypeImpl;
import org.apache.ibatis.mapping.*;
import org.apache.ibatis.scripting.xmltags.*;
import java.lang.reflect.Type;
import java.util.Collection;

/**
 * @see KeyTableMapper#updateBatch(Collection)
 * @Author: junjie
 * @Date: 2020/4/27
 */
public class UpdateBatchMappedStatementFactory extends AbstractUpdateMappedStatementFactory {

    public static final String MATCH_METHOD_NAME = "updateBatch";

    @Override
    protected boolean isMatch(MappedStatementMateData mappedStatementMateData) {
        return super.isMatchMethodSignature(mappedStatementMateData.getMapperMethodMateData().getMethodSignature() ,
                new MethodSignature(int.class , MATCH_METHOD_NAME, ParameterizedTypeImpl.make(Collection.class ,
                        new Type[]{mappedStatementMateData.getEntityMateData().getEntityClass()} ,null)));
    }

    /**
        <foreach collection="list" item="item" separator=";">
            update table
                set
                    col1 = #{item.propertyName1,jdbcType=XXX},
                    col2 = #{item.propertyName2,jdbcType=XXX},
                    ...
                    col5 = defaultValue5
                    ...
                    where
                    primaryKey1 = #{item.keyPropertyName1,jdbcType=XXX},
                    ...
                    and col7 = defaultValue7
                     ...
                    (and logicalCol = existValue)
        </foreach
     */
    @Override
    protected SqlSource sqlSource(MappedStatementMateData mappedStatementMateData)
    {
        return new DynamicSqlSource(mappedStatementMateData.getConfiguration() ,new ForEachSqlNode(
                mappedStatementMateData.getConfiguration() ,
                super.createSqlNode(mappedStatementMateData ,name -> "item." + name ,false) ,
                "list" ,"index" ,"item" ,
                null ,null ,";"));
    }

    @Override
    protected SqlCommandType sqlCommandType(MappedStatementMateData mappedStatementMateData) {
        return SqlCommandType.UPDATE;
    }
}
