## 三、Tomcat

### 3.1、简介

#### 3.1.1 什么是Web服务器

Web服务器是一个应用程序（软件），对HTTP协议的操作（如上面的通过serversocket和socket实现自定义服务器）进行封装，使得程序员不必直接对协议进行操作，让Web开发更加便捷。主要功能是"提供网上信息浏览服务"。

![1627058356051](%E4%B8%89%E3%80%81Tomcat.assets/1627058356051.png)

 Web服务器是安装在服务器端的一款软件，将来我们把自己写的Web项目部署到Web Tomcat服务器软件中，当Web服务器软件启动后，部署在Web服务器软件中的页面就可以直接通过浏览器来访问了。

**Web服务器软件使用步骤**

* 准备静态资源
* 下载安装Web服务器软件
* 将静态资源部署到Web服务器上
* 启动Web服务器使用浏览器访问对应的资源

上述内容在演示的时候，使用的是Apache下的Tomcat软件，至于Tomcat软件如何使用，后面会详细的讲到。而对于Web服务器来说，实现的方案有很多，Tomcat只是其中的一种，而除了Tomcat以外，还有很多优秀的Web服务器，比如:

![1627060368806](%E4%B8%89%E3%80%81Tomcat.assets/1627060368806.png)





Tomcat就是一款软件，我们主要是以学习如何去使用为主。具体我们会从以下这些方向去学习:

1. 简介: 初步认识下Tomcat

2. 基本使用: 安装、卸载、启动、关闭、配置和项目部署，这些都是对Tomcat的基本操作

3. IDEA中如何创建Maven Web项目

4. IDEA中如何使用Tomcat,后面这两个都是我们以后开发经常会用到的方式

首选我们来认识下Tomcat。

**Tomcat**

Tomcat的相关概念:

* Tomcat是Apache软件基金会一个核心项目，是一个开源免费的轻量级Web服务器，支持Servlet/JSP少量JavaEE规范。

* 概念中提到了JavaEE规范，那什么又是JavaEE规范呢?

  JavaEE: Java Enterprise Edition,Java企业版。指Java企业级开发的技术规范总和。包含13项技术规范:JDBC、JNDI、EJB、RMI、JSP、Servlet、XML、JMS、Java IDL、JTS、JTA、JavaMail、JAF。

* 因为Tomcat支持Servlet/JSP规范，所以Tomcat也被称为Web容器、Servlet容器。Servlet需要依赖Tomcat才能运行。

* Tomcat的官网: https://tomcat.apache.org/ 从官网上可以下载对应的版本进行使用。

Tomcat的相关特点：

- 独立性：Tomcat是一个独立的、自包含的Web服务器，不依赖于其他服务器。它内嵌了一个HTTP服务器，可以独立地处理HTTP请求和响应。
- Servlet容器：Tomcat实现了Java Servlet规范（Java Enterprise Edition（Java EE）中定义的一套标准，用于开发基于Java的Web应用程序。它提供了一种在服务器端处理HTTP请求和响应的机制，并定义了Servlet容器如何加载、初始化、执行和管理Servlet的行为和规则），可以动态地加载、初始化、执行和管理Servlet。它充当了Servlet的承载环境，并提供了Servlet的生命周期管理和线程池等功能。
- JSP支持：Tomcat还支持JavaServer Pages（JSP），它能够编译和执行JSP页面，将其转换为Java Servlet并进行处理。
- 多线程处理：Tomcat能够处理并发的HTTP请求，使用线程池技术来管理和复用线程，以提高性能和效率。
- 配置灵活性：Tomcat提供了灵活的配置选项，可以通过XML配置文件进行定制和扩展，以满足不同应用程序的需求。

**Tomcat的LOGO**

![1627176045795](%E4%B8%89%E3%80%81Tomcat.assets/1627176045795.png)

#### 3.1.2 Web服务器作用

- 提供静态和动态内容：Web服务器可以托管并提供静态的HTML、CSS、JavaScript等文件，以及动态生成的内容，如服务器端脚本语言（如PHP、Python等）生成的动态页面。
- 处理HTTP请求：Web服务器能够接收客户端发送的HTTP请求，包括GET、POST等各种请求方法，以及处理请求中的参数和数据。
- 网络路由和负载均衡：Web服务器可以根据请求的URL路由到对应的处理程序或文件。它还可以支持负载均衡，将请求分发到多个后端服务器，以提高系统吞吐量和处理能力。
- 连接管理：Web服务器管理客户端的连接，包括建立和终止连接，以及处理并发连接请求和连接池管理。
- 安全性和身份验证：Web服务器可以提供安全功能，如SSL/TLS加密通信、安全套接层（HTTPS）、基于令牌的身份验证等，以保护数据的安全和用户身份的验证。
- 缓存和性能优化：Web服务器可以缓存静态资源或生成的页面，以减少对后端服务器的请求，提高响应速度和性能。它还可以提供gzip压缩和其他优化技术，以减少传输的数据量。
- 错误处理和日志记录：Web服务器可以处理错误情况并返回适当的错误码或页面。它还可以记录日志，包括访问日志、错误日志等，以便后续分析和故障排查。

总的来说，Web服务器扮演着连接客户端和提供Web内容之间的桥梁角色。它接收客户端的请求，处理请求并响应客户端，同时提供一系列的功能来提高性能、安全性和可靠性，为用户提供优质的Web服务。

#### 3.1.3 其他服务器介绍

除了Web服务器，还有其他类型的服务器，每个服务器都用于不同的用途。以下是一些常见的服务器类型：

- 文件服务器：文件服务器用于存储和管理文件，提供对文件的访问和共享。它可以通过网络提供文件访问服务，允许用户上传、下载和管理文件。
- 数据库服务器：数据库服务器用于存储和管理数据，提供对数据库的访问和查询。它管理数据库系统，并允许客户端应用程序通过SQL语言与数据库进行交互。
- 应用服务器：应用服务器用于托管和执行应用程序，提供特定的应用程序运行环境和服务。它可以支持不同的应用程序框架和技术，如Java EE应用服务器（如Tomcat、Jetty、WebLogic、WebSphere等）、.NET应用服务器（如IIS）等。
- 邮件服务器：邮件服务器用于处理和传输电子邮件。它负责接收、存储、发送和传递电子邮件，提供SMTP、POP3、IMAP等协议来支持电子邮件的收发。
- DNS服务器：DNS服务器负责将域名解析为相应的IP地址。它维护一个域名系统，以提供域名和IP地址之间的转换服务，使用户可以通过域名访问网站和服务。
- 文件传输服务器：文件传输服务器用于在客户端和服务器之间提供文件传输服务。它允许用户上传和下载文件，支持FTP、SFTP、TFTP等协议。
- 物联网（IoT）服务器：物联网服务器用于管理和控制物联网设备。它与智能设备通信，收集和分析数据，并提供与物联网相关的服务和功能。

这些是一些常见的服务器类型，每个服务器类型都有其特定的功能和用途。具体选择哪种服务器类型取决于你的需求和应用场景。



### 3.2、基本使用

#### 3.2.1 下载

直接从[tomact官网](https://tomcat.apache.org/)下载对应版本的Tomcat

![1627178001030](%E4%B8%89%E3%80%81Tomcat.assets/1627178001030.png)

#### 3.2.2 安装

Tomcat是绿色版,直接解压即可

* 在D盘的software目录下，将`apache-tomcat-8.5.68-windows-x64.zip`进行解压缩，会得到一个`apache-tomcat-8.5.68`的目录，Tomcat就已经安装成功。

  注意，Tomcat在解压缩的时候，解压所在的目录可以任意，但最好解压到一个不包含中文和空格的目录，因为后期在部署项目的时候，如果路径有中文或者空格可能会导致程序部署失败。

* 打开`apache-tomcat-8.5.68`目录就能看到如下目录结构，每个目录中包含的内容需要认识下,

  ![1627178815892](%E4%B8%89%E3%80%81Tomcat.assets/1627178815892.png)

  说明：

  bin:目录下有两类文件，一种是以`.bat`结尾的，是Windows系统的可执行文件，一种是以`.sh`结尾的，是Linux系统的可执行文件。

  webapps:就是以后项目部署的目录


#### 3.2.3 卸载

卸载比较简单，可以直接删除目录即可

#### 3.2.4 启动

双击: bin\startup.bat

![1627179006011](%E4%B8%89%E3%80%81Tomcat.assets/1627179006011.png)

启动后，通过浏览器访问 `http://localhost:8080`能看到Apache Tomcat的内容就说明Tomcat已经启动成功。

![1627199957728](%E4%B8%89%E3%80%81Tomcat.assets/1627199957728.png)

==注意==: 启动的过程中，控制台有中文乱码，需要修改conf/logging.prooperties

![1627199827589](%E4%B8%89%E3%80%81Tomcat.assets/1627199827589.png)

#### 3.2.5 关闭

关闭有三种方式 

* 直接x掉运行窗口:强制关闭[不建议]
* bin\shutdown.bat：正常关闭
* ctrl+c： 正常关闭

#### 3.2.6 配置

**修改端口**

* Tomcat默认的端口是8080，要想修改Tomcat启动的端口号，需要修改 conf/server.xml

![1627200509883](%E4%B8%89%E3%80%81Tomcat.assets/1627200509883.png)

> 注: HTTP协议默认端口号为80，如果将Tomcat端口号改为80，则将来访问Tomcat时，将不用输入端口号。

**启动时可能出现的错误**

* Tomcat的端口号取值范围是0-65535之间任意未被占用的端口，如果设置的端口号被占用，启动的时候就会包如下的错误

  ![1627200780590](%E4%B8%89%E3%80%81Tomcat.assets/1627200780590.png)

* Tomcat启动的时候，启动窗口一闪而过: 需要检查JAVA_HOME环境变量是否正确配置

![1627201248802](%E4%B8%89%E3%80%81Tomcat.assets/1627201248802.png)

#### 3.2.7 部署

* Tomcat部署项目： 将项目放置到webapps目录下，即部署完成。

  * 将 a.html文件拷贝到Tomcat的webapps目录的hello目录下

  * 通过浏览器访问`http://localhost/hello/a.html`，能看到下面的内容就说明项目已经部署成功。

    ![1627201572748](%E4%B8%89%E3%80%81Tomcat.assets/1627201572748.png)

    但是呢随着项目的增大，项目中的资源也会越来越多，项目在拷贝的过程中也会越来越费时间，该如何解决呢?

* 一般JavaWeb项目会被打包称==war==包，然后将war包放到Webapps目录下，Tomcat会自动解压缩war文件

  * 将 javaweb项目`haha.war`目录拷贝到Tomcat的webapps目录下

  * Tomcat检测到war包后会自动完成解压缩，在webapps目录下就会多一个haha目录

  * 通过浏览器访问`http://localhost/haha/a.html`，能看到下面的内容就说明项目已经部署成功。

    ![1627201868752](%E4%B8%89%E3%80%81Tomcat.assets/1627201868752.png)

至此，Tomcat的部署就已经完成了，至于如何获得项目对应的war包，后期我们会借助于IDEA工具来生成。



### 3.3、Maven创建Web项目

介绍完Tomcat的基本使用后，我们来学习在IDEA中如何创建Maven Web项目，学习这种方式的原因是以后Tomcat中运行的绝大多数都是Web项目，而使用Maven工具能更加简单快捷的把Web项目给创建出来，所以Maven的Web项目具体如何来构建呢?

在真正创建Maven Web项目之前，我们先要知道Web项目长什么样子，具体的结构是什么?

#### 3.3.1 Web项目结构

Web项目的结构分为:开发中的项目和开发完可以部署的Web项目,这两种项目的结构是不一样的，我们一个个来介绍下:

* Maven Web项目结构: 开发中的项目

  ![1627202865978](%E4%B8%89%E3%80%81Tomcat.assets/1627202865978.png)

* 开发完成部署的Web项目

  ![1627202903750](%E4%B8%89%E3%80%81Tomcat.assets/1627202903750.png)

  * 开发项目通过执行Maven打包命令package,可以获取到部署的Web项目目录
  * 编译后的Java字节码文件和resources的资源文件，会被放到WEB-INF下的classes目录下
  * pom.xml中依赖坐标对应的jar包，会被放入WEB-INF下的lib目录下

#### 3.3.2 创建Maven Web项目

介绍完Maven Web的项目结构后，接下来使用Maven来创建Web项目，创建方式有两种:使用骨架和不使用骨架

**使用骨架**

> 具体的步骤包含:
>
> 1.创建Maven项目
>
> 2.选择使用Web项目骨架
>
> 3.输入Maven项目坐标创建项目
>
> 4.确认Maven相关的配置信息后，完成项目创建
>
> 5.删除pom.xml中多余内容
>
> 6.补齐Maven Web项目缺失的目录结构

1. 创建Maven项目

   ![1627227574092](%E4%B8%89%E3%80%81Tomcat.assets/1627227574092.png)

2. 选择使用Web项目骨架

   ![1627227650406](%E4%B8%89%E3%80%81Tomcat.assets/1627227650406.png)

3. 输入Maven项目坐标创建项目

   ![1627228065007](%E4%B8%89%E3%80%81Tomcat.assets/1627228065007.png)

4. 确认Maven相关的配置信息后，完成项目创建

   ![1627228413280](%E4%B8%89%E3%80%81Tomcat.assets/1627228413280.png)

5. 删除pom.xml中多余内容，只留下面的这些内容，注意打包方式 jar和war的区别

   ![1627228584625](%E4%B8%89%E3%80%81Tomcat.assets/1627228584625.png)

6. 补齐Maven Web项目缺失的目录结构，默认没有java和resources目录，需要手动完成创建补齐，最终的目录结果如下

   ![](%E4%B8%89%E3%80%81Tomcat.assets/1627228673162.png)



**不使用骨架**

>具体的步骤包含:
>
>1.创建Maven项目
>
>2.选择不使用Web项目骨架
>
>3.输入Maven项目坐标创建项目
>
>4.在pom.xml设置打包方式为war
>
>5.补齐Maven Web项目缺失webapp的目录结构
>
>6.补齐Maven Web项目缺失WEB-INF/web.xml的目录结构

1. 创建Maven项目

   ![1627229111549](%E4%B8%89%E3%80%81Tomcat.assets/1627229111549.png)

2. 选择不使用Web项目骨架

   ![1627229137316](%E4%B8%89%E3%80%81Tomcat.assets/1627229137316.png)

3. 输入Maven项目坐标创建项目

   ![1627229371251](%E4%B8%89%E3%80%81Tomcat.assets/1627229371251.png)

4. 在pom.xml设置打包方式为war,默认是不写代表打包方式为jar

   ![1627229428161](%E4%B8%89%E3%80%81Tomcat.assets/1627229428161.png)

5. 补齐Maven Web项目缺失webapp的目录结构

   ![1627229584134](%E4%B8%89%E3%80%81Tomcat.assets/1627229584134.png)

6. 补齐Maven Web项目缺失WEB-INF/web.xml的目录结构

   ![1627229676800](%E4%B8%89%E3%80%81Tomcat.assets/1627229676800.png)

7. 补充完后，最终的项目结构如下:

   

   

   ![1627229478030](%E4%B8%89%E3%80%81Tomcat.assets/1627229478030.png)

上述两种方式，创建的web项目，都不是很全，需要手动补充内容，至于最终采用哪种方式来创建Maven Web项目，都是可以的，根据各自的喜好来选择使用即可。

### 3.4 IDEA使用Tomcat

* Maven Web项目创建成功后，通过Maven的package命令可以将项目打包成war包，将war文件拷贝到Tomcat的webapps目录下，启动Tomcat就可以将项目部署成功，然后通过浏览器进行访问即可。
* 然而我们在开发的过程中，项目中的内容会经常发生变化，如果按照上面这种方式来部署测试，是非常不方便的
* 如何在IDEA中能快速使用Tomcat呢?

在IDEA中集成使用Tomcat有两种方式，分别是集成本地Tomcat和Tomcat Maven插件

#### 3.4.1 集成本地Tomcat

目标: 将刚才本地安装好的Tomcat8集成到IDEA中，完成项目部署，具体的实现步骤

1. 打开添加本地Tomcat的面板

   ![1627229992900](%E4%B8%89%E3%80%81Tomcat.assets/1627229992900.png)

2. 指定本地Tomcat的具体路径

   ![1627230313062](%E4%B8%89%E3%80%81Tomcat.assets/1627230313062.png)

3. 修改Tomcat的名称，此步骤可以不改，只是让名字看起来更有意义，HTTP port中的端口也可以进行修改，比如把8080改成80

   ![1627230366658](%E4%B8%89%E3%80%81Tomcat.assets/1627230366658.png)

4. 将开发项目部署项目到Tomcat中

   ![1627230913259](%E4%B8%89%E3%80%81Tomcat.assets/1627230913259.png)

   扩展内容： xxx.war和 xxx.war exploded这两种部署项目模式的区别?

   * war模式是将WEB工程打成war包，把war包发布到Tomcat服务器上

   * war exploded模式是将WEB工程以当前文件夹的位置关系发布到Tomcat服务器上
   * war模式部署成功后，Tomcat的webapps目录下会有部署的项目内容
   * war exploded模式部署成功后，Tomcat的webapps目录下没有，而使用的是项目的target目录下的内容进行部署
   * 建议大家都选war模式进行部署，更符合项目部署的实际情况

5. 部署成功后，就可以启动项目，为了能更好的看到启动的效果，可以在webapp目录下添加a.html页面

   ![1627233265351](%E4%B8%89%E3%80%81Tomcat.assets/1627233265351.png)

6. 启动成功后，可以通过浏览器进行访问测试

   ![1627232743706](%E4%B8%89%E3%80%81Tomcat.assets/1627232743706.png)

7. 最终的注意事项

   ![1627232916624](%E4%B8%89%E3%80%81Tomcat.assets/1627232916624.png)

   

至此，IDEA中集成本地Tomcat进行项目部署的内容我们就介绍完了，整体步骤如下，大家需要按照流程进行部署操作练习。

![1627205657117](%E4%B8%89%E3%80%81Tomcat.assets/1627205657117.png)

 #### 3.4.2 Tomcat Maven插件

在IDEA中使用本地Tomcat进行项目部署，相对来说步骤比较繁琐，所以我们需要一种更简便的方式来替换它，那就是直接使用Maven中的Tomcat插件来部署项目，具体的实现步骤，只需要两步，分别是:

1. 在pom.xml中添加Tomcat插件

   ```xml
   <build>
       <plugins>
       	<!--Tomcat插件 -->
           <plugin>
               <groupId>org.apache.tomcat.maven</groupId>
               <artifactId>tomcat7-maven-plugin</artifactId>
               <version>2.2</version>
           </plugin>
       </plugins>
   </build>
   ```

2. 使用Maven Helper插件快速启动项目，选中项目，右键-->Run Maven --> tomcat7:run

![1627233963315](%E4%B8%89%E3%80%81Tomcat.assets/1627233963315.png)

==注意:==

* 如果选中项目并右键点击后，看不到Run Maven和Debug Maven，这个时候就需要在IDEA中下载Maven Helper插件，具体的操作方式为: File --> Settings --> Plugins --> Maven Helper ---> Install,安装完后按照提示重启IDEA，就可以看到了。

![1627234184076](%E4%B8%89%E3%80%81Tomcat.assets/1627234184076.png)

* Maven Tomcat插件目前只有Tomcat7版本，没有更高的版本可以使用
* 使用Maven Tomcat插件，要想修改Tomcat的端口和访问路径，可以直接修改pom.xml

```xml
<build>
    <plugins>
    	<!--Tomcat插件 -->
        <plugin>
            <groupId>org.apache.tomcat.maven</groupId>
            <artifactId>tomcat7-maven-plugin</artifactId>
            <version>2.2</version>
            <configuration>
            	<port>80</port><!--访问端口号 -->
                <!--项目访问路径
					未配置访问路径: http://localhost:80/tomcat-demo2/a.html
					配置/后访问路径: http://localhost:80/a.html
					如果配置成 /hello,访问路径会变成什么?
						答案: http://localhost:80/hello/a.html
				-->
                <path>/</path>
            </configuration>
        </plugin>
    </plugins>
</build>
```

**小结**

通过这一节的学习，大家要掌握在IDEA中使用Tomcat的两种方式，集成本地Tomcat和使用Maven的Tomcat插件。后者更简单，推荐大家使用，但是如果对于Tomcat的版本有比较高的要求，要在Tomcat7以上，这个时候就只能用前者了。



### 补充

#### 1、jar包和war包的区别

**JAR包**

JAR包（Java Archive File）是类（JAVA类文件）的归档文件，与平台无关的文件格式，它允许将许多文件组合成一个压缩文件。JAR 文件格式以流行的 ZIP 文件格式为基础（准确地说，它就是 zip 包，所以叫它文件包）。但与 ZIP 文件不同的是，JAR 文件不仅用于压缩和发布，而且还用于部署和封装库、组件和插件程序，并可被像编译器和 JVM 这样的工具直接使用（我们可以在自己的项目中引入其他人的jar包，直接使用这些jar包中的类和属性）。因为jar包主要是对class文件进行打包，而[java编译](https://so.csdn.net/so/search?q=java编译&spm=1001.2101.3001.7020)生成的class文件是平台无关的，这就意味着jar包是跨平台的，所以不必关心涉及具体平台的问题。

我们用压缩包打开JAR包（或用命令jar -tf jar包名打开，需要配置jdk环境），如果要打包成为可执行的jar文件需要再maven中配置如下插件

```xml
<build>
    <plugins>
        <plugin>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-maven-plugin</artifactId>
        </plugin>
    </plugins>
</build>
```

直接使用[maven](https://so.csdn.net/so/search?q=maven&spm=1001.2101.3001.7020)自带的package打包功能，在打Jar包的时候，不会将该项目所依赖的Jar包一起打进去，在使用“java -jar”命令启动项目时会报错，项目无法正常启动。这个时候，我们就可以考虑引用spring-boot-maven-plugin插件来为项目打Jar包。maven项目的pom.xml中，添加了下述插件，当运行“maven package”进行打包时，会打包成一个可以直接运行的JAR文件，使用“java -jar”命令就可以直接运行。

特别说明：引入了spring-boot-maven-plugin插件后，在使用打包功能时会将mvn package 生成的 jar或者war 重新打包成可执行文件，同时修改原文件名，增加.origin 后缀。如果项目需要打包为war包，则其实不需要引入该插件，maven原生自带的package插件就已经可以满足。



没加插件打包结果

![image-20240519193113656](%E4%B8%89%E3%80%81Tomcat.assets/image-20240519193113656.png)

加了插件打包结果

![image-20240519193437054](%E4%B8%89%E3%80%81Tomcat.assets/image-20240519193437054.png)

- META-INF：这个目录通常位于JAR包的根目录下。它包含了一些与JAR包有关的元数据信息。其中最常见的文件是清单文件（MANIFEST.MF），该文件描述了JAR包的基本信息，如版本号、作者、依赖项等。除了清单文件外，还可能包含一些签名相关的文件，用于验证JAR包的完整性和安全性，其中Main-Class是一个重要的属性，用于指定该JAR包的主要入口类（Main Class），当你运行JAR包时，Java虚拟机（JVM）会尝试加载并执行指定的主类。

- BOOT-INF：这个目录是Spring Boot项目中的一个重要目录。它包含了打包后的可执行JAR包的内部结构。其中最常见的子目录是`classes`和`lib`

  - classes：这个目录包含了项目的编译后的类文件（.class）。这些类文件是项目源代码编译后生成的，存放在各自的包结构目录中。
  - lib：这个目录包含了项目所依赖的JAR包文件。这些JAR包是项目所需的外部库和依赖，可以直接在项目中使用。

- org：这是一个按照Java的包结构组织的文件夹，用于存放JAR包中的类文件。具体的包名可能有所不同，但通常是以`org`为根目录开始。这个目录存放的是JAR包所提供的一些类、工具或库，可以在Java应用程序中引入和使用。其中包含特殊启动类（即Main-Class中指定的主要入口类）org.springframework.boot.loader.JarLauncher类，其是Spring Boot框架中的一个特殊启动类，负责加载和启动Spring Boot应用程序，在Spring Boot应用程序的打包过程中，会将应用程序的所有依赖（包括项目自身的代码和外部的依赖库）打包成一个可执行的JAR文件。通常情况下，使用`java -jar`命令执行这个JAR文件来启动Spring Boot应用程序。

  启动过程如下：

  1. 首先，`java -jar`命令会触发JVM加载JDK中的java命令，并指定参数为`-jar`和要执行的JAR文件的路径。
  2. JVM会解析JAR文件的清单文件（MANIFEST.MF），根据清单文件中指定的Main-Class属性来定位启动类。
  3. 找到Main-Class属性指定的启动类，这里就是`org.springframework.boot.loader.JarLauncher`。
  4. `org.springframework.boot.loader.JarLauncher`的主要功能是创建一个新的类加载器，并使用这个加载器加载JAR文件中的应用程序类和依赖。
  5. 加载完类之后，`org.springframework.boot.loader.JarLauncher`再使用反射机制调用应用程序的主类的main方法，从而启动应用程序。



**war包**

War包是JavaWeb程序打的包，war包里面包括写的代码编译成的class文件，依赖的包，配置文件，所有的网站页面，包括html，jsp、web.xml的配置文件等等。一个war包可以理解为是一个web项目，里面是项目的所有东西。我们部署工程，直接放到tomcat的webapps目录下，直接启动tomcat即可。同时，可以使用WinRAR查看war包，直接将后缀.war改成.rar。



**JAR包和War包的区别**

- jar是java普通项目打包，通常是开发时要引用通用类，打成jar包便于存放管理。当你使用某些功能时就需要这些jar包的支持，需要导入jar包。war是java web项目打包，web网站完成后，打成war包部署到服务器，目的是为了节省资源，提供效率。

jar文件（扩展名为. Jar，Java Application Archive）包含Java类的普通库、资源（resources）、辅助文件（auxiliary files）等。通常是开发时要引用的通用类，打成包便于存放管理。简单来说，jar包就是别人已经写好的一些类，然后对这些类进行打包。可以将这些jar包引入到你的项目中，可以直接使用这些jar包中的类和属性，这些jar包一般放在lib目录下。



war文件（扩展名为.War,Web Application Archive）包含全部Web应用程序。在这种情形下，一个Web应用程序被定义为单独的一组文件、类和资源，用户可以对jar文件进行封装，并把它作为小型服务程序（servlet）来访问。 war包是一个可以直接运行的web模块，通常用于网站，打成包部署到容器中。以Tomcat来说，将war包放置在其\webapps\目录下,然后启动Tomcat，这个包就会自动解压，就相当于发布了。war包是Sun提出的一种web应用程序格式，与jar类似，是很多文件的压缩包。war包中的文件按照一定目录结构来组织。根据其根目录下包含有html和jsp文件，或者包含有这两种文件的目录，另外还有WEB-INF目录。通常在WEB-INF目录下含有一个web.xml文件和一个classes目录，web.xml是这个应用的配置文件，而classes目录下则包含编译好的servlet类和jsp，或者servlet所依赖的其他类（如JavaBean）。通常这些所依赖的类也可以打包成jar包放在WEB-INF下的lib目录下。



Ear文件（扩展名为.Ear,Enterprise Application Archive）包含全部企业应用程序。在这种情形下，一个企业应用程序被定义为多个jar文件、资源、类和Web应用程序的集合。



SpringBoot项目既可以打成war包发布，也可以找成jar包发布。
jar包：直接通过内置Tomcat运行，不需要额外安装Tomcat。如需修改内置Tomcat的配置，只需要在SpringBoot的配置文件中配置。内置Tomcat没有自己的日志输出，全靠jar包应用输出日志。但是比较方便，快速，比较简单。
war包：传统的应用交付方式，需要安装Tomcat，然后放到wabapps目录下运行war包，可以灵活选择Tomcat版本，可以直接修改Tomcat的配置，有自己的Tomcat日志输出，可以灵活配置安全策略,相对打成jar包来说没那么快速方便。

