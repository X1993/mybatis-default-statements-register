package com.github.ibatis.statement.util;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;

/**
 * @Author: junjie
 * @Date: 2020/3/16
 */
public class ClassUtils {

    /**
     * 解析类字段，按字段定义的顺序 （父类 先于 子类 ，定义在前 先于 定义在后）
     * @param clazz
     * @param containStatic 是否包含静态字段
     * @return
     */
    public static List<Field> getFields(Class<?> clazz , boolean containStatic)
    {
        if (clazz.getPackage().getName().startsWith("java")){
            return Collections.EMPTY_LIST;
        }
        List<Field> fields = new ArrayList<>();
        fields.addAll(getFields(clazz.getSuperclass() ,containStatic));
        for (Field field : clazz.getDeclaredFields()) {
            if (containStatic || !Modifier.isStatic(field.getModifiers())) {
                fields.add(field);
            }
        }
        return fields;
    }

}
