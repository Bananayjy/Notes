package com.example.config;

import com.example.beans.OtherBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;

@Configuration
/*<context:component-scan base-package="com.example"></context:component-scan>*/
/* 全写 @ComponentScan(basePackages = {"com.example"})*/
@ComponentScan("com.example")
/*<context:property-placeholder location="classpath:jdbc.properties"/>*/
/*多个情况 @PropertySource({"classpath:jdbc.properties", "xxx"})*/
@PropertySource("classpath:jdbc.properties")
/*<import resource="classpath:beans.xml"/>*/
@Import(OtherBean.class)
public class ApplicationContextConfig {
}
