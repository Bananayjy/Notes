package com.example.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

/**
 * @author banana
 * @create 2023-10-05 0:59
 */
@RestController
public class ExceptionController {

    //运行时异常
    @RequestMapping("/ex1")
    public String exceptionMethod(){
        int i = 1/0;
        return "hello Exception";
    }


    @RequestMapping("/ex2")
    public String exceptionMethod2() throws FileNotFoundException {
        FileInputStream inputStream = new FileInputStream("C:xxx/xxx.xxx");
        return "Hello Exception";
    }

}
