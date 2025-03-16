package com.example.processor;

import com.example.advice.MyAdvice;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class MockAopBeanPostProcessor implements BeanPostProcessor, ApplicationContextAware {
    //声明一个spring容器，通过Aware接口让spring帮忙注入底层的对象引用
    private ApplicationContext applicationContext;

    /*public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        //目的：对UserServiceImpl中的show1和show2进行方法进行增强，增强方法存在于MyAdvice中
        //问题：
        // 1、需要筛选serice.impl包下的所有的类的所有方法可以进行增强，不然是所有的bean在Init方法后都可以进行增强
        // 解决方案：if-else
        // 2、MyAdvice怎么去获取
        // 解决方案：将MyAdvice加入到容器中，从Spring 容器中获得MyAdvice
        if(bean.getClass().getPackage().getName().equals("com.example.service.Impl")){
            //生成当前bean的Proxy对象（使用jdk动态代理）
            Object beanProxy = Proxy.newProxyInstance(
                    bean.getClass().getClassLoader(),
                    bean.getClass().getInterfaces(),
                    (Object proxy, Method method, Object[] args) ->{
                            //通过Spring容器获得MyAdvice对象
                            MyAdvice myAdvice = applicationContext.getBean(MyAdvice.class);
                            //执行增强对象的before方法
                            myAdvice.beforeAdvice();
                            //执行目标对象的目标方法
                            Object result = method.invoke(bean, args);
                            //执行增强对象的after方法
                            myAdvice.afterReturningAdvice();
                            return result;
                        }
            );
            //返回增强后的代理类
            return beanProxy;
        }
        //Impl包以外的bean原封不动返回
        return bean;
    }*/

    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
