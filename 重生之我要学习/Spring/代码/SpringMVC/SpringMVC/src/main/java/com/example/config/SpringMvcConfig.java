package com.example.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.handler.SimpleMappingExceptionResolver;

import java.util.Properties;

/**
 * @author banana
 * @create 2023-10-02 23:54
 */
//声明当前是一个配置类
@Configuration
//扫描注解
@ComponentScan({"com.example.controller","com.example.config", "com.example.ex"})
@EnableWebMvc
public class SpringMvcConfig {

    //简单异常处理器
    /*@Bean
    public SimpleMappingExceptionResolver simpleMappingExceptionResolver(){
        SimpleMappingExceptionResolver simpleMappingExceptionResolver
                = new SimpleMappingExceptionResolver();
        //不管是什么异常，都响应一个友好页面
        simpleMappingExceptionResolver.setDefaultErrorView("/error1.html");
        //区分异常类型，根据不同的异常类型，可以跳转不同的视图
        //键值对：key：异常对象全限定名 value：跳转的视图
        Properties properties = new Properties();
        properties.setProperty("java.lang.RuntimeException", "/error1.html");
        properties.setProperty("java.io.FileNotFoundException", "/error2.html");
        simpleMappingExceptionResolver.setExceptionMappings(properties);
        //将simpleMappingExceptionResolver注册到springmvc容器中
        return simpleMappingExceptionResolver;
    }*/



    @Bean   //不指定名字默认以方法名作为Bean的名称
    public CommonsMultipartResolver multipartResolver(){
        CommonsMultipartResolver commonsMultipartResolver
                = new CommonsMultipartResolver();
        commonsMultipartResolver.setDefaultEncoding("UTF-8");
        commonsMultipartResolver.setMaxUploadSizePerFile(1048576);
        commonsMultipartResolver.setMaxUploadSize(3145728);
        commonsMultipartResolver.setMaxInMemorySize(1048576);
        return commonsMultipartResolver;
    }
}
