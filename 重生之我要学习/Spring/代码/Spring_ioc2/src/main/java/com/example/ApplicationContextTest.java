package com.example;

import com.example.config.ApplicationContextConfig;
import com.example.dao.Impl.UserDaoImpl;
import com.example.service.Impl.UserServiceImpl;
import com.example.service.UserService;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class ApplicationContextTest {
    public static void main(String[] args) {
        //xml方式的Spring容器
        //ClassPathXmlApplicationContext applicationContext = new ClassPathXmlApplicationContext("applicationContext.xml");
        //注解方式去加载Spring的核心配置类
        AnnotationConfigApplicationContext annotationConfigApplicationContext =
                new AnnotationConfigApplicationContext(ApplicationContextConfig.class);
        /*Object userDao = annotationConfigApplicationContext.getBean("userDao");
        System.out.println(userDao);*/
    }
}
