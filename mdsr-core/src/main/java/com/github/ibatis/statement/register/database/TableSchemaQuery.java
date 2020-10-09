package com.github.ibatis.statement.register.database;

import com.github.ibatis.statement.base.core.matedata.ColumnMateData;
import com.github.ibatis.statement.base.core.matedata.TableMateData;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.type.JdbcType;
import java.util.List;
import java.util.Optional;

/**
 * 表结构查询接口，不同数据库可能有不同实现
 * @Author: junjie
 * @Date: 2020/2/21
 */
public interface TableSchemaQuery {

    /**
     * 查询指定表包含的列信息
     * @param tableName 表名
     * @return
     */
    List<ColumnMateData> queryTableColumns(SqlSession sqlSession ,String tableName);

    /**
     * 查询指定表信息
     * @param tableName
     * @return
     */
    Optional<TableMateData> queryTable(SqlSession sqlSession ,String tableName);

    /**
     * 支持的数据库类型
     * @return
     */
    String[] databaseType();

    /**
     * 获取不同数据库列类型对应的Jdbc类型
     * @param dataType
     * @return
     */
    JdbcType mappingJdbcType(String dataType);

    /**
     * 获取不同数据库表类型映射的标准类型
     * @param tableType
     * @return
     */
    TableMateData.Type mappingTableType(String tableType);

}