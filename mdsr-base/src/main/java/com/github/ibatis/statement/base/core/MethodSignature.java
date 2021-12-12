package com.github.ibatis.statement.base.core;

import com.github.ibatis.statement.util.TypeUtils;
import lombok.Data;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Objects;

/**
 * 方法签名
 * @author X1993
 * @date 2020/3/28
 */
@Data
public class MethodSignature {

    /**
     * 方法名
     */
    private final String methodName;

    /**
     * 方法返回类型
     */
    private final Type genericReturnType;

    /**
     * 方法参数类型
     */
    private final Type[] genericParameterTypes;

    public MethodSignature(Method method){
        this(method.getGenericReturnType() ,method.getName() ,method.getGenericParameterTypes());
    }

    public MethodSignature(Type genericReturnType, String methodName, Type ... genericParameterTypes) {
        Objects.requireNonNull(methodName ,"methodName is null");
        Objects.requireNonNull(methodName ,"genericReturnType is null");
        Objects.requireNonNull(methodName ,"genericParameterTypes is null");
        this.methodName = methodName;
        this.genericReturnType = genericReturnType;
        this.genericParameterTypes = genericParameterTypes;
    }

    /**
     * 是否匹配指定的方法签名
     * @param defined 匹配的方法签名
     * @return
     */
    public boolean isMatch(MethodSignature defined)
    {
        //方法名相同
        if (getMethodName().equals(defined.getMethodName())){
            if (TypeUtils.isAssignableFrom(getGenericReturnType() ,defined.getGenericReturnType())
                    && getGenericParameterTypes().length == defined.getGenericParameterTypes().length){
                // 返回类型兼容
                for (int i = 0; i < getGenericParameterTypes().length; i++) {
                    Type actualParameterType = getGenericParameterTypes()[i];
                    Type definedParameterType = defined.getGenericParameterTypes()[i];
                    if (!TypeUtils.isAssignableFrom(definedParameterType ,actualParameterType)){
                        // 参数类型不兼容
                        return false;
                    }
                }
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        StringBuilder message = new StringBuilder("[")
                .append(getGenericReturnType().getTypeName())
                .append(" ")
                .append(getMethodName())
                .append("(");

        Type[] genericParameterTypes = getGenericParameterTypes();
        if (genericParameterTypes.length > 0){
            for (Type parameterType : getGenericParameterTypes()) {
                message.append(parameterType.getTypeName())
                        .append(",");
            }
            message.deleteCharAt(message.length() - 1);
        }
        message.append(")]");
        return message.toString();
    }
}
