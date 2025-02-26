## Java四大引用类型

### 一、概述

在 Java 中，四大引用类型指的是 `强引用`、`软引用`、`弱引用` 和 `虚引用`。这些引用类型的主要作用是影响垃圾回收器（GC）如何管理对象的生命周期，特别是在内存不足的情况下。不同类型的引用影响着对象的可回收性，它们的具体行为如下：

#### 1. **强引用 (Strong Reference)**

定义：

强引用是 Java 中最常见的引用类型。凡是你通过 `new` 创建一个对象，或者直接赋值给一个引用变量的对象，都是强引用。

特性：

- 只要对象有强引用指向它，垃圾回收器不会回收该对象。
- 垃圾回收器会在内存充足时，尽量不回收有强引用的对象。
- 只有当强引用的对象不再被引用（即强引用指向 `null` 或超出作用域）时，垃圾回收器才会回收该对象。

示例：

```java
Object obj = new Object(); // 这里是一个强引用
```

结论：

- 强引用会使对象一直存在，只要引用不被解除，GC 不会回收该对象。

#### 2. **软引用 (Soft Reference)**

定义：

软引用是一种弱化的引用类型，它的特点是当系统内存充足时，垃圾回收器不会回收它所引用的对象；当内存不足时，垃圾回收器会回收这些对象。

特性：

- 软引用非常适合用于缓存的实现。比如，当内存充足时，你可以保留缓存对象，内存紧张时会自动释放缓存。
- Java 提供了 `SoftReference` 类来实现软引用。

示例：

```java
SoftReference<MyObject> softRef = new SoftReference<>(new MyObject());
```

结论：

- 软引用的对象会在内存充足时存在，但当 JVM 内存不足时会被回收。
- 通常用于内存敏感型的缓存场景。

#### 3. **弱引用 (Weak Reference)**

定义：

弱引用是比软引用更弱的一种引用类型。对象如果只有弱引用指向它，则无论系统内存是否充足，垃圾回收器都会在下一次垃圾回收时回收该对象。

特性：

- 垃圾回收器会在每次垃圾回收时回收所有被弱引用指向的对象。
- Java 提供了 `WeakReference` 类来实现弱引用。

示例：

```java
WeakReference<MyObject> weakRef = new WeakReference<>(new MyObject());
```

结论：

- 弱引用的对象会在每次垃圾回收时被回收。
- 适合于当对象生命周期非常短，或者只在特定时刻使用时。

#### 4. **虚引用 (Phantom Reference)**

定义：

虚引用是最弱的引用类型。它与弱引用类似，但是虚引用的对象在被垃圾回收时并不会立即被回收，垃圾回收器会在对象被回收时通知你，通常是通过 `ReferenceQueue`。

特性：

- 虚引用的存在不会影响对象是否被回收。
- 使用虚引用时，一般是为了做一些资源清理工作（例如释放一些与对象关联的非堆内存，像直接内存等）。
- Java 提供了 `PhantomReference` 类来实现虚引用。

示例：

```java
PhantomReference<MyObject> phantomRef = new PhantomReference<>(new MyObject(), queue);
```

结论：

- 虚引用用于跟踪对象的回收过程，并在对象被回收后做一些额外的处理。
- 它不会阻止对象被回收，通常和 `ReferenceQueue` 一起使用来进行回收后的清理工作。



#### 5、引用类型对比

| 引用类型                   | 影响 GC 的强度                   | 被 GC 回收的时机                 | 适用场景                         |
| -------------------------- | -------------------------------- | -------------------------------- | -------------------------------- |
| 强引用 (Strong Reference)  | 强烈阻止对象被回收               | 只要强引用存在，对象不会被回收   | 普通对象引用，保持对象的生命周期 |
| 软引用 (Soft Reference)    | 比强引用弱，但内存不足时会被回收 | 内存充足时不回收，内存不足时回收 | 缓存、内存敏感型应用             |
| 弱引用 (Weak Reference)    | 较弱，几乎总是会被回收           | 每次 GC 都会回收                 | 临时对象，生命周期短的引用       |
| 虚引用 (Phantom Reference) | 极弱，不影响对象被回收           | 对象即将被回收时，通知并清理     | 清理资源，内存回收后处理工作     |

#### 6、总结

- **强引用**：常见引用类型，不会被 GC 回收。
- **软引用**：适用于缓存，内存足够时不回收，内存不足时回收。
- **弱引用**：每次 GC 都会被回收，适合短期生命周期的对象。
- **虚引用**：最弱的引用类型，主要用于资源清理和对象回收后的通知





## 尾言

### 1、参考文章

- 原理：https://www.cnblogs.com/pengxurui/p/16576791.html

- 使用&概要：https://zhulinyin.github.io/2019/01/28/Java%E7%9A%84%E5%9B%9B%E7%A7%8D%E5%BC%95%E7%94%A8%E6%96%B9%E5%BC%8F/

### 2、补充

#### 1、GC Root 和 GC Root 引用链

在 Java 的垃圾回收机制中，**GC Root（垃圾回收根节点）** 是一组特殊的对象，它们被视为垃圾回收的起始点，所有从 GC Root 可以直接或间接访问到的对象，都会被视为“活的对象”，而不会被回收。换句话说，GC 根节点及其引用链上的对象会一直存在，垃圾回收器不会回收它们。

**引用链**（Reference Chain）是指从 GC Root 出发，通过对象之间的引用关系，追踪到可以访问的对象。任何无法通过 GC Root 访问到的对象都会被认为是不可达的，进而成为垃圾回收的候选对象。

Java 中定义了几种对象，作为 GC Root：

1. **虚拟机栈（Java Stack）中的引用**：
   - 每个线程都有自己的虚拟机栈，栈帧中包含方法的局部变量表。局部变量表中可能保存对对象的引用。栈帧中的对象引用通常是 GC Root 的一部分。
2. **静态字段（Static Fields）**：
   - 被类或接口持有的静态字段也被视为 GC Root。即使没有创建对象，只要类被加载，它的静态字段（类变量）就会被视为 GC Root。
3. **常量池中的引用**：
   - 类加载器（ClassLoader）加载的类或类中的常量（如 `String`）也会作为 GC Root。常量池中保存着很多常用的对象引用。
4. **JNI（Java Native Interface）引用**：
   - 通过本地方法（native code）持有的引用也是 GC Root 的一部分。它们可以通过 JNI 与 Java 堆中的对象建立联系。

#### 2、关于弱引用的回收问题

```java
public static void main(String[] args) {

    ReferenceQueue queue = new  ReferenceQueue();
    WeakReference<Object> objectWeakReference = new WeakReference<>(new Object(), queue);
    System.gc();


    Reference poll = queue.poll();
    System.out.println(poll);

}
```

正常情况下，当这个WeakReference所弱引用的Object对象被垃圾收集器回收的同时，objectWeakReference所强引用的Object对象被列入ReferenceQueue，但是实际接口没有，如下所示：

```
null
```

`WeakReference` 是一种特殊的引用类型，它会允许垃圾回收器回收指向的对象，即使该对象还存在弱引用。换句话说，弱引用不会阻止对象被垃圾回收。

当一个对象仅被弱引用持有时，垃圾回收器可以在下一次回收时回收这个对象，并且会将这个对象放入与之关联的 `ReferenceQueue` 中，**前提是你在垃圾回收后调用 `queue.poll()`** 来从队列中取出这个引用。

在你的代码中，虽然你调用了 `System.gc()` 来触发垃圾回收，但这并不保证对象一定被回收。垃圾回收是由 JVM 决定的，虽然你请求了垃圾回收，但它可能不会立刻发生，或者有时甚至根本不会发生，尤其是在没有足够内存压力的情况下。

我们需要做如下事情：

**确保垃圾回收有足够的机会执行**

- 虽然你调用了 `System.gc()`，但它只是建议 JVM 执行垃圾回收，并不保证会马上执行。你可以多次尝试调用 `System.gc()`，或者等待一段时间。

**确认 `WeakReference` 的对象是否已被回收**

- 即使调用了 `System.gc()`，对象的回收还是受到其他因素（比如内存压力、GC 阶段等）的影响，无法保证一定回收。

修改后的代码如下：

```java
public static void main(String[] args) {

    ReferenceQueue queue = new  ReferenceQueue();
    WeakReference<Object> objectWeakReference = new WeakReference<>(new Object(), queue);
    System.gc();

    // 让 GC 有足够的时间执行
    try {
        Thread.sleep(1000); // 等待一段时间，给 GC 回收机会
    } catch (InterruptedException e) {
        e.printStackTrace();
    }

    Reference poll = queue.poll();
    System.out.println(poll);

}
```

加入了 `Thread.sleep(1000)` 来等待一段时间，确保垃圾回收器有足够的时间去清理弱引用指向的对象。