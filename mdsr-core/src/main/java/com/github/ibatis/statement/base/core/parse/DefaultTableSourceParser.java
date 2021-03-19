package com.github.ibatis.statement.base.core.parse;

import com.github.ibatis.statement.base.core.Entity;
import com.github.ibatis.statement.util.StringUtils;
import lombok.Data;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

/**
 * @author X1993
 * @date 2020/2/23
 */
@Data
public class DefaultTableSourceParser implements TableSourceParser {

    /**
     * 扩展：自定义解析规则
     */
    private List<TableSourceParser> customParsers = new ArrayList<>();

    /**
     * 默认表名为实体类名驼峰转下划线
     */
    private Function<Class<?> ,String> defaultTableNameFunction = clazz ->
            StringUtils.camelCaseToUnderscore(clazz.getSimpleName());

    /**
     * 默认每个实体类都存在映射的表，规则为{@link #defaultTableNameFunction}
     */
    private boolean defaultMappingTable = true;

    public DefaultTableSourceParser() {
    }

    public DefaultTableSourceParser(List<TableSourceParser> customParsers) {
        this.setCustomParsers(customParsers);
    }

    @Override
    public Optional<Source> parse(Class<?> entityClass)
    {
        //优先级最高
        Entity entityAnnotation = entityClass.getAnnotation(Entity.class);
        if (entityAnnotation != null){
            String tableName = entityAnnotation.tableName();
            if (tableName == null || "".equals(tableName)){
                if (defaultTableNameFunction != null){
                    tableName = defaultTableNameFunction.apply(entityClass);
                }else {
                    throw new IllegalArgumentException(MessageFormat.format(
                            "@Entity#tableName() is emtpy on class {0}" ,entityClass));
                }
            }
            return Optional.of(new Source(tableName ,entityAnnotation.resolutionStrategy()));
        }

        for (TableSourceParser customParser : customParsers) {
            Optional<Source> optional = customParser.parse(entityClass);
            if (optional.isPresent()){
                return optional;
            }
        }

        //如果默认解析失败再尝试自定义解析
        return defaultMappingTable && defaultTableNameFunction != null ? Optional.ofNullable(new Source(
                defaultTableNameFunction.apply(entityClass))) : Optional.empty();
    }

}
