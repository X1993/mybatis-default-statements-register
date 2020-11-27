package com.github.ibatis.statement.mapper;

import org.apache.ibatis.session.SqlSession;
import org.junit.Test;

/**
 * 测试列名中包含关键字时方法名解析是否正常
 * @Author: junjie
 * @Date: 2020/11/27
 */
public class Entity6Test {

    @Removed
    static class Entity6 {

        private String id;

        private String id2;

        private String or;

        private String and;

        private String like;

        private String by;

        private String byAndLike;

        public Entity6(String id, String id2) {
            this.id = id;
            this.id2 = id2;
        }

        public Entity6() {
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getId2() {
            return id2;
        }

        public void setId2(String id2) {
            this.id2 = id2;
        }

        public String getOr() {
            return or;
        }

        public void setOr(String or) {
            this.or = or;
        }

        public String getAnd() {
            return and;
        }

        public void setAnd(String and) {
            this.and = and;
        }

        public String getLike() {
            return like;
        }

        public void setLike(String like) {
            this.like = like;
        }

        public String getBy() {
            return by;
        }

        public void setBy(String by) {
            this.by = by;
        }

        public String getByAndLike() {
            return byAndLike;
        }

        public void setByAndLike(String byAndLike) {
            this.byAndLike = byAndLike;
        }
    }

    interface Entity6Mapper extends KeyTableMapper<Entity6Test.Entity6, Entity6Test.Entity6> {

        Entity6Test.Entity6 selectByByAndLikeOrderByOrDesc(String byAndLike);

        Entity6Test.Entity6 selectByByAndLikeOrderByOrAsc(String by ,String like);

        Entity6Test.Entity6 selectByByAndLikeAndLikeOrderByOrByLikeAsc(String by ,String like);

    }

    final static String SCHEMA_SQL = "DROP TABLE IF EXISTS `entity6`;\n" +
            "CREATE TABLE `entity6` (\n" +
            "  `id` varchar(50) ,\n" +
            "  `id2` varchar(50) ,\n" +
            "  `or` varchar(30) DEFAULT NULL,\n" +
            "  `and` varchar(30) DEFAULT NULL,\n" +
            "  `like` varchar(30) DEFAULT NULL,\n" +
            "  `by` varchar(30) DEFAULT NULL,\n" +
            "  `by_and_like` varchar(30) DEFAULT NULL,\n" +
            "  `removed` char(1) ,\n" +
            "  CONSTRAINT table_entity6_pk PRIMARY KEY (id, id2) \n" +
            ") DEFAULT CHARSET=utf8; ";

    @Test
    public void test(){
        MybatisEnvironment environment = MybatisEnvironment.ENVIRONMENT;
        environment.initTableSchema(SCHEMA_SQL);
        environment.registerMappedStatementsForMappers(Entity6Test.Entity6Mapper.class);
        testMapper(environment);
    }

    private void testMapper(MybatisEnvironment environment)
    {
        SqlSession sqlSession = environment.getSqlSession();
        Entity6Test.Entity6Mapper mapper = sqlSession.getMapper(Entity6Test.Entity6Mapper.class);
        mapper.selectByByAndLikeAndLikeOrderByOrByLikeAsc("12" ,"13");
        mapper.selectByByAndLikeOrderByOrDesc("11");
        mapper.selectByByAndLikeOrderByOrAsc("12" ,"14");
    }

}
