package com.example.utils;

import org.springframework.context.ApplicationContext;

import javax.servlet.ServletContext;

/**
 * @author banana
 * @create 2023-09-17 14:59
 */
public class WebApplicationContextUtils {
    //封装名称
    private static String CONTEXT_CONFIG_LOCATION = "contextConfigLocation";

    public static ApplicationContext getWebApplicationContext(ServletContext servletContext){
        String contextConfigLocation = servletContext.getInitParameter(CONTEXT_CONFIG_LOCATION);
        //解析出配置文件名称
        contextConfigLocation = contextConfigLocation.substring("classpath:".length());
        //去除后缀
        contextConfigLocation = contextConfigLocation.substring(0, contextConfigLocation.indexOf("."));
        ApplicationContext app = (ApplicationContext) servletContext.getAttribute(contextConfigLocation);
        return app;
    }
}
