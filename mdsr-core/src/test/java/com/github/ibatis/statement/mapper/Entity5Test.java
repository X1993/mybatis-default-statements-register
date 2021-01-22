package com.github.ibatis.statement.mapper;

import com.github.ibatis.statement.DataSourceEnvironment;
import com.github.ibatis.statement.base.core.Column;
import com.github.ibatis.statement.base.core.Entity;
import com.github.ibatis.statement.base.core.MappingStrategy;
import com.github.ibatis.statement.base.core.TableSchemaResolutionStrategy;
import com.github.ibatis.statement.mapper.param.ConditionParams;
import com.github.ibatis.statement.mapper.param.DynamicParams;
import lombok.Data;
import org.apache.ibatis.session.SqlSession;
import org.junit.Assert;
import org.junit.Test;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * 测试 定义 联合主键、逻辑列、默认排序的实体注册的MappedStatement是否符合预期
 * @Author: X1993
 * @Date: 2020/9/8
 */
public class Entity5Test {

    @Removed
    @Data
    @Entity(resolutionStrategy = TableSchemaResolutionStrategy.ENTITY)
    static class Entity5 {

        @Column(mappingStrategy = MappingStrategy.PRIMARY_KEY)
        private String id;

        @Column(mappingStrategy = MappingStrategy.PRIMARY_KEY)
        private String id2;

        private String valueOne;

        private String value2;

        public Entity5(String id, String id2) {
            this.id = id;
            this.id2 = id2;
        }

        public Entity5() {
        }

        public Entity5(String id, String id2, String valueOne, String value2) {
            this.id = id;
            this.id2 = id2;
            this.valueOne = valueOne;
            this.value2 = value2;
        }
    }

    interface Entity5Mapper extends KeyTableMapper<Entity5,Entity5> {}

    final static String SCHEMA_SQL = "DROP TABLE IF EXISTS `entity5`;\n" +
            "CREATE TABLE `entity5` (\n" +
            "  `id` varchar(50) ,\n" +
            "  `id2` varchar(50) ,\n" +
            "  `value_one` varchar(255) DEFAULT NULL,\n" +
            "  `value2` varchar(30) DEFAULT NULL,\n" +
            "  `value3` varchar(30) DEFAULT NULL,\n" +
            "  `removed` char(1) ,\n" +
            "  CONSTRAINT table_entity5_pk PRIMARY KEY (id, id2) \n" +
            ") DEFAULT CHARSET=utf8; ";

    @Test
    public void test(){
        MybatisEnvironment environment = MybatisEnvironment.ENVIRONMENT;
        environment.initTableSchema(SCHEMA_SQL);
        environment.registerMappedStatementsForMappers(Entity5Mapper.class);
        testMapper(environment);
    }

    private void testMapper(MybatisEnvironment environment)
    {
        String environmentId = environment.getSqlSession().getConfiguration().getEnvironment().getId();
        SqlSession sqlSession = environment.getSqlSession();
        Entity5Mapper mapper = sqlSession.getMapper(Entity5Mapper.class);

        Entity5 entity51 = new Entity5("1" ,"1");
        Entity5 entity52 = new Entity5("2" ,"2");

        Assert.assertFalse(mapper.existByPrimaryKey(entity51));
        mapper.insert(entity51);
        Assert.assertTrue(mapper.existByPrimaryKey(entity51));
        mapper.insertSelective(entity52);

        Entity5 selective = new Entity5();
        selective.setValueOne("1");
        Assert.assertEquals(mapper.selectSelective(selective).size() ,0);

        entity51.setValueOne("1");
        mapper.updateByPrimaryKey(entity51);
        entity52.setValue2("2");
        mapper.updateByPrimaryKeySelective(entity52);
        List<Entity5> entity5s = Arrays.asList(entity51, entity52);

        if (DataSourceEnvironment.MYSQL.name().equals(environmentId)) {
            mapper.updateBatch(entity5s);
        }else {
            for (Entity5Test.Entity5 entity : entity5s) {
                mapper.updateByPrimaryKey(entity);
            }
        }

        Assert.assertEquals(mapper.totalSelective(entity51) ,1);
        mapper.updateBatchSameValue(entity5s, entity51);

        Assert.assertEquals(mapper.selectSelective(selective).size() ,2);
        Assert.assertNotNull(mapper.selectByPrimaryKey(entity51));

        mapper.deleteByPrimaryKey(entity51);
        Assert.assertTrue(mapper.existByPrimaryKeyOnPhysical(entity51));
        mapper.deleteByPrimaryKeyOnPhysical(entity52);
        Assert.assertFalse(mapper.existByPrimaryKeyOnPhysical(entity52));

        Assert.assertNull(mapper.selectByPrimaryKey(entity51));
        Assert.assertNotNull(mapper.selectByPrimaryKeyOnPhysical(entity51));

        Assert.assertEquals(mapper.selectSelective(selective).size() ,0);
        Assert.assertEquals(mapper.selectSelective(selective ,false).size() ,1);

        Entity5 entity53 = new Entity5("3" ,"3");
        Entity5 entity54 = new Entity5("4" ,"4");
        entity5s = Arrays.asList(entity53, entity54);

        Assert.assertEquals(2, mapper.insertBatch(entity5s));

        Assert.assertEquals(mapper.countByDynamicParams(new DynamicParams()
                .where(new ConditionParams()
                        .in("id" ,Arrays.asList("3" ,"4"))
                        .ne("id"))
                .groupBy("id")
                .having(new ConditionParams()
                        .eq("id" ,3)
                        .notNull("id"))),1);

        Assert.assertEquals(2, mapper.deleteBatchByPrimaryKey(entity5s));

        selective = new Entity5();
        Assert.assertEquals(mapper.selectSelective(selective).size() ,0);
        Assert.assertEquals(mapper.totalSelective(selective ,false) ,3);
        Assert.assertEquals(mapper.total(false) ,3);
        Assert.assertEquals(mapper.selectSelective(selective ,false).size() ,3);

        Assert.assertEquals(2, mapper.deleteBatchByPrimaryKeyOnPhysical(entity5s));
        Assert.assertEquals(mapper.selectSelective(selective ,false).size() ,1);

        List<Entity5> list = IntStream.range(10, 20)
                .mapToObj(i -> new Entity5(String.valueOf(i) ,String.valueOf(i)))
                .collect(Collectors.toList());
        mapper.insertBatch(list);

        Assert.assertEquals(mapper.selectAll().size() ,10);
        Entity5 condition = new Entity5();
        condition.setId("12");
        condition.setValue2("value");
        Assert.assertEquals(mapper.selectSelective(condition).size() ,0);

        mapper.deleteSelective(entity51);
        mapper.deleteByPrimaryKeyOnPhysical(entity51);
    }

}
