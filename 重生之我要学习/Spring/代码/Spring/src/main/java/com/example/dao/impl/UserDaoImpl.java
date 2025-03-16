package com.example.dao.impl;

import com.example.dao.UserDao;
import org.springframework.beans.factory.InitializingBean;

public class UserDaoImpl implements UserDao, InitializingBean {
    //构造器
    public UserDaoImpl() {
        System.out.println("userDaoImpl构造器");
    }

    public void init(){
        System.out.println("init初始化方法执行");
    }


    @Override
    public void afterPropertiesSet() throws Exception {
        System.out.println("属性设置之后执行");
    }
}
