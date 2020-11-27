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

    User selectByEqIdAndNotNullAddressOrBetweenCreateTimeOrderByNameCreateTimeDesc(Integer id ,Date startDate ,Date endDate);

    User selectByInNameAndGtCreateTime(Collection<String> names ,Date startTime);

    User selectByInName(Collection<String> names);

    List<User> selectById(Integer id);

    User selectByInId(int... ids);

}
