# Spring（三）

## IV、SpringMVC部分

### 一、SpringMVC简介

#### 1、SpringMVC概述

SpringMVC是一个基于Spring开发的MVC轻量级框架，Spring3.0后发布的组件，SpringMVC和Spring可以无 缝整合，使用DispatcherServlet作为前端控制器，且内部提供了处理器映射器、处理器适配器、视图解析器等组 件，可以简化JavaBean封装，Json转化、文件上传等操作。

![image-20230822212155645](.\assets\image-20230822212155645.png)

#### 2、SpringMVC快速入门

![image-20230822212311328](.\assets\image-20230822212311328.png)

1.先创建一个新的工厂，然后导入对应的springmvc资源

```xml
<?xml version="1.0" encoding="UTF-8"?>

<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
  xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
  <modelVersion>4.0.0</modelVersion>

  <groupId>org.example</groupId>
  <artifactId>SpringMVC</artifactId>
  <version>1.0-SNAPSHOT</version>
  <packaging>war</packaging>

  <dependencies>
    <dependency>
      <groupId>org.springframework</groupId>
      <artifactId>spring-webmvc</artifactId>
      <version>5.3.7</version>
    </dependency>
  </dependencies>
</project>
```

2.配置前端控制器DispatcherServlet

```xml
<?xml version="1.0" encoding="UTF-8"?>
<web-app xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xmlns="http://java.sun.com/xml/ns/javaee"
         xsi:schemaLocation="http://java.sun.com/xml/ns/javaee http://java.sun.com/xml/ns/javaee/web-app_3_0.xsd"
         id="WebApp_ID" version="3.0">
    <!--配置DispatcherServlet-->
    <!--
    	配置DispatcherServlet
    	引入spring-mvc依赖后可以直接获取该类全限定名称
  	-->
    <!--声明servlet-->
    <servlet>
        <!--servlet名称-->
        <servlet-name>DispatcherServlet</servlet-name>
        <!--servlet对应的类的全限定名（这里由spring-webmvc提供）-->
        <servlet-class>org.springframework.web.servlet.DispatcherServlet</servlet-class>
    </servlet>
    <!--将servlet和url进行绑定-->
    <!--
    	配置映射地址
    	这里默认所有的请求都经过它，然后由它去分发到对应的控制器中
 	 -->
    <servlet-mapping>
        <servlet-name>DispatcherServlet</servlet-name>
        <url-pattern>/</url-pattern>
    </servlet-mapping>

</web-app>
```



3.编写controller，配置映射路径

```java
package com.example.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @author banana
 * @create 2023-09-17 16:11
 */

//交给spring容器去进行管理
@Controller
public class QuickController {

    //配置映射路径
    @RequestMapping("/show")
    public void show(){
        System.out.println("show……");
    }
}

```



由于我们配置了controller，因此我们需要进行组件扫描，我们再resources下创建一个spring-mvc的xml配置文件spring-mvc.xml

![image-20230917161444411](Spring%EF%BC%88%E4%B8%89%EF%BC%89.assets/image-20230917161444411.png)

```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xmlns:context="http://www.springframework.org/schema/context"
       xsi:schemaLocation="
       http://www.springframework.org/schema/beans
       http://www.springframework.org/schema/beans/spring-beans.xsd
        http://www.springframework.org/schema/context
       http://www.springframework.org/schema/context/spring-context.xsd
       ">
    <!--组件扫描-->
    <context:component-scan base-package="com.example.controller"/>

</beans>
```

那么在哪里去读取该spring配置文件呢？

我们需要在DispatcherServlet前端控制器中的初始化参数加入该spring-mvc.xml文件的配置信息，因为前端控制器是由web容器（tomcat）进行创建，在创建的时候，我们就可以像之前的spring集成web的listen一样，给他指定一个初始化参数（spring配置文件），在创建这个前端控制器的时候，就会帮我们去加载spring-mvc.xml配置文件

```xml
<web-app xmlns="http://xmlns.jcp.org/xml/ns/javaee"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://xmlns.jcp.org/xml/ns/javaee http://xmlns.jcp.org/xml/ns/javaee/web-app_4_0.xsd"
         version="4.0">
  <!--
    配置DispatcherServlet
    引入spring-mvc依赖后可以直接获取该类全限定名称
  -->
  <servlet>
    <servlet-name>DispatcherServlet</servlet-name>
    <servlet-class>org.springframework.web.servlet.DispatcherServlet</servlet-class>
    <!--
              指定springMVC配置文件的位置
              通过getServletConfig().getInitParameter("initParam")的方式获取;
          -->
    <init-param>
      <param-name>contextConfigLocation</param-name>
      <param-value>classpath:spring-mvc.xml</param-value>
    </init-param>
    <!--服务器启动就创建-->
    <load-on-startup>2</load-on-startup>
  </servlet>
  <!--
    配置映射地址
    这里默认所有的请求都经过它，然后由它去分发到对应的控制器中
  -->
  <servlet-mapping>
    <servlet-name>DispatcherServlet</servlet-name>
    <url-pattern>/</url-pattern>
  </servlet-mapping>
</web-app>

```

测试：

配置本地tomcat，将当前项目部署当web服务器tomcat上

![image-20230917161940181](Spring%EF%BC%88%E4%B8%89%EF%BC%89.assets/image-20230917161940181.png)

启动tomcat服务器，然后访问对应路径

![image-20230917162111766](Spring%EF%BC%88%E4%B8%89%EF%BC%89.assets/image-20230917162111766.png)

查看控制台，可以看到打印了对应的信息，说明已经访问到了

![image-20230917162135026](Spring%EF%BC%88%E4%B8%89%EF%BC%89.assets/image-20230917162135026.png)



从上面可以看到，根据我们输的地址，可以找到对应的方法并执行，但为什么会报500错误呢？

因为默认情况下，一个方法应该要返回视图的名字，进行一个视图展示（可以参考javaweb中的jsp）

我们可以改一下这个方法

先在webapp下创建一个jsp文件

![image-20230917162805070](Spring%EF%BC%88%E4%B8%89%EF%BC%89.assets/image-20230917162805070.png)

```jsp
<html>
<body>
<h2>Hello World!</h2>
</body>
    <h1>hello!</h1>
</html>

```

重写方法，跳转到对应的视图

```java
package com.example.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @author banana
 * @create 2023-09-17 16:11
 */

//交给spring容器去进行管理
@Controller
public class QuickController {

    //配置映射路径
    @RequestMapping("/show")
    public String show(){
        System.out.println("show……");
        return "index.jsp";
    }
}

```

启动web服务器tomcat，然后进行访问，可以发现成功展示实体

![image-20230917163103282](Spring%EF%BC%88%E4%B8%89%EF%BC%89.assets/image-20230917163103282.png)

#### 3、Controller中访问容器中的Bean

我们要在我们刚才写的controller中取访问spring容器中的bean

首先我们要导入spring的依赖，由于我们之前已经导入了spring-webmvc的依赖，其中就包含了spring的相关依赖

![image-20230917163358116](Spring%EF%BC%88%E4%B8%89%EF%BC%89.assets/image-20230917163358116.png)

我们创建一个service下的内容，作为需要访问的spring容器中的Bean

![image-20230917163926666](Spring%EF%BC%88%E4%B8%89%EF%BC%89.assets/image-20230917163926666.png)

service接口：

```java
package com.example.service;

/**
 * @author banana
 * @create 2023-09-17 16:39
 */
public interface QuickService {
}

```

service实现类：

```java
package com.example.service.Impl;

import com.example.service.QuickService;
import org.springframework.stereotype.Service;

/**
 * @author banana
 * @create 2023-09-17 16:39
 */
@Service
public class QuickServiceImpl implements QuickService {
}

```

在resources下创建一个配置文件applicationContext.xml,用来进行组件扫描，扫描service

![image-20230917164100681](Spring%EF%BC%88%E4%B8%89%EF%BC%89.assets/image-20230917164100681.png)

```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xmlns:context="http://www.springframework.org/schema/context"
       xsi:schemaLocation="
       http://www.springframework.org/schema/beans
       http://www.springframework.org/schema/beans/spring-beans.xsd
        http://www.springframework.org/schema/context
       http://www.springframework.org/schema/context/spring-context.xsd
       ">
    <!--组件扫描-->
    <context:component-scan base-package="com.example.service"/>

</beans>
```

利用之前学的spring集成web环境，在web.xml中去加载该配置文件

```xml
<web-app xmlns="http://xmlns.jcp.org/xml/ns/javaee"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://xmlns.jcp.org/xml/ns/javaee http://xmlns.jcp.org/xml/ns/javaee/web-app_4_0.xsd"
         version="4.0">

  <!-- 配置ContextLoaderListener-->
  <context-param>
    <param-name>contextConfigLocation</param-name>
    <param-value>classpath:applicationContext.xml</param-value>
  </context-param>
  <listener>
    <listener-class>org.springframework.web.context.ContextLoaderListener</listener-class>
  </listener>

  <!--
    配置DispatcherServlet
    引入spring-mvc依赖后可以直接获取该类全限定名称
  -->
  <servlet>
    <servlet-name>DispatcherServlet</servlet-name>
    <servlet-class>org.springframework.web.servlet.DispatcherServlet</servlet-class>
    <!--
              指定springMVC配置文件的位置
              通过getServletConfig().getInitParameter("initParam")的方式获取;
          -->
    <init-param>
      <param-name>contextConfigLocation</param-name>
      <param-value>classpath:spring-mvc.xml</param-value>
    </init-param>
    <!--服务器启动就创建-->
    <load-on-startup>2</load-on-startup>
  </servlet>
  <!--
    配置映射地址
    这里默认所有的请求都经过它，然后由它去分发到对应的控制器中
  -->
  <servlet-mapping>
    <servlet-name>DispatcherServlet</servlet-name>
    <url-pattern>/</url-pattern>
  </servlet-mapping>
</web-app>

```

在controller中直接注入servie

```java
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

```

访问对应的网页地址

![image-20230917164751983](Spring%EF%BC%88%E4%B8%89%EF%BC%89.assets/image-20230917164751983.png)

可以在控制台中看到输出内容中已经携带了service的地址，即注入servie成功，成功从spring容器中获得了service

![image-20230917164739496](Spring%EF%BC%88%E4%B8%89%EF%BC%89.assets/image-20230917164739496.png)





#### 4、SpringMVC关键组件浅析

上面已经完成的快速入门的操作，也在不知不觉中完成的Spring和SpringMVC的整合，我们只需要按照规则去定

义Controller和业务方法就可以。但是在这个过程中，肯定是很多核心功能类参与到其中，这些核心功能类，一

般称为组件。当请求到达服务器时，是哪个组件接收的请求，是哪个组件帮我们找到的Controller，是哪个组件

帮我们调用的方法，又是哪个组件最终解析的视图？

![image-20230917171521742](Spring%EF%BC%88%E4%B8%89%EF%BC%89.assets/image-20230917171521742.png)



三个重要组件的关系：

![image-20230917171540868](Spring%EF%BC%88%E4%B8%89%EF%BC%89.assets/image-20230917171540868.png)



SpringMVC的默认组件，SpringMVC 在前端控制器 DispatcherServlet加载时，就会进行初始化操作，在进行初始

化时，就会加载SpringMVC默认指定的一些组件，这些默认组件配置在 DispatcherServlet.properties 文件中，该文

件存在与spring-webmvc-5.3.7.jar包下的 org\springframework\web\servlet\DispatcherServlet.properties

![image-20230917172334216](Spring%EF%BC%88%E4%B8%89%EF%BC%89.assets/image-20230917172334216.png)



![image-20230917172405953](Spring%EF%BC%88%E4%B8%89%EF%BC%89.assets/image-20230917172405953.png)



这些默认的组件是在DispatcherServlet中进行初始化加载的，在DispatcherServlet中存在集合存储着这些组件，

SpringMVC的默认组件会在 DispatcherServlet 中进行维护，但是并没有存储在与SpringMVC的容器中

![image-20230917172546353](Spring%EF%BC%88%E4%B8%89%EF%BC%89.assets/image-20230917172546353.png)

```java
public class DispatcherServlet extends FrameworkServlet {
     //存储处理器映射器
     private List<HandlerMapping> handlerMappings;
     //存储处理器适配器
     private List<HandlerAdapter> handlerAdapters;
     //存储视图解析器
     private List<ViewResolver> viewResolvers;
     // ... 省略其他代码 ...
}
```



配置组件代替默认组件，如果不想使用默认组件，可以将替代方案使用Spring Bean的方式进行配置，例如，在

spring-mvc.xml中配置RequestMappingHandlerMapping

```xml
<bean 
class="org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping"/>
```

当我们在Spring容器中配置了HandlerMapping，则就不会在加载默认的HandlerMapping策略了，原理比较简单，

DispatcherServlet 在进行HandlerMapping初始化时，先从SpringMVC容器中找是否存在HandlerMapping，如果

存在直接取出容器中的HandlerMapping，在存储到 DispatcherServlet 中的handlerMappings集合中去。

### 二、SpringMVC的请求处理

#### 1、请求映射路径的配置

配置映射路径，映射器处理器才能找到Controller的方法资源，目前主流映射路径配置方式就@RequestMapping

![image-20230917191023241](Spring%EF%BC%88%E4%B8%89%EF%BC%89.assets/image-20230917191023241.png)

@GetMapping和@PostMapping差不多是@RequestMapping的一个细分。使用这两个，就可以省去@RequestMapping中对于method属性的配置。



@RequestMapping（有新增的内容）：

```java
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Mapping
public @interface RequestMapping {

	/**
	 * Assign a name to this mapping.
	 * <p><b>Supported at the type level as well as at the method level!</b>
	 * When used on both levels, a combined name is derived by concatenation
	 * with "#" as separator.
	 * @see org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder
	 * @see org.springframework.web.servlet.handler.HandlerMethodMappingNamingStrategy
	 */
	String name() default "";

	/**
	 * The primary mapping expressed by this annotation.
	 * <p>This is an alias for {@link #path}. For example,
	 * {@code @RequestMapping("/foo")} is equivalent to
	 * {@code @RequestMapping(path="/foo")}.
	 * <p><b>Supported at the type level as well as at the method level!</b>
	 * When used at the type level, all method-level mappings inherit
	 * this primary mapping, narrowing it for a specific handler method.
	 * <p><strong>NOTE</strong>: A handler method that is not mapped to any path
	 * explicitly is effectively mapped to an empty path.
	 */
	@AliasFor("path")
	String[] value() default {};

	/**
	 * The path mapping URIs (e.g. {@code "/profile"}).
	 * <p>Ant-style path patterns are also supported (e.g. {@code "/profile/**"}).
	 * At the method level, relative paths (e.g. {@code "edit"}) are supported
	 * within the primary mapping expressed at the type level.
	 * Path mapping URIs may contain placeholders (e.g. <code>"/${profile_path}"</code>).
	 * <p><b>Supported at the type level as well as at the method level!</b>
	 * When used at the type level, all method-level mappings inherit
	 * this primary mapping, narrowing it for a specific handler method.
	 * <p><strong>NOTE</strong>: A handler method that is not mapped to any path
	 * explicitly is effectively mapped to an empty path.
	 * @since 4.2
	 */
	@AliasFor("value")
	String[] path() default {};

	/**
	 * The HTTP request methods to map to, narrowing the primary mapping:
	 * GET, POST, HEAD, OPTIONS, PUT, PATCH, DELETE, TRACE.
	 * <p><b>Supported at the type level as well as at the method level!</b>
	 * When used at the type level, all method-level mappings inherit this
	 * HTTP method restriction.
	 */
	RequestMethod[] method() default {};

	/**
	 * The parameters of the mapped request, narrowing the primary mapping.
	 * <p>Same format for any environment: a sequence of "myParam=myValue" style
	 * expressions, with a request only mapped if each such parameter is found
	 * to have the given value. Expressions can be negated by using the "!=" operator,
	 * as in "myParam!=myValue". "myParam" style expressions are also supported,
	 * with such parameters having to be present in the request (allowed to have
	 * any value). Finally, "!myParam" style expressions indicate that the
	 * specified parameter is <i>not</i> supposed to be present in the request.
	 * <p><b>Supported at the type level as well as at the method level!</b>
	 * When used at the type level, all method-level mappings inherit this
	 * parameter restriction.
	 */
	String[] params() default {};

	/**
	 * The headers of the mapped request, narrowing the primary mapping.
	 * <p>Same format for any environment: a sequence of "My-Header=myValue" style
	 * expressions, with a request only mapped if each such header is found
	 * to have the given value. Expressions can be negated by using the "!=" operator,
	 * as in "My-Header!=myValue". "My-Header" style expressions are also supported,
	 * with such headers having to be present in the request (allowed to have
	 * any value). Finally, "!My-Header" style expressions indicate that the
	 * specified header is <i>not</i> supposed to be present in the request.
	 * <p>Also supports media type wildcards (*), for headers such as Accept
	 * and Content-Type. For instance,
	 * <pre class="code">
	 * &#064;RequestMapping(value = "/something", headers = "content-type=text/*")
	 * </pre>
	 * will match requests with a Content-Type of "text/html", "text/plain", etc.
	 * <p><b>Supported at the type level as well as at the method level!</b>
	 * When used at the type level, all method-level mappings inherit this
	 * header restriction.
	 * @see org.springframework.http.MediaType
	 */
	String[] headers() default {};

	/**
	 * Narrows the primary mapping by media types that can be consumed by the
	 * mapped handler. Consists of one or more media types one of which must
	 * match to the request {@code Content-Type} header. Examples:
	 * <pre class="code">
	 * consumes = "text/plain"
	 * consumes = {"text/plain", "application/*"}
	 * consumes = MediaType.TEXT_PLAIN_VALUE
	 * </pre>
	 * Expressions can be negated by using the "!" operator, as in
	 * "!text/plain", which matches all requests with a {@code Content-Type}
	 * other than "text/plain".
	 * <p><b>Supported at the type level as well as at the method level!</b>
	 * If specified at both levels, the method level consumes condition overrides
	 * the type level condition.
	 * @see org.springframework.http.MediaType
	 * @see javax.servlet.http.HttpServletRequest#getContentType()
	 */
	String[] consumes() default {};

	/**
	 * Narrows the primary mapping by media types that can be produced by the
	 * mapped handler. Consists of one or more media types one of which must
	 * be chosen via content negotiation against the "acceptable" media types
	 * of the request. Typically those are extracted from the {@code "Accept"}
	 * header but may be derived from query parameters, or other. Examples:
	 * <pre class="code">
	 * produces = "text/plain"
	 * produces = {"text/plain", "application/*"}
	 * produces = MediaType.TEXT_PLAIN_VALUE
	 * produces = "text/plain;charset=UTF-8"
	 * </pre>
	 * <p>If a declared media type contains a parameter (e.g. "charset=UTF-8",
	 * "type=feed", "type=entry") and if a compatible media type from the request
	 * has that parameter too, then the parameter values must match. Otherwise
	 * if the media type from the request does not contain the parameter, it is
	 * assumed the client accepts any value.
	 * <p>Expressions can be negated by using the "!" operator, as in "!text/plain",
	 * which matches all requests with a {@code Accept} other than "text/plain".
	 * <p><b>Supported at the type level as well as at the method level!</b>
	 * If specified at both levels, the method level produces condition overrides
	 * the type level condition.
	 * @see org.springframework.http.MediaType
	 */
	String[] produces() default {};

}

```



@RequestMapping注解，主要使用在控制器的方法上，用于标识客户端访问资源路径，常用的属性有value、path、method、headers、params等。当@RequestMapping只有一个访问路径需要指定时，使用value属性、path属性或省略value和path，当有多个属性时，value和path不能省略

```java
@RequestMapping(value = "/show")//使用value属性指定一个访问路径
public String show(){}
@RequestMapping(value = {"/show","/haohao","/abc"})//使用value属性指定多个访问路径
public String show(){}
@RequestMapping(path = "/show")//使用path属性指定一个访问路径
public String show(){}
@RequestMapping(path = {"/show","/haohao","/abc"})//使用path属性指定多个访问路径
public String show(){}
@RequestMapping("/show")//如果只设置访问路径时，value和path可以省略
public String show(){}
@RequestMapping({"/show","/haohao","/abc"})
public String show(){}
```



当@RequestMapping 需要限定访问方式时，可以通过method属性设置

```java
//请求地址是/show,且请求方式必须是POST才能匹配成功
@RequestMapping(value = "/show",method = RequestMethod.POST)
public String show(){}
```

method的属性值是一个枚举类型，源码如下：

```java
public enum RequestMethod {
     GET,
     HEAD,
     POST,
     PUT,
     PATCH,
     DELETE,
     OPTIONS,
     TRACE;
     private RequestMethod() {
     }
}
```



@GetMapping，当请求方式是GET时，我们可以使用@GetMapping替代@RequestMapping

```java
@GetMapping("/show")
public String show(){}
```

@PostMapping，当请求方式是POST时，我们可以使用@PostMapping替代@RequestMapping

```java
@PostMapping("/show")
public String show(){}
```



@RequestMapping 在类上使用，@RequestMapping 、@GetMapping、@PostMapping还可以使用在

Controller类上，使用在类上后，该类所有方法都公用该@RequestMapping设置的属性，访问路径则为类上的映射

地址+方法上的映射地址，例如：

```java
@Controller
@RequestMapping("/xxx")
public class UserController implements ApplicationContextAware, ServletContextAware {
     @GetMapping("/aaa")
     public ModelAndView aaa(HttpServletResponse response) throws IOException, 
    ModelAndViewDefiningException {
     return null;
     }
}
```

此时的访问路径为：/xxx/aaa



#### 2、请求数据的接受

**1.接受普通数据（Get请求发送的键值对形式）**

当客户端提交的数据是普通的键值对形式，直接使用同名形参接受即可，如下传参：

```
username=yjy&age=18
```

我们创建对应的controller控制器进行测试

控制器；

```java
package com.example.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * @author banana
 * @create 2023-09-17 19:26
 */

@Controller
public class ParamController {

    //localhost:8080/项目名称/param1?username=yjy&age=18
    @GetMapping("/param1")
    public String param1(String username, int age){
        System.out.println(username + "::::" + age);
        return "index.jsp";
    }

}

```

请求：

![image-20230917192924968](Spring%EF%BC%88%E4%B8%89%EF%BC%89.assets/image-20230917192924968.png)

控制器打印结果：

![image-20230917192934923](Spring%EF%BC%88%E4%B8%89%EF%BC%89.assets/image-20230917192934923.png)



**扩展：**

（1）如果我们想把请求的username给名字为name的形参，可以通过@Requestparm来实现参数名称的映射

```java
//localhost:8080/项目名称/param2?username=yjy&age=18
@GetMapping("/param2")
public String param2(@RequestParam("username") String name, int age){
    System.out.println(name + "::::" + age);
    return "index.jsp";
}
```

```
http://localhost:8080/SpringMVC_war/param2?username=yjy&age=18
```

![image-20230917194522799](Spring%EF%BC%88%E4%B8%89%EF%BC%89.assets/image-20230917194522799.png)

@RequestParam的相关属性：

```java
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequestParam {

	/**映射名称
	 * Alias for {@link #name}.
	 */
	@AliasFor("name")
	String value() default "";

	/** 同value
	 * The name of the request parameter to bind to.
	 * @since 4.2
	 */
	@AliasFor("value")
	String name() default "";

	/** 是否必须传值
	 * Whether the parameter is required.
	 * <p>Defaults to {@code true}, leading to an exception being thrown
	 * if the parameter is missing in the request. Switch this to
	 * {@code false} if you prefer a {@code null} value if the parameter is
	 * not present in the request.
	 * <p>Alternatively, provide a {@link #defaultValue}, which implicitly
	 * sets this flag to {@code false}.
	 */
	boolean required() default true;
 
	/** 默认值
	 * The default value to use as a fallback when the request parameter is
	 * not provided or has an empty value.
	 * <p>Supplying a default value implicitly sets {@link #required} to
	 * {@code false}.
	 */
	String defaultValue() default ValueConstants.DEFAULT_NONE;

}

```

注：并且注意，入参竟可能是包装类如Interger，如果是用int的话，并且客户端没有传值，会报错，因为无法把null赋值给基本数据类型int。



（2）如果我们想接受数组的入参

```java
//数组入参
//localhost:8080/项目名称/param3?hoppy=play&hoppy=sleep
@GetMapping("/param3")
public String param3(String[] hoppy){
    for(String hb : hoppy){
        System.out.println("hoppy:" + hb);
    }
    return "index.jsp";
}
```

```
http://localhost:8080/SpringMVC_war/param3?hoppy=play&hoppy=sleep
```

![image-20230917195011646](Spring%EF%BC%88%E4%B8%89%EF%BC%89.assets/image-20230917195011646.png)



（3）如果我们想接受列表的入参

```java
//列表入参
//localhost:8080/项目名称/param4?hoppy=play&hoppy=sleep
@GetMapping("/param4")
public String param4(@RequestParam List<String> hoppy){
    for(String hb : hoppy){
        System.out.println("hoppy:" + hb);
    }
    return "index.jsp";
}
```

```
http://localhost:8080/SpringMVC_war/param4?hoppy=play&hoppy=sleep
```

![image-20230917194954872](Spring%EF%BC%88%E4%B8%89%EF%BC%89.assets/image-20230917194954872.png)

（4）如果我们想接受map的入参

```java
//map入参
//localhost:8080/项目名称/param5?hoppy=play&hoppy=sleep
@GetMapping("/param5")
public String param5(@RequestParam Map<String, String> map){
    map.forEach((k, v) ->{
        System.out.println(k + "==>" + v);
    });
    return "index.jsp";
}
```

```
http://localhost:8080/SpringMVC_war/param5?username=yjy&age=18
```

![image-20230917194935436](Spring%EF%BC%88%E4%B8%89%EF%BC%89.assets/image-20230917194935436.png)



（5）接收普通请求数据，当请求参数有特殊格式数据，如日期时

```java
@GetMapping("/show")
public String show(String username,int age,Date birthday){
 System.out.println(username+"=="+age+"=="+birthday);
 return "/index.jsp";
}
```

入参：

```
username=haohao&age=35&birthday=1986/01/01
```

Date可以正常接收，因为Spring内置的类型解析器，可以识别的日期格式是 yyyy/MM/dd，但是如果我们提交其他格式，例如：yyyy-MM-dd 时，类型转换会报错，如下：

解决方案，使用@DateTimeFormat 指定日期格式，修改UserController如下：

```java
@GetMapping("/show")
public String show(String username,int age,@DateTimeFormat(pattern = "yyyy-MM-dd") Date 
birthday){
     System.out.println(username+"=="+age+"=="+birthday);
     return "/index.jsp";
}
```

**问题：**

（1）为什么使用List、Map这种集合作为入参的时候需要加@RequstParam注解

如我们现在入参是List，springmvc会尝试将其创建成对象，然后再进行数据封装，但是List是接口，没办法创建对象，但我这里是向springmvc将参数封装到List中，因此通过加@RequestParam注解，告诉springmvc，这里不需要创建对象，是用其作为一个容器，接受客户端的请求参数/数据，直接放到其中即可（具体如何操作没了解）。





**2.接受实体JavaBean属性数据（Get请求）**

我们先创建两个实体类（/pojo）User和address，在User中放address类作为成员变量

User：

```java
package com.example.pojo;

import java.util.Arrays;
import java.util.Date;

/**
 * @author banana
 * @create 2023-09-17 20:11
 */
public class User {

    private String username;
    private Integer age;
    private String[] hobbies;
    private Date birthday;
    private Address address;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public String[] getHobbies() {
        return hobbies;
    }

    public void setHobbies(String[] hobbies) {
        this.hobbies = hobbies;
    }

    public Date getBirthday() {
        return birthday;
    }

    public void setBirthday(Date birthday) {
        this.birthday = birthday;
    }

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    @Override
    public String toString() {
        return "User{" +
                "username='" + username + '\'' +
                ", age=" + age +
                ", hobbies=" + Arrays.toString(hobbies) +
                ", birthday=" + birthday +
                ", address=" + address +
                '}';
    }
}

```

address：

```java
package com.example.pojo;

/**
 * @author banana
 * @create 2023-09-17 20:11
 */
public class Address {

    private String city;
    private String area;

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getArea() {
        return area;
    }

    public void setArea(String area) {
        this.area = area;
    }

    @Override
    public String toString() {
        return "Address{" +
                "city='" + city + '\'' +
                ", area='" + area + '\'' +
                '}';
    }
}

```

编写控制类：

```java
@Controller
public class ParamController {
    
    //JavaBean入参
    @GetMapping("/param6")
    public String param6(User user){
        System.out.println(user);
        return "index.jsp";
    }
}

```

入参：

```
http://localhost:8080/SpringMVC_war/param6?username=yjy&age=18&hobbies=play&hobbies=sing&birthday=2002/08/28&address.city=zhoushan&address.area=wudashan
```

控制台输出：

![image-20230917201856547](Spring%EF%BC%88%E4%B8%89%EF%BC%89.assets/image-20230917201856547.png)





**3.接受请求体的数据（Post请求）**

我们通过@RequestBody注解来接受请求体（post请求，放在body中的数据）的数据

通过postman进行请求的发送，然后接受

控制器：

```java
 //post请求body数据结接受
@PostMapping("/param7")
public String param7(@RequestBody String body){
    System.out.println(body);
    return "index.jsp";
}
```

客户端请求：

![image-20230917202749407](Spring%EF%BC%88%E4%B8%89%EF%BC%89.assets/image-20230917202749407.png)

控制台输出：

![image-20230917202808483](Spring%EF%BC%88%E4%B8%89%EF%BC%89.assets/image-20230917202808483.png)





我们从上面可以看出，通过@RequestBody注解能够接受到客户端发送的请求中的body中的数据，但是我们通过string类型去接受json格式的数据并不好后期的处理，因此我们都是通过javabean对象去进行接受的

使用Json工具（ jackson ）将Json格式的字符串转化为JavaBean进行操作

```xml
<dependency>
     <groupId>com.fasterxml.jackson.core</groupId>
     <artifactId>jackson-databind</artifactId>
     <version>2.9.0</version>
</dependency>
```

```java
@PostMapping("/show")
public String show(@RequestBody String body) throws IOException {
     System.out.println(body);
     //获得ObjectMapper
     ObjectMapper objectMapper = new ObjectMapper();
     //将json格式字符串转化成指定的User
     User user = objectMapper.readValue(body, User.class);
     System.out.println(user);
     return "/index.jsp";
}
```

但是这样还是有缺点的，我们在接受到json格式的数据后，都需要手动去进行处理，我们可以通过如下方法进行解决：

配置RequestMappingHandlerAdapter，指定消息转换器（配置后，覆盖原先默认的，即默认的不会生成），就不用手动转换json格式字符串了

```xml
<!--配置HandlerAdapter-->
    <bean class="org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter">
        <property name="messageConverters">
            <list>
                <bean class="org.springframework.http.converter.json.MappingJackson2HttpMessageConverter"/>
            </list>
        </property>
    </bean>
```

控制器：

```java
//post请求接受json数据，并封装给对应的javaBean对象
@PostMapping("/param8")
public String param8(@RequestBody Address address){
    System.out.println(address);
    return "index.jsp";
}
```

输入：

![image-20230917204530588](Spring%EF%BC%88%E4%B8%89%EF%BC%89.assets/image-20230917204530588.png)

控制器输出：

![image-20230917204546290](Spring%EF%BC%88%E4%B8%89%EF%BC%89.assets/image-20230917204546290.png)

我们可以把消息转换器看做是我们之前写的那一段逻辑，现在直接由springmvc帮我们进行处理了

```java
//获得ObjectMapper
ObjectMapper objectMapper = new ObjectMapper();
//将json格式字符串转化成指定的User
User user = objectMapper.readValue(body, User.class);
```





**4.RestFul风格**

接收Restful风格数据

什么是Rest风格？

Rest（Representational State Transfer）表象化状态转变（表述性状态转变），在2000年被提出，基于HTTP、URI、xml、JSON等标准和协议，支持轻量级、跨平台、跨语言的架构设计。是Web服务的一种新网络应用程序的设计风格和开发方式。



接收Restful风格数据

Restful风格的请求，常见的规则有如下三点：

- 用URI表示某个模块资源，资源名称为名词；

![image-20230917205919632](Spring%EF%BC%88%E4%B8%89%EF%BC%89.assets/image-20230917205919632.png)

- 用请求方式表示模块具体业务动作，例如：GET表示查询、POST表示插入、PUT表示更新、DELETE表示删除

![image-20230917205952710](Spring%EF%BC%88%E4%B8%89%EF%BC%89.assets/image-20230917205952710.png)

- 用HTTP响应状态码表示结果，国内常用的响应包括三部分：状态码、状态信息、响应数据

![image-20230917210008399](Spring%EF%BC%88%E4%B8%89%EF%BC%89.assets/image-20230917210008399.png)



（1）接收Restful风格数据，Restful请求数据一般会在URL地址上携带，可以使用注解 @PathVariable(占位符参数名称)

```
http://localhost/user/100
```

```java
@PostMapping("/user/{id}")
public String findUserById(@PathVariable("id") Integer id){
     System.out.println(id);
     return "/index.jsp";
}
```

（2）请求URL资源地址包含多个参数情况

```
http://localhost/user/haohao/18
```

```java
@PostMapping("/user/{username}/{age}")
public String findUserByUsernameAndAge(@PathVariable("username") String 
username,@PathVariable("age") Integer age){
     System.out.println(username+"=="+age);
     return "/index.jsp";
}
```



**5.文件上传**

**场景：**客户端提交文件给服务端，服务端将该文件进行存储

**接受文件上传的数据，文件上传的表单需要一定的要求，如下：**

- 表单的提交方式必须是Post
- 表单的enctype属性必须是multipart/form-data（多表单方式）
- 文件上传项需要name属性

**具体示例：**

编写对应的前端控制器：

```java
@PostMapping("/param10")
public String param10(@RequestBody MultipartFile myFile){
    System.out.println(myFile);
    return "index.jsp";
}
```

通过postman编写测试样例，模拟前端提交表单，上传文件

![image-20231001141339452](Spring%EF%BC%88%E4%B8%89%EF%BC%89.assets/image-20231001141339452.png)

结果500报错，提示么没有提供multi-part配置，无法处理，因为springmvc默认情况下，没有开启文件上传的解析器，需要人为配置

![image-20231001141412896](Spring%EF%BC%88%E4%B8%89%EF%BC%89.assets/image-20231001141412896.png)



配置文件上传解析器

在服务器端，由于映射器适配器需要文件上传解析器，而该解析器未被注册，所以需要手动注册

在spring-mvc.xml配置文件中配置一个bean对象作为文件上传解析器到spring容器中，并且该bean的名称必须为multipartResolver，因为其底层是直接通过该名称multipartResolver去进行获取CommonsMultipartResolver的

```java
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xmlns:context="http://www.springframework.org/schema/context"
       xsi:schemaLocation="
       http://www.springframework.org/schema/beans
       http://www.springframework.org/schema/beans/spring-beans.xsd
        http://www.springframework.org/schema/context
       http://www.springframework.org/schema/mvc/spring-context.xsd
       ">
    <!--组件扫描-->
    <context:component-scan base-package="com.example.controller"/>

    <!--配置HandlerAdapter 处理将Json格式的字符串转化为JavaBean-->
    <bean class="org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter">
        <property name="messageConverters">
            <list>
                <bean class="org.springframework.http.converter.json.MappingJackson2HttpMessageConverter"/>
            </list>
        </property>
    </bean>
    
    <!--配置文件上传解析器，注意：id的名字是固定写法-->
    <bean id="multipartResolver"
          class="org.springframework.web.multipart.commons.CommonsMultipartResolver">
        <!-- 一些配置的参数，可以不写，使用默认值-->
        <property name="defaultEncoding" value="UTF-8"/><!--文件的编码格式 默认是ISO8859-1-->
        <property name="maxUploadSizePerFile" value="1048576"/><!--上传的每个文件限制的大小 单位字节-->
        <property name="maxUploadSize" value="3145728"/><!--上传文件的总大小 默认单位：字节-->
        <property name="maxInMemorySize" value="1048576"/><!--上传文件的缓存大小-->
    </bean>
</beans>
```

而CommonsMultipartResolver底层使用的Apache的是Common-fileuplad等工具API进行的文件上传

因此我们需要导入对应的依赖

```xml
<dependency>
    <groupId>commons-fileupload</groupId>
    <artifactId>commons-fileupload</artifactId>
    <version>1.4</version>
</dependency>
```

或

```xml
<dependency>
     <groupId>commons-io</groupId>
     <artifactId>commons-io</artifactId>
     <version>2.7</version>
</dependency>
```

看一下上传文件的结果

成功返回了指定了jsp页面

![image-20231001144035357](Spring%EF%BC%88%E4%B8%89%EF%BC%89.assets/image-20231001144035357.png)

在控制台中成功打印了上传文件的输出内容

![image-20231001144108635](Spring%EF%BC%88%E4%B8%89%EF%BC%89.assets/image-20231001144108635.png)



接下去我们要将获取的文件进行保存到本地

```java
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
```

如果进行多文件上传的话，则使用MultipartFile数组即可

```java
public String param10(@RequestBody MultipartFile[] myFiles) throws IOException{
……
}
```



**6.获取请求头中的数据**

可以看到请求头中有各种各样的键值对形式的数据，那么我们如何取进行获取呢

![image-20231001150901992](Spring%EF%BC%88%E4%B8%89%EF%BC%89.assets/image-20231001150901992.png)

我们可以通过@RequestHeader来讲控制器中的入参接受请求头的内容

接受Http请求头数据，接受指定名称的请求头：

```java
@GetMapping("/headers")
public String headers(@RequestHeader("Accept-Encoding") String acceptEncoding){
     System.out.println("Accept-Encoding:"+acceptEncoding);
     return "/index.jsp";
}
```

接受所有的请求头信息：

```java
@GetMapping("/headersMap")
public String headersMap(@RequestHeader Map<String,String> map){
     map.forEach((k,v)->{
     System.out.println(k+":"+v);
     });
     return "/index.jsp";
}
```

获得客户端携带的Cookie数据

```java
@GetMapping("/cookies")
public String cookies(@CookieValue(value = "JSESSIONID",defaultValue = "") String jsessionid){
    System.out.println(jsessionid);
    return "/index.jsp";
}
```



**7.获得转发Request域中的数据**

我们在控制器中加入形参HttpServletRequest request，那么Springmvc就会帮我们将Request与赋值给该形参，并且我们需要添加对应的servlet依赖：

```java
<dependency>
    <groupId>javax.servlet</groupId>
    <artifactId>javax.servlet-api</artifactId>
    <version>4.0.1</version>
</dependency>
```

编写对应的代码

```java
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
```

访问对应的url

```
http://localhost:8080/SpringMVC_war/request1
```

可以查看到控制台中打印出了在request1路由中保存到request域中的信息

![image-20231001152609719](Spring%EF%BC%88%E4%B8%89%EF%BC%89.assets/image-20231001152609719.png)



**8.请求数据接受时候乱码解决方案**

请求参数乱码的解决方案，Spring已经提供好的CharacterEncodingFilter来进行编码过滤

```java
<!--配置全局的编码过滤器-->
<filter>
     <filter-name>CharacterEncodingFilter</filter-name>
     <filter-class>org.springframework.web.filter.CharacterEncodingFilter</filter-class>
     <init-param>
         <param-name>encoding</param-name>
         <param-value>UTF-8</param-value>
     </init-param>
</filter>
<filter-mapping>
     <filter-name>CharacterEncodingFilter</filter-name>
     <url-pattern>/*</url-pattern>
</filter-mapping>
```



#### 3、Javaweb常用对象

获得Javaweb常见原生对象，有时在我们的Controller方法中需要用到Javaweb的原生对象，例如：Request、

Response等，我们只需要将需要的对象以形参的形式写在方法上，SpringMVC框架在调用Controller方法时，会自动传递实参：

```java
@GetMapping("/javawebObject")
public String javawebObject(HttpServletRequest request, HttpServletResponse response, 
HttpSession session){
    System.out.println(request);
    System.out.println(response);
    System.out.println(session);
    return "/index.jsp";
}
```



#### 4、请求静态资源

**问题产生：**

在当前项目中的webapp下建立两个文件，作为静态资源

![image-20231001155659076](Spring%EF%BC%88%E4%B8%89%EF%BC%89.assets/image-20231001155659076.png)

我们如果之前像javaweb中一样去访问静态资源，可以看到是访问不到的

![image-20231001155746625](Spring%EF%BC%88%E4%B8%89%EF%BC%89.assets/image-20231001155746625.png)

![image-20231001155815729](Spring%EF%BC%88%E4%B8%89%EF%BC%89.assets/image-20231001155815729.png)



**原因：**

原因是在javaweb中的应用服务器tomcat中的config目录下，有一个配置文件web.xml，其对tomcat中的所有web应用都适用

![image-20231001163011034](Spring%EF%BC%88%E4%B8%89%EF%BC%89.assets/image-20231001163011034.png)

其中配置了一个叫`DefaultServlet`的Servlet

![image-20231001163702315](Spring%EF%BC%88%E4%B8%89%EF%BC%89.assets/image-20231001163702315.png)

其对应的映射路径为`/`，表示缺省，所有的资源都适用

![image-20231001163804974](Spring%EF%BC%88%E4%B8%89%EF%BC%89.assets/image-20231001163804974.png)

即在javaweb中，如果匹配不到对应映射路径的servlet，就会被`DefaultServlet`所接受，然后其中就有处理静态资源的逻辑。

而在我们现在的SpringMVC中，我们声明了一个前端控制器

```java
<!--
    配置DispatcherServlet
    引入spring-mvc依赖后可以直接获取该类全限定名称
  -->
  <servlet>
    <servlet-name>DispatcherServlet</servlet-name>
    <servlet-class>org.springframework.web.servlet.DispatcherServlet</servlet-class>
    <!--
              指定springMVC配置文件的位置
              通过getServletConfig().getInitParameter("initParam")的方式获取;
          -->
    <init-param>
      <param-name>contextConfigLocation</param-name>
      <param-value>classpath:spring-mvc.xml</param-value>
    </init-param>
    <!--服务器启动就创建-->
    <load-on-startup>2</load-on-startup>
  </servlet>
  <!--
    配置映射地址
    这里默认所有的请求都经过它，然后由它去分发到对应的控制器中
  -->
  <servlet-mapping>
    <servlet-name>DispatcherServlet</servlet-name>
    <url-pattern>/</url-pattern>
  </servlet-mapping>
```

其的映射路径也是缺省的，相对于全局的配置，其优先级更高，因此`DispatcherServlet`将`DefaultServlet`覆盖了，并且`DispatcherServlet`中不具有处理静态资源的逻辑，因此会出现上面的情况，无法访问到静态资源。

url-pattern配置为 / 的Servlet我们称其为缺省的Servlet，作用是当其他Servlet都匹配不成功时，就找缺省的Servlet，静态资源由于没有匹配成功的Servlet，所以会找缺省的DefaultServlet，该DefaultServlet具备二次去匹配静态资源的功能。但是我们配置DispatcherServlet后就将其覆盖掉了，而DispatcherServlet会将请求的静态资源的名称当成Controller的映射路径去匹配，即静态资源访问不成功了！

**处理方法：**

（1）再次激活`DefaultServlet`，将映射路径设置更加精确

在当前项目的web.xml中配置`DefaultServlet`，并且将映射路径设置更加精确，这样优先级将会比缺省`/`的更高

```xml
<web-app xmlns="http://xmlns.jcp.org/xml/ns/javaee"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://xmlns.jcp.org/xml/ns/javaee http://xmlns.jcp.org/xml/ns/javaee/web-app_4_0.xsd"
         version="4.0">

  <!-- 配置ContextLoaderListener-->
  <context-param>
    <param-name>contextConfigLocation</param-name>
    <param-value>classpath:applicationContext.xml</param-value>
  </context-param>
  <listener>
    <listener-class>org.springframework.web.context.ContextLoaderListener</listener-class>
  </listener>

  <!--
    配置DispatcherServlet
    引入spring-mvc依赖后可以直接获取该类全限定名称
  -->
  <servlet>
    <servlet-name>DispatcherServlet</servlet-name>
    <servlet-class>org.springframework.web.servlet.DispatcherServlet</servlet-class>
    <!--
              指定springMVC配置文件的位置
              通过getServletConfig().getInitParameter("initParam")的方式获取;
          -->
    <init-param>
      <param-name>contextConfigLocation</param-name>
      <param-value>classpath:spring-mvc.xml</param-value>
    </init-param>
    <!--服务器启动就创建-->
    <load-on-startup>2</load-on-startup>
  </servlet>
  <!--
    配置映射地址
    这里默认所有的请求都经过它，然后由它去分发到对应的控制器中
  -->
  <servlet-mapping>
    <servlet-name>DispatcherServlet</servlet-name>
    <url-pattern>/</url-pattern>
  </servlet-mapping>

  <!--再次激活DefaultServlet url-pattern配置更加精确一点 -->
  <servlet-mapping>
    <!--全局中已经声明了DeafualtServlet的名字为default-->
    <servlet-name>default</servlet-name>
    <!--设置扩展名匹配-->
    <url-pattern>*.html</url-pattern>
  </servlet-mapping>
  <servlet-mapping>
    <!--全局中已经声明了DeafualtServlet的名字为default-->
    <servlet-name>default</servlet-name>
    <!--设置通路匹配-->
    <url-pattern>/images/*</url-pattern>
  </servlet-mapping>
</web-app>

```

运行结果

访问

![image-20231001194320528](Spring%EF%BC%88%E4%B8%89%EF%BC%89.assets/image-20231001194320528.png)

访问图片

![image-20231001194451653](Spring%EF%BC%88%E4%B8%89%EF%BC%89.assets/image-20231001194451653.png)

（2）在spring-mvc.xml中去配置静态资源映射，匹配映射路径的请求到指定的位置去匹配资源

```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xmlns:context="http://www.springframework.org/schema/context"
       xmlns:mvc="http://www.springframework.org/schema/mvc"
       xsi:schemaLocation="
       http://www.springframework.org/schema/beans
       http://www.springframework.org/schema/beans/spring-beans.xsd
       http://www.springframework.org/schema/context
       http://www.springframework.org/schema/context/spring-context.xsd
       http://www.springframework.org/schema/mvc
       http://www.springframework.org/schema/mvc/spring-mvc.xsd
       ">
    <!--组件扫描-->
    <context:component-scan base-package="com.example.controller"/>

    <!--配置HandlerAdapter 处理将Json格式的字符串转化为JavaBean-->
    <bean class="org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter">
        <property name="messageConverters">
            <list>
                <bean class="org.springframework.http.converter.json.MappingJackson2HttpMessageConverter"/>
            </list>
        </property>
    </bean>

    <!--配置文件上传解析器，注意：id的名字是固定写法-->
    <bean id="multipartResolver"
          class="org.springframework.web.multipart.commons.CommonsMultipartResolver">
        <property name="defaultEncoding" value="UTF-8"/><!--文件的编码格式 默认是ISO8859-1-->
        <property name="maxUploadSizePerFile" value="1048576"/><!--上传的每个文件限制的大小 单位字节-->
        <property name="maxUploadSize" value="3145728"/><!--上传文件的总大小-->
        <property name="maxInMemorySize" value="1048576"/><!--上传文件的缓存大小-->
    </bean>

    <!-- mapping是映射资源路径，location是对应资源所在的位置 -->
    <!--
		所有请求路径为……/images/*的，都会去webapp/images下面查找对应的静态资源
		一般我们都会将静态资源放到一个目录下，如这里的“/images”
		不允许在location的值之间为“/”
	-->
    <mvc:resources mapping="/images/*" location="/images/"/>
    
</beans>
```

继续访问静态图片资源

![image-20231001201411101](Spring%EF%BC%88%E4%B8%89%EF%BC%89.assets/image-20231001201411101.png)

（3）在spring-mvc.xml中去配置< mvc:default-servlet-handler >，该方式是注册了一DefaultServletHttpRequestHandler 处理器，静态资源的访问都由该处理器去处理，这也是开发中使用最多的

```xml
<mvc:default-servlet-handler/>
```

![image-20231001203757337](Spring%EF%BC%88%E4%B8%89%EF%BC%89.assets/image-20231001203757337.png)

![image-20231001203809428](Spring%EF%BC%88%E4%B8%89%EF%BC%89.assets/image-20231001203809428.png)



#### 5、注解驱动<mvc:annotation-drive>标签

**问题产生：**

在上面进行静态资源配置的第二种和第三种方式后，我们就可以正常访问静态资源了。

但是控制器Controller又无法访问了，报错404，即找不到对应的资源

Controller：

```java
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
```

访问url为`[HTTP Status 404 – 未找到](http://localhost:8080/SpringMVC_war/show)`的资源

结果显示404，未找到对应的资源

![image-20231002111515415](Spring%EF%BC%88%E4%B8%89%EF%BC%89.assets/image-20231002111515415.png)



**原因：**

在SpringMVC的依赖jar包Spring-webmvc中，有一个默认的配置文件`DispatcherServlet.properties`

![image-20231002121344354](Spring%EF%BC%88%E4%B8%89%EF%BC%89.assets/image-20231002121344354.png)

当不在SpringMVC的核心配置文件（spring-mvc.xml）中配置处理器映射器、适配器和视图解析器的时候，默认会使用默认配置文件`DispatcherServlet.properties`的配置

![image-20231002142943913](Spring%EF%BC%88%E4%B8%89%EF%BC%89.assets/image-20231002142943913.png)

其中默认处理器映射器中的`org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping`就是用来处理`@RequestMapping`标签的

![image-20231002144957947](Spring%EF%BC%88%E4%B8%89%EF%BC%89.assets/image-20231002144957947.png)

但是当我们在spring-mvc.xml配置文件中配置`<mvc:default-servlet-handler/>`和`<mvc:resources mapping="/images/*" location="/images/"/>`后，为什么就找不到该资源了呢？

我们在springmvc的jar包中找到xml中mvc自定义命名空间的解析器

![image-20231002145141782](Spring%EF%BC%88%E4%B8%89%EF%BC%89.assets/image-20231002145141782.png)

其中初始化了各种解析器

```java
public class MvcNamespaceHandler extends NamespaceHandlerSupport {

	@Override
	public void init() {
		registerBeanDefinitionParser("annotation-driven", new AnnotationDrivenBeanDefinitionParser());
		registerBeanDefinitionParser("default-servlet-handler", new DefaultServletHandlerBeanDefinitionParser());
		registerBeanDefinitionParser("interceptors", new InterceptorsBeanDefinitionParser());
		registerBeanDefinitionParser("resources", new ResourcesBeanDefinitionParser());
		registerBeanDefinitionParser("view-controller", new ViewControllerBeanDefinitionParser());
		registerBeanDefinitionParser("redirect-view-controller", new ViewControllerBeanDefinitionParser());
		registerBeanDefinitionParser("status-controller", new ViewControllerBeanDefinitionParser());
		registerBeanDefinitionParser("view-resolvers", new ViewResolversBeanDefinitionParser());
		registerBeanDefinitionParser("tiles-configurer", new TilesConfigurerBeanDefinitionParser());
		registerBeanDefinitionParser("freemarker-configurer", new FreeMarkerConfigurerBeanDefinitionParser());
		registerBeanDefinitionParser("groovy-configurer", new GroovyMarkupConfigurerBeanDefinitionParser());
		registerBeanDefinitionParser("script-template-configurer", new ScriptTemplateConfigurerBeanDefinitionParser());
		registerBeanDefinitionParser("cors", new CorsBeanDefinitionParser());
	}
}
```

先来看下`annotation-driven`的解析器`DefaultServletHandlerBeanDefinitionParser`

![image-20231002145951253](Spring%EF%BC%88%E4%B8%89%EF%BC%89.assets/image-20231002145951253.png)

我们可以看到其在解析的时候，帮我们注册了一个叫`SimpleUrlHandlerMapping`的映射器处理器，那么我们默认的映射器处理器就不会加载，因为一旦SpringMVC容器中存在 HandlerMapping 类型的组件时，前端控制器

DispatcherServlet在进行初始化时，就会从容器中获得HandlerMapping ，不在加载 dispatcherServlet.properties

中默认处理器映射器策略，那也就意味着RequestMappingHandlerMapping不会被加载到了。由于默认处理器映射器中的`org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping`就是用来处理`@RequestMapping`标签的，而它没有加载，所以会出现找不到资源的情况。



看一下`resources`的解析器`ResourcesBeanDefinitionParser`也是同理

![image-20231002150019873](Spring%EF%BC%88%E4%B8%89%EF%BC%89.assets/image-20231002150019873.png)



**解决方法：**

（1）我们可以在spring-mvc.xml配置文件中，重新声明`RequestMappingHandlerMapping`

```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xmlns:context="http://www.springframework.org/schema/context"
       xmlns:mvc="http://www.springframework.org/schema/mvc"
       xsi:schemaLocation="
       http://www.springframework.org/schema/beans
       http://www.springframework.org/schema/beans/spring-beans.xsd
       http://www.springframework.org/schema/context
       http://www.springframework.org/schema/context/spring-context.xsd
       http://www.springframework.org/schema/mvc
       http://www.springframework.org/schema/mvc/spring-mvc.xsd
       ">
    <!--组件扫描-->
    <context:component-scan base-package="com.example.controller"/>

    <!--配置HandlerMapping-->
    <bean class="org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping"/>

    <!--配置HandlerAdapter 处理将Json格式的字符串转化为JavaBean-->
    <bean class="org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter">
        <property name="messageConverters">
            <list>
                <bean class="org.springframework.http.converter.json.MappingJackson2HttpMessageConverter"/>
            </list>
        </property>
    </bean>

    <!--配置文件上传解析器，注意：id的名字是固定写法-->
    <bean id="multipartResolver"
          class="org.springframework.web.multipart.commons.CommonsMultipartResolver">
        <property name="defaultEncoding" value="UTF-8"/><!--文件的编码格式 默认是ISO8859-1-->
        <property name="maxUploadSizePerFile" value="1048576"/><!--上传的每个文件限制的大小 单位字节-->
        <property name="maxUploadSize" value="3145728"/><!--上传文件的总大小-->
        <property name="maxInMemorySize" value="1048576"/><!--上传文件的缓存大小-->
    </bean>

    <!-- mapping是映射资源路径，location是对应资源所在的位置 -->
    <!--<mvc:resources mapping="/images/*" location="/images/"/>-->

    <!--向容器中注册静态资源处理器-->
    <mvc:default-servlet-handler/>

</beans>
```

可以看到访问成功

![image-20231002150431462](Spring%EF%BC%88%E4%B8%89%EF%BC%89.assets/image-20231002150431462.png)

之后我们再搭配`<mvc:default-servlet-handler/>`和`<mvc:resources mapping="/images/*" location="/images/"/>`就既可以解析静态资源，也可以找到对应的前端控制器进行解析。



注意：但是把配置HandlerMapping放在`default-servlet-handler`下面还是会失败，可能和注册的顺序有关，先被前面的映射器处理器捕捉到。



（2）注解驱动 <mvc:annotation-driven> 标签

根据上面的讲解，可以总结一下，要想使用@RequestMapping正常映射到资源方法，同时静态资源还能正常访问，还可以将请求json格式字符串和JavaBean之间自由转换，我们就需要在spring-mvc.xml中尽心如下配置：

```xml
<!-- 显示配置RequestMappingHandlerMapping -->
<bean class="org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping"/>
<!-- 显示配置RequestMappingHandlerAdapter -->
<bean class="org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter">
     <property name="messageConverters">
     <list>
         <bean class="org.springframework.http.converter.json.MappingJackson2HttpMessageConverter"/>
     </list>
     </property>
</bean>
<!--配置DefaultServletHttpRequestHandler-->
<mvc:default-servlet-handler/>
```

这么复杂繁琐的配置，是不是看上去有点头大？Spring是个"暖男"，将上述配置浓缩成了一个简单的配置标签，那就

是mvc的注解驱动，该标签内部会帮我们注册RequestMappingHandlerMapping、注册

RequestMappingHandlerAdapter并注入Json消息转换器等，上述配置就可以简化成如下：

```xml
<!--mvc注解驱动-->
<mvc:annotation-driven/>
<!--配置DefaultServletHttpRequestHandler-->
<mvc:default-servlet-handler/>
```

PS：<mvc:annotation-driven> 标签在不同的版本中，帮我们注册的组件不同，Spring 3.0.X 版本注册是

DefaultAnnotationHandlerMapping 和 AnnotationMethodHandlerAdapter，由于框架的发展，从Spring 3.1.X 

开始注册组件变为 RequestMappingHandlerMapping和RequestMappingHandlerAdapter

可以正常访问控制器

![image-20231002152619579](Spring%EF%BC%88%E4%B8%89%EF%BC%89.assets/image-20231002152619579.png)

可以正常访问静态资源

![image-20231002152642134](Spring%EF%BC%88%E4%B8%89%EF%BC%89.assets/image-20231002152642134.png)



### 三、SpingMVC的响应处理

Spring在给客户端响应数据的时候可以分为两部分：

- 传统同步方式：准备好模型数据，在跳转到执行页面进行展示，此方式使用越来越少了，基于历史原因，一些旧

  项目还在使用；

- 前后端分离异步方式：：前端使用Ajax技术+Restful风格与服务端进行Json格式为主的数据交互，目前市场上几乎

  都是此种方式了。



#### 1、传统同步业务数据响应

传统同步业务在数据响应时，SpringMVC又涉及以下四种方式：

- 请求资源转发；

![image-20231002153522188](Spring%EF%BC%88%E4%B8%89%EF%BC%89.assets/image-20231002153522188.png)

演示：

index.jsp

```jsp
<html>
<body>
<h2>Hello World!</h2>
</body>
<h1>hello!</h1>
</html>
```

控制器：

```java
@Controller
public class ResponseController {
    //转发
    @RequestMapping("/res1")
    public String res1(){
        return "forward:/index.jsp";
    }
}

```

运行结果：

![image-20231002155438123](Spring%EF%BC%88%E4%B8%89%EF%BC%89.assets/image-20231002155438123.png)

除了重定向资源，还可以重定向控制器



- 请求资源重定向；

![image-20231002154202573](Spring%EF%BC%88%E4%B8%89%EF%BC%89.assets/image-20231002154202573.png)



演示：

index.jsp

```jsp
<html>
<body>
<h2>Hello World!</h2>
</body>
<h1>hello!</h1>
</html>
```

控制器：

```java
@Controller
public class ResponseController {
    //重定向
    @RequestMapping("/res2")
    public String res2(){
        return "redirect:/index.jsp";
    }
}
```

运行结果：

输入url

```
http://localhost:8080/SpringMVC_war/res2
```

![image-20231002155527239](Spring%EF%BC%88%E4%B8%89%EF%BC%89.assets/image-20231002155527239.png)



注意：转发和重定向的对象除了资源外，还可以是控制器

![image-20231002155647392](Spring%EF%BC%88%E4%B8%89%EF%BC%89.assets/image-20231002155647392.png)

- 响应模型数据；

响应模型数据，响应模型数据本质也是转发，在转发时可以准备模型数据（也可以用javaweb中，将数据存放到request域中，然后再jsp中进行显示，springmvc为我们提供了模型数据）



演示：

index2.jsp

```jsp
<%--
  Created by IntelliJ IDEA.
  User: 23220
  Date: 2023/10/2
  Time: 16:06
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>Title</title>
</head>
<body>
    <h1>转发显示的模型数据是：${user.username}==${user.age}</h1>
</body>
</html>

```

控制器：

```java
package com.example.controller;

import com.example.pojo.User;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

/**
 * @author banana
 * @create 2023-10-02 15:49
 */
@Controller
public class ResponseController {
    //响应模型数据
    @RequestMapping("/res3")
    public ModelAndView res3(ModelAndView modelAndView){
        //准备javaBean模型数据
        User user = new User();
        user.setUsername("yjy");
        user.setAge(18);
        //设置模型
        modelAndView.addObject("user", user);
        //设置视图
        modelAndView.setViewName("/index2.jsp");
        return modelAndView;
    }
}

```

结果：

![image-20231002161053955](Spring%EF%BC%88%E4%B8%89%EF%BC%89.assets/image-20231002161053955.png)

- 直接回写数据给客户端；

直接回写数据，直接通过方法的返回值返回给客户端的字符串，但是SpringMVC默认的方法返回值是视图，可以通过

@ResponseBody 注解显示的告知此处的返回值不要进行视图处理，是要以响应体的方式处理的



演示：

控制器：

```java
//响应模型数据
//@ResponseBody:告知此处的返回值不要进行视图处理，是要以响应体的方式处理的
@RequestMapping("/res4")
@ResponseBody
public String res4(){
    return "i am yjy";
}
```

运行结果：

![image-20231002161439155](Spring%EF%BC%88%E4%B8%89%EF%BC%89.assets/image-20231002161439155.png)

#### 2、前后端分离异步业务数据响应

其实此处的回写数据，跟上面回写数据给客户端的语法方式一样，只不过有如下一些区别：

- 同步方式回写数据，是将数据响应给浏览器进行页面展示的，而异步方式回写数据一般是回写给Ajax引擎的，即谁访问服务器端，服务器端就将数据响应给谁
- 同步方式回写的数据，一般就是一些无特定格式的字符串，而异步方式回写的数据大多是Json格式字符串



（1）回写普通数据使用@ResponseBody标注方法，直接返回字符串即可

控制器：

```java
@Controller
public class ResponseController2 {

    @GetMapping("ajax/req1")
    @ResponseBody
    public String res1(){
        return "123";
    }
}

```

结果：

![image-20231002170704953](Spring%EF%BC%88%E4%B8%89%EF%BC%89.assets/image-20231002170704953.png)

当然我们一般是返回json格式的字符串

控制器：

```java
@Controller
public class ResponseController2 {

    @GetMapping("ajax/res1")
    @ResponseBody
    public String res1(){
        return "{\"username\":\"yjy\"}";
    }
}

```

结果：

![image-20231002170838370](Spring%EF%BC%88%E4%B8%89%EF%BC%89.assets/image-20231002170838370.png)



（2）回写Json格式的字符串，即将直接拼接Json格式的字符串或使用工具将JavaBean转换成Json格式的字符串回写

演示：

控制器：

```java
@Controller
public class ResponseController2 {
    @GetMapping("ajax/res2")
    @ResponseBody
    public String res2() throws JsonProcessingException {
        //创建JavaBean
        User user = new User();
        user.setUsername("yjy");
        user.setAge(18);
        //使用jackson转换成json格式的字符串
        String json = new ObjectMapper().writeValueAsString(user);
        return json;
    }
}

```

结果：

![image-20231002171508155](Spring%EF%BC%88%E4%B8%89%EF%BC%89.assets/image-20231002171508155.png)

（3）@Resonpose自动转json格式

在讲解SringMVC接收请求数据时，客户端提交的Json格式的字符串，也是使用Jackson进行的手动转换成JavaBean，可以当我们使用了@RequestBody时，直接用JavaBean就接收了Json格式的数据，原理其实就是SpringMVC底层帮我们做了转换，此处@ResponseBody也可以将JavaBean自动给我们转换成Json格式字符串回响应

演示：

控制器：

```java
@Controller
public class ResponseController2 {
    @GetMapping("ajax/res3")
    @ResponseBody
    public User res3() throws JsonProcessingException {
        //创建JavaBean
        User user = new User();
        user.setUsername("yjy");
        user.setAge(18);
        return user;
    }
}
```

结果：

![image-20231002172250847](Spring%EF%BC%88%E4%B8%89%EF%BC%89.assets/image-20231002172250847.png)

@ResponseBody注解使用优化，在进行前后端分离开发时，Controller的每个方法都是直接回写数据的，所以每个方法上都得写@ResponseBody，可以将@ResponseBody写到Controller上，那么该Controller中的所有方法都具备了返回响应体数据的功能了

```java
@Controller
@ResponseBody
public class ResponseController2 {

    @GetMapping("ajax/res1")
    public String res1(){
        return "{\"username\":\"yjy\"}";
    }

    @GetMapping("ajax/res2")
    public String res2() throws JsonProcessingException {
        //创建JavaBean
        User user = new User();
        user.setUsername("yjy");
        user.setAge(18);
        //使用jackson转换成json格式的字符串
        String json = new ObjectMapper().writeValueAsString(user);
        return json;
    }

    @GetMapping("ajax/res3")
    public User res3() throws JsonProcessingException {
        //创建JavaBean
        User user = new User();
        user.setUsername("yjy");
        user.setAge(18);
        return user;
    }
}

```



### 四、SpringMVC的拦截器

#### 1、拦截器Interceptor简介

**简介：**

SpringMVC的拦截器Interceptor规范，主要是对Controller资源访问时进行拦截操作的技术，当然拦截后可以进行权限控制，功能增强等都是可以的。拦截器有点类似 Javaweb 开发中的Filter，拦截器与Filter的区别如下图：

![image-20231002185614634](Spring%EF%BC%88%E4%B8%89%EF%BC%89.assets/image-20231002185614634.png)

**Filter和Interceptor对比：**

![image-20231002185709153](Spring%EF%BC%88%E4%B8%89%EF%BC%89.assets/image-20231002185709153.png)



其具体是通过实现`HandlerInterceptor`接口来实现的，且被Spring管理的Bean都是拦截器，该接口的定义如下：

```java
public interface HandlerInterceptor {
    
     default boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object 
    handler) throws Exception {
     return true;
     }
    
     default void postHandle(HttpServletRequest request, HttpServletResponse response, Object 
    handler, @Nullable ModelAndView modelAndView) throws Exception {
     }
    
     default void afterCompletion(HttpServletRequest request, HttpServletResponse response, 
    Object handler, @Nullable Exception ex) throws Exception {
     }
}
```

![image-20231002190324090](Spring%EF%BC%88%E4%B8%89%EF%BC%89.assets/image-20231002190324090.png)

Controller方法处理器就是controller中的一个方法，如：

```java
//配置映射路径
@RequestMapping("/show")
public String show(){
    System.out.println("show……" + quickService);
    return "index.jsp";
}
```



#### 2、拦截器快速入门

（1）编写拦截器类实现接口

并对接口的方法进行重写，打印相关的信息，便于后面的测试

```java
package com.example.interceptors;

import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author banana
 * @create 2023-10-02 19:05
 */
public class MyInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        System.out.println("MyInterceotor……preHandle");
        //true:放行 false：不放行
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        System.out.println("MyInterceptor1……postHandle");
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        System.out.println("MyInterceptor1……afterCompletion");
    }
}

```

（2）在配置文件spring-mvc.xml中进行拦截器的配置

```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xmlns:context="http://www.springframework.org/schema/context"
       xmlns:mvc="http://www.springframework.org/schema/mvc"
       xsi:schemaLocation="
       http://www.springframework.org/schema/beans
       http://www.springframework.org/schema/beans/spring-beans.xsd
       http://www.springframework.org/schema/context
       http://www.springframework.org/schema/context/spring-context.xsd
       http://www.springframework.org/schema/mvc
       http://www.springframework.org/schema/mvc/spring-mvc.xsd
       ">
    <!--组件扫描-->
    <context:component-scan base-package="com.example.controller"/>

    <!--配置HandlerMapping-->
    <!--<bean class="org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping"/>
-->
    <!--配置HandlerAdapter 处理将Json格式的字符串转化为JavaBean-->
    <!--<bean class="org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter">
        <property name="messageConverters">
            <list>
                <bean class="org.springframework.http.converter.json.MappingJackson2HttpMessageConverter"/>
            </list>
        </property>
    </bean>-->

    <!--配置文件上传解析器，注意：id的名字是固定写法-->
    <bean id="multipartResolver"
          class="org.springframework.web.multipart.commons.CommonsMultipartResolver">
        <property name="defaultEncoding" value="UTF-8"/><!--文件的编码格式 默认是ISO8859-1-->
        <property name="maxUploadSizePerFile" value="1048576"/><!--上传的每个文件限制的大小 单位字节-->
        <property name="maxUploadSize" value="3145728"/><!--上传文件的总大小-->
        <property name="maxInMemorySize" value="1048576"/><!--上传文件的缓存大小-->
    </bean>

    <!-- mapping是映射资源路径，location是对应资源所在的位置 -->
    <!--<mvc:resources mapping="/images/*" location="/images/"/>-->

    <!--mvc注解驱动-->
    <mvc:annotation-driven/>

    <!--向容器中注册静态资源处理器-->
    <mvc:default-servlet-handler/>

    <!--配置拦截器-->
    <mvc:interceptors>
        <mvc:interceptor>
            <!--
            对那些请求路径进行拦截
            /*：一级拦截
            /**：多级拦截
            -->
            <mvc:mapping path="/**"/>
            <bean class="com.example.interceptors.MyInterceptor"/>
        </mvc:interceptor>
    </mvc:interceptors>
</beans>
```

（3）启动服务测试

访问 控制器对象：

```java
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

```

当拦截器中的preHabdle方法的返回值是true结果：

```java
@Override
public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
    System.out.println("MyInterceotor……preHandle");
    //true:放行 false：不放行
    return true;
}
```

![image-20231002191535776](Spring%EF%BC%88%E4%B8%89%EF%BC%89.assets/image-20231002191535776.png)

![image-20231002191544100](Spring%EF%BC%88%E4%B8%89%EF%BC%89.assets/image-20231002191544100.png)

当拦截器中的preHabdle方法的返回值是false结果：

```java
@Override
public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
    System.out.println("MyInterceotor……preHandle");
    //true:放行 false：不放行
    return false;
}
```

![image-20231002191639609](Spring%EF%BC%88%E4%B8%89%EF%BC%89.assets/image-20231002191639609.png)

![image-20231002191648779](Spring%EF%BC%88%E4%B8%89%EF%BC%89.assets/image-20231002191648779.png)





#### 3、拦截器执行顺序

![image-20231002192639098](Spring%EF%BC%88%E4%B8%89%EF%BC%89.assets/image-20231002192639098.png)

![image-20231002192646259](Spring%EF%BC%88%E4%B8%89%EF%BC%89.assets/image-20231002192646259.png)

![image-20231002192653545](Spring%EF%BC%88%E4%B8%89%EF%BC%89.assets/image-20231002192653545.png)

#### 4、拦截器执行原理

**拦截器的大致流程图：**

其中`RequestMappingHandlerMapping`帮我们匹配对应的映射路径的时候，最后的资源即Conrtoller处理器只有一个，但是对应的拦截器可能是有多个的。

![image-20231002193130969](Spring%EF%BC%88%E4%B8%89%EF%BC%89.assets/image-20231002193130969.png)



**源码分析：**

首先我们找到`DispatcherServlet`类的`doDispatch`方法

`doDispatch`方法扩展：

DispatcherServlet中的doDispatch方法是Spring MVC框架中核心的请求分发方法之一。该方法负责处理客户端发送的HTTP请求，并将请求分发给相应的处理器(Controller)进行处理。

在Spring MVC框架中，DispatcherServlet是前端控制器(Front Controller)，负责接收所有的请求，并将这些请求分发给适当的处理器进行处理。具体而言，doDispatch方法的作用包括以下几个方面：

1. 解析请求：首先，doDispatch方法会调用HandlerMapping组件来解析请求URL，并确定与之对应的处理器(Controller)。通过使用不同的HandlerMapping实现类，可以根据不同的匹配规则来确定合适的处理器。
2. 执行处理器：一旦找到了合适的处理器，doDispatch方法会调用HandlerAdapter组件来执行处理器中的业务逻辑。HandlerAdapter根据处理器的类型和特征，调用处理器的相应方法进行处理，并获取处理结果。
3. 处理视图：在处理器执行完毕后，doDispatch方法会获得处理结果，并调用ViewResolver组件来解析视图名，并获取相应的视图对象。ViewResolver可以根据视图名的规则和配置，确定最终需要渲染的视图。
4. 视图渲染：最后，doDispatch方法会调用视图对象的渲染方法，将处理结果渲染到响应体中，并返回给客户端。

需要注意的是，doDispatch方法还会处理一些异常情况，例如请求方法不被允许、请求参数绑定失败等。在这些情况下，它会生成相应的错误响应，并返回给客户端。



我们可以看到其中声明了一个`HandlerExecutionChain`的对象，并且值置为空

![image-20231002231058204](Spring%EF%BC%88%E4%B8%89%EF%BC%89.assets/image-20231002231058204.png)

`HandlerExecutionChain`类中的结构如下所示，该类用于存放此时请求隐射地址的处理区和对应的过滤器

```java
public class HandlerExecutionChain {

	private static final Log logger = LogFactory.getLog(HandlerExecutionChain.class);
	
    //存放当前请求映射地址对应的处理器controller
	private final Object handler;

    //存放当前请求映射地址对应的所有过滤器
	private final List<HandlerInterceptor> interceptorList = new ArrayList<>();

	private int interceptorIndex = -1;
	
    //省略……
}
```



之后调用getHandler方法，并将processedRequest（这里就是doDispatch方法的HttpServletRequest参数，由应用服务器进行入参）作为参数，然后去寻找对应的映射器处理器，然后返回一个HandlerExecutionChain对象给mappedHandler

![image-20231002232627148](Spring%EF%BC%88%E4%B8%89%EF%BC%89.assets/image-20231002232627148.png)

可以从下面的getHeadler方法中看出，就是调用加载到spring容器中的处理器映射器对我们的请求进行处理，并放回一个HandlerExecutionChain对象

```java
@Nullable
protected HandlerExecutionChain getHandler(HttpServletRequest request) throws Exception {
    //此时spring容器中的处理器映射器不为空
    if (this.handlerMappings != null) {
        for (HandlerMapping mapping : this.handlerMappings) {
            HandlerExecutionChain handler = mapping.getHandler(request);
            if (handler != null) {
                return handler;
            }
        }
    }
    return null;
}
```



如下用于执行目标方法，即控制器中的方法

![image-20231002234003156](Spring%EF%BC%88%E4%B8%89%EF%BC%89.assets/image-20231002234003156.png)







然后我们能够看到其调用mapperHandler的applyPreHandle方法，如果返回值为false，就直接return调，即结束

![image-20231002233429228](Spring%EF%BC%88%E4%B8%89%EF%BC%89.assets/image-20231002233429228.png)

applyPreHandle方法用于将mapperHandler中与这一次请求映射地址相匹配的过滤器进行一个顺序遍历，分别调用过滤器的preHandle的方法

```java
boolean applyPreHandle(HttpServletRequest request, HttpServletResponse response) throws Exception {
    for (int i = 0; i < this.interceptorList.size(); i++) {
        HandlerInterceptor interceptor = this.interceptorList.get(i);
        if (!interceptor.preHandle(request, response, this.handler)) {
            triggerAfterCompletion(request, response, null);
            return false;
        }
        this.interceptorIndex = i;
    }
    return true;
}
```



然后在后面调用mappedHandler的applyPostHandle方法

![image-20231002233737367](Spring%EF%BC%88%E4%B8%89%EF%BC%89.assets/image-20231002233737367.png)

applyPostHandle方法用于倒叙遍历过滤器链中的过滤器，并调用他们的postHandle方法

```java
void applyPostHandle(HttpServletRequest request, HttpServletResponse response, @Nullable ModelAndView mv)
			throws Exception {

    for (int i = this.interceptorList.size() - 1; i >= 0; i--) {
        HandlerInterceptor interceptor = this.interceptorList.get(i);
        interceptor.postHandle(request, response, this.handler, mv);
    }
}
```



进入最后的该方法

![image-20231002234218266](Spring%EF%BC%88%E4%B8%89%EF%BC%89.assets/image-20231002234218266.png)

在刚方法中有一个处理，如下所示

![image-20231002234240115](Spring%EF%BC%88%E4%B8%89%EF%BC%89.assets/image-20231002234240115.png)

其就对应过滤器中的最后一个方法，倒序调用过滤器链中的过滤的afterCompletion方法

```java
void triggerAfterCompletion(HttpServletRequest request, HttpServletResponse response, @Nullable Exception ex) {
    for (int i = this.interceptorIndex; i >= 0; i--) {
        HandlerInterceptor interceptor = this.interceptorList.get(i);
        try {
            interceptor.afterCompletion(request, response, this.handler, ex);
        }
        catch (Throwable ex2) {
            logger.error("HandlerInterceptor.afterCompletion threw exception", ex2);
        }
    }
}
```



这样从源码看来，前端控制器是如何将过滤器存储，以及如何调用就可以看得很清楚了。



### 五、SpringMVC的全注解开发

#### 1、 spring-mvc.xml 中组件转化为注解形式

我们可以将spring-mvc.xml中的内容分为三大类

1、组件扫描

2、非自定义Bean

3、非Bean配置

```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xmlns:context="http://www.springframework.org/schema/context"
       xmlns:mvc="http://www.springframework.org/schema/mvc"
       xsi:schemaLocation="
       http://www.springframework.org/schema/beans
       http://www.springframework.org/schema/beans/spring-beans.xsd
       http://www.springframework.org/schema/context
       http://www.springframework.org/schema/context/spring-context.xsd
       http://www.springframework.org/schema/mvc
       http://www.springframework.org/schema/mvc/spring-mvc.xsd
       ">


    <!--1、组件扫描-->
    <context:component-scan base-package="com.example.controller"/>


    <!--2、非自定义Bean-->
    <!--配置文件上传解析器，注意：id的名字是固定写法-->
    <bean id="multipartResolver"
          class="org.springframework.web.multipart.commons.CommonsMultipartResolver">
        <property name="defaultEncoding" value="UTF-8"/><!--文件的编码格式 默认是ISO8859-1-->
        <property name="maxUploadSizePerFile" value="1048576"/><!--上传的每个文件限制的大小 单位字节-->
        <property name="maxUploadSize" value="3145728"/><!--上传文件的总大小-->
        <property name="maxInMemorySize" value="1048576"/><!--上传文件的缓存大小-->
    </bean>



    <!--3、非Bean配置-->
    <!--mvc注解驱动-->
    <mvc:annotation-driven/>
    <!--向容器中注册静态资源处理器-->
    <mvc:default-servlet-handler/>
    <!--配置拦截器-->
    <mvc:interceptors>
        <mvc:interceptor>
            <!--
            对那些请求路径进行拦截
            /*：一级拦截
            /**：多级拦截
            -->
            <mvc:mapping path="/**"/>
            <bean class="com.example.interceptors.MyInterceptor"/>
        </mvc:interceptor>
    </mvc:interceptors>
</beans>
```



**消除1、组件扫描**

首先创建个核心配置类

![image-20231002235514606](Spring%EF%BC%88%E4%B8%89%EF%BC%89.assets/image-20231002235514606.png)

在该类上添加`@Configuration`和`@ComponentScan("com.example.controller")`注解

@Configuration：声明当前是一个配置类

@ComponentScan("com.example.controller")：替代配置类中的组件扫描

```java
package com.example.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * @author banana
 * @create 2023-10-02 23:54
 */
//声明当前是一个配置类
@Configuration
//扫描注解
@ComponentScan("com.example.controller")
public class SpringMvcConfig {

}

```





**消除2、非自定义Bean**

通过@Bean注解进行配置非自定义Bean

```java
//声明当前是一个配置类
@Configuration
//扫描注解
@ComponentScan("com.example.controller")
public class SpringMvcConfig {

    @Bean   //不指定名字默认以方法名作为Bean的名称
    public CommonsMultipartResolver multipartResolver(){
        CommonsMultipartResolver commonsMultipartResolver 
                = new CommonsMultipartResolver();
        commonsMultipartResolver.setDefaultEncoding("UTF-8");
        commonsMultipartResolver.setMaxUploadSizePerFile(1048576);
        commonsMultipartResolver.setMaxUploadSize(3145728);
        commonsMultipartResolver.setMaxInMemorySize(1048576);
        return commonsMultipartResolver;
    }
}

```



**消除3、非Bean配置**

对于<mvc:annotation-driven />、<mvc:default-servlet-handler /> 和 <mvc:interceptor > 这些非Bean的配置，SpringMVC为我们提供了一个注解叫做@EnableWebMVC

```java
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
@Import(DelegatingWebMvcConfiguration.class)
public @interface EnableWebMvc {
}
```

其通过`@Import(DelegatingWebMvcConfiguration.class)`引入了一个配置类`DelegatingWebMvcConfiguration`

```java
@Configuration(proxyBeanMethods = false)
public class DelegatingWebMvcConfiguration extends WebMvcConfigurationSupport {

	private final WebMvcConfigurerComposite configurers = new WebMvcConfigurerComposite();
	
    //在方法上使用@Autowired，从spring容器中自动注入WebMvcConfigurer类型的Bean
    @Autowired(required = false)
	public void setConfigurers(List<WebMvcConfigurer> configurers) {
		if (!CollectionUtils.isEmpty(configurers)) {
			this.configurers.addWebMvcConfigurers(configurers);
		}
	}
	//省略……
}
```

由于在上面的setConfigurers方法上添加了@Autowired自动注入的注释，且其中的参数类型为集合，Spring容器中的所有类型为`WebMvcConfigurer`会被自动注入到上面的setConfigurers方法中的参数中，然后被自动调用，因此可以实线WebMvcConfigurer接口，完成一些解析器、默认Servlet等的指定，WebMvcConfigurer接口如下

```java
public interface WebMvcConfigurer {
 //配置默认Servet处理器
 default void configureDefaultServletHandling(DefaultServletHandlerConfigurer configurer) { }
 //添加拦截器
 default void addInterceptors(InterceptorRegistry registry) { }
 //添加资源处理器
 default void addResourceHandlers(ResourceHandlerRegistry registry) { }
 //添加视图控制器
 default void addViewControllers(ViewControllerRegistry registry) { }
 //配置视图解析器
 default void configureViewResolvers(ViewResolverRegistry registry) { }
 //添加参数解析器
 default void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) { }
//... 省略其他代码 ...
}
```





**<mvc:annotation-driven />的全注释：**

<mvc:annotation-driven />有许多功能，我们在这里使用到的就是自动帮助我们注入`defaultServletHandlerMapping`映射器处理器和`RequestMappingHandlerAdapter`处理器适配器，我们可以在`EnableWebMvc`接口引入的配置类`DelegatingWebMvcConfiguration`的父类`WebMvcConfigurationSupport`中，找到两个注入非自定义Bean的两个方法

![image-20231003113057863](Spring%EF%BC%88%E4%B8%89%EF%BC%89.assets/image-20231003113057863.png)

`defaultServletHandlerMapping`：

```java
@Bean
@Nullable
public HandlerMapping defaultServletHandlerMapping() {
    Assert.state(this.servletContext != null, "No ServletContext set");
    DefaultServletHandlerConfigurer configurer = new DefaultServletHandlerConfigurer(this.servletContext);
    configureDefaultServletHandling(configurer);
    return configurer.buildHandlerMapping();
}
```

`requestMappingHandlerAdapter`：

```java
@Bean
public RequestMappingHandlerAdapter requestMappingHandlerAdapter(
    @Qualifier("mvcContentNegotiationManager") ContentNegotiationManager contentNegotiationManager,
    @Qualifier("mvcConversionService") FormattingConversionService conversionService,
    @Qualifier("mvcValidator") Validator validator) {

    RequestMappingHandlerAdapter adapter = createRequestMappingHandlerAdapter();
    adapter.setContentNegotiationManager(contentNegotiationManager);
    adapter.setMessageConverters(getMessageConverters());
    adapter.setWebBindingInitializer(getConfigurableWebBindingInitializer(conversionService, validator));
    adapter.setCustomArgumentResolvers(getArgumentResolvers());
    adapter.setCustomReturnValueHandlers(getReturnValueHandlers());

    if (jackson2Present) {
        adapter.setRequestBodyAdvice(Collections.singletonList(new JsonViewRequestBodyAdvice()));
        adapter.setResponseBodyAdvice(Collections.singletonList(new JsonViewResponseBodyAdvice()));
    }

    AsyncSupportConfigurer configurer = getAsyncSupportConfigurer();
    if (configurer.getTaskExecutor() != null) {
        adapter.setTaskExecutor(configurer.getTaskExecutor());
    }
    if (configurer.getTimeout() != null) {
        adapter.setAsyncRequestTimeout(configurer.getTimeout());
    }
    adapter.setCallableInterceptors(configurer.getCallableInterceptors());
    adapter.setDeferredResultInterceptors(configurer.getDeferredResultInterceptors());

    return adapter;
}

```

这两个方法就是帮助我们完成了<mvc:annotation-driven />的功能，帮助我们注入`defaultServletHandlerMapping`映射器处理器和`RequestMappingHandlerAdapter`处理器适配器，因此我们只需要在我们核心配置类`SpringMVCConfig`上添加@EnableWebMVC，即可完成对`<mvc:annotation-driven />`的注解配置

```java
//声明当前是一个配置类
@Configuration
//扫描注解
@ComponentScan("com.example.controller")
@EnableWebMvc
public class SpringMvcConfig {

    @Bean   //不指定名字默认以方法名作为Bean的名称
    public CommonsMultipartResolver multipartResolver(){
        CommonsMultipartResolver commonsMultipartResolver
                = new CommonsMultipartResolver();
        commonsMultipartResolver.setDefaultEncoding("UTF-8");
        commonsMultipartResolver.setMaxUploadSizePerFile(1048576);
        commonsMultipartResolver.setMaxUploadSize(3145728);
        commonsMultipartResolver.setMaxInMemorySize(1048576);
        return commonsMultipartResolver;
    }
}
```





**`<mvc:default-servlet-handler />`的全注释：**

从上面可以知道，我们只需要自己定义一个类，并实现`WebMvcConfigurer`接口，spring就会自动帮我们去进行注入，并且`DelegatingWebMvcConfiguration`会根据注入的`WebMvcConfigurer`类的对象中的配置，帮我们去实现特定的功能

我们可以看到`WebMvcConfigurer`有一个叫做配置默认处理器的方法`configureDefaultServletHandling`

```java
default void configureDefaultServletHandling(DefaultServletHandlerConfigurer configurer) {
}
```

在其参数`DefaultServletHandlerConfigurer`中有一个`enable`方法，只要将`enable`设置为true，即开启了默认处理器

```java
public class DefaultServletHandlerConfigurer {
    //省略……
    
	public void enable() {
		enable(null);
	}
    
    //省略……
}
```

我们自定义一个类`MyWebMvcConfigurer`，并实现`WebMvcConfigurer`接口，然后重写`configureDefaultServletHandling`方法，开启处理静态资源的默认处理器

```java
@Component
public class MyWebMvcConfigurer implements WebMvcConfigurer {
    @Override
    public void configureDefaultServletHandling(DefaultServletHandlerConfigurer configurer) {
        //开启DefaultServlet 可以处理静态资源
        configurer.enable();
    }
}

```

我们将`MyWebMvcConfigurer`放到config目录下，为了能够被spring扫到，并注入，我们需要指定一下扫描

```java
@Configuration
//扫描注解
@ComponentScan({"com.example.controller","com.example.config"})
@EnableWebMvc
public class SpringMvcConfig {

    @Bean   //不指定名字默认以方法名作为Bean的名称
    public CommonsMultipartResolver multipartResolver(){
        CommonsMultipartResolver commonsMultipartResolver
                = new CommonsMultipartResolver();
        commonsMultipartResolver.setDefaultEncoding("UTF-8");
        commonsMultipartResolver.setMaxUploadSizePerFile(1048576);
        commonsMultipartResolver.setMaxUploadSize(3145728);
        commonsMultipartResolver.setMaxInMemorySize(1048576);
        return commonsMultipartResolver;
    }
}
```





 **<mvc:interceptor > 的全注解：**

同理我们是用`WebMvcConfigurer`接口中的addInterceptors方法来实现的

```java
default void addInterceptors(InterceptorRegistry registry) {
}
```

我们在`MyWebMvcConfigurer`类中重写`addInterceptors`方法，并调用其参数`InterceptorRegistry`的`addInterceptor`方法添加过滤器，配置完成后，接下去`DelegatingWebMvcConfiguration`会自动帮我们的注册配置我们添加的过滤器

```java
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
        //其执行顺序和这里的配置顺序一样
        registry.addInterceptor(new MyInterceptor()).addPathPatterns("/**");
    }
}

```



到这里我们已经通过注解的方式，以核心配置类把spirng-mvc.xml配置文件替代了。



#### 2、DispatcherServlet加载核心配置类

DispatcherServlet在进行SpringMVC配置文件（spring-mvc.xml）加载的时候，使用的是以下方法：

 在web.xml中进行配置：

```xml
<web-app xmlns="http://xmlns.jcp.org/xml/ns/javaee"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://xmlns.jcp.org/xml/ns/javaee http://xmlns.jcp.org/xml/ns/javaee/web-app_4_0.xsd"
         version="4.0">

  <!-- 配置ContextLoaderListener-->
  <context-param>
    <param-name>contextConfigLocation</param-name>
    <param-value>classpath:applicationContext.xml</param-value>
  </context-param>
  <listener>
    <listener-class>org.springframework.web.context.ContextLoaderListener</listener-class>
  </listener>

  <!--
    配置DispatcherServlet
    引入spring-mvc依赖后可以直接获取该类全限定名称
  -->
  <servlet>
    <servlet-name>DispatcherServlet</servlet-name>
    <servlet-class>org.springframework.web.servlet.DispatcherServlet</servlet-class>
    <!--
              指定springMVC配置文件的位置
              通过getServletConfig().getInitParameter("initParam")的方式获取;
          -->
    <init-param>
      <param-name>contextConfigLocation</param-name>
      <param-value>classpath:spring-mvc.xml</param-value>
    </init-param>
    <!--服务器启动就创建-->
    <load-on-startup>2</load-on-startup>
  </servlet>
  <!--
    配置映射地址
    这里默认所有的请求都经过它，然后由它去分发到对应的控制器中
  -->
  <servlet-mapping>
    <servlet-name>DispatcherServlet</servlet-name>
    <url-pattern>/</url-pattern>
  </servlet-mapping>
</web-app>

```



现在是使用SpringMVCConfig核心配置类提替代的spring-mvc.xml，怎么加载呢？参照Spring的ContextLoaderListener加载核心配置类的做法，定义了一个AnnotationConfigWebApplicationContext，通过代码注册核心配置类



我们可以在`DispatcherServlet`类的构造器上面看到，有一个叫`setContextClass`的方法，我们可以通过初始化参数（init-param）contextClass去指定我们spring容器的加载方式，默认的方式是（xml方式）XmlWebApplicationContext，我们也可以指定（注解方式）AnnotationConfigWebApplicationContext

![image-20231003140106306](Spring%EF%BC%88%E4%B8%89%EF%BC%89.assets/image-20231003140106306.png)



我们不可能自己去重新写一个AnnotationConfigWebApplicationContext类（注解方式加载spring容器），我们可以写一个子类去继承他，然后调用该父类的register方法，把我们的核心配置类注入进去

```java
public class MyAnnotationConfigWebApplicationContext extends AnnotationConfigWebApplicationContext {
    public MyAnnotationConfigWebApplicationContext(){
        //注册核心配置类
        super.register(SpringMvcConfig.class);
    }
}
```

通过在web.xml中配置初始化参数contextClass去加载核心配置类

```xml
<web-app xmlns="http://xmlns.jcp.org/xml/ns/javaee"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://xmlns.jcp.org/xml/ns/javaee http://xmlns.jcp.org/xml/ns/javaee/web-app_4_0.xsd"
         version="4.0">

  <!-- 配置ContextLoaderListener-->
  <context-param>
    <param-name>contextConfigLocation</param-name>
    <param-value>classpath:applicationContext.xml</param-value>
  </context-param>
  <listener>
    <listener-class>org.springframework.web.context.ContextLoaderListener</listener-class>
  </listener>

  <!--
    配置DispatcherServlet
    引入spring-mvc依赖后可以直接获取该类全限定名称
  -->
  <servlet>
    <servlet-name>DispatcherServlet</servlet-name>
    <servlet-class>org.springframework.web.servlet.DispatcherServlet</servlet-class>
    <!--
              指定springMVC配置文件的位置
              通过getServletConfig().getInitParameter("initParam")的方式获取;
          -->
    <!--<init-param>
      <param-name>contextConfigLocation</param-name>
      <param-value>classpath:spring-mvc.xml</param-value>
    </init-param>-->
    <!--加载springMVC核心配置类-->
    <init-param>
      <param-name>contextClass</param-name>
      <param-value>com.example.config.MyAnnotationConfigWebApplicationContext</param-value>
    </init-param>
    
    <!--服务器启动就创建-->
    <load-on-startup>2</load-on-startup>
  </servlet>
  <!--
    配置映射地址
    这里默认所有的请求都经过它，然后由它去分发到对应的控制器中
  -->
  <servlet-mapping>
    <servlet-name>DispatcherServlet</servlet-name>
    <url-pattern>/</url-pattern>
  </servlet-mapping>
</web-app>

```



我们启动服务，访问对应的url，可以看到显示成功，即表示我们DispatcherServlet加载核心配置类和spring-mvc.xml 中组件转化为注解形式没有问题

![image-20231003141618975](Spring%EF%BC%88%E4%B8%89%EF%BC%89.assets/image-20231003141618975.png)

#### 3、消除web.xml

当前基本上消除了配置文件(spring-mvc.xml)，但是web工程的入口还是使用的web.xml进行配置

```java
<!--配置springMVC前端控制器-->
<servlet>
     <servlet-name>DispatcherServlet</servlet-name>
     <servlet-class>org.springframework.web.servlet.DispatcherServlet</servlet-class>
     <!--指定springMVC的applicationContext全限定名 -->
     <init-param>
         <param-name>contextClass</param-name>
         <param-value>com.itheima.config.MyAnnotationConfigWebApplicationContext</param-value>
     </init-param>
     <!--服务器启动就创建-->
     <load-on-startup>2</load-on-startup>
</servlet>
<servlet-mapping>
     <servlet-name>DispatcherServlet</servlet-name>
     <url-pattern>/</url-pattern>
</servlet-mapping>
```



- Servlet3.0环境中，web容器提供了javax.servlet.ServletContainerInitializer接口，实现了该接口后，在对应的类加载路径的META-INF/services 目录创建一个名为javax.servlet.ServletContainerInitializer的文件，文件内容指定具体的ServletContainerInitializer实现类，那么，当web容器启动时就会运行这个初始化器做一些组件内的初始化工作；

**`ServletContainerInitializer`源码：**

```java
package javax.servlet;

import java.util.Set;

/**
 * Interface which allows a library/runtime to be notified of a web
 * application's startup phase and perform any required programmatic
 * registration of servlets, filters, and listeners in response to it.
 *
 * <p>Implementations of this interface may be annotated with
 * {@link javax.servlet.annotation.HandlesTypes HandlesTypes}, in order to
 * receive (at their {@link #onStartup} method) the Set of application
 * classes that implement, extend, or have been annotated with the class
 * types specified by the annotation.
 * 
 * <p>If an implementation of this interface does not use <tt>HandlesTypes</tt>
 * annotation, or none of the application classes match the ones specified
 * by the annotation, the container must pass a <tt>null</tt> Set of classes
 * to {@link #onStartup}.
 *
 * <p>When examining the classes of an application to see if they match
 * any of the criteria specified by the <tt>HandlesTypes</tt> annotation
 * of a <tt>ServletContainerInitializer</tt>, the container may run into
 * classloading problems if any of the application's optional JAR
 * files are missing. Because the container is not in a position to decide
 * whether these types of classloading failures will prevent
 * the application from working correctly, it must ignore them,
 * while at the same time providing a configuration option that would
 * log them. 
 *
 * <p>Implementations of this interface must be declared by a JAR file
 * resource located inside the <tt>META-INF/services</tt> directory and
 * named for the fully qualified class name of this interface, and will be 
 * discovered using the runtime's service provider lookup mechanism
 * or a container specific mechanism that is semantically equivalent to
 * it. In either case, <tt>ServletContainerInitializer</tt> services from web
 * fragment JAR files excluded from an absolute ordering must be ignored,
 * and the order in which these services are discovered must follow the
 * application's classloading delegation model.
 *
 * @see javax.servlet.annotation.HandlesTypes
 *
 * @since Servlet 3.0
 */
public interface ServletContainerInitializer {

    /**
     * Notifies this <tt>ServletContainerInitializer</tt> of the startup
     * of the application represented by the given <tt>ServletContext</tt>.
     *
     * <p>If this <tt>ServletContainerInitializer</tt> is bundled in a JAR
     * file inside the <tt>WEB-INF/lib</tt> directory of an application,
     * its <tt>onStartup</tt> method will be invoked only once during the
     * startup of the bundling application. If this
     * <tt>ServletContainerInitializer</tt> is bundled inside a JAR file
     * outside of any <tt>WEB-INF/lib</tt> directory, but still
     * discoverable as described above, its <tt>onStartup</tt> method
     * will be invoked every time an application is started.
     *
     * @param c the Set of application classes that extend, implement, or
     * have been annotated with the class types specified by the 
     * {@link javax.servlet.annotation.HandlesTypes HandlesTypes} annotation,
     * or <tt>null</tt> if there are no matches, or this
     * <tt>ServletContainerInitializer</tt> has not been annotated with
     * <tt>HandlesTypes</tt>
     *
     * @param ctx the <tt>ServletContext</tt> of the web application that
     * is being started and in which the classes contained in <tt>c</tt>
     * were found
     *
     * @throws ServletException if an error has occurred
     */
    public void onStartup(Set<Class<?>> c, ServletContext ctx)
        throws ServletException; 
}

```



删除web.xml文件

![image-20231003144757121](Spring%EF%BC%88%E4%B8%89%EF%BC%89.assets/image-20231003144757121.png)





创建一个实现ServletContainerInitializer的自定义类

```java
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
@HandlesTypes(QuickService.class)
public class MyServletContainerInitializer implements ServletContainerInitializer {
    @Override
    public void onStartup(Set<Class<?>> c, ServletContext ctx) throws ServletException {
        System.out.println("MyServletContainerInitializer running……");
    }
}

```

在resources下创建一个META-INF/service目录，然后在该目录下创建一个com.example.init.ServletContainerInitializer的文件，在文件中写入我们实现ServletContainerInitializer的自定义类的全限定名，引用服务器会根据我们这里的配置，找到我们自定义类，然后调用其onStartup方法

![image-20231003150818400](Spring%EF%BC%88%E4%B8%89%EF%BC%89.assets/image-20231003150818400.png)



我们打断点运行，可以看到成功进来了，并且第一个参数c是我们通过@HandlesTypes注解指定的接口的实现类，第二个参数ctx是web应用上下文

![image-20231003150949097](Spring%EF%BC%88%E4%B8%89%EF%BC%89.assets/image-20231003150949097.png)



并且我们可以通过web上下文ctx的特点方法，去进行添加sevlet、filter、listener等，如下所示

```
ctx.addFilter();
ctx.addListener();
ctx.addServlet();
```



- 基于这个特性，Spring就定义了一个SpringServletContainerInitializer实现了ServletContainerInitializer接口;

我们上面是通过自定义类`MyServletContainerInitializer`去实现`ServletContainerInitializer`接口，而spring也提供了一个实现`ServletContainerInitializer`接口的类`SpringServletContainerInitializer**`**

**`SpringServletContainerInitializer`源码：**

```java
package org.springframework.web;

@HandlesTypes(WebApplicationInitializer.class)
public class SpringServletContainerInitializer implements ServletContainerInitializer {
	@Override
	public void onStartup(@Nullable Set<Class<?>> webAppInitializerClasses, ServletContext servletContext)
			throws ServletException {

		List<WebApplicationInitializer> initializers = Collections.emptyList();

		if (webAppInitializerClasses != null) {
			initializers = new ArrayList<>(webAppInitializerClasses.size());
			for (Class<?> waiClass : webAppInitializerClasses) {
				// Be defensive: Some servlet containers provide us with invalid classes,
				// no matter what @HandlesTypes says...
				if (!waiClass.isInterface() && !Modifier.isAbstract(waiClass.getModifiers()) &&
						WebApplicationInitializer.class.isAssignableFrom(waiClass)) {
					try {
						initializers.add((WebApplicationInitializer)
								ReflectionUtils.accessibleConstructor(waiClass).newInstance());
					}
					catch (Throwable ex) {
						throw new ServletException("Failed to instantiate WebApplicationInitializer class", ex);
					}
				}
			}
		}

		if (initializers.isEmpty()) {
			servletContext.log("No Spring WebApplicationInitializer types detected on classpath");
			return;
		}

		servletContext.log(initializers.size() + " Spring WebApplicationInitializers detected on classpath");
		AnnotationAwareOrderComparator.sort(initializers);
		for (WebApplicationInitializer initializer : initializers) {
			initializer.onStartup(servletContext);
		}
	}

}

```



我可以找到其对应的包`package org.springframework.web;`的下的META-INF的services中的`javax.servlet.ServletContainerInitializer`，其就对应我们上面写的，其中存放的就是实现ServletContainerInitializer的自定义类的全限定名

![image-20231003184438901](Spring%EF%BC%88%E4%B8%89%EF%BC%89.assets/image-20231003184438901.png)

![image-20231003184603537](Spring%EF%BC%88%E4%B8%89%EF%BC%89.assets/image-20231003184603537.png)

因此应用服务启动后，会自动帮我们去调用SpringServletContainerInitializer中的onStartup方法

并且会将`WebApplicationInitializer`的子类全部注入给参数`webAppInitializerClasses`

![image-20231003185121931](Spring%EF%BC%88%E4%B8%89%EF%BC%89.assets/image-20231003185121931.png)



我们可以看到其有4个子类

![image-20231003185426205](Spring%EF%BC%88%E4%B8%89%EF%BC%89.assets/image-20231003185426205.png)

到这里可以看出，spring已经帮我们完成了相应的配置，我们只需要去调用重写其中的方法即可



- 而SpringServletContainerInitializer会查找实现了WebApplicationInitializer的类，Spring又提供了一个WebApplicationInitializer的基础实现类AbstractAnnotationConfigDispatcherServletInitializer，当我们编写类继承AbstractAnnotationConfigDispatcherServletInitializer时，容器就会自动发现我们自己的类，在该类中我们就可以配置Spring和SpringMVC的入口了。

`AbstractAnnotationConfigDispatcherServletInitializer`类中有几个方法我们可以注意下：

![image-20231003214534610](Spring%EF%BC%88%E4%B8%89%EF%BC%89.assets/image-20231003214534610.png)

`createRootApplicationContext`：

该方法用于注册根容器（父容器，Spring容器），在springmvc中注入的时候一开始是在springmvc容器中找，springmvc容器找不到，再去其父容器（spring容器）中找

```java
@Override
@Nullable
protected WebApplicationContext createRootApplicationContext() {
    Class<?>[] configClasses = getRootConfigClasses();
    if (!ObjectUtils.isEmpty(configClasses)) {
        AnnotationConfigWebApplicationContext context = new AnnotationConfigWebApplicationContext();
        context.register(configClasses);
        return context;
    }
    else {
        return null;
    }
}
```



`createServletApplicationContext`：

创建springmvc容器

```java
@Override
protected WebApplicationContext createServletApplicationContext() {
    AnnotationConfigWebApplicationContext context = new AnnotationConfigWebApplicationContext();
    Class<?>[] configClasses = getServletConfigClasses();
    if (!ObjectUtils.isEmpty(configClasses)) {
        context.register(configClasses);
    }
    return context;
}
```



`getRootConfigClasses`：

获取根的配置字节码文件

这里是初始化时候，配置Spring容器的核心配置类

```java
@Nullable
protected abstract Class<?>[] getRootConfigClasses();
```



`getServletConfigClasses`：

这里是初始化时候，配置SpringMVC容器的核心配置类

```java
@Nullable
protected abstract Class<?>[] getServletConfigClasses();
```



我们还需要配置前端控制器的映射路径，其配置是在`AbstractAnnotationConfigDispatcherServletInitializer`的父类`AbstractDispatcherServletInitializer`中的`getServletMappings`方法

```
protected abstract String[] getServletMappings();
```



因此我们只需要创建一个继承AbstractAnnotationConfigDispatcherServletInitializer类的自定义类，并实现上面两个抽象方法，去配置Spring容器或SpringMVC容器，然后再配置前端控制器的映射路径，即可消除web.xml的配置

首先创建一个Spring的核心配置类

```java
@Configuration
@ComponentScan("com.example.service")
public class SpringConfig {
}

```

创建继承AbstractAnnotationConfigDispatcherServletInitializer类的自定义类，并重写其中的方法

```java
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

```

由于该类是继承了WebApplicationInitializer，因此会自动进行注入到onStartup方法的参数中。



到这里，我们就消去了web.xml的配置，我们重启一下服务，看是否能够访问

可以看到访问成功了！即我们成功消去了web.xml的配置

![image-20231003222112521](Spring%EF%BC%88%E4%B8%89%EF%BC%89.assets/image-20231003222112521.png)



### 六、SpringMVC的组件原理剖析

#### 1、前端控制器初始化

**概念：**

前端控制器DispatcherServlet是SpringMVC的入口，也是SpringMVC的大脑，主流程的工作都是在此完成的，梳理一下DispatcherServlet 代码。DispatcherServlet 本质是个Servlet，当配置了 load-on-startup 时，会在服务器启动时就执行创建和执行初始化init方法，每次请求都会执行service方法

DispatcherServlet 的初始化主要做了两件事：

- 获得了一个 SpringMVC 的 ApplicationContext容器；
- 注册了 SpringMVC的 九大组件。

**DispatcherServlet继承体系：**

![image-20231003224211358](Spring%EF%BC%88%E4%B8%89%EF%BC%89.assets/image-20231003224211358.png)





**源码分析：**

**（1）DispatcherServlet 的初始化**

DispatcherServlet 本质是个Servlet，当Servlet创建完成后并配置了 load-on-startup 时，就会调用其init方法

servlet中的init方法：

```java
 public void init(ServletConfig config) throws ServletException;
```

而在servlet的子类中回去调用无参的init方法，并且该无参的init方法什么都不做，然后让子类去覆盖它

如GericServlet中的init方法如下，其中又调用了一个无参的init方法：

```java
public void init(ServletConfig config) throws ServletException {
	this.config = config;
	this.init();
}
```

无参的init方法是一个空方法，让子类去实现覆盖

```java
public void init() throws ServletException {

}
```



DispatcherServlet是servlet的子类，也是GericServlet的子类，GericServlet的子类中肯定会实现覆盖这个无参的init方法，因此我们先找DispatcherServlet对应的无参init方法，其中有其初始化的信息

在DispatcherServlet中是没有无参的init方法的，我们就去其父类中查找

我们最终可以在DispatcherServlet的间接父类HttpServletBean中找到无参的init方法，其最终调用了一个initServletBean的方法，这个方法上面有一个注释，大致意思是让后代去做他们向做的事情

```java
@Override
public final void init() throws ServletException {

    // Set bean properties from init parameters.
    PropertyValues pvs = new ServletConfigPropertyValues(getServletConfig(), this.requiredProperties);
    if (!pvs.isEmpty()) {
        try {
            BeanWrapper bw = PropertyAccessorFactory.forBeanPropertyAccess(this);
            ResourceLoader resourceLoader = new ServletContextResourceLoader(getServletContext());
            bw.registerCustomEditor(Resource.class, new ResourceEditor(resourceLoader, getEnvironment()));
            initBeanWrapper(bw);
            bw.setPropertyValues(pvs, true);
        }
        catch (BeansException ex) {
            if (logger.isErrorEnabled()) {
                logger.error("Failed to set bean properties on servlet '" + getServletName() + "'", ex);
            }
            throw ex;
        }
    }

    // Let subclasses do whatever initialization they like.
    initServletBean();
}
```

我们查看HttpServletBean中的initServletBean方法，是一个空方法，因此可以推断出是其子类FrameworkServlet去重写该方法

```java
protected void initServletBean() throws ServletException {
	}
```

我们查看FrameworkServlet的initServletBean方法

```java
@Override
protected final void initServletBean() throws ServletException {
   getServletContext().log("Initializing Spring " + getClass().getSimpleName() + " '" + getServletName() + "'");
   if (logger.isInfoEnabled()) {
      logger.info("Initializing Servlet '" + getServletName() + "'");
   }
   long startTime = System.currentTimeMillis();

   try {
      this.webApplicationContext = initWebApplicationContext();
      initFrameworkServlet();
   }
   catch (ServletException | RuntimeException ex) {
      logger.error("Context initialization failed", ex);
      throw ex;
   }

   if (logger.isDebugEnabled()) {
      String value = this.enableLoggingRequestDetails ?
            "shown which may lead to unsafe logging of potentially sensitive data" :
            "masked to prevent unsafe logging of potentially sensitive data";
      logger.debug("enableLoggingRequestDetails='" + this.enableLoggingRequestDetails +
            "': request parameters and headers will be " + value);
   }

   if (logger.isInfoEnabled()) {
      logger.info("Completed initialization in " + (System.currentTimeMillis() - startTime) + " ms");
   }
}
```

我们可以看到上面有一个创建web中spring容器的语句，其结果是`WebApplicationContext webApplicationContext`

```java
this.webApplicationContext = initWebApplicationContext();
```

在该方法内，通过如下的语句创建spring容器，和我们之前创建spring容器的方法一样

![image-20231003230948017](Spring%EF%BC%88%E4%B8%89%EF%BC%89.assets/image-20231003230948017.png)

如果web的spring容器是空的，即wac == null

![image-20231003232328233](Spring%EF%BC%88%E4%B8%89%EF%BC%89.assets/image-20231003232328233.png)

执行如下语句，我们可以看到其将我们的spring容器作为参数传入

```java
if (wac == null) {
    // No context instance is defined for this servlet -> create a local one
    wac = createWebApplicationContext(rootContext);
}
```

进入该方法，我们可以看到该方法将spring容器设置为当前web中的spring容器（springmvc容器）的父容器，这里是通过属性的方式进行关联的，那么之后如果在springmvc容器中找不到某一个bean，那么就会通过getParent，去从spring容器中查找对应的bean，通过这个就可体现spring容器和springmvc容器是父子容器的概念

![image-20231003232423475](Spring%EF%BC%88%E4%B8%89%EF%BC%89.assets/image-20231003232423475.png)



我们通过打断点的方式，跑一边场景web的spring容器的过程

到这个过程时，spring容器已经创建完成，并且webApplicationContext也已经被注入spring容器（由于我们之前`MyAbstractAnnotationConfigDispatcherServletInitializer`中，帮我们创建了springmvc的容器）

![image-20231004000353268](Spring%EF%BC%88%E4%B8%89%EF%BC%89.assets/image-20231004000353268.png)

之后到达这里

![image-20231004000824300](Spring%EF%BC%88%E4%B8%89%EF%BC%89.assets/image-20231004000824300.png)

其已经将我们的spring容器以parent参数的形式关联到了springmvc容器中

![image-20231004000922291](Spring%EF%BC%88%E4%B8%89%EF%BC%89.assets/image-20231004000922291.png)

**（2）DispatcherServlet 注册九大组件**

无论webApplicationContext是否存在，都会执行`configureAndRefreshWebApplicationContext(cwac)`方法

webApplicationContext存在：

进入if语句执行configureAndRefreshWebApplicationContext方法

![image-20231004004152148](Spring%EF%BC%88%E4%B8%89%EF%BC%89.assets/image-20231004004152148.png)



webApplicationContext不存在：

![image-20231004014045929](Spring%EF%BC%88%E4%B8%89%EF%BC%89.assets/image-20231004014045929.png)

![image-20231004014059450](Spring%EF%BC%88%E4%B8%89%EF%BC%89.assets/image-20231004014059450.png)



我们进入`configureAndRefreshWebApplicationContext`方法中，可以在其后面找到一个叫做`refresh`的方法

![image-20231004152512875](Spring%EF%BC%88%E4%B8%89%EF%BC%89.assets/image-20231004152512875.png)

点击`refresh`方法，然后进入该方法的实现类

![image-20231004152600494](Spring%EF%BC%88%E4%B8%89%EF%BC%89.assets/image-20231004152600494.png)

其实现类源码如下：

```java
@Override
	public void refresh() throws BeansException, IllegalStateException {
		synchronized (this.startupShutdownMonitor) {
			StartupStep contextRefresh = this.applicationStartup.start("spring.context.refresh");

			// Prepare this context for refreshing.
			prepareRefresh();

			// Tell the subclass to refresh the internal bean factory.
            //创建一个beanFactory
			ConfigurableListableBeanFactory beanFactory = obtainFreshBeanFactory();

			// Prepare the bean factory for use in this context.
            //准备工作
			prepareBeanFactory(beanFactory);

			try {
				// Allows post-processing of the bean factory in context subclasses.
				//执行Bean的后处理器
                postProcessBeanFactory(beanFactory);

				StartupStep beanPostProcess = this.applicationStartup.start("spring.context.beans.post-process");
				// Invoke factory processors registered as beans in the context.
				//调用在上下文中注册为 Bean 的工厂后置处理器，例如实现了 BeanFactoryPostProcessor 接口的类
                invokeBeanFactoryPostProcessors(beanFactory);

				// Register bean processors that intercept bean creation.
                //注册 Bean 后置处理器
				registerBeanPostProcessors(beanFactory);
				beanPostProcess.end();

				// Initialize message source for this context.
                //初始化消息源，用于国际化和本地化
				initMessageSource();

				// Initialize event multicaster for this context.
                //初始化事件广播器，用于发布和监听应用程序事件
				initApplicationEventMulticaster();

				// Initialize other special beans in specific context subclasses.
                //在特定的上下文子类中初始化其他特殊的 Bean
				onRefresh();

				// Check for listener beans and register them.
                //注册应用程序事件监听器，用于监听并响应应用程序的各种事件
				registerListeners();

				// Instantiate all remaining (non-lazy-init) singletons.
                //实例化所有剩余的非懒加载单例 Bean
				finishBeanFactoryInitialization(beanFactory);

				// Last step: publish corresponding event.
                //刷新应用程序上下文完成，发布相应的事件
				finishRefresh();
			}

			catch (BeansException ex) {
				if (logger.isWarnEnabled()) {
					logger.warn("Exception encountered during context initialization - " +
							"cancelling refresh attempt: " + ex);
				}

				// Destroy already created singletons to avoid dangling resources.
				destroyBeans();

				// Reset 'active' flag.
				cancelRefresh(ex);

				// Propagate exception to caller.
				throw ex;
			}

			finally {
				// Reset common introspection caches in Spring's core, since we
				// might not ever need metadata for singleton beans anymore...
				resetCommonCaches();
				contextRefresh.end();
			}
		}
	}
```

我们看`finishRefresh`方法，到达该方法，说明容器已经加载完了，基本的初始化都完成了

我们可以看到其中有一个叫publishEvent方法，即发布一个ContextRefreshedEvent事件，即上下文刷新完毕事件。发布了该事件后，只要涉及ContextRefreshedEvent事件的监听都会执行（spring中有一个监听机制，但和我们web中的监听机制的实现技术是不一样的，意思是差不多的，都是监听某一个事件的发生）

```java
protected void finishRefresh() {
    // Clear context-level resource caches (such as ASM metadata from scanning).
    clearResourceCaches();

    // Initialize lifecycle processor for this context.
    initLifecycleProcessor();

    // Propagate refresh to lifecycle processor first.
    getLifecycleProcessor().onRefresh();

    // Publish the final event.
    publishEvent(new ContextRefreshedEvent(this));

    // Participate in LiveBeansView MBean, if active.
    if (!NativeDetector.inNativeImage()) {
        LiveBeansView.registerApplicationContext(this);
    }
}
```

我们可以在`FrameworkServlet`类中找到一个内部类叫`ContextRefreshListener`

![image-20231004153421921](Spring%EF%BC%88%E4%B8%89%EF%BC%89.assets/image-20231004153421921.png)

该内部类ContextRefreshListener实现了ApplicationListener接口，说明其是一个监听器，并且其泛型是ContextRefreshedEvent，表示监听的事件是ContextRefreshedEvent，只要有地方发布了ContextRefreshedEvent事件，就会执行其中的onApplicationEvent方法

```java
private class ContextRefreshListener implements ApplicationListener<ContextRefreshedEvent> {

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
    	FrameworkServlet.this.onApplicationEvent(event);
    }
}
```



因此我们在`finishRefresh`方法中执行` publishEvent(new ContextRefreshedEvent(this));`，会调用该监听事件的`onApplicationEvent`方法

我们看下这`onApplicationEvent`方法

```java
public void onApplicationEvent(ContextRefreshedEvent event) {
    this.refreshEventReceived = true;
    synchronized (this.onRefreshMonitor) {
        onRefresh(event.getApplicationContext());
    }
}
```

其中调用了一个`onRefresh`方法，其还是在`FrameworkServlet`类中，我们继续点击查看，可以看到其默认什么都没有写，交给子类去腹泻

```java
protected void onRefresh(ApplicationContext context) {
    // For subclasses: do nothing by default.
}
```

因此我们去`FrameworkServlet`类的子类`DispatcherServlet`中查看该方法，可以看到其中调用了一个`initStrategies`方法

```java
@Override
protected void onRefresh(ApplicationContext context) {
    initStrategies(context);
}
```

我们进入`initStrategies`方法，可以看到其中注册了9个组件，分别对应MVC的9个组件

```java
protected void initStrategies(ApplicationContext context) {
    //初始化处理文件上传的解析器，用于处理多部分请求中的文件上传
    initMultipartResolver(context);
    //初始化处理本地化的解析器，用于解析客户端发送的本地化信息
    initLocaleResolver(context);
    //初始化处理主题的解析器，用于解析客户端请求所使用的主题
    initThemeResolver(context);
    //初始化处理器映射器，用于将请求映射到相应的处理器
    initHandlerMappings(context);
    //初始化处理器适配器，用于将处理器包装成可以处理具体请求的适配器
    initHandlerAdapters(context);
    //初始化处理器异常解析器，用于处理请求过程中发生的异常
    initHandlerExceptionResolvers(context);
    //初始化请求到视图名称的转换器，用于根据请求生成相应的视图名称
    initRequestToViewNameTranslator(context);
    //初始化视图解析器，用于将视图名称解析为具体的视图实现
    initViewResolvers(context);
    //初始化FlashMap管理器，用于处理Flash属性（在重定向之间共享数据）
    initFlashMapManager(context);
}
```



**（3）处理器映射器处理细节**

我们进入`initHandlerMappings`方法，去看下处理器映射器的处理细节

其源码如下：

```java
//定义List容器存储HandlerMapping
private List<HandlerMapping> handlerMappings;

//初始化HandlerMapping的方法
private void initHandlerMappings(ApplicationContext context) {
		//初始化集合为null
    	this.handlerMappings = null;
		//detectAllHandlerMappings默认为true，代表是否从所有容器中(父子容器)检测HandlerMapping
		if (this.detectAllHandlerMappings) {
			// Find all HandlerMappings in the ApplicationContext, including ancestor contexts.
			//从Spring容器中去匹配HandlerMapping
            Map<String, HandlerMapping> matchingBeans =
					BeanFactoryUtils.beansOfTypeIncludingAncestors(context, HandlerMapping.class, true, false);
			//如果从容器中获取的HandlerMapping不为null就加入到事先定义好的handlerMappings容器中
            if (!matchingBeans.isEmpty()) {
				this.handlerMappings = new ArrayList<>(matchingBeans.values());
				// We keep HandlerMappings in sorted order.
				AnnotationAwareOrderComparator.sort(this.handlerMappings);
			}
		}
		else {
			try {
				HandlerMapping hm = context.getBean(HANDLER_MAPPING_BEAN_NAME, HandlerMapping.class);
				this.handlerMappings = Collections.singletonList(hm);
			}
			catch (NoSuchBeanDefinitionException ex) {
				// Ignore, we'll add a default HandlerMapping later.
			}
		}

		// Ensure we have at least one HandlerMapping, by registering
		// a default HandlerMapping if no other mappings are found.
		//如果从容器中没有获得HandlerMapping，意味着handlerMappings集合是空的
    	if (this.handlerMappings == null) {
            //加载默认的HandlerMapping，就是加载DispatcherServlet.properties文件中的键值对
			this.handlerMappings = getDefaultStrategies(context, HandlerMapping.class);
			if (logger.isTraceEnabled()) {
				logger.trace("No HandlerMappings declared for servlet '" + getServletName() +
						"': using default strategies from DispatcherServlet.properties");
			}
		}

		for (HandlerMapping mapping : this.handlerMappings) {
			if (mapping.usesPathPatterns()) {
				this.parseRequestPath = true;
				break;
			}
		}
	}
```

这一句就是从springmvc容器中去获取我们已经添加的映射器处理器的bean对象

![image-20231004155304964](Spring%EF%BC%88%E4%B8%89%EF%BC%89.assets/image-20231004155304964.png)

如果无法从springmvc容器中获取我们自己添加的映射器处理器，就去加载默认的策略，即springmvc我们默认提供的处理器映射器

![image-20231004155317290](Spring%EF%BC%88%E4%B8%89%EF%BC%89.assets/image-20231004155317290.png)

我们点进去`getDefaultStrategies`

其中配置了一个资源路径，可以看到就是我们之前`DispatcherServlet`中默认配置处理器文件的位置，因此其默认加载的时候，就是根据该默认配置文件中的内容去进行加载的

![image-20231004155906246](Spring%EF%BC%88%E4%B8%89%EF%BC%89.assets/image-20231004155906246.png)

![image-20231004155913647](Spring%EF%BC%88%E4%B8%89%EF%BC%89.assets/image-20231004155913647.png)

![image-20231004160006537](Spring%EF%BC%88%E4%B8%89%EF%BC%89.assets/image-20231004160006537.png)

![image-20231004160022646](Spring%EF%BC%88%E4%B8%89%EF%BC%89.assets/image-20231004160022646.png)



然后我们看上面的自定义的处理器映射器的注入，这是谁负责帮我们把这4个注入到spring容器的，我们并没有做注册注入的操作

![image-20231004160619646](Spring%EF%BC%88%E4%B8%89%EF%BC%89.assets/image-20231004160619646.png)

实际上是通过springmvc配置文件中的` <mvc:annotation-driven/>`来实现的

其中的处理器映射器就是在这里进行注册的

```java
<!--mvc注解驱动-->
    <mvc:annotation-driven/>
```

后来我们用了springmvc的核心配置类，也就是通过注解`@EnableWebMvc`帮我们开启的



我们再来看下`requestMappingHandleMapping`处理器映射器的细节

![image-20231004162012184](Spring%EF%BC%88%E4%B8%89%EF%BC%89.assets/image-20231004162012184.png)

其中有一个属性叫interceptor，即过滤器，其中MyInterceptor使我们自己配置的，其他两个是springmvc帮我们注入的

![image-20231004162109748](Spring%EF%BC%88%E4%B8%89%EF%BC%89.assets/image-20231004162109748.png)



![image-20231004162201307](Spring%EF%BC%88%E4%B8%89%EF%BC%89.assets/image-20231004162201307.png)



还有一个属性叫`mappingRegistry`的方法，其中就帮我们已经加载了对应的映射路径和对应的方法的匹配关系的映射信息，到时候请求的时候，就去这里面找即可。

![image-20231004162245762](Spring%EF%BC%88%E4%B8%89%EF%BC%89.assets/image-20231004162245762.png)

#### 2、前端控制器执行流程

**概述：**

上面讲解了一下，当服务器启动时，DispatcherServlet 会执行初始化操作（我们是通过Servlet的init方法去进行剖析的），接下来，每次访问都会执行service

方法，我们先宏观的看一下执行流程，在去研究源码和组件执行细节



**前端控制器执行主流程：**

![image-20231004163216773](Spring%EF%BC%88%E4%B8%89%EF%BC%89.assets/image-20231004163216773.png)

我们主要去剖析两个部分的源码

1.处理器映射器是如何加载执行链对象

![image-20231004163255322](Spring%EF%BC%88%E4%B8%89%EF%BC%89.assets/image-20231004163255322.png)

2.处理器适配器是如何匹配对应的处理器

![image-20231004163320336](Spring%EF%BC%88%E4%B8%89%EF%BC%89.assets/image-20231004163320336.png)

3.对于视图解析器，由于现在都是前后端分离的，因此就不去过多探究了



**源码分析：**

我们都以`servlet`的service方法去进行剖析

我们先在`DispatcherServlet`类中找`service`方法是没有的，因此我们去其父类`FrameworkServlet`中找,其方法如下

```JAVA
protected void service(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

    HttpMethod httpMethod = HttpMethod.resolve(request.getMethod());
    if (httpMethod == HttpMethod.PATCH || httpMethod == null) {
        processRequest(request, response);
    }
    else {
        super.service(request, response);
    }
}
```

但是我们原生的`servlet`的`service`方法的参数是带http的，说明上面的这个`service`方法并不是`servlet`原生的

```java
public void service(ServletRequest req, ServletResponse res)
	throws ServletException, IOException;
```

我们继续往其父级`HttpServlet`中找，

可以看到在其重写了父类的service中，将参数进行了一个转换，然后调用自己的service方法

```java
@Override
    public void service(ServletRequest req, ServletResponse res)
        throws ServletException, IOException
    {
        HttpServletRequest  request;
        HttpServletResponse response;
        
        if (!(req instanceof HttpServletRequest &&
                res instanceof HttpServletResponse)) {
            throw new ServletException("non-HTTP request or response");
        }

        request = (HttpServletRequest) req;
        response = (HttpServletResponse) res;

        service(request, response);
    }
```

其自己的service方法中对doGet等方法进行了重写

```java
 protected void service(HttpServletRequest req, HttpServletResponse resp)
        throws ServletException, IOException
    {
        String method = req.getMethod();

        if (method.equals(METHOD_GET)) {
            long lastModified = getLastModified(req);
            if (lastModified == -1) {
                // servlet doesn't support if-modified-since, no reason
                // to go through further expensive logic
                doGet(req, resp);
            } else {
                long ifModifiedSince = req.getDateHeader(HEADER_IFMODSINCE);
                if (ifModifiedSince < lastModified) {
                    // If the servlet mod time is later, call doGet()
                    // Round down to the nearest second for a proper compare
                    // A ifModifiedSince of -1 will always be less
                    maybeSetLastModified(resp, lastModified);
                    doGet(req, resp);
                } else {
                    resp.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
                }
            }

        } else if (method.equals(METHOD_HEAD)) {
            long lastModified = getLastModified(req);
            maybeSetLastModified(resp, lastModified);
            doHead(req, resp);

        } else if (method.equals(METHOD_POST)) {
            doPost(req, resp);
            
        } else if (method.equals(METHOD_PUT)) {
            doPut(req, resp);
            
        } else if (method.equals(METHOD_DELETE)) {
            doDelete(req, resp);
            
        } else if (method.equals(METHOD_OPTIONS)) {
            doOptions(req,resp);
            
        } else if (method.equals(METHOD_TRACE)) {
            doTrace(req,resp);
            
        } else {
            //
            // Note that this means NO servlet supports whatever
            // method was requested, anywhere on this server.
            //

            String errMsg = lStrings.getString("http.method_not_implemented");
            Object[] errArgs = new Object[1];
            errArgs[0] = method;
            errMsg = MessageFormat.format(errMsg, errArgs);
            
            resp.sendError(HttpServletResponse.SC_NOT_IMPLEMENTED, errMsg);
        }
    }
```

我们再去看`FrameworkService`中的service方法，其中就调用了super的service方法

```java
@Override
protected void service(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {

    HttpMethod httpMethod = HttpMethod.resolve(request.getMethod());
    if (httpMethod == HttpMethod.PATCH || httpMethod == null) {
        processRequest(request, response);
    }
    else {
        super.service(request, response);
    }
}
```

并且`HttpServlet`类方法中的doGet还是doPost都被`FrameworkService`重写了，并且其中都调用了一个叫做processRequest的方法

```java
@Override
protected final void doGet(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {

    processRequest(request, response);
}
```

```java
@Override
protected final void doPost(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {

    processRequest(request, response);
}

```

`processRequest`方法是在`FrameworkService`类中实现的，并且其中调用了doService方法

![image-20231004164744649](Spring%EF%BC%88%E4%B8%89%EF%BC%89.assets/image-20231004164744649.png)

doService方法在`FrameworkService`类中是一个抽象方法，具体的实现是在其子类`DispatcherServlet`中实现的

```java
protected abstract void doService(HttpServletRequest request, HttpServletResponse response)
			throws Exception;
```

`DispatcherServlet`对`FrameworkService`的`doService`方法进行了覆盖

```java
@Override
protected void doService(HttpServletRequest request, HttpServletResponse response) throws Exception {
    logRequest(request);

    // Keep a snapshot of the request attributes in case of an include,
    // to be able to restore the original attributes after the include.
    Map<String, Object> attributesSnapshot = null;
    if (WebUtils.isIncludeRequest(request)) {
        attributesSnapshot = new HashMap<>();
        Enumeration<?> attrNames = request.getAttributeNames();
        while (attrNames.hasMoreElements()) {
            String attrName = (String) attrNames.nextElement();
            if (this.cleanupAfterInclude || attrName.startsWith(DEFAULT_STRATEGIES_PREFIX)) {
                attributesSnapshot.put(attrName, request.getAttribute(attrName));
            }
        }
    }

    // Make framework objects available to handlers and view objects.
    request.setAttribute(WEB_APPLICATION_CONTEXT_ATTRIBUTE, getWebApplicationContext());
    request.setAttribute(LOCALE_RESOLVER_ATTRIBUTE, this.localeResolver);
    request.setAttribute(THEME_RESOLVER_ATTRIBUTE, this.themeResolver);
    request.setAttribute(THEME_SOURCE_ATTRIBUTE, getThemeSource());

    if (this.flashMapManager != null) {
        FlashMap inputFlashMap = this.flashMapManager.retrieveAndUpdate(request, response);
        if (inputFlashMap != null) {
            request.setAttribute(INPUT_FLASH_MAP_ATTRIBUTE, Collections.unmodifiableMap(inputFlashMap));
        }
        request.setAttribute(OUTPUT_FLASH_MAP_ATTRIBUTE, new FlashMap());
        request.setAttribute(FLASH_MAP_MANAGER_ATTRIBUTE, this.flashMapManager);
    }

    RequestPath previousRequestPath = null;
    if (this.parseRequestPath) {
        previousRequestPath = (RequestPath) request.getAttribute(ServletRequestPathUtils.PATH_ATTRIBUTE);
        ServletRequestPathUtils.parseAndCache(request);
    }

    try {
        doDispatch(request, response);
    }
    finally {
        if (!WebAsyncUtils.getAsyncManager(request).isConcurrentHandlingStarted()) {
            // Restore the original attribute snapshot, in case of an include.
            if (attributesSnapshot != null) {
                restoreAttributesAfterInclude(request, attributesSnapshot);
            }
        }
        if (this.parseRequestPath) {
            ServletRequestPathUtils.setParsedRequestPath(previousRequestPath, request);
        }
    }
}
```

而最核心的流程都是在`DispatcherServlet`的`doService`方法中的`doDispatch(request, response);`方法中

![image-20231004165009222](Spring%EF%BC%88%E4%B8%89%EF%BC%89.assets/image-20231004165009222.png)







**（1）处理器映射器是如何加载执行链对象**

首先我们可以在doDispatch方法中看到其定义了一个执行链对象，并置为null

![image-20231004165506535](Spring%EF%BC%88%E4%B8%89%EF%BC%89.assets/image-20231004165506535.png)

接着我们可以看到，其调用了一个getHandler方法，并把HttpServletRequest对象（请求的信息都在这里）作为参数传入，然后返回一个执行链对象

![image-20231004165819003](Spring%EF%BC%88%E4%B8%89%EF%BC%89.assets/image-20231004165819003.png)

我们下面通过打断点的方式来查看源码，在`mappedHandler = getHandler(processedRequest);`上打上断点

![image-20231004230653556](Spring%EF%BC%88%E4%B8%89%EF%BC%89.assets/image-20231004230653556.png)

我们通过debug启动引用服务器，访问对应的url = `http://localhost:8080/SpringMVC_war/show`

断点到这里，此时mappedHandler还是为null，因为getHandler方法还没有执行完毕

![image-20231004231540223](Spring%EF%BC%88%E4%B8%89%EF%BC%89.assets/image-20231004231540223.png)

我们进入到getHandler方法中，可以看到其对handlerMappings进行判断，如果不为空，就进行遍历，然后调用其getHandler的方法

![image-20231004234453347](Spring%EF%BC%88%E4%B8%89%EF%BC%89.assets/image-20231004234453347.png)

handlerMappings就是存放HandlerMapping的集合，其在服务器启动的时候，通过init方法，帮我们注册并填充到该集合中了（由@EnableWebMvc帮我们进行注册）

![image-20231004234652057](Spring%EF%BC%88%E4%B8%89%EF%BC%89.assets/image-20231004234652057.png)

其有四个值如下所示

![image-20231004234811142](Spring%EF%BC%88%E4%B8%89%EF%BC%89.assets/image-20231004234811142.png)

在遍历handlerMappings的时候，实际上是使用了RequestMappingHandlerMapping，我们点击getHandler方法进行查看，其是`HandlerMapping`的一个抽象方法

```java
@Nullable
HandlerExecutionChain getHandler(HttpServletRequest request) throws Exception;
```

我们点击，进入其实现类`AbstractHandlerMapping`

![image-20231004235332448](Spring%EF%BC%88%E4%B8%89%EF%BC%89.assets/image-20231004235332448.png)

其中有一个getHandlerInternal，其就是将我们当前访问url对应的处理器抽象成一个handler（封装一下？）

![image-20231004235624200](Spring%EF%BC%88%E4%B8%89%EF%BC%89.assets/image-20231004235624200.png)

之后会到达这样一个方法，其传入我们的处理器和请求参数

![image-20231005000019161](Spring%EF%BC%88%E4%B8%89%EF%BC%89.assets/image-20231005000019161.png)

经过下面这一步，我们就肯定会创建出一个HandlerExecutionChain对象，其根据handler是否是HandlerExcutionChain，来决定是直接把handler作为HandlerExecutionChain对象，还是再新建一个HandlerExecutionChain对象

![image-20231005000135866](Spring%EF%BC%88%E4%B8%89%EF%BC%89.assets/image-20231005000135866.png)

在服务器启动后，在init方法中，都会将我们的过滤器注册到一个为adaptedInterceptors的集合中

![image-20231005000329550](Spring%EF%BC%88%E4%B8%89%EF%BC%89.assets/image-20231005000329550.png)

![image-20231005000336925](Spring%EF%BC%88%E4%B8%89%EF%BC%89.assets/image-20231005000336925.png)

然后就遍历所有的过滤器

![image-20231005000555811](Spring%EF%BC%88%E4%B8%89%EF%BC%89.assets/image-20231005000555811.png)

并查看当前的请求是否与当前遍历到的过滤器匹配

![image-20231005000605611](Spring%EF%BC%88%E4%B8%89%EF%BC%89.assets/image-20231005000605611.png)

如果匹配，就将当前的过滤器加入到执行链中

![image-20231005000628763](Spring%EF%BC%88%E4%B8%89%EF%BC%89.assets/image-20231005000628763.png)

将过滤器加入到执行链对象中的过滤器集合中

![image-20231005000732006](Spring%EF%BC%88%E4%B8%89%EF%BC%89.assets/image-20231005000732006.png)

最后将该执行链返回

![image-20231005000752554](Spring%EF%BC%88%E4%B8%89%EF%BC%89.assets/image-20231005000752554.png)

最后将执行链返回给mappedHandler，即完成了执行链的封装

![image-20231005001322742](Spring%EF%BC%88%E4%B8%89%EF%BC%89.assets/image-20231005001322742.png)

**（2）处理器适配器是如何匹配对应的处理器**

获得了`HandlerExecutionChain`，最终还要进行执行

其执行靠得就是我们的`HandlerAdapter`

![image-20231005002202670](Spring%EF%BC%88%E4%B8%89%EF%BC%89.assets/image-20231005002202670.png)

该方法表示去执行Interceptor的前置方法

![image-20231005002404050](Spring%EF%BC%88%E4%B8%89%EF%BC%89.assets/image-20231005002404050.png)

该方法代表执行最终方法

![image-20231005002454967](Spring%EF%BC%88%E4%B8%89%EF%BC%89.assets/image-20231005002454967.png)

该方法代表去执行Interceptor的后置方法

![image-20231005002604052](Spring%EF%BC%88%E4%B8%89%EF%BC%89.assets/image-20231005002604052.png)

该方法代表去执行Interceptor的最终方法

![image-20231005002521258](Spring%EF%BC%88%E4%B8%89%EF%BC%89.assets/image-20231005002521258.png)

我们从上面可以看到，在执行Interceptor的前置、后置、最终方法的时候，靠的不是HandlerAdapter，只要在执行最终目标方法的时候，才是靠HandlerAdapter去完成的

我们通过断点的方式，通过访问url  =`http://localhost:8080/SpringMVC_war/param2?username=yjy&age=18`去查看

到这里的时候，就会调用HandlerAdapter对象的handle方法，帮我们调用最终目标方法

![image-20231005003135265](Spring%EF%BC%88%E4%B8%89%EF%BC%89.assets/image-20231005003135265.png)

我们进入handle方法，其是一个抽象方法

![image-20231005003213676](Spring%EF%BC%88%E4%B8%89%EF%BC%89.assets/image-20231005003213676.png)

我们选择其实现类，然后在其中打一个断点

![image-20231005003228258](Spring%EF%BC%88%E4%B8%89%EF%BC%89.assets/image-20231005003228258.png)

然后到达这里，调用了一个handleInternal方法

![image-20231005003304301](Spring%EF%BC%88%E4%B8%89%EF%BC%89.assets/image-20231005003304301.png)

我们进入handleInternal方法，并进入其对应的实现类

![image-20231005003339985](Spring%EF%BC%88%E4%B8%89%EF%BC%89.assets/image-20231005003339985.png)

进入该实现类后，我们可以看到其进入到了RequestMapingHandlerAdapter中

![image-20231005003419246](Spring%EF%BC%88%E4%B8%89%EF%BC%89.assets/image-20231005003419246.png)

之后其会调用该`invokeHandlerMethod`方法

![image-20231005003547788](Spring%EF%BC%88%E4%B8%89%EF%BC%89.assets/image-20231005003547788.png)

之后其会调用`invokeHandlerMethod`方法中的`invocableMethod.invokeAndHandle(webRequest, mavContainer)`

![image-20231005003742224](Spring%EF%BC%88%E4%B8%89%EF%BC%89.assets/image-20231005003742224.png)

在`invokeAndHandle`中调用invokeForRequest方法

![image-20231005003941948](Spring%EF%BC%88%E4%B8%89%EF%BC%89.assets/image-20231005003941948.png)

进入invokeForRequest方法，其通过`getMethodArgumentValues(request, mavContainer, providedArgs)`去获取参数信息，给args

![image-20231005004005987](Spring%EF%BC%88%E4%B8%89%EF%BC%89.assets/image-20231005004005987.png)

我们从这张图可以看出，此时的args的值，就是我们请求url时候传入的参数

![image-20231005004251749](Spring%EF%BC%88%E4%B8%89%EF%BC%89.assets/image-20231005004251749.png)

最后，其会执行doInvoke方法

![image-20231005004343846](Spring%EF%BC%88%E4%B8%89%EF%BC%89.assets/image-20231005004343846.png)

进入doInvoke方法后，会执行method.invoke(getBean(), args);方法

该方法就是通过字节码反射的方法，执行对应的方法，并把参数传入

![image-20231005004453602](Spring%EF%BC%88%E4%B8%89%EF%BC%89.assets/image-20231005004453602.png)

![image-20231005004615019](Spring%EF%BC%88%E4%B8%89%EF%BC%89.assets/image-20231005004615019.png)



### 七、SpringMVC的异常处理机制

#### 1、SpringMVC异常的处理流程

异常分为编译时异常和运行时异常，编译时异常我们 try-cache 进行捕获，捕获后自行处理，而运行时异常是不可预期的，就需要规范编码来避免，在SpringMVC 中，不管是编译异常还是运行时异常，都可以最终由SpringMVC提供的异常处理器进行统一处理，这样就避免了随时随地捕获处理的繁琐性。

当然除了繁琐之外，我们在进行前后端分离异步开发时，往往返回统一格式的结果给客户端，例如：

{"code":200,"message":"","data":{"username":"haohao","age":null}}，即使报异常了，也不能把状态码500直接扔给客户端丢给用户，需要将异常转换成符合上面格式的数据响应给客户端更友好。



SpringMVC 处理异常的思路是，一路向上抛，都抛给前端控制器 DispatcherServlet ，DispatcherServlet 在调用异常处理器ExceptionResolver进行处理，如下图（原先的异常都是在Service中通过try-catch进行处理的）：

![image-20231005005456704](Spring%EF%BC%88%E4%B8%89%EF%BC%89.assets/image-20231005005456704.png)

#### 2、SpringMVC的异常处理方式

**准备工作：**

首先我们装备一个异常处理器：

```java
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

```



**问题来由：**

我们如果直接访问，会报500错误

![image-20231005010343498](Spring%EF%BC%88%E4%B8%89%EF%BC%89.assets/image-20231005010343498.png)

![image-20231005010353631](Spring%EF%BC%88%E4%B8%89%EF%BC%89.assets/image-20231005010353631.png)

这样直接把服务端的错误回显给客户端是不友好的。





**SpringMVC 提供了以下三种处理异常的方式：**

- 简单异常处理器：使用SpringMVC 内置的异常处理器处理 SimpleMappingExceptionResolver；

我们新建一个错误页面error1.html：

![image-20231005010954388](Spring%EF%BC%88%E4%B8%89%EF%BC%89.assets/image-20231005010954388.png)

```html
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Title</title>
</head>
<body>
    <h1>对不起，网络繁忙，请稍后重试！</h1>
</body>
</html>
```

然后再springmvc核心配置类中，进行异常处理器的配置

```java
package com.example.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.handler.SimpleMappingExceptionResolver;

/**
 * @author banana
 * @create 2023-10-02 23:54
 */
//声明当前是一个配置类
@Configuration
//扫描注解
@ComponentScan({"com.example.controller","com.example.config"})
@EnableWebMvc
public class SpringMvcConfig {

    @Bean
    public SimpleMappingExceptionResolver simpleMappingExceptionResolver(){
        SimpleMappingExceptionResolver simpleMappingExceptionResolver
                = new SimpleMappingExceptionResolver();
        //不管是什么异常，都响应一个友好页面
        simpleMappingExceptionResolver.setDefaultErrorView("/error1.html");
        //将simpleMappingExceptionResolver注册到springmvc容器中
        return simpleMappingExceptionResolver;
    }



    @Bean   //不指定名字默认以方法名作为Bean的名称
    public CommonsMultipartResolver multipartResolver(){
        CommonsMultipartResolver commonsMultipartResolver
                = new CommonsMultipartResolver();
        commonsMultipartResolver.setDefaultEncoding("UTF-8");
        commonsMultipartResolver.setMaxUploadSizePerFile(1048576);
        commonsMultipartResolver.setMaxUploadSize(3145728);
        commonsMultipartResolver.setMaxInMemorySize(1048576);
        return commonsMultipartResolver;
    }
}

```

我们再次访问异常的url，可以看是显示了友好的界面

![image-20231005011210828](Spring%EF%BC%88%E4%B8%89%EF%BC%89.assets/image-20231005011210828.png)

![image-20231005011217555](Spring%EF%BC%88%E4%B8%89%EF%BC%89.assets/image-20231005011217555.png)

我们也可以区分不通的异常，根据不同的异常显示不同的友好界面

再建立一个error2.html:

```java
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Title</title>
</head>
<body>
    <h1>对不起，网络繁忙，请稍后重试！ 2222</h1>
</body>
</html>
```

修改springmvc核心配置类中的配置信息：

```java
package com.example.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.handler.SimpleMappingExceptionResolver;

import java.util.Properties;

/**
 * @author banana
 * @create 2023-10-02 23:54
 */
//声明当前是一个配置类
@Configuration
//扫描注解
@ComponentScan({"com.example.controller","com.example.config"})
@EnableWebMvc
public class SpringMvcConfig {

    @Bean
    public SimpleMappingExceptionResolver simpleMappingExceptionResolver(){
        SimpleMappingExceptionResolver simpleMappingExceptionResolver
                = new SimpleMappingExceptionResolver();
        //不管是什么异常，都响应一个友好页面
        simpleMappingExceptionResolver.setDefaultErrorView("/error1.html");
        //区分异常类型，根据不同的异常类型，可以跳转不同的视图
        //键值对：key：异常对象全限定名 value：跳转的视图
        Properties properties = new Properties();
        properties.setProperty("java.lang.RuntimeException", "/error1.html");
        properties.setProperty("java.io.FileNotFoundException", "/error2.html");
        simpleMappingExceptionResolver.setExceptionMappings(properties);
        //将simpleMappingExceptionResolver注册到springmvc容器中
        return simpleMappingExceptionResolver;
    }



    @Bean   //不指定名字默认以方法名作为Bean的名称
    public CommonsMultipartResolver multipartResolver(){
        CommonsMultipartResolver commonsMultipartResolver
                = new CommonsMultipartResolver();
        commonsMultipartResolver.setDefaultEncoding("UTF-8");
        commonsMultipartResolver.setMaxUploadSizePerFile(1048576);
        commonsMultipartResolver.setMaxUploadSize(3145728);
        commonsMultipartResolver.setMaxInMemorySize(1048576);
        return commonsMultipartResolver;
    }
}

```

结果：

![image-20231005011814486](Spring%EF%BC%88%E4%B8%89%EF%BC%89.assets/image-20231005011814486.png)



![image-20231005012022690](Spring%EF%BC%88%E4%B8%89%EF%BC%89.assets/image-20231005012022690.png)

同样，我们也可以通过xml配置的方式进行简单的异常处理器的配置，如下所示

```java
<bean class="org.springframework.web.servlet.handler.SimpleMappingExceptionResolver">
     <property name="defaultErrorView" value="/error.html"/>
     <property name="exceptionMappings">
         <props>
             <!-- 配置异常类型对应的展示视图 -->
             <prop key="java.lang.RuntimeException">/error.html</prop>
             <prop key="java.io.FileNotFoundException">/io.html</prop>
         </props>
     </property>
</bean>
```





**注意：**

如果异常即不是`java.lang.RuntimeException`也不是`java.io.FileNotFoundException`，就会根据`simpleMappingExceptionResolver.setDefaultErrorView("/error1.html");`跳转到error1.html页面。



**弊端：**

当时现在前后端分离，一般都是给前端一个json格式的响应，而不是视图，如果用简单的异常处理器就不太好搞。



- 自定义异常处理器：实现HandlerExceptionResolver接口，自定义异常进行处理；

在项目中建立一个ex包，然后在下面中创建一个MyHandlerExceptionResolver

![image-20231005113909176](Spring%EF%BC%88%E4%B8%89%EF%BC%89.assets/image-20231005113909176.png)

```java
package com.example.ex;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author banana
 * @create 2023-10-05 11:28
 */
@Component
public class MyHandlerExceptionResolver implements HandlerExceptionResolver {
    @Override
    public ModelAndView resolveException(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        //简单的响应一个友好的提示页面error1.html
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("/error1.html");

        return modelAndView;
    }
}

```

由于`MyHandlerExceptionResolver`上添加了@Component注解，我们需要告诉配置类，去扫描它

我们可以在springMVC核心配置类去中取配置，也可以去Spring核心配置类中取配置，我们这里就直接去spring核心配置类中去配置

![image-20231005114202852](Spring%EF%BC%88%E4%B8%89%EF%BC%89.assets/image-20231005114202852.png)

然后我们对异常页面进行访问，可以看到成功返回了友好的界面（error1.html）

![image-20231005114324205](Spring%EF%BC%88%E4%B8%89%EF%BC%89.assets/image-20231005114324205.png)

但我们现在一般都是采用前后端分离的开发方式，一般后端（应用服务器）遇到异常的时候，会给前端传递一个json字符串信息

```java
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
@Component
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

```

![image-20231005114833368](Spring%EF%BC%88%E4%B8%89%EF%BC%89.assets/image-20231005114833368.png)





- 注解方式：使用@ControllerAdvice + @ExceptionHandler 来处理。

使用注解 @ControllerAdvice + @ExceptionHandler 配置异常，@ControllerAdvice 注解本质是一个@Component，也会被扫描到，与此同时，具备AOP功能，默认情况下对所有的Controller都进行拦截操作，拦截后干什么呢？就需要在结合@ExceptionHandler、@InitBinder、@ModelAttribute 注解一起使用了，此

处我们讲解的是异常，所以是@ControllerAdvice + @ExceptionHandler的组合形式。



编写全局异常处理器类，使用@ControllerAdvice标注，且@ExceptionHandler指定异常类型

```java
@ControllerAdvice
public class GlobalExceptionHandler {
     @ExceptionHandler(RuntimeException.class)
     public ModelAndView runtimeHandleException(RuntimeException e){
         System.out.println("全局异常处理器执行...."+e);
         ModelAndView modelAndView = new ModelAndView("/error.html");
         return modelAndView;
     }
     @ExceptionHandler(IOException.class)
     @ResponseBody
     public ResultInfo ioHandleException(IOException e){
        //模拟一个ResultInfo
        ResultInfo resultInfo = new ResultInfo(0,"IOException",null);
         return resultInfo;
     }
}
```

如果全局异常处理器响应的数据都是Json格式的字符串的话，可以使用@RestControllerAdvice替代@ControllerAdvice 和 @ResponseBody

```java
@RestControllerAdvice
public class GlobalExceptionHandler {
     @ExceptionHandler(RuntimeException.class)
     public ResultInfo runtimeHandleException(RuntimeException e){
         //模拟一个ResultInfo
         ResultInfo resultInfo = new ResultInfo(0,"RuntimeException",null);
         return resultInfo;
     }
     @ExceptionHandler(IOException.class)
     public ResultInfo ioHandleException(IOException e){
         //模拟一个ResultInfo
         ResultInfo resultInfo = new ResultInfo(0,"IOException",null);
         return resultInfo;
     }
}
```



#### 3、异常处理机制原理剖析

初始化加载的处理器异常解析器，SpringMVC 的前置控制器在进行初始化的时候（即注册九大组件那里），会初始化处理器异常解析器HandlerExceptionResolver

```java
//初始化处理器异常解析器
this.initHandlerExceptionResolvers(context);
```

根据是否自定义添加异常解析器到spring容器，进行加载自定义异常解析器还是默认异常解析器

```java
private void initHandlerExceptionResolvers(ApplicationContext context) {
     //从容器中获得HandlerExceptionResolver的Map集合
     Map<String, HandlerExceptionResolver> matchingBeans = 
    BeanFactoryUtils.beansOfTypeIncludingAncestors(context, HandlerExceptionResolver.class, true, 
    false);
     //如果容器中没有HandlerExceptionResolver的话，则加载默认的HandlerExceptionResolver
     if (this.handlerExceptionResolvers == null) {
         //从dispatcherServlet.properties中加载
         this.handlerExceptionResolvers = this.getDefaultStrategies(context, 
       	 HandlerExceptionResolver.class);
     }
}
```

加载DispatcherServlet.properties中默认的异常处理器

```java
org.springframework.web.servlet.HandlerExceptionResolver=org.springframework.web.servlet.mvc.me
thod.annotation.ExceptionHandlerExceptionResolver,\
 org.springframework.web.servlet.mvc.annotation.ResponseStatusExceptionResolver,\
 org.springframework.web.servlet.mvc.support.DefaultHandlerExceptionResolver
```

配置了自定义的异常处理器后，默认的异常处理器就不会被加载，当配置<mvc:annotation-driven /> 或配置了

注解@EnableWebMvc后，默认异常处理器和自定的处理器异常解析器都会被注册

![image-20231005121850695](Spring%EF%BC%88%E4%B8%89%EF%BC%89.assets/image-20231005121850695.png)

异常处理器加载完毕后，当发生异常时，就会进行处理，跟踪 DispatcherServlet 的 doDispatch() 方法

```java
protected void doDispatch(HttpServletRequest request, HttpServletResponse response) {
     Object dispatchException = null;//定义异常
     try {
     	// ... 省略代码 ...
     } catch (Exception e) {
     	dispatchException = e;
     } catch (Throwable te) {
     	dispatchException = new NestedServletException("Handler dispatch failed", te);
     }
     //视图处理、拦截器最终方法调用、异常处理都在该方法内
     this.processDispatchResult(processedRequest, response, mappedHandler, mv, 
    (Exception)dispatchException);
}
```

跟踪processDispatchResult方法

```java
private void processDispatchResult(HttpServletRequest request, HttpServletResponse response, 
@Nullable HandlerExecutionChain mappedHandler, @Nullable ModelAndView mv, @Nullable Exception 
exception) throws Exception {
     boolean errorView = false;//定义错误视图标识，默认为false
     if (exception != null) {
         //判断当前捕获的异常是否是ModelAndViewDefiningException类型的异常
         if (exception instanceof ModelAndViewDefiningException) {
             //获得ModelAndViewDefiningException异常对象中的ModelAndView对象
             mv = ((ModelAndViewDefiningException)exception).getModelAndView();
     } else {
         //捕获到其他异常，获得当前发生异常的Handler对象
         Object handler = mappedHandler != null ? mappedHandler.getHandler() : null;
         //执行processHandlerException 方法
         mv = this.processHandlerException(request, response, handler, exception);
         //如果异常处理返回了ModelAndView 则修改错误视图的标识为true
         errorView = mv != null;
     }}
     // ... 省略其他代码 ...}
```

跟踪processHandlerException 方法

```java
protected ModelAndView processHandlerException(HttpServletRequest request, HttpServletResponse response, 
@Nullable Object handler, Exception ex) throws Exception {
     ModelAndView exMv = null;//定义ModelAndView
     //判断处理器异常解析器集合是否是空的
     if (this.handlerExceptionResolvers != null) {
         //遍历处理器异常解析器List集合
         Iterator var6 = this.handlerExceptionResolvers.iterator();
         while(var6.hasNext()) {
             //取出每一个异常解析器
             HandlerExceptionResolver resolver = (HandlerExceptionResolver)var6.next();
             //执行异常解析器的resolveException方法
             exMv = resolver.resolveException(request, response, handler, ex);
             //只要有一个异常处理器返回ModelAndView 则不在执行后面的异常处理器
             if (exMv != null) {
             	break;
             }
         } }
     //如果视图解析器不为null
     if (exMv != null) {return exMv;
 } else { throw ex; } }
```



我们通过断点的方式，进行测试说明：

全局异常处理器：

```java
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

```

Reuslt方法：

```java
package com.example.pojo;

/**
 * @author banana
 * @create 2023-10-05 12:49
 */
public class Result {
    private Integer code;
    private String message;
    private Object data;

    public Result(Integer code, String message, Object data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }
}

```

有异常的控制器：

```java
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

```



首先我们访问路径为/ex1的

因为其是运行异常，会被下面这个异常处理器捕获，返回的是视图模型

```java
@ExceptionHandler(RuntimeException.class)
public ModelAndView runtimeHandleException(RuntimeException e){
    System.out.println(e);
    ModelAndView modelAndView = new ModelAndView();
    modelAndView.setViewName("/error1.html");
    return modelAndView;
}
```

然后经过processHandlerException处理后，会将我们设定的视图存储到mv中

![image-20231005125710524](Spring%EF%BC%88%E4%B8%89%EF%BC%89.assets/image-20231005125710524.png)



我们访问路径为/ex2的

因为其是IO异常，会被下面这个异常处理器捕获，其会将返回的字符串写到response的缓存区中

```java
@ExceptionHandler(IOException.class)
@ResponseBody
public Result ioHandleException(IOException e){
    System.out.println(e);
    Result result = new Result(0, "", "");
    return result;
}
```

然后经过processHandlerException处理后，我们可以看到其mv是空的，即没有视图

![image-20231005130257076](Spring%EF%BC%88%E4%B8%89%EF%BC%89.assets/image-20231005130257076.png)

其输出的字符串存储到了缓存区中，然后在bytesWritten记录了当前输出字符的字节数

![image-20231005130315059](Spring%EF%BC%88%E4%B8%89%EF%BC%89.assets/image-20231005130315059.png)





如果mv有值的话，即有视图，经过下面就会返回视图信息。如果mv没有值，在response中已经会写数据了，tomcat会从response的缓存区中得到数据，然后组装一个http响应，给客户端响应。





#### 4、SpringMVC常用的异常解析器

![image-20231005131227246](Spring%EF%BC%88%E4%B8%89%EF%BC%89.assets/image-20231005131227246.png)



![image-20231005131232796](Spring%EF%BC%88%E4%B8%89%EF%BC%89.assets/image-20231005131232796.png)





我们着重看一下`ExceptionHandlerExceptionResolver`

在SpringMVC注册九大组件的，会调用`initHandlerExceptionResolvers`

```java
private void initHandlerExceptionResolvers(ApplicationContext context) {
    this.handlerExceptionResolvers = null;

    if (this.detectAllHandlerExceptionResolvers) {
        // Find all HandlerExceptionResolvers in the ApplicationContext, including ancestor contexts.
        Map<String, HandlerExceptionResolver> matchingBeans = BeanFactoryUtils
            .beansOfTypeIncludingAncestors(context, HandlerExceptionResolver.class, true, false);
        if (!matchingBeans.isEmpty()) {
            this.handlerExceptionResolvers = new ArrayList<>(matchingBeans.values());
            // We keep HandlerExceptionResolvers in sorted order.
            AnnotationAwareOrderComparator.sort(this.handlerExceptionResolvers);
        }
    }
    else {
        try {
            HandlerExceptionResolver her =
                context.getBean(HANDLER_EXCEPTION_RESOLVER_BEAN_NAME, HandlerExceptionResolver.class);
            this.handlerExceptionResolvers = Collections.singletonList(her);
        }
        catch (NoSuchBeanDefinitionException ex) {
            // Ignore, no HandlerExceptionResolver is fine too.
        }
    }

    // Ensure we have at least some HandlerExceptionResolvers, by registering
    // default HandlerExceptionResolvers if no other resolvers are found.
    if (this.handlerExceptionResolvers == null) {
        this.handlerExceptionResolvers = getDefaultStrategies(context, HandlerExceptionResolver.class);
        if (logger.isTraceEnabled()) {
            logger.trace("No HandlerExceptionResolvers declared in servlet '" + getServletName() +
                         "': using default strategies from DispatcherServlet.properties");
        }
    }
}
```

其自动帮我们注入了一个异常解析器混合器

![image-20231005133102620](Spring%EF%BC%88%E4%B8%89%EF%BC%89.assets/image-20231005133102620.png)

其中包含了3个异常解析器，其中有一个就是`ExceptionHandlerExceptionResolver`



在`ExceptionHandlerExceptionResolver`中有一个叫做`doResolveHandlerMethodException`的方法，就是去解析我们的`ExceptionHandler`注解的

![image-20231005133309896](Spring%EF%BC%88%E4%B8%89%EF%BC%89.assets/image-20231005133309896.png)S











### 补充知识：

在SpringMvc中 通常存在两个Spring容器

1. 核心应用程序上下文（Core Application Context）：这个容器是整个应用程序的根容器，负责管理应用程序的核心组件，例如服务层、数据访问层和业务逻辑。这个容器由`ContextLoaderListener`负责初始化和销毁，它会在应用程序启动时加载，并且在整个应用程序的生命周期内保持活动状态。
2. Web应用程序上下文（Web Application Context）：这个容器是建立在核心应用程序上下文之上的，负责管理与Web相关的组件，例如控制器、视图解析器和处理器映射等。它由`DispatcherServlet`负责初始化和销毁，每个`DispatcherServlet`都会有自己的Web应用程序上下文。它的生命周期与每个`DispatcherServlet`实例相关联，所以在多个`DispatcherServlet`的情况下，就会有多个Web应用程序上下文

Web应用程序上下文也被称为Spring MVC容器。它是建立在核心应用程序上下文之上的，专门用于管理与Web相关的组件，并提供支持Spring MVC框架的功能
