package com.github.ibatis.statement.demo;

import com.github.ibatis.statement.mapper.KeyTableMapper;

/**
 * @author junjie
 * @date 2020/9/9
 */
public interface UserMapper extends KeyTableMapper<Integer ,User> {

    Integer selectMaxKey();

}
