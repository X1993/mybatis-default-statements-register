package com.github.ibatis.statement.base.core.matedata;

import com.github.ibatis.statement.base.core.MethodSignature;
import com.github.ibatis.statement.util.TypeUtils;
import lombok.Data;
import java.lang.reflect.Method;
import java.util.Objects;

/**
 * mapper方法元数据
 * @Author: junjie
 * @Date: 2020/2/21
 */
@Data
public class MapperMethodMateData implements Cloneable{

    private final Method mappedMethod;

    private final Class<?> mapperClass;

    private final MethodSignature methodSignature;

    public MapperMethodMateData(Method mappedMethod , Class<?> mapperClass) {
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

    /**
     * 获取mappedStatementId
     * @return
     */
    public String getMappedStatementId(){
        return getMapperClass().getName() + "." + getMappedMethod().getName();
    }

    /**
     * 获取方法签名
     * @return
     */
    public MethodSignature getMethodSignature()
    {
        return TypeUtils.methodSignature(getMappedMethod() ,getMapperClass());
    }

}
