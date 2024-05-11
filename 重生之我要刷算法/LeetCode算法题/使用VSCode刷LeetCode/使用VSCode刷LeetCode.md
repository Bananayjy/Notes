# 使用VS Code刷LeetCode

## 一、前言

### 1.1、说明

本文章用来记录使用VS Code刷[LeetCode](https://leetcode.cn/)中遇到的一系列问题。

### 1.2、原因

- leetcode 网站中的在线编程环境代码提示、快捷键不友好
- VS Code中可以通过插件同步Leetcode所有题目，并且在VS Code中进行查看、测试、提交。

- ……

### 1.2、参考文章

- [在 VsCode 中优雅的刷 LeetCode 🔥](https://juejin.cn/post/7044565186656600072)
- [强大的Leetcode插件 ------ 直接在VS Code中刷LeetCode](https://blog.csdn.net/qq_45359288/article/details/124351804)

## 二、具体操作

### 2.1、VS Code中初始化刷题环境

首先确保已经安装node.js,通过通过如下命令查看安装情况,具体安装过程可以自行搜索，这里推荐使用nvm（nodejs的版本管理工具）去安装和管理node.js和对应的npm(Node.js 的官方包管理工具)：[nvm下载教程](https://www.cnblogs.com/gaozejie/p/10689742.html)

![image-20240511202456684](%E4%BD%BF%E7%94%A8VSCode%E5%88%B7LeetCode.assets/image-20240511202456684.png)

在VS Code中搜索LeetCode的插件，并安装

![image-20240511200412160](%E4%BD%BF%E7%94%A8VSCode%E5%88%B7LeetCode.assets/image-20240511200412160.png)

安装完成后，在VS Code界面的左侧会出现一个LeetCode的图标

![image-20240511200526698](%E4%BD%BF%E7%94%A8VSCode%E5%88%B7LeetCode.assets/image-20240511200526698.png)

点击Sign in to LeetCode，并选择用LeetCode的账号登录

![image-20240511200622892](%E4%BD%BF%E7%94%A8VSCode%E5%88%B7LeetCode.assets/image-20240511200622892.png)

输入账号和密码

![image-20240511200718341](%E4%BD%BF%E7%94%A8VSCode%E5%88%B7LeetCode.assets/image-20240511200718341.png)

如果提示如下内容，是因为Node14以下版本的[NodeJS项目](https://so.csdn.net/so/search?q=NodeJS项目&spm=1001.2101.3001.7020)在Node14及以上版本中运行时所抛出的异常

```
login: (node:40124) Warning: Accessing non-existent property 'padLevels' of module exports inside circular dependency
(Use `node --trace-warnings ...` to show where the warning was created)
pass: - Signing in leetcode.com
[ERROR] session expired, please login again [code=-1]
```

我们通过nvm切换较低的node版本,nvm相关命令可以参考这篇文章（最下面）:https://blog.csdn.net/Bananaaay/article/details/131738477

![image-20240511203301375](%E4%BD%BF%E7%94%A8VSCode%E5%88%B7LeetCode.assets/image-20240511203301375.png)

此时提示的内容是，可以发现不报错了

```
login: pass: - Signing in leetcode.com
[ERROR] session expired, please login again [code=-1]
```

我们将leetcode切换到中国版本

![image-20240511204140288](%E4%BD%BF%E7%94%A8VSCode%E5%88%B7LeetCode.assets/image-20240511204140288.png)

之后再按上面的登录流程，即可登录成功

![image-20240511204232164](%E4%BD%BF%E7%94%A8VSCode%E5%88%B7LeetCode.assets/image-20240511204232164.png)

此时，我们可以在VS CODE左侧看到刷题的内容

![image-20240511204305102](%E4%BD%BF%E7%94%A8VSCode%E5%88%B7LeetCode.assets/image-20240511204305102.png)

之后就可以开始刷题了

![image-20240511204733734](%E4%BD%BF%E7%94%A8VSCode%E5%88%B7LeetCode.assets/image-20240511204733734.png)

![image-20240511205121328](%E4%BD%BF%E7%94%A8VSCode%E5%88%B7LeetCode.assets/image-20240511205121328.png)