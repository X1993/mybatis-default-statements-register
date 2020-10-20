package com.github.ibatis.statement.register.database;

import com.github.ibatis.statement.base.core.matedata.ColumnMateData;
import com.github.ibatis.statement.base.core.matedata.TableMateData;
import org.apache.ibatis.annotations.Select;
import java.util.List;

/**
 * 查询Mysql表结构
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

}
