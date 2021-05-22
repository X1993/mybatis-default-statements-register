package com.github.ibatis.statement.mapper;

import com.github.ibatis.statement.DataSourceEnvironment;
import com.github.ibatis.statement.base.dv.DefaultValue;
import com.github.ibatis.statement.base.logical.Logical;
import com.github.ibatis.statement.mapper.param.DynamicParams;
import lombok.Data;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.session.SqlSession;
import org.junit.Assert;
import org.junit.Test;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * 测试 定义 联合主键、逻辑列、默认赋值、默认过滤的实体注册的MappedStatement是否符合预期
 * @Author: X1993
 * @Date: 2020/9/8
 */
public class Entity2Test {

    @Data
    @Logical(columnName = "removed" ,existValue = "0" ,notExistValue = "1")
    static class Entity2 {

        private String id;

        private String id2;

        private String valueOne;

        @DefaultValue(commandTypes = SqlCommandType.UPDATE ,value = "&{column} + 1")
        @DefaultValue(commandTypes = SqlCommandType.INSERT ,value = "2")
        private Integer value2;

        private String value3;

        @DefaultValue(value = "now()")
        private Date updateTime;

        @DefaultValue(value = "now()" ,commandTypes = SqlCommandType.INSERT)
        private Date createTime;

        public Entity2() {
        }

        public Entity2(String id, String id2) {
            this.id = id;
            this.id2 = id2;
        }

    }

    public interface Entity2Mapper extends KeyTableMapper<Entity2,Entity2> {
    }

    final static String SCHEMA_SQL = "DROP TABLE IF EXISTS `entity2`;\n" +
            "CREATE TABLE `entity2` (\n" +
            "  `id` varchar(50) ,\n" +
            "  `id2` varchar(50) ,\n" +
            "  `value_one` varchar(255) DEFAULT NULL,\n" +
            "  `value2` tinyint DEFAULT 0,\n" +
            "  `value3` varchar(30) DEFAULT NULL,\n" +
            "  `update_time` datetime DEFAULT NULL,\n" +
            "  `create_time` datetime DEFAULT NULL,\n" +
            "  `removed` char(1) ,\n" +
            "  CONSTRAINT table_entity2_pk PRIMARY KEY (id, id2)\n" +
            ") DEFAULT CHARSET=utf8;";

    @Test
    public void test(){
        MybatisEnvironment environment = MybatisEnvironment.ENVIRONMENT;
        environment.initTableSchema(SCHEMA_SQL);
        environment.registerMappedStatementsForMappers(Entity2Mapper.class);
        testMapper(environment);
    }

    private void testMapper(MybatisEnvironment environment)
    {
        String environmentId = environment.getSqlSession().getConfiguration().getEnvironment().getId();
        SqlSession sqlSession = environment.getSqlSession();
        Entity2Mapper mapper = sqlSession.getMapper(Entity2Mapper.class);
        Entity2 entity20 = new Entity2("1" ,"1");

        mapper.insert(entity20);
        entity20 = mapper.selectByPrimaryKey(entity20);
        Assert.assertNotNull(entity20.getCreateTime());
        Assert.assertNotNull(entity20.getUpdateTime());
        Assert.assertTrue(entity20.getValue2() == 2);

        Entity2 finalEntity2 = entity20;
        Assert.assertNotNull(mapper.selectByDynamicParams(new DynamicParams()
                .where(params -> params.eq("create_time" , finalEntity2.getCreateTime()))));

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        int i = mapper.updateByPrimaryKey(entity20);
        Assert.assertEquals(i , 1);

        Entity2 updateEntity2 = mapper.selectByPrimaryKey(entity20);

        Assert.assertTrue(updateEntity2.getValue2() == 3);
        Assert.assertEquals(entity20.getCreateTime() , updateEntity2.getCreateTime());
        Assert.assertTrue(updateEntity2.getUpdateTime().compareTo(entity20.getUpdateTime()) > 0);

        entity20 = mapper.selectByPrimaryKey(entity20);

        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Assert.assertEquals(1 ,mapper.updateByPrimaryKey(entity20));
        Assert.assertTrue(mapper.selectByPrimaryKey(entity20).getValue2() == 4);

        Assert.assertEquals(1 ,mapper.deleteByPrimaryKey(entity20));
        Assert.assertEquals(1 ,mapper.deleteByPrimaryKeyOnPhysical(entity20));

        Entity2 entity21 = new Entity2("1" ,"2");
        Entity2 entity22 = new Entity2("1" ,"3");
        Entity2 entity23 = new Entity2("1" ,"4");
        List<Entity2> entity2s = Arrays.asList(entity21, entity22, entity23);
        mapper.insertBatch(entity2s);

        List<Entity2> getExistPrimaryKeys = mapper.getExistPrimaryKeys(Arrays.asList(entity21, entity22));
        getExistPrimaryKeys.sort((e1 ,e2) -> e1.getId2().compareTo(e1.getId2()));
        Assert.assertEquals(getExistPrimaryKeys.get(0).getId2() ,"2");
        Assert.assertEquals(getExistPrimaryKeys.get(1).getId2() ,"3");

        Assert.assertEquals(mapper.selectBatchByPrimaryKey(entity2s).size() ,3);
        Assert.assertEquals(mapper.countByPrimaryKeys(entity2s) ,3);
        Assert.assertEquals(mapper.selectBatchByPrimaryKeyOnPhysical(entity2s).size() ,3);
        Assert.assertEquals(mapper.countByPrimaryKeysOnPhysical(entity2s) ,3);

        entity21 = mapper.selectByPrimaryKey(entity21);
        entity22 = mapper.selectByPrimaryKey(entity22);
        entity23 = mapper.selectByPrimaryKey(entity23);

        Assert.assertEquals(mapper.selectBatchByPrimaryKey(entity2s).size() ,3);
        Assert.assertEquals(mapper.countByPrimaryKeys(entity2s) ,3);

        Date now = new Date();
        Assert.assertNotNull(mapper.selectUniqueByDynamicParams(new DynamicParams()
                .where(conditionParams -> conditionParams
                        .eq("id" ,"1")
                        .eq("id2" ,"3")
                        .or()
                        .gt("create_time" , now)
                        .lt("create_time" ,now)
                        .like("value3" ,"12")
                        .likeLeft("value3" ,"1")
                        .likeRight("value3" ,"1")
                        .or())
                .groupBy("id" ,"id2")
                .having(conditionParams -> conditionParams.notIn("id" ,"3" ,"4")
                        .between("id2" ,"2" ,"4"))
                .asc("create_time")
                .limit(0 ,2)));

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        entity21.setValue3("2");
        entity23.setValue3("4");
        entity22.setValue3("3");

        entity2s = Arrays.asList(entity21, entity22, entity23);
        if (DataSourceEnvironment.MYSQL.name().equals(environmentId)) {
            mapper.updateBatch(entity2s);
        }else {
            for (Entity2 entity2 : entity2s) {
                mapper.updateByPrimaryKey(entity2);
            }
        }

        entity21 = mapper.selectByPrimaryKey(entity21);
        entity22 = mapper.selectByPrimaryKey(entity22);
        entity23 = mapper.selectByPrimaryKey(entity23);

        Assert.assertEquals(entity21.getValue3() ,"2");
        Assert.assertEquals(entity22.getValue3() ,"3");
        Assert.assertEquals(entity23.getValue3() ,"4");

        Assert.assertEquals(3 ,mapper.deleteBatchByPrimaryKey(entity2s));

        Assert.assertEquals(mapper.selectBatchByPrimaryKey(entity2s).size() ,0);
        Assert.assertEquals(mapper.countByPrimaryKeys(entity2s) ,0);
        Assert.assertEquals(mapper.selectBatchByPrimaryKeyOnPhysical(entity2s).size() ,3);
        Assert.assertEquals(mapper.countByPrimaryKeysOnPhysical(entity2s) ,3);

        Assert.assertEquals(3 ,mapper.deleteBatchByPrimaryKeyOnPhysical(entity2s));

        Assert.assertEquals(mapper.selectBatchByPrimaryKeyOnPhysical(entity2s).size() ,0);
        Assert.assertEquals(mapper.countByPrimaryKeysOnPhysical(entity2s) ,0);
    }

}
