package com.example.ex;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author banana
 * @create 2023-10-05 11:28
 */
/*@Component*/
public class MyHandlerExceptionResolver implements HandlerExceptionResolver {
    @Override
    public ModelAndView resolveException(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        //简单的响应一个友好的提示页面error1.html
        /*ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("/error1.html");
        */
        //编写要返回的json格式的字符串
        String jsonStr = "{\"code\":0,\"message\":\"error\",\"data\":\"\"}";
        try {
            response.getWriter().write(jsonStr);
        } catch (IOException e1) {
            e1.printStackTrace();
        }

        return null;
    }
}
