## FINAL、流程梳理

### 一、调用链路概述

整个mybatis查询数据的调用链如下两张图所示，仅供参考：

![流程图](FINAL%E3%80%81%E6%B5%81%E7%A8%8B%E6%A2%B3%E7%90%86.assets/05.png)

![流程图](FINAL%E3%80%81%E6%B5%81%E7%A8%8B%E6%A2%B3%E7%90%86.assets/02.png)



### 二、调用链路详解

以下内容从BindingTest#shouldExecuteBoundSelectOneBlogStatement测试方法入手，对整个流程链路进行跟踪

#### 1.SqlSessionFactory创建、初始化

首先是SqlSessionFactory对象的创建，其主要作用是读取mybatis-config.xml配置文件，并根据配置文件设置Configuration对象中各个成员变量，并将Configuration对象维护在SqlSesionFactory实例对象的成员变量中。在BindingTest测试模块中，其在前置操作是直接主动声明了一个Configuration对象，并完成了一些成员变量的配置，然后调用SqlSessionFactoryBuilder#build(Configuration config)方法创建出一个SqlSessionFactory对象，相关源码如下所示：

```java
@BeforeAll
static void setup() throws Exception {
    // 创建数据源对象
    DataSource dataSource = BaseDataTest.createBlogDataSource();
    // 传入数据源和一个名为 BLOG_DDL 的 SQL 脚本。这个方法会在数据库上执行 BLOG_DDL 脚本，通常这个脚本包含数据库的表结构定义（DDL），比如创建表、索引、约束等。
    BaseDataTest.runScript(dataSource, BaseDataTest.BLOG_DDL);
    // 传入 BLOG_DATA 脚本。这个脚本通常包含测试数据的插入（DML），它会向数据库中插入一些数据
    BaseDataTest.runScript(dataSource, BaseDataTest.BLOG_DATA);
    // 建一个 JdbcTransactionFactory 的实例，它是 MyBatis 中用于创建事务对象的工厂。JdbcTransactionFactory 使用 JDBC 来管理数据库事务
    TransactionFactory transactionFactory = new JdbcTransactionFactory();
    /**
     * 创建一个 Environment 对象，表示 MyBatis 的运行环境。Environment 定义了数据库连接、事务管理等设置
     * "Production"：环境的名称，通常是一个标识，用来区分不同的环境（比如开发、测试、生产等环境）。
     * transactionFactory：事务工厂，用于创建数据库事务。
     * dataSource：数据源，用于提供数据库连接
     */
    Environment environment = new Environment("Production", transactionFactory, dataSource);
    /**
     * 创建一个 Configuration 对象，表示 MyBatis 的配置信息。通过这个配置，MyBatis 会知道如何与数据库交互，如何管理事务、缓存等
     * 在创建 Configuration 时，将前面创建的 environment 传入，告诉 MyBatis 使用哪个数据库环境和事务管理
     */
    Configuration configuration = new Configuration(environment);
    // 这行代码启用 MyBatis 的延迟加载功能。延迟加载（Lazy Loading）意味着在访问某个对象的属性时，才会加载该属性的数据，
    // 而不是在查询时立即加载所有数据。这有助于提高性能，特别是在处理大量数据时。
    configuration.setLazyLoadingEnabled(true);
    // 设置 UseActualParamName 为 false，表示 MyBatis 在 SQL 映射中使用 #{0} 和 #{1} 等位置参数风格来传递参数，而不是使用参数的实际名字
    configuration.setUseActualParamName(false); // to test legacy style reference (#{0} #{1})
    // 注册别名，类型别名可以让我们在 SQL 映射文件中使用简短的类名，而不是使用完整的类名
    configuration.getTypeAliasRegistry().registerAlias(Blog.class);
    configuration.getTypeAliasRegistry().registerAlias(Post.class);
    configuration.getTypeAliasRegistry().registerAlias(Author.class);
    // 注册类作为 MyBatis 的映射器（Mapper），Mapper 是 MyBatis 中的接口，用于定义数据库操作（如查询、插入、更新等）。通过注册这些 Mapper，
    // MyBatis 会在后续的操作中自动映射数据库中的记录到对应的 Java 对象。
    configuration.addMapper(BoundBlogMapper.class);
    configuration.addMapper(BoundAuthorMapper.class);
    // 使用 SqlSessionFactoryBuilder 来构建 SqlSessionFactory 实例。SqlSessionFactory 是 MyBatis 的核心对象，
    // 提供了创建 SqlSession 的方法，SqlSession 用于执行 SQL 操作。
    sqlSessionFactory = new SqlSessionFactoryBuilder().build(configuration);
}
```

为了熟悉其是如何从加载mybatis配置文件的，从org.apache.ibatis.session.SqlSessionTest的setUp测试方法入手，其通过指定mybatis.xml配置文件，然后创建SqlSessionFacory对象的（这里是通过Reader作为入参，当然还有一个重载方法是通过InputStream作为输入的，最终都是通过Reader和InputStream创建XPathParser对象），相关源码如下图所示：

```java
private static SqlSessionFactory sqlMapper;

@BeforeAll
static void setup() throws Exception {
    createBlogDataSource();
    final String resource = "org/apache/ibatis/builder/MapperConfig.xml";
    final Reader reader = Resources.getResourceAsReader(resource);
    sqlMapper = new SqlSessionFactoryBuilder().build(reader);
}
```

整个调用链路如下图所示：

![mybatis创建SqlSessionFactory对象.drawio (1)](FINAL%E3%80%81%E6%B5%81%E7%A8%8B%E6%A2%B3%E7%90%86.assets/mybatis%E5%88%9B%E5%BB%BASqlSessionFactory%E5%AF%B9%E8%B1%A1.drawio%20(1).png)

#### 2、获取SqlSession

获取到SqlSessionFactory（实现类：DefaultSqlSessionFactory）后，调用其openSession方法，获取SqlSession对象

```java
SqlSession session = sqlSessionFactory.openSession()
```

相关源码如下所示：

```java
// 获得 SqlSession 对象
private SqlSession openSessionFromDataSource(ExecutorType execType, TransactionIsolationLevel level,
                                             boolean autoCommit) {
    Transaction tx = null;
    try {
        // 获得 Environment 对象
        final Environment environment = configuration.getEnvironment();
        // 创建 Transaction 对象
        // 如果environment中声明了先使用声明的TransactionFactory对象，否则使用ManagedTransactionFactory
        final TransactionFactory transactionFactory = getTransactionFactoryFromEnvironment(environment);
        tx = transactionFactory.newTransaction(environment.getDataSource(), level, autoCommit);
        // 创建 Executor 对象，即执行器对象，根据配置对象中的ExecutorType，默认为 configuration.getDefaultExecutorType() = SIMPLE
        final Executor executor = configuration.newExecutor(tx, execType);
        // 创建 DefaultSqlSession 对象
        // （需要 configuration、executor、autoCommit 三个参数，将他们维护在SqlSession对象的对应成员变量中）
        return createSqlSession(configuration, executor, autoCommit);
    } catch (Exception e) {
        // 如果发生异常，则关闭 Transaction 对象
        closeTransaction(tx); // may have fetched a connection so lets call close()
        throw ExceptionFactory.wrapException("Error opening session.  Cause: " + e, e);
    } finally {
        ErrorContext.instance().reset();
    }
}
```

#### 3、获取Mapper接口代理实例

调用SqlSession实例对象（这里实现类是DefaultSqlSession）的getMapper方法获取对应的Mapper接口实现类

```java
BoundBlogMapper mapper = session.getMapper(BoundBlogMapper.class);
```

调用Configuration对象的getMapper方法，简介调用维护的mapperRegistry成员变量的getMapper方法

```java
public <T> T getMapper(Class<T> type, SqlSession sqlSession) {
    return mapperRegistry.getMapper(type, sqlSession);
}
```

> 关于mapperRegistry中的mapper接口是在前置操作中的这一步塞入的，将当前类对象type和其MapperProxyFactory对象添加到knownMappers中：
>
> ```java
> configuration.addMapper(BoundBlogMapper.class);
> configuration.addMapper(BoundAuthorMapper.class);
> 
> /**
>    * 将符合的类，添加到 knownMappers （MapperRegistry被Configuration配置类维护）
>    * @param type 需要添加的类对象
>    */
> public <T> void addMapper(Class<T> type) {
>     if (type.isInterface()) { // 当前类需要是接口
>         if (hasMapper(type)) {  // 判断是否已经添加，如果已经添加则抛出异常
>             throw new BindingException("Type " + type + " is already known to the MapperRegistry.");
>         }
>         // 标记加载是否完成
>         boolean loadCompleted = false;
>         try {
>             // 将当前类对象type和其MapperProxyFactory对象添加到knownMappers中
>             knownMappers.put(type, new MapperProxyFactory<>(type));
>             // It's important that the type is added before the parser is run 在解析器运行之前添加类型是很重要的
>             // otherwise the binding may automatically be attempted by the 方法可能会自动尝试绑定
>             // mapper parser. If the type is already known, it won't try. 映射器解析器。如果类型是已知的，它就不会尝试
>             // 通过MapperAnnotationBuilder对象解析Mapper接口的注解配置
>             // 创建MapperAnnotationBuilder对象
>             MapperAnnotationBuilder parser = new MapperAnnotationBuilder(config, type);
>             // 调用MapperAnnotationBuilder对象的parse方法对注解进行解析
>             parser.parse();
>             // 标记加载完成
>             loadCompleted = true;
>         } finally {
>             if (!loadCompleted) { // 若加载未完成，从 knownMappers 中移除
>                 knownMappers.remove(type);
>             }
>         }
>     }
> }
> ```

通过MapperProxyFacory创建Mapper接口的Proxy对象，并返回

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

#### 4、Mapper接口代理实例执行方法

这里以查询为例，其他不同的操作其实大致上类似查询操作如下所示：

```java
Blog blog = mapper.selectBlog(1);
```

其对应的SQL直接通过注解声明（不管有没有Mapper配置类，都会封装成MappedStatement对象，并维护在Configuration对象中）：

```java
// @formatter:off
@Select("SELECT * FROM "
  + "blog WHERE id = #{id}")
// @formatter:on
Blog selectBlog(int id);
```

调用mapper接口方法的时候，会调用它其代理对象MapperProxy的invoke方法（代理对象是在初始化SqlSessionFactory的时候，入口：XMLMapperBuilder#bindMapperForNamespace，通过调用Configuration对象维护的成员变量MapperRegistry的addMapper方法，将对应接口和其代理对象加入到MapperRegistry维护的knownMappers映射中的），如下图所示

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

其首先通过MapperProxy#cachedInvoker方法，完成对当前调用方法的封装，首先封装成MapperMethod对象（在 Mapper 接口中，每个定义的方法，对应一个 MapperMethod 对象），然后再根据是否是默认方法，封装成PlainMethodInvoker和DefaultMethodInvoker：

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

然后调用PlainMethodInvoker的invoke方法，实际上是调用MapperMethod的execute方法：

![image-20250415165950052](FINAL%E3%80%81%E6%B5%81%E7%A8%8B%E6%A2%B3%E7%90%86.assets/image-20250415165950052.png)

DefaultMethodInvoker的invoke方法如下所示：

当接口有默认方法时，框架不需要自己实现这些方法，而是直接调用接口提供的默认实现

![image-20250415170028071](FINAL%E3%80%81%E6%B5%81%E7%A8%8B%E6%A2%B3%E7%90%86.assets/image-20250415170028071.png)



关于MapperMethod的方法如下图所示：

```java
// 执行对应的操作
public Object execute(SqlSession sqlSession, Object[] args) {
    Object result;
    switch (command.getType()) {
        case INSERT: {
            // 转换参数
            Object param = method.convertArgsToSqlCommandParam(args);
            // 执行 INSERT 操作
            // 转换 rowCount
            result = rowCountResult(sqlSession.insert(command.getName(), param));
            break;
        }
        case UPDATE: {
            // 转换参数
            Object param = method.convertArgsToSqlCommandParam(args);
            // 执行更新
            // 转换 rowCount
            result = rowCountResult(sqlSession.update(command.getName(), param));
            break;
        }
        case DELETE: {
            // 转换参数
            Object param = method.convertArgsToSqlCommandParam(args);
            // 转换 rowCount
            result = rowCountResult(sqlSession.delete(command.getName(), param));
            break;
        }
        case SELECT:
            // 无返回，并且有 ResultHandler 方法参数，则将查询的结果，提交给 ResultHandler 进行处理
            if (method.returnsVoid() && method.hasResultHandler()) {
                executeWithResultHandler(sqlSession, args);
                result = null;
            } else if (method.returnsMany()) { // 执行查询，返回列表
                result = executeForMany(sqlSession, args);
            } else if (method.returnsMap()) { // 执行查询，返回 Map
                result = executeForMap(sqlSession, args);
            } else if (method.returnsCursor()) { // 执行查询，返回 Cursor
                result = executeForCursor(sqlSession, args);
            } else { // 执行查询，返回单个对象
                // 转换参数
                Object param = method.convertArgsToSqlCommandParam(args);
                // 查询单条
                result = sqlSession.selectOne(command.getName(), param);
                if (method.returnsOptional() && (result == null || !method.getReturnType().equals(result.getClass()))) {
                    result = Optional.ofNullable(result);
                }
            }
            break;
        case FLUSH:
            // 刷入批处理
            result = sqlSession.flushStatements();
            break;
        default:
            throw new BindingException("Unknown execution method for: " + command.getName());
    }
    // 返回结果为 null ，并且返回类型为基本类型，则抛出 BindingException 异常
    if (result == null && method.getReturnType().isPrimitive() && !method.returnsVoid()) {
        throw new BindingException("Mapper method '" + command.getName()
                                   + "' attempted to return null from a method with a primitive return type (" + method.getReturnType() + ").");
    }
    return result;
}
```

根据测试方法：BindingTest#shouldSelectBlogWithAParamNamedValue 其对应的时序图和调用链路如下所示（仅供参考，有些调用链比较长就省略了，最后自己代码去跟着流程调试一下）：

![Mapper接口的执行](FINAL%E3%80%81%E6%B5%81%E7%A8%8B%E6%A2%B3%E7%90%86.assets/Mapper%E6%8E%A5%E5%8F%A3%E7%9A%84%E6%89%A7%E8%A1%8C.png)



### 三、相关问题详解

#### 1、关于SqlSource的创建

首先SqlSource表示SQL 来源接口，它代表从 Mapper XML 或方法注解上，读取的一条 SQL 内容，用于表示一条 SQL 语句的原始内容及其动态特性（如包含 ${} 或 #{} 的动态参数），它负责在运行时生成最终可执行的 SQL 和参数映射。

SqlSource可以分为如下4种实现:

- DynamicSqlSource：动态 SQL，含 ${}、OGNL 表达式 或 XML注解中的动态标签

  OGNL表达式如下图所示：

  ```sql
  <select id="findUser" resultType="User">
      SELECT * FROM user
      <where>
          <!-- 直接使用 OGNL 表达式 -->
          <if test="name != null and name != ''">
              AND name = #{name}
          </if>
          <if test="age > 18">
              AND age = #{age}
          </if>
      </where>
  </select>
  ```

  XML注解中的动态标签示例：

  ```sql
  <select id="findUsersByIds" resultType="User">
      SELECT * FROM user
      WHERE id IN
      <foreach item="id" collection="ids" open="(" separator="," close=")">
          #{id}
      </foreach>
  </select>
  ```

- RawSqlSource：静态SQL，适用于仅使用 #{} 表达式，无动态标签，如 ${} 或 <if>，或者不使用任何表达式的情况，所以它是静态的，仅需要在构造方法中，直接生成对应的 SQL。

- ProviderSqlSource：处理通过 @Provider 注解（如 @SelectProvider）提供的 SQL

- StaticSqlSource：最终解析后的静态 SQL（已替换所有动态部分，直接可执行），相对于 DynamicSqlSource 和 RawSqlSource 来说，StaticSqlSource.sql 属性，上面还是可能包括 ? 占位符。



不管是注解方式还是XML映射配置的SQL语句，都会封装成MappedStatement对象，MappedStatement对象中维护了一个SqlSource对象。

在XMLStatementBuilder调用parseStatementNode对select、insert、update、delete标签进行解析的时候，会通过LanguageDriver对象（语言驱动接口，默认实现类是XMLLanguageDriver类）去对当前的sql进行解析，并根据是否需要动态生成对应的SqlSource对象

![image-20250416113404612](FINAL%E3%80%81%E6%B5%81%E7%A8%8B%E6%A2%B3%E7%90%86.assets/image-20250416113404612.png)

createSqlSource方法内场景XMLScriptBuilder对象，对SQL进行解析

```java
@Override
public SqlSource createSqlSource(Configuration configuration, XNode script, Class<?> parameterType) {
    // 创建 XMLScriptBuilder 对象，执行解析
    XMLScriptBuilder builder = new XMLScriptBuilder(configuration, script, parameterType);
    return builder.parseScriptNode();
}
```

然后可以在XMLScriptBuilder的parseScriptNode接口中看到，其根据SQL是否包含动态内容生成DynamicSqlSource或者RawSqlSource

```java
public SqlSource parseScriptNode() {
    // 解析 SQL
    MixedSqlNode rootSqlNode = parseDynamicTags(context);
    // 根据是否动态 创建 SqlSource 对象
    SqlSource sqlSource;
    if (isDynamic) {
        sqlSource = new DynamicSqlSource(configuration, rootSqlNode);
    } else {
        sqlSource = new RawSqlSource(configuration, rootSqlNode, parameterType);
    }
    return sqlSource;
}
```

在MapperAnnotationBuilder的parseStatement方法中去解析注解的SQL时候，生成SqlSource对象的方法如下，其也是通过LanguageDriver对象去完成SQL是否动态的解析，并创建出对应的SqlSource

```java
final SqlSource sqlSource = buildSqlSource(statementAnnotation.getAnnotation(), parameterTypeClass,
                                           languageDriver, method);

private SqlSource buildSqlSource(Annotation annotation, Class<?> parameterType, LanguageDriver languageDriver,
                                 Method method) {
    if (annotation instanceof Select) {
        return buildSqlSourceFromStrings(((Select) annotation).value(), parameterType, languageDriver);
    }
    if (annotation instanceof Update) {
        return buildSqlSourceFromStrings(((Update) annotation).value(), parameterType, languageDriver);
    } else if (annotation instanceof Insert) {
        return buildSqlSourceFromStrings(((Insert) annotation).value(), parameterType, languageDriver);
    } else if (annotation instanceof Delete) {
        return buildSqlSourceFromStrings(((Delete) annotation).value(), parameterType, languageDriver);
    } else if (annotation instanceof SelectKey) {
        return buildSqlSourceFromStrings(((SelectKey) annotation).statement(), parameterType, languageDriver);
    }
    return new ProviderSqlSource(assistant.getConfiguration(), annotation, type, method);
}

private SqlSource buildSqlSourceFromStrings(String[] strings, Class<?> parameterTypeClass,
                                            LanguageDriver languageDriver) {
    return languageDriver.createSqlSource(configuration, String.join(" ", strings).trim(), parameterTypeClass);
}
```



#### 2、一级二级缓存实现

> 关于mybatis的一级二级缓存概述：
>
> [MyBatis](https://so.csdn.net/so/search?q=MyBatis&spm=1001.2101.3001.7020) 提供了两种缓存机制，分别是 **一级缓存** 和 **二级缓存**。它们可以显著提高数据库操作的性能，通过减少数据库的访问次数，但它们的工作原理、作用范围以及使用方式有所不同
>
> 一级缓存是 SqlSession 级别的缓存，也叫做 本地缓存。它默认开启，并且是 MyBatis 的默认缓存机制。在一次数据库会话（SqlSession）中，MyBatis 会将查询到的结果缓存到一级缓存中。如果相同的 SQL 被多次执行（在同一个 SqlSession 中），MyBatis 会从缓存中读取数据，而不去数据库中查询，这样可以减少数据库的访问。
> 二级缓存是 **SqlSessionFactory** 级别的缓存，也叫做 **全局缓存**。它在 MyBatis 的多个 `SqlSession` 之间共享缓存数据。换句话说，二级缓存的数据是跨 `SqlSession` 存在的，可以共享缓存内容。

二级缓存在CachingExecutor中实现，一级缓存在Executor基础实现类BaseExecutor中实现。

首先二级缓存是可以手动开启和关闭的，如果二级缓存开启后（默认是开启的），那么在配置对象Confuguration创建执行器的时候，会通过CachingExecutor对当前使用的执行器进行包装，如下图所示：

![image-20250416162018183](FINAL%E3%80%81%E6%B5%81%E7%A8%8B%E6%A2%B3%E7%90%86.assets/image-20250416162018183.png)

- 二级缓存

开启二级缓存后，在调用Executor对象的方法后，会先调用其包装的CachingExecutor的方法，然后再调用被包装（委托）的原始执行器的方法

![image-20250416162349035](FINAL%E3%80%81%E6%B5%81%E7%A8%8B%E6%A2%B3%E7%90%86.assets/image-20250416162349035.png)

可以看到他是通过TransactionalCacheManager对象去进行实现二级缓存的操作的

> TransactionalCacheManager：专门用于在事务性环境中协调多个 TransactionalCache 实例的缓存操作，确保缓存数据与数据库事务的一致性,因为二级缓存是支持跨 Session 进行共享，此处需要考虑事务，那么，必然需要做到事务提交时，才将当前事务中查询时产生的缓存，同步到二级缓存中，若事务回滚，这些操作将被丢弃，避免脏缓存。

```java
private final TransactionalCacheManager tcm = new TransactionalCacheManager();
```

对于不同的命名空间，其Cache实现对象是不一样的，TransactionalCacheManager对象中就维护了一个key=Cache，Value=TransactionalCache的映射对象

```java
private final Map<Cache, TransactionalCache> transactionalCaches = new HashMap<>();
```

TransactionalCache是支持事务的 Cache 实现类，其通过delegate成员变量对原始的二级缓存对象进行包装，并且将查询的结果和查询不到的内容都通过集合维护起来：

![image-20250416172050637](FINAL%E3%80%81%E6%B5%81%E7%A8%8B%E6%A2%B3%E7%90%86.assets/image-20250416172050637.png)

注意：TransactionalCache它并不存储实际缓存数据，而是管理事务期间对共享 `Cache` 的临时操作（提交或回滚），真正的缓存数据 存储在 `Mapper` 级别 的 `Cache` 对象中（如 `PerpetualCache`、`RedisCache` 等），这些 `Cache` 对象是 `SqlSessionFactory` 初始化时创建的，生命周期与 `SqlSessionFactory` 绑定：

我们可以看到在对mybatis的xml配置文件进行解析的时候，通过XMLMapperBuilder的cacheElement方法去维护Cache对象：

![image-20250416211459859](FINAL%E3%80%81%E6%B5%81%E7%A8%8B%E6%A2%B3%E7%90%86.assets/image-20250416211459859.png)

对于注释，是在MapperAnnotationBuilder的parseCache方法完成对Cache的初始化

![image-20250416211549253](FINAL%E3%80%81%E6%B5%81%E7%A8%8B%E6%A2%B3%E7%90%86.assets/image-20250416211549253.png)

在事务提交、回滚后，再将结果一起刷新到二级缓存Cache的实现对象中。即在本次会话结束，即DefualtSqlSession调用close时，关闭会话，会调用执行器的close方法

```java
// 关闭会话
@Override
public void close() {
    try {
        // 关闭执行器
        executor.close(isCommitOrRollbackRequired(false));
        // 关闭所有游标
        closeCursors();
        // 重置 dirty 为 false
        dirty = false;
    } finally {
        ErrorContext.instance().reset();
    }
}
```

然后其首先会调用包装类CachingExecutor的close方法，对tcm中数据内容进行提交或回滚（即更新到其包装的Cache实现类中）

```java
@Override
public void close(boolean forceRollback) {
    try {
        // issues #499, #524 and #573
        if (forceRollback) { // 如果强制回滚，则回滚 TransactionalCacheManager
            tcm.rollback();
        } else { // 如果强制提交，则提交 TransactionalCacheManager
            tcm.commit();
        }
    } finally {
        // 执行 delegate 对应的方法
        delegate.close(forceRollback);
    }
}
```

- 一级缓存

一级缓存位于BaseExecutor（被CachingExecutor包装的对象），通过一个成员变量实现

```java
// 本地缓存，即一级缓存
protected PerpetualCache localCache;
```

PerpetualCache类是Cache类的一个实现，相关缓存信息维护在cache成员变量中

```java
public class PerpetualCache implements Cache {

    private final String id;

    private final Map<Object, Object> cache = new HashMap<>();
    
    // ……
}
```

每次开启会话（即创建SqlSession）的时候，都会重新创建BaseExecutor对象，因此一级缓存是会话级别的，即sqlSession级别的。

#### 3、关于sqlNode

SQL Node 接口是 MyBatis 中用于处理 动态 SQL 标签 的核心接口，它代表了 XML 映射文件中动态 SQL 的各个组成部分，定义了所有动态 SQL 节点（如 <if>、<foreach>、<where> 等）的解析和执行行为，每个 XML 中的动态标签在解析后都会转换为对应的 SqlNode 实现类，通过调用apply方法对动态标签进行处理。

其实现类：

1. **StaticTextSqlNode** - 处理静态文本 SQL
2. **IfSqlNode** - 处理 `<if>` 标签
3. **TrimSqlNode** - 处理 `<trim>` 标签
4. **WhereSqlNode** - 处理 `<where>` 标签（是 TrimSqlNode 的特殊形式）
5. **SetSqlNode** - 处理 `<set>` 标签（是 TrimSqlNode 的特殊形式）
6. **ForEachSqlNode** - 处理 `<foreach>` 标签
7. **VarDeclSqlNode** - 处理 `<bind>` 标签
8. **ChooseSqlNode** - 处理 `<choose>`、`<when>`、`<otherwise>` 标签组合
9. **MixedSqlNode** - 混合多个 SqlNode

10.**TextSqlNode**-处理 包含动态表达式（如 ${}）的文本 SQL 片段 的核心类



#### 4、关于ParamNameResolver对入参的包装

```java
// 执行对应的操作
public Object execute(SqlSession sqlSession, Object[] args) {
    Object result;
    switch (command.getType()) {
        case INSERT: {
            // 转换参数
            Object param = method.convertArgsToSqlCommandParam(args);
            // 执行 INSERT 操作
            // 转换 rowCount
            result = rowCountResult(sqlSession.insert(command.getName(), param));
            break;
        }
        case UPDATE: {
            // 转换参数
            Object param = method.convertArgsToSqlCommandParam(args);
            // 执行更新
            // 转换 rowCount
            result = rowCountResult(sqlSession.update(command.getName(), param));
            break;
        }
        case DELETE: {
            // 转换参数
            Object param = method.convertArgsToSqlCommandParam(args);
            // 转换 rowCount
            result = rowCountResult(sqlSession.delete(command.getName(), param));
            break;
        }
        case SELECT:
            // 无返回，并且有 ResultHandler 方法参数，则将查询的结果，提交给 ResultHandler 进行处理
            if (method.returnsVoid() && method.hasResultHandler()) {
                executeWithResultHandler(sqlSession, args);
                result = null;
            } else if (method.returnsMany()) { // 执行查询，返回列表
                result = executeForMany(sqlSession, args);
            } else if (method.returnsMap()) { // 执行查询，返回 Map
                result = executeForMap(sqlSession, args);
            } else if (method.returnsCursor()) { // 执行查询，返回 Cursor
                result = executeForCursor(sqlSession, args);
            } else { // 执行查询，返回单个对象
                // 转换参数（入参映射关系）
                Object param = method.convertArgsToSqlCommandParam(args);
                // 查询单条
                result = sqlSession.selectOne(command.getName(), param);
                if (method.returnsOptional() && (result == null || !method.getReturnType().equals(result.getClass()))) {
                    result = Optional.ofNullable(result);
                }
            }
            break;
        case FLUSH:
            // 刷入批处理
            result = sqlSession.flushStatements();
            break;
        default:
            throw new BindingException("Unknown execution method for: " + command.getName());
    }
    // 返回结果为 null ，并且返回类型为基本类型，则抛出 BindingException 异常
    if (result == null && method.getReturnType().isPrimitive() && !method.returnsVoid()) {
        throw new BindingException("Mapper method '" + command.getName()
                                   + "' attempted to return null from a method with a primitive return type (" + method.getReturnType() + ").");
    }
    return result;
}
```

可以看到在调用MapperMethod的execute方法的时候，里面会根据操作类型进入到不同的逻辑中，但是都会去调用一个共同的操作，即转换参数：

```java
// 转换参数
Object param = method.convertArgsToSqlCommandParam(args);
```

该convertArgsToSqlCommandParam方法下，就是调用ParamNameResolver的getNamedParams完成对入参处理：

```java
// 获得 SQL 通用参数映射
public Object convertArgsToSqlCommandParam(Object[] args) {
    return paramNameResolver.getNamedParams(args);
}
```

关于ParamNameResolver成员变量：

```java
public static final String GENERIC_NAME_PREFIX = "param";

// 通过静态代码块初始化后：param0、param1、param2 ……
public static final String[] GENERIC_NAME_CACHE = new String[10];

static {
    for (int i = 0; i < 10; i++) {
        GENERIC_NAME_CACHE[i] = GENERIC_NAME_PREFIX + (i + 1);
    }
}

// 是否使用实际的参数名称
private final boolean useActualParamName;

// 参数名映射  key：参数顺序  value：参数名
/**
   * The key is the index and the value is the name of the parameter.<br />
   * The name is obtained from {@link Param} if specified. When {@link Param} is not specified, the parameter index is
   * used. Note that this index could be different from the actual index when the method has special parameters (i.e.
   * {@link RowBounds} or {@link ResultHandler}).
   * <ul>
   * <li>aMethod(@Param("M") int a, @Param("N") int b) -&gt; {{0, "M"}, {1, "N"}}</li>
   * <li>aMethod(int a, int b) -&gt; {{0, "0"}, {1, "1"}}</li>
   * <li>aMethod(int a, RowBounds rb, int b) -&gt; {{0, "0"}, {2, "1"}}</li>
   * </ul>
   */
private final SortedMap<Integer, String> names;

// 是否有@Param注解
private boolean hasParamAnnotation;
```

关于ParamNameResolver的构造器

```java
/**
* 构造器功能：初始化hasParamAnnotation、useActualParamName、names成员变量
* 1.从当前Mapper接口对应的方法的@Param注解中获取参数名称
* 2.如果没有@Param注解，看是否开启了使用实际的参数名称，如果开启了，则根据方法参数的参数名中获取参数名称
* 3.如果没有开启使用实际的参数名称，使用 map 的顺序，作为编号
* 会将上面获取到的名称放入到map中，然后赋值给成员变量names对象进行存储
*
* @param config
* @param method
*/
public ParamNameResolver(Configuration config, Method method) {
    // 从配置文件中读取参数，是否使用实际的参数名称
    this.useActualParamName = config.isUseActualParamName();
    // 获取当前方法的参数类型，并作为一个数组返回
    final Class<?>[] paramTypes = method.getParameterTypes();
    // 获取当前所有参数的注解信息（二维，一维表示哪一个参数，二维表示有哪些注释）
    final Annotation[][] paramAnnotations = method.getParameterAnnotations();
    // 声明一个TreeMap，用于参访参数顺序（key）和参数名称（value）
    final SortedMap<Integer, String> map = new TreeMap<>();
    // 获取当前参数属性
    int paramCount = paramAnnotations.length;
    // get names from @Param annotations
    for (int paramIndex = 0; paramIndex < paramCount; paramIndex++) {
        // 忽略，如果是特殊参数
        if (isSpecialParameter(paramTypes[paramIndex])) {
            // skip special parameters
            continue;
        }
        String name = null;
        for (Annotation annotation : paramAnnotations[paramIndex]) { // 首先，从 @Param 注解中获取参数
            if (annotation instanceof Param) {
                hasParamAnnotation = true;
                // 获取名称
                name = ((Param) annotation).value();
                break;
            }
        }
        if (name == null) {
            // @Param was not specified.
            if (useActualParamName) { // 其次，获取真实的参数名（如果在配置文件中开启了使用真实参数名称）
                name = getActualParamName(method, paramIndex);
            }
            if (name == null) {  // 最差，使用 map 的顺序，作为编号
                // use the parameter index as the name ("0", "1", ...)
                // gcode issue #71
                name = String.valueOf(map.size());
            }
        }
        // 添加到 map 中
        map.put(paramIndex, name);
    }
    // 构建不可变集合
    names = Collections.unmodifiableSortedMap(map);
}
```

该构造器实在场景MapperMethod的成员变量MethodSignature时候进行初始化的

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

关于getNamedParams方法如下所示：

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

> 具体示例：
>
> 示例1：
>
> ```java
> // @formatter:off
> @Select("SELECT * FROM blog "
>        + "WHERE id = #{param1} AND title = #{param2}")
> // @formatter:on
> Blog selectBlogByDefault31ParamNames(int id, String title);
> ```
>
> 调用getNamedParams结果：
>
> ![image-20250417095741933](FINAL%E3%80%81%E6%B5%81%E7%A8%8B%E6%A2%B3%E7%90%86.assets/image-20250417095741933.png)
>
> 示例2：
>
> ```java
> // @formatter:off
> @Select("SELECT * FROM blog "
>        + "WHERE ${column} = #{id} AND title = #{value}")
> // @formatter:on
> Blog selectBlogWithAParamNamedValue(@Param("column") String column, @Param("id") int id,
>     @Param("value") String title);
> ```
>
> 调用getNamedParams结果：
>
> ![image-20250417095804370](FINAL%E3%80%81%E6%B5%81%E7%A8%8B%E6%A2%B3%E7%90%86.assets/image-20250417095804370.png)



#### 5、延迟加载  △

没用过，先放着









