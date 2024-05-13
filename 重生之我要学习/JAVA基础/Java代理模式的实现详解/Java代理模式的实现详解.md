# Java代理模式的实现详解

## 一、前言

### 1.1、说明

本文章是在学习`mybatis框架`源码的过程中，发现对于动态代理Mapper接口这一块的代理实现还是有些遗忘和陌生，因此在本文章中就Java实现代理模式的过程进行一个学习和总结。

### 1.2、参考文章

- 《设计模式》（第2版） 刘伟 
- [终于有人把 java 代理 讲清楚了，万字详解！](https://xie.infoq.cn/article/9a9387805a496e1485dc8430f)
- [Java代理模式详解](https://javaguide.cn/java/basis/proxy.html#_2-%E9%9D%99%E6%80%81%E4%BB%A3%E7%90%86)

## 二、代理模式

### 2.1、代理模式定义

>  代理模式（Proxy Pattern）定义：给某一个对象提供一个代理，并由代理对象控制对原对象的引用。代理模式的英文叫做Proxy或Surrogate，它是一种对象结构型模式。
>
> 英文定义："Provide a surrogate or placeholder for another object to control access to it."

### 2.2、代理模式介绍

> 代理模式是最常用的结构型设计模式之一，当直接访问某些对象存在问题时，可以通过一个代理对象来间接访问，为了保证客户端使用的透明性（即面向抽象接口编程，原对象和代理对象都要实现同一个接口，客户端引用基类，能够透明地使用其子类的对象，是一种多态的实现），所访问的真实对象与代理对象需要实现相同的接口。

具体场景：

1、同学A想要买房，但是他不了解如何去找好的房源，因此其委托中介B去帮忙完成寻找房源的过程，此时中介B在这里充当一个代理角色，所谓代理，就是一个人或者一个机构代表另一个人或者另一个机构采取行动。

2、在网页上查看一张图片，由于网速等原因不能立即显示，可以在图片传输过程中，先将一些简单的用于描述图片的文字或小图片传输到客户端，此时这些文字或小图片就成为该图片的代理。

上述两个例子都通过引入一个新的对象：中介、小图片来实现对真实对象的操作或者将新的的对象作为真实对象的一个替身，这种实现机制即为代理模式，通过引入代理对象来间接访问一个对象，这也是代理模式的模式动机。

### 2.3、代理模式UML结构图

![代理模式UML结构图.drawio](Java%E4%BB%A3%E7%90%86%E6%A8%A1%E5%BC%8F%E7%9A%84%E5%AE%9E%E7%8E%B0%E8%AF%A6%E8%A7%A3.assets/%E4%BB%A3%E7%90%86%E6%A8%A1%E5%BC%8FUML%E7%BB%93%E6%9E%84%E5%9B%BE.drawio.png)

<div align="center"><sub >图2-1 代理模式UML结构图</sub></div>

注：如果对UML图不太了解的，可以自行查阅资料，本文章不在这里过多展开介绍。

各角色介绍：

- **Subject（抽象角色）**

  是真实角色和代理角色的共同接口（基类），这样在任何使用真实角色的地方都可以使用代理角色（多态特性），客户端需要针对抽象角色进行编程。

- **Proxy（代理角色/代理对象）**

  代理角色内部包含了对真实角色的引用，从而可以在任何时候操作真实角色对象。在代理角色中提供了一个与真实角色相同的接口（即基类的抽象接口），以便在任何时候都可以替代真实角色。代理角色还可以控制真实就角色的使用，负责在需要的时候创建和删除真实角色，并对真实角色对象的使用加以约束。代理角色通常在客户端调用所引用的真实角色操作之前或之后还需要执行其他操作，而不仅仅是单纯地调用真实角色对象的操作。

- **RealSubject（真实角色/目标对象）**

  真实角色定义了代理角色所代表的真实对象，在真实角色中实现真实的业务操作，客户端可以通过代理角色间接调用真实角色中定义的方法。



## 三、Java实现静态代理

### 3.1、说明

静态代理，我们通过手动的方式，在不修改目标对象的基础上，扩展代理对象，对目标对象进行一些功能的附加和增强，实现对目标对象的增强。需要有一个通用的抽象接口，代理对象和目标对象实现抽象接口，客户端编写抽象接口进行编程，但是其实际应用场景非常非常少，日常开发几乎看不到使用静态代理的场景。

优点：

- 封装性强：代理类可以对真实对象进行封装，客户端不需要知道真实对象的具体实现细节，只需要和代理类交互，从而达到解耦的效果。
- 扩展性好：通过代理类，可以在不修改真实对象的情况下，对其进行功能扩展或增强，例如添加额外的操作、控制访问权限等。
- 访问控制：代理类可以控制客户端对真实对象的访问，实现访问权限的管理，例如在访问某些敏感方法前后进行权限验证或日志记录。
- 保护真实对象：代理类可以充当真实对象的保护层，可以控制对真实对象的直接访问，防止恶意操作或错误调用。

缺点：

- 编码复杂：每一个需要代理的对象都需要单独编写代理类，如果真实对象很多，会导致代理类的数量激增，增加了系统的复杂度。
- 静态类型：静态代理在编译期间就已经确定了代理对象和真实对象的关系，在编译时就将接口、实现类、代理类这些都变成了一个个实际的 class 文件，因此无法在运行时动态改变代理对象，灵活性较差。
- 功能局限：静态代理只能代理固定类型的对象，无法代理不同类型的对象，因此对于不同类型的真实对象需要编写不同的代理类，增加了开发成本。
- 维护成本高：当真实对象的接口发生变化时，代理类的接口也需要相应地进行修改，维护起来相对麻烦。

### 3.2、场景实现

**模拟场景**：同学A想要买房，但是他不了解如何去找好的房源，因此其委托中介B去帮忙完成寻找房源的过程。

**具体实现**

创建一个抽象角色，表示买房的对象，在其中定义买房的抽象方法

```java
/**
 * 买房对象 同学A
 * @author banana
 * @create 2024-05-12 15:18
 */
public class BuyHomeA implements BuyHomeObject {
    // 同学A的购买方法
    @Override
    public void buy() {
        System.out.println("同学A付买房费用……");
    }
}
```

创建真实角色/目标对象，即同学A，并定义其买房方法

```java
/**
 * 买房对象 同学A
 * @author banana
 * @create 2024-05-12 15:18
 */
public class BuyHomeA implements BuyHomeObject {
    // 同学A的购买方法
    @Override
    public void buy() {
        System.out.println("同学A完成买房……");
    }
}
```

创建代理角色/代理对象，即中介B，实现中介B的买房方法，即并对同学A的买房方法进行一个增强

```java
/**
 * 买房对象 B中介
 * @author banana
 * @create 2024-05-12 15:21
 */
public class BuyHomeB implements BuyHomeObject {

    // 这里使用基类，在代理对象中维护目标对象
    BuyHomeObject buyHomeObject;

    // 创建构造器完成目标对象的注入
    public BuyHomeB(BuyHomeObject buyHomeObject) {
        this.buyHomeObject = buyHomeObject;
    }

    // 中介B的购买方法（对目标对象的购买方法进行增加，加入购买前/后的操作）
    @Override
    public void buy() {
        preBuy();
        buyHomeObject.buy();
        afterBuy();
    }

    // 购买前操作
    public void preBuy() {
        System.out.println("中介收取前期费用……");
        System.out.println("中介找到适合的房源……");
    }

    // 购买后操作
    public void afterBuy() {
        System.out.println("中介收取后期费用……");
    }
}
```

创建测试类

```java
/**
 * @author banana
 * @create 2024-05-12 15:32
 */
public class BuyHomeStaticTest {
    public static void main(String[] args) {
        // 创建A同学对象
        BuyHomeA buyHomeA = new BuyHomeA();
        // 创建中介B对象，A同学找到中介B，要求帮忙查找房源
        BuyHomeB buyHomeB = new BuyHomeB(buyHomeA);
        // 中介B帮忙寻找、购买房源（购买肯定是A同学自己购买）
        buyHomeB.buy();
    }
}

```

运行结果

```
中介收取前期费用……
中介找到适合的房源……
同学A付买房费用……
中介收取后期费用……
```



## 四、Java中的动态代理（jdk动态代理）

### 4.1、说明

相较于静态代理实现，我们不需要为每个目标对象都创建一个代理类，动态代理类的字节码在程序运行时由 Java 反射机制动态生成，无需程序员手工编写它的源代码。动态代理类不仅简化了编程工作，而且提高了软件系统的可扩展性，因为 Java 反射机制可以生成任意类型的动态代理类。`java.lang.reflect` 包中的 Proxy 类和`InvocationHandler` 接口提供了生成动态代理类的能力。

其在日常场景中应用还是比较少的，但是在框架中几乎是很常见的，例如AOP、RPC框架等等。

优点：

- 灵活性高：与静态代理相比，动态代理更加灵活，可以在运行时动态地生成代理类，无需提前编写大量的代理类。
- 代码简洁：由于动态代理是在运行时生成的，因此可以大大减少代码量，使代码更加简洁、清晰。
- 维护成本低：由于动态代理不需要为每个被代理的类编写单独的代理类，因此当原始类的接口发生变化时，对代码的影响较小，维护成本低。
- 适用范围广：动态代理可以代理任意实现了接口的类，不限于特定的类或接口类型，因此适用范围更广泛。

缺点：

- 性能稍低：相比静态代理，动态代理在运行时需要动态生成代理类，因此可能会稍微降低程序的运行效率。
- 复杂度高：动态代理涉及到反射机制和动态生成字节码等技术，因此相对于静态代理而言，实现和理解的难度较高。
- 不支持对类的直接代理：动态代理只能代理实现了接口的类，无法直接代理类，这在某些情况下可能会限制其使用。
- 难以调试：动态代理生成的代理类通常是在运行时动态生成的字节码，因此在调试时可能会增加一定的难度，不如静态代理那样直观。

### 4.2、场景实现

**模拟场景**：同学A想要买房，但是他不了解如何去找好的房源，因此其委托中介B去帮忙完成寻找房源的过程。

**具体实现**

创建一个抽象角色，表示买房的对象，在其中定义买房的抽象方法

```java
/**
 * 买房对象
 * @author banana
 * @create 2024-05-12 15:15
 */
public interface BuyHomeObject {

    // 抽象购买方法
    public void buy();

}

```

创建真实角色/目标对象，即同学A，并定义其买房方法

```java
/**
 * 买房对象 同学A
 * @author banana
 * @create 2024-05-12 15:18
 */
public class BuyHomeA implements BuyHomeObject {
    // 同学A的购买方法
    @Override
    public void buy() {
        System.out.println("同学A付买房费用……");
    }
}
```

通过动态代理实现中介B代理对象

```java
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * 动态代理实现中介B代理对象
 * @author banana
 * @create 2024-05-12 17:46
 */
public class MyInvocationHandler implements InvocationHandler {

    // 维护目标对象
    private Object object;

    // 创建构造器完成目标对象的注入
    public MyInvocationHandler(Object object) {
        this.object = object;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        preBuy();
        Object invoke = method.invoke(object, args);
        afterBuy();
        return invoke;
    }

    // 购买前操作
    public void preBuy() {
        System.out.println("中介收取前期费用……");
        System.out.println("中介找到适合的房源……");
    }

    // 购买后操作
    public void afterBuy() {
        System.out.println("中介收取后期费用……");
    }

}
```

创建一个测试类

```java
import java.lang.reflect.Proxy;

/**
 * @author banana
 * @create 2024-05-12 18:03
 */
public class BuyHomeDynamicProxyTest {
    public static void main(String[] args) {
        // 创建A同学对象
        BuyHomeA buyHomeA = new BuyHomeA();
        
        // 通过动态代理创建中介B对象
        MyInvocationHandler myInvocationHandler = new MyInvocationHandler(buyHomeA);
        BuyHomeObject dynamicProxyBuyHomeB  = (BuyHomeObject) Proxy.newProxyInstance(BuyHomeA.class.getClassLoader(),
                BuyHomeA.class.getInterfaces(), myInvocationHandler);
        
        // 中介B帮忙寻找、购买房源（购买肯定是A同学自己购买）
        dynamicProxyBuyHomeB.buy();
    }
}

```

运行结果，可以看到，我们在这次并没有和静态代理一样，创建代理对象中介B对象，却实现了相同的功能

```
中介收取前期费用……
中介找到适合的房源……
同学A付买房费用……
中介收取后期费用……
```



### 4.3、动态代理实现原理（源码分析）

我们可以在场景实现中看到，整个动态代理的过程，主要涉及到一个接口InvocationHandler和一个类Proxy。

Proxy类通过静态方法newProxyInstance来生成一个代理对象实例，其源码以及注释（含英文注释翻译）如下所示

```java
 /**
     * Returns a proxy instance for the specified interfaces
     * that dispatches method invocations to the specified invocation
     * handler.【返回指定接口的代理实例，该接口将方法调用分派给指定的调用处理程序】
     * <p>
     * <a id="restrictions">{@code IllegalArgumentException} will be thrown
     * if any of the following restrictions is violated:</a>
     * 【违反了以下任何限制将会抛出IllegalArgumentException异常】
     * <ul>
     * <li>All of {@code Class} objects in the given {@code interfaces} array
     * must represent interfaces, not classes or primitive types.
     * 【所有传给interfaces入参的Class对象必须表示接口，不是类或基本类型】
     * <li>No two elements in the {@code interfaces} array may
     * refer to identical {@code Class} objects.
     * 【传给interfaces入参的类对象不能引用相同的Class对象】
     *
     * <li>All of the interface types must be visible by name through the
     * specified class loader. In other words, for class loader
     * {@code cl} and every interface {@code i}, the following
     * expression must be true:<p>
     * {@code Class.forName(i.getName(), false, cl) == i}
     *
     * <li>All of the types referenced by all
     * public method signatures of the specified interfaces
     * and those inherited by their superinterfaces
     * must be visible by name through the specified class loader.
     *
     * <li>All non-public interfaces must be in the same package
     * and module, defined by the specified class loader and
     * the module of the non-public interfaces can access all of
     * the interface types; otherwise, it would not be possible for
     * the proxy class to implement all of the interfaces,
     * regardless of what package it is defined in.
     *
     * <li>For any set of member methods of the specified interfaces
     * that have the same signature:
     * <ul>
     * <li>If the return type of any of the methods is a primitive
     * type or void, then all of the methods must have that same
     * return type.
     * <li>Otherwise, one of the methods must have a return type that
     * is assignable to all of the return types of the rest of the
     * methods.
     * </ul>
     *
     * <li>The resulting proxy class must not exceed any limits imposed
     * on classes by the virtual machine.  For example, the VM may limit
     * the number of interfaces that a class may implement to 65535; in
     * that case, the size of the {@code interfaces} array must not
     * exceed 65535.
     * </ul>
     *
     * <p>Note that the order of the specified proxy interfaces is
     * significant: two requests for a proxy class with the same combination
     * of interfaces but in a different order will result in two distinct
     * proxy classes.
     *
     * @param   loader the class loader to define the proxy class【装入类装入器以定义代理类】
     * @param   interfaces the list of interfaces for the proxy class
     *          to implement【接口代理类要实现的接口列表】
     * @param   h the invocation handler to dispatch method invocations to【将方法调用分派到的调用处理程序】
     * @return  a proxy instance with the specified invocation handler of a
     *          proxy class that is defined by the specified class loader
     *          and that implements the specified interfaces 【具有代理类的指定调用处理程序的代理实例，该代理类由指定的类装入器定义，并实现指定的接口】
     * @throws  IllegalArgumentException if any of the <a href="#restrictions">
     *          restrictions</a> on the parameters are violated 【如果违反了上面restrictions内的任何一条，则返回IllegalArgumentException异常】
     * @throws  SecurityException if a security manager, <em>s</em>, is present
     *          and any of the following conditions is met:
     *          <ul>
     *          <li> the given {@code loader} is {@code null} and
     *               the caller's class loader is not {@code null} and the
     *               invocation of {@link SecurityManager#checkPermission
     *               s.checkPermission} with
     *               {@code RuntimePermission("getClassLoader")} permission
     *               denies access;</li>
     *          <li> for each proxy interface, {@code intf},
     *               the caller's class loader is not the same as or an
     *               ancestor of the class loader for {@code intf} and
     *               invocation of {@link SecurityManager#checkPackageAccess
     *               s.checkPackageAccess()} denies access to {@code intf};</li>
     *          <li> any of the given proxy interfaces is non-public and the
     *               caller class is not in the same {@linkplain Package runtime package}
     *               as the non-public interface and the invocation of
     *               {@link SecurityManager#checkPermission s.checkPermission} with
     *               {@code ReflectPermission("newProxyInPackage.{package name}")}
     *               permission denies access.</li>
     *          </ul>
     * @throws  NullPointerException if the {@code interfaces} array
     *          argument or any of its elements are {@code null}, or
     *          if the invocation handler, {@code h}, is
     *          {@code null}
     *
     * @see <a href="#membership">Package and Module Membership of Proxy Class</a>
     * @revised 9
     * @spec JPMS
     */

	// @CallerSensitive是Java中的一个注解，用于标记方法的敏感性，表示该方法的调用者会被特别关注。
    @CallerSensitive
	// newProxyInstance方法入参，详细说明见①
    public static Object newProxyInstance(ClassLoader loader,
                                          Class<?>[] interfaces,
                                          InvocationHandler h) {
        // 判断h调用处理程序是否为空，为空抛出NullPointerException异常
        Objects.requireNonNull(h);
		// 如果存在安全管理器，则获取调用者的类；否则，返回null，详细说明见②
        final Class<?> caller = System.getSecurityManager() == null
                                    ? null
                                    : Reflection.getCallerClass();

        /*
         * Look up or generate the designated proxy class and its constructor.
         * 【查找或生成指定的代理类及其构造函数】 详细说明见③
         */
        Constructor<?> cons = getProxyConstructor(caller, loader, interfaces);

        // 创建代理对象实例，详细说明见④
        return newProxyInstance(caller, cons, h);
    }
```

① newProxyInstance方法入参详细说明

关于newProxyInstance静态方法的三个入参在源码的注释中已经有清晰的说明，这里再进行一个详细的解释：

- `ClassLoader loader`: 这里放入的是目标对象的类加载器，用于加载代理对象 。

- `Class<?>[] interfaces`: 被代理对象需要实现的接口列表（如果有多个，生成代理对象会实现所有的接口）

- `InvocationHandler h`: （调用处理器）是Java动态代理机制中的一个接口，它定义了代理对象调用方法时的处理方式。传入实现了 `InvocationHandler` 接口的对象，代理对象方法调用时分派到的调用处理程序。

  当动态代理对象调用一个方法时，这个方法的调用就会被转发到实现InvocationHandler 接口类的 invoke 方法来调用，可以在`invoke`方法中添加前置处理（如日志记录、权限验证等）和后置处理（如性能监控、异常处理等），以达到更灵活地控制代理对象的行为。InvocationHandler 接口类中只有一个invoke抽象方法，源码如下所示

  ```java
  /**
   * {@code InvocationHandler} is the interface implemented by
   * the <i>invocation handler</i> of a proxy instance.
   * 【InvocationHandler接口是代理对象实例的调用处理程序实现的接口】即定义了代理对象调用方法时的处理方式
   *
   * <p>Each proxy instance has an associated invocation handler.
   * When a method is invoked on a proxy instance, the method
   * invocation is encoded and dispatched to the {@code invoke}
   * method of its invocation handler.
   * 【每个代理实例都有一个关联的调用处理程序。在代理实例上调用方法时，对方法调用进行编码并将其分派给其调用处理程序的调用方法。】
   *
   * @author      Peter Jones
   * @see         Proxy
   * @since       1.3
   */
  public interface InvocationHandler {
  
      /**
       * Processes a method invocation on a proxy instance and returns
       * the result.  This method will be invoked on an invocation handler
       * when a method is invoked on a proxy instance that it is
       * associated with.
       * 【处理代理实例上的方法调用并返回结果。当在与其关联的代理实例上调用方法时，将在调用处理程序上调用此方法。】 即代理对象的方法
       *
       * @param   proxy the proxy instance that the method was invoked on
       * 【代理调用该方法的代理实例】即方法调用发生的那个代理对象
       * 
       * @param   method the {@code Method} instance corresponding to
       * the interface method invoked on the proxy instance.  The declaring
       * class of the {@code Method} object will be the interface that
       * the method was declared in, which may be a superinterface of the
       * proxy interface that the proxy class inherits the method through.
       * 【方法在代理实例上调用的接口方法对应的method实例。Method对象的声明类将是该方法被声明的接口，该接口可能是代理类继承该方法所通过的代理接口的超接口。】 即当前代理对象所调用方法所对应的抽象基类的方法信息，Method对象包含了关于被调用方法的所有信息，包括方法名、参数列表、返回类型等
       * 
       * @param   args an array of objects containing the values of the
       * arguments passed in the method invocation on the proxy instance,
       * or {@code null} if interface method takes no arguments.
       * Arguments of primitive types are wrapped in instances of the
       * appropriate primitive wrapper class, such as
       * {@code java.lang.Integer} or {@code java.lang.Boolean}.
       * 【args:一个对象数组，其中包含在代理实例的方法调用中传递的参数值，如果接口方法不接受参数，则为null。基本类型的参数会被包装在适当的基本包装器类的实例中，例如java.lang。Integer或 java.lang.Boolean。】 即代理对象方法在调用时传入的参数，并且参数只能通过对象的方式传递，而不是原始的基本数据类型
       *
       * @return  the value to return from the method invocation on the
       * proxy instance.  If the declared return type of the interface
       * method is a primitive type, then the value returned by
       * this method must be an instance of the corresponding primitive
       * wrapper class; otherwise, it must be a type assignable to the
       * declared return type.  If the value returned by this method is
       * {@code null} and the interface method's return type is
       * primitive, then a {@code NullPointerException} will be
       * thrown by the method invocation on the proxy instance.  If the
       * value returned by this method is otherwise not compatible with
       * the interface method's declared return type as described above,
       * a {@code ClassCastException} will be thrown by the method
       * invocation on the proxy instance.
       * 【从代理实例上的方法调用返回的值。如果接口方法声明的返回类型是原始类型，则此方法返回的值必须是相应的原始包装器类的实例;否则，它必须是可赋值给声明的返回类型的类型。如果此方法返回的值为null，并且接口方法的返回类型为原始类型，则代理实例上的方法调用将抛出NullPointerException。如果此方法返回的值与上面描述的接口方法声明的返回类型不兼容，则代理实例上的方法调用将抛出ClassCastException。】
       *
       * @throws  Throwable the exception to throw from the method
       * invocation on the proxy instance.  The exception's type must be
       * assignable either to any of the exception types declared in the
       * {@code throws} clause of the interface method or to the
       * unchecked exception types {@code java.lang.RuntimeException}
       * or {@code java.lang.Error}.  If a checked exception is
       * thrown by this method that is not assignable to any of the
       * exception types declared in the {@code throws} clause of
       * the interface method, then an
       * {@link UndeclaredThrowableException} containing the
       * exception that was thrown by this method will be thrown by the
       * method invocation on the proxy instance.
       * 【要从代理实例上的方法调用中抛出的异常。异常的类型必须可以赋值给接口方法的抛出子句中声明的任何异常类型，或者赋值给未检查的异常类型java.lang.RuntimeException或java.lang.Error。如果此方法抛出的检查异常不能赋值给接口方法的throw子句中声明的任何异常类型，则代理实例上的方法调用将抛出包含此方法抛出的异常的未声明throwableexception。】
       *
       * @see     UndeclaredThrowableException
       */
      public Object invoke(Object proxy, Method method, Object[] args)
          throws Throwable;
  }
  ```
  
  在invoke方法中打一个断点，然后当代理对象dynamicProxyBuyHomeB调用buy（）方法后，结果如下所示
  
  ![image-20240513132831684](Java%E4%BB%A3%E7%90%86%E6%A8%A1%E5%BC%8F%E7%9A%84%E5%AE%9E%E7%8E%B0%E8%AF%A6%E8%A7%A3.assets/image-20240513132831684.png)
  
  其中这里会涉及到Method对象的invoke方法，可以参考文章:[java反射之Method的invoke方法实现]( https://blog.csdn.net/wenyuan65/article/details/81145900)



② 安全管理器相关内容

```java
final Class<?> caller = System.getSecurityManager() == null
                                    ? null
                                    : Reflection.getCallerClass();
```

其是一个三元操作符，首先通过调用System.getSecurityManager()判断是否存在安全管理器，如果结果是null，则返回null，如果结果不为null，则通过Reflection.getCallerClass() 获取调用者的类。



其他说明：

1、关于Reflection.getCallerClass()：

`Reflection.getCallerClass()`方法是Java反射API中的一个方法，用于获取调用当前方法的类或者代码的类。这个方法可以用于在运行时获取调用者的类信息，通常用于诊断、调试或者安全性检查等场景。

在Java 9及以后的版本中，`Reflection.getCallerClass()`方法被标记为过时（Deprecated），并建议使用其他方式来替代。这是因为在Java 9中，引入了模块化系统，对反射的访问做了一些限制，包括限制了对调用堆栈信息的访问，以提高安全性和性能。

因此，建议在开发新的代码时，尽量避免使用过时的`Reflection.getCallerClass()`方法，而是考虑使用其他更加安全和可靠的方式来获取调用者的类信息，例如通过堆栈跟踪（Stack Trace）或者传递参数的方式。

2、SecurityManager

安全管理器（Security Manager）是Java安全模型中的一个重要组件，用于控制Java应用程序对系统资源的访问和执行权限的判断。它提供了一种安全机制，帮助防止恶意代码对系统造成损害，同时保护系统资源免受未经授权的访问。

具体来说，安全管理器的主要作用包括：

- 权限控制：安全管理器可以对Java应用程序的各种操作进行权限控制，如文件操作、网络访问、线程管理等。它根据安全策略文件中定义的规则，对这些操作的权限进行判断，只有在被授权的情况下才允许执行。
- 安全检查：安全管理器可以对Java应用程序中的敏感操作进行安全检查，以防止潜在的安全漏洞或恶意行为。例如，它可以检查代码是否尝试访问系统资源、执行危险的操作，或者试图绕过安全检查进行非法操作。
- 保护系统资源：安全管理器可以保护系统资源免受未经授权的访问和滥用。通过限制应用程序对系统资源的访问权限，它可以防止恶意代码对系统造成损害，提高系统的安全性和稳定性。
- 定制安全策略：安全管理器允许开发人员根据实际需求定制安全策略，以适应不同的应用场景和安全需求。通过配置安全策略文件，开发人员可以定义允许或禁止的操作，从而实现对应用程序的细粒度权限控制。



③ 查找或生成指定的代理类及其构造函数

该方法源码如下所示

```java
 /**
 * Returns the {@code Constructor} object of a proxy class that takes a
 * single argument of type {@link InvocationHandler}, given a class loader
 * and an array of interfaces. The returned constructor will have the
 * {@link Constructor#setAccessible(boolean) accessible} flag already set.
 * 【返回代理类的构造函数对象，该代理类接受一个InvocationHandler类型的参数，给定一个类装入器和一个接口数组。返回的构造函数将已经设置了constructor #setAccessible(boolean) accessible标志。】
 *
 * @param   caller passed from a public-facing @CallerSensitive method if
 *                 SecurityManager is set or {@code null} if there's no
 *                 SecurityManager
 * 【如果设置了SecurityManager，则从面向公众的@CallerSensitive方法传递调用方;如果没有SecurityManager，则为空】
 *
 * @param   loader the class loader to define the proxy class
 * 【载入类加载器以定义代理类】
 *
 * @param   interfaces the list of interfaces for the proxy class
 *          to implement
 * 【接口代理类要实现的接口列表】
 *
 * @return  a Constructor of the proxy class taking single
 *          {@code InvocationHandler} parameter
 * 【带有单个InvocationHandler参数的代理类的构造函数】
 */
private static Constructor<?> getProxyConstructor(Class<?> caller,
                                                  ClassLoader loader,
                                                  Class<?>... interfaces)
{
    // optimization for single interface 对单个接口的优化
    if (interfaces.length == 1) {
        // 接口数量为1的处理
        
        // 获取当前接口的类对象
        Class<?> intf = interfaces[0];
        // 如果当前调用方的类对象存在，即有安全管理器，检查代理访问权限，检查调用者是否有权创建代理对象,详情说明见a
        if (caller != null) {
            checkProxyAccess(caller, loader, intf);
        }
        
        // 从proxyCache缓存中获取与给定接口intf相关联的代理类构造函数。如果缓存中不存在这样的构造函数，则使用computeIfAbsent方法计算并将其放入缓存中，详情说明见b
        return proxyCache.sub(intf).computeIfAbsent(
            loader,
            (ld, clv) -> new ProxyBuilder(ld, clv.key()).build()
        );
    } else {
        // 接口数量不为1的处理
        
        // interfaces cloned
        // 将接口数组进行克隆，以避免修改原始数组
        final Class<?>[] intfsArray = interfaces.clone();
        
       // 如果当前调用方的类对象存在，即有安全管理器，检查代理访问权限，检查调用者是否有权创建代理对象,详情说明见a
        if (caller != null) {
            checkProxyAccess(caller, loader, intfsArray);
        }
        // 将克隆后的接口数组转换为列表
        final List<Class<?>> intfs = Arrays.asList(intfsArray);
        
         // 从proxyCache缓存中获取与给定接口intf相关联的代理类构造函数。如果缓存中不存在这样的构造函数，则使用computeIfAbsent方法计算并将其放入缓存中，详情说明见b
        return proxyCache.sub(intfs).computeIfAbsent(
            loader,
            (ld, clv) -> new ProxyBuilder(ld, clv.key()).build()
        );
    }
}
```

a、检查代理访问权限

可以看到不管是单个接口还是多个接口，都会去进行判断调用方类对象是否存在，如果存在，就会去检查调用者是否有权创建代理对象。

具体的源码挖掘先在这里省略，不作为重点，有兴趣的可以自己挖掘。



b、获取类构造函数

可以看到不管是单个接口还是多个接口，都会从proxyCache缓存中获取与给定接口intf相关联的代理类构造函数。如果缓存中不存在这样的构造函数，则使用computeIfAbsent方法计算并将其放入缓存中。

首先proxyCache在该类中定义，定义的语句如下所示

```java
 /**
 * a cache of proxy constructors with
 * {@link Constructor#setAccessible(boolean) accessible} flag already set
 * 私有静态的proxyCache变量
 * 存储代理构造函数的缓存，而且这些构造函数的accessible标志已经设置为true，即允许绕过访问权限检查
 */
private static final ClassLoaderValue<Constructor<?>> proxyCache =
    new ClassLoaderValue<>();
```

首先调用proxyCache的sub方法，以指定的接口作为入参，在缓存中搜索与给定接口相关的值，并返回它们。调用computeIfAbsent方法，如果没有在缓存中找到对应的值，则通过new ProxyBuilder(ld, clv.key()）方法，新建一个代理类构造函数。ld` 代表 `loader，clv是与 `loader` 相关的某种缓存值。

关于new ProxyBuilder(ld, clv.key()）源码如下所示

```java
// loader：目标类的类加载器
// interfaces：接口代理类要实现的接口列表
ProxyBuilder(ClassLoader loader, List<Class<?>> interfaces) {
    // 检查虚拟机的模块系统是否已经初始化完成
    if (!VM.isModuleSystemInited()) {
        // 如果模块系统尚未完全初始化，就会抛出一个 InternalError 异常，其中包含一条消息指示代理（Proxy）功能不受支持
        throw new InternalError("Proxy is not supported until "
                + "module system is fully initialized");
    }
    
    // 判断要实现的接口数量是否超出限制范围
    if (interfaces.size() > 65535) {
        // 如果 interfaces 集合中的接口数量超过了 65535，就会抛出一个 IllegalArgumentException 异常，其中包含一条消息指示接口数量超过了限制。
        throw new IllegalArgumentException("interface limit exceeded: "
                + interfaces.size());
    }
	
    // 获取所有代理接口的公共非静态方法所引用的除原始类型的方法签名引用类型的类对象存入集合中
    // 方法签名引用类型：包括返回类型、参数类型、可能抛出的异常类型
    // 通过isStatic方法，判断是否为静态
    // 通过isPrimitive方法，判断是否为原始类型
    Set<Class<?>> refTypes = referencedTypes(loader, interfaces);

    // IAE if violates any restrictions specified in newProxyInstance
    // 验证代理接口是否满足在 newProxyInstance 方法中指定的任何限制。如果代理接口不满足限制，将抛出相应的异常
    /*
    	验证内容如下：
    	1、创建了一个 IdentityHashMap 对象 interfaceSet，用于存储代理对象所要实现的接口，并初始化其大小为接口列表的大小
    	2、遍历代理对象要实现的接口列表IdentityHashMap，对每个接口进行验证
    		- 使用 ensureVisible 方法确保类加载器能够解析接口的名称，并将其映射到相同的 Class 对象（详情可见5.1）
    		- 使用 isInterface() 方法验证当前类对象是否表示一个接口，如果不是，则抛出 IllegalArgumentException 异常 (详情见5.2)
    		- 确保当前接口不是重复的，如果是重复的，则抛出 IllegalArgumentException 异常 (详情见5.3)
    	3、通过ensureVisible 方法确保类加载器能够解析refTypes方法签名引用类型的名称，并将其映射到相同的 Class 对象
    */
    validateProxyInterfaces(loader, interfaces, refTypes);
	
    // 将构造函数参数 interfaces 赋值给类的成员变量 this.interfaces
    this.interfaces = interfaces;
    // 调用 mapToModule 方法，将类加载器、接口列表和引用类型集合映射到一个模块上，并将返回的模块赋值给类的成员变量 this.module
    this.module = mapToModule(loader, interfaces, refTypes);
    // 使用断言来验证获取到的模块的类加载器是否与传入的类加载器相同。如果不相同，可能存在错误
    // 如果条件为真，那么程序会继续正常执行；但如果条件为假，断言就会失败，并抛出一个 AssertionError 异常
    assert getLoader(module) == loader;
}
```

到此，创建了一个代理构建器（ProxyBuilder）的实例对象，并将其加入到了proxyCache缓存中。



④创建代理对象实例

源码如下所示

```java
/*
	caller：调用方的 Class 对象，如果没有安全管理器，则为 null
	cons：代理类的构造函数
	h：代理对象的调用处理器
*/
private static Object newProxyInstance(Class<?> caller, // null if no SecurityManager
                                           Constructor<?> cons,
                                           InvocationHandler h) {
    /*
     * Invoke its constructor with the designated invocation handler.
     */
    try {
        // checkNewProxyPermission 方法检查了创建代理对象的权限。这是 Java 安全管理器的一部分，用于确保调用方有权限创建代理对象
        if (caller != null) {
            checkNewProxyPermission(caller, cons.getDeclaringClass());
        }
		// cons.newInstance(new Object[]{h}) 创建了一个新的代理对象。这里的 cons 是代理类的构造函数，h 是代理对象的调用处理器。通过调用构造函数并传入调用处理器，创建了一个代理对象。
        return cons.newInstance(new Object[]{h});
    } catch (IllegalAccessException | InstantiationException e) {
        throw new InternalError(e.toString(), e);
    } catch (InvocationTargetException e) {
        Throwable t = e.getCause();
        if (t instanceof RuntimeException) {
            throw (RuntimeException) t;
        } else {
            throw new InternalError(t.toString(), t);
        }
    }
}
```



### 4.4、查看代理对象信息

我们在测试类中添加如下代码，将生成的代理对象保存到工作目录下

```java
//新版本 jdk产生代理类
System.getProperties().put("jdk.proxy.ProxyGenerator.saveGeneratedFiles", "true");

// 如果上述代码加上不生效可以考虑加下下面的代码：
 // 老版本jdk
System.getProperties().put("sun.misc.ProxyGenerator.saveGeneratedFiles", "true");
//  该设置用于输出cglib动态代理产生的类
System.setProperty(DebuggingClassWriter.DEBUG_LOCATION_PROPERTY, "C:\\class"); 
```

再次运行测试类代码

```java
public class BuyHomeDynamicProxyTest {
    public static void main(String[] args) {

        //新版本 jdk产生代理类
        // 指示 JDK 在生成动态代理类时将其保存到文件系统中，通常是在当前工作目录下的 com/sun/proxy 目录中，而不是在内存中动态生成
        System.getProperties().put("jdk.proxy.ProxyGenerator.saveGeneratedFiles", "true");

        // 创建A同学对象
        BuyHomeA buyHomeA = new BuyHomeA();
        // 通过动态代理创建中介B对象
        MyInvocationHandler myInvocationHandler = new MyInvocationHandler(buyHomeA);
        BuyHomeObject dynamicProxyBuyHomeB  = (BuyHomeObject) Proxy.newProxyInstance(BuyHomeA.class.getClassLoader(),
                BuyHomeA.class.getInterfaces(), myInvocationHandler);
        // 中介B帮忙寻找、购买房源（购买肯定是A同学自己购买）
        dynamicProxyBuyHomeB.buy();
		
        // 打印代理类的类对象信息
        /*System.out.println("代理类："+ dynamicProxyBuyHomeB.getClass());*/

    }
}
```

可以在工作目录com/sun/proxy下找到我们输出的代理对象信息，内容如下所示

```java
//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.sun.proxy;

import com.yjy.Proxy.DynamicProxy.BuyHomeObject;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.lang.reflect.UndeclaredThrowableException;

// 集成Proxy 实现目标对象的基类BuyHomeObject
//  jdk 动态代理必须基于接口，因为java是单继承的，已经集成了Proxy
public final class $Proxy0 extends Proxy implements BuyHomeObject {
    private static Method m1;
    private static Method m2;
    private static Method m3;
    private static Method m0;

    public $Proxy0(InvocationHandler var1) throws  {
        super(var1);
    }

    public final boolean equals(Object var1) throws  {
        try {
            return (Boolean)super.h.invoke(this, m1, new Object[]{var1});
        } catch (RuntimeException | Error var3) {
            throw var3;
        } catch (Throwable var4) {
            throw new UndeclaredThrowableException(var4);
        }
    }

    public final String toString() throws  {
        try {
            return (String)super.h.invoke(this, m2, (Object[])null);
        } catch (RuntimeException | Error var2) {
            throw var2;
        } catch (Throwable var3) {
            throw new UndeclaredThrowableException(var3);
        }
    }

    public final void buy() throws  {
        try {
            // h即proxy类中的protected InvocationHandler h
            // 即调用调用处理程序的invoke方法
            super.h.invoke(this, m3, (Object[])null);
        } catch (RuntimeException | Error var2) {
            throw var2;
        } catch (Throwable var3) {
            throw new UndeclaredThrowableException(var3);
        }
    }

    public final int hashCode() throws  {
        try {
            return (Integer)super.h.invoke(this, m0, (Object[])null);
        } catch (RuntimeException | Error var2) {
            throw var2;
        } catch (Throwable var3) {
            throw new UndeclaredThrowableException(var3);
        }
    }

    static {
        try {
            m1 = Class.forName("java.lang.Object").getMethod("equals", Class.forName("java.lang.Object"));
            m2 = Class.forName("java.lang.Object").getMethod("toString");
            m3 = Class.forName("com.yjy.Proxy.DynamicProxy.BuyHomeObject").getMethod("buy");
            m0 = Class.forName("java.lang.Object").getMethod("hashCode");
        } catch (NoSuchMethodException var2) {
            throw new NoSuchMethodError(var2.getMessage());
        } catch (ClassNotFoundException var3) {
            throw new NoClassDefFoundError(var3.getMessage());
        }
    }
}

```



## 五、cglib动态代理

### 5.1、说明

上述jdk动态代理只能代理实现接口的类，如果想要对类实现代理我们可以通过cglib动态代理来解决关于类的动态代理。

> [CGLIB](https://github.com/cglib/cglib)(*Code Generation Library*)是一个基于[ASM](http://www.baeldung.com/java-asm)的字节码生成库，它允许我们在运行时对字节码进行修改和动态生成。CGLIB 通过继承方式实现代理。很多知名的开源框架都使用到了[CGLIB](https://github.com/cglib/cglib)， 例如 Spring 中的 AOP 模块中：如果目标对象实现了接口，则默认采用 JDK 动态代理，否则采用 CGLIB 动态代理。

优点：

1. 性能高： CGLIB 直接对字节码进行操作，相比于 JDK 动态代理的反射调用，性能更高。因为它通过生成子类的方式来代理目标类，而不是通过实现接口的方式。
2. 不需要目标对象实现接口： JDK 动态代理要求目标对象必须实现接口，而 CGLIB 可以代理没有实现接口的类。
3. 更强大的功能： CGLIB 不仅可以代理类的方法，还可以代理类的属性。
4. 更灵活： CGLIB 可以代理没有公共构造方法的类，以及被 `final` 修饰的类的方法。

缺点：

1. 性能相对 JDK 动态代理更低： 尽管 CGLIB 的性能较高，但相比于直接调用目标方法，仍然存在一定的性能开销。而且生成的代理类会增加类加载的时间和内存消耗。
2. 类加载器敏感： CGLIB 动态代理生成的代理类是目标类的子类，因此可能会受到类加载器的限制。在一些场景下，例如使用不同的类加载器加载目标类和代理类时，可能会出现类转换异常。
3. 无法代理 final 方法和 private 方法： CGLIB 无法代理 `final` 方法和 `private` 方法，因为它是通过生成子类来代理目标类的方法，而 `final` 方法和 `private` 方法无法被子类重写。
4. Debugging困难： 由于 CGLIB 是在运行时生成字节码来创建代理类，因此调试起来可能会比较困难，不如 JDK 动态代理那样直观。

### 5.2、场景实现

**模拟场景**：同学A想要买房，但是他不了解如何去找好的房源，因此其委托中介B去帮忙完成寻找房源的过程。

**具体实现:*

引入cglib依赖

```xml
<dependency>
    <groupId>cglib</groupId>
    <artifactId>cglib</artifactId>
    <version>3.3.0</version>
</dependency>
```

创建买房对象同学A

```java
/**
 * 买房对象 同学A
 * @author banana
 * @create 2024-05-12 15:18
 */
public class BuyHomeA implements BuyHomeObject {
    // 同学A的购买方法
    @Override
    public void buy() {
        System.out.println("同学A付买房费用……");
    }
}

```

创建一个自定义 MethodInterceptor

```java
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

import java.lang.reflect.Method;

/**
 * @author banana
 * @create 2024-05-13 22:28
 */
public class MyInterceptor implements MethodInterceptor {
    @Override
    public Object intercept(Object o, Method method, Object[] objects, MethodProxy methodProxy) throws Throwable {
        preBuy();
        Object object = methodProxy.invokeSuper(o, objects);
        afterBuy();
        return object;
    }

    // 购买前操作
    public void preBuy() {
        System.out.println("中介收取前期费用……");
        System.out.println("中介找到适合的房源……");
    }

    // 购买后操作
    public void afterBuy() {
        System.out.println("中介收取后期费用……");
    }
}
```

创建测试类进行测试

```java
/**
 * 测试类
 * @author banana
 * @create 2024-05-13 22:29
 */
public class CglibProxyTest {
    public static void main(String[] args) {
        // //在指定目录下生成动态代理类
        System.setProperty(DebuggingClassWriter.DEBUG_LOCATION_PROPERTY, "C:\\class");
        
        //创建Enhancer对象，类似于JDK动态代理的Proxy类，下一步就是设置几个参数
        Enhancer enhancer = new Enhancer();
        //设置目标类的字节码文件
        enhancer.setSuperclass(BuyHomeA.class);
        //设置回调函数
        enhancer.setCallback(new MyInterceptor());
        //这里的creat方法就是正式创建代理类
        BuyHomeA buyHomeA = (BuyHomeA)enhancer.create();
        //调用代理类的buy方法
        buyHomeA.buy();
        
        // 打印代理类的类对象信息
        System.out.println("cglib动态代理："+ buyHomeA.getClass());
    }
}
```

运行结果

```
中介收取前期费用……
中介找到适合的房源……
同学A付买房费用……
中介收取后期费用……
cglib动态代理：class com.yjy.Proxy.CglibProxy.BuyHomeA$$EnhancerByCGLIB$$c382daf2
```

### 5.3、实现原理

略，有空补充

### 5.4、查看代理对象信息

```java
//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.yjy.Proxy.CglibProxy;

import java.lang.reflect.Method;
import net.sf.cglib.core.ReflectUtils;
import net.sf.cglib.core.Signature;
import net.sf.cglib.proxy.Callback;
import net.sf.cglib.proxy.Factory;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

// 其集成了目标啊对象 实现了Factory
public class BuyHomeA$$EnhancerByCGLIB$$c382daf2 extends BuyHomeA implements Factory {
    private boolean CGLIB$BOUND;
    public static Object CGLIB$FACTORY_DATA;
    private static final ThreadLocal CGLIB$THREAD_CALLBACKS;
    private static final Callback[] CGLIB$STATIC_CALLBACKS;
    private MethodInterceptor CGLIB$CALLBACK_0;
    private static Object CGLIB$CALLBACK_FILTER;
    private static final Method CGLIB$buy$0$Method;
    private static final MethodProxy CGLIB$buy$0$Proxy;
    private static final Object[] CGLIB$emptyArgs;
    private static final Method CGLIB$equals$1$Method;
    private static final MethodProxy CGLIB$equals$1$Proxy;
    private static final Method CGLIB$toString$2$Method;
    private static final MethodProxy CGLIB$toString$2$Proxy;
    private static final Method CGLIB$hashCode$3$Method;
    private static final MethodProxy CGLIB$hashCode$3$Proxy;
    private static final Method CGLIB$clone$4$Method;
    private static final MethodProxy CGLIB$clone$4$Proxy;

    static void CGLIB$STATICHOOK1() {
        CGLIB$THREAD_CALLBACKS = new ThreadLocal();
        CGLIB$emptyArgs = new Object[0];
        Class var0 = Class.forName("com.yjy.Proxy.CglibProxy.BuyHomeA$$EnhancerByCGLIB$$c382daf2");
        Class var1;
        CGLIB$buy$0$Method = ReflectUtils.findMethods(new String[]{"buy", "()V"}, (var1 = Class.forName("com.yjy.Proxy.CglibProxy.BuyHomeA")).getDeclaredMethods())[0];
        CGLIB$buy$0$Proxy = MethodProxy.create(var1, var0, "()V", "buy", "CGLIB$buy$0");
        Method[] var10000 = ReflectUtils.findMethods(new String[]{"equals", "(Ljava/lang/Object;)Z", "toString", "()Ljava/lang/String;", "hashCode", "()I", "clone", "()Ljava/lang/Object;"}, (var1 = Class.forName("java.lang.Object")).getDeclaredMethods());
        CGLIB$equals$1$Method = var10000[0];
        CGLIB$equals$1$Proxy = MethodProxy.create(var1, var0, "(Ljava/lang/Object;)Z", "equals", "CGLIB$equals$1");
        CGLIB$toString$2$Method = var10000[1];
        CGLIB$toString$2$Proxy = MethodProxy.create(var1, var0, "()Ljava/lang/String;", "toString", "CGLIB$toString$2");
        CGLIB$hashCode$3$Method = var10000[2];
        CGLIB$hashCode$3$Proxy = MethodProxy.create(var1, var0, "()I", "hashCode", "CGLIB$hashCode$3");
        CGLIB$clone$4$Method = var10000[3];
        CGLIB$clone$4$Proxy = MethodProxy.create(var1, var0, "()Ljava/lang/Object;", "clone", "CGLIB$clone$4");
    }

    final void CGLIB$buy$0() {
        super.buy();
    }

    public final void buy() {
        MethodInterceptor var10000 = this.CGLIB$CALLBACK_0;
        if (var10000 == null) {
            CGLIB$BIND_CALLBACKS(this);
            var10000 = this.CGLIB$CALLBACK_0;
        }

        if (var10000 != null) {
            /*
            调用intercept()方法，intercept()方法由自定义MyInterceptor实现，所以，最后调用MyInterceptor中的intercept()方法，从而完成了由代理对象访问到目标对象的动态代理实现
            */
            var10000.intercept(this, CGLIB$buy$0$Method, CGLIB$emptyArgs, CGLIB$buy$0$Proxy);
        } else {
            super.buy();
        }
    }

    final boolean CGLIB$equals$1(Object var1) {
        return super.equals(var1);
    }

    public final boolean equals(Object var1) {
        MethodInterceptor var10000 = this.CGLIB$CALLBACK_0;
        if (var10000 == null) {
            CGLIB$BIND_CALLBACKS(this);
            var10000 = this.CGLIB$CALLBACK_0;
        }

        if (var10000 != null) {
            Object var2 = var10000.intercept(this, CGLIB$equals$1$Method, new Object[]{var1}, CGLIB$equals$1$Proxy);
            return var2 == null ? false : (Boolean)var2;
        } else {
            return super.equals(var1);
        }
    }

    final String CGLIB$toString$2() {
        return super.toString();
    }

    public final String toString() {
        MethodInterceptor var10000 = this.CGLIB$CALLBACK_0;
        if (var10000 == null) {
            CGLIB$BIND_CALLBACKS(this);
            var10000 = this.CGLIB$CALLBACK_0;
        }

        return var10000 != null ? (String)var10000.intercept(this, CGLIB$toString$2$Method, CGLIB$emptyArgs, CGLIB$toString$2$Proxy) : super.toString();
    }

    final int CGLIB$hashCode$3() {
        return super.hashCode();
    }

    public final int hashCode() {
        MethodInterceptor var10000 = this.CGLIB$CALLBACK_0;
        if (var10000 == null) {
            CGLIB$BIND_CALLBACKS(this);
            var10000 = this.CGLIB$CALLBACK_0;
        }

        if (var10000 != null) {
            Object var1 = var10000.intercept(this, CGLIB$hashCode$3$Method, CGLIB$emptyArgs, CGLIB$hashCode$3$Proxy);
            return var1 == null ? 0 : ((Number)var1).intValue();
        } else {
            return super.hashCode();
        }
    }

    final Object CGLIB$clone$4() throws CloneNotSupportedException {
        return super.clone();
    }

    protected final Object clone() throws CloneNotSupportedException {
        MethodInterceptor var10000 = this.CGLIB$CALLBACK_0;
        if (var10000 == null) {
            CGLIB$BIND_CALLBACKS(this);
            var10000 = this.CGLIB$CALLBACK_0;
        }

        return var10000 != null ? var10000.intercept(this, CGLIB$clone$4$Method, CGLIB$emptyArgs, CGLIB$clone$4$Proxy) : super.clone();
    }

    public static MethodProxy CGLIB$findMethodProxy(Signature var0) {
        String var10000 = var0.toString();
        switch(var10000.hashCode()) {
        case -1377614033:
            if (var10000.equals("buy()V")) {
                return CGLIB$buy$0$Proxy;
            }
            break;
        case -508378822:
            if (var10000.equals("clone()Ljava/lang/Object;")) {
                return CGLIB$clone$4$Proxy;
            }
            break;
        case 1826985398:
            if (var10000.equals("equals(Ljava/lang/Object;)Z")) {
                return CGLIB$equals$1$Proxy;
            }
            break;
        case 1913648695:
            if (var10000.equals("toString()Ljava/lang/String;")) {
                return CGLIB$toString$2$Proxy;
            }
            break;
        case 1984935277:
            if (var10000.equals("hashCode()I")) {
                return CGLIB$hashCode$3$Proxy;
            }
        }

        return null;
    }

    public BuyHomeA$$EnhancerByCGLIB$$c382daf2() {
        CGLIB$BIND_CALLBACKS(this);
    }

    public static void CGLIB$SET_THREAD_CALLBACKS(Callback[] var0) {
        CGLIB$THREAD_CALLBACKS.set(var0);
    }

    public static void CGLIB$SET_STATIC_CALLBACKS(Callback[] var0) {
        CGLIB$STATIC_CALLBACKS = var0;
    }

    private static final void CGLIB$BIND_CALLBACKS(Object var0) {
        BuyHomeA$$EnhancerByCGLIB$$c382daf2 var1 = (BuyHomeA$$EnhancerByCGLIB$$c382daf2)var0;
        if (!var1.CGLIB$BOUND) {
            var1.CGLIB$BOUND = true;
            Object var10000 = CGLIB$THREAD_CALLBACKS.get();
            if (var10000 == null) {
                var10000 = CGLIB$STATIC_CALLBACKS;
                if (var10000 == null) {
                    return;
                }
            }

            var1.CGLIB$CALLBACK_0 = (MethodInterceptor)((Callback[])var10000)[0];
        }

    }

    public Object newInstance(Callback[] var1) {
        CGLIB$SET_THREAD_CALLBACKS(var1);
        BuyHomeA$$EnhancerByCGLIB$$c382daf2 var10000 = new BuyHomeA$$EnhancerByCGLIB$$c382daf2();
        CGLIB$SET_THREAD_CALLBACKS((Callback[])null);
        return var10000;
    }

    public Object newInstance(Callback var1) {
        CGLIB$SET_THREAD_CALLBACKS(new Callback[]{var1});
        BuyHomeA$$EnhancerByCGLIB$$c382daf2 var10000 = new BuyHomeA$$EnhancerByCGLIB$$c382daf2();
        CGLIB$SET_THREAD_CALLBACKS((Callback[])null);
        return var10000;
    }

    public Object newInstance(Class[] var1, Object[] var2, Callback[] var3) {
        CGLIB$SET_THREAD_CALLBACKS(var3);
        BuyHomeA$$EnhancerByCGLIB$$c382daf2 var10000 = new BuyHomeA$$EnhancerByCGLIB$$c382daf2;
        switch(var1.length) {
        case 0:
            var10000.<init>();
            CGLIB$SET_THREAD_CALLBACKS((Callback[])null);
            return var10000;
        default:
            throw new IllegalArgumentException("Constructor not found");
        }
    }

    public Callback getCallback(int var1) {
        CGLIB$BIND_CALLBACKS(this);
        MethodInterceptor var10000;
        switch(var1) {
        case 0:
            var10000 = this.CGLIB$CALLBACK_0;
            break;
        default:
            var10000 = null;
        }

        return var10000;
    }

    public void setCallback(int var1, Callback var2) {
        switch(var1) {
        case 0:
            this.CGLIB$CALLBACK_0 = (MethodInterceptor)var2;
        default:
        }
    }

    public Callback[] getCallbacks() {
        CGLIB$BIND_CALLBACKS(this);
        return new Callback[]{this.CGLIB$CALLBACK_0};
    }

    public void setCallbacks(Callback[] var1) {
        this.CGLIB$CALLBACK_0 = (MethodInterceptor)var1[0];
    }

    static {
        CGLIB$STATICHOOK1();
    }
}

```



## 六、补充

### 5.1、ensureVisible方法确保类加载器能够解析接口的名称，并将其映射到相同的 Class 对象

其源码如下所示

```java
/*
 * Ensure the given class is visible to the class loader.
 * 确保给定的类对类装入器可见。
 * ld：类加载器
 * c：代理类要实现的接口
 */
private static void ensureVisible(ClassLoader ld, Class<?> c) {
    Class<?> type = null;
    try {
        // 使用 Class.forName() 方法尝试在给定的类加载器 ld 中加载与指定类名 c.getName() 对应的类
        // 说明：每个类加载器都维护着一个类加载环境，用于加载和管理类。
        type = Class.forName(c.getName(), false, ld);
    } catch (ClassNotFoundException e) {
    }
    if (type != c) {
        // 加载的类对象 type 不等于原始类对象 c，则说明在给定的类加载器中未找到相应的类，即原始类对象 c 对于给定的类加载器不可见
        throw new IllegalArgumentException(c.getName() +
                " referenced from a method is not visible from class loader");
    }
}

```

关于Class类的forName静态方法：

`Class.forName()` 方法的作用是通过类的全限定名（Fully Qualified Name）加载并返回对应的 `Class` 对象

- `public static Class<?> forName(String className) throws ClassNotFoundException`

这个方法接受一个字符串参数 `className`，该参数是要加载的类的全限定名。全限定名包括包名和类名，例如 `"java.lang.String"`。它会尝试在默认的类加载器中加载指定名称的类，并返回对应的 `Class` 对象。如果找不到该类，则抛出 `ClassNotFoundException` 异常。

- `public static Class<?> forName(String className, boolean initialize, ClassLoader loader) throws ClassNotFoundException`

  - `className`：要加载的类的全限定名。

  - `initialize`：一个布尔值，指示是否立即初始化加载的类。如果为 `true`，则在加载类之后会立即执行其静态初始化块（如果有）。如果为 `false`，则只加载类而不执行静态初始化块(静态初始化是指类加载时执行的一段代码，它主要用于初始化静态成员变量和执行静态代码块)。一般情况下，建议将其设置为 `false`。

  - `loader`：一个类加载器对象，用于加载指定类。如果为 `null`，则使用调用 `forName` 方法的类的类加载器。



### 5.2、isInterface方法验证当前类对象是否表示一个接口

其在Proxy类的validateProxyInterfaces方法中被调用

```java
/*
 * Verify that the Class object actually represents an
 * interface.
 */
if (!intf.isInterface()) {
    throw new IllegalArgumentException(intf.getName() + " is not an interface");
}
```

isInterface是Class对象的方法，其内部实现如下所示

```java
 /**
 * Determines if the specified {@code Class} object represents an
 * interface type.
 *
 * @return  {@code true} if this object represents an interface;
 *          {@code false} otherwise.
 */
@HotSpotIntrinsicCandidate
public native boolean isInterface();
```

注释中的 `@HotSpotIntrinsicCandidate` 是一个注解，表示该方法是 HotSpot 虚拟机中的内置方法，可以由虚拟机进行优化处理。这个注解是为了帮助 JVM 进行优化，提高方法的执行效率。



### 5.3、确保当前接口不是重复的

```java
 /*
 * Verify that this interface is not a duplicate.
 */
if (interfaceSet.put(intf, Boolean.TRUE) != null) {
    throw new IllegalArgumentException("repeated interface: " + intf.getName());
}
```

put方法被调用，将接口 intf 作为键，Boolean.TRUE 作为值放入集合中。由于 Set 是一个无序集合，并且不允许重复元素，所以 put 方法的主要作用是向集合中添加元素，并返回添加操作的结果,如果有重复的数据，其返回值不为null，则抛出异常，这里`Boolean.TRUE` 作为值是为了简单地表示该接口存在于集合中，而不关心具体的值。



## 七、问题

### 6.1、关于clv的来源

```java
 return proxyCache.sub(intfs).computeIfAbsent(
                loader,
                (ld, clv) -> new ProxyBuilder(ld, clv.key()).build()
            );
```

### 6.2、关于④创建代理对象实例深挖

略

### 6.3、如果被代理的对象有多个方法呢，那生成的代理对象的InvocationHandler实现类要怎么写

如果被代理的对象有多个方法，那么生成的代理对象的 `InvocationHandler` 实现类需要根据被代理对象的方法数量和逻辑来进行编写。

通常情况下，你可以按照以下步骤编写 `InvocationHandler` 实现类：

1. 实现 `InvocationHandler` 接口，并重写 `invoke` 方法。
2. 在 `invoke` 方法中根据传入的方法名和参数来区分不同的方法调用，并根据业务逻辑进行处理。
3. 如果被代理的对象有多个方法，你可能需要在 `invoke` 方法中使用 `if` 或 `switch` 语句来区分不同的方法调用，并分别处理每个方法的逻辑。

举个例子，假设被代理的对象是一个名为 `ExampleService` 的接口，其中有两个方法：`void method1()` 和 `String method2(int param)`，那么你可以这样编写 `InvocationHandler` 实现类：

```java
javaCopy Codeimport java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

public class ExampleInvocationHandler implements InvocationHandler {

    private ExampleService target;

    public ExampleInvocationHandler(ExampleService target) {
        this.target = target;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        String methodName = method.getName();
        switch (methodName) {
            case "method1":
                // 处理 method1 的逻辑
                target.method1();
                return null;
            case "method2":
                // 处理 method2 的逻辑
                int param = (int) args[0];
                return target.method2(param);
            default:
                throw new UnsupportedOperationException("Method not supported: " + methodName);
        }
    }
}
```

在这个例子中，`invoke` 方法根据方法名来区分不同的方法调用，并调用相应的被代理对象的方法。你可以根据实际情况修改和扩展这个 `invoke` 方法，以满足你的业务需求。



把生成的代理类展示一下！