package com.github.ibatis.statement.demo;

import com.github.ibatis.statement.mapper.KeyTableMapper;
import java.util.Collection;
import java.util.Date;
import java.util.List;

/**
 * @author junjie
 * @date 2020/9/9
 */
public interface UserMapper extends KeyTableMapper<Integer ,User> {

    Integer selectMaxKey();

    User selectByIdEqAndAddressNotNullOrCreateTimeBetweenOrderByNameCreateTimeDesc(Integer id , Date startDate , Date endDate);

    User selectByNameInAndCreateTimeGt(Collection<String> names , Date startTime);

    User selectByNameIn(Collection<String> names);

    List<User> selectById(Integer id);

    User selectByIdIn(int... ids);

}
