package com.example.init;

import com.example.config.SpringConfig;
import com.example.config.SpringMvcConfig;
import org.springframework.web.servlet.support.AbstractAnnotationConfigDispatcherServletInitializer;

/**
 * @author banana
 * @create 2023-10-03 22:13
 */
public class MyAbstractAnnotationConfigDispatcherServletInitializer extends AbstractAnnotationConfigDispatcherServletInitializer {
    //提供Spring容器的核心配置类
    @Override
    protected Class<?>[] getRootConfigClasses() {
        return new Class[]{SpringConfig.class};
    }

    //提供SpringMVC容器的核心配置类
    @Override
    protected Class<?>[] getServletConfigClasses() {
        return new Class[]{SpringMvcConfig.class};
    }

    //提供前端控制器的映射路径
    @Override
    protected String[] getServletMappings() {
        return new String[]{"/"};
    }
}
