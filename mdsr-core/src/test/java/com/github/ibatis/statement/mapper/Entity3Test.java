package com.github.ibatis.statement.mapper;

import org.apache.ibatis.session.SqlSession;
import org.junit.Assert;
import org.junit.Test;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * 测试 定义了复合主键、默认排序的实体注册的MappedStatement是否符合预期
 * @Author: junjie
 * @Date: 2020/9/8
 */
public class Entity3Test {

    static class Entity3 {

        private int id1;

        private int id2;

        private String value;

        private String value2;

        public Entity3(int id1, int id2, String value) {
            this.id1 = id1;
            this.id2 = id2;
            this.value = value;
        }

        public Entity3() {
        }

        public int getId1() {
            return id1;
        }

        public void setId1(int id1) {
            this.id1 = id1;
        }

        public int getId2() {
            return id2;
        }

        public void setId2(int id2) {
            this.id2 = id2;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        public String getValue2() {
            return value2;
        }

        public void setValue2(String value2) {
            this.value2 = value2;
        }

    }

    interface Entity3Mapper extends KeyTableMapper<Entity3, Entity3> {}

    final static String SCHEMA_SQL = "DROP TABLE IF EXISTS `entity3`;\n" +
            "CREATE TABLE `entity3` (\n" +
            "  `id1` int(32) ,\n" +
            "  `id2` int(32) ,\n" +
            "  `value` varchar(30) DEFAULT NULL,\n" +
            "  `value2` varchar(30) DEFAULT NULL,\n" +
            "  PRIMARY KEY (`id1` ,`id2`)\n" +
            ") ENGINE=InnoDB DEFAULT CHARSET=utf8;";

    @Test
    public void mysqlTest(){
        MybatisEnvironment environment = MybatisEnvironment.ENVIRONMENT;
        environment.initTableSchema(SCHEMA_SQL);
        environment.registerMappedStatementsForMappers(Entity3Mapper.class);
        testMapper(environment.getSqlSession());
    }

    private void testMapper(SqlSession sqlSession)
    {
        Entity3Mapper mapper = sqlSession.getMapper(Entity3Mapper.class);
        Entity3 entity31 = new Entity3(1 ,1 ,"1");
        Entity3 entity32 = new Entity3(2 ,2 ,null);

        Assert.assertFalse(mapper.existByPrimaryKey(entity31));
        mapper.insert(entity31);
        mapper.insertSelective(entity32);
        Assert.assertTrue(mapper.existByPrimaryKey(entity31));

        Assert.assertEquals(mapper.total() ,2);
        Assert.assertEquals(mapper.total(false) ,2);

        Assert.assertNotNull(mapper.selectByPrimaryKey(entity31).getValue());
        entity32.setValue("1");
        mapper.updateByPrimaryKey(entity31);
        mapper.updateByPrimaryKeySelective(entity32);
        List<Entity3> entity3s = Arrays.asList(entity31, entity32);
        mapper.updateBatch(entity3s);
        mapper.updateBatchSameValue(entity3s, entity32);

        Assert.assertNotNull(mapper.selectSelective(entity31));

        mapper.deleteByPrimaryKey(entity31);
        Assert.assertEquals(mapper.total() ,1);
        Assert.assertEquals(mapper.totalSelective(entity31) ,0);
        Assert.assertEquals(mapper.totalSelective(entity31,false) ,0);
        mapper.deleteByPrimaryKeyOnPhysical(entity32);

        Assert.assertNull(mapper.selectByPrimaryKey(entity31));
        Assert.assertNull(mapper.selectByPrimaryKey(entity32));

        Assert.assertEquals(mapper.insertBatch(entity3s) , entity3s.size());
        Assert.assertEquals(mapper.deleteBatchByPrimaryKey(entity3s) , entity3s.size());
        Assert.assertEquals(mapper.deleteBatchByPrimaryKeyOnPhysical(entity3s) ,0);

        List<Entity3> list = IntStream.range(10, 20)
                .mapToObj(i -> new Entity3(i ,i ,String.valueOf(i)))
                .collect(Collectors.toList());
        mapper.insertBatch(list);
        Assert.assertEquals(mapper.selectAll().size() ,10);
        Entity3 condition = new Entity3();
        condition.setId1(11);
        condition.setValue("value");
        Assert.assertEquals(mapper.selectSelective(condition).size() ,0);

        mapper.deleteSelective(entity31);
        mapper.deleteSelective(entity32);
    }

}