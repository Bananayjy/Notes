package com.example.controller;

import com.example.pojo.Address;
import com.example.pojo.User;
import org.apache.commons.io.IOUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

/**
 * @author banana
 * @create 2023-09-17 19:26
 */

@Controller
public class ParamController {



    @GetMapping("/request1")
    public String request1(HttpServletRequest httpServletRequest){
        //将数据存储到equest域中
        httpServletRequest.setAttribute("username", "yjy");
        //转发到request2
        return "forward:/request2";
    }

    @GetMapping("/request2")
    public String request2(@RequestAttribute("username") String username){
        //打印存储在request中的内容
        System.out.println(username);
        return "/index.jsp";
    }


    @PostMapping("/param10")
    public String param10(@RequestBody MultipartFile myFile) throws IOException {
        System.out.println(myFile);
        //将上传的文件进行保存
        //1.获得上传的文件的流对象
        InputStream inputStream = myFile.getInputStream();
        //2.获得上传文件位置的输出流
        FileOutputStream outputStream = new
                FileOutputStream("C:\\Users\\haohao\\"+myFile.getOriginalFilename());
        //3.使用commons-io 执行文件拷贝
        IOUtils.copy(inputStream,outputStream);
        //4.关闭资源
        inputStream.close();
        outputStream.close();
        return "index.jsp";
    }

    //post请求接受json数据，并封装给对应的javaBean对象
    @PostMapping("/param8")
    public String param8(@RequestBody Address address){
        System.out.println(address);
        return "index.jsp";
    }

    //post请求body数据结接受
    @PostMapping("/param7")
    public String param7(@RequestBody String body){
        System.out.println(body);
        return "index.jsp";
    }

    //JavaBean入参
    @GetMapping("/param6")
    public String param6(User user){
        System.out.println(user);
        return "index.jsp";
    }

    //map入参
    //localhost:8080/项目名称/param5?hoppy=play&hoppy=sleep
    @GetMapping("/param5")
    public String param5(@RequestParam Map<String, String> map){
        map.forEach((k, v) ->{
            System.out.println(k + "==>" + v);
        });
        return "index.jsp";
    }

    //列表入参
    //localhost:8080/项目名称/param4?hoppy=play&hoppy=sleep
    @GetMapping("/param4")
    public String param4(@RequestParam List<String> hoppy){
        for(String hb : hoppy){
            System.out.println("hoppy:" + hb);
        }
        return "index.jsp";
    }

    //数组入参
    //localhost:8080/项目名称/param3?hoppy=play&hoppy=sleep
    @GetMapping("/param3")
    public String param3(String[] hoppy){
        for(String hb : hoppy){
            System.out.println("hoppy:" + hb);
        }
        return "index.jsp";
    }

    //指定接受参数名称
    //localhost:8080/项目名称/param2?username=yjy&age=18
    @GetMapping("/param2")
    public String param2(@RequestParam("username") String name, int age){
        System.out.println(name + "::::" + age);
        return "index.jsp";
    }

    //localhost:8080/项目名称/param1?username=yjy&age=18
    @GetMapping("/param1")
    public String param1(String username, int age){
        System.out.println(username + "::::" + age);
        return "index.jsp";
    }

}
