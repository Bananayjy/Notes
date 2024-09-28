# Nacos源码分析前言

## 一、前言

### 1.1、参考文章汇总

- [Nacos官方文档（1.x/2.x版本）](https://nacos.io/zh-cn/docs/what-is-nacos.html)
- [Nacos2.X源码阅读总结](https://www.cnblogs.com/wkynf/p/15951743.html)
- https://juejin.cn/post/7231427492610850874
- https://juejin.cn/post/7208088676852940860
- https://www.iocoder.cn/Nacos/good-collection/
- https://www.iocoder.cn/Nacos/good-collection/
- https://juejin.cn/post/7141766518131392525
- 图：https://www.processon.com/view/link/631365641efad46b1f52aee7

### 1.2、Nacos

#### 1.2.1、Nacos概览

一个更易于构建云原生应用的动态服务发现（Nacos Discovery）、服务配置（Nacos Config）的服务管理平台。

> 官方文档
>
> Nacos /nɑ:kəʊs/ 是 Dynamic Naming and Configuration Service的首字母简称，一个更易于构建云原生应用的动态服务发现、配置管理和服务管理平台。
>
> Nacos 致力于帮助您发现、配置和管理微服务。Nacos 提供了一组简单易用的特性集，帮助您快速实现动态服务发现、服务配置、服务元数据及流量管理。
>
> Nacos 帮助您更敏捷和容易地构建、交付和管理微服务平台。 Nacos 是构建以“服务”为中心的现代应用架构 (例如微服务范式、云原生范式) 的服务基础设施。



#### 1.2.2、Nacos支持的服务

> 官方文档
>
> 服务（Service）是 Nacos 世界的一等公民。Nacos 支持几乎所有主流类型的“服务”的发现、配置和管理：
>
> [Kubernetes Service](https://kubernetes.io/docs/concepts/services-networking/service/)
>
> [gRPC](https://grpc.io/docs/guides/concepts.html#service-definition) & [Dubbo RPC Service](https://dubbo.incubator.apache.org/)
>
> [Spring Cloud RESTful Service](https://spring.io/projects/spring-restdocs)

在 Nacos 中，服务（Service）被看作是非常重要的一种资源，是构成分布式系统的基本组成部分之一，Nacos 提供了丰富的功能和 API，用于对服务进行发现、配置和管理。无论是传统的基于主机的服务，还是现代的容器化服务，Nacos 都能够支持几乎所有主流类型的服务,其中支持的服务具体说明如下所示

- Kubernetes Service

[Kubernetes Service](https://kubernetes.io/docs/concepts/services-networking/service/) 是 Kubernetes 中的一个概念，用于定义一组 Pod 的逻辑集合和访问这些 Pod 的策略。Service 可以将一组 Pod 封装成一个单一的入口，提供了负载均衡、服务发现和透明代理等功能。在 Nacos 中，可以使用 Service 来管理 Kubernetes 集群中的服务，实现对这些服务的发现、配置和管理。

在 Kubernetes 中，Pod 是最小的调度单元，它是一组容器的集合，共享网络命名空间、IPC 和文件系统。Pod 通常用于运行一个特定的应用程序实例或一组紧密耦合的服务。Pod 可以包含一个或多个容器，这些容器共享相同的网络、存储和生命周期。它们被部署到同一台物理机或虚拟机上，并且可以直接通信。Pod 可以通过 Service 对外暴露，Service 提供了一个稳定的网络端点，用于访问一组具有相同功能的 Pod。这样，无论 Pod 如何变化，Service 都可以确保应用程序的稳定访问。

- gRPC

[gRPC](https://grpc.io/docs/guides/concepts.html#service-definition) 是一个高性能、开源和通用的远程过程调用（RPC）框架，基于 HTTP/2 标准，使用 Protocol Buffers 作为接口描述语言。在 gRPC 中，Service 是一个抽象的概念，代表一个或多个方法的集合，客户端可以通过该 Service 对象调用服务器端的方法。在 Nacos 中，可以使用 Service 来管理 gRPC 服务的发现、配置和管理。

- Dubbo RPC Service

[Dubbo](https://dubbo.incubator.apache.org/) 是阿里巴巴开源的高性能、轻量级的 Java RPC 框架。Dubbo 中的 Service 也是一个重要概念，表示一个服务的提供者，通过 Dubbo 可以方便地发布、订阅和调用服务。在 Nacos 中，可以使用 Service 来管理 Dubbo RPC 服务，实现对 Dubbo 服务的发现、配置和管理。

- Spring Cloud RESTful Service

[Spring Cloud](https://spring.io/projects/spring-cloud) 是一个用于快速构建分布式系统的框架，其中的 RESTful Service 是 Spring Cloud 中的核心组件之一。RESTful Service 是基于 RESTful 架构风格设计的服务，通过 HTTP 协议进行通信。在 Nacos 中，可以使用 Service 来管理 Spring Cloud RESTful 服务，实现对这些服务的发现、配置和管理。



#### 1.2.3、Nacos特性

> 官方文档
>
> - **服务发现和服务健康监测**
>
> Nacos 支持基于 DNS 和基于 RPC 的服务发现。服务提供者使用 [原生SDK](https://nacos.io/zh-cn/docs/v2/guide/user/sdk.html)、[OpenAPI](https://nacos.io/zh-cn/docs/v2/guide/user/open-api.html)、或一个[独立的Agent TODO](https://nacos.io/zh-cn/docs/v2/guide/user/other-language.html)注册 Service 后，服务消费者可以使用[DNS TODO](https://nacos.io/zh-cn/docs/v2/xx) 或[HTTP&API](https://nacos.io/zh-cn/docs/v2/guide/user/open-api.html)查找和发现服务。
>
> Nacos 提供对服务的实时的健康检查，阻止向不健康的主机或服务实例发送请求。Nacos 支持传输层 (PING 或 TCP)和应用层 (如 HTTP、MySQL、用户自定义）的健康检查。 对于复杂的云环境和网络拓扑环境中（如 VPC、边缘网络等）服务的健康检查，Nacos 提供了 agent 上报模式和服务端主动检测2种健康检查模式。Nacos 还提供了统一的健康检查仪表盘，帮助您根据健康状态管理服务的可用性及流量。
>
> - **动态配置服务**
>
> 动态配置服务可以让您以中心化、外部化和动态化的方式管理所有环境的应用配置和服务配置。
>
> 动态配置消除了配置变更时重新部署应用和服务的需要，让配置管理变得更加高效和敏捷。
>
> 配置中心化管理让实现无状态服务变得更简单，让服务按需弹性扩展变得更容易。
>
> Nacos 提供了一个简洁易用的UI ([控制台样例 Demo](http://console.nacos.io/nacos/index.html)) 帮助您管理所有的服务和应用的配置。Nacos 还提供包括配置版本跟踪、金丝雀发布、一键回滚配置以及客户端配置更新状态跟踪在内的一系列开箱即用的配置管理特性，帮助您更安全地在生产环境中管理配置变更和降低配置变更带来的风险。
>
> - **动态 DNS 服务**
>
> 动态 DNS 服务支持权重路由，让您更容易地实现中间层负载均衡、更灵活的路由策略、流量控制以及数据中心内网的简单DNS解析服务。动态DNS服务还能让您更容易地实现以 DNS 协议为基础的服务发现，以帮助您消除耦合到厂商私有服务发现 API 上的风险。
>
> Nacos 提供了一些简单的 [DNS APIs TODO](https://nacos.io/zh-cn/docs/v2/xx) 帮助您管理服务的关联域名和可用的 IP:PORT 列表
>
> - **服务及其元数据管理**w
>
> Nacos 能让您从微服务平台建设的视角管理数据中心的所有服务及元数据，包括管理服务的描述、生命周期、服务的静态依赖分析、服务的健康状态、服务的流量管理、路由及安全策略、服务的 SLA 以及最首要的 metrics 统计数据。



#### 1.2.4、Nacos相关图

1.、Nacos地图

> ![nacos_map](Nacos%E6%BA%90%E7%A0%81%E5%88%86%E6%9E%90%E5%89%8D%E8%A8%80.assets/nacosMap.jpg)
>
> - 特性大图：要从功能特性，非功能特性，全面介绍我们要解的问题域的特性诉求
> - 架构大图：通过清晰架构，让您快速进入 Nacos 世界
> - 业务大图：利用当前特性可以支持的业务场景，及其最佳实践
> - 生态大图：系统梳理 Nacos 和主流技术生态的关系
> - 优势大图：展示 Nacos 核心竞争力
> - 战略大图：要从战略到战术层面讲 Nacos 的宏观优势

2、Nacos生态图

> ![nacos_landscape.png](Nacos%E6%BA%90%E7%A0%81%E5%88%86%E6%9E%90%E5%89%8D%E8%A8%80.assets/1533045871534-e64b8031-008c-4dfc-b6e8-12a597a003fb.png)
>
> 如 Nacos 全景图所示，Nacos 无缝支持一些主流的开源生态，例如
>
> - [Spring Cloud](https://nacos.io/zh-cn/docs/v2/ecology/use-nacos-with-spring-cloud.html)
> - [Apache Dubbo and Dubbo Mesh](https://nacos.io/zh-cn/docs/v2/ecology/use-nacos-with-dubbo.html)
> - [Kubernetes and CNCF](https://nacos.io/zh-cn/docs/v2/quickstart/quick-start-kubernetes.html)
>
> 使用 Nacos 简化服务发现、配置管理、服务治理及管理的解决方案，让微服务的发现、管理、共享、组合更加容易。
>
> 关于如何在这些生态中使用 Nacos，请参考以下文档：
>
> [Nacos与Spring Cloud一起使用](https://nacos.io/zh-cn/docs/v2/ecology/use-nacos-with-spring-cloud.html)
>
> [Nacos与Kubernetes一起使用](https://nacos.io/zh-cn/docs/v2/quickstart/quick-start-kubernetes.html)
>
> [Nacos与Dubbo一起使用](https://nacos.io/zh-cn/docs/v2/ecology/use-nacos-with-dubbo.html)

#### 1.2.5、Nacos相关基本概念

> - 地域
>
> 物理的数据中心，资源创建成功后不能更换。
>
> - 可用区
>
> 同一地域内，电力和网络互相独立的物理区域。同一可用区内，实例的网络延迟较低。
>
> - 接入点
>
> 地域的某个服务的入口域名。
>
> - 命名空间
>
> 用于进行租户粒度的配置隔离。不同的命名空间下，可以存在相同的 Group 或 Data ID 的配置。Namespace 的常用场景之一是不同环境的配置的区分隔离，例如开发测试环境和生产环境的资源（如配置、服务）隔离等。
>
> - 配置
>
> 在系统开发过程中，开发者通常会将一些需要变更的参数、变量等从代码中分离出来独立管理，以独立的配置文件的形式存在。目的是让静态的系统工件或者交付物（如 WAR，JAR 包等）更好地和实际的物理运行环境进行适配。配置管理一般包含在系统部署的过程中，由系统管理员或者运维人员完成。配置变更是调整系统运行时的行为的有效手段。
>
> - 配置管理
>
> 系统配置的编辑、存储、分发、变更管理、历史版本管理、变更审计等所有与配置相关的活动。
>
> - 配置项
>
> 一个具体的可配置的参数与其值域，通常以 param-key=param-value 的形式存在。例如我们常配置系统的日志输出级别（logLevel=INFO|WARN|ERROR） 就是一个配置项。
>
> - 配置集
>
> 一组相关或者不相关的配置项的集合称为配置集。在系统中，一个配置文件通常就是一个配置集，包含了系统各个方面的配置。例如，一个配置集可能包含了数据源、线程池、日志级别等配置项。
>
> - 配置集 ID
>
> Nacos 中的某个配置集的 ID。配置集 ID 是组织划分配置的维度之一。Data ID 通常用于组织划分系统的配置集。一个系统或者应用可以包含多个配置集，每个配置集都可以被一个有意义的名称标识。Data ID 通常采用类 Java 包（如 com.taobao.tc.refund.log.level）的命名规则保证全局唯一性。此命名规则非强制。
>
> - 配置分组
>
> Nacos 中的一组配置集，是组织配置的维度之一。通过一个有意义的字符串（如 Buy 或 Trade ）对配置集进行分组，从而区分 Data ID 相同的配置集。当您在 Nacos 上创建一个配置时，如果未填写配置分组的名称，则配置分组的名称默认采用 DEFAULT_GROUP 。配置分组的常见场景：不同的应用或组件使用了相同的配置类型，如 database_url 配置和 MQ_topic 配置。
>
> - 配置快照
>
> Nacos 的客户端 SDK 会在本地生成配置的快照。当客户端无法连接到 Nacos Server 时，可以使用配置快照显示系统的整体容灾能力。配置快照类似于 Git 中的本地 commit，也类似于缓存，会在适当的时机更新，但是并没有缓存过期（expiration）的概念。
>
> - 服务（Service）
>
> 服务是指一个或一组软件功能（例如特定信息的检索或一组操作的执行），其目的是不同的客户端可以为不同的目的重用（例如通过跨进程的网络调用）。Nacos 支持主流的服务生态，如 Kubernetes Service、gRPC|Dubbo RPC Service 或者 Spring Cloud RESTful Service。
>
> - 服务名
>
> 服务提供的标识，通过该标识可以唯一确定其指代的服务。
>
> - 服务注册中心（Service Registry）
>
> 服务注册中心，它是服务，其实例及元数据的数据库。服务实例在启动时注册到服务注册表，并在关闭时注销。服务和路由器的客户端查询服务注册表以查找服务的可用实例。服务注册中心可能会调用服务实例的健康检查 API 来验证它是否能够处理请求。
>
> - 服务发现
>
> 在计算机网络上，（通常使用服务名）对服务下的实例的地址和元数据进行探测，并以预先定义的接口提供给客户端进行查询。
>
> - 元信息
>
> Nacos数据（如配置和服务）描述信息，如服务版本、权重、容灾策略、负载均衡策略、鉴权配置、各种自定义标签 (label)，从作用范围来看，分为服务级别的元信息、集群的元信息及实例的元信息。
>
> - 应用
>
> 用于标识服务提供方的服务的属性。
>
> - 服务分组
>
> 不同的服务可以归类到同一分组。
>
> - 虚拟集群
>
> 同一个服务下的所有服务实例组成一个默认集群, 集群可以被进一步按需求划分，划分的单位可以是虚拟集群。
>
> - 实例
>
> 提供一个或多个服务的具有可访问网络地址（IP:Port）的进程。
>
> - 权重
>
> 实例级别的配置。权重为浮点数。权重越大，分配给该实例的流量越大。
>
> - 健康检查
>
> 以指定方式检查服务下挂载的实例 (Instance) 的健康度，从而确认该实例 (Instance) 是否能提供服务。根据检查结果，实例 (Instance) 会被判断为健康或不健康。对服务发起解析请求时，不健康的实例 (Instance) 不会返回给客户端。
>
> - 健康保护阈值
>
> 为了防止因过多实例 (Instance) 不健康导致流量全部流向健康实例 (Instance) ，继而造成流量压力把健康实例 (Instance) 压垮并形成雪崩效应，应将健康保护阈值定义为一个 0 到 1 之间的浮点数。当域名健康实例数 (Instance) 占总服务实例数 (Instance) 的比例小于该值时，无论实例 (Instance) 是否健康，都会将这个实例 (Instance) 返回给客户端。这样做虽然损失了一部分流量，但是保证了集群中剩余健康实例 (Instance) 能正常工作。
>
> - 服务提供方(Service Provider)
>
> 是指提供可复用和可调用服务的应用方
>
> - 服务消费方(Service Consumer)
>
> 是指会发起对某个服务调用的应用方。
>
> - 配置管理 (Configuration Management)
>
> 在数据中心中，系统中所有配置的编辑、存储、分发、变更管理、历史版本管理、变更审计等所有与配置相关的活动统称为配置管理。
>
> - 名字服务 (Naming Service)
>
> 提供分布式系统中所有对象(Object)、实体(Entity)的“名字”到关联的元数据之间的映射管理服务，例如 ServiceName -> Endpoints Info, Distributed Lock Name -> Lock Owner/Status Info, DNS Domain Name -> IP List, 服务发现和 DNS 就是名字服务的2大场景。
>
> - 配置服务 (Configuration Service)
>
> 在服务或者应用运行过程中，提供动态配置或者元数据以及配置管理的服务提供者。



#### 1.2.6、Nacos架构

##### 1、基本架构

> ![nacos_arch.jpg](Nacos%E6%BA%90%E7%A0%81%E5%88%86%E6%9E%90%E5%89%8D%E8%A8%80.assets/1561217892717-1418fb9b-7faa-4324-87b9-f1740329f564.jpeg)

- Multi-Datacenter Nacos Cluster

Nacos 的一种部署架构，用于实现跨多个数据中心的高可用和灾备容错。在这种架构中，Nacos 集群分布在不同的数据中心，并通过网络连接进行通信和协作。

- Multi-Datacenter Nacos Cluster中的Nacos Console

Nacos Console即Nacos控制台。

> 官方文档
>
> [Nacos 控制台](http://console.nacos.io/nacos/index.html)主要旨在于增强对于服务列表，健康状态管理，服务治理，分布式配置管理等方面的管控能力，以便进一步帮助用户降低管理微服务应用架构的成本，将提供包括下列基本功能:
>
> - 服务管理
>   - 服务列表及服务健康状态展示
>   - 服务元数据存储及编辑
>   - 服务流量权重的调整
>   - 服务优雅上下线
> - 配置管理
>   - 多种配置格式编辑
>   - 编辑DIFF
>   - 示例代码
>   - 推送状态查询
>   - 配置版本及一键回滚
> - 命名空间
> - 登录管理

- Multi-Datacenter Nacos Cluster中的Nacos Service

Nacos Service 是整个系统的核心组件，它由几个子服务组成，每个子服务都有不同的功能和作用：

OpenAPI: OpenAPI 服务提供了一个开放的接口，允许外部系统与 Nacos 集群进行交互。通过 OpenAPI，用户可以通过 HTTP 或 RESTful API 访问 Nacos 集群，进行服务注册、配置管理、服务发现等操作。

Config Service: Config Service 是 Nacos 集群中负责配置管理的组件。它允许用户将配置信息存储在 Nacos 中，并提供了动态配置管理的能力。Config Service 负责配置的发布、修改、删除等操作，并能够将配置同步到集群中的所有节点，确保配置的一致性和可靠性。

Name Service: Name Service 是 Nacos 集群中负责服务注册与发现的组件。它允许服务提供者将自己的服务注册到 Nacos 中，并允许服务消费者通过 Nacos 进行服务发现。Name Service 负责管理服务的注册表、服务实例的健康状态、以及服务的动态路由和负载均衡等功能。

Nacos Core: Nacos Core 是 Nacos 集群的核心模块，负责协调和管理集群中的各个子服务。它提供了集群管理、节点发现、数据同步、故障恢复等功能，保证整个集群的稳定运行和高可用性。

Consistency Protocol: 一致性协议是 Nacos 集群中用于确保数据一致性和可靠性的协议。Nacos 使用一致性协议来保证配置信息和服务注册信息在集群中的复制和同步，并在节点故障或网络分区时保证数据的一致性和可用性。

- Consumer APP

Consumer APP 是指使用某种服务或资源的应用程序。它通常会通过服务注册中心（如 Nacos）来发现并调用其他应用程序提供的服务。Consumer APP 负责从服务注册中心获取服务的信息，并调用提供者应用程序提供的服务。

Nacos Client: 是一个用于与 Nacos Server 进行通信的客户端库。它提供了一组 API，允许应用程序在运行时注册服务、发现服务、订阅配置等。通过 Nacos Client，应用程序可以动态地注册自己提供的服务，并实时发现其他服务的实例，以便进行通信。

Sidecar: Sidecar 是一种部署模式，它将 Nacos Client 作为一个独立的进程或容器与应用程序一起部署。Sidecar 通常用于将非 Java 应用程序（如 Node.js、Python 等）集成到 Nacos 中，使它们能够享受到 Nacos 提供的服务治理功能。通过 Sidecar，这些应用程序可以利用 Nacos 的服务注册与发现、配置管理等功能，而无需修改应用程序本身的代码。

- Provider APP

Provider APP 是指提供某种服务或资源的应用程序。它通常会将自己的服务注册到服务注册中心（如 Nacos），以便让其他应用程序能够发现并调用它所提供的服务。Provider APP 负责向服务注册中心注册自己的服务信息，并响应其他应用程序的服务调用请求。



##### 2、逻辑架构及其组件

> ![nacos-logic.jpg](Nacos%E6%BA%90%E7%A0%81%E5%88%86%E6%9E%90%E5%89%8D%E8%A8%80.assets/1646715315872-7ee3679a-e66e-49e9-ba9f-d24168a86c14.png)
>
> - 服务管理：实现服务CRUD，域名CRUD，服务健康状态检查，服务权重管理等功能
> - 配置管理：实现配置管CRUD，版本管理，灰度管理，监听管理，推送轨迹，聚合数据等功能
> - 元数据管理：提供元数据CURD 和打标能力
> - 插件机制：实现三个模块可分可合能力，实现扩展点SPI机制
> - 事件机制：实现异步化事件通知，sdk数据变化异步通知等逻辑
> - 日志模块：管理日志分类，日志级别，日志可移植性（尤其避免冲突），日志格式，异常码+帮助文档
> - 回调机制：sdk通知数据，通过统一的模式回调用户处理。接口和数据结构需要具备可扩展性
> - 寻址模式：解决ip，域名，nameserver、广播等多种寻址模式，需要可扩展
> - 推送通道：解决server与存储、server间、server与sdk间推送性能问题
> - 容量管理：管理每个租户，分组下的容量，防止存储被写爆，影响服务可用性
> - 流量管理：按照租户，分组等多个维度对请求频率，长链接个数，报文大小，请求流控进行控制
> - 缓存机制：容灾目录，本地缓存，server缓存机制。容灾目录使用需要工具
> - 启动模式：按照单机模式，配置模式，服务模式，dns模式，或者all模式，启动不同的程序+UI
> - 一致性协议：解决不同数据，不同一致性要求情况下，不同一致性机制
> - 存储模块：解决数据持久化、非持久化存储，解决数据分片问题
> - Nameserver：解决namespace到clusterid的路由问题，解决用户环境与nacos物理环境映射问题
> - CMDB：解决元数据存储，与三方cmdb系统对接问题，解决应用，人，资源关系
> - Metrics：暴露标准metrics数据，方便与三方监控系统打通
> - Trace：暴露标准trace，方便与SLA系统打通，日志白平化，推送轨迹等能力，并且可以和计量计费系统打通
> - 接入管理：相当于阿里云开通服务，分配身份、容量、权限过程
> - 用户管理：解决用户管理，登录，sso等问题
> - 权限管理：解决身份识别，访问控制，角色管理等问题
> - 审计系统：扩展接口方便与不同公司审计系统打通
> - 通知系统：核心数据变更，或者操作，方便通过SMS系统打通，通知到对应人数据变更
> - OpenAPI：暴露标准Rest风格HTTP接口，简单易用，方便多语言集成
> - Console：易用控制台，做服务管理、配置管理等操作
> - SDK：多语言sdk
> - Agent：dns-f类似模式，或者与mesh等方案集成
> - CLI：命令行对产品进行轻量化管理，像git一样好用



##### 3、领域模型

> - 数据模型
>
> Nacos 数据模型 Key 由三元组唯一确定, Namespace默认是空串，公共命名空间（public），分组默认是 DEFAULT_GROUP。
>
> ![nacos_data_model](Nacos%E6%BA%90%E7%A0%81%E5%88%86%E6%9E%90%E5%89%8D%E8%A8%80.assets/1561217857314-95ab332c-acfb-40b2-957a-aae26c2b5d71.jpeg)
>
> - 服务领域模型
>
> ![nacos_naming_data_model](Nacos%E6%BA%90%E7%A0%81%E5%88%86%E6%9E%90%E5%89%8D%E8%A8%80.assets/1561217924697-ba504a35-129f-4fc6-b0df-1130b995375a.jpeg)
>
> - 配置领域模型
>
> 围绕配置，主要有两个关联的实体，一个是配置变更历史，一个是服务标签（用于打标分类，方便索引），由 ID 关联。
>
> ![nacos_config_er](Nacos%E6%BA%90%E7%A0%81%E5%88%86%E6%9E%90%E5%89%8D%E8%A8%80.assets/1561217958896-4465757f-f588-4797-9c90-a76e604fabb4.jpeg)

##### 4、类视图

> Nacos-SDK 类视图
>
> ![nacos_sdk_class_relation](Nacos%E6%BA%90%E7%A0%81%E5%88%86%E6%9E%90%E5%89%8D%E8%A8%80.assets/1650771676187-d95a9e45-8656-4d1a-8b5b-ed63a23a816b.png)



##### 5、阿里云Nacos架构图

Nacos2.x版本的架构图

> ![5.png](Nacos%E6%BA%90%E7%A0%81%E5%88%86%E6%9E%90%E5%89%8D%E8%A8%80.assets/827bcfbe47c04cd085acbefe2f12e6de.png)

##### 6、Nacos结构图

> Nacos通过Namespace（命名空间）进行环境的隔离,然后我们可以把根据服务之间的关联性来把不同的服务划分到不同的组（Group）之间，每一个组之间可以有多个服务（Service）,同时为了容灾，我们可以把一个服务划分为不同的集群（Cluster）部署在不同的地区或机房，每一个具体的集群下就是我们一个个实例（Instance）了，也就是我们开发的微服务项目。
>
> ![img](Nacos%E6%BA%90%E7%A0%81%E5%88%86%E6%9E%90%E5%89%8D%E8%A8%80.assets/2232696-20220301182557312-908392997.png)

### 1.3、Nacos版本说明

官方目前有两大版本，分别是1.x版本和2.x版本。2.x版本相较于1.x版本比较重大的更新是2.X使用grpc长连接的方式取代了1.X需要一直发送心跳包导出服务器CPU占用较高的问题，同时2.X也对1.X做了重大的升级，无论是从架构层面还是代码层面都做了重大的升级。

|              | 1.X                  | 2.X                         |
| ------------ | -------------------- | --------------------------- |
| 连接方式     | Http短连接           | GRpc、Http短连接（兼容1.X） |
| 推送方式     | UDP                  | GRpc                        |
| 健康检测方式 | Http短连接定时心跳包 | Grpc长连接(轻量级心跳包)    |

现在企业一般主流在使用的是1.4.x版本和2.x版本，并且官方将来可能会停止维护1.x版本，因此建议使用2.x版本，相关2.x版本的部署和1.x平滑升级2.x版本可以参考官方文档。

 

### 1.4、环境搭建

#### 1.4.1、预备环境

> Nacos 依赖 [Java](https://docs.oracle.com/cd/E19182-01/820-7851/inst_cli_jdk_javahome_t/) 环境来运行。如果您是从代码开始构建并运行Nacos，还需要为此配置 [Maven](https://maven.apache.org/index.html)环境，请确保是在以下版本环境中安装使用:
>
> 1. 64 bit OS，支持 Linux/Unix/Mac/Windows，推荐选用 Linux/Unix/Mac。
> 2. 64 bit JDK 1.8+；[下载](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html) & [配置](https://docs.oracle.com/cd/E19182-01/820-7851/inst_cli_jdk_javahome_t/)。
> 3. Maven 3.2.x+；[下载](https://maven.apache.org/download.cgi) & [配置](https://maven.apache.org/settings.html)。

#### 1.4.2、获取Nacos

github地址：https://github.com/alibaba/nacos

##### 1. 官方提供了两种获取Nacos的渠道：源码和发行包

- 源码方式

源码包（1.4.4版本）：https://github.com/alibaba/nacos/releases/download/1.4.4/nacos-server-1.4.4.zip



源码是指软件的原始代码，通常以文本文件的形式存储。它是开发人员编写的、用于构建软件的代码。源码包含了软件的逻辑、算法、功能实现等内容，是软件开发过程中的核心。通常情况下，开发人员会通过编写、修改、测试源码来构建软件的功能。

```shell
// 从 GitHub 上克隆 Nacos 的源代码仓库到本地
git clone https://github.com/alibaba/nacos.git

// 进入到克隆下来的 Nacos 源代码目录
cd nacos/

// 使用 Maven 构建 Nacos 
// -Prelease-nacos：指定构建配置为release-nacos
// -Dmaven.test.skip=true：跳过单元测试
// clean install：编译源码并生成发行包
// -U：Maven 会跳过本地缓存，确保从远程仓库重新下载依赖项和插件，以确保你获得最新版本的依赖包和插件
mvn -Prelease-nacos -Dmaven.test.skip=true clean install -U  

// 进入生成的发行包目录
ls -al distribution/target/

// 根据实际情况修改 $version 为正确的版本号
// change the $version to your actual path
cd distribution/target/nacos-server-$version/nacos/bin
```

- 发行包方式

发行包（二进制包,1.4.4版本）：https://github.com/alibaba/nacos/archive/refs/tags/1.4.4.zip



发行包是指经过编译、打包处理后的可执行文件或者软件包。它是源码经过构建、编译、打包等过程后生成的，可以直接在目标环境中运行的文件或者包。发行包通常包含了可执行文件、库文件、配置文件等，是用户或者系统管理员部署和使用软件的主要形式。

从 [最新稳定版本](https://github.com/alibaba/nacos/releases) 下载 `nacos-server-$version.zip` 包

```shell
// 解压
unzip nacos-server-$version.zip 或 tar -xvf nacos-server-$version.tar.gz
// 进入目录
cd nacos/bin
```



#### 1.4.3 发行包方式启动

##### 1. 启动Nacos服务器

- Linux/Unix/Mac

启动命令(standalone代表着单机模式运行，非集群模式):

```
sh startup.sh -m standalone
```

如果您使用的是ubuntu系统，或者运行脚本报错提示[[符号找不到，可尝试如下运行：

```
bash startup.sh -m standalone
```

- Windows

启动命令(standalone代表着单机模式运行，非集群模式):

```
startup.cmd -m standalone
```

##### 2. 关闭服务器

- Linux/Unix/Mac

```
sh shutdown.sh
```

- Windows

```
shutdown.cmd
```

或者双击shutdown.cmd运行文件

##### 3. 数据持久化相关配置可参考1.4.4



#### 1.4.4、使用源码搭建、启动

采用直接获取源码的方式启动nacos，拉取nacos源码到本地

```shell
git clone https://github.com/alibaba/nacos.git
```

通过`git tag`查看git仓库中所有的标签信息

通过`git describe --tags`查看当前所选的标签（如果当前提交是一个标签的话，它会显示这个标签的名字；如果不是，它会显示距离最近的标签的名字，以及当前提交与最近标签的相对位置，比如距离最近标签有多少个提交）

选择指定的tag（如选择Tag 1.4.1版本）

```shell
git checkout 1.4.1
```

当然我们也可以在下拉源码的时候直接指定需要的tag

```shell
git clone -b 1.4.1 https://github.com/alibaba/nacos.git
```

创建一个nacos的数据库

![image-20240505001457505](Nacos%E6%BA%90%E7%A0%81%E5%88%86%E6%9E%90%E5%89%8D%E8%A8%80.assets/image-20240505001457505.png)

在数据库中执行 `distribution/conf` 目录下的 nacos-mysql.sql 脚本，初始化nacos数据持久化到mysql相关的表结构和数据

```sql
/*
 * Copyright 1999-2018 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/******************************************/
/*   数据库全名 = nacos_config   */
/*   表名称 = config_info   */
/******************************************/
CREATE TABLE `config_info` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `data_id` varchar(255) NOT NULL COMMENT 'data_id',
  `group_id` varchar(255) DEFAULT NULL,
  `content` longtext NOT NULL COMMENT 'content',
  `md5` varchar(32) DEFAULT NULL COMMENT 'md5',
  `gmt_create` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `gmt_modified` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '修改时间',
  `src_user` text COMMENT 'source user',
  `src_ip` varchar(50) DEFAULT NULL COMMENT 'source ip',
  `app_name` varchar(128) DEFAULT NULL,
  `tenant_id` varchar(128) DEFAULT '' COMMENT '租户字段',
  `c_desc` varchar(256) DEFAULT NULL,
  `c_use` varchar(64) DEFAULT NULL,
  `effect` varchar(64) DEFAULT NULL,
  `type` varchar(64) DEFAULT NULL,
  `c_schema` text,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_configinfo_datagrouptenant` (`data_id`,`group_id`,`tenant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin COMMENT='config_info';

/******************************************/
/*   数据库全名 = nacos_config   */
/*   表名称 = config_info_aggr   */
/******************************************/
CREATE TABLE `config_info_aggr` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `data_id` varchar(255) NOT NULL COMMENT 'data_id',
  `group_id` varchar(255) NOT NULL COMMENT 'group_id',
  `datum_id` varchar(255) NOT NULL COMMENT 'datum_id',
  `content` longtext NOT NULL COMMENT '内容',
  `gmt_modified` datetime NOT NULL COMMENT '修改时间',
  `app_name` varchar(128) DEFAULT NULL,
  `tenant_id` varchar(128) DEFAULT '' COMMENT '租户字段',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_configinfoaggr_datagrouptenantdatum` (`data_id`,`group_id`,`tenant_id`,`datum_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin COMMENT='增加租户字段';


/******************************************/
/*   数据库全名 = nacos_config   */
/*   表名称 = config_info_beta   */
/******************************************/
CREATE TABLE `config_info_beta` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `data_id` varchar(255) NOT NULL COMMENT 'data_id',
  `group_id` varchar(128) NOT NULL COMMENT 'group_id',
  `app_name` varchar(128) DEFAULT NULL COMMENT 'app_name',
  `content` longtext NOT NULL COMMENT 'content',
  `beta_ips` varchar(1024) DEFAULT NULL COMMENT 'betaIps',
  `md5` varchar(32) DEFAULT NULL COMMENT 'md5',
  `gmt_create` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `gmt_modified` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '修改时间',
  `src_user` text COMMENT 'source user',
  `src_ip` varchar(50) DEFAULT NULL COMMENT 'source ip',
  `tenant_id` varchar(128) DEFAULT '' COMMENT '租户字段',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_configinfobeta_datagrouptenant` (`data_id`,`group_id`,`tenant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin COMMENT='config_info_beta';

/******************************************/
/*   数据库全名 = nacos_config   */
/*   表名称 = config_info_tag   */
/******************************************/
CREATE TABLE `config_info_tag` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `data_id` varchar(255) NOT NULL COMMENT 'data_id',
  `group_id` varchar(128) NOT NULL COMMENT 'group_id',
  `tenant_id` varchar(128) DEFAULT '' COMMENT 'tenant_id',
  `tag_id` varchar(128) NOT NULL COMMENT 'tag_id',
  `app_name` varchar(128) DEFAULT NULL COMMENT 'app_name',
  `content` longtext NOT NULL COMMENT 'content',
  `md5` varchar(32) DEFAULT NULL COMMENT 'md5',
  `gmt_create` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `gmt_modified` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '修改时间',
  `src_user` text COMMENT 'source user',
  `src_ip` varchar(50) DEFAULT NULL COMMENT 'source ip',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_configinfotag_datagrouptenanttag` (`data_id`,`group_id`,`tenant_id`,`tag_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin COMMENT='config_info_tag';

/******************************************/
/*   数据库全名 = nacos_config   */
/*   表名称 = config_tags_relation   */
/******************************************/
CREATE TABLE `config_tags_relation` (
  `id` bigint(20) NOT NULL COMMENT 'id',
  `tag_name` varchar(128) NOT NULL COMMENT 'tag_name',
  `tag_type` varchar(64) DEFAULT NULL COMMENT 'tag_type',
  `data_id` varchar(255) NOT NULL COMMENT 'data_id',
  `group_id` varchar(128) NOT NULL COMMENT 'group_id',
  `tenant_id` varchar(128) DEFAULT '' COMMENT 'tenant_id',
  `nid` bigint(20) NOT NULL AUTO_INCREMENT,
  PRIMARY KEY (`nid`),
  UNIQUE KEY `uk_configtagrelation_configidtag` (`id`,`tag_name`,`tag_type`),
  KEY `idx_tenant_id` (`tenant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin COMMENT='config_tag_relation';

/******************************************/
/*   数据库全名 = nacos_config   */
/*   表名称 = group_capacity   */
/******************************************/
CREATE TABLE `group_capacity` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `group_id` varchar(128) NOT NULL DEFAULT '' COMMENT 'Group ID，空字符表示整个集群',
  `quota` int(10) unsigned NOT NULL DEFAULT '0' COMMENT '配额，0表示使用默认值',
  `usage` int(10) unsigned NOT NULL DEFAULT '0' COMMENT '使用量',
  `max_size` int(10) unsigned NOT NULL DEFAULT '0' COMMENT '单个配置大小上限，单位为字节，0表示使用默认值',
  `max_aggr_count` int(10) unsigned NOT NULL DEFAULT '0' COMMENT '聚合子配置最大个数，，0表示使用默认值',
  `max_aggr_size` int(10) unsigned NOT NULL DEFAULT '0' COMMENT '单个聚合数据的子配置大小上限，单位为字节，0表示使用默认值',
  `max_history_count` int(10) unsigned NOT NULL DEFAULT '0' COMMENT '最大变更历史数量',
  `gmt_create` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `gmt_modified` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '修改时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_group_id` (`group_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin COMMENT='集群、各Group容量信息表';

/******************************************/
/*   数据库全名 = nacos_config   */
/*   表名称 = his_config_info   */
/******************************************/
CREATE TABLE `his_config_info` (
  `id` bigint(64) unsigned NOT NULL,
  `nid` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `data_id` varchar(255) NOT NULL,
  `group_id` varchar(128) NOT NULL,
  `app_name` varchar(128) DEFAULT NULL COMMENT 'app_name',
  `content` longtext NOT NULL,
  `md5` varchar(32) DEFAULT NULL,
  `gmt_create` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `gmt_modified` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `src_user` text,
  `src_ip` varchar(50) DEFAULT NULL,
  `op_type` char(10) DEFAULT NULL,
  `tenant_id` varchar(128) DEFAULT '' COMMENT '租户字段',
  PRIMARY KEY (`nid`),
  KEY `idx_gmt_create` (`gmt_create`),
  KEY `idx_gmt_modified` (`gmt_modified`),
  KEY `idx_did` (`data_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin COMMENT='多租户改造';


/******************************************/
/*   数据库全名 = nacos_config   */
/*   表名称 = tenant_capacity   */
/******************************************/
CREATE TABLE `tenant_capacity` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `tenant_id` varchar(128) NOT NULL DEFAULT '' COMMENT 'Tenant ID',
  `quota` int(10) unsigned NOT NULL DEFAULT '0' COMMENT '配额，0表示使用默认值',
  `usage` int(10) unsigned NOT NULL DEFAULT '0' COMMENT '使用量',
  `max_size` int(10) unsigned NOT NULL DEFAULT '0' COMMENT '单个配置大小上限，单位为字节，0表示使用默认值',
  `max_aggr_count` int(10) unsigned NOT NULL DEFAULT '0' COMMENT '聚合子配置最大个数',
  `max_aggr_size` int(10) unsigned NOT NULL DEFAULT '0' COMMENT '单个聚合数据的子配置大小上限，单位为字节，0表示使用默认值',
  `max_history_count` int(10) unsigned NOT NULL DEFAULT '0' COMMENT '最大变更历史数量',
  `gmt_create` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `gmt_modified` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '修改时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_tenant_id` (`tenant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin COMMENT='租户容量信息表';


CREATE TABLE `tenant_info` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `kp` varchar(128) NOT NULL COMMENT 'kp',
  `tenant_id` varchar(128) default '' COMMENT 'tenant_id',
  `tenant_name` varchar(128) default '' COMMENT 'tenant_name',
  `tenant_desc` varchar(256) DEFAULT NULL COMMENT 'tenant_desc',
  `create_source` varchar(32) DEFAULT NULL COMMENT 'create_source',
  `gmt_create` bigint(20) NOT NULL COMMENT '创建时间',
  `gmt_modified` bigint(20) NOT NULL COMMENT '修改时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_tenant_info_kptenantid` (`kp`,`tenant_id`),
  KEY `idx_tenant_id` (`tenant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin COMMENT='tenant_info';

CREATE TABLE `users` (
	`username` varchar(50) NOT NULL PRIMARY KEY,
	`password` varchar(500) NOT NULL,
	`enabled` boolean NOT NULL
);

CREATE TABLE `roles` (
	`username` varchar(50) NOT NULL,
	`role` varchar(50) NOT NULL,
	UNIQUE INDEX `idx_user_role` (`username` ASC, `role` ASC) USING BTREE
);

CREATE TABLE `permissions` (
    `role` varchar(50) NOT NULL,
    `resource` varchar(255) NOT NULL,
    `action` varchar(8) NOT NULL,
    UNIQUE INDEX `uk_role_permission` (`role`,`resource`,`action`) USING BTREE
);

INSERT INTO users (username, password, enabled) VALUES ('nacos', '$2a$10$EuWPZHzz32dJN7jexM34MOeYirDdFAZm2kuWj7VEOJhhZkDrxfvUu', TRUE);

INSERT INTO roles (username, role) VALUES ('nacos', 'ROLE_ADMIN');

```

修改 `console\src\main\resources` 目录下的 `application.properties` 文件里的mysql配置，将数据持久化到数据库

```properties
#*************** Config Module Related Configurations ***************#
### If use MySQL as datasource:
spring.datasource.platform=mysql

### Count of DB:
db.num=1

### Connect URL of DB:
db.url.0=jdbc:mysql://127.0.0.1:3306/nacos?characterEncoding=utf8&connectTimeout=1000&socketTimeout=3000&autoReconnect=true&useUnicode=true&useSSL=false&serverTimezone=UTC
db.user.0=root
db.password.0=123456
```

增加启动vm参数 ，表示单击运行nacos

```
-Dnacos.standalone=true
```

启动nacos-console服务，即nacos客户端

![image-20240505002244208](Nacos%E6%BA%90%E7%A0%81%E5%88%86%E6%9E%90%E5%89%8D%E8%A8%80.assets/image-20240505002244208.png)

登录nacos，账号密码：nacos/nacos

![image-20240505002428016](Nacos%E6%BA%90%E7%A0%81%E5%88%86%E6%9E%90%E5%89%8D%E8%A8%80.assets/image-20240505002428016.png)

进入nacos菜单页面，即表示我们nacos服务启动成功

![image-20240505002443337](Nacos%E6%BA%90%E7%A0%81%E5%88%86%E6%9E%90%E5%89%8D%E8%A8%80.assets/image-20240505002443337.png)



除了nacos服务外，我们还需要准备一个应用服务，用于后续对nacos注册和配置的调试

Idea选择File-New-Project创建一个新的项目

![image-20240505003542492](Nacos%E6%BA%90%E7%A0%81%E5%88%86%E6%9E%90%E5%89%8D%E8%A8%80.assets/image-20240505003542492.png)

选择初始化一个Spring项目

![image-20240505003618464](Nacos%E6%BA%90%E7%A0%81%E5%88%86%E6%9E%90%E5%89%8D%E8%A8%80.assets/image-20240505003618464.png)

相关配置

![image-20240505003640233](Nacos%E6%BA%90%E7%A0%81%E5%88%86%E6%9E%90%E5%89%8D%E8%A8%80.assets/image-20240505003640233.png)

在pom.xml引入相应的依赖，使其成为一个web应用服务

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>2.2.1.RELEASE</version>
        <relativePath/> <!-- lookup parent from repository -->
    </parent>
    <groupId>com.example</groupId>
    <artifactId>demo</artifactId>
    <version>0.0.1-SNAPSHOT</version>
    <name>demo</name>
    <description>Demo project for Spring Boot</description>

    <properties>
        <java.version>1.8</java.version>
    </properties>
    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>

        <!--nacos 注册/发现中心依赖-->
        <dependency>
            <groupId>com.alibaba.cloud</groupId>
            <artifactId>spring-cloud-starter-alibaba-nacos-discovery</artifactId>
            <version>2.1.0.RELEASE</version>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>
        </plugins>
    </build>

</project>

```

配置文件中配置相关的注册中心

```yml
spring:
  application:
    name: demo
  cloud:
    nacos:
      discovery:
        server-addr: 192.168.56.1:8848

server:
  port: 8081
```

在启动类上添加@EnableDiscoveryClient，其是Spring Cloud 提供的一个注解，用于开启服务发现功能。注意，在 Spring Cloud Alibaba 中，如果你使用的是 Nacos 作为服务注册中心，即使没有在启动类上显式添加 `@EnableDiscoveryClient` 注解，也可以将服务注册到 Nacos 注册中心。这是因为 Spring Cloud Alibaba 在集成 Nacos 时，默认已经对服务注册和发现进行了自动配置，无需显式添加 `@EnableDiscoveryClient` 注解。

Spring Cloud Alibaba 通过自动配置实现了将服务注册到 Nacos 注册中心的功能，这包括了服务的注册、发现以及其他相关的配置。因此，即使你没有在启动类上添加 `@EnableDiscoveryClient` 注解，Spring Cloud Alibaba 仍然能够将服务注册到 Nacos 注册中心，并能够被其他服务发现和调用。

```java
@SpringBootApplication
/*@EnableDiscoveryClient*/
public class DemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }

}
```

启动该服务，在nacos客户端的服务列表中可以看到，则表示注册成功

![image-20240505141952064](Nacos%E6%BA%90%E7%A0%81%E5%88%86%E6%9E%90%E5%89%8D%E8%A8%80.assets/image-20240505141952064.png)

到此，我们完成了环境的搭建



### 1.5、相关问题汇总

#### 1.5.1、java: 程序包com.alibaba.nacos.consistency.entity不存在

场景：

![image-20240829094733139](Nacos%E6%BA%90%E7%A0%81%E5%88%86%E6%9E%90%E5%89%8D%E8%A8%80.assets/image-20240829094733139.png)

通过nacos将整个项目重新编译一下即可，相关原因可以及参考文章：https://blog.csdn.net/ibigboy/article/details/119413998

> 原因是com.alibaba.nacos.consistency.entity包目录是由[protobuf](https://so.csdn.net/so/search?q=protobuf&spm=1001.2101.3001.7020)在编译时自动生成的
>
> 可以通过mvn compile来自动生成他们。如果使用的是IDEA，也可以使用IDEA的protobuf插件。



#### 1.5.2 中文路径问题

**问题描述：**

配置了单例模式启动

```
-Dnacos.standalone=true
```

配置了数据库相关内容

启动后报错

```


         ,--.
       ,--.'|
   ,--,:  : |                                           Nacos 
,`--.'`|  ' :                       ,---.               Running in stand alone mode, All function modules
|   :  :  | |                      '   ,'\   .--.--.    Port: 8848
:   |   \ | :  ,--.--.     ,---.  /   /   | /  /    '   Pid: 14472
|   : '  '; | /       \   /     \.   ; ,. :|  :  /`./   Console: http://192.168.56.1:8848/nacos/index.html
'   ' ;.    ;.--.  .-. | /    / ''   | |: :|  :  ;_
|   | | \   | \__\/: . ..    ' / '   | .; : \  \    `.      https://nacos.io
'   : |  ; .' ," .--.; |'   ; :__|   :    |  `----.   \
|   | '`--'  /  /  ,.  |'   | '.'|\   \  /  /  /`--'  /
'   : |     ;  :   .'   \   :    : `----'  '--'.     /
;   |.'     |  ,     .-./\   \  /            `--'---'
'---'        `--`---'     `----'

2024-08-29 11:26:25.158  INFO 14472 --- [           main] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat initialized with port(s): 8848 (http)
2024-08-29 11:26:25.317  INFO 14472 --- [           main] w.s.c.ServletWebServerApplicationContext : Root WebApplicationContext: initialization completed in 2450 ms
2024-08-29 11:26:27.636  WARN 14472 --- [           main] ConfigServletWebServerApplicationContext : Exception encountered during context initialization - cancelling refresh attempt: org.springframework.beans.factory.UnsatisfiedDependencyException: Error creating bean with name 'namingHealthController': Unsatisfied dependency expressed through field 'healthOperatorV2'; nested exception is org.springframework.beans.factory.UnsatisfiedDependencyException: Error creating bean with name 'healthOperatorV2Impl' defined in file [D:\codesaver\Banana-project\Banana-Own-Projects\nacos-2.2.0\naming\target\classes\com\alibaba\nacos\naming\core\HealthOperatorV2Impl.class]: Unsatisfied dependency expressed through constructor parameter 2; nested exception is org.springframework.beans.factory.UnsatisfiedDependencyException: Error creating bean with name 'clientOperationServiceProxy' defined in file [D:\codesaver\Banana-project\Banana-Own-Projects\nacos-2.2.0\naming\target\classes\com\alibaba\nacos\naming\core\v2\service\ClientOperationServiceProxy.class]: Unsatisfied dependency expressed through constructor parameter 1; nested exception is org.springframework.beans.factory.BeanCreationException: Error creating bean with name 'persistentClientOperationServiceImpl' defined in file [D:\codesaver\Banana-project\Banana-Own-Projects\nacos-2.2.0\naming\target\classes\com\alibaba\nacos\naming\core\v2\service\impl\PersistentClientOperationServiceImpl.class]: Bean instantiation via constructor failed; nested exception is org.springframework.beans.BeanInstantiationException: Failed to instantiate [com.alibaba.nacos.naming.core.v2.service.impl.PersistentClientOperationServiceImpl]: Constructor threw exception; nested exception is java.lang.IllegalStateException: Fail to init node, please see the logs to find the reason.
2024-08-29 11:26:27.659  INFO 14472 --- [           main] c.a.n.c.l.StartingApplicationListener    : Nacos Log files: C:\Users\23220\nacos\logs
2024-08-29 11:26:27.660  INFO 14472 --- [           main] c.a.n.c.l.StartingApplicationListener    : Nacos Log files: C:\Users\23220\nacos\conf
2024-08-29 11:26:27.660  INFO 14472 --- [           main] c.a.n.c.l.StartingApplicationListener    : Nacos Log files: C:\Users\23220\nacos\data
2024-08-29 11:26:27.660 ERROR 14472 --- [           main] c.a.n.c.l.StartingApplicationListener    : Startup errors : 

org.springframework.beans.factory.UnsatisfiedDependencyException: Error creating bean with name 'namingHealthController': Unsatisfied dependency expressed through field 'healthOperatorV2'; nested exception is org.springframework.beans.factory.UnsatisfiedDependencyException: Error creating bean with name 'healthOperatorV2Impl' defined in file [D:\codesaver\Banana-project\Banana-Own-Projects\nacos-2.2.0\naming\target\classes\com\alibaba\nacos\naming\core\HealthOperatorV2Impl.class]: Unsatisfied dependency expressed through constructor parameter 2; nested exception is org.springframework.beans.factory.UnsatisfiedDependencyException: Error creating bean with name 'clientOperationServiceProxy' defined in file [D:\codesaver\Banana-project\Banana-Own-Projects\nacos-2.2.0\naming\target\classes\com\alibaba\nacos\naming\core\v2\service\ClientOperationServiceProxy.class]: Unsatisfied dependency expressed through constructor parameter 1; nested exception is org.springframework.beans.factory.BeanCreationException: Error creating bean with name 'persistentClientOperationServiceImpl' defined in file [D:\codesaver\Banana-project\Banana-Own-Projects\nacos-2.2.0\naming\target\classes\com\alibaba\nacos\naming\core\v2\service\impl\PersistentClientOperationServiceImpl.class]: Bean instantiation via constructor failed; nested exception is org.springframework.beans.BeanInstantiationException: Failed to instantiate [com.alibaba.nacos.naming.core.v2.service.impl.PersistentClientOperationServiceImpl]: Constructor threw exception; nested exception is java.lang.IllegalStateException: Fail to init node, please see the logs to find the reason.
	at org.springframework.beans.factory.annotation.AutowiredAnnotationBeanPostProcessor$AutowiredFieldElement.resolveFieldValue(AutowiredAnnotationBeanPostProcessor.java:659)
	at org.springframework.beans.factory.annotation.AutowiredAnnotationBeanPostProcessor$AutowiredFieldElement.inject(AutowiredAnnotationBeanPostProcessor.java:639)
	at org.springframework.beans.factory.annotation.InjectionMetadata.inject(InjectionMetadata.java:119)
	at org.springframework.beans.factory.annotation.AutowiredAnnotationBeanPostProcessor.postProcessProperties(AutowiredAnnotationBeanPostProcessor.java:399)
	at org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory.populateBean(AbstractAutowireCapableBeanFactory.java:1431)
	at org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory.doCreateBean(AbstractAutowireCapableBeanFactory.java:619)
	at org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory.createBean(AbstractAutowireCapableBeanFactory.java:542)
	at org.springframework.beans.factory.support.AbstractBeanFactory.lambda$doGetBean$0(AbstractBeanFactory.java:335)
	at org.springframework.beans.factory.support.DefaultSingletonBeanRegistry.getSingleton(DefaultSingletonBeanRegistry.java:234)
	at org.springframework.beans.factory.support.AbstractBeanFactory.doGetBean(AbstractBeanFactory.java:333)
	at org.springframework.beans.factory.support.AbstractBeanFactory.getBean(AbstractBeanFactory.java:208)
	at org.springframework.beans.factory.support.DefaultListableBeanFactory.preInstantiateSingletons(DefaultListableBeanFactory.java:953)
	at org.springframework.context.support.AbstractApplicationContext.finishBeanFactoryInitialization(AbstractApplicationContext.java:918)
	at org.springframework.context.support.AbstractApplicationContext.refresh(AbstractApplicationContext.java:583)
	at org.springframework.boot.web.servlet.context.ServletWebServerApplicationContext.refresh(ServletWebServerApplicationContext.java:145)
	at org.springframework.boot.SpringApplication.refresh(SpringApplication.java:745)
	at org.springframework.boot.SpringApplication.refreshContext(SpringApplication.java:420)
	at org.springframework.boot.SpringApplication.run(SpringApplication.java:307)
	at org.springframework.boot.SpringApplication.run(SpringApplication.java:1317)
	at org.springframework.boot.SpringApplication.run(SpringApplication.java:1306)
	at com.alibaba.nacos.Nacos.main(Nacos.java:35)
Caused by: org.springframework.beans.factory.UnsatisfiedDependencyException: Error creating bean with name 'healthOperatorV2Impl' defined in file [D:\codesaver\Banana-project\Banana-Own-Projects\nacos-2.2.0\naming\target\classes\com\alibaba\nacos\naming\core\HealthOperatorV2Impl.class]: Unsatisfied dependency expressed through constructor parameter 2; nested exception is org.springframework.beans.factory.UnsatisfiedDependencyException: Error creating bean with name 'clientOperationServiceProxy' defined in file [D:\codesaver\Banana-project\Banana-Own-Projects\nacos-2.2.0\naming\target\classes\com\alibaba\nacos\naming\core\v2\service\ClientOperationServiceProxy.class]: Unsatisfied dependency expressed through constructor parameter 1; nested exception is org.springframework.beans.factory.BeanCreationException: Error creating bean with name 'persistentClientOperationServiceImpl' defined in file [D:\codesaver\Banana-project\Banana-Own-Projects\nacos-2.2.0\naming\target\classes\com\alibaba\nacos\naming\core\v2\service\impl\PersistentClientOperationServiceImpl.class]: Bean instantiation via constructor failed; nested exception is org.springframework.beans.BeanInstantiationException: Failed to instantiate [com.alibaba.nacos.naming.core.v2.service.impl.PersistentClientOperationServiceImpl]: Constructor threw exception; nested exception is java.lang.IllegalStateException: Fail to init node, please see the logs to find the reason.
	at org.springframework.beans.factory.support.ConstructorResolver.createArgumentArray(ConstructorResolver.java:800)
	at org.springframework.beans.factory.support.ConstructorResolver.autowireConstructor(ConstructorResolver.java:229)
	at org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory.autowireConstructor(AbstractAutowireCapableBeanFactory.java:1372)
	at org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory.createBeanInstance(AbstractAutowireCapableBeanFactory.java:1222)
	at org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory.doCreateBean(AbstractAutowireCapableBeanFactory.java:582)
	at org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory.createBean(AbstractAutowireCapableBeanFactory.java:542)
	at org.springframework.beans.factory.support.AbstractBeanFactory.lambda$doGetBean$0(AbstractBeanFactory.java:335)
	at org.springframework.beans.factory.support.DefaultSingletonBeanRegistry.getSingleton(DefaultSingletonBeanRegistry.java:234)
	at org.springframework.beans.factory.support.AbstractBeanFactory.doGetBean(AbstractBeanFactory.java:333)
	at org.springframework.beans.factory.support.AbstractBeanFactory.getBean(AbstractBeanFactory.java:208)
	at org.springframework.beans.factory.config.DependencyDescriptor.resolveCandidate(DependencyDescriptor.java:276)
	at org.springframework.beans.factory.support.DefaultListableBeanFactory.doResolveDependency(DefaultListableBeanFactory.java:1389)
	at org.springframework.beans.factory.support.DefaultListableBeanFactory.resolveDependency(DefaultListableBeanFactory.java:1309)
	at org.springframework.beans.factory.annotation.AutowiredAnnotationBeanPostProcessor$AutowiredFieldElement.resolveFieldValue(AutowiredAnnotationBeanPostProcessor.java:656)
	... 20 common frames omitted
Caused by: org.springframework.beans.factory.UnsatisfiedDependencyException: Error creating bean with name 'clientOperationServiceProxy' defined in file [D:\codesaver\Banana-project\Banana-Own-Projects\nacos-2.2.0\naming\target\classes\com\alibaba\nacos\naming\core\v2\service\ClientOperationServiceProxy.class]: Unsatisfied dependency expressed through constructor parameter 1; nested exception is org.springframework.beans.factory.BeanCreationException: Error creating bean with name 'persistentClientOperationServiceImpl' defined in file [D:\codesaver\Banana-project\Banana-Own-Projects\nacos-2.2.0\naming\target\classes\com\alibaba\nacos\naming\core\v2\service\impl\PersistentClientOperationServiceImpl.class]: Bean instantiation via constructor failed; nested exception is org.springframework.beans.BeanInstantiationException: Failed to instantiate [com.alibaba.nacos.naming.core.v2.service.impl.PersistentClientOperationServiceImpl]: Constructor threw exception; nested exception is java.lang.IllegalStateException: Fail to init node, please see the logs to find the reason.
	at org.springframework.beans.factory.support.ConstructorResolver.createArgumentArray(ConstructorResolver.java:800)
	at org.springframework.beans.factory.support.ConstructorResolver.autowireConstructor(ConstructorResolver.java:229)
	at org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory.autowireConstructor(AbstractAutowireCapableBeanFactory.java:1372)
	at org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory.createBeanInstance(AbstractAutowireCapableBeanFactory.java:1222)
	at org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory.doCreateBean(AbstractAutowireCapableBeanFactory.java:582)
	at org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory.createBean(AbstractAutowireCapableBeanFactory.java:542)
	at org.springframework.beans.factory.support.AbstractBeanFactory.lambda$doGetBean$0(AbstractBeanFactory.java:335)
	at org.springframework.beans.factory.support.DefaultSingletonBeanRegistry.getSingleton(DefaultSingletonBeanRegistry.java:234)
	at org.springframework.beans.factory.support.AbstractBeanFactory.doGetBean(AbstractBeanFactory.java:333)
	at org.springframework.beans.factory.support.AbstractBeanFactory.getBean(AbstractBeanFactory.java:208)
	at org.springframework.beans.factory.config.DependencyDescriptor.resolveCandidate(DependencyDescriptor.java:276)
	at org.springframework.beans.factory.support.DefaultListableBeanFactory.doResolveDependency(DefaultListableBeanFactory.java:1389)
	at org.springframework.beans.factory.support.DefaultListableBeanFactory.resolveDependency(DefaultListableBeanFactory.java:1309)
	at org.springframework.beans.factory.support.ConstructorResolver.resolveAutowiredArgument(ConstructorResolver.java:887)
	at org.springframework.beans.factory.support.ConstructorResolver.createArgumentArray(ConstructorResolver.java:791)
	... 33 common frames omitted
Caused by: org.springframework.beans.factory.BeanCreationException: Error creating bean with name 'persistentClientOperationServiceImpl' defined in file [D:\codesaver\Banana-project\Banana-Own-Projects\nacos-2.2.0\naming\target\classes\com\alibaba\nacos\naming\core\v2\service\impl\PersistentClientOperationServiceImpl.class]: Bean instantiation via constructor failed; nested exception is org.springframework.beans.BeanInstantiationException: Failed to instantiate [com.alibaba.nacos.naming.core.v2.service.impl.PersistentClientOperationServiceImpl]: Constructor threw exception; nested exception is java.lang.IllegalStateException: Fail to init node, please see the logs to find the reason.
	at org.springframework.beans.factory.support.ConstructorResolver.instantiate(ConstructorResolver.java:315)
	at org.springframework.beans.factory.support.ConstructorResolver.autowireConstructor(ConstructorResolver.java:296)
	at org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory.autowireConstructor(AbstractAutowireCapableBeanFactory.java:1372)
	at org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory.createBeanInstance(AbstractAutowireCapableBeanFactory.java:1222)
	at org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory.doCreateBean(AbstractAutowireCapableBeanFactory.java:582)
	at org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory.createBean(AbstractAutowireCapableBeanFactory.java:542)
	at org.springframework.beans.factory.support.AbstractBeanFactory.lambda$doGetBean$0(AbstractBeanFactory.java:335)
	at org.springframework.beans.factory.support.DefaultSingletonBeanRegistry.getSingleton(DefaultSingletonBeanRegistry.java:234)
	at org.springframework.beans.factory.support.AbstractBeanFactory.doGetBean(AbstractBeanFactory.java:333)
	at org.springframework.beans.factory.support.AbstractBeanFactory.getBean(AbstractBeanFactory.java:208)
	at org.springframework.beans.factory.config.DependencyDescriptor.resolveCandidate(DependencyDescriptor.java:276)
	at org.springframework.beans.factory.support.DefaultListableBeanFactory.doResolveDependency(DefaultListableBeanFactory.java:1389)
	at org.springframework.beans.factory.support.DefaultListableBeanFactory.resolveDependency(DefaultListableBeanFactory.java:1309)
	at org.springframework.beans.factory.support.ConstructorResolver.resolveAutowiredArgument(ConstructorResolver.java:887)
	at org.springframework.beans.factory.support.ConstructorResolver.createArgumentArray(ConstructorResolver.java:791)
	... 47 common frames omitted
Caused by: org.springframework.beans.BeanInstantiationException: Failed to instantiate [com.alibaba.nacos.naming.core.v2.service.impl.PersistentClientOperationServiceImpl]: Constructor threw exception; nested exception is java.lang.IllegalStateException: Fail to init node, please see the logs to find the reason.
	at org.springframework.beans.BeanUtils.instantiateClass(BeanUtils.java:224)
	at org.springframework.beans.factory.support.SimpleInstantiationStrategy.instantiate(SimpleInstantiationStrategy.java:117)
	at org.springframework.beans.factory.support.ConstructorResolver.instantiate(ConstructorResolver.java:311)
	... 61 common frames omitted
Caused by: java.lang.IllegalStateException: Fail to init node, please see the logs to find the reason.
	at com.alipay.sofa.jraft.RaftServiceFactory.createAndInitRaftNode(RaftServiceFactory.java:48)
	at com.alipay.sofa.jraft.RaftGroupService.start(RaftGroupService.java:129)
	at com.alibaba.nacos.core.distributed.raft.JRaftServer.createMultiRaftGroup(JRaftServer.java:260)
	at com.alibaba.nacos.core.distributed.raft.JRaftProtocol.addRequestProcessors(JRaftProtocol.java:163)
	at com.alibaba.nacos.naming.core.v2.service.impl.PersistentClientOperationServiceImpl.<init>(PersistentClientOperationServiceImpl.java:96)
	at sun.reflect.NativeConstructorAccessorImpl.newInstance0(Native Method)
	at sun.reflect.NativeConstructorAccessorImpl.newInstance(NativeConstructorAccessorImpl.java:62)
	at sun.reflect.DelegatingConstructorAccessorImpl.newInstance(DelegatingConstructorAccessorImpl.java:45)
	at java.lang.reflect.Constructor.newInstance(Constructor.java:423)
	at org.springframework.beans.BeanUtils.instantiateClass(BeanUtils.java:211)
	... 63 common frames omitted

2024-08-29 11:26:27.966  WARN 14472 --- [           main] c.a.nacos.sys.file.WatchFileCenter       : [WatchFileCenter] start close
2024-08-29 11:26:27.967  WARN 14472 --- [           main] c.a.nacos.sys.file.WatchFileCenter       : [WatchFileCenter] start to shutdown this watcher which is watch : C:\Users\23220\nacos\conf
2024-08-29 11:26:27.967  WARN 14472 --- [           main] c.a.nacos.sys.file.WatchFileCenter       : [WatchFileCenter] already closed
2024-08-29 11:26:27.967  WARN 14472 --- [           main] c.a.nacos.common.notify.NotifyCenter     : [NotifyCenter] Start destroying Publisher
2024-08-29 11:26:27.967  WARN 14472 --- [           main] c.a.nacos.common.notify.NotifyCenter     : [NotifyCenter] Destruction of the end
2024-08-29 11:26:27.968 ERROR 14472 --- [           main] c.a.n.c.l.StartingApplicationListener    : Nacos failed to start, please see C:\Users\23220\nacos\logs\nacos.log for more details.
2024-08-29 11:26:27.982  INFO 14472 --- [           main] ConditionEvaluationReportLoggingListener : 

Error starting ApplicationContext. To display the conditions report re-run your application with 'debug' enabled.
2024-08-29 11:26:28.013 ERROR 14472 --- [           main] o.s.boot.SpringApplication               : Application run failed

org.springframework.beans.factory.UnsatisfiedDependencyException: Error creating bean with name 'namingHealthController': Unsatisfied dependency expressed through field 'healthOperatorV2'; nested exception is org.springframework.beans.factory.UnsatisfiedDependencyException: Error creating bean with name 'healthOperatorV2Impl' defined in file [D:\codesaver\Banana-project\Banana-Own-Projects\nacos-2.2.0\naming\target\classes\com\alibaba\nacos\naming\core\HealthOperatorV2Impl.class]: Unsatisfied dependency expressed through constructor parameter 2; nested exception is org.springframework.beans.factory.UnsatisfiedDependencyException: Error creating bean with name 'clientOperationServiceProxy' defined in file [D:\codesaver\Banana-project\Banana-Own-Projects\nacos-2.2.0\naming\target\classes\com\alibaba\nacos\naming\core\v2\service\ClientOperationServiceProxy.class]: Unsatisfied dependency expressed through constructor parameter 1; nested exception is org.springframework.beans.factory.BeanCreationException: Error creating bean with name 'persistentClientOperationServiceImpl' defined in file [D:\codesaver\Banana-project\Banana-Own-Projects\nacos-2.2.0\naming\target\classes\com\alibaba\nacos\naming\core\v2\service\impl\PersistentClientOperationServiceImpl.class]: Bean instantiation via constructor failed; nested exception is org.springframework.beans.BeanInstantiationException: Failed to instantiate [com.alibaba.nacos.naming.core.v2.service.impl.PersistentClientOperationServiceImpl]: Constructor threw exception; nested exception is java.lang.IllegalStateException: Fail to init node, please see the logs to find the reason.
	at org.springframework.beans.factory.annotation.AutowiredAnnotationBeanPostProcessor$AutowiredFieldElement.resolveFieldValue(AutowiredAnnotationBeanPostProcessor.java:659)
	at org.springframework.beans.factory.annotation.AutowiredAnnotationBeanPostProcessor$AutowiredFieldElement.inject(AutowiredAnnotationBeanPostProcessor.java:639)
	at org.springframework.beans.factory.annotation.InjectionMetadata.inject(InjectionMetadata.java:119)
	at org.springframework.beans.factory.annotation.AutowiredAnnotationBeanPostProcessor.postProcessProperties(AutowiredAnnotationBeanPostProcessor.java:399)
	at org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory.populateBean(AbstractAutowireCapableBeanFactory.java:1431)
	at org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory.doCreateBean(AbstractAutowireCapableBeanFactory.java:619)
	at org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory.createBean(AbstractAutowireCapableBeanFactory.java:542)
	at org.springframework.beans.factory.support.AbstractBeanFactory.lambda$doGetBean$0(AbstractBeanFactory.java:335)
	at org.springframework.beans.factory.support.DefaultSingletonBeanRegistry.getSingleton(DefaultSingletonBeanRegistry.java:234)
	at org.springframework.beans.factory.support.AbstractBeanFactory.doGetBean(AbstractBeanFactory.java:333)
	at org.springframework.beans.factory.support.AbstractBeanFactory.getBean(AbstractBeanFactory.java:208)
	at org.springframework.beans.factory.support.DefaultListableBeanFactory.preInstantiateSingletons(DefaultListableBeanFactory.java:953)
	at org.springframework.context.support.AbstractApplicationContext.finishBeanFactoryInitialization(AbstractApplicationContext.java:918)
	at org.springframework.context.support.AbstractApplicationContext.refresh(AbstractApplicationContext.java:583)
	at org.springframework.boot.web.servlet.context.ServletWebServerApplicationContext.refresh(ServletWebServerApplicationContext.java:145)
	at org.springframework.boot.SpringApplication.refresh(SpringApplication.java:745)
	at org.springframework.boot.SpringApplication.refreshContext(SpringApplication.java:420)
	at org.springframework.boot.SpringApplication.run(SpringApplication.java:307)
	at org.springframework.boot.SpringApplication.run(SpringApplication.java:1317)
	at org.springframework.boot.SpringApplication.run(SpringApplication.java:1306)
	at com.alibaba.nacos.Nacos.main(Nacos.java:35)
Caused by: org.springframework.beans.factory.UnsatisfiedDependencyException: Error creating bean with name 'healthOperatorV2Impl' defined in file [D:\codesaver\Banana-project\Banana-Own-Projects\nacos-2.2.0\naming\target\classes\com\alibaba\nacos\naming\core\HealthOperatorV2Impl.class]: Unsatisfied dependency expressed through constructor parameter 2; nested exception is org.springframework.beans.factory.UnsatisfiedDependencyException: Error creating bean with name 'clientOperationServiceProxy' defined in file [D:\codesaver\Banana-project\Banana-Own-Projects\nacos-2.2.0\naming\target\classes\com\alibaba\nacos\naming\core\v2\service\ClientOperationServiceProxy.class]: Unsatisfied dependency expressed through constructor parameter 1; nested exception is org.springframework.beans.factory.BeanCreationException: Error creating bean with name 'persistentClientOperationServiceImpl' defined in file [D:\codesaver\Banana-project\Banana-Own-Projects\nacos-2.2.0\naming\target\classes\com\alibaba\nacos\naming\core\v2\service\impl\PersistentClientOperationServiceImpl.class]: Bean instantiation via constructor failed; nested exception is org.springframework.beans.BeanInstantiationException: Failed to instantiate [com.alibaba.nacos.naming.core.v2.service.impl.PersistentClientOperationServiceImpl]: Constructor threw exception; nested exception is java.lang.IllegalStateException: Fail to init node, please see the logs to find the reason.
	at org.springframework.beans.factory.support.ConstructorResolver.createArgumentArray(ConstructorResolver.java:800)
	at org.springframework.beans.factory.support.ConstructorResolver.autowireConstructor(ConstructorResolver.java:229)
	at org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory.autowireConstructor(AbstractAutowireCapableBeanFactory.java:1372)
	at org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory.createBeanInstance(AbstractAutowireCapableBeanFactory.java:1222)
	at org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory.doCreateBean(AbstractAutowireCapableBeanFactory.java:582)
	at org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory.createBean(AbstractAutowireCapableBeanFactory.java:542)
	at org.springframework.beans.factory.support.AbstractBeanFactory.lambda$doGetBean$0(AbstractBeanFactory.java:335)
	at org.springframework.beans.factory.support.DefaultSingletonBeanRegistry.getSingleton(DefaultSingletonBeanRegistry.java:234)
	at org.springframework.beans.factory.support.AbstractBeanFactory.doGetBean(AbstractBeanFactory.java:333)
	at org.springframework.beans.factory.support.AbstractBeanFactory.getBean(AbstractBeanFactory.java:208)
	at org.springframework.beans.factory.config.DependencyDescriptor.resolveCandidate(DependencyDescriptor.java:276)
	at org.springframework.beans.factory.support.DefaultListableBeanFactory.doResolveDependency(DefaultListableBeanFactory.java:1389)
	at org.springframework.beans.factory.support.DefaultListableBeanFactory.resolveDependency(DefaultListableBeanFactory.java:1309)
	at org.springframework.beans.factory.annotation.AutowiredAnnotationBeanPostProcessor$AutowiredFieldElement.resolveFieldValue(AutowiredAnnotationBeanPostProcessor.java:656)
	... 20 common frames omitted
Caused by: org.springframework.beans.factory.UnsatisfiedDependencyException: Error creating bean with name 'clientOperationServiceProxy' defined in file [D:\codesaver\Banana-project\Banana-Own-Projects\nacos-2.2.0\naming\target\classes\com\alibaba\nacos\naming\core\v2\service\ClientOperationServiceProxy.class]: Unsatisfied dependency expressed through constructor parameter 1; nested exception is org.springframework.beans.factory.BeanCreationException: Error creating bean with name 'persistentClientOperationServiceImpl' defined in file [D:\codesaver\Banana-project\Banana-Own-Projects\nacos-2.2.0\naming\target\classes\com\alibaba\nacos\naming\core\v2\service\impl\PersistentClientOperationServiceImpl.class]: Bean instantiation via constructor failed; nested exception is org.springframework.beans.BeanInstantiationException: Failed to instantiate [com.alibaba.nacos.naming.core.v2.service.impl.PersistentClientOperationServiceImpl]: Constructor threw exception; nested exception is java.lang.IllegalStateException: Fail to init node, please see the logs to find the reason.
	at org.springframework.beans.factory.support.ConstructorResolver.createArgumentArray(ConstructorResolver.java:800)
	at org.springframework.beans.factory.support.ConstructorResolver.autowireConstructor(ConstructorResolver.java:229)
	at org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory.autowireConstructor(AbstractAutowireCapableBeanFactory.java:1372)
	at org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory.createBeanInstance(AbstractAutowireCapableBeanFactory.java:1222)
	at org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory.doCreateBean(AbstractAutowireCapableBeanFactory.java:582)
	at org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory.createBean(AbstractAutowireCapableBeanFactory.java:542)
	at org.springframework.beans.factory.support.AbstractBeanFactory.lambda$doGetBean$0(AbstractBeanFactory.java:335)
	at org.springframework.beans.factory.support.DefaultSingletonBeanRegistry.getSingleton(DefaultSingletonBeanRegistry.java:234)
	at org.springframework.beans.factory.support.AbstractBeanFactory.doGetBean(AbstractBeanFactory.java:333)
	at org.springframework.beans.factory.support.AbstractBeanFactory.getBean(AbstractBeanFactory.java:208)
	at org.springframework.beans.factory.config.DependencyDescriptor.resolveCandidate(DependencyDescriptor.java:276)
	at org.springframework.beans.factory.support.DefaultListableBeanFactory.doResolveDependency(DefaultListableBeanFactory.java:1389)
	at org.springframework.beans.factory.support.DefaultListableBeanFactory.resolveDependency(DefaultListableBeanFactory.java:1309)
	at org.springframework.beans.factory.support.ConstructorResolver.resolveAutowiredArgument(ConstructorResolver.java:887)
	at org.springframework.beans.factory.support.ConstructorResolver.createArgumentArray(ConstructorResolver.java:791)
	... 33 common frames omitted
Caused by: org.springframework.beans.factory.BeanCreationException: Error creating bean with name 'persistentClientOperationServiceImpl' defined in file [D:\codesaver\Banana-project\Banana-Own-Projects\nacos-2.2.0\naming\target\classes\com\alibaba\nacos\naming\core\v2\service\impl\PersistentClientOperationServiceImpl.class]: Bean instantiation via constructor failed; nested exception is org.springframework.beans.BeanInstantiationException: Failed to instantiate [com.alibaba.nacos.naming.core.v2.service.impl.PersistentClientOperationServiceImpl]: Constructor threw exception; nested exception is java.lang.IllegalStateException: Fail to init node, please see the logs to find the reason.
	at org.springframework.beans.factory.support.ConstructorResolver.instantiate(ConstructorResolver.java:315)
	at org.springframework.beans.factory.support.ConstructorResolver.autowireConstructor(ConstructorResolver.java:296)
	at org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory.autowireConstructor(AbstractAutowireCapableBeanFactory.java:1372)
	at org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory.createBeanInstance(AbstractAutowireCapableBeanFactory.java:1222)
	at org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory.doCreateBean(AbstractAutowireCapableBeanFactory.java:582)
	at org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory.createBean(AbstractAutowireCapableBeanFactory.java:542)
	at org.springframework.beans.factory.support.AbstractBeanFactory.lambda$doGetBean$0(AbstractBeanFactory.java:335)
	at org.springframework.beans.factory.support.DefaultSingletonBeanRegistry.getSingleton(DefaultSingletonBeanRegistry.java:234)
	at org.springframework.beans.factory.support.AbstractBeanFactory.doGetBean(AbstractBeanFactory.java:333)
	at org.springframework.beans.factory.support.AbstractBeanFactory.getBean(AbstractBeanFactory.java:208)
	at org.springframework.beans.factory.config.DependencyDescriptor.resolveCandidate(DependencyDescriptor.java:276)
	at org.springframework.beans.factory.support.DefaultListableBeanFactory.doResolveDependency(DefaultListableBeanFactory.java:1389)
	at org.springframework.beans.factory.support.DefaultListableBeanFactory.resolveDependency(DefaultListableBeanFactory.java:1309)
	at org.springframework.beans.factory.support.ConstructorResolver.resolveAutowiredArgument(ConstructorResolver.java:887)
	at org.springframework.beans.factory.support.ConstructorResolver.createArgumentArray(ConstructorResolver.java:791)
	... 47 common frames omitted
Caused by: org.springframework.beans.BeanInstantiationException: Failed to instantiate [com.alibaba.nacos.naming.core.v2.service.impl.PersistentClientOperationServiceImpl]: Constructor threw exception; nested exception is java.lang.IllegalStateException: Fail to init node, please see the logs to find the reason.
	at org.springframework.beans.BeanUtils.instantiateClass(BeanUtils.java:224)
	at org.springframework.beans.factory.support.SimpleInstantiationStrategy.instantiate(SimpleInstantiationStrategy.java:117)
	at org.springframework.beans.factory.support.ConstructorResolver.instantiate(ConstructorResolver.java:311)
	... 61 common frames omitted
Caused by: java.lang.IllegalStateException: Fail to init node, please see the logs to find the reason.
	at com.alipay.sofa.jraft.RaftServiceFactory.createAndInitRaftNode(RaftServiceFactory.java:48)
	at com.alipay.sofa.jraft.RaftGroupService.start(RaftGroupService.java:129)
	at com.alibaba.nacos.core.distributed.raft.JRaftServer.createMultiRaftGroup(JRaftServer.java:260)
	at com.alibaba.nacos.core.distributed.raft.JRaftProtocol.addRequestProcessors(JRaftProtocol.java:163)
	at com.alibaba.nacos.naming.core.v2.service.impl.PersistentClientOperationServiceImpl.<init>(PersistentClientOperationServiceImpl.java:96)
	at sun.reflect.NativeConstructorAccessorImpl.newInstance0(Native Method)
	at sun.reflect.NativeConstructorAccessorImpl.newInstance(NativeConstructorAccessorImpl.java:62)
	at sun.reflect.DelegatingConstructorAccessorImpl.newInstance(DelegatingConstructorAccessorImpl.java:45)
	at java.lang.reflect.Constructor.newInstance(Constructor.java:423)
	at org.springframework.beans.BeanUtils.instantiateClass(BeanUtils.java:211)
	... 63 common frames omitted

2024-08-29 11:26:28.015  WARN 14472 --- [       Thread-5] c.a.n.common.executor.ThreadPoolManager  : [ThreadPoolManager] Start destroying ThreadPool
2024-08-29 11:26:28.015  WARN 14472 --- [       Thread-5] c.a.n.common.executor.ThreadPoolManager  : [ThreadPoolManager] Destruction of the end
Disconnected from the target VM, address: '127.0.0.1:54508', transport: 'socket'

Process finished with exit code 1

```



**解决：**

配置VM参数：

```
-Dnacos.home=D:\nacos-2.0.0-bugfix\logs\
```



**原因：**

参考文章：

- https://github.com/alibaba/nacos/issues/10870

- https://blog.csdn.net/qq_42857911/article/details/132185015

 

## 二、Nacos源码分析文章

- [Nacos源码分析一()]()