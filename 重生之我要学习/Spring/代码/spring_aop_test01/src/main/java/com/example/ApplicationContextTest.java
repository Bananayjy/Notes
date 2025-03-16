package com.example;

import com.example.config.SpringConfig;
import com.example.service.UserService;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class ApplicationContextTest {
    public static void main(String[] args) {
        //基于xml的aop测试
        /*ApplicationContext app = new ClassPathXmlApplicationContext("applicationContext3.xml");
        UserService userService = app.getBean(UserService.class);
        userService.show1();*/

        //基于注解的aop测试
        ApplicationContext app = new AnnotationConfigApplicationContext(SpringConfig.class);
        UserService bean = app.getBean(UserService.class);
        bean.show1();
    }
}
