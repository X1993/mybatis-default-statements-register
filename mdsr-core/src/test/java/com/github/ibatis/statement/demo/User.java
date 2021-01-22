package com.github.ibatis.statement.demo;

import com.github.ibatis.statement.base.condition.Condition;
import com.github.ibatis.statement.base.core.Column;
import com.github.ibatis.statement.base.core.Entity;
import com.github.ibatis.statement.base.dv.DefaultValue;
import com.github.ibatis.statement.base.logical.Logical;
import com.github.ibatis.statement.mapper.param.ConditionRule;
import lombok.Data;
import org.apache.ibatis.mapping.SqlCommandType;
import java.util.Date;

/**
 * @author X1993
 * @date 2020/9/9
 */
@Logical(columnName = "removed" ,existValue = "0" ,notExistValue = "1")//定义逻辑列
@Entity(tableName = "user")//申明实体映射的表
@DefaultValue(commandTypes = SqlCommandType.UPDATE,columnName = "note" ,value = "CONCAT(&{column} ,'1')")
@DefaultValue(columnName = "address2" ,value = "'中国'" ,overwriteCustom = false)
@Data
public class User
{
    @Column(value = "id")//申明字段映射的列
    private Integer id;
    @Column("name")
    private String name;

    @DefaultValue(commandTypes = SqlCommandType.INSERT, value = "now()")
    @Condition(commandTypes = SqlCommandType.SELECT, rule = ConditionRule.GT , value = "'2020-08-12 00:00:00'")
    @Condition(commandTypes = SqlCommandType.DELETE, rule = ConditionRule.LT , value = "'2020-08-11 00:00:00'")
    @Condition(commandTypes = SqlCommandType.UPDATE, rule = ConditionRule.BETWEEN , value = "'2020-08-11 00:00:00' AND '2020-08-12 00:00:00'")
    @Column(value = "create_time" ,commandTypeMappings = {SqlCommandType.SELECT ,SqlCommandType.INSERT})
    private Date createTime;

//    @DefaultValue(commandTypes = {SqlCommandType.INSERT ,SqlCommandType.UPDATE}, value = "now()")
    private Date updateTime;

    @Column("address")
    @DefaultValue(value = "'中国'" ,overwriteCustom = false)
    private String address;

    @DefaultValue(commandTypes = {SqlCommandType.UPDATE} ,value = "&{column} + 1")
    private int version;

}
