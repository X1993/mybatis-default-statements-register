package com.github.ibatis.statement.base.condition;

/**
 * 过滤值选择策略
 * @Author: junjie
 * @Date: 2020/9/11
 */
public enum Strategy
{
    /**
     * 强制使用默认值
     *  `column` = {@link Condition#value()}
     */
    DEFAULT,
    /**
     * 强制使用自定义值
     *  `column` = #{propertyName}
     */
    CUSTOM,
    /**
     * 如果有自定义值使用自定义值，否则使用默认值
     * <choose>
     *     <when test="property != null">
     *          `column` = #{propertyName}
     *     </when>
     *     <otherwise>
     *          `column` = {@link Condition#value()}
     *     </otherwise>
     * </choose>
     */
    CUSTOM_MISS_DEFAULT,
    /**
     * 如果有自定义值使用自定义值，否则略过该条件
     * <if test="property != null">
     *     `column` = #{propertyName}
     * </if>
     */
    CUSTOM_MISS_SKIP,

}
