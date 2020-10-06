# 文档

## 运行环境:
JDK 8+, Maven, Mysql/MariaDB

## 快速开始
### 配置参数
>   参考单元测试(package:com.github.ibatis.statement.demo)
-   maven依赖
```xml
    <dependencies>
        <dependency>
            <groupId>com.github.X1993</groupId>
            <artifactId>mdsr-core</artifactId>
            <version>2.2.0-SNAPSHOT</version>
        </dependency>

        <dependency>
            <groupId>org.mariadb.jdbc</groupId>
            <artifactId>mariadb-java-client</artifactId>
            <version>2.2.5</version>
        </dependency>
    </dependencies>
```
-   user表Schema脚本 
```sql
    DROP TABLE IF EXISTS `user`;
    CREATE TABLE `user` (
      `id` int(11) NOT NULL AUTO_INCREMENT,
      `name` varchar(6) COLLATE utf8_bin DEFAULT NULL COMMENT '姓名',
      `create_time` datetime DEFAULT NULL COMMENT '创建时间',
      `update_time` datetime DEFAULT NULL COMMENT '更新时间',
      `address` varchar(50) COLLATE utf8_bin DEFAULT NULL COMMENT '地址',
      `note` varchar(100) COLLATE utf8_bin DEFAULT NULL COMMENT '备注',
      `version` int(255) DEFAULT NULL COMMENT '版本号',
      `removed` bit(1) DEFAULT b'0' COMMENT '逻辑列，是否已删除 1：已删除',
      PRIMARY KEY (`id`)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;
```
-   Mybatis配置文件SqlMapConfig
```xml
<?xml version="1.0" encoding="UTF-8" ?>
    <!DOCTYPE configuration
            PUBLIC "-//mybatis.org//DTD Config 3.0//EN"
            "http://mybatis.org/dtd/mybatis-3-config.dtd">
    <configuration>
        <environments default="mysql">
            <environment id="mysql">
                <!-- 使用jdbc事务管理 -->
                <transactionManager type="JDBC" />
                <!-- 数据库连接池 -->
                <dataSource type="POOLED">
                    <property name="driver" value="org.mariadb.jdbc.Driver" />
                    <property name="url" value="jdbc:mariadb://localhost:3306/test?allowMultiQueries=true" />
                    <property name="username" value="root" />
                    <property name="password" value="123456" />
                </dataSource>
            </environment>
        </environments>
    
        <mappers>
            <package name="com.github.ibatis.statement.demo"/>
        </mappers>
    </configuration>
```

### 项目启动
-   初始化SqlSessionFactory 
[mybatis入门](https://mybatis.org/mybatis-3/zh/getting-started.html)
```java
    package com.github.ibatis.statement.demo;
    
    import com.github.ibatis.statement.base.core.parse.*;
    import com.github.ibatis.statement.mapper.param.ConditionParams;
    import com.github.ibatis.statement.mapper.param.DynamicParams;
    import com.github.ibatis.statement.register.DefaultStatementAutoRegister;
    import com.github.ibatis.statement.register.StatementAutoRegister;
    import com.github.ibatis.statement.register.database.DefaultTableSchemaQueryRegister;
    import com.github.ibatis.statement.register.database.MysqlTableSchemaQuery;
    import com.github.ibatis.statement.register.database.TableSchemaQueryRegister;
    import org.apache.ibatis.io.Resources;
    import org.apache.ibatis.jdbc.ScriptRunner;
    import org.apache.ibatis.session.SqlSession;
    import org.apache.ibatis.session.SqlSessionFactory;
    import org.apache.ibatis.session.SqlSessionFactoryBuilder;
    import org.junit.Test;
    import java.io.IOException;
    import java.util.Arrays;
    import java.util.Date;
    import java.util.List;
    
    public class Demo{
        
        private UserMapper userMapper;
    
        public Demo() throws IOException {
            SqlSessionFactory sqlSessionFactory = initSqlSessionFactory();
            SqlSession sqlSession = sqlSessionFactory.openSession();
            userMapper = sqlSessionFactory.getConfiguration().getMapper(UserMapper.class ,sqlSession);
        }
        
        /*
         初始化SqlSessionFactory
         */
       public static SqlSessionFactory initSqlSessionFactory() throws IOException
       {
           SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder()
                   .build(Resources.getResourceAsStream("demo/SqlMapConfig.xml"));
           SqlSession sqlSession = sqlSessionFactory.openSession();
   
           ScriptRunner scriptRunner = new ScriptRunner(sqlSession.getConnection());
           scriptRunner.setAutoCommit(true);
           scriptRunner.setStopOnError(true);
           scriptRunner.runScript(Resources.getResourceAsReader("demo/schema.sql"));
   
           //不同数据库需要使用不同的MysqlTableSchemaQuery实现
           MysqlTableSchemaQuery mysqlTableSchemaQuery = new MysqlTableSchemaQuery();
           TableSchemaQueryRegister tableSchemaQueryRegister = new DefaultTableSchemaQueryRegister();
           tableSchemaQueryRegister.register(mysqlTableSchemaQuery);
   
           StatementAutoRegister register = new DefaultStatementAutoRegister.Builder()
                   .setEntityMateDataParser(
                           new DefaultEntityMateDataParser.Builder()
                           .setTableSchemaQueryRegister(tableSchemaQueryRegister)
                           .build())
                   .addDefaultMappedStatementFactories()
                   .build();
   
           register.registerDefaultMappedStatement(sqlSession);
           sqlSession.close();
   
           return sqlSessionFactory;
       }
    }
```
-   User实体类
```java
    package com.github.ibatis.statement.demo;
    import com.github.ibatis.statement.base.core.Column;
    import com.github.ibatis.statement.base.core.Entity;
    import java.util.Date;

    @Entity(tableName = "user")//申明实体映射的表
    public class User {
        @Column(value = "id")//申明字段映射的列
        private Integer id;
        @Column("name")
        private String name;
        @Column("create_time")
        private Date createTime;
        @Column("update_time")
        private Date updateTime;
        @Column("address")
        private String address;
        @Column("note")
        private String note;
        @Column("version")
        private int version;
        // ... (ellipsis get/set methods)
    }
```
```java
    package com.github.ibatis.statement.base.core;
    
    import com.github.ibatis.statement.base.core.matedata.PropertyMateData;
    import com.github.ibatis.statement.base.core.parse.DefaultPropertyMateDataParser;
    import com.github.ibatis.statement.register.MappedStatementFactory;
    import org.apache.ibatis.mapping.SqlCommandType;
    import org.apache.ibatis.type.TypeHandler;
    import org.apache.ibatis.type.UnknownTypeHandler;
    import java.lang.annotation.*;
    
    /**
     * 实体类属性映射的列信息
     * @Author: junjie
     * @Date: 2020/2/21
     */
    @Target({ElementType.FIELD})
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    public @interface Column {
    
        /**
         * 列名，缺省时默认通过{@link DefaultPropertyMateDataParser#getDefaultNameFunction()}获取
         * @see PropertyMateData#mappingColumnName
         * @return
         */
        String value() default "";
    
        /**
         * 列匹配策略
         * @return
         */
        MappingStrategy mappingStrategy() default MappingStrategy.AUTO;
    
        /**
         * 如果不包含{@link SqlCommandType#INSERT},则自动注入的插入语句会过滤该列的插入
         * 如果不包含{@link SqlCommandType#UPDATE}, ...修改...过滤该列的修改
         * 如果不包含{@link SqlCommandType#SELECT}, ...查询...过滤该列的查询
         * （具体实现逻辑由每个{@link MappedStatementFactory}自定义，也可不实现）
         * @see PropertyMateData#commandTypeMappings
         * @return
         */
        SqlCommandType[] commandTypeMappings() default {SqlCommandType.INSERT ,SqlCommandType.UPDATE ,SqlCommandType.SELECT};
    
        /**
         * 类型转换器类型
         * @see PropertyMateData#typeHandlerClass
         * @return
         */
        Class<? extends TypeHandler<?>> typeHandler() default UnknownTypeHandler.class;
    
    }
```
-   UserMapper接口
```java
    package com.github.ibatis.statement.demo;
    import com.github.ibatis.statement.mapper.KeyTableMapper;
    
    public interface UserMapper extends KeyTableMapper<Integer ,User> {
    
    }
```

## 功能介绍
### 1.自动注册特定Mapper方法
#### 1.1. 演示效果
-   insert
```java
    package com.github.ibatis.statement.demo;
    import com.github.ibatis.statement.demo.User;
    import com.github.ibatis.statement.demo.UserMapper;
    import org.junit.Test;
    
    public class Demo{
        
        @Test
        public void insert(){
            User user = new User();
            user.setName("张三");
            user.setAddress("杭州");
            user.setCreateTime(new Date());
            userMapper.insert(user);
        }
    }
```
    MyBatis-Plus will execute the following SQL
```sql
  INSERT INTO `user` (`note`,`update_time`,`address`,`create_time`,`name`,`id`,`version`) VALUES (NULL ,NULL ,'杭州','2020-09-09 16:10:22.129','张三',NULL ,0); 
```
-   select
```java
    package com.github.ibatis.statement.demo;
    import com.github.ibatis.statement.demo.User;
    import com.github.ibatis.statement.demo.UserMapper;
    import org.junit.Test;
   
    public class Demo{
        
        @Test
        public void selectByPrimaryKey(){
            userMapper.selectByPrimaryKey(2);
        }
    }
``` 
    MyBatis-Plus will execute the following SQL
```sql
  SELECT `note`,`update_time`,`address`,`create_time`,`name`,`id`,`version` FROM `user` WHERE `id` = 2;
```
-   update
```java
    package com.github.ibatis.statement.demo;
    import com.github.ibatis.statement.demo.User;
    import com.github.ibatis.statement.demo.UserMapper;
    import org.junit.Test;
   
    public class Demo{
        
        @Test
        public void updateByPrimaryKey(){
            User user = new User();
            user.setId(10);
            user.setName("张三");
            user.setAddress("杭州");
            user.setCreateTime(new Date());
            userMapper.insert(user);
            user.setAddress("嘉兴");
            userMapper.updateByPrimaryKey(user);
        }
    }
```
    MyBatis-Plus will execute the following SQL
```sql
  UPDATE `user` SET `note` = NULL , `update_time` = NULL , `address` = '嘉兴', `create_time` = '2020-09-09 22:26:33.359', `name` = '张三', `version` = 0 WHERE `id` = 10;
```
-   delete
```java
    package com.github.ibatis.statement.demo;
    import com.github.ibatis.statement.demo.User;
    import com.github.ibatis.statement.demo.UserMapper;
    import org.junit.Test;
   
    public class Demo{
        
        @Test
        public void deleteByPrimaryKey(){
            User user = new User();
            user.setId(11);
            user.setName("张三");
            user.setAddress("杭州");
            user.setCreateTime(new Date());
            userMapper.insert(user);
            userMapper.deleteByPrimaryKey(user.getId());
        }
    }
```
    MyBatis-Plus will execute the following SQL
```sql
  DELETE FROM `user` WHERE `id` = ? AND 1 = 1;
```
>   更多方法请参考接口*package com.github.ibatis.statement.mapper.KeyTableMapper*及其父接口方法注释

####  1.2.自定义sql覆盖自动注册的方法
-   通过XML或注解申明自定义sql
```java
    package com.github.ibatis.statement.demo;
    
    import com.github.ibatis.statement.mapper.KeyTableMapper;
    import org.apache.ibatis.annotations.Select;
    
    /**
     * @author junjie
     * @date 2020/9/9
     */
    public interface UserMapper extends KeyTableMapper<Integer ,User> {
    
        @Override
        @Select("select * from user where id = #{param1} and removed = 0")
        User selectByPrimaryKey(Integer key);
    
    }
```
```java
    package com.github.ibatis.statement.demo;
    import com.github.ibatis.statement.demo.User;
    import com.github.ibatis.statement.demo.UserMapper;
    import org.junit.Test;
   
    public class Demo{
        
        @Test
        public void selectByPrimaryKey(){
            userMapper.selectByPrimaryKey(2);
        }
    }
``` 
    MyBatis-Plus will execute the following SQL
```sql
  SELECT `note`,`update_time`,`address`,`create_time`,`name`,`id`,`version` FROM `user` WHERE `id` = 2 and removed = 0;
```  

####  1.3.自由定义需要注册的方法
-   方法签名与*package com.github.ibatis.statement.mapper.KeyTableMapper*及其父接口方法兼容（不包含默认方法）,
    如果不想注册所有方法，可以自由选择注册的方法
>   方法签名：返回值（兼容泛型实际声明的类型）+方法名+方法参数列表（兼容泛型实际声明的类型）
```java
    package com.github.ibatis.statement.demo;
    
    import com.github.ibatis.statement.mapper.EntityType;
    import com.github.ibatis.statement.mapper.KeyParameterType;
    import java.util.Collection;
    
    /**
     * @Author: junjie
     * @Date: 2020/9/14
     */
    public interface CustomUserMapper extends KeyParameterType<Integer> ,EntityType<User>{
    
        /**
         * 与{@link com.github.ibatis.statement.mapper.KeyTableMapper#selectByPrimaryKey(Object)}方法签名兼容
         * @param key
         * @return
         */
        User selectByPrimaryKey(Integer key);
    
        /**
         * 与{@link com.github.ibatis.statement.mapper.SelectMapper#selectSelective(Object)}方法签名兼容
         * @param condition
         * @param logicalExist
         * @return
         */
        Collection<? extends User> selectSelective(User condition , boolean logicalExist);
    
    }
```

### 2.实体类自定义表名解析
-   将User类上的*@com.github.ibatis.statement.base.core.Entity*注解注释掉
```java
    package com.github.ibatis.statement.demo;
    import com.github.ibatis.statement.base.core.Column;
    import com.github.ibatis.statement.base.core.Entity;
    import java.util.Date;
    

    //@Entity(tableName = "user")//申明实体映射的表
    public class User {
        
        // columns
        
        // ... (ellipsis get/set methods)
    }
```
#### 2.1.表名默认Entity类名驼峰转下划线

    类名            |      表名 
    -----           |      -----
    User            |      user
    Location        |      location
    MerchantInfo    |      merchant_info    

#### 2.2.自定义解析规则 
-   实现*com.github.ibatis.statement.base.core.parse.TableSourceParser*自定义全局解析规则
```java
    package com.github.ibatis.statement.base.core.parse;
    
    import com.github.ibatis.statement.base.core.TableSchemaResolutionStrategy;
    import com.github.ibatis.statement.util.Sorter;
    import java.util.Optional;
    
    /**
     * 解析实体类映射的表来源信息
     * @author junjie
     * @date 2020/2/23
     */
    public interface TableSourceParser extends Sorter{
    
        /**
         * 解析
         * @param entityClass
         * @return
         */
        Optional<Source> parse(Class<?> entityClass);
    
        /**
         * 表来源
         * @author junjie
         * @date 2020/9/26
         */
        class Source {
    
            /**
             * 表名
             */
            private String tableName;
    
            /**
             * 表结构解析策略
             */
            private TableSchemaResolutionStrategy tableSchemaResolutionStrategy;
    
            public Source(String tableName, TableSchemaResolutionStrategy tableSchemaResolutionStrategy) {
                this.tableName = tableName;
                this.tableSchemaResolutionStrategy = tableSchemaResolutionStrategy;
            }
    
            public Source(String tableName) {
                this.tableName = tableName;
            }
    
            public Source() {
            }
    
            public String getTableName() {
                return tableName;
            }
    
            public void setTableName(String tableName) {
                this.tableName = tableName;
            }
    
            public TableSchemaResolutionStrategy getTableSchemaResolutionStrategy() {
                return tableSchemaResolutionStrategy;
            }
    
            public void setTableSchemaResolutionStrategy(TableSchemaResolutionStrategy tableSchemaResolutionStrategy) {
                this.tableSchemaResolutionStrategy = tableSchemaResolutionStrategy;
            }
    
        }
    
    }

```
-   注册实现类
```java
    package com.github.ibatis.statement.demo;
    
    import com.github.ibatis.statement.base.core.parse.*;
    import com.github.ibatis.statement.mapper.param.ConditionParams;
    import com.github.ibatis.statement.mapper.param.DynamicParams;
    import com.github.ibatis.statement.register.DefaultStatementAutoRegister;
    import com.github.ibatis.statement.register.StatementAutoRegister;
    import com.github.ibatis.statement.register.database.DefaultTableSchemaQueryRegister;
    import com.github.ibatis.statement.register.database.MysqlTableSchemaQuery;
    import com.github.ibatis.statement.register.database.TableSchemaQueryRegister;
    import com.github.ibatis.statement.util.StringUtils;
    import org.apache.ibatis.io.Resources;
    import org.apache.ibatis.jdbc.ScriptRunner;
    import org.apache.ibatis.session.SqlSession;
    import org.apache.ibatis.session.SqlSessionFactory;
    import org.apache.ibatis.session.SqlSessionFactoryBuilder;
    import org.junit.Test;
    import java.io.IOException;
    import java.util.Arrays;
    import java.util.Date;
    import java.util.List;
    import java.util.Optional;
    
    public class Demo{
        /*
         初始化SqlSessionFactory
         */
        public SqlSessionFactory init() throws IOException 
        {   
            // ...
            
               StatementAutoRegister register = new DefaultStatementAutoRegister.Builder()
                            .setEntityMateDataParser(new DefaultEntityMateDataParser.Builder()
                                    .setTableSchemaQueryRegister(tableSchemaQueryRegister)
                                    .setTableSourceParser(new DefaultTableSourceParser(
                                    Arrays.asList(
                                            entityClass -> Optional.of(new TableSourceParser.Source(
                                                "act_" + StringUtils.camelCaseToUnderscore(entityClass.getSimpleName())
                                            ))
                                        )
                                    ))
                                    .build())
                            .addDefaultMappedStatementFactories()
                            .build();
            
            // ...
        }
    }
```
-   映射结果


    类名              |           表名 
    ----                         -----
    User              |         act_user
    Location          |         act_location
    MerchantInfo      |         act_merchant_info
    
#### 2.3.优先级    
-   @Entity指定 > 自定义TableNameParser实现（多个实现通过order()方法确定优先级）> 默认驼峰转下划线

### 3.实体类字段映射的列解析
-   将User类属性上的*@com.github.ibatis.statement.base.core.Column*注解去掉
```java
    package com.github.ibatis.statement.demo;
    import com.github.ibatis.statement.base.core.Column;
    import com.github.ibatis.statement.base.core.Entity;
    import java.util.Date;

    public class User {
        
        private Integer id;
        private String name;
        private Date createTime;
        private Date updateTime;
        private String address;
        private String note;
        private int version;
        
        // ... (ellipsis get/set methods)
    }
```
#### 3.1.自定义解析规则 
-   实现*com.github.ibatis.statement.base.core.parse.PropertyMateDataParser*自定义全局解析规则
```java
    package com.github.ibatis.statement.base.core.parse;
    import com.github.ibatis.statement.base.core.matedata.PropertyMateData;
    import com.github.ibatis.statement.util.Sorter;
    import java.lang.reflect.Field;
    import java.util.Optional;
    
    /**
     * 解析类属性对应列名
     * @Author: junjie
     * @Date: 2020/2/21
     */
    public interface PropertyMateDataParser extends Sorter {
    
        /**
         * 解析
         * @param field 字段
         * @param entityClass 实体类
         * @return
         */
        Optional<PropertyMateData> parse(Field field , Class<?> entityClass);
    
    }
```
-   注册实现类
```java
    package com.github.ibatis.statement.demo;
    import com.github.ibatis.statement.base.condition.DefaultColumnConditionParser;
    import com.github.ibatis.statement.base.core.parse.DefaultMapperEntityParser;
    import com.github.ibatis.statement.base.core.parse.DefaultPropertyMateDataParser;
    import com.github.ibatis.statement.base.core.parse.DefaultTableSourceParser;
    import com.github.ibatis.statement.base.core.parse.TryMappingEveryPropertyMateDataParser;
    import com.github.ibatis.statement.base.dv.DefaultColumnValueParser;
    import com.github.ibatis.statement.base.logical.DefaultLogicalColumnMateDataParser;
    import com.github.ibatis.statement.register.DefaultStatementAutoRegister;
    import com.github.ibatis.statement.register.StatementAutoRegister;
    // ... 
    
    public class Demo{
        
        /*
         初始化SqlSessionFactory
         */
        public SqlSessionFactory init() throws IOException 
        {   
            // ...
            
            StatementAutoRegister register = new DefaultStatementAutoRegister.Builder()
                            .setEntityMateDataParser(
                                    new DefaultEntityMateDataParser.Builder()
                                    .setTableSchemaQueryRegister(tableSchemaQueryRegister)
                                    .setPropertyMateDataParser(new DefaultPropertyMateDataParser(
                                            Arrays.asList(
                                                new TryMappingEveryPropertyMateDataParser()
                                            )
                                        )
                                    )
                                    .build())
                            .addDefaultMappedStatementFactories()
                            .build();
            
            // ...
        }
    }
```
```java
    package com.github.ibatis.statement.base.core.parse;
    import com.github.ibatis.statement.base.core.matedata.PropertyMateData;
    import com.github.ibatis.statement.util.StringUtils;
    import java.lang.reflect.Field;
    import java.util.Optional;
    
    public class TryMappingEveryPropertyMateDataParser implements PropertyMateDataParser {
    
        /**
         * 属性名 -> 列名映射
         */
        private PropertyToColumnNameFunction defaultNameFunction = (propertyName) ->
                StringUtils.camelCaseToUnderscore(propertyName);
    
        @Override
        public int order() {
            return Integer.MAX_VALUE;
        }
    
        @Override
        public Optional<PropertyMateData> parse(Field field, Class<?> entityClass)
        {
            return Optional.of(new PropertyMateData(defaultNameFunction.apply(field.getName()) ,field));
        }
    
        public PropertyToColumnNameFunction getDefaultNameFunction() {
            return defaultNameFunction;
        }
    
        public void setDefaultNameFunction(PropertyToColumnNameFunction defaultNameFunction) {
            this.defaultNameFunction = defaultNameFunction;
        }
    }
```
-   映射结果


    字段名            |         列名 
    ------           |         ------
    id               |         id
    address          |         address
    createTime       |         create_time
    updateTime       |         update_time

### 4.逻辑列
#### 4.1.在实体类上使用注解申明逻辑列
```java
    package com.github.ibatis.statement.demo;
    import com.github.ibatis.statement.base.core.Column;
    import com.github.ibatis.statement.base.core.Entity;
    import com.github.ibatis.statement.base.logical.Logical;
    import java.util.Date;

    @Logical(columnName = "removed" ,existValue = "0" ,notExistValue = "1")//定义逻辑列
    @Entity(tableName = "user")//申明实体映射的表
    public class User {
        @Column(value = "id")//申明字段映射的列
        private Integer id;
        
        //... column
        
        // ... (ellipsis get/set methods)
        
    }
```
#### 4.2.全局配置匹配逻辑列
-   实现*com.github.ibatis.statement.base.logical.SpecificLogicalColumnMateDataParser*自定义全局解析规则
```java
    package com.github.ibatis.statement.base.logical;
    
    import com.github.ibatis.statement.base.core.matedata.EntityMateData;
    import com.github.ibatis.statement.util.Sorter;

    /**
     * 逻辑列元数据解析
     * @Author: junjie
     * @Date: 2020/3/4
     */
    public interface LogicalColumnMateDataParser extends Sorter{
    
        /**
         * 解析
         * @param entityMateData
         */
        void parse(EntityMateData entityMateData);
    
    }
```
-   注册实现类
```java
    package com.github.ibatis.statement.demo;

    import com.github.ibatis.statement.base.core.parse.*;
    import com.github.ibatis.statement.base.logical.DefaultLogicalColumnMateDataParser;
    import com.github.ibatis.statement.base.logical.SpecificLogicalColumnMateDataParser;
    import com.github.ibatis.statement.mapper.param.ConditionParams;
    import com.github.ibatis.statement.mapper.param.DynamicParams;
    import com.github.ibatis.statement.register.DefaultStatementAutoRegister;
    import com.github.ibatis.statement.register.StatementAutoRegister;
    import com.github.ibatis.statement.register.database.DefaultTableSchemaQueryRegister;
    import com.github.ibatis.statement.register.database.MysqlTableSchemaQuery;
    import com.github.ibatis.statement.register.database.TableSchemaQueryRegister;
    import com.github.ibatis.statement.util.StringUtils;
    import org.apache.ibatis.io.Resources;
    import org.apache.ibatis.jdbc.ScriptRunner;
    import org.apache.ibatis.session.SqlSession;
    import org.apache.ibatis.session.SqlSessionFactory;
    import org.apache.ibatis.session.SqlSessionFactoryBuilder;
    import org.junit.Test;
    import java.io.IOException;
    import java.util.Arrays;
    import java.util.Date;
    import java.util.List;
    import java.util.Optional;
    
    public class Demo{
        
        /*
         初始化SqlSessionFactory
         */
        public SqlSessionFactory init() throws IOException 
        {   
            // ...
            
            StatementAutoRegister register = new DefaultStatementAutoRegister.Builder()
                            .setEntityMateDataParser(
                                    new DefaultEntityMateDataParser.Builder()
                                    .setTableSchemaQueryRegister(tableSchemaQueryRegister)
                                    .setLogicalColumnMateDataParser(new DefaultLogicalColumnMateDataParser(
                                        //列名为removed的列,默认为逻辑列
                                        Arrays.asList(
                                            new SpecificLogicalColumnMateDataParser("removed" ,"1" ,"0")
                                        ))
                                    )
                                    .build())
                            .addDefaultMappedStatementFactories()
                            .build();
            
            // ...
        }
    }
```
#### 效果演示
-   insert
```java
    package com.github.ibatis.statement.demo;
    import com.github.ibatis.statement.demo.User;
    import com.github.ibatis.statement.demo.UserMapper;
    import org.junit.Test;
    
    public class Demo{
        
        @Test
        public void insert(){
            User user = new User();
            user.setName("张三");
            user.setAddress("杭州");
            user.setCreateTime(new Date());
            userMapper.insert(user);
        }
    }
```
    MyBatis-Plus will execute the following SQL
```sql
  INSERT INTO `user` (`note`,`update_time`,`address`,`create_time`,`name`,`id`,`version`,`removed`) VALUES (NULL ,NULL ,'杭州','2020-09-09 16:10:22.129','张三',NULL ,0 ,0); 
```
-   select
```java
    package com.github.ibatis.statement.demo;
    import com.github.ibatis.statement.demo.User;
    import com.github.ibatis.statement.demo.UserMapper;
    import org.junit.Test;
   
    public class Demo{
        
        @Test
        public void selectByPrimaryKey(){
            userMapper.selectByPrimaryKey(2);
        }
    }
``` 
    MyBatis-Plus will execute the following SQL
```sql
  SELECT `note`,`update_time`,`address`,`create_time`,`name`,`id`,`version` FROM `user` WHERE `id` = 2 and removed = 0;
```
-   update
```java
    package com.github.ibatis.statement.demo;
    import com.github.ibatis.statement.demo.User;
    import com.github.ibatis.statement.demo.UserMapper;
    import org.junit.Test;
   
    public class Demo{
        
        @Test
        public void updateByPrimaryKey(){
            User user = new User();
            user.setId(10);
            user.setName("张三");
            user.setAddress("杭州");
            user.setCreateTime(new Date());
            userMapper.insert(user);
            user.setAddress("嘉兴");
            userMapper.updateByPrimaryKey(user);
        }
    }
```
    MyBatis-Plus will execute the following SQL
```sql
  UPDATE `user` SET `note` = NULL , `update_time` = NULL , `address` = '嘉兴', `create_time` = '2020-09-09 22:26:33.359', `name` = '张三', `version` = 0 WHERE `id` = 10 and removed = 0;
```
-   delete
```java
    package com.github.ibatis.statement.demo;

    import com.github.ibatis.statement.demo.User;
    import com.github.ibatis.statement.demo.UserMapper;
    import org.junit.Test;
   
    public class Demo{
        
        @Test
        public void deleteByPrimaryKey(){
            User user = new User();
            user.setId(11);
            user.setName("张三");
            user.setAddress("杭州");
            user.setCreateTime(new Date());
            userMapper.insert(user);
            userMapper.deleteByPrimaryKey(user.getId());
        }
    }
```
    MyBatis-Plus will execute the following SQL
```sql
  UPDATE `user` SET `removed` = 1 WHERE `id` = 11 AND `removed` = 0; 
```
>   更多方法请参考接口*package com.github.ibatis.statement.mapper.KeyTableMapper*及其父接口方法注释

### 5.复合主键
-   schema.sql
```sql
    DROP TABLE IF EXISTS `entity3`;
    
    CREATE TABLE `entity3` (
      `id1` int(32) ,
      `id2` int(32) ,
      `value` varchar(30) DEFAULT NULL,
      `value2` varchar(30) DEFAULT NULL,
      PRIMARY KEY (`id1` ,`id2`)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8;
```
-   实体类
```java
    public class Entity3 {

        private int id1;

        private int id2;

        private String value;

        private String value2;

        // ... (ellipsis get/set methods)

    }
```
-   Mapper接口，主键类型使用实体类型
```java
    interface Entity3Mapper extends KeyTableMapper<Entity3, Entity3> {
        
    }
```

### 6.默认赋值 
>   仅支持新增/修改指令
#### 6.1 在实体类属性上申明默认赋值规则
```java
    @Logical(columnName = "removed" ,existValue = "0" ,notExistValue = "1")//定义逻辑列
    public class User {
        
        // ... columns    
    
        //在执行新增和修改指令时，如果没有指定值，使用默认值now()
        @DefaultValue(commandTypes = SqlCommandType.INSERT, value = "now()")
        private Date createTime;
        
        //在执行新增指令时，如果没有指定值，使用默认值now()
        @DefaultValue(commandTypes = {SqlCommandType.INSERT ,SqlCommandType.UPDATE}, value = "now()")
        private Date updateTime;
        
        // ... columns    
        
        // ... (ellipsis get/set methods)
    
    }
```
```java
    /**
     * 添加该注解的属性映射的列每次执行插入/修改操作的时候会自动赋值
     * @Author: junjie
     * @Date: 2020/7/21
     */
    @Target({ElementType.FIELD ,ElementType.TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    @Inherited
    @Repeatable(DefaultValues.class)
    public @interface DefaultValue {
    
        /**
         * 定义执行插入或修改操作时使用默认值
         * @return
         */
        SqlCommandType[] commandTypes() default {SqlCommandType.INSERT ,SqlCommandType.UPDATE};
    
        /**
         * 列名
         * 如果该注解添加在类属性上，则该值默认为属性映射的列名
         */
        String columnName() default "";
    
        /**
         * 默认使用该值作为插入值/修改值，会直接拼接到sql语句上，可以使用支持的数据库函数
         * 注意！如果是字符类型需要加引号
         * @return
         */
        String value();
    
        /**
         * 插入的列如果有自定义插入值/修改值，是否使用{@link DefaultValue#value()}覆盖声明的值
         * @return
         */
        boolean overwriteCustom() default true;
    
    }
```

#### 6.2 全局配置解析默认赋值规则
-   实现*com.github.ibatis.statement.base.dv.ColumnDefaultValue*自定义全局解析规则
```java
    /**
     * 解析使用默认赋值的列
     * @Author: junjie
     * @Date: 2020/7/22
     */
    public interface ColumnValueParser extends Sorter{
    
        /**
         * 解析
         * @param entityMateData
         */
        void parse(EntityMateData entityMateData);
    
    }
```
-   注册实现类
```java

    import com.github.ibatis.statement.base.condition.DefaultColumnConditionParser;
    import com.github.ibatis.statement.base.core.parse.*;
    import com.github.ibatis.statement.base.dv.ColumnValueParser;
    import com.github.ibatis.statement.base.dv.DefaultColumnValueParser;
    import com.github.ibatis.statement.base.dv.SpecificColumnValueParser;
    import com.github.ibatis.statement.base.logical.DefaultLogicalColumnMateDataParser;
    import com.github.ibatis.statement.base.logical.SpecificLogicalColumnMateDataParser;
    import com.github.ibatis.statement.mapper.param.ConditionParams;
    import com.github.ibatis.statement.mapper.param.DynamicParams;
    import com.github.ibatis.statement.register.DefaultStatementAutoRegister;
    import com.github.ibatis.statement.register.StatementAutoRegister;
    import com.github.ibatis.statement.register.database.DefaultTableSchemaQueryRegister;
    import com.github.ibatis.statement.register.database.MysqlTableSchemaQuery;
    import com.github.ibatis.statement.register.database.TableSchemaQueryRegister;
    import com.github.ibatis.statement.util.StringUtils;
    import org.apache.ibatis.io.Resources;
    import org.apache.ibatis.jdbc.ScriptRunner;
    import org.apache.ibatis.mapping.SqlCommandType;
    import org.apache.ibatis.session.SqlSession;
    import org.apache.ibatis.session.SqlSessionFactory;
    import org.apache.ibatis.session.SqlSessionFactoryBuilder;
    import org.junit.Test;
    import java.io.IOException;
    import java.util.Arrays;
    import java.util.Date;
    import java.util.List;
    import java.util.Optional;
    
    public class Demo{
        
        /*
         初始化SqlSessionFactory
         */
        public SqlSessionFactory init() throws IOException 
        {   
            // ...
            
            //列名为`update_time`的列在执行新增和修改指令时，如果没有指定值，使用默认值now()
            ColumnValueParser updateTimeColumnValueParser = new SpecificColumnValueParser(
                    columnMateData -> "update_time".equals(columnMateData.getColumnName()) ,
                    new SqlCommandType[]{SqlCommandType.UPDATE ,SqlCommandType.INSERT} ,"now()" ,false);
    
            //列名为`create_time`的列在执行新增指令时，如果没有指定值，使用默认值now()
            ColumnValueParser createTimeColumnValueParser = new SpecificColumnValueParser(columnMateData ->
                    "create_time".equals(columnMateData.getColumnName()) ,
                    new SqlCommandType[]{SqlCommandType.INSERT} ,"now()" ,false);
    
            StatementAutoRegister register = new DefaultStatementAutoRegister.Builder()
                            .setEntityMateDataParser(
                                    new DefaultEntityMateDataParser.Builder()
                                    .setTableSchemaQueryRegister(tableSchemaQueryRegister)
                                    .setColumnValueParser(new DefaultColumnValueParser(
                                        Arrays.asList(
                                            updateTimeColumnValueParser ,
                                            createTimeColumnValueParser
                                        ))
                                    )
                                    .build())
                            .addDefaultMappedStatementFactories()
                            .build();
            
            // ...
        }
    }
```
#### 效果演示
-   insert
```java
    import com.github.ibatis.statement.demo.User;
    import com.github.ibatis.statement.demo.UserMapper;
    import org.junit.Test;
    
    public class Demo{
        
        @Test
        public void insert(){
            User user = new User();
            user.setName("张三");
            user.setAddress("杭州");
            userMapper.insert(user);
        }
    }
```
    MyBatis-Plus will execute the following SQL
```sql
  INSERT INTO `user` (`note`,`update_time`,`address`,`create_time`,`name`,`id`,`version`,`removed`) VALUES (null,now(),'杭州',now(),'张三',null,0,0);
```
-   update
```java
    import com.github.ibatis.statement.demo.User;
    import com.github.ibatis.statement.demo.UserMapper;
    import org.junit.Test;
   
    public class Demo{
        
        @Test
        public void updateByPrimaryKey(){
            User user = new User();
            user.setId(10);
            user.setName("张三");
            user.setAddress("杭州");
            userMapper.insert(user);
            user.setAddress("嘉兴");
            userMapper.updateByPrimaryKey(user);
        }
    }
```
    MyBatis-Plus will execute the following SQL
```sql
  UPDATE `user` SET `note` = null, `update_time` = now(), `address` = '嘉兴', `create_time` = null, 
  `name` = '张三', `version` = 0 WHERE `id` = 10 AND `removed` = 0;
```
>   更多方法请参考接口*package com.github.ibatis.statement.mapper.KeyTableMapper*及其父接口方法注释

### 7.默认where条件
>   仅支持修改/删除/查询指令

#### 7.1 在实体类属性上申明默认where条件
```java
    import com.github.ibatis.statement.base.condition.Condition;
    import com.github.ibatis.statement.base.condition.Strategy;
    import com.github.ibatis.statement.base.core.Column;
    import com.github.ibatis.statement.base.core.Entity;
    import com.github.ibatis.statement.base.logical.Logical;
    import com.github.ibatis.statement.mapper.param.ConditionRule;
    import org.apache.ibatis.mapping.SqlCommandType;
    import java.util.Date;
    
    //@Logical(columnName = "removed" ,existValue = "0" ,notExistValue = "1")//定义逻辑列
    @Entity(tableName = "user")//申明实体映射的表
    public class User {
        
        // ... columns    
    
        //执行查询指令时，如果`create_time`列没有指定查询条件，添加默认查询条件 create_time > '2020-08-12 00:00:00'
        @Condition(commandTypes = SqlCommandType.SELECT, rule = ConditionRule.GT , value = "2020-08-12 00:00:00")
        //执行删除指令时，如果`create_time`列没有指定查询条件，添加默认查询条件 create_time < '2020-08-12 00:00:00'
        @Condition(commandTypes = SqlCommandType.DELETE, rule = ConditionRule.LT , value = "2020-08-11 00:00:00")
        //执行修改指令时，如果`create_time`列没有指定查询条件，添加默认查询条件 create_time between '2020-08-11 00:00:00' AND '2020-08-12 00:00:00'
        @Condition(commandTypes = SqlCommandType.UPDATE, rule = ConditionRule.BETWEEN ,value = "'2020-08-11 00:00:00' AND '2020-08-12 00:00:00'")
        @Column("create_time")
        private Date createTime;
        
        // ... columns    
        
        // ... (ellipsis get/set methods)
    
    }
```
```java
    package com.github.ibatis.statement.base.condition;
    
    import com.github.ibatis.statement.base.core.ExpressionParser;
    import com.github.ibatis.statement.mapper.param.ConditionRule;
    import org.apache.ibatis.mapping.SqlCommandType;
    import java.lang.annotation.*;
    
    /**
     * 声明列作为过滤条件
     * @Author: junjie
     * @Date: 2020/7/22
     */
    @Target({ElementType.FIELD ,ElementType.TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    @Inherited
    @Repeatable(Conditions.class)
    public @interface Condition {
    
        /**
         * 定义执行哪些操作（修改/删除/查询）时使用条件过滤
         * @see ColumnCondition#sqlCommandType
         */
        SqlCommandType[] commandTypes() default {SqlCommandType.UPDATE ,SqlCommandType.DELETE};
    
        /**
         * 列名
         * 如果该注解添加在类属性上，则该值默认为属性映射的列名
         * @see ColumnCondition#columnName
         */
        String columnName() default "";
    
        /**
         * 规则
         * @return
         */
        ConditionRule rule() default ConditionRule.EQ;
    
        /**
         * 默认使用该值作为过滤值，会直接拼接到sql语句上，可以使用支持的数据库函数
         * 注意！如果是字符类型需要带''
         * 支持{@link ExpressionParser}解析
         * @see ColumnCondition#value
         */
        String value() default ColumnCondition.EMPTY_VALUE;
    
        /**
         * 过滤值选择策略
         */
        Strategy strategy() default Strategy.DEFAULT;
    
    }
```

#### 7.2 全局配置解析默认赋值规则
-   实现*com.github.ibatis.statement.base.dv.ColumnDefaultValue*自定义全局解析规则
```java
    /**
     * 解析使用默认赋值的列
     * @Author: junjie
     * @Date: 2020/7/22
     */
    public interface ColumnValueParser extends Sorter{
    
        /**
         * 解析
         * @param entityMateData
         */
        void parse(EntityMateData entityMateData);
    
    }
```
-   注册实现类
```java
    import com.github.ibatis.statement.base.condition.ColumnConditionParser;
    import com.github.ibatis.statement.base.condition.DefaultColumnConditionParser;
    import com.github.ibatis.statement.base.condition.SpecificColumnConditionParser;
    import com.github.ibatis.statement.base.condition.Strategy;
    import com.github.ibatis.statement.base.core.parse.*;
    import com.github.ibatis.statement.base.dv.ColumnValueParser;
    import com.github.ibatis.statement.base.dv.DefaultColumnValueParser;
    import com.github.ibatis.statement.base.dv.SpecificColumnValueParser;
    import com.github.ibatis.statement.base.logical.DefaultLogicalColumnMateDataParser;
    import com.github.ibatis.statement.base.logical.SpecificLogicalColumnMateDataParser;
    import com.github.ibatis.statement.mapper.param.ConditionParams;
    import com.github.ibatis.statement.mapper.param.ConditionRule;
    import com.github.ibatis.statement.mapper.param.DynamicParams;
    import com.github.ibatis.statement.register.DefaultStatementAutoRegister;
    import com.github.ibatis.statement.register.StatementAutoRegister;
    import com.github.ibatis.statement.register.database.DefaultTableSchemaQueryRegister;
    import com.github.ibatis.statement.register.database.MysqlTableSchemaQuery;
    import com.github.ibatis.statement.register.database.TableSchemaQueryRegister;
    import com.github.ibatis.statement.util.StringUtils;
    import org.apache.ibatis.io.Resources;
    import org.apache.ibatis.jdbc.ScriptRunner;
    import org.apache.ibatis.mapping.SqlCommandType;
    import org.apache.ibatis.session.SqlSession;
    import org.apache.ibatis.session.SqlSessionFactory;
    import org.apache.ibatis.session.SqlSessionFactoryBuilder;
    import org.junit.Test;
    import java.io.IOException;
    import java.util.Arrays;
    import java.util.Date;
    import java.util.List;
    import java.util.Optional;
    
    public class Demo{
        
        /*
         初始化SqlSessionFactory
         */
        public SqlSessionFactory init() throws IOException 
        {   
            // ...
            
            //执行修改指令时，如果`create_time`列没有指定查询条件，添加默认查询条件 create_time between '2020-08-11 00:00:00' AND '2020-08-12 00:00:00'
            ColumnConditionParser updateColumnConditionParser = new SpecificColumnConditionParser(
                    columnMateData -> "create_time".equals(columnMateData.getColumnName()) ,
                    new SqlCommandType[]{SqlCommandType.UPDATE}, ConditionRule.BETWEEN, 
                    Strategy.CUSTOM_MISS_DEFAULT ,"'2020-08-11 00:00:00' AND '2020-08-12 00:00:00'");
            
            //执行查询指令时，如果`create_time`列没有指定查询条件，添加默认查询条件 create_time > '2020-08-12 00:00:00'
            ColumnConditionParser selectColumnConditionParser = new SpecificColumnConditionParser(
                    columnMateData -> "create_time".equals(columnMateData.getColumnName()) ,
                    new SqlCommandType[]{SqlCommandType.SELECT}, ConditionRule.GT, 
                    Strategy.CUSTOM_MISS_DEFAULT ,"2020-08-12 00:00:00");
            
            //执行删除指令时，如果`create_time`列没有指定查询条件，添加默认查询条件 create_time < '2020-08-12 00:00:00'
            ColumnConditionParser deleteColumnConditionParser = new SpecificColumnConditionParser(
                    columnMateData -> "create_time".equals(columnMateData.getColumnName()) ,
                    new SqlCommandType[]{SqlCommandType.DELETE}, ConditionRule.LT, 
                    Strategy.CUSTOM_MISS_DEFAULT ,"2020-08-11 00:00:00");
            
             StatementAutoRegister register = new DefaultStatementAutoRegister.Builder()
                    .setEntityMateDataParser(
                            new DefaultEntityMateDataParser.Builder()
                            .setTableSchemaQueryRegister(tableSchemaQueryRegister)
                            .setColumnConditionParser(new DefaultColumnConditionParser(
                                    Arrays.asList(
                                            updateColumnConditionParser ,
                                            selectColumnConditionParser,
                                            deleteColumnConditionParser
                                    )
                            ))
                            .build())
                    .addDefaultMappedStatementFactories()
                    .build();
            
            // ...
        }
    }
```
#### 效果演示
-   select
```java
    package com.github.ibatis.statement.demo;
    import com.github.ibatis.statement.demo.User;
    import com.github.ibatis.statement.demo.UserMapper;
    import org.junit.Test;
   
    public class Demo{
        
        @Test
        public void selectByPrimaryKey(){
            userMapper.selectByPrimaryKey(2);
        }
    }
``` 
    MyBatis-Plus will execute the following SQL
```sql
  SELECT `note`,`update_time`,`address`,`create_time`,`name`,`id`,`version`
   FROM `user`
   WHERE `id` = 2 AND `create_time` > '2020-08-12 00:00:00';
```
-   update
```java
    import com.github.ibatis.statement.demo.User;
    import com.github.ibatis.statement.demo.UserMapper;
    import org.junit.Test;
   
    public class Demo{
        
        @Test
        public void updateByPrimaryKey(){
            User user = new User();
            user.setId(10);
            user.setName("张三");
            user.setAddress("杭州");
            user.setCreateTime(new Date());
            userMapper.insert(user);
            user.setAddress("嘉兴");
            userMapper.updateByPrimaryKey(user);
        }
    }
```
    MyBatis-Plus will execute the following SQL
```sql
  UPDATE `user` SET `note` = null , `update_time` = null , `address` = '嘉兴' , `create_time` = null , `name` = '张三' ,
   `version` = 0 WHERE `id` = 10 AND `create_time` between '2020-08-11 00:00:00' AND '2020-08-12 00:00:00';
```
-   delete
```java
    import com.github.ibatis.statement.demo.User;
    import com.github.ibatis.statement.demo.UserMapper;
    import org.junit.Test;
   
    public class Demo{
        
        @Test
        public void deleteByPrimaryKey(){
            User user = new User();
            user.setId(11);
            user.setName("张三");
            user.setAddress("杭州");
            user.setCreateTime(new Date());
            userMapper.insert(user);
            userMapper.deleteByPrimaryKey(user.getId());
        }
        
    }
```
    MyBatis-Plus will execute the following SQL
```sql
  DELETE
   FROM `user`
   WHERE `id` = 11 AND `create_time` < '2020-08-11 00:00:00' AND 1 = 1;
```

>   更多方法请参考接口*package com.github.ibatis.statement.mapper.KeyTableMapper*及其父接口方法注释

### 8.禁止特定列查询/修改/新增

-   使用@Column#commandTypeMappings属性指定允许的指令
```java
    import com.github.ibatis.statement.base.condition.Condition;
    import com.github.ibatis.statement.base.condition.Strategy;
    import com.github.ibatis.statement.base.core.Column;
    import com.github.ibatis.statement.base.core.Entity;
    import com.github.ibatis.statement.base.logical.Logical;
    import com.github.ibatis.statement.mapper.param.ConditionRule;
    import org.apache.ibatis.mapping.SqlCommandType;
    import java.util.Date;
    
    @Entity(tableName = "user")//申明实体映射的表
    public class User {
        
        // ... columns    
        
        //禁止修改该列
        @Column(value = "create_time" ,commandTypeMappings = {SqlCommandType.SELECT ,SqlCommandType.INSERT})
        private Date createTime;
        
        // ... columns    
        
        // ... (ellipsis get/set methods)
    
    }
```

#### 效果演示
-   update
```java
    import com.github.ibatis.statement.demo.User;
    import com.github.ibatis.statement.demo.UserMapper;
    import org.junit.Test;
   
    public class Demo{
        
        @Test
        public void updateByPrimaryKey(){
            User user = new User();
            user.setId(10);
            user.setName("张三");
            user.setAddress("杭州");
            user.setCreateTime(new Date());
            userMapper.insert(user);
            user.setAddress("嘉兴");
            userMapper.updateByPrimaryKey(user);
        }
    }
```
    MyBatis-Plus will execute the following SQL
```sql
  UPDATE `user` SET `note` = null , `update_time` = null , `address` = '嘉兴' , `name` = '张三' , `version` = 0
   WHERE `id` = 10 AND 1 = 1;
```

>   更多方法请参考接口*package com.github.ibatis.statement.mapper.KeyTableMapper*及其父接口方法注释

### 9.动态条件查询

-   Mapper接口继承*com.github.ibatis.statement.mapper.DynamicSelectMapper*
```java
    package com.github.ibatis.statement.mapper;
    
    import com.github.ibatis.statement.mapper.param.ConditionParams;
    import com.github.ibatis.statement.mapper.param.DynamicParams;
    import org.apache.ibatis.exceptions.TooManyResultsException;
    import java.util.List;
    
    /**
     * 自定义规则查询接口
     * @Author: junjie
     * @Date: 2020/8/31
     */
    public interface DynamicSelectMapper<T> extends EntityType<T> {
    
        /**
         * 通过自定义规则查询符合条件的数据
         * @param dynamicParams
         * @return
         */
        List<T> selectByDynamicParams(DynamicParams dynamicParams);
    
        /**
         * 通过自定义规则查询符合条件的数据
         * @param dynamicParams
         * @throws org.apache.ibatis.exceptions.TooManyResultsException 如果存在多个满足条件的结果
         * @return
         */
        default T selectUniqueByDynamicParams(DynamicParams dynamicParams)
        {
            List<T> results = selectByDynamicParams(dynamicParams);
            if (results.size() > 1){
                throw new TooManyResultsException("result count :" + results.size());
            }
            return results.size() > 0 ? results.get(0) : null;
        }
    
        /**
         * 通过自定义规则查询符合条件的数据
         * @param conditionParams
         * @return
         */
        default List<T> selectByWhereConditions(ConditionParams conditionParams){
            return selectByDynamicParams(conditionParams == null ? null : conditionParams.dynamicParams());
        }
    
        /**
         * 通过自定义规则查询符合条件的数据
         * @param conditionParams
         * @return
         * @throws org.apache.ibatis.exceptions.TooManyResultsException 如果存在多个满足条件的结果
         */
        default T selectUniqueByWhereConditions(ConditionParams conditionParams){
            return selectUniqueByDynamicParams(conditionParams == null ? null : conditionParams.dynamicParams());
        }
    
        /**
         * 通过自定义规则查询符合条件的数据条数
         * @param dynamicParams
         * @return
         */
        int countDynamicParams(DynamicParams dynamicParams);
    
        /**
         * 通过自定义规则查询符合条件的数据条数
         * @param conditionParams
         * @return
         */
        default int countWhereConditions(ConditionParams conditionParams){
            return countDynamicParams(conditionParams == null ? null : conditionParams.dynamicParams());
        }
    
    }

```
#### 效果演示
```java
    import com.github.ibatis.statement.demo.User;
    import com.github.ibatis.statement.demo.UserMapper;
    import org.junit.Test;
    // ...
   
    public class Demo{
        
        @Test
        public void selectByDynamicParams(){
            userMapper.selectByDynamicParams(new DynamicParams()
                .where(new ConditionParams()
                        .between("create_time", "2020-08-11", new Date())
                        .likeLeft("`name`", "张"))
                .groupBy("address", "`name`")
                .having(new ConditionParams().notNull("note"))
                .limit(10));
        }
    }
    
```
    MyBatis-Plus will execute the following SQL
```sql
    select `note`,`update_time`,`address`,`create_time`,`name`,`id`,`version`
     FROM `user`
     WHERE 1 = 1 and create_time between '2020-08-11' and '2020-09-11 23:37:51.541' 
     and `name` like '%张' group by address , `name` having 1 = 1 and note is not null
     LIMIT 0, 10;
```

### 10.基于默认where条件和默认赋值配置可实现乐观锁
```java
    import com.github.ibatis.statement.base.condition.Condition;
    import com.github.ibatis.statement.base.condition.Strategy;
    import com.github.ibatis.statement.base.core.Column;
    import com.github.ibatis.statement.base.core.Entity;
    import com.github.ibatis.statement.base.logical.Logical;
    import com.github.ibatis.statement.mapper.param.ConditionRule;
    import org.apache.ibatis.mapping.SqlCommandType;
    import java.util.Date;
    
    @Entity(tableName = "user")//申明实体映射的表
    public class User {
        
        // ... columns    
        
        @DefaultValue(commandTypes = {SqlCommandType.UPDATE} ,value = "&{column} + 1")
        @Condition(commandTypes = {SqlCommandType.UPDATE} ,strategy = Strategy.CUSTOM)
        private int version;
        
        // ... (ellipsis get/set methods)
    
    }
```
#### 效果演示
```java
    import com.github.ibatis.statement.demo.User;
    import com.github.ibatis.statement.demo.UserMapper;
    import org.junit.Test;
   
    public class Demo{
        
        @Test
        public void optimisticLock(){
            User user = new User();
            user.setId(11);
            user.setName("张三");
            user.setAddress("杭州");
            user.setCreateTime(new Date());
            user.setVersion(12);
            userMapper.insert(user);
            user.setNote("无");
            userMapper.updateByPrimaryKey(user);
        }
    }
    
```
    MyBatis-Plus will execute the following SQL
```sql
    UPDATE `user` SET `note` = '无' , `update_time` = null , `address` = '杭州' , `name` = '张三' , 
    `version` = `version` + 1
     WHERE `id` = 11 AND `version` = 12 AND 1 = 1;
```

### 11.table schema解析策略
```java
    package com.github.ibatis.statement.base.core;

    import com.github.ibatis.statement.base.core.matedata.PropertyMateData;
    
    /**
     * 表结构来源
     */
    public enum TableSchemaResolutionStrategy {
    
        /**
         * 查询数据库schema
         * 如果{@link PropertyMateData#getMappingStrategy()} == {@link MappingStrategy#AUTO}，允许类属性映射的列不存在，会忽略
         */
        DATA_BASE,
    
        /**
         * 解析实体类
         * 类似 hibernate/jpa 
         */
        ENTITY,
    
        /**
         * 默认使用全局配置
         * @see com.github.ibatis.statement.base.core.parse.DefaultEntityMateDataParser#defaultTableSchemaResolutionStrategy
         */
        GLOBAL,
    
    }
```

#### 11.1指定table schema解析策略
```java
    /**
     * @author junjie
     * @date 2020/9/9
     */
    @Entity(tableName = "user" ,resolutionStrategy = TableSchemaResolutionStrategy.ENTITY)
    public class User
    {
        //TableSchemaResolutionStrategy.ENTITY 策略需要指定主键列
        @Column(value = "id" ,mappingStrategy = MappingStrategy.PRIMARY_KEY)//申明字段映射的列
        private Integer id;
        @Column("name")
        private String name;
    
        //...
        
        //定义一个不存在的列
        @Column("no_exist_column")
        private String noExistColumn;
        
    }
```

#### 效果演示
-   select
```java
    package com.github.ibatis.statement.demo;
    import com.github.ibatis.statement.demo.User;
    import com.github.ibatis.statement.demo.UserMapper;
    import org.junit.Test;
   
    public class Demo{
        
        @Test
        public void selectByPrimaryKey(){
            userMapper.selectByPrimaryKey(2);
        }
    }
``` 
    MyBatis-Plus will execute the following SQL
```sql
  SELECT `note`,`update_time`,`address`,`create_time`,`name`,`id`,`version`,`no_exist_column`
   FROM `user`
   WHERE `id` = 2;
```
>   java.sql.SQLException: Unknown column 'not_exist_column' in 'field list'

#### 11.2全局配置
```java
    package com.github.ibatis.statement.demo;
    
    import com.github.ibatis.statement.base.core.parse.*;
    import com.github.ibatis.statement.mapper.param.ConditionParams;
    import com.github.ibatis.statement.mapper.param.DynamicParams;
    import com.github.ibatis.statement.register.DefaultStatementAutoRegister;
    import com.github.ibatis.statement.register.StatementAutoRegister;
    import com.github.ibatis.statement.register.database.DefaultTableSchemaQueryRegister;
    import com.github.ibatis.statement.register.database.MysqlTableSchemaQuery;
    import com.github.ibatis.statement.register.database.TableSchemaQueryRegister;
    import com.github.ibatis.statement.util.StringUtils;
    import org.apache.ibatis.io.Resources;
    import org.apache.ibatis.jdbc.ScriptRunner;
    import org.apache.ibatis.session.SqlSession;
    import org.apache.ibatis.session.SqlSessionFactory;
    import org.apache.ibatis.session.SqlSessionFactoryBuilder;
    import org.junit.Test;
    import java.io.IOException;
    import java.util.Arrays;
    import java.util.Date;
    import java.util.List;
    import java.util.Optional;
    import com.github.ibatis.statement.base.core.TableSchemaResolutionStrategy;
    
    public class Demo{
        /*
         初始化SqlSessionFactory
         */
        public SqlSessionFactory init() throws IOException 
        {   
            // ...
            
               StatementAutoRegister register = new DefaultStatementAutoRegister.Builder()
                            .setEntityMateDataParser(new DefaultEntityMateDataParser.Builder()
                                    .setTableSchemaQueryRegister(tableSchemaQueryRegister)
                                    .setTableSchemaResolutionStrategy(TableSchemaResolutionStrategy.ENTITY)
                                    .build())
                            .addDefaultMappedStatementFactories()
                            .build();
            
            // ...
        }
    }
```

### 12.自定义mappedStatementFactory
-   定义自定义方法
```java
    package com.github.ibatis.statement.demo;
    
    import com.github.ibatis.statement.mapper.KeyTableMapper;
    
    /**
     * @author junjie
     * @date 2020/9/9
     */
    public interface UserMapper extends KeyTableMapper<Integer ,User> {
    
        Integer selectMaxKey();
    
    }
```
-   工厂
```java
    package com.github.ibatis.statement.demo;
    
    import com.github.ibatis.statement.base.core.MethodSignature;
    import com.github.ibatis.statement.base.core.matedata.EntityMateData;
    import com.github.ibatis.statement.base.core.matedata.MappedStatementMateData;
    import com.github.ibatis.statement.base.core.matedata.TableMateData;
    import com.github.ibatis.statement.register.factory.AbstractSelectMappedStatementFactory;
    import org.apache.ibatis.builder.StaticSqlSource;
    import org.apache.ibatis.mapping.SqlSource;
    
    /**
     * @author junjie
     * @date 2020/9/27
     */
    public class SelectMaxIdMappedStatementFactory extends AbstractSelectMappedStatementFactory {
    
        @Override
        protected boolean isMatch(MappedStatementMateData mappedStatementMateData)
        {
            MethodSignature methodSignature = mappedStatementMateData.getMapperMethodMateData().getMethodSignature();
            EntityMateData entityMateData = mappedStatementMateData.getEntityMateData();
    
            if (entityMateData.getPrimaryKeyCount() != 1) {
                return false;
            }
    
            return super.isMatchMethodSignature(methodSignature ,new MethodSignature(
                    entityMateData.getReasonableKeyParameterClass() ,"selectMaxKey"));
        }
    
        @Override
        protected SqlSource sqlSource(MappedStatementMateData mappedStatementMateData) {
            TableMateData tableMateData = mappedStatementMateData.getEntityMateData().getTableMateData();
            String keyName = tableMateData.getKeyColumnMateDataMap().values()
                    .stream()
                    .findFirst()
                    .get()
                    .getEscapeColumnName();
            StringBuilder content = new StringBuilder("select ")
                    .append(keyName)
                    .append(" from ")
                    .append(tableMateData.getEscapeTableName()).append(" order by ")
                    .append(keyName)
                    .append(" limit 1");
            return new StaticSqlSource(mappedStatementMateData.getConfiguration() ,content.toString());
        }
    
    }
```
-   注册工厂
```java
    package com.github.ibatis.statement.demo;
    
    import com.github.ibatis.statement.base.core.parse.*;
    import com.github.ibatis.statement.mapper.param.ConditionParams;
    import com.github.ibatis.statement.mapper.param.DynamicParams;
    import com.github.ibatis.statement.register.DefaultStatementAutoRegister;
    import com.github.ibatis.statement.register.StatementAutoRegister;
    import com.github.ibatis.statement.register.database.DefaultTableSchemaQueryRegister;
    import com.github.ibatis.statement.register.database.MysqlTableSchemaQuery;
    import com.github.ibatis.statement.register.database.TableSchemaQueryRegister;
    import com.github.ibatis.statement.util.StringUtils;
    import org.apache.ibatis.io.Resources;
    import org.apache.ibatis.jdbc.ScriptRunner;
    import org.apache.ibatis.session.SqlSession;
    import org.apache.ibatis.session.SqlSessionFactory;
    import org.apache.ibatis.session.SqlSessionFactoryBuilder;
    import org.junit.Test;
    import java.io.IOException;
    import java.util.Arrays;
    import java.util.Date;
    import java.util.List;
    import java.util.Optional;
    
    public class Demo{
        /*
         初始化SqlSessionFactory
         */
        public SqlSessionFactory init() throws IOException 
        {   
            // ...
            
               StatementAutoRegister register = new DefaultStatementAutoRegister.Builder()
                            .setEntityMateDataParser(new DefaultEntityMateDataParser.Builder()
                                    .setTableSchemaQueryRegister(tableSchemaQueryRegister)
                                    .build())
                            .addDefaultMappedStatementFactories()
                            .addMappedStatementFactory(new SelectMaxIdMappedStatementFactory())
                            .build();
            
            // ...
        }
    }
```

#### 效果演示
-   select
```java
    package com.github.ibatis.statement.demo;
    import com.github.ibatis.statement.demo.User;
    import com.github.ibatis.statement.demo.UserMapper;
    import org.junit.Test;
   
    public class Demo{
      
        @Test
        public void selectMaxKey(){
            userMapper.selectMaxKey();
        }
    }
``` 
    MyBatis-Plus will execute the following SQL
```sql
    select `id` from `user` order by `id` desc limit 1; 
```