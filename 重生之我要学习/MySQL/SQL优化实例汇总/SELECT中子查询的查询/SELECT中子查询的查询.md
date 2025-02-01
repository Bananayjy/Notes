# SELECT中子查询的查询

## 一、SQL优化

### 1.1 SQL DEMO

a表DDL：

```mysql
CREATE TABLE `a` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `a_name` varchar(30) DEFAULT NULL,
  `a_age` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=10002 DEFAULT CHARSET=utf8;
```

b表DDL：

```mysql
CREATE TABLE `b` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `b_name` varchar(30) DEFAULT NULL,
  `b_age` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=5002 DEFAULT CHARSET=utf8;
```

查询语句（优化前）：

```mysql
select 
b.id,
b.b_name,
b.b_age,
(select a_name from a where a.id = b.id) as a_name
from b 
where b.b_age > 30

```

### 1.2 SQL优化

查询语句（优化后）：

```mysql
select
b.id,
b.b_name,
b.b_age,
a.a_name
from b left join a 
on b.id = a.id
where b.b_age > 30
```

> 注：实际执行sql时，两者效率是一样的，因为mysql会对优化前的SQL进行优化，我们可以看下他们的执行计划。
>
> 优化前：
>
> ```mysql
> explain select 
> b.id,
> b.b_name,
> b.b_age,
> (select a_name from a where a.id = b.id) as a_name
> from b 
> where b.b_age > 30
> ```
>
> ![image-20250201105111245](SELECT%E4%B8%AD%E5%AD%90%E6%9F%A5%E8%AF%A2%E7%9A%84%E6%9F%A5%E8%AF%A2.assets/image-20250201105111245.png)
>
> 优化后:
>
> ```mysql
> explain select
> b.id,
> b.b_name,
> b.b_age,
> a.a_name
> from b left join a 
> on b.id = a.id
> where b.b_age > 30
> ```
>
> ![image-20250201105122649](SELECT%E4%B8%AD%E5%AD%90%E6%9F%A5%E8%AF%A2%E7%9A%84%E6%9F%A5%E8%AF%A2.assets/image-20250201105122649.png)

优化点说明：

1、如果该子查询返回单行结果，可以考虑将其改为 `JOIN`，避免每次查询都执行子查询

sql的执行过程如下所示：

- from
- on
- join
- where
- goup by
- having + 聚合函数
- select
- order by
- limit

`(select a_name from a where a.id = b.id) as a_name` 是一个**子查询**，它会在每一行的其他列被选择并返回之前执行。子查询会在查询的主查询执行过程中，每遍历一行时逐行计算，针对每一行 `b.id` 的值进行查找，并返回相应的 `a_name` 列的值，然后将其作为 `a_name` 列的值进行返回。因此，子查询的执行是**依赖于主查询的每一行数据**的。

1. **主查询**会先执行
2. **在遍历主查询的每一行数据时**，对于每一行，都会对 `(select a_name from a where a.id = b.id)` 进行执行。
3. 每次执行子查询时，它会根据 `b.id` 的当前值从`a 表`中检索 `a_name` 的值。

使用了子查询 `(select a_name from a where a.id = b.id)`，如果该子查询返回单行结果，可以考虑将其改为 `JOIN`，避免每次查询都执行子查询。

