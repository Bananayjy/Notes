## 十三、MyBatis初始化

### 一、前言

#### 1、MyBatis初始化概述

在 MyBatis 初始化过程中，首先会加载 `mybatis-config.xml` 配置文件、映射配置文件以及 Mapper 接口中的注解信息，解析后的配置信息会形成相应的对象并保存到 Configuration 对象中。利用该 Configuration 对象创建 SqlSessionFactory对象。MyBatis 初始化完成后，可以通过初始化得到 SqlSessionFactory 创建 SqlSession 对象并完成数据库操作。从本质上来说，是对原生JDBC相关接口的封装，完成对数据库的操作。

#### 2、相关模块

- `builder` 模块：为配置解析过程
- `mapping` 模块：主要为 SQL 操作解析后的**映射**

#### 3、流程概括





### 二、加载mybatis-config

#### 1、入口

MyBatis 的初始化流程的**入口**是 SqlSessionFactoryBuilder（位于org.apache.ibatis.session包下） 的 `#build(Reader reader, String environment, Properties properties)` 方法或`public SqlSessionFactory build(InputStream inputStream, String environment, Properties properties)`方法

```java
/**
 * MyBatis 的初始化流程的入口
 *
 * Builds {@link SqlSession} instances.
 *
 * @author Clinton Begin
 */
public class SqlSessionFactoryBuilder {

    /**
   * build的重载方法，最终调用的都{@link SqlSessionFactoryBuilder#build(Reader, String, Properties)}方法
   */
    public SqlSessionFactory build(Reader reader) {
        return build(reader, null, null);
    }

    public SqlSessionFactory build(Reader reader, String environment) {
        return build(reader, environment, null);
    }

    public SqlSessionFactory build(Reader reader, Properties properties) {
        return build(reader, null, properties);
    }

    /**
   * 构造 SqlSessionFactory 对象
   * @param reader Reader 对象
   * @param environment 环境信息
   * @param properties properties变量
   * @return SqlSessionFactory 工厂对象
   */
    public SqlSessionFactory build(Reader reader, String environment, Properties properties) {
        try {
            // 1.创建XMLConfigBuilder对象
            XMLConfigBuilder parser = new XMLConfigBuilder(reader, environment, properties);
            // 2.parse.parse():执行 XML 解析，返回 Configuration 对象
            // 3.build: 创建 DefaultSqlSessionFactory 对象
            return build(parser.parse());
        } catch (Exception e) { // 创建SqlSession异常，抛出相关异常信息
            throw ExceptionFactory.wrapException("Error building SqlSession.", e);
        } finally {
            // 重置ErrorContext对象
            ErrorContext.instance().reset();
            try {
                // 关闭readder对象
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException e) {
                // Intentionally ignore. Prefer previous error.
            }
        }
    }


    /**
   * build的重载方法，最终调用的都{@link SqlSessionFactoryBuilder#build(InputStream, String, Properties)}方法
   */
    public SqlSessionFactory build(InputStream inputStream) {
        return build(inputStream, null, null);
    }

    public SqlSessionFactory build(InputStream inputStream, String environment) {
        return build(inputStream, environment, null);
    }

    public SqlSessionFactory build(InputStream inputStream, Properties properties) {
        return build(inputStream, null, properties);
    }


    /**
   * 构造 SqlSessionFactory 对象，本质和 {@link SqlSessionFactoryBuilder#build(Reader, String, Properties)}方法一样
   * 只不过获取创建 XMLConfigBuilder 对象的时候使用的输入数据的类型不同，一个使用 Reader，另一个使用
   * - Reader 是 Java 中用于读取字符流的类，通常用于处理文本数据，如 XML 配置文件（UTF-8 编码等）
   * - InputStream 是 Java 中用于读取字节流的类，通常用于处理二进制数据，如从文件、网络或其他来源读取字节数据
   *
   * @param inputStream 输入流对象
   * @param environment 环境信息
   * @param properties properties变量
   * @return SqlSessionFactory 工厂对象
   */
    public SqlSessionFactory build(InputStream inputStream, String environment, Properties properties) {
        try {
            // 1.创建XMLConfigBuilder对象
            XMLConfigBuilder parser = new XMLConfigBuilder(inputStream, environment, properties);
            // 2.parse.parse():执行 XML 解析，返回 Configuration 对象
            // 3.build: 创建 DefaultSqlSessionFactory 对象
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


    /**
   * 创建DefaultSqlSessionFactory 对象
   * @param config Configuration 对象
   * @return DefaultSqlSessionFactory 对象
   */
    public SqlSessionFactory build(Configuration config) {
        return new DefaultSqlSessionFactory(config);
    }

}
```



#### 2、BaseBuilder

`org.apache.ibatis.builder.BaseBuilder`是基础构造器抽象类，为子类提供通用的工具类，除了XMLConfigBuilder类（实际在使用的）外，其还有很多实现类，其结构如下图所示：











### 三、加载Mapper映射配置文件

### 四、加载Statement配置

### 五、加载注解配置

