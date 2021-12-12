package com.github.ibatis.statement.register.schema;

import org.apache.ibatis.session.SqlSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.*;

/**
 * @author X1993
 * @date 2020/9/26
 */
public class DefaultTableSchemaQueryRegister implements TableSchemaQueryRegister {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultTableSchemaQueryRegister.class);

    private final List<TableSchemaQuery> tableSchemaQueries = new ArrayList<>();

    public DefaultTableSchemaQueryRegister(boolean registerBySPI)
    {
        if (registerBySPI) {
            loadTableSchemaQueriesBySPI();
        }
    }

    public DefaultTableSchemaQueryRegister(){
        this(true);
    }

    public void register(TableSchemaQuery ... tableSchemaQueries) {
        for (TableSchemaQuery tableSchemaQuery : tableSchemaQueries) {
            this.tableSchemaQueries.add(tableSchemaQuery);
        }
    }

    private final void loadTableSchemaQueriesBySPI(){
        ServiceLoader<TableSchemaQuery> serviceLoader = ServiceLoader.load(TableSchemaQuery.class);
        for (TableSchemaQuery tableSchemaQuery : serviceLoader) {
            tableSchemaQueries.add(tableSchemaQuery);
        }
    }

    @Override
    public Optional<TableSchemaQuery> getTableSchemaQuery(SqlSession sqlSession ,String databaseProductName)
    {
        for (TableSchemaQuery tableSchemaQuery : tableSchemaQueries) {
            if (tableSchemaQuery.match(sqlSession ,databaseProductName)){
                return Optional.of(tableSchemaQuery);
            }
        }

        LOGGER.warn("don't match TableSchemaQuery for data base type [{}]" ,databaseProductName);
        return Optional.empty();
    }

}
