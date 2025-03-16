package com.example.service.Impl;

import com.example.dao.UserDao;
import com.example.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

@Component("userService")
public class UserServiceImpl implements UserService {
    //根据类型注入
    /*@Autowired
    @Qualifier("userDao2")
    private UserDao userDao;*/

    /*public void setUserDao(UserDao userDao) {
        this.userDao = userDao;
    }*/

    public void show() {
        /*System.out.println("UserDao:" + userDao);*/
    }

}
