# 隐式连接和LEFT JOIN连接

在 SQL 中，`WHERE` 子句使用隐式连接（**也称为逗号连接**）和 `LEFT JOIN` 连接的区别主要体现在连接类型以及如何处理表中的数据行。让我们详细分析一下这两者的区别：

1. 隐式连接（逗号连接）

隐式连接是通过在 `FROM` 子句中列出多个表，并在 `WHERE` 子句中指定连接条件来实现的。它的格式如下：

```
sqlCopy CodeSELECT columns
FROM table1, table2
WHERE table1.column = table2.column;
```

这种方式本质上是 **内连接（INNER JOIN）** 的一种表现方式。即，只有当 `table1` 和 `table2` 中有匹配的数据时，这些数据行才会出现在结果集中。

特点：

- 隐式连接是通过 `WHERE` 子句中设置的条件来进行连接的。
- 它默认执行 **内连接**（INNER JOIN），只返回符合连接条件的匹配行。
- 如果有一个表中没有匹配的记录，整行数据就会被排除在外。

示例（隐式连接）：

```
sqlCopy CodeSELECT br.name, yz.disease
FROM zy_bingrenxx br, yz_bingrenyz yz
WHERE br.patient_id = yz.patient_id;
```

这里，只有当 `zy_bingrenxx` 和 `yz_bingrenyz` 中的 `patient_id` 匹配时，才会返回结果。

2. `LEFT JOIN` 连接

`LEFT JOIN`（左连接）是显式连接的一种方式，它返回左表（在 `FROM` 子句中第一个表）中的所有行，以及右表中符合连接条件的匹配行。如果右表中没有匹配的行，结果集会用 `NULL` 填充右表中的列。

特点：

- `LEFT JOIN` 返回的是左表中的所有行，即使右表没有匹配的行。
- 如果右表没有匹配的行，右表中的列会被填充为 `NULL`。
- 这种连接会包含所有左表的数据，而不仅仅是符合条件的匹配数据。

示例（`LEFT JOIN`）：

```
sqlCopy CodeSELECT br.name, yz.disease
FROM zy_bingrenxx br
LEFT JOIN yz_bingrenyz yz ON br.patient_id = yz.patient_id;
```

在这个例子中，**即使** `yz_bingrenyz` 表中没有与 `zy_bingrenxx` 表中的某个患者（`patient_id`）匹配的记录，左表 `zy_bingrenxx` 中的该患者依然会出现在结果中，而 `yz.disease` 会返回 `NULL`。

总结两者的区别：

| 特性               | 隐式连接（逗号连接）                                         | `LEFT JOIN` 连接                                  |
| ------------------ | ------------------------------------------------------------ | ------------------------------------------------- |
| 连接类型           | 内连接（INNER JOIN）                                         | 左连接（LEFT JOIN）                               |
| 返回的记录         | 只有匹配的行（在 `WHERE` 中定义连接条件的表中都存在匹配数据时） | 左表中的所有行，右表中没有匹配的行时会返回 `NULL` |
| 处理没有匹配的数据 | 没有匹配的行不会出现在结果中                                 | 没有匹配的右表行会以 `NULL` 填充                  |
| 可读性             | 可读性较差，尤其在连接多个表时                               | 结构更清晰，尤其适用于需要保留左表数据的场景      |

关键点：

- **隐式连接**默认执行内连接，只返回匹配的行。
- **`LEFT JOIN` 连接**则确保左表中的所有数据行都会出现在结果中，即使右表没有匹配的行。