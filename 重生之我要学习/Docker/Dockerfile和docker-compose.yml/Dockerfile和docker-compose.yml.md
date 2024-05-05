# Dockerfile和docker-compose.yml

## 一、前言

### 1.1、说明



### 1.2、参考文章

- Docker文档：https://yeasy.gitbook.io/docker_practice

- CSDN相关文章：https://blog.csdn.net/m0_46090675/article/details/121846718

## 二、概念介绍

### 2.1、Dockerfile

#### 2.1.1、什么是dockerfile

> **Docker** 最初是 `dotCloud` 公司创始人 [Solomon Hykes](https://github.com/shykes) 在法国期间发起的一个公司内部项目，它是基于 `dotCloud` 公司多年云服务技术的一次革新，并于 [2013 年 3 月以 Apache 2.0 授权协议开源](https://en.wikipedia.org/wiki/Docker_(software))，主要项目代码在 [GitHub](https://github.com/moby/moby) 上进行维护。`Docker` 项目后来还加入了 Linux 基金会，并成立推动 [开放容器联盟（OCI）](https://opencontainers.org/)。
>
> **Docker** 自开源后受到广泛的关注和讨论，至今其 [GitHub 项目](https://github.com/moby/moby) 已经超过 5 万 7 千个星标和一万多个 `fork`。甚至由于 `Docker` 项目的火爆，在 `2013` 年底，[dotCloud 公司决定改名为 Docker](https://www.docker.com/blog/dotcloud-is-becoming-docker-inc/)。`Docker` 最初是在 `Ubuntu 12.04` 上开发实现的；`Red Hat` 则从 `RHEL 6.5` 开始对 `Docker` 进行支持；`Google` 也在其 `PaaS` 产品中广泛应用 `Docker`。
>
> **Docker** 使用 `Google` 公司推出的 [Go 语言](https://golang.google.cn/) 进行开发实现，基于 `Linux` 内核的 [cgroup](https://zh.wikipedia.org/wiki/Cgroups)，[namespace](https://en.wikipedia.org/wiki/Linux_namespaces)，以及 [OverlayFS](https://docs.docker.com/storage/storagedriver/overlayfs-driver/) 类的 [Union FS](https://en.wikipedia.org/wiki/Union_mount) 等技术，对进程进行封装隔离，属于 [操作系统层面的虚拟化技术](https://en.wikipedia.org/wiki/Operating-system-level_virtualization)。由于隔离的进程独立于宿主和其它的隔离的进程，因此也称其为容器。最初实现是基于 [LXC](https://linuxcontainers.org/lxc/introduction/)，从 `0.7` 版本以后开始去除 `LXC`，转而使用自行开发的 [libcontainer](https://github.com/docker/libcontainer)，从 `1.11` 版本开始，则进一步演进为使用 [runC](https://github.com/opencontainers/runc) 和 [containerd](https://github.com/containerd/containerd)。
>
> ![img](https://yeasy.gitbook.io/~gitbook/image?url=https%3A%2F%2Fdocs.microsoft.com%2Fen-us%2Fvirtualization%2Fwindowscontainers%2Fdeploy-containers%2Fmedia%2Fdocker-on-linux.png&width=768&dpr=4&quality=100&sign=063b3784ee73435dbd9d52bfe4fa7212d0b7c3b2576d9a552ec8f67b0db0927a)
>
> [^Docker 架构]: Docker的架构图，`runc` 是一个 Linux 命令行工具，用于根据 [OCI容器运行时规范](https://github.com/opencontainers/runtime-spec) 创建和运行容器。`containerd` 是一个守护程序，它管理容器生命周期，提供了在一个节点上执行容器和管理镜像的最小功能集。
>
> 





Dockerfile是一个`纯文本文件`，`用于指示docker image build命令自动构建Image的源代码`, 其中包含了一条条的指令，用于构建镜像。每一条指令构建一层镜像，描述该层镜像应当如何构建。

对`docker image build`命令的补充说明：

`docker image build` 和 `docker build` 实际上是同一个命令的两种写法，可以互换使用。在 Docker 17.05 之前，使用的是 `docker build` 命令来构建镜像，而在 17.05 及以后的版本中，官方推荐使用 `docker image build` 命令，以下两个命令是等效的：

```shell
docker build [选项] <上下文路径/URL/->
docker image build [选项] <上下文路径/URL/->
```

`PATH | URL | -` 参数指定要使用的 Dockerfile 的位置。其中 `PATH` 是本地文件系统中 Dockerfile 的路径；`URL` 是远程 Git 存储库中 Dockerfile 的 URL；`-` 表示从标准输入流中读取 Dockerfile 的内容。

#### 2.1.2、为什么使用dockerfile

在官方的dockerhub中已经提供了很多镜像，但是有些时候我们需要自定义镜像，将自己应用打包成镜像，这样可以让我们自己的应用进行容器运行，并且可以对官方已有的镜像进行扩展，从而实现符合我们生产要求的镜像。

#### 2.1.3、镜像的格式

镜像主要由三个部分组成：

- 文件系统层（Filesystem Layers）：镜像是由多个只读的文件系统层堆叠而成。每个文件系统层都包含了镜像中的一部分文件和目录，通过联合文件系统（Union File System）的方式进行堆叠。这种分层结构使得镜像的复用、更新和分发更加高效。即③中所提到的。
- 元数据（Metadata）：镜像包含了关于镜像本身的元数据信息，如名称、标签、版本、作者、创建时间等。这些元数据可以用来描述和标识镜像的特征和属性。
- 配置项（Configuration）：镜像的配置项定义了在容器中运行该镜像所需的环境变量、工作目录、启动命令等设置。配置项指定了镜像在容器运行时的行为和环境。



镜像中文件系统层的层次结构图：

![image-20240203220041035](Dockerfile%E5%92%8Cdocker-compose.yml.assets/image-20240203220041035.png)

镜像是由多个层（layer）组成的，每个层都是只读的，并且可以被多个镜像共用，每个层都包含了文件系统的一部分，这些层通过联合文件系统（Union File System）的方式进行堆叠，形成了一个完整的镜像。这种设计使得镜像非常轻量级，因为不同的镜像可以共用相同的基础层，而不需要重复存储相同的文件或者数据，从而节约了磁盘空间。在 Docker 中，一个镜像可能由多个基础镜像组成，每个基础镜像都是一个只读层（镜像），顶部是一个可读写的层（docker容器），用于保存容器运行时所产生的数据和变化。当使用一个Docker镜像启动一个新的容器时，Docker会在镜像的顶部创建一个新的可读写层，并将其挂载到镜像的只读层之上。这个新的可读写层就是容器。

如上图所示，由4个只读层和1个可读写的层组成。从下到上，分别是bootfs镜像、rootfs镜像、jdk镜像、tomcat镜像以及最上层的docker容器：

- bootfs：bootfs是一个非常小的镜像，它包含了Linux内核和一些用于启动的文件，例如启动引导程序（bootloader）和内核参数文件等。这些文件被加载到内存中，用于启动操作系统。bootfs是只读的，因为它的内容不应该被修改，而且一旦启动就不需要再访问了
- rootfs：rootfs则是一个完整的文件系统镜像，包含了操作系统中所有的文件和目录，例如/bin、/etc、/usr等。rootfs通常是从一个操作系统基础镜像构建而来，可以是Ubuntu、Alpine等各种不同的操作系统。rootfs是可读写的，因为容器中的进程需要对文件系统进行增删改查等操作。

- jdk镜像：JDK镜像是一个包含Java Development Kit（JDK）的Docker镜像，它提供了Java编程环境和运行时环境。使用JDK镜像可以快速、可靠地部署和运行Java应用程序，而无需在本地安装JDK。
- Tomcat镜像：Tomcat镜像是一个包含Apache Tomcat服务器的Docker镜像，它提供了一个用于部署和运行Java Web应用程序的容器化环境。使用Tomcat镜像可以方便地搭建和管理Web应用程序的开发、测试和生产环境。

- 可写容器：当我们创建一个容器时，Docker会在镜像的基础上再添加一个最上层的可写层。这个可写层允许在容器内进行写操作，并将更改保存在该层中，而不会影响到原始的镜像。这样可以实现容器的状态隔离和持久化。所有对容器内文件系统的写操作都会在最上层的可写层中进行。当需要读取文件时，Docker会按照层级顺序从最上层的可写层开始查找，然后逐层向下查找，直到找到所需的文件或目录，需要注意的是，可写层是容器生命周期中的一个临时层，当容器被删除时，这个可写层也会一同被删除，其中的修改和数据也会丢失。因此，如果需要持久保存容器内的数据，应该使用数据卷或挂载宿主机目录来实现。





### 2.2、docker-compose.yml

### 2.3、Dockerfile和docker-compose.yml的区别





