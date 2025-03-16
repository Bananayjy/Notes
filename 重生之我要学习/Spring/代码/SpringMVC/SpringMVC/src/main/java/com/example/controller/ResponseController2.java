package com.example.controller;

import com.example.pojo.User;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @author banana
 * @create 2023-10-02 17:04
 */
@Controller
public class ResponseController2 {

    @GetMapping("ajax/res1")
    @ResponseBody
    public String res1(){
        return "{\"username\":\"yjy\"}";
    }

    @GetMapping("ajax/res2")
    @ResponseBody
    public String res2() throws JsonProcessingException {
        //创建JavaBean
        User user = new User();
        user.setUsername("yjy");
        user.setAge(18);
        //使用jackson转换成json格式的字符串
        String json = new ObjectMapper().writeValueAsString(user);
        return json;
    }

    @GetMapping("ajax/res3")
    @ResponseBody
    public User res3() throws JsonProcessingException {
        //创建JavaBean
        User user = new User();
        user.setUsername("yjy");
        user.setAge(18);
        return user;
    }

}
