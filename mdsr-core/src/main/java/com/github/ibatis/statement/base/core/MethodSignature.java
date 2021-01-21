package com.github.ibatis.statement.base.core;

import lombok.Data;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Objects;

/**
 * 方法签名
 * @author junjie
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
