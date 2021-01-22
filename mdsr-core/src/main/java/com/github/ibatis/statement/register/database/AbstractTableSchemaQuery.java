package com.github.ibatis.statement.register.database;

import org.apache.ibatis.type.JdbcType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @Author: X1993
 * @Date: 2020/10/21
 */
public abstract class AbstractTableSchemaQuery implements TableSchemaQuery {

    private final static Logger LOGGER = LoggerFactory.getLogger(AbstractTableSchemaQuery.class);

    @Override
    public JdbcType mappingJdbcType(String dataType) {
        if (dataType == null || "".equals(dataType)){
            return JdbcType.UNDEFINED;
        }
        dataType = dataType.toUpperCase();
        switch (dataType){
            case "LONGTEXT":
                return JdbcType.LONGNVARCHAR;
            case "DATETIME":
                return JdbcType.TIMESTAMP;
            case "BIT":
                return JdbcType.BOOLEAN;
            case "TEXT":
                return JdbcType.CLOB;
            case "INT":
                return JdbcType.INTEGER;
            case "ENUM":
                return JdbcType.VARCHAR;
            default:
                try {
                    return JdbcType.valueOf(dataType);
                }catch (IllegalArgumentException e){
                    LOGGER.warn("can't mapping JdbcType for dataType {}" ,dataType);
                    return JdbcType.UNDEFINED;
                }
        }
    }

}
