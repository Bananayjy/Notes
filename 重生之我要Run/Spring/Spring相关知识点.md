## Spring相关知识点

### 一、Spring实例化bean方式

- .构造器方式(反射);

  bean class="com.tuling.user"/> @Component

  通过BeanDefinition.beanClass去进行反射创建

- 静态工厂方式; factory-method （配置文件中指定factory-method属性）

- 实例工厂方式(@Bean); factory-bean+factory-method（配置文件中指定factory-method属性和factory-bean属性）

- FactoryBean方式

SpringBoot：

- 注解格式导入XML格式配置的bean

  再补充一个小知识，由于早起开发的系统大部分都是采用xml的形式配置bean，现在的企业级开发

  基本上不用这种模式了。但是如果你特别幸运，需要基于之前的系统进行二次开发，这就尴尬了。新开

  发的用注解格式，之前开发的是xml格式。这个时候可不是让你选择用哪种模式的，而是两种要同时使

  用。spring提供了一个注解可以解决这个问题，@ImportResource，在配置类上直接写上要被融合的

  xml配置文件名即可，算的上一种兼容性解决方案，没啥实际意义。

  ```
  @Configuration
  @ImportResource("applicationContext1.xml")
  public class SpringConfig32 {
  }
  ```

- 使用@Import注入bean、配置类

- 编程方式手动加载（在程序中，即Bean初始化后进行控制加载）

  ![image-20250317175608785](Spring%E7%9B%B8%E5%85%B3%E7%9F%A5%E8%AF%86%E7%82%B9.assets/image-20250317175608785.png)

- 导入实现了ImportSelector接口的类（在Bean初始化时进行控制加载，这种可以自定义决定是否加载也是SpringBoot能够自动配置的重要原因之一）

  ![image-20250317180103846](Spring%E7%9B%B8%E5%85%B3%E7%9F%A5%E8%AF%86%E7%82%B9.assets/image-20250317180103846.png)

- 导入实现了ImportBeanDefinitionRegistrar接口的类

方式六中提供了给定类全路径类名控制bean加载的形式，如果对spring的bean的加载原理比较熟悉的小伙伴知道，其实bean的加载不是一个简简单单的对象，spring中定义了一个叫做BeanDefinition的东西，它才是控制bean初始化加载的核心。BeanDefinition接口中给出了若干种方法，可以控制bean的相关属性。说个最简单的，创建的对象是单例还是非单例，在BeanDefinition中定义了scope属性就可以控制这个。如果你感觉方式六没有给你开放出足够的对bean的控制操作，那么方式七你值得拥有。我们可以通过定义一个类，然后实现ImportBeanDefinitionRegistrar接口的方式定义bean，并且还可以让你对bean的初始化进行更加细粒度的控制，不过对于新手并不是很友好。忽然给你开放了若干个操作，还真不知道如何下手。

![image-20250317181300811](Spring%E7%9B%B8%E5%85%B3%E7%9F%A5%E8%AF%86%E7%82%B9.assets/image-20250317181300811.png)

- 导入实现了BeanDefinitionRegistryPostProcessor接口的类

![image-20250317181541689](Spring%E7%9B%B8%E5%85%B3%E7%9F%A5%E8%AF%86%E7%82%B9.assets/image-20250317181541689.png)



![image-20250317181601574](Spring%E7%9B%B8%E5%85%B3%E7%9F%A5%E8%AF%86%E7%82%B9.assets/image-20250317181601574.png)

可编程控制，即可进行加载控制（不是什么bean都要加载的，不然会有很多无用的bean），是自动配置的关键。然后也出来了@Condition主键，去进行加载控制！





### 二、关于@Configuration注解

- https://www.bilibili.com/video/BV1kmkqY9E35?spm_id_from=333.788.player.switch&vd_source=6fd1666460d7e069201c2db8d906cbfe&p=43
- https://blog.csdn.net/gp_911014/article/details/122839974