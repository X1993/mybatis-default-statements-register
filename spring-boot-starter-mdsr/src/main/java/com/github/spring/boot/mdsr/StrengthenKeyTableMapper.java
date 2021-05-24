package com.github.spring.boot.mdsr;

import com.github.ibatis.statement.mapper.KeyTableMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DuplicateKeyException;
import java.util.Collection;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 基于{@link KeyTableMapper}现有方法做一些组合增强
 * @Author: X1993
 * @Date: 2021/5/22
 */
public interface StrengthenKeyTableMapper<K ,T> extends KeyTableMapper<K ,T> {

    Logger LOGGER = LoggerFactory.getLogger(StrengthenKeyTableMapper.class);

    /**
     * 如果不存在就新增数据
     * @param collection 需要保存的数据集
     * @param keyFunction 主键获取函数
     * @return
     */
    default int safeInsert(Collection<T> collection, Function<T, K> keyFunction)
    {
        boolean duplicateKey = false;
        while (!collection.isEmpty())
        {
            Set<K> existKeys = getExistPrimaryKeysOnPhysical(collection.stream()
                    .map(t -> keyFunction.apply(t))
                    .collect(Collectors.toSet()));

            int count = collection.size();
            collection = collection.stream()
                    .filter(t -> !existKeys.contains(keyFunction.apply(t)))
                    .collect(Collectors.toList());

            if (duplicateKey && count == collection.size()){
                //没有过滤出重复数据,请检查是否合理的重写了主键类的equals和hashCode方法
                throw new IllegalStateException("Duplicate data is not filtered out, please check whether the" +
                        " [equals] and [hashCode] methods of the primary key class are reasonably rewritten");
            }else {
                duplicateKey = false;
            }

            if (collection.size() > 0) {
                try {
                    return insertBatch(collection);
                } catch (DuplicateKeyException e) {
                    LOGGER.warn("{} ,filter and retry", e.getMessage());
                    duplicateKey = true;
                }
            }
        }
        return 0;
    }

    /**
     * 如果不存在就新增数据
     * @param t 需要保存的数据
     * @param keyFunction 主键获取函数
     * @return
     */
    default int safeInsert(T t ,Function<T ,K> keyFunction)
    {
        if (!existByPrimaryKeyOnPhysical(keyFunction.apply(t))) {
            try {
                return insert(t);
            }catch (DuplicateKeyException e){
                LOGGER.warn("{}" ,e.getMessage());
            }
        }
        return 0;
    }

    /**
     * 如果存在则更新数据，否则新增数据
     * @param t 需要更新的数据
     * @param keyFunction 主键获取函数
     * @return
     */
    default int safeUpdate(T t ,Function<T ,K> keyFunction)
    {
        if (!existByPrimaryKeyOnPhysical(keyFunction.apply(t))) {
            try {
                return insert(t);
            }catch (DuplicateKeyException e){
                LOGGER.warn("{}" ,e.getMessage());
            }
        }
        return updateByPrimaryKeySelective(t);
    }

}
