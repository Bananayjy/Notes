# IDEA获取源码失败问题

### 一、参考文章

- [IntelliJ IDEA :decompiled.class file bytecode version:52.0(java 8) ，open source file.](https://blog.csdn.net/qq_27093465/article/details/52858092)
- [IDEA Maven 下载源码出现：Cannot download sources Sources not found for: xxx](https://www.jianshu.com/p/a259e322794c)
- [maven下载不到源码：Cannot download sources Sources not found for:](https://blog.csdn.net/maoyuanming0806/article/details/120079191)

## 二、原由

在查看一些引入的第三方jar包或java源码时，会出现如下的提示

![image-20240505154616591](IDEA%E4%B8%8B%E8%BD%BD%E6%BA%90%E7%A0%81%E5%A4%B1%E8%B4%A5.assets/image-20240505154616591.png)

```
# 原文
Decompiled .class file, bytecode version:52.0
# 翻译
反编译的.class文件，字节码版本:52.0

# 后面的两个选项分为是
# 下载源码jar包
Download Sources
# 选择已经有的源码jar包
Choose Sources
```

> 关于.java和.classs说明
>
> - .java
>
> Java的源文件后缀，编写的代码需要写在.java文件中。
>
> - .class
>
> 字节码文件，是.java源文件通过javac命令编译后生成的文件,Java虚拟机就是去运行.class文件从而实现程序的运行。

在 Maven 构建项目时，通常会生成以下几种 JAR 文件

- 主 JAR 文件： 这是项目的主要 JAR 文件，包含编译后的类文件（.class 文件），以及资源文件（如配置文件、静态资源等）。这个 JAR 文件是项目的主要部署包，用于在其他项目中引用或部署。
- 源代码 JAR 文件： 这个 JAR 文件包含项目的源代码文件（.java 文件）。通常情况下，这个 JAR 文件的名称会带有 `-sources` 后缀，例如 `your-project-name-sources.jar`。这个 JAR 文件用于开发者查看源代码，进行调试或者在 IDE 中进行跳转。
- Javadoc JAR 文件： 这个 JAR 文件包含项目的 Javadoc 文档，通常是 API 文档。它包含了项目中公开的类、方法、字段等的文档说明。通常情况下，这个 JAR 文件的名称会带有 `-javadoc` 后缀，例如 `your-project-name-javadoc.jar`。这个 JAR 文件用于开发者查看项目的文档说明，了解项目的 API 接口和用法。

>出现该提示的原因
>
>idea可以把class文件直接给反编译成Java文件，里面的方法变量啥的都有，但是就是没的注释，什么情况下才能完美反编译呢，就是你本地的代码仓库里面有对应jar的source文件，这个时候，你在看源码的时候，idea可以根据class文件找到source文件里面对应的Java文件，这个Java文件，里面代码才是带着完整注释的文件。若是idea不能智能的找到对应的source文件jar，你可以手动的chose source，前提是你本地得有source jar。要是你本地压根儿就没得source文件，比如你使用的这个jar是别人非开源的，在代码仓库是没有对应source.jar的，那可不就是看不到一个完美的带注释的Java文件，看到的只是一个简单的经过反编译的class文件，里面的方法名和参数名可能就不那么能表达方法或者属性的意思了
>一般情况下，你新打开到一个源码的jar的时候，本地是没的source的，你就点download，下载source，要是能下载成功，那就下载下来了，就可以看到带注释的源码了，要是下载失败，那估计就看不到完整的source Java文件了，只能讲究看一下idea粗糙反编译的文件了。没的注释的那种

但是在下载源码的时候，出现了如下问题

![image-20240505153642679](IDEA%E4%B8%8B%E8%BD%BD%E6%BA%90%E7%A0%81%E5%A4%B1%E8%B4%A5.assets/image-20240505153642679.png)

## 二、解决

在IDEA提供的cmd命令行终端中输入`mvn -v `，查看是否配置maven的环境变量

![image-20240505161810746](IDEA%E4%B8%8B%E8%BD%BD%E6%BA%90%E7%A0%81%E5%A4%B1%E8%B4%A5.assets/image-20240505161810746.png)

然后在cmd命令行终端中执行如下命令，去下载jar和源码

```
mvn dependency:resolve -Dclassifier=sources
```

- -Dclassifier=sources参数： Maven 解析器要下载带有 "sources" 分类的依赖,Maven 将会解析项目的依赖，并尝试下载每个依赖的源代码 JAR 文件（如果有的话）。如果成功，这些源代码 JAR 文件将会被下载到本地 Maven 仓库中，并可以在你的项目中使用。