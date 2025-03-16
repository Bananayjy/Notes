package com.example.ex;

import com.example.pojo.Result;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.ModelAndView;

import java.io.IOException;

/**
 * @author banana
 * @create 2023-10-05 12:08
 */
@ControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(RuntimeException.class)
    public ModelAndView runtimeHandleException(RuntimeException e){
        System.out.println(e);
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("/error1.html");
        return modelAndView;
    }
    @ExceptionHandler(IOException.class)
    @ResponseBody
    public Result ioHandleException(IOException e){
        System.out.println(e);
        Result result = new Result(0, "", "");
        return result;
    }
}
