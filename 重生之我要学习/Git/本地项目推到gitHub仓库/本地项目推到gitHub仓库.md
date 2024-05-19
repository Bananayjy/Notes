# 本地项目推到gitHub仓库

### 前言

参考文章：

```
https://blog.csdn.net/qq_15125937/article/details/125604629
https://blog.csdn.net/jiunian_2761/article/details/122909443
https://blog.csdn.net/mukes/article/details/115693833
```

#### 1、需要提前安装Git，这里不过多说明了

可以参考文章`https://blog.csdn.net/mukes/article/details/115693833`


#### 2、Git本地仓库绑定远端仓库的两种方式

GitHub的HTTP和SSH主要的区别在于认证方式和安全性。

1. 认证方式：使用HTTP协议时，提交到服务器的认证信息是用户名和密码，每次提交代码都需要输入这些信息。而使用SSH协议时，提交到服务器的认证信息是公钥和私钥，不需要手动输入用户名和密码，提高了便捷性。
2. 安全性：HTTP协议安全性较低，因为密码会以明文的方式在网络中传输，存在被截获的风险。而SSH协议具有较高的安全性，因为其使用加密的方式在网络中传输数据，可以有效地防止密码被截获。

总的来说，如果更关心便捷性，可以选择使用HTTP协议；如果更关心安全性，可以选择使用SSH协议。



### 具体步骤（以SSH推送为例）：

#### 1、本地创建一个需要上传到GitHub仓库中的文件夹，并完成初始化

通过`mkdir`命名还是可视化创建都可以，这里通过Git bash命令行窗口中执行`mkdir tmp`来创建一个命名为`tmp`的文件夹

在D盘目录下，右击空白位置，选中`Git Bash Here`打开`Git Bash`命令行窗口

![image-20231119155207960](%E6%9C%AC%E5%9C%B0%E9%A1%B9%E7%9B%AE%E6%8E%A8%E5%88%B0gitHub%E4%BB%93%E5%BA%93.assets/image-20231119155207960.png)

通过`mkdir tmp`创建一个名为`tmp`的目录

![image-20231119155408247](%E6%9C%AC%E5%9C%B0%E9%A1%B9%E7%9B%AE%E6%8E%A8%E5%88%B0gitHub%E4%BB%93%E5%BA%93.assets/image-20231119155408247.png)

结果如下所示

![image-20231119155449654](%E6%9C%AC%E5%9C%B0%E9%A1%B9%E7%9B%AE%E6%8E%A8%E5%88%B0gitHub%E4%BB%93%E5%BA%93.assets/image-20231119155449654.png)

我们进入`tmp`文件，在里面创建一个txt文件，名字为`123.txt`，里面的内容可以随便编辑编辑完之后，记得按`ESC`，然后输入`:wq`退出

![image-20231119155836434](%E6%9C%AC%E5%9C%B0%E9%A1%B9%E7%9B%AE%E6%8E%A8%E5%88%B0gitHub%E4%BB%93%E5%BA%93.assets/image-20231119155836434.png)

结果如下所示

![image-20231119155937060](%E6%9C%AC%E5%9C%B0%E9%A1%B9%E7%9B%AE%E6%8E%A8%E5%88%B0gitHub%E4%BB%93%E5%BA%93.assets/image-20231119155937060.png)

#### 2、将该文件夹中的内容设置为被Git管理

在当前`tmp`目录下，执行`git init`命令，将该文件夹中的内容变成Git可管理的仓库

![image-20231119160231090](%E6%9C%AC%E5%9C%B0%E9%A1%B9%E7%9B%AE%E6%8E%A8%E5%88%B0gitHub%E4%BB%93%E5%BA%93.assets/image-20231119160231090.png)

执行完该命令后，可以看到当前目录下多了一个叫`.git`的隐藏文件，它就是用来跟踪和管理版本库的。

![image-20231119160309460](%E6%9C%AC%E5%9C%B0%E9%A1%B9%E7%9B%AE%E6%8E%A8%E5%88%B0gitHub%E4%BB%93%E5%BA%93.assets/image-20231119160309460.png)

#### 3、之后就可以在这个被Git管理的本地仓库中进行git相关的操作命令了

我们通过`git status`查看当前git仓库的状态，可以看到有一个叫`123.txt`的文件还是一个未追踪的文件，即还没有提交到我们的git仓库中

![image-20231119160613079](%E6%9C%AC%E5%9C%B0%E9%A1%B9%E7%9B%AE%E6%8E%A8%E5%88%B0gitHub%E4%BB%93%E5%BA%93.assets/image-20231119160613079.png)

我们先通过`git add`命令将该文件先提交到暂存区

![image-20231119160915731](%E6%9C%AC%E5%9C%B0%E9%A1%B9%E7%9B%AE%E6%8E%A8%E5%88%B0gitHub%E4%BB%93%E5%BA%93.assets/image-20231119160915731.png)

然后再通过`git commit`命令将该文件提交到`git`仓库中

![image-20231119161008203](%E6%9C%AC%E5%9C%B0%E9%A1%B9%E7%9B%AE%E6%8E%A8%E5%88%B0gitHub%E4%BB%93%E5%BA%93.assets/image-20231119161008203.png)

#### 4、创建SSH KEY

看一下你C盘用户目录下有没有.ssh目录，有的话看下里面有没有id_rsa和id_rsa.pub这两个文件，有就跳到下一步，没有就通过下面命令创建

```
$ ssh-keygen -t rsa -C <a href="mailto:youremail@example.com" rel="external nofollow">youremail@example.com</a>
```

输入这个命令后，然后一路回车，这时你就会在用户下的.ssh目录里找到id_rsa和id_rsa.pub这两个文件

![image-20231119162201725](%E6%9C%AC%E5%9C%B0%E9%A1%B9%E7%9B%AE%E6%8E%A8%E5%88%B0gitHub%E4%BB%93%E5%BA%93.assets/image-20231119162201725.png)



#### 5、GitHub 上进行SSH Key的加密

登录Github,找到右上角的图标，打开点进里面的Settings，再选中里面的SSH and GPG KEYS，点击右上角的New SSH key，然后Title里面随便填，再把刚才id_rsa.pub里面的内容复制到Title下面的Key内容框里面，最后点击Add SSH key，这样就完成了SSH Key的加密
![image-20231119162516324](%E6%9C%AC%E5%9C%B0%E9%A1%B9%E7%9B%AE%E6%8E%A8%E5%88%B0gitHub%E4%BB%93%E5%BA%93.assets/image-20231119162516324.png)

![image-20231119162952863](%E6%9C%AC%E5%9C%B0%E9%A1%B9%E7%9B%AE%E6%8E%A8%E5%88%B0gitHub%E4%BB%93%E5%BA%93.assets/image-20231119162952863.png)

![image-20231119163005875](%E6%9C%AC%E5%9C%B0%E9%A1%B9%E7%9B%AE%E6%8E%A8%E5%88%B0gitHub%E4%BB%93%E5%BA%93.assets/image-20231119163005875.png)

![image-20231119163520929](%E6%9C%AC%E5%9C%B0%E9%A1%B9%E7%9B%AE%E6%8E%A8%E5%88%B0gitHub%E4%BB%93%E5%BA%93.assets/image-20231119163520929.png)

![image-20231119163550695](%E6%9C%AC%E5%9C%B0%E9%A1%B9%E7%9B%AE%E6%8E%A8%E5%88%B0gitHub%E4%BB%93%E5%BA%93.assets/image-20231119163550695.png)



在密钥管理系统中，"Authentication key" 和 "signing key" 是两种不同类型的密钥，它们在加密和身份验证过程中扮演不同的角色。

1. Authentication key（认证密钥）：

认证密钥主要用于数据传输过程中的身份验证。在安全通信中，发送方和接收方需要确认彼此的身份。认证密钥用于在通信双方之间建立安全连接，并验证消息的发送者是否是预期的发送者。通过使用认证密钥，可以防止中间人攻击（Man-in-the-Middle Attack），确保数据的完整性和机密性。

1. Signing key（签名密钥）：

签名密钥用于数字签名生成和验证。数字签名是一种用于验证消息完整性和来源的技术。签名密钥用于在发送方对消息进行签名，接收方则使用相应的签名密钥来验证签名的有效性。通过使用签名密钥，可以确保消息的完整性和可信度，防止消息被篡改或伪造。



#### 6、在GitHub上创建一个Git仓库

具体过程就不展示了，结果如下

![image-20231119163806683](%E6%9C%AC%E5%9C%B0%E9%A1%B9%E7%9B%AE%E6%8E%A8%E5%88%B0gitHub%E4%BB%93%E5%BA%93.assets/image-20231119163806683.png)

#### 七、在GitHub上创建好Git仓库后，我们就可以和本地仓库进行关联

![image-20231119164048699](%E6%9C%AC%E5%9C%B0%E9%A1%B9%E7%9B%AE%E6%8E%A8%E5%88%B0gitHub%E4%BB%93%E5%BA%93.assets/image-20231119164048699.png)

通过`git remote`命令将本地仓库和Github上的仓库进行关联,这里的`orgin`是自定义远端仓库的名称

这里采用SSH

![image-20231119180301390](%E6%9C%AC%E5%9C%B0%E9%A1%B9%E7%9B%AE%E6%8E%A8%E5%88%B0gitHub%E4%BB%93%E5%BA%93.assets/image-20231119180301390.png)

#### 八、将本地仓库的内容推送到远程仓库上

![image-20231119180446746](%E6%9C%AC%E5%9C%B0%E9%A1%B9%E7%9B%AE%E6%8E%A8%E5%88%B0gitHub%E4%BB%93%E5%BA%93.assets/image-20231119180446746.png)



#### 补充

1、查看本地仓库和远端仓库的绑定情况

`git remote -v`

![image-20231119165608723](%E6%9C%AC%E5%9C%B0%E9%A1%B9%E7%9B%AE%E6%8E%A8%E5%88%B0gitHub%E4%BB%93%E5%BA%93.assets/image-20231119165608723.png)



在上述示例中，远程仓库名称为 "origin"，说明已经成功绑定到 GitHub 仓库。如果输出中没有与 GitHub 相关的条目，则表示没有绑定到 GitHub 仓库。

请注意，如果你使用的是 HTTPS 协议，需要确保在本地仓库中配置了正确的用户名和密码，以便能够与 GitHub 进行通信。如果使用的是 SSH 协议，则需要确保已经生成并添加了正确的 SSH 密钥。

在上面的示例中，origin后面的fetch和push分别表示与远程仓库的拉取和推送操作。这意味着当你执行fetch命令时，Git会从origin远程仓库获取最新的版本。而当你执行push命令时，Git会将本地仓库的更改推送到origin远程仓库。



2、删除本地仓库和绑定的远端仓库

`git remote remove [远端仓库的名称]`





3、拉下remote代码，并进行远程分支切换

①`git clone` + SSH 或 http 拉下代码

②`git branch -a`查看所有远程分支

③`git branch` 查看本地分支

④`git check xxx(分支名称)`切换分支

示例：

![image-20231210235343533](%E6%9C%AC%E5%9C%B0%E9%A1%B9%E7%9B%AE%E6%8E%A8%E5%88%B0gitHub%E4%BB%93%E5%BA%93.assets/image-20231210235343533.png)



如果需要创建一个分支，并切换到上面，命令如下：

```
git checkout -b xxx

等价于

git branch xxx   创建xxx分支

git checkout xxx  切换到xxx分支
```





4、在远端创建仓库后，在本地初始化git后，需要通过`git pull --rebase origin master`命令同步一下，将分支的情况进行一个同步，远程仓库中的 `origin` 仓库的 `master` 分支拉取最新的提交，并将你本地的修改应用到这些提交之上。
