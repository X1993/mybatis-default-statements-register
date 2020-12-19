package com.github.ibatis.statement.register.database;

import com.github.ibatis.statement.base.core.matedata.ColumnMateData;
import com.github.ibatis.statement.base.core.matedata.KeyColumnUsage;
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
 * MySQL/MariaDB 表结构查询接口
 * @Author: junjie
 * @Date: 2020/2/21
 */
public class MysqlTableSchemaQuery extends AbstractTableSchemaQuery
{
    private final static Logger LOGGER = LoggerFactory.getLogger(MysqlTableSchemaQuery.class);

    private List<ColumnMateData> queryTableColumns(SqlSession sqlSession , String tableName) {
        return this.getTableSchemaMapper(sqlSession)
                .map(mapper -> mapper.tableColumnMateData(tableName)
                        .stream()
                        .peek(columnMateData -> columnMateData.setJdbcType(
                                mappingJdbcType(columnMateData.getDataType())))
                        .collect(Collectors.toList()))
                .orElseGet(() -> new ArrayList());
    }

    @Override
    public Optional<TableMateData> queryTable(SqlSession sqlSession ,String tableName)
    {
        MysqlTableSchemaMapper tableSchemaMapper = this.getTableSchemaMapper(sqlSession).orElse(null);
        if (tableSchemaMapper != null){
            TableMateData tableMateData = tableSchemaMapper.tableMateData(tableName);
            tableMateData.setType(mappingTableType(tableMateData.getTableType()));
            tableMateData.setColumnMateDataList(queryTableColumns(sqlSession, tableName));
            tableMateData.setKeyColumnUsages(tableSchemaMapper.keyColumnUsage(tableName));
            return Optional.of(tableMateData);
        }else {
            LOGGER.warn("not exist table [{}]" ,tableName);
            return Optional.empty();
        }
    }

    @Override
    public boolean match(SqlSession sqlSession ,String databaseProductName) {
        return "MYSQL".equalsIgnoreCase(databaseProductName) || "MariaDB".equalsIgnoreCase(databaseProductName);
    }

    @Override
    public TableMateData.Type mappingTableType(String tableType) {
        if (tableType == null || "".equals(tableType)) {
            return TableMateData.Type.UNDEFINED;
        }
        tableType = tableType.replaceAll(" ", "_").toUpperCase();
        try {
            return TableMateData.Type.valueOf(tableType);
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

    /**
     * 查询Mysql表schema
     * @Author: junjie
     * @Date: 2020/3/19
     */
    public interface MysqlTableSchemaMapper {

        /**
         * 查询表信息
         * @param tableName
         * @return
         */
        @Select("SELECT table_name as tableName, table_type as tableType FROM information_schema.`TABLES`\n" +
                "WHERE TABLE_SCHEMA = (SELECT DATABASE()) AND table_name = #{0}")
        TableMateData tableMateData(String tableName);

        /**
         * 查询列信息
         * @param tableName
         * @return
         */
        @Select("SELECT column_name as columnName, data_type as dataType, column_key = 'pri' as primaryKey\n" +
                "FROM information_schema.COLUMNS WHERE table_name = #{0} AND table_schema = (SELECT DATABASE())")
        List<ColumnMateData> tableColumnMateData(String tableName);

        /**
         * 查询主键信息
         * @param tableName
         * @return
         */
        @Select("SELECT column_name as columnName, ordinal_position as ordinalPosition " +
                "FROM information_schema.KEY_COLUMN_USAGE WHERE table_name = #{0} AND " +
                "table_schema = (SELECT DATABASE()) ORDER BY ordinal_position ASC")
        List<KeyColumnUsage> keyColumnUsage(String tableName);

    }

}
