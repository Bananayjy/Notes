# Redis整理

## 一、资料

### 1、Redis数据持久化机制

- https://blog.csdn.net/m0_63947499/article/details/143026087

### 2、Redis数据类型详解

1.五种数据类型：字符串（String）、哈希（map）、列表（List）、集合（Set集合）、有序结合（Sorted Set 即Zset有序集合）

- [基础操作命令](https://blog.csdn.net/m0_63947499/article/details/142671224 )
- [基础操作&使用场景](https://blog.csdn.net/qq_50596778/article/details/124554777)

2.四种特殊的数据结构：位图（Bitmap）、HyperLogLog(基数统计),Geospatial(地理信息),Stream  

- https://blog.csdn.net/qq_73924465/article/details/141504379
- [位图详解](https://blog.csdn.net/weixin_47363690/article/details/142872279)

3.各个数据类型的底层实现

- https://blog.csdn.net/weixin_45690643/article/details/135890450



### 3、Redis单线程为什么快？（后面又切换成了多线程）



### 4、SpringBoot集成Redis

https://blog.csdn.net/qq_20236937/article/details/137561788







## 二、问题

### 1、redis开启事务方式

以为和java集成mysql的（java jdbc）一样，首先要声明语句，然后调用执行方法……

```java
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class NativeJdbcExample {

    public static void main(String[] args) {
        // 数据库连接的 URL, 用户名和密码
        String url = "jdbc:mysql://localhost:3306/yourdb";
        String user = "yourusername";
        String password = "yourpassword";

        // SQL 查询语句
        String sql = "SELECT COUNT(*) FROM users";

        try (Connection connection = DriverManager.getConnection(url, user, password);
             PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) {
                int count = rs.getInt(1);
                System.out.println("Total number of users: " + count);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

```

然后写出了如下代码

```java
@PostConstruct
public void init() {
    System.out.println(redisTemplate);
    redisTemplate.opsForValue().set("name", "张三");
    redisTemplate.exec();
}
```

其实调用set的时候就已经执行了，exec是用于redis的事务管理中的提交事务的，在执行exec，需要通过multi开启事务

```java
@PostConstruct
public void init() {
    System.out.println(redisTemplate);  // 打印 redisTemplate 以调试
    
    // 启动一个 Redis 事务
    redisTemplate.multi();  // 等同于 Redis 中的 "MULTI"
    
    // 在事务中排队命令
    redisTemplate.opsForValue().set("name", "张三");
    
    // 执行事务中的所有命令
    List<Object> execResult = redisTemplate.exec();  // 提交事务
    System.out.println(execResult);
}

```

同样我们也可以通过`RedisTransactionManager`配置的方法，配置事务

如果你不想手动管理 `MULTI` 和 `EXEC`，可以使用 Spring 提供的 `RedisTransactionManager` 来管理事务，它会帮你自动处理事务的生命周期。示例如下：

```java
@Configuration
public class RedisConfig {
    
    @Bean
    public RedisTransactionManager redisTransactionManager(RedisTemplate<String, String> redisTemplate) {
        return new RedisTransactionManager(redisTemplate.getConnectionFactory());
    }
}

@PostConstruct
public void init() {
    System.out.println(redisTemplate);  // 打印 redisTemplate 以调试
    
    // 使用 RedisTransactionManager 自动管理事务
    redisTemplate.opsForValue().set("name", "张三");
}

```





## 三、TRY

### 1、可以实践一下：通过Redis5.0的Stream实现消息队列(△)

关于List和Stream都可以实现消息队列的功能

> 使用Redis实现消息队列时，选择List和Stream的确有不同的优缺点。两者都能有效地处理消息队列，但具体的选择取决于你的应用场景和需求。以下是它们各自的特点：
>
> 1. **List**
>
> Redis的List是一个简单的双向链表结构，常用的操作有 `LPUSH`、`RPUSH`、`LPOP` 和 `RPOP`，可以用于实现消息队列。
>
> 优点：
>
> - **简单易用**：List的操作简单，符合传统的消息队列模型，可以通过 `LPUSH` 添加消息，`RPOP` 或 `LPOP` 消费消息，适用于简单的消息队列应用。
> - **支持多个消费者**：多个消费者可以同时从队列中弹出消息，适合并发处理。
> - **性能**：Redis的List性能较好，尤其是在队列操作上。
>
> 缺点：
>
> - **没有消息确认机制**：List本身不提供消息确认机制。如果消费者在处理消息时崩溃，消息会丢失。
> - **不支持消息持久化**：虽然Redis本身支持持久化（RDB和AOF），但List中的消息并没有明确的持久化机制，可能会在Redis重启后丢失。
> - **有限的功能**：List缺少消息队列管理的高级功能，如延迟队列、消息重试、消息排序等。
>
> 2. **Stream**
>
> Redis 5.0引入了Stream数据类型，专门用于处理消息流，提供更丰富的功能。
>
> 优点：
>
> - **消息确认机制**：Stream支持消费者确认（`XACK`），确保消息被成功处理，可以防止消息丢失。
> - **消息持久化**：Stream中的消息会被持久化，可以通过 `XREAD` 等命令来获取和消费消息。
> - **消费者组**：Stream支持消费者组（`XGROUP`），允许多个消费者协同工作，每个消费者只会处理未处理的消息，避免重复消费。
> - **更强大的功能**：Stream支持消息流的管理，能够存储更多的元数据，并且支持消息排序、流的剪裁等高级功能。
> - **支持消费历史消息**：Stream允许消费者读取指定时间点的消息，使得消息历史管理更加灵活。
>
> 缺点：
>
> - **复杂性较高**：Stream的操作相对复杂，尤其是在管理消费者组和消息确认方面，可能需要更多的开发和运维工作。
> - **性能开销**：与List相比，Stream的操作可能有更高的性能开销，因为它管理更多的元数据。
>
> 选择建议：
>
> - **使用List**：
>   - 如果你的需求比较简单，消息顺序很重要，但没有太多复杂的功能要求。
>   - 如果只需要一个简单的“生产者-消费者”模型，且消息确认和历史记录管理不是关键需求。
> - **使用Stream**：
>   - 如果需要更强大的消息管理功能，例如消息确认、消费者组、持久化、高可用等。
>   - 如果需要避免消息丢失，并确保每条消息被消费且不会重复消费。
>   - 如果需要处理更复杂的消息队列场景，比如大规模的消息流处理、消息重试、回溯读取历史消息等。
>
> 总体来说，如果你需要一个简单的队列，List可以满足大多数需求。如果你的需求更复杂，特别是涉及到消息确认、多个消费者和持久化等，Stream是一个更好的选择。



