package com.github.ibatis.statement.base.core.parse;

import com.github.ibatis.statement.base.core.Entity;
import com.github.ibatis.statement.base.core.TableSchemaResolutionStrategy;
import com.github.ibatis.statement.util.StringUtils;
import java.util.*;

/**
 * @author junjie
 * @date 2020/2/23
 */
public class DefaultTableSourceParser implements TableSourceParser {

    /**
     * 扩展：自定义解析规则
     */
    private List<TableSourceParser> customParsers = new ArrayList<>();

    /**
     * 如果无法解析实体类映射的表名，使用实体类名称
     * （如果{@link DefaultTableSourceParser#mapUnderscoreToCamelCase == true}驼峰转下划线）
     */
    private boolean defaultEntityMappingTable = true;

    /**
     * 实体类名驼峰转下划线为表名
     */
    private boolean mapUnderscoreToCamelCase = true;

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
                tableName = useEntityNameAsColName(entityClass.getSimpleName());
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
        return Optional.ofNullable(defaultEntityMappingTable ?
                new Source(useEntityNameAsColName(entityClass.getSimpleName()) , TableSchemaResolutionStrategy.GLOBAL) : null);
    }

    private String useEntityNameAsColName(String entityName){
        if (mapUnderscoreToCamelCase){
            return StringUtils.camelCaseToUnderscore(entityName);
        }else {
            return entityName;
        }
    }

    public boolean isMapUnderscoreToCamelCase() {
        return mapUnderscoreToCamelCase;
    }

    public void setMapUnderscoreToCamelCase(boolean mapUnderscoreToCamelCase) {
        this.mapUnderscoreToCamelCase = mapUnderscoreToCamelCase;
    }

    public boolean isDefaultEntityMappingTable() {
        return defaultEntityMappingTable;
    }

    public void setDefaultEntityMappingTable(boolean defaultEntityMappingTable) {
        this.defaultEntityMappingTable = defaultEntityMappingTable;
    }

    public List<TableSourceParser> getCustomParsers() {
        return customParsers;
    }

    public void setCustomParsers(List<TableSourceParser> customParsers) {
        Collections.sort(customParsers);
        this.customParsers = customParsers;
    }

}
