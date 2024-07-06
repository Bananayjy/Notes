# Stream流

## 一、前言

### 参考文章

使用相关文章：

- [使用教程博客1](https://www.cnblogs.com/softwarearch/p/16466235.html) 
- [使用教程博客2](https://juejin.cn/post/6844903830254010381)

- https://cloud.tencent.com/developer/article/1752773

- https://blog.csdn.net/mu_wind/article/details/109516995

- https://blog.csdn.net/Clearlove_S7/article/details/129971803

原理相关文章：

- https://www.cnblogs.com/xiaoxiongcanguan/p/10511233.html
- https://juejin.cn/post/7022611600821469220
- https://www.cnblogs.com/throwable/p/15371609.html

stream流和for的效率比较：

- https://blog.csdn.net/qq_15037231/article/details/79586587

### 1.1 说明

**Stream流**：是在**jdk8版本**中出现的新特性，是处理集合的关键抽象概念。

**jdk8前处理集合的痛点：**在我们处理集合数据的时候，需要进行大量的循环、遍历，并且很多场景下，会涉及对遍历结果赋值给另一个新的集合，然后再进行循环遍历的操作，这样会造成代码量偏多、代码冗余、可读性不高等问题。对此，我们通过Stream API可以帮助我们在操作集合的过程中带来极大的方便，并且性能会比传统的快。



### 1.2 Stream流代码示例

引用[相关文章](https://www.cnblogs.com/softwarearch/p/16466235.html)中的代码示例，是一个很好的例子。

> jdk8版本前操作集合
>
> ```java
> /**
>  * 【常规方式】
>  * 从给定句子中返回单词长度大于5的单词列表，按长度倒序输出，最多返回3个
>  *
>  * @param sentence 给定的句子，约定非空，且单词之间仅由一个空格分隔
>  * @return 倒序输出符合条件的单词列表
>  */
> public List<String> sortGetTop3LongWords(@NotNull String sentence) {
>     // 先切割句子，获取具体的单词信息
>     String[] words = sentence.split(" ");
>     List<String> wordList = new ArrayList<>();
>     // 循环判断单词的长度，先过滤出符合长度要求的单词
>     for (String word : words) {
>         if (word.length() > 5) {
>             wordList.add(word);
>         }
>     }
>     // 对符合条件的列表按照长度进行排序
>     wordList.sort((o1, o2) -> o2.length() - o1.length());
>     // 判断list结果长度，如果大于3则截取前三个数据的子list返回
>     if (wordList.size() > 3) {
>         wordList = wordList.subList(0, 3);
>     }
>     return wordList;
> }
> ```
>
> jdk8版本后操作集合
>
> ```java
> /**
>  * 【Stream方式】
>  * 从给定句子中返回单词长度大于5的单词列表，按长度倒序输出，最多返回3个
>  *
>  * @param sentence 给定的句子，约定非空，且单词之间仅由一个空格分隔
>  * @return 倒序输出符合条件的单词列表
>  */
> public List<String> sortGetTop3LongWordsByStream(@NotNull String sentence) {
>     return Arrays.stream(sentence.split(" "))
>             .filter(word -> word.length() > 5)
>             .sorted((o1, o2) -> o2.length() - o1.length())
>             .limit(3)
>             .collect(Collectors.toList());
> }
> ```



### 1.3 前置知识点

#### 1. java8 中的默认方法

**默认方法特点：**

- default方法可以有方法体
- 接口中的普通方法必须被实现类重写；默认方法可以不被实现类重写。

**具体应用：**

当我们进行业务扩展时，需要在接口中新增方法。如果新增的这个方法写成普通方法的话，那么需要在该接口所有的实现类中都重写这个方法。如果新增的方法定义为default类型，就不需要在所有的实现类中全部重写该default方法，哪个实现类需要新增该方法，就在哪个实现类中进行实现。

**注意：**

1、如果一个实现类只实现了一个接口，那么可以不用重写接口中的默认方法；如果一个实现类实现了多个接口，并且这些接口中有两个接口的默认方法是一样的，那么就必须在实现类中重写默认方法

2、默认方法可以通过接口的实现对象直接调用，可以被接口的实现类重写

3、接口中定义的默认方法（default methods）是不能直接被接口本身调用的，因为接口本身不能被实例化



#### 2. @Contract注解

该注解用在方法上，是由 IntelliJ IDEA 提供的一种注解，用于表达函数/方法的某些期望行为。这有助于IDEA的代码分析器理解您的代码并提供更精确的警告和错误提示。

`pure=true` 表示该方法是纯的，也就是说它不会修改对象的状态，并且对于相同的输入参数，总是返回相同的结果。这类似于函数式编程语言中的纯函数概念

```java
@Contract(pure = true)
public int add(int a, int b) {
    return a + b;
}
```

`@Contract(pure = false)` 是 IntelliJ IDEA 提供的一种注解，表示该方法不是纯的，这个方法可能会改变对象的状态，或者其返回结果可能依赖于除输入参数之外的其他因素

```java
@Contract(pure = false)
public void incrementCounter() {
    this.counter++;
}
```

`@Contract` 注解并不会改变程序的行为，只是帮助IDE提供更好的代码观测和警告。使用该注解需要添加 JetBrains 的 `annotations.jar` 依赖到项目中



#### 3. `Streams`

在 `java.util.stream` 包中，`Streams` 是一个 final 类，定义了一些静态方法来支持 `Stream` 操作和管道。这个类主要是为库设计者提供的，通常情况下我们不会直接使用它，`Stream` 是 Java 提供的一个功能强大的接口，用于处理数据序列；`Streams` 是一个内部工具类，提供了一些静态方法来辅助 `Stream` 操作；"streams" 则可能是表示多个 `Stream` 对象，或者是指整个 Stream API。这是 Java 内部 API 的一部分，通常我们不直接使用它。其中 `Streams.StreamBuilderImpl<T>` 是一个实现了 `Stream.Builder<T>` 接口的内部类，用于构建由单个元素或固定数目元素组成的流。

如：

`Stream`中的方法：

```java
public static<T> Builder<T> builder() {
    return new Streams.StreamBuilderImpl<>();
}
```

`Streams`的方法

```java
private abstract static class AbstractStreamBuilderImpl<T, S extends Spliterator<T>> implements Spliterator<T> {
        // >= 0 when building, < 0 when built
        // -1 == no elements
        // -2 == one element, held by first
        // -3 == two or more elements, held by buffer
        int count;

        // Spliterator implementation for 0 or 1 element
        // count == -1 for no elements
        // count == -2 for one element held by first

        @Override
        public S trySplit() {
            return null;
        }

        @Override
        public long estimateSize() {
            return -count - 1;
        }

        @Override
        public int characteristics() {
            return Spliterator.SIZED | Spliterator.SUBSIZED |
                   Spliterator.ORDERED | Spliterator.IMMUTABLE;
        }
    }
```

```java
static final class StreamBuilderImpl<T>
            extends AbstractStreamBuilderImpl<T, Spliterator<T>>
            implements Stream.Builder<T> {
        // The first element in the stream
        // valid if count == 1
        T first;
        
        ……
        }
```



#### 4. `StreamSupport`

`StreamSupport` 是 Java 8 中引入的一个工具类，它主要用于创建和操作 `Stream` 对象。

这个类提供了一些静态方法，如：

1. **stream()**: 这个方法接收一个 `Spliterator` 和一个布尔值（表示是否创建一个并行流），并返回一个新的 `Stream`。例如，你可以使用这个方法从一个集合或数组创建一个流。

```java
public static <T> Stream<T> stream(Spliterator<T> spliterator, boolean parallel) {
        Objects.requireNonNull(spliterator);
        return new ReferencePipeline.Head<>(spliterator,
                                            StreamOpFlag.fromCharacteristics(spliterator),
                                            parallel);
    }
```

2. **intStream(), longStream(), doubleStream()**: 这些方法都是用来从对应类型的 `Spliterator` 创建流的。与 `stream()` 方法类似，每个方法都接收一个 `Spliterator` 和一个布尔值，并返回一个对应类型的流。

大部分情况下，我们不需要直接使用 `StreamSupport`。因为在实际开发中，我们通常会使用更高级的方法来创建流，比如 `Collection.stream()`, `Arrays.stream(array)`, 或者 `Stream.of(elements)` 等。

然而，如果需要从自定义数据结构（尤其是那些没有默认支持创建流的数据结构）创建流，或者创建一个特殊的流（比如串行或并行流），那么 `StreamSupport` 就可能会派上用场。



#### 5. `Spliterator`

在 Java 8 中，`Spliterator` 是一个新引入的接口，用于处理并行流（Parallel Streams）中元素的迭代和分区。

`Spliterator` 名称是 "splitable iterator" 的缩写，也就是可分割的迭代器。它旨在为多核处理提供支持，可以将数据源拆分成多个部分，以便并行处理。

与普通 `Iterator` 和 `ListIterator` 相比，`Spliterator` 具有以下特点：

1. **可分割**：`Spliterator` 可以通过 `trySplit()` 方法拆分为其他 `Spliterator`，以支持高效的并行处理。
2. **可估计大小**：`Spliterator` 可以通过 `estimateSize()` 方法估计剩余元素数量，有利于进行优化和规划。
3. **支持一系列特性值**：`Spliterator` 定义了一组特性值，表示其排序、唯一性、非空性等特性。

`Spliterator` 主要用于创建并行流，并不常直接使用。当我们调用 `Collection.parallelStream()` 或 `Arrays.stream().parallel()` 等方法时，Java 底层会使用 `Spliterator` 将数据源拆分，以便并行处理。



#### 6. 关于Lambda表达式和函数式接口Functional Interface

Lambda表达式和函数式接口Functional Interface也属于jdk8的新特性，详情见如下文章

[参考文章](https://www.exception.site/java8/java8-new-features)

> 在学习 `Lambda` 表达式之前，我们先来看一段老版本的示例代码，其对一个含有字符串的集合进行排序：
>
> ```java
> List<String> names = Arrays.asList("peter", "anna", "mike", "xenia");
> 
> Collections.sort(names, new Comparator<String>() {
>     @Override
>     public int compare(String a, String b) {
>         return b.compareTo(a);
>     }
> });
> ```
>
> `Collections` 工具类提供了静态方法 `sort` 方法，入参是一个 `List` 集合，和一个 `Comparator` 比较器，以便对给定的 `List` 集合进行 排序。上面的示例代码创建了一个匿名内部类作为入参，这种类似的操作在我们日常的工作中随处可见。
>
> Java 8 中不再推荐这种写法，而是推荐使用 Lambda 表达：
>
> ```java
> Collections.sort(names, (String a, String b) -> {
>     return b.compareTo(a);
> });
> ```
>
> 正如你看到的，上面这段代码变得简短很多而且易于阅读。但是我们还可以再精炼一点：
>
> ```java
> Collections.sort(names, (String a, String b) -> b.compareTo(a));
> ```
>
> 对于只包含一行方法的代码块，我们可以省略大括号，直接 `return` 关键代码即可。追求极致，我们还可以让它再短点：
>
> ```java
> names.sort((a, b) -> b.compareTo(a));
> ```
>
> `List` 集合现在已经添加了 `sort` 方法。而且 Java 编译器能够根据**类型推断机制**判断出参数类型，这样，你连入参的类型都可以省略啦，怎么样，是不是感觉很强大呢！

>抛出一个疑问：在我们书写一段 Lambda 表达式后（比如上一章节中匿名内部类的 Lambda 表达式缩写形式），Java 编译器是如何进行类型推断的，它又是怎么知道重写的哪个方法的？
>
>需要说明的是，不是每个接口都可以缩写成 Lambda 表达式。只有那些函数式接口（Functional Interface）才能缩写成 Lambda 表示式。
>
>那么什么是函数式接口（Functional Interface）呢？
>
>所谓函数式接口（Functional Interface）就是只包含一个抽象方法的声明。针对该接口类型的所有 Lambda 表达式都会与这个抽象方法匹配。
>
>> 注意：你可能会有疑问，Java 8 中不是允许通过 defualt 关键字来为接口添加默认方法吗？那它算不算抽象方法呢？答案是：不算。因此，你可以毫无顾忌的添加默认方法，它并不违反函数式接口（Functional Interface）的定义。
>
>总结一下：只要接口中仅仅包含一个抽象方法，我们就可以将其改写为 Lambda 表达式。为了保证一个接口明确的被定义为一个函数式接口（Functional Interface），我们需要为该接口添加注解：`@FunctionalInterface`。这样，一旦你添加了第二个抽象方法，编译器会立刻抛出错误提示。
>
>示例代码：
>
>```
>@FunctionalInterface
>interface Converter<F, T> {
>    T convert(F from);
>}
>```
>
>示例代码2：
>
>```
>Converter<String, Integer> converter = (from) -> Integer.valueOf(from);
>Integer converted = converter.convert("123");
>System.out.println(converted);    // 123
>```
>
>> 注意：上面的示例代码，即使去掉 `@FunctionalInterface` 也是好使的，它仅仅是一种约束而已。
>
>



## 二、Stream流的使用【待完善】

### 2.1、Stream流概述

在传统的集合处理中，我们通常使用迭代器或者增强型for循环来遍历集合元素，然后进行相应的操作。而Stream流则允许我们以一种更为声明式的方式对集合进行操作，可以更加简洁和高效地编写代码，`Stream`将要处理的元素集合看作一种流，在流的过程中，借助`Stream API`对流中的元素进行操作，比如：筛选、排序、聚合等，Stream流本质上是一种数据流，它并不存储数据，而是通过管道将数据进行处理和传输。

`Stream`可以由数组或集合创建，对流的操作分为两种：

1. 中间操作，每次返回一个新的流，可以有多个。
2. 终端操作，每个流只能进行一次终端操作，终端操作结束后流无法再次使用。终端操作会产生一个新的集合或值。

`Stream`的特性：

1. stream不存储数据，而是按照特定的规则对数据进行计算，一般会输出结果。
2. stream不会改变数据源，通常情况下会产生一个新的集合或一个值。
3. stream具有延迟执行特性，只有调用终端操作时，中间操作才会执行。

4. stream不可复用，对一个已经进行过终端操作的流再次调用，会抛出异常。

总的来说，Stream流提供了一种更加现代化和便捷的集合处理方式，能够有效地提升Java程序的开发效率和性能。

### 2.2、Stream创建

java.util.stream.Stream 是Java 8新加入的流接口,获取一个Stream（创建Stream）的几种常用方式：

① 所有的 Collection 集合都可以通过 stream 默认方法获取流（顺序流）；

查看Collection集合中有一个默认方法，用来获取`stream`对象（顺序流）

```java
@Contract(pure = true)
default Stream<E> stream() {
     return StreamSupport.stream(spliterator(), false);
 }
```

1、List列表实现类ArrayList获取顺序流

```java
List list = new ArrayList();
Stream stream = list.stream();
```

2、List列表实现类Vector获取顺序流

```java
Vector vector = new Vector();
Stream stream = vector.stream();
```

3、Set实现类HashSet获取顺序流

```java
Set set = new HashSet();
Stream stream = set.stream();
```

4、Queue实现类LinkedList获取顺序流

```java
Queue queue = new LinkedList();
Stream stream = queue.stream();
```



② 所有的 Collection 集合都可以通过parallelStream获取并行流

查看Collection集合中有一个默认方法，用来获取`stream`对象（并行流）

```java
@Contract(pure = true)
default Stream<E> parallelStream() {
    return StreamSupport.stream(spliterator(), true);
}
```

1、List列表实现类ArrayList获取并行流

```java
List list = new ArrayList();
Stream stream = list.parallelStream();
```

2、List列表实现类Vector获取顺序流

```java
Vector vector = new Vector();
Stream stream = vector.parallelStream();
```

3、Set实现类HashSet获取顺序流

```java
Set set = new HashSet();
Stream stream = set.parallelStream();
```

4、Queue实现类LinkedList获取顺序流

```java
Queue queue = new LinkedList();
Stream stream = queue.parallelStream();
```



③ Stream 接口的静态方法 可以获取数组对应的流。

1、`public static<T> Stream<T> of(T... values)`

源码：

```java
public static<T> Stream<T> of(T... values) {
    return Arrays.stream(values);
}
```

说明：

`T... values` 是 Java 中表示可变长参数列表的方式。你可以向这个方法传入任意数量的同类型参数（或者不传入任何参数）

其就是调用了`Arrays`的`stream`方法来创建对象

示例：

```java
Stream<String> stream = Stream.of("ab", "cd", "ef", "gh");
stream.forEach(System.out::println);

/*
运行结果：
ab
cd
ef
gh
*/
```



2、`public static<T> Stream<T> of(T t) `

源码：

```java
public static<T> Stream<T> of(T t) {
    return StreamSupport.stream(new Streams.StreamBuilderImpl<>(t), false);
}
```

说明：

这段代码定义了一个名为 `of` 的静态方法，它接受一个类型为 `T` 的参数，并返回一个由这个单一元素构成的 `Stream<T>`

其使用的`StreamSupport`和`Streams`去创建对象，如果入参有多个，就会使用多参方法`public static <T> Stream<T> stream(T[] array)`去创建对应的stream对象

示例：

```java
Stream<String> stream = Stream.of("Hello");
stream.forEach(System.out::println);

/*
运行结果：
Hello
*/
```



3、`public static<T> Stream<T> iterate(final T seed, final UnaryOperator<T> f)`

源码：

```java
 public static<T> Stream<T> iterate(final T seed, final UnaryOperator<T> f) {
     Objects.requireNonNull(f);
     Spliterator<T> spliterator = new Spliterators.AbstractSpliterator<>(Long.MAX_VALUE,
                                                                   Spliterator.ORDERED | Spliterator.IMMUTABLE) {
         T prev;
         boolean started;

         @Override
         public boolean tryAdvance(Consumer<? super T> action) {
             Objects.requireNonNull(action);
             T t;
             if (started)
                 t = f.apply(prev);
             else {
                 t = seed;
                 started = true;
             }
             action.accept(prev = t);
             return true;
         }
     };
     return StreamSupport.stream(spliterator, false);
 }
```

说明：

`Stream.iterate` 是 Java 中 `Stream` 类提供的一个方法，用于创建一个无限长度的流，该流的每个元素都是通过指定的迭代函数生成的。

`iterate`方法入参如下所示

```java
static <T> Stream<T> iterate(T seed, UnaryOperator<T> f)
```

其中：

- `seed` 是流的第一个元素，也是迭代函数的初始输入值。
- `f` 是一个一元操作符（`UnaryOperator`），它接受上一个元素作为参数，并返回生成下一个元素的逻辑，其实一个函数式接口，并且继承`Function`函数式接口，具有`apply`方法，当我们执行`UnaryOperator<Integer> identityOperator = x -> x; `的时候实际上创建了一个匿名函数，它代表了 `UnaryOperator` 接口中的 `apply` 方法的具体实现，由于 `UnaryOperator` 是 `Function` 的子接口，因此这里你实现的就是 `Function` 接口中的 `apply` 方法。`UnaryOperator`会对输入的参数执行恒等操作，也就是直接返回输入的参数本身。在函数式编程中，恒等操作是指对输入值不做任何改变，直接返回原始输入值。

```java
public interface UnaryOperator<T> extends Function<T, T> {
    static <T> UnaryOperator<T> identity() {
        return t -> t;
    }
}
```

示例：

```java
Stream<Integer> stream = Stream.iterate(0, x -> x + 3).limit(4);
stream.forEach(System.out::println);
/*
运行结果：
0
3
6
9
*/
```



4、`public static<T> Stream<T> generate(Supplier<? extends T> s)`

源码：

```java
public static<T> Stream<T> generate(Supplier<? extends T> s) {
    Objects.requireNonNull(s);
    return StreamSupport.stream(
        new StreamSpliterators.InfiniteSupplyingSpliterator.OfRef<>(Long.MAX_VALUE, s), false);
}
```

说明：

这段代码是 Java 中 `Stream` 类中的 `generate` 方法的定义。`generate` 方法用于创建一个由提供的 `Supplier` 生成的无限长度流，每次调用 `Supplier` 都会产生一个新的值。

让我为你解释一下这个方法的作用：

1. `Supplier` 是一个函数式接口，它没有任何参数，返回一个泛型类型的值。在这里，`Supplier` 的作用是每次被调用时生成一个新的值。
2. `generate` 方法接受一个 `Supplier` 对象作为参数，该 `Supplier` 负责提供流中的元素。
3. 当你使用这个方法创建流并开始遍历时，它会不断调用 `Supplier` 的 `get` 方法来生成新的元素，从而创建一个无限长度的流。

举个简单的例子，假设我们有一个`Supplier`用于生成随机数：

```
javaCopy CodeSupplier<Integer> randomSupplier = () -> (int) (Math.random() * 100);
Stream<Integer> randomStream = Stream.generate(randomSupplier).limit(10);
randomStream.forEach(System.out::println);
```

在这个例子中，`randomSupplier` 是一个生成随机数的 `Supplier`，`Stream.generate(randomSupplier).limit(10)` 将会创建一个包含 10 个随机数的流。

需要注意的是，由于 `generate` 方法创建的是无限长度的流，如果不使用 `limit` 或者其他终止操作来限制流的大小，它将会无限地产生元素，导致无限循环。因此，在使用 `generate` 方法创建流时，要谨慎考虑对流的限制和终止条件。

示例：

```java
Stream<Double> stream = Stream.generate(Math::random).limit(3);
stream.forEach(System.out::println);
/*
运行结果：
0.3136819083661848
0.4560890969811652
0.09732825405694123
*/
```



④ Arrays的静态方法stream也可以获取流

源码：

```java
@Contract(pure = true)
public static <T> Stream<T> stream(T[] array) {
    return stream(array, 0, array.length);
}
```

说明：

`Arrays.stream(values)` 是 Java 8 中引入的一个用于创建流（Stream）的方法。这个方法接受一个数组作为参数，并将其转换为一个 Stream 对象

创建Stream示例：

```java
Integer[] numbers = {1, 2, 3, 4, 5};
Stream<Integer> stream = Arrays.stream(numbers);
```



扩展：

1、根据Map获取对应流

Map中没有像Collection集合一样，有默认方法去获取Stream对象，如果需要通过Map对获取对应的流

其实就是通过Map先转化为Collection 集合，再通过Collection集合的默认方法去获取顺序流。

```java
Map<String, String> map = new HashMap<>();
//map --> Set<String> --> Stream
Stream<String> stream = map.keySet().stream();
//map --> collection<String> --> Stream
Stream<String> stream1 = map.values().stream();
//map --> Set<Entry<String, String>> --> Stream
Stream<Map.Entry<String, String>> stream2 = map.entrySet().stream();
```

2、`stream`和`parallelStream`

**`stream`和`parallelStream`的简单区分：** `stream`是顺序流，由主线程按顺序对流执行操作，而`parallelStream`是并行流，内部以多线程并行执行的方式对流进行操作，但前提是流中的数据处理没有顺序要求。例如筛选集合中的奇数，两者的处理不同之处：

![在这里插入图片描述](Stream%E6%B5%81.assets/20201106164400889.png)

如果流中的数据量足够大，并行流可以加快处速度。

除了直接创建并行流，还可以通过`parallel()`把顺序流转换成并行流

```java
Optional<Integer> findFirst = list.stream().parallel().filter(x->x>6).findFirst();
```



### 2.3、Stream使用

Stream使用汇总：

```
终端操作，每个流只能进行一次终端操作，终端操作结束后流无法再次使用。终端操作会产生一个新的集合或值
- foreach（遍历）
- find、match（匹配）
- redue（规约）
- max、min、count（聚合）
- collect（收集）
	- summarizing、counting、averaging （统计）
	- groupingBy、partitioningBy （分组）
	- reduce （规约）
	- toList、toSet、toMap （归集）
	
中间操作，每次返回一个新的流，可以有多个
- filter （筛选）
- map （映射）
- sorted （排序）
- 提取与组合
```

上述方法可以被分为两种：

**延迟方法：** 返回值类型仍然是 Stream 接口自身类型的方法，因此支持链式调用。（除了终结方法外，其余方法均为延迟方法。）

**终结方法：**返回值类型不再是 Stream 接口自身类型的方法，因此不再支持类似 StringBuilder 那样的链式调用。终结方法包括 count 和 forEach 方法。



① forEach（遍历/终结方法/终端操作）

源码：

```java
void forEach(Consumer<? super T> action);

//两个实现类
//①ReferencePipeline的内部类Head<E_IN, E_OUT>
static class Head<E_IN, E_OUT> extends ReferencePipeline<E_IN, E_OUT>{
	@Override
    public void forEach(Consumer<? super E_OUT> action) {
        if (!isParallel()) {
            sourceStageSpliterator().forEachRemaining(action);
        }
        else {
            super.forEach(action);
        }
    }    
}

//②ReferencePipeline的forEach方法
@Override
public void forEach(Consumer<? super P_OUT> action) {
    evaluate(ForEachOps.makeRef(action, false));
}

```

说明：

`forEach`主要用于遍历，可简化for循环遍历，其入参是一个函数式接口：`Consumer`，接受一个`T`类型的参数，返回参数为void

```java
public interface Consumer<T> {
    void accept(T t);
}
```

示例：

```java
Stream<String> stream = Stream.of("n1", "n2", "n3", "n4");
//lambda 表达式入参
//stream.forEach(x -> System.out.println(x));
//方法引用入参
stream.forEach(System.out::println);

/*
运行结果：
n1
n2
n3
n4
*/
```

注意：

如果把上面两个`forEach`都打开，会报错

```java
Stream<String> stream = Stream.of("n1", "n2", "n3", "n4");
//lambda 表达式入参
stream.forEach(x -> System.out.println(x));
//方法引用入参
stream.forEach(System.out::println);

/*
运行结果：
n1
n2
n3
n4

java.lang.IllegalStateException: stream has already been operated upon or closed

*/
```

在你的代码中，当你尝试使用方法引用 `System.out::println` 来操作流时，会遇到一个错误。这是因为在调用 `stream.forEach(System.out::println)` 之后，流 `stream` 已经被消耗（consumed），无法再次使用。在 Java 中，一旦流被消耗，就无法再次使用。即一旦流执行了终端操作（terminal operation），流就会被消耗（consumed），并且在执行完终端操作后，流会自动关闭



②filter（过滤）





## 三、Stream流原理

### 3.1 概括（特点）

流式编程

使用建造者模式，允许你创建复杂对象的过程与其表示分离，使得同样的构建过程可以创建不同的表示。

惰性求值（Lazy Evaluation）策略，在调用终端操作的时候才会进行相关操作。

提供了并行处理的能力



## # 扩展问题

### 1、为什么通过Stream流能够使得操作集合的速度、性能得到一个提升。

一般情况上，Stream的效率确实会比for循环慢上很多，然后在绝大部分情况下是没有什么区别的。主要是通过stream流可以方便我们对集合的处理。



### 2、函数式编程模式

Java8 中的 Stream API 时，说实话，我非常困惑，因为它的名字听起来与 Java I0 框架中的 `InputStream` 和 `OutputStream` 非常类似。但是实际上，它们完全是不同的东西。

Java8 Stream 使用的是函数式编程模式，如同它的名字一样，它可以被用来对集合进行链状流式的操作。



