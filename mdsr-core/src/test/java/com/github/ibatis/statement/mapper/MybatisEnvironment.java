package com.github.ibatis.statement.mapper;

import com.github.ibatis.statement.DataSourceEnvironment;
import com.github.ibatis.statement.base.core.parse.*;
import com.github.ibatis.statement.register.DefaultStatementAutoRegister;
import com.github.ibatis.statement.register.StatementAutoRegister;
import com.github.ibatis.statement.register.database.DefaultTableSchemaQueryRegister;
import com.github.ibatis.statement.register.database.H2TableSchemaQuery;
import com.github.ibatis.statement.register.database.MysqlTableSchemaQuery;
import com.github.ibatis.statement.register.database.TableSchemaQueryRegister;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.jdbc.ScriptRunner;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.Arrays;

/**
 * @Author: junjie
 * @Date: 2020/9/8
 */
public class MybatisEnvironment {

    /**
     * 读取配置文件初始化SqlSessionFactory
     */
    final SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder()
            .build(Resources.getResourceAsStream("SqlMapConfig.xml") ,DataSourceEnvironment.H2.name());

    public static MybatisEnvironment ENVIRONMENT;

    static {
        try {
            ENVIRONMENT = new MybatisEnvironment();
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    private final SqlSession sqlSession;

    public SqlSession getSqlSession() {
        return sqlSession;
    }

    private MybatisEnvironment() throws IOException {
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
        tableSchemaQueryRegister.register(new MysqlTableSchemaQuery() ,new H2TableSchemaQuery());

        StatementAutoRegister register = new DefaultStatementAutoRegister.Builder()
                .setEntityMateDataParser(new DefaultEntityMateDataParser.Builder()
                        .setTableSchemaQueryRegister(tableSchemaQueryRegister)
                        .setPropertyMateDataParser(
                                new DefaultPropertyMateDataParser(Arrays.asList(new TryMappingEveryPropertyMateDataParser()))
                        ).build())
                .addDefaultMappedStatementFactories()
                .build();

        if (mapperClasses != null && mapperClasses.length > 0) {
            for (Class mapperClass : mapperClasses) {
                register.registerDefaultMappedStatement(sqlSession, mapperClass);
            }
        }else {
            register.registerDefaultMappedStatement(sqlSession);
        }
    }

}
