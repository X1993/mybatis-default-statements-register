package com.github.mdsr.sample.mapper;

import com.github.ibatis.statement.mapper.KeyTableMapper;
import com.github.mdsr.sample.model.User;
import org.apache.ibatis.annotations.Select;

/**
 * @author junjie
 * @date 2020/9/9
 */
public interface UserMapper extends KeyTableMapper<Integer ,User> {

    @Override
    @Select("select * from user where id = #{param1} and removed = 0")
    User selectByPrimaryKey(Integer key);

    Integer selectMaxKey();

//    Integer cannotRegisterMethod();

}
