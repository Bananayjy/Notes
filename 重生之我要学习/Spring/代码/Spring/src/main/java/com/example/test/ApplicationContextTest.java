package com.example.test;

import com.example.dao.UserDao;
import com.example.service.UserService;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class ApplicationContextTest {
    public static void main(String[] args) {
        //创建ApplicationContext，加载配置文件，实例化容器
        ApplicationContext applicationContext = new ClassPathXmlApplicationContext("beans.xml");
        Object otherBean =  applicationContext.getBean("otherBean");
        System.out.println(otherBean);
    }
}
