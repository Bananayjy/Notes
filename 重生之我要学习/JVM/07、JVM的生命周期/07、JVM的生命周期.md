## 07、JVM的生命周期

### 7.1 JVM生命周期概述

#### 1. 虚拟机的启动

Java虚拟机的启动时通过引导类加载器（bootstrap class loader）创建一个初始类（initial class）来完成的，这个类是由虚拟机的具体实现指定的。

#### 2. 虚拟机的执行

- 一个运行中的Java虚拟机有着一个清洗的任务：执行Java程序
- 程序开始执行时他才运行，程序结束时他就停止
- 执行一个所谓的Java程序的时候，真真正正在执行的是一个叫做Java虚拟机的进程。（我们可以通过Java JDK中的一个命令行工具`jps`是Java Virtual Machine Process Status Tool的缩写，用于列出当前系统中正在运行的Java进程，获取正在执行的Java进程的名称和它们的进程ID（PID））

#### 3. 虚拟机的退出

如下几种情况，会造成虚拟机的退出：

- 程序正常执行结束
- 程序在执行过程中遇到了异常或错误而异常终止
- 由于操作系统出现错误而导致Java虚拟机进程终止
- 某线程调用Runtime类（即JVM运行时数据区对应的单例类,Runtime类允许应用程序与其运行的环境进行交互，例如执行系统命令、查询系统信息等）或System类的exit方法，或Runtime类的halt方法，并且Java安全管理器也允许这次exit或halt操作。
- 除此之外,JNI（Java Native Interface, 本地方法接口）规范描述了用JNI Invocation API来加载或卸载Java虚拟机时，Java虚拟机退出的情况。