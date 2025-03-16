package com.example.init;

import com.example.service.QuickService;

import javax.servlet.ServletContainerInitializer;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.HandlesTypes;
import java.util.Set;

/**
 * @author banana
 * @create 2023-10-03 14:49
 */
@HandlesTypes({QuickService.class})
public class MyServletContainerInitializer implements ServletContainerInitializer {
    @Override
    public void onStartup(Set<Class<?>> c, ServletContext ctx) throws ServletException {
        System.out.println("MyServletContainerInitializer running……");
    }
}
