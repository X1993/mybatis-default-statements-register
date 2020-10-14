package com.github.ibatis.statement.register.database;

import com.github.ibatis.statement.base.core.matedata.ColumnMateData;
import com.github.ibatis.statement.base.core.matedata.TableMateData;
import org.apache.ibatis.binding.MapperRegistry;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.type.JdbcType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.*;
import java.util.stream.Collectors;

/**
 * MySQL/MariaDB 表结构查询接口
 * @Author: junjie
 * @Date: 2020/2/21
 */
public class MysqlTableSchemaQuery implements TableSchemaQuery
{
    private final static Logger LOGGER = LoggerFactory.getLogger(MysqlTableSchemaQuery.class);

    @Override
    public String[] databaseType() {
        return new String[]{"MySQL" ,"MariaDB"};
    }

    @Override
    public List<ColumnMateData> queryTableColumns(SqlSession sqlSession ,String tableName) {
        return this.getTableSchemaMapper(sqlSession)
                .map(mapper -> mapper.tableColumnMateData(tableName)
                        .stream()
                        .peek(columnMateData -> columnMateData.setJdbcType(
                                mappingJdbcType(columnMateData.getDataType())))
                        .collect(Collectors.toList()))
                .orElseGet(() -> new ArrayList());
    }

    @Override
    public Optional<TableMateData> queryTable(SqlSession sqlSession ,String tableName) {
        Optional<TableMateData> optional = this.getTableSchemaMapper(sqlSession)
                .map(mapper -> mapper.tableMateData(tableName))
                .map(tableMateData -> {
                    tableMateData.setType(mappingTableType(tableMateData.getTableType()));
                    tableMateData.setColumnMateDataList(queryTableColumns(sqlSession, tableName));
                    return tableMateData;
                });

        if (!optional.isPresent()){
            LOGGER.warn("not found table [{}]" ,tableName);
        }
        return optional;
    }

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

    @Override
    public TableMateData.Type mappingTableType(String tableType) {
        if (tableType == null || "".equals(tableType)) {
            return TableMateData.Type.UNDEFINED;
        }
        try {
            return TableMateData.Type.valueOf(tableType.replaceAll(" ", "_").toUpperCase());
        }catch (IllegalArgumentException e){
            LOGGER.warn("can't mapping TableMateData.Type for tableType {}" ,tableType);
            return TableMateData.Type.UNDEFINED;
        }
    }

    private Optional<MysqlTableSchemaMapper> getTableSchemaMapper(SqlSession sqlSession)
    {
        Configuration configuration = sqlSession.getConfiguration();
        MapperRegistry mapperRegistry = configuration.getMapperRegistry();
        Class<MysqlTableSchemaMapper> mapperType = MysqlTableSchemaMapper.class;
        if (!mapperRegistry.hasMapper(mapperType)){
            mapperRegistry.addMapper(mapperType);
        }
        return Optional.ofNullable(mapperRegistry.getMapper(mapperType ,sqlSession));
    }

}
