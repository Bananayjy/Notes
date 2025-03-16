package com.example.advice;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

public class Myadvice3 implements MethodInterceptor {
    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        System.out.println("环绕前……");
        //执行目标方法
        Object res = invocation.getMethod().invoke(invocation.getThis(), invocation.getArguments());
        System.out.println("环绕后……");
        return res;
    }
}
