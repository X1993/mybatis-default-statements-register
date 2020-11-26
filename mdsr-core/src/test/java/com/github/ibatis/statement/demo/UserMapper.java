package com.github.ibatis.statement.demo;

import com.github.ibatis.statement.mapper.KeyTableMapper;
import com.github.ibatis.statement.mapper.param.BetweenParam;
import java.util.Collection;
import java.util.Date;

/**
 * @author junjie
 * @date 2020/9/9
 */
public interface UserMapper extends KeyTableMapper<Integer ,User> {

    Integer selectMaxKey();

    User selectById(Integer id);

    User selectByEqIdAndNotNullAddressOrBetweenCreateTimeOrderByNameCreateTimeDesc(Integer id ,BetweenParam<Date> betweenParam);

    User selectByInNameAndGtCreateTime(Collection<String> names ,Date startTime);

    User selectByInName(Collection<String> names);

    User selectByInId(int... ids);

}
