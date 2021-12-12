package com.github.ibatis.statement.register.mysql.factory;

import com.github.ibatis.statement.base.core.MethodSignature;
import com.github.ibatis.statement.base.core.matedata.MappedStatementMateData;
import com.github.ibatis.statement.mapper.method.MapperMethodEnum;
import com.github.ibatis.statement.register.AbstractMappedStatementFactory;
import com.github.ibatis.statement.register.mysql.AdapterProperties;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.mapping.SqlSource;
import org.apache.ibatis.scripting.xmltags.DynamicSqlSource;
import org.apache.ibatis.scripting.xmltags.ForEachSqlNode;

/**
 * @Author: X1993
 * @Date: 2020/4/27
 */
public class UpdateBatchMappedStatementFactory extends AbstractMappedStatementFactory {

    @Override
    protected boolean isMatch(MappedStatementMateData mappedStatementMateData) {
        if (!AdapterProperties.matchDatabase(mappedStatementMateData.getEntityMateData())){
            return false;
        }
        MethodSignature methodSignature = mappedStatementMateData.getMapperMethodMateData().getMethodSignature();
        return methodSignature.isMatch(MapperMethodEnum.UPDATE_BATCH.methodSignature(mappedStatementMateData.getEntityMateData()));
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
                mappedStatementMateData.updateSqlNode(name -> "item." + name ,false) ,
                "collection" ,"index" ,"item" ,
                null ,null ,";"));
    }

    @Override
    protected SqlCommandType sqlCommandType(MappedStatementMateData mappedStatementMateData) {
        return SqlCommandType.UPDATE;
    }

}
