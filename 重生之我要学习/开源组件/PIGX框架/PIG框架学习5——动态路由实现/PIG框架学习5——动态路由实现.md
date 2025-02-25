## PIG框架学习5——动态路由实现

### 一、前言

> 参考PIGX官方文档：https://www.yuque.com/pig4cloud/pig/ggnc3e

#### 1.1 说明

在微服务中，定义路由一般是在网关gateway对应的配置文件中进行定义声明的，PIGX框架下，如何通过动态路由实现网关路由的实时更新？

实现方式：通过upms下的一张路由表（sys_route_conf）来动态维护路由信息，路由信息通过spring的监听事件 + redis的发布订阅模式实时和网关中维护的路由绑定和实时同步。

#### 1.2 涉及相关类

通用用户权限管理服务（pigx-upms）：

- DynamicRouteInitRunner

  package：`com.pig4cloud.pigx.admin.config`

  容器启动后保存配置文件里面的路由信息（数据库中维护的路由信息）到Redis中，并通知网关重置路由；配置redis的订阅。

- SysRouteConfServiceImpl

  package：`com.pig4cloud.pigx.admin.service.impl`

  数据库中维护路由信息的增删改查，修改时发布Spring事件，调用DynamicRouteInitRunner的initRoute方法，保存配置文件里面的路由信息（数据库中维护的路由信息）到Redis中。

网关服务（pigx-gateway）：

- RouteCacheHolder

  package：`com.pig4cloud.pigx.common.gateway.support`

  路由缓存工具类（LFU淘汰策略）

- DynamicRouteAutoConfiguration

  package：`com.pig4cloud.pigx.common.gateway.configuration`

  动态路由配置类，注册两个Bean对象 ①配置redis订阅者，监听配置，重新加载网关路由 ②动态路由监控工具

- RedisRouteDefinitionWriter

  package：`com.pig4cloud.pigx.common.gateway.support`

  获取网关路由信息（通过重写RouteDefinitionLocator的getRouteDefinitions的方法），gateway重启的时候会调用getRouteDefinitions方法，upms重启的时候会发布redis通知，gateway订阅/监听到后会发布spring的事件：RefreshRoutesEvent，也会调用其中的getRouteDefinitions方法

### 二、具体流程

#### 1、upms先启动，后启动gateway：

upms启动：

- DynamicRouteInitRunner#afterPropertiesSet

  在bean初始化方法中执行redis订阅者的创建

  ![image-20250213145010376](PIG%E6%A1%86%E6%9E%B6%E5%AD%A6%E4%B9%A05%E2%80%94%E2%80%94%E5%8A%A8%E6%80%81%E8%B7%AF%E7%94%B1%E5%AE%9E%E7%8E%B0.assets/image-20250213145010376.png)

- DynamicRouteInitRunner#webServerInit

  该方法通过@Async注解修饰，即通过异步方式执行。

  `@EventListener` 注解用于监听指定事件。当指定的事件发生时，Spring 会自动调用该方法，`WebServerInitializedEvent` 是一个 Spring 事件，表示 Web 服务器已初始化并准备就绪，因此该方法会在 Web 服务器初始化完成后被触发执行

  ![image-20250213145447567](PIG%E6%A1%86%E6%9E%B6%E5%AD%A6%E4%B9%A05%E2%80%94%E2%80%94%E5%8A%A8%E6%80%81%E8%B7%AF%E7%94%B1%E5%AE%9E%E7%8E%B0.assets/image-20250213145447567.png)

- DynamicRouteInitRunner#initRoute

  获取数据库对应表中维护的路由信息，并将其放到redis中

  ![image-20250225140454301](PIG%E6%A1%86%E6%9E%B6%E5%AD%A6%E4%B9%A05%E2%80%94%E2%80%94%E5%8A%A8%E6%80%81%E8%B7%AF%E7%94%B1%E5%AE%9E%E7%8E%B0.assets/image-20250225140454301.png)

  ![image-20250213145529105](PIG%E6%A1%86%E6%9E%B6%E5%AD%A6%E4%B9%A05%E2%80%94%E2%80%94%E5%8A%A8%E6%80%81%E8%B7%AF%E7%94%B1%E5%AE%9E%E7%8E%B0.assets/image-20250213145529105.png)

  然后会通过redis的订阅/发布模式，通知网关gateway服务重新刷新路由信息

  ![image-20250213145853730](PIG%E6%A1%86%E6%9E%B6%E5%AD%A6%E4%B9%A05%E2%80%94%E2%80%94%E5%8A%A8%E6%80%81%E8%B7%AF%E7%94%B1%E5%AE%9E%E7%8E%B0.assets/image-20250213145853730.png)

  注意：就算gateway网关服务没有启动，即对应的redis订阅者没有，这里发布了也不影响。
  
  到此redis中已经有路由的信息了。

gateway启动：

- 通过DynamicRouteAutoConfiguration，会注册redis订阅者和动态路由监控检查

  ![image-20250213150523186](PIG%E6%A1%86%E6%9E%B6%E5%AD%A6%E4%B9%A05%E2%80%94%E2%80%94%E5%8A%A8%E6%80%81%E8%B7%AF%E7%94%B1%E5%AE%9E%E7%8E%B0.assets/image-20250213150523186.png)

- gateway启动会调用实现RouteDefinitionRepository接口的RedisRouteDefinitionWriter类的getRouteDefinitions方法，来获取所有的路由，其先从内存中，即维护的缓存对象RouteCacheHolder中获取，再从redis中获取，此时缓存对象中没有，因此可以从redis中获取所有的路由信息，并返回给网关处理。

  ![image-20250213150726395](PIG%E6%A1%86%E6%9E%B6%E5%AD%A6%E4%B9%A05%E2%80%94%E2%80%94%E5%8A%A8%E6%80%81%E8%B7%AF%E7%94%B1%E5%AE%9E%E7%8E%B0.assets/image-20250213150726395.png)

#### 2、gateway先启动，后启动upms：

- upms启动的时候，就调用到了gateway的redis订阅者
- 会发布RefreshRoutesEvent事件，调用实现RouteDefinitionRepository接口的RedisRouteDefinitionWriter类的getRouteDefinitions方法获取相关的路由信息，并重新加载路由



### 三、一些问题

如下设计到gateway如何加载路由相关的内容了，需要在后面学习源码过程中补充。

#### 3.1 为什么说自定义的RedisRouteDefinitionWriter（RouteDefinitionLocator实现类）比配置文件的优先级高呢？

在GatewayAutoConfiguration中的注册中

```
@Bean
@Primary
public RouteDefinitionLocator routeDefinitionLocator(List<RouteDefinitionLocator> routeDefinitionLocators) {
    return new CompositeRouteDefinitionLocator(Flux.fromIterable(routeDefinitionLocators));
}
```

其先注入的是RedisRouteDefinitionWriter类

![image-20250213134211247](PIG%E6%A1%86%E6%9E%B6%E5%AD%A6%E4%B9%A05%E2%80%94%E2%80%94%E5%8A%A8%E6%80%81%E8%B7%AF%E7%94%B1%E5%AE%9E%E7%8E%B0.assets/image-20250213134211247.png)

`CompositeRouteDefinitionLocator` 是一个组合型的路由定义定位器，它会逐个尝试列表中的每个 `RouteDefinitionLocator`，直到找到一个有效的路由定义。如果第一个定位器返回了路由定义，后续的定位器将不再被调用；如果第一个定位器没有找到路由定义，系统会尝试下一个定位器，依此类推。

#### 3.2 关于RouteDefinitionLocator类

1.介绍

`RouteDefinitionLocator` 是 Spring Cloud Gateway 中的一个重要接口，它的作用是定义如何获取路由定义。路由定义指的是在 Spring Cloud Gateway 中配置的路由规则，包括路由的匹配条件、目标 URI、过滤器等。具体来说，`RouteDefinitionLocator` 的作用是提供一种机制，用于从不同的源（如配置文件、数据库、外部系统等）动态地获取这些路由定义。

2.主要作用：

- **提供路由定义**： `RouteDefinitionLocator` 的核心作用是提供 `RouteDefinition` 对象。`RouteDefinition` 是一个表示单个路由的信息的对象，它包含路由匹配条件、目标 URI、过滤器等。

- **从不同的数据源获取路由定义**： `RouteDefinitionLocator` 可以从不同的数据源获取路由定义，常见的实现包括：

  - **静态配置**：从配置文件（如 `application.yml` 或 `application.properties`）中加载路由定义。

  - **动态路由**：从数据库、Redis 或其他外部系统中动态获取路由定义。

- **支持响应式编程**： `RouteDefinitionLocator` 的实现通常支持响应式编程模型（基于 Reactor），使得路由定义的获取过程能够异步进行。这意味着它能够在不阻塞主线程的情况下，从外部系统获取路由定义（例如，通过 API 调用、数据库查询等）。



3.主要接口

`RouteDefinitionLocator` 接口定义了以下主要方法：

- **`Flux<RouteDefinition> getRouteDefinitions()`**： 这个方法是 `RouteDefinitionLocator` 的核心方法，它返回一个 `Flux<RouteDefinition>`，该 `Flux` 可以异步地获取所有路由定义。`RouteDefinition` 包含了关于路由的所有必要信息，如路径匹配规则、目标 URI、过滤器等。

```
Flux<RouteDefinition> getRouteDefinitions();
```

`Flux<RouteDefinition>` 是一个响应式流，表示多个路由定义的集合，可以异步获取这些路由定义，并支持背压等响应式编程的特性



4.常见的`RouteDefinitionLocator` 实现

- **`PropertiesRouteDefinitionLocator`**： 这是一个常见的实现，它从配置文件中加载路由定义。Spring Cloud Gateway 允许通过 `application.yml` 或 `application.properties` 文件配置路由。这些路由定义会在应用启动时加载，`PropertiesRouteDefinitionLocator` 就是用来从这些配置中读取路由的。

  yml配置文件定义如下所示：

  ```yml
  spring:
    cloud:
      gateway:
        routes:
          - id: route1
            uri: http://example.org
            predicates:
              - Path=/foo
          - id: route2
            uri: http://example.com
            predicates:
              - Path=/bar
  ```

  `PropertiesRouteDefinitionLocator` 将读取这些配置并将它们转换成 `RouteDefinition` 对象，最终通过 `RouteDefinitionLocator` 提供给 Spring Cloud Gateway 进行路由处理。

- **`InMemoryRouteDefinitionRepository`**： 这是另一个常见的实现，用于在内存中管理路由定义。它通常用于临时存储路由，支持动态添加、删除和更新路由。

- **自定义实现**： 如果需要从数据库、Redis、或其他外部系统中动态获取路由定义，可以实现自定义的 `RouteDefinitionLocator`。这种实现可以与 Spring Cloud Gateway 配合，支持灵活的路由管理。



5.总结

`RouteDefinitionLocator` 在 Spring Cloud Gateway 中的作用是提供路由定义的来源，它负责从配置文件、数据库或其他外部系统中获取路由定义。通过实现这个接口，Spring Cloud Gateway 能够灵活地支持静态和动态路由，同时也能够支持响应式编程，使得路由查找过程更加高效和异步。



#### 3.3 关于RouteDefinitionLocator类的实现类方法的调用

首先网关会有一个自动配置类：GatewayAutoConfiguration

在自动配置类中有一个注册bean的方法如下，就是用来注册RouteDefinitionLocator的实现类的

```java
@Bean
@Primary
public RouteDefinitionLocator routeDefinitionLocator(List<RouteDefinitionLocator> routeDefinitionLocators) {
    return new CompositeRouteDefinitionLocator(Flux.fromIterable(routeDefinitionLocators));
}
```

![image-20250213140511617](PIG%E6%A1%86%E6%9E%B6%E5%AD%A6%E4%B9%A05%E2%80%94%E2%80%94%E5%8A%A8%E6%80%81%E8%B7%AF%E7%94%B1%E5%AE%9E%E7%8E%B0.assets/image-20250213140511617.png)

`CompositeRouteDefinitionLocator` 是一个组合型的路由定义定位器，它会逐个尝试列表中的每个 `RouteDefinitionLocator`，直到找到一个有效的路由定义。如果第一个定位器返回了路由定义，后续的定位器将不再被调用；如果第一个定位器没有找到路由定义，系统会尝试下一个定位器，依此类推。

关于上面的配置文件路由定义加载器，也是在这个配置文件GatewayAutoConfiguration中定义的，源码如下所示：

```
@Bean
@ConditionalOnMissingBean
public PropertiesRouteDefinitionLocator propertiesRouteDefinitionLocator(GatewayProperties properties) {
    return new PropertiesRouteDefinitionLocator(properties);
}
```



可以看到其通过Flux.fromIterable(routeDefinitionLocators)方法，将去分装成Flux<RouteDefinitionLocator>对象，并通过CompositeRouteDefinitionLocator构造器，将其设置为CompositeRouteDefinitionLocator对象的属性。

> 关于**`Flux`** 
>
> `Flux` 是 **Reactor** 库中提供的一个反应式（Reactive）流的核心类之一。它代表一个异步的、按需消费的元素流，可以包含零个、一个或多个元素。`Flux` 可以用于处理流式数据，适用于响应式编程模型。
>
> `Mono` 和 `Flux` 是 Reactor 的两个主要类型：
>
> - **Mono**：表示 0 或 1 个元素的异步流。
> - **Flux**：表示 0 个或多个元素的异步流。
>
> 关于**`fromIterable()` 方法的作用**
>
> Flux.fromIterable()` 方法将一个实现了 `Iterable` 接口的对象（如 `List`、`Set` 或其他可迭代集合）转换成 `Flux
>
> ```
> public static <T> Flux<T> fromIterable(Iterable<? extends T> iterable);
> ```
>
> - **参数**：`Iterable<? extends T>`：任何实现了 `Iterable` 接口的对象，例如 `List`、`Set`、`Queue` 等。
> - **返回值**：返回一个 `Flux<T>`，这是一个包含集合中所有元素的流。
>
> 例子：
>
> 假设我们有一个 `List<RouteDefinitionLocator>`，里面包含了多个 `RouteDefinitionLocator` 实例。我们可以使用 `Flux.fromIterable()` 将这个 `List` 转换成一个 `Flux`，然后进行流式操作：
>
> ```java
> import reactor.core.publisher.Flux;
> import java.util.List;
> 
> public class RouteDefinitionService {
>     
>     public Flux<RouteDefinition> getRouteDefinitions(List<RouteDefinitionLocator> routeDefinitionLocators) {
>         // 将 routeDefinitionLocators 列表转为 Flux
>         return Flux.fromIterable(routeDefinitionLocators)
>                    .flatMap(locator -> locator.getRouteDefinitions());
>     }
> }
> 
> ```
>
> 在上面的例子中：
>
> 1. `Flux.fromIterable(routeDefinitionLocators)` 会将 `routeDefinitionLocators`（一个 `List<RouteDefinitionLocator>`）转换成 `Flux<RouteDefinitionLocator>`，这意味着它将逐个遍历列表中的每个 `RouteDefinitionLocator` 对象。
> 2. `flatMap(locator -> locator.getRouteDefinitions())` 会调用每个 `RouteDefinitionLocator` 的 `getRouteDefinitions()` 方法（它返回一个 `Flux<RouteDefinition>`），并将多个 `Flux<RouteDefinition>` 合并成一个 `Flux<RouteDefinition>`。
>
> 为什么使用Flux.fromIterable
>
> 使用 `Flux.fromIterable()` 可以轻松地将任何可迭代集合转为反应式流。这是处理异步数据流和响应式编程的常见做法。在 `Spring Cloud Gateway` 中，路由定义经常是从多个来源（例如配置文件、数据库或外部服务）动态加载的，而这些来源通常会返回可迭代的集合。因此，将这些集合转换为 `Flux`，并使用流的操作（如 `map`、`flatMap`、`filter` 等）来处理数据是非常方便和自然的。
>
> 总结：
>
> - `Flux.fromIterable(routeDefinitionLocators)` 将一个 `Iterable`（如 `List` 或 `Set`）转换为一个 `Flux` 对象。
> - 这个转换的好处在于你可以使用响应式编程的方式，逐个处理 `routeDefinitionLocators` 中的元素，并将它们的值流式传递。
> - `Flux` 是响应式流的一部分，适合处理异步和大量数据的场景。



关于其调用，在gateway启动的时候，通过bean主键完成CompositeRouteDefinitionLocator对象（其也是RouteDefinitionLocator的实现类）的注册后，该对象中会维护RouteDefinitionLocator的具体实现

![image-20250213142849519](PIG%E6%A1%86%E6%9E%B6%E5%AD%A6%E4%B9%A05%E2%80%94%E2%80%94%E5%8A%A8%E6%80%81%E8%B7%AF%E7%94%B1%E5%AE%9E%E7%8E%B0.assets/image-20250213142849519.png)



然后会注册一个RouteLocator对象

```
@Bean
@Primary
@ConditionalOnMissingBean(name = "cachedCompositeRouteLocator")
// TODO: property to disable composite?
public RouteLocator cachedCompositeRouteLocator(List<RouteLocator> routeLocators) {
    return new CachingRouteLocator(new CompositeRouteLocator(Flux.fromIterable(routeLocators)));
}
```

其使用的构造器如下所示

```java
public CachingRouteLocator(RouteLocator delegate) {
    this.delegate = delegate;
    routes = CacheFlux.lookup(cache, CACHE_KEY, Route.class).onCacheMissResume(this::fetch);
}
```

通过代用routes = CacheFlux.lookup(cache, CACHE_KEY, Route.class).onCacheMissResume(this::fetch);就拿到了所有配置的路由

> 使用了 **Reactor** 框架中的缓存处理以及异步流操作
>
> 1. `CacheFlux.lookup(cache, CACHE_KEY, Route.class)`
>
> - `CacheFlux` 是 **Reactor** 中一个与缓存相关的类，提供了针对缓存的异步处理。
>
> - ```
>   lookup(cache, CACHE_KEY, Route.class)
>   ```
>
>    
>
>   是一个静态方法，用于从指定的缓存中查找数据。
>
>   - `cache`：这个参数表示缓存对象。它是缓存数据的存储位置，可能是某种实现了 `Cache` 接口的对象。
>   - `CACHE_KEY`：是缓存的键，通常是一个唯一标识符，用于在缓存中查找数据。
>   - `Route.class`：表示缓存中的数据类型。在这里，它指定了缓存中存储的值类型是 `Route`。
>
> `lookup()` 方法的作用是从缓存中查找指定键（`CACHE_KEY`）对应的数据。如果缓存中存在这个数据，它会返回一个包含缓存数据的 `Mono<Route>`，如果缓存中不存在数据，则会返回一个 `Mono.empty()`。
>
> 2. `.onCacheMissResume(this::fetch)`
>
> - `.onCacheMissResume()` 是 `CacheFlux` 提供的一个操作符。它用于处理缓存未命中的情况。
>
> - ```
>   this::fetch
>   ```
>
>    
>
>   是一个方法引用，表示当缓存没有命中时调用
>
>    
>
>   ```
>   fetch()
>   ```
>
>    
>
>   方法。
>
>   - `fetch()` 方法可能是一个异步操作，用于从某个源（比如数据库、远程服务等）获取数据。在缓存未命中时，它会触发 `fetch()` 来获取数据，并将其重新放入缓存。
>
> 简而言之，`onCacheMissResume(this::fetch)` 的作用是在缓存中没有找到数据时，调用 `fetch()` 方法来获取数据。
>
> 3. `routes = ...`
>
> - 最终的 `routes` 是一个 `Mono<Route>`，表示异步获取的 `Route` 对象。
> - 如果缓存中有数据，`lookup()` 返回缓存的 `Mono<Route>`；如果没有缓存数据，它会调用 `fetch()` 异步获取数据，并返回一个新的 `Mono<Route>`。



反正这里就会调用一些操作……这里不是重点，后面先把Flux学一下吧，再跟进（△）



最后就会调用到RedisRouteDefinitionWriter的getRouteDefinitions方法区获取路由信息

![image-20250213144752362](PIG%E6%A1%86%E6%9E%B6%E5%AD%A6%E4%B9%A05%E2%80%94%E2%80%94%E5%8A%A8%E6%80%81%E8%B7%AF%E7%94%B1%E5%AE%9E%E7%8E%B0.assets/image-20250213144752362.png)