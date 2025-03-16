package com.example.service.impl;

import com.example.dao.UserDao;
import com.example.service.UserService;
import org.springframework.beans.factory.InitializingBean;

import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

public class UserServiceImpl implements UserService {
    private UserDao userDao;

    public void setUserDao(UserDao userDao) {
        this.userDao = userDao;
    }

    public void show() {
        System.out.println(userDao);
    }
}
