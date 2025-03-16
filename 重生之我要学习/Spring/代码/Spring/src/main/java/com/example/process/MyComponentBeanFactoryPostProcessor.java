package com.example.process;

import com.example.utils.BaseClassScanUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.beans.factory.support.RootBeanDefinition;

import java.util.Map;

public class MyComponentBeanFactoryPostProcessor implements BeanDefinitionRegistryPostProcessor {
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
        //通过扫描工具去扫描指定包及其子包下的所有类，收集所有带有自定义注解@Mycomponent的注解的类
        //并将扫描的结果放到Map中
        Map<String, Class> stringClassMap = BaseClassScanUtils.scanMyComponentAnnotation("com.example");
        //通过forEach遍历Map
        stringClassMap.forEach((k, v) ->{
            //在这里注册bean，即组装BeanDefinition对象后进行注册
            //1.创建一个BeanDefinition对象实现类
            BeanDefinition beanDefinition = new RootBeanDefinition();
            //2.获取类的全限定名
            String beanClassName = v.getName();
            //3.组装BeanDefinition对象
            beanDefinition.setBeanClassName(beanClassName);
            //4.注册(将该beandefinition对象加入到BeanDefinitionMap中即完成注册)
            registry.registerBeanDefinition(k, beanDefinition);
        });
    }

    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {

    }
}
