package com.github.mdsr.sample.mapper;

import com.github.mdsr.sample.model.User;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class CustomUserMapperTest {

    @Autowired
    private CustomUserMapper customUserMapper;

    @Test
    public void selectByPrimaryKey() {
        customUserMapper.selectByPrimaryKey(2);
    }

    @Test
    public void selectSelective() {
        User user = new User();
        user.setName("ha");
        customUserMapper.selectSelective(user ,true);
    }
}
