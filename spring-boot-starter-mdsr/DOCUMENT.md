使用文档
=================

  * [运行环境:](#运行环境)
  * [快速开始](#快速开始)
     * [配置参数](#配置参数)
     * [项目启动](#项目启动)
  * [功能介绍](#功能介绍)
     * [1.通用方法自动注册](#1通用方法自动注册)
        * [1.1.继承指定接口](#11继承指定接口)
        * [1.2.自定义sql覆盖自动注册的方法](#12自定义sql覆盖自动注册的方法)
        * [1.3.选择性注册](#13选择性注册)
     * [2.表名解析](#2表名解析)
        * [2.1.注解声明](#21注解声明)
        * [2.2.自定义解析](#22自定义解析)
        * [2.3.默认驼峰转下划线](#23默认驼峰转下划线)
        * [2.4.优先级](#24优先级)
     * [3.列解析](#3列解析)
        * [3.1.注解声明](#31注解声明)
        * [3.2.自定义解析](#32自定义解析)
        * [3.3.默认规则](#33默认规则)
        * [3.4.优先级](#34优先级)
     * [4.逻辑列](#4逻辑列)
        * [4.1.注解声明](#41注解声明)
        * [4.2.自定义解析](#42自定义解析)
        * [4.3.优先级](#43优先级)
     * [5.复合主键](#5复合主键)
     * [6.默认赋值](#6默认赋值)
        * [6.1.注解声明](#61注解声明)
        * [6.2.自定义解析](#62自定义解析)
        * [6.3.优先级](#63优先级)
     * [7.默认where条件](#7默认where条件)
        * [7.1.注解声明](#71注解声明)
        * [7.2.自定义解析](#72自定义解析)
        * [7.3.优先级](#73优先级)
     * [8.禁止特定列查询/修改/新增](#8禁止特定列查询修改新增)
     * [9.动态条件查询](#9动态条件查询)
     * [10.table schema解析策略](#10table-schema解析策略)
        * [10.1.注解声明](#101注解声明)
        * [10.2.默认配置](#102默认配置)
        * [10.3.优先级](#103优先级)
     * [11.扩展自动注册方法](#11扩展自动注册方法)
     * [12.方法名解析自动注册](#12方法名解析自动注册)
        * [12.1.方法命名规则](#121方法命名规则)
        * [12.2.If注解](#122if注解)
            
>  Directory Created by [gh-md-toc](https://github.com/ekalinin/github-markdown-toc)

## 运行环境:
JDK 8+, Maven, Mysql/MariaDB/H2/(OTHER有要求)

*支持的数据库*:
部分mapper方法支持标准sql实现,适用所有数据库(参考*com.github.ibatis.statement.mapper.method.MapperMethodEnum*#common=true),
其他需要使用数据库自定义语法的接口目前仅实现了mysql 、mariaDB 、H2(MODE=MySql)的适配
> 其他数据库需实现适配器,参考mysql适配模块,通过SPI机制获取服务提供者
```xml
    <dependency>
        <groupId>com.github.X1993</groupId>
        <artifactId>mdsr-mysql-adapter</artifactId>
        <version>1.0.0-SNAPSHOT</version>
    </dependency>
```    

## 快速开始
>   Spring-Boot项目 [参考](https://github.com/X1993/mybatis-default-statements-register/tree/master/spring-boot-starter-mdsr-sample) 
### 配置参数
-   maven依赖
```xml
  <dependencies>
        <dependency>
            <groupId>com.github.X1993</groupId>
            <artifactId>spring-boot-starter-mdsr</artifactId>
            <version>1.0.0-SNAPSHOT</version>
        </dependency>

        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>

        <dependency>
            <groupId>org.mariadb.jdbc</groupId>
            <artifactId>mariadb-java-client</artifactId>
            <version>2.2.5</version>
        </dependency>

    </dependencies>
```
-   schema脚本 
```sql
    DROP TABLE IF EXISTS user;
    CREATE TABLE user (
      id int(11) PRIMARY KEY AUTO_INCREMENT,
      name varchar(6) DEFAULT NULL ,
      create_time datetime DEFAULT NULL ,
      update_time datetime DEFAULT NULL ,
      address varchar(50) DEFAULT NULL ,
      address2 varchar(50) DEFAULT NULL ,
      note varchar(100) DEFAULT NULL ,
      version int(255) DEFAULT NULL ,
      removed bit(1) DEFAULT 0 COMMENT '是否已删除，1：已删除'
    ) DEFAULT CHARSET=utf8;
```
-   spring-boot.application.yaml配置
```yaml
    spring:
      datasource:
        driver-class-name: org.mariadb.jdbc.Driver
        url: jdbc:mariadb://localhost:3306/test?allowMultiQueries=true
        username: root
        password: 123456
        schema: classpath:db/schema.sql
```

### 项目启动
```java
    package com.github.mdsr.sample;
    
    import org.mybatis.spring.annotation.MapperScan;
    import org.springframework.boot.SpringApplication;
    import org.springframework.boot.autoconfigure.SpringBootApplication;
    
    @SpringBootApplication
    @MapperScan("com.github.mdsr.sample.mapper")
    public class Application {
    
        public static void main(String[] args) {
            SpringApplication.run(Application.class, args);
        }
    
    }
```
-   User实体类
```java
    package com.github.mdsr.sample.model;
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
        private int version;
        // ... (ellipsis get/set methods)
    }
```
-   UserMapper接口
```java
    package com.github.mdsr.sample.mapper;
    
    import com.github.ibatis.statement.mapper.KeyTableMapper;
    import com.github.mdsr.sample.model.User;
    import org.apache.ibatis.annotations.Select;
    
    /**
     * @author X1993
     * @date 2020/9/9
     */
    public interface UserMapper extends KeyTableMapper<Integer ,User> {
    
    }
```

## 功能介绍
### 1.通用方法自动注册
#### 1.1.继承指定接口
    *com.github.ibatis.statement.mapper*包下的接口方法支持自动注册，！表类型要匹配
>   效果演示
-   insert
```java
    @RunWith(SpringRunner.class)
    @SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
    public class UserMapperTest{
    
        @Autowired
        private UserMapper userMapper;
    
        @Test
        public void insert(){
            User user = new User();
            user.setName("张三");
            user.setAddress("杭州");
            userMapper.insert(user);
        }
    }
```
    will execute the following SQL
```sql
  INSERT INTO `user` (`note`,`update_time`,`address`,`create_time`,`name`,`id`,`version`) VALUES (NULL ,NULL ,'杭州','2020-09-09 16:10:22.129','张三',NULL ,0); 
```
-   select
```java
    @RunWith(SpringRunner.class)
    @SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
    public class UserMapperTest{
    
        @Autowired
        private UserMapper userMapper;
        
        @Test
        public void selectByPrimaryKey(){
            userMapper.selectByPrimaryKey(2);
        }
    }
``` 
    will execute the following SQL
```sql
  SELECT `note`,`update_time`,`address`,`create_time`,`name`,`id`,`version` FROM `user` WHERE `id` = 2;
```
-   update
```java
    @RunWith(SpringRunner.class)
    @SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
    public class UserMapperTest{
    
        @Autowired
        private UserMapper userMapper;
        
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
    will execute the following SQL
```sql
  UPDATE `user` SET `note` = NULL , `update_time` = NULL , `address` = '嘉兴', `create_time` = '2020-09-09 22:26:33.359', `name` = '张三', `version` = 0 WHERE `id` = 10;
```
-   delete
```java
    @RunWith(SpringRunner.class)
    @SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
    public class UserMapperTest{
    
        @Autowired
        private UserMapper userMapper;
        
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
    will execute the following SQL
```sql
  DELETE FROM `user` WHERE `id` = ? AND 1 = 1;
```

>   更多方法请参考接口*com.github.ibatis.statement.mapper.KeyTableMapper*及其父接口方法注释

####  1.2.自定义sql覆盖自动注册的方法
-   通过XML或注解申明自定义sql
```java
    package com.github.mdsr.sample.mapper;
    
    import com.github.ibatis.statement.mapper.KeyTableMapper;
    import org.apache.ibatis.annotations.Select;
    
    /**
     * @author X1993
     * @date 2020/9/9
     */
    public interface UserMapper extends KeyTableMapper<Integer ,User> {
    
        @Override
        @Select("select * from user where id = #{param1} and removed = 0")
        User selectByPrimaryKey(Integer key);
    
    }
```
```java
    @RunWith(SpringRunner.class)
    @SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
    public class UserMapperTest{
    
        @Autowired
        private UserMapper userMapper;
        
        @Test
        public void selectByPrimaryKey(){
            userMapper.selectByPrimaryKey(2);
        }
    }
``` 
    will execute the following SQL （don't auto register）
```sql
  SELECT `note`,`update_time`,`address`,`create_time`,`name`,`id`,`version` FROM `user` WHERE `id` = 2 and removed = 0;
```  

####  1.3.选择性注册
-   方法签名与*package com.github.ibatis.statement.mapper.KeyTableMapper*及其父接口方法兼容（不包含默认方法）,
     如果不想注册所有方法，可以自由选择注册的方法
>   方法签名：返回值（兼容泛型实际声明的类型）+方法名+方法参数列表（兼容泛型实际声明的类型）
```java
    package com.github.mdsr.sample.mapper;
    
    import com.github.ibatis.statement.mapper.EntityType;
    import com.github.ibatis.statement.mapper.KeyParameterType;
    import java.util.Collection;
    
    /**
     * @Author: X1993
     * @Date: 2020/9/14
     */
    public interface CustomUserMapper extends KeyParameterType<Integer> ,EntityType<User>{
    
        /**
         * 与{@link com.github.ibatis.statement.mapper.KeyTableMapper#selectByPrimaryKey(Object)}方法签名兼容，可以自动注册
         * @param key
         * @return
         */
        User selectByPrimaryKey(Integer key);
    
        /**
         * 与{@link com.github.ibatis.statement.mapper.SelectMapper#selectSelective(Object)}方法签名兼容，可以自动注册
         * @param condition
         * @param logicalExist
         * @return
         */
        Collection<? extends User> selectSelective(User condition , boolean logicalExist);
    
    }
```

### 2.表名解析
#### 2.1.注解声明
```java
    package com.github.mdsr.sample.model;
    import com.github.ibatis.statement.base.core.Column;
    import com.github.ibatis.statement.base.core.Entity;
    import java.util.Date;
    

    @Entity(tableName = "user")//申明实体映射的表
    public class User {
        
        // columns
        
        // ... (ellipsis get/set methods)
    }
```
#### 2.2.自定义解析
-   实现*com.github.ibatis.statement.base.core.parse.TableSourceParser*，注册到spring容器即可

```java
    package com.github.mdsr.sample.env;

import com.github.ibatis.statement.base.core.parse.TableSourceParser;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Optional;

/**
 * @author X1993
 * @date 2020/9/13
 */

@Configuration
public class MybatisConfiguration {

    /**
     * 注册实体类默认映射的表名
     */
    @Bean
    public TableSourceParser actTableNameParser() {
        return entityClass -> Optional.of(new TableSourceParser.Source(
                        "act_" + StringUtils.camelCaseToUnderscore(entityClass.getName())
                )
        );
    }

}
```

-   映射结果


    类名              |         表名 
    ----              |         ------
    User              |         act_user
    Location          |         act_location
    MerchantInfo      |         act_merchant_info


#### 2.3.默认驼峰转下划线
-   映射结果


    类名             |     表名 
    ------          |      -------
    User            |      user
    Location        |      location
    MerchantInfo    |      merchant_info    

>   可关闭或自定义规则
```yaml
    mybatis:
      mapped-statement:
        auto-register:
          default-mapping-table: true #默认每个实体类都存在映射的表
          #table-name-function-class: #默认规则
```

>   实现
```java
    /**
    * @see com.github.ibatis.statement.base.core.parse.DefaultTableSourceParser
    * @see com.github.spring.boot.mdsr.MappedStatementProperties
    */
```    

#### 2.4.优先级
   @Entity > 自定义*TableNameParser*实现（多个实现通过order()方法确定优先级）> 默认驼峰转下划线

>   原则：粒度越小，优先级越高

### 3.列解析
#### 3.1.注解声明
```java
    package com.github.ibatis.statement.demo;
    import com.github.ibatis.statement.base.core.Column;
    import com.github.ibatis.statement.base.core.Entity;
    import java.util.Date;

    public class User {
        
        @Column(value = "id")
        private Integer id;
        
        @Column(value = "name")
        private Integer name;
        
        // ... other columns
        
        // ... (ellipsis get/set methods)
    }
```

#### 3.2.自定义解析
-   实现*com.github.ibatis.statement.base.core.parse.PropertyMateDataParser*，注册到spring容器即可

```java
    package com.github.ibatis.statement.base.core.parse;

import com.github.ibatis.statement.base.core.matedata.PropertyMateData;

import java.util.Optional;

/**
 * 解析类属性对应列名
 * @Author: X1993
 * @Date: 2020/2/21
 */
public interface PropertyMateDataParser extends Sorter {

    /**
     * 解析
     * @param entityClass 实体类
     * @param field 类型属性
     * @return
     */
    Optional<PropertyMateData> parse(Class<?> entityClass, Field field);

}
```

#### 3.3.默认规则

>   指定某个类开启自动映射
```java
    package com.github.ibatis.statement.base.core.parse;
    
    import java.lang.annotation.*;
    
    /**
     * 默认所有属性都有映射的列
     * @see TryMappingEveryPropertyMateDataParser
     * @Author: X1993
     * @Date: 2021/6/10
     */
    @Target({ElementType.TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    @Inherited
    public @interface AutoMappingColumns {
    
        /**
         * 是否启用
         * @return
         */
        boolean enable() default true;
    
    }
```

>   可关闭或自定义全局映射规则
```yaml
    mybatis:
      mapped-statement:
        auto-register:
          each-property-mapping-column: true #默认为每一个属性需要尝试映射列
          #column-name-function-class: #默认规则
```

>   实现
```java
    /**
    * @see com.github.ibatis.statement.base.core.parse.DefaultPropertyMateDataParser
    * @see com.github.spring.boot.mdsr.MappedStatementProperties
    */
```

-   映射结果


    字段名            |         列名 
    ------           |         ------
    id               |         id
    address          |         address
    createTime       |         create_time
    updateTime       |         update_time
    

#### 3.4.优先级
   @Column > 自定义*PropertyMateDataParser*实现（多个实现通过order()方法确定优先级）
  
>   原则：粒度越小，优先级越高

### 4.逻辑列
#### 4.1.注解声明

```java
    package com.github.ibatis.statement.demo;

import com.github.ibatis.statement.base.core.Column;
import com.github.ibatis.statement.base.core.Entity;

//定义逻辑列
@Entity(tableName = "user")//申明实体映射的表
        public class User {
    @Column(value = "id")//申明字段映射的列
    private Integer id;

    //... column

    // ... (ellipsis get/set methods)

}
```

#### 4.2.自定义解析
-   实现*com.github.ibatis.statement.base.logical.SpecificLogicalColumnMateDataParser*，注册到spring容器即可

```java
    package com.github.ibatis.statement.base.logical;

import com.github.ibatis.statement.base.core.matedata.EntityMateData;

/**
 * 逻辑列元数据解析
 * @Author: X1993
 * @Date: 2020/3/4
 */
public interface LogicalColumnMateDataParser extends Sorter {

    /**
     * 解析
     * @param entityMateData
     */
    void parse(EntityMateData entityMateData);

}
```
-   注册实现类
```java
    @Configuration
    public class MybatisConfiguration {
        
        /**
         * 列名为`removed`的列为逻辑列
         * @return
         */
        @Bean
        public LogicalColumnMateDataParser removedLogicalColumnMateDataParser(){
            return new SpecificLogicalColumnMateDataParser("removed" ,"0" ,"1");
        }
    }
```
>   效果演示
-   insert
```java
    @RunWith(SpringRunner.class)
    @SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
    public class UserMapperTest{
    
        @Autowired
        private UserMapper userMapper;
    
        @Test
        public void insert(){
            User user = new User();
            user.setName("张三");
            user.setAddress("杭州");
            userMapper.insert(user);
        }
    }
```
    will execute the following SQL
```sql
  INSERT INTO `user` (`note`,`update_time`,`address`,`create_time`,`name`,`id`,`version`,`removed`) VALUES (NULL ,NULL ,'杭州','2020-09-09 16:10:22.129','张三',NULL ,0 ,0); 
```
-   select
```java
    @RunWith(SpringRunner.class)
    @SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
    public class UserMapperTest{
    
        @Autowired
        private UserMapper userMapper;
        
        @Test
        public void selectByPrimaryKey(){
            userMapper.selectByPrimaryKey(2);
        }
    }
``` 
    will execute the following SQL
```sql
  SELECT `note`,`update_time`,`address`,`create_time`,`name`,`id`,`version` FROM `user` WHERE `id` = 2 and removed = 0;
```
-   update
```java
    @RunWith(SpringRunner.class)
    @SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
    public class UserMapperTest{
    
        @Autowired
        private UserMapper userMapper;
        
        @Test
        public void updateByPrimaryKey(){
            User user = new User();
            user.setName("张三");
            user.setAddress("杭州");
            userMapper.insert(user);
            user.setAddress("嘉兴");
            userMapper.updateByPrimaryKey(user);
        }
    }
```
    will execute the following SQL
```sql
  UPDATE `user` SET `note` = NULL , `update_time` = NULL , `address` = '嘉兴', `create_time` = '2020-09-09 22:26:33.359', `name` = '张三', `version` = 0 WHERE `id` = 10 and removed = 0;
```
-   delete
```java
    @RunWith(SpringRunner.class)
    @SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
    public class UserMapperTest{
    
        @Autowired
        private UserMapper userMapper;
        
        @Test
        public void deleteByPrimaryKey(){
            User user = new User();
            user.setName("张三");
            user.setAddress("杭州");
            user.setCreateTime(new Date());
            userMapper.insert(user);
            userMapper.deleteByPrimaryKey(user.getId());
        }
    }
```
    will execute the following SQL
```sql
  UPDATE `user` SET `removed` = 1 WHERE `id` = 11 AND `removed` = 0; 
```
>   更多方法请参考接口*com.github.ibatis.statement.mapper.KeyTableMapper*及其父接口方法注释

#### 4.3.优先级
   @Logical > 自定义*LogicalColumnMateDataParser*实现（多个实现通过order()方法确定优先级）

>   原则：粒度越小，优先级越高

### 5.复合主键
-   schema.sql
```sql
    DROP TABLE IF EXISTS `entity3`;
        
    CREATE TABLE `entity3` (
      `id1` int(32) ,
      `id2` int(32) ,
      `value` varchar(30) DEFAULT NULL,
      `value2` varchar(30) DEFAULT NULL,
      CONSTRAINT table_entity3_pk PRIMARY KEY (id1, id2)
    )  DEFAULT CHARSET=utf8;;
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
>   仅支持新增/修改指令，各方法注册器*MappedStatementFactory*提供实现，不保证所有方法都支持
#### 6.1.注解声明
```java
    @Logical(columnName = "removed" ,existValue = "0" ,notExistValue = "1")//定义逻辑列
    public class User {
        
        // ... columns    
        
        //执行新增和修改指令时，如果没有指定值，使用默认值now()
        @DefaultValue(commandTypes = SqlCommandType.INSERT, value = "now()")
        private Date createTime;
        
        //执行新增指令时，如果没有指定值，使用默认值now()
        @DefaultValue(commandTypes = {SqlCommandType.INSERT ,SqlCommandType.UPDATE}, value = "now()")
        private Date updateTime;
        
        // ... columns    
        
        // ... (ellipsis get/set methods)
    
    }
```
```java
    /**
     * 添加该注解的属性映射的列每次执行插入/修改操作的时候会自动赋值
     * @Author: X1993
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

#### 6.2.自定义解析
-   实现*com.github.ibatis.statement.base.dv.ColumnDefaultValue*，注册到spring容器即可
```java
    /**
     * 解析使用默认赋值的列
     * @Author: X1993
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
    @Configuration
    public class MybatisConfiguration {
    
        /**
         * 列名为`update_time`的列在执行新增和修改指令时，如果没有指定值，使用默认值now()
         * @return
         */
        @Bean
        public ColumnValueParser updateTimeColumnValueParser(){
            return new SpecificColumnValueParser(columnMateData -> "update_time".equals(columnMateData.getColumnName()) ,
                    new SqlCommandType[]{SqlCommandType.UPDATE ,SqlCommandType.INSERT} ,"now()" ,false);
        }
    
        /**
         * 列名为`create_time`的列在执行新增指令时，如果没有指定值，使用默认值now()
         * @return
         */
        @Bean
        public ColumnValueParser createTimeColumnValueParser(){
            return new SpecificColumnValueParser(columnMateData ->
                    "create_time".equals(columnMateData.getColumnName()) ,
                    new SqlCommandType[]{SqlCommandType.INSERT} ,"now()" ,false);
        }
    }
```
>   效果演示
-   insert
```java
    @RunWith(SpringRunner.class)
    @SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
    public class UserMapperTest{
    
        @Autowired
        private UserMapper userMapper;
        
        @Test
        public void insert(){
            User user = new User();
            user.setName("张三");
            user.setAddress("杭州");
            userMapper.insert(user);
        }
    }
```
    will execute the following SQL
```sql
  INSERT INTO `user` (`note`,`update_time`,`address`,`create_time`,`name`,`id`,`version`,`removed`) VALUES (null,now(),'杭州',now(),'张三',null,0,0);
```
-   update
```java
    @RunWith(SpringRunner.class)
    @SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
    public class UserMapperTest{
    
        @Autowired
        private UserMapper userMapper;
        
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
    will execute the following SQL
```sql
  UPDATE `user` SET `note` = null, `update_time` = now(), `address` = '嘉兴', `create_time` = null, `name` = '张三', `version` = 0
   WHERE `id` = 10 AND `removed` = 0;
```
>   更多方法请参考接口*com.github.ibatis.statement.mapper.KeyTableMapper*及其父接口方法注释

#### 6.3.优先级
   @DefaultValue > 自定义*ColumnValueParser*实现（多个实现通过order()方法确定优先级）
   
>   原则：粒度越小，优先级越高

### 7.默认where条件
>   仅支持修改/删除/查询指令，各方法注册器*MappedStatementFactory*提供实现，不保证所有方法都支持

#### 7.1.注解声明
```java
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
     * @Author: X1993
     * @Date: 2020/7/22
     */
    @Target({ElementType.FIELD ,ElementType.TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    @Inherited
    @Repeatable(Conditions.class)
    public @interface Condition {
    
        /**
         * 定义执行哪些操作（修改/删除/查询）时使用条件过滤
         */
        SqlCommandType[] commandTypes() default {SqlCommandType.UPDATE ,SqlCommandType.DELETE};
    
        /**
         * 列名
         * 如果该注解添加在类属性上，则该值默认为属性映射的列名
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
         */
        String value() default ColumnCondition.EMPTY_VALUE;
    
    }
```

#### 7.2.自定义解析
-   实现*com.github.ibatis.statement.base.condition.ColumnConditionParser*，注册到spring容器即可

```java
    package com.github.ibatis.statement.base.condition;

import com.github.ibatis.statement.base.core.matedata.EntityMateData;

/**
 * 解析使用默认条件过滤的列
 * @see EntityMateData#commandTypeConditionMap
 * @Author: X1993
 * @Date: 2020/7/22
 */
public interface ColumnConditionParser extends Sorter {

    /**
     * 解析
     * @param entityMateData
     */
    void parse(EntityMateData entityMateData);

}
```
-   注册实现类
```java
    @Configuration
    public class MybatisConfiguration {
    
        /**
         * 执行查询指令时，如果`create_time`列没有指定查询条件，添加默认查询条件 create_time > '2020-08-12 00:00:00'
         * @return
         */
        @Bean
        public ColumnConditionParser selectColumnConditionParser(){
            return new SpecificColumnConditionParser(
                    columnMateData -> "create_time".equals(columnMateData.getColumnName()) ,
                    new SqlCommandType[]{SqlCommandType.SELECT}, ConditionRule.GT, "2020-08-12 00:00:00");
        }
    
        /**
         * 执行修改指令时，如果`create_time`列没有指定查询条件，添加默认查询条件 create_time between '2020-08-11 00:00:00' AND '2020-08-12 00:00:00'
         * @return
         */
        @Bean
        public ColumnConditionParser updateColumnConditionParser(){
            return new SpecificColumnConditionParser(
                    columnMateData -> "create_time".equals(columnMateData.getColumnName()) ,
                    new SqlCommandType[]{SqlCommandType.UPDATE}, ConditionRule.BETWEEN, "'2020-08-11 00:00:00' AND '2020-08-12 00:00:00'");
        }
    
        /**
         * 执行删除指令时，如果`create_time`列没有指定查询条件，添加默认查询条件 create_time < '2020-08-12 00:00:00'
         * @return
         */
        @Bean
        public ColumnConditionParser deleteColumnConditionParser(){
            return new SpecificColumnConditionParser(
                    columnMateData -> "create_time".equals(columnMateData.getColumnName()) ,
                    new SqlCommandType[]{SqlCommandType.DELETE}, ConditionRule.LT, "2020-08-11 00:00:00");
        }
    }
```
>   效果演示
-   select
```java
    @RunWith(SpringRunner.class)
    @SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
    public class UserMapperTest{
    
        @Autowired
        private UserMapper userMapper;
        
        @Test
        public void selectByPrimaryKey(){
            userMapper.selectByPrimaryKey(2);
        }
    }
``` 
    will execute the following SQL
```sql
  SELECT `note`,`update_time`,`address`,`create_time`,`name`,`id`,`version`
   FROM `user`
   WHERE `id` = 2 AND `create_time` > '2020-08-12 00:00:00';
```
-   update
```java
    @RunWith(SpringRunner.class)
    @SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
    public class UserMapperTest{
    
        @Autowired
        private UserMapper userMapper;
        
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
    will execute the following SQL
```sql
  UPDATE `user` SET `note` = null , `update_time` = null , `address` = '嘉兴' , `create_time` = null , `name` = '张三' , `version` = 0
     WHERE `id` = 10 AND `create_time` between '2020-08-11 00:00:00' AND '2020-08-12 00:00:00';
```
-   delete
```java
    @RunWith(SpringRunner.class)
    @SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
    public class UserMapperTest{
    
        @Autowired
        private UserMapper userMapper;
        
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
    will execute the following SQL
```sql
  DELETE
   FROM `user`
   WHERE `id` = 11 AND `create_time` < '2020-08-11 00:00:00' AND 1 = 1;
```

>   更多方法请参考接口*com.github.ibatis.statement.mapper.KeyTableMapper*及其父接口方法注释

#### 7.3.优先级
   @Condition > 自定义*ColumnConditionParser*实现（多个实现通过order()方法确定优先级）

>   原则：粒度越小，优先级越高

### 8.禁止特定列查询/修改/新增
-   使用@Column#commandTypeMappings属性指定允许的指令
```java
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
>   效果演示
-   update
```java
    @RunWith(SpringRunner.class)
    @SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
    public class UserMapperTest{
    
        @Autowired
        private UserMapper userMapper;
        
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
    will execute the following SQL
```sql
  UPDATE `user` SET `note` = null , `update_time` = null , `address` = '嘉兴' , `name` = '张三' , `version` = 0
   WHERE `id` = 10 AND 1 = 1;
```

>   更多方法请参考接口*com.github.ibatis.statement.mapper.KeyTableMapper*及其父接口方法注释

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
     * @Author: X1993
     * @Date: 2020/8/31
     */
    public interface DynamicSelectMapper<T> extends EntityType<T> {
    
        /**
         * 通过自定义规则查询符合条件的数据
         * @param dynamicParams
         * @return
         */
        List<T> selectByDynamicParams(DynamicParams dynamicParams);
    
        default List<T> selectByWhereConditions(ConditionParams conditionParams){
            return selectByDynamicParams(conditionParams == null ? null : conditionParams.dynamicParams());
        }
    
        /**
         * 通过自定义规则查询一条符合条件的数据
         * @param dynamicParams
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
         * 通过自定义规则查询符合条件的数据条数
         * @param dynamicParams
         * @return
         */
        int countDynamicParams(DynamicParams dynamicParams);
    
        default int countWhereConditions(ConditionParams conditionParams){
            return countDynamicParams(conditionParams == null ? null : conditionParams.dynamicParams());
        }
    
    }
```
>   效果演示
```java
    @RunWith(SpringRunner.class)
    @SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
    public class UserMapperTest{
    
        @Autowired
        private UserMapper userMapper;
        
        @Test
        public void selectByDynamicParams(){
            userMapper.selectByDynamicParams(new DynamicParams()
                .where(new ConditionParams()
                        .between("create_time", "2020-08-11", new Date())
                        .likeLeft("name", "张"))
                .groupBy("address", "name")
                .having(new ConditionParams().notNull("note"))
                .page(0, 10));
        }
    }
    
```
    will execute the following SQL
```sql
    select `note`,`update_time`,`address`,`create_time`,`name`,`id`,`version`
     FROM `user`
     WHERE 1 = 1 and create_time between '2020-08-11' and '2020-09-11 23:37:51.541' 
     and `name` like '%张' group by address , `name` having 1 = 1 and note is not null
     LIMIT 0, 10;
```

### 10.table schema解析策略

```java
    package com.github.ibatis.statement.base.core;
    
    import com.github.ibatis.statement.base.core.matedata.PropertyMateData;
    
    /**
     * 表结构解析策略
     * @author X1993
     * @date 2020/10/6
     */
    public enum TableSchemaResolutionStrategy {
    
        /**
         * 查询数据库schema
         * 不同的数据需要实现各自的{@link com.github.ibatis.statement.register.schema.TableSchemaQuery}
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
#### 10.1.注解声明
```java
    /**
     * @author X1993
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

>   效果演示
-   select
```java
    public class Demo{
        
        @Test
        public void selectByPrimaryKey(){
            userMapper.selectByPrimaryKey(2);
        }
        
    }
``` 
    will execute the following SQL
```sql
  SELECT `note`,`update_time`,`address`,`create_time`,`name`,`id`,`version`,`no_exist_column`
   FROM `user`
   WHERE `id` = 2;
```
>   java.sql.SQLException: Unknown column 'not_exist_column' in 'field list'

#### 10.2.默认配置
```yaml
  mybatis:
    mapped-statement:
      auto-register:
        table-schema-resolution-strategy: entity #默认解析策略    
```
#### 10.3.优先级
   @Entity > 全局默认配置

>   原则：粒度越小，优先级越高

### 11.扩展自动注册方法
-   定义方法签名
```java
    package com.github.mdsr.sample.mapper;
        
    import com.github.ibatis.statement.mapper.KeyTableMapper;
    import org.apache.ibatis.annotations.Select;
    
    /**
     * @author X1993
     * @date 2020/9/9
     */
    public interface UserMapper extends KeyTableMapper<Integer ,User> {
    
        Integer selectMaxPrimaryKey();
    
    }
```

-   构建工厂对象并注册到Spring容器

```java
    package com.github.mdsr.sample.env;

import com.github.ibatis.statement.base.core.MethodSignature;
import com.github.ibatis.statement.base.core.matedata.EntityMateData;
import com.github.ibatis.statement.base.core.matedata.MappedStatementMateData;
import com.github.ibatis.statement.base.core.matedata.TableMateData;
import org.apache.ibatis.builder.StaticSqlSource;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.mapping.SqlSource;
import org.springframework.stereotype.Component;

/**
 * @author X1993
 * @date 2020/9/27
 */
@Component
public class SelectMaxIdMappedStatementFactory extends AbstractMappedStatementFactory {

    @Override
    protected boolean isMatch(MappedStatementMateData mappedStatementMateData) {
        MethodSignature methodSignature = mappedStatementMateData.getMapperMethodMateData().getMethodSignature();
        EntityMateData entityMateData = mappedStatementMateData.getEntityMateData();

        if (entityMateData.getPrimaryKeyCount() != 1) {
            return false;
        }

        return methodSignature.isMatch(new MethodSignature(
                entityMateData.getReasonableKeyParameterClass(), selectMaxPrimaryKey));
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
                .append(" desc limit 1");
        return new StaticSqlSource(mappedStatementMateData.getConfiguration(), content.toString());
    }

    @Override
    protected SqlCommandType sqlCommandType(MappedStatementMateData mappedStatementMateData) {
        return SqlCommandType.SELECT;
    }
}
```

>   效果演示
-   select
```java
    @RunWith(SpringRunner.class)
    @SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
    public class UserMapperTest{
    
        @Autowired
        private UserMapper userMapper;
        
        @Test
        public void selectMaxPrimaryKey(){
            userMapper.selectMaxPrimaryKey();
        }
    }
``` 
    will execute the following SQL
```sql
    select `id` from `user` order by `id` desc limit 1;
```

### 12.方法名解析自动注册
-   实现类
```java
    /** @see  com.github.ibatis.statement.register.mysql.factory.MethodNameParseMappedStatementFactory*/
```

#### 12.1.方法命名规则 
       
-   查询（select/find 开头）：


    关键字           |        方法                          |          sql
    :------          |      :-------------                  |        :------
    And              |      findByNameAndCode(?,?)          |     where name= ? and code = ?     
    Or               |      findByNameOrCode(?,?)           |     where name= ? or code = ?       
    Eq               |      findByNameEq(?)                 |     where name= ?
    NotEq            |      findByNameNotEq(?)              |     where name != ?
    Lt               |      findByTimeLt(?)                 |     where time < ?
    Gt               |      findByTimeGt(?)                 |     where time > ?
    Le               |      findByTimeLe(?)                 |     where time <= ?
    Ge               |      findByTimeGe(?)                 |     where time >= ?
    NotLike          |      findByNameNotLike(?)            |     where name not like '%?%'
    Like             |      findByNameLike(?)               |     where name like '%?%'
    LikeLeft         |      findByNameLikeLeft(?)           |     where name like '%?'
    LikeRight        |      findByNameLikeRight(?)          |     where name like '?%'
    NotIn            |      findByIdNotIn(Collection<?>)    |     where id not in (?)
    In               |      findByIdIn(Collection<?>)       |     where id in (?)
    IsNull           |      findByNameIsNull()              |     where name is null
    NotNull          |      findByNameNotNull()             |     where name is not null
    Between          |      findByTimeBetween(?,?)          |     where name between ? and ?
    Between          |      findByTimeBetween(BetweenParam) |     where name between ? and ?
    NotBetween       |      findByNameNotBetween(?,?)       |     where name not between ? and ?
    Ne               |      findByNameNe()                  |     where name <> ''
    OrderBy          |      findOrderByNameTimeDesc         |     order by name time desc
    Count            |      selectCountByNameAndCode(?,?)   |     select count(0) from `table` where name= ? and code = ?  
    Limit            |      findByNameNeLimit(int)          |     where name <> '' limit ?
    Limit            |      findByNameNeLimit(LimitParam)   |     where name <> '' limit ? ,?
    
    
-  修改（update 开头）：

    除了前缀update，之后支持的规则和SELECT命令一样，方法的第一个参数类型必须为实体类型，实体对象非空属性映射的列作为
    update column（注意基本类型），注意 update sql 不支持 limit ?,? ，只允许为 limit ?


    关键字                                       |        方法                              |         sql
    :------                                      |      :-------------                      |       :------
    update(方法第一个为实体类，非空属性作为赋值项) | updateByNameNeOrderByIdAscLimit(int)     |   update `table` set column = ?, column2 = ? where name <> '' order by `id` limit ?
     
     
-  删除
    
    根据是否定义了逻辑列采用合适的删除策略，除了前缀delete，之后支持的规则和SELECT命令一样，
    注意 update/delete sql 不支持 limit ?,? ，只允许为 limit ?
    

    关键字                      |        方法                                   |         sql
    :------                     |      :-------------                          |       :------
    delete(没有定义逻辑列)       |      deleteByNameNeOrderByIdAscLimit(int)    |   delete from `table` where name <> '' order by `id` limit ? ,?
    delete(有定义逻辑列)         |      deleteByNameNeOrderByIdAscLimit(int)    |   update `table` set logical_column = 'delVal' where name <> '' order by `id` limit ?
   
   
#### 12.2.If注解

```java
    package com.github.ibatis.statement.mapper.param;

import com.github.ibatis.statement.base.core.matedata.EntityMateData;

import java.lang.annotation.*;

/**
 * 为方法参数添加if标签
 * @Author: X1993
 * @Date: 2020/7/24
 */
@Target({ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface If {

    String NULL = "";

    String PARAM_PLACEHOLDER = "#{?}";

    /**
     * if标签条件
     * #{param}：方法参数占位符
     * @return
     */
    String test() default PARAM_PLACEHOLDER + " != null";

    /**
     * {@link #otherwise()}设置了值且{@link #test()} == false，生效。
     * ！如果值为字符串需要自己添加引号
     * @return
     */
    String otherwise() default NULL;

}
```

-   效果演示
 mapper方法定义

```java
    package com.github.ibatis.statement.demo;

import com.github.ibatis.statement.mapper.EntityType;

public interface UserMapper extends EntityType<User> {

    User selectByNameAndAddressIn(@If(otherwise = "'jack'") String name , @If String ... address);

}
``` 

方法调用
```java
    class Test{
        @Test
        public void selectByNameAndAddressIn(){
            userMapper.selectByNameAndAddressIn(null ,"beijing");
            userMapper.selectByNameAndAddressIn(null ,null);
            userMapper.selectByNameAndAddressIn("tony" ,"hangzhou" ,"beijing");
        }
    }
```

    will execute the following SQL
```sql
    SELECT `update_time`,`address`,`create_time`,`name`,`id`,`version` FROM `user` WHERE 1 = 1 AND `name` = 'jack' AND `address` IN ( 'beijing' ) ; 
    SELECT `update_time`,`address`,`create_time`,`name`,`id`,`version` FROM `user` WHERE 1 = 1 AND `name` = 'jack'; 
    SELECT `update_time`,`address`,`create_time`,`name`,`id`,`version` FROM `user` WHERE 1 = 1 AND `name` = 'tony' AND `address` IN ( 'hangzhou' , 'beijing' ) 
```