package com.github.ibatis.statement.register.listener;

import com.github.ibatis.statement.base.core.matedata.EntityMateData;
import com.github.ibatis.statement.base.core.matedata.TableMateData;
import com.github.ibatis.statement.mapper.TableMapper;
import com.github.ibatis.statement.register.DefaultStatementAutoRegister;

import java.text.MessageFormat;

/**
 * @Author: X1993
 * @Date: 2020/10/29
 */
public class TableMapperListener implements DefaultStatementAutoRegister.Listener {

    @Override
    public void verify(EntityMateData entityMateData, Class mapperClass) {
        TableMateData tableMateData = entityMateData.getTableMateData();
        if (TableMapper.class.isAssignableFrom(mapperClass)){
            TableMateData.Type type = tableMateData.getType();
            if ((TableMateData.Type.VIEW.equals(type) || TableMateData.Type.SYSTEM_VIEW.equals(type))){
                //如果是视图不应该继承表相关的mapper接口
                throw new IllegalArgumentException(MessageFormat.format("mapper [{0}] implement [{1}] ," +
                                "but mapper entity mapping table [{2}] type is [{3}]" ,
                        mapperClass ,TableMapper.class ,entityMateData.getTableName() ,tableMateData.getTableType()));
            }
        }
    }

}
