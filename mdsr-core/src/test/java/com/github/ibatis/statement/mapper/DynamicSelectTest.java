package com.github.ibatis.statement.mapper;

import com.github.ibatis.statement.DataSourceEnvironment;
import com.github.ibatis.statement.mapper.param.ConditionParams;
import com.github.ibatis.statement.mapper.param.DynamicParams;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.ibatis.session.SqlSession;
import org.junit.Assert;
import org.junit.Test;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * 测试 定义了复合主键的实体注册的MappedStatement是否符合预期
 * @Author: X1993
 * @Date: 2020/9/8
 */
public class DynamicSelectTest {

    @NoArgsConstructor
    @AllArgsConstructor
    @Data
    static class Entity10 {

        private Integer id;

        private String group;

    }

    @Data
    static class ValueGroupResult{

        private String group;

        private int count;

    }

    interface Entity10Mapper extends KeyTableMapper<Integer, Entity10>
    {

        List<ValueGroupResult> countGroupValue(DynamicParams dynamicParams);

    }

    final static String SCHEMA_SQL = "DROP TABLE IF EXISTS `entity10`;\n" +
            "CREATE TABLE `entity10` (\n" +
            "  `id` int(32) PRIMARY KEY NOT NULL,\n" +
            "  `group` varchar(100) DEFAULT NULL\n" +
            ") DEFAULT CHARSET=utf8;";

    @Test
    public void test() throws IOException
    {
        MybatisEnvironment environment2 = new MybatisEnvironment(DataSourceEnvironment.MYSQL);
        environment2.initTableSchema(SCHEMA_SQL);
        environment2.registerMappedStatementsForMappers(Entity10Mapper.class);
        testMapper(environment2);
        environment2.close();

        for (DataSourceEnvironment dataSourceEnvironment : DataSourceEnvironment.values())
        {
            MybatisEnvironment environment = new MybatisEnvironment(dataSourceEnvironment);
            environment.initTableSchema(SCHEMA_SQL);
            environment.registerMappedStatementsForMappers(Entity10Mapper.class);
            testMapper(environment);
            environment.close();
        }
    }

    private void testMapper(MybatisEnvironment environment)
    {
        SqlSession sqlSession = environment.getSqlSession();
        Entity10Mapper mapper = sqlSession.getMapper(Entity10Mapper.class);

        Entity10 entity101 = new Entity10(1 ,"group1");
        Entity10 entity102 = new Entity10(2 ,"group1");
        Entity10 entity103 = new Entity10(3 ,"group1");
        Entity10 entity104 = new Entity10(4 ,"group1");
        Entity10 entity105 = new Entity10(5 ,"group2");
        Entity10 entity106 = new Entity10(6 ,"group3");

        mapper.insertBatch(Arrays.asList(entity101 ,entity102 ,entity103 ,entity104 ,entity105 ,entity106));

        List<ValueGroupResult> valueGroupResults = mapper.countGroupValue(new DynamicParams()
                .addSelectElements("count(0) as `count`")
                .addSelectColumns("`group`")
                .where(new ConditionParams().customCondition("`id` < 4"))
                .groupBy("group")
                .having(new ConditionParams().customCondition("count(0) > 0 AND `group` = 'group1'")));

        Assert.assertEquals(valueGroupResults.size() ,1);
        ValueGroupResult valueGroupResult = valueGroupResults.get(0);
        Assert.assertEquals(valueGroupResult.getGroup() ,"group1");
        Assert.assertEquals(valueGroupResult.getCount() ,3);

        valueGroupResults = mapper.countGroupValue(new DynamicParams()
                .addSelectElements("count(0) as `count`")
                .addSelectColumns("group")
                .where(new ConditionParams().eq("group" ,"group1").lt("id" ,4))
                .groupBy("group")
                .having(new ConditionParams().customCondition("count(0) > 0 ")));

        Assert.assertEquals(valueGroupResults.size() ,1);
        valueGroupResult = valueGroupResults.get(0);
        Assert.assertEquals(valueGroupResult.getGroup() ,"group1");
        Assert.assertEquals(valueGroupResult.getCount() ,3);
    }

}
