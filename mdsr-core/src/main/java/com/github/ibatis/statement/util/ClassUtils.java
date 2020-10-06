package com.github.ibatis.statement.util;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

/**
 * @Author: junjie
 * @Date: 2020/3/16
 */
public class ClassUtils {

    /**
     * 解析类包含基类所有非静态字段
     * @param clazz
     * @return
     */
    public static Set<Field> getBaseNonStaticFields(Class<?> clazz){
        Set<Field> fields = new HashSet<>();
        for (Field field : clazz.getDeclaredFields()) {
            if (!Modifier.isStatic(field.getModifiers())) {
                fields.add(field);
            }
        }
        Class<?> superclass = clazz.getSuperclass();
        if (!superclass.equals(Object.class)) {
            for (Field superField : getBaseNonStaticFields(superclass)) {
                if (!Modifier.isStatic(superField.getModifiers())) {
                    fields.add(superField);
                }
            }
        }
        return fields;
    }

}
