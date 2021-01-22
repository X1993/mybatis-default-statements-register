package com.github.ibatis.statement.base.logical;

import com.github.ibatis.statement.base.core.TableSchemaResolutionStrategy;
import com.github.ibatis.statement.base.core.matedata.ColumnMateData;
import com.github.ibatis.statement.base.core.matedata.EntityMateData;
import com.github.ibatis.statement.base.core.matedata.TableMateData;
import org.apache.ibatis.type.JdbcType;
import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.List;

/**
 * @Author: X1993
 * @Date: 2020/3/4
 */
public class DefaultLogicalColumnMateDataParser implements LogicalColumnMateDataParser {

    /**
     * 扩展：自定义解析规则
     */
    private List<LogicalColumnMateDataParser> customParsers = Collections.EMPTY_LIST;

    public DefaultLogicalColumnMateDataParser() {
    }

    public DefaultLogicalColumnMateDataParser(List<LogicalColumnMateDataParser> customParsers) {
        this.setCustomParsers(customParsers);
    }

    @Override
    public void parse(EntityMateData entityMateData)
    {
        NoLogical noLogical = entityMateData.getEntityClass().getAnnotation(NoLogical.class);
        if (noLogical != null){
            return;
        }

        Logical logical = this.parseLogical(entityMateData.getEntityClass());
        if (logical != null) {
            String logicalColumnName = logical.columnName();
            TableMateData tableMateData = entityMateData.getTableMateData();

            TableSchemaResolutionStrategy schemaResolutionStrategy = entityMateData.getSchemaResolutionStrategy();
            ColumnMateData logicalColumnMateData = tableMateData.getColumnMateDataMap().get(logicalColumnName);
            if (logicalColumnMateData == null) {
                if (TableSchemaResolutionStrategy.DATA_BASE.equals(schemaResolutionStrategy)) {
                    //表上没有对应的列
                    throw new IllegalArgumentException(new StringBuilder("table [")
                            .append(tableMateData.getTableName())
                            .append("] not found column [")
                            .append(logicalColumnName)
                            .append("]").toString());
                } else if (TableSchemaResolutionStrategy.ENTITY.equals(schemaResolutionStrategy)){
                    logicalColumnMateData = new ColumnMateData();
                    logicalColumnMateData.setJdbcType(JdbcType.UNDEFINED);
                    logicalColumnMateData.setPrimaryKey(false);
                    logicalColumnMateData.setColumnName(logicalColumnName);
                    tableMateData.addColumnMateData(logicalColumnMateData);
                }
            }

            //主键列不能作为逻辑列
            if (entityMateData.getKeyPrimaryColumnPropertyMappings().keySet().contains(logicalColumnName))
            {
                throw new IllegalArgumentException(new StringBuilder("table [")
                        .append(tableMateData.getTableName())
                        .append("] primary key column [").append(logical.columnName())
                        .append("] cannot be used as logical column").toString());
            }

            LogicalColumnMateData mateData = new LogicalColumnMateData();
            mateData.setColumnName(logicalColumnMateData.getColumnName());
            mateData.setDataType(logicalColumnMateData.getDataType());
            mateData.setPrimaryKey(logicalColumnMateData.isPrimaryKey());
            mateData.setExistValue(logical.existValue());
            mateData.setNotExistValue(logical.notExistValue());
            entityMateData.setLogicalColumnMateData(mateData);

            return;
        }

        for (LogicalColumnMateDataParser parser : customParsers) {
            parser.parse(entityMateData);
            if (entityMateData.getLogicalColumnMateData() != null){
                return;
            }
        }
    }

    private Logical parseLogical(Class<?> entityClass){
        Annotation[] annotations = entityClass.getAnnotations();
        for (Annotation annotation : annotations) {
            if (annotation instanceof Logical){
                return (Logical) annotation;
            }else {
                Logical logical = annotation.annotationType().getAnnotation(Logical.class);
                if (logical != null){
                    return logical;
                }
            }
        }
        return null;
    }

    public List<LogicalColumnMateDataParser> getCustomParsers() {
        return customParsers;
    }

    public void setCustomParsers(List<LogicalColumnMateDataParser> customParsers) {
        Collections.sort(customParsers);
        this.customParsers = customParsers;
    }
}
