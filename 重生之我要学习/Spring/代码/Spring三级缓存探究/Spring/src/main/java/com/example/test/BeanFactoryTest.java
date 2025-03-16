package com.example.test;

import com.example.dao.UserDao;
import com.example.service.UserService;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;

public class BeanFactoryTest {

    public static void main(String[] args) {
        //1.创建工厂对象
        //这里使用的是其默认的实现类DefaultListableBeanFactory
        DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();

        //2.创建一个读取器（xml文件）
        XmlBeanDefinitionReader reader = new XmlBeanDefinitionReader(beanFactory);

        //3.读取配置文件内容给工厂
        reader.loadBeanDefinitions("beans.xml");

        //4.根据id获取Bean实例对象
        UserService userService = (UserService)beanFactory.getBean("userService");
        //com.example.service.impl.UserServiceImpl@725bef66
        System.out.println(userService);    

        UserDao userDao = (UserDao)beanFactory.getBean("userDao");
        //com.example.dao.impl.UserDaoImpl@2aaf7cc2
        System.out.println(userDao);

    }
}
