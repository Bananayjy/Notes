package com.example.config;

import com.example.interceptors.MyInterceptor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.config.annotation.DefaultServletHandlerConfigurer;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @author banana
 * @create 2023-10-03 11:48
 */
@Component
public class MyWebMvcConfigurer implements WebMvcConfigurer {
    @Override
    public void configureDefaultServletHandling(DefaultServletHandlerConfigurer configurer) {
        //开启DefaultServlet 可以处理静态资源
        configurer.enable();
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        //添加一个拦截器，并配置拦截器的映射路径
        registry.addInterceptor(new MyInterceptor()).addPathPatterns("/**");
    }
}
