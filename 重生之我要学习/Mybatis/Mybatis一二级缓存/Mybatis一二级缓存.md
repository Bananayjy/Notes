# Mybatis一二级缓存

## 一、说明

MyBatis 包含一个非常强大的查询缓存特性,它可以非常方便地配置和定制。缓存可以极大的提升查询效率。

MyBatis 系统中默认定义了两级缓存：一级缓存（本地缓存）和二级缓存（全局缓存）

- 默认情况下，只有一级缓存(SqlSession级别的缓存也称为本地缓存)开启
- 二级缓存需要手动开启和配置，是基于namespace级别(mapper级别)的缓存。
- 为了提高扩展性。MyBatis定义了缓存接口Cache。我们可以通过实现Cache接口来自定义二级缓存

### 1.1 原理图

![image-20250105172456797](Mybatis%E4%B8%80%E4%BA%8C%E7%BA%A7%E7%BC%93%E5%AD%98.assets/image-20250105172456797.png)

## 二、一级缓存（本地缓存）

### 2.1 说明

- 属于SqlSession级别的缓存（不同的SqlSession是不能共用缓存的），即作用域默认为sqlsession，并且一级缓存是一直开启的，本质上是SqlSession级别的一个Map
- 当Session flush 或 close后，该session中的所有Cache将被清空

- 与数据库同一次会话期间查询到的数据会放在本地缓存中，以后如果需要获取相同的数据，直接从缓存中拿，没必要再去查询数据库。

- 本地缓存不能被关闭，但可以调用clearCache方法清空本地缓存

- 在mybatis3.1之后，可以通过在mybatis.xml中配置本地缓存作用域localCacheScope

  ![image-20250105171525632](Mybatis%E4%B8%80%E4%BA%8C%E7%BA%A7%E7%BC%93%E5%AD%98.assets/image-20250105171525632.png)

触发条件:

- **相同的查询语句**：即 SQL 语句必须一致。
- **相同的参数**：查询的参数必须完全相同。
- **相同的 `SqlSession`**：必须在同一个 `SqlSession` 对象内。

缓存存储位置:

- 一级缓存的存储是基于 `SqlSession` 实例的。每个 `SqlSession` 对象都有一个独立的缓存，称为 **LocalCache**。
- **LocalCache** 是一个 `HashMap`，它存储了当前 `SqlSession` 查询的结果。缓存中的 key 是查询的 SQL 语句和查询参数的组合，value 是查询的结果（例如，查询的对象或数据列表）

### 2.2 一级缓存示例

demo测试代码

```java
// 一级缓存测试
@Test
public void testFirstLevelCache() throws IOException {
    // 1、获取sqlSessionFactory对象
    SqlSessionFactory sqlSessionFactory = getSqlSessionFactory();
    // 2、获取sqlSession对象（一个sqlSession对象代表和数据库的一次会话   ）
    SqlSession openSession = sqlSessionFactory.openSession();
    try {
        // 3、获取接口的实现类对象
        //会为接口自动的创建一个代理对象，代理对象去执行增删改查方法
        DemoMapper mapper = openSession.getMapper(DemoMapper.class);
        Demo demo1 = mapper.getById(1);
        System.out.println("demo1:" + demo1);

        // 再次查询
        Demo demo2 = mapper.getById(1);
        System.out.println("demo2:" + demo2);
        System.out.println(demo1 == demo2);
    } finally {
        openSession.close();
    }
}
```

运行结果如下所示，在同一个会话中，对于两次getById方法，只进行了一次sql的预编译、参数设置、执行操作，并且查出来的demo1和demo2对象是相同的，说明demo2并没有去获取新的数据，而是之前将demo1查询到的缓存数据拿来使用

![image-20250105153250786](Mybatis%E4%B8%80%E4%BA%8C%E7%BA%A7%E7%BC%93%E5%AD%98.assets/image-20250105153250786.png)



### 2.3 一级缓存失效情况

#### 1.不同的SqlSession（不同会话）

```java
// 一级缓存失效（不同SqlSession，即不同会话）
@Test
public void testFirstLevelCache2() throws IOException {
    SqlSessionFactory sqlSessionFactory = getSqlSessionFactory();
    SqlSession openSession = sqlSessionFactory.openSession();
    Demo demo1, demo2;
    try {
        DemoMapper mapper = openSession.getMapper(DemoMapper.class);
        demo1 = mapper.getById(1);
        System.out.println("demo1:" + demo1);
    } finally {
        openSession.close();
    }

    SqlSession openSession2 = sqlSessionFactory.openSession();

    try {
        DemoMapper mapper = openSession2.getMapper(DemoMapper.class);
        // 再次查询
        demo2 = mapper.getById(1);
        System.out.println("demo2:" + demo2);
    } finally {
        openSession2.close();
    }
    System.out.println(demo1 == demo2);
}
```

执行结果如下，可以看到进行了两次的数据库执行，并且demo1和demo2不是同一个对象，说明不同会话的一级缓存是不互通的，查询结果只会放到自己会话的缓存中

![image-20250105154309398](Mybatis%E4%B8%80%E4%BA%8C%E7%BA%A7%E7%BC%93%E5%AD%98.assets/image-20250105154309398.png)



#### 2.SqlSession相同，查询条件不同

```java
// 一级缓存失效,查询条件不同
@Test
public void testFirstLevelCache3() throws IOException {
    // 1、获取sqlSessionFactory对象
    SqlSessionFactory sqlSessionFactory = getSqlSessionFactory();
    // 2、获取sqlSession对象（一个sqlSession对象代表和数据库的一次会话   ）
    SqlSession openSession = sqlSessionFactory.openSession();
    try {
        // 3、获取接口的实现类对象
        //会为接口自动的创建一个代理对象，代理对象去执行增删改查方法
        DemoMapper mapper = openSession.getMapper(DemoMapper.class);
        Demo demo1 = mapper.getById(1);
        System.out.println("demo1:" + demo1);

        // 再次查询
        Demo demo2 = mapper.getById(2);
        System.out.println("demo2:" + demo2);
        System.out.println(demo1 == demo2);
    } finally {
        openSession.close();
    }
}
```

运行结果可以看到，会进行两场数据库查询，因为demo1查询后进行的缓存并不是demo2查询需要的（即一级缓存中还没有这个数据）

![image-20250105154515144](Mybatis%E4%B8%80%E4%BA%8C%E7%BA%A7%E7%BC%93%E5%AD%98.assets/image-20250105154515144.png)



#### 3. SqlSession相同，两次查询之间执行了增删改查

```java
// 一级缓存失效(SqlSession相同，两次查询之间执行了增删改查)
@Test
public void testFirstLevelCache4() throws IOException {
    // 1、获取sqlSessionFactory对象
    SqlSessionFactory sqlSessionFactory = getSqlSessionFactory();
    // 2、获取sqlSession对象（一个sqlSession对象代表和数据库的一次会话   ）
    SqlSession openSession = sqlSessionFactory.openSession();
    try {
        // 3、获取接口的实现类对象
        //会为接口自动的创建一个代理对象，代理对象去执行增删改查方法
        DemoMapper mapper = openSession.getMapper(DemoMapper.class);
        Demo demo1 = mapper.getById(1);
        System.out.println("demo1:" + demo1);

        // 新增操作
        Demo demo = new Demo();
        demo.setId(99);
        demo.setName("hahha");
        mapper.addDemo(demo);

        // 再次查询
        Demo demo2 = mapper.getById(1);
        System.out.println("demo2:" + demo2);
        System.out.println(demo1 == demo2);

    } finally {
        openSession.commit();
        openSession.close();
    }
}
```

运行结果如下，因为增删改可能对当前数据会有影响，所以肯定是不能再走缓存的

![image-20250105160118552](Mybatis%E4%B8%80%E4%BA%8C%E7%BA%A7%E7%BC%93%E5%AD%98.assets/image-20250105160118552.png)

#### 4. SqlSession相同，手动清除一级缓存

```java
// 一级缓存失效(SqlSession相同，手动清除一级缓存)
@Test
public void testFirstLevelCache5() throws IOException {
    // 1、获取sqlSessionFactory对象
    SqlSessionFactory sqlSessionFactory = getSqlSessionFactory();
    // 2、获取sqlSession对象（一个sqlSession对象代表和数据库的一次会话   ）
    SqlSession openSession = sqlSessionFactory.openSession();
    try {
        // 3、获取接口的实现类对象
        //会为接口自动的创建一个代理对象，代理对象去执行增删改查方法
        DemoMapper mapper = openSession.getMapper(DemoMapper.class);
        Demo demo1 = mapper.getById(1);
        System.out.println("demo1:" + demo1);

        // 手动清除一级缓存
        openSession.clearCache();

        // 再次查询
        Demo demo2 = mapper.getById(1);
        System.out.println("demo2:" + demo2);
        System.out.println(demo1 == demo2);

    } finally {
        openSession.close();
    }
}
```

运行结果

![image-20250105160517579](Mybatis%E4%B8%80%E4%BA%8C%E7%BA%A7%E7%BC%93%E5%AD%98.assets/image-20250105160517579.png)



## 三、二级缓存（全局缓存）

### 3.1 说明

对于一级缓存的范围还是比较小的，因为其是会话级别的范围，一旦会话更改，缓存信息也就不存在了。

- 二级缓存基于namespace级别（mybatis的增删改xml配置文件中）的缓存，一个namespace对应一个二级缓存。![image-20250105160900393](Mybatis%E4%B8%80%E4%BA%8C%E7%BA%A7%E7%BC%93%E5%AD%98.assets/image-20250105160900393.png)
- 一个会话，查询一条数据，这个数据就会被放到当前会话的一级缓存中，如果会话关掉，一级缓存中的数据会被保存到二级缓存中，新的会话查询信息，就可以去二级缓存中查看缓存信息，并且不同namespace查出的数据放在自己对应的缓存中（map）

### 3.2 使用

1.在mybatis的xml配置文件中开启全局二级缓存配置

![image-20250105161921871](Mybatis%E4%B8%80%E4%BA%8C%E7%BA%A7%E7%BC%93%E5%AD%98.assets/image-20250105161921871.png)

```xml
<?xml version="1.0" encoding="UTF-8" ?>
<!DOCTYPE configuration
 PUBLIC "-//mybatis.org//DTD Config 3.0//EN"
 "http://mybatis.org/dtd/mybatis-3-config.dtd">
<configuration>


	<settings>
		<setting name="cacheEnabled" value="true"/>
	</settings>

	<!-- plugins: 注册插件 -->
	<!--<plugins>
		<plugin interceptor="com.example.plugin.MyPlugin">
			<property name="username" value="root"/>
			<property name="password" value="12345"/>
		</plugin>
		<plugin interceptor="com.example.plugin.MyPlugin2">
		</plugin>
	</plugins>-->
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

2.在mapper.xml中配置使用二级缓存

![image-20250105162427863](Mybatis%E4%B8%80%E4%BA%8C%E7%BA%A7%E7%BC%93%E5%AD%98.assets/image-20250105162427863.png)

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN" "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="com.example.mapper.DemoMapper">
    <!--
    cache参数说明：
	eviction:缓存的回收策略（Cache接口有不同的实现类）：
		• LRU – 最近最少使用的：移除最长时间不被使用的对象。
		• FIFO – 先进先出：按对象进入缓存的顺序来移除它们。
		• SOFT – 软引用：移除基于垃圾回收器状态和软引用规则的对象。
		• WEAK – 弱引用：更积极地移除基于垃圾收集器状态和弱引用规则的对象。
		• 默认的是 LRU。
	flushInterval：缓存刷新间隔
		缓存多长时间清空一次，默认不清空，设置一个毫秒值
	readOnly:是否只读：
		true：只读；mybatis认为所有从缓存中获取数据的操作都是只读操作，不会修改数据。
				 mybatis为了加快获取速度，直接就会将数据在缓存中的引用交给用户。不安全，速度快
		false：非只读：mybatis觉得获取的数据可能会被修改。
				mybatis会利用序列化&反序列的技术克隆一份新的数据给你。安全，速度慢
	size：缓存存放多少元素；
	type=""：指定自定义缓存的全类名；
			实现Cache接口即可；
	-->
    <cache eviction="FIFO" flushInterval="60000" readOnly="false" size="1024"></cache>

    <insert id="addDemo" parameterType="com.example.model.entity.Demo">
        insert into demo(id,name)
        values(#{id},#{name})
    </insert>

    <select id="getById" resultType="com.example.model.entity.Demo">
        select id, name from demo where id = #{id}
    </select>

</mapper>

```

3.实体类/POJO需要实现序列化接口

```java
@Data
public class Demo implements Serializable {

    private static final long serialVersionUID = 1L;
    
    private Integer id;

    private String name;
}
```

4.测试

```java
// ================ 二级缓存测试  =====================
@Test
public void testSecondLevelCache() throws IOException {
    SqlSessionFactory sqlSessionFactory = getSqlSessionFactory();
    // 开启两个会话
    SqlSession openSession = sqlSessionFactory.openSession();
    SqlSession openSession2 = sqlSessionFactory.openSession();
    try {
        DemoMapper mapper = openSession.getMapper(DemoMapper.class);
        DemoMapper mapper2 = openSession2.getMapper(DemoMapper.class);

        Demo demo1 = mapper.getById(1);
        System.out.println("demo1:" + demo1);
        // 一定要把第一个会话关了（查询的数据会先放到一级缓存中，只有会话提交或关掉后才会放到二级缓存中）
        openSession.close();

        // 第二次查询是从二级缓存中拿到的数据，并没有发送新的sql
        Demo demo2 = mapper2.getById(1);
        System.out.println("demo2:" + demo2);
        openSession.close();

        System.out.println(demo1 == demo2);

    } finally {
        openSession.close();
    }
}
```

运行结果可以看到命中二级缓存

![image-20250105163408317](Mybatis%E4%B8%80%E4%BA%8C%E7%BA%A7%E7%BC%93%E5%AD%98.assets/image-20250105163408317.png)



> 其他内容：
>
> 1.关于readOnly，我们可以看到上面运行结果返回中demo1和demo2比较是不一样的，因为readOnly的值设置为了false，mybatis会利用序列化&反序列的技术克隆一份新的数据给你。当我们设置为true后，再次查询，demo1和demo2指向同一个引用地址
>
> ![image-20250105163949598](Mybatis%E4%B8%80%E4%BA%8C%E7%BA%A7%E7%BC%93%E5%AD%98.assets/image-20250105163949598.png)
>
> ![image-20250105164013392](Mybatis%E4%B8%80%E4%BA%8C%E7%BA%A7%E7%BC%93%E5%AD%98.assets/image-20250105164013392.png)
>
> 2.如果readOnly设置为false，即通过序列化返回，如果实体类没有实现序列化接口会报错
>
> ![image-20250105164124304](Mybatis%E4%B8%80%E4%BA%8C%E7%BA%A7%E7%BC%93%E5%AD%98.assets/image-20250105164124304.png)

### 3.3 和缓存相关的设置/属性

上面mapper.xml中cache标签中相关的配置已经说明，在这里不重复说明。

1、mybatis的配置中chacheEnabled：true开启缓存，false：关闭缓存（二级缓存关闭，一级缓存一直可用）

![image-20250105170141463](Mybatis%E4%B8%80%E4%BA%8C%E7%BA%A7%E7%BC%93%E5%AD%98.assets/image-20250105170141463.png)

2、每个select标签都有useCache属性

- true：使用缓存（chacheEnabled=true，就使用一二级缓存，反之只是用一级缓存）
- false：不使用缓存（一级缓存依然使用，二级缓存不使用）

3、每个增删改标签都有flushCache的属性，默认值为true

因为flushCache=true，增删改执行完成后会清除缓存（一级、二级都清除），对于查询标签的flushCache属性的默认值是false，如果设置查询标签的flushCache的值为ture，则每次查询之后都会清空缓存。

4.sqlSession的clearCache方法只清楚当前session的一级缓存

5.localCacheScope：用于配置本地缓存作用域，默认SESSION，即当前会话，如果配置为STATEMENT，可以看做是禁用一级缓存。



## 四、第三方缓存的整合

对于二级缓存，mybatis是通过Cache接口的各个实现类实现的，其中有各种各样的实现类，对应不同的配置类

![image-20250105174227824](Mybatis%E4%B8%80%E4%BA%8C%E7%BA%A7%E7%BC%93%E5%AD%98.assets/image-20250105174227824.png)

我们可以整合第三方的缓存对mybatis的二级缓存进行一个扩展，相关第三方实现可以在官方git地址https://github.com/mybatis/mybatis-3中获取

