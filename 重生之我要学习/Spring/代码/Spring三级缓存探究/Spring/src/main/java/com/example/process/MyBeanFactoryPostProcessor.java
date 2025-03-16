package com.example.process;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.RootBeanDefinition;

public class MyBeanFactoryPostProcessor implements BeanFactoryPostProcessor {
    //参数本质就是DefaultListableBeanFactory
    //可以通过该参数拿到BeanFactory的引用，因此可以对beanDefinitionMap中的BeanDefinition进行操作
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        //注册BeanDefinition
        //1.实例化一个BeanDefinition对象
        //一般我们使用RootBeanDefinition实现类
        BeanDefinition beanfinition = new RootBeanDefinition();
        //2.设置BeanDefinition对象的全限定名称
        beanfinition.setBeanClassName("com.example.dao.impl.PersonDaoImpl");
        //3.注册BeanDefinition对象，加入到Map集合中
        //由于ConfigurableListableBeanFactory接口比较上面，所以功能比较少
        //我们将其强转为DefaultListableBeanFactory
        //然后调用其中的注册方法
        DefaultListableBeanFactory defaultListableBeanFactory = (DefaultListableBeanFactory)beanFactory;
        defaultListableBeanFactory.registerBeanDefinition("personDao", beanfinition);
    }
}
