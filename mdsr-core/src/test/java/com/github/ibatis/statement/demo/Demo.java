package com.github.ibatis.statement.demo;

import com.github.ibatis.statement.DataSourceEnvironment;
import com.github.ibatis.statement.base.condition.ColumnConditionParser;
import com.github.ibatis.statement.base.condition.SpecificColumnConditionParser;
import com.github.ibatis.statement.base.core.parse.*;
import com.github.ibatis.statement.base.dv.ColumnValueParser;
import com.github.ibatis.statement.base.dv.SpecificColumnValueParser;
import com.github.ibatis.statement.mapper.param.ConditionParams;
import com.github.ibatis.statement.mapper.param.ConditionRule;
import com.github.ibatis.statement.mapper.param.DynamicParams;
import com.github.ibatis.statement.mapper.param.OrderRule;
import com.github.ibatis.statement.register.DefaultStatementAutoRegister;
import com.github.ibatis.statement.register.StatementAutoRegister;
import com.github.ibatis.statement.register.database.DefaultTableSchemaQueryRegister;
import com.github.ibatis.statement.register.database.H2TableSchemaQuery;
import com.github.ibatis.statement.register.database.MysqlTableSchemaQuery;
import com.github.ibatis.statement.register.database.TableSchemaQueryRegister;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.jdbc.ScriptRunner;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.Test;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * @author X1993
 * @date 2020/9/9
 */
public class Demo {

    private static SqlSession sqlSession;

    private final UserMapper userMapper;

    private final CustomUserMapper customUserMapper;

    static {
        try {
            sqlSession = initSqlSessionFactory(DataSourceEnvironment.H2.name()).openSession();
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    public Demo() {
        userMapper = sqlSession.getConfiguration().getMapperRegistry().getMapper(UserMapper.class ,sqlSession);
        customUserMapper = sqlSession.getConfiguration().getMapperRegistry().getMapper(CustomUserMapper.class ,sqlSession);
    }

    public static SqlSessionFactory initSqlSessionFactory(String environment) throws IOException
    {
        SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder()
                .build(Resources.getResourceAsStream("demo/SqlMapConfig.xml") ,environment);
        SqlSession sqlSession = sqlSessionFactory.openSession();

        ScriptRunner scriptRunner = new ScriptRunner(sqlSession.getConnection());
        scriptRunner.setAutoCommit(true);
        scriptRunner.setStopOnError(true);
        scriptRunner.runScript(Resources.getResourceAsReader("demo/schema.sql"));

        //不同数据库需要使用不同的MysqlTableSchemaQuery实现
        TableSchemaQueryRegister tableSchemaQueryRegister = new DefaultTableSchemaQueryRegister();
        tableSchemaQueryRegister.register(new MysqlTableSchemaQuery() ,new H2TableSchemaQuery());

        //列名为`update_time`的列在执行新增和修改指令时，如果没有指定值，使用默认值now()
        ColumnValueParser updateTimeColumnValueParser = new SpecificColumnValueParser(
                columnMateData -> "update_time".equals(columnMateData.getColumnName()) ,
                new SqlCommandType[]{SqlCommandType.UPDATE ,SqlCommandType.INSERT} ,"now()" ,false);

        //列名为`create_time`的列在执行新增指令时，如果没有指定值，使用默认值now()
        ColumnValueParser createTimeColumnValueParser = new SpecificColumnValueParser(columnMateData ->
                "create_time".equals(columnMateData.getColumnName()) ,
                new SqlCommandType[]{SqlCommandType.INSERT} ,"now()" ,false);

        //执行修改指令时，添加默认查询条件 create_time between '2020-08-11 00:00:00' AND '2020-08-12 00:00:00'
        ColumnConditionParser updateColumnConditionParser = new SpecificColumnConditionParser(
                columnMateData -> "create_time".equals(columnMateData.getColumnName()) ,
                new SqlCommandType[]{SqlCommandType.UPDATE}, ConditionRule.BETWEEN, "'2020-08-11 00:00:00' AND '2020-08-12 00:00:00'");

        //执行查询指令时，添加默认查询条件 create_time > '2020-08-12 00:00:00'
        ColumnConditionParser selectColumnConditionParser = new SpecificColumnConditionParser(
                columnMateData -> "create_time".equals(columnMateData.getColumnName()) ,
                new SqlCommandType[]{SqlCommandType.SELECT}, ConditionRule.GT, "2020-08-12 00:00:00");

        //执行删除指令时，添加默认查询条件 create_time < '2020-08-12 00:00:00'
        ColumnConditionParser deleteColumnConditionParser = new SpecificColumnConditionParser(
                columnMateData -> "create_time".equals(columnMateData.getColumnName()) ,
                new SqlCommandType[]{SqlCommandType.DELETE}, ConditionRule.LT, "2020-08-11 00:00:00");

        StatementAutoRegister register = new DefaultStatementAutoRegister.Builder()
                .setEntityMateDataParser(
                        new DefaultEntityMateDataParser.Builder()
//                        .setTableSchemaResolutionStrategy(TableSchemaResolutionStrategy.ENTITY)
                                .setTableSchemaQueryRegister(tableSchemaQueryRegister)
//                        .setTableSourceParser(new DefaultTableSourceParser(
//                            Arrays.asList(
//                                entityClass -> Optional.of(new TableSourceParser.Source(
//                                    "act_" + StringUtils.camelCaseToUnderscore(entityClass.getSimpleName())
//                                ))
//                            )
//                        ))
                                .setPropertyMateDataParser(new DefaultPropertyMateDataParser(
                                        Arrays.asList(
                                                new TryMappingEveryPropertyMateDataParser()
                                        )
                                ))
//                        .setLogicalColumnMateDataParser(new DefaultLogicalColumnMateDataParser(
//                            //列名为removed的列,默认为逻辑列
//                            Arrays.asList(
//                                new SpecificLogicalColumnMateDataParser(
//                                "removed" ,"1" ,"0")
//                            )
//                        ))
//                        .setColumnValueParser(new DefaultColumnValueParser(
//                            Arrays.asList(
//                                updateTimeColumnValueParser ,
//                                createTimeColumnValueParser
//                            )
//                        ))
//                        .setColumnConditionParser(new DefaultColumnConditionParser(
//                            Arrays.asList(
//                                    updateColumnConditionParser ,
//                                    selectColumnConditionParser,
//                                    deleteColumnConditionParser
//                            )
//                        ))
                                .build())
                .addDefaultMappedStatementFactories()
                .addMappedStatementFactory(new SelectMaxIdMappedStatementFactory())
                .addDefaultListeners()
                .build();

        register.registerDefaultMappedStatement(sqlSession);

        return sqlSessionFactory;
    }

    @Test
    public void insert(){
        User user = new User();
        user.setId(11);
        user.setName("张三");
        user.setAddress("杭州");
        userMapper.insert(user);
    }

    @Test
    public void insertSelective(){
        User user = new User();
        user.setId(12);
        user.setName("张三");
//        user.setAddress("杭州");
        userMapper.insertSelective(user);
    }

    @Test
    public void insertBatch(){
        User user = new User();
        user.setId(13);
        user.setName("张三");
        user.setAddress("杭州");

        User user2 = new User();
        user.setId(14);
        user.setName("李四");
        userMapper.insertBatch(Arrays.asList(user ,user2));
    }

    @Test
    public void selectAll(){
        userMapper.selectAll();
    }

    @Test
    public void selectExistPrimaryKeys(){
        userMapper.getExistPrimaryKeys(13 ,14);
    }

    @Test
    public void selectSelective(){
        User user = new User();
        user.setName("ha");
        userMapper.selectSelective(user);
    }

    @Test
    public void countSelective(){
        User user = new User();
        user.setName("ha");
        userMapper.totalSelective(user);
    }

    @Test
    public void selectMaxPrimaryKey(){
        userMapper.selectMaxPrimaryKey();
    }

    @Test
    public void selectByPrimaryKey(){
        userMapper.selectByPrimaryKey(2);
    }

    @Test
    public void updateByPrimaryKey(){
        User user = new User();
        user.setId(1);
        user.setName("张三");
        userMapper.insert(user);
        user.setAddress(null);
        userMapper.updateByPrimaryKey(user);
    }

    @Test
    public void updateByPrimaryKeySelective(){
        User user = new User();
        user.setId(2);
        user.setName("张三");
        user.setAddress("杭州");
        userMapper.insert(user);
        user.setAddress("嘉兴");
        userMapper.updateByPrimaryKeySelective(user);
    }

    @Test
    public void updateBatch(){
        User user1 = new User();
        user1.setId(3);
        user1.setName("张三");
        user1.setAddress("杭州");
        User user2 = new User();
        user2.setId(4);
        user2.setName("李四");
        user2.setAddress("宁波");
        List<User> users = Arrays.asList(user1, user2);
        userMapper.insertBatch(users);
        user1.setAddress(null);
        //H2不支持批量更新
        if (DataSourceEnvironment.MYSQL.name().equals(sqlSession.getConfiguration().getEnvironment().getId())) {
            userMapper.updateBatch(users);
        }
    }

    @Test
    public void deleteByPrimaryKey(){
        User user = new User();
        user.setId(5);
        user.setName("张三");
        user.setAddress("杭州");
        user.setCreateTime(new Date());
        userMapper.insert(user);
        userMapper.deleteByPrimaryKey(user.getId());
    }

    @Test
    public void updateBatchSameValue(){
        User user1 = new User();
        user1.setId(6);
        user1.setName("张三");
        user1.setAddress("杭州");
        User user2 = new User();
        user2.setId(7);
        user2.setName("李四");
        user2.setAddress("宁波");
        List<User> users = Arrays.asList(user1, user2);
        userMapper.insertBatch(users);
        User updateUser = new User();
        updateUser.setAddress("衢州");
        userMapper.updateBatchSameValue(Arrays.asList(user1.getId() ,user2.getId()) ,updateUser);
    }

    @Test
    public void selectByDynamicParams(){
        userMapper.selectByDynamicParams(new DynamicParams()
                .addSelectElements("id" ,"`name`")
                .addSelectColumns("create_time" ,"`address`")
                .where(new ConditionParams()
                        .between("create_time", "2020-08-11", new Date())
                        .likeLeft("`name`", "张"))
                .groupBy("`address`", "name")
                .having(new ConditionParams().notNull("create_time"))
                .asc("name","`address`")
                .addOrderRule(OrderRule.Rule.ASC ,"`create_time`","name")
                .addOrderRule(new OrderRule[]{new OrderRule("name" , OrderRule.Rule.DESC)})
                .page0(0, 10));
    }

    @Test
    public void selectIn(){
        userMapper.selectByWhereConditions(new ConditionParams().in("id" ,new ArrayList()));
    }

    @Test
    public void selectByPrimaryKey2(){
        customUserMapper.selectByPrimaryKey(2);
    }

    @Test
    public void selectSelective2(){
        User user = new User();
        user.setName("ha");
        customUserMapper.selectSelective(user ,true);
    }

    @Test
    public void countByDynamicParams(){
        System.out.println(customUserMapper.countByDynamicParams(new DynamicParams()));
    }

    @Test
    public void methodNameParseTest(){
        userMapper.selectById(12);
        userMapper.selectByIdEqAndAddressNotNullOrCreateTimeBetweenOrderByNameCreateTimeDesc(12 ,new Date() ,new Date());
        userMapper.selectByNameInAndCreateTimeGt(Arrays.asList("name1" ,"name2") ,new Date());
        userMapper.selectByNameIn(Arrays.asList("name1" ,"name2"));
        userMapper.selectByIdIn(1 ,3);
    }

    @Test
    public void selectByNameAndAddressIn(){
        userMapper.selectByNameAndAddressIn(null ,"beijing");
        userMapper.selectByNameAndAddressIn(null ,null);
        userMapper.selectByNameAndAddressIn("tony" ,"hangzhou" ,"beijing");
    }

}
