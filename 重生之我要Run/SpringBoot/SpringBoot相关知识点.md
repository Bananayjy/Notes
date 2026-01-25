## SpringBoot相关知识点

### 一、SpringBoot自动配置

https://www.bilibili.com/video/BV1kmkqY9E35?spm_id_from=333.788.videopod.episodes&vd_source=6fd1666460d7e069201c2db8d906cbfe&p=79

概括：

通过@SpringBootConfiguration上的@EnableAutoConfiguration开启自动配置

在@EnableAutoConfiguration中通过@Import引入了AutoConfigurationImportSelector类（其是ImportSelector的实现类，直接实现DeferredImportSelector，它会使SpringBoot自动配置类的顺序在最后，方便我们扩展和覆盖，有助于对@ConditionalOnBean配置的解析，先让其他能加载的都加载好）去导入一些Bean组件，Spring容器启动时，加载IOC容器时会解析@Import注解，然后读取所有的META-INF/spring.factories文件（key-value的格式），去获取其中所有的EnableAutoConfiguration的内容。通过@ConditionXXX注解，排除无效的自动配置类。

![image-20250317190123815](SpringBoot%E7%9B%B8%E5%85%B3%E7%9F%A5%E8%AF%86%E7%82%B9.assets/image-20250317190123815.png)



### 二、SpringBoot启动原理

https://www.bilibili.com/video/BV1kmkqY9E35?spm_id_from=333.788.videopod.episodes&vd_source=6fd1666460d7e069201c2db8d906cbfe&p=81

两个步骤，new SpringApplication，调用SpringApplication对象的run方法。

通过new SpringApplication，将启动类传递给primarySources，并调用 SpringApplication的有参构造方法，实例一个Spirng应用对象。在构造方法里主要完成启动环境初始化工作，如，推断当前web应用类型，读取初始化器、监听器，将main方法所在类放入到mainApplicationClass（SpringApplication的实例对象）。

运行run方法：

- 读取环境变量，配置信息（如 `application.properties` 或 `application.yml`）
- 创建Spring IOC容器（SpringApplication上下文，ServletWebServerApplicationContext,非web应用: `AnnotationConfigApplicationContext`），

- 与初始化上下文：将启动类作为配置类进行读取-> 将配置注册为BeanDefinition(注释配置的获取)
- 调用refresh，加载IOC容器,是初始化 Spring IOC 容器的核心步骤。
  - invokeBeanFactoryPostProcessor，解析启动类(配置类),@Import：加载所有的自动配置类
  - onRefresh：创建内置servlet容器
- 在整个过程中springboot会调用很多监听器对外进行扩展