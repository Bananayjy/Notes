package com.example.controller;

import com.example.pojo.User;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

/**
 * @author banana
 * @create 2023-10-02 15:49
 */
@Controller
public class ResponseController {
    //转发
    @RequestMapping("/res1")
    public String res1(){
        return "forward:/res2";
    }

    //重定向
    @RequestMapping("/res2")
    public String res2(){
        return "redirect:/index.jsp";
    }

    //响应模型数据
    @RequestMapping("/res3")
    public ModelAndView res3(ModelAndView modelAndView){
        //准备javaBean模型数据
        User user = new User();
        user.setUsername("yjy");
        user.setAge(18);
        //设置模型
        modelAndView.addObject("user", user);
        //设置视图
        modelAndView.setViewName("/index2.jsp");
        return modelAndView;
    }


    //响应模型数据
    //@ResponseBody:告知此处的返回值不要进行视图处理，是要以响应体的方式处理的
    @RequestMapping("/res4")
    @ResponseBody
    public String res4(){
        return "i am yjy";
    }
}
