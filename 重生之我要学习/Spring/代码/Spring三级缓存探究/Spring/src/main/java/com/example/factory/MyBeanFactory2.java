package com.example.factory;

import com.example.dao.UserDao;
import com.example.dao.impl.UserDaoImpl;

public class MyBeanFactory2 {
    //非静态工厂方法
    public UserDao getUserDao(){
        return new UserDaoImpl();
    }
}
