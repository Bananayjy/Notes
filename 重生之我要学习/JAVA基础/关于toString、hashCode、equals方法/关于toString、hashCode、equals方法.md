# 一、关于toString、hashCode、equals方法

## 1.1 自动实现重写方式

当我们在类上添加lombok的@Data注解后，除了会为类中的各个字段添加get和set方法外，还会自动重写类中的toString、hashCode、equals方法，具体如下所示

```java
@Data
public class Student{
    
    private String name;

    private Integer age;
}
```

编译后的类文件内容如下所示

```java
public class Student {
    private String name;
    private Integer age;

    // 空参构造器（默认）
    public Student() {
    }

    // getter 和 setter
    public String getName() {
        return this.name;
    }

    public Integer getAge() {
        return this.age;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public void setAge(final Integer age) {
        this.age = age;
    }

    // 重新equals方法
    public boolean equals(final Object o) {
        if (o == this) {
            return true;
        } else if (!(o instanceof Student)) {
            return false;
        } else {
            Student other = (Student)o;
            if (!other.canEqual(this)) {
                return false;
            } else {
                Object this$age = this.getAge();
                Object other$age = other.getAge();
                if (this$age == null) {
                    if (other$age != null) {
                        return false;
                    }
                } else if (!this$age.equals(other$age)) {
                    return false;
                }

                Object this$name = this.getName();
                Object other$name = other.getName();
                if (this$name == null) {
                    if (other$name != null) {
                        return false;
                    }
                } else if (!this$name.equals(other$name)) {
                    return false;
                }

                return true;
            }
        }
    }
	
    // 检查传入的对象是否是当前对象的相同类型
    // 通常与equals(Object obj)方法一起使用，用于确保在进行对象比较时只比较相同类型的对象
    protected boolean canEqual(final Object other) {
        return other instanceof Student;
    }
    
	// 重新hashCode方法
    public int hashCode() {
        int PRIME = true;
        int result = 1;
        Object $age = this.getAge();
        int result = result * 59 + ($age == null ? 43 : $age.hashCode());
        Object $name = this.getName();
        result = result * 59 + ($name == null ? 43 : $name.hashCode());
        return result;
    }
    
	// 重新toString方法
    public String toString() {
        return "Student(name=" + this.getName() + ", age=" + this.getAge() + ")";
    }
}
```

## 1.2、父类中实现

Java中所有对象的顶级父类都是Object类，如果在类的声明中没有使用extends关键字指明父类，则默认的父类为java.lang.Object类。

- 在Object中的toString方法实现

通过该方法可以获取对象的一种字符串形式，默认的字符串是全类名@十六进制内存地址（示例：com.example.DemoApplicationTests$Student@239b98cb）

通过getClass().getName() 获取全类名

通过hashCode() 获取当前类的哈希值

通过调用Integer.toHexString(hashCode())获取十六进制值

```java
public String toString() {
    return getClass().getName() + "@" + Integer.toHexString(hashCode());
}
```

关于Integer.toHexString(hashCode())的深挖见1.3部分



- 在Object中的hashCode方法实现

其是一个native本地方法（该方法的具体实现是由本地代码（通常是C或C++）编写，Java虚拟机（JVM）会在运行时加载本地方法的实现，以执行实际的操作），用于返回对象的哈希码，它是一个32位的整数值。

哈希码值在集合类（如HashMap、HashSet等）中广泛使用，用于确定对象在集合中的存储位置。根据Java规范，如果两个对象使用equals方法判断为相等，那么它们的hashCode值也必须相等。反之，如果两个对象的hashCode值相等，不一定代表它们相等，只是有可能相等。（即通过hashCode方法，判断两个对象是否在某个散列存储结构的同一个位置中，通过equals方法，判断两个在同一个散列存储结构位置的对象是否相同）

```java
public native int hashCode();
```



- 在Object中的equals方法实现

对于引用变量来说，==比较的是两个对象的地址值是否相同。

对于基本类型，==比较的其值大小。

一般我们会重写了equals方法，比如String的equals被重写后，比较的是字符值，另外重写了equlas后，也必须重写hashcode()方法，因为在Java中，equals和hashCode方法是配对使用的。

```java
public boolean equals(Object obj) {
    return (this == obj);
}
```

## 1.3、Integer.toHexString(hashCode())源码深挖

> 参考文章：https://blog.csdn.net/fyzzlz/article/details/79716825

### 1.源码说明

Integer.toHexString(hashCode())方法是在Object父类中toString方法中调用的，用于返回当前对象的类全限定名和其十六进制内存地址，具体内容如下所示：

```java
public String toString() {
    return getClass().getName() + "@" + Integer.toHexString(hashCode());
}
```

### 2.详细说明：

（1）getClass

Object中的方法，不能被子类重写覆盖（final），是一个本地方法（native），返回一个调用该方法对象所属类的Class对象。

```
public final native Class<?> getClass();
```

（2）getClass().getName()

是Java中`Class`类的一个方法，它用于获取表示类的名称的字符串，返回的是包含类的全限定名的字符串。

（3）Integer.toHexString(hashCode())

获取当前的哈希值作为参数，调用Integer的toHexString方法，以16进制无符号整数形式返回此哈希码的字符串表示形式。

toHexString方法的源码如下所示，其中会调用toUnsignedString0方法：

```java
public static String toHexString(int i) {
    return toUnsignedString0(i, 4);
}
```

toUnsignedString0方法源码如下所示

```java
private static String toUnsignedString0(int val, int shift) {
    // assert shift > 0 && shift <=5 : "Illegal shift value";
    // 计算val参数除去前导零后的有效位数
    //  Integer.size == 32  int类型的最大位数 （int在jvm中占4个字节，共32位）
    //  Integer.numberOfLeadingZeros 方法用来计算前导零的数量，详情见a
    // mag为有效二进制位数（即除去前导零后，i需要占用的位数）
    int mag = Integer.SIZE - Integer.numberOfLeadingZeros(val);
    // 计算i有效二进制位数（除去前导零数量）所需要占的字符数（即对应的16进制的有效位数，每4个二进制位代表一个16进制数）
    /*
    	可以分为三类：
    	1. 有效位数mag == 0 那么只需要（1）个字符（4位）
    	2. 有效位数mag % 4 == 0 那么需要（mg / 4）个字符
    	3. 有效位数mag % 4 != 0 呢么需要（mag / 4 + 1）个字符
    	表达式Math.max(((mag + (shift - 1)) / shift), 1)可以覆盖上述三种情况
    */	
    int chars = Math.max(((mag + (shift - 1)) / shift), 1);
    // 按照有效二进制位数，创建一个字符数组
    char[] buf = new char[chars];
	
    // 将val值以16进制形式存入buf数组中 详情见b
    formatUnsignedInt(val, shift, buf, 0, chars);

    // Use special constructor which takes over "buf".
    return new String(buf, true);
}
```

详情a：Integer.numberOfLeadingZeros

该方法的作用是使用二分法，计算输入Interge类型i的前导零数量（即左边第一个非零值的位置）。

关于有符号右移运算符和无符号右移运算符说明（左移运算符同理）：

- 有符号右移运算符(>>)

  - 将操作数的二进制表示向右移动指定的位数。

  - 对于正数，向右移动时用 0 填充左侧空出的位。

  - 对于负数，向右移动时用 1 填充左侧空出的位，以保持负数的负号不变。

- 无符号右移运算符(>>>)

  - 将操作数的二进制表示向右移动指定的位数。

  - 无论操作数是正数还是负数，都用 0 填充左侧空出的位，不考虑符号位。

- 在移位运算符的后面加上等号表示移位并赋值
  - 如>>=表示有符号的右移并赋值操作运算符

具体的源码如下所示：

其二分的方法是每一次通过二分法将位数分为前一部分后后一部分（如此时是32位，经过二分后，分成高16位和低16位），对前一部分是否全是前导零进行判断（即经过无符号右移操作后判断是否为0），如果前导零在后一部分（即经过无符号右移操作后不为0），就将后一部分的内容放到前一部分（通过左移运算赋值符进行操作），再次通过二分获取前一部分的高位，并进行判断，如此往复，一直二分到两位的时候。最后对最高位进行判断，将i无符号右移31位，结果为0，说明此时的最高位为0，此时 n 就是i的前导零位数，结果为1，则说明最高位不为0，返回 n-1，即前导零个数减1（注意初始的前导零个数为1）。因为integer的位数是固定的，并且每次二分的对象都是前一部分，因此其二分的步骤也是固定的，可以分为五个步骤，如下所示。

```java
public static int numberOfLeadingZeros(int i) {
    // 如果i是0，则表示前导零的个数就是Integer最大的位数，即32
    if (i == 0)return 32;
    // 变量n记录前导0个数，初始的前导0的个数为1
    int n = 1;
    // 二分计算前导零个数
    // 32位划分高16位和低16位，对高16位是否全是零进行判断
    if (i >>> 16 == 0) { 
        // 如果高16位全是0，那么前端0的个数至少有16个
        n += 16; 
        // 将低位部分放到高位，进行进行二分判断
        i <<= 16; 
    }
    // 16位划分高8位和低8位，对高8位是否全是零进行判断
    if (i >>> 24 == 0) { n +=  8; i <<=  8; }
    // 8位划分高4位和低4位，对高4位是否全是零进行判断
    if (i >>> 28 == 0) { n +=  4; i <<=  4; }
    // 8位划分高2位和低2位，对高2位是否全是零进行判断
    if (i >>> 30 == 0) { n +=  2; i <<=  2; }
    // 判断最高位是否为零
    n -= i >>> 31;
    return n;
}
```



详情b：formatUnsignedInt(val, shift, buf, 0, chars);

源码详解：

```java
static int formatUnsignedInt(int val, int shift, char[] buf, int offset, int len) {
    // val的有效二进制位数
    int charPos = len;
    // shift == 4
    // 1 << 4 == 10000B
    int radix = 1 << shift;
    // mask == 1111B
    int mask = radix - 1;
    do {
        // offset + --charPos 每次从字符数组的最高位开始赋值
        // val & mask val低4位对应的十进制值 获取Integer.digits对应的字符
        buf[offset + --charPos] = Integer.digits[val & mask];
        // val 低4位右移
        val >>>= shift;
    } while (val != 0 && charPos > 0);

    // 返回16进制无符号字符串
    return charPos;
}

final static char[] digits = {
    '0' , '1' , '2' , '3' , '4' , '5' ,
    '6' , '7' , '8' , '9' , 'a' , 'b' ,
    'c' , 'd' , 'e' , 'f' , 'g' , 'h' ,
    'i' , 'j' , 'k' , 'l' , 'm' , 'n' ,
    'o' , 'p' , 'q' , 'r' , 's' , 't' ,
    'u' , 'v' , 'w' , 'x' , 'y' , 'z'
};
```

