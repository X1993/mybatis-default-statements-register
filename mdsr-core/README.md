### 简介：
[mdsr-core](.)基于MyBatis二次开发，
在MyBatis的基础上只做增强不做改变，提高开发效率。

### 特征：
-   1.无侵入：启动阶段为符合特定规则的mapper方法自动注入，启动成功之后完全委托Mybatis管理，与Mybatis有很好的兼容性。
-   2.消耗小：启动成功之后完全委托Mybatis管理，框架本身不再执行任何其他任务。
-   3.灵活性：允许开发人员使用Mybatis原有机制（xml/注解）覆盖默认实现（默认方法不可以覆盖），允许切换table schema解析策略。
-   4.扩展性：预留大量扩展点，允许开发人员根据需要配置或扩展。
    -   4.1. 允许根据需要实现MappedStatement工厂扩展自动注册的方法 
    -   4.2. 支持基于注解的细粒度配置，提供全局匹配实现默认配置，并支持自由实现自定义解析器扩展配置
-   5.功能：除自动注入特定CRUD方法外，支持逻辑列、复合主键、默认where条件、默认赋值、禁止特定列查询/修改/新增、
    动态条件查询、方法名称解析注册。

### 流程框架:
<p align="center">
  <a>
   <img alt="Framework" src="Framework.jpg">
  </a>
</p>

### [文档](DOCUMENT.md)
>   直接使用基础组件比较繁琐，通常使用spring boot封装版本[spring-boot-starter-mdsr](../spring-boot-starter-mdsr)

### 快速开始
[参考单元测试](src/test/java/com/github/ibatis/statement/demo)

  
