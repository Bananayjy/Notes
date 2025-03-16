package com.example.config;

import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;

/**
 * @author banana
 * @create 2023-10-03 14:07
 */
public class MyAnnotationConfigWebApplicationContext extends AnnotationConfigWebApplicationContext {
    public MyAnnotationConfigWebApplicationContext(){
        //注册核心配置类
        super.register(SpringMvcConfig.class);
    }
}
