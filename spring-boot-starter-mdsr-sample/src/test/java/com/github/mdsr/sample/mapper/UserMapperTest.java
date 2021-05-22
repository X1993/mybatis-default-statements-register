package com.github.mdsr.sample.mapper;

import com.github.ibatis.statement.base.core.parse.EntityMateDataParser;
import com.github.ibatis.statement.mapper.param.ConditionParams;
import com.github.ibatis.statement.mapper.param.DynamicParams;
import com.github.mdsr.sample.model.User;
import org.apache.ibatis.session.SqlSession;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

@RunWith(SpringRunner.class)
@Transactional
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class UserMapperTest{

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private SqlSession sqlSession;

    @Autowired
    private EntityMateDataParser entityMateDataParser;

    @Test
    public void entityMateDataParser0() {
        Assert.assertNotNull(entityMateDataParser.parse(User.class, sqlSession).get());
    }

    @Test
    public void insert(){
        User user = new User();
        user.setName("张三");
        user.setAddress("杭州");
        userMapper.insert(user);
    }

    @Test
    public void selectAll(){
        userMapper.selectAll();
    }

    @Test
    public void selectSelective(){
        User user = new User();
        user.setName("ha");
        userMapper.selectSelective(user);
    }

    @Test
    public void countSelective(){
        User user = new User();
        user.setName("ha");
        userMapper.totalSelective(user);
    }

    @Test
    public void selectByPrimaryKey(){
        userMapper.selectByPrimaryKey(2);
    }

    @Test
    public void updateByPrimaryKey(){
        User user = new User();
        user.setName("张三");
        user.setAddress("杭州");
        userMapper.insert(user);
        user.setAddress("嘉兴");
        userMapper.updateByPrimaryKey(user);
    }

    @Test
    public void deleteByPrimaryKey(){
        User user = new User();
        user.setName("张三");
        user.setAddress("杭州");
        user.setCreateTime(new Date());
        userMapper.insert(user);
        userMapper.deleteByPrimaryKey(user.getId());
    }

    @Test
    public void selectByDynamicParams(){
        userMapper.selectByDynamicParams(new DynamicParams()
                .where(new ConditionParams()
                        .between("create_time", "2020-08-11", new Date())
                        .likeLeft("name", "张"))
                .groupBy("address", "name")
                .having(new ConditionParams().notNull("note"))
                .page0(0, 10));
    }

    @Test
    public void selectMaxPrimaryKey(){
        userMapper.selectMaxPrimaryKey();
    }

}
