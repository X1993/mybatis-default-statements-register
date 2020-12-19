package com.github.mdsr.sample.mapper;

import com.github.ibatis.statement.mapper.EntityType;
import com.github.mdsr.sample.model.Entity6;
import org.apache.ibatis.annotations.Delete;

import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * 根据方法名自动注册{@link org.apache.ibatis.mapping.MappedStatement}
 */
public interface Entity6Mapper extends EntityType<Entity6> {

        int insertBatch(Collection<Entity6> collection);

        Entity6 selectByByAndLikeOrderByOrDesc(String byAndLike);

        List<Entity6> selectByByAndLikeOrderByOrAsc(String by, String like);

        Entity6 selectByByAndLikeAndLikeOrderByOrByLikeAsc(String byAndLike, String like);

        Entity6 selectByIndexAndLike(String index, String like);

        List<Entity6> selectByIndex(String index);

        Collection<Entity6> selectByInIndex(Collection<String> index);

        Set<Entity6> selectByInOr(String... or);

        List<Entity6> selectByLikeLeftLocationCodeAndBetweenOrOrderByLoCodeAsc(String locationCode, String startOr, String endOr);

        List<Entity6> selectByLikeOrIndexOrGtLikeAndNotNullBy(String like, String index, String gtLike);

        @Deprecated
        List<Entity6> selectByLikeOrIndexOrGtLikeAndIsNullBy(String like, String index, String gtLike, String by);

        @Deprecated
        Entity6 selectByLocationCodeAndNotBetweenOrOrderByLoCodeAsc(String locationCode, String startOr);

        @Delete("delete from `entity6`")
        void deleteAll();
}