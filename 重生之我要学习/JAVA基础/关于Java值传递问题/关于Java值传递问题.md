# 关于Java值传递问题

## 一、前言

### 1.1 问题引出

调用Method方法程序

```java
// 在调用Method方法之前，声明了两个Map，分别是aMap和bMap
Map<String, List<A>> aMap = new HashMap<>();
Map<String, List<A>> bMap = new HashMap<>();
try {
    // 调用Method方法的时候，将该两个参数传入
    Method(aMap, bMap);
} catch (JsonProcessingException e) {
    throw new RuntimeException(e);
}
```

Method方法

```java
private void Method(Map<String, List<A>> aMap, Map<String, List<A>> bMap) throws JsonProcessingException {

    String text = "……";

	// 通过JSON的parseObject将json字符串text转化为Map<String,List<A>>对象并赋值给aMap（注意：此时的对象不再是入参时候的对象了）
    aMap = JSON.parseObject(text, new TypeReference<Map<String, List<A>>>() {});

    bMap = JSON.parseObject(text, new TypeReference<Map<String, List<A>>>() {});
}
```

通过如下截图，查看对象的地址，可以发现对象是变化了

![image-20250129174058409](%E5%85%B3%E4%BA%8EJava%E5%80%BC%E4%BC%A0%E9%80%92%E9%97%AE%E9%A2%98.assets/image-20250129174058409.png)



### 1.2 参考文章

之前在学习C++语言的时候，将实参传递给方法（或函数）的方式分为两种：值传递和引用传递，但在JAVA中只有值传递（颠覆认知，基础没学踏实）

> 参考文章：https://blog.csdn.net/aaaa_1111111/article/details/143747247
>
> 相关内容（转载）：
>
> #### 1、形参&实参
>
> 方法的定义可能会用到 **参数**（有参的方法），参数在程序语言中分为：
>
> - **实参（实际参数，Arguments）**：用于传递给函数/方法的参数，必须有确定的值。
> - **形参（形式参数，Parameters）**：用于定义函数/方法，接收实参，不需要有确定的值。
>
> ```java
> String hello = "Hello!";
> // hello 为实参
> sayHello(hello);
> // str 为形参
> void sayHello(String str) {
>     System.out.println(str);
> }
> ```
>
> 
>
> #### 2、值传递&引用传递
>
> 程序设计语言将实参传递给方法（或函数）的方式分为两种：
>
> - 值传递：方法接收的是实参值的拷贝，会创建副本。
>
> - 引用传递：方法接收的直接是实参所引用的对象在堆中的地址，不会创建副本，对形参的修改将影响到实参。
>
> 很多程序设计语言（比如 C++、 Pascal )提供了两种参数传递的方式，不过，在 Java 中只有值传递。
>
> #### 3、为什么 Java 只有值传递
>
> **为什么说 Java 只有值传递呢？** 不需要太多废话，我通过 3 个例子来给大家证明。
>
> 案例 1：传递基本类型参数
>
> 代码：
>
> ```java
> public static void main(String[] args) {
>     int num1 = 10;
>     int num2 = 20;
>     swap(num1, num2);
>     System.out.println("num1 = " + num1);
>     System.out.println("num2 = " + num2);
> }
>  
> public static void swap(int a, int b) {
>     int temp = a;
>     a = b;
>     b = temp;
>     System.out.println("a = " + a);
>     System.out.println("b = " + b);
> }
> ```
>
> 输出：
>
> ```
> a = 20
> b = 10
> num1 = 10
> num2 = 20
> ```
>
> 解析：
>
> 在 `swap()` 方法中，`a`、`b` 的值进行交换，并不会影响到 `num1`、`num2`。因为，`a`、`b` 的值，只是从 `num1`、`num2` 的复制过来的。也就是说，a、b 相当于 `num1`、`num2` 的副本，副本的内容无论怎么修改，都不会影响到原件本身。
>
> ![img](%E5%85%B3%E4%BA%8EJava%E5%80%BC%E4%BC%A0%E9%80%92%E9%97%AE%E9%A2%98.assets/7c9c8143be884f4e9bfc86ea55b11bfd.png)
>
> 通过上面例子，我们已经知道了一个方法不能修改一个基本数据类型的参数，而对象引用作为参数就不一样，请看案例 2
>
> 案例 2：传递引用类型参数
>
> 代码：
>
> ```java
> public static void main(String[] args) {
>   int[] arr = { 1, 2, 3, 4, 5 };
>   System.out.println(arr[0]);
>   change(arr);
>   System.out.println(arr[0]);
> }
> 
> public static void change(int[] array) {
>   // 将数组的第一个元素变为0
>   array[0] = 0;
> }
> ```
>
> 输出：
>
> ```
> 1
> 0
> ```
>
> 解析：
>
> ![img](%E5%85%B3%E4%BA%8EJava%E5%80%BC%E4%BC%A0%E9%80%92%E9%97%AE%E9%A2%98.assets/48eb1782a5a74ff887f4ed853e91a10c.png)
>
> 看了这个案例很多人肯定觉得 Java 对引用类型的参数采用的是引用传递。
>
> 实际上，并不是的，这里传递的还是值，不过，这个值是实参的地址罢了！
>
> 也就是说 change 方法的参数拷贝的是 arr （实参）的地址，因此，它和 arr 指向的是同一个数组对象。这也就说明了为什么方法内部对形参的修改会影响到实参。
>
> 为了更强有力地反驳 Java 对引用类型的参数采用的不是引用传递，我们再来看下面这个案例！
>
> 
>
> 案例 3：传递引用类型参数 2
>
> ```java
> public class Person {
>     private String name;
>    // 省略构造函数、Getter&Setter方法
> }
>  
> public static void main(String[] args) {
>     Person xiaoZhang = new Person("小张");
>     Person xiaoLi = new Person("小李");
>     swap(xiaoZhang, xiaoLi);
>     System.out.println("xiaoZhang:" + xiaoZhang.getName());
>     System.out.println("xiaoLi:" + xiaoLi.getName());
> }
>  
> public static void swap(Person person1, Person person2) {
>     Person temp = person1;
>     person1 = person2;
>     person2 = temp;
>     System.out.println("person1:" + person1.getName());
>     System.out.println("person2:" + person2.getName());
> }
> ```
>
> 输出:
>
> ```
> person1:小李
> person2:小张
> xiaoZhang:小张
> xiaoLi:小李
> ```
>
> 解析：
>
> 怎么回事？？？两个引用类型的形参互换并没有影响实参啊！
>
> swap 方法的参数 person1 和 person2 只是拷贝的实参 xiaoZhang 和 xiaoLi 的地址。因此， person1 和 person2 的互换只是拷贝的两个地址的互换罢了，并不会影响到实参 xiaoZhang 和 xiaoLi 
> ![img](%E5%85%B3%E4%BA%8EJava%E5%80%BC%E4%BC%A0%E9%80%92%E9%97%AE%E9%A2%98.assets/615e498921354aa19b71e08247e69411.png)
>
> 
>
> #### 4、引用传递是怎么样的
>
> 看到这里，相信你已经知道了 Java 中只有值传递，是没有引用传递的。
> 但是，引用传递到底长什么样呢？下面以 `C++` 的代码为例，让你看一下引用传递的庐山真面目。
>
> ```java
> #include <iostream>
>  
> void incr(int& num)
> {
>     std::cout << "incr before: " << num << "\n";
>     num++;
>     std::cout << "incr after: " << num << "\n";
> }
>  
> int main()
> {
>     int age = 10;
>     std::cout << "invoke before: " << age << "\n";
>     incr(age);
>     std::cout << "invoke after: " << age << "\n";
> }
> ```
>
> 输出结果：
>
> ```
> invoke before: 10
> incr before: 10
> incr after: 11
> invoke after: 11
> ```
>
> 分析：可以看到，在 `incr` 函数中对形参的修改，可以影响到实参的值。要注意：这里的 `incr` 形参的数据类型用的是 `int&` 才为引用传递，如果是用 `int` 的话还是值传递哦！
>
> #### 5、为什么 Java 不引入引用传递
>
> 引用传递看似很好，能在方法内就直接把实参的值修改了，但是，为什么 Java 不引入引用传递呢？
>
> 注意：以下为个人观点看法，并非来自于 Java 官方：
>
> 出于安全考虑，方法内部对值进行的操作，对于调用者都是未知的（把方法定义为接口，调用方不关心具体实现）。你也想象一下，如果拿着银行卡去取钱，取的是 100，扣的是 200，是不是很可怕。
> Java 之父 James Gosling 在设计之初就看到了 C、C++ 的许多弊端，所以才想着去设计一门新的语言 Java。在他设计 Java 的时候就遵循了简单易用的原则，摒弃了许多开发者一不留意就会造成问题的“特性”，语言本身的东西少了，开发者要学习的东西也少了。
>
> #### 6、总结
>
> Java 中将实参传递给方法（或函数）的方式是 **值传递**：
>
> - 如果参数是基本类型的话，很简单，传递的就是基本类型的字面量值的拷贝，会创建副本。
> - 如果参数是引用类型，传递的就是实参所引用的对象在堆中地址值的拷贝，同样也会创建副本。



## 二、分析

### 2.1 几个值传递示例

#### 1. Boolean类型值传递问题

**demo**

```java
// Boolean类型
@Test
public void BooleanTest() {
    Boolean bool = false;
    transmitBoolean(bool);
    System.out.println(bool);
}
private void transmitBoolean(Boolean bool) {
    bool = true;
}
```

**结果**

```
false
```

**原因分析**

明明传递的是引用类型，为什么无法修改呢？

首先Java只支持值传递，此时的形参bool是实参的副本（即实参和形参的值都指向值=false的Boolean对象，但是形参时实参的拷贝副本），当我们在方法中修改形参bool的Boolean值的时候，因为Boolean 是不可变的对象，因此在方法中改变它的值时，它并不会影响原始的值，因此会创建一个新的对象并赋值给形参（即拷贝实参的副本），其并没有影响到实参，因此在该方法调用结束后，实参的值还是false。

调用方法前，实参地址

![image-20241229170753598](%E5%85%B3%E4%BA%8EJava%E5%80%BC%E4%BC%A0%E9%80%92%E9%97%AE%E9%A2%98.assets/image-20241229170753598.png)

调用方法中，形参地址

![image-20241229170818423](%E5%85%B3%E4%BA%8EJava%E5%80%BC%E4%BC%A0%E9%80%92%E9%97%AE%E9%A2%98.assets/image-20241229170818423.png)

方法中修改Boolean值后，形参地址

![image-20241229170838402](%E5%85%B3%E4%BA%8EJava%E5%80%BC%E4%BC%A0%E9%80%92%E9%97%AE%E9%A2%98.assets/image-20241229170838402.png)

**解决方法**

1. 使用 `AtomicBoolean`：

`AtomicBoolean` 是一个可以在多线程环境下保证原子性的类，它是可变的。你可以使用 `AtomicBoolean` 来避免不可变性的问题。

```java
import java.util.concurrent.atomic.AtomicBoolean;

public class Main {
    public static void main(String[] args) {
        AtomicBoolean pd = new AtomicBoolean(false);
        method(pd);
        System.out.println(pd.get());  // 现在会打印 true
    }

    public static void method(AtomicBoolean if1) {
        if1.set(true);  // 修改 AtomicBoolean 的值
    }
}
```

`AtomicBoolean` 使用 `get()` 方法来获取值，使用 `set()` 方法来修改值。

2. 使用数组或者容器对象（例如 `List`）：

如果你不想使用 `AtomicBoolean`，你还可以使用一个数组或容器（例如 `List`）来传递值，因为数组和容器是可变的。

```java
public class Main {
    public static void main(String[] args) {
        Boolean[] pd = { false };
        method(pd);
        System.out.println(pd[0]);  // 现在会打印 true
    }

    public static void method(Boolean[] if1) {
        if1[0] = true;  // 修改数组中的值
    }
}
```

3. 使用 `Wrapper` 类来返回新值：

如果你不想使用 `AtomicBoolean` 或容器，你可以通过方法的返回值来返回修改后的 `Boolean` 值。

```java
public class Main {
    public static void main(String[] args) {
        Boolean pd = false;
        pd = method(pd);  // 方法返回修改后的 Boolean 值
        System.out.println(pd);  // 现在会打印 true
    }

    public static Boolean method(Boolean if1) {
        return true;  // 直接返回修改后的值
    }
}
```



#### 2. Map类型值传递问题

**demo**

```java
// Map
@Test
public void MapTest() {
    Map<String, String> map = new HashMap<>();
    map.put("hhh", "123");
    transmitMap(map);
    System.out.println(map);
}
private void transmitMap(Map map) {
    //map.put("hhh", "321");
    map = new HashMap();
    map.put("xixi", "321");
}
```

**结果**

```java
{hhh=123}
```

**原因分析**

和1是一样原因，在方法中对形参指向引用对象的更改只对形参有效，形参只是对实参的一个拷贝，不会对实参造成影响！



#### 3.正常对象的值传递问题

**demo**

```java
// 自定义引用类型
@Test
public void ClassTest() {
    A a = new A();
    a.setId(123L);
    a.setName("www");
    transmitClass(a);
    System.out.println(a);
}
private void transmitClass(A a) {
    a.setId(321L);
    a.setName("hhh");
}
@Data
public class A{
    private Long id;
    private String name;
}
```

**结果**

```
DemoApplicationTests.A(id=321, name=hhh)
```

**原因**

这里显示的结果正确的，虽然形参是对实参的拷贝，但是修改的内容都是对同一个堆中对象的修改。所以在方法调用后，可以完成对内容的修改。