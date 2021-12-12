### 运行环境:
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

### 流程框架:
<p align="center">
  <a>
   <img alt="Framework" src="../mdsr-core/Framework.jpg">
  </a>
</p>

### [文档](DOCUMENT.md)

### [快速开始](../spring-boot-starter-mdsr-sample)

  
