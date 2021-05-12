package com.github.ibatis.statement.base.core.matedata;

import com.github.ibatis.statement.mapper.param.ConditionRule;
import lombok.Data;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.scripting.xmltags.IfSqlNode;
import org.apache.ibatis.scripting.xmltags.StaticTextSqlNode;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.type.JdbcType;
import java.lang.reflect.Field;
import java.util.Optional;
import java.util.function.Function;

/**
 * 数据库表-列 -》 类型-属性 映射
 * @Author: X1993
 * @Date: 2020/3/10
 */
@Data
public class ColumnPropertyMapping implements Cloneable{

    /**
     * 类-属性 元数据
     */
    private PropertyMateData propertyMateData;

    /**
     * 表->列 元数据
     */
    private ColumnMateData columnMateData;

    public ColumnPropertyMapping() {
    }

    public ColumnPropertyMapping(PropertyMateData propertyMateData, ColumnMateData columnMateData) {
        this.propertyMateData = propertyMateData;
        this.columnMateData = columnMateData;
    }

    public String getColumnName(){
        return columnMateData.getColumnName();
    }

    public String getPropertyName(){
        return getPropertyMateData().getField().getName();
    }

    public String getEscapeColumnName(){
        return getColumnMateData().getEscapeColumnName();
    }

    public boolean isPrimaryKey(){
        return columnMateData.isPrimaryKey();
    }

    public JdbcType getJdbcType()
    {
        JdbcType jdbcType = getPropertyMateData().getJdbcType();
        if (jdbcType == null || jdbcType == JdbcType.NULL){
            jdbcType = getColumnMateData().getJdbcType();
            if (jdbcType == JdbcType.NULL){
                jdbcType = null;
            }
        }
        return jdbcType;
    }

    @Override
    public ColumnPropertyMapping clone() throws CloneNotSupportedException {
        ColumnPropertyMapping cloneColumnPropertyMapping = (ColumnPropertyMapping) super.clone();
        cloneColumnPropertyMapping.setColumnMateData(columnMateData.clone());
        cloneColumnPropertyMapping.setPropertyMateData(propertyMateData.clone());
        return cloneColumnPropertyMapping;
    }

    /**
     * 生成属性占位符
     * <p>
     *     #{propertyName,jdbcType=XXX}
     * </p>
     * @param propertyNameFunction
     * @return
     */
    public StringBuilder createPropertyPrecompiledText(Function<String ,String> propertyNameFunction)
    {
        PropertyMateData propertyMateData = this.getPropertyMateData();
        return new StringBuilder("#{")
                .append(propertyNameFunction.apply(propertyMateData.getPropertyName()))
                .append(Optional.ofNullable(getJdbcType())
                        .filter(jdbcType -> jdbcType != JdbcType.NULL)
                        .map(jdbcType -> ",jdbcType=" + jdbcType).orElse(""))
                .append(Optional.ofNullable(propertyMateData.getTypeHandlerClass())
                        .map(typeHandlerClass -> ",typeHandler=" + typeHandlerClass.getName()).orElse(""))
                .append("} ");
    }

    /**
     * `column` = #{propertyName ,jdbcType=XX ,typeHandler=XX};
     * @return
     */
    public StringBuilder createConditionSqlContent(ConditionRule rule ,
                                                   Function<String , String> propertyNameFunction){
        return new StringBuilder(getEscapeColumnName())
                .append(" ")
                .append(rule.expression)
                .append(" ")
                .append(createPropertyPrecompiledText(propertyNameFunction));
    }

    /**
     * `column` = #{propertyName ,jdbcType=XX ,typeHandler=XX};
     * @return
     */
    public StringBuilder createEqSqlContent(Function<String ,String> propertyNameFunction){
        return createConditionSqlContent(ConditionRule.EQ ,propertyNameFunction);
    }

    /**
     * <if test="propertyName != null">
     *    `column` = #{propertyName ,jdbcType=XX ,typeHandler=XX} [delimiter]
     * </if>
     * @return
     */
    public IfSqlNode createEqIfSqlNode(Function<String ,String> propertyNameFunction ,
                                       Function<StringBuilder ,StringBuilder> sqlContentFunction)
    {
        return new IfSqlNode(new StaticTextSqlNode(
                sqlContentFunction.apply(createEqSqlContent(propertyNameFunction)).toString()) ,
                propertyNameFunction.apply(getPropertyName()) + " != null");
    }

    /**
     * 为指定的列属性映射构建{@link ParameterMapping}
     * @param propertyNameFunction
     * @param configuration
     * @return
     */
    public ParameterMapping buildParameterMapping(Function<String ,String> propertyNameFunction,
                                                  Configuration configuration)
    {
        ColumnMateData columnMateData = this.getColumnMateData();
        PropertyMateData propertyMateData = this.getPropertyMateData();

        Field propertyField = propertyMateData.getField();
        return new ParameterMapping.Builder(configuration,
                propertyNameFunction.apply(propertyField.getName()), propertyField.getType())
                .jdbcType(columnMateData.getJdbcType())
                .build();
    }

    /**
     * 为指定的列属性映射构建{@link ParameterMapping}
     * @param configuration
     * @return
     */
    public ParameterMapping buildParameterMapping(Configuration configuration){
        return buildParameterMapping(propertyName -> propertyName ,configuration);
    }

}
