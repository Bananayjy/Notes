package com.example.dao.Impl;

import com.example.dao.UserDao;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.print.DocFlavor;

@Component("userDao")
@Controller
public class UserDaoImpl implements UserDao {

    private String name;

    @Value("yjy")
    public void setName(String name){
        this.name = name;
    }

    public void show() {
        System.out.println("name:" + name);
    }
}
