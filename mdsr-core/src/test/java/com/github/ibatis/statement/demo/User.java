package com.github.ibatis.statement.demo;

import com.github.ibatis.statement.base.condition.Condition;
import com.github.ibatis.statement.base.condition.Strategy;
import com.github.ibatis.statement.base.core.Column;
import com.github.ibatis.statement.base.core.Entity;
import com.github.ibatis.statement.base.dv.DefaultValue;
import com.github.ibatis.statement.base.logical.Logical;
import com.github.ibatis.statement.mapper.param.ConditionRule;
import org.apache.ibatis.mapping.SqlCommandType;
import java.util.Date;

/**
 * @author junjie
 * @date 2020/9/9
 */
@Logical(columnName = "removed" ,existValue = "0" ,notExistValue = "1")//定义逻辑列
@Entity(tableName = "user")//申明实体映射的表
@DefaultValue(columnName = "note" ,value = "CONCAT(&{column} ,'1')")
@DefaultValue(columnName = "address2" ,value = "'中国'" ,overwriteCustom = false)
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
    @Condition(commandTypes = {SqlCommandType.UPDATE} ,strategy = Strategy.CUSTOM)
    private int version;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }
}
