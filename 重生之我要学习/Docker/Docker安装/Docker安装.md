## Docker安装

### 一、在线方式安装

在 CentOS 或 RHEL 系统中，可以通过 `yum` 包管理器安装 Docker。以下是详细的步骤：

**1. 准备工作**

确保系统已经更新到最新版本：

```
sudo yum update -y
```

安装 `yum-utils` 工具包，它提供了 `yum-config-manager` 工具，用于管理 Yum 仓库：

```
sudo yum install -y yum-utils
```

**2. 添加 Docker 的 Yum 仓库**

1. 添加 Docker 的官方仓库：

   ```
   sudo yum-config-manager --add-repo https://download.docker.com/linux/centos/docker-ce.repo
   ```

2. 如果官方仓库无法访问，可以使用阿里云镜像：

   bash

   复制

   ```
   sudo yum-config-manager --add-repo http://mirrors.aliyun.com/docker-ce/linux/centos/docker-ce.repo
   ```

**3. 安装 Docker**

1. 安装 Docker CE（社区版）：

   ```
   sudo yum install -y docker-ce docker-ce-cli containerd.io
   ```

2. 如果提示有多个版本可用，可以指定版本安装。例如，安装特定版本：

   ```
   sudo yum install -y docker-ce-20.10.9 docker-ce-cli-20.10.9 containerd.io
   ```

**4. 启动并启用 Docker 服务**

1. 启动 Docker 服务：

   ```
   sudo systemctl start docker
   ```

2. 设置 Docker 开机自启：

   ```
   sudo systemctl enable docker
   ```

3. 检查 Docker 是否正常运行：

   ```
   sudo systemctl status docker
   ```

**5. 验证 Docker 安装**

运行以下命令，验证 Docker 是否安装成功：

```
sudo docker --version
```

如果安装成功，会显示 Docker 版本信息，例如：

```
Docker version 20.10.9, build c2ea9bc
```

**6. 配置 Docker（可选）**

1. **添加用户到 Docker 组**：
   为了避免每次使用 Docker 都需要 `sudo`，可以将当前用户添加到 `docker` 组：

   ```
   sudo usermod -aG docker $USER
   ```

   然后重新登录系统，使配置生效。

2. **配置 Docker 镜像加速器**（国内用户建议配置）：
   编辑 Docker 配置文件：

   ```
   sudo mkdir -p /etc/docker
   sudo tee /etc/docker/daemon.json <<-'EOF'
   {
     "registry-mirrors": [
       "https://<your-mirror-url>"
     ]
   }
   EOF
   ```

   将 `<your-mirror-url>` 替换为镜像加速器地址。例如，使用阿里云镜像加速器：

   ```
   {
     "registry-mirrors": [
       "https://<your-aliyun-mirror>.mirror.aliyuncs.com"
     ]
   }
   ```

   > 阿里云镜像网站：https://cr.console.aliyun.com/cn-hangzhou/instances/mirrors
   >
   > 选择centos
   >
   > ![image-20231220230055885](Docker%E5%AE%89%E8%A3%85.assets/image-20231220230055885.png)
   >
   > ```
   > 阿里云，容器镜像服务
   > 针对 Docker 客户端版本大于 1.10.0 的用户
   > 您可以通过修改 daemon 配置文件/etc/docker/daemon.json 来使用加速器
   > sudo mkdir -p /etc/docker
   > sudo tee /etc/docker/daemon.json <<-'EOF'
   > {
   >   "registry-mirrors": [
   >     "https://82m9ar63.mirror.aliyuncs.com"
   >   ]
   > }
   > EOF
   > sudo systemctl daemon-reload
   > sudo systemctl restart docker
   > ```
   >
   > 配置完aliyun加速，之后dokcer在下载镜像的时候，都是通过aliyun镜像进行的，速度会更快
   >
   > 报错：
   >
   > ```
   > Job for docker.service failed because start of the service was attempted too often
   > ```
   >
   > 进入docker修改其中的配置文件后缀
   >
   > ```
   > cd /etc/docker
   > mv daemon.json daemon.conf
   > ```

   重启 Docker 服务：

   ```
   sudo systemctl daemon-reload
   sudo systemctl restart docker
   ```

**8. 测试 Docker**

运行一个测试容器，验证 Docker 是否正常工作：

```
sudo docker run hello-world
```

如果一切正常，会看到以下输出：

```
Hello from Docker!
This message shows that your installation appears to be working correctly.
```