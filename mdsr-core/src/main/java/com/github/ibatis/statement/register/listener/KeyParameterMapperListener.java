package com.github.ibatis.statement.register.listener;

import com.github.ibatis.statement.base.core.matedata.EntityMateData;
import com.github.ibatis.statement.mapper.KeyParameterType;
import com.github.ibatis.statement.register.DefaultStatementAutoRegister;
import com.github.ibatis.statement.util.TypeUtils;
import java.lang.reflect.Type;
import java.text.MessageFormat;

/**
 * @Author: X1993
 * @Date: 2020/10/29
 */
public class KeyParameterMapperListener implements DefaultStatementAutoRegister.Listener {

    @Override
    public void verify(EntityMateData entityMateData, Class mapperClass)
    {
        if (KeyParameterType.class.isAssignableFrom(mapperClass)){
            int primaryKeyCount = entityMateData.getPrimaryKeyCount();
            if (primaryKeyCount <= 0) {
                //声明了主键参数但实际上没有主键
                throw new IllegalArgumentException(MessageFormat.format("mapper [{0}] implement [{1}] ," +
                                "but mapper entity mapping table [{2}] no primary key",
                        mapperClass, KeyParameterType.class, entityMateData.getTableName()));
            }
            Class<?> matchKeyParameterClass = entityMateData.getReasonableKeyParameterClass();
            Type defineKeyParameterClass = TypeUtils.parseSuperTypeVariable(mapperClass,
                    KeyParameterType.class.getTypeParameters()[0]);

            boolean match = false;
            if (matchKeyParameterClass.isPrimitive() && defineKeyParameterClass instanceof Class)
            {
                Class<?> defineParamClass = (Class<?>) defineKeyParameterClass;
                if (Number.class.isAssignableFrom(defineParamClass))
                {
                    //主键不可能为空，兼容下
                    if (Byte.class == defineParamClass && short.class == matchKeyParameterClass){
                        match = true;
                    }else if (Short.class == defineParamClass && short.class == matchKeyParameterClass){
                        match = true;
                    }else if (Integer.class == defineParamClass && int.class == matchKeyParameterClass){
                        match = true;
                    }else if (Float.class == defineParamClass && float.class == matchKeyParameterClass){
                        match = true;
                    }else if (Double.class == defineParamClass && double.class == matchKeyParameterClass){
                        match = true;
                    }else if (Long.class == defineParamClass && long.class == matchKeyParameterClass){
                        match = true;
                    }else if (Character.class == defineParamClass && char.class == matchKeyParameterClass){
                        match = true;
                    }else if (Boolean.class == defineParamClass && boolean.class == matchKeyParameterClass){
                        match = true;
                    }
                }
            }

            if (!match){
                match = TypeUtils.isAssignableFrom(matchKeyParameterClass ,defineKeyParameterClass);
            }

            if (!match){
                //声明的主键参数类型与实际类型不匹配
                throw new IllegalArgumentException(MessageFormat.format("mapper [{0}] " +
                                "declared primary key parameter type is [{1}] ,but should implement [{2}] ," +
                                "because it is a composite primary key" ,
                        mapperClass, defineKeyParameterClass ,matchKeyParameterClass));
            }
        }
    }

}
