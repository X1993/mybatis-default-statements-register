package com.github.ibatis.statement.demo;

import com.github.ibatis.statement.mapper.KeyTableMapper;
import com.github.ibatis.statement.mapper.param.If;

import java.util.Collection;
import java.util.Date;
import java.util.List;

/**
 * @author X1993
 * @date 2020/9/9
 */
public interface UserMapper extends KeyTableMapper<Integer ,User> {

    Integer selectMaxPrimaryKey();

    User selectByIdEqAndAddressNotNullOrCreateTimeBetweenOrderByNameCreateTimeDesc(Integer id , Date startDate , Date endDate);

    User selectByNameInAndCreateTimeGt(Collection<String> names , Date startTime);

    User selectByNameIn(Collection<String> names);

    List<User> selectById(Integer id);

    User selectByIdIn(int... ids);

    User selectByNameAndAddressIn(@If(otherwise = "'jack'") String name , @If String ... address);

}
