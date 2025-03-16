package com.example.beans;

import com.alibaba.druid.pool.DruidDataSource;
import com.example.dao.UserDao;
import com.example.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;

//需要将该类交给spring去管理
//不然找不到该bean  那么里面的工厂方法spring也获取不到
/*@Component*/
public class OtherBean {

    @Bean("dataSource")
    public DataSource dataSource(
            @Value("${jdbc.driver}") String driverClassName,
            @Qualifier("userDao2")UserDao userDao,
            UserService userService
            ){
        DruidDataSource dataSource = new DruidDataSource();
        //打印注入的参数
        System.out.println(driverClassName);
        System.out.println(userDao);
        System.out.println(userService);
        return dataSource;
    }
}
