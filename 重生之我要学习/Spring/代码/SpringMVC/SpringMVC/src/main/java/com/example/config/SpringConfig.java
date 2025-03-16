package com.example.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

/**
 * @author banana
 * @create 2023-10-03 22:15
 */
@Configuration
@ComponentScan({"com.example.service"})
public class SpringConfig {
}
