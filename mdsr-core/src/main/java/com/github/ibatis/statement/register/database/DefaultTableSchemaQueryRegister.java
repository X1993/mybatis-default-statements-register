package com.github.ibatis.statement.register.database;

import org.apache.ibatis.session.SqlSession;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * @author junjie
 * @date 2020/9/26
 */
public class DefaultTableSchemaQueryRegister implements TableSchemaQueryRegister {

    private Map<String ,TableSchemaQuery> tableSchemaQueryMap = new HashMap<>();

    @Override
    public void register(Collection<TableSchemaQuery> tableSchemaQueries) {
        if (tableSchemaQueries != null){
            for (TableSchemaQuery tableSchemaQuery : tableSchemaQueries) {
                register(tableSchemaQuery);
            }
        }
    }

    @Override
    public void register(TableSchemaQuery tableSchemaQuery) {
        for (String databaseName : tableSchemaQuery.databaseType()) {
            tableSchemaQueryMap.put(databaseName.toUpperCase() ,tableSchemaQuery);
        }
    }

    @Override
    public Optional<TableSchemaQuery> getTableSchemaQuery(SqlSession sqlSession)
    {
        Connection connection = sqlSession.getConnection();
        String databaseProductName = null;
        try {
            databaseProductName = connection.getMetaData().getDatabaseProductName();
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        }
        return Optional.ofNullable(tableSchemaQueryMap.get(databaseProductName.toUpperCase()));
    }

}
