package com.github.ibatis.statement.base.core.matedata;

import com.github.ibatis.statement.base.core.MappingStrategy;
import com.github.ibatis.statement.register.MappedStatementFactory;
import lombok.Data;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.TypeHandler;
import java.lang.reflect.Field;
import java.util.Objects;

/**
 * 实体类属性元数据
 * @Author: X1993
 * @Date: 2020/3/10
 */
@Data
public class PropertyMateData implements Cloneable{

    /**
     * 映射的列名
     */
    private String mappingColumnName;

    /**
     * 映射的类属性
     */
    private Field field;

    /**
     * 如果不包含{@link SqlCommandType#INSERT},则自动注入的插入语句（具体实现逻辑由
     * 每个{@link MappedStatementFactory}自定义，也可不实现）会过滤该列的插入
     * 如果不包含{@link SqlCommandType#UPDATE}, ...修改...过滤该列的修改
     * 如果不包含{@link SqlCommandType#SELECT}, ...查询...过滤该列的查询
     */
    private SqlCommandType[] commandTypeMappings = new SqlCommandType[]{SqlCommandType.UPDATE ,
            SqlCommandType.INSERT ,SqlCommandType.SELECT};

    /**
     * 类型转换器
     */
    private Class<? extends TypeHandler<?>> typeHandlerClass;

    /**
     * 映射的列jdbcType
     */
    private JdbcType jdbcType;

    /**
     * 列映射策略
     */
    private MappingStrategy mappingStrategy = MappingStrategy.AUTO;

    public PropertyMateData() {
    }

    public PropertyMateData(String mappingColumnName, Field field) {
        this.mappingColumnName = mappingColumnName;
        this.field = field;
    }

    @Override
    public PropertyMateData clone() throws CloneNotSupportedException {
        return (PropertyMateData) super.clone();
    }

    public boolean isSelectMapping() {
        for (SqlCommandType commandTypeMapping : commandTypeMappings) {
            if (SqlCommandType.SELECT.equals(commandTypeMapping)){
                return true;
            }
        }
        return false;
    }

    public boolean isUpdateMapping() {
        for (SqlCommandType commandTypeMapping : commandTypeMappings) {
            if (SqlCommandType.UPDATE.equals(commandTypeMapping)){
                return true;
            }
        }
        return false;
    }

    public boolean isInsertMapping() {
        for (SqlCommandType commandTypeMapping : commandTypeMappings) {
            if (SqlCommandType.INSERT.equals(commandTypeMapping)){
                return true;
            }
        }
        return false;
    }

    public boolean isRequiredMappingColumn() {
        return MappingStrategy.REQUIRED.equals(mappingStrategy)
                || MappingStrategy.PRIMARY_KEY.equals(mappingStrategy);
    }

    public boolean isPrimaryKey() {
        return MappingStrategy.PRIMARY_KEY.equals(mappingStrategy);
    }

    public boolean isIgnore() {
        return MappingStrategy.IGNORE.equals(mappingStrategy);
    }

    public void setCommandTypeMappings(SqlCommandType[] commandTypeMappings) {
        if (commandTypeMappings == null){
            this.commandTypeMappings = new SqlCommandType[0];
        }else {
            this.commandTypeMappings = commandTypeMappings;
        }
    }

    public Class<?> getType(){
        return field.getType();
    }

    public String getPropertyName(){
        return getField().getName();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        PropertyMateData that = (PropertyMateData) o;
        return Objects.equals(mappingColumnName, that.mappingColumnName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(mappingColumnName);
    }

}
