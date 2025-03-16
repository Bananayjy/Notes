package com.example.factory;

import com.example.dao.UserDao;
import com.example.dao.impl.UserDaoImpl;
import org.springframework.beans.factory.FactoryBean;

public class MyBeanFactory3 implements FactoryBean {
    public Object getObject() throws Exception {
        return new UserDaoImpl();
    }

    public Class<?> getObjectType() {
        return UserDao.class;
    }
}
