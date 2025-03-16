package com.example.listener;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

/**
 * @author banana
 * @create 2023-09-16 18:12
 */
public class ContextLoaderListener implements ServletContextListener {
    //封装名称
    private String CONTEXT_CONFIG_LOCATION = "contextConfigLocation";

    public void contextInitialized(ServletContextEvent sce) {
        System.out.println("contextInitialized ……");
        //0. 获取contextConfigLocation配置文件名称
        ServletContext servletContext = sce.getServletContext();
        String contextConfigLocation = servletContext.getInitParameter(CONTEXT_CONFIG_LOCATION);
        //解析出配置文件名称
        contextConfigLocation = contextConfigLocation.substring("classpath:".length());
        //1.创建Spring 容器
        ApplicationContext app = new ClassPathXmlApplicationContext(contextConfigLocation);
        //2.将容器存储到servletContext域中
        //通过ServletContextEvent获取servletContext（其参数由tomcat调用的时候传递）
        sce.getServletContext().setAttribute("applicationContext", app);
    }
}
