package com.github.mdsr.sample.env;

import com.github.ibatis.statement.base.condition.ColumnConditionParser;
import com.github.ibatis.statement.base.condition.SpecificColumnConditionParser;
import com.github.ibatis.statement.base.core.matedata.MappedStatementMateData;
import com.github.ibatis.statement.mapper.param.ConditionRule;
import com.github.ibatis.statement.register.DefaultStatementAutoRegister;
import org.apache.ibatis.mapping.SqlCommandType;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author X1993
 * @date 2020/9/13
 */

@MapperScan("com.github.mdsr.sample.mapper")
@Configuration
public class MybatisConfiguration {

    /**
     * 执行查询指令时，如果`create_time`列没有指定查询条件，添加默认查询条件 create_time > '2020-08-12 00:00:00'
     * @return
     */
//    @Bean
//    public ColumnConditionParser selectColumnConditionParser(){
//        return new SpecificColumnConditionParser(
//                columnMateData -> "create_time".equals(columnMateData.getColumnName()) ,
//                new SqlCommandType[]{SqlCommandType.SELECT}, ConditionRule.GT, "2020-08-12 00:00:00");
//    }

    /**
     * 执行修改指令时，如果`create_time`列没有指定查询条件，添加默认查询条件 create_time between '2020-08-11 00:00:00' AND '2020-08-12 00:00:00'
     * @return
     */
//    @Bean
//    public ColumnConditionParser updateColumnConditionParser(){
//        return new SpecificColumnConditionParser(
//                columnMateData -> "create_time".equals(columnMateData.getColumnName()) ,
//                new SqlCommandType[]{SqlCommandType.UPDATE}, ConditionRule.BETWEEN, "'2020-08-11 00:00:00' AND '2020-08-12 00:00:00'");
//    }

    /**
     * 执行删除指令时，如果`create_time`列没有指定查询条件，添加默认查询条件 create_time < '2020-08-12 00:00:00'
     * @return
     */
//    @Bean
//    public ColumnConditionParser deleteColumnConditionParser(){
//        return new SpecificColumnConditionParser(
//                columnMateData -> "create_time".equals(columnMateData.getColumnName()) ,
//                new SqlCommandType[]{SqlCommandType.DELETE}, ConditionRule.LT, "2020-08-11 00:00:00");
//    }

    /**
     * 列名为`update_time`的列在执行新增和修改指令时，如果没有指定值，使用默认值now()
     * @return
     */
//    @Bean
//    public ColumnValueParser updateTimeColumnValueParser(){
//        return new SpecificColumnValueParser(columnMateData -> "update_time".equals(columnMateData.getColumnName()) ,
//                new SqlCommandType[]{SqlCommandType.UPDATE ,SqlCommandType.INSERT} ,"now()" ,false);
//    }

    /**
     * 列名为`create_time`的列在执行新增指令时，如果没有指定值，使用默认值now()
     * @return
     */
//    @Bean
//    public ColumnValueParser createTimeColumnValueParser(){
//        return new SpecificColumnValueParser(columnMateData ->
//                "create_time".equals(columnMateData.getColumnName()) ,
//                new SqlCommandType[]{SqlCommandType.INSERT} ,"now()" ,false);
//    }

    /**
     * 列名为`removed`的列为逻辑列
     * @return
     */
//    @Bean
//    public LogicalColumnMateDataParser removedLogicalColumnMateDataParser(){
//        return new SpecificLogicalColumnMateDataParser("removed" ,"0" ,"1");
//    }

    /**
     * 注册实体类默认映射的表名
     */
//    @Bean
//    public TableSourceParser actTableNameParser(){
//        return entityClass ->  Optional.of(new TableSourceParser.Source(
//                "act_" + StringUtils.camelCaseToUnderscore(entityClass.getName())
//                )
//        );
//    }

//    @Bean
//    public DefaultStatementAutoRegister.Listener listener(){
//        return new DefaultStatementAutoRegister.Listener() {
//            @Override
//            public void registerMappedStatementFail(MappedStatementMateData mappedStatementMateData) {
//                throw new IllegalArgumentException();
//            }
//        };
//    }

}
