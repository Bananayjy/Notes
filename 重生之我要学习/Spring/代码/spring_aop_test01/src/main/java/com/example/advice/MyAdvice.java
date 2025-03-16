package com.example.advice;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

//增强类，内部提供增强方法
@Component
@Aspect
public class MyAdvice {
    /*
        前置通知xml配置：
        <aop:before method="beforeAdvice" pointcut-ref="execution(void com.example.service.Impl.UserServiceImpl.show1())"/>
    */
    @Before("execution(void com.example.service.Impl.UserServiceImpl.show1())")
    public void beforeAdvice(JoinPoint joinPoint){
        System.out.println("当前目标对象" + joinPoint.getTarget());
        System.out.println("表达式：" + joinPoint.getStaticPart());
        System.out.println("前置的增强……");
    }

    public void afterReturningAdvice(){
        System.out.println("后置的增强……");
    }

    //环绕目标方法
    //ProceedingJoinPoint正在执行的切入点方法 = 切点 = 目标实际被增强方法
    //Object返回 目标方法可能有返回值
    public Object around(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        System.out.println("环绕前通知……");
        //执行目标方法
        Object res = proceedingJoinPoint.proceed();
        System.out.println("环绕后通知……");
        return  res;
    }
}
