package com.github.ibatis.statement.util;

import org.apache.ibatis.session.SqlSession;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * @author jie
 * @date 2021/12/12
 * @description
 */
public class SqlSessionUtils {

    public static String getDatabaseProductName(SqlSession sqlSession)
    {
        String databaseProductName = "UNKNOWN";
        try {
            Connection connection = sqlSession.getConnection();
            if (!connection.isClosed()) {
                databaseProductName = connection.getMetaData().getDatabaseProductName();
            }else {
                throw new IllegalStateException("sqlSession connection is closed ,can't get database product name");
            }
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        }
        return databaseProductName;
    }

}
