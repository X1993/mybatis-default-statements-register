package com.github.ibatis.statement.mapper;

import com.github.ibatis.statement.DataSourceEnvironment;
import com.github.ibatis.statement.base.core.parse.*;
import com.github.ibatis.statement.register.DefaultStatementAutoRegister;
import com.github.ibatis.statement.register.StatementAutoRegister;
import com.github.ibatis.statement.register.schema.DefaultTableSchemaQueryRegister;
import com.github.ibatis.statement.register.schema.TableSchemaQueryRegister;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.jdbc.ScriptRunner;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import java.io.Closeable;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.Arrays;

/**
 * @Author: X1993
 * @Date: 2020/9/8
 */
public class MybatisEnvironment implements Closeable{

    /**
     * 读取配置文件初始化SqlSessionFactory
     */
    final SqlSessionFactory sqlSessionFactory;

    private final SqlSession sqlSession;

    public SqlSession getSqlSession() {
        return sqlSession;
    }

    public MybatisEnvironment(DataSourceEnvironment dataSourceEnvironment) throws IOException
    {
        sqlSessionFactory = new SqlSessionFactoryBuilder()
                .build(Resources.getResourceAsStream("SqlMapConfig.xml") , dataSourceEnvironment.name());

        this.sqlSession = sqlSessionFactory.openSession();
    }

    /**
     * 执行
     * @param schemaReader
     */
    public void initTableSchema(Reader schemaReader)
    {
        ScriptRunner scriptRunner = new ScriptRunner(sqlSession.getConnection());
        scriptRunner.setAutoCommit(true);
        scriptRunner.setStopOnError(true);
        scriptRunner.runScript(schemaReader);
    }

    public void initTableSchema(String schemaSql)
    {
        initTableSchema(new StringReader(schemaSql));
    }

    public void registerMappedStatementsForMappers(Class ... mapperClasses)
    {
        //不同数据库需要使用不同的TableMateDataQueryRegister实现
        TableSchemaQueryRegister tableSchemaQueryRegister = new DefaultTableSchemaQueryRegister();

        StatementAutoRegister register = new DefaultStatementAutoRegister.Builder()
                .setEntityMateDataParser(new DefaultEntityMateDataParser.Builder()
                        .setTableSchemaQueryRegister(tableSchemaQueryRegister)
                        .setPropertyMateDataParser(
                                new DefaultPropertyMateDataParser(Arrays.asList(new TryMappingEveryPropertyMateDataParser()))
                        ).build())
                .addDefaultListeners()
                .build();

        if (mapperClasses != null && mapperClasses.length > 0) {
            for (Class mapperClass : mapperClasses) {
                register.registerDefaultMappedStatement(sqlSession, mapperClass);
            }
        }else {
            register.registerDefaultMappedStatement(sqlSession);
        }
    }

    @Override
    public void close() throws IOException {
        sqlSession.close();
    }
}
