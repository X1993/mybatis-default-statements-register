package com.github.ibatis.statement.register.factory;

import com.github.ibatis.statement.base.condition.DefaultColumnConditionParser;
import com.github.ibatis.statement.base.core.matedata.EntityMateData;
import java.lang.annotation.*;

/**
 * 为方法参数添加if标签
 * @see DefaultColumnConditionParser#parse(EntityMateData)
 * @Author: junjie
 * @Date: 2020/7/24
 */
@Target({ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface If {

    String NULL = "";

    String PARAM_PLACEHOLDER = "#{?}";

    /**
     * if标签条件
     * #{param}：方法参数占位符
     * @return
     */
    String test() default PARAM_PLACEHOLDER + " != null";

    /**
     * {@link #otherwise()}设置了值且{@link #test()} == false，生效。
     * ！如果值为字符串需要自己添加引号
     * @return
     */
    String otherwise() default NULL;

}
