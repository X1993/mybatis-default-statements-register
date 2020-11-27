## 简介：
基于mybatis-spring-boot-starter组装可运行的[mdsr-core](https://github.com/X1993/mybatis-default-statements-register/tree/master/mdsr-core)

### 特征：
-   1.无侵入：启动阶段为符合特定规则的mapper方法自动注入，启动成功之后完全委托Mybatis管理，与Mybatis有很好的兼容性。
-   2.消耗小：启动成功之后完全委托Mybatis管理，框架本身不再执行任何其他任务。
-   3.灵活性：允许开发人员使用Mybatis原有机制（xml/注解）覆盖默认实现（默认方法不可以覆盖），允许切换table schema解析策略。
-   4.扩展性：预留大量扩展点，允许开发人员根据需要配置或扩展。
    -   4.1. 允许根据需要实现MappedStatement工厂扩展自动注册的方法 
    -   4.2. 支持基于注解的细粒度配置，提供全局匹配实现默认配置，并支持自由实现自定义解析器扩展配置
-   5.功能：除自动注入特定方法外，支持逻辑列、复合主键、默认where条件、默认赋值、禁止特定列查询/修改/新增、动态条件查询、方法名称解析注册。

### 运行环境:
JDK 8+, Maven, Mysql/MariaDB/H2/(OTHER有特殊要求)

### 支持的数据库:  
1.TableSchemaResolutionStrategy=DATA_BASE，适用mysql 、mariaDB 、H2
> 其他数据库只需实现特定的适配器
```java
    /**
    * @see com.github.ibatis.statement.register.database.TableSchemaQuery
    */
```
2.TableSchemaResolutionStrategy=ENTITY，适用Mybatis支持的所有数据库

### 流程框架:
<p align="center">
  <a>
   <img alt="Framework" src="https://github.com/X1993/mybatis-default-statements-register/blob/master/mdsr-core/Framework.jpg">
  </a>
</p>

### 当前最新版本
-   Maven
```xml
    <dependency>
        <groupId>com.github.X1993</groupId>
        <artifactId>spring-boot-starter-mdsr</artifactId>
        <version>3.1.0-SNAPSHOT</version>
    </dependency>
```

### 文档
[文档](https://github.com/X1993/mybatis-default-statements-register/blob/master/spring-boot-starter-mdsr/DOCUMENT.md)

## 快速开始
[参考](https://github.com/X1993/mybatis-default-statements-register/tree/master/spring-boot-starter-mdsr-sample)

  