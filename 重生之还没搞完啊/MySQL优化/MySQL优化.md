# MySQL优化

## 一、优化过程分析

### 1.1 SQL优化过程图示

对于数据库调优的过程，可以参考如下图步骤进行分析：

![MySQL优化过程分析图.drawio](MySQL%E4%BC%98%E5%8C%96.assets/MySQL%E4%BC%98%E5%8C%96%E8%BF%87%E7%A8%8B%E5%88%86%E6%9E%90%E5%9B%BE.drawio.png)

对于优化成本和效果的关系图如下所示，硬件优化成本最高，并且优化效果也是最低的，对于SQL及索引的优化成本最低，并且其优化效果是最高的

![image-20241103171347400](MySQL%E4%BC%98%E5%8C%96.assets/image-20241103171347400.png)

### 1.2 其他优化策略

#### 1、目标

- 尽可能节省系统资源 ，以便系统可以提供更大负荷的服务。（吞吐量更大）

- 合理的结构设计和参数调整，以提高用户操作响应的速度 。（响应速度更快）
- 减少系统的瓶颈，提高MySQL数据库整体的性能。

#### 2、调优问题的定位

随着用户量的不断增加，以及应用程序复杂度的提升，我们很难用“更快”去定义数据库调优的目标，因为用户在不同时间段访问服务器遇到的瓶颈不同，比如双十一促销的时候会带来大规模的 并发访问;还有用户在进行不同业务操作的时候，数据库的事务处理 和 SQL查询 都会有所不同。因此我们还需要更加精细的定位，去确定调优的目标。具体方式如下：

- 用户反馈（主要）

用户是我们的服务对象，因此他们的反馈是最直接的。虽然他们不会直接提出技术建议，但是有些问题往往是用户第一时间发现的。我们要重视用户的反馈，找到和数据相关的问题。

- 日志分析（主要）

​	我们可以通过查看数据库日志和操作系统日志等方式找出异常情况，通过它们来定位遇到的问题。

- 服务器资源使用监控

通过监控服务器的 CPU、内存、I/O 等使用情况，可以实时了解服务器的性能使用，与历史情况进行对比。

- 数据库内部状况监控

在数据库的监控中，活动会话(Active Session)监控 是一个重要的指标。通过它，你可以清楚地了解数据库当前是否处于非常繁忙的状态，是否存在 SQL堆积等。

- 其他

除了活动会话监控以外，我们也可以对事务、 锁等待 等进行监控，这些都可以帮助我们对数据库的运行状态有更全面的认识。

#### 3、具体调优步骤

##### 第1步：选择合适的DBMS（Database Management System，数据库管理系统）

如果对 事务性处理 以及 安全性要求高 的话，可以选择商业的数据库产品。这些数据库在事务处理和查询性能上都比较强，比如采用 SQL Server、oracle，那么 单表存储上亿条数据 是没有问题的。如果数据表设计得好，即使不采用 分库分表 的方式，查询效率也不差。
除此以外，你也可以采用开源的 MySQL进行存储，它有很多存储引擎可以选择，如果进行事务处理的话可以选择InnoDB，非事务处理可以选择 MyISAM。
NoSOL 阵营包括 键值型数据库 、 文档型数据库、 搜索引擎、 列式存储和图形数据库。这些数据库的优缺点和使用场景各有不同，比如列式存储数据库可以大幅度降低系统的 IO，适合于分布式文件系统，但如果数据需要频繁
地增删改，那么列式存储就不太适用了
DBMS 的选择关系到了后面的整个设计过程，所以第一步就是要选择适合的 DBMS。如果已经确定好了 DBMS，那么这步可以跳过。



##### 第2步：优化表设计

选择了 DBMS 之后，我们就需要进行表设计了。而数据表的设计方式也直接影响了后续的 SQL查询语句。RDBMS（关系型数据库管理系统*）中，每个对象都可以定义为一张表，表与表之间的关系代表了对象之间的关系。如果用的是 MySQL，我们还可以根据不同表的使用需求，选择不同的存储引擎。除此以外，还有一些优化的原则可以参考:
1.表结构要尽量 遂循三范式的原则。这样可以让数据结构更加清晰规范，减少冗余字段，同时也减少了在更新，插入和删除数据时等异常情况的发生。
2.如果查询应用比较多，尤其是需要进行多表联査的时候，可以采用反范式进行优化。反范式采用空间换时间 的方式，通过增加冗余字段提高查询的效率。
3.表字段的数据类型 选择，关系到了查询效率的高低以及存储空间的大小。一般来说，如果字段可以采用数值类型就不要采用字符类型;字符长度要尽可能设计得短一些。针对字符类型来说，当确定字符长度固定时就可以采用 CHAR 类型:当长度不固定时，通常采用 VARCHAR 类型。
数据表的结构设计很基础，也很关键。好的表结构可以在业务发展和用户量增加的情况下依然发挥作用，不好的表结构设计会让数据表变得非常臃肿，查询效率也会降低。



##### 第3步：优化逻辑查询

当我们建立好数据表之后，就可以对数据表进行增删改查的操作了。这时我们首先需要考虑的是逻辑查询优化。SQL 查询优化，可以分为 **逻辑查询优化** 和 **物理査询优化**。

逻辑查询优化就是通过改变 SQL 语句的内容让 SQL 执效率更高效，采用的方式是对 SQL语句进行等价变换，对查询进行重写。
SQL 的查询重写包括了子查询优化、等价谓词重写、视图重写、条件简化、连接消除和嵌套连接消除等比如我们在讲解 EXISTS 子查询和 IN 子查询的时候，会根据 小表驱动大表 的原则选择适合的子查询。在 WHERE句中会尽量避免对字段进行函数运算，它们会让字段的索引失效。
举例:查询评论内容开头为 abc 的内容都有哪些，如果在 WHERE 子句中使用了函数，语句就会写成下面这样

```mysql
SELECT comment_id, comment_text, comment_time FROM product_comment WHERE SUBSTRING(comment_text, 1,3)='abc'
```

采用查询重写的方式进行等价替换(上述sql因为使用函数，无法走索引，下述可以走索引)：

```mysql
SELECT comment id, comment text, comment_time FROM product_comment WHERE comment _text LIKE 'abc%'
```



##### 第4步：优化物理查询

物理查询优化是在确定了逻辑查询优化之后，采用物理优化技术(比如索引等)，通过计算代价模型对各种可能的访问路径进行估算，从而找到执行方式中代价最小的作为执行计划。在这个部分中，我们需要掌握的重点是对索引的创建和使用。
但索引不是万能的，我们需要根据实际情况来创建索引。那么都有哪些情况需要考虑呢?我们在前面几章中已经进行了细致的剖析。
SQL 查询时需要对不同的数据表进行查询，因此在物理查询优化阶段也需要确定这些查询所采用的路径，具体的情况包括:

- 单表扫描:对于单表扫描来说，我们可以全表扫描所有的数据，也可以局部扫描。
- 两张表的连接:常用的连接方式包括了嵌套循环连接、HASH 连接和合并连接
- 多张表的连接:多张数据表进行连接的时候， 顺序 很重要，因为不同的连接路径査询的效率不同，搜索空间也会不同。我们在进行多表连接的时候，搜索空间可能会达到很高的数据量级，巨大的搜索空间显然会占用更多的资源，因此我们需要通过调整连接顺序，将搜索空间调整在一个可接受的范围内。



##### 第5步：使用Redis或Memcached作为缓存

除了可以对 SQL本身进行优化以外，我们还可以请外援提升查询的效率。
因为数据都是存放到数据库中，我们需要从数据库层中取出数据放到内存中进行业务逻辑的操作，当用户量增大的时候，如果频繁地进行数据查询，会消耗数据库的很多资源。如果我们将常用的数据直接放到内存中，就会大幅提升查询的效率。
键值存储数据库可以帮我们解决这个问题。
常用的键值存储数据库有 Redis 和 Memcached，它们都可以将数据存放到内存中。
从可靠性来说， Redis 支持持久化，可以让我们的数据保存在硬盘上，不过这样一来性能消耗也会比较大。而Memcached 仅仅是内存存储，不支持持久化。
从支持的数据类型来说，Redis 比 Memcached 要多，它不仅支持 key-value 类型的数据，还支持 List，Set，Hasf等数据结构。 当我们有持久化需求或者是更高级的数据处理需求的时候，就可以使用 Redis。如果是简单的 keyvalue 存储，则可以使用 Memcached。
通常我们对于査询响应要求高的场景(响应时间短，吞吐量大)，可以考虑内存数据库，毕竟术业有专攻。传统的 RDBMS 都是将数据存储在硬盘上，而内存数据库则存放在内存中，查询起来要快得多。不过使用不同的工具，也增加了开发人员的使用成本。



##### 第6步：库级优化

库级优化是站在数据库的维度上进行的优化策略，比如控制一个库中的数据表数量。另外，单一的数据库总会遇到各种限制，不如取长补短，利用"外援"的方式。通过 主从架构 优化我们的读写策略，通过对数据库进行垂直或者水平切分，突破单一数据库或数据表的访问限制，提升查询的性能。

①读写分离

如果读和写的业务量都很大，并且它们都在同一个数据库服务器中进行操作，那么数据库的性能就会出现瓶颈,这时为了提升系统的性能，优化用户体验，我们可以采用 读写分离的方式降低主数据库的负载，比如用主数据库(master)完成写操作，用从数据库(slave)完成读操作。

![image-20241117205238806](MySQL%E4%BC%98%E5%8C%96.assets/image-20241117205238806.png)

![image-20241117205243626](MySQL%E4%BC%98%E5%8C%96.assets/image-20241117205243626.png)



②数据分片

对 数据库分库分表。当数据量级达到千万级以上时，有时候我们需要把一个数据库切成多份，放到不同的数据库服务器上，减少对单一数据库服务器的访问压力。如果你使用的是 MVSOL，就可以使用 MVSOL 自带的分区表功能，当然你也可以考虑自己做 垂直拆分(分库)、水平拆分(分表)、垂直+水平拆分(分库分表)。

![image-20241117205719544](MySQL%E4%BC%98%E5%8C%96.assets/image-20241117205719544.png)

![image-20241117205724090](MySQL%E4%BC%98%E5%8C%96.assets/image-20241117205724090.png)

注意：但需要注意的是，分拆在提升数据库性能的同时，也会增加维护和使用成本。



#### 4、优化MySQL服务器

优化MySQL服务器主要从两个方面来优化，一方面是对 硬件 进行优化;另一方面是对MySQL 服务的参数 进行优化。这部分的内容需要较全面的知识，一般只有专业的数据库管理员 才能进行这一类的优化。对于可以定制参数的操作系统，也可以针对MySQL进行操作系统优化。

##### （1）优化服务器硬件

服务器的硬件性能直接决定着MySQL数据库的性能。硬件的性能瓶颈直接决定MySQL数据库的运行速度和效率。针对性能瓶颈提高硬件配置，可以提高MySQL数据库查询、更新的速度。

(1)配置较大的内存。足够大的内存是提高MySQL数据库性能的方法之一。内存的速度比磁盘IO快得多，可以通过增加系统的 缓冲区容量 使数据在内存中停留的时间更长，以 减少磁盘IO。

(2)配置高速磁盘系统，以减少读盘的等待时间，提高响应速度。磁盘的IO能力，也就是它的寻道能力，目前的 SCSI 高速旋转的是7200转/分钟，这样的速度，一旦访问的用户量上去，磁盘的压力就会过大，如果是每天的网站pv(page view)在150w，这样的一般的配置就无法满足这样的需求了。现在SSD盛行，在SSD上随机访问和顺序访问性能几乎差不多，使用SSD可以减少随机IO带来的性能损耗。

(3)合理分布磁盘IO，把磁盘IO分散在多个设备上，以减少资源竞争，提高并行操作能力。

(4)配置多处理器，MySQL是多线程的数据库，多处理器可同时执行多个线程。



##### （2）优化MySQL参数

- innodb_buffer_pool_size ：这个参数是Mysql数据库最重要的参数之一，表示InnoDB类型的 表

和索引的最大缓存 。它不仅仅缓存 索引数据 ，还会缓存 表的数据 。这个值越大，查询的速度就会越

快。但是这个值太大会影响操作系统的性能。

- key_buffer_size ：表示 索引缓冲区的大小 。索引缓冲区是所有的 线程共享 。增加索引缓冲区可

以得到更好处理的索引（对所有读和多重写）。当然，这个值不是越大越好，它的大小取决于内存

的大小。如果这个值太大，就会导致操作系统频繁换页，也会降低系统性能。对于内存在 4GB 左右

的服务器该参数可设置为 256M 或 384M 。

- table_cache ：表示 同时打开的表的个数 。这个值越大，能够同时打开的表的个数越多。物理内

  存越大，设置就越大。默认为2402，调到512-1024最佳。这个值不是越大越好，因为同时打开的表

  太多会影响操作系统的性能。

- query_cache_size ：表示 查询缓冲区的大小 。可以通过在MySQL控制台观察，如果

  Qcache_lowmem_prunes的值非常大，则表明经常出现缓冲不够的情况，就要增加Query_cache_size

  的值；如果Qcache_hits的值非常大，则表明查询缓冲使用非常频繁，如果该值较小反而会影响效

  率，那么可以考虑不用查询缓存；Qcache_free_blocks，如果该值非常大，则表明缓冲区中碎片很

  多。MySQL8.0之后失效。该参数需要和query_cache_type配合使用。

- query_cache_type 的值是0时，所有的查询都不使用查询缓存区。但是query_cache_type=0并不会导致MySQL释放query_cache_size所配置的缓存区内存。

  - 当query_cache_type=1时，所有的查询都将使用查询缓存区，除非在查询语句中指定SQL_NO_CACHE ，如SELECT SQL_NO_CACHE * FROM tbl_name。
  - 当query_cache_type=2时，只有在查询语句中使用 SQL_CACHE 关键字，查询才会使用查询缓存区。使用查询缓存区可以提高查询的速度，这种方式只适用于修改操作少且经常执行相同的查询操作的情况。

- sort_buffer_size ：表示每个 需要进行排序的线程分配的缓冲区的大小 。增加这个参数的值可以

  提高 ORDER BY 或 GROUP BY 操作的速度。默认数值是2 097 144字节（约2MB）。对于内存在4GB

  左右的服务器推荐设置为6-8M，如果有100个连接，那么实际分配的总共排序缓冲区大小为100 × 6

  ＝ 600MB。

- join_buffer_size = 8M ：表示 联合查询操作所能使用的缓冲区大小 ，和sort_buffer_size一样，

  该参数对应的分配内存也是每个连接独享。

- read_buffer_size ：表示 每个线程连续扫描时为扫描的每个表分配的缓冲区的大小（字节） 。当线

  程从表中连续读取记录时需要用到这个缓冲区。SET SESSION read_buffer_size=n可以临时设置该参

  数的值。默认为64K，可以设置为4M。

- innodb_flush_log_at_trx_commit ：表示 何时将缓冲区的数据写入日志文件 ，并且将日志文件

  写入磁盘中。该参数对于innoDB引擎非常重要。该参数有3个值，分别为0、1和2。该参数的默认值

  为1。

  值为 0 时，表示 每秒1次 的频率将数据写入日志文件并将日志文件写入磁盘。每个事务的

  commit并不会触发前面的任何操作。该模式速度最快，但不太安全，mysqld进程的崩溃会导

  致上一秒钟所有事务数据的丢失。

  值为 1 时，表示 每次提交事务时 将数据写入日志文件并将日志文件写入磁盘进行同步。该模

  式是最安全的，但也是最慢的一种方式。因为每次事务提交或事务外的指令都需要把日志写入

  （flush）硬盘。

  值为 2 时，表示 每次提交事务时 将数据写入日志文件， 每隔1秒 将日志文件写入磁盘。该模

  式速度较快，也比0安全，只有在操作系统崩溃或者系统断电的情况下，上一秒钟所有事务数

  据才可能丢失。

- innodb_log_buffer_size ：这是 InnoDB 存储引擎的 事务日志所使用的缓冲区 。为了提高性能，

  也是先将信息写入 Innodb Log Buffer 中，当满足 innodb_flush_log_trx_commit 参数所设置的相应条

  件（或者日志缓冲区写满）之后，才会将日志写到文件（或者同步到磁盘）中。

- max_connections ：表示 允许连接到MySQL数据库的最大数量 ，默认值是 151 。如果状态变量

  connection_errors_max_connections 不为零，并且一直增长，则说明不断有连接请求因数据库连接

  数已达到允许最大值而失败，这是可以考虑增大max_connections 的值。在Linux 平台下，性能好的

  服务器，支持 500-1000 个连接不是难事，需要根据服务器性能进行评估设定。这个连接数 不是越大

  越好 ，因为这些连接会浪费内存的资源。过多的连接可能会导致MySQL服务器僵死。

- back_log ：用于 控制MySQL监听TCP端口时设置的积压请求栈大小 。如果MySql的连接数达到

  max_connections时，新来的请求将会被存在堆栈中，以等待某一连接释放资源，该堆栈的数量即

  back_log，如果等待连接的数量超过back_log，将不被授予连接资源，将会报错。5.6.6 版本之前默

  认值为 50 ， 之后的版本默认为 50 + （max_connections / 5）， 对于Linux系统推荐设置为小于512

  的整数，但最大不超过900。如果需要数据库在较短的时间内处理大量连接请求， 可以考虑适当增大back_log 的值。

- thread_cache_size ： 线程池缓存线程数量的大小 ，当客户端断开连接后将当前线程缓存起来，

  当在接到新的连接请求时快速响应无需创建新的线程 。这尤其对那些使用短连接的应用程序来说可

  以极大的提高创建连接的效率。那么为了提高性能可以增大该参数的值。默认为60，可以设置为

  120。可以通过如下几个MySQL状态值来适当调整线程池的大小：

  ![image-20241117212058105](MySQL%E4%BC%98%E5%8C%96.assets/image-20241117212058105.png)

​			当 Threads_cached 越来越少，但 Threads_connected 始终不降，且 Threads_created 持续升高，可适当增加 thread_cache_size 的大小。

- wait_timeout ：指定 一个请求的最大连接时间 ，对于4GB左右内存的服务器可以设置为5-10
- interactive_timeout ：表示服务器在关闭连接前等待行动的秒数



**关于mysql配置文件my.cnf的参考配置**

```cnf
[mysqld]
port = 3306 serverid = 1 socket = /tmp/mysql.sock skip-locking #避免MySQL的外部锁定，减少
出错几率增强稳定性。 skip-name-resolve #禁止MySQL对外部连接进行DNS解析，使用这一选
项可以消除MySQL进行DNS解析的时间。但需要注意，如果开启该选项，则所有远程主机连接授权
都要使用IP地址方式，否则MySQL将无法正常处理连接请求！ back_log = 384
key_buffer_size = 256M max_allowed_packet = 4M thread_stack = 256K
table_cache = 128K sort_buffer_size = 6M read_buffer_size = 4M
read_rnd_buffer_size=16M join_buffer_size = 8M myisam_sort_buffer_size =
64M table_cache = 512 thread_cache_size = 64 query_cache_size = 64M
tmp_table_size = 256M max_connections = 768 max_connect_errors = 10000000
wait_timeout = 10 thread_concurrency = 8 #该参数取值为服务器逻辑CPU数量*2，在本
例中，服务器有2颗物理CPU，而每颗物理CPU又支持H.T超线程，所以实际取值为4*2=8 skipnetworking #开启该选项可以彻底关闭MySQL的TCP/IP连接方式，如果WEB服务器是以远程连接
的方式访问MySQL数据库服务器则不要开启该选项！否则将无法正常连接！ table_cache=1024
innodb_additional_mem_pool_size=4M #默认为2M innodb_flush_log_at_trx_commit=1
innodb_log_buffer_size=2M #默认为1M innodb_thread_concurrency=8 #你的服务器CPU
有几个就设置为几。建议用默认一般为8 tmp_table_size=64M #默认为16M，调到64-256最挂
thread_cache_size=120 query_cache_size=32M
```



**具体通过SQL参数优化举例：**

下面是一个电商平台，类似京东或天猫这样的平台。商家购买服务，入住平台，开通之后，商家可以在系统中上架各种商品，客户通过手机 App、微信小程序等渠道购买商品，商家接到订单以后安排快递送货。
刚刚上线 的时候，系统运行状态良好。但是，随着入住的 商家不断增多 ，使用系统的 用户量越来越多，每天的订单数据达到了5万条以上。这个时候，系统开始出现问题，CPU 使用率不断飙升。终于，双十一或者618活动高峰的时候，CPU 使用率达到 99%，这实际上就意味着，系统的计算资源已经耗尽，再也无法处理任何新的订单了。换句话说，系统已经崩溃了，
这个时候，我们想到了对系统参数进行调整，因为参数的值决定了资源配置的方式和投放的程度。

为了解决这个问题，一共调整3个系统参数，分别是

- InnoDB_flush_log_at_trx_commit

这个参数适用于 InnoDB 存储引擎，电商平台系统中的表用的存储引擎都是 InnoDB。默认的值是 1，意思是每次提交事务的时候，都把数据写入日志，并把日志写入磁盘。这样做的好处是数据 安全性最佳，不足之处在于每次提交事务，都要进行磁盘写入的操作。在大并发的场景下，过于频繁的磁盘读写会导致 CPU 资源浪费，系统效率变低。
这个参数的值还有2个可能的选项，分别是0和 2。我们把这个参数的值改成了 2。这样就不用每次提交事务的时候都启动磁盘读写了，在大并发的场景下，可以改善系统效率，降低 CPU 使用率。即便出现故障，损失的数据也比较小。

- InnoDB_buffer_pool_size

这个参数的意思是，InnoDB存储引擎使用 缓存来存储索引和数据。这个值越大，可以加载到缓存区的索引和数据量就越多，需要的 磁盘读写就越少。
因为我们的 MySQL 服务器是数据库 专属服务器 ，只用来运行 MySQL数据库服务，没有其他应用了，而我们的计算机是 64 位机器，内存也有 128G。于是我们把这个参数的值调整为 64G。这样一来，磁盘读写次数可以大幅降低，我们就可以充分利用内存，释放出一些 CPU 的资源。

- InnoDB_bufferpool_instances

这个参数可以将 InnoDB 的缓存区分成几个部分，这样可以提高系统的 并行处理能力，因为可以允许多个进程同时处理不同部分的缓存区。
我们把, InnoDB_buffer_pool_instances 的值修改为 64，意思就是把 InnoDB 的缓存区分成 64 个分区，这样就可以同时有 多个进程 进行数据操作，CPU 的效率就高多了。修改好了系统参数的值，要重启 MySQL 数据库服务器。

总结一下就是遇到 CPU 资源不足的问题，可以从下面2个思路去解决。

- 疏通拥堵路段，消除瓶颈，让等待的时间更短(InnoDB_flush_log_at_trx_commit、InnoDB_buffer_pool_size)
- 开拓新的通道，增加并行处理能力（InnoDB_bufferpool_instances）

#### 5、优化数据库结构

一个好的 数据库设计方案 对于数据库的性能常常会起到 事半功倍 的效果。合理的数据库结构不仅可以使数据库占用更小的磁盘空间，而且能够使査询速度更快。数据库结构的设计需要考虑 数据冗余 、 査询和更新的速度 、 字段的数据类型 是否合理等多方面的内容。

##### (1) 拆分表：冷热数据分离

拆分表的思路是，把1个包含很多字段的表拆分成2个或者多个相对较小的表。这样做的原因是，这些表中某些字段的操作频率很高(热数据)，经常要进行査询或者更新操作，而另外一些字段的使用频率却很低(冷数据)，冷热数据分离，可以减小表的宽度。如果放在一个表里面，每次查询都要读取大记录，会消耗较多的资源。
MvSOL限制每个表最多存储 4896列，并且每一行数据的大小不能超过 65535字节。表越宽，把表装载进内存缓冲池时所占用的内存也就越大，也会消耗更多的IO。 冷热数据分离的目的 是:① 减少磁盘IO，保证热数据的内存缓存命中率。② 更有效的利用缓存，避免读入无用的冷数据。

**举例1:**会员members表 存储会员登录认证信息，该表中有很多字段，如id、姓名、密码、地址、电话、个人描述字段。其中地址、电话、个人描述等字段并不常用，可以将这些不常用的字段分解出另一个表。将这个表取名叫members_detail，表中有member_id、address、telephone、description等字段。这样就把会员表分成了两个表分别为members表和members_detail表。

##### (2) 增加中间表

对于需要经常联合查询的表，可以建立中间表以提高查询效率。通过建立中间表，把需要经常联合查询的数据插入中间表中，然后将原来的联合查询改为对中间表的查询，以此来提高查询效率
首先，分析经常联合查询表中的字段;然后，使用这些字段建立一个中间表，并将原来联合查询的表的数据插入中间表中;最后，使用中间表来进行查询。

注意：如果表中数据修改了，会导致中间表的数据不一致问题该如何解决？①清空数据，重新添加数据 ②视图

##### (3) 增加冗余字段

设计数据库表时应尽量遵循范式理论的规约，尽可能减少冗余字段，让数据库设计看起来精致、优雅。但是，合理地加入冗余字段可以提高查询速度。
表的规范化程度越高，表与表之间的关系就越多，需要连接査询的情况也就越多。尤其在数据量大，而且需要频繁进行连接的时候，为了提升效率，我们也可以考虑增加几余字段来减少连接。
这部分内容在《第11章 数据库的设计规范》章节中 反范式化小节 中具体展开讲解了。这里省略。

##### (4) 优化数据类型

改进表的设计时，可以考虑优化字段的数据类型。这个问题在大家刚从事开发时基本不算是问题。但是，随着你的经验越来越丰富，参与的项目越来越大，数据量也越来越多的时候，你就不能只从系统稳定性的角度来思考问题了，还要考虑到系统整体的稳定性和效率。此时，优先选择符合存储需要的最小的数据类型。
列的 字段越大 ，建立索引时所需要的 空间也就越大，这样一页中所能存储的索引节点的 数量也就越少，不在遍历时所需要的 IO次数也就越多， 索引的性能也就越差。

具体来说：

情况1：对整数类型数据进行优化

遇到整数类型的字段可以用 INT型。这样做的理由是，INT型数据有足够大的取值范围，不用担心数据超出取值范围的问题。刚开始做项目的时候，首先要保证系统的稳定性，这样设计字段类型是可以的。但在数据量很大的时候，数据类型的定义，在很大程度上会影响到系统整体的执行效率。
对于 非负型 的数据(如自增ID、整型IP)来说，要优先使用无符号整型 UNSIGNED 来存储。因为无符号相对于有符号，同样的字节数，存储的数值范围更大。如tinyint有符号为-128-127，无符号为0-255，多出一倍的存储空间。

情况2：既可以使用文本类型也可以使用整数类型的字段，要选择使用整数类型

跟文本类型数据相比，大整数往往占用 更少的存储空间，因此，在存取和比对的时候，可以占用更少的内存空间。所以，在二者皆可用的情况下，尽量使用整数类型，这样可以提高查询的效率。如:将IP地址转换成整型数据。

情况3：避免使用TEXT、BLOB数据类型

MySOL 内存临时表 不支持TEXT、BLOB这样的大数据类型，如果查询中包含这样的数据，在排序等操作时，就不能使用内存临时表，必须使用 磁盘临时表 进行。并且对于这种数据，MySQL还是要进行 二次査询 ，会使SQL性能变得很差，但是不是说一定不能使用这样的数据类型。
如果一定要使用，建议把BLOB或是TEXT列 分离到单独的扩展表中，查询时一定不要使用select*（查出来加载到内存中开销很大），而只需要取出必要的列，不需要TEXT列的数据时不要对该列进行查询。

情况4:避免使用ENUM类型
修改ENUM值需要使用ALTER语句。ENUM类型的ORDER BY操作效率低需要额外操作。使用TINYINT来代替ENUM类型。
情况5:使用TIMESTAMP存储时间
TIMESTAMP 存储的时间范围 1970-01-01 00:00:01~2038-01-19-03:14:07。TIMESTAMP使用4字节，DATETIME使用8个字节，同时TIMESTAMP具有自动赋值以及自动更新的特性。
情况6:用DECIMAL代替FLOAT和DOUBLE存储精确浮点数
1)非精准浮点:float,double
2)精准浮点:decimal
Decimal类型为精准浮点数，在计算时不会丢失精度，尤其是财务相关的金融类数据。占用空间由定义的宽度决定，每4个字节可以存储9位数字，并且小数点要古用一个字节。可用于存储比bigint更大的整型数据。

总之，遇到数据量大的项目时一定要在充分了解业务需求的前提下，合理优化数据类型，这样才能充分发挥资源的效率，使系统达到最优。

##### (5) 优化插入记录的速度

插入记录时，影响插入速度的主要是索引、唯一性校验、一次插入记录条数等。根据这些情况可以分别进行优化。这里我们分为MyISAM引擎和InnoDB存储引擎来讲。

MyISAM引擎：

- 禁用索引
- 禁用唯一性检查
- 使用批量插入

```
insert into student values(1,'zhangsan',18,1);
insert into student values(2,'lisi',17,1);
insert into student values(3,'wangwu',17,1);
insert into student values(4,'zhaoliu',19,1);
```

使用一条INSERT语句插入多条记录的情形如下：

```
insert into student values
(1,'zhangsan',18,1),
(2,'lisi',17,1),
(3,'wangwu',17,1),
(4,'zhaoliu',19,1);
```

第2种情形的插入速度要比第1种情形快。

- 使用LOAD DATA INFILE批量导入

当需要批量导入数据时，如果能用LOAD DATA INFILE语句，就尽量使用。因为LOAD DATA INFILE语句导入数据的速度比INSERT语句快。



InnoDB存储引擎：

- 禁用唯一性检查

插入数据之前执行 set unique_checks=0 来禁止对唯一索引的检査，数据导入完成之后再运行 setunique_checks=1。这个和MyISAM引擎的使用方法一样。

- 禁用外键检查

插入数据之前执行禁止对外键的检査，数据插入完成之后再恢复对外键的检查。禁用外键检查的语句如下:

```
SET foreign_key_checks=0;
```

恢复对外键的检查语句如下:

```
SET foreign_key_checks=1;
```

- 禁止自动提交

事务开启后，每次增删改都会默认进行事务的提交。

插入数据之前禁止事务的自动提交，数据导入完成之后，执行恢复自动提交操作。禁止自动提交的语句如下：

```mysql
set autocommit=0；
```

恢复自动提交的语句如下：

```mysql
set autocommit=1；
```



##### (6)使用非空约束

在设计字段的时候，如果业务允许，建议尽量使用非空约束,这样做的好处是：

① 进行比较和计算时，省去要对NULL值的字段判断是否为空的开销，提高存储效率。
② 非空字段也容易创建索引。因为索引NULL列需要额外的空间来保存，所以要占用更多的空间。使用非空约束，就可以节省存储空间(每个字段1个bit)。



##### (7) 分析表、检查表、优化表

**分析表：**

MySQL中提供了ANALYZE TABLE语句分析表，ANALYZE TABLE语句的基本语法如下：

```mysql
ANALYZE [LOCAL | NO_WRITE_TO_BINLOG] TABLE tbl_name[,tbl_name]…
```

默认的，MySQL服务会将 ANALYZE TABLE语句写到binlog中，以便在主从架构中，从服务能够同步数据。

可以添加参数LOCAL 或者 NO_WRITE_TO_BINLOG取消将语句写到binlog中。

使用 ANALYZE TABLE 分析表的过程中，数据库系统会自动对表加一个 只读锁 。在分析期间，只能读取

表中的记录，不能更新和插入记录。ANALYZE TABLE语句能够分析InnoDB和MyISAM类型的表，但是不能

作用于视图。

ANALYZE TABLE分析后的统计结果会反应到 cardinality 的值，该值统计了表中某一键所在的列不重复

的值的个数。**该值越接近表中的总行数，则在表连接查询或者索引查询时，就越优先被优化器选择使**

**用。**也就是索引列的cardinality的值与表中数据的总条数差距越大，即使查询的时候使用了该索引作为查

询条件，存储引擎实际查询的时候使用的概率就越小。下面通过例子来验证下。cardinality可以通过

SHOW INDEX FROM 表名查看。

如：

有一张表，id自增，name都是一样的，并且 给name增加一个索引。

此时通过show index from table查看索引的时候，name索引的Cardinality = 1

然后我们修改其中一条name为其他的，再次通过show index from table查看索引的时候，name索引的Cardinality 还是1

此时我们执行analyze table 表名后，再次通过show index from table查看索引的时候，name索引的Cardinality 为2

**检查表：**

MySQL中可以使用 CHECK TABLE 语句来检查表。CHECK TABLE语句能够检查InnoDB和MyISAM类型的表

是否存在错误。CHECK TABLE语句在执行过程中也会给表加上 只读锁 。

对于MyISAM类型的表，CHECK TABLE语句还会更新关键字统计数据。而且，CHECK TABLE也可以检查视

图是否有错误，比如在视图定义中被引用的表已不存在。该语句的基本语法如下：

```
CHECK TABLE tbl_name [, tbl_name] ... [option] ...
option = {QUICK | FAST | MEDIUM | EXTENDED | CHANGED}
```

其中，tbl_name是表名；option参数有5个取值，分别是QUICK、FAST、MEDIUM、EXTENDED和

- CHANGED。各个选项的意义分别是：
- QUICK ：不扫描行，不检查错误的连接。
- FAST ：只检查没有被正确关闭的表。
- CHANGED ：只检查上次检查后被更改的表和没有被正确关闭的表。
- MEDIUM ：扫描行，以验证被删除的连接是有效的。也可以计算各行的关键字校验和，并使用计算出的校验和验证这一点。
- EXTENDED ：对每行的所有关键字进行一个全面的关键字查找。这可以确保表是100%一致的，但是花的时间较长。

option只对MyISAM类型的表有效，对InnoDB类型的表无效。比如：

![image-20241117224801121](MySQL%E4%BC%98%E5%8C%96.assets/image-20241117224801121.png)

该语句对于检查的表可能会产生多行信息。最后一行有一个状态的 Msg_type 值，Msg_text 通常为 OK。

如果得到的不是 OK，通常要对其进行修复；是 OK 说明表已经是最新的了。表已经是最新的，意味着存

储引擎对这张表不必进行检查。

**优化表：**

方式1：OPTIMIZE TABLE

MySQL中使用 OPTIMIZE TABLE 语句来优化表。但是，OPTILMIZE TABLE语句只能优化表中的VARCHAR 、 BLOB 或 TEXT 类型的字段。一个表使用了这些字段的数据类型，若已经 删除 了表的一大部分数据，或者已经对含有可变长度行的表（含有VARCHAR、BLOB或TEXT列的表）进行了很多 更新 ，则应使用OPTIMIZE TABLE来重新利用未使用的空间，并整理数据文件的 碎片 。

OPTIMIZE TABLE 语句对InnoDB和MyISAM类型的表都有效。该语句在执行过程中也会给表加上 只读锁 。

OPTILMIZE TABLE语句的基本语法如下：

```
OPTIMIZE [LOCAL | NO_WRITE_TO_BINLOG] TABLE tbl_name [, tbl_name] ...
```

LOCAL | NO_WRITE_TO_BINLOG关键字的意义和分析表相同，都是指定不写入二进制日志。

在MyISAM中，是先分析这张表，然后会整理相关的MySQL datafile，之后回收未使用的空间；在InnoDB中，回收空间是简单通过Alter table进行整理空间。在优化期间，MySQL会创建一个临时表，优化完成之后会删除原始表，然后会将临时表rename成为原始表。

说明： 在多数的设置中，根本不需要运行OPTIMIZE TABLE。即使对可变长度的行进行了大量的更新，也不需要经常运行， 每周一次 或 每月一次 即可，并且只需要对 特定的表 运行。

![image-20241117225141967](MySQL%E4%BC%98%E5%8C%96.assets/image-20241117225141967.png)

 ![image-20241117225200552](MySQL%E4%BC%98%E5%8C%96.assets/image-20241117225200552.png)



方式2：使用mysqlcheck命令

```mysql
mysqlcheck -o DatabaseName TableName -U root -p******
```

mysqlcheck是linux中的rompt，-o是代表Optimize

![image-20241117225331327](MySQL%E4%BC%98%E5%8C%96.assets/image-20241117225331327.png)



##### (8)小结

- 修改数据类型，节省存储空间的同时，你要考虑到数据不能超过取值范围；
- 增加冗余字段的时候，不要忘了确保数据一致性；
- 把大表拆分，也意味着你的查询会增加新的连接，从而增加额外的开销和运维的成本；
- 要结合实际的业务需求进行权衡；



#### 6、大表优化

当MySQL单表记录数过大时，数据库的CRUD性能会明显下降，一些常见的优化措施如下

##### （1）限定查询范围

禁止不带任何限制数据范围条件的查询语句。比如：我们当用户在查询订单历史的时候，我们可以控制

在一个月的范围内；

##### （2）读/写分离

经典的数据库拆分方案，主库负责写，从库负责读。

一主一从模式：

![image-20241117230253769](MySQL%E4%BC%98%E5%8C%96.assets/image-20241117230253769.png)

双主双从模式：

![image-20241117230302353](MySQL%E4%BC%98%E5%8C%96.assets/image-20241117230302353.png)



##### （3）垂直拆分

当数据量级达到 千万级 以上时，有时候我们需要把一个数据库切成多份，放到不同的数据库服务器上，减少对单一数据库服务器的访问压力

![image-20241117230356273](MySQL%E4%BC%98%E5%8C%96.assets/image-20241117230356273.png)

- 如果数据库中的数据表过多，可以采用 垂直分库 的方式，将关联的数据表部署在同一个数据库上。

- 如果数据表中的列过多，可以采用 垂直分表 的方式，将一张数据表分拆成多张数据表，把经常一起使用的列放到同一张表里。

  ![image-20241117230519454](MySQL%E4%BC%98%E5%8C%96.assets/image-20241117230519454.png)

**垂直拆分的优点：** 可以使得列数据变小，在查询时减少读取的Block数，减少I/O次数。此外，垂直分区

可以简化表的结构，易于维护。

**垂直拆分的缺点：** 主键会出现冗余，需要管理冗余列，并会引起 JOIN 操作。此外，垂直拆分会让事务

变得更加复杂。



##### （4）水平拆分

- 尽量控制单表数据量的大小，建议控制在1000万以内。1000万并不是MySQL数据库的限制，过大会造成修改表结构、备份、恢复都会有很大的问题。此时可以用历史数据归档(应用于日志数据)，水平分表(应用于业务数据)等手段来控制数据量大小。
- 这里我们主要考虑业务数据的水平分表策略。将大的数据表按照 某个属性维度 分拆成不同的小表，每张小表保持相同的表结构。比如你可以按照年份来划分，把不同年份的数据放到不同的数据表中。2017年、2018年和 2019 年的数据就可以分别放到三张数据表中。
- 水平分表仅是解决了单一表数据过大的问题，但由于表的数据还是在同一台机器上，其实对于提升MySOL并发能力没有什么意义，所以 水平拆分最好分库，从而达到分布式的目的。

![image-20241117230731941](MySQL%E4%BC%98%E5%8C%96.assets/image-20241117230731941.png)

水平拆分能够支持非常大的数据量存储，应用端改造也少，但分片事务难以解决，跨节点Join性能较差，逻辑复杂。《Jav工程师修炼之道》的作者推荐 尽量不要对数据进行分片，因为拆分会带来逻辑、部署、运维的各种复杂度 ，一般的数据表在优化得当的情况下支撑千万以下的数据量是没有太大问题的。如果实在要分片，尽量选择客户端分片架构，这样可以减少一次和中间件的网络IO。

下面补充一下数据库分片的两种常见方案：

- 客户端代理： 分片逻辑在应用端，封装在jar包中，通过修改或者封装JDBC层来实现。当当网的Sharding-JDBC 、阿里的TDDL是两种比较常用的实现。

- 中间件代理： 在应用和数据中间加了一个代理层。分片逻辑统一维护在中间件服务中。我们现在谈的 Mycat 、360的Atlas、网易的DDB等等都是这种架构的实现。



#### 7、其他调优策略

##### （1）**服务器语句超时处理**

在MySQL 8.0中可以设置 服务器语句超时的限制 ，单位可以达到 毫秒级别 。当中断的执行语句超过设置的毫秒数后，服务器将终止查询影响不大的事务或连接，然后将错误报给客户端。

设置服务器语句超时的限制，可以通过设置系统变量 MAX_EXECUTION_TIME 来实现。默认情况下，MAX_EXECUTION_TIME的值为0，代表没有时间限制。 例如：

```mysql
SET GLOBAL MAX_EXECUTION_TIME=2000;
```

```mysql
SET SESSION MAX_EXECUTION_TIME=2000; #指定该会话中SELECT语句的超时时间
```



##### （2）创建全局通用表空间

 MySQL8.0使用 CREATE TABLESPACE 语句来创建一个`全局通用表空间`。全局表空间可以被所有的数据库的表共享，而且相比于独享表空间，使用手动创建共享表空间可以节约元数据方面的内存。可以在创建表的时候，指定属于哪个表空间，也可以对已有表进行表空间修改等。

![image-20241117230010530](MySQL%E4%BC%98%E5%8C%96.assets/image-20241117230010530.png)



##### （3）MySQL 8.0新特性：隐藏索引对调优的帮助

不可见索引的特性对于性能调试非常有用。在MySQL 8.0中，索引可以被“隐藏”和“显示”。当一个索引被隐藏时,它不会被查询优化器所使用。也就是说，管理员可以隐藏一个索引，然后观察对数据库的影响。如果数据库性能有所下降，就说明这个索引是有用的，于是将其“恢复显示”即可;如果数据库性能看不出变化，就说明这个索引是多余的，可以删掉了。
需要注意的是当索引被隐藏时，它的内容仍然是和正常索引一样 实时更新 的。如果一个索引需要长期被隐藏，那么可以将其删除，因为索引的存在会影响插入、更新和删除的性能。数据表中的主键不能被设置为invisible。



## 二、优化命令操作汇总

### 2.1 查看系统性能参数

在MySQL中，可以使用 SHOW STATUS 语句查询一些MySQL数据库服务器的 性能参数 、 执行频率 ，其语句如下所示

```mysql
SHOW [GLOBAL|SESSION] STATUS LIKE '参数';
```

常用性能参数如下所示：

- Connections:连接MySQL服务器的次数。

```mysql
SHOW STATUS LIKE 'Connections'; # 默认SESSION
```

- Uptime:MySQL服务器的上线时间。

```mysql
SHOW STATUS LIKE 'Uptime'; 
```

- Slow_queries:慢查询的次数。

```mysql
SHOW STATUS LIKE 'Slow_queries'; 
```

慢查询次数参数可以结合慢查询日志找出慢查询语句，然后针对慢查询语句进行表结构优化或者查询语句的优化。

- Innodb_rows_read:Select查询返回的行数
- Innodb_rows_inserted:执行INSERT操作插入的行数
- Innodb_rows_updated:执行UPDATE操作更新的行数
- Innodb_rows_deleted:执行DELETE操作删除的行数
- Com_select:查询操作的次数。
- Com_insert:插入操作的次数。对于批量插入的INSERT操作，只累加一次。
- Com_update:更新操作的次数。
- Com_delete:删除操作的次数。



### 2.2 SQL查询成本

通过命令`last_query_cost`可以查询上一个查询语句的查询成本（即需要检索的数据页数量）

可能会遇到查询页数相差20倍的情况，但是查询的效率没有明显变化的情况，是因为因为采用了顺序读取的方式将页面一次性加载到缓冲池中，然后再进行查找。虽然 页数量(last_query_cost)增加了不少，但是通过缓冲池的机制，并没有增加多少査询时间。

**使用场景：**

该命令对于比较不同查询的开销是非常有用的。

SQL 查询是一个动态的过程，从页加载的角度来看，我们可以得到以下两点结论:

- 位置决定效率 。如果页就在数据库 缓冲池 中，那么效率是最高的，否则还需要从 内存 或者 磁盘 中进行读取，当然针对单个页的读取来说，如果页存在于内存中，会比在磁盘中读取效率高很多。
- 批量决定效率。如果我们从磁盘中对单一页进行随机读，那么效率是很低的(差不多 10ms)，而采用顺序读取的方式，批量对页进行读取，平均一页的读取效率就会提升很多，甚至要快于单个页面在内存中的随机读取。

所以说，遇到I/O并不用担心，方法找对了，效率还是很高的。我们首先要考虑数据存放的位置，如果是经常使用的数据就要尽量放到 缓冲池 中，其次我们可以充分利用磁盘的吞吐能力，一次性批量读取数据，这样单个页的读取效率也就得到了提升。



### 2.3 慢查询日志

#### 1、介绍

MySOL的慢查询日志，用来记录在MySQL中响应时间超过阀值的语句，具体指运行时间超过long_query_time 值的SQL，则会被记录到慢查询日志中。long_query_time的默认值为 10，意思是运行10秒以上(不含10秒)的语句，认为是超出了我们的最大忍耐时间值。
它的主要作用是，帮助我们发现那些执行时间特别长的 SQL查询，并且有针对性地进行优化，从而提高系统的整体效率。当我们的数据库服务器发生阻塞、运行变慢的时候，检査一下查询日志，找到那些慢查询，对解决问题很有帮助。比如一条sql执行超过5秒钟，我们就算慢SQL，希望能收集超过5秒的sql，结合explain进行全面分你
默认情况下，MySQL数据库没有开启慢査询日志，需要我们手动来设置这个参数。如果不是调优需要的话，一般不建议启动该参数，因为开启慢查询日志会或多或少带来一定的性能影响。慢查询日志支持将日志记录写入文件。



#### 2、开启慢查询日志参数

通过show variables like ‘%slow_query_log’查看慢查询日志是否开启，以及慢查询日志文件的位置

![image-20241103205042999](MySQL%E4%BC%98%E5%8C%96.assets/image-20241103205042999.png)

如果慢查询日志没有开启，通过如下命令开启

```mysql
set global slow_query_log='ON';  #设置全局参数
```

通过设置long_query_time参数来修改慢查询的时间阈值

查看命令如下

```mysql
show variableslike'%long_query_time%';
```

设置命令如下所示

```mysql
set long_query_time=1;
```

设置global的方式（setgloballong_query_time=1;）对当前session的long_query_time失效。对新连接的客户端有效。如果要对当前会话及时生效，可以用如下语句，直接设置当前会话的慢查询参数



#### 3、查询慢查询的数量

查询当前系统中有多少条慢查询记录

```mysql
SHOWGLOBALSTATUSLIKE'%Slow_queries%';
```



#### 4、慢查询日志分析

在生产环境中，如果要手工分析日志，查找、分析SQL，显然是个体力活，MySQL提供了日志分析工具mysqldumpslow，可以通过如下命令查看相关的参数

```
mysqldumpslow --help
```

相关参数内容如下所示：

- -a:不将数字抽象成N，字符串抽象成S

- -s :是表示按照何种方式排序：

  - c:访问次数

  - l:锁定时间
  - r:返回记录

  - t:查询时间

  - al:平均锁定时间

  - ar:平均返回记录数

  - at:平均查询时间（默认方式）

  - ac:平均查询次数

- -t:即为返回前面多少条的数据；

- g:后边搭配一个正则匹配模式，大小写不敏感的；

示例：

```mysql
#得到返回记录集最多的10个SQL
mysqldumpslow -s r -t 10 /var/lib/mysql/atguigu-slow.log
#得到访问次数最多的10个SQL
mysqldumpslow -sc -t 10 /var/lib/mysql/atguigu-slow.log
#得到按照时间排序的前10条里面含有左连接的查询语句
mysqldumpslow -s t-t10 -g "leftjoin"/var/lib/mysql/atguigu-slow.log #另外建议在使用这些命令时结合|和more使用，否则有可能出现爆屏情况
mysqldumpslow -sr -t10 /var/lib/mysql/atguigu-slow.log|more
```



#### 5、关闭慢查询日志

MySQL服务器停止慢查询日志功能有两种方法：

- 永久性

在配置文件中设置，或者把slow_query_log一项注释掉

```
[mysqld]
slow_query_log=OFF
```

重启MySQL服务，执行如下语句查询慢日志功能

```mysql
SHOW VARIABLES LIKE '%slow%'; #查询慢查询日志所在目录
SHOW VARIABLES LIKE '%long_query_time%'; #查询超时时长
```



- 临时性

使用SET语句来设置。（1）停止MySQL慢查询日志功能，具体SQL语句如下

```
SET GLOBA slow_query_log=off;
```

重启MySQL服务，执行如下语句查询慢日志功能



#### 6、删除慢查询日志

使用SHOW语句显示慢查询日志信息，具体SQL语句如下

```
SHOW VARIABLES LIKE 'slow_query_log%';
```

从执行结果可以看出，慢查询日志的目录默认为MySQL的数据目录，在该目录下 手动删除慢査询日志文件 即可。

使用命令 mysqladmin flush-logs 来重新生成查询日志文件，具体命令如下，执行完毕会在数据目录下重新生成慢查询日志文件。

```mysql
mysqladmin -uroot -p flush-logs slow
```

注意：

慢查询日志都是使用mysqladmin flush-logs命令来删除重建的。使用时一定要注意，一旦执行了这个命令，慢查询日志都只存在新的日志文件中，如果需要旧的查询日志，就必须事先备份。



### 2.4 查看SQL执行成本语句：Show Profile

Show Profile是MySQL提供的可以用来分析当前会话中SQL 都做了什么、执行的资源消耗情况的工具，可用于sql调优的测量。 默认情况下处于关闭状态，并保存最近15次的运行结果。

通过如下命令可以查看其开启状态

```mysql
show variables like 'profiling';
```

![image-20241103211531367](MySQL%E4%BC%98%E5%8C%96.assets/image-20241103211531367.png)

通过设置profiling='ON’来开启showprofile：

```mysql
set profiling='ON';
```

![image-20241103211549338](MySQL%E4%BC%98%E5%8C%96.assets/image-20241103211549338.png)

执行相关的查询语句。接着看下当前会话都有哪些profiles，使用下面这条命令

```
show profiles;
```

![image-20241103211647252](MySQL%E4%BC%98%E5%8C%96.assets/image-20241103211647252.png)

看到当前会话一共有2个查询。如果我们想要查看最近一次查询的开销，可以使用

```mysql
show profile;
```

![image-20241103211713052](MySQL%E4%BC%98%E5%8C%96.assets/image-20241103211713052.png)

如果需要指定查看的查询语句，可以使用如下命令

```mysql
show profile cpu,block io for query2;
```

show profile的常用查询参数：

①ALL：显示所有的开销信息。②BLOCKIO：显示块IO开销。③CONTEXTSWITCHES：上下文切换开

销。④CPU：显示CPU开销信息。⑤IPC：显示发送和接收开销信息。⑥MEMORY：显示内存开销信

息。⑦PAGEFAULTS：显示页面错误开销信息。⑧SOURCE：显示和Source_function，Source_file，

Source_line相关的开销信息。⑨SWAPS：显示交换次数开销信息。



### 2.5 分析查询语句：EXPLAIN（△）

#### 1、概述

定位了查询慢的 SQL之后，就可以使用 EXPLAIN 或 DESCRIBE 工具做针对性的分析查询语句。DESCRIBE语句的使用方法与EXPLAIN语句是一样的，并且分析结果也是一样的。
MySQL中有专门负责优化SELECT语句的优化器模块，主要功能:通过计算分析系统中收集到的统计信息，为客户端请求的Query提供它认为最优的 执行计划(他认为最优的数据检索方式，但不见得是DBA认为是最优的，这部分最耗费时间)。
这个执行计划展示了接下来具体执行查询的方式，比如多表连接的顺序是什么，对于每个表采用什么访问方法来具体执行査询等等。MySQL为我们提供了 EXPLAIN语句来帮助我们查看某个查询语句的具体执行计划，大家看懂EXPLAIN 语句的各个输出项，可以有针对性的提升我们查询语句的性能。

通过EXPLAIN我们可以看出如下内容：

- 表的读取顺序
- 数据读取操作的操作类型
- 那些索引可以使用
- 那些索引被实际使用
- 表之间的引用
- 每张表有多少行被优化器查询

#### 2、版本说明

- MySQL5.6.3以前只能使用EXPLAIN SELECT；在MySQL5.6.3后可以使用EXPLAIN SELECT、UPDATE、DELETE语句
- 在5.7以前的版本中，想要显示partitions需要使用explainpartitions命令；想要显示filtered，需要使用explain extended命令。在5.7版本后，默认explain直接显示partitions和filtered中的信息。

#### 3、基本语法&各列内容说明

EXPLAIN或DESCRIBE语句的语法形式如下：

```mysql
EXPLAIN SELECT select_options 
DESCRIBE SELECT select_options
```

EXPLAIN语句输出的各个列的作用如下：

| 列名          | 描述                                                         |
| ------------- | ------------------------------------------------------------ |
| id            | select查询的序列号，包含一组数字，表示查询中执行select子句或操作表的顺序 |
| select_type   |                                                              |
| table         |                                                              |
| type          |                                                              |
| possible_keys |                                                              |
| key           |                                                              |
| key_len       |                                                              |
| ref           |                                                              |
| rows          |                                                              |
| filtered      |                                                              |
| Extra         |                                                              |



### 2.6 分析优化器执行计划：trace

OPTIMIZER_TRACE 是MySQL5.6引入的一项跟踪功能，它可以跟踪优化器做出的各种决策(比如访问表的方法各种开销计算、各种转换等)，并将跟踪结果记录到 INFORMATION_SCHEMA.OPTIMIZER_TRACE 表中。此功能默认关闭。开启trace，并设置格式为 JSON，同时设置trace最大能够使用的内存大小，避免解析过程中因为默认内存过小而不能够完整展示。

开启trace命令

```mysql
SET optimizer_trace="enabled=on",end_markers_in_json=on;
```

设置最大能够使用内存大小

```mysql
set optimizer trace_max mem size=1880000:
```

其可分析语句如下：

- SELECT
- INSERT
- REPLACE
- UPDATE 
- DELETE
- EXPLAIN 
- SET
- DECLARE 
- CASE
- IF
- RETURN 
- CALL

示例：

执行如下SQL

```mysql
select * from student where id<10;
```

最后，查询information_schema.optimizer_trace就可以知道MySQL是如何执行SQL的

```mysql
select * from information_schema.optimizer_trace \G
```



### 2.7 监控分析视图：sys schema

#### 1、概要

关于MySQL的性能监控和问题诊断，我们一般都从performance_schema中去获取想要的数据，在MySQL5.7.7版本中新增sys schema，它将performance_schema和information_schema中的数据以更容易理解的方式总结归纳为”视图”，其目的就是为了 降低査询performance_schema的复杂度，让DBA能够快速的症位问题。下面看看这些库中都有哪些监控表和视图，掌握了这些，在我们开发和运维的过程中就起到了事半功倍的效果

#### 2、Sys schema视图摘要

1.主机相关：以host_summary开头，主要汇总了IO延迟的信息。

2.Innodb相关：以innodb开头，汇总了innodbbuffer信息和事务等待innodb锁的信息。

3.I/o相关**：**以io开头，汇总了等待I/O、I/O使用量情况。

4.内存使用情况：以memory开头，从主机、线程、事件等角度展示内存的使用情况

5.连接与会话信息：processlist和session相关视图，总结了会话相关信息。

6.表相关：以schema_table开头的视图，展示了表的统计信息。

7.索引信息：统计了索引的使用情况，包含冗余索引和未使用的索引情况。

8.语句相关：以statement开头，包含执行全表扫描、使用临时表、排序等的语句信息。

9.用户相关：以user开头的视图，统计了用户使用的文件I/O、执行语句统计信息。

10.等待事件相关信息：以wait开头，展示等待事件的延迟情况。

#### 3、使用场景

索引情况

![image-20241103224709079](MySQL%E4%BC%98%E5%8C%96.assets/image-20241103224709079.png)

表相关

![image-20241103224713565](MySQL%E4%BC%98%E5%8C%96.assets/image-20241103224713565.png)

语句相关

![image-20241103224719161](MySQL%E4%BC%98%E5%8C%96.assets/image-20241103224719161.png)

IO相关

![image-20241103224728202](MySQL%E4%BC%98%E5%8C%96.assets/image-20241103224728202.png)

innodb相关

![image-20241103224736263](MySQL%E4%BC%98%E5%8C%96.assets/image-20241103224736263.png)





### 2.8 外连接与内连接查询优化

#### 1、左连接（右连接）

左连接：left join

右连接：right join

对以下语句进行分析，a为驱动表，b为被驱动表，通过name字段进行关联

```mysql
EXPLAIN SELECT SQL_NO_CACHE * FROM a LEFT JOIN b ON a.a_name = b.b_name;
```

在未加任何索引的情况下，走的都是全表扫描，即type = ALL。

![image-20241105234121135](MySQL%E4%BC%98%E5%8C%96.assets/image-20241105234121135.png)

此时我们给被驱动表t2的a字段加索引，可以避免全表扫描

```mysql
ALTER TABLE b ADD INDEX X ( b_name);
```

再次通过EXPLAIN查看执行计划，此时第二行（即被驱动表）的 type 变为了 ref，rows 也变小了，优化比较明显。

![image-20241105234306488](MySQL%E4%BC%98%E5%8C%96.assets/image-20241105234306488.png)

被驱动表需要在驱动表数据的基础上，进行查找，所以被驱动表是我们的关键点,一定需要建立索引 。

在没有建立索引前，就相当于是一个双层循环，时间复杂度是O(n^2),但当我们为被驱动表添加索引后，在B+树上搜索的时间复杂度是O（log2），因此总的时间复杂度是O（nlog2）

给驱动表也加上索引

```mysql
ALTER TABLE a ADD INDEX Y ( a_name);
```

在查看执行计划发现a还是走的全表扫描，在优化器看来，a既然要扫描全部的数据，如果走索引又要回表，开销比较大，不如直接走全表扫描

![image-20241105234529372](MySQL%E4%BC%98%E5%8C%96.assets/image-20241105234529372.png)

如果使用索引覆盖，避免回表，a还是会使用索引的

```mysql
EXPLAIN SELECT SQL_NO_CACHE a_name FROM a LEFT JOIN b ON a.a_name = b.b_name;
```

执行结果

![image-20241105234830051](MySQL%E4%BC%98%E5%8C%96.assets/image-20241105234830051.png)

接着，把b表上的索引删掉

```mysql
drop index X on b
EXPLAIN SELECT SQL_NO_CACHE a_name FROM a LEFT JOIN b ON a.a_name = b.b_name;
```

查看执行结果如下，a选择走索引，b则选择走全表扫描

![image-20241105234959123](MySQL%E4%BC%98%E5%8C%96.assets/image-20241105234959123.png)

注意：

- 如果连接的字段类型不一样，会出现索引失效的情况（涉及到类型转换，函数调用）。

- left join、right join的驱动表和被驱动表确定后，优化器也会对其驱动表和被驱动表的选择 进行优化



#### 2、内连接

内连接：inner join （mysql查询优化器会自动选择驱动表和被驱动表）

原则：

- 没有索引的时候，小表驱动大表
- 连接条件中只有一个字段有索引的时候，有索引的表作为被驱动表
- 连接条件中的字段都有索引都有索引的时候，依旧符合小表驱动大表



#### 3、join语句实现原理

 join方式连接多个表，本质就是各个表之间数据的循环匹配。MySQL5.5 版本之前，MySQL只支持一种表间关联方式，就是嵌套循环(Nested Loop Join)。如果关联表的数据量很大，则join关联的执行时间会非常长。在MySQL5.5以后的版本中，MySQL通过引入BNLJ算法来优化嵌套执行。

**（1）关于驱动表和非驱动表**

驱动表就是主表，被驱动表就是从表，非驱动表。

对于内连接：

```mysql
select * from a inner join b on ……
```

对于上述的语句，A不一定是驱动表，优化器会根据你查询语句做优化，决定先查哪张表。先查询的那张表就是驱动表反之就是被驱动表。通过 explain关键字可以查看。

对于外连接：

```mysql
select * from a left join b on ……
select * from b left join a on ……
```

优化器也会对其驱动表和被驱动表的选择 进行优化，具体代码示例如下（△）

```mysql
CREATE TABLE a(f1 INT, f2 INT, INDEX(f1))ENGINE=INNODB;
CREATE TABLE b(f1 INT,f2 INT)ENGINE=INNODB;
INSERT INTO a VALUES(1,1),(2,2),(3,3),(4,4),(5,5),(6,6);
INSERT INTO b VALUES(3,3),(4,4),(5,5),(6,6),(7,7),(8,8);
SELECT * FROM b;
#测试1（底层将left join 优化为内连接 inner join，然后选择驱动表和非驱动表）
EXPLAIN SELECT * FROM a LEFT JOIN b ON(a.f1=b.f1) WHERE (a.f2=b.f2);   # a为被驱动表
#测试2
EXPLAIN SELECT * FROM a LEFT JOIN b ON(a.f1=b.f1) AND (a.f2=b.f2);	   # b为被驱动表
```



**（2）Simple Nested-Loop Join（简单嵌套循环连接）**

算法相当简单，从表A中取出一条数据1，遍历表B，将匹配到的数据放到result.以此类推，驱动表A中的每一条记录与被驱动表B的记录进行判断:

![image-20241106001458278](MySQL%E4%BC%98%E5%8C%96.assets/image-20241106001458278.png)

首先遍历A中的每一条数据，对于每一条数据，就爱那个B中的数据加载到内存中，然后进行比较匹配，比较结束后，将B从内存中清去，后续同理。（IO高）

这种方式效率是非常低的，以上述表A数据100条，表B数据1000条计算，则A*B=10万次。开销统计如
下:

外表扫描次数即只扫描一遍A遍，即1次。内部扫描次数即B表需要扫描A次，即A次。![image-20241106001535655](MySQL%E4%BC%98%E5%8C%96.assets/image-20241106001535655.png)

当然mysql 肯定不会这么粗暴的去进行表的连接，所以就出现了后面的两种对Nested-Loop Join 优化算法。



**（3）Index Nested-Loop Join（索引嵌套循环连接）**

Index Nested-Loop Join其优化的思路主要是为了 减少内层表数据的匹配次数 ，所以要求被驱动表上必须 有索引才行。通过外层表匹配条件直接与内层表索引进行匹配，避免和内层表的每条记录去进行比较，这样极大的减少了对内层表的匹配次数。

![image-20241106001946828](MySQL%E4%BC%98%E5%8C%96.assets/image-20241106001946828.png)

驱动表中的每条记录通过被驱动表的索引进行访问，因为索引查询的成本是比较固定的，故mysql优化器都倾向于使用记录数少的表作为驱动表(外表)。

![image-20241106002021363](MySQL%E4%BC%98%E5%8C%96.assets/image-20241106002021363.png)

参数：

- B（match）：匹配的数据
-  Index（Height）：索引高度

如果被驱动表加索引，效率是非常高的，但如果索引不是主键索引，所以还得进行一次回表查询。相比，被驱动
表的索引是主键索引，效率会更高。



**（4）Index Nested-Loop Join（索引嵌套循环连接）**

如果存在索引，那么会使用index的方式进行join，如果join的列没有索引，被驱动表要扫描的次数太多了。每次访问被驱动表，其表中的记录都会被加载到内存中，然后再从驱动表中取一条与其匹配，匹配结束后清除内存，然后再从驱动表中加载一条记录，然后把被驱动表的记录再加载到内存匹配，这样周而复始，大大增加了IO的次数。为了减少被驱动表的IO次数，就出现了Block Nested-Loop Join的方式。
不再是逐条获取驱动表的数据，而是一块一块的获取，引入了join buffer缓冲区，将驱动表join相关的部分数据列(大小受join bufer的限制)缓存到join bufer中，然后全表扫描被驱动表，被驱动表的每一条记录一次性和joinbufer中的所有驱动表记录进行匹配(内存中操作)，将简单嵌套循环中的多次比较合并成一次，降低了被驱动表的访问频率。

> 注意：
>
> - 这里缓存的不只是关联表的列，select 后面的列也会缓存起来。
> - 在一个有N个Join关联的sql中会分配N-1个join buffer。所以査询的时候尽量减少不必要的字段，可以让join buffer中可以存放更多的列。

![image-20241106002804275](MySQL%E4%BC%98%E5%8C%96.assets/image-20241106002804275.png)

![image-20241106002850260](MySQL%E4%BC%98%E5%8C%96.assets/image-20241106002850260.png)

参数：

- A*used_column_size:A表总共需要的内存大小（包括字段）
- A*used_column_size/join_buffer_size:需要分成几个块区进行批量匹配，后面的1是本身要对A进行一次扫描



相关参数：

- block_nested_loop

通过 show variables like'%optimizer_switch%'查看block_nested_loop 状态。默认是开启的.

- join_buffer_size

驱动表能不能一次加载完，要看能不能存储所有的数据，默认情况下 ioin_buffer_size=256k。

join_buffer_size的最大值在32位系统可以申请4G，而在64位操做系统下可以申请大于4G的Join Buffer空间(64 位Windows 除外，其大值会被截断为 4GB 并发出警告)。



**(5)Hash join**(△)

- Nested Loop:
  对于被连接的数据子集较小的情况，Nested Loop是个较好的选择。
- Hash join是做大数据集连接 时的常用方式，优化器使用两个表中较小(相对较小)的表利用Join Key在内存中建立 散列表 ，然后扫描较大的表并探测散列表，找出与Hash表匹配的行。
  - 这种方式适用于较小的表完全可以放于内存中的情况，这样总成本就是访问两个表的成本之和。
  - 在表很大的情况下并不能完全放入内存，这时优化器会将它分割成 若干不同的分区，不能放入内存的部分就把该分区写入磁盘的临时段，此时要求有较大的临时段从而尽量提高IO 的性能。
  - 它能够很好的工作于没有索引的大表和并行査询的环境中，并提供最好的性能。大多数人都说它是Join的重型升降机。Hash Join只能应用于等值连接(如WHERE A.COL1=B.COL2)，这是由Hash的特点决定的。

![image-20241106004618373](MySQL%E4%BC%98%E5%8C%96.assets/image-20241106004618373.png)



**(6)总结**

- 效率：INLJ>BNLJ>SNLJ
- 永远用小结果集驱动大结果集(其本质就是减少外层循环的数据数量)(小的度量单位指的是 表行数"每行大小)

```mysql
# straight_join强行指定驱动表和被驱动表，不被优化
select t1.b,t2.* from It1 straight_join t2 on(t1.b=t2.b) where t2.id<=100;#推荐（查询t1字段少）
select t1.b,t2.* from t2 straight_join t1 on(t1.b=t2.b) where t2.id<=100;#不推荐
```

- 为被驱动表匹配的条件增加索引(减少内层表的循环匹配次数)
- 增大join buffer size的大小(一次缓存的数据越多，那么内层包的扫表次数就越少)
- 减少驱动表不必要的字段査询(字段越少，join buffer 所缓存的数据就越多)



### 2.9 子查询优化

#### 1、关于子查询&优点

MySQL从4.1版本开始支持子查询，使用子查询可以进行SELECT语句的嵌套查询，即一个SELECT查询的结果作为另一个SELECT语句的条件。子查询可以一次性完成很多逻辑上需要多个步骤才能完成的SQL操作。

#### 2、弊端

通过子查询可以实现比较复杂的查询，但是，子查询的执行效率不高，其原因如下:

- 执行子査询时，MySQL需要为内层查询语句的査询结果建立一个**临时表**，然后外层查询语句从临时表中查询记录。查询完毕后，再撤销这些临时表。这样会消耗过多的CPU和IO资源，产生大量的慢查询。

- 子查询的结果集存储的临时表，不论是内存临时表还是磁盘临时表都不会存在索引，所以查询性能会受到一定的影响。
- 对于返回结果集比较大的子查询，其对查询性能的影响也就越大。

解决方法：①将子查询SQL拆成多个SQL来多次查询 ② 使用JOIN来代替子查询（连接查询不需要建立临时表，其速度会比子查询快，并且如果在被驱动表上加索引，性能会更好）

#### 3、示例

a表结构

```mysql
CREATE TABLE `a` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `a_name` varchar(30) DEFAULT NULL,
  `a_age` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=10002 DEFAULT CHARSET=utf8;
```

b表结构

```mysql
CREATE TABLE `b` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `b_name` varchar(30) DEFAULT NULL,
  `b_age` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=5002 DEFAULT CHARSET=utf8;
```

子查询语句

```mysql
explain select * from a where
a.a_age in (select b.b_age from b where b.b_age is not null)
```

通过explain看下其执行计划如下

![image-20241106204343118](MySQL%E4%BC%98%E5%8C%96.assets/image-20241106204343118.png)

其中MATERIAUZED是对内查询的雾化，然后与外面的查询进行连接。其查询时间为0.018s

我们可以通过join连接的方式来对上面的内查询进行优化

```mysql
explain select * from a join b on a.a_age = b.b_age where  b.b_age is not null
```

执行结果如下所示，其执行时间为0.015s（由于数据量比较小，差异不是很明显）

![image-20241106204632347](MySQL%E4%BC%98%E5%8C%96.assets/image-20241106204632347.png)

对于子查询not in的语句，我们可以用如下的连接进行互相替换

```mysql
# 内查询
explain select * from a where
a.a_age not in (select b.b_age from b where b.b_age is not null)

# 连接（注：left join 是 left outer join的缩写）
explain select * from a left outer join b on a.a_age = b.b_age where  b.b_age is null
```

#### 4、总结

- 竟可能不要使用not in和not exists，而是通过left join xxx on xxx where xx is null去代替，从而提高查询的效率。



### 2.10 排序优化

#### 1、什么是排序优化

在Where条件上加索引，可以优化查询速度，避免全表扫描。在Order by字段上加索引，可以提高排序字段（因为索引是有序的），避免使用FileSort排序。（当然，在数据量小的情况下，全表扫描和FileSort排序不一定比索引慢，但是应当竟可能避免，来提高查询效率）

在 MySQL中，支持两种排序方式，分别是 FileSort 和 Index 排序。

- Index 排序中，索引可以保证数据的有序性，不需要再进行排序，效率更高。
- FileSort 排序则一般将数据加载到内存中，在内存中进行排序，占用 CPU 较多。如果待排序数据的结果较大，会产生临时文件 IO 到磁盘进行排序的情况，效率较低。

竟可能使用索引去完成Order by排序，如果where和order by后面是相同的列就使用单索引列，如果不同就使用联合索引（如where a = 1 order by b，使用索引index（a，b）），如果无法使用索引时，需要对FileSort方法进行调优。

#### 2、示例

- 没有索引，走FileSort

不加任何索引，直接order by

![image-20241106210954331](MySQL%E4%BC%98%E5%8C%96.assets/image-20241106210954331.png)

![image-20241106211013985](MySQL%E4%BC%98%E5%8C%96.assets/image-20241106211013985.png)

- order by 时不limit，索引失效

其失效的原因是查询所有的字段数据，并且走的是二级索引，需要大量的回表，因此优化器认为还是直接对内存中的数据排序来得更快，效率更高

![image-20241106211416396](MySQL%E4%BC%98%E5%8C%96.assets/image-20241106211416396.png)

![image-20241106211352250](MySQL%E4%BC%98%E5%8C%96.assets/image-20241106211352250.png)

![image-20241106211358378](MySQL%E4%BC%98%E5%8C%96.assets/image-20241106211358378.png)

如果查询索引字段，即索引覆盖，让其不会去回表，那么会使用索引

![image-20241106211528215](MySQL%E4%BC%98%E5%8C%96.assets/image-20241106211528215.png)

![image-20241106211532986](MySQL%E4%BC%98%E5%8C%96.assets/image-20241106211532986.png)



- 增加limit条件，使用上索引

原因就是数据量小了，优化器觉得走索引效率更高

![image-20241106211647009](MySQL%E4%BC%98%E5%8C%96.assets/image-20241106211647009.png)

![image-20241106211652248](MySQL%E4%BC%98%E5%8C%96.assets/image-20241106211652248.png)



- order by 顺序错误，导致索引失效

```mysql
# 建索引
CREATE INDEX idx_1 oN student (age,classid,stuno);
CREATE INDEX idx_2 oN student (age,classid,name);

# 索引失效，不符合最左前缀原则
EXPLAIN SELECT * FROM student order by classid limit 10;

# 索引失效，不符合最左前缀原则
EXPLAIN SELECT * FROM student order by classid, name limit 10;

# 索引生效
EXPLAIN SELECT * FROM student order by age,classid,stuno limit 10;

# 索引生效，使用idx_2(应该是竟可能选择匹配长key_len长的)
EXPLAIN SELECT * FROM student order by age,classid limit 10;

# 索引生效，使用idx_2
EXPLAIN SELECT * FROM student order by age limit 10;

```



- order by时规则不一致，索引失效（顺序错，不索引；方向反，不索引）

```mysql
# 建索引（顺序默认递增asc）
CREATE INDEX idx_1 oN student (age,classid,stuno);
CREATE INDEX idx_2 oN student (age,classid,name);

# 不使用索引，age为desc，classic为asc，方向有错误
EXPLAIN SELECT * FROM student order by age desc, classid asc limit 10;

# 不使用索引 顺序错
EXPLAIN SELECT * FROM student order by classid desc, name desc limit 10;

# 不使用索引 方向有错误
EXPLAIN SELECT * FROM student order by age asc, classid desc limit 10;

# 使用顺序，方向都和索引顺序相反，即反向遍历即可（负负得正）
EXPLAIN SELECT * FROM student order by age desc, classid desc limit 10;
```



- 无过滤，不索引

```mysql
# 建索引（顺序默认递增asc）
CREATE INDEX idx_age_classid_stuno oN student (age,classid,stuno);
CREATE INDEX idx_age_classid_name oN student (age,classid,name);
```

语句一

![image-20241106213602200](MySQL%E4%BC%98%E5%8C%96.assets/image-20241106213602200.png)

![image-20241106213606578](MySQL%E4%BC%98%E5%8C%96.assets/image-20241106213606578.png)

其走了索引，但是使用索引长度只有5，即只走了age索引字段，没有走classid，因为查询过程中线过滤数据再排序，过滤玩数据之后，数据量比较小了，优化器认为走内存排序更快，所以就没有走classid索引字段。

语句二

![image-20241106213736764](MySQL%E4%BC%98%E5%8C%96.assets/image-20241106213736764.png)

![image-20241106213741131](MySQL%E4%BC%98%E5%8C%96.assets/image-20241106213741131.png)

同理语句一，过滤完数据后，直接在内存中排序。 

语句三

![image-20241106213939199](MySQL%E4%BC%98%E5%8C%96.assets/image-20241106213939199.png)

![image-20241106214006588](MySQL%E4%BC%98%E5%8C%96.assets/image-20241106214006588.png)

语句四

![image-20241106214021212](MySQL%E4%BC%98%E5%8C%96.assets/image-20241106214021212.png)

![image-20241106214025299](MySQL%E4%BC%98%E5%8C%96.assets/image-20241106214025299.png)

相比较语句三，其使用了索引，其先进行排序，排序后再通过where条件去过滤数据，然后只取前十个，因此将索引全部用上了（优化器认为这样性能更高一点）。对于order by + limit，limit后面比较小，优化器有时候会先执行order by后where，官方文档8.2.19有说明。



#### 3、filesort算法：双路排序和单路排序

 排序的字段如果不在索引列上，则会使用filesort，其有两种算法：双路排序和单路排序。

**①双路排序**

MSQL 4.1之前是使用双路排序，字面意思就是两次扫描磁盘，最终得到数据。首先读取行指针和order by列，
对他们进行排序，然后扫描已经排序好的列表，按照列表中的值重新从列表中读取对应的数据输出，即从磁盘取排序字段，在buffer进行排序，再从磁盘取其他字段。
取一批数据，要对磁盘进行两次扫描，众所周知，IO是很耗时的，所以在mysql4.1之后，出现了第二种改进的算法，就是单路排序。

**②单路排序**

MYSQL4.1之后，出现了单路排序。从磁盘读取查询需要的 所有列，按照order by列在buffer对它们进行排序，然后扫描排序后的列表进行输出，它的效率更快一些，避免了第二次读取数据。并且把随机IO变成了顺序IO，但是它会使用更多的空间，因为它把每一行都保存在内存中了。

**利弊分析**

- 由于单路是后出的，总体而言好过双路
- 但是用单路有问题，在sort_buffer中，单路比多路要多占用很多空间，因为单路是把所有字段都取出,所以有可能取出的数据的总大小超出了 sort_buffer的容量，导致每次只能取sort_buffer容量大小的数据，进行排序(创建tmp文件，多路合并)，排完再取sort_buffer容量大小，再排..…从而多次I/O。单路本来想省一次I/O操作，反而导致了大量的I/O操作，反而得不偿失。

**优化（对filesort的优化策略）**

- 尝试提高 sort buffer_size

不管用哪种算法，提高这个参数都会提高效率，要要根据系统的能力去提高，因为这个参数是针对每个进程(connection)的1M-8M之间调整。MySOL5.7，InnoDB存储引擎默认值是1048576字节，1MB.

- 提高 max_length _for_sort_data

提高这个参数，会增加用改进算法的概率。但是如果设的太高，数据总容量超出sort_buffer_size的概率就增大，明显症状是高的磁盘IO活动和低的处理器使用率。如果需要返回的列的总长度大于max_length_for_sort_data，使用双路算法，否则使用单路算法。1024-8192字节之间调整。

- Order by 时select*是一个大忌。最好只Query需要的字段

当Query的字段大小总和小于 max_length_for_sort_data，而且排序字段不是TEXTIBLOB 类型时，会用改进后的算法单路排序，否则用老算法多路排序。
两种算法的数据都有可能超出sort_bufer_size的容量，超出之后，会创建tmp文件进行合并排序，导致多次IO，但是用单路排序算法的风险会更大一些，所以要提高sort_buffer_siz。

#### 4、小结

1、一些使用索引

![image-20241106214446629](MySQL%E4%BC%98%E5%8C%96.assets/image-20241106214446629.png)

2、filesort不一定慢

![image-20241106215509326](MySQL%E4%BC%98%E5%8C%96.assets/image-20241106215509326.png)

为了优化filesort，我们可以建立如下索引

![image-20241106215618171](MySQL%E4%BC%98%E5%8C%96.assets/image-20241106215618171.png)

其执行计划如下所示

![image-20241106215629802](MySQL%E4%BC%98%E5%8C%96.assets/image-20241106215629802.png)

如果建立如下索引

![image-20241106215700506](MySQL%E4%BC%98%E5%8C%96.assets/image-20241106215700506.png)

期执行计划如下，只是用了age和stuno索引字段，并且速度会比上面的更快

![image-20241106215710628](MySQL%E4%BC%98%E5%8C%96.assets/image-20241106215710628.png)

按照正常理解有filesort结果会慢一些，但是 filesort的 sql 运行速度，超过了已经优化掉 filesort的 sql，而且快了很多，几乎一瞬间就出现了结果。
原因:
所有的排序都是在条件过滤之后才执行的。所以，如果条件过滤掉大部分数据的话，剩下几百几千条数据进行排序其实并不是很消耗性能，即使索引优化了排序，但实际提升性能很有限。相对的 stuno<101000 这个条件，如果没有用到索引的话，要对几万条的数据进行扫描，这是非常消耗性能的，所以索引放在这个字段上性价比最高，是最优选择。

因此:

- 两个索引同时存在，mysql自动选择最优的方案。(对于这个例子，mysql选择idx_age_stuno_name)但是，随着数据量的变化，选择的索引也会随之变化的。
- 当【范围条件】和【group by 或者 order by】的字段出现二选一时，优先观察条件字段的过滤数量如果过滤的数据足够多，而需要排序的数据并不多时，优先把索引放在范围字段上。反之，亦然。



### 2.11 group by 优化

- group by 使用索引的原则几乎跟order by一致 ，group by 即使没有过滤条件用到索引，也可以直接使用索引。
- group by先排序再分组，遵照索引建的最佳左前缀法则
- 当无法使用索引列，增大max_length_for_sort_data和 sort_buffer_size 参数的设置。
- where效率高于having，能写在where限定的条件就不要写在having中了
- 减少使用order by，和业务沟通能不排序就不排序，或将排序放到程序端去做。order by、group by、distinct这些语句较为耗费CPU，数据库的CPU资源是极其宝贵的。
- 包含了order by、group by、distinct这些査询的语句，where条件过滤出来的结果集请保持在1000行以内，否则SQL会很慢。



### 2.12 优化分页查询

#### 1、问题引出

即深分页的问题，竟可能往走索引上去靠，竟可能减少回表次数。

一般分页查询时，通过创建覆盖索引能够比较好地提高性能。一个常见又非常头疼的问题就是 limit 2000000,10，此时需要MySQL排序前2000010 记录，仅仅返回2000000-2000010的记录，其他记录丢弃，查询排序的代价非常大 。

```mysql
EXPLAIN SELECT *FROM student LIMIT 2008000,10;
```

#### 2、优化方式

优化方式一：

在索引上完成排序分页的操作，然后根据主键关联回原表查询所需要的其他列内容

此时在a中可以通过主键索引，快速定位到指定的数据，外层通过关联关系，并且驱动表的关联字段是主键也是有索引的，大大加快查询速度。

```mysql
EXPLAIN SELECT * FROM student t,(SELECT id FROM student ORDER BY id LIMIT 2808888,18) a WHERE t.id = a.id;
```

![image-20241106233424707](MySQL%E4%BC%98%E5%8C%96.assets/image-20241106233424707.png)



优化方式二

对于主键是自增的表，可以吧limit查询换成where条件，来查询某个位置的值

```mysql
EXPLAIN SELECT *FROM student WHERE id >2800000 LIMIT 10;
```

![image-20241106233644710](MySQL%E4%BC%98%E5%8C%96.assets/image-20241106233644710.png)



### 2.13 索引覆盖

#### 1、什么是索引覆盖

理解方式一:索引是高效找到行的一个方法，但是一般数据库也能使用索引找到一个列的数据，因此它不必读取整个行。毕竟索引叶子节点存储了它们索引的数据;当能通过读取索引就可以得到想要的数据，那就不需要读取行了。一个索引包含了满足查询结果的数据就叫做覆盖索引。
理解方式二:非聚簇复合索引的一种形式，它包括在査询里的SELECT、JOIN和WHERE子句用到的所有列(即建索引的字段正好是覆盖查询条件中所涉及的字段)。
简单说就是索引列+主键`包含 SELEIT 到 FROM之间查询的列 。

#### 2、示例

因为不需要回表了，如下sql都是可以走索引的，一切对是出于优化器对于性能的考虑，而不是固定的规则！

![1730909937353](MySQL%E4%BC%98%E5%8C%96.assets/1730909937353.jpg)

![1730910089455](MySQL%E4%BC%98%E5%8C%96.assets/1730910089455.jpg)

#### 3、利弊

利:

- 避免Innodb表进行索引的二次查询(回表)

Innodb是以聚集索引的顺序来存储的，对于Innodb来说，二级索引在叶子节点中所保存的是行的主键信息，如果是用二级索引查询数据，在查找到相应的键值后，还需通过主键进行二次查询才能获取我们真实所需要的数据。
在覆盖索引中，二级索引的键值中可以获取所要的数据，避免了对主键的二次査询 ，减少了IO操作 ，提升了查询效率。

- 可以把随机IO变成顺序IO加快查询效率

由于覆盖索引是按键值的顺序存储的，对于IO密集型的范围査找来说，对比随机从磁盘读取每一行的数据IO要少的多，因此利用覆盖索引在访问时也可以把磁盘的 随机读取的IO转变成索引查找的 顺序IO
由于覆盖索引可以减少树的搜索次数，显著提升查询性能，所以使用覆盖索引是一个常用的性能优化手段。

即回表需要拿着主键去找，可能位于不同的页上，是随机IO，而不会表就在二级索引的叶子节点上，都是挨着的，可以说是顺序IO。

弊：

索引字段的维护 总是有代价的。因此，在建立冗余索引来支持覆盖索引时就需要权衡考虑了。如查询的时候需要a、b、c三个字段，所以把abc建成联合索引这样是不多的，因为在建立和维护索引的时候是有开销的。



### 2.14 前缀索引

MySQL是支持前缀索引的，默认地，如果你创建索引的语句不指定前缀的长度，那么索引就会包含整个字符串，如下所示

```mysql
# 不使用前缀索引
alter table teacher add index index1(email);
# 使用前缀索引
alter table teacher add index index2(email(6));
```

上述两个语句对应的索引在数据结构和存储上如下所示：

- 不使用前缀索引：

即email整个字符串的索引结构

![image-20241110205110371](MySQL%E4%BC%98%E5%8C%96.assets/image-20241110205110371.png)

其执行顺序是这样的

1、从index1索引树找到满足索引值是’ **zhangssxyz@xxx.com** ’的这条记录，取得ID2的值；

2、到主键索引上查到主键值是ID2的行，判断email的值是正确的，将这行记录加入结果集；

3、取index1索引树上刚刚查到的位置的下一条记录，发现已经不满足email=' **zhangssxyz@xxx.com** ’的

条件了，循环结束。

这个过程中，只需要回主键索引取一次数据，所以系统认为只扫描了一行。



- 使用前缀索引

![image-20241110205148697](MySQL%E4%BC%98%E5%8C%96.assets/image-20241110205148697.png)

即email(6)索引结构，其查找值等于zhangssxyz@xxx.com的执行顺序如下所示

1、从index2索引树找到满足索引值是’zhangs’的记录（因为是前缀索引，所以要匹配前缀），找到的第一个是ID1；

2、到主键索引上查到主键值是ID1的行，判断出email的值不是’ **zhangssxyz@xxx.com** ’，这行记录丢弃

3、取index2上刚刚查到的位置的下一条记录，发现仍然是’zhangs’，取出ID2，再到ID索引上取整行然

后判断，这次值对了，将这行记录加入结果集；

4、重复上一步，直到在idxe2上取到的值不是’zhangs’时，循环结束。

也就是说使用前缀索引，定义好长度，就可以做到既节省空间，又不用额外增加太多的查询成本（如果区分度好，一次回表就可以查询出结果）。前面

已经讲过区分度，区分度越高越好。因为区分度越高，意味着重复的键值越少。



注意：使用前缀索引就用不上覆盖索引对查询性能的优化了，这也是你在选择是否使用前缀索引时需要考

虑的一个因素。因为前缀索引在一定条件下需要回表二次查询，而索引覆盖是只需要一次二级查询，不进行回表操作。



### 2.15 索引条件下推

#### 1、使用前后对比

索引下推（ICP, Index Condition Pushdown）是MySQL5.6中新特性，是一种在存储引擎层使用索引过滤数据的优化方式。允许 MySQL 在查询过程中将一部分查询条件推到索引扫描的阶段，而不是等到访问表的实际数据时才应用。工作原理是通过将查询条件推送到索引层面，而不是等到查询结果返回后再过滤，减少了不必要的行访问，从而提高查询效率。具体的步骤包括

- 如果没有ICP，存储引擎会遍历索引以及定位基表中的行，并将他们返回给MySQL服务器，由MySQL服务器评估Where后面的条件是否保留行。其通常包括如下几个步骤：
  - **索引扫描**:首先，存储引擎会使用索引（比如 B+ 树索引）扫描符合条件的记录。这个索引扫描会按照某种顺序遍历索引中的条目。
  - **定位数据行**：索引扫描完成，存储引擎会定位到基表（即实际存储数据的表）中的行。通常，如果索引包含了查询所需的所有列，存储引擎会直接通过索引返回数据，这种情况称为 **覆盖索引**（Covering Index）。如果索引不包含查询所需的所有列，存储引擎则会进行 **回表**（Lookup），通过索引中存储的主键或唯一键查找数据表中的完整记录（非聚簇索引），如果是聚簇索引，则直接可以从叶子节点中获取对应的数据。
  - **服务器层面执行过滤**：返回的记录还可能包含一些不符合 `WHERE` 子句条件的行。由于没有使用 ICP，过滤条件会在 MySQL 服务器 层面进行评估。也就是说，存储引擎并不会在返回数据之前应用这些 `WHERE` 条件。
- 启用ICP后，如果部分 WHERE 条件可以仅使用索引中的列进行筛选，则 MySQL 服务器会把这部分 WHERE 条件放到存储引擎筛选。然后，存储引擎通过使用索引条目来筛选数据，并且只有在满足这一条件时才从表中读取行。
  - 好处:ICP可以减少存储引擎必须访问基表的次数和MySQL服务器必须访问存储引擎的次数。
  - 但是，ICP的 加速效果 取决于在存储引擎内通过 ICP筛选 掉的数据的比例。

#### 2、过程分析

不使用ICP扫描过程：

storage层：只将满足index key条件的索引记录对应的整行记录取出，返回给server层

server 层：对返回的数据，使用后面的where条件过滤，直至返回最后一行。

![image-20241111001924769](MySQL%E4%BC%98%E5%8C%96.assets/image-20241111001924769.png)

![image-20241111001932411](MySQL%E4%BC%98%E5%8C%96.assets/image-20241111001932411.png)



使用ICP扫描过程：

storage层：首先将index key条件满足的索引记录区间确定，然后在索引上使用index filter进行过滤。将满足的index filter条件的索引记录才去回表取出整行记录返回server层。不满足index filter条件的索引记录丢弃，不回

表、也不会返回server层。

server 层：对返回的数据，使用table filter条件做最后的过滤

![image-20241111002009390](MySQL%E4%BC%98%E5%8C%96.assets/image-20241111002009390.png)

![image-20241111002017716](MySQL%E4%BC%98%E5%8C%96.assets/image-20241111002017716.png)



使用前，存储层多返回了需要被index filter过滤掉的整行记录使用ICP后，直接就去掉了不满足index filter条件的记录，省去了他们回表和传递到server层的成本。ICP的 加速效果 取决于在存储引擎内通过 ICP筛选 掉的数据的比例。



#### 3、ICP的开启/关闭

默认情况下启用索引条件下推。可以通过设置系统变量 optimizer_switch 控制:index condition pushdown

```mysql
#关闭索引下推
SET optimizer_switch='index_condition_pushdown=off';
#打开索引下推
SET optimizer_switch ='index_condition_pushdown=on';
```

当使用索引条件下推时，EXPLAIN语句输出结果中 Extra列内容显示为 Using index condition。



#### 4、示例

1、举例1

s1表有对key建立了索引

```mysql
EXPLAIN SELECT * FROM s1 where key > 'z' and key like '%a'
```

对于‘%a’无法使用上索引，对于前面的范围查询可以使用索引。

我们认为是首先根据key在对应的索引结构上根据大于‘z'进行查询，查询到的数据进行回表（聚簇索引到一级索引上对数据进行定位），然后再根据like ’%a‘条件进行数据的过滤。（并且在聚簇索引上定位的数据都是不连续的，属于磁盘随机IO，然后需要将这些数据页加载到内存中进行过滤处理）

实际上是在根据大于’z‘查询之后，开始like '%a'条件的判断（因为此时有key字段，并且是对key字段进行条件判断，是可行的，即下推条件），然后再去回表操作。



2、举例2

一般来说，对于索引下推，适用于联合索引。

```mysql
# 为字段a、b、c创建一个联合索引
# ……

# 执行sql
EXPLAIN SELECT * FROM table
WHERE a = '001'
and b like '%哈%'
and c like '%呱'
```

从上面的查询语句可以看出，只有a字段用上了联合索引，而b、c字段没有用上联合索引。

没有ICP的情况，首先根据条件a = '001'在索引上查出对应的数据，然后回表查询到对应的数据，然后将对应的数据加载到内存中，通过b like '%哈%'和c like '%呱'对数据进行过滤处理。

有ICP的情况，根据条件a = '001'在索引上查出对应的数据后，由于此时二级索引是联合索引，并且过滤的条件b like '%哈%'和c like '%呱'的字段b、c都是在叶子节点中的，因此可以直接过滤，之后再回表查询对应的数据。（降低筛选前的数据量，降低随机OI加载到内存的次数）

因此对于这种索引中有这个字段，但是这个索引字段又失效的情况，ICP就十分适用。并且可以通过EXPLAIN中的Extra中看出是否使用的索引下推，其中信息为Using index condition就是，如果Using index condition：Using where则说明where条件中存在失效的索引。



#### 5、ICP的使用条件

- 如果表访问的类型为 range、ref、eq_ref和ref_or_null 可以使用ICP
  - **`range`** 访问类型表示查询条件中使用了范围操作符（如 `BETWEEN`、`>`、`<`、`>=`、`<=`、`IN()` 等），并且能够利用索引的顺序访问数据。
  - **`ref`**：适用于非唯一索引的查询，通过索引查找某些列的值，可能返回多行。
  - **`eq_ref`**：适用于通过主键或唯一索引进行精确匹配的查询，通常只返回一行。
  - **`ref_or_null`**：适用于同时包含精确匹配和 `IS NULL` 条件的查询。
- ICP可以用于 InnoDB 和MyISAM 表，包括分区表 InnoDB和 MyISAM表
- 对于 InnoDB 表，ICP 仅用于 二级索引。ICP 的目标是减少全行读取次数，从而减少 I/O 操作。
- 当SQL使用覆盖索引时，不支持ICP。因为这种情况下使用ICP 不会减少 I/O。
- 相关子查询的条件不能使用ICP



### 2.16 普通索引和唯一索引

#### 1、查询过程

如果执行查询的语句是 select id from test where k=5

- 对于普通索引来说，查找到满足条件的第一个记录(5,500)后，需要查找下一个记录，直到碰到第一个不满足k=5条件的记录。
- 对于唯一索引来说，由于索引定义了唯一性，查找到第一个满足条件的记录后，就会停止继续检索。

这个不同带来的性能差距是微乎其微。

#### 2、更新过程

为了说明普通索引和唯一索引对更新语句性能的影响这个问题，介绍一下change buffer。

当需要更新一个数据页时，如果数据页在内存中就直接更新，而如果这个数据页还没有在内存中的话，

在不影响数据一致性的前提下， InooDB会将这些更新操作缓存在change buffer中 ，这样就不需要从磁

盘中读入这个数据页了。在下次查询需要访问这个数据页的时候，将数据页读入内存，然后执行change

buffer中与这个页有关的操作。通过这种方式就能保证这个数据逻辑的正确性。将change buffer中的操作应用到原数据页，得到最新结果的过程称为 merge 。除了 访问这个数据页 会触

发merge外，系统有 后台线程会定期 merge。在 数据库正常关闭（shutdown） 的过程中，也会执行merge

操作。如果能够将更新操作先记录在change buffer， 减少读磁盘 ，语句的执行速度会得到明显的提升。而且，

数据读入内存是需要占用 buffer pool 的，所以这种方式还能够 避免占用内存 ，提高内存利用率。

#### 3、change buffer使用场景

- 普通索引和唯一索引应该怎么选择？其实，这两类索引在查询能力上是没差别的，主要考虑的是对 更新性能 的影响。所以，建议你尽量选择普通索引 。
- 在实际使用中会发现， 普通索引 和 change buffer 的配合使用，对于 数据量大 的表的更新优化还是很明显的。
- 如果所有的更新后面，都马上 伴随着对这个记录的查询 ，那么你应该 关闭change buffer 。而在其他情况下，change buffer都能提升更新性能。
- 由于唯一索引用不上change buffer的优化机制，因此如果 业务可以接受 ，从性能角度出发建议优

先考虑非唯一索引。但是如果"业务可能无法确保"的情况下：

首先， 业务正确性优先 。我们的前提是“业务代码已经保证不会写入重复数据”的情况下，讨论性能

问题。如果业务不能保证，或者业务就是要求数据库来做约束，那么没得选，必须创建唯一索引。

这种情况下，本节的意义在于，如果碰上了大量插入数据慢、内存命中率低的时候，给你多提供一

个排查思路。

然后，在一些“ 归档库 ”的场景，你是可以考虑使用唯一索引的。比如，线上数据只需要保留半年，

然后历史数据保存在归档库。这时候，归档数据已经是确保没有唯一键冲突了。要提高归档效率，

可以考虑把表里面的唯一索引改成普通索引。



### 2.17 EXISTS和IN区分

索引是个前提，其实选择与否还是要看表的大小。你可以将选择的标准理解为 小表驱动大表 。在这种方式下效率是最高的。

如下所示：

```mysql
SELECT * FROM A WHERE CC IN(SELECT CC FROM B)
SELECT * FROM A WHERE EXISTS(SELECT CC FROM B WHERE B.CC=A.CC)
```

当A小于B时，用 EXISTS。因为 EXISTS的实现，相当于外表循环，实现的逻辑类似于：

```mysql
for i in A
	for j in B
		if j.cc == i.cc then 
```

当 B小于A时用 IN，因为实现的逻辑类似于:

```mysql
for i in B
	for j in A
		if j.cc == i.cc then ...
```

哪个表小就用哪个表来驱动，A表小就用 EXISTS，B 表小就用 IN。



### 2.18 Count（*）、Count（1）、Count（具体字段） 

> 参考文章：
>
> - https://blog.csdn.net/weixin_46200547/article/details/120020025
> - https://blog.csdn.net/qq_21103471/article/details/124706159
> - https://juejin.cn/post/6854573219089907720

对于Count（*）、Count（1）、Count（具体字段）都用于Mysql统计数据表的行数。

#### 1、执行结果的区别

- Count（*）：统计返回结果中的所有行，包括所有列的值，NULL 值也会被计算在内。
- Count（1）：统计的是每一行的数据，忽略所有列，用1代表代码行，而不是列的具体值。它实际上统计的是行的数量（因为是行的数量，所以NULL 值也会被计算在内）
- Count（具体字段）：统计只包括列名那一列，且字段列非NULL的行数，即会忽略列值为空（这里的空表示NULL值）

#### 2、执行效率

- 在开发中需要使用count聚合函数，统计数量的情况时，优先使用count（*），因为mysql本身对于`count(*)`做了特别的优化处理。

- 





#### 3、不同存储引擎的区别









### 2.19 SELECT(*)

在表查询中，建议明确字段，不要使用*作为查询的字段列表，推荐使用SELECT<字段列表>查询。原因：*

① MySQL 在解析的过程中，会通过 査询数据字典 将"*"按序转换成所有列名，这会大大的耗费资源和时间。

② 无法使用覆盖索引，竟可能对添加索引的字段进行查询，可以通过索引覆盖来优化效率。



### 2.20 多使用COMMIT

只要有可能，在程序中尽量多使用 COMMIT，这样程序的性能得到提高，需求也会因为 COMMIT 所释放的资源而减少。通过频繁地执行 `COMMIT` 操作来提交事务。这样做的目的是提高性能，并减少因为事务未提交而占用的系统资源

COMMIT 所释放的资源:

- 回滚段上用于恢复数据的信息
- 事务持有的锁，被程序语句获得的锁
- redo/undo log buffer 中的空间，只有当事务提交时，这些变更才会被永久写入磁盘。如果不及时执行 `COMMIT`，这些日志和缓存就会一直占用内存空间。随着事务越来越多，内存占用逐渐增加，最终可能导致内存压力增大或出现性能瓶颈。
- 管理上述3种资源中的内部花费



### 2.21 使用Limit 1进行优化

对于会进行全表扫描的 SQL 语句，如果你可以确定结果集只有一条，那么加上 LIMIT 1 的时候，当找

到一条结果的时候就不会继续扫描了，这样会加快查询速度。

如果数据表已经对字段建立了唯一索引，那么可以通过索引进行查询，不会全表扫描的话，就不需要加

上 LIMIT 1 了。



## 扩展

### 1、生成数据sql

```mysql
# 建表
CREATE TABLE `a` (
`id` INT(11) NOT NULL AUTO_INCREMENT,
`a_name` VARCHAR(30) DEFAULT NULL,
`a_age` INT NULL ,
PRIMARY KEY (`id`)
) ENGINE=INNODB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;

CREATE TABLE `b` (
`id` INT(11) NOT NULL AUTO_INCREMENT,
`b_name` VARCHAR(30) DEFAULT NULL,
`b_age` INT NULL ,
PRIMARY KEY (`id`)
) ENGINE=INNODB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;


# 设置参数，允许函数创建
set global log_bin_trust_function_creators= 1; # 不加global只是当前窗口有效。


# 函数
## 随机创建字符串函数
DELIMITER //		# DELIMITER指定结束符
CREATE FUNCTION rand_string(n INT) RETURNS VARCHAR(255)
BEGIN
DECLARE chars_str VARCHAR(100) DEFAULT
'abcdefghijklmnopqrstuvwxyzABCDEFJHIJKLMNOPQRSTUVWXYZ';
DECLARE return_str VARCHAR(255) DEFAULT '';
DECLARE i INT DEFAULT 0;
WHILE i < n DO
SET return_str =CONCAT(return_str,SUBSTRING(chars_str,FLOOR(1+RAND()*52),1));
SET i = i + 1;
END WHILE;
RETURN return_str;
END //
DELIMITER ;

#删除
#drop function rand_string;


## 随机产生多少到多少的编号
DELIMITER //
CREATE FUNCTION rand_num (from_num INT ,to_num INT) RETURNS INT(11)
BEGIN
DECLARE i INT DEFAULT 0;
SET i = FLOOR(from_num +RAND()*(to_num - from_num+1)) ;
RETURN i;
END //
DELIMITER ;
#假如要删除
#drop function rand_num;


# 存储过程
## 插入数据
DELIMITER //
CREATE PROCEDURE insert_data( START INT , max_num INT )
BEGIN
DECLARE i INT DEFAULT 0;
SET autocommit = 0; #设置手动提交事务
REPEAT #循环
SET i = i + 1; #赋值
INSERT INTO a (id, a_name , a_age ) VALUES
((START+i),rand_string(6),rand_num(1,50));
UNTIL i = max_num
END REPEAT;
COMMIT; #提交事务
END //
DELIMITER ;

#假如要删除
drop PROCEDURE insert_data;


# 调用存储过程
#执行存储过程，往表添加1万条数据
CALL insert_data(1, 10000);

```



### 2、基表和索引

#### 2.1  关于基表

基表，指的是原始数据表，也就是实际存储数据的表。在数据库系统中，“基表”是存储数据的实际物理表，它包含表的所有数据行和完整的列内容。当我们对数据库进行查询时，很多情况下是通过索引来快速定位需要的行，但索引并不包含所有的数据。因此，数据库在使用索引之后，通常还需要访问基表以获得完整的数据。

#### 2.2 索引和基表的关系

索引可以理解为表数据的一种“快速访问路径”，它是一种辅助结构，用于加速数据的查找和排序。索引会存储一部分列的值和与基表中记录的“指针”，以便通过索引快速定位基表中的行。当我们通过索引查找到目标位置后，数据库系统就会使用索引中存储的指针访问基表，以读取完整的数据行。这个过程叫做**回表**操作（回到**基表**或**主键索引**（即一级索引）中查找完整数据的过程，就叫做**回表**）。

#### 2.3 索引访问基表的过程（非聚簇索引）

假设有一个表 `users`，包含字段 `id`, `name`, `age`，我们在 `id` 列上创建了一个 B+ 树索引。当查询使用索引时，存储引擎会按照以下步骤来完成整个查找过程：

1. **使用索引查找**：存储引擎先通过 B+ 树的索引结构来查找符合条件的 `id`。
2. **索引中找到指针**：B+ 树的叶子节点中保存了 `id` 的值，并包含指向基表中完整行的“指针”，这个指针指向存储在基表中的实际数据行的位置。
3. **回表读取完整行**：根据指针，存储引擎定位到基表中的目标行，并读取该行中所有字段的数据，比如 `name` 和 `age`。

#### 2.4 innodb中的存储方式

在 InnoDB 存储引擎中，确实不存在单独的“基表”概念，因为**InnoDB 将所有表数据都存储在主键索引（即一级索引）的 B+ 树的叶子节点上**。这意味着 InnoDB 的主键索引不仅存储索引信息，还存储完整的数据行，因此**主键索引的叶子节点既是索引又是存储数据的实际位置**。

这种设计使得 InnoDB 的主键索引称为**聚簇索引（Clustered Index）**。聚簇索引是一种特殊的索引结构，其叶子节点包含了表的完整数据行，因此访问主键索引的叶子节点等价于访问“基表”。



### 3、聚簇索引和非聚簇索引的全表扫描

聚簇索引（clustered index）和非聚簇索引（non-clustered index）在全表扫描时有着显著的不同。这些区别涉及数据的物理存储方式、访问的效率以及使用场景。为了更好地理解，我们可以从以下几个方面进行详细分析：

#### 3.1 聚簇索引和非聚簇索引的定义

- **聚簇索引**：聚簇索引是一种索引结构，其中数据表的物理顺序与索引的顺序相同。一个表只能有一个聚簇索引，因为数据行本身按索引的顺序存储。聚簇索引通常在主键上创建，因此主键索引实际上就是聚簇索引。扫描聚簇索引时，实际上是直接扫描整张表的数据，因为索引和数据是“合而为一”的。
- **非聚簇索引**：非聚簇索引是指索引和数据是分开的。索引中包含指向数据表行的指针（通常是行号或主键值）。一个表可以有多个非聚簇索引，每个索引指向表中的数据，但不改变数据的物理存储顺序。扫描非聚簇索引时，读取的是索引本身，而获取实际数据时可能需要进行额外的回表操作。

#### 3.2 **全表扫描的概念**

全表扫描是指读取整个表的所有数据，无论是通过索引扫描还是直接扫描数据页。当我们说“全表扫描”时，通常是指以下两种情况：

- **通过索引扫描整个索引（索引全表扫描）**：根据索引顺序逐条读取索引项，然后通过索引查找到数据行。
- **数据页扫描（表扫描）**：直接读取数据表的所有数据页，而不使用索引。



#### 3.3 **聚簇索引的全表扫描**

扫描方式

在一个具有聚簇索引的表中，聚簇索引本身就是数据表，数据以索引键的顺序存储。因此，当进行全表扫描时，直接从头到尾读取聚簇索引，即扫描整个表的实际数据页。

特点和效率

- **顺序访问**：因为数据是按照索引键的顺序存储，全表扫描时读取数据是顺序的，这在磁盘 I/O 层面上更为高效。
- **无需回表**：读取数据时不需要额外的步骤来访问数据行，因为聚簇索引中已经包含了所有数据。
- **较慢的更新**：由于数据物理顺序与索引一致，插入、更新和删除操作会影响数据的物理存储，导致页分裂和数据重排，从而影响性能。



#### 3.4 **非聚簇索引的全表扫描**

扫描方式

在一个具有非聚簇索引的表中，索引结构和数据是分开的。全表扫描非聚簇索引时，会读取整个索引结构，并可能需要回表（即访问数据页）来获取完整的行数据。

特点和效率

- **非顺序访问**：非聚簇索引保存的是指向数据行的引用（如行 ID 或主键值），所以扫描索引并获取数据行时，数据访问可能是随机的，这会带来额外的 I/O 开销。
- **回表开销**：如果查询中涉及的列不在非聚簇索引中，系统需要根据索引中的指针回到数据表中获取完整的数据。这种回表操作会显著增加读取的随机 I/O。
- **灵活性**：由于非聚簇索引不限制数据的物理存储顺序，一个表可以有多个非聚簇索引，允许更灵活的查询优化。





### 待

- EXPLAIN相关参数学习
- not in 和in的走索引情况：具体情况具体分析
- join底层原理中 hash join的实现了解
- 2.18 Count（*）、Count（1）、Count（具体字段）
