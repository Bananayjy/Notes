# MySQL索引

## 一、索引

### 1.1 介绍

用**一句话概括**，索引是一种用于快速查询和检索数据的数据结构，其本质可以看成是一种排序好的数据结构。

用**比喻说明**，索引就相当于一本书的目录，在查字典的时候，如果没有目录，那我们就只能一页一页的去找我们需要查的那个字，速度很慢。如果有目录了，我们只需要先去目录里查找字的位置，然后直接翻到那一页就行了。

索引是一个单独的、存储在磁盘上的数据库结构，它们包含着对数据表里所有记录的引用指针。使用索引用于快速找出在某个或多个列中有一特定值的行，所有MySQL列类型都可以被索引，对相关列使用索引是提高查询操作速度的最佳途径。

实际上，索引也是一张“表”，该表保存了主键与索引字段，并指向实体表的记录，虽然索引大大提高了查询速度，同时却会降低更新表的速度，如对表进行INSERT、UPDATE和DELETE。因为更新表时，MySQL不仅要保存数据，还要保存一下索引文件，建立索引会占用磁盘空间的索引文件。说白了索引就是用来提高速度的，但是就需要维护索引造成资源的浪费，所以合理的创建索引是必要的。

### 1.2 优缺点

**优点**

- 使用索引可以大大加快数据的检索速度（大大减少检索的数据量）, 减少 IO 次数，这也是创建索引的最主要的原因。

**缺点**

- 创建索引和维护索引需要耗费许多时间。当对表中的数据进行增删改的时候，如果数据有索引，那么索引也需要动态的修改，会降低 SQL 执行效率。
- 索引需要使用物理文件存储，也会耗费一定空间。

### 1.3 MySQL 8.0 索引新特性

#### 1.降序索引

降序索引以降序存储键值。虽然在语法上，从MySQL4版本开始就已经支持降序索引的语法了，但实际上该DESC定义是被忽略的（即实际创建的还是升序索引，在遍历降序索引的时候，通过反向扫描的方式进行），直到MySQL8.x版本才开始真正支持降序索引(仅限于InnoDB存储引擎)。
MySQL在8.0版本之前创建的仍然是升序索引，使用时进行反向扫描，这大大降低了数据库的效率。在某些场景下，降序索引意义重大。例如，如果一个查询，需要对多个列进行排序（有的列需要升序，有的列需要降序），且顺序要求不一致，那么使用降序索引将会避免数据库使用额外的文件排序（Using filesort）操作，从而提高性能（使用降序索引可以帮助数据库在排序时更加高效）。

降序索引是指在某个列上创建的索引，使得该列的数据按照降序排列。当查询时，如果使用了这种降序索引，数据库可以直接利用索引中的顺序来返回结果，而不需要额外的文件排序操作（即“Using filesort”），从而提高查询性能。

举个例子，假设你有一个包含姓名、年龄和收入的表格，你想按照年龄升序、收入降序进行排序。如果在收入这一列上创建了降序索引，数据库在执行查询时就能更快地找到符合条件的记录，而不需要先将所有记录取出来再进行排序，从而减少了资源消耗和执行时间。

举例:分别在MySOL5.7版本和MySOL8.0版本中创建数据表ts1，结果如下:

```mysql
CREATE TABLE ts1(a int,b int,index idx_a_b(a,b desc));
```

在MySQL 5.7版本中查看数据表ts1的结构，结果如下，索引仍然是默认升序

![image-20241030222412520](MySQL%E7%B4%A2%E5%BC%95.assets/image-20241030222412520.png)

在MySQL 8.0版本中查看数据表ts1的结构，，索引已经是降序了

![image-20241030222507466](MySQL%E7%B4%A2%E5%BC%95.assets/image-20241030222507466.png)

在MySQL 5.7版本和MySQL 8.0版本的数据表ts1中插入800条随机数据，执行语句如下：

```mysql
DELIMITER //
CREATE PROCEDURE ts_insert()
BEGIN
    DECLARE i INT DEFAULT 1;
    WHILE i < 800
    DO
        insert into ts1 select rand()*80000,rand()*80000;
        SET i = i + 1;
    END WHILE;
    commit;
END //
DELIMITER ;

#调用
CALL ts_insert();
```

在MySQL 5.7版本中查看数据表ts1的执行计划，结果可以看出，执行计划中扫描数为799，而且使用了Using filesort，Using filesort是MySQL中一种速度比较慢的外部排序，能避免是最好的。多数情况下，管理员

可以通过优化索引来尽量避免出现Using filesort，从而提高数据库执行速度。

```mysql
EXPLAIN SELECT * FROM ts1 ORDER BY a,b DESC LIMIT 5;
```

![image-20241030223100810](MySQL%E7%B4%A2%E5%BC%95.assets/image-20241030223100810.png)

在MySQL 8.0版本中查看数据表ts1的执行计划。从结果可以看出，执行计划中扫描数为5，而且没有使用

Using filesort。

![image-20241030223130703](MySQL%E7%B4%A2%E5%BC%95.assets/image-20241030223130703.png)

注意：降序索引只对查询中特定的排序顺序有效，如果使用不当，反而查询效率更低。例如，上述

查询排序条件改为order by a desc, b desc（没有匹配索引的顺序），MySQL 5.7的执行计划要明显好于MySQL 8.0。

```mysql
EXPLAIN SELECT * FROM ts1 ORDER BY a DESC,b DESC LIMIT 5;
```

![image-20241030223233151](MySQL%E7%B4%A2%E5%BC%95.assets/image-20241030223233151.png)

在MySQL 8.0版本中查看数据表ts1的执行计划。从结果可以看出，修改后MySQL 5.7的执行计划要明显好于MySQL 8.0

#### 2.隐藏索引

在MySQL 5.7版本及之前，只能通过显式的方式删除索引。此时，如果发现删除索引后出现错误，又只能通过显式创建索引的方式将删除的索引创建回来（如删除一个唯一索引后，若后续的插入操作会导致重复数据，与业务不符合）。如果数据表中的数据量非常大，或者数据表本身比较大，这种操作就会消耗系统过多的资源，操作成本非常高。

从MySQL 8.x开始支持 隐藏索引（invisible indexes） ，只需要将待删除的索引设置为隐藏索引，使查询优化器不再使用这个索引（即使使用force index（强制使用索引），优化器也不会使用该索引），确认将索引设置为隐藏索引后系统不受任何响应，就可以彻底删除索引。 这种通过先将索引设置为隐藏索引，再删除索引的方式就是软删除 。

如果需要验证某个索引删除之后的查询性能影响，就可以暂时应参该索引。

注意：主键不能被设置为隐藏索引。当表中没有显式主键时，表中第一个唯一非空索引会成为隐式主键，也不能设置为隐藏索引。

索引默认是可见的，在使用CREATETABLE，CREATEINDEX或者ALTER TABLE等语句时可以通过 VISIBLE 或者INVISIBLE 关键词设置索引的可见性，具体的做法如下所示

- **创建表时直接创建** 在MySQL中创建隐藏索引通过SQL语句INVISIBLE来实现，其语法形式如下

上述语句比普通索引多了一个关键字INVISIBLE，用来标记索引为不可见索引。

```mysql
CREATE TABLE tablename(
    propname1 type1[CONSTRAINT1],
    propname2 type2[CONSTRAINT2],
    ……
    propnamen typen,
    INDEX [indexname](propname1 [(length)]) INVISIBLE
);
```

- **在已经存在的表上创建**

可以为已经存在的表设置隐藏索引，其语法形式如下：

```mysql
CREATE INDEX indexname
ON tablename(propname[(length)]) INVISIBLE;
```

- **通过ALTER TABLE语句创建**

语法形式如下：

```mysql
ALTER TABLE tablename
ADD INDEX indexname (propname [(length)]) INVISIBLE;
```

- **切换索引可见状态** 

已存在的索引可通过如下语句切换可见状态

```mysql
ALTER TABLE tablename ALTER INDEX index_name INVISIBLE; #切换成隐藏索引
ALTER TABLE tablename ALTER INDEX index_name VISIBLE; #切换成非隐藏索引
```

注意：当索引被隐藏时，它的内容仍然是和正常索引一样实时更新的。如果一个索引需要长期被隐藏，那么可以将其删除，因为索引的存在会影响插入、更新和删除的性能。通过设置隐藏索引的可见性可以查看索引对调优的帮助。

- **使隐藏索引对查询优化器可见**

在MySQL 8.x版本中，为索引提供了一种新的测试方式，可以通过查询优化器的一个开（use_invisible_indexes）来打开某个设置，使隐藏索引对查询优化器可见。如果 use_invisible_indexes设置为off(默认)，优化器会忽略隐藏索引。如果设置为on，即使隐藏索引不可见，优化器在生成执行计划时仍会考虑使用隐藏索引。

（1）在MySQL命令行执行如下命令查看查询优化器的开关设置。

```
mysql> select @@optimizer_switch \G
```

在输出的结果信息中找到如下属性配置。

```
use_invisible_indexes=off
```

此属性配置值为off，说明隐藏索引默认对查询优化器不可见。

（2）使隐藏索引对查询优化器可见，需要在MySQL命令行执行如下命令：

```mysql
mysql> set session optimizer_switch="use_invisible_indexes=on";
Query OK, 0 rows affected (0.00 sec)
```

SQL语句执行成功，再次查看查询优化器的开关设置

```mysql
mysql> select @@optimizer_switch \G
*************************** 1. row ***************************
@@optimizer_switch:
index_merge=on,index_merge_union=on,index_merge_sort_union=on,index_merge_
intersection=on,engine_condition_pushdown=on,index_condition_pushdown=on,mrr=on,mrr_co
st_based=on,block_nested_loop=on,batched_key_access=off,materialization=on,semijoin=on
,loosescan=on,firstmatch=on,duplicateweedout=on,subquery_materialization_cost_based=on
,use_index_extensions=on,condition_fanout_filter=on,derived_merge=on,use_invisible_ind
exes=on,skip_scan=on,hash_join=on
1 row in set (0.00 sec)
```

此时，在输出结果中可以看到如下属性配置。

```mysql
use_invisible_indexes=on
```

use_invisible_indexes属性的值为on，说明此时隐藏索引对查询优化器可见。

（3）使用EXPLAIN查看以字段invisible_column作为查询条件时的索引使用情况。

```mysql
explain select * from classes where cname = '高一2班';
```

查询优化器会使用隐藏索引来查询数据。

（4）如果需要使隐藏索引对查询优化器不可见，则只需要执行如下命令即可。

```mysql
mysql> set session optimizer_switch="use_invisible_indexes=off";
Query OK, 0 rows affected (0.00 sec)
```

再次查看查询优化器的开关设置。

```mysql
mysql> select @@optimizer_switch \G
```

此时，use_invisible_indexes属性的值已经被设置为“off”。



#### 3.函数索引

从 MySQL 8.0.13 版本开始支持在索引中使用函数或者表达式的值，也就是在索引中可以包含函数或者表达式。



## 二、索引分类

### 2.1 按照功能分类

按照功能分类，可以将索引分为主键索引、唯一索引、普通索引、全文索引、组合索引以及空间索引：

- 主键索引（Primary Key Index）

  列值唯一，且不可以为NULL值。

  由于InnoDB是基于聚簇索引存储，因此它需要一个主键标识符来组织数据。如果建表的时候没有显示地指定主键，则会使用第一非空的唯一索引（注：唯一索引是可以有null值，但是其他非null的值必须唯一，不重复，这里需要满足两个条件一个是非空，即不为null，另一个是唯一索引即值唯一，正是主键索引的特点）作为主键，如果表中没有主键也没有唯一的非空索引列，则会创建一个6字节的自增隐藏主键，即row_id作为主键，该隐藏注解无法被用户直接访问。

- 唯一索引（Unique Index）

  索引列的值必须唯一，但允许有空值（null值）。若是组合索引，则列值的组合必须唯一。主键索引是一种特殊的唯一索引，不允许有空值。

- 普通索引（Normal/Non-Unique Index）

  基本索引类型，允许在定义索引的列中插入重复值和空值。

- 全文索引（Fulltext Index）

  对文本的内容进行分词后，进行搜索。目前只有 `CHAR`、`VARCHAR` ，`TEXT` 列上可以创建全文索引，允许在这些索引列中插入重复值和空值。一般不会使用，效率较低，通常使用搜索引擎如 ElasticSearch等搜索引擎代替。

- 组合索引/联合索引（Composite Index）

  组合索引指在表的多个字段组成的索引，只有在查询条件中使用了这些字段的左边字段时，索引才会被使用（即遵循最左前缀集合）。专门用于组合搜索，其效率大于索引合并（查询时能够使用多个单列索引（而不是组合索引）的组合来优化查询）

- 空间索引（SPATIAL）

  空间索引是对空间数据类型的字段建立的索引，MySQL中的空间数据类型有4种，分别是GEOMETRY、POINT、LINESTRING和POLYGON。MySQL使用SPATIAL关键字进行扩展，使得能够用于创建正规索引类似的语法创建空间索引。创建空间索引的列必须声明为NOT NULL。目前只有MyISAM存储引擎支持空间检索，而且索引的字段不能为空值

### 2.2 按照物理实现分类

按照物理实现分类，可以将索引分为聚簇索引和非聚簇索引两种：

- 聚簇索引

  聚簇索引将数据行的物理顺序与索引的顺序相同，即数据存储本身就是按照索引的顺序排列。这意味着一个表只能有一个聚簇索引，因为数据行只能按照一种顺序存储。在InnoDB存储引擎中，主键默认是聚簇索引。如果没有显式定义主键，InnoDB会选择一个唯一非空索引作为聚簇索引。索引结构和数据一起存放的索引，InnoDB 中的主键索引就属于聚簇索引

- 非聚簇索引

  非聚簇索引创建一个单独的索引结构，该结构存储索引列的值和指向实际数据行的指针（通常是行ID，即主键id）。数据行的物理顺序与索引顺序无关。一个表可以有多个非聚簇索引，因为数据行的存储顺序不受影响。非聚簇索引在查找时需要访问两次：第一次查找索引，第二次通过指针访问数据行（即回表，去聚簇索引上进行查找）。对于某些查询（尤其是只需访问非索引列的查询，需要回表，无法索引覆盖），可能效率较低。索引结构和数据分开存放的索引，二级索引(辅助索引)就属于非聚簇索引。MySQL 的 MyISAM 引擎，不管主键还是非主键，使用的都是非聚簇索引。

### 2.3 按照作用字段个数分类

按照物理实现分类，可以将索引分为单列索引和联合/组合索引两种：

- 单列索引

  一个列作为一个索引。

- 组合索引

  多个列构成一个索引。

### 2.4 按照数据结构分类

- BTree 索引：MySQL 里默认和最常用的索引类型。只有叶子节点存储 value，非叶子节点只有指针和 key。存储引擎 MyISAM 和 InnoDB 实现 BTree 索引都是使用 B+Tree，但二者实现方式不一样（前面已经介绍了）。
- 哈希索引：类似键值对的形式，一次即可定位。
- RTree 索引：一般不会使用，仅支持 geometry 数据类型，优势在于范围查找，效率较低，通常使用搜索引擎如 ElasticSearch 代替。
- 全文索引：对文本的内容进行分词，进行搜索。目前只有 `CHAR`、`VARCHAR` ，`TEXT` 列上可以创建全文索引。一般不会使用，效率较低，通常使用搜索引擎如 ElasticSearch 代替。

对于不同存储引擎支持的索引类型不一样，如下所示

- InnoDB： 支持 B-tree、Full-text 等索引，不支持 Hash索引；

- MyISAM ： 支持 B-tree、Full-text 等索引，不支持 Hash 索引； 

- Memory ： 支持 B-tree、Hash 等索引，不支持 Full-text 索引；

- NDB ： 支持 Hash 索引，不支持 B-tree、Full-text 等索引； 

- Archive ： 不支持 B-tree、Hash、Full-text 等索引；



## 三、索引操作

在创建表的定义语句`CREATE TABLE`中指定索引列，使用`ALTER TABLE`语句在存在的表上创建索引，或者使用`CREATE INDEX`语句在已存在的表上添加索引。

### 3.1 创建表时创建索引

#### 1. 隐式方式创建索引（约束）

使用CREATE TABLE创建表时，除了可以定义列的数据类型外，还可以定义主键约束、外键约束或者唯一性约束，而不论创建哪种约束，在定义约束的同时相当于在指定列上创建了一个索引。

示例：

```mysql
CREATE TABLE dept(
dept_id INT PRIMARY KEY AUTO_INCREMENT,	# 主键约束创建唯一索引
dept_name VARCHAR( 20 )
);

CREATE TABLE emp(
emp_id INT PRIMARY KEY AUTO_INCREMENT,	# 主键约束创建唯一索引
emp_name VARCHAR( 20 ) UNIQUE,	# 唯一约束创建唯一索引（字段名即为索引名称）
dept_id INT,
CONSTRAINT emp_dept_id_fk FOREIGN KEY(dept_id) REFERENCES dept(dept_id)	# 外键约束创建普通索引
);
```

#### 2.显示方式创建索引

##### （1）显示声明语句：

```mysql
CREATE TABLE table_name [col_name data_type]
[UNIQUE | FULLTEXT | SPATIAL][INDEX |KEY][index_name] (col_name [length]) [ASC | DESC]
```

参数说明：

- UNIQUE、FULLTEXT和SPATIAL为可选参数，分别表示唯一索引、全文索引和空间索引；
- INDEX与KEY为同义词，两者的作用相同，用来指定创建索引；
- index_name指定索引的名称，为可选参数，如果不指定，那么MySQL默认col_name为索引名；
- col_name为需要创建索引的字段列，该列必须从数据表中定义的多个列中选择；
- length为可选参数，表示索引的长度，只有字符串类型的字段才能指定索引长度；
- ASC或DESC指定升序或者降序的索引值存储。

##### （2）查看索引

除了通过UI的方式查看创建的索引外，我们还可以通过如下命令的方式查看

```mysql
# 方式1
show  create table [tableName] \G
# 方式2
show index from [tableName];
```

##### （3）创建普通索引

```mysql
CREATE TABLE book (
    book_id INT ,
    book_name VARCHAR (100) ,
    AUTHORS VARCHAR (100) ,
    info VARCHAR(100) ,
    COMMENT VARCHAR (100) ,
    year_publication YEAR,
    #声明索引
	INDEX idx_bname (book_name));
```

##### （5）主键索引

主键可以直接在列定义中定义，也可以通过表约束的方式定义

设定为主键后数据库会自动建立索引（innodb为聚簇索引）

方式一

```mysql
CREATE TABLE emp (
    emp_id INT NOT NULL PRIMARY KEY,  -- 在列定义中直接指定主键
    emp_name VARCHAR(20) UNIQUE,
    dept_id INT
);
```

方式二

```mysql
CREATE TABLE emp (
    emp_id INT NOT NULL,
    emp_name VARCHAR(20) UNIQUE,
    dept_id INT,
    PRIMARY KEY (emp_id)  -- 使用表约束定义主键
);
```

如果需要删除主键索引，可以使用如下命令

```mysql
ALTER TABLE [tableName] drop PRIMARY KEY ;
```

修改主键索引：必须先删除掉(drop)原索引，再新建(add)索引

##### （6）组合/联合索引

```mysql
# 创建唯一索引
CREATE TABLE book (
    book_id INT ,
    book_name VARCHAR (100) ,
    author VARCHAR (100) ,
    #声明索引
	INDEX union_key_ba (book_name,author))
;
```

##### （7）全文索引

方式一

在表中的info字段上建立全文索引

```mysql
CREATE TABLE test4(
    id INT NOT NULL,
    name CHAR(30) NOT NULL,
    age INT NOT NULL,
    info VARCHAR(255),
    FULLTEXT INDEX futxt_idx_info(info)
) ENGINE=MyISAM;	#在MySQL5.7及之后版本中可以不指定最后的ENGINE了，因为在此版本中InnoDB支持全文索引。
```

方式二

创建了一个给title和body字段添加全文索引的表（省略索引名，默认名称的格式一般为 `table_name_column_name1_column_name2`）。

```mysql
CREATE TABLE articles (
    id INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR (200),
    body TEXT,
    FULLTEXT index (title, body)
) ENGINE = INNODB ;
```

方式三

```mysql
CREATE TABLE `papers` (
    `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
    `title` varchar(200) DEFAULT NULL,
    `content` text,
    PRIMARY KEY (`id`),
    FULLTEXT KEY `title` (`title`,`content`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;
```



全文索引检索的方式不同于like查询

```
SELECT * FROM papers WHERE content LIKE ‘%查询字符串%’;
```

而是通过match+against方式进行查询

```
SELECT * FROM papers WHERE MATCH(title,content) AGAINST (‘查询字符串’);
```

注意：

1. 使用全文索引前，搞清楚版本支持情况；
2. 全文索引比 like + % 快 N 倍，但是可能存在精度问题；
3. 如果需要全文索引的是大量数据，建议先添加数据，再创建索引（避免建立索引结构后，对数据修改后，频繁修改索引结构）。

##### （8）空间索引

空间索引创建中，要求空间类型的字段必须为 非空 。

创建表test5，在空间类型为GEOMETRY的字段上创建空间索引，SQL语句如下：

```mysql
CREATE TABLE test5(
    geo GEOMETRY NOT NULL,
    SPATIAL INDEX spa_idx_geo(geo)
) ENGINE=MyISAM;
```



### 3.2 在已经存在的表上创建索引

在已经存在的表中创建索引可以使用ALTER TABLE语句或者CREATE INDEX语句。

#### 1.使用ALTER TABLE语句创建索引

```mysql
ALTER TABLE table_name ADD [UNIQUE | FULLTEXT | SPATIAL] [INDEX | KEY]
[index_name] (col_name[length],...) [ASC | DESC]
```

#### 2.使用CREATE INDEX创建索引

```mysql
CREATE [UNIQUE | FULLTEXT | SPATIAL] INDEX index_name
ON table_name (col_name[length],...) [ASC | DESC]
```



### 3.3 删除索引

#### 1.使用ALTER TABLE删除索引

```mysql
ALTER TABLE table_name DROP INDEX index_name;
```

#### 2.使用DROP INDEX语句删除索引

```mysql
DROP INDEX index_name ON table_name;
```

注意：删除表中的列时，如果要删除的列为索引的组成部分，则该列也会从索引中删除。如果组成索引的所有列都被删除，则整个索引将被删除。



## 四、索引设计原则

### 4.1 索引适用情况

#### 1、字段的数值有唯一性限制/约束

索引本身可以起到约束的作用（唯一索引：优化查询性能，确保唯一性；唯一约束：确保表中的数据唯一，不重复），如唯一索引、主键索引都是可以起到唯一性约束的，因此在数据表中如果为有唯一性限制的字段创建唯一性索引或者主键索引，除了保证数据唯一的情况下，还可以更快速地通过该索引来确定某条记录。

alibaba规范中提到：业务上具有唯一特性的字段，即是是组合字段也必须建成唯一索引。即是建立唯一索引后，在维护过程中可能会影响insert速度，但是这个速度损耗可以忽略，但其塔高查找速度是很明显的（如在B+树上搜索的时候只需要定位一条即可，不用去前后查找相同数据，因为其是唯一的）



#### 2、频繁作为Where查询条件的字段

某个字段在SELECT语句的 WHERE 条件中经常被使用到，那么就需要给这个字段创建索引了。尤其是在数据量大的情况下，创建普通索引就可以大幅提升数据查询的效率。



#### 3、使用Group by 和 order by的列

索引会让数据按照某种顺序进行存储或检索，因此当使用 GROUP BY 对数据进行分组查询（加了索引，相同的数据都在一块），或者使用 ORDER BY 对数据进行排序的时候（加了索引数据都是有序的），就需要对分组或者排序的字段进行索引 。如果待排序的列有多个，那么可以在这些列上建立组合索引 。

特殊例子：

当同时存在group by 和order by的情况（以下sql需要设置sql_mode，去掉ONLY_FULL_GROUP_BY），如下

```mysql
select student_id, count(*) from table
group by student_id
order by create_time desc
limit 100
```

创建索引的方式

① 分别为student_id 和 create_time  创建索引

```mysql
alter table table add index idx_student_id(student_id)
alter table table add index idx_create_time(create_time)
```

实际使用的是student_id对应的索引，因为执行顺序是先group by再order by。

② 为student_id 和 create_time 创建联合索引

创建方式一

```mysql
 alter table student_info
 add index idx_sid_cre_time(student_id, create_time desc)
 # desc 在8.0有效，5.7版本无效，可见一种的mysql新特性
```

实际使用的是该联合索引，性能相较于①更快

创建方式二：顺序修改

```mysql
 alter table student_info
 add index idx_sid_cre_time(create_time desc, student_id)
```

如果在方式一的前提下，又加了一个方式二的联合索引，使用的还是方式一的，因为select执行顺序是先group by再order by。并且如果只有方式二索引，和①中的索引条件下，会优先使用idx_student_id索引，还是和执行顺序有关，先执行group by student_id的时候，去看有没有对应的索引，即找到了idx_student_id索引。

综上，如果既有group by 和order by，建议场景联合索引，把group by的字段写在联合索引前，order by的字段写在联合索引后，其效率是以最高的。



#### 4、UPDATE、DELETE的Where条件列

对数据按照某个条件进行查询后再进行 UPDATE 或 DELETE 的操作，如果对 WHERE 字段创建了索引，就

能大幅提升效率。原理是因为我们需要先根据 WHERE 条件列检索出来这条记录，然后再对它进行更新或

删除。如果进行更新的时候，更新的字段是非索引字段，提升的效率会更明显，这是因为非索引字段更

新不需要对索引进行维护。



#### 5、为DISTINCT字段创建索引

有时候我们需要对某个字段进行去重，使用 DISTINCT，那么对这个字段创建索引，也会提升查询效率。因

为索引会对数据按照某种顺序进行排序，所以在去重的时候也会快很多



#### 6、多表join连接操作

- 连接表的数量尽量不要超过 3 张 ，因为每增加一张表就相当于增加了一次嵌套的循环，数量级增

长会非常快，严重影响查询的效率。

-  对 WHERE 条件创建索引 ，因为 WHERE 才是对数据条件的过滤。如果在数据量非常大的情况下，

没有 WHERE 条件过滤是非常可怕的（通过索引去过滤数据，而不是全表扫描）

- 对用于连接的字段创建索引 ，并且该字段在多张表中的 类型必须一致 。比如 course_id 在

student_info 表和 course 表中都为 int(11) 类型，而不能一个为 int 另一个为 varchar 类型（其中会设计到隐式转换，即函数调用，从而造成索引失效）



#### 7、使用列类型效的字段创建索引

这里的类型大小指的就是该类型表示的数据范围大小。

数据类型越小，在查询时进行的比较操作越快；数据类型越小，索引占用的存储空间就越小，在一个数据页内就可以放下更多记录，从而减少磁盘I/O带来的性能损耗，也就意味着可以把更多的数据页缓存在内存页中，从而加快读锁页效率。对于表的主键更加适用，因为不仅是聚簇索引索引中会存储主键值，其他所有的二级索引的叶子节点处都会存储一份记录的主键值，如果主键使用更小的数据类型，也就意味节省更多的存储空间和更高效的I/O。



#### 8、使用字符串前缀创建索引

如果一个字段是字符串类型，并且字符串的长度很长，那么存储一个字符串就需要占用很大的存储空间。如果我们需要在字符串列建立索引，在对应的B+树中会存在两个问题：

- B+树索引中的记录如果把该字符串列的完整字符串存储起来，构建时十分费时，并且字符串越长，在索引中占用的存储空间就越大（二级索引中，无论是叶子节点还是中间节点都会涉及到该索引值的存储，需要占用更多的空间，并且如果是主键的话，那就聚簇索引和二级索引都会占用空间）
- 如果B+树索引中索引列存储的字符串很长，那在做字符串比较时就会占用更多的时间（因为字符串的比较在相同长度的情况下，会从头到尾比较）

针对上述问题，可以通过截取字段的前面一部分内容建立索引，这被称为前缀索引。这样在查找记录时虽然不能精确的定位到记录的位置，但是能定位到相应前缀所在的位置，然后根据前缀相同的记录的主键值回表查询完整的字符串值。即节约了空间，又减少了字符串的比较时间。

那么又有一个问题，截取多少合适？截取多了，达不到节省索引存储空间的目的，截取的少了，重复内容太多，字段的散列度（选择性）会降低，会增加更大的回表操作，增加IO操作次数。

计算选择性的公式如下所示

```mysql
select count(distinct left(列名，索引长度)) / count(*)
```

通过不同长度去计算，与全表的选择性对比,竟可能让选择性接近1，例如

```mysql
select count(distinct left(address,10)) / count(*) as sub10, -- 截取前10个字符的选择度
count(distinct left(address,15)) / count(*) as sub11, -- 截取前15个字符的选择度
count(distinct left(address,20)) / count(*) as sub12, -- 截取前20个字符的选择度
count(distinct left(address,25)) / count(*) as sub13 -- 截取前25个字符的选择度
```

引申另一个问题：索引列前缀对排序的影响

如果使用了索引列前缀，比方说前边只把address列的 前12个字符 放到了二级索引中，下边这个查询可能就有点儿尴尬了:

```
SELECT * FROM shop
ORDER BY address
LIMIT 12;
```

因为二级索引中不包含完整的address列信息，所以无法对前12个字符相同，后边的字符不同的记录进行排序，也就是使用索引列前缀的方式 无法支持使用索引排序 ，只能使用文件排序。

拓展：Alibaba《Java开发手册》

【强制】在 varchar 字段上建立索引时，必须指定索引长度，没必要对全字段建立索引，根据实际文本区分度决定索引长度。
说明:索引的长度与区分度是一对矛盾体，一般对字符串类型数据，长度为20的索引，区分度会 高达 98%以可以使用 count(distinct left(列名,索引长度))/count(*)的区分度来确定。



#### 9、区分度高（散列性高）的列适合作为索引

列的基数指的是某一列中不重复数据的个数，比方说某个列包含值2，5，8，2，5，8，2，5，8，虽然有9条记录，但该列的基数却是3。也就是说，在记录行数一定的情况下，列的基数越大，该列中的值越分散;列的基数越小，该列中的值越集中。这个列的基数指标非常重要，直接影响我们是否能有效的利用索引。最好为列的基数大的列建立索引，为基数太小列的建立索引效果可能不好。 ǐ
可以使用公式 select count(distinct a)/count(*)from t1 计算区分度，越接近1越好，，一般超过 33%就算是比较高效的索引了
**拓展:** 联合索引把区分度高(散列性高)的列放在前面

**原因：**因为当存在大量重复数据且数据分布均匀的字段在创建索引后，需要进行大量的回表操作，并且回表读的页面并不相连，是随机IO，所以此时不如直接去聚簇索引中执行全表扫描。



#### 10、使用频繁的列放到联合索引的左侧



#### 11、**在多个字段都要创建索引的情况下，联合索引优于单值索引**

可以较少的建立一些索引。同时，由于"最左前缀原则"，可以增加联合索引的使用率（即一个字段也可以被联合索引覆盖，前提是遵循最左前缀原则）



#### 12、限制索引数目

在实际工作中，我们也需要注意平衡，索引的数目不是越多越好。我们需要限制每张表上的索引数量，建议单张表索引数量 不超过6个。原因:
① 每个索引都需要占用 磁盘空间，索引越多，需要的磁盘空间就越大。
② 索引会影响 INSERT、DELETE、UPDATE等语句的性能，因为表中的数据更改的同时，索引也会进行调整和更新，会造成负担。
③ 优化器在选择如何优化查询时，会根据统一信息，对每一个可以用到的 索引来进行评估，以生成出一个最好的执行计划，如果同时有很多个索引都可以用于查询，会增加MySQL优化器生成执行计划时间，降低查询性能。



### 4.2 不适用索引的情况

#### 1、在where、order by、group by中用不到的字段，不需要设置索引

#### 2、数据量小的表不用使用索引

在数据量不打的情况下，索引就发挥不出作用了。如，在数据表中的数据行数比较少的情况下，比如不到 1000 行，是不需要创建索引的

#### 3、有大量重复数据（选择性低）的列上不建议创建索引

要在 100 万行数据中查找其中的 50 万行（比如性别为男的数据），一旦创建了索引，你需要先访问 50 万次索引，然后再访问 50 万次数据表（回表），这样加起来的开销比不使用索引可能还要大。

假设有一个学生表，学生总数为 100 万人，男性只有 10 个人，也就是占总人口的 10 万分之 1。学生表 student_gender 结构如下。其中数据表中的 student_gender 字段取值为 0 或 1，0 代表女性，1 代表男性。那么可以使用索引。

结论：当数据重复度大，比如 高于 10% 的时候，也不需要对这个字段使用索引。

#### 4、避免对经常需要新增、更新、删除操作的表建立过多的索引

维护也需要成本

#### 5、不建议使用无序的值作为索引

身份证、UUID(在索引比较时需要转为ASCII，并且插入时可能造成页分裂)、MD5、HASH、无序长字符串等。

#### 6、删除不再使用或者很少使用的索引

维护需要成本

#### 7、不要定义容易或重复的索引

①冗余索引

如联合索引定义了a、b、c字段，有定义了一个a的普通索引，重复。

②重复索引

设置字段A为PRIMARY KEY 主键约束，又给它定义为一个唯一索引，还给它定义了一个普通索引，可是主键本身就会生成聚簇索引，所以定义的唯一索引和普通索引是重复的，这种情况要避免。



## 五、索引失效

### 5.1 优先考虑全值匹配

两个索引，A对a字段创立索引，B对a、b、c字段创立索引

当我们使用如下查询

```mysql
select * from table where a = 1 and b = 2
```

优先会使用B索引，并且B索引的效率会把A索引高，因为在A的索引中，匹配到a = 1后，叶子结点中就没有b的相关信息了，需要回表继续查询，而B索引仍旧有b索引信息，因此其查找的效率会更高（相当于更加精确定位，多列索引的范围会比单列的范围更小）。



### 5.2 最佳左前缀法则

在MySQL建立联合索引时会遵守最佳左前缀匹配原则，即最左优先，在检索数据时从联合索引的最左边开始匹
配。一个索引可以包括16个字段。对于多列索引，过滤条件要使用索引必须按照索引建立时的顺序，依次满足，一旦跳过某个字段，索引后面的字段都无法被使用。如果查询条件中没有使用这些字段中第1个字段时，多列(或联合)索引不会被使用。

具体示例：

A为字段a，b，c创建联合索引

对于如下可以用到索引A，但是由于其没有b，索引只用到了A索引中的a字段

```java
select * from table where a = 5 and c = 6
```

对于如下可以用到索引A，SQL优化器会对顺序进行优化，以符合最左前缀原则

```mysql
select * from table where c = 1 and a = 3 and b = 4
```

对于如下不可用到索引A

```mysql
select * from table where c = 1
```



### 5.3 主键插入顺序

![image-20241104000355464](MySQL%E7%B4%A2%E5%BC%95.assets/image-20241104000355464.png)

这个数据页已经满了，再插进来需要把当前 页面分裂成两个页面，把本页中的一些记录移动到新创建的这个页中。页面分裂和记录移位意味着 性能损耗 ！所以如果我们想尽量避免这样无谓的性能损耗，最好让插入的记录的 主键值依次递增 ，这样就不会发生这样的性能损耗了。所以我们建议：让主键具有 AUTO_INCREMENT ，让存储引擎自己为表生成主键，而不是我们手动插入 ，我们自定义的主键列 id 拥有 AUTO_INCREMENT 属性，在插入记录时存储引擎会自动为我们填入自增的主键值。这样的主键占用空间小，顺序写入，减少页分裂。



### 5.4 计算、函数、类型转换(自动手动，显示隐式）导致索引失效

在使用计算、函数、类型转化的时候，其结果对于前者是不可见的，因此走不了索引，并且索引是按照列值的原始顺序组织和存储的。当对列应用函数时（如数学运算、字符串操作或日期函数等），函数会改变原始数据的值或格式，使得数据库无法直接定位到这些经过函数转换后的值。因此，数据库不得不执行全表扫描，以确保能够评估所有行上的函数操作，这导致查询性能下降。

示例1：函数

```mysql
select * from table a like 'abc%' #可以走索引
select * from table LEFT(a, 3) = 'abc' #不可走索引，结果abc对于函数不可见，需要一个个获取数据，然后调用函数，然后与abc进行比较
```

示例2：计算

```mysql
select * from table a  = 123 #可以
select * from table a + 1 = 123 + 1 #不可以
```

示例3：隐式转换

```mysql
select * from table a  = 123 # a是字符串类型，不可以走索引
select * from table a  = '123' # a是字符串类型，可以走索引
```



## 十、索引结构

p115 -- 127



## 结尾

### 1、参考文章

- https://javaguide.cn/database/mysql/mysql-index.htm
- https://www.cnblogs.com/zsql/p/13808417.html
- https://juejin.cn/post/6931901822231642125
- 官方文档：https://dev.mysql.com/doc/refman/8.0/en/create-index.html

## 2、其他

### 2.1 索引合并和组合索引

索引合并是指 MySQL 在查询时能够使用多个单列索引（而不是组合索引）的组合来优化查询。即使没有组合索引，MySQL 也可以通过合并多个单列索引来满足查询条件。

特点

- **无需创建组合索引**：适用于没有组合索引，但有多个单列索引的情况。MySQL 会在查询时选择多个单列索引并将它们的结果合并，以满足查询条件。
- **适用于 OR 条件**：索引合并在涉及 `OR` 查询条件时较为常见，例如 `(column_a = 'value' OR column_b = 'value')`。
- **性能可能不如组合索引**：索引合并在特定场景下比组合索引慢，尤其是在多列频繁联合查询时，组合索引通常更高效。

示例

```sql
CREATE INDEX idx_a ON table_name (column_a);
CREATE INDEX idx_b ON table_name (column_b);
-- 查询时可以加速如下查询：
SELECT * FROM table_name WHERE column_a = 'value' OR column_b = 'value';
```

区别总结

- **结构差异**：组合索引是一个单一的多列索引，而索引合并是多个单列索引在查询时的组合。
- **性能差异**：组合索引通常在多列查询中比索引合并性能更高，减少了回表次数和数据读取量。
- **适用场景**：组合索引适用于多列组合查询的高频场景；索引合并更适用于 OR 条件查询，或者当组合索引不适用时。