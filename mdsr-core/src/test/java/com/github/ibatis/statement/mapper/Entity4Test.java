package com.github.ibatis.statement.mapper;

import com.github.ibatis.statement.DataSourceEnvironment;
import com.github.ibatis.statement.base.logical.Logical;
import org.apache.ibatis.session.SqlSession;
import org.junit.Assert;
import org.junit.Test;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 *
 * 测试 定义 联合主键、逻辑列、默认排序的实体注册的MappedStatement是否符合预期
 * @Author: junjie
 * @Date: 2020/9/8
 */
public class Entity4Test {

    @Logical(columnName = "removed" ,existValue = "0" ,notExistValue = "1")
    static class Entity4 {

        private String id;

        private String value;

        private String remark;

        private String valueOne;

        public Entity4() {
        }

        public Entity4(String id, String value, String remark) {
            this.id = id;
            this.value = value;
            this.remark = remark;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        public String getRemark() {
            return remark;
        }

        public void setRemark(String remark) {
            this.remark = remark;
        }

        public String getValueOne() {
            return valueOne;
        }

        public void setValueOne(String valueOne) {
            this.valueOne = valueOne;
        }
    }

    public interface Entity4Mapper extends KeyTableMapper<String ,Entity4> {

    }

    final static String SCHEMA_SQL = "DROP TABLE IF EXISTS `entity4`;\n" +
            "CREATE TABLE `entity4` (\n" +
            "  `id` varchar(255) PRIMARY KEY NOT NULL,\n" +
            "  `value` varchar(255) DEFAULT NULL,\n" +
            "  `removed` char(1) \n" +
            ") DEFAULT CHARSET=utf8;;";

    @Test
    public void test(){
        MybatisEnvironment environment = MybatisEnvironment.ENVIRONMENT;
        environment.initTableSchema(SCHEMA_SQL);
        environment.registerMappedStatementsForMappers(Entity4Mapper.class);
        testMapper(environment);
    }

    private void testMapper(MybatisEnvironment environment)
    {
        String environmentId = environment.getSqlSession().getConfiguration().getEnvironment().getId();
        SqlSession sqlSession = environment.getSqlSession();
        Entity4Mapper mapper = sqlSession.getMapper(Entity4Mapper.class);
        Entity4 entity4 = new Entity4("1" ,"value1" ,"rx");
        Assert.assertFalse(mapper.existByPrimaryKey(entity4.getId()));
        mapper.insert(entity4);
        Assert.assertTrue(mapper.existByPrimaryKey(entity4.getId()));
        Entity4 entity42 = new Entity4("2" ,"value1" ,null);
        mapper.insertSelective(entity42);
        Assert.assertEquals(mapper.total() ,2);

        Entity4 selective = new Entity4(null ,"value1" ,null);
        Assert.assertEquals(mapper.selectSelective(selective).size() ,2);
        Assert.assertNotNull(mapper.selectByPrimaryKey(entity4.getId()));

        mapper.updateByPrimaryKey(entity4);
        mapper.updateByPrimaryKeySelective(entity42);
        List<Entity4> entity4s = Arrays.asList(entity4, entity42);

        if (DataSourceEnvironment.MYSQL.name().equals(environmentId)) {
            mapper.updateBatch(entity4s);
        }else {
            for (Entity4Test.Entity4 entity : entity4s) {
                mapper.updateByPrimaryKey(entity);
            }
        }

        mapper.updateBatchSameValue(entity4s.stream().map(x -> x.getId()).collect(Collectors.toList()) , entity4);

        mapper.deleteByPrimaryKey(entity4.getId());
        Assert.assertFalse(mapper.existByPrimaryKey(entity4.getId()));
        Assert.assertTrue(mapper.existByPrimaryKeyOnPhysical(entity4.getId()));
        Assert.assertEquals(mapper.total() ,1);
        Assert.assertEquals(mapper.total(false) ,2);
        mapper.deleteByPrimaryKeyOnPhysical(entity42.getId());

        Assert.assertNull(mapper.selectByPrimaryKey(entity4.getId()));
        Assert.assertNotNull(mapper.selectByPrimaryKeyOnPhysical(entity4.getId()));

        Assert.assertEquals(mapper.selectSelective(selective).size() ,0);
        Assert.assertEquals(mapper.selectSelective(selective ,false).size() ,1);

        Entity4 entity43 = new Entity4("3" ,"value1" ,null);
        Entity4 entity44 = new Entity4("4" ,"value1" ,null);
        entity4s = Arrays.asList(entity43, entity44);

        Assert.assertEquals(2, mapper.insertBatch(entity4s));
        List<String> ids = entity4s.stream().map(obj -> obj.getId()).collect(Collectors.toList());
        Assert.assertEquals(2, mapper.deleteBatchByPrimaryKey(ids));

        Assert.assertEquals(mapper.selectSelective(selective).size() ,0);
        Assert.assertEquals(mapper.selectSelective(selective ,false).size() ,3);

        Assert.assertEquals(2, mapper.deleteBatchByPrimaryKeyOnPhysical(ids));
        Assert.assertEquals(mapper.selectSelective(selective ,false).size() ,1);

        List<Entity4> list = IntStream.range(10, 20)
                .mapToObj(i -> new Entity4(String.valueOf(i) ,String.valueOf(i) ,String.valueOf(i)))
                .collect(Collectors.toList());
        mapper.insertBatch(list);
        Assert.assertEquals(mapper.selectAll().size() ,10);

        mapper.deleteSelective(entity4);
        mapper.deleteSelectiveOnPhysical(entity4);
    }

}
