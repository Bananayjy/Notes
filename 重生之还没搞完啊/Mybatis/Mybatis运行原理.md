# Mybatis运行原理

## 一、前言

### 1.1 依赖说明

Spring Boot 提供的一个 测试启动器

> 关键功能：
>
> - **JUnit**：默认使用 JUnit 作为测试框架，支持 JUnit 5。
> - **Spring Test**：集成 Spring Test 模块，使得 Spring 容器能够在测试中启动，支持应用上下文的加载、依赖注入等。
> - **Mockito**：提供强大的模拟框架，帮助你在单元测试中模拟对象和行为。
> - **AssertJ**：一个流式的断言库，提供更丰富的断言方法来检查测试结果。
> - **Hamcrest**：与 AssertJ 类似，提供一些匹配器（Matchers）来更方便地编写断言。
> - **Spring Boot Test**：`@SpringBootTest` 注解帮助你启动 Spring Boot 应用上下文来进行集成测试。
> - **Spring MVC Test**：用于进行 Spring MVC 控制器层的测试，通常使用 `@WebMvcTest` 等注解。

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-test</artifactId>
    <scope>test</scope>
</dependency>
```

mybatis依赖

```xml
 <dependency>
    <groupId>org.mybatis</groupId>
    <artifactId>mybatis</artifactId>
    <version>3.5.16</version>
</dependency>
```

mysql连接驱动

```xml
<dependency>
    <groupId>com.mysql</groupId>
    <artifactId>mysql-connector-j</artifactId>
    <version>8.2.0</version>
</dependency>
```

lombok配置（用于自动生成实体对象的get和set方法）

```xml
<dependency>
    <groupId>org.projectlombok</groupId>
    <artifactId>lombok</artifactId>
    <version>1.18.20</version>
</dependency>
```



### 1.2 demo内容init

整体结构：

![image-20241222224154916](Mybatis%E8%BF%90%E8%A1%8C%E5%8E%9F%E7%90%86.assets/image-20241222224154916.png)

Demomapper接口

```java
package com.example.mapper;

import com.example.model.entity.Demo;

public interface DemoMapper {

    Demo getById(Integer i);
}

```

DemoMapper.xml

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN" "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="com.example.mapper.DemoMapper">

    <select id="getById" resultType="com.example.model.entity.Demo">
        select id, name from demo where id = #{id}
    </select>

</mapper>
```

实体类

```java
@Data
public class Demo {

    private Integer id;

    private String name;

}
```

DemoApplicationTests

```java
import com.example.mapper.DemoMapper;
import com.example.model.entity.Demo;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;

class DemoApplicationTests {

    public SqlSessionFactory getSqlSessionFactory() throws IOException {
        // mybatis匹配文件目录
        String resource = "mybatis-config.xml";
        // 获取配置文件输入流
        InputStream inputStream = Resources.getResourceAsStream(resource);
        // 使用SqlSessionFactoryBuilder创建SqlSessionFactory
        return new SqlSessionFactoryBuilder().build(inputStream);
    }
    
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
}
```

mybatis-config.xml

```xml
<?xml version="1.0" encoding="UTF-8" ?>
<!DOCTYPE configuration
 PUBLIC "-//mybatis.org//DTD Config 3.0//EN"
 "http://mybatis.org/dtd/mybatis-3-config.dtd">
<configuration>

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

> - environments环境
>
>   - MyBatis可以配置多种环境，比如开发、测试和生产环境需要有不同的配置。
>   - 每种环境使用一个environment标签进行配置并指定唯一标识符
>   - 可以通过environments标签中的default属性指定一个环境的标识符来快速的切换环境
>
> - environment
>
>   指定具体环境
>
>   - id:指定当前环境的唯一标识
>   - transactionManager、和dataSource都必须有
>
> - transactionManager
>
>   type:JDBC | MANAGED | 自定义
>
>   - JDBC:使用了 JDBC 的提交和回滚设置，依赖于从数据源得到的连接来管理事务范围。JdbcTransactionFactory
>   - MANAGED:不提交或回滚一个连接、让容器来管理事务的整个生命周期(比如 JEE应用服务器的上下文)。ManagedTransactionFactory
>   - 自定义:实现TransactionFactory接口，type=全类名/别名
>
> - dataSource
>
>   type:UNPOOLED| POOLED | JND |自定义
>
>   - UNPOOLED:不使用连接池,UnpooledDataSourceFactory
>   - POOLED:使用连接池，PooledDataSourceFactory
>   - NDI:在EJB 或应用服务器这类容器中查找指定的数据源
>   - 自定义:实现DataSourceFactory接口，定义数据源的获取方式。
>
>   实际开发中我们使用Spring管理数据源，并进行事务控制的配置来覆盖上述配置

数据表结果

![image-20241225213819287](Mybatis%E8%BF%90%E8%A1%8C%E5%8E%9F%E7%90%86.assets/image-20241225213819287.png)

运行一下test，结果如下所示

![image-20241225213911223](Mybatis%E8%BF%90%E8%A1%8C%E5%8E%9F%E7%90%86.assets/image-20241225213911223.png)



## 二、运行原理

对`DemoApplicationTests`中的内容进行原理分析。

Mybaits结构如下所示

![image-20241225214936045](Mybatis%E8%BF%90%E8%A1%8C%E5%8E%9F%E7%90%86.assets/image-20241225214936045.png)

Mybatis的运行原理大致如下所示

![image-20241225214810516](Mybatis%E8%BF%90%E8%A1%8C%E5%8E%9F%E7%90%86.assets/image-20241225214810516.png)

### 2.1 获取sqlSessionFactory对象

在test方法的第一行会调用`SqlSessionFactory sqlSessionFactory = getSqlSessionFactory();`获取到一个`SqlSessionFactory`对象,关于`getSqlSessionFactory`方法的源码如下所示

```java
public SqlSessionFactory getSqlSessionFactory() throws IOException {
    // mybatis匹配文件目录
    String resource = "mybatis-config.xml";
    // 获取配置文件输入流
    InputStream inputStream = Resources.getResourceAsStream(resource);
    // 使用SqlSessionFactoryBuilder创建SqlSessionFactory
    // 通过new的方式创建一个SqlSessionFactoryBuilder对象，然后调用SqlSessionFactoryBuilder的build方法，并将mybatis配置文件的输入流传入
    return new SqlSessionFactoryBuilder().build(inputStream);
}
```

其本质是调用了`SqlSessionFactoryBuilder`下`build`的另一个重载方法

```java
 public SqlSessionFactory build(InputStream inputStream) {
    return build(inputStream, null, null);
  }
```

`build(inputStream, null, null)`方法源码如下所示

```java
public SqlSessionFactory build(InputStream inputStream, String environment, Properties properties) {
    try {
        // 创建XML解析器parser
        XMLConfigBuilder parser = new XMLConfigBuilder(inputStream, environment, properties);
        // 调用XML解析器的parser方法进行解析 a
        // build b
        return build(parser.parse());
    } catch (Exception e) {
        throw ExceptionFactory.wrapException("Error building SqlSession.", e);
    } finally {
        ErrorContext.instance().reset();
        try {
            if (inputStream != null) {
                inputStream.close();
            }
        } catch (IOException e) {
            // Intentionally ignore. Prefer previous error.
        }
    }
}
```

a.调用XMLConfigBuilder中的parser方法进行解析，其源码如下

```java
public Configuration parse() {
    // 判断是否重复及解析
    if (parsed) {
        throw new BuilderException("Each XMLConfigBuilder can only be used once.");
    }
    // 标记已经解析
    parsed = true;
    // 调用xml解析器的evalNode方法计算xml配置文件的configuration节点（解析略）
   // 拿到configuration根节点后调用parseConfiguration方法
    parseConfiguration(parser.evalNode("/configuration"));
    // 解析完成后返回configuration对象（其中保存了全局配置等所有信息）
    return configuration;
}
```

parser（XML解析器）的内容如下所示

![image-20241225220944506](Mybatis%E8%BF%90%E8%A1%8C%E5%8E%9F%E7%90%86.assets/image-20241225220944506.png)

parseConfiguration方法源码如下所示

```java
private void parseConfiguration(XNode root) {
    try {
        // issue #117 read properties first
        // 解析配置文件，拿到每一个元素信息
        propertiesElement(root.evalNode("properties"));
        // 将全局配置设置为一个Properties
        Properties settings = settingsAsProperties(root.evalNode("settings"));
        loadCustomVfsImpl(settings);
        loadCustomLogImpl(settings);
        typeAliasesElement(root.evalNode("typeAliases"));
        pluginsElement(root.evalNode("plugins"));
        objectFactoryElement(root.evalNode("objectFactory"));
        objectWrapperFactoryElement(root.evalNode("objectWrapperFactory"));
        reflectorFactoryElement(root.evalNode("reflectorFactory"));
        // 将解析的全局配置settings中的信息，保存到configuration中
        settingsElement(settings);
        // read it after objectFactory and objectWrapperFactory issue #631
        environmentsElement(root.evalNode("environments"));
        databaseIdProviderElement(root.evalNode("databaseIdProvider"));
        typeHandlersElement(root.evalNode("typeHandlers"));
        // 解析mappers标签
        mappersElement(root.evalNode("mappers"));
    } catch (Exception e) {
        throw new BuilderException("Error parsing SQL Mapper Configuration. Cause: " + e, e);
    }
}
```

settingsElement(settings);方法源码如下所示

获取解析到的值，如果没有就取默认值

```java
private void settingsElement(Properties props) {
    configuration
        .setAutoMappingBehavior(AutoMappingBehavior.valueOf(props.getProperty("autoMappingBehavior", "PARTIAL")));
    configuration.setAutoMappingUnknownColumnBehavior(
        AutoMappingUnknownColumnBehavior.valueOf(props.getProperty("autoMappingUnknownColumnBehavior", "NONE")));
    configuration.setCacheEnabled(booleanValueOf(props.getProperty("cacheEnabled"), true));
    configuration.setProxyFactory((ProxyFactory) createInstance(props.getProperty("proxyFactory")));
    configuration.setLazyLoadingEnabled(booleanValueOf(props.getProperty("lazyLoadingEnabled"), false));
    configuration.setAggressiveLazyLoading(booleanValueOf(props.getProperty("aggressiveLazyLoading"), false));
    configuration.setMultipleResultSetsEnabled(booleanValueOf(props.getProperty("multipleResultSetsEnabled"), true));
    configuration.setUseColumnLabel(booleanValueOf(props.getProperty("useColumnLabel"), true));
    configuration.setUseGeneratedKeys(booleanValueOf(props.getProperty("useGeneratedKeys"), false));
    configuration.setDefaultExecutorType(ExecutorType.valueOf(props.getProperty("defaultExecutorType", "SIMPLE")));
    configuration.setDefaultStatementTimeout(integerValueOf(props.getProperty("defaultStatementTimeout"), null));
    configuration.setDefaultFetchSize(integerValueOf(props.getProperty("defaultFetchSize"), null));
    configuration.setDefaultResultSetType(resolveResultSetType(props.getProperty("defaultResultSetType")));
    configuration.setMapUnderscoreToCamelCase(booleanValueOf(props.getProperty("mapUnderscoreToCamelCase"), false));
    configuration.setSafeRowBoundsEnabled(booleanValueOf(props.getProperty("safeRowBoundsEnabled"), false));
    configuration.setLocalCacheScope(LocalCacheScope.valueOf(props.getProperty("localCacheScope", "SESSION")));
    configuration.setJdbcTypeForNull(JdbcType.valueOf(props.getProperty("jdbcTypeForNull", "OTHER")));
    configuration.setLazyLoadTriggerMethods(
        stringSetValueOf(props.getProperty("lazyLoadTriggerMethods"), "equals,clone,hashCode,toString"));
    configuration.setSafeResultHandlerEnabled(booleanValueOf(props.getProperty("safeResultHandlerEnabled"), true));
    configuration.setDefaultScriptingLanguage(resolveClass(props.getProperty("defaultScriptingLanguage")));
    configuration.setDefaultEnumTypeHandler(resolveClass(props.getProperty("defaultEnumTypeHandler")));
    configuration.setCallSettersOnNulls(booleanValueOf(props.getProperty("callSettersOnNulls"), false));
    configuration.setUseActualParamName(booleanValueOf(props.getProperty("useActualParamName"), true));
    configuration.setReturnInstanceForEmptyRow(booleanValueOf(props.getProperty("returnInstanceForEmptyRow"), false));
    configuration.setLogPrefix(props.getProperty("logPrefix"));
    configuration.setConfigurationFactory(resolveClass(props.getProperty("configurationFactory")));
    configuration.setShrinkWhitespacesInSql(booleanValueOf(props.getProperty("shrinkWhitespacesInSql"), false));
    configuration.setArgNameBasedConstructorAutoMapping(
        booleanValueOf(props.getProperty("argNameBasedConstructorAutoMapping"), false));
    configuration.setDefaultSqlProviderType(resolveClass(props.getProperty("defaultSqlProviderType")));
    configuration.setNullableOnForEach(booleanValueOf(props.getProperty("nullableOnForEach"), false));
  }
```

解析mappers标签`mappersElement(root.evalNode("mappers"));`的源码如下所示

对应mybatis-config.xml中的这个

![image-20241225222505480](Mybatis%E8%BF%90%E8%A1%8C%E5%8E%9F%E7%90%86.assets/image-20241225222505480.png)

```java
private void mappersElement(XNode context) throws Exception {
    if (context == null) {
        return;
    }
    for (XNode child : context.getChildren()) {
        if ("package".equals(child.getName())) {
            String mapperPackage = child.getStringAttribute("name");
            configuration.addMappers(mapperPackage);
        } else {
            String resource = child.getStringAttribute("resource");
            String url = child.getStringAttribute("url");
            String mapperClass = child.getStringAttribute("class");
            if (resource != null && url == null && mapperClass == null) {
                ErrorContext.instance().resource(resource);
                try (InputStream inputStream = Resources.getResourceAsStream(resource)) {
                    // 获取到mapper.xml，并通过XMLMapperBuilder创建一个xmlmapper解析器
                    XMLMapperBuilder mapperParser = new XMLMapperBuilder(inputStream, configuration, resource,
                                                                         configuration.getSqlFragments());
					// 调用xmlmapper解析器的parse方法进行解析
                    mapperParser.parse();
                }
            } else if (resource == null && url != null && mapperClass == null) {
                ErrorContext.instance().resource(url);
                try (InputStream inputStream = Resources.getUrlAsStream(url)) {
                    XMLMapperBuilder mapperParser = new XMLMapperBuilder(inputStream, configuration, url,
                                                                         configuration.getSqlFragments());
                    mapperParser.parse();
                }
            } else if (resource == null && url == null && mapperClass != null) {
                Class<?> mapperInterface = Resources.classForName(mapperClass);
                configuration.addMapper(mapperInterface);
            } else {
                throw new BuilderException(
                    "A mapper element may only specify a url, resource or class, but not more than one.");
            }
        }
    }
}
```

xmlmapper解析器如下所示（其中parser还是XPathParser）

![image-20241225223137996](Mybatis%E8%BF%90%E8%A1%8C%E5%8E%9F%E7%90%86.assets/image-20241225223137996.png)

调用xmlmapper解析器的parse方法进行解析（即mapperParser.parse();）源码如下所示

```java
public void parse() {
    if (!configuration.isResourceLoaded(resource)) {
        configurationElement(parser.evalNode("/mapper"));
        configuration.addLoadedResource(resource);
        bindMapperForNamespace();
    }
    configuration.parsePendingResultMaps(false);
    configuration.parsePendingCacheRefs(false);
    configuration.parsePendingStatements(false);
}
```

可以看到其先通过`parser.evalNode("/mapper")`拿到mapper标签，然后调用`configurationElement(parser.evalNode("/mapper"));`方法，源码如下所示

```java
private void configurationElement(XNode context) {
    try {
        //如下就是拿到mapper.xml中的某一个值
        
        // 拿到namespace
        String namespace = context.getStringAttribute("namespace");
        if (namespace == null || namespace.isEmpty()) {
            throw new BuilderException("Mapper's namespace cannot be empty");
        }
        // 将namespace设置给builderAssistant
        builderAssistant.setCurrentNamespace(namespace);
        cacheRefElement(context.evalNode("cache-ref"));
        cacheElement(context.evalNode("cache"));
        // 解析自定义参数map
        parameterMapElement(context.evalNodes("/mapper/parameterMap"));
        // 解析自定义结果集
        resultMapElements(context.evalNodes("/mapper/resultMap"));
        // 可重用的sql
        sqlElement(context.evalNodes("/mapper/sql"));
        // 解析select/insert/update/delete标签
        buildStatementFromContext(context.evalNodes("select|insert|update|delete"));
    } catch (Exception e) {
        throw new BuilderException("Error parsing Mapper XML. The XML location is '" + resource + "'. Cause: " + e, e);
    }
}
```

 mapper.xml如下所示

![image-20241225223440458](Mybatis%E8%BF%90%E8%A1%8C%E5%8E%9F%E7%90%86.assets/image-20241225223440458.png)

以上述解析select/insert/update/delete标签（即`buildStatementFromContext(context.evalNodes("select|insert|update|delete"))`）为例进行详细说明，源码如下所示

```java
private void buildStatementFromContext(List<XNode> list) {
    if (configuration.getDatabaseId() != null) {
        buildStatementFromContext(list, configuration.getDatabaseId());
    }
    buildStatementFromContext(list, null);
}
```

从configuration对象（保存配置文件中所有信息）中拿到databaseId，然后其调用buildStatementFromContext方法，源码如下

```java
private void buildStatementFromContext(List<XNode> list, String requiredDatabaseId) {
    for (XNode context : list) {
      final XMLStatementBuilder statementParser = new XMLStatementBuilder(configuration, builderAssistant, context,
          requiredDatabaseId);
      try {
        statementParser.parseStatementNode();
      } catch (IncompleteElementException e) {
        configuration.addIncompleteStatement(statementParser);
      }
    }
  }
```

这里的list就是我们要解析的语句（即增删改查标签等信息）

![image-20241226000056156](Mybatis%E8%BF%90%E8%A1%8C%E5%8E%9F%E7%90%86.assets/image-20241226000056156.png)

对于增删改查标签的解析是通过上面的statementParser调用parseStatementNode方法去完成的，其中源码如下所示

```java
public void parseStatementNode() {
    String id = context.getStringAttribute("id");
    String databaseId = context.getStringAttribute("databaseId");

    if (!databaseIdMatchesCurrent(id, databaseId, this.requiredDatabaseId)) {
        return;
    }

    String nodeName = context.getNode().getNodeName();
    SqlCommandType sqlCommandType = SqlCommandType.valueOf(nodeName.toUpperCase(Locale.ENGLISH));
    boolean isSelect = sqlCommandType == SqlCommandType.SELECT;
    boolean flushCache = context.getBooleanAttribute("flushCache", !isSelect);
    boolean useCache = context.getBooleanAttribute("useCache", isSelect);
    boolean resultOrdered = context.getBooleanAttribute("resultOrdered", false);

    // Include Fragments before parsing
    XMLIncludeTransformer includeParser = new XMLIncludeTransformer(configuration, builderAssistant);
    includeParser.applyIncludes(context.getNode());

    String parameterType = context.getStringAttribute("parameterType");
    Class<?> parameterTypeClass = resolveClass(parameterType);

    String lang = context.getStringAttribute("lang");
    LanguageDriver langDriver = getLanguageDriver(lang);

    // Parse selectKey after includes and remove them.
    processSelectKeyNodes(id, parameterTypeClass, langDriver);

    // Parse the SQL (pre: <selectKey> and <include> were parsed and removed)
    KeyGenerator keyGenerator;
    String keyStatementId = id + SelectKeyGenerator.SELECT_KEY_SUFFIX;
    keyStatementId = builderAssistant.applyCurrentNamespace(keyStatementId, true);
    if (configuration.hasKeyGenerator(keyStatementId)) {
        keyGenerator = configuration.getKeyGenerator(keyStatementId);
    } else {
        keyGenerator = context.getBooleanAttribute("useGeneratedKeys",
                                                   configuration.isUseGeneratedKeys() && SqlCommandType.INSERT.equals(sqlCommandType))
            ? Jdbc3KeyGenerator.INSTANCE : NoKeyGenerator.INSTANCE;
    }

    SqlSource sqlSource = langDriver.createSqlSource(configuration, context, parameterTypeClass);
    StatementType statementType = StatementType
        .valueOf(context.getStringAttribute("statementType", StatementType.PREPARED.toString()));
    Integer fetchSize = context.getIntAttribute("fetchSize");
    Integer timeout = context.getIntAttribute("timeout");
    String parameterMap = context.getStringAttribute("parameterMap");
    String resultType = context.getStringAttribute("resultType");
    Class<?> resultTypeClass = resolveClass(resultType);
    String resultMap = context.getStringAttribute("resultMap");
    if (resultTypeClass == null && resultMap == null) {
        resultTypeClass = MapperAnnotationBuilder.getMethodReturnType(builderAssistant.getCurrentNamespace(), id);
    }
    String resultSetType = context.getStringAttribute("resultSetType");
    ResultSetType resultSetTypeEnum = resolveResultSetType(resultSetType);
    if (resultSetTypeEnum == null) {
        resultSetTypeEnum = configuration.getDefaultResultSetType();
    }
    String keyProperty = context.getStringAttribute("keyProperty");
    String keyColumn = context.getStringAttribute("keyColumn");
    String resultSets = context.getStringAttribute("resultSets");
    boolean dirtySelect = context.getBooleanAttribute("affectData", Boolean.FALSE);

    builderAssistant.addMappedStatement(id, sqlSource, statementType, sqlCommandType, fetchSize, timeout, parameterMap,
                                        parameterTypeClass, resultMap, resultTypeClass, resultSetTypeEnum, flushCache, useCache, resultOrdered,
                                        keyGenerator, keyProperty, keyColumn, databaseId, langDriver, resultSets, dirtySelect);
}
```

其将增删改查每一个标签中的所有属性都解析出来，最后通过builderAssistant.addMappedStatement方法将其封装成一个mappedStatement（即一个mappedStatement就代表一个增删改查标签的详细信息）

在addMappedStatement方法中，会将MappedStatement对象也加入到configuration中，如下所示

```java
public MappedStatement addMappedStatement(String id, SqlSource sqlSource, StatementType statementType,
                                          SqlCommandType sqlCommandType, Integer fetchSize, Integer timeout, String parameterMap, Class<?> parameterType,
                                          String resultMap, Class<?> resultType, ResultSetType resultSetType, boolean flushCache, boolean useCache,
                                          boolean resultOrdered, KeyGenerator keyGenerator, String keyProperty, String keyColumn, String databaseId,
                                          LanguageDriver lang, String resultSets, boolean dirtySelect) {

    if (unresolvedCacheRef) {
        throw new IncompleteElementException("Cache-ref not yet resolved");
    }

    id = applyCurrentNamespace(id, false);

    MappedStatement.Builder statementBuilder = new MappedStatement.Builder(configuration, id, sqlSource, sqlCommandType)
        .resource(resource).fetchSize(fetchSize).timeout(timeout).statementType(statementType)
        .keyGenerator(keyGenerator).keyProperty(keyProperty).keyColumn(keyColumn).databaseId(databaseId).lang(lang)
        .resultOrdered(resultOrdered).resultSets(resultSets)
        .resultMaps(getStatementResultMaps(resultMap, resultType, id)).resultSetType(resultSetType)
        .flushCacheRequired(flushCache).useCache(useCache).cache(currentCache).dirtySelect(dirtySelect);

    ParameterMap statementParameterMap = getStatementParameterMap(parameterMap, parameterType, id);
    if (statementParameterMap != null) {
        statementBuilder.parameterMap(statementParameterMap);
    }

    MappedStatement statement = statementBuilder.build();
    configuration.addMappedStatement(statement);
    return statement;
}
```

可以看到MappedStatement对象中就封装了标签的相关信息

![image-20241226000942554](Mybatis%E8%BF%90%E8%A1%8C%E5%8E%9F%E7%90%86.assets/image-20241226000942554.png)



最后会将解析完成的configuration对象返回，其中封装了全局变量等所有信息。

包括：

增伤改查标签的id和对应的mappedstatement对象

![image-20241226001556172](Mybatis%E8%BF%90%E8%A1%8C%E5%8E%9F%E7%90%86.assets/image-20241226001556172.png)

mapper接口和其对应的创建工厂映射关系

![image-20241226001809443](Mybatis%E8%BF%90%E8%A1%8C%E5%8E%9F%E7%90%86.assets/image-20241226001809443.png)





b、最外层的 build(parser.parse())方法

```java
public SqlSessionFactory build(Configuration config) {
    return new DefaultSqlSessionFactory(config);
}
```

返回创建的DefaultSqlSession，包含了保存全局配置的Configuration。



**整个流程时序图：**

![image-20241226002204607](Mybatis%E8%BF%90%E8%A1%8C%E5%8E%9F%E7%90%86.assets/image-20241226002204607.png)



### 2.2 获取sqlSession对象（一个sqlSession对象代表和数据库的一次会话）

拿到sqlSessionFactory后，会调用其openSession获取一个sqlSession对象

```java
 SqlSession openSession = sqlSessionFactory.openSession();
```

其最终调用的是DefaultSqlSessionFactory的openSession方法

```java
@Override
public SqlSession openSession() {
    return openSessionFromDataSource(configuration.getDefaultExecutorType(), null, false);
}
```

从入参中可以看到去会从configuration中获取默认的ExecutorType（configuration.getDefaultExecutorType()）

![image-20241226003324067](Mybatis%E8%BF%90%E8%A1%8C%E5%8E%9F%E7%90%86.assets/image-20241226003324067.png)

> Mybatis中通过设置项可以设置三种类型的执行器：
>
> - SIMPLE
> - REUSE
> - BATCH

openSessionFromDataSource方法源码如下所示

```java
private SqlSession openSessionFromDataSource(ExecutorType execType, TransactionIsolationLevel level,
                                             boolean autoCommit) {
    Transaction tx = null;
    try {
        // 1.获取环境信息
        final Environment environment = configuration.getEnvironment();
        // 2.创建事务
        final TransactionFactory transactionFactory = getTransactionFactoryFromEnvironment(environment);
        tx = transactionFactory.newTransaction(environment.getDataSource(), level, autoCommit);
        // 3.创建执行器
        final Executor executor = configuration.newExecutor(tx, execType);
        // 4.创建defaultSqlSession，包含configuration和executor
        return new DefaultSqlSession(configuration, executor, autoCommit);
    } catch (Exception e) {
        closeTransaction(tx); // may have fetched a connection so lets call close()
        throw ExceptionFactory.wrapException("Error opening session.  Cause: " + e, e);
    } finally {
        ErrorContext.instance().reset();
    }
}
```

1.获取环境信息

```java
final Environment environment = configuration.getEnvironment()
```

2.创建事务

```java
 final TransactionFactory transactionFactory = getTransactionFactoryFromEnvironment(environment);
        tx = transactionFactory.newTransaction(environment.getDataSource(), level, autoCommit);
```

3.创建执行器

```java
final Executor executor = configuration.newExecutor(tx, execType);
```

其源码如下，根据全局配置中设置的类型创建出不同的执行器

```java
public Executor newExecutor(Transaction transaction, ExecutorType executorType) {
    executorType = executorType == null ? defaultExecutorType : executorType;
    Executor executor;
    if (ExecutorType.BATCH == executorType) {
        executor = new BatchExecutor(this, transaction);
    } else if (ExecutorType.REUSE == executorType) {
        executor = new ReuseExecutor(this, transaction);
    } else {
        executor = new SimpleExecutor(this, transaction);
    }
    if (cacheEnabled) {
        executor = new CachingExecutor(executor);
    }
    return (Executor) interceptorChain.pluginAll(executor);
}
```

如果有二级缓存，用CachingExecutor进行包装

![image-20241226004401381](Mybatis%E8%BF%90%E8%A1%8C%E5%8E%9F%E7%90%86.assets/image-20241226004401381.png)

其中实际的增删改成还是用的delegate对象，即执行器对象去进行操作的

![image-20241226004448082](Mybatis%E8%BF%90%E8%A1%8C%E5%8E%9F%E7%90%86.assets/image-20241226004448082.png)

最后会通过`(Executor) interceptorChain.pluginAll(executor)`方法，调用所有拦截器，包装我们的执行器对象，即executor

```java
public Object pluginAll(Object target) {
    for (Interceptor interceptor : interceptors) {
        target = interceptor.plugin(target);
    }
    return target;
}
```

**整个过程流程图：**

![image-20241226004914263](Mybatis%E8%BF%90%E8%A1%8C%E5%8E%9F%E7%90%86.assets/image-20241226004914263.png)



### 2.3 getMapper获取接口代理/实现对象

在2.2获取到DefaultSqlSession后，会通过其对象调用getMapper方法，获取对应接口的代理/实现对象

```java
DemoMapper mapper = openSession.getMapper(DemoMapper.class);
```

其本质上调用的是DefaultSqlSession中维护的configuration对象的getMapper方法，方法传递需要代理对象的class对象（type）和当前的DefaultSqlSession对象（this）

```java
@Override
public <T> T getMapper(Class<T> type) {
    return configuration.getMapper(type, this);
}
```

configuration对象的getMapper方法又去调用mapperRegistry的getMapper的方法

> mapperRegistry对象是configuration对象中一个重要竖向，其中维护了一个knownMappers对象（HashMap），存放的是每一个mapper对应的mapper代理工厂
>
> ![image-20250101160257901](Mybatis%E8%BF%90%E8%A1%8C%E5%8E%9F%E7%90%86.assets/image-20250101160257901.png)

```java
public <T> T getMapper(Class<T> type, SqlSession sqlSession) {
    return mapperRegistry.getMapper(type, sqlSession);
}
```

关于mapperRegistry的getMapper方法如下所示

```java
public <T> T getMapper(Class<T> type, SqlSession sqlSession) {
    // 从knownMappers对象中根据mapper接口类型获取对应mapper的代理工厂
    final MapperProxyFactory<T> mapperProxyFactory = (MapperProxyFactory<T>) knownMappers.get(type);
    // 未获取到对应的mapper代理工程，直接报错
    if (mapperProxyFactory == null) {
        throw new BindingException("Type " + type + " is not known to the MapperRegistry.");
    }
    try {
        // 调用mapper的代理工厂代理工程的newInstance方法，并传入sqlSession对象
      return mapperProxyFactory.newInstance(sqlSession);
    } catch (Exception e) {
      throw new BindingException("Error getting mapper instance. Cause: " + e, e);
    }
}
```

mapperProxyFactory的newInstance方法如下所示

```java
public T newInstance(SqlSession sqlSession) {
    // 创建mapperProxy对象（是一个InvocationHandler）
    final MapperProxy<T> mapperProxy = new MapperProxy<>(sqlSession, mapperInterface, methodCache);
    // 创建代理对象
    return newInstance(mapperProxy);
}
```

关于MapperProxy，其实现了InvocationHandler接口，InvocationHandler接口用于实现动态代理。

![image-20250101160830161](Mybatis%E8%BF%90%E8%A1%8C%E5%8E%9F%E7%90%86.assets/image-20250101160830161.png)

最后通过newInstance方法，使用java.lang包下的Proxy创建代理对象

```java
protected T newInstance(MapperProxy<T> mapperProxy) {
    return (T) Proxy.newProxyInstance(mapperInterface.getClassLoader(), new Class[] { mapperInterface }, mapperProxy);
}
```

可以看到返回的对象又包含sqlSession对象（defaultSqlSession实行类，通过其中的execute去执行增删改查操作），以及接口代理对象

![image-20250101161139722](Mybatis%E8%BF%90%E8%A1%8C%E5%8E%9F%E7%90%86.assets/image-20250101161139722.png)



**整个执行过程：**

![image-20250101161225209](Mybatis%E8%BF%90%E8%A1%8C%E5%8E%9F%E7%90%86.assets/image-20250101161225209.png)



### 2.4 执行增删改查方法

首先调用2.3中获取到的代理对象的getById方法，对指定id的数据进行查询

```mysql
Demo demo = mapper.getById(1);
```

因为其实一个InvocationHandler接口的实现，因此会先调用invoke方法

```java
@Override
public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
    try {
        // 当前调用方法是不是Object类的，而不是接口指定的方法
        if (Object.class.equals(method.getDeclaringClass())) {
            // 直接执行
            return method.invoke(this, args);
        }
        // 在调用invoke方法前，会先调用cachedInvoker方法对method进行包装为MapperMethod （a）
        // 然后调用invoke方法调用mapperMethod的execute方法进行执行 （b）
        return cachedInvoker(method).invoke(proxy, method, args, sqlSession);
    } catch (Throwable t) {
        throw ExceptionUtil.unwrapThrowable(t);
    }
}
```

method.getDeclaringClass()可以看出当前getById是DemoMapper接口的，而不是Object接口的（如toString等方法）

![image-20250101161930560](Mybatis%E8%BF%90%E8%A1%8C%E5%8E%9F%E7%90%86.assets/image-20250101161930560.png)

（a）调用cachedInvoker方法对method进行包装为MapperMethod 

可以看到其通过new MapperMethod方法创建了一个MapperMethod 对象，并且外面又通过new PlainMethodInvoker构造函数，包了一层PlainMethodInvoker对象

```java
private MapperMethodInvoker cachedInvoker(Method method) throws Throwable {
    try {
        return MapUtil.computeIfAbsent(methodCache, method, m -> {
            if (!m.isDefault()) {
                return new PlainMethodInvoker(new MapperMethod(mapperInterface, method, sqlSession.getConfiguration()));
            }
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

（b）调用invoke方法调用mapperMethod的execute方法进行执行

```java
interface MapperMethodInvoker {
    Object invoke(Object proxy, Method method, Object[] args, SqlSession sqlSession) throws Throwable;
}
```

此时实现类是PlainMethodInvoker，即调用的PlainMethodInvoker中的invoke方法，其调用的是mapperMethod的execute方法

![image-20250101162804933](Mybatis%E8%BF%90%E8%A1%8C%E5%8E%9F%E7%90%86.assets/image-20250101162804933.png)

mapperMethod的execute方法如下所示

```java
public Object execute(SqlSession sqlSession, Object[] args) {
    Object result;
    // 首先判断SQL的类型（增删改查）
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
                // 返回空的如何执行
                executeWithResultHandler(sqlSession, args);
                result = null;
            } else if (method.returnsMany()) {
                // 返回多个参数的如何执行
                result = executeForMany(sqlSession, args);
            } else if (method.returnsMap()) {
                // 返回map的如何执行
                result = executeForMap(sqlSession, args);
            } else if (method.returnsCursor()) {
                // 返回cursor的如何执行
                result = executeForCursor(sqlSession, args);
            } else {
                // 其他的如何执行
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

当前首先会进入最后一个判断条件，即else条件，首先调用的是MpperMethod方法的convertArgsToSqlCommandParam的方法

```
Object param = method.convertArgsToSqlCommandParam(args);
```

其调用的是paramNameResolver的convertArgsToSqlCommandParam的方法

```java
public Object convertArgsToSqlCommandParam(Object[] args) {
  return paramNameResolver.getNamedParams(args);
}
```

其源码如下所示，即包装参数为一个map对象或是直接返回

```java
public Object getNamedParams(Object[] args) {
    final int paramCount = names.size();
    if (args == null || paramCount == 0) {
        return null;
    }
    if (!hasParamAnnotation && paramCount == 1) {
        Object value = args[names.firstKey()];
        return wrapToMapIfCollection(value, useActualParamName ? names.get(names.firstKey()) : null);
    } else {
        final Map<String, Object> param = new ParamMap<>();
        int i = 0;
        for (Map.Entry<Integer, String> entry : names.entrySet()) {
            param.put(entry.getValue(), args[entry.getKey()]);
            // add generic param names (param1, param2, ...)
            final String genericParamName = GENERIC_NAME_PREFIX + (i + 1);
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

然后调用sqlSession对象（defualtSqlSession）的selectOne方法（不同的返回类型调用不同的方法，如有多个返回参数，调用的时sqlSession的selectList方法），并将command.getName()即方法名称和上面包装的参数传入。

调用的DefualtSqlSession方法的selectList如下所示，虽然是调用单个，但本质上还是调用的selectList方法，只不过只返回其中的第一个。如果有多个直接抛出异常

```java
@Override
public <T> T selectOne(String statement, Object parameter) {
    // Popular vote was to return null on 0 results and throw exception on too many.
    List<T> list = this.selectList(statement, parameter);
    if (list.size() == 1) {
        return list.get(0);
    }
    if (list.size() > 1) {
        throw new TooManyResultsException(
            "Expected one result (or null) to be returned by selectOne(), but found: " + list.size());
    } else {
        return null;
    }
}
```

selectList会调用其重载的方法

```java
@Override
public <E> List<E> selectList(String statement, Object parameter) {
	return this.selectList(statement, parameter, RowBounds.DEFAULT);
}
```

```java
private <E> List<E> selectList(String statement, Object parameter, RowBounds rowBounds, ResultHandler handler) {
    try {
        // 获取到对应接口方法对应的封装了的xml中配置的增删改查标签详细信息
        MappedStatement ms = configuration.getMappedStatement(statement);
        dirty |= ms.isDirtySelect();
        // 调用executor的query方法
        return executor.query(ms, wrapCollection(parameter), rowBounds, handler);
    } catch (Exception e) {
        throw ExceptionFactory.wrapException("Error querying database.  Cause: " + e, e);
    } finally {
        ErrorContext.instance().reset();
    }
}
```

在调用executor的query方法的时候，又会调用wrapCollection对已经包装的参数再进行一次包装，其如下所示，其调用的也是ParamNameResolver的wrapToMapIfCollection方法

```java
private Object wrapCollection(final Object object) {
    return ParamNameResolver.wrapToMapIfCollection(object, null);
}
```

该方法本质上就是包装集合的过程，将集合放到map对象中，并返回

```java
public static Object wrapToMapIfCollection(Object object, String actualParamName) {
    if (object instanceof Collection) {
        ParamMap<Object> map = new ParamMap<>();
        map.put("collection", object);
        if (object instanceof List) {
            map.put("list", object);
        }
        Optional.ofNullable(actualParamName).ifPresent(name -> map.put(name, object));
        return map;
    }
    if (object != null && object.getClass().isArray()) {
        ParamMap<Object> map = new ParamMap<>();
        map.put("array", object);
        Optional.ofNullable(actualParamName).ifPresent(name -> map.put(name, object));
        return map;
    }
    return object;
}
```

最后调用的CachingExecutor的query方法

```java
@Override
public <E> List<E> query(MappedStatement ms, Object parameterObject, RowBounds rowBounds, ResultHandler resultHandler)
    throws SQLException {
    BoundSql boundSql = ms.getBoundSql(parameterObject);
    CacheKey key = createCacheKey(ms, parameterObject, rowBounds, boundSql);
    return query(ms, parameterObject, rowBounds, resultHandler, key, boundSql);
}
```

首先从MappedStatement对象中获取绑定的sql对象信息，即BoundSql对象（即sql的详细信息）

> sql: sql语句
>
> parameterMappings：参数在sql中映射关系
>
> parameterObject： 参数值
>
> additionalParameters：额外参数
>
> metaParameters:……

![image-20250101165105795](Mybatis%E8%BF%90%E8%A1%8C%E5%8E%9F%E7%90%86.assets/image-20250101165105795.png)

之后，通过createCacheKey，获取到缓存需要保存的key（很长，方法id+sql+参数……）

```
CacheKey key = createCacheKey(ms, parameterObject, rowBounds, boundSql);
```

再调用query的重载方法

```
query(ms, parameterObject, rowBounds, resultHandler, key, boundSql);
```

```java
@Override
public <E> List<E> query(MappedStatement ms, Object parameterObject, RowBounds rowBounds, ResultHandler resultHandler,
                         CacheKey key, BoundSql boundSql) throws SQLException {
    // 从MappedStatement中获取缓存
    Cache cache = ms.getCache();
    // 如果有缓存的执行逻辑，即二级缓存
    if (cache != null) {
        flushCacheIfRequired(ms);
        if (ms.isUseCache() && resultHandler == null) {
            ensureNoOutParams(ms, boundSql);
            @SuppressWarnings("unchecked")
            List<E> list = (List<E>) tcm.getObject(cache, key);
            if (list == null) {
                list = delegate.query(ms, parameterObject, rowBounds, resultHandler, key, boundSql);
                tcm.putObject(cache, key, list); // issue #578 and #116
            }
            return list;
        }
    }
    // 因为当前对象是CacheExecutor，是对Executor对象的包装，因此其实际调用的还是之前执行对象，即SimpleExecutor的query方法
    return delegate.query(ms, parameterObject, rowBounds, resultHandler, key, boundSql);
}
```

实际调用的SimpleExecutor父类BaseExecutor的query方法（SimpleExecutor没有重写该方法）

![image-20250101165826990](Mybatis%E8%BF%90%E8%A1%8C%E5%8E%9F%E7%90%86.assets/image-20250101165826990.png)

```java
@Override
public <E> List<E> query(MappedStatement ms, Object parameter, RowBounds rowBounds, ResultHandler resultHandler,
                         CacheKey key, BoundSql boundSql) throws SQLException {
    // 获取资源
    ErrorContext.instance().resource(ms.getResource()).activity("executing a query").object(ms.getId());
    if (closed) {
        throw new ExecutorException("Executor was closed.");
    }
    if (queryStack == 0 && ms.isFlushCacheRequired()) {
        clearLocalCache();
    }
    List<E> list;
    try {
        queryStack++;
        // 从本地缓存中获取对应的结果值（localCache.getObject(key)）即一级缓存
        list = resultHandler == null ? (List<E>) localCache.getObject(key) : null;
        if (list != null) {
            handleLocallyCachedOutputParameters(ms, key, parameter, boundSql);
        } else {
            // 如果一级缓存中没有缓存，调用queryFromDatabase方法
            list = queryFromDatabase(ms, parameter, rowBounds, resultHandler, key, boundSql);
        }
    } finally {
        queryStack--;
    }
    if (queryStack == 0) {
        for (DeferredLoad deferredLoad : deferredLoads) {
            deferredLoad.load();
        }
        // issue #601
        deferredLoads.clear();
        if (configuration.getLocalCacheScope() == LocalCacheScope.STATEMENT) {
            // issue #482
            clearLocalCache();
        }
    }
    return list;
}
```

queryFromDatabase方法如下所示

```java
private <E> List<E> queryFromDatabase(MappedStatement ms, Object parameter, RowBounds rowBounds,
                                      ResultHandler resultHandler, CacheKey key, BoundSql boundSql) throws SQLException {
    List<E> list;
    // 先将key放到本地缓存中，并且其值为一个占位符EXECUTION_PLACEHOLDER
    localCache.putObject(key, EXECUTION_PLACEHOLDER);
    try {
        list = doQuery(ms, parameter, rowBounds, resultHandler, boundSql);
    } finally {
        localCache.removeObject(key);
    }
    // 查出数据后，将list结果放到上面的占位符中
    localCache.putObject(key, list);
    if (ms.getStatementType() == StatementType.CALLABLE) {
        localOutputParameterCache.putObject(key, parameter);
    }
    return list;
}
```

其中通过`list = doQuery(ms, parameter, rowBounds, resultHandler, boundSql);`去进行查询

> ms: 当前增删改查标签的详细信息
>
> parameter：入参
>
> rowBounds：逻辑分页
>
> resultHandler：结果处理
>
> boundSql：sql语句详细信息

这个doQuery调用的就是SimpleExecutor的Query方法，如下所示

```java
@Override
public <E> List<E> doQuery(MappedStatement ms, Object parameter, RowBounds rowBounds, ResultHandler resultHandler,
                           BoundSql boundSql) throws SQLException {
    Statement stmt = null;
    try {
        // 拿到全局配置信息
        Configuration configuration = ms.getConfiguration();
        // 创建StatementHandler对象
        StatementHandler handler = configuration.newStatementHandler(wrapper, ms, parameter, rowBounds, resultHandler,
                                                                     boundSql);
        stmt = prepareStatement(handler, ms.getStatementLog());
        return handler.query(stmt, resultHandler);
    } finally {
        closeStatement(stmt);
    }
}
```

关于创建statementHandler对象newStatementHandler方法源码如下所示，其本质是调用了new RoutingStatementHandler构造函数进行创建，并调用interceptorChain方法将拦截器包装statementHandler对象

```java
public StatementHandler newStatementHandler(Executor executor, MappedStatement mappedStatement,
                                            Object parameterObject, RowBounds rowBounds, ResultHandler resultHandler, BoundSql boundSql) {
    StatementHandler statementHandler = new RoutingStatementHandler(executor, mappedStatement, parameterObject,
                                                                    rowBounds, resultHandler, boundSql);
    return (StatementHandler) interceptorChain.pluginAll(statementHandler);
}
```

RoutingStatementHandler构造函数方法如下所示，其会根据增删改查标签信息中的statementType进行创建不同的对象。

statementType在select等标签上可以进行设置

> STATEMENT：非预编译
>
> PREPARED: 原生PREPARED
>
> CALLABLE: 原生CALLABLE

![image-20250101171010707](Mybatis%E8%BF%90%E8%A1%8C%E5%8E%9F%E7%90%86.assets/image-20250101171010707.png)

```java
public RoutingStatementHandler(Executor executor, MappedStatement ms, Object parameter, RowBounds rowBounds,
                               ResultHandler resultHandler, BoundSql boundSql) {

    switch (ms.getStatementType()) {
        case STATEMENT:
            delegate = new SimpleStatementHandler(executor, ms, parameter, rowBounds, resultHandler, boundSql);
            break;
        case PREPARED:
            delegate = new PreparedStatementHandler(executor, ms, parameter, rowBounds, resultHandler, boundSql);
            break;
        case CALLABLE:
            delegate = new CallableStatementHandler(executor, ms, parameter, rowBounds, resultHandler, boundSql);
            break;
        default:
            throw new ExecutorException("Unknown statement type: " + ms.getStatementType());
    }

}
```

在创建PreparedStatementHandler对象，调用其构造函数的时候

```java
public PreparedStatementHandler(Executor executor, MappedStatement mappedStatement, Object parameter,
                                RowBounds rowBounds, ResultHandler resultHandler, BoundSql boundSql) {
    super(executor, mappedStatement, parameter, rowBounds, resultHandler, boundSql);
}
```

会将其他的处理器也设置进来

![image-20250101172024909](Mybatis%E8%BF%90%E8%A1%8C%E5%8E%9F%E7%90%86.assets/image-20250101172024909.png)



然后调用`stmt = prepareStatement(handler, ms.getStatementLog());`方法对Statement对象（PrepareStatement）进行预处理

```java
private Statement prepareStatement(StatementHandler handler, Log statementLog) throws SQLException {
    Statement stmt;
    Connection connection = getConnection(statementLog);
    stmt = handler.prepare(connection, transaction.getTimeout());
    handler.parameterize(stmt);
    return stmt;
}
```

其中通过parameterize方法对参数进行预编译，调用parameterHandler设置参数

```java
@Override
public void parameterize(Statement statement) throws SQLException {
    parameterHandler.setParameters((PreparedStatement) statement);
}
```

调用TypeHandler获取对应的jdbc参数类型，并为PreparedStatement设置参数（预编译参数）

![image-20250101172314243](Mybatis%E8%BF%90%E8%A1%8C%E5%8E%9F%E7%90%86.assets/image-20250101172314243.png)



最后调用StatementHandler对象的query方法

![image-20250101172432873](Mybatis%E8%BF%90%E8%A1%8C%E5%8E%9F%E7%90%86.assets/image-20250101172432873.png)

通过resultSetHandler对结果值进行处理

![image-20250101172539822](Mybatis%E8%BF%90%E8%A1%8C%E5%8E%9F%E7%90%86.assets/image-20250101172539822.png)





**整个流程图：**
