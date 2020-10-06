package com.github.ibatis.statement.base.core.matedata;

import com.github.ibatis.statement.base.core.MethodSignature;
import com.github.ibatis.statement.util.TypeUtils;
import java.lang.reflect.Method;
import java.util.Objects;

/**
 * @Author: junjie
 * @Date: 2020/2/21
 */
public class RootMapperMethodMateData implements MapperMethodMateData {

    private final Method mappedMethod;

    private final Class<?> mapperClass;

    private final MethodSignature methodSignature;

    public RootMapperMethodMateData(Method mappedMethod , Class<?> mapperClass) {
        Objects.requireNonNull("Construction parameters [mappedMethod] is null");
        Objects.requireNonNull("Construction parameters [mapperClass] is null");
        this.mappedMethod = mappedMethod;
        this.mapperClass = mapperClass;
        this.methodSignature = TypeUtils.methodSignature(mappedMethod ,mapperClass);
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    @Override
    public Method getMappedMethod() {
        return mappedMethod;
    }

    @Override
    public Class<?> getMapperClass() {
        return mapperClass;
    }

    @Override
    public MethodSignature getMethodSignature() {
        return methodSignature;
    }

    @Override
    public String toString() {
        return "RootMapperMethodMateData{" +
                "mappedMethod=" + mappedMethod +
                ", mapperClass=" + mapperClass +
                '}';
    }
}
