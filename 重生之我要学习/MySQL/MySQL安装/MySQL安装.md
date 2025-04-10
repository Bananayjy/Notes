## MySQL安装

### 一、Docker版本配置

通过docker命令，拉取中央仓库指定版本的mysql镜像

```
docker pull mysql:8.0.19
```

然后配置堆栈……然后启动容器

配置参考：

- mysql堆栈配置参考：

```
# docker compose的版本（支持那些语法等）
version: '3.8'
services:
  mysql:
    restart: always  # 配置失败自动重启
    container_name: mysql  # 容器名称
    image: mysql:8.0.19  # 使用的镜像
    volumes:  # 文件挂载配置
      - /app/mysql/logs:/var/log/mysql # 日志挂载
      - /app/mysql/data:/var/lib/mysql # 数据挂载
      - /app/mysql/conf:/etc/mysql/conf.d # 多模块配置文件挂载
      - /app/mysql/my.cnf:/etc/mysql/my.cnf # 主配置文件挂载
      - /etc/localtime:/etc/localtime:ro # 时区挂载
    ports:
      - "3306:3306"  # 容器与宿主机的端口映射 宿主机端口：容器端口
    environment:   # 配置容器环境变量
      - MYSQL_ROOT_PASSWORD=123456
    privileged: true  # 启用容器特权模式 容器内的root拥有真正root权限，否则容器内root只是外部普通用户权限
   
```

- 主配置文件

```
# [client] 影响所有 MySQL 客户端工具（包括 mysql、mysqldump 等）
[client]
# 默认字符集
default-character-set=utf8
# Unix域套接字文件位置，用于本地连接
socket=/var/run/mysqld/mysqld.sock

# [mysql] 专门针对 mysql 命令行客户端工具（只影响 mysql 命令，不影响其他客户端工具）
[mysql]
# 默认字符集
default-character-set=utf8mb4

# MySQL 服务器守护进程的配置
[mysqld]
# 不缓存主机名信息
skip-host-cache
# 禁用 DNS 反查，只使用 IP 地址（提高性能，避免 DNS 解析问题）
skip-name-resolve
# MySQL 数据存储目录
datadir=/var/lib/mysql
# 服务器使用的 Unix 域套接字文件
socket=/var/run/mysqld/mysqld.sock
# 限制 LOAD DATA 和 SELECT ... INTO OUTFILE 操作的文件目录
secure-file-priv=/var/lib/mysql
# 指定了MySQL 服务器进程(mysqld)运行时使用的系统用户账户
user=mysql
# 存储 MySQL 服务器进程 ID 的文件
pid-file=/var/run/mysqld/mysqld.pid

# 包含 /etc/mysql/conf.d/ 目录下的所有配置文件
# Custom config should go here
!includedir /etc/mysql/conf.d/
```





### 二、原始版本

暂时省略……



### 其他问题

#### 1、关于conf.d目录和my.cnf文件

在 MySQL 配置中，`conf.d` 目录和 `my.cnf` 文件都是用于配置 MySQL 服务器行为的，但它们的使用方式和优先级有所不同。

- my.cnf (或 my.ini)

是 MySQL 的主配置文件，通常位于 `/etc/my.cnf`、`/etc/mysql/my.cnf` 或 `/usr/local/mysql/etc/my.cnf` 等位置。包含全局的 MySQL 服务器配置，可以包含所有配置部分 (`[mysqld]`, `[client]`, `[mysql]` 等)。

具体配置示例：

```
[mysqld]
datadir=/var/lib/mysql
socket=/var/lib/mysql/mysql.sock
user=mysql
symbolic-links=0

[mysqld_safe]
log-error=/var/log/mysqld.log
pid-file=/var/run/mysqld/mysqld.pid
```

- conf.d目录

是一个包含额外配置文件的目录 (通常为 `/etc/mysql/conf.d/` 或 `/etc/my.cnf.d/`)允许模块化配置，可以将不同功能的配置放在不同文件中，目录中的 `.cnf` 文件会被 MySQL 按字母顺序读取，常用于 Docker 容器或需要灵活配置的场景。

**两者主要区别：**

| 特性        | my.cnf               | conf.d 目录                  |
| :---------- | :------------------- | :--------------------------- |
| 形式        | 单个文件             | 目录包含多个 .cnf 文件       |
| 灵活性      | 修改需要编辑整个文件 | 可以单独添加/删除配置文件    |
| 典型位置    | /etc/my.cnf          | /etc/mysql/conf.d/           |
| Docker 使用 | 通常挂载整个文件     | 通常挂载单个配置文件         |
| 加载顺序    | 主文件最先加载       | 目录中的文件按字母顺序后加载 |

**配置加载顺序：**

MySQL 按以下顺序加载配置：

1. `/etc/my.cnf`
2. `/etc/mysql/my.cnf`
3. `~/.my.cnf`
4. `/etc/mysql/conf.d/` 目录中的 `.cnf` 文件
5. 命令行参数

#### 2、ssh 禁止对应的ip访问，需要给root设置host=%

即 解决"Host '192.168.209.1' is not allowed to connect to this MySQL server"

- https://blog.csdn.net/weixin_44928329/article/details/132711038
- https://blog.csdn.net/weixin_37632381/article/details/84933091

#### 3、修改堆栈后需要清理相关数据

如堆栈修改密码不生效，需要删除原始的data文件，否则挂载的时候会有问题