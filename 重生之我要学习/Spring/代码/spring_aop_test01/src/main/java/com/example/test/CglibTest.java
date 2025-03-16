package com.example.test;


import com.example.advice.MyAdvice4;
import org.springframework.cglib.proxy.Enhancer;
import org.springframework.cglib.proxy.MethodInterceptor;
import org.springframework.cglib.proxy.MethodProxy;

import java.lang.reflect.Method;

//Cglib基于父类（目标类）生成Proxy
public class CglibTest {
    public static void main(String[] args) {
        //创建目标类
        Target targer = new Target();

        //创建增强类（通知对象）
        MyAdvice4 myAdvice4 = new MyAdvice4();

        //编写CGlib代码
        Enhancer enhancer = new Enhancer();

        //设置代理类的父类
        //父类为目标类，生成的代理对象就是目标类的子类
        enhancer.setSuperclass(Target.class);

        //设置回调
        enhancer.setCallback(new MethodInterceptor() {
            //interceptor方法相当于JDK的Proxy的invoke方法
            @Override
            public Object intercept(Object o, Method method, Object[] objects, MethodProxy methodProxy) throws Throwable {
                myAdvice4.before();
                Object res = method.invoke(targer, objects);
                myAdvice4.after();
                return res;
            }
        });

        //生成代理对象(是目标类的子类)
        Target o = (Target) enhancer.create();
        o.show();
    }
}
