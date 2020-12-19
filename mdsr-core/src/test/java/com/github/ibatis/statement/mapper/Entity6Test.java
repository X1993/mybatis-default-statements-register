package com.github.ibatis.statement.mapper;

import com.github.ibatis.statement.base.core.Column;
import org.apache.ibatis.binding.BindingException;
import org.apache.ibatis.session.SqlSession;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * 测试列名中包含关键字时方法名解析是否正常
 * @Author: junjie
 * @Date: 2020/11/27
 */
public class Entity6Test{

    @Removed
    static class Entity6 {

        private String id;

        private String id2;

        private String or;

        private String and;

        private String like;

        private String by;

        private String byAndLike;

        private String index;

        @Column(value = "lo_code")
        private String locationCode;

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

        public String getIndex() {
            return index;
        }

        public void setIndex(String index) {
            this.index = index;
        }

        public String getLocationCode() {
            return locationCode;
        }

        public void setLocationCode(String locationCode) {
            this.locationCode = locationCode;
        }
    }

    interface Entity6Mapper extends EntityType<Entity6> {

        int insertBatch(Collection<Entity6> collection);

        Entity6 selectByByAndLikeOrderByOrDesc(String byAndLike);

        List<Entity6> selectByByAndLikeOrderByOrAsc(String by, String like);

        Entity6 selectByByAndLikeAndLikeOrderByOrByLikeAsc(String byAndLike, String like);

        Entity6 selectByIndexAndLike(String index, String like);

        List<Entity6> selectByIndex(String index);

        Collection<Entity6> selectByInIndex(Collection<String> index);

        Set<Entity6> selectByInOr(String... or);

        List<Entity6> selectByLikeLeftLocationCodeAndBetweenOrOrderByLoCodeAsc(String locationCode, String startOr, String endOr);

        List<Entity6> selectByLikeOrIndexOrGtLikeAndNotNullBy(String like, String index, String gtLike);

        @Deprecated
        List<Entity6> selectByLikeOrIndexOrGtLikeAndIsNullBy(String like, String index, String gtLike, String by);

        @Deprecated
        Entity6 selectByLocationCodeAndNotBetweenOrOrderByLoCodeAsc(String locationCode, String startOr);

        int selectCountByIndex(String index);

        @Deprecated
        Integer selectCountOrderByIndexDesc();

        int selectCountByLikeOrIndexOrGtLikeAndNotNullBy(String like, String index, String gtLike);

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
            "  `index` varchar(30) DEFAULT NULL,\n" +
            "  `lo_code` varchar(30) DEFAULT NULL,\n" +
            "  `removed` char(1) ,\n" +
            "  CONSTRAINT table_entity6_pk PRIMARY KEY (id, id2) \n" +
            ") DEFAULT CHARSET=utf8; ";

    private static final Entity6Mapper mapper = entity6Mapper();

    private static Entity6Mapper entity6Mapper(){
        MybatisEnvironment environment = MybatisEnvironment.ENVIRONMENT;
        environment.initTableSchema(SCHEMA_SQL);
        environment.registerMappedStatementsForMappers(Entity6Mapper.class);
        SqlSession sqlSession = environment.getSqlSession();
        return sqlSession.getMapper(Entity6Mapper.class);
    }

    @BeforeClass
    public static void beforeClass(){
        Entity6 entity61 = new Entity6();
        entity61.setId("1");
        entity61.setId2("2");
        entity61.setAnd("3");
        entity61.setBy("4");
        entity61.setByAndLike("5");
        entity61.setIndex("6");
        entity61.setLike("7");
        entity61.setLocationCode("8");
        entity61.setOr("9");

        Entity6 entity62 = new Entity6();
        entity62.setId("11");
        entity62.setId2("12");
        entity62.setAnd("13");
        entity62.setBy("14");
        entity62.setByAndLike("15");
        entity62.setIndex("6");
        entity62.setLike("17");
        entity62.setLocationCode("18");
        entity62.setOr("19");

        Entity6 entity63 = new Entity6();
        entity63.setId("21");
        entity63.setId2("22");
        entity63.setAnd("23");
        entity63.setBy("24");
        entity63.setByAndLike("25");
        entity63.setIndex("26");
        entity63.setLike("27");
        entity63.setLocationCode("28");
        entity63.setOr("29");

        mapper.insertBatch(Arrays.asList(entity61 ,entity62 ,entity63));
    }

    @Test
    public void selectByByAndLikeAndLikeOrderByOrByLikeAsc(){
        Assert.assertNotNull(mapper.selectByByAndLikeAndLikeOrderByOrByLikeAsc("5" ,"7"));
    }

    @Test
    public void selectByByAndLikeOrderByOrDesc(){
        Assert.assertNotNull(mapper.selectByByAndLikeOrderByOrDesc("15"));
    }

    @Test
    public void selectByByAndLikeOrderByOrAsc(){
        Assert.assertEquals(mapper.selectByByAndLikeOrderByOrAsc("4" ,"7").size() ,1);
    }

    @Test
    public void selectByIndexAndLike(){
        Assert.assertNull(mapper.selectByIndexAndLike("12" ,"12"));
    }

    @Test
    public void selectByIndex(){
        Assert.assertEquals(mapper.selectByIndex("6").size() ,2);
    }

    @Test
    public void selectByInIndex(){
        Assert.assertEquals(mapper.selectByInIndex(Arrays.asList("6", "26")).size() ,3);
    }

    @Test
    public void selectByInOr(){
        Assert.assertEquals(mapper.selectByInOr("9", "19").size() ,2);
    }

    @Test
    public void selectByLikeLeftLocationCodeAndBetweenOrOrderByLoCodeAsc(){
        Assert.assertEquals(mapper.selectByLikeLeftLocationCodeAndBetweenOrOrderByLoCodeAsc(
                "8" ,"1" ,"29").size() , 2);
        Assert.assertEquals(mapper.selectByLikeLeftLocationCodeAndBetweenOrOrderByLoCodeAsc(
                "8" ,"18" ,"27").size() , 1);
    }

    @Test
    public void selectByLikeOrIndexOrGtLikeAndNotNullBy(){
        Assert.assertEquals(mapper.selectByLikeOrIndexOrGtLikeAndNotNullBy(
                "27" ,"6" ,"6").size() ,3);
    }

    @Test(expected = BindingException.class)
    public void selectByLikeOrIndexOrGtLikeAndIsNullBy(){
        mapper.selectByLikeOrIndexOrGtLikeAndIsNullBy("27" ,"19" ,"6" ,null);
    }

    @Test(expected = BindingException.class)
    public void selectByLocationCodeAndNotBetweenOrOrderByLoCodeAsc(){
        mapper.selectByLocationCodeAndNotBetweenOrOrderByLoCodeAsc("1" ,"2");
    }

    @Test
    public void selectCountByIndex(){
        Assert.assertEquals(mapper.selectCountByIndex("6") ,2);
    }

    @Test(expected = BindingException.class)
    public void selectCountOrderByIndexDesc(){
        Assert.assertTrue(mapper.selectCountOrderByIndexDesc() == 2);
    }

    @Test
    public void selectCountByLikeOrIndexOrGtLikeAndNotNullBy(){
        Assert.assertEquals(mapper.selectCountByLikeOrIndexOrGtLikeAndNotNullBy(
                "27" ,"6" ,"6") ,3);
    }

}
