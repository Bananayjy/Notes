# 并发修改导致MVCC脏写问题

## 一、概要

### 1.1 业务场景

**数据库表结构设计：**一个主档数据，通过一个字段，逗号分隔的方式去关联其他明细信息的id。

如主档数据A，有3条明细数据与A关联，其id分别是1,2,3，那么其存储在关联字段的值为1,2,3。

**操作场景：** 

接口设计操作：①根据id查询主档数据 ②获取主档数据关联的明细id ③更新主档明细id

入参：主档id、明细id（这里不传列表，是因为相应参数需要返回相应的信息，因此就涉及成这样了）

在实际应用中，可能需要涉及到批量新增的操作，因此前端会出现某一时间点，同时调用多次该接口，导致MVCC脏写问题。

### 1.2 DEMO 初始化

MySQL中创建demo表

```mysql
# 建表
CREATE TABLE `test` (
`id` INT(11) NOT NULL COMMENT '主档id',
`ids` VARCHAR(255) DEFAULT NULL COMMENT '关联ids',
PRIMARY KEY (`id`)
) ENGINE=INNODB DEFAULT CHARSET=utf8;
```

建立相应的web三层架构：controller、service、mapper

controller

```java
@RestController
@RequestMapping("/demo")
public class democontroller {

    @Resource
    private demoService demoservice;

    @GetMapping("/test")
    public void test(Long id, String relId) {
        demoservice.test(id, relId);
    }

}
```

service

```java
public interface demoService {
    void test(Long id, String relId);
}
```

service-实现类

```java
@Slf4j
@Service
public class demoServiceImpl extends ServiceImpl<DemoMapper, Demo> implements demoService {

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void test(Long id, String relId) {
        // 获取主档对象
        Demo demo = getById(id);
        // 打印当前线程获取到的ids列表
        log.info("Thread Name:{}, before demo:{}",Thread.currentThread().getName(), demo);
        // 获取当前关联明细id
        List<String> idsList =  Arrays.stream(demo.getIds().split(","))
                .collect(Collectors.toList());
        // 添加新的元素（去重操作省略）
        idsList.add(relId);
        // 更新关联字段
        demo.setIds(idsList.stream().collect(Collectors.joining(",")));
        // 打印当前线程获取到的ids列表
        log.info("Thread Name:{}, after demo:{}",Thread.currentThread().getName(), demo);
        updateById(demo);
    }
}
```

mapper

```java
@Mapper
public interface DemoMapper extends BaseMapper<Demo> {
}
```

实体类

```java
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("test")
public class Demo extends Model<Demo> {

    @TableId(value = "id", type= IdType.ASSIGN_ID)
    private Integer id;

    private String ids;
    
}
```

### 1.3 问题复现

数据初始化

```
INSERT INTO `study_test`.`test`(`id`, `ids`) VALUES (1, '1');
```

![image-20241216224638876](%E5%B9%B6%E5%8F%91%E4%BF%AE%E6%94%B9%E5%AF%BC%E8%87%B4MVCC%E8%84%8F%E5%86%99%E9%97%AE%E9%A2%98.assets/image-20241216224638876.png)

通过apifox开三个线程并发调用“/demo”接口，入参如下所示：id固定为1，relId为一个整数值（1-999随机数）

![image-20241216231203890](%E5%B9%B6%E5%8F%91%E4%BF%AE%E6%94%B9%E5%AF%BC%E8%87%B4MVCC%E8%84%8F%E5%86%99%E9%97%AE%E9%A2%98.assets/image-20241216231203890.png)

调用结果如下所示

![image-20241216231250551](%E5%B9%B6%E5%8F%91%E4%BF%AE%E6%94%B9%E5%AF%BC%E8%87%B4MVCC%E8%84%8F%E5%86%99%E9%97%AE%E9%A2%98.assets/image-20241216231250551.png)

![image-20241216231259049](%E5%B9%B6%E5%8F%91%E4%BF%AE%E6%94%B9%E5%AF%BC%E8%87%B4MVCC%E8%84%8F%E5%86%99%E9%97%AE%E9%A2%98.assets/image-20241216231259049.png)

问题到此复现。



## 二、 原因分析

原因已经在标题中打出来了，即MVCC的脏写问题，当我们使用`@Transactional(rollbackFor = Exception.class)`注解后，Spring 会为当前事务设置 MySQL 会话的事务隔离级别为MySQL默认隔离级别即RR（可重复读）。

我们在打个断点后，通过apifox发起并发访问

![image-20241216234446630](%E5%B9%B6%E5%8F%91%E4%BF%AE%E6%94%B9%E5%AF%BC%E8%87%B4MVCC%E8%84%8F%E5%86%99%E9%97%AE%E9%A2%98.assets/image-20241216234446630.png)

在MySQL中通过`select trx_state, trx_started, trx_mysql_thread_id, trx_query, trx_isolation_level from information_schema.innodb_trx;`命令查看当前执行中且未提交的事务，可以看到此时有三条事务正在运行中，并且他们的隔离级别是可重复读（RR）

![image-20241216234510058](%E5%B9%B6%E5%8F%91%E4%BF%AE%E6%94%B9%E5%AF%BC%E8%87%B4MVCC%E8%84%8F%E5%86%99%E9%97%AE%E9%A2%98.assets/image-20241216234510058.png)

由于在RR隔离级别下，访问数据（即getById操作）访问的数据是通过MVCC实现的快照读，此时在并发访问的情况下，可能三个事务查询到的结果都是一样的，所以导致最后在更新的时候只出现了新增成功一个值。



## 三、解决

### 3.1 将隔离级别改成串行（无效）

一开始是这个想法，因为串行级别下都是会加锁的，因此通过改变`@Transactional`参数：`@Transactional(rollbackFor = Exception.class, isolation = Isolation.SERIALIZABLE)`将事物隔离级别改成串行也许可以解决问题。

当我们并发调用该接口后，发生报错，即在尝试加锁的时候，检测到了死锁

```
2024-12-16 23:59:48.615 ERROR 45800 --- [nio-6666-exec-3] o.a.c.c.C.[.[.[/].[dispatcherServlet]    : Servlet.service() for servlet [dispatcherServlet] in context with path [] threw exception [Request processing failed; nested exception is org.springframework.dao.DeadlockLoserDataAccessException: 
### Error updating database.  Cause: com.mysql.cj.jdbc.exceptions.MySQLTransactionRollbackException: Deadlock found when trying to get lock; try restarting transaction
### The error may exist in com/example/mapper/DemoMapper.java (best guess)
### The error may involve com.example.mapper.DemoMapper.updateById-Inline
### The error occurred while setting parameters
### SQL: UPDATE test  SET ids=?  WHERE id=?
### Cause: com.mysql.cj.jdbc.exceptions.MySQLTransactionRollbackException: Deadlock found when trying to get lock; try restarting transaction
; Deadlock found when trying to get lock; try restarting transaction; nested exception is com.mysql.cj.jdbc.exceptions.MySQLTransactionRollbackException: Deadlock found when trying to get lock; try restarting transaction] with root cause
```

打个断点，将其中两个事物停在查询查找后更新操作前，将其中一个操作停在更新操作后

![image-20241217002124838](%E5%B9%B6%E5%8F%91%E4%BF%AE%E6%94%B9%E5%AF%BC%E8%87%B4MVCC%E8%84%8F%E5%86%99%E9%97%AE%E9%A2%98.assets/image-20241217002124838.png)

通过命令`select trx_state, trx_started, trx_mysql_thread_id, trx_query, trx_isolation_level from information_schema.innodb_trx;`查看当前未提交的任务

可以看到有三个未提交事务其中一个在进行更新操作的时候发生了锁等待	

![image-20241217210354730](%E5%B9%B6%E5%8F%91%E4%BF%AE%E6%94%B9%E5%AF%BC%E8%87%B4MVCC%E8%84%8F%E5%86%99%E9%97%AE%E9%A2%98.assets/image-20241217210354730.png)

通过命令` SELECT * FROM INFORMATION_SCHEMA.INNODB_LOCKS;`查看一下当前加锁情况

结果就是两个事务给数据加了S锁，一个事务需要给更改记录加X锁，同时前两个事物在后面也需要给数据加X锁，造成了死锁。

![image-20241217210747936](%E5%B9%B6%E5%8F%91%E4%BF%AE%E6%94%B9%E5%AF%BC%E8%87%B4MVCC%E8%84%8F%E5%86%99%E9%97%AE%E9%A2%98.assets/image-20241217210747936.png)

注：关于多线程并发访问打断点需要进行如下设置

![image-20241217210643386](%E5%B9%B6%E5%8F%91%E4%BF%AE%E6%94%B9%E5%AF%BC%E8%87%B4MVCC%E8%84%8F%E5%86%99%E9%97%AE%E9%A2%98.assets/image-20241217210643386.png)



### 3.2 开批量新增接口（无效）

开批量新增接口，将需要关联的id通过一个接口传入，而不是同时调用三次接口分别传入来解决该问题，但是如果在并发场景下还是会出关联id丢失的问题。

### 3.3 乐观锁 (无效)

相关乐观锁实现可以参考mybatis-plus官网的文档：https://baomidou.com/plugins/optimistic-locker/。（乐观锁在我理解上就是做版本控制，每次更新操作都要对版本进行验证）

这里直接采用mybatis-plus提供的配置方式进行测试

配置文件

```java
@Configuration
public class MybatisPlusConfig {

    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        interceptor.addInnerInterceptor(new OptimisticLockerInnerInterceptor());
        return interceptor;
    }
}

```

实体类：

```java
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("test")
public class Demo extends Model<Demo> {

    @TableId(value = "id", type= IdType.ASSIGN_ID)
    private Integer id;

    private String ids;

    @Version
    private Integer version;

}
```

在数据表test中新增一个字段version，并设置其值为版本为2，再次并发调用接口，查看当前版本都为2

![image-20241217213206832](%E5%B9%B6%E5%8F%91%E4%BF%AE%E6%94%B9%E5%AF%BC%E8%87%B4MVCC%E8%84%8F%E5%86%99%E9%97%AE%E9%A2%98.assets/image-20241217213206832.png)

### 3.4 悲观锁（有效）

采用JUC、分布式锁等进行加锁，此时只有获取到锁的线程才可以对这一条数据进行操作，就不会出现并发问题。

修改代码，这里使用JUC下的ReentrantLock

```java
@Slf4j
@Service
public class demoServiceImpl extends ServiceImpl<DemoMapper, Demo> implements demoService {

    private Lock lock = new ReentrantLock();

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void test(Long id, String relId) {
        lock.lock();
        // 获取主档对象
        Demo demo = getById(id);
        // 打印当前线程获取到的ids列表
        log.info("Thread Name:{}, before demo:{}",Thread.currentThread().getName(), demo);
        // 获取当前关联明细id
        List<String> idsList =  Arrays.stream(demo.getIds().split(","))
                .collect(Collectors.toList());
        // 添加新的元素（去重操作省略）
        idsList.add(relId);
        // 更新关联字段
        demo.setIds(idsList.stream().collect(Collectors.joining(",")));
        // 打印当前线程获取到的ids列表
        log.info("Thread Name:{}, after demo:{}",Thread.currentThread().getName(), demo);
        updateById(demo);
        lock.unlock();
    }
}
```

结果成功

![image-20241217213530899](%E5%B9%B6%E5%8F%91%E4%BF%AE%E6%94%B9%E5%AF%BC%E8%87%B4MVCC%E8%84%8F%E5%86%99%E9%97%AE%E9%A2%98.assets/image-20241217213530899.png)

### 3.5 其他方法

如果有其他更好的方法，欢迎讨论！