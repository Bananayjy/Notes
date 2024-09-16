# MySQL隐式转换

## 一、说明

### 1.1 官方文档

文档：https://blog.csdn.net/weixin_64940494/article/details/126115774

关于MySQL隐式转换的问题，在[官方文档](https://dev.mysql.com/doc/refman/5.7/en/type-conversion.html?spm=5176.100239.blogcont47339.5.1FTben)中有详细的说明，可以进行参考。文章后续也会结合官方文档进行详细深入说明，见部分二。

### 1.2 场景示例

在实际开发中，很多时候会遇到隐式转换带来的坑，实际在MySql中A字段是varchar类型，在应用中传递的是Long类型，从而导致了字符串类型和整数类型的比较带了的隐式转换,其结果可能会查出多条数据。

```sql
select * from table where id in （9223372036854775807, 9223372036854775808）
```

### 1.3 原因

见2.2的最后一点说明



## 二、详细说明 

### 2.1 隐式转换和显示转换

在 MySQL 中，隐式转换和显示转换是处理数据类型转换的两种方式

- 隐式转换（Implicit Conversion）

自动发生，无需用户显式地指定转换操作，MySQL 会在查询执行时进行。

隐式转换发生在 MySQL 自动将一种数据类型转换为另一种数据类型时。MySQL 在执行查询时会自动处理这些转换，以确保运算的兼容性。以下是一些常见的隐式转换情况：

1. **数字与字符串**：当你将一个字符串与数字进行比较或运算时，MySQL 会自动将字符串转换为数字。例如：

   ```mysql
   SELECT '123' + 1;  -- 结果是 124
   ```

   在这个例子中，MySQL 将字符串 `'123'` 隐式转换为数字 `123`，然后进行加法操作。

2. **类型兼容性**：在比较两个不同类型的值时，MySQL 会将它们转换为兼容的类型。例如：

   ```mysql
   SELECT 1 = '1';  -- 结果是 1 (TRUE)
   ```

   在这个例子中，MySQL 会将字符串 `'1'` 隐式转换为数字 `1`，然后进行比较。

3. **自动转换**：当将不同数据类型的列用于运算或比较时，MySQL 会自动进行转换，以确保数据类型一致。



- 显示转换（Explicit Conversion）

通过特定的函数显式地指定数据类型转换，提供了更大的控制权和精确度

显示转换是指你在 SQL 查询中显式地指定数据类型转换的过程。你可以使用 MySQL 提供的函数来进行显示转换。常见的函数包括：

1. **`CAST()` 函数**：用于将表达式转换为指定的数据类型。

   ```mysql
   SELECT CAST('2024-09-16' AS DATE);  -- 将字符串转换为日期
   ```

2. **`CONVERT()` 函数**：用于将表达式转换为指定的数据类型或字符集。

   ```mysql
   SELECT CONVERT('123.45', DECIMAL(10,2));  -- 将字符串转换为 DECIMAL 类型
   ```

3. **`DATE()` 函数**：用于从日期时间表达式中提取日期部分。

   ```mysql
   SELECT DATE('2024-09-16 10:30:00');  -- 结果是 '2024-09-16'
   ```



### 2.2 隐式转换

- If one or both arguments are `NULL`, the result of the comparison is `NULL`, except for the `NULL`-safe [`<=>`](https://dev.mysql.com/doc/refman/5.7/en/comparison-operators.html#operator_equal-to) equality comparison operator. For `NULL <=> NULL`, the result is true. No conversion is needed.

  **译：**除了安全的<=>相等比较操作符，如果一个或两个参数为NULL，则比较的结果为NULL; 对于NULL  <=> NULL，结果为true，不需要转换。

  **示例：**

  ```SQL
  select 1 = NULL  
  # 结果：NULL
  select NULL = 1  
  # 结果：NULL
  select NULL = NULL  
  # 结果：NULL
  select NULL <=> NULL  
  # 结果：1
  ```

  

- If both arguments in a comparison operation are strings, they are compared as strings.

  **译：**如果比较操作中的两个参数都是字符串，则将它们作为字符串进行比较。

  **示例：**

  ```sql
  select '123' = '12'   
  # 结果：0 
  select '111' = '111'  
  # 结果：1
  ```

  

- If both arguments are integers, they are compared as integers.

  **译：**如果两个参数都是整数，则将它们作为整数进行比较。

  

- Hexadecimal values are treated as binary strings if not compared to a number.

  **译:**  如果不将十六进制值与数字进行比较，则将其视为二进制字符串

  **示例：**

  ```sql
  #1、MySQL支持十六进制值。在数字上下文中，十六进制数如同整数(64位精度)。在字符串上下文，如同二进制字符串，每对十六进制数字被转换为一个字符。十六进制值的默认类型是字符串。
  
  select 0xa = 10
  # 结果：1
  
  select 0xa+''
  # 结果：10
  
  SELECT 0x4D7953514C
  # 结果：MySQL
  
  #2、如果想要确保该值作为数字处理，可以使用CAST(...AS UNSIGNED)
  SELECT 0x41, CAST(0x41 AS UNSIGNED)
  # 结果 A, 65
  
  #3、0x语法基于ODBC。十六进制字符串通常用于ODBC以便为BLOB列提供值。x’hexstring’语法基于标准SQL。可以用HEX()函数将一个字符串或数字转换为十六进制格式的字符串
  SELECT x'4D7953514C'
  # 结果：MySQL
  
  select hex(10)
  # 结果：A
  
  select hex('10')
  # 结果：3130
   
  ```

  

- If one of the arguments is a [`TIMESTAMP`](https://dev.mysql.com/doc/refman/5.7/en/datetime.html) or [`DATETIME`](https://dev.mysql.com/doc/refman/5.7/en/datetime.html) column and the other argument is a constant, the constant is converted to a timestamp before the comparison is performed. This is done to be more ODBC-friendly. This is not done for the arguments to [`IN()`](https://dev.mysql.com/doc/refman/5.7/en/comparison-operators.html#operator_in). To be safe, always use complete datetime, date, or time strings when doing comparisons. For example, to achieve best results when using [`BETWEEN`](https://dev.mysql.com/doc/refman/5.7/en/comparison-operators.html#operator_between) with date or time values, use [`CAST()`](https://dev.mysql.com/doc/refman/5.7/en/cast-functions.html#function_cast) to explicitly convert the values to the desired data type.

  A single-row subquery from a table or tables is not considered a constant. For example, if a subquery returns an integer to be compared to a [`DATETIME`](https://dev.mysql.com/doc/refman/5.7/en/datetime.html) value, the comparison is done as two integers. The integer is not converted to a temporal value. To compare the operands as [`DATETIME`](https://dev.mysql.com/doc/refman/5.7/en/datetime.html) values, use [`CAST()`](https://dev.mysql.com/doc/refman/5.7/en/cast-functions.html#function_cast) to explicitly convert the subquery value to [`DATETIME`](https://dev.mysql.com/doc/refman/5.7/en/datetime.html).

  **译：** 如果参数之一是 timestamp 或 datatime，而另一个参数是常量，则在执行比较之前，该常量将转换为时间戳。这样做是为了使odbc更加友好。[' IN() '](https://dev.mysql.com/doc/refman/5.7/en/comparison-operators.html#operator_in)的参数不会这样做。为了安全起见，在进行比较时总是使用完整的datetime、date或time字符串。例如，为了在使用[' BETWEEN '](https://dev.mysql.com/doc/refman/5.7/en/comparison-operators.html#operator_between)处理日期或时间值时获得最佳结果，请使用[' CAST() '](https://dev.mysql.com/doc/refman/5.7/en/cast-functions.html#function_cast)显式地将值转换为所需的数据类型。

  来自一个或多个表的单行子查询不被视为常量。例如，如果子查询返回一个要与[' DATETIME '](https://dev.mysql.com/doc/refman/5.7/en/datetime.html)值进行比较的整数，那么比较将作为两个整数进行。该整数不会转换为时间值。为了比较[' DATETIME '](https://dev.mysql.com/doc/refman/5.7/en/datetime.html)值的操作数，使用[' CAST() '](https://dev.mysql.com/doc/refman/5.7/en/cast-functions.html#function_cast)显式地将子查询值转换为[' DATETIME '](https://dev.mysql.com/doc/refman/5.7/en/datetime.html)。【关于这一部分可以参考[官方文档说明](https://dev.mysql.com/doc/refman/5.7/en/date-and-time-type-conversion.html)】

  

- If one of the arguments is a decimal value, comparison depends on the other argument. The arguments are compared as decimal values if the other argument is a decimal or integer value, or as floating-point values if the other argument is a floating-point value.

  **译：** 如果其中一个参数是十进制值，则比较取决于另一个参数。如果另一个参数是小数或整数值，则将参数作为十进制值进行比较;如果另一个参数是浮点值，则将参数作为浮点值进行比较。

  

- In all other cases, the arguments are compared as floating-point (double-precision) numbers. For example, a comparison of string and numeric operands takes place as a comparison of floating-point numbers.

  **译：** 在所有其他情况下，将实参作为浮点(双精度)数进行比较。例如，字符串和数字操作数的比较就像浮点数的比较一样。

  **示例1：**

  ```sql
  select 'a' = 0
  # 结果：1
  ```

  **示例2（官方）：**

  ```mysql
  mysql> SELECT 1 > '6x';
          -> 0
  mysql> SELECT 7 > '6x';
          -> 1
  mysql> SELECT 0 > 'x6';
          -> 0
  mysql> SELECT 0 = 'x6';
          -> 1
  ```

  在 MySQL 中，当字符串被尝试转换为数值时，其处理方式是从字符串的开头开始，尽可能多地提取数字部分，直到遇到无法识别的字符为止。

  如

  ```mysql
  ‘6x’ -> 6
  'x6' -> 0
  ```

  

- Comparison of JSON values takes place at two levels. The first level of comparison is based on the JSON types of the compared values. If the types differ, the comparison result is determined solely by which type has higher precedence. If the two values have the same JSON type, a second level of comparison occurs using type-specific rules. For comparison of JSON and non-JSON values, the non-JSON value is converted to JSON and the values compared as JSON values. For details, see [Comparison and Ordering of JSON Values](https://dev.mysql.com/doc/refman/5.7/en/json.html#json-comparison).

- JSON值的比较在两个级别上进行。第一级比较基于被比较值的JSON类型。如果类型不同，比较结果完全取决于哪个类型具有更高的优先级。如果两个值具有相同的JSON类型，则使用特定于类型的规则进行第二级比较。对于JSON和非JSON值的比较，将非JSON值转换为JSON，将比较的值转换为JSON值。



- For comparisons of a string column with a number, MySQL cannot use an index on the column to look up the value quickly. If *`str_col`* is an indexed string column, the index cannot be used when performing the lookup in the following statement:

  对于字符串列与数字的比较，MySQL不能在列上使用索引来快速查找值。如果 ' str_col ' 是索引字符串列，则在执行以下语句中的查找时不能使用索引:
  
  ```
  SELECT * FROM tbl_name WHERE str_col=1;
  ```
  
  The reason for this is that there are many different strings that may convert to the value `1`, such as `'1'`, `' 1'`, or `'1a'`.
  
  这样做的原因是有许多不同的字符串可以转换为值“1”，例如"1''，"1“或"1a”



- Comparisons between floating-point numbers and large values of INTEGER type are approximate because the integer is converted to double-precision floating point before comparison, which is not capable of representing all 64-bit integers exactly. For example, the integer value 253 + 1 is not representable as a float, and is rounded to 253 or 253 + 2 before a float comparison, depending on the platform.

  **译：**浮点数和INTEGER类型的大值之间的比较是近似的，因为整数在比较之前被转换为双精度浮点数，这不能准确地表示所有64位整数。例如，整数值253 + 1不能表示为浮点数，在浮点数比较之前被舍入为253或253 + 2，具体取决于平台。

  **示例（官方）：**

  ```sql
  mysql> SELECT '9223372036854775807' = 9223372036854775807;
          -> 1
  mysql> SELECT '9223372036854775807' = 9223372036854775806;
          -> 1
  ```

  当从字符串到浮点数和从整数到浮点数的转换发生时，它们不一定以相同的方式发生。整数可以由CPU转换为浮点数，而字符串则是在涉及浮点乘法的操作中逐位转换的。此外，结果可能受到诸如计算机体系结构或编译器版本或优化级别等因素的影响。避免此类问题的一种方法是使用CAST()，这样值就不会隐式地转换为浮点数:

  ```sql
  mysql> SELECT CAST('9223372036854775807' AS UNSIGNED) = 9223372036854775806;
          -> 0
  ```




### 2.3 显示转换

1. **`CAST()` 函数**：用于将表达式转换为指定的数据类型。

   ```mysq
   SELECT CAST('2024-09-16' AS DATE);  -- 将字符串转换为日期
   ```

2. **`CONVERT()` 函数**：用于将表达式转换为指定的数据类型或字符集。

   ```mysq
   SELECT CONVERT('123.45', DECIMAL(10,2));  -- 将字符串转换为 DECIMAL 类型
   ```

3. **`DATE()` 函数**：用于从日期时间表达式中提取日期部分。

   ```mysql
   SELECT DATE('2024-09-16 10:30:00');  -- 结果是 '2024-09-16'
   ```

