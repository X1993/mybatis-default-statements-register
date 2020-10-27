package com.github.ibatis.statement.register.database;

import com.github.ibatis.statement.base.core.matedata.ColumnMateData;
import com.github.ibatis.statement.base.core.matedata.TableMateData;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.binding.MapperRegistry;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.*;
import java.util.stream.Collectors;

/**
 * H2 表结构查询接口
 * @Author: junjie
 * @Date: 2020/2/21
 */
public class H2TableSchemaQuery extends AbstractTableSchemaQuery
{
    private final static Logger LOGGER = LoggerFactory.getLogger(H2TableSchemaQuery.class);

    @Override
    public List<ColumnMateData> queryTableColumns(SqlSession sqlSession ,String tableName)
    {
        Set<String> primaryKeySet = this.getTableSchemaMapper(sqlSession)
                .map(mapper -> mapper.primaryKeys(tableName))
                .orElse(Collections.EMPTY_SET);

        return this.getTableSchemaMapper(sqlSession)
                .map(mapper -> mapper.tableColumnMateData(tableName)
                        .stream()
                        .peek(columnMateData -> columnMateData.setJdbcType(
                                mappingJdbcType(columnMateData.getDataType())))
                        .peek(columnMateData -> columnMateData.setPrimaryKey(
                                primaryKeySet.contains(columnMateData.getColumnName().toUpperCase()))
                        ).collect(Collectors.toList()))
                .orElseGet(() -> new ArrayList());
    }

    @Override
    public Optional<TableMateData> queryTable(SqlSession sqlSession ,String tableName)
    {
        Optional<TableMateData> optional = this.getTableSchemaMapper(sqlSession)
                .map(mapper -> mapper.tableMateData(tableName))
                .map(tableMateData -> {
                    tableMateData.setType(mappingTableType(tableMateData.getTableType()));
                    tableMateData.setColumnMateDataList(queryTableColumns(sqlSession, tableName));
                    return tableMateData;
                });

        if (!optional.isPresent()){
            LOGGER.warn("not exist table [{}]" ,tableName);
        }
        return optional;
    }

    @Override
    public boolean match(SqlSession sqlSession ,String databaseProductName) {
        return "H2".equals(databaseProductName);
    }

    @Override
    public TableMateData.Type mappingTableType(String tableType) {
        if (tableType == null || "".equals(tableType)) {
            return TableMateData.Type.UNDEFINED;
        }
        tableType = tableType.replaceAll(" ", "_").toUpperCase();
        switch (tableType){
            case "TABLE":
                return TableMateData.Type.BASE_TABLE;
            case "SYSTEM_TABLE":
                return TableMateData.Type.SYSTEM_VIEW;
            default:
                try {
                    return TableMateData.Type.valueOf(tableType);
                }catch (IllegalArgumentException e){
                    LOGGER.warn("can't mapping TableMateData.Type for tableType {}" ,tableType);
                    return TableMateData.Type.UNDEFINED;
                }
        }
    }

    private Optional<H2TableSchemaMapper> getTableSchemaMapper(SqlSession sqlSession)
    {
        Configuration configuration = sqlSession.getConfiguration();
        MapperRegistry mapperRegistry = configuration.getMapperRegistry();
        Class<H2TableSchemaMapper> mapperType = H2TableSchemaMapper.class;
        if (!mapperRegistry.hasMapper(mapperType)){
            mapperRegistry.addMapper(mapperType);
        }
        return Optional.ofNullable(mapperRegistry.getMapper(mapperType ,sqlSession));
    }

    /**
     * 查询H2表schema
     * @Author: junjie
     * @Date: 2020/3/19
     */
    public interface H2TableSchemaMapper {

        /**
         * 查询表信息
         * @param tableName
         * @return
         */
        @Select("SELECT table_name as tableName, table_type as tableType FROM information_schema.`TABLES`\n" +
                "WHERE TABLE_CATALOG = (SELECT DATABASE()) AND table_name = #{0}")
        TableMateData tableMateData(String tableName);

        /**
         * 查询列信息
         * @param tableName
         * @return
         */
        @Select("SELECT column_name as columnName, type_name as dataType FROM information_schema.COLUMNS " +
                "WHERE table_name = #{0} AND TABLE_CATALOG = (SELECT DATABASE())")
        List<ColumnMateData> tableColumnMateData(String tableName);

        /**
         * 查询主键信息
         * @param tableName
         * @return
         */
        @Select("SELECT column_name as columnName FROM information_schema.KEY_COLUMN_USAGE " +
                "WHERE table_name = #{0} AND TABLE_CATALOG = (SELECT DATABASE())")
        Set<String> primaryKeys(String tableName);

    }


}