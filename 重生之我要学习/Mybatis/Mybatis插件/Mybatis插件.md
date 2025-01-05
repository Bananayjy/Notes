# Mybatis插件

## 一、回顾Mybatis工作原理

mybaits的工作原理如下所示

![image-20250104160828453](Mybatis%E6%8F%92%E4%BB%B6.assets/image-20250104160828453.png)

首先根据各种配置文件（全局配置文件、mysql映射文件）得到SqlSessionFactory，SqlSessionFactory又创建出SqlSession对象，SqlSession对象通过Executor去进行增删改查，每一个增删改查标签的详细信息被映射成为一个MappedStatement。Executor通过StatementHandlert去进行增删改查，本质上是通过JDBC的statement去进行的增删改查，增删改查要设置参数通过parameterHandler（借助TypeHandler）去实现，增删改查完成后通过ResultHandler（借助TypeHandler）封装结果。

## 二、关于插件开发

MyBatis在四大对象的创建过程中都会有插件进行介入。插件可以利用动态代理机制一层层的包装自标而实现在目标对象执行目标方法之前进行拦截对象的效果。MyBatis 允许在已映射语句执行过程中的某一点进行拦截调用。默认情况下，MyBatis 允许使用插件来拦截的方法（即四大对象）调用包括：

- Executor (update, query,flushStatements, commit, rollback，getTransaction,close,isClosed)

- ParameterHandler(getParameterObject, setParameters)
- ResultSetHandler (handleResultSets, handleOutputParameters)
- StatementHandler (prepare, parameterize, batch, update, query)

在四大对象创建的时候，每个创建出来的对象不是直接返回的，而是会通过如下的代码去封装一层（这也是为什么可以进行插件开发的原因，这里以ParameterHandler为例），其创建出ParameterHandler对象后，还会调用interceptorChain的pluginAll的方法对ParameterHandler对象进行封装

```java
return (ParameterHandler) interceptorChain.pluginAll(parameterHandler);
```

pluginAll方法的实现如下，获取到所有的Interceptor对象（拦截器，插件需要实现爱你的接口），然后调用Interceptor对象的plugin方法，返回target包装后的对象

```java
public Object pluginAll(Object target) {
    for (Interceptor interceptor : interceptors) {
        target = interceptor.plugin(target);
    }
    return target;
}
```

因此，我们可以利用插件为目标对象（四大对象）创建一个代理对象，在调用目标对象的时候，代理对象就可以拦截到四大对象的每一个执行，就会调用代理对象增强的方法（即AOP，面向切面）

### 三、插件实现&原理

### 3.1 实现demo

创建一个mybatis插件Interceptor实现类

```JAVA
package com.example.plugin;

import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.plugin.*;

import java.sql.Statement;
import java.util.Properties;

/**
 * @author banana
 * @create 2025-01-04 17:00
 */
// 插件签名，需要拦截哪个对象
@Intercepts(
        {
                // type：需要拦截对象的class对象 method：需要拦截的方法  入参：方法可能有重载的情况，入参情况
                @Signature(type= StatementHandler.class, method = "parameterize", args = Statement.class )
        }
)
public class MyPlugin implements Interceptor {

    /**
     * 拦截目标对象的目标方法执行
     * @param invocation
     * @return
     * @throws Throwable
     */
    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        System.out.println("拦截目标对象执行：" + invocation.getMethod());
        // 执行目标方法
        Object proceed = invocation.proceed();
        return proceed;
    }

    /**
     * 包装目标对象（为目标对象创建一个代理对象）
     * @param target
     * @return
     */
    @Override
    public Object plugin(Object target) {
        System.out.println("将要包装的对象：" + target);
        // 借助Plugin的wrap方法来使用当前Interceptor包装我们的目标对象
        // 也可以使用 Interceptor.super.plugin(target)
        Object wrap = Plugin.wrap(target, this);
        // 返回当前target创建的动态代理
        return wrap;
    }

    /**
     * 将插件注册时的property属性设置进来
     * @param properties
     */
    @Override
    public void setProperties(Properties properties) {
        System.out.println("插件配置信息：" + properties);
        Interceptor.super.setProperties(properties);
    }
}

```

在mybatis配置文件中声明插件

```xml
<?xml version="1.0" encoding="UTF-8" ?>
<!DOCTYPE configuration
 PUBLIC "-//mybatis.org//DTD Config 3.0//EN"
 "http://mybatis.org/dtd/mybatis-3-config.dtd">
<configuration>

	<!-- plugins: 注册插件 -->
	<plugins>
		<plugin interceptor="com.example.plugin.MyPlugin">
			<property name="username" value="root"/>
			<property name="password" value="12345"/>
		</plugin>
	</plugins>
	<environments default="development">
		<environment id="development">
			<transactionManager type="JDBC" />
			<dataSource type="POOLED">
				<property name="driver" value="com.mysql.cj.jdbc.Driver" />
				<property name="url" value="jdbc:mysql://192.168.56.10:3306/study_test" />
				<property name="username" value="root" />
				<property name="password" value="root" />
			</dataSource>
		</environment>
	</environments>
	<!-- 将我们写好的sql映射文件（DemoMapper.xml）一定要注册到全局配置文件（mybatis-config.xml）中 -->
	<mappers>
		<mapper resource="mapper/DemoMapper.xml" />
	</mappers>
</configuration>
```

test内容

```java
@Test
public void test01() throws IOException {
    // 1、获取sqlSessionFactory对象
    SqlSessionFactory sqlSessionFactory = getSqlSessionFactory();
    // 2、获取sqlSession对象（一个sqlSession对象代表和数据库的一次会话   ）
    SqlSession openSession = sqlSessionFactory.openSession();
    try {
        // 3、获取接口的实现类对象
        //会为接口自动的创建一个代理对象，代理对象去执行增删改查方法
        DemoMapper mapper = openSession.getMapper(DemoMapper.class);
        Demo demo = mapper.getById(1);
        System.out.println(mapper);
        System.out.println(demo);
    } finally {
        openSession.close();
    }
}
```

运行test后，显示：

![image-20250104193710251](Mybatis%E6%8F%92%E4%BB%B6.assets/image-20250104193710251.png)

### 3.2 原理

通过插件签名@Intercepts（该注解可以包含一个或多个 `@Signature` 注解，`@Signature` 描述了具体要拦截的目标类、方法以及方法参数）可以知道，定义的拦截器的目标是mybatis四大类中的`StatementHandler` 类（MyBatis 中负责执行 SQL 语句的核心类，负责将 SQL 语句和参数绑定到 `Statement` 对象上）要拦截的目标方法是 `StatementHandler` 类中的 `parameterize`（用于将参数传递给 SQL 语句）指定方法的参数类型为 `Statement`。

```java
@Intercepts(
        {
                // type：需要拦截对象的class对象 method：需要拦截的方法  入参：方法可能有重载的情况，入参情况
                @Signature(type= StatementHandler.class, method = "parameterize", args = Statement.class )
        }
)
```

可以看到四大对象在创建的时候都会调用interceptorChain的pluginAll方法

![image-20250104195557496](Mybatis%E6%8F%92%E4%BB%B6.assets/image-20250104195557496.png)

![image-20250104195740443](Mybatis%E6%8F%92%E4%BB%B6.assets/image-20250104195740443.png)

![image-20250104195751988](Mybatis%E6%8F%92%E4%BB%B6.assets/image-20250104195751988.png)

![image-20250104195802678](Mybatis%E6%8F%92%E4%BB%B6.assets/image-20250104195802678.png)

interceptorChain的pluginAll方法如下所示，遍历所有的interceptors（因为我们这里只声明了一个拦截器，所以interceptors的数量是1个），并执行拦截器的plugin方法，并将需要增强的对象（四大对象，这里即target）作为参数传入

```java
public Object pluginAll(Object target) {
    for (Interceptor interceptor : interceptors) {
        target = interceptor.plugin(target);
    }
    return target;
}
```

interceptors的数量

![image-20250104200301183](Mybatis%E6%8F%92%E4%BB%B6.assets/image-20250104200301183.png)

那么四大对象每一个都会调用我们自定义拦截器中重写的plugin方法，其中会调用Plugin类的wrap类，将需要增强类和当前自定义拦截器对象作为参数传入

![image-20250104200824322](Mybatis%E6%8F%92%E4%BB%B6.assets/image-20250104200824322.png)

Plugin的wrap方法如下所示

```java
public static Object wrap(Object target, Interceptor interceptor) {
    Map<Class<?>, Set<Method>> signatureMap = getSignatureMap(interceptor);
    Class<?> type = target.getClass();
    Class<?>[] interfaces = getAllInterfaces(type, signatureMap);
    if (interfaces.length > 0) {
        return Proxy.newProxyInstance(type.getClassLoader(), interfaces, new Plugin(target, interceptor, signatureMap));
    }
    return target;
}
```

首先调用getSignatureMap获取当前自定义拦截器签名中的信息，以需要代理的目标对象的class对象作为key，以需要增强的方法作为value，最终放到signatureMap的map对象中

![image-20250104202024494](Mybatis%E6%8F%92%E4%BB%B6.assets/image-20250104202024494.png)

通过target.getClass()方法，获取目标对象的class类对象

![image-20250104202255562](Mybatis%E6%8F%92%E4%BB%B6.assets/image-20250104202255562.png)

调用getAllInterfaces方法获取目标对象满足当前拦截器需要进行代理的对象的接口

通过获取当前目标对象的接口，并判断signatureMap中的key是包含该接口实现爱你

```java
private static Class<?>[] getAllInterfaces(Class<?> type, Map<Class<?>, Set<Method>> signatureMap) {
    Set<Class<?>> interfaces = new HashSet<>();
    while (type != null) {
        for (Class<?> c : type.getInterfaces()) {
            if (signatureMap.containsKey(c)) {
                interfaces.add(c);
            }
        }
        type = type.getSuperclass();
    }
    return interfaces.toArray(new Class<?>[0]);
}
```

这里interfaces的值（数组）就是StatementHandler接口

![image-20250104202542330](Mybatis%E6%8F%92%E4%BB%B6.assets/image-20250104202542330.png)

后面就是如果目标对象满足当前拦截器需要进行代理的对象的接口，就创建其代理对象

```java
if (interfaces.length > 0) {
    return Proxy.newProxyInstance(type.getClassLoader(), interfaces, new Plugin(target, interceptor, signatureMap));
}
```

代理对象：

![image-20250104234207639](Mybatis%E6%8F%92%E4%BB%B6.assets/image-20250104234207639.png)

之后，当目标对象调用parameterize方法的时候，就会调用到代理对象的增强方法

![image-20250104202941179](Mybatis%E6%8F%92%E4%BB%B6.assets/image-20250104202941179.png)

其实际调用的时Plugin对象（实现InvocationHandler接口，动态代理）中的invoke方法

![image-20250104203043503](Mybatis%E6%8F%92%E4%BB%B6.assets/image-20250104203043503.png)

invoke方法又会调用我们自定义拦截器的intercept方法（在调用invocation.proceed前后增加方法，及是对该方法的增强）

![image-20250104203120469](Mybatis%E6%8F%92%E4%BB%B6.assets/image-20250104203120469.png)

## 三、关于多插件

### 3.1 说明

多个插件就会产生多层代理，创建动态代理的时候，是按照插件配置顺序创建层层代理对象，并且执行目标方法之后，按照逆向顺序执行（最后一个插件先执行）

### 3.2 示例

我们再声明一个拦截器，同样是对StatementHandler对象的parameterize方法进行代理

```java
@Intercepts(
        {
                // type：需要拦截对象的class对象 method：需要拦截的方法  入参：方法可能有重载的情况，入参情况
                @Signature(type= StatementHandler.class, method = "parameterize", args = Statement.class )
        }
)
public class MyPlugin2 implements Interceptor {

    /**
     * 拦截目标对象的目标方法执行
     * @param invocation
     * @return
     * @throws Throwable
     */
    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        System.out.println("拦截目标对象执行（2）：" + invocation.getMethod());
        // 执行目标方法
        Object proceed = invocation.proceed();
        return proceed;
    }

    /**
     * 包装目标对象（为目标对象创建一个代理对象）
     * @param target
     * @return
     */
    @Override
    public Object plugin(Object target) {
        System.out.println("将要包装的对象（2）：" + target);
        // 借助Plugin的wrap方法来使用当前Interceptor包装我们的目标对象
        // 也可以使用 Interceptor.super.plugin(target)
        Object wrap = Plugin.wrap(target, this);
        // 返回当前target创建的动态代理
        return wrap;
    }

    /**
     * 将插件注册时的property属性设置进来
     * @param properties
     */
    @Override
    public void setProperties(Properties properties) {
        System.out.println("插件配置信息（2）：" + properties);
        Interceptor.super.setProperties(properties);
    }
}
```

并将其加入到配置文件中

```xml
<?xml version="1.0" encoding="UTF-8" ?>
<!DOCTYPE configuration
 PUBLIC "-//mybatis.org//DTD Config 3.0//EN"
 "http://mybatis.org/dtd/mybatis-3-config.dtd">
<configuration>

    <!-- plugins: 注册插件 -->
    <plugins>
       <plugin interceptor="com.example.plugin.MyPlugin">
          <property name="username" value="root"/>
          <property name="password" value="12345"/>
       </plugin>
       <plugin interceptor="com.example.plugin.MyPlugin2">
       </plugin>
    </plugins>
    <environments default="development">
       <environment id="development">
          <transactionManager type="JDBC" />
          <dataSource type="POOLED">
             <property name="driver" value="com.mysql.cj.jdbc.Driver" />
             <property name="url" value="jdbc:mysql://192.168.56.10:3306/study_test" />
             <property name="username" value="root" />
             <property name="password" value="root" />
          </dataSource>
       </environment>
    </environments>
    <!-- 将我们写好的sql映射文件（DemoMapper.xml）一定要注册到全局配置文件（mybatis-config.xml）中 -->
    <mappers>
       <mapper resource="mapper/DemoMapper.xml" />
    </mappers>
</configuration>
```

重新运行测试样例，可以看到此时拦截器有两个

![image-20250105001739969](Mybatis%E6%8F%92%E4%BB%B6.assets/image-20250105001739969.png)

代理对象结果

![image-20250105002119061](Mybatis%E6%8F%92%E4%BB%B6.assets/image-20250105002119061.png)



## 四、扩展

### 4.1 自定义插件开发

**目的：**现在需要自定义一个插件，使得之前通过getById查询id=1数据的时候，偷梁换柱，使得其查出来的时id=2的数据。查询方法如下所示

```java
@Test
public void test01() throws IOException {
    // 1、获取sqlSessionFactory对象
    SqlSessionFactory sqlSessionFactory = getSqlSessionFactory();
    // 2、获取sqlSession对象（一个sqlSession对象代表和数据库的一次会话   ）
    SqlSession openSession = sqlSessionFactory.openSession();
    try {
        // 3、获取接口的实现类对象
        //会为接口自动的创建一个代理对象，代理对象去执行增删改查方法
        DemoMapper mapper = openSession.getMapper(DemoMapper.class);
        Demo demo = mapper.getById(1);
        System.out.println(mapper);
        System.out.println(demo);
    } finally {
        openSession.close();
    }
}
```

**实现：**

我们只需要对StatementHandler目标对象进行代理，将其parameterize方法进行增强，在其对预编译SQL设置参数的时候，改成查询id=2的数据即可。具体实现如下

拦截对象的关系是这样的，首先我们拦截的接口是StatementHandler，其使用实现类是RoutingStatementHandler（其继承StatementHandler），其中维护一个delegate对象（StatementHandler接口实现类），其构造方法会根据StatementType的类型创建不同的实现类，并赋值给delegate对象

![image-20250105110353972](Mybatis%E6%8F%92%E4%BB%B6.assets/image-20250105110353972.png)

我们这里的类型就是PREPARED，即PreparedStatementHandler实现类，其中有个parameterize方法，就是以用来通过参数处理器parameterHandler（其通过BaseStatementHandler对象中维护，PreparedStatementHandler继承自BaseStatementHandler）来设置参数的

```java
@Override
public void parameterize(Statement statement) throws SQLException {
    parameterHandler.setParameters((PreparedStatement) statement);
}
```

而parameterHandler的实现类是DefaultParameterHandler，其参数是通过parameterObject传递的，因此我们只需要将parameterObject更改就可以实现查询id=2的数据

![image-20250105110933631](Mybatis%E6%8F%92%E4%BB%B6.assets/image-20250105110933631.png)

修改拦截的intercept方法

```java
@Intercepts(
        {
                // type：需要拦截对象的class对象 method：需要拦截的方法  入参：方法可能有重载的情况，入参情况
                @Signature(type= StatementHandler.class, method = "parameterize", args = Statement.class )
        }
)
public class MyPlugin implements Interceptor {

    /**
     * 拦截目标对象的目标方法执行
     * @param invocation
     * @return
     * @throws Throwable
     */
    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        System.out.println("拦截目标对象执行：" + invocation.getMethod());

        // 动态改变sql的参数 id=1 ===> id=2
        Object target = invocation.getTarget();
        System.out.println("被拦截的对象：" + target);

        // 拿到StatementHandler中parameterHandler对象的parameterObject
        // target元数据
        MetaObject metaObject = SystemMetaObject.forObject(target);
        Object value = metaObject.getValue("parameterHandler.parameterObject");
        System.out.println("当前sql语句的参数:" + value);
        metaObject.setValue("parameterHandler.parameterObject", 2);

        // 执行目标方法
        Object proceed = invocation.proceed();
        return proceed;
    }

    /**
     * 包装目标对象（为目标对象创建一个代理对象）
     * @param target
     * @return
     */
    @Override
    public Object plugin(Object target) {
        System.out.println("将要包装的对象：" + target);
        // 借助Plugin的wrap方法来使用当前Interceptor包装我们的目标对象
        // 也可以使用 Interceptor.super.plugin(target)
        Object wrap = Plugin.wrap(target, this);
        // 返回当前target创建的动态代理
        return wrap;
    }

    /**
     * 将插件注册时的property属性设置进来
     * @param properties
     */
    @Override
    public void setProperties(Properties properties) {
        System.out.println("插件配置信息：" + properties);
        Interceptor.super.setProperties(properties);
    }
}
```

执行结果如下

![image-20250105112112764](Mybatis%E6%8F%92%E4%BB%B6.assets/image-20250105112112764.png)

### 4.2 Mybaits的分页插件

详情查看PageHelper分页插件

### 4.3 批量执行

在通过SqlSessionFacory创建SqlSession的时候，我们可以传入参数，设置当前执行器Executor类型为批量

```java
SqlSession openSession = sqlSessionFactory.openSession(ExecutorType.BATCH);
```

对于如下操作

![image-20250105112443627](Mybatis%E6%8F%92%E4%BB%B6.assets/image-20250105112443627.png)

没有开启批量操作时，是预编译sql->设置参数->给mysql进行执行，需要10000次

如果开启批量操作，是预编译sql一次，设置参数10000次，发送给mysql执行一次。



关于spring中整合一个可以进行批量操作的SqlSession，只需要在spring配置文件中进行如下配置

```xml
<bean id="sqlSession" class="org.mybatis.spring.SqlSessionTemplate">
	<Contructor-arg name="sqlSessionFactory" ref="sqlSessionFactoryBean"></Contructor-arg>
    <Contructor-arg name="executorType" value="BATCH"></Contructor-arg>
</bean>
```

然后在用的时候，通过注入的对象方式即可使用

```java
@AutoWired
private SqlSession sqlSession
```



> 关于`SqlSession` 和 `SqlSessionTemplate`
>
> 两者都是 MyBatis 框架中用于与数据库交互的核心类，但它们的角色和用途略有不同。
>
> -  **SqlSession**
>
> `SqlSession` 是 MyBatis 中的一个接口，用于执行 SQL 语句、获取映射器 (Mapper) 对象以及管理数据库会话。它提供了对数据库的直接操作，包括增、删、改、查等常见操作。
>
> - **SqlSessionTemplate**
>
> `SqlSessionTemplate` 是 Spring 和 MyBatis 集成时提供的一个实现类，属于 Spring 的 MyBatis 支持类。它继承了 `SqlSession` 接口，并对其进行了封装。`SqlSessionTemplate` 是 Spring 环境下管理数据库会话的推荐方式，提供了更多的功能来简化 MyBatis 与 Spring 事务的整合。
>
> 总结：
>
> `SqlSession` 是 MyBatis 中的基本数据库会话接口，负责执行 SQL 和获取映射器。
>
> `SqlSessionTemplate` 是 Spring 与 MyBatis 集成时使用的模板类，提供了线程安全和事务管理等额外功能，简化了 MyBatis 与 Spring 事务管理的整合。



### 4.4 自定义类型处理器

在statementHandler进行参数设置的时候，不管是入参设置还是结果集参数映射，都是通过TypeHandler来实现的，该接口方法如下

```java
public interface TypeHandler<T> {

    // 为jdbc的PreparedStatement的参数进行设置
    void setParameter(PreparedStatement ps, int i, T parameter, JdbcType jdbcType) throws SQLException;

    // 结果集按照列名称对封装为javaBean对象
    T getResult(ResultSet rs, String columnName) throws SQLException;

    // 结果集按照列索引对封装为javaBean对象
    T getResult(ResultSet rs, int columnIndex) throws SQLException;

    // 从存储过程中按照列索引对封装为javaBean对象
    T getResult(CallableStatement cs, int columnIndex) throws SQLException;

}
```

可以看一下BaseTypeHandler（TypeHandler的基本抽象实现类，其他实现类都继承BaseTypeHandler）对setParameter方法的实现爱你

```java
@Override
public void setParameter(PreparedStatement ps, int i, T parameter, JdbcType jdbcType) throws SQLException {
    // 如果参数parameter为null或者jdbc的类型为null，则将该参数设置为null
    if (parameter == null) {
        if (jdbcType == null) {
            throw new TypeException("JDBC requires that the JdbcType must be specified for all nullable parameters.");
        }
        try {
            ps.setNull(i, jdbcType.TYPE_CODE);
        } catch (SQLException e) {
            throw new TypeException("Error setting null for parameter #" + i + " with JdbcType " + jdbcType + " . "
                                    + "Try setting a different JdbcType for this parameter or a different jdbcTypeForNull configuration property. "
                                    + "Cause: " + e, e);
        }
    } else {
        try {
            // 否则调用setNonNullParameter进行设置
            setNonNullParameter(ps, i, parameter, jdbcType);
        } catch (Exception e) {
            throw new TypeException("Error setting non null for parameter #" + i + " with JdbcType " + jdbcType + " . "
                                    + "Try setting a different JdbcType for this parameter or a different configuration property. " + "Cause: "
                                    + e, e);
        }
    }
}
```

setNonNullParameter方法，在子类中都有实现，如对String类型进行处理的StringTypeHandler，其直接调用jdbc的PreparedStatement的setString方法实现对参数的设置

![image-20250105114606707](Mybatis%E6%8F%92%E4%BB%B6.assets/image-20250105114606707.png)

