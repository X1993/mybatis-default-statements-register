package com.github.mdsr.sample.mapper;

import com.github.ibatis.statement.mapper.param.ConditionParams;
import com.github.ibatis.statement.mapper.param.DynamicParams;
import com.github.mdsr.sample.model.User;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import java.util.Date;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class UserMapperTest{

    @Autowired
    private UserMapper userMapper;

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
                        .likeLeft("`name`", "张"))
                .groupBy("address", "`name`")
                .having(new ConditionParams().notNull("note"))
                .page0(0, 10));
    }

    @Test
    public void optimisticLock(){
        User user = new User();
        user.setId(11);
        user.setName("张三");
        user.setAddress("杭州");
        user.setCreateTime(new Date());
        user.setVersion(12);
        userMapper.insert(user);
        user.setNote("无");
        userMapper.updateByPrimaryKey(user);
    }

    @Test
    public void selectMaxKey(){
        userMapper.selectMaxKey();
    }

}
