package com.github.ibatis.statement.mapper;

import com.github.ibatis.statement.base.core.Column;
import com.github.ibatis.statement.mapper.param.BetweenParam;
import com.github.ibatis.statement.mapper.param.LimitParam;
import com.github.ibatis.statement.register.factory.If;
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

        Collection<Entity6> selectByIndexIn(Collection<String> index);

        Collection<Entity6> selectByIdAndIndexIn(String id, List<String> strings);

        Set<Entity6> selectByOrIn(String... or);

        List<Entity6> selectByLocationCodeLikeLeftAndOrBetweenOrderByLoCodeAsc(String locationCode, String startOr, String endOr);

        List<Entity6> selectByLikeOrIndexOrLikeGtAndByNotNull(String like, String index, String gtLike);

        @Deprecated
        List<Entity6> selectByLikeOrIndexOrLikeGtAndByIsNull(String like, String index, String gtLike, String by);

        @Deprecated
        Entity6 selectByLocationCodeAndOrNotBetweenOrderByLoCodeAsc(String locationCode, String or);

        int selectCountByIndex(String index);

        int countByIndex(String index);

        @Deprecated
        Integer selectCountOrderByIndexDesc();

        int selectCountByLikeOrIndexOrLikeGtAndByNotNull(String like, String index, String gtLike);

        int countByLikeOrIndexOrLikeGtAndByNotNull(String like, String index, String gtLike);

        /**
         * 忽略大小写
         * @param like
         * @param index
         * @return
         */
        List<Entity6> selectByLIKEAnDIndex(String like ,String index);

        List<Entity6> selectByLikeAndOrAndIndex(@If String like ,@If(otherwise = "'9'") String or ,String index);

        List<Entity6> selectByLikeAndOrBetweenAndIndexAndIdNotNull(@If String like ,
                                                                   @If(otherwise = "'1'") String minOr ,
                                                                   @If String maxOr ,
                                                                   String index);

        List<Entity6> selectByIdIn(@If(otherwise = "('1' ,'2')") String... id);

        List<Entity6> selectByLikeLike(@If(otherwise = "'7'") String like);

        List<Entity6> selectByLikeLikeLeftOrderByOrLikeAsc(@If(otherwise = "'7'") String like);

        List<Entity6> selectByLikeLikeRight(@If(otherwise = "'7'") String like);

        List<Entity6> selectByLoCodeBetweenAndIdNotBetweenAndId2Between(
                BetweenParam<String> loCode , @If BetweenParam<String> id ,
                @If(otherwise = "'1' AND '3'") BetweenParam<String> id2);

        List<Entity6> selectByLoCodeLimit(String loCode ,Integer limit);

        List<Entity6> selectByIdInLimit(Collection<String> id ,@If(otherwise = "4") Integer limit);

        List<Entity6> selectByLikeInOrderByIdAscLimit(@If Collection<String> like ,@If LimitParam limitParam);

    }

    @Test
    public void selectByLoCodeLimit(){
        Assert.assertEquals(mapper.selectByLoCodeLimit("8" ,0).size() ,0);
        Assert.assertEquals(mapper.selectByLoCodeLimit("8" ,3).size() ,1);
        Assert.assertEquals(mapper.selectByLoCodeLimit("8" ,null).size() ,1);
    }

    @Test
    public void selectByIdInLimit(){
        Assert.assertEquals(mapper.selectByIdInLimit(Arrays.asList("1" ,"11" ,"21") ,null).size() ,3);
        Assert.assertEquals(mapper.selectByIdInLimit(Arrays.asList("1" ,"11" ,"21") ,2).size() ,2);
    }

    @Test
    public void selectByLikeInOrderByIdAscLimit(){
        Assert.assertEquals(mapper.selectByLikeInOrderByIdAscLimit(Arrays.asList("7" ,"17" ,"27") ,null).size() ,3);
        Assert.assertEquals(mapper.selectByLikeInOrderByIdAscLimit(Arrays.asList("8" ,"17" ,"27") ,new LimitParam(4)).size() ,2);
        Assert.assertEquals(mapper.selectByLikeInOrderByIdAscLimit(Arrays.asList("7" ,"17" ,"27") ,new LimitParam(1)).size() ,1);
        Assert.assertEquals(mapper.selectByLikeInOrderByIdAscLimit(Arrays.asList("7" ,"17" ,"27") ,new LimitParam(2 ,1)).size() ,1);
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
    public void selectByIdAndIndexIn(){
        Assert.assertEquals(mapper.selectByIdAndIndexIn("1" ,Arrays.asList("6", "26")).size() ,1);
    }

    @Test
    public void selectByIndexIn(){
        Assert.assertEquals(mapper.selectByIndexIn(Arrays.asList("6", "26")).size() ,3);
    }

    @Test
    public void selectByOrIn(){
        Assert.assertEquals(mapper.selectByOrIn("9", "19").size() ,2);
    }

    @Test
    public void selectByLocationCodeLikeLeftAndOrBetweenOrderByLoCodeAsc(){
        Assert.assertEquals(mapper.selectByLocationCodeLikeLeftAndOrBetweenOrderByLoCodeAsc(
                "8" ,"1" ,"29").size() , 2);
        Assert.assertEquals(mapper.selectByLocationCodeLikeLeftAndOrBetweenOrderByLoCodeAsc(
                "8" ,"18" ,"27").size() , 1);
    }

    @Test
    public void selectByLikeOrIndexOrLikeGtAndByNotNull(){
        Assert.assertEquals(mapper.selectByLikeOrIndexOrLikeGtAndByNotNull(
                "27" ,"6" ,"6").size() ,3);
    }

    @Test(expected = BindingException.class)
    public void selectByLikeOrIndexOrLikeGtAndByIsNull(){
        mapper.selectByLikeOrIndexOrLikeGtAndByIsNull("27" ,"19" ,"6" ,null);
    }

    @Test(expected = BindingException.class)
    public void selectByLocationCodeAndOrNotBetweenOrderByLoCodeAsc(){
        mapper.selectByLocationCodeAndOrNotBetweenOrderByLoCodeAsc("1" ,"2");
    }

    @Test
    public void selectCountByIndex(){
        Assert.assertEquals(mapper.selectCountByIndex("6") ,2);
    }

    @Test
    public void countByIndex(){
        Assert.assertEquals(mapper.countByIndex("6") ,2);
    }

    @Test(expected = BindingException.class)
    public void selectCountOrderByIndexDesc(){
        Assert.assertTrue(mapper.selectCountOrderByIndexDesc() == 2);
    }

    @Test
    public void selectCountByLikeOrIndexOrGtLikeAndNotNullBy(){
        Assert.assertEquals(mapper.selectCountByLikeOrIndexOrLikeGtAndByNotNull(
                "27" ,"6" ,"6") ,3);
    }

    @Test
    public void countByLikeOrIndexOrGtLikeAndNotNullBy(){
        Assert.assertEquals(mapper.countByLikeOrIndexOrLikeGtAndByNotNull(
                "27" ,"6" ,"6") ,3);
    }

    @Test
    public void selectByLIKEAnDIndex(){
        Assert.assertEquals(mapper.selectByLIKEAnDIndex("17" ,"6").size() ,1);
    }

    @Test
    public void selectByLikeAndOrAndIndex(){
        Assert.assertEquals(mapper.selectByLikeAndOrAndIndex(null ,null ,"6").size() ,1);
        Assert.assertEquals(mapper.selectByLikeAndOrAndIndex(null ,null ,null).size() ,0);
        Assert.assertEquals(mapper.selectByLikeAndOrAndIndex("7" ,null ,"6").size() ,1);
        Assert.assertEquals(mapper.selectByLikeAndOrAndIndex("5" ,null ,"6").size() ,0);
    }

    @Test
    public void selectByLikeAndOrBetweenAndIndexAndIdNotNull(){
        Assert.assertEquals(mapper.selectByLikeAndOrBetweenAndIndexAndIdNotNull(
                null ,null ,"9" ,"6").size() ,2);
        Assert.assertEquals(mapper.selectByLikeAndOrBetweenAndIndexAndIdNotNull(
                "7" ,null ,"9" ,"6").size() ,1);
        Assert.assertEquals(mapper.selectByLikeAndOrBetweenAndIndexAndIdNotNull(
                "17" ,null ,"9" ,"6").size() ,1);
        Assert.assertEquals(mapper.selectByLikeAndOrBetweenAndIndexAndIdNotNull(
                null ,null ,"9" ,null).size() ,0);
        Assert.assertEquals(mapper.selectByLikeAndOrBetweenAndIndexAndIdNotNull(
                null ,"2" ,"1" ,"6").size() ,0);
        Assert.assertEquals(mapper.selectByLikeAndOrBetweenAndIndexAndIdNotNull(
                null ,"2" ,"3" ,"26").size() ,1);
        Assert.assertEquals(mapper.selectByLikeAndOrBetweenAndIndexAndIdNotNull(
                "27" ,"2" ,"3" ,"26").size() ,1);
        Assert.assertEquals(mapper.selectByLikeAndOrBetweenAndIndexAndIdNotNull(
                "23" ,"2" ,"3" ,"26").size() ,0);
    }

    @Test
    public void selectByIdIn(){
        Assert.assertEquals(mapper.selectByIdIn("1" ,"11").size() ,2);
        Assert.assertEquals(mapper.selectByIdIn(null).size() ,1);
    }

    @Test
    public void selectByLikeLike(){
        Assert.assertEquals(mapper.selectByLikeLike(null).size() ,3);
        Assert.assertEquals(mapper.selectByLikeLike("17").size() ,1);
        Assert.assertEquals(mapper.selectByLikeLike("1").size() ,1);
        Assert.assertEquals(mapper.selectByLikeLike("3").size() ,0);
    }

    @Test
    public void selectByLikeLikeLeft(){
        Assert.assertEquals(mapper.selectByLikeLikeLeftOrderByOrLikeAsc(null).size() ,3);
        Assert.assertEquals(mapper.selectByLikeLikeLeftOrderByOrLikeAsc("17").size() ,1);
        Assert.assertEquals(mapper.selectByLikeLikeLeftOrderByOrLikeAsc("37").size() ,0);
    }

    @Test
    public void selectByLikeLikeRight(){
        Assert.assertEquals(mapper.selectByLikeLikeRight(null).size() ,1);
        Assert.assertEquals(mapper.selectByLikeLikeRight("2").size() ,1);
        Assert.assertEquals(mapper.selectByLikeLikeRight("3").size() ,0);
    }

    @Test
    public void selectByLoCodeBetweenAndIdNotBetween(){
        Assert.assertEquals(mapper.selectByLoCodeBetweenAndIdNotBetweenAndId2Between(
                new BetweenParam<>("1" ,"4") ,
                new BetweenParam<>("3" ,"4") ,null).size() ,2);
        Assert.assertEquals(mapper.selectByLoCodeBetweenAndIdNotBetweenAndId2Between(
                new BetweenParam<>("1" ,"4") ,
                new BetweenParam<>("3" ,"4") ,new BetweenParam<>("1" ,"2")).size() ,1);
        Assert.assertEquals(mapper.selectByLoCodeBetweenAndIdNotBetweenAndId2Between(
                new BetweenParam<>("2" ,"4") ,
                null ,null).size() ,1);
    }

}
