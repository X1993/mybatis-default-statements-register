package com.github.ibatis.statement.register.listener;

import com.github.ibatis.statement.base.core.matedata.EntityMateData;
import com.github.ibatis.statement.mapper.KeyParameterType;
import com.github.ibatis.statement.register.DefaultStatementAutoRegister;
import com.github.ibatis.statement.util.TypeUtils;
import java.lang.reflect.Type;
import java.text.MessageFormat;

/**
 * @Author: junjie
 * @Date: 2020/10/29
 */
public class KeyParameterMapperListener implements DefaultStatementAutoRegister.Listener {

    @Override
    public void verify(EntityMateData entityMateData, Class mapperClass) {
        if (KeyParameterType.class.isAssignableFrom(mapperClass)){
            int primaryKeyCount = entityMateData.getPrimaryKeyCount();
            if (primaryKeyCount <= 0) {
                //声明了主键参数但实际上没有主键
                throw new IllegalArgumentException(MessageFormat.format("mapper [{0}] implement [{1}] ," +
                                "but mapper entity mapping table [{2}] no primary key",
                        mapperClass, KeyParameterType.class, entityMateData.getTableName()));
            }
            Class<?> matchKeyParameterClass = entityMateData.getReasonableKeyParameterClass();
            Type defineKeyParameterClass = TypeUtils.parseSuperTypeVariable(mapperClass, KeyParameterType.class.getTypeParameters()[0]);
            if (!TypeUtils.isAssignableFrom(matchKeyParameterClass ,defineKeyParameterClass)){
                //声明的主键参数类型与实际类型不匹配
                throw new IllegalArgumentException(MessageFormat.format("mapper [{0}] " +
                                "declared primary key parameter type is [{1}] ,but should implement [{2}] ," +
                                "because it is a composite primary key" ,
                        mapperClass, defineKeyParameterClass ,matchKeyParameterClass));
            }
        }
    }

}
