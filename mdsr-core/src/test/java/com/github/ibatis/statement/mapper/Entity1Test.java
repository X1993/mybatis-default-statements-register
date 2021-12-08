package com.github.ibatis.statement.mapper;

import com.github.ibatis.statement.DataSourceEnvironment;
import com.github.ibatis.statement.base.core.Column;
import lombok.Data;
import org.apache.ibatis.session.SqlSession;
import org.junit.Assert;
import org.junit.Test;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * 测试 简单实体注册的MappedStatement是否符合预期
 * @Author: X1993
 * @Date: 2020/9/8
 */
public class Entity1Test {

    @Data
    static class Entity1 {

        private Integer id;

        private String value;

        @Column(value = "value_one")
        private String value1;

        public Entity1() {
        }

        public Entity1(Integer id, String value) {
            this.id = id;
            this.value = value;
        }
    }

    /**
     * 只需要方法前面匹配，并没有要求实现指定接口
     * @param <K>
     * @param <T>
     */
    interface CustomMapper<K ,T> {

        /**
         * 根据主键删除（如果定义了逻辑列则为逻辑删除，否则物理删除）
         * @param key
         * @return
         */
        int deleteByPrimaryKey(K key);

        /**
         * 根据主键物理删除
         * @param key
         * @return
         */
        int deleteByPrimaryKeyOnPhysical(K key);

        /**
         * 根据主键批量删除（根据有无逻辑列执行逻辑删除或物理删除）
         * @return
         */
        int deleteBatchByPrimaryKey(List<K> keys);

        /**
         * 根据主键批量删除 （物理删除）
         * @param keys
         * @return
         */
        int deleteBatchByPrimaryKeyOnPhysical(List<? extends K> keys);

        /**
         * 新增
         * @param t
         * @return
         */
        int insert(T t);

        /**
         * 条件插入不为空的数据
         * @param t
         * @return
         */
        int insertSelective(T t);

        /**
         * 批量新增
         * @param list
         * @return
         */
        int insertBatch(List<? extends T> list);

        /**
         * 将对象属性值作为查询条件，查询总数
         * 条件：1.非空属性，2.有对应的列
         * 注意：基本类型是一定会满足1.非空 条件的
         * @param condition
         * @param logicalExist 是否只查询逻辑存在
         * @return
         */
        int totalSelective(T condition, boolean logicalExist);

        default int totalSelective(T condition){
            return totalSelective(condition ,true);
        }

        /**
         * 查询总数
         * @param logicalExist 是否只查询逻辑存在
         * @return
         */
        default int total(boolean logicalExist){
            return totalSelective(null ,logicalExist);
        }

        default int total(){
            return total(true);
        }

        /**
         * 将对象属性值作为查询条件
         * 条件：1.非空属性，2.有对应的列
         * 注意：基本类型是一定会满足1.非空 条件的
         * @param condition
         * @param logicalExist 是否只查询逻辑存在
         * @return
         */
        Collection<T> selectSelective(T condition, boolean logicalExist);

        default Collection<T> selectSelective(T condition){
            return selectSelective(condition ,true);
        }

        /**
         * @throws org.apache.ibatis.exceptions.TooManyResultsException 如果存在多个满足条件的结果
         */
        default T selectSelectiveOne(T condition){
            return selectSelective(condition ,true)
                    .stream()
                    .findFirst()
                    .orElse(null);
        }

        /**
         * 查询最大的主键
         * @return
         */
        K selectMaxPrimaryKey();

    }

    interface Entity1Mapper extends CustomMapper<Integer, Entity1>, EntityType<Entity1> {

        /**
         * 根据主键查询 (如果有逻辑列，只查询逻辑存在的)
         * @param key 主键
         * @return
         */
        Entity1 selectByPrimaryKey(Integer key);

        /**
         * 根据主键物理查询
         * @param key
         * @return
         */
        Entity1 selectByPrimaryKeyOnPhysical(Integer key);

        /**
         * 修改属性不为空的数据
         * @param t
         * @return
         */
        int updateByPrimaryKeySelective(Entity1 t);

        /**
         * 修改数据
         * @param t
         * @return
         */
        int updateByPrimaryKey(Entity1 t);

        /**
         * 判断指定主键的数据是否存在(如果有逻辑列，只查询逻辑存在的)
         * @param key
         * @return
         */
        boolean existByPrimaryKey(Integer key);

        /**
         * 判断指定主键的数据是否物理存在
         * @param key
         * @return
         */
        boolean existByPrimaryKeyOnPhysical(Integer key);

        /**
         * 根据主键集查询匹配的行数(如果有逻辑列，只统计逻辑存在的)
         * @param keys
         * @return
         */
        int countByPrimaryKeys(Collection<Integer> keys);

        /**
         * 根据主键集查询匹配的行数（包含逻辑删除的行）
         * @param keys
         * @return
         */
        int countByPrimaryKeysOnPhysical(Collection<Integer> keys);

        /**
         * 返回已存在的主键 (如果有逻辑列，只统计逻辑存在的)
         * @param keys
         * @return
         */
        Set<Integer> getExistPrimaryKeys(Collection<Integer> keys);

        /**
         * 返回已存在的主键（包含逻辑删除的行）
         * @param keys
         * @return
         */
        Set<Integer> getExistPrimaryKeysOnPhysical(Collection<Integer> keys);

        /**
         * 根据主键集批量查询(如果有逻辑列，只查询逻辑存在的)
         * @param keys
         * @return
         */
        List<Entity1> selectBatchByPrimaryKey(Collection<Integer> keys);

        /**
         * 根据主键集批量物理查询
         * @param keys
         * @return
         */
        List<Entity1> selectBatchByPrimaryKeyOnPhysical(Collection<Integer> keys);

        /**
         * 批量修改，set值为每个元素不为空的值
         * @param list
         * @return
         */
        int updateBatch(Collection<Entity1> list);

        /**
         * 批量修改
         * @param list
         * @param update
         * @return
         */
        int updateBatchSameValue(Collection<Integer> list, Entity1 update);

        /**
         * 批量删除（根据有无逻辑列执行逻辑删除或物理删除）
         * @param condition 利用非空属性作为查询条件，将符合条件的记录删除
         * @return
         */
        int deleteSelective(Entity1 condition);

        /**
         * 批量物理删除
         * @param condition 利用非空属性作为查询条件，将符合条件的记录删除
         * @return
         */
        int deleteSelectiveOnPhysical(Entity1 condition);

    }

    final static String SCHEMA_SQL = "DROP TABLE IF EXISTS `entity1`;\n" +
            "CREATE TABLE `entity1` (\n" +
            "  `id` int(32) PRIMARY KEY NOT NULL,\n" +
            "  `value` varchar(255) DEFAULT NULL,\n" +
            "  `value_one` varchar(255) DEFAULT NULL\n" +
            ") DEFAULT CHARSET=utf8;";

    @Test
    public void test() throws IOException
    {
        MybatisEnvironment environment = new MybatisEnvironment(DataSourceEnvironment.defaultDatabase());
        environment.initTableSchema(SCHEMA_SQL);
        environment.registerMappedStatementsForMappers(Entity1Mapper.class);
        testMapper(environment.getSqlSession());
        environment.close();
    }

    private void testMapper(SqlSession sqlSession)
    {
        Entity1Mapper mapper = sqlSession.getMapper(Entity1Mapper.class);
        Entity1 entity1 = new Entity1(1 ,"1");
        Entity1 entity12 = new Entity1(2 ,"2");

        Assert.assertFalse(mapper.existByPrimaryKey(entity1.getId()));
        mapper.insert(entity1);
        mapper.insert(entity12);
        Assert.assertTrue(mapper.existByPrimaryKey(entity1.getId()));

        Assert.assertEquals(mapper.selectBatchByPrimaryKey(Arrays.asList(1 ,2)).size() ,2);
        Assert.assertTrue(mapper.getExistPrimaryKeys(Arrays.asList(1 ,4)).stream().findFirst().get() == 1);
        Assert.assertTrue(mapper.getExistPrimaryKeysOnPhysical(Arrays.asList(1 ,4)).stream().findFirst().get() == 1);
        Assert.assertEquals(mapper.countByPrimaryKeys(Arrays.asList(1 ,2)) ,2);
        Assert.assertEquals(mapper.selectBatchByPrimaryKeyOnPhysical(Arrays.asList(1 ,4)).size() ,1);
        Assert.assertEquals(mapper.countByPrimaryKeysOnPhysical(Arrays.asList(3 ,2)) ,1);

        Assert.assertNotNull(mapper.selectByPrimaryKey(entity1.getId()));
        Assert.assertEquals(mapper.total() ,2);

        Assert.assertTrue(mapper.selectMaxPrimaryKey() == 2);

        entity1.setValue1("12");
        mapper.updateByPrimaryKey(entity1);
        entity1.setValue1("13");
        mapper.updateByPrimaryKeySelective(entity1);
        List<Entity1> entity1s = Arrays.asList(entity1);
        mapper.updateBatch(entity1s);
        mapper.updateBatchSameValue(entity1s
                .stream()
                .map(s -> s.getId())
                .collect(Collectors.toList()), entity1);

        Assert.assertEquals(mapper.totalSelective(entity1) ,1);
        Assert.assertNotNull(mapper.selectSelectiveOne(entity1));

        mapper.deleteByPrimaryKey(entity1.getId());
        mapper.deleteByPrimaryKeyOnPhysical(entity12.getId());
        Assert.assertNull(mapper.selectByPrimaryKey(entity1.getId()));
        Assert.assertNull(mapper.selectByPrimaryKey(entity12.getId()));

        Assert.assertEquals(mapper.insertBatch(entity1s) , entity1s.size());
        List<Integer> ids = entity1s.stream().map(s -> s.getId()).collect(Collectors.toList());
        Assert.assertEquals(mapper.deleteBatchByPrimaryKey(ids) , entity1s.size());
        Assert.assertEquals(mapper.deleteBatchByPrimaryKeyOnPhysical(ids) ,0);
        mapper.deleteSelective(entity1);
        mapper.deleteSelectiveOnPhysical(entity1);

        List<Entity1> list = IntStream.range(10, 20)
                .mapToObj(i -> new Entity1(i, String.valueOf(i)))
                .collect(Collectors.toList());
        mapper.insertBatch(list);
        Assert.assertEquals(mapper.selectSelective(null ,true).size() ,10);

        mapper.deleteBatchByPrimaryKeyOnPhysical(list
                .stream()
                .map(s -> s.getId())
                .collect(Collectors.toList()));
    }

}
