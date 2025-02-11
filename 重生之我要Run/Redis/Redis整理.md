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

> 字符串：int（8个字节的长整型）、SDS
>
> 哈希：zipList、Hashtable(数组+列表)
>
> 列表：LinkedList+zipList（低版本）、quickList+zipList（高版本）
>
> 集合：
>
> 有序集合：

- https://blog.csdn.net/weixin_45690643/article/details/135890450

- 跳表详解：https://blog.csdn.net/sssxlxwbwz/article/details/123769262

### 3、Redis为什么快？

> 参考文章：
>
> - https://blog.csdn.net/qq_38196449/article/details/144583793
> - https://open8gu.com/redis/trending/utx7gm7khpfuq3h6/
> - https://www.cnblogs.com/coderacademy/p/18099027 (还没看)
> - https://juejin.cn/post/6978280894704386079 （还没看）

Redis 官方早前发布过一套基准测试，**在 Redis 服务连接数小于 1 万时，并发数量每秒可以达到 10-12 万左右**。连接数在 3-6 万 时，也能支持每秒 5-6 万的并发。

- **从存储方式上看**：Redis 是基于内存的数据库（当然也支持AOF和RDB的持久化操作），而直接访问内存的速度要比访问磁盘高上几个数量级。这是 Redis 快最主要的原因。

  > 磁盘内存横向对比数据的佐证：计算机访问一次 SSD 硬盘大概需要 50 ~ 150 微秒，如果是传统的硬盘时间则需要 1 ~ 10 毫秒，而访问一次内存仅需要 120 纳秒。可见，磁盘和内存访问的速度差了数个数量级。
  >
  > 注意：如果涉及到AOF和RDB持久化的，需要和磁盘打交道：除了少数一些需要需要跟磁盘打交道的时候（比如持久化），大部分时候 Redis 都只在内存中进行读写，因此它的效率极高。

- **从线程模型上看**：Redis 使用单线程模型，这意味着它的所有操作都是在一个线程内完成的，不需要进行线程切换和上下文切换（操作系统需要从一个线程切换到另一个线程，保存和恢复相应的状态）。这大大提高了 Redis 的运行效率和响应速度

  > 其核心思想是通过事件循环和 I/O 多路复用（如 `epoll` 或 `select`）来处理所有客户端的请求。由于每个请求都在同一个线程中顺序执行，避免了多线程模型中常见的线程切换、同步和上下文切换的开销，以及多线程带来的线程安全问题。**虽然 Redis 的单个主线程模型确实带来的不少的好处，但是这个设计更多的还是在性能与设计之间（即与硬件有密切关系）取得的一个平衡**。实际上不少市面上开源或者大公司内部自研的 KV 数据库 —— 比如 KeyDB 或者 Dragonfly —— 都是基于多线程模型实现的，它们以单机模式运行在多核机器上时也确实表现出了比 Redis 更高的性能，因为他们能更好地去利用CPU的多核进行操作。总的来说，Redis的单线程更加适用于**I/O 密集型**的任务，Redis 在 I/O 密集型任务（如简单的数据访问、缓存等）上表现很好，但如果应用涉及到复杂的计算或者需要大量并发的读写，单线程就会成为瓶颈。KeyDB 和 Dragonfly 通过多线程使得 CPU 密集型任务也能够在多个核上并行处理，从而提供更高的吞吐量和响应速度，更加适用CPU密集型任务。
  >
  > 注意：不过，Redis 也并非真的就是单线程的，从 4.0 开始，Redis 就引入了 `UNLINK` 这类命令，用于异步执行删除等重操作，并在 6.0 以后引入了专门的 IO 线程，实现了多线程的非阻塞式 IO，它们也进一步的提升了 Redis 的执行效率。

- **从IO模型上看：** Redis 在单线程的基础上，采用了 I/O 多路复用技术，实现了单个线程同时处理多个客户端连接的能力，从而提高了 Redis 的并发性能

  > 关于各个操作系统的I/O复用机制：
  >
  > `evport`（Solaris）、`epoll`（Linux）、和 `kQueue`（Mac OS/FreeBSD） 都是操作系统提供的高效 I/O 多路复用机制（非IO阻塞）。它们的主要作用是提供一种方式，使得程序可以高效地监视多个 I/O 流（例如，文件描述符、套接字等），并在有数据准备好或事件发生时进行处理。它们广泛用于需要处理大量并发 I/O 请求的高性能服务器或网络应用。
  >
  > I/O复用机制的目的：
  >
  > 1. **高效的 I/O 多路复用**： 这些机制的基本目的是让单一线程能够高效地监视多个 I/O 通道，而不需要为每个 I/O 通道创建单独的线程或进程。它们允许程序在某一时刻等待多个 I/O 操作的完成，从而在 I/O 阻塞时不浪费系统资源。
  > 2. **避免阻塞**： 在传统的阻塞 I/O 模型中，程序会在 I/O 操作完成之前一直等待，这样会浪费 CPU 资源。而这些高效 I/O 多路复用机制允许程序在等待 I/O 操作时可以去做其他任务，从而提高了应用程序的响应能力。
  >
  > 在这个模型中，它将会来自客户端的网络请求作为一个事件发布到队列中，然后线程将同步的获取事件并派发到不同的处理器，而处理器处理完毕后又会再发布另一个事件……整个主流程都由 Redis 的主线程在一个不间断的循环中完成，这就是事件循环。
  >
  > 熟悉 Netty 的同学可能会觉得有点既视感，因为两者都可以认为基于反应器模式实现的 IO 模型，不过 Netty 可以有多个事件循环，并且还可以划分为 Boss 和 Worker 两类事件循环组，而 Redis 只有一个事件循环，并且在早期版本只有一个 IO 线程（也就是主线程本身）。
  >
  > ![image-20250211154205991](Redis%E6%95%B4%E7%90%86.assets/image-20250211154205991.png)

- **从基本数据的数据结构上看：** Redis 提供了多种高效的数据结构，如哈希表、有序集合、列表等，这些数据结构都被实现得非常高效，能够在 O(1) 的时间复杂度内完成数据读写操作，这也是 Redis 能够快速处理数据请求的重要因素之一（数据结构要熟悉，参考2、Redis数据类型详解）

- **6.0版本多线程的引入：** 在 Redis 6.0 中，为了进一步提升 IO 的性能，引入了多线程的机制。采用多线程，使得网络处理的请求并发进行，就可以大大的提升性能。多线程除了可以减少由于网络 I/O 等待造成的影响，还可以充分利用 CPU 的多核优势。

  > 随着请求规模的扩大，单个线程在网络 IO 上消耗的 CPU 时间越来越多，它逐渐成为了 Redis 的性能瓶颈。因此在 6.0 版及以上版本，Redis 正式引入了多线程来处理网络 IO。
  >
  > 在新的版本中，Redis 依然使用单个主线程来执行命令，但是使用多个线程来处理 IO 请求，主线程不再负责包括建立连接、读取数据和回写数据这些事情，而只是专注于执行命令。
  >
  > ![image-20250211154346427](Redis%E6%95%B4%E7%90%86.assets/image-20250211154346427.png)
  >
  > 这个做法在保证单注线程设计的原有优点的情况下，又进一步提高了网络 IO 的处理效率。

### 4、为什么设计为单线程？

> 参考文章：https://blog.csdn.net/qq_38196449/article/details/144583793

Redis 作为一个成熟的分布式缓存框架，它由很多个模块组成，如网络请求模块、索引模块、存储模块、高可用集群支撑模块、数据操作模块等。

我们所说的 Redis 单线程，指的是“其网络 IO 和键值对读写是由一个线程完成的”，也就是说，Redis 中只有网络请求模块和数据操作模块是单线程的。而其他的如持久化存储模块、集群支撑模块等是多线程的。所以说，Redis 中并不是没有多线程模型的，早在 Redis 4.0 的时候就已经针对部分命令做了多线程化。

那么，为什么网络操作模块和数据存储模块最初并没有使用多线程呢？

一个计算机程序在执行的过程中，主要需要进行两种操作分别是读写操作（I/O操作，其中包括网络 I/O 和磁盘 I/O）和计算操作（设计CPU资源，消耗CPU资源）。

多线程的目的，就是通过并发的方式来提升 I/O 的利用率和 CPU 的利用率。那么，Redis 需不需要通过多线程的方式来提升提升 I/O 的利用率和 CPU 的利用率呢？我们可以肯定地说，Redis 不需要提升 CPU 利用率，因为 Redis 的操作基本都是基于内存的，CPU 资源根本就不是 Redis 的性能瓶颈，在 Redis 的场景下，由于数据存储在内存中，内存的访问速度比磁盘快得多，这意味着 CPU 在访问内存中的数据时非常迅速，且消耗较少的时间。因此，Redis 以非常高的速度处理内存中的数据，而不需要大量依赖 CPU 来进行计算或复杂的操作。因此，通过多线程技术来提升 Redis 的 CPU 利用率这一点是完全没必要的。Redis 确实是一个 I/O 操作密集的框架，他的数据操作过程中，会有大量的网络 I/O 和磁盘 I/O 的发生。要想提升 Redis 的性能，是一定要提升 Redis 的 I/O 利用率的，这一点毋庸置疑。但是，**提升 I/O 利用率，并不是只有采用多线程技术这一条路可以走！**
多线程的引入有很多弊端，如线程安全问题、线程切换、上下文切换带来的开销，会为语言和框架带来了更多的复杂性，所以，在提升 I/O 利用率这个方面上，Redis 并没有采用多线程技术，而是选择了多路复用 I/O 技术。

总结：

Redis 并没有在网络请求模块和数据操作模块中使用多线程模型，主要是基于以下四个原因：

- Redis 操作基于内存，绝大多数操作的性能瓶颈不在 CPU。

- 使用单线程模型，可维护性更高，开发、调试和维护的成本更低。
- 单线程模型，避免了线程间切换带来的性能开销。
- 在单线程中使用多路复用 I/O 技术也能提升 Redis 的 I/O 利用率。

还是要记住：Redis 并不是完全单线程的，只是有关键的键值对读写是由一个线程完成的。

### 5、为什么6.0之后又引入多线程？

https://blog.csdn.net/qq_38196449/article/details/141391514

### 6、SpringBoot集成Redis方式

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



### 2. 整理一下IO模型（△）
