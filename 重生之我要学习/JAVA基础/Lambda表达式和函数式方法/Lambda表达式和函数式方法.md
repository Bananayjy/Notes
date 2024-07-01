# Lambda表达式和函数式方法

### 1、引入

Lambda表达式和函数式方法是从Java 8 版本开始引入的重要特性。Java 8 发布于2014年3月，它引入了一系列新的语言特性和API改进，其中包括Lambda表达式和函数式方法。Lambda表达式使得在Java中可以更方便地支持函数式编程风格，使得代码更为简洁和灵活。函数式方法也是与Lambda表达式紧密相关的概念，它们允许在集合类上进行便捷的数据处理操作，如映射、过滤和归约等。

在Java 8之前，Java主要以面向对象的编程方式为主，虽然也可以通过匿名内部类实现类似函数式编程的功能，但语法相对冗长并不够简洁明了。Java 8的引入使得Java语言在现代编程语言的潮流中更加具有竞争力，使得开发者能够更容易地编写简洁而功能强大的代码。

### 2、具体示例

通过@FunctionalInterface 自定义一个函数式接口

```java
@FunctionalInterface
public interface MyFuncInterface<T, U> {
    U method(T a);
}
```

匿名函数调用

```java
Integer a = 123, b = 321;
MyFuncInterface<Integer, String> myFuncInterface = new MyFuncInterface() {
    @Override
    public Object method(Object a) {
        return a.toString();
    }
};
String method = myFuncInterface.method(a);
System.out.println(method);
```

lambda调用

```java
Integer a = 123;
MyFuncInterface<Integer, String> myFuncInterface = r -> r.toString();
String method = myFuncInterface.method(a);
System.out.println(method);
```



注意：

1、如果有多个入参，需要加括号

声明函数式接口：

```java
Integer a = 123;
MyFuncInterface<Integer, String> myFuncInterface = (r) -> r.toString();
String method = myFuncInterface.method(a);
System.out.println(method);
```

调用：

```java
Integer a = 123, b = 321;
MyFuncInterface<Integer, String, Integer> myFuncInterface = (r1,r2) -> r1.toString();
String method = myFuncInterface.method(a, b);
System.out.println(method);
```

2、如果方法体有多行，也需要通过{}括起来。



### 3、关于Lambda表达式（Lambda expressions）和函数式方法（Functional interfaces and methods）的关系

Lambda 表达式（Lambda expressions）和函数式方法（Functional interfaces and methods）在 Java 中密切相关，它们一起构成了函数式编程的基础。

1. **Lambda 表达式**：
   - Lambda 表达式是一种轻量级的匿名函数，可以用来简化函数式接口（Functional Interface）的实现。
   - Lambda 表达式的基本语法形式是：`(parameters) -> expression` 或 `(parameters) -> { statements; }`。
   - Lambda 表达式可以让你直接传递代码块作为方法参数，或者在集合的遍历、过滤和映射等操作中更加方便和简洁。
2. **函数式接口**：
   - 函数式接口是一个具有单个抽象方法的接口。Java 8 引入了 `@FunctionalInterface` 注解来明确标识这样的接口。
   - 函数式接口允许通过 Lambda 表达式来创建接口实例，从而实现更加简洁的代码编写方式。
   - Java 中很多函数式接口可以直接使用，比如 `Runnable`、`Callable`、`Comparator` 等。
3. **函数式方法**：
   - 函数式方法指的是能够作为 Lambda 表达式的目标类型的方法。这些方法通常是函数式接口中定义的抽象方法。
   - Lambda 表达式可以直接作为函数式接口的实现，实现接口中的抽象方法，从而实现函数式编程的思想。
4. **关系**：
   - Lambda 表达式提供了一种便捷的语法来实现函数式接口中的抽象方法，从而避免了传统的匿名类的冗长语法。
   - 函数式方法是 Lambda 表达式的目标类型，Lambda 表达式本质上是对函数式方法的一个实现。
   - Lambda 表达式和函数式接口的结合，使得 Java 8 及以后版本能够更加方便地支持函数式编程范式，同时保持了 Java 的面向对象特性。