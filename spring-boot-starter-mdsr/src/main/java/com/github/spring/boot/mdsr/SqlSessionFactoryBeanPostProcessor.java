package com.github.spring.boot.mdsr;

import com.github.ibatis.statement.register.StatementAutoRegister;
import org.apache.ibatis.session.SqlSession;
import org.mybatis.spring.mapper.MapperFactoryBean;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * @Author: junjie
 * @Date: 2020/3/9
 */
@Import(DefaultStatementConfiguration.class)
@Configuration
public class SqlSessionFactoryBeanPostProcessor implements BeanPostProcessor,ApplicationContextAware {

    private ApplicationContext applicationContext;

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException
    {
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        /*
         * 为什么要通过bean处理器实现，如果使用类似监听器之类的方式注册缺省statement，
         * sqlSessionFactory的创建与缺省statement的注册之间会有一个时间差，
         * 如果这个时候有其他代码调用了缺省statement的方法会报错
         */
        if (bean instanceof MapperFactoryBean){
            MapperFactoryBean mapperFactoryBean = (MapperFactoryBean) bean;
            try (SqlSession sqlSession = mapperFactoryBean.getSqlSessionFactory().openSession()){
                StatementAutoRegister statementAutoRegister = this.applicationContext.getBean(StatementAutoRegister.class);
                statementAutoRegister.registerDefaultMappedStatement(sqlSession ,mapperFactoryBean.getMapperInterface());
            }
        }
        return bean;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

}
