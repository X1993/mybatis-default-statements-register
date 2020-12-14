package com.github.ibatis.statement.register.database;

import org.apache.ibatis.session.SqlSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;

/**
 * @author junjie
 * @date 2020/9/26
 */
public class DefaultTableSchemaQueryRegister implements TableSchemaQueryRegister {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultTableSchemaQueryRegister.class);

    private final List<TableSchemaQuery> tableSchemaQueries = new ArrayList<>();

    @Override
    public void register(TableSchemaQuery ... tableSchemaQueries) {
        for (TableSchemaQuery tableSchemaQuery : tableSchemaQueries) {
            this.tableSchemaQueries.add(tableSchemaQuery);
        }
    }

    @Override
    public Optional<TableSchemaQuery> getTableSchemaQuery(SqlSession sqlSession)
    {
        String databaseProductName = "UNKNOWN";
        try (Connection connection = sqlSession.getConfiguration().getEnvironment().getDataSource().getConnection()){
            if (!connection.isClosed()) {
                databaseProductName = connection.getMetaData().getDatabaseProductName().toUpperCase();
            }else {
                LOGGER.warn("sqlSession connection is closed");
            }
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        }

        for (TableSchemaQuery tableSchemaQuery : tableSchemaQueries) {
            if (tableSchemaQuery.match(sqlSession ,databaseProductName)){
                return Optional.of(tableSchemaQuery);
            }
        }

        LOGGER.warn("don't match TableSchemaQuery for data base type [{}]" ,databaseProductName);
        return Optional.empty();
    }

}
