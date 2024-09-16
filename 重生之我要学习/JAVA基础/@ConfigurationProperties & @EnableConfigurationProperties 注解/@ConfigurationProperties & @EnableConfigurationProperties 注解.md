# @ConfigurationProperties & @EnableConfigurationProperties 注解

[TOC]

## 一、说明

### 1.1 前言

本文对@ConfigurationProperties以及@EnableConfigurationProperties实现绑定外部属性的方式和实现原理进行学习总结。

文章如有错误，欢迎指正，一起沟通学习！

### 1.2 前置知识点

#### 1. 外部化配置注释简介

如果你想要绑定和验证一些外部属性(例如，从 `Properties`、`yaml`、`yml`文件)，`@ConfigurationProperties` 和 `@Value` 是其中两个常用的注解用来外部化和注入配置属性。

#### 2. @ConfigurationProperties绑定的两种方式：

- 通过`setter方法`注入配置属性
- 使用`@ConstructorBinding`，通过绑定到构造函数参数来执行（Spring Boot 2.2 ）。

#### 3. @Value 和@ ConfigurationProperties 作用/特点

**@ConfigurationProperties**

- **作用**: `@ConfigurationProperties` 用于将配置文件中的属性（如`application.properties`或`application.yml`中的属性）绑定到一个`POJO（Plain Old Java Object）`上。通过这个注解，Spring会自动将配置文件中的属性映射到`POJO`类的字段上。
- **特点**: 属性的值是在启动时从配置文件中读取的，这些属性值不会经过`SpEL（Spring Expression Language）`表达式的计算。它们是直接从配置文件中读取并注入到`POJO`中的。

**@Value**

- **作用**: `@Value` 注解用于将配置属性注入到`Spring Bean`中的字段、方法或构造函数参数。它支持使用SpEL表达式，这意味着可以在注解的值中使用`Spring表达式语言`来动态计算属性值。
- **特点**: `@Value` 可以用于直接注入配置值，也可以利用SpEL表达式来处理更复杂的动态计算或逻辑。

**SqEL表达式示例**

在`application.properties`中定义了两个属性

```properties
app.message=Hello
app.name=World
```

在Spring Boot应用程序中使用SpEL来组合这些属性

```java
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class GreetingService {

    @Value("#{${app.message} + ' ' + ${app.name}}")
    private String greetingMessage;

    public String getGreetingMessage() {
        return greetingMessage;
    }
}

```

结果

```
Hello World
```



### 1.3 参考文章

**官方文档**

- [官方文档](https://docs.spring.io/spring-boot/api/java/org/springframework/boot/context/properties/ConfigurationProperties.html)

**使用@ConfigurationProperties注解的相关文档**

- [使用文档1](https://www.cnblogs.com/tian874540961/p/12146467.html)
- [使用文档2](https://developer.aliyun.com/article/912763)
- [使用文档3](https://juejin.cn/post/6974307745121959966)

**@ConfigurationProperties注解原理实现的文档**

- [原理分析1](https://juejin.cn/post/6844903952941596680)

- [源理分析2](https://blog.csdn.net/yaomingyang/article/details/109519099)

- [手撸代码实现](https://www.cnblogs.com/dayu123/p/16486345.html)



## 二、使用示例

参考1.3中，使用@ConfigurationProperties注解的相关文档。

## 三、原理

### 3.1 @ConfigurationProperties源码

可以从源码中看到 @ConfigurationProperties只是起一个注解，其中各个参数的说明如下所示

```java
@Target({ ElementType.TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Indexed
public @interface ConfigurationProperties {

	/**
	 * The prefix of the properties that are valid to bind to this object. Synonym for
	 * {@link #prefix()}. A valid prefix is defined by one or more words separated with
	 * dots (e.g. {@code "acme.system.feature"}).
	 * @return the prefix of the properties to bind
	 * 可有效绑定到此对象的属性的前缀。前缀()的同义词。一个有效的前缀是由一个或多个用点分隔的单词定义，示例acme. system. feature")。
     * 返回: 要绑定的属性的前缀
	 */
	@AliasFor("prefix")
	String value() default "";

	/**
	 * The prefix of the properties that are valid to bind to this object. Synonym for
	 * {@link #value()}. A valid prefix is defined by one or more words separated with
	 * dots (e.g. {@code "acme.system.feature"}).
	 * @return the prefix of the properties to bind
	 * prefix的同义词
	 */
	@AliasFor("value")
	String prefix() default "";

	/**
	 * Flag to indicate that when binding to this object invalid fields should be ignored.
	 * Invalid means invalid according to the binder that is used, and usually this means
	 * fields of the wrong type (or that cannot be coerced into the correct type).
	 * @return the flag value (default false)
	 * 标志，表示绑定到该对象时应忽略无效字段。根据所使用的绑定器，Invalid表示无效，通常这意味着字段类型错误(或者不能强制转换为正确类型)。
     * 返回: 标志值（默认为false）
	 */
	boolean ignoreInvalidFields() default false;

	/**
	 * Flag to indicate that when binding to this object unknown fields should be ignored.
	 * An unknown field could be a sign of a mistake in the Properties.
	 * @return the flag value (default true)
	 * 标志，表示绑定到该对象时应忽略未知字段。未知字段可能是属性中出现错误的标志。
     * 返回：标志值（默认为true）
	 */
	boolean ignoreUnknownFields() default true;

}
```



### 3.2 关于@ConfigurationProperties如何实现配置文件与Java Bean对象的绑定

我们可以使用`@ConfigurationProperties` + `@Configuration`（本质上只需要就该类注册为`bean对象`即可，`@Component注解`也是适用的 ）来实现配置文件与`Java Bean对象`的绑定，如下所示

```java
@Configuration
@ConfigurationProperties(prefix = "demo")
public class demoProperties {
    
    private String value;

}
```

我们也可以使用`@ConfigurationProperties` + `@EnableConfigurationProperties`来实现配置文件与Java `Bean对象`的绑定。与`@ConfigurationProperties`关系比较密切的就是`EnableConfigurationProperties注解`（`Spring Boot`项目自身当中大量`autoconfigure`都是使用`EnableConfigurationProperties注解`启用`XXXProperties`功能，并且会配合`xxxcondition`来实现不同条件下是否进行自动配置的功能）。

`@EnableConfigurationProperties`源码如下所示：

```java
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(EnableConfigurationPropertiesRegistrar.class)
public @interface EnableConfigurationProperties {

	/**
	 * The bean name of the configuration properties validator.
	 * 配置属性验证器的bean名称
	 * @since 2.2.0
	 */
	String VALIDATOR_BEAN_NAME = "configurationPropertiesValidator";

	/**
	 * Convenient way to quickly register
	 * {@link ConfigurationProperties @ConfigurationProperties} annotated beans with
	 * Spring. Standard Spring Beans will also be scanned regardless of this value.
	 * @return {@code @ConfigurationProperties} annotated beans to register
	 * 方便快捷的注册方式
     * 带@ConfigurationProperties注释的bean。无论该值如何，将扫描标准Spring bean，并注册到IOC容器中。
     * @return @ConfigurationProperties带注释的bean注册
	 */
	Class<?>[] value() default {};

}
```

可以在该注释上看到其通过@import注解，实现ImportBeanDefinitionRegistrar注册器接口的方式，注入了一些Bean注册对象，具体源码如下所示

```java
class EnableConfigurationPropertiesRegistrar implements ImportBeanDefinitionRegistrar {
	
    // 构建一个 Bean 名称，用于在 Spring 容器中查找或注册与 EnableConfigurationProperties 注解相关的过滤器 Bean
    // 结果：EnableConfigurationPropertiesRegistrar全限定类名 + "." + "methodValidationExcludeFilter"
	private static final String METHOD_VALIDATION_EXCLUDE_FILTER_BEAN_NAME = Conventions
		.getQualifiedAttributeName(EnableConfigurationPropertiesRegistrar.class, "methodValidationExcludeFilter");

    // 在 Spring 容器启动时,EnableConfigurationPropertiesRegistrar的 registerBeanDefinitions 方法被调用，允许在 Spring 上下文中动态地注册 Bean（在 Spring 容器的 bean 定义阶段 执行）
	@Override
	public void registerBeanDefinitions(AnnotationMetadata metadata, BeanDefinitionRegistry registry) {
		// 一、注册基础的Bean定义对象
        registerInfrastructureBeans(registry);
		// 二、注册方法验证排除的Bean定义对象
        registerMethodValidationExcludeFilter(registry);
        // 三、场景注册器代理类 详情见1
		ConfigurationPropertiesBeanRegistrar beanRegistrar = new ConfigurationPropertiesBeanRegistrar(registry);
		// 四、获取注解@EnableConfigurationProperties指定的被@ConfigurationProperties注解标注的bean实例对象
    	// forEach循环调用beanRegistrar的注册器方法将@ConfigurationProperties标注的bean注册到IOC容器之中
        getTypes(metadata).forEach(beanRegistrar::register);
	}

    
    // 四、获取注解@EnableConfigurationProperties指定的被@ConfigurationProperties注解标注的bean实例对象
	private Set<Class<?>> getTypes(AnnotationMetadata metadata) {
		return metadata.getAnnotations()
            // 使用流式操作开始处理注解集合。这里选择了 EnableConfigurationProperties 注解作为流的起点
			.stream(EnableConfigurationProperties.class)
            // 对于每个 EnableConfigurationProperties 注解，使用 getClassArray(MergedAnnotation.VALUE) 方法获取 MergedAnnotation.VALUE 属性的类数组，并将其扁平化为一个流。这一步是为了处理注解中可能存在的多个类类型。
			.flatMap((annotation) -> Arrays.stream(annotation.getClassArray(MergedAnnotation.VALUE)))
			// 过滤掉 void.class 类型的元素。这一步是为了确保最终的结果集中不包含 void.class，只保留有效的类类型
            .filter((type) -> void.class != type)
            // 将流中的元素收集到一个 Set 中。由于 Set 不允许重复元素，这确保了返回的类型集合中每个类只出现一次
			.collect(Collectors.toSet());
	}

    
    // 一、注册基础的Bean定义对象
    // 用于为每个bean对象绑定的后处理器ConfigurationPropertiesBindingPostProcessor
    // 用于提供属性绑定的BoundConfigurationProperties类
	static void registerInfrastructureBeans(BeanDefinitionRegistry registry) {
		// 将后置处理器（BeanPostProcessor bean）ConfigurationPropertiesBindingPostProcessor注册到IOC容器 详情见2
        ConfigurationPropertiesBindingPostProcessor.register(registry);
		// 将提供属性绑定的BoundConfigurationProperties类注册到IOC容器之中
        BoundConfigurationProperties.register(registry);
	}

    
    
    // 二、注册方法验证排除的Bean定义对象
    // 该实例根据 ConfigurationProperties 注解来进行方法验证排除
    // 关于MethodValidationExcludeFilter类的作用可以参考官方文档
    // 其主要作用是从方法验证中排除类型的过滤器，方法级别的验证通常是使用 JSR-303/JSR-380 标准的验证注解（如 @Validated、@Valid、@NotNull 等）进行的。
    // https://docs.spring.io/spring-boot/api/java/org/springframework/boot/validation/beanvalidation/MethodValidationExcludeFilter.html
	static void registerMethodValidationExcludeFilter(BeanDefinitionRegistry registry) {
		// 检查 Bean 是否已注册
        if (!registry.containsBeanDefinition(METHOD_VALIDATION_EXCLUDE_FILTER_BEAN_NAME)) {
            // 构建 BeanDefinition
			BeanDefinition definition = BeanDefinitionBuilder
				.genericBeanDefinition(MethodValidationExcludeFilter.class,
						() -> MethodValidationExcludeFilter.byAnnotation(ConfigurationProperties.class))
                // 设置 Bean 角色为 ROLE_INFRASTRUCTURE，表明这是一个基础设施组件，通常用于框架内部，而不是应用程序业务逻辑的一部分
				.setRole(BeanDefinition.ROLE_INFRASTRUCTURE)
				.getBeanDefinition();
            // 注册 BeanDefinition：
			registry.registerBeanDefinition(METHOD_VALIDATION_EXCLUDE_FILTER_BEAN_NAME, definition);
		}
	}

}
```

**详情1 ConfigurationPropertiesBeanRegistrar**

`ConfigurationPropertiesBeanRegistrar`是`BeanDefinitionRegistry注册器`的代理实现类,通过类名称就可以分析出，其是`ConfigurationProperties类对象`的`Bean的注册类`,相关源码分析如下所示

```java
final class ConfigurationPropertiesBeanRegistrar {

    // 被代理对象：bean定义注册器
	private final BeanDefinitionRegistry registry;

    // IOC容器工厂类
	private final BeanFactory beanFactory;

    // 构造器，创建一个ConfigurationPropertiesBeanRegistrar实现类，并对bean定义注册器和IOC容器工厂类变量完成属性值的注入
	ConfigurationPropertiesBeanRegistrar(BeanDefinitionRegistry registry) {
		this.registry = registry;
		this.beanFactory = (BeanFactory) this.registry;
	}

    // 将@ConfigurationProperties注解标注的bean注册到IOC容器
	void register(Class<?> type) {
        // MergedAnnotations 是 Spring Framework 提供的一个工具类，用于处理和访问合并注解。合并注解指的是类或方法上的注解可能来源于多个地方，例如类本身、父类、接口等
		MergedAnnotation<ConfigurationProperties> annotation = MergedAnnotations
            // 用于获取指定 type（类）上的合并注解。SearchStrategy.TYPE_HIERARCHY 表示在类型层次结构中查找注解，包括类本身、其父类和接口等。
			.from(type, SearchStrategy.TYPE_HIERARCHY)
            //  type 类上存在 ConfigurationProperties 注解，则将其返回为一个 MergedAnnotation 对象。
			.get(ConfigurationProperties.class);
        // 调用register方法
		register(type, annotation);
	}

    // register注册方法
	void register(Class<?> type, MergedAnnotation<ConfigurationProperties> annotation) {
		// 调用getName方法，获取bean定义对象的名称
        String name = getName(type, annotation);
		if (!containsBeanDefinition(name)) {
            // 调用registerBeanDefinition方法，注册这个bean对象
			registerBeanDefinition(name, type, annotation);
		}
	}
	
    // 获取带有@ConfigurationProperties注解标注bean前缀的beanName
	private String getName(Class<?> type, MergedAnnotation<ConfigurationProperties> annotation) {
		String prefix = annotation.isPresent() ? annotation.getString("prefix") : "";
		return (StringUtils.hasText(prefix) ? prefix + "-" + type.getName() : type.getName());
	}

    // 判断是否已经存在名称为name的bean定义对象
	private boolean containsBeanDefinition(String name) {
		return containsBeanDefinition(this.beanFactory, name);
	}

    // 判定IOC容器中是否包含指定name的bean定义对象
	private boolean containsBeanDefinition(BeanFactory beanFactory, String name) {
		// 不同的IOC容器的实例对象获取指定bean定义对象的方式不同
        if (beanFactory instanceof ListableBeanFactory
				&& ((ListableBeanFactory) beanFactory).containsBeanDefinition(name)) {
			return true;
		}
		if (beanFactory instanceof HierarchicalBeanFactory) {
			return containsBeanDefinition(((HierarchicalBeanFactory) beanFactory).getParentBeanFactory(), name);
		}
		return false;
	}

    // 注册Bean定义对象
	private void registerBeanDefinition(String beanName, Class<?> type,
			MergedAnnotation<ConfigurationProperties> annotation) {
		Assert.state(annotation.isPresent(), () -> "No " + ConfigurationProperties.class.getSimpleName()
				+ " annotation found on  '" + type.getName() + "'.");
		this.registry.registerBeanDefinition(beanName, createBeanDefinition(beanName, type));
	}

    // 根据beanName和class实例创建BeanDefinition
	private BeanDefinition createBeanDefinition(String beanName, Class<?> type) {
		// 设置@ConfigurationProperties声明对象的绑定方式
        BindMethod bindMethod = BindMethod.forType(type);
		RootBeanDefinition definition = new RootBeanDefinition(type);
        // 设置属性，用于后面判断属性值的绑定方式
		definition.setAttribute(BindMethod.class.getName(), bindMethod);
		if (bindMethod == BindMethod.VALUE_OBJECT) {
            // 使用构造器进行绑定
            // setInstanceSupplier 方法是用来设置一个供应器（Supplier），这个供应器可以用于提供 Bean 实例的逻辑
            // 传入一个 Supplier 实现时，你实际上在告诉 Spring 框架，不要通过传统的方式（如构造函数或工厂方法）创建 Bean 的实例，而是通过调用这个 Supplier 的 get() 方法来获取 Bean 的实例
			definition.setInstanceSupplier(() -> createValueObject(beanName, type));
		}
		return definition;
	}

	private Object createValueObject(String beanName, Class<?> beanType) {
        // 调用 ConfigurationPropertiesBean 类的静态方法 forValueObject，根据 beanType 和 beanName 创建一个 ConfigurationPropertiesBean 对象。这个对象通常用于表示一个需要绑定配置属性的类。
		ConfigurationPropertiesBean bean = ConfigurationPropertiesBean.forValueObject(beanType, beanName);
		// 从当前对象所持有的 beanFactory 中获取一个 ConfigurationPropertiesBinder 实例。这个实例可能是用来处理配置属性绑定和创建对象的工具类
        ConfigurationPropertiesBinder binder = ConfigurationPropertiesBinder.get(this.beanFactory);
		try {
            // 使用 binder 对象调用 bindOrCreate 方法，将之前创建的 bean 对象传入。
            // bindOrCreate 方法用于使用构造器的方式将配置属性绑定到一个目标对象上。如果目标对象不存在或未初始化，该方法会创建一个新的对象并绑定配置属性。
			return binder.bindOrCreate(bean);
		}
		catch (Exception ex) {
			throw new ConfigurationPropertiesBindException(bean, ex);
		}
	}

}
```

关于BindMethod及其forType方法

```java
public enum BindMethod {

    /**
     * Java Bean using getter/setter binding.
     * java bean对象使用getter和setter方式进行绑定
     */
    JAVA_BEAN,

    /**
     * Value object using constructor binding.
     * value object使用构造器进行绑定
     */
    VALUE_OBJECT;

    static BindMethod forType(Class<?> type) {
        // 调用单例ConfigurationPropertiesBindConstructorProvider对象的getBindConstructor方法，判断是通过什么方式进行属性的绑定
        return (ConfigurationPropertiesBindConstructorProvider.INSTANCE.getBindConstructor(type, false) != null)
                ? VALUE_OBJECT : JAVA_BEAN;
    }

}
```



**详情2 ConfigurationPropertiesBindingPostProcessor**

从名字就可以看出，该后处理器的作用就是用来进行属性绑定

其在Bean生命周期的后处理中进行调用，包括BeanPostProcessor接口的postProcessBeforeInitialization方法，和InitializingBean的afterPropertiesSet方法，该两个方法在bean初始化后执行

```java
public class ConfigurationPropertiesBindingPostProcessor
		implements BeanPostProcessor, PriorityOrdered, ApplicationContextAware, InitializingBean {

	/**
	 * 该后处理器被注册到IOC容器中的名称
	 * The bean name that this post-processor is registered with.
	 */
	public static final String BEAN_NAME = ConfigurationPropertiesBindingPostProcessor.class.getName();

    // Spring上下文
	private ApplicationContext applicationContext;

    // 提供注册和管理 bean 定义的能力
	private BeanDefinitionRegistry registry;

    // 提供属性绑定的对象
	private ConfigurationPropertiesBinder binder;

    // 通过Aware接口设置spring上下文的属性值给成员变量applicationContext
	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}

    /*
    在bean的初始化方法中，完成了对属性registry和binder的值注入
    其中英文注释说到：不能使用应用程序上下文的构造函数注入，因为它会导致急切的工厂bean初始化。
    在 afterPropertiesSet 方法中进行注入，可以避免急切初始化问题（A依赖B，B依赖A，循环依赖问题），从而在所有依赖项都准备好之后再进行注入。这样可以避免循环依赖和其他潜在问题。
    */
	@Override
	public void afterPropertiesSet() throws Exception {
        // We can't use constructor injection of the application context because it causes eager factory bean initialization
       
        //	通过this.applicationContext的getAutowireCapableBeanFactory() 方法，该方法返回一个 ConfigurableBeanFactory 对象。ConfigurableBeanFactory 是 BeanFactory 的一个扩展，它提供了更多的配置功能，但并不直接提供注册 bean 定义的能力。
        // ConfigurableBeanFactory 实现了 BeanDefinitionRegistry，将其强制转化为BeanDefinitionRegistry并复制给成员变量registry
		this.registry = (BeanDefinitionRegistry) this.applicationContext.getAutowireCapableBeanFactory();
		
        // 从spring上下文中获取ConfigurationPropertiesBinder实例，并复制给成员变量binder
        this.binder = ConfigurationPropertiesBinder.get(this.applicationContext);
	}

    // 保证前期执行，且非最先
	@Override
	public int getOrder() {
		return Ordered.HIGHEST_PRECEDENCE + 1;
	}

    // bean 被实例化并且其属性被注入后， @PostConstruct 注解的方法或者实现了 InitializingBean 接口的 afterPropertiesSet 方法之前调用
	@Override
	public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        // 通过ConfigurationPropertiesBean类的静态方法get从 Spring 应用上下文中，利用传入的参数（applicationContext, bean, beanName）来获取 @ConfigurationProperties 注解标记的 bean 实例
        // 然后调用bind绑定方法
		bind(ConfigurationPropertiesBean.get(this.applicationContext, bean, beanName));
		// 返回绑定属性后的bean对象
        return bean;
	}
    
	// 绑定方法
	private void bind(ConfigurationPropertiesBean bean) {
        // 如果bean为null,或者bean在IOC容器中并且是以构造器方式绑定的方式，就返回
		if (bean == null || hasBoundValueObject(bean.getName())) {
			return;
		}
        // 检查 bean 的绑定方法是否为 JAVA_BEAN。如果不是，它会抛出一个异常，提示 @ConfigurationProperties 绑定到普通 bean 时出现问题，并确保没有在常规 bean 上应用 @ConstructorBinding。这个检查确保配置属性绑定的正确性。
		Assert.state(bean.getBindMethod() == BindMethod.JAVA_BEAN, "Cannot bind @ConfigurationProperties for bean '"
				+ bean.getName() + "'. Ensure that @ConstructorBinding has not been applied to regular bean");
		try {
            // bean是JAVA_BEAN绑定方式时通过递归的方式将配置文件中的属性绑定到bean对象中
            // bind 方法的目的是将配置属性注入到现有的 bean 实例中，而不是替换或创建新的对象。绑定过程是直接对原有对象进行属性填充和设置的(具体实现过程不进一步展开)
			this.binder.bind(bean);
		}
		catch (Exception ex) {
			throw new ConfigurationPropertiesBindException(bean, ex);
		}
	}

    // 判断是否是构造器方式进行绑定
	private boolean hasBoundValueObject(String beanName) {
		return this.registry.containsBeanDefinition(beanName) && BindMethod.VALUE_OBJECT
			.equals(this.registry.getBeanDefinition(beanName).getAttribute(BindMethod.class.getName()));
	}
	
	/**
	 * 如果容器中不存在ConfigurationPropertiesBindingPostProcessor对应的BeanDefinition，则将其注册到IOC
	 * Register a {@link ConfigurationPropertiesBindingPostProcessor} bean if one is not
	 * already registered.
	 * @param registry the bean definition registry
	 * @since 2.2.0
	 */
	public static void register(BeanDefinitionRegistry registry) {
		Assert.notNull(registry, "Registry must not be null");
		if (!registry.containsBeanDefinition(BEAN_NAME)) {
			BeanDefinition definition = BeanDefinitionBuilder
				.rootBeanDefinition(ConfigurationPropertiesBindingPostProcessor.class)
				.getBeanDefinition();
			definition.setRole(BeanDefinition.ROLE_INFRASTRUCTURE);
			registry.registerBeanDefinition(BEAN_NAME, definition);
		}
		ConfigurationPropertiesBinder.register(registry);
	}

}
```



### 3.3 总结

属性的绑定的整体过程如下：

- `EnableConfigurationProperties`通过`@import`完成`EnableConfigurationPropertiesRegistrar`的引入
- `EnableConfigurationPropertiesRegistrar`中主要完成`ConfigurationPropertiesBindingPostProcessorRegistrar`及`ConfigurationPropertiesBeanRegistrar`的引入

- `ConfigurationPropertiesBeanRegistrar`完成标注`@ConfigurationProperties`的类的查找并组装成`BeanDefinition`加入`registry`,如果其指定的是构造器绑定，也会在这一步完成。
- `ConfigurationPropertiesBindingPostProcessorRegistrar`完成`ConfigurationPropertiesBindingPostProcessor`及`ConfigurationBeanFactoryMetadata`引入
  - ConfigurationPropertiesBindingPostProcessor完成所有标注@ConfigurationProperties的Bean到prefix的properties值绑定
  - ConfigurationBeanFactoryMetadata仅用于提供上面处理中需要的一些元数据信息



## 三、相关问题汇总

### 3.1 自动绑定子父类

```java
@ConfigurationProperties(prefix = "banana")
@Data
public class ExamConfigProperties {

    private String id;

    private String name;
}

```

```java
@ConfigurationProperties(prefix = "banana")
@Data
public class ExamConfigProperties2  extends ExamConfigProperties{

    private int age;

}

```

```java
@Configuration
@EnableConfigurationProperties({ExamConfigProperties.class, ExamConfigProperties2.class})
public class ExamConfig {

}

```

```java
@RestController
@RequestMapping("/demo")
public class democontroller {

    @Autowired
    private ExamConfigProperties examConfigProperties;

    @Autowired
    private ExamConfigProperties2 examConfigProperties2;

    @PostConstruct
    public void tt() {
        System.out.println(examConfigProperties);
        System.out.println(examConfigProperties.getId());
        System.out.println(examConfigProperties.getName());

        System.out.println(examConfigProperties2);

        // todo 这个注解可以出一期文档
        MergedAnnotation<ConfigurationProperties> configurationPropertiesMergedAnnotation = MergedAnnotations.from(ExamConfigProperties.class, MergedAnnotations.SearchStrategy.TYPE_HIERARCHY)
                .get(ConfigurationProperties.class);
        System.out.println(configurationPropertiesMergedAnnotation);
    }
}
```

```
***************************
APPLICATION FAILED TO START
***************************

Description:

Field examConfigProperties in com.example.controller.democontroller required a single bean, but 2 were found:
	- banana-com.example.config.ExamConfigProperties2: defined in unknown location
	- banana-com.example.config.ExamConfigProperties: defined in unknown location


Action:

Consider marking one of the beans as @Primary, updating the consumer to accept multiple beans, or using @Qualifier to identify the bean that should be consumed

```

**原因：**

autowired根据类型注入，在 @Autowired
    private ExamConfigProperties examConfigProperties;父类的时候，匹配到了一个父类对象，一个其子类对象（注入的时候都是按照ExamConfigProperties注入的），所以报错了

可以使用按照名称方式进行注入

```java
@RestController
@RequestMapping("/demo")
public class democontroller {

    @Resource(name = "banana-com.example.config.ExamConfigProperties")
    private ExamConfigProperties examConfigProperties;

    @Resource(name = "banana-com.example.config.ExamConfigProperties2")
    private ExamConfigProperties2 examConfigProperties2;

    @PostConstruct
    public void tt() {


         System.out.println(examConfigProperties);
        System.out.println(examConfigProperties.getId());
        System.out.println(examConfigProperties.getName());

        System.out.println(examConfigProperties2);

        // todo 这个注解可以出一期文档
        MergedAnnotation<ConfigurationProperties> configurationPropertiesMergedAnnotation = MergedAnnotations.from(ExamConfigProperties.class, MergedAnnotations.SearchStrategy.TYPE_HIERARCHY)
                .get(ConfigurationProperties.class);
        System.out.println(configurationPropertiesMergedAnnotation);
    }
}
```

也可以使用**使用 `@Primary` 注解**注解指定哪个先注入

从上面这个案例也可以得出，对于重复的前缀，以及子父类都是可以完成属性的注入的。



### 3.2 待办

```java
 // todo 这个注解可以出一期文档
MergedAnnotation<ConfigurationProperties> configurationPropertiesMergedAnnotation = MergedAnnotations.from(ExamConfigProperties.class, MergedAnnotations.SearchStrategy.TYPE_HIERARCHY)
        .get(ConfigurationProperties.class);
System.out.println(configurationPropertiesMergedAnnotation);
```

