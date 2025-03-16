package com.example.web;

import com.example.service.AccountService;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author banana
 * @create 2023-09-16 18:43
 */
@WebServlet(urlPatterns = "/testServlet")
public class TestServlet extends HttpServlet {
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        //从servletContext域中获取spring容器
        ServletContext servletContext = request.getServletContext();
        ApplicationContext app = (ApplicationContext) WebApplicationContextUtils.getWebApplicationContext(servletContext);

        //从spring容器中获取对应的service的bean对象
        AccountService bean = app.getBean(AccountService.class);
        //调用其方法，对数据库进行操作
        bean.select();
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

    }
}
