package com.example.controller;

import com.example.service.QuickService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @author banana
 * @create 2023-09-17 16:11
 */

//交给spring容器去进行管理
@Controller
public class QuickController {

    //直接注入Service进行使用
    @Autowired
    private QuickService quickService;

    //配置映射路径
    @RequestMapping("/show")
    public String show(){
        System.out.println("show……" + quickService);
        return "index.jsp";
    }
}
