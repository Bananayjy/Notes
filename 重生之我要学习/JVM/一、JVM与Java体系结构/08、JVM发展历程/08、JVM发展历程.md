## 08、JVM发展历程

### 8.1 Sun Classic VM

#### 1. 说明

- 早在1996年Java1.0版本的时候，sun公司发布了一款名为SunclassicVM的Java虚拟机，它同时也是世界上第一款商用Java虚拟机，JDK1.4时完全被淘汰。

- 这款虚拟机内部只提供解释器，没有即时编译器，无法对热点代码进行缓存，需要逐行对字节码文件进行解析，效率慢、程序性能低，但是其响应的时间是非常快的。

- 如果使用JIT编译器，就需要进行外挂。但是一旦使用了JIT编译器，JIT就会接管虚拟机的执行系统。解释器就不再工作。解释器和编译器不能配合工作。
  现在hotspot内置了此虚拟机。

#### 2. 注意

在实际过程中，如果只有JIT（Just-In-Time ，即时编译器）编译器也是不行的，因为其在程序运行前，需要等待时间，去对一些热点代码进行处理，并缓存，最好的方式应该是JIT编译器和解释器一起使用。

举个例子：从A点到B点，如果只有解释器，没有即是编译器，那么就想到与步行，从一开始就直接从A点出发，向B点前进。如果只有即是编译器，没有解释器，那么就好比等公交车，需要花费一点时间去进行等待，然后再上车，过程可能会超过之前步行的人，并且在行进过程中，可能涉及到换乘，需要频繁等待。如果只有解释器，也有即是编译器，那么我们可以在路近的节点选择步行，远的选择公交车，更加灵活，高效。

### 8.2 Exact VM

#### 1. 说明

为了解决上一个虚拟机问题，jdk1.2时，sun公司提供了此虚拟机。

Exact VM（Exact Memory Management）: 准确式内存管理

- 也可以叫Non-Conservative/Accurate Memory Management
- 虚拟机可以知道内存中某个位置的数据具体是什么类型（一般只知道在内存中的地址，而不知道是什么类型），具备现代高性能虚拟机的雏形
-  热点探测（即JIT编译器对热点代码进行编译、缓存）
- 编译器与解释器混合工作模式只在solaris平台短暂使用，其他平台上还是classicm
- 英雄气短，终被Hotspot虚拟机替换

### 8.3 HotSpot VM

#### 1. HotSpot历史

- 最初由一家名为“Longview Technologies"的小公司设计
- 1997年，此公司被sun收购
- 2009年，sun公司被甲骨文收购
- JDK1.3时，Hotspot vM成为默认虚拟机

#### 2. 说明

- 目前Hotspot占有绝对的市场地位，称霸武林
  - 不管是现在仍在广泛使用的JDK6，还是使用比例较多的JDK8中，默认的虚拟机都是HotSpot
  - Sun/oracle JDK和OpenJDK的默认虚拟机
  - 因此后续内容默认介绍的虚拟机都是Hotspot，相关机制也主要是指HotSpot的GC机制。(比如其他两个商用虚拟机都没有方法区的概念）

- 从服务器、桌面到移动端、嵌入式都有应用
- 名称中的Hotspot指的就是它的热点代码探测技术
  - 通过计数器找到最具编译价值代码，触发即时编译或栈上替换
  - 通过编译器与解释器协同工作，在最优化的程序响应时间（解释器）与最佳执行性能（编译器）中取得平衡

### 8.4 BEA的JRockit

- 专注于服务器端应用
  - 它可以不太关注程序启动速度，因此JRockit内部不包含解析器实现，全部代码都靠即时编译器编译后执行。
  - 大量的行业基准测试显示，JRockit Jvm是世界上最快的IVM。使用JRockit产品，客户已经体验到了显著的性能提高(一些超过了70号)和硬件成本的减少(达50号)。
- 优势:全面的Java运行时解决方案组合
  - JRockit面向延迟敏感型应用的解决方案JRockitRealime提供以毫秒或微秒级的JV响应时间，适合财务、军事指挥、电信网络的需要
  - MissionContro1服务套件，它是一组以极低的开销来监控、管理和分析生产环境中的应用程序的工具。

- 2008年，BEA被oracle收购。

- Oracle表达了整合两大优秀虚拟机的工作，大致在JDK8中完成。整合的方式是在Hotspot的基础上，移植JRockit的优秀特性。
- 高斯林:目前就职于谷歌，研究人工智能和水下机器人



### 8.5 IBM的I9

- 全称:IBM Technology for Java Virtual Machine，简称IT4J，内部代号:J9
- 市场定位与HotSpot接近，服务器端、桌面应用、嵌入式等多用途VM
- 广泛用于IBM的各种Java产品。
- 目前，有影响力的三大商用服务器之一，也号称是世界上最快的Java虚拟机。
- 2017年左右，IBM发布了开源J9 VM，命名为openJ9，交给Eclipse基金会管理，也称为 Ecilpse openJ9



#### 8.6 KVM和CDC/CLDC Hotspot

- Oracle在Java ME(Java平台的一个分支，专门设计用于嵌入式设备、移动设备和其他资源受限的环境中)产品线上的两款虚拟机为:CDC/CLDC HotSpot Implementation VM

- KVM(Kilobyte)是CLDC-HI早期产品
- 目前移动领域地位尴尬，智能手机被Android和ios二分天下
- KVM简单、轻量、高度可移植，而向更低端的设备上还维持自己的一片市场
  - 智能控制器、传感器
  - 老人手机、经济欠发达地区的功能手机
- 所有的虚拟机的原则:一次编译，到处运行。



#### 8.7 Azul VM

- 前面三大“高性能Java虚拟机”使用在通用硬件平台上
- 这里Azu1 VM和BEA Liquid V是与特定硬件平台绑定、软硬件配合的专有虚拟机
  - 高性能Java虚拟机中的战斗机。
- Azu1 VM是Azul Systems公司在HotSpot基础上进行大量改进，运行于Azul systems公司的专有硬件vega系统上的Java虚拟机。
- 每个Azu1 VM实例都可以管理至少数十个CPU和数百GB内存的硬件资源，并提供在巨大内存范围内实现可控的GC时间的垃圾收集器、专有硬件优化的线程调度等优秀特性。
- 2010年，Azul systems公司开始从硬件转向软件，发布了自己的zingJVM，可以在通用x86平台上提供接近于vega系统的特性。



#### 8.8 Liquid vM

- 高性能Java虚拟机中的战斗机。
- BEA公司开发的，直接运行在自家Hypervisor系统上
- Liquid v即是现在的JRockit vE(virtual Editin),Liquid
  eVM不需要操作系统的支持，或者说它自己本身实现了一个专用操作系统的必要功能，如线程调度、文件系统、网络支持等。
- 随着JRockit虚拟机终止开发，Liquid 项目也停止了。



#### 8.9 Apache Harmony

- Apache也曾经推出过与JDK1.5和JDK1.6兼容的Java运行平台Apache Harmony.
- 它是IBM和Inte1联合开发的开源JM，受到同样开源的openJDK的压制，Sun坚决不让Harmony获得JCP认证，最终于2011年退役，IBM转而参与OpenJDK
- 虽然目前并没有apache Harmony被大规模商用的案例，但是它的Java类库代码吸纳进了Android sDK。



#### 8.10 Microsoft IM

- 微软为了在IE3浏览器中支持Java pplets，开发了Microsoft JVM。
- 只能在window平台下运行。但确是当时windows下性能最好的Java V。
- 1997年，sun以侵犯商标、不正当竞争罪名指控微软成功，赔了sun很多钱。微软在windowsXP SP3中抹掉了其VM。现在windows上安装的jdk都是Hotspot。



#### 8.11 taobaoJVM

- 由AliJVM 团队发布。阿里，国内使用Java最强大的公司，覆盖云计算、金融、物流电商等众多领域， 需要解决高并发、高可用、分布式的复合问题。有大量的开源产品。基于openJDK 开发了自己的定制版本AlibabaJDK，简称AJDK。是整个阿里Java体系的基石。
- 基于openJDK Hotspot V 发布的国内第一个优化、深度定制且开源的高性能服务器版Java虚拟机。
  - 创新的GCIH(GC invisible heap )技术实现了off-heap ，即将生命周期较长的ava对象从heap中移到heap之外，并且GC不能管理GCIH内部的Java 对象，以此达到降低Gc 的回收频率和提升Gc 的回收效率的目的。
  - GCIH 中的对象还能够在多个Java 虚拟机进程中实现共享
  - 使用crc32指令实现 JV intrinsic 降低JNI 的调用开销
  - PMU hardware 的Java profiling tool 和诊断协助功能
  - 针对大数据场景的zenGc
- taobao vm应用在阿里产品上性能高，硬件严重依赖intel的cpu，损失了兼容性，但提高了性能
  - 目前已经在淘宝、天猫上线，把oracle官方IVM版本全部替换了。



#### 8.12 Dalvik VM

- 谷歌开发的，应用于Android系统，并在Android2.2中提供了JIT，发展迅猛。，它没有遵循 Java
- Dalvik VM 只能称作虚拟机，而不能称作“Java 拟机”虚拟机规范
- 不能直接执行 Java的class文件
- 基于寄存器架构，不是jvm的栈架构。
- 执行的是编译以后的dex(DalvikExecutable)文件。执行效率比较高。
  - 它执行的dex(Dalvik Executable)文件可以通过class文件转化而来使用Java语法编写应用程序，可以直接使用大部分的ava pI。
- Android 5.0 使用文持提前编译(Ahead of Time Compilation，aoT)的ART VM替换Dalvik VM。



#### 8.13 Graal VM

- 2018年4月，0racle abs公开了GraalVM，号称"Run Programs raster Anywhere"，勃勃野心。与1995年java的”write once,run anywhere"遥相呼应。
- Graal VM在HotSpot VM基础上增强而成的跨语言全栈虚拟机，可以作为“任何语言的运行平台使用。语言包括:Java、scala、Groovy、Kotlin;C、C++、JavaScript、Ruby、Python、R等
- 支持不同语言中混用对方的接口和对象，支持这些语言使用已经编写好的本地库文件
- 工作原理是将这些语言的源代码或源代码编译后的中间格式，通过解释器转换为能被Graal VM接受的中间表示。Graal VM 提供Truffle工具集快速构建面问一种新谐言的解释器。在运行时还能进行即时编译优化，获得比原生编译器更优秀的执行效率
- 如果说Hotspot有一天真的被取代，Graal V希望最大。但是Java的软件生态没有丝毫变化。