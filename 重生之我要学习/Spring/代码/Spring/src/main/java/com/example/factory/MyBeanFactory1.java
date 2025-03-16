package com.example.factory;

import com.example.dao.UserDao;
import com.example.dao.impl.UserDaoImpl;

//工厂类
public class MyBeanFactory1 {
    //静态工厂方法
    public static UserDao getUserDao(String name){
        //可以在此编写一些其他逻辑的代码
        //System.out.println("参数名称name为：" + name);
        return new UserDaoImpl();
    }
}
