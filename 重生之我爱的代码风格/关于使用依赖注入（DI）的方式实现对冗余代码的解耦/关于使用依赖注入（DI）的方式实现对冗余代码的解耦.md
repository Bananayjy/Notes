## 一、关于使用依赖注入（DI）的方式实现对冗余代码的解耦

### 1.1、优化前代码

```java
@GetMapping("/test")
public void test(@RequestParam("params") String params){
    if("1".equals(params)){
        // 逻辑代码
    }

    if("2".equals(params)){
        // 逻辑代码
    }

    if("3".equals(params)){
        // 逻辑代码
    }
}
```

### 1.2、优化后代码

**接口**

```java
public interface CAL {
    public void solve(String params);
}
```

**实现类1**

```java
@Service
public class CAL1 implements CAL {
    public void solve(String params){
        if("1".equals(params)){
            System.out.println("1的处理逻辑");
        }
    }

}
```

**实现类2**

```java
@Service
public class CAL2 implements CAL{
    public void solve(String params){
        if("2".equals(params)){
            System.out.println("2的处理逻辑");
        }
    }
}

```

**实现类3**

```java
@Service
public class CAL3 implements CAL {
void solve(String params){
        if("3".equals(params)){
            System.out.println("3的处理逻辑");
        }
    }
}

```

**控制层**

```java
@Autowired
// 或 @Resource 也可，这里是根据类型注入，
List<CAL> calList;

@GetMapping("/test")
public void test(@RequestParam("params") String params){
    for (CAL cal : calList) {
        cal.solve(params);
    }
}
```



