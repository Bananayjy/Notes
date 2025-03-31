## 十二、Binding模块

### 一、前言

#### 1、概述

> 在调用 SqlSession 相应方法执行数据库操作时，需要指定映射文件中定义的 SQL 节点，如果出现拼写错误，我们只能在运行时才能发现相应的异常。为了尽早发现这种错误，MyBatis 通过 Binding 模块，将用户自定义的 Mapper 接口与映射配置文件关联起来，系统可以通过调用自定义 Mapper 接口中的方法执行相应的 SQL 语句完成数据库操作，从而避免上述问题。
>
> 值得读者注意的是，开发人员无须编写自定义 Mapper 接口的实现，MyBatis 会自动为其创建动态代理对象。在有些场景中，自定义 Mapper 接口可以完全代替映射配置文件，但有的映射规则和 SQL 语句的定义还是写在映射配置文件中比较方便，例如动态 SQL 语句的定义。

- 传统方式执行SQL方式

XML映射文件(namespace不一定要是这种格式，任意字符串表示唯一即可)：

```xml
<!-- src/main/resources/com/example/mapper/UserMapper.xml -->
<mapper namespace="com.example.mapper.UserMapper">
    <select id="selectUserById" resultType="com.example.model.User">
        SELECT * FROM user WHERE id = #{id}
    </select>
</mapper>
```

通过SqlSession执行SQL：

```java
// 传统方式使用 SqlSession
try (SqlSession session = sqlSessionFactory.openSession()) {
    // 直接使用字符串指定映射文件中的SQL ID
    User user = session.selectOne("com.example.mapper.UserMapper.selectUserById", 1);

    // 如果这里拼写错了，比如写成 "selectUserByIde"，只有在运行时才会报错
    User user2 = session.selectOne("com.example.mapper.UserMapper.selectUserByIde", 2);
}
```

上面的代码中，`selectUserByIde` 这个拼写错误只有在程序运行时执行到这一行才会抛出异常。

- 接口方式执行SQL方式

定义一个 Mapper 接口：

```java
package com.example.mapper;

public interface UserMapper {
    User selectUserById(Integer id);
}
```

对应的映射文件 UserMapper.xml：

```java
<mapper namespace="com.example.mapper.UserMapper">
    <select id="selectUserById" resultType="com.example.model.User">
        SELECT * FROM user WHERE id = #{id}
    </select>
</mapper>
```

通过SqlSession执行SQL：

```java
try (SqlSession session = sqlSessionFactory.openSession()) {
    // 通过getMapper获取接口实例
    UserMapper mapper = session.getMapper(UserMapper.class);
    
    // 调用接口方法
    User user = mapper.selectUserById(1);
    
    // 如果这里拼写错误，比如写成 mapper.selectUserByIde(2);
    // 代码将无法编译通过，而不是等到运行时才报错
}
```

- 两者对比

1. 传统方式：
   - 使用字符串指定 SQL ID
   - 拼写错误只能在运行时发现
   - 重构困难（如重命名SQL ID时，字符串不会自动更新）
2. Mapper接口方式：
   - 通过接口方法调用SQL
   - 拼写错误在编译期就能发现
   - 支持IDE的代码补全和重构
   - 类型安全，参数和返回值都有明确的类型



#### 2、位置/包路径

相关内容位于org.apache.ibatis.binding包下，如下图所示：

![image-20250330210034087](%E5%8D%81%E4%BA%8C%E3%80%81Binding%E6%A8%A1%E5%9D%97.assets/image-20250330210034087.png)



#### 3、扩展：mybatis的接口实现类是怎么被Spring管理 

利用了Spring的扩展点，Bean工厂后处理器，默认情况下，Spring对扫描的通过@Compnent注解、@Service等注解注册为Bean的类会先将其放入到BeanDefinationMap中，并且排除接口。因此首先重写Spring的扫描类ClassPathBeanDefinitionScanner对剔除接口的方法（isCandidateComponent）进行重写，通过实现BeanDefinitionRegistryPostProcessor ，然后扫描获取接口，将这些接口注册为BeanDefination，并放入到Map中，然后将BeanDefinition的BeanClass 通过FactoryBean替换成JDK动态代理的实例，将其改为其实现类。



### 二、相关内容

#### 1、MapperRegistry

Mapper 注册表，其位于包`org.apache.ibatis.binding`下

##### 1. 成员变量

```java
// MyBatis Configuration 对象（相关配置信息）
private final Configuration config;
// MapperProxyFactory的映射
// key：Mapper接口  Value：MapperProxyFactory（创建Mapper代理实现类的工厂）
private final Map<Class<?>, MapperProxyFactory<?>> knownMappers = new ConcurrentHashMap<>();
```

##### 2.构造器

```java
// MapperRegistry构造器
public MapperRegistry(Configuration config) {
    this.config = config;
}
```

##### 3.addMappers

扫描指定包，并找到匹配的类，然后调用addMapper方法添加到knownMappers中

```java
/**
   * Adds the mappers.
   * 添加mappers
   *
   * @param packageName
   *          the package name 包名
   * @param superType
   *          the super type 当前匹配的类对象
   *
   * @since 3.2.2
   */
public void addMappers(String packageName, Class<?> superType) {
    // 通过resolverUtil扫描指定包下的指定类
    ResolverUtil<Class<?>> resolverUtil = new ResolverUtil<>();
    resolverUtil.find(new ResolverUtil.IsA(superType), packageName);
    Set<Class<? extends Class<?>>> mapperSet = resolverUtil.getClasses();
    for (Class<?> mapperClass : mapperSet) {  // 遍历匹配到的类，添加到knowMappers中
        addMapper(mapperClass);
    }
}

/**
   * Adds the mappers.
   * 添加mappers（默认匹配寻找Object类对象）
   *
   * @param packageName
   *          the package name 包名
   *
   * @since 3.2.2
   */
public void addMappers(String packageName) {
    addMappers(packageName, Object.class);
}
```

##### 4. addMapper

将符合要求的type类对象，添加到 `knownMappers` 中

```java
/**
   * 将符合的类，添加到 knownMappers 中
   * @param type 需要添加的类对象
   */
public <T> void addMapper(Class<T> type) {
    if (type.isInterface()) { // 当前类需要是接口
        if (hasMapper(type)) {  // 判断是否已经添加，如果已经添加则抛出异常
            throw new BindingException("Type " + type + " is already known to the MapperRegistry.");
        }
        // 标记加载是否完成
        boolean loadCompleted = false;
        try {
            // 将当前类对象type和其MapperProxyFactory对象添加到knownMappers中
            knownMappers.put(type, new MapperProxyFactory<>(type));
            // It's important that the type is added before the parser is run 在解析器运行之前添加类型是很重要的
            // otherwise the binding may automatically be attempted by the 方法可能会自动尝试绑定
            // mapper parser. If the type is already known, it won't try. 映射器解析器。如果类型是已知的，它就不会尝试
            // 解析Mapper的注解配置
            // 创建MapperAnnotationBuilder对象
            MapperAnnotationBuilder parser = new MapperAnnotationBuilder(config, type);
            // 调用MapperAnnotationBuilder对象的parse方法对注解进行解析
            parser.parse();
            // 标记加载完成
            loadCompleted = true;
        } finally {
            if (!loadCompleted) { // 若加载未完成，从 knownMappers 中移除
                knownMappers.remove(type);
            }
        }
    }
}
```

##### 5.hasMapper

判断，是否有对应的Mapper（即knownMappers中是否有对应的类对象type）

```java
// 判断当前类是否已经添加到 knownMappers 中
public <T> boolean hasMapper(Class<T> type) {
    return knownMappers.containsKey(type);
}
```

##### 6.getMapper

获取Mapper的代理对象

```java
// 获得 Mapper Proxy 对象
@SuppressWarnings("unchecked")
public <T> T getMapper(Class<T> type, SqlSession sqlSession) {
    // 获得 MapperProxyFactory 对象
    final MapperProxyFactory<T> mapperProxyFactory = (MapperProxyFactory<T>) knownMappers.get(type);
    if (mapperProxyFactory == null) { // 不存在，抛出异常
        throw new BindingException("Type " + type + " is not known to the MapperRegistry.");
    }
    // 创建Mapper Proxy对象
    try {
        return mapperProxyFactory.newInstance(sqlSession);
    } catch (Exception e) {
        throw new BindingException("Error getting mapper instance. Cause: " + e, e);
    }
}
```

#### 2、MapperProxyFactory

Mapper Proxy的工厂类，位于包`org.apache.ibatis.binding.MapperProxyFactory`下，相关源码如下所示：

```java
/**
 * Mapper代理对象的工厂类
 * @author Lasse Voss
 */
public class MapperProxyFactory<T> {

    // mapper接口
    private final Class<T> mapperInterface;
    // mapper方法对应的MapperMethodInvoker的映射（方法与 MapperMethod 的映射）
    private final Map<Method, MapperMethodInvoker> methodCache = new ConcurrentHashMap<>();

    // 构造器
    // 传入的mapper接口，并将其赋值给成员变量mapperInterface
    public MapperProxyFactory(Class<T> mapperInterface) {
        this.mapperInterface = mapperInterface;
    }

    // 获取mapper接口
    public Class<T> getMapperInterface() {
        return mapperInterface;
    }

    // 获取mapper方法对应的MapperMethodInvoker的映射
    public Map<Method, MapperMethodInvoker> getMethodCache() {
        return methodCache;
    }

    // 创建 Mapper Proxy 对象 （JDK动态代理实现）
    @SuppressWarnings("unchecked")
    protected T newInstance(MapperProxy<T> mapperProxy) {
        return (T) Proxy.newProxyInstance(mapperInterface.getClassLoader(), new Class[] { mapperInterface }, mapperProxy);
    }

    public T newInstance(SqlSession sqlSession) {
        final MapperProxy<T> mapperProxy = new MapperProxy<>(sqlSession, mapperInterface, methodCache);
        return newInstance(mapperProxy);
    }

}
```

#### 3、MapperProxy

mapper的代理对象，位于包org.apache.ibatis.binding.MapperProxy下，实现 InvocationHandler、Serializable 接口。

##### 1.成员变量

```java
// 序列化UID
private static final long serialVersionUID = -4724728412955527868L;

// 允许修饰符
private static final int ALLOWED_MODES = MethodHandles.Lookup.PRIVATE | MethodHandles.Lookup.PROTECTED
    | MethodHandles.Lookup.PACKAGE | MethodHandles.Lookup.PUBLIC;

private static final Constructor<Lookup> lookupConstructor;
private static final Method privateLookupInMethod;

// SqlSession 对象
private final SqlSession sqlSession;

// mapper接口
private final Class<T> mapperInterface;

// mapper方法对应的MapperMethodInvoker的映射（方法与 MapperMethod 的映射）
private final Map<Method, MapperMethodInvoker> methodCache;

```

##### 2.构造器

```java
// 构造器
public MapperProxy(SqlSession sqlSession, Class<T> mapperInterface, Map<Method, MapperMethodInvoker> methodCache) {
    this.sqlSession = sqlSession;
    this.mapperInterface = mapperInterface;
    this.methodCache = methodCache;
}
```

##### 3.静态代码初始化

```java
// 用于初始化 JDK 方法句柄（MethodHandles）相关的反射工具，目的是为了支持 JDK 8 和更高版本的不同 API 差异
/**
   * 1、检测当前 JDK 版本，选择合适的方式获取 MethodHandles.Lookup 对象
   * 2、初始化两种可能的访问方式（适配 JDK 8 和 JDK 9+ 的 API 变化）
   */
static {
    Method privateLookupIn;
    try {
        privateLookupIn = MethodHandles.class.getMethod("privateLookupIn", Class.class, MethodHandles.Lookup.class);
    } catch (NoSuchMethodException e) {
        privateLookupIn = null;
    }
    privateLookupInMethod = privateLookupIn;

    Constructor<Lookup> lookup = null;
    if (privateLookupInMethod == null) {
        // JDK 1.8
        try {
            lookup = MethodHandles.Lookup.class.getDeclaredConstructor(Class.class, int.class);
            lookup.setAccessible(true);
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException(
                "There is neither 'privateLookupIn(Class, Lookup)' nor 'Lookup(Class, int)' method in java.lang.invoke.MethodHandles.",
                e);
        } catch (Exception e) {
            lookup = null;
        }
    }
    lookupConstructor = lookup;
}
```

##### 4.invoke

```java
@Override
public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
    try {
        if (Object.class.equals(method.getDeclaringClass())) {  // 如果是Object定义的方法直接调用
            return method.invoke(this, args);
        }
        // 其他情况，先调用cachedInvoker方法，先将method封装成一个MapperMethodInvoker对象，然后调用其invoke方法
        // invoke方法本质上调用MapperMethod对象的execute方法
        return cachedInvoker(method).invoke(proxy, method, args, sqlSession);
    } catch (Throwable t) {
        throw ExceptionUtil.unwrapThrowable(t);
    }
}
```

cachedInvoker方法如下所示:

```java
private MapperMethodInvoker cachedInvoker(Method method) throws Throwable {
    try {
        // 遍历methodCache
        return MapUtil.computeIfAbsent(methodCache, method, m -> {
            if (!m.isDefault()) { // 如果不是默认方法
                return new PlainMethodInvoker(new MapperMethod(mapperInterface, method, sqlSession.getConfiguration()));
            }
            // 默认方法的处理逻辑，根据 privateLookupInMethod 的值来选择不同的获取方法句柄的方式
            try {
                if (privateLookupInMethod == null) {
                    return new DefaultMethodInvoker(getMethodHandleJava8(method));
                }
                return new DefaultMethodInvoker(getMethodHandleJava9(method));
            } catch (IllegalAccessException | InstantiationException | InvocationTargetException
                     | NoSuchMethodException e) {
                throw new RuntimeException(e);
            }
        });
    } catch (RuntimeException re) {
        Throwable cause = re.getCause();
        throw cause == null ? re : cause;
    }
}
```

会根据是否是默认方法，将其包装成MapperMethodInvoker的两个实现类：MapperMethodInvoker和DefaultMethodInvoker，本质上都维护了一个MethodHandle对象。调用MapperMethodInvoker的invoke方法，本质上就是调用MapperMethod对象的execute方法

```java
private static class PlainMethodInvoker implements MapperMethodInvoker {
    private final MapperMethod mapperMethod;

    public PlainMethodInvoker(MapperMethod mapperMethod) {
        this.mapperMethod = mapperMethod;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args, SqlSession sqlSession) throws Throwable {
        return mapperMethod.execute(sqlSession, args);
    }
}

private static class DefaultMethodInvoker implements MapperMethodInvoker {
    private final MethodHandle methodHandle;

    public DefaultMethodInvoker(MethodHandle methodHandle) {
        this.methodHandle = methodHandle;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args, SqlSession sqlSession) throws Throwable {
        return methodHandle.bindTo(proxy).invokeWithArguments(args);
    }
}
```

#### 4、MapperMethod

在 Mapper 接口中，每个定义的方法，对应一个 MapperMethod 对象。其位于包`org.apache.ibatis.binding`下。

##### 1.成员变量

```java
// SqlCommand 对象
private final SqlCommand command;
// MethodSignature 对象
private final MethodSignature method;
```

##### 2.构造方法

```java
// 构造器
public MapperMethod(Class<?> mapperInterface, Method method, Configuration config) {
    this.command = new SqlCommand(config, mapperInterface, method);
    this.method = new MethodSignature(config, mapperInterface, method);
}
```

##### 3.execute方法

```java
// 执行对应的操作
public Object execute(SqlSession sqlSession, Object[] args) {
    Object result;
    switch (command.getType()) {
        case INSERT: {
            Object param = method.convertArgsToSqlCommandParam(args);
            result = rowCountResult(sqlSession.insert(command.getName(), param));
            break;
        }
        case UPDATE: {
            Object param = method.convertArgsToSqlCommandParam(args);
            result = rowCountResult(sqlSession.update(command.getName(), param));
            break;
        }
        case DELETE: {
            Object param = method.convertArgsToSqlCommandParam(args);
            result = rowCountResult(sqlSession.delete(command.getName(), param));
            break;
        }
        case SELECT:
            if (method.returnsVoid() && method.hasResultHandler()) {
                executeWithResultHandler(sqlSession, args);
                result = null;
            } else if (method.returnsMany()) {
                result = executeForMany(sqlSession, args);
            } else if (method.returnsMap()) {
                result = executeForMap(sqlSession, args);
            } else if (method.returnsCursor()) {
                result = executeForCursor(sqlSession, args);
            } else {
                Object param = method.convertArgsToSqlCommandParam(args);
                result = sqlSession.selectOne(command.getName(), param);
                if (method.returnsOptional() && (result == null || !method.getReturnType().equals(result.getClass()))) {
                    result = Optional.ofNullable(result);
                }
            }
            break;
        case FLUSH:
            result = sqlSession.flushStatements();
            break;
        default:
            throw new BindingException("Unknown execution method for: " + command.getName());
    }
    if (result == null && method.getReturnType().isPrimitive() && !method.returnsVoid()) {
        throw new BindingException("Mapper method '" + command.getName()
                                   + "' attempted to return null from a method with a primitive return type (" + method.getReturnType() + ").");
    }
    return result;
}
```

#### 5、SqlCommand

SqlCommand ，是 MapperMethod 的内部静态类，SQL 命令。

##### 1.成员变量

```java
// {@link MappedStatement#getId()}
private final String name;

// SQL 命令类型
private final SqlCommandType type;
```

##### 2.构造方法

```java
public SqlCommand(Configuration configuration, Class<?> mapperInterface, Method method) {
    // 获取方法名称
    final String methodName = method.getName();
    // 获取方法类对象
    final Class<?> declaringClass = method.getDeclaringClass();
    // 获得 MappedStatement 对象
    MappedStatement ms = resolveMappedStatement(mapperInterface, methodName, declaringClass, configuration);
    if (ms == null) { // 未找到对应MappedStatement 对象，说明该方法上，没有对应的 SQL 声明
        if (method.getAnnotation(Flush.class) == null) { // 抛出 BindingException 异常，如果找不到 MappedStatement
            throw new BindingException(
                "Invalid bound statement (not found): " + mapperInterface.getName() + "." + methodName);
        }
        // 如果有 @Flush 注解，则标记为 FLUSH 类型，说明该方法是用于执行 flush 操作
        name = null;
        type = SqlCommandType.FLUSH;
    } else {
        // 设置 name（MappedStatement的id）
        // 对应 MappedStatement#getId() 方法获得的标识。实际上，就是 ${NAMESPACE_NAME}.${语句_ID}，
        // 例如："org.apache.ibatis.autoconstructor.AutoConstructorMapper.getSubject2"
        name = ms.getId();
        // 设置 type （当前SQL操作类型）
        type = ms.getSqlCommandType();
        if (type == SqlCommandType.UNKNOWN) { // 如果是 UNKNOWN 类型，抛出 BindingException 异常
            throw new BindingException("Unknown execution method for: " + name);
        }
    }
}
```

##### 3.resolveMappedStatement

获得 MappedStatement 对象

```java
// 获得 MappedStatement 对象
private MappedStatement resolveMappedStatement(Class<?> mapperInterface, String methodName, Class<?> declaringClass,
                                               Configuration configuration) {
    // 获取 MappedStatement的id ，即 ${NAMESPACE_NAME}.${语句_ID}
    String statementId = mapperInterface.getName() + "." + methodName;
    if (configuration.hasStatement(statementId)) {  // 如果有，获得 MappedStatement 对象，并返回
        // Configuration 里缓存了所有的 MappedStatement ，并且每一个 XML 里声明的例如 <select /> 或者 <update /> 等等，都对应一个 MappedStatement 对象
        return configuration.getMappedStatement(statementId);
    }
    if (mapperInterface.equals(declaringClass)) { // 如果没有，并且当前方法就是 declaringClass 声明的，则说明真的找不到（不用向上找了）
        return null;
    }
    // 遍历父接口，递归父接口获得 MappedStatement 对象（该方法定义在父接口中）
    for (Class<?> superInterface : mapperInterface.getInterfaces()) {
        if (declaringClass.isAssignableFrom(superInterface)) {
            MappedStatement ms = resolveMappedStatement(superInterface, methodName, declaringClass, configuration);
            if (ms != null) {
                return ms;
            }
        }
    }
    return null;
}
}
```



#### 6、MethodSignature

MethodSignature 是 MapperMethod 的内部静态类，存储方法签名

##### 1.成员变量

```java
// 返回类型是否为集合
private final boolean returnsMany;
// 返回类型是否为 Map
private final boolean returnsMap;
// 返回类型是否为 void
private final boolean returnsVoid;
// 返回类型是否为 Cursor
private final boolean returnsCursor;
// 返回类型是否为 Optional
private final boolean returnsOptional;
// 返回类型
private final Class<?> returnType;
// 返回方法上的 {@link MapKey#value()} ，前提是返回类型为 Map
private final String mapKey;
// 获得 {@link ResultHandler} 在方法参数中的位置（如果为 null ，说明不存在这个类型）
private final Integer resultHandlerIndex;
// 获得 {@link RowBounds} 在方法参数中的位置（如果为 null ，说明不存在这个类型）
private final Integer rowBoundsIndex;
// ParamNameResolver 对象
private final ParamNameResolver paramNameResolver;
```

##### 2.构造方法

```java
// 构造器
public MethodSignature(Configuration configuration, Class<?> mapperInterface, Method method) {
    // 初始化 returnType 属性
    Type resolvedReturnType = TypeParameterResolver.resolveReturnType(method, mapperInterface);
    if (resolvedReturnType instanceof Class<?>) { // 普通类
        this.returnType = (Class<?>) resolvedReturnType;
    } else if (resolvedReturnType instanceof ParameterizedType) { // 泛型
        this.returnType = (Class<?>) ((ParameterizedType) resolvedReturnType).getRawType();
    } else { // 内部类等等
        this.returnType = method.getReturnType();
    }
    // 初始化 returnsVoid 属性：判断返回类似是否为void
    this.returnsVoid = void.class.equals(this.returnType);
    // 初始化 returnsMany 属性
    this.returnsMany = configuration.getObjectFactory().isCollection(this.returnType) || this.returnType.isArray();
    // 初始化 returnsCursor 属性
    this.returnsCursor = Cursor.class.equals(this.returnType);
    // 初始化 returnsOptional 属性
    this.returnsOptional = Optional.class.equals(this.returnType);
    // 获得注解的 @MapKey的value()值，并初始化mapKey
    this.mapKey = getMapKey(method);
    // 初始化 returnsMap 属性
    this.returnsMap = this.mapKey != null;
    // 初始化rowBoundsIndex：获取RowBounds在方法参数中的位置（如果为 null ，说明不存在这个类型）
    this.rowBoundsIndex = getUniqueParamIndex(method, RowBounds.class);
    // 初始化ResultHandler：获得ResultHandler}在方法参数中的位置（如果为 null ，说明不存在这个类型）
    this.resultHandlerIndex = getUniqueParamIndex(method, ResultHandler.class);
    // 初始化 ParamNameResolver 对象
    this.paramNameResolver = new ParamNameResolver(configuration, method);
}
```

- getMapKey：获取注解@MapKey上的Value

```java
// 获得注解的 {@link MapKey#value()}
    private String getMapKey(Method method) {
      String mapKey = null;
      if (Map.class.isAssignableFrom(method.getReturnType())) { // 返回类型需要满足为 Map
        // 使用 @MapKey 注解
        final MapKey mapKeyAnnotation = method.getAnnotation(MapKey.class);
        if (mapKeyAnnotation != null) {
          // 获得 @MapKey 注解的键
          mapKey = mapKeyAnnotation.value();
        }
      }
      // 返回@MapKey 注解的键
      return mapKey;
    }
  }
```

- getUniqueParamIndex：获得指定参数类型在方法参数中的位置

```java
// 获得指定参数类型在方法参数中的位置
private Integer getUniqueParamIndex(Method method, Class<?> paramType) {
    Integer index = null;
    // 遍历方法参数
    final Class<?>[] argTypes = method.getParameterTypes();
    for (int i = 0; i < argTypes.length; i++) {
        if (paramType.isAssignableFrom(argTypes[i])) { // 类型符合
            if (index != null) { // 如果重复类型了，则抛出 BindingException 异常
                throw new BindingException(
                    method.getName() + " cannot have multiple " + paramType.getSimpleName() + " parameters");
            }
            // 获得第一次的位置
            index = i;
        }
    }
    // 返回位置
    return index;
}
```



##### 3.convertArgsToSqlCommandParam

```java
// 获得 SQL 通用参数映射
public Object convertArgsToSqlCommandParam(Object[] args) {
    return paramNameResolver.getNamedParams(args);
}
```

其调用的时反射中ParamNameResolver对象的getNamedParams方法：

```java
/**
   * A single non-special parameter is returned without a name. Multiple parameters are named using the naming rule. In
   * addition to the default names, this method also adds the generic names (param1, param2, ...).
   * 返回一个没有名称的非特殊参数。多个参数使用命名规则命名。除了默认名称之外，该方法还添加了泛型名称（param1, param2，…）
   *
   * @param args
   *          the args
   *
   * @return the named params
   */
public Object getNamedParams(Object[] args) {
    // 获取参数数量
    final int paramCount = names.size();
    if (args == null || paramCount == 0) { // 无参数，则返回 null
        return null;
    }
    if (!hasParamAnnotation && paramCount == 1) { // 只有一个非注解的参数，直接返回首元素
        Object value = args[names.firstKey()];
        return wrapToMapIfCollection(value, useActualParamName ? names.get(names.firstKey()) : null);
    } else {
        // 集合。
        // 组合 1 ：KEY：参数名（names中存储的参数名），VALUE：参数值
        // 组合 2 ：KEY：GENERIC_NAME_PREFIX + 参数顺序，VALUE ：参数值
        final Map<String, Object> param = new ParamMap<>();
        int i = 0;
        for (Map.Entry<Integer, String> entry : names.entrySet()) { // 遍历 names 集合
            // 组合 1 ：添加到 param 中
            param.put(entry.getValue(), args[entry.getKey()]);
            // add generic param names (param1, param2, ...)
            // 组合 2 ：添加到 param 中
            final String genericParamName = i < 10 ? GENERIC_NAME_CACHE[i] : GENERIC_NAME_PREFIX + (i + 1);
            // ensure not to overwrite parameter named with @Param
            if (!names.containsValue(genericParamName)) {
                param.put(genericParamName, args[entry.getKey()]);
            }
            i++;
        }
        return param;
    }
}
```



### 三、测试

△ 可以跟着测试走一下
