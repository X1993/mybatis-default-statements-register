package com.github.mdsr.sample.model;

import com.github.ibatis.statement.base.core.Column;
import com.github.ibatis.statement.base.core.Entity;
import com.github.ibatis.statement.base.dv.DefaultValue;
import com.github.ibatis.statement.base.logical.Logical;
import org.apache.ibatis.mapping.SqlCommandType;
import java.util.Date;

/**
 * @author X1993
 * @date 2020/9/9
 */
//列名为`removed`的列为逻辑列
@Logical(columnName = "removed" ,existValue = "0" ,notExistValue = "1")
@Entity(tableName = "user")
public class User
{
//    @Column(mappingStrategy = MappingStrategy.PRIMARY_KEY)
    private Integer id;

    private String name;

    //列名为`create_time`的列在执行新增指令时，如果没有指定值，使用默认值now()
    @DefaultValue(commandTypes = SqlCommandType.INSERT, value = "now()")
    //禁止create_time列的修改
    @Column(value = "create_time" ,commandTypeMappings = {SqlCommandType.SELECT ,SqlCommandType.INSERT})
    private Date createTime;

    //列名为`update_time`的列在执行新增/修改指令时，如果没有指定值，使用默认值now()
    @DefaultValue(commandTypes = {SqlCommandType.INSERT ,SqlCommandType.UPDATE}, value = "now()")
    private Date updateTime;

    private String address;

    private String note;

    //利用默认赋值和默认更新条件实现乐观锁
    @DefaultValue(commandTypes = {SqlCommandType.UPDATE} ,value = "&{column} + 1")
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

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }
}
