# Spring（二）

[TOC]



## II、AOP

## 一、AOP简介

### 1、AOP概念

**AOP概念：**AOP，Aspect Oriented Programming，面向切面编程，是对面向对象编程OOP的升华。OOP是纵向对一个 事物的抽象，一个对象包括静态的属性信息，包括动态的方法信息等。而AOP是横向的对不同事物的抽象，属 性与属性、方法与方法、对象与对象都可以组成一个切面，而用这种思维去设计编程的方式叫做面向切面编程

![image-20230810205510008](Spring（二）.assets\image-20230810205510008.png)



### 2、AOP思想的实现方案

下面需要通过B对象对A对象进行一个增强

对A对象的A1和A2方法加入B对象的B1和B2方法来进行增强，对于要被增强的对象A我们称之为目标对象，要被增强的方法A1和A2我们称之为目标方法。对于B对象我们称之为增强对象，对于B对象中的B1和B2方法我们称之为增强方法。

实际做法就是对A对象去产生一个代理对象（代理对象类型和A对象类型相同，A对象中有A1和A2方法，那么产生的代理对象中也有A1和A2方法，在代理对象中将A对象的方法和B对象的方法组合在一起）

相对于OOP的纵向，横向将A对象和B对象中的方法抽取出来，将B的B1方法和B2方法去包围A的方法，实现一个增强。

![image-20230810210417735](Spring（二）.assets\image-20230810210417735.png)

总而言之，AOP思想的实际实现方案就是去产生一个动态代理对象。



### 3、模拟AOP的基础代码

创建一个目标对象，这里使用UserServiceImpl的实现类来充当

UserService接口

```java
package com.example.service;

public interface UserService {
    void show1();
    void show2();
}

```

UserSericeImpl实现类

```java
package com.example.service.Impl;

import com.example.service.UserService;

public class UserServiceImpl implements UserService {
    public void show1() {
        System.out.println("show1……");
    }

    public void show2() {
        System.out.println("show2……");
    }
}

```

然后写一个增强对象

```java
package com.example.advice;

//增强类，内部提供增强方法
public class MyAdvice {
    public void beforeAdvice(){
        System.out.println("前置的增强……");
    }

    public void afterAdvice(){
        System.out.println("后置的增强……");
    }
}

```

创建一个自定义的Bean后处理器，创建目标对象的代理对象，并放入到单例池中

```java
package com.example.processor;

import com.example.advice.MyAdvice;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class MockAopBeanPostProcessor implements BeanPostProcessor, ApplicationContextAware {
    //声明一个spring容器，通过Aware接口让spring帮忙注入底层的对象引用
    private ApplicationContext applicationContext;

    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        //目的：对UserServiceImpl中的show1和show2进行方法进行增强，增强方法存在于MyAdvice中
        //问题：
        // 1、需要筛选serice.impl包下的所有的类的所有方法可以进行增强，不然是所有的bean在Init方法后都可以进行增强
        // 解决方案：if-else
        // 2、MyAdvice怎么去获取
        // 解决方案：将MyAdvice加入到容器中，从Spring 容器中获得MyAdvice
        if(bean.getClass().getPackage().getName().equals("com.example.service.Impl")){
            //生成当前bean的Proxy对象（使用jdk动态代理）
            Object beanProxy = Proxy.newProxyInstance(
                    bean.getClass().getClassLoader(),
                    bean.getClass().getInterfaces(),
                    (Object proxy, Method method, Object[] args) ->{
                            //通过Spring容器获得MyAdvice对象
                            MyAdvice myAdvice = applicationContext.getBean(MyAdvice.class);
                            //执行增强对象的before方法
                            myAdvice.beforeAdvice();
                            //执行目标对象的目标方法
                            Object result = method.invoke(bean, args);
                            //执行增强对象的after方法
                            myAdvice.afterAdvice();
                            return result;
                        }
            );
            //返回增强后的代理类
            return beanProxy;
        }
        //Impl包以外的bean原封不动返回
        return bean;
    }

    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}

```

在配置文件中，对这些类进行bean的配置，将其交给spring容器去管理

```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:context="http://www.springframework.org/schema/context"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xsi:schemaLocation="http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans.xsd
        http://www.springframework.org/schema/context
        http://www.springframework.org/schema/context/spring-context.xsd">

        <bean id="userService" class="com.example.service.Impl.UserServiceImpl"></bean>

        <bean id="myAdvice" class="com.example.advice.MyAdvice"></bean>

        <bean class="com.example.processor.MockAopBeanPostProcessor"></bean>

</beans>
```

创建一个测试类

```java
package com.example;

import com.example.service.UserService;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class ApplicationContextTest {
    public static void main(String[] args) {
        ApplicationContext app = new ClassPathXmlApplicationContext("applicationContext.xml");
        UserService userService = app.getBean(UserService.class);
        userService.show1();
    }
}

```

然后我们查看一下

![image-20230810222210427](Spring（二）.assets\image-20230810222210427.png)

我们调用其show2方法，也一样是被增强了

![image-20230810222248896](Spring（二）.assets\image-20230810222248896.png)



但是这段代码还是有缺陷的的，比如说

将需要进行增强的目标对象写死

`if(bean.getClass().getPackage().getName().equals("com.example.service.Impl"))`

将增强的内容写死，只能是MyAdvice类中的beforeAdvice方法和afterAdvice方法

```java
 //通过Spring容器获得MyAdvice对象
MyAdvice myAdvice = applicationContext.getBean(MyAdvice.class);
//执行增强对象的before方法
myAdvice.beforeAdvice();
//执行目标对象的目标方法
Object result = method.invoke(bean, args);
//执行增强对象的after方法
myAdvice.afterAdvice();
return result;
```



### 4、AOP相关概念

![image-20230810223853332](Spring（二）.assets\image-20230810223853332.png)

![image-20230810224627535](Spring（二）.assets\image-20230810224627535.png)

## 二、基于xml配置的AOP

### 1、xml方式AOP快速入门

Spring中的实现AOP思想的框架已经完成了对我们上面代码的封装，并且将其中两个缺陷通过配置的方式让用户去进行选择，并实现了解耦。

![image-20230810225534131](Spring（二）.assets\image-20230810225534131.png)

**通过配置文件的方式去解决上述问题**

- 配置哪些包、哪些类、哪些方法需要被增强 （切点（切入点）表达式的配置）
- 配置目标方法要被哪些通知方法所增强，在目标方法执行之前还是之后执行增强 （指定通知）

配置方式的设计、配置文件（注解）的解析工作，Spring已经帮我们封装好了



**xml方式AOP快速入门**

1、导入AOP相关坐标

实现AOP思想的框架有很多，市面上有一个实现AOP的小框架叫aspectj，spring在早期的时候有自己的一套处理AOP的框架，后期发现aspectj处理框架处理AOP挺好的，后面也被spring整合到了里面，并且作为spring的AOP开发的一部分。因此现在学习的spring AOP配置都是学的aspectj的这一套，配置的时候一部分是spring核心的部分，一部分是aspectj的部分，因此我们导包的时候导入aspectj

```java
<!--AOP相关坐标-->
<dependency>
    <groupId>org.aspectj</groupId>
    <artifactId>aspectjweaver</artifactId>
    <version>1.9.6</version>
</dependency>
```

2、准备目标类、准备增强类、并配置给Spring管理

沿用上面的

```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:context="http://www.springframework.org/schema/context"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xsi:schemaLocation="http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans.xsd
        http://www.springframework.org/schema/context
        http://www.springframework.org/schema/context/spring-context.xsd">
        <!--配置目标类-->
        <bean id="userService" class="com.example.service.Impl.UserServiceImpl"></bean>
        <!--配置增强类-->
        <bean id="myAdvice" class="com.example.advice.MyAdvice"></bean>

        <!--<bean class="com.example.processor.MockAopBeanPostProcessor"></bean>-->

</beans>
```



3、配置切点表达式（哪些方法被增强）

需要用到aop的标签，引入aop的自定义命名空间，并配置切点表达式

```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:context="http://www.springframework.org/schema/context"
       xmlns:aop="http://www.springframework.org/schema/aop"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xsi:schemaLocation="http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans.xsd
        http://www.springframework.org/schema/context
        http://www.springframework.org/schema/context/spring-context.xsd
        http://www.springframework.org/schema/aop
        http://www.springframework.org/schema/aop/spring-aop.xsd">
        <!--配置目标类-->
        <bean id="userService" class="com.example.service.Impl.UserServiceImpl"></bean>
        <!--配置增强类-->
        <bean id="myAdvice" class="com.example.advice.MyAdvice"></bean>

        <!--配置aop-->
        <aop:config>
                <!--
                 配置切点表达式
                 目的：指定哪些方法被增强
                -->
                <aop:pointcut id="myPointcut" expression="execution(void com.example.service.Impl.UserServiceImpl.show1())"/>
        </aop:config>

      

</beans>
```

4、配置织入（切点被哪些通知方法增强，是前置增强还是后置增强）

```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:context="http://www.springframework.org/schema/context"
       xmlns:aop="http://www.springframework.org/schema/aop"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xsi:schemaLocation="http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans.xsd
        http://www.springframework.org/schema/context
        http://www.springframework.org/schema/context/spring-context.xsd
        http://www.springframework.org/schema/aop
        http://www.springframework.org/schema/aop/spring-aop.xsd">
        <!--配置目标类-->
        <bean id="userService" class="com.example.service.Impl.UserServiceImpl"></bean>
        <!--配置增强类-->
        <bean id="myAdvice" class="com.example.advice.MyAdvice"></bean>

        <!--配置aop-->
        <aop:config>
                <!--
                 配置切点表达式
                 目的：指定哪些方法被增强
                -->
                <aop:pointcut id="myPointcut" expression="execution(void com.example.service.Impl.UserServiceImpl.show1())"/>
                <!--
                配置织入
                目的是要执行哪些切点于哪些通知进行结合
                -->
                <aop:aspect ref="myAdvice">
                        <aop:before method="beforeAdvice" pointcut-ref="myPointcut"/>
                </aop:aspect>
        </aop:config>

     

</beans>
```

然后我们通过测试类去进行一下测试

```java
package com.example;

import com.example.service.UserService;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class ApplicationContextTest {
    public static void main(String[] args) {
        ApplicationContext app = new ClassPathXmlApplicationContext("applicationContext.xml");
        UserService userService = app.getBean(UserService.class);
        userService.show1();
    }
}

```

查看一下结果，可以发现我们在没有通过编写任何代码，知识通过aop对应的框架和配置文件实现了目标对象的增强

![image-20230810232817811](Spring（二）.assets\image-20230810232817811.png)



### 2、xml方式AOP配置详解

**xml方式AOP配置详解**

xml配置AOP的方式还是比较简单的，下面看一下AOP详细配置的细节： 

- 切点表达式的配置方式 

切点表达式的配置方式有两种，直接将切点表达式配置在通知上，也可以将切点表达式抽取到外面，在通知上 进行引用，引用的好处就是方便修改（如果直接配置在通知上，如果有十个一样的，需要修改十个，如果是引用的，只需要修改一处）

```xml
<aop:config>
    <!--配置切点表达式,对哪些方法进行增强-->
    <aop:pointcut id="myPointcut" expression="execution(void 
    com.itheima.service.impl.UserServiceImpl.show1())"/>
    <!--切面=切点+通知-->
    <aop:aspect ref="myAdvice">
        <!--指定前置通知方法是beforeAdvice-->
        <aop:before method="beforeAdvice" pointcut-ref="myPointcut"/>
        <!--指定后置通知方法是afterAdvice-->
        <aop:after-returning method="afterAdvice" pointcut="execution(void 
        com.itheima.service.impl.UserServiceImpl.show1())"/>
    </aop:aspect>
</aop:config>

```



- 切点表达式的配置语法 

切点表达式是配置要对哪些连接点（哪些类的哪些方法）进行通知的增强，语法如下：

```
execution([访问修饰符]返回值类型 包名.类名.方法名(参数))
```

其中： 

- 访问修饰符可以省略不写； 
- 返回值类型、某一级包名、类名、方法名 可以使用 * 表示任意； 
- 包名与类名之间使用单点 . 表示该包下的类，使用双点 .. 表示该包及其子包下的类；

com.itheima.service..UserServiceImpl.show1() 表示匹配service包下的UserServiceImpl或者service包的子包下的UserServiceImpl

- 参数列表可以使用两个点 .. 表示任意参数

com.itheima.service..UserServiceImpl.show1(..)  可以匹配show1() 和 show1(string name)



切点表达式的几个例子

![image-20230811001431207](Spring（二）.assets\image-20230811001431207.png)



- 通知的类型 

AspectJ通知由五种类型

![image-20230819093635948](Spring（二）.assets\image-20230819093635948.png)

1.前置通知 和 后置通知

增强类

```java
package com.example.advice;

//增强类，内部提供增强方法
public class MyAdvice {
    public void beforeAdvice(){
        System.out.println("前置的增强……");
    }

    public void afterReturningAdvice(){
        System.out.println("后置的增强……");
    }
}

```

配置aop

```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:context="http://www.springframework.org/schema/context"
       xmlns:aop="http://www.springframework.org/schema/aop"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xsi:schemaLocation="http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans.xsd
        http://www.springframework.org/schema/context
        http://www.springframework.org/schema/context/spring-context.xsd
        http://www.springframework.org/schema/aop
        http://www.springframework.org/schema/aop/spring-aop.xsd">
        <!--配置目标类-->
        <bean id="userService" class="com.example.service.Impl.UserServiceImpl"></bean>
        <!--配置增强类-->
        <bean id="myAdvice" class="com.example.advice.MyAdvice"></bean>

        <!--配置aop-->
        <aop:config>
                <!--
                 配置切点表达式
                 目的：指定哪些方法被增强
                -->
                <aop:pointcut id="myPointcut" expression="execution(void com.example.service.Impl.UserServiceImpl.show1())"/>
                <!--
                配置织入
                目的是要执行哪些切点于哪些通知进行结合
                -->
                <aop:aspect ref="myAdvice">
                        <!--前置通知-->
                        <aop:before method="beforeAdvice" pointcut-ref="myPointcut"/>
                        <!--后置不抛出异常通知-->
                        <aop:after-returning method="afterReturningAdvice" pointcut-ref="myPointcut"/>
                </aop:aspect>
        </aop:config>

 
</beans>
```

运行结果

![image-20230819094400474](Spring（二）.assets\image-20230819094400474.png)



2.环绕通知

增强对象

```java
package com.example.advice;

import org.aspectj.lang.ProceedingJoinPoint;

//增强类，内部提供增强方法
public class MyAdvice {
    //环绕目标方法
    //ProceedingJoinPoint正在执行的切入点方法 = 切点 = 目标实际被增强方法
    //Object返回 目标方法可能有返回值
    public Object around(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        System.out.println("环绕前通知……");
        //执行目标方法
        Object res = proceedingJoinPoint.proceed();
        System.out.println("环绕后通知……");
        return  res;
    }
}

```

aop配置

```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:context="http://www.springframework.org/schema/context"
       xmlns:aop="http://www.springframework.org/schema/aop"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xsi:schemaLocation="http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans.xsd
        http://www.springframework.org/schema/context
        http://www.springframework.org/schema/context/spring-context.xsd
        http://www.springframework.org/schema/aop
        http://www.springframework.org/schema/aop/spring-aop.xsd">
        <!--配置目标类-->
        <bean id="userService" class="com.example.service.Impl.UserServiceImpl"></bean>
        <!--配置增强类-->
        <bean id="myAdvice" class="com.example.advice.MyAdvice"></bean>

        <!--配置aop-->
        <aop:config>
                <!--
                 配置切点表达式
                 目的：指定哪些方法被增强
                -->
                <aop:pointcut id="myPointcut" expression="execution(void com.example.service.Impl.UserServiceImpl.show1())"/>
                <!--
                配置织入
                目的是要执行哪些切点于哪些通知进行结合
                -->
                <aop:aspect ref="myAdvice">
                        <!--环绕通知-->
                        <aop:around method="around" pointcut-ref="myPointcut"/>
                </aop:aspect>
        </aop:config>

        <!--<bean class="com.example.processor.MockAopBeanPostProcessor"></bean>-->

</beans>
```

结果

![image-20230819095207275](Spring（二）.assets\image-20230819095207275.png)

3.异常通知

![image-20230819095414012](Spring（二）.assets\image-20230819095414012.png)

4.最终通知

![image-20230819095435342](Spring（二）.assets\image-20230819095435342.png)



通知方法在被调用时，Spring可以为其传递一些必要的参数

![image-20230819095536791](Spring（二）.assets\image-20230819095536791.png)

1.JoinPoint

增强类：

```java
package com.example.advice;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;

//增强类，内部提供增强方法
public class MyAdvice {
    public void beforeAdvice(JoinPoint joinPoint){
        System.out.println("当前目标对象" + joinPoint.getTarget());
        System.out.println("表达式：" + joinPoint.getStaticPart());
        System.out.println("前置的增强……");
    }
}
```

aop配置

```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:context="http://www.springframework.org/schema/context"
       xmlns:aop="http://www.springframework.org/schema/aop"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xsi:schemaLocation="http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans.xsd
        http://www.springframework.org/schema/context
        http://www.springframework.org/schema/context/spring-context.xsd
        http://www.springframework.org/schema/aop
        http://www.springframework.org/schema/aop/spring-aop.xsd">
        <!--配置目标类-->
        <bean id="userService" class="com.example.service.Impl.UserServiceImpl"></bean>
        <!--配置增强类-->
        <bean id="myAdvice" class="com.example.advice.MyAdvice"></bean>

        <!--配置aop-->
        <aop:config>
                <!--
                 配置切点表达式
                 目的：指定哪些方法被增强
                -->
                <aop:pointcut id="myPointcut" expression="execution(void com.example.service.Impl.UserServiceImpl.show1())"/>
                <!--
                配置织入
                目的是要执行哪些切点于哪些通知进行结合
                -->
                <aop:aspect ref="myAdvice">
                        <!--前置通知-->
                        <aop:before method="beforeAdvice" pointcut-ref="myPointcut"/>
       
                </aop:aspect>
        </aop:config>

        <!--<bean class="com.example.processor.MockAopBeanPostProcessor"></bean>-->

</beans>
```

结果

![image-20230819100103035](Spring（二）.assets\image-20230819100103035.png)

JoinPoint对象的其他的一些方法

```java
public void 通知方法名称(JoinPoint joinPoint){
    //获得目标方法的参数
    System.out.println(joinPoint.getArgs());
    //获得目标对象
    System.out.println(joinPoint.getTarget());
    //获得精确的切点表达式信息
    System.out.println(joinPoint.getStaticPart());
}
```



2.ProceedingJpinPoint

如上所示，不重复赘述



3.Throwable

使用前需要在配置文件中进行通过throwing属性，置顶参数名称进行配置

用法类似异常类

![image-20230819100538261](Spring（二）.assets\image-20230819100538261.png)



- AOP的配置的两种方式

AOP的xml的两种配置方式，如下:

- 使用`<advisor>`配置切面

上面就是通过配置文件中的advisor进行配置的

```java
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:context="http://www.springframework.org/schema/context"
       xmlns:aop="http://www.springframework.org/schema/aop"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xsi:schemaLocation="http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans.xsd
        http://www.springframework.org/schema/context
        http://www.springframework.org/schema/context/spring-context.xsd
        http://www.springframework.org/schema/aop
        http://www.springframework.org/schema/aop/spring-aop.xsd">
        <!--配置目标类-->
        <bean id="userService" class="com.example.service.Impl.UserServiceImpl"></bean>
        <!--配置增强类-->
        <bean id="myAdvice" class="com.example.advice.MyAdvice"></bean>

        <!--配置aop-->
        <aop:config>
                <!--
                 配置切点表达式
                 目的：指定哪些方法被增强
                -->
                <aop:pointcut id="myPointcut" expression="execution(void com.example.service.Impl.UserServiceImpl.show1())"/>
                <!--
                配置织入
                目的是要执行哪些切点于哪些通知进行结合
                -->
                <aop:aspect ref="myAdvice">
                        <!--前置通知-->
                        <aop:before method="beforeAdvice" pointcut-ref="myPointcut"/>
                        <!--后置不抛出异常通知-->
                        <!-- <aop:after-returning method="afterReturningAdvice" pointcut-ref="myPointcut"/>-->
                        <!--环绕通知-->
                        <!--<aop:around method="around" pointcut-ref="myPointcut"/>-->

                </aop:aspect>
        </aop:config>

        <!--<bean class="com.example.processor.MockAopBeanPostProcessor"></bean>-->

</beans>
```



- 使用`<aspect>`配置切面

该方法需要通知类（增强类）去实现Advice的子功能接口

父接口Advice

```java
package org.aopalliance.aop;

public interface Advice {

}
```

Advice的功能子接口

![image-20230819102243538](Spring（二）.assets\image-20230819102243538.png)

1.MethodInterceptor

相当于aspect中的around 环绕通知

```
package org.aopalliance.intercept;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
@FunctionalInterface
public interface MethodInterceptor extends Interceptor {
	@Nullable
	Object invoke(@Nonnull MethodInvocation invocation) throws Throwable;

}

```

2.MethodBeforeAdvice

类似于aspect中的before

```
package org.springframework.aop;

import java.lang.reflect.Method;

import org.springframework.lang.Nullable;

public interface MethodBeforeAdvice extends BeforeAdvice {

	void before(Method method, Object[] args, @Nullable Object target) throws Throwable;

}

```

……



具体实例1

增强类实现Advice的功能子接口

```java
package com.example.advice;

import org.springframework.aop.AfterAdvice;
import org.springframework.aop.AfterReturningAdvice;
import org.springframework.aop.MethodBeforeAdvice;

import java.lang.reflect.Method;

public class MyAdvice2 implements MethodBeforeAdvice, AfterReturningAdvice {
    @Override
    public void before(Method method, Object[] args, Object target) throws Throwable {
        System.out.println("通过aspect前置通知……");
    }
    @Override
    public void afterReturning(Object returnValue, Method method, Object[] args, Object target) throws Throwable {
        System.out.println("通过aspect后置通知……");
    }
}

```

aop配置

```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:context="http://www.springframework.org/schema/context"
       xmlns:aop="http://www.springframework.org/schema/aop"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xsi:schemaLocation="http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans.xsd
        http://www.springframework.org/schema/context
        http://www.springframework.org/schema/context/spring-context.xsd
        http://www.springframework.org/schema/aop
        http://www.springframework.org/schema/aop/spring-aop.xsd">

    <!--配置目标类-->
    <bean id="userService" class="com.example.service.Impl.UserServiceImpl"></bean>
    <!--配置增强类-->
    <bean id="myAdvice2" class="com.example.advice.MyAdvice2"></bean>

    <!--aop配置-->
    <aop:config>
        <!--配置切点表达式-->
        <aop:pointcut id="myPointcut" expression="execution(void com.example.service.Impl.UserServiceImpl.show1())"/>
        <!--使用<aspect>配置切面-->
        <aop:advisor advice-ref="myAdvice2" pointcut-ref="myPointcut"/>
    </aop:config>

</beans>
```

启动类，使用配置类applicationContext2.xml

```java
package com.example;

import com.example.service.UserService;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class ApplicationContextTest {
    public static void main(String[] args) {
        ApplicationContext app = new ClassPathXmlApplicationContext("applicationContext2.xml");
        UserService userService = app.getBean(UserService.class);
        userService.show1();
    }
}

```

结果

![image-20230819103838412](Spring（二）.assets\image-20230819103838412.png)





具体实例2

增强类

```java
package com.example.advice;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

public class Myadvice3 implements MethodInterceptor {
    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        System.out.println("环绕前……");
        //执行目标方法
        Object res = invocation.getMethod().invoke(invocation.getThis(), invocation.getArguments());
        System.out.println("环绕后……");
        return res;
    }
}

```

aop配置

```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:context="http://www.springframework.org/schema/context"
       xmlns:aop="http://www.springframework.org/schema/aop"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xsi:schemaLocation="http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans.xsd
        http://www.springframework.org/schema/context
        http://www.springframework.org/schema/context/spring-context.xsd
        http://www.springframework.org/schema/aop
        http://www.springframework.org/schema/aop/spring-aop.xsd">

    <!--配置目标类-->
    <bean id="userService" class="com.example.service.Impl.UserServiceImpl"></bean>
    <!--配置增强类-->
    <bean id="Myadvice3" class="com.example.advice.Myadvice3"></bean>

    <!--aop配置-->
    <aop:config>
        <!--配置切点表达式-->
        <aop:pointcut id="myPointcut" expression="execution(void com.example.service.Impl.UserServiceImpl.show1())"/>
        <!--使用<aspect>配置切面-->
        <aop:advisor advice-ref="Myadvice3" pointcut-ref="myPointcut"/>
    </aop:config>

</beans>
```

测试类

```java
package com.example;

import com.example.service.UserService;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class ApplicationContextTest {
    public static void main(String[] args) {
        ApplicationContext app = new ClassPathXmlApplicationContext("applicationContext2.xml");
        UserService userService = app.getBean(UserService.class);
        userService.show1();
    }
}

```

结果

![image-20230819105112958](Spring（二）.assets\image-20230819105112958.png)



**AOP配置的两种语法形式不同点：**

1）配置语法不同

```java
<!-- 使用advisor配置 -->
<aop:config>
    <!-- advice-ref:通知Bean的id -->
    <aop:advisor advice-ref="advices" pointcut="execution(void 
    com.itheima.aop.TargetImpl.show())"/>
</aop:config>

<!-- 使用aspect配置 -->
<aop:config>
    <!-- ref:通知Bean的id -->
    <aop:aspect ref="advices">
    	<aop:before method="before" pointcut="execution(void 
   		com.itheima.aop.TargetImpl.show())"/>
    </aop:aspect>
</aop:config>

```

2）通知类的定义要求不同

advisor 需要的通知类需要实现Advice的子功能接口

```java
public class Advices implements MethodBeforeAdvice {
    public void before(Method method, Object[] objects, Object o) throws Throwable {
        System.out.println("This is before Advice ...");
    }

    public void afterReturning(Object o, Method method, Object[] objects, Object o1) throws Throwable {
        System.out.println("This is afterReturn Advice ...");
    }
}

```

aspect 不需要通知类实现任何接口，在配置的时候指定哪些方法属于哪种通知类型即可，更加灵活方便：

```java
public class Advices {
    public void before() {
    	System.out.println("This is before Advice ...");
    }
    public void afterReturning() {
    	System.out.println("This is afterReturn Advice ...");
    }
}
```

3）可配置的切面数量不同

- 一个advisor只能配置一个固定通知和一个切点表达式； 

如下就一个Myadvice3通知，如果想要其他的通知，需要新建一个advice的功能实现类

```xml
<!--aop配置-->
<aop:config>
    <!--配置切点表达式-->
    <aop:pointcut id="myPointcut" expression="execution(void com.example.service.Impl.UserServiceImpl.show1())"/>
    <!--使用<aspect>配置切面-->
    <aop:advisor advice-ref="Myadvice3" pointcut-ref="myPointcut"/>
</aop:config>
```

-  一个aspect可以配置多个通知和多个切点表达式任意组合，粒度更细。

```xml
<aop:config>
                <!--
                 配置切点表达式
                 目的：指定哪些方法被增强
                -->
                <aop:pointcut id="myPointcut" expression="execution(void com.example.service.Impl.UserServiceImpl.show1())"/>
                <!--
                配置织入
                目的是要执行哪些切点于哪些通知进行结合
                -->
                <aop:aspect ref="myAdvice">
                        <!--前置通知-->
                        <aop:before method="beforeAdvice" pointcut-ref="myPointcut"/>
                        <!--后置不抛出异常通知-->
                        <aop:after-returning method="afterReturningAdvice" pointcut-ref="myPointcut"/>
                        <!--环绕通知-->
                        <aop:around method="around" pointcut-ref="myPointcut"/>

                </aop:aspect>
        </aop:config>
```

4）使用场景不同

- 如果通知类型多、允许随意搭配情况下可以使用aspect进行配置；
-  如果通知类型单一、且通知类中通知方法一次性都会使用到的情况下可以使用advisor进行配置； 
-  在通知类型已经固定，不用人为指定通知类型时，可以使用advisor进行配置，例如后面要学习的Spring事务 控制的配置；



由于实际开发中，自定义aop功能的配置大多使用aspect的配置方式，所以我们后面主要讲解aspect的配置， advisor是为了后面Spring声明式事务控制做铺垫，此处大家了解即可。





**xml方式的AOP原理分析**

概述：

首先会对自定义命名空间的解析，为对应的标签去执行对应的解析器

首先找到aop命名空间的处理器

![image-20230819135022169](Spring（二）.assets\image-20230819135022169.png)

在init方法中，为不同的标签注册对应的解析器

![image-20230819135037471](Spring（二）.assets\image-20230819135037471.png)

看一下config标签对应的解析器ConfigBeanDefinitionParser，其中调用parse方法，向spring容器中注入AspectAwareAdviceProxyCreator的类（自动代理创建器），该解析器对应的parse方法如下所示

```java
@Override
	@Nullable
	public BeanDefinition parse(Element element, ParserContext parserContext) {
		CompositeComponentDefinition compositeDef =
				new CompositeComponentDefinition(element.getTagName(), parserContext.extractSource(element));
		parserContext.pushContainingComponent(compositeDef);

		configureAutoProxyCreator(parserContext, element);   // <--- 创建自动代理创建器

		List<Element> childElts = DomUtils.getChildElements(element);
		for (Element elt: childElts) {
			String localName = parserContext.getDelegate().getLocalName(elt);
			if (POINTCUT.equals(localName)) {
				parsePointcut(elt, parserContext);
			}
			else if (ADVISOR.equals(localName)) {
				parseAdvisor(elt, parserContext);
			}
			else if (ASPECT.equals(localName)) {
				parseAspect(elt, parserContext);
			}
		}

		parserContext.popAndRegisterContainingComponent();
		return null;
	}
```

创建自动代理器，其内部会先判断BeanFactory中有没有`org.springframework.aop.config.internalAutoProxyCreator`的对象，如果没有就创建一个BeanName为`org.springframework.aop.config.internalAutoProxyCreator`，对象为AspectAwareAdviceProxyCreator的Bean对象

```java
private void configureAutoProxyCreator(ParserContext parserContext, Element element) {
	AopNamespaceUtils.registerAspectJAutoProxyCreatorIfNecessary(parserContext, element);
}
```

AspectAwareAdviceProxyCreator的继承体系如下，可以看到它实现了BeanPostProcessor接口

![image-20230819141241710](Spring（二）.assets\image-20230819141241710.png)

我们可以在AspectAwareAdviceProxyCreator的间接父类中找到BeanPostProcessor接口的实现方法postProcessAfterInitialization（所有的Bean在实例化后，执行器对应的生命周期，都会执行该方法，并且BeanPostProcessor接口有postProcessBeforeInitialization和postProcessAfterInitialization两个方法需要实现，但在这里是用到了After），

其内部就是通过jdk的动态代理，为我们对应的bean创建一个proxy，并把proxy放入到spring容器中

```java
@Override
public Object postProcessAfterInitialization(@Nullable Object bean, String beanName) {
    if (bean != null) {
        Object cacheKey = getCacheKey(bean.getClass(), beanName);
        if (this.earlyProxyReferences.remove(cacheKey) != bean) {
            return wrapIfNecessary(bean, beanName, cacheKey);
        }
    }
    return bean;
}
```

通过断点方式观察，当bean是匹配切点表达式时，this.wrapIfNecessary(bean, beanName, cacheKey)返回的是 一个JDKDynamicAopProxy

![image-20230819142643980](Spring（二）.assets\image-20230819142643980.png)

可以按照器步骤，最后找到创建一个代理类并返回

![image-20230819142736876](Spring（二）.assets\image-20230819142736876.png)





**Aop底层两种生成Proxy的方式**

我们在查看源码的时候，可以发现其getProxy的时候有两种实现方式

```
package org.springframework.aop.framework;

import org.springframework.lang.Nullable;

public interface AopProxy {

	Object getProxy();

	Object getProxy(@Nullable ClassLoader classLoader);

}

```

![image-20230819143144787](Spring（二）.assets\image-20230819143144787.png)

那么在spring运行的时候是如何对这两个实现类进行选择的呢，这两者有什么区别？



动态代理的实现的选择，在调用getProxy() 方法时，我们可选用的 AopProxy接口有两个实现类，如上图，这两种 都是动态生成代理对象的方式，一种就是基于JDK的，一种是基于Cglib的（通过为目标类创建子类来实现代理，要求目标类不能是final，不然创建不了子类）

![image-20230819143427891](Spring（二）.assets\image-20230819143427891.png)



两者具体的底层原理图

JDK动态代理技术是为其动态创建一个兄弟类，然后调用其中的方法

Cglib是创建其子类，然后调用其中的方法

![image-20230819143924387](Spring（二）.assets\image-20230819143924387.png)



Cglib的实现：

创建增强类

```java
package com.example.advice;

public class MyAdvice4 {
    public void before(){
        System.out.println("前置增强");
    }

    public void after(){
        System.out.println("后置增强");
    }
}

```

创建目标类

```java
package com.example.test;

public class Target {
    public void show(){
        System.out.println("show...");
    }
}

```

Cglib实现代码

```java
package com.example.test;


import com.example.advice.MyAdvice4;
import org.springframework.cglib.proxy.Enhancer;
import org.springframework.cglib.proxy.MethodInterceptor;
import org.springframework.cglib.proxy.MethodProxy;

import java.lang.reflect.Method;

//Cglib基于父类（目标类）生成Proxy
public class CglibTest {
    public static void main(String[] args) {
        //创建目标类
        Target targer = new Target();

        //创建增强类（通知对象）
        MyAdvice4 myAdvice4 = new MyAdvice4();

        //编写CGlib代码
        Enhancer enhancer = new Enhancer();

        //设置代理类的父类
        //父类为目标类，生成的代理对象就是目标类的子类
        enhancer.setSuperclass(Target.class);

        //设置回调
        enhancer.setCallback(new MethodInterceptor() {
            //interceptor方法相当于JDK的Proxy的invoke方法
            @Override
            public Object intercept(Object o, Method method, Object[] objects, MethodProxy methodProxy) throws Throwable {
                myAdvice4.before();
                Object res = method.invoke(targer, objects);
                myAdvice4.after();
                return res;
            }
        });

        //生成代理对象(是目标类的子类)
        Target o = (Target) enhancer.create();
        o.show();
    }
}

```

测试结果

![image-20230819150333742](Spring（二）.assets\image-20230819150333742.png)





### 3、基于注解配置的AOP

**注解方式AOP基本使用**

使用注解配置，就是将我们之前通过xml配置文件进行配置的内容全部替换掉接口，Spring的AOP也提供了注解方式配置，使用相应的注解替代之前的xml配置。

在xml配置文件中，主要有如下三点

- 目标类被Spring容器管理
- 通知类被Spring管理
- 通知与切点的织入（切面）

```xml
<!--配置目标-->
<bean id="target" class="com.itheima.aop.TargetImpl"></bean>
<!--配置通知-->
<bean id="advices" class="com.itheima.aop.Advices"></bean>
<!--配置aop-->
<aop:config proxy-target-class="true">
    <aop:aspect ref="advices">
    	<aop:around method="around" pointcut="execution(* com.itheima.aop.*.*(..))"/>
    </aop:aspect>
</aop:config>
```



通过注解方式的具体操作如下所示

配置目标类，将其交给Spring容器进行管理

```java
package com.example.service.Impl;

import com.example.service.UserService;
import org.springframework.stereotype.Service;

@Service("userService")
public class UserServiceImpl implements UserService {
    public void show1() {
        System.out.println("show1……");
    }

    public void show2() {
        System.out.println("show2……");
    }
}

```

配置通知类，将其交给spring容器进行管理

```java
package com.example.advice;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.springframework.stereotype.Component;

//增强类，内部提供增强方法
@Component
public class MyAdvice {
    public void beforeAdvice(JoinPoint joinPoint){
        System.out.println("当前目标对象" + joinPoint.getTarget());
        System.out.println("表达式：" + joinPoint.getStaticPart());
        System.out.println("前置的增强……");
    }

    public void afterReturningAdvice(){
        System.out.println("后置的增强……");
    }

    //环绕目标方法
    //ProceedingJoinPoint正在执行的切入点方法 = 切点 = 目标实际被增强方法
    //Object返回 目标方法可能有返回值
    public Object around(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        System.out.println("环绕前通知……");
        //执行目标方法
        Object res = proceedingJoinPoint.proceed();
        System.out.println("环绕后通知……");
        return  res;
    }
}

```

配置切面，这里只通过注释配置一个前置通知

```java
package com.example.advice;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

//增强类，内部提供增强方法
@Component
@Aspect
public class MyAdvice {
    /*
        前置通知xml配置：
        <aop:before method="beforeAdvice" pointcut-ref="execution(void com.example.service.Impl.UserServiceImpl.show1())"/>
    */
    @Before("execution(void com.example.service.Impl.UserServiceImpl.show1())")
    public void beforeAdvice(JoinPoint joinPoint){
        System.out.println("当前目标对象" + joinPoint.getTarget());
        System.out.println("表达式：" + joinPoint.getStaticPart());
        System.out.println("前置的增强……");
    }

    public void afterReturningAdvice(){
        System.out.println("后置的增强……");
    }

    //环绕目标方法
    //ProceedingJoinPoint正在执行的切入点方法 = 切点 = 目标实际被增强方法
    //Object返回 目标方法可能有返回值
    public Object around(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        System.out.println("环绕前通知……");
        //执行目标方法
        Object res = proceedingJoinPoint.proceed();
        System.out.println("环绕后通知……");
        return  res;
    }
}

```

其配置切面具体分析如下

![image-20230819154419106](Spring（二）.assets\image-20230819154419106.png)

在xml配置注解扫描，也可以以注解的方式创建一个配置类，通过注解的方式配置注解扫描，这里使用xml配置

```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:context="http://www.springframework.org/schema/context"
       xmlns:aop="http://www.springframework.org/schema/aop"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xsi:schemaLocation="http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans.xsd
        http://www.springframework.org/schema/context
        http://www.springframework.org/schema/context/spring-context.xsd
        http://www.springframework.org/schema/aop
        http://www.springframework.org/schema/aop/spring-aop.xsd">

        <!--组件扫描-->
        <context:component-scan base-package="com.example"/>

      
</beans>
```

开启AOP自动代理，AOP会向spring容器主动提供解析注解的类，那么Spring就回去解析找这些注解并进行解析

```java
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:context="http://www.springframework.org/schema/context"
       xmlns:aop="http://www.springframework.org/schema/aop"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xsi:schemaLocation="http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans.xsd
        http://www.springframework.org/schema/context
        http://www.springframework.org/schema/context/spring-context.xsd
        http://www.springframework.org/schema/aop
        http://www.springframework.org/schema/aop/spring-aop.xsd">

        <!--组件扫描-->
        <context:component-scan base-package="com.example"/>

        <!--使用注解配置AOP，需要开启AOP自动代理-->
        <aop:aspectj-autoproxy/>
</beans>
```

如果配置类是注解的话，如下进行配置组件扫描和开启AOP自动代理

![image-20230819154336538](Spring（二）.assets\image-20230819154336538.png)

执行测试类

```java
package com.example;

import com.example.service.UserService;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class ApplicationContextTest {
    public static void main(String[] args) {

        ApplicationContext app = new ClassPathXmlApplicationContext("applicationContext3.xml");
        UserService userService = app.getBean(UserService.class);
        userService.show1();
    }
}

```

结果：

![image-20230819154122478](Spring（二）.assets\image-20230819154122478.png)







**注解方式AOP配置详解**

各种注解方式通知类型

```java
//前置通知
@Before("execution(* com.itheima.aop.*.*(..))")
public void before(JoinPoint joinPoint){}
//后置通知
@AfterReturning("execution(* com.itheima.aop.*.*(..))")
public void AfterReturning(JoinPoint joinPoint){}
//环绕通知
@Around("execution(* com.itheima.aop.*.*(..))")
public void around(ProceedingJoinPoint joinPoint) throws Throwable {}
//异常通知
@AfterThrowing("execution(* com.itheima.aop.*.*(..))")
public void AfterThrowing(JoinPoint joinPoint){}
//最终通知
@After("execution(* com.itheima.aop.*.*(..))")
public void After(JoinPoint joinPoint){}
```



切点表达式的抽取：使用一个空方法，将切点表达式标注在空方法上，其他通知方法引用即可，这里不是java的语法，而是框架开发者自定义的一种使用方式

```java
@Component
@Aspect
public class AnnoAdvice {
    //切点表达式抽取
    @Pointcut("execution(* com.itheima.aop.*.*(..))")
    public void pointcut(){}
    //前置通知
    @Before("pointcut()")
    public void before(JoinPoint joinPoint){}
    //后置通知
    @AfterReturning("AnnoAdvice.pointcut()")
    public void AfterReturning(JoinPoint joinPoint){}
    // ... 省略其他代码 ...
}
```



**注解方式AOP原理剖析**

替换上面的xml配置，将核心配置也换成注解类

```java
package com.example.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@Configuration
@ComponentScan("com.example")
@EnableAspectJAutoProxy
public class SpringConfig {
}

```

测试类

```java
package com.example;

import com.example.config.SpringConfig;
import com.example.service.UserService;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class ApplicationContextTest {
    public static void main(String[] args) {
        //基于xml的aop测试
        /*ApplicationContext app = new ClassPathXmlApplicationContext("applicationContext3.xml");
        UserService userService = app.getBean(UserService.class);
        userService.show1();*/

        //基于注解的aop测试
        ApplicationContext app = new AnnotationConfigApplicationContext(SpringConfig.class);
        UserService bean = app.getBean(UserService.class);
        bean.show1();
    }
}

```

结果

![image-20230819155758617](Spring（二）.assets\image-20230819155758617.png)





之前在使用xml配置AOP时，是借助的Spring的外部命名空间的加载方式完成的，使用注解配置后，就抛弃了 标签，而该标签最终加载了名为AspectJAwareAdvisorAutoProxyCreator的BeanPostProcessor ， 最终，在该BeanPostProcessor中完成了代理对象的生成。



要先看半注解方式配置AOP（配置文件+注解）是如何实现的，我们就需要去解析它开启AOP自动代理的标签`<aop:aspectj-autoproxy/>`

现在对应的命名空间中找到对应的解析器，`aspectj-autoproxy`对应的解析器是AspectJAutoProxyBeanDefinitionParser

![image-20230819160715839](Spring（二）.assets\image-20230819160715839.png)



在AspectJAutoProxyBeanDefinitionParser中的parse方法中，也执行了和xml配置方式一样的`AopNamespaceUtils.registerAspectJAnnotationAutoProxyCreatorIfNecessary(parserContext, element)`的方法

```java
@Override
@Nullable
public BeanDefinition parse(Element element, ParserContext parserContext) {
    AopNamespaceUtils.registerAspectJAnnotationAutoProxyCreatorIfNecessary(parserContext, element);
    extendBeanDefinition(element, parserContext);
    return null;
}
```

一路追踪下去，最终还是注册了 AwareAspectJAutoProxyCreator 这个类，只不过其交给Spring容器管理的实现类是AnnotationAwareAspectJAutoProxyCreator

```java
@Nullable
	public static BeanDefinition registerAspectJAnnotationAutoProxyCreatorIfNecessary(
			BeanDefinitionRegistry registry, @Nullable Object source) {

		return registerOrEscalateApcAsRequired(AnnotationAwareAspectJAutoProxyCreator.class, registry, source);
	}
```

`AnnotationAwareAspectJAutoProxyCreator`是`AspectJAwareAdvisorAutoProxyCreator`的一个子类

```java
public class AnnotationAwareAspectJAutoProxyCreator extends AspectJAwareAdvisorAutoProxyCreator{
……
}
```

其也是实现了BeanPostProcessor类，然后实现了其中的after方法，其具体实现就是AspectJAwareAdvisorAutoProxyCreator的间接父类中实现的内容，是一样的

![image-20230819162513758](Spring（二）.assets\image-20230819162513758.png)



postProcessAfterInitialization中具体实现，根据对应的目标bean对象去为其创建代理

```java
@Override
	public Object postProcessAfterInitialization(@Nullable Object bean, String beanName) {
		if (bean != null) {
			Object cacheKey = getCacheKey(bean.getClass(), beanName);
			if (this.earlyProxyReferences.remove(cacheKey) != bean) {
				return wrapIfNecessary(bean, beanName, cacheKey);
			}
		}
		return bean;
	}
```





如果是全注解配置AOP方式（配置类 + 注解），我们从其`@EnableAspectJAutoProxy`注解入手

```java
package org.springframework.context.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(AspectJAutoProxyRegistrar.class)
public @interface EnableAspectJAutoProxy {


	boolean proxyTargetClass() default false;

	boolean exposeProxy() default false;

}

```

使用@Import导入的AspectJAutoProxyRegistrar源码，一路追踪下去，最终还是注册了 AnnotationAwareAspectJAutoProxyCreator 这个类

```java
@Nullable
	public static BeanDefinition registerAspectJAnnotationAutoProxyCreatorIfNecessary(
			BeanDefinitionRegistry registry, @Nullable Object source) {

		return registerOrEscalateApcAsRequired(AnnotationAwareAspectJAutoProxyCreator.class, registry, source);
	}
```



注解方式AOP原理分析图总结：

无论是哪种累些，最终都将调用特点的方法（xml+注解和全注解调用registerAspectJAnnotationAutoProxyCreatorIfNecessary创建，xml调用registerAspectJAutoProxyCreatorIfNecessary方法创建），将AspectJAwareAdvisorAutoProxyCreator或其子类Bean加入到Spring容器中，然后调用其间接父类的postProcessAfterInitialization的方法（继承BeanPostPosseccor后的方法）来完成对指定对象的代理对象的创建，并将代理对象放入到Spring容器中

![image-20230819163602135](Spring（二）.assets\image-20230819163602135.png)

### 4、基于AOP的声明式事务控制

由于现在事务基本上都是通过注解@Transaction 进行配置的 ，所以这一章就大致学习一下，不进行代码的添加和学习了。



**编程事务概述**

事务是开发中必不可少的东西，使用JDBC开发时，我们使用connnection对事务进行控制，使用MyBatis时，我们 使用SqlSession对事务进行控制，缺点显而易见，当我们切换数据库访问技术时，事务控制的方式总会变化， Spring 就将这些技术基础上，提供了统一的控制事务的接口。Spring的事务分为：编程式事务控制 和 声明式事务 控制

![image-20230819175357008](Spring（二）.assets\image-20230819175357008.png)



Spring事务编程相关的类主要有如下三个

总结：平台事务管理器（动态） + 事务定义（静态） = 事务状态

![image-20230819175418711](Spring（二）.assets\image-20230819175418711.png)





**搭建测试环境 **

搭建一个转账的环境，dao层一个转出钱的方法，一个转入钱的方法，service层一个转账业务方法，内部分别调 用dao层转出钱和转入钱的方法，准备工作如下： 

- 数据库准备一个账户表tb_account; 

![image-20230819192427839](Spring（二）.assets\image-20230819192427839.png)

- dao层准备一个AccountMapper，包括incrMoney和decrMoney两个方法； 

![image-20230819192738522](Spring（二）.assets\image-20230819192738522.png)

- service层准备一个transferMoney方法，分别调用incrMoney和decrMoney方法； 

![image-20230819193141309](Spring（二）.assets\image-20230819193141309.png)

- 在applicationContext文件中进行Bean的管理配置； 

![image-20230819193608613](Spring（二）.assets\image-20230819193608613.png)

![image-20230819193857495](Spring（二）.assets\image-20230819193857495.png)

![image-20230819193820941](Spring（二）.assets\image-20230819193820941.png)

- 测试正常转账与异常转账。

正常转账：

![image-20230819193929823](Spring（二）.assets\image-20230819193929823.png)

![image-20230819193955695](Spring（二）.assets\image-20230819193955695.png)

异常转账：

转账和收账中间出现异常

![image-20230819194022708](Spring（二）.assets\image-20230819194022708.png)

tom-钱了 单lucy没减

![image-20230819194109518](Spring（二）.assets\image-20230819194109518.png)



**xml方式声明事务通知入门**

为了解决上面的问题，我们应该把AccountMapper整个transferMoney方法作为一个事务来进行管理，如果出现异常的话，就需要进行回滚操作。

解决方法：

结合上面我们学习的AOP的技术，很容易就可以想到，可以使用AOP对Service的方法进行事务的增强。 

- 目标类：自定义的AccountServiceImpl，内部的方法是切点
- 通知类：Spring提供的（事务管理的通知类/增强类），通知方法已经定义好，只需要配置即可

具体分析：

- 通知类是Spring提供的，需要导入Spring事务的相关的坐标； 

如果我们到如果导入过jdbc的依赖

```xml
 <dependency>
     <groupId>org.springframework</groupId>
     <artifactId>spring-jdbc</artifactId>
     <version>5.2.13.RELEASE</version>
</dependency>
```

其中会有一个依赖叫做tx，其就是事务管理transcation的缩写，用于事务管理，由于jdbc本来就是对于数据库的，因此也会有涉及到事务的依赖

![image-20230819203832238](Spring（二）.assets\image-20230819203832238.png)

- 配置目标类AccountServiceImpl； 

```xml
<bean id="accountService" class="com.itheima.service.impl.AccoutServiceImpl">
	<property name="accountMapper" ref="accountMapper"></property>
</bean>
```

- 使用advisor标签配置切面（只管配，不用管是哪一种通知，因为其在实现接口的时候，就已经确定好了）

![image-20230819210554277](Spring（二）.assets\image-20230819210554277.png)

![image-20230819210610720](Spring（二）.assets\image-20230819210610720.png)

![image-20230819210856508](Spring（二）.assets\image-20230819210856508.png)



**事务定义信息配置，配置不同方法（目标方法）的事务属性：**

![image-20230819211716651](Spring（二）.assets\image-20230819211716651.png)

（1）name：方法名称 *代表通配符 如添加操作addUser、addAccount、addOrders =》add *

![image-20230819212658276](Spring（二）.assets\image-20230819212658276.png)

![image-20230819212749806](Spring（二）.assets\image-20230819212749806.png)



（2）isolation：事务的隔离级别

![image-20230819213246633](Spring（二）.assets\image-20230819213246633.png)

（3）timeout：超时时间

默认-1：没有超时时间，但是数据库一般有默认的超时时间

其单位是秒

（4）read-only：是否只读

![image-20230819213435109](Spring（二）.assets\image-20230819213435109.png)

（5）propagation：事务的传播行为，解决业务方法调用业务方法（事务嵌套问题，A业务调用B业务，A和B业务都有事务，到底用谁的）

以下情况是A调用B，B发生异常，引发事务的情况

![image-20230819213641776](Spring（二）.assets\image-20230819213641776.png)



我们一般平时项目配置可以按照如下要求

![image-20230819214603081](Spring（二）.assets\image-20230819214603081.png)





**xml方式声明事务控制的原理剖析**（待分析）

![image-20230819215249066](Spring（二）.assets\image-20230819215249066.png)

![image-20230819215258145](Spring（二）.assets\image-20230819215258145.png)



**注解方式声明事务控制**

半注解：注解+xml配置

在需要进行事务管理的方法或类（类中的方法都添加该事务）上添加@Transactional注解

![image-20230819221139800](Spring（二）.assets\image-20230819221139800.png)

这里配置的事务的注解驱动中的transaction-manager属性不配置的话，会默认找名称为transactionManager的bean对象作为平台事务管理器。



全注解：

![image-20230819221219996](Spring（二）.assets\image-20230819221219996.png)



## III、Web部分

### 一、Spring整合Web环境

#### 1.JavaWeb三大组件及环境特点

在Java语言范畴内，web层框架都是基于Javaweb基础组件完成的，所以有必要复习一下Javaweb组件的特点

![image-20230819230744032](Spring（二）.assets\image-20230819230744032.png)

#### 2.Spring整合web环境的思想及实现

在进行Java开发时要遵循MVC+三层架构（`MVC 模式` 理解成是一个大的概念，而 `三层架构` 是对 `MVC 模式` 实现架构的思想），Spring操作最核心的就是Spring容器，web层（通过servlet进行操作）需要注入Service， service层需要注入Dao（Mapper），web层使用Servlet技术充当的话，需要在Servlet中获得Spring容器

```java
//这里使用的是注解配置类的形式
AnnotationConfigApplicationContext applicationContext =
	new AnnotationConfigApplicationContext(ApplicationContextConfig.class);
AccountService accountService = (AccountService)applicationContext.getBean("accountService");
accountService.transferMoney("tom","lucy",100);

```

web层代码如果都去编写创建AnnotationConfigApplicationContext的代码，那么配置类重复被加载了（因为每一个请求路径，只能由一个servlet去接受，并通过service方法做出处理）， Spring容器也重复被创建了（因为对于每一个请求，Tomcat都会创建对应的servlet对象），不能每次想从容器中获得一个Bean都得先创建一次容器，这样肯定是不允许。 所以，我们现在的诉求很简单，如下： 

- ApplicationContext创建一次，配置类加载一次; 
- 最好web服务器启动时，就执行第1步操作，后续直接从容器中获取Bean使用即可; 
- ApplicationContext的引用需要在web层任何位置都可以获取到。

出解决思路，如下：

- 在ServletContextListener（ Java Servlet 规范中的一个接口，用于监听 Web 应用程序的生命周期事件）的contextInitialized方法（在 Web 应用程序启动时被调用）中执行ApplicationContext的创建。或在Servlet的init 方法中执行ApplicationContext的创建，并给Servlet的load-on-startup属性一个数字值（配置服务器启动就创建Servlet对象，数值越小代表创建的优先级越高），确保服务器启动 Servlet就创建; 
- 将创建好的ApplicationContext存储到ServletContext域中，这样整个web层任何位置就都可以获取到了



具体实现如下所示：

在pom.xml中导入对应的依赖

```xml
<?xml version="1.0" encoding="UTF-8"?>

<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
  xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
  <modelVersion>4.0.0</modelVersion>

  <groupId>org.example</groupId>
  <artifactId>SpringMVC</artifactId>
  <version>1.0-SNAPSHOT</version>

  <!--
    打包方式为war 说明当前是一个web工程
  -->
  <packaging>war</packaging>


  <dependencies>
    <!--<dependency>
      <groupId>org.springframework</groupId>
      <artifactId>spring-web</artifactId>
      <version>5.3.7</version>
    </dependency>-->
    <!--
      使用spring框架的时候，通常需要引入以下核心依赖:
      spring-core：提供Spring框架的基本功能和核心工具类。
      spring-context：提供IoC（控制反转）和DI（依赖注入）等功能，包括Spring容器的实现。
      spring-beans：提供了Bean的定义和管理相关的功能。
      spring-aop：提供面向切面编程（AOP）的支持，用于实现横切关注点的分离和集中管理。
      spring-expression：提供了SpEL（Spring表达式语言）的支持，用于在运行时进行动态属性的访问和操作。
      通过引入spring-context依赖，即可将上面的依赖全部引入
    -->
    <dependency>
      <groupId>org.springframework</groupId>
      <artifactId>spring-context</artifactId>
      <version>5.3.7</version>
    </dependency>
    <!--
      Java Servlet API的核心包
     提供了开发基于Java的Web应用程序的一组接口和类
    -->
    <dependency>
      <groupId>javax.servlet</groupId>
      <artifactId>javax.servlet-api</artifactId>
      <version>4.0.1</version>
    </dependency>
    <!--
      提供对JDBC（Java数据库连接）的支持。它简化了使用JDBC进行数据库操作的开发流程，并提供了一些便利的功能和特性
      通过Spring框架来管理数据库连接、执行SQL语句和处理查询结果，从而减少了传统JDBC编程的样板代码量
    -->
    <dependency>
      <groupId>org.springframework</groupId>
      <artifactId>spring-jdbc</artifactId>
      <version>5.2.13.RELEASE</version>
    </dependency>
    <!-- mysql连接器 -->
    <dependency>
      <groupId>mysql</groupId>
      <artifactId>mysql-connector-java</artifactId>
      <version>8.0.21</version>
    </dependency>
    <!--spring集成mybatis相关依赖-->
    <dependency>
      <groupId>org.mybatis</groupId>
      <artifactId>mybatis-spring</artifactId>
      <version>2.0.7</version>
    </dependency>
    <!--mybatis核心依赖-->
    <dependency>
      <groupId>org.mybatis</groupId>
      <artifactId>mybatis</artifactId>
      <version>3.5.10</version>
    </dependency>
  </dependencies>

</project>

```

为之前的项目创建Web项目的结构，创建一个webapp包，在webapp包下添加一个WEB-INF包，并在其中放入web的xml配置文件，并添加对应的头

![image-20230916183757722](Spring%EF%BC%88%E4%BA%8C%EF%BC%89.assets/image-20230916183757722.png)

web.xml:

旧版本的web.xml文件的格式:在Java Servlet规范2.3之前（即J2EE 1.2和JavaEE 1.3），使用这种DTD（文档类型定义）方式来声明和配置web应用程序的部署描述符（Deployment Descriptor）文件

```xml
<!DOCTYPE web-app PUBLIC
 "-//Sun Microsystems, Inc.//DTD Web Application 2.3//EN"
 "http://java.sun.com/dtd/web-app_2_3.dtd" >

<web-app>
  <display-name>Archetype Created Web Application</display-name>
</web-app>

```

新版本的web.xml:自从Servlet规范2.4（J2EE 1.3、JavaEE 1.4）以及之后的版本，推荐使用XML Schema来替代DTD,在新的web.xml文件中，使用`xmlns`属性指定XML命名空间，并通过`xsi:schemaLocation`属性引用XML Schema文件来进行验证。这种方式可以提供更好的语义化和规范性，更易于理解和维护。

```xml
<web-app xmlns="http://xmlns.jcp.org/xml/ns/javaee"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://xmlns.jcp.org/xml/ns/javaee http://xmlns.jcp.org/xml/ns/javaee/web-app_4_0.xsd"
         version="4.0">
    <display-name>Archetype Created Web Application</display-name>
    <!-- 其他配置 -->
</web-app>

```



创建一个listener包，在其中创建一个类ContextLoaderListener，让其继承ServletContextListener，当web程序启动，ServletContext创建，ServletContextListener对应的contextInitialized方法运行，我们在创建的类中对该方法进行重写，分别进行创建Spring容器和将容器存储到最大（servletContext域）域（通过ServletContextEvent的对象的getServletContext来获取ServletContext，然后通过setAttribute方法将spring容器放到servletContext中），能够满足我们在web层中任何地方（通过HttpServletRequest对象的getgetServletContext来获取ServletContext，并调用ServletContext的getAttribute来获取spring容器）都可以拿到对应的Spring容器，并且只需要创建一次Spring容器

ContextLoderListener类：

```java
/**
 * @author banana
 * @create 2023-09-16 18:12
 */
public class ContextLoaderListener implements ServletContextListener {

    public void contextInitialized(ServletContextEvent sce) {
        //1.创建Spring 容器
        ApplicationContext app = new ClassPathXmlApplicationContext("applicationContext.xml");
        //2.将容器存储到servletContext域中
        //通过ServletContextEvent获取servletContext（其参数由tomcat调用的时候传递）
        sce.getServletContext().setAttribute("applicationContext", app);
    }
}

```

tomcat通过xml配置文件进行解析，我们在web.xml配置文件中进行配置Listener：ContextLoderListener

```xml
<web-app xmlns="http://xmlns.jcp.org/xml/ns/javaee"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://xmlns.jcp.org/xml/ns/javaee http://xmlns.jcp.org/xml/ns/javaee/web-app_4_0.xsd"
         version="4.0">

    <!--配置listener-->
    <listener>
        <listener-class>com.example.listener.ContextLoaderListener</listener-class>
    </listener>
</web-app>

```

![image-20230821220927041](assets\image-20230821220927041.png)



servlet实现类中的请求代码：

```java
/**
 * @author banana
 * @create 2023-09-16 18:43
 */
@WebServlet(urlPatterns = "/testServlet")
public class TestServlet extends HttpServlet {
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        //从servletContext域中获取spring容器
        ServletContext servletContext = request.getServletContext();
        ApplicationContext app = (ApplicationContext) servletContext.getAttribute("applicationContext");

        //从spring容器中获取对应的service的bean对象
        AccountService bean = app.getBean(AccountService.class);
        //调用其方法，对数据库进行操作
        bean.select();
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

    }
}

```

经过上述的配置，我们就可以成功在只创建一次spring容器的情况下，进行web资源的访问了。

此时的Service层和mapper和spring的xml配置文件：

Service接口：

```java
package com.example.service;

public interface AccountService {
    void select();
}

```

Service实现类：

```java
package com.example.service.impl;

import com.example.mapper.AccountMapper;
import com.example.service.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service("accountService")
public class AccountServiceImpl implements AccountService {

    @Autowired
    private AccountMapper accountMapper;

    @Override
    public void select() {
        accountMapper.incrMoney();
    }

}

```

mapper：

作用将数据库中tb_user中的所有数据的密码改成345

```java
package com.example.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface AccountMapper {

    //查询
    @Select("Update tb_user set password = '345'")
    public void incrMoney();

}

```

xml配置文件：

```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xmlns:context="http://www.springframework.org/schema/context"
       xmlns:aop="http://www.springframework.org/schema/aop"
       xmlns:tx="http://www.springframework.org/schema/tx"
       xsi:schemaLocation="
       http://www.springframework.org/schema/beans
       http://www.springframework.org/schema/beans/spring-beans.xsd
       http://www.springframework.org/schema/context
       http://www.springframework.org/schema/context/spring-context.xsd
       http://www.springframework.org/schema/aop
       http://www.springframework.org/schema/aop/spring-aop.xsd
       http://www.springframework.org/schema/tx
       http://www.springframework.org/schema/tx/spring-tx.xsd
       ">

    <!--组件扫描-->
    <context:component-scan base-package="com.example"/>

    <!--加载properties文件-->
    <context:property-placeholder location="classpath:jdbc.properties"/>

    <!--
        配置数据源信息
        这里数据源使用Spring框架提供的简单的数据源实现类
    -->
    <bean id="dataSource" class="org.springframework.jdbc.datasource.DriverManagerDataSource">
        <property name="driverClassName" value="com.mysql.cj.jdbc.Driver" />
        <property name="url" value="jdbc:mysql://localhost:3306/mybatis?serverTimezone=GMT" />
        <property name="username" value="root" />
        <property name="password" value="123456" />
    </bean>

    <!--配置SqlSessionFactoryBean，作用将SqlSessionFactory存储到spring容器-->
    <bean class="org.mybatis.spring.SqlSessionFactoryBean">
        <property name="dataSource" ref="dataSource"></property>
    </bean>

    <!--MapperScannerConfigurer,作用扫描指定的包，产生Mapper对象存储到Spring容器-->
    <bean class="org.mybatis.spring.mapper.MapperScannerConfigurer">
        <property name="basePackage" value="com.example.mapper"></property>
    </bean>

</beans>
```



测试：

当前数据库信息

![image-20230917143447634](Spring%EF%BC%88%E4%BA%8C%EF%BC%89.assets/image-20230917143447634.png)

将当前项目部署到本地的tomcat中

![image-20230917143517502](Spring%EF%BC%88%E4%BA%8C%EF%BC%89.assets/image-20230917143517502.png)

运行当前tomcat，访问对应的servlet的路径：

![image-20230917143604059](Spring%EF%BC%88%E4%BA%8C%EF%BC%89.assets/image-20230917143604059.png)

查看数据库，可以发现，成功运行了对应的sql，即我们成功创建了一次spring容器，并完成了对数据库的操作：

![image-20230917143638580](Spring%EF%BC%88%E4%BA%8C%EF%BC%89.assets/image-20230917143638580.png)





但是对于框架的开发者来说，不会让程序员自己去写ContextLoderListener，需要框架开发者对其进行一个封装，并且我们把Spring的配置文件写死为`applicationContext`，意味着之后程序员去进行开发的时候，必须将spring配置文件的名称定义为`applicationContext`,这样是不好的，应该允许程序员去进行定制。并且我们在将spring容器加入到servletcontext域中的时候是`applicationContext`这个名称，这也意味着程序员在后续获取的时候，只能通过这个名称去获取我们的spring容器。在自定义框架的时候，我们应该追求不自定义一些特殊的字符串，让程序员去记，让这些耦合死的字符串在框架内进行消化。

对于将spring容器添加到servletcontext中需要指定的名称，我们可以将该名称进行提取到xml文件中进行一个全局的配置来解决，之后我们的spring的配置文件名称就可以自定义，并且只需要在xml文件中更改对应的全局配置名称即可将对应的spring容器添加到servletcontext中

context-param元素含有一对**参数名和参数值**，用作应用的**Servlet上下文初始化参数**，**参数名在整个Web应用中必须是惟一的，在web应用的整个生命周期中上下文初始化参数都存在，任意的Servlet和jsp都可以随时随地访问它。**在servlet里面可以通过getServletContext().getInitParameter(“context/param”)得到。

```xml
<web-app xmlns="http://xmlns.jcp.org/xml/ns/javaee"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://xmlns.jcp.org/xml/ns/javaee http://xmlns.jcp.org/xml/ns/javaee/web-app_4_0.xsd"
         version="4.0">

    <!-- 定义全局参数 -->
    <context-param>
        <param-name>contextConfigLocation</param-name>
        <param-value>classpath:applicationContext.xml</param-value>
    </context-param>

    <!--配置listener-->
    <listener>
        <listener-class>com.example.listener.ContextLoaderListener</listener-class>
    </listener>

</web-app>

```

![image-20230821223206817](assets\image-20230821223206817.png)

修改监听类中的代码，解析配置文件的文件名

```java
package com.example.listener;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

/**
 * @author banana
 * @create 2023-09-16 18:12
 */
public class ContextLoaderListener implements ServletContextListener {
    //封装名称
    private String CONTEXT_CONFIG_LOCATION = "contextConfigLocation";

    public void contextInitialized(ServletContextEvent sce) {
        System.out.println("contextInitialized ……");
        //0. 获取contextConfigLocation配置文件名称
        ServletContext servletContext = sce.getServletContext();
        String contextConfigLocation = servletContext.getInitParameter(CONTEXT_CONFIG_LOCATION);
        //解析出配置文件名称
        contextConfigLocation = contextConfigLocation.substring("classpath:".length());
        //1.创建Spring 容器
        ApplicationContext app = new ClassPathXmlApplicationContext(contextConfigLocation);
        //2.将容器存储到servletContext域中
        //通过ServletContextEvent获取servletContext（其参数由tomcat调用的时候传递）
        sce.getServletContext().setAttribute("applicationContext", app);
    }
}

```

![image-20230821223138124](assets\image-20230821223138124.png)



对于获取spring容器需要指定的名称，我们可以自定义一个工具类，然后在工具类中自定义一个方法，其作用就是获取spring容器的引用，这样就不用直接通过名称取获取spring容器了

工具类：

```java
package com.example.utils;

import org.springframework.context.ApplicationContext;

import javax.servlet.ServletContext;

/**
 * @author banana
 * @create 2023-09-17 14:59
 */
public class WebApplicationContextUtils {
    //封装名称
    private static String CONTEXT_CONFIG_LOCATION = "contextConfigLocation";

    public static ApplicationContext getWebApplicationContext(ServletContext servletContext){
        String contextConfigLocation = servletContext.getInitParameter(CONTEXT_CONFIG_LOCATION);
        //解析出配置文件名称
        contextConfigLocation = contextConfigLocation.substring("classpath:".length());
        //去除后缀
        contextConfigLocation = contextConfigLocation.substring(0, contextConfigLocation.indexOf("."));
        ApplicationContext app = (ApplicationContext) servletContext.getAttribute(contextConfigLocation);
        return app;
    }
}

```

在servlet只需要调用工具类的方法，然后将servletContext作为参数传入即可获取spring容器

```java
package com.example.web;

import com.example.service.AccountService;
import com.example.utils.WebApplicationContextUtils;
import org.springframework.context.ApplicationContext;

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

```



如上就是用listener监听器取实现spring集成web环境的代码完成了（模拟框架底层实现），我们程序员只需要取调用WebApplicationContextUtils工具类和配置xml中全局参数如上文的contextConfigLocation即可实现spring继承web环境，而对于工具类的代码编写，和监听器的代码时框架开发者实现的



#### 3、Spring的web开发组件Spring-web(spring自动集成了上面的内容)

到此，就将一开始的诉求都解决了，当然我们能想到的Spring 框架自然也会想到，Spring其实已经为我们定义 好了一个ContextLoaderListener，使用方式跟我们上面自己定义的大体一样，但是功能要比我们强百倍，所以 ，遵循Spring "拿来主义" 的精神，我们直接使用Spring提供的就可以了，开发如下：

先导入Spring-web的坐标：

```xml
<dependency>
    <groupId>org.springframework</groupId>
    <artifactId>spring-web</artifactId>
    <version>5.3.7</version>
</dependency>
```

![image-20230916180108713](Spring%EF%BC%88%E4%BA%8C%EF%BC%89.assets/image-20230916180108713.png)

![image-20230917152003313](Spring%EF%BC%88%E4%BA%8C%EF%BC%89.assets/image-20230917152003313.png)

在web.xml中去配置ContextLoaderListener，并指定配置文件的位置

```xml
<context-param>
    <param-name>contextConfigLocation</param-name>
    <param-value>classpath:applicationContext.xml</param-value>
</context-param>
<listener>
	<listener-class>org.springframework.web.context.ContextLoaderListener</listener-class>
</listener>
```

在Servlet中直接使用，其中WebApplicationContextUtils用的是spring提供的（其实底层更我们上面写的差不多上）

![image-20230917152101047](Spring%EF%BC%88%E4%BA%8C%EF%BC%89.assets/image-20230917152101047.png)

```java
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

```

经过测试，也是可以完成对数据库的操作的。



#### 4、核心配置类方式配置spring web

P106

如果当前的配置类是核心配置类（注解类型），那该如何取进行配置呢？

Servlet上下文初始化参数自定义contextClass，创建一个注解的WebApplicationContextUtils去帮我们根据配置类去创建对应的spring容器。



### 二、Web层MVC框架思想与设计思路

![image-20230822203414213](assets\image-20230822203414213.png)

 问题1：客户端在访问的时候，每一个功能对应一个或多个Servlet，如果是一个大型的项目，可能有成百上千的与之对应。 

问题2：每一个servlet中执行的行为基本都是一致的，虽然具体的代码不一样。servlet接受到客户端的请求，然后进行一个javaBean的封装实体数据，然后把实体数据传递给业务层，业务层再通过dao层操作完毕后，在返回给servlet，servlet拿到数据后，再将其存到域中（一般存在request域），再指派给jsp进行视图的展示。我们再编写程序的时候，应该将经历放到业务的编写中，而需要将这些共同的行为进行一个抽取，抽取成一个前端的Bean，这个前端的Bean帮我们取完成这些通用的操作

问题3：虽然现在已经完成了在web层只需要加载一次spring容器，但实际的代码还是比较繁琐的，如下所示，我们取获取Service层的bean的时候是通过调用spring容器的getbean的方法取获取的

![image-20230822204351535](assets\image-20230822204351535.png)

我们能不能让spring为我们提供一种扩展的方案，直接像业务层中注入dao层一样，直接将service层注入到web层中



将上面三点问题总结一下，就是如下

原始Javaweb开发中，Servlet充当Controller的角色，Jsp充当View角色，JavaBean充当模型角色，后期Ajax异 步流行后，在加上现在前后端分离开发模式成熟后，View就被原始Html+Vue替代。原始Javaweb开发中， Service充当Controller有很多弊端，显而易见的有如下几个：

![image-20230822204457530](assets\image-20230822204457530.png)

而上面这些问题，都很好地被spring提供的框架springMVC解决了





Servlet具有共有行为，如封装数据、放到域中、指派视图等，同时也拥有特有的行为。

我们可以把一些封装好的功能放到一个Servlet（前端控制器），该servlet内部帮我们去执行共有的行为，该servlet再执行一个分发，分发给某一个具体的Bean，这个Bean就是一个控制器，让Bean中的具体的方法去完成一些特有的行为，这样由共有行为和特有行为共同组成了一个业务层，既原先的servlet。我们将前面的这个Sevlet称为前端控制器，后面的springmvc通过一个servlet去完成前端控制器的。

![image-20230822205227693](assets\image-20230822205227693.png)
