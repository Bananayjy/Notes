package com.example.service.Impl;

import com.example.service.UserService;
import org.springframework.stereotype.Service;

@Service("userService")
public class UserServiceImpl implements UserService {
    public void show1() {
        System.out.println("show1……");
    }

    public void show2() {
        System.out.println("show2……");
    }
}
