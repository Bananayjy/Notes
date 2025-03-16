package com.example;


import com.example.service.AccountService;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class AccountTest {

    public static void main(String[] args) {
        ApplicationContext app = new ClassPathXmlApplicationContext("applicationContext.xml");
        //ApplicationContext app = new AnnotationConfigApplicationContext(SpringConfig.class);
        AccountService accountService = app.getBean(AccountService.class);
        accountService.select();
    }

}
