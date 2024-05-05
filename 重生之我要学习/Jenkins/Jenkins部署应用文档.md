# Jenkins部署应用文档

## 一、参考文章

- [Jenkins自动化部署实现原理](https://www.cnblogs.com/wangchengyi/p/15874746.html)
- [docker启动jenkins环境](https://blog.csdn.net/jialiu111111/article/details/123844802)
- [超详细本地搭建 docker + jenkins 自动部署项目](https://juejin.cn/post/6967243012199940110)
- [Docker 搭建 Jenkins 实现自动部署](https://learnku.com/articles/39597)

- [maven 构建报错](https://blog.csdn.net/LONG_Yi_1994/article/details/131206755)
- [Java类文件格式的主要版本号](https://stackoverflow.com/questions/9170832/list-of-java-class-file-format-major-version-numbers)
- [jdk11版本jenkins打包jdk8项目，同时兼容jdk11与jdk8](https://blog.csdn.net/leilei1366615/article/details/127347906)

## 二、说明

### 2.1、部署方式

采用`docker + jenkins`的方式实多租户项目的自动化构建、部署。

### 2.2、为什么

 单租户和多租户的api冲突问题，多租户单独部署一套，避免频繁地切换单/多租户的api。

### 2.3、服务器

数据库服务器（mysql、redis、jenkins）：192.168.10.176   

应用服务器（应用服务、portianer、nginx）：192.168.10.118  

### 2.4、堆栈配置说明

#### 2.4.1、MySql

**docker-compose.yml**

```yml
version: '3.8' # docker compose的版本（支持那些语法等）
services:
  mysql:
    restart: always  # 配置失败自动重启
    container_name: mysql  # 容器名称
    image: mysql:8.0.34  # 使用的镜像
    volumes:  # 文件挂载配置
      - /etc/localtime:/etc/localtime
      - /home/mysql/conf/my.cnf:/etc/my.cnf
      - /home/mysql/data:/var/lib/mysql
    ports:
      - "3306:3306"  # 容器与宿主机的端口映射 宿主机端口：容器端口
    environment:   # 配置容器环境变量
      - MYSQL_ROOT_PASSWORD=Cc123@leo
    privileged: true  # 启用容器特权模式
    network_mode: bridge
```

**docker run启动命令**

MySql服务一般只需要启动一次，直接使用docker run附带启动参数的方式启动

```shell
docker run -d --restart always --name mysql -v /etc/localtime:/etc/localtime -v /home/mysql/conf/my.cnf:/etc/my.cnf -v /home/mysql/data:/var/lib/mysql -p 3306:3306 -e MYSQL_ROOT_PASSWORD=Cc123@leo --privileged --network bridge mysql:8.0.34
```

参数说明：

- `--restart always`：使容器在退出时总是重新启动。
- `--name mysql`：设置容器的名称为 `mysql`。
- `-v`：用于挂载卷，这里挂载了本地目录到容器内的目录。
- `-p 3306:3306`：将宿主机的 `3306` 端口映射到容器的 `3306` 端口。
- `-e MYSQL_ROOT_PASSWORD=Cc123@leo`：设置环境变量 `MYSQL_ROOT_PASSWORD` 为 `Cc123@leo`，用于 MySQL 容器的 root 密码。
- `--privileged`：以特权模式运行容器。
- `--network bridge`：将容器连接到默认的 `bridge` 网络。
- `mysql:8.0.34`：指定要运行的镜像及其版本。



#### 2.4.2、Redis

**docker-compose.yml**

```yml
version: '3.8'
services:
  redis:
    restart: always  # 配置失败自动重启
    container_name: redis  # 容器名称
    image: redis:latest  # 使用的镜像
    ulimits:        # 栈内存的上限
      memlock:
        soft: -1    # 不限制
        hard: -1    # 不限制
    logging:        #日志大小限制
      options:
        max-size: 100m
        max-file: 3
    volumes:  # 文件挂载配置
      - /home/redis/data:/data
      - /home/redis/conf:/etc/redis/conf
      - /home/redis/logs:/etc/redis/logs
    ports:
      - "6379:6379"  # 容器与宿主机的端口映射 宿主机端口：容器端口
    environment:   # 配置容器环境变量
      - "TZ=Asia/Shanghai"
    privileged: true  # 启用容器特权模式
    #network_mode: host  # 配置容器的网络模式
    command: #指定容器启动运行的命令
      redis-server /etc/redis/conf --requirepass Cc123456 --port 6379 #指定容器启动运行的命令,密码设置，端口设置
    network_mode: bridge
```

**docker run启动命令**

redis服务一般只需要启动一次，直接使用docker run附带启动参数的方式启动

```shell
docker run -d --restart always --name redis --ulimit memlock=-1:-1 --log-opt max-size=100m --log-opt max-file=3 -v /home/redis/data:/data -v /home/redis/conf:/etc/redis/conf -v /home/redis/logs:/etc/redis/logs -p 6379:6379 -e TZ=Asia/Shanghai --privileged --network bridge redis:latest redis-server /etc/redis/conf/redis.conf --requirepass Cc123456 --port 6379
```

参数说明：

- `--name` 对应 `container_name`。
- `--ulimit` 设置了内存锁定的限制。
- `--log-opt` 设置了日志的配置，这在 `docker run` 中是通过 `--log-opt` 参数指定的。
- `-v` 用于挂载卷，对应 `volumes`。
- `-p` 用于端口映射，对应 `ports`。
- `-e` 用于设置环境变量，对应 `environment`。
- `--privileged` 启用特权模式。
- `--network bridge` 设置了网络模式，这与 `docker-compose.yml` 中的 `network_mode` 相对应。
- `redis:latest` 是镜像名称和标签，对应 `image`。
- `redis-server /etc/redis/conf/redis.conf --requirepass Cc123456 --port 6379` 是容器启动时要运行的命令，注意这里我假设您的 `redis.conf` 文件位于 `/home/redis/conf` 目录内，因此您需要将其挂载到容器的 `/etc/redis/conf` 目录，并在 `command` 中引用它。



#### 2.4.3、Nginx

**docker-compose.yml**

```yml
version: '3.8'
services:
  nginx:    # 服务名称
    restart: always  # 自动重启策略
    container_name: nginx  # 容器名称
    image: nginx:latest  # 使用镜像
    ports:
      - "9001:8888"  # 容器与宿主机的端口映射 宿主机端口：容器端口
    environment:  # 配置容器环境变量
      - "TZ=Asia/Shanghai"  
    ulimits:        # 栈内存的上限
      memlock:
        soft: -1    # 不限制
        hard: -1    # 不限制
    volumes:
      - "/home/nginx/logs:/var/log/nginx" # nginx日志的挂载
      - "/home/nginx/cert:/etc/nginx/cert"
      - "/front:/front"
      - "/home/nginx/conf/conf.d:/etc/nginx/conf.d"
      - "/home/nginx/conf/nginx.conf:/etc/nginx/nginx.conf"
    network_mode: bridge
```



#### 2.4.4、应用服务堆栈配置

略……

## 三、部署

### 3.1、前言

**说明：**本部署过程仅作为参考，具体的版本选择可根据实际情况自行选择。整个部署文章侧重于jenkins，关于docker部署具体过程可参考其他文章学习。

**插件：**除了安装jenkins推荐的插件外，还需要安装如下插件：

- Publish Over SSH：https://plugins.jenkins.io/publish-over-ssh/

- Maven Integration

- Nodejs

- Fail The Build

- GitLab（公司代码的版本控制GitLab）

- JDK Parameter Plugin

### 3.2、Jenkins初始化

首先登录`hub.docker.com`去docker中央仓库查找对应的jenkins镜像的版本或通过命令`docker search jenkins`查看各个jenkins的镜像版本

从中央残酷拉取jenkins镜像（这里选择的版本为latest-jdk17）

```shell
docker pull jenkins/jenkins:latest-jdk17
```

创建jenkinis在宿主机的挂载目录，并赋予读写权限

```shell
mkdir /home/jenkins
chmod 777 /home/jenkins
```

通过docker run携带启动参数的方式启动jenkins

```shell
docker run -d -p 8899:8080 -p 50000:50000 --restart always -v /home/jenkins:/var/jenkins_home -v /etc/localtime:/etc/localtime --name jenkins jenkins/jenkins:latest-jdk17
```

上述端口映射说明

- HTTP 端口（默认为 8080）： Jenkins Web UI 使用此端口提供用户界面，用户可以通过浏览器访问 Jenkins 控制台、执行构建任务等。
-  JNLP 端口（默认为 50000）： Jenkins Slaves 使用此端口与 Jenkins Master 进行通信。Jenkins Slaves 是 Jenkins Master 用于执行构建任务的节点。通过此端口，Jenkins Master 可以与 Slaves 进行通信并委托构建任务。

（可选）配置镜像加速，进入 cd /home/jenkins 目录，修改`hudson.model.UpdateCenter.xml`里的内容

```xml
# 修改前
<?xml version='1.1' encoding='UTF-8'?>
<sites>
  <site>
    <id>default</id>
    <url>https://updates.jenkins.io/update-center.json</url>
  </site>
</sites>


# 修改后
<?xml version='1.1' encoding='UTF-8'?>
<sites>
  <site>
    <id>default</id>
    <url>https://mirrors.tuna.tsinghua.edu.cn/jenkins/updates/update-center.json</url>
  </site>
</sites>
```

访问jenkins 的 UI界面 `http://192.168.10.176:8899/login`，根据提示去对应目录下获取初始化密码，并输入

![image-20240430100357055](Jenkins%E9%83%A8%E7%BD%B2%E5%BA%94%E7%94%A8%E6%96%87%E6%A1%A3.assets/image-20240430100357055.png)

这里直接安装推荐的插件即可

![image-20240430100600992](Jenkins%E9%83%A8%E7%BD%B2%E5%BA%94%E7%94%A8%E6%96%87%E6%A1%A3.assets/image-20240430100600992.png)

可能会出现部分插件甚至全部插件安装失败的情况，根据具体情况选择手动安装等方式进行解决，不再这里提供具体解决方案

![image-20240430100610496](Jenkins%E9%83%A8%E7%BD%B2%E5%BA%94%E7%94%A8%E6%96%87%E6%A1%A3.assets/image-20240430100610496.png)

创建第一个管理员账号（Banana/123456）

![image-20240430100658759](Jenkins%E9%83%A8%E7%BD%B2%E5%BA%94%E7%94%A8%E6%96%87%E6%A1%A3.assets/image-20240430100658759.png)

实例配置使用默认值即可

![image-20240430100716708](Jenkins%E9%83%A8%E7%BD%B2%E5%BA%94%E7%94%A8%E6%96%87%E6%A1%A3.assets/image-20240430100716708.png)

到此，完成jenkins的初始化操作。

### 3.3、配置SSH Server

**作用**

需要Publish Over SSH插件的支持。SSH Server的作用通常是允许远程机器通过SSH协议连接到Jenkins服务器，并执行一些远程操作，比如构建项目、触发任务等。这对于需要在分布式环境中管理构建或需要在远程机器上执行构建的情况非常有用。SSH Server插件可以使Jenkins成为一个支持SSH访问的服务器，使得用户可以通过SSH连接到Jenkins并执行一些操作，而无需通过Web界面或API。并且支持将构建的项目推送到应用服务器上，进行启动、部署（主要是使用了这一特点，能够实现将代码库中的代码下拉后，通过ssh将构建后的jar包推送到应用服务器上进行部署）。



**具体过程**

在系统管理-系统配置喜爱的Publish over SSH中，配置SSH，新增一个SSH Server，输入对应的应用服务器信息，在高级部分选择`Use password authentication, or use a different key`，并输入服务器的密码，实现配置

![image-20240430101805735](Jenkins%E9%83%A8%E7%BD%B2%E5%BA%94%E7%94%A8%E6%96%87%E6%A1%A3.assets/image-20240430101805735.png)

点击Test Configuration检查是否配置成功，如果左侧出现Success则表示配置成功

![image-20240430101845730](Jenkins%E9%83%A8%E7%BD%B2%E5%BA%94%E7%94%A8%E6%96%87%E6%A1%A3.assets/image-20240430101845730.png)

### 3.4、配置Maven和JDK

本次部署使用的Maven和JDK版本如下

- Maven:apache-maven-3.8.8
- JDK: jdk8、jdk11（jdk11用于兼容jenkins的构建，jdk8用于兼容项目的构建）

官方下载地址

- [Maven](https://maven.apache.org/download.cgi)

- [JDK](https://www.oracle.com/java/technologies/downloads/#java11)

本次部署，将下载的JDK和Maven放在宿主机的目录如下所示

- Maven: /usr/local/apache-maven-3.8.8

- JDK1.8: /usr/local/java/jdk1.8.0_151

- JDK11: /usr/local/java/jdk-11.0.23

修改jenkins的挂载，将Maven和JDK的目录挂载到Jenkins的容器中，重新启动容器

```
docker stop jenkins

docker rm jenkins 

docker run -d -p 8899:8080 -p 50000:50000 --restart always -v /home/jenkins:/var/jenkins_home -v /etc/localtime:/etc/localtime -v /usr/local/java:/usr/local/java -v /usr/local/apache-maven-3.8.8:/usr/local/apache-maven-3.8.8 -v /usr/local/java/jdk-11.0.23:/usr/local/java/jdk-11.0.23 -v /usr/local/java/jdk1.8.0_151:/usr/local/java/jdk1.8.0_151 --name jenkins jenkins/jenkins:latest-jdk17
```

注意，还需要给对应的文件赋予读写执行权限，不然会出现如下问题

```
java.io.IOException: Cannot run program "/usr/local/java/jdk1.8.0_151/bin/java" (in directory "/var/jenkins_home/workspace/pigx common"): error=13, Permission denied
```

赋予相关目录文件读写执行权限

```
chmod -R 777 /usr/local/apache-maven-3.8.8
chmod -R 777 /usr/local/java
```

在Jenkins的系统管理-全局工具配置中配置maven和JDK

配置Maven

![image-20240430103849593](Jenkins%E9%83%A8%E7%BD%B2%E5%BA%94%E7%94%A8%E6%96%87%E6%A1%A3.assets/image-20240430103849593.png)

![image-20240430103858048](Jenkins%E9%83%A8%E7%BD%B2%E5%BA%94%E7%94%A8%E6%96%87%E6%A1%A3.assets/image-20240430103858048.png)

配置JDK

![image-20240430103918753](Jenkins%E9%83%A8%E7%BD%B2%E5%BA%94%E7%94%A8%E6%96%87%E6%A1%A3.assets/image-20240430103918753.png)

详细说明一下，这里为什么要配置两个JDK。因为当前Jenkins的版本的编译需要jdk11，而项目的编译需要jdk8，因此我们这里配置两个JDK，在jenkins编译的时候会去轮询jdk版本，通过jdk11进行编译，然后我们使用JDK Parameter Plugin插件，指定项目编译的时候使用jdk8即可。否者，如果只按照一个jdk8的版本，会出现jenkins编译失败的问题，如下所示

```
# 报错信息示例
[pigx common] $ /usr/local/java/jdk1.8.0_151/bin/java -cp /var/jenkins_home/plugins/maven-plugin/WEB-INF/lib/maven35-agent-1.14.jar:/usr/local/apache-maven-3.8.8/boot/plexus-classworlds-2.6.0.jar:/usr/local/apache-maven-3.8.8/conf/logging jenkins.maven3.agent.Maven35Main /usr/local/apache-maven-3.8.8 /var/jenkins_home/war/WEB-INF/lib/remoting-3206.vb_15dcf73f6a_9.jar /var/jenkins_home/plugins/maven-plugin/WEB-INF/lib/maven35-interceptor-1.14.jar /var/jenkins_home/plugins/maven-plugin/WEB-INF/lib/maven3-interceptor-commons-1.14.jar 45892
Exception in thread "main" java.lang.UnsupportedClassVersionError: hudson/remoting/Launcher has been compiled by a more recent version of the Java Runtime (class file version 55.0), this version of the Java Runtime only recognizes class file versions up to 52.0
```



### 3.5、具体服务配置演示

这里演示配置基础平台的upms的全过程

在目标视图（可以自己新建）下，新建一个任务

![image-20240430105305646](Jenkins%E9%83%A8%E7%BD%B2%E5%BA%94%E7%94%A8%E6%96%87%E6%A1%A3.assets/image-20240430105305646.png)

![image-20240430105546039](Jenkins%E9%83%A8%E7%BD%B2%E5%BA%94%E7%94%A8%E6%96%87%E6%A1%A3.assets/image-20240430105546039.png)

配置项目的构建

配置对旧的构建的丢弃策略，这里选择保存旧的构建天数为1天，保存构建的最大数量为3，具体可根据实际情况自行选择

![image-20240430105800795](Jenkins%E9%83%A8%E7%BD%B2%E5%BA%94%E7%94%A8%E6%96%87%E6%A1%A3.assets/image-20240430105800795.png)

选择项目构建使用的jdk版本，这里选择jdk8版本，具体可根据实际情况自行选择

![image-20240430105813162](Jenkins%E9%83%A8%E7%BD%B2%E5%BA%94%E7%94%A8%E6%96%87%E6%A1%A3.assets/image-20240430105813162.png)

配置源码的git仓库以及凭证，选择对应的代码库和分支

![image-20240430105832762](Jenkins%E9%83%A8%E7%BD%B2%E5%BA%94%E7%94%A8%E6%96%87%E6%A1%A3.assets/image-20240430105832762.png)

配置凭证（git对应的账号和密码）

![image-20240430110412815](Jenkins%E9%83%A8%E7%BD%B2%E5%BA%94%E7%94%A8%E6%96%87%E6%A1%A3.assets/image-20240430110412815.png)

构建触发器，选中Build whenever a SNAPSHOT dependency is built

这里的 SNAPSHOT dependency 指正在开发中或尚未发布的版本

如果选中，Jenkins将解析这个项目的pom，并查看它的快照依赖是否也构建在这个Jenkins上。如果是这样，Jenkins将设置构建依赖关系，以便每当构建依赖作业和创建新的SNAPSHOT jar时，Jenkins将安排这个项目的构建(即在一个项目依赖的 SNAPSHOT 版本被构建时，自动触发本项目的构建)。
这对于自动执行持续集成很方便。Jenkins将检查POM中`<dependency>`元素中的快照依赖，以及POM中使用的`<plugin>`和`<extension>`。

![image-20240430105900851](Jenkins%E9%83%A8%E7%BD%B2%E5%BA%94%E7%94%A8%E6%96%87%E6%A1%A3.assets/image-20240430105900851.png)

配置Maven构建命令

![image-20240430105914062](Jenkins%E9%83%A8%E7%BD%B2%E5%BA%94%E7%94%A8%E6%96%87%E6%A1%A3.assets/image-20240430105914062.png)命令详解：

```
clean install -pl hs-workflow-biz -am -Pdev -Dmaven.test.skip=true -Dserver.port=5100
```

- `clean`: 清理项目，删除之前生成的构建文件。
- `install`: 安装项目到本地 Maven 仓库，以便其他项目可以引用它。
- `-pl pigx-upms/pigx-upms-biz`: 只处理名为 `pigx-upms`下的`pigx-upms-biz` 的模块，而不是整个项目。
- `-am`: 在 Maven 命令中代表 "also make"，它告诉 Maven 在构建指定模块时同时构建其所有依赖模块。这意味着如果在构建 `pigx-upms-biz` 模块时，它依赖于其他模块，那么 Maven 会确保这些依赖模块也被构建，以确保构建过程中所有必要的组件都是最新的。
- `-Pdev`:  Maven 命令中的一个 profile 参数，它指定了一个名为 `dev` 的 Maven profile。Maven profile 可以用来定义特定于不同环境或需求的构建配置。在这种情况下，`-Pdev` 可能代表着开发环境的配置，其中包含了一些特定于开发阶段的设置，比如开启调试信息、使用开发数据库等。
- `-Dmaven.test.skip=true`: 跳过运行测试。
- `-Dserver.port=5100`: 设置服务器端口为 `5100`。

配置构建完成后，发送到指定应用服务器，进行部署启动

![image-20240430105931824](Jenkins%E9%83%A8%E7%BD%B2%E5%BA%94%E7%94%A8%E6%96%87%E6%A1%A3.assets/image-20240430105931824.png)

对这里的配置进行一个详细的解读

该步骤`Send files or execute commands over SSH`该步骤是在构建后执行，用于向指定服务器发送文件和执行命令。

这里是首先通过之前配置的SSH Server的Name来指定需要连接哪一个应用服务器

然后通过Source files指定需要发送的文件（根目录为jenkins构建后文件所在工作目录的位置）

通过Remove prefix指定构建后文件所在工作目录下的位置的前缀，并在发送后去除该前缀

通过Remote directory指定发送到指定服务器的位置

通过Exec command指定发送文件后需要执行的命令

总的来说，这里的配置，就是将构建后的jar包发送到应用服务器，然后再应用服务器中通过docker启动对应的应用。