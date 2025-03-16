package com.example.anno;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

//自定义注解
//指定该注解在类上使用（元注解）
@Target(ElementType.TYPE)
//指定存活范围,使其存货到运行（元注解）
@Retention(RetentionPolicy.RUNTIME)
public @interface MyComponent {
    //用来存放beanName
    String value();
}
