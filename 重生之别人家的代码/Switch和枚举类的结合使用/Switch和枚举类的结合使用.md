# Switch和枚举类的结合使用

## 一、关于枚举类（ENUM）

### 1.1 概念

- `enum` 是 Java 中用来定义枚举类型的关键字，是一种特殊的类。
- 它有固定数量的预定义的实例，每个实例都是枚举类型的唯一实例，这些实例在程序运行时是不可更改的。每个枚举常量在枚举类型中都是唯一的实例。

- 枚举常量通常在编译时就被创建，它们在整个程序生命周期内是不可变的。
- 枚举类型天然支持单例模式（由于枚举常量是唯一的实例，并且在枚举类型加载时就被创建，这种特性类似于单例模式的实现。在使用枚举时，每个枚举常量本身就是一个单例对象），因此枚举常量之间可以使用 == 运算符进行比较（直接比较各个示例的引用地址来判断），而不需要使用 .equals() 方法。
- 枚举类型不能继承其他类，但可以实现接口，并且枚举常量可以有字段、方法和构造函数。

### 1.2 定义示例

```java
public enum ConsultTypeEnum {
    
    PHONE("Phone consultation"),
    VIDEO("Video consultation"),
    IN_PERSON("In-person consultation")
        
    private final String description;

    private ConsultType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
```

使用注解版本

```java
@AllArgsConstructor
@Getter
public enum ConsultTypeEnum {
    
    PHONE("Phone consultation"),
    VIDEO("Video consultation"),
    IN_PERSON("In-person consultation");
        
    private final String description;
}
```

### 1.3 使用普通类来实现枚举类

```java
public class ConsultType {
    // 多个实例声明
    public static final ConsultType PHONE = new ConsultType("Phone consultation");
    public static final ConsultType VIDEO = new ConsultType("Video consultation");
    public static final ConsultType IN_PERSON = new ConsultType("In-person consultation");

    private final String description;

    private ConsultType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}

```

### 1.4 相关知识点

#### 1. 关于静态变量和非静态变量

- 存储位置和生命周期
  - 静态变量：存储在静态存储区域，在程序生命周期内只被分配一次内存。即使没有创建类的实例，静态变量也可以使用。它们的生命周期与类的生命周期相同。
  - 非静态变量（实例变量）：每个类的实例都有自己的一份，它们存储在堆或栈中，具体取决于编程语言和实现。

- 访问权限
  - 静态变量：可以直接通过类名来访问（就类名.静态变量名）
  - 非静态变量：需要通过类的实例（对象）来进行访问
- 初始化
  - 静态变量：通常在类加载的时候进行初始化，只会初始化一次。可以在声明时或者静态初始化块中进行初始化。静态变量在没有显式初始化时，会被赋予默认值。这些默认值通常依赖于变量的数据类型。
  - 非静态变量：在每个对象创建时进行初始化，每个对象的实例变量可以有不同的值，可以在声明时或构造函数中初始化
- 共享性
  - 静态变量：所有该类的实例共享，因此当一个实例改变了这个静态变量的值，其他实例访问到的值也会发生变化。
  - 非静态变量：每个实例独立拥有的，一个实例的变化不会影响其他实例的变量。



## 二、关于Switch

### 2.1 Switch语句

```java
switch (number) {
    case 1:
        // 执行代码
        break;
    case 2:
        // 执行代码
        break;
    default:
        // 执行代码
        break;
}

```

- `switch (number)` 中的 `type` 是被检查的变量（表达式可以是整数类型（`byte`, `short`, `char`, `int`) 或枚举类型。从Java 7开始，也可以是字符串类型（`String`）。对于其他类型的表达式，如布尔类型（`boolean`）、浮点数类型（`float`, `double`）或者对象类型，是不允许作为`switch`的表达式的。）
- `case 1:` 是一个分支，当 `number` 的值为 `1` 时，执行紧随其后的代码块。
- `break;` 表示跳出 `switch` 语句，避免执行下一个分支。
- `default:` 是可选的，当 `number` 不匹配任何 `case` 时执行的代码块，执行内部代码。



## 三、Switch和枚举类的结合

```java
public class ConsultTypeExample {

    // 假设 ConsultType 是一个枚举类型
    enum ConsultType {
        PHONE,
        VIDEO,
        IN_PERSON
    }

    public static void main(String[] args) {
        ConsultType type = ConsultType.PHONE;

        switch (type) {
            case PHONE:
                System.out.println("Initiating phone consultation...");
                break;
            case VIDEO:
                System.out.println("Setting up video consultation...");
                break;
            case IN_PERSON:
                System.out.println("Preparing for in-person consultation...");
                break;
            default:
                System.out.println("Consultation type not recognized.");
                break;
        }
    }
}

```

