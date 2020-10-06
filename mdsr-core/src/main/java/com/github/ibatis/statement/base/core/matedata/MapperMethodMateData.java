package com.github.ibatis.statement.base.core.matedata;

import com.github.ibatis.statement.base.core.MethodSignature;
import com.github.ibatis.statement.util.TypeUtils;
import java.lang.reflect.Method;

/**
 * mapper方法元数据
 * @Author: junjie
 * @Date: 2020/2/21
 */
public interface MapperMethodMateData extends Cloneable{

    /**
     * 目标方法
     * @return
     */
    Method getMappedMethod();

    /**
     * 映射器类型
     * @return
     */
    Class<?> getMapperClass();

    /**
     * 获取mappedStatementId
     * @return
     */
    default String getMappedStatementId(){
        return getMapperClass().getName() + "." + getMappedMethod().getName();
    }

    /**
     * 获取方法签名
     * @return
     */
    default MethodSignature getMethodSignature()
    {
        return TypeUtils.methodSignature(getMappedMethod() ,getMapperClass());
    }

}
