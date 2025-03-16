package com.example.dao.impl;

import com.example.dao.UserDao;
import com.example.service.UserService;
import com.example.service.impl.UserServiceImpl;
import org.springframework.beans.factory.InitializingBean;

public class UserDaoImpl implements UserDao, InitializingBean {
    private UserService userService;

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

    public void setUserService(UserService userService) {
        this.userService = userService;
    }
}
