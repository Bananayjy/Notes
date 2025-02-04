# MySQL 多版本并发控制（MVCC）

## 一、概要

### 1.1 MVCC

MVCC （Multiversion Concurrency Control），多版本并发控制。顾名思义，MVCC 是通过数据行的多个版本（通过undo log来体现）管理来实现数据库的 并发控制 。这项技术使得在InnoDB的事务隔离级别下执行 一致性读（即快照读） 操作有了保证。换言之，就是为了查询一些正在被另一个事务更新的行，并且可以看到它们被更新之前的值，这样在做查询的时候就不用等待另一个事务释放锁。（即不通过加锁的方式，保证事务的读时的隔离性）

MVCC 没有正式的标准，在不同的 DBMS（数据库管理系统） 中 MVCC 的实现方式可能是不同的，也不是普遍使用的(大家可以参考相关的 DBMS 文档)。这里讲解 InnoDB 中 MVCC 的实现机制(MySQL其它的存储引擎并不支持它)。

### 1.2 快照读与当前读

MVCC在MySQL InnoDB中的实现主要是为了提高数据库并发性能，用更好的方式去处理 读（使用快照读）-写（针对当前读取进行写）冲突 问题，做到即使有读写冲突时，也能做到 不加锁 ， 非阻塞并发读 ，而这个读指的就是 快照读 , 而非 当前读 。当前读实际上是一种加锁的操作，是悲观锁的实现（select的时候加X或S锁）。而MVCC本质是采用乐观锁思想的一种方式。

#### 1、快照读

快照读又叫一致性读，读取的是快照数据。**不加锁的简单的** **SELECT** **都属于快照读**，即不加锁的非阻塞

读；比如这样：

```mysql
SELECT * FROM table WHERE ...
```

之所以出现快照读的情况，是基于提高并发性能的考虑，快照读的实现是基于MVCC，它在很多情况下，避免了加锁操作，降低了开销。

既然是基于多版本，那么快照读可能读到的并不一定是数据的最新版本，而有可能是之前的历史版本。

快照读的前提是隔离级别不是串行级别，串行级别下的快照读会退化成当前读。



#### 2、当前读

当前读读取的是记录的最新版本（最新数据，而不是历史版本的数据），读取时还要保证其他并发事务不能修改当前记录，会对读取的记录进行加锁。加锁的 SELECT，或者对数据进行增删改都会进行当前读。比如：

```mysql
SELECT * FROM student LOCK IN SHARE MODE; # 共享锁
```

```mysql
SELECT * FROM student FOR UPDATE; # 排他锁
```

```mysql
INSERT INTO student values ... # 排他锁
```

```mysql
DELETE FROM student WHERE ...  # 排他锁
```

```mysql
UPDATE student SET ...  # 排他锁
```



### 1.3 前置知识

#### 1、隔离级别

事务的有4个隔离级别，可能会存在三种并发问题，如下所示：

![image-20241210221738514](MySQL%20%E5%A4%9A%E7%89%88%E6%9C%AC%E5%B9%B6%E5%8F%91%E6%8E%A7%E5%88%B6%EF%BC%88MVCC%EF%BC%89.assets/image-20241210221738514.png)

上述不是mysql的隔离界别，在mysql的可重复读中，同样解决了幻读的问题（通过MVCC，快照读的方式，每次读取的数据都是一个快照，不存在因为中介插入数据而导致幻读）

![image-20241210221827276](MySQL%20%E5%A4%9A%E7%89%88%E6%9C%AC%E5%B9%B6%E5%8F%91%E6%8E%A7%E5%88%B6%EF%BC%88MVCC%EF%BC%89.assets/image-20241210221827276.png)

在 MySQL 中，默认的隔离级别是可重复读，可以解决脏读和不可重复读的问题，如果仅从定义的角度来看，它并不能解决幻读问题。如果我们想要解决幻读问题，就需要采用串行化的方式，也就是将隔离级别提升到最高，但这样一来就会大幅降低数据库的事务并发能力。
MVCC 可以不采用锁机制，而是通过乐观锁的方式来解决不可重复读和幻读问题!它可以在大多数情况下替代行级锁，降低系统的开销。



## 二、MVCC实现原理

MVCC的实现依赖于：隐藏字段、Undo Log、 Read View

### 2.1 ReadView

#### 1、概念

在 MVCC 机制中，多个事务对同一个行记录进行更新会产生多个历史快照，这些历史快照保存在 Undo Log里。如果一个事务想要查询这个行记录，需要读取哪个版本的行记录呢?这时就需要用到 ReadView 了，它帮我们解决了行的可见性问题。
ReadView 就是事务A在使用MVCC机制进行快照读操作时产生的读视图。当事务启动时，会生成数据库系统当前的一个快照，InnoDB为每个事务构造了一个数组，用来记录并维护系统当前 活跃事务 的ID(“活跃”指的就是启动了但还没提交)。

####  2、 设计思路

在READ UNCOMMITTED隔离级别中，由于可以读到未提交事务修改过的记录，所以直接读取记录

的最新版本就好了。

SERIALIZABLE串行化 隔离级别的事务，InnoDB规定使用加锁的方式来访问记录。

使用 READ COMMITTED 和 REPEATABLE READ 隔离级别的事务，都必须保证读到 已经提交了的 事务修改过的记录。假如另一个事务已经修改了记录但是尚未提交，是不能直接读取最新版本的记录的，核心问题就是需要判断一下版本链中的哪个版本是当前事务可见的，这是ReadView要解决的主要问题。



#### 3、ReadView组成内容

- creator_trx_id 

创建这个 Read View 的事务 ID.

说明：只有在对表中的记录做改动时（执行INSERT、DELETE、UPDATE这些语句时）才会为

事务分配事务id，否则在一个只读事务中的事务id值都默认为0。

- trx_ids

表示在生成ReadView时当前系统中活跃的读写事务的 事务id列表 (已经启动，但还没有提交，即在一个事务启动的时候，就会将这些活跃的事务放到trx_ids中)

- up_limit_id

活跃的事务中最小的事务 ID（即trx_ids中最小的id）

- low_limit_id

表示生成ReadView时系统中应该分配给下一个事务的 id 值。low_limit_id 是系统最大的事务id值，这里要注意是系统中(不是up_limit_id中最大的事务id)的事务id，需要区别于正在活跃的事务ID。

> 注意：low_limit_id并不是trx_ids中的最大值，事务id是递增分配的。比如，现在有id为1，2，3这三个事务，之后id为3的事务提交了。那么一个新的读事务在生成ReadView时，trx_ids就包括1和2，up_limit_id的值就是1，low_limit_id的值就是4。系统中的事务id是递增去分配的

![image-20241211220938238](MySQL%20%E5%A4%9A%E7%89%88%E6%9C%AC%E5%B9%B6%E5%8F%91%E6%8E%A7%E5%88%B6%EF%BC%88MVCC%EF%BC%89.assets/image-20241211220938238.png)



#### 4、ReadView规则

有了这个ReadView，这样在访问某条记录时（Undo Log上的记录），只需要按照下边的步骤判断记录的某个版本是否可见。如下所示，一条条就爱那个Undo Log上的记录拿出来和ReadView进行“匹配”

![image-20241211222915719](MySQL%20%E5%A4%9A%E7%89%88%E6%9C%AC%E5%B9%B6%E5%8F%91%E6%8E%A7%E5%88%B6%EF%BC%88MVCC%EF%BC%89.assets/image-20241211222915719.png)

- 如果被访问版本的trx_id属性值与ReadView中的 creator_trx_id 值（创建这个 Read View 的事务 ID）相同，意味着当前事务在访问它自己修改过的记录，所以该版本可以被当前事务访问。

- 如果被访问版本的trx_id属性值小于ReadView中的 up_limit_id 值（最小值），表明生成该版本的事务在当前事务生成ReadView前已经提交，所以该版本可以被当前事务访问。

- 如果被访问版本的trx_id属性值大于或等于ReadView中的 low_limit_id 值，表明生成该版本的事务在当前事务生成ReadView后才开启，所以该版本不可以被当前事务访问。

- 如果被访问版本的trx_id属性值在ReadView的 up_limit_id 和 low_limit_id 之间，那就需要判断一下trx_id属性值是不是在 trx_ids 列表中。

  - 如果在，说明创建ReadView时生成该版本的事务还是活跃的，该版本不可以被访问。

  - 如果不在，说明创建ReadView时生成该版本的事务已经被提交，该版本可以被访问。

#### 5、MVCC操作流程

当查询一条记录的时候:

- 首先获取事务自己的版本号，也就是事务ID
- 获取 ReadView;
- 查询得到的数据，然后与 ReadView 中的事务版本号进行比较
- 如果不符合 ReadView 规则，就需要从 Undo Log 中获取历史快照;
- 最后返回符合规则的数据

如果某个版本的数据对当前事务不可见的话，那就顺着版本链找到下一个版本的数据，继续按照上边的步骤判断可见性，依此类推，直到版本链中的最后一个版本。如果最后一个版本也不可见的话，那么就意味着该条记录对该事务完全不可见，查询结果就不包含该记录。

> InnoDB 中，MVCC 是通过 Undo Log+ Read View 进行数据读取，Undo Log保存了历史快照 而 Read View规则帮我们判断当前版本的数据是否可见。

各个隔离级别下获取的ReadView：

- 在隔离级别为读已提交（Read Committed）时，一个事务中的每一次 SELECT 查询都会重新获取一次Read View。

![image-20241211223302750](MySQL%20%E5%A4%9A%E7%89%88%E6%9C%AC%E5%B9%B6%E5%8F%91%E6%8E%A7%E5%88%B6%EF%BC%88MVCC%EF%BC%89.assets/image-20241211223302750.png)

> 注意，此时同样的查询语句都会重新获取一次 Read View，这时如果 Read View 不同，就可能产生
>
> 不可重复读或者幻读的情况

- 当隔离级别为可重复读的时候，就避免了不可重复读，这是因为一个事务只在第一次 SELECT 的时候会获取一次 Read View，而后面所有的 SELECT 都会复用这个 Read View，如下表所示：

![image-20241211223349298](MySQL%20%E5%A4%9A%E7%89%88%E6%9C%AC%E5%B9%B6%E5%8F%91%E6%8E%A7%E5%88%B6%EF%BC%88MVCC%EF%BC%89.assets/image-20241211223349298.png)

### 2.2 隐藏字段、Undo Log版本链

回顾一下undo日志的版本链，对于使用InnoDB 存储引擎的表来说，它的聚簇索引记录中都包含两个必要的隐藏列。

- trx_id：每次一个事务对某条聚簇索引记录进行改动时，都会把该事务的事务id赋值给trx_id隐藏列。
- roll_pointer：每次对某条聚簇索引记录进行改动时，都会把旧的版本写入到undo日志 中，然后这个隐藏列就相当于一个指针，可以通过它来找到该记录修改前的信息。（与Undo Log版本链相关）

举例：student表数据如下所示

![image-20241210222222959](MySQL%20%E5%A4%9A%E7%89%88%E6%9C%AC%E5%B9%B6%E5%8F%91%E6%8E%A7%E5%88%B6%EF%BC%88MVCC%EF%BC%89.assets/image-20241210222222959.png)

假设插入该记录的事务id为8，那么此刻该记录的示意图如下所示

![image-20241210222259749](MySQL%20%E5%A4%9A%E7%89%88%E6%9C%AC%E5%B9%B6%E5%8F%91%E6%8E%A7%E5%88%B6%EF%BC%88MVCC%EF%BC%89.assets/image-20241210222259749.png)

> insert undo只在事务回滚时起作用，当事务提交后，该类型的undo日志就没用了，它占用的Undo 
>
> Log Segment也会被系统回收（也就是该undo日志占用的Undo页面链表要么被重用，要么被释
>
> 放）。

假设之后两个事务id分别为10、20的事务对这条记录进行UPDATE操作，操作流程如下

![image-20241210222349449](MySQL%20%E5%A4%9A%E7%89%88%E6%9C%AC%E5%B9%B6%E5%8F%91%E6%8E%A7%E5%88%B6%EF%BC%88MVCC%EF%BC%89.assets/image-20241210222349449.png)

> 能不能在两个事务中交叉更新同一条记录呢?不能!这不就是一个事务修改了另一个未提交事务修改过的数据，脏写。
> InnoDB使用锁来保证不会有脏写情况的发生，也就是在第一个事务更新了某条记录后，就会给这条记录加锁，另一个事务再次更新时就需要等待第一个事务提交了，把锁释放之后才可以继续更新。

每次对记录进行改动，都会记录一条undo日志，每条undo日志也都有一个 roll_pointer 属性

（ INSERT 操作对应的undo日志没有该属性，因为该记录并没有更早的版本），可以将这些 undo日志

都连起来，串成一个链表：

![image-20241210234456970](MySQL%20%E5%A4%9A%E7%89%88%E6%9C%AC%E5%B9%B6%E5%8F%91%E6%8E%A7%E5%88%B6%EF%BC%88MVCC%EF%BC%89.assets/image-20241210234456970.png)

对该记录每次更新后，都会将旧值放到一条 undo日志 中，就算是该记录的一个旧版本，随着更新次数的增多，所有的版本都会被 roll_pointer 属性连接成一个链表，我们把这个链表称之为 版本链 ，版本链的头节点就是当前记录最新的值。每个版本中还包含生成该版本时对应的 事务id 。



## 三、举例

假设现在student表中只有一条由事务id为8的事务插入的一条记录

![image-20241211235758829](MySQL%20%E5%A4%9A%E7%89%88%E6%9C%AC%E5%B9%B6%E5%8F%91%E6%8E%A7%E5%88%B6%EF%BC%88MVCC%EF%BC%89.assets/image-20241211235758829.png)

MVCC 只能在READ COMMITTED 和 REPEATABLE READ 两个隔离级别下工作。接下来看一下READ COMMITTED和REPEATABLE READ 所谓的生成ReadView的时机不同到底不同在哪里。

### 3.1 READ COMMITTED隔离级别下

RR：每次读取数据前都生成一个ReadView

![image-20241211235944961](MySQL%20%E5%A4%9A%E7%89%88%E6%9C%AC%E5%B9%B6%E5%8F%91%E6%8E%A7%E5%88%B6%EF%BC%88MVCC%EF%BC%89.assets/image-20241211235944961.png)

![image-20241212000015228](MySQL%20%E5%A4%9A%E7%89%88%E6%9C%AC%E5%B9%B6%E5%8F%91%E6%8E%A7%E5%88%B6%EF%BC%88MVCC%EF%BC%89.assets/image-20241212000015228.png)

![image-20241212000039817](MySQL%20%E5%A4%9A%E7%89%88%E6%9C%AC%E5%B9%B6%E5%8F%91%E6%8E%A7%E5%88%B6%EF%BC%88MVCC%EF%BC%89.assets/image-20241212000039817.png)

![image-20241212000051097](MySQL%20%E5%A4%9A%E7%89%88%E6%9C%AC%E5%B9%B6%E5%8F%91%E6%8E%A7%E5%88%B6%EF%BC%88MVCC%EF%BC%89.assets/image-20241212000051097.png)

![image-20241212000425309](MySQL%20%E5%A4%9A%E7%89%88%E6%9C%AC%E5%B9%B6%E5%8F%91%E6%8E%A7%E5%88%B6%EF%BC%88MVCC%EF%BC%89.assets/image-20241212000425309.png)

![image-20241212000439357](MySQL%20%E5%A4%9A%E7%89%88%E6%9C%AC%E5%B9%B6%E5%8F%91%E6%8E%A7%E5%88%B6%EF%BC%88MVCC%EF%BC%89.assets/image-20241212000439357.png)

![](MySQL%20%E5%A4%9A%E7%89%88%E6%9C%AC%E5%B9%B6%E5%8F%91%E6%8E%A7%E5%88%B6%EF%BC%88MVCC%EF%BC%89.assets/image-20241212000454957.png)

![image-20241212000533689](MySQL%20%E5%A4%9A%E7%89%88%E6%9C%AC%E5%B9%B6%E5%8F%91%E6%8E%A7%E5%88%B6%EF%BC%88MVCC%EF%BC%89.assets/image-20241212000533689.png)

![image-20241212000649624](MySQL%20%E5%A4%9A%E7%89%88%E6%9C%AC%E5%B9%B6%E5%8F%91%E6%8E%A7%E5%88%B6%EF%BC%88MVCC%EF%BC%89.assets/image-20241212000649624.png)

![image-20241212000727816](MySQL%20%E5%A4%9A%E7%89%88%E6%9C%AC%E5%B9%B6%E5%8F%91%E6%8E%A7%E5%88%B6%EF%BC%88MVCC%EF%BC%89.assets/image-20241212000727816.png)

### 3.2 REPEATABLE READ隔离级别下

![image-20241212002035830](MySQL%20%E5%A4%9A%E7%89%88%E6%9C%AC%E5%B9%B6%E5%8F%91%E6%8E%A7%E5%88%B6%EF%BC%88MVCC%EF%BC%89.assets/image-20241212002035830.png)

![image-20241212002100859](MySQL%20%E5%A4%9A%E7%89%88%E6%9C%AC%E5%B9%B6%E5%8F%91%E6%8E%A7%E5%88%B6%EF%BC%88MVCC%EF%BC%89.assets/image-20241212002100859.png)

![image-20241212002222952](MySQL%20%E5%A4%9A%E7%89%88%E6%9C%AC%E5%B9%B6%E5%8F%91%E6%8E%A7%E5%88%B6%EF%BC%88MVCC%EF%BC%89.assets/image-20241212002222952.png)

![image-20241212002211548](MySQL%20%E5%A4%9A%E7%89%88%E6%9C%AC%E5%B9%B6%E5%8F%91%E6%8E%A7%E5%88%B6%EF%BC%88MVCC%EF%BC%89.assets/image-20241212002211548.png)

![image-20241212002243544](MySQL%20%E5%A4%9A%E7%89%88%E6%9C%AC%E5%B9%B6%E5%8F%91%E6%8E%A7%E5%88%B6%EF%BC%88MVCC%EF%BC%89.assets/image-20241212002243544.png)

![image-20241212002313310](MySQL%20%E5%A4%9A%E7%89%88%E6%9C%AC%E5%B9%B6%E5%8F%91%E6%8E%A7%E5%88%B6%EF%BC%88MVCC%EF%BC%89.assets/image-20241212002313310.png)



![image-20241212002319124](MySQL%20%E5%A4%9A%E7%89%88%E6%9C%AC%E5%B9%B6%E5%8F%91%E6%8E%A7%E5%88%B6%EF%BC%88MVCC%EF%BC%89.assets/image-20241212002319124.png)

## 四、如何解决幻读

前提：MySQL在RR隔离级别下可以解决幻读问题

接下来说明InnoDB 是如何解决幻读的。

假设现在表 student 中只有一条数据，数据内容中，主键 id=1，隐藏的 trx_id=10，它的 undo log 如下图所示。

![image-20241212002501566](MySQL%20%E5%A4%9A%E7%89%88%E6%9C%AC%E5%B9%B6%E5%8F%91%E6%8E%A7%E5%88%B6%EF%BC%88MVCC%EF%BC%89.assets/image-20241212002501566.png)

假设现在有事务 A 和事务 B 并发执行， 事务 A 的事务 id 为 20 ， 事务 B 的事务 id 为 30 。

步骤1：事务 A 开始第一次查询数据，查询的 SQL 语句如下

```
select * from student where id >= 1;
```

在开始查询之前，MySQL 会为事务 A 产生一个 ReadView，此时 ReadView 的内容如下： trx_ids=

[20,30] ， up_limit_id=20 ， low_limit_id=31 ， creator_trx_id=20 。

由于此时表 student 中只有一条数据，且符合 where id>=1 条件，因此会查询出来。然后根据 ReadView机制，发现该行数据的trx_id=10，小于事务 A 的 ReadView 里 up_limit_id，这表示这条数据是事务 A 开启之前，其他事务就已经提交了的数据，因此事务 A 可以读取到。

结论：事务 A 的第一次查询，能读取到一条数据，id=1。

步骤2：接着事务 B(trx_id=30)，往表 student 中新插入两条数据，并提交事务

```
insert into student(id,name) values(2,'李四');
insert into student(id,name) values(3,'王五');
```

此时表student 中就有三条数据了，对应的 undo 如下图所示

![image-20241212002631962](MySQL%20%E5%A4%9A%E7%89%88%E6%9C%AC%E5%B9%B6%E5%8F%91%E6%8E%A7%E5%88%B6%EF%BC%88MVCC%EF%BC%89.assets/image-20241212002631962.png)

步骤3：接着事务 A 开启第二次查询，根据可重复读隔离级别的规则，此时事务 A 并不会再重新生成

ReadView。此时表 student 中的 3 条数据都满足 where id>=1 的条件，因此会先查出来。然后根据

ReadView 机制，判断每条数据是不是都可以被事务 A 看到。

1）首先 id=1 的这条数据，前面已经说过了，可以被事务 A 看到。

2）然后是 id=2 的数据，它的 trx_id=30，此时事务 A 发现，这个值处于 up_limit_id 和 low_limit_id 之

间，因此还需要再判断 30 是否处于 trx_ids 数组内。由于事务 A 的 trx_ids=[20,30]，因此在数组内，这表示 id=2 的这条数据是与事务 A 在同一时刻启动的其他事务提交的，所以这条数据不能让事务 A 看到。

3）同理，id=3 的这条数据，trx_id 也为 30，因此也不能被事务 A 看见。

![image-20241212002727716](MySQL%20%E5%A4%9A%E7%89%88%E6%9C%AC%E5%B9%B6%E5%8F%91%E6%8E%A7%E5%88%B6%EF%BC%88MVCC%EF%BC%89.assets/image-20241212002727716.png)

结论：最终事务 A 的第二次查询，只能查询出 id=1 的这条数据。这和事务 A 的第一次查询的结果是一样的，因此没有出现幻读现象，所以说在 MySQL 的可重复读隔离级别下，不存在幻读问题。



## 五、总结

![image-20241212002838362](MySQL%20%E5%A4%9A%E7%89%88%E6%9C%AC%E5%B9%B6%E5%8F%91%E6%8E%A7%E5%88%B6%EF%BC%88MVCC%EF%BC%89.assets/image-20241212002838362.png)

![image-20241212002844321](MySQL%20%E5%A4%9A%E7%89%88%E6%9C%AC%E5%B9%B6%E5%8F%91%E6%8E%A7%E5%88%B6%EF%BC%88MVCC%EF%BC%89.assets/image-20241212002844321.png)
