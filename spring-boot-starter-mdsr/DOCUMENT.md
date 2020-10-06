# 文档

## 运行环境:
JDK 8+, Maven, Mysql/MariaDB, Spring-Boot

## 快速开始
>   Spring-Boot项目 [参考](https://github.com/X1993/mybatis-default-statements-register/tree/master/spring-boot-starter-mdsr-sample) 
### 配置参数
-   maven依赖
```xml
  <dependencies>
        <dependency>
            <groupId>com.github.X1993</groupId>
            <artifactId>spring-boot-starter-mdsr</artifactId>
            <version>3.0.0-SNAPSHOT</version>
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
     * @author junjie
     * @date 2020/9/9
     */
    public interface UserMapper extends KeyTableMapper<Integer ,User> {
    
    }
```

## 功能介绍
### 1.使用自动注册的Mapper方法
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
    MyBatis-Plus will execute the following SQL
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
    MyBatis-Plus will execute the following SQL
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
    MyBatis-Plus will execute the following SQL
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
    MyBatis-Plus will execute the following SQL
```sql
  DELETE FROM `user` WHERE `id` = ? AND 1 = 1;
```

>   更多方法请参考接口*package com.github.ibatis.statement.mapper.KeyTableMapper*及其父接口方法注释

####  1.2.自定义sql覆盖自动注册的方法
-   通过XML或注解申明自定义sql
```java
    package com.github.mdsr.sample.mapper;
    
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
    MyBatis-Plus will execute the following SQL
```sql
  SELECT `note`,`update_time`,`address`,`create_time`,`name`,`id`,`version` FROM `user` WHERE `id` = 2 and removed = 0;
```  

####  1.3.方法匹配规则
-   方法签名与*package com.github.ibatis.statement.mapper.KeyTableMapper*及其父接口方法兼容（不包含默认方法）,
     如果不想注册所有方法，可以自由选择注册的方法
>   方法签名：返回值（兼容泛型实际声明的类型）+方法名+方法参数列表（兼容泛型实际声明的类型）
```java
    package com.github.mdsr.sample.mapper;
    
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
    package com.github.mdsr.sample.model;
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
    package com.github.mdsr.sample.env;
    
    import com.github.ibatis.statement.base.core.parse.TableSourceParser;
    import com.github.ibatis.statement.util.StringUtils;
    import org.springframework.context.annotation.Bean;
    import org.springframework.context.annotation.Configuration;
    import java.util.Optional;
    
    /**
     * @author junjie
     * @date 2020/9/13
     */
    
    @Configuration
    public class MybatisConfiguration {
        
        /**
         * 注册实体类默认映射的表名
         */
        @Bean
        public TableSourceParser actTableNameParser(){
            return entityClass ->  Optional.of(new TableSourceParser.Source(
                    "act_" + StringUtils.camelCaseToUnderscore(entityClass.getName())
                    )
            );
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

#### 3.1 通过yaml配置默认类#属性->表#列映射规则
```java
    package com.github.spring.boot.mdsr;
    
    import com.github.ibatis.statement.base.core.parse.DefaultPropertyMateDataParser;
    import com.github.ibatis.statement.base.core.parse.PropertyToColumnNameFunction;
    import com.github.ibatis.statement.base.core.parse.TryMappingEveryPropertyMateDataParser;
    import org.springframework.boot.context.properties.ConfigurationProperties;
    
    /**
     * @Author: junjie
     * @Date: 2020/3/17
     */
    @ConfigurationProperties(MappedStatementProperties.PREFIX)
    public class MappedStatementProperties {
    
        static final String PREFIX = "mybatis.mapped-statement.auto-register";
    
        /**
         * 默认为每一个属性需要尝试映射列
         */
        private boolean eachPropertyMappingColumn = true;
    
        /**
         * 如果没有指定属性映射的列名，默认通过属性名函数获取对应列名
         */
        private Class<? extends PropertyToColumnNameFunction> columnNameFunctionClass;
    
        public boolean isEachPropertyMappingColumn() {
            return eachPropertyMappingColumn;
        }
    
        public void setEachPropertyMappingColumn(boolean eachPropertyMappingColumn) {
            this.eachPropertyMappingColumn = eachPropertyMappingColumn;
        }
    
        public Class<? extends PropertyToColumnNameFunction> getColumnNameFunctionClass() {
            return columnNameFunctionClass;
        }
    
        public void setColumnNameFunctionClass(Class<? extends PropertyToColumnNameFunction> columnNameFunctionClass) {
            this.columnNameFunctionClass = columnNameFunctionClass;
        }
    }
```
```yaml
    mybatis:
      mapped-statement:
        auto-register:
          each-property-mapping-column: true #默认为每一个属性需要尝试映射列
```

#### 3.2.自定义解析规则 
-   实现*com.github.ibatis.statement.base.core.parse.PropertyMateDataParser*自定义全局解析规则，注册到spring容器即可
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
#### 效果演示
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
    MyBatis-Plus will execute the following SQL
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
    MyBatis-Plus will execute the following SQL
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
    MyBatis-Plus will execute the following SQL
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
#### 效果演示
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
    MyBatis-Plus will execute the following SQL
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
    MyBatis-Plus will execute the following SQL
```sql
  UPDATE `user` SET `note` = null, `update_time` = now(), `address` = '嘉兴', `create_time` = null, `name` = '张三', `version` = 0
   WHERE `id` = 10 AND `removed` = 0;
```
>   更多方法请参考接口*package com.github.ibatis.statement.mapper.KeyTableMapper*及其父接口方法注释

### 7.默认where条件
>   仅支持修改/删除/查询指令

#### 7.1 在实体类属性上申明默认where条件
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
    
        /**
         * 过滤值选择策略
         */
        Strategy strategy() default Strategy.CUSTOM_MISS_DEFAULT;
    
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
                    new SqlCommandType[]{SqlCommandType.SELECT}, ConditionRule.GT,
                    Strategy.CUSTOM_MISS_DEFAULT ,"2020-08-12 00:00:00");
        }
    
        /**
         * 执行修改指令时，如果`create_time`列没有指定查询条件，添加默认查询条件 create_time between '2020-08-11 00:00:00' AND '2020-08-12 00:00:00'
         * @return
         */
        @Bean
        public ColumnConditionParser updateColumnConditionParser(){
            return new SpecificColumnConditionParser(
                    columnMateData -> "create_time".equals(columnMateData.getColumnName()) ,
                    new SqlCommandType[]{SqlCommandType.UPDATE}, ConditionRule.BETWEEN,
                    Strategy.CUSTOM_MISS_DEFAULT ,"'2020-08-11 00:00:00' AND '2020-08-12 00:00:00'");
        }
    
        /**
         * 执行删除指令时，如果`create_time`列没有指定查询条件，添加默认查询条件 create_time < '2020-08-12 00:00:00'
         * @return
         */
        @Bean
        public ColumnConditionParser deleteColumnConditionParser(){
            return new SpecificColumnConditionParser(
                    columnMateData -> "create_time".equals(columnMateData.getColumnName()) ,
                    new SqlCommandType[]{SqlCommandType.DELETE}, ConditionRule.LT,
                    Strategy.CUSTOM_MISS_DEFAULT ,"2020-08-11 00:00:00");
        }
    }
```
#### 效果演示
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
    MyBatis-Plus will execute the following SQL
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
    MyBatis-Plus will execute the following SQL
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
#### 效果演示
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
                        .likeLeft("`name`", "张"))
                .groupBy("address", "`name`")
                .having(new ConditionParams().notNull("note"))
                .page(0, 10));
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
    @RunWith(SpringRunner.class)
    @SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
    public class UserMapperTest{
    
        @Autowired
        private UserMapper userMapper;
        
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
```yaml
  mybatis:
    mapped-statement:
      auto-register:
        table-schema-resolution-strategy: entity    
```

### 12.自定义mappedStatementFactory
-   定义自定义方法
```java
    package com.github.mdsr.sample.mapper;
        
    import com.github.ibatis.statement.mapper.KeyTableMapper;
    import org.apache.ibatis.annotations.Select;
    
    /**
     * @author junjie
     * @date 2020/9/9
     */
    public interface UserMapper extends KeyTableMapper<Integer ,User> {
    
        Integer selectMaxKey();
    
    }
```
-   注册工厂
```java
    package com.github.mdsr.sample.env;
    
    import com.github.ibatis.statement.base.core.MethodSignature;
    import com.github.ibatis.statement.base.core.matedata.EntityMateData;
    import com.github.ibatis.statement.base.core.matedata.MappedStatementMateData;
    import com.github.ibatis.statement.base.core.matedata.TableMateData;
    import com.github.ibatis.statement.register.factory.AbstractSelectMappedStatementFactory;
    import org.apache.ibatis.builder.StaticSqlSource;
    import org.apache.ibatis.mapping.ResultMap;
    import org.apache.ibatis.mapping.SqlSource;
    import org.springframework.stereotype.Component;
    import java.util.Collections;
    
    /**
     * @author junjie
     * @date 2020/9/27
     */
    @Component
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
                    .append(" desc limit 1");
            return new StaticSqlSource(mappedStatementMateData.getConfiguration() ,content.toString());
        }
    
        @Override
        protected ResultMap resultMaps(MappedStatementMateData mappedStatementMateData) {
            return new ResultMap.Builder(mappedStatementMateData.getConfiguration(),
                    mappedStatementMateData.getMapperMethodMateData().getMappedStatementId() + "-ResultMap",
                    Integer.class,
                    Collections.EMPTY_LIST,
                    null).build();
        }
    }
```

#### 效果演示
-   select
```java
    @RunWith(SpringRunner.class)
    @SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
    public class UserMapperTest{
    
        @Autowired
        private UserMapper userMapper;
        
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

