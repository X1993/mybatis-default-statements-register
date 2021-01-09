package com.github.mdsr.sample.mapper;

import com.github.mdsr.sample.model.Entity6;
import org.apache.ibatis.binding.BindingException;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Arrays;

/**
 * 测试列名中包含关键字时方法名解析是否正常
 * @Author: junjie
 * @Date: 2020/11/27
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class Entity6MapperTest {

    @Autowired
    private Entity6Mapper mapper;

    @Before
    public void before(){
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

    @After
    public void after(){
        mapper.deleteAll();
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

}
