## Web相关基础

### web服务器

web服务器：接收用户请求，处理用户请求，做出响应



### Http工作原理

​		Http协议是浏览器和服务器之间的数据传输协议，作为应用层的协议，HTTP是基于TCP/IP的协议来传输数据的；HTTP协议不涉及数据包的传输，主要规定了客户端和服务器之间的通信格式

![Http请求web服务器](.\images\Http请求web服务器.png)



### servlet 容器工作流程

​		当客户请求某个资源时，HTTP 服务器会用一个 `ServletRequest` 对象把客户的请求信息封装起来，然后调用 `Servlet` 容器的 `service` 方法。

​		`Servlet` 拿到请求后，根据请求的URL和 `Servlet` 的映射关系，找到对应的 `service` 方法，如果该 `Servlet` 还没被加载，就会使用反射机制创建该 `service` ，并调用 `Servlet` 的 `init` 方法来完成初始化，接着调用 `service` 方法来处理客户的请求，把 `ServletResponse` 对象返回给HTTP服务器，由该服务器返回响应给客户端



## Tomcat架构

### Tomcat核心功能

1. 处理 `Socket` 连接，负责网络字节流与 `Request` 和 `Response`  对象的转换
2. 加载和管理 `Servlet` ，以及具体处理 `Request` 请求

这两个功能具体由 Tomcat 核心组件完成：连接器 (Connector) 和 容器 (Container)

- 连接器负责对外的连接
- 容器负责内部请求的处理



### Catalina架构

![catalina架构](.\images\catalina架构.png)

​		Catalina负责管理Server，而Server表示着整个服务器。Server下面有多个服务Service，每个服务都包含着多个连接器组件 Connector（coyote实现） 和一个容器组件 Container

Tomcat启动的时候，会生成一个 catalina 的实例，其各个组件的功能：

| 组件      | 作用                                                         |
| --------- | ------------------------------------------------------------ |
| Catalina  | 负责解析Tomcat配置文件，创建服务器Server组件，并根据相应命令管理Server |
| Server    | 负责组装并启动Servle引擎、tomcat连接器；Server通过实现Lifecycle接口提供一种优雅的启动和关闭系统的方式 |
| Service   | 一个Server包含多个Service，它将若干个Connector组件绑定到一个Container上 |
| Connector | 连接器，负责与客户端通信；接收客户请求，转给相关的容器处理，最后返回响应结果 |
| Container | 负责处理用户的servlet请求，并且返回响应                      |



### 连接器Connector

​		`Coyote` 是 Tomcat 连接器框架的名称，是 Tomcat服务器提供的供客户端访问的外部接口。客户端通过 `Coyote` 与服务器建立连接、发送请求并且接受响应

​		`Coyote` 封装了底层的网络通信（`Socket` 请求和响应处理） ，为 `Catalina` 容器提供了统一的接口，使`cateline` 容器与具体的请求协议和IO操作方式完全解耦。 `Coyote` 将 `Socket` 输入转换封装为 `Request` 对象，交给 `Cateline` 容器进行处理，处理完成后通过 `Coyote` 提供的 `Response` 对象将结果写入输出流

​		`Coyote` 只负责具体的协议和 IO 相关操作，与 `Servlet` 规范没有直接的关系，因此 `Request` 和`Response` 对象并没有实现 `Servlet` 规范对应的接口，而是在 `Catalina` 容器中被封装成 `ServletRequest` 和 `ServletResponse` 对象

#### IO模型和协议

Tomcat支持的IO模型（8.5和9.0后，移除了 BIO 的支持）

| IO模型 | 描述                            |
| ------ | ------------------------------- |
| NIO    | 非阻塞IO                        |
| NIO2   | 异步IO，采用 JDK 的NIO2类库实现 |
| APR    | 采用Apache可移植运行库实现      |

Tomcat支持的应用层协议：

| 应用层协议 | 描述                                                  |
| ---------- | ----------------------------------------------------- |
| HTTP/1.1   | 常用的协议                                            |
| HTTP/2     | 提升了Web的性能，下一代HTTP协议                       |
| AJP        | 用于和Web服务器集成，以实现对静态资源的优化和集群部署 |

#### 连接器组件

![连接器](.\images\连接器.png)

1. EndPoint
   - `Coyote` 的通信端点，即通信监听的接口；是具体 `Socekt` 接收和发送处理器，是对传输层的抽象，所以 `EndPoint` 是用来实现 TCP/IP 协议的
   - tomcat 提供了 `AbstractEndPoint` 抽象类，并且定义了两个内部类：`Acceptor` 和 `SocketProcessor` 
     - `Acceptor` 用于监听 `Socket` 连接请求
     - `SocketProcessor` 用于处理接收到的 `Socket` 请求，它实现了 `Runnable` 接口，在 `run`方法中调用协议处理组件 `Processor` 进行处理。并且为了提高处理能力，`SocketProcessor` 会被提交给线程池来执行（Executor）
2. Processor
   - `Coyote` 的协议处理接口，用于实现 HTTP 协议的。其接收来自 `EndPoint` 的 `Socket`，读取字节流解析成 tomcat 的 `Request` 和 `Response` 对象，并且通过 `Apadter` 来适配成 `ServletRequest` 对象后交给容器处理。`Processor` 是对应用层协议的抽象
3. ProtocolHandler
   - `Coyote` 的协议接口，通过 `EndPoint` 和 `Processor` 实现针对不同协议的处理能力；按照协议和 I/O 提供了6个实现类
     - AJP协议
       - `AjpNioProtocol` ：采用NIO模型
       - `AjpAprProtocol`：采用APR模型，依赖APR库
       - `AjpNio2Protocol`：采用NIO2模型
     - HTTP协议
       - `Http11NioProtocol` 默认使用的协议（没有APR库的情况下）
       - `Http11Nio2Protocol` 
       - `Http11AprProtocol`
     - 在配置 `server.xml` 时，需要指定具体的 `ProtocolHandler` ，也可以指定协议的名称（如HTTP/1.1）
4. Adapter
   - 这是适配器模式的经典应用。由于协议的不同，客户端发送的请求信息也不同；Tomcat 定义了 Request 类来存放请求信息。ProtocolHandler 负责解析请求并且生成 Request 对象，需要使用 `CoyoteAdapter` 来将 Request 对象适配成 `ServletRequest` 对象



### Container容器

Tomcat中定义了四种容器：

- Engine：表示整个 `Catalina` 的 `servlet` 引擎（可以看做容器对外提供功能的入口），一个引擎包含若干个Host

- Host： 表示一个虚拟主机，一个主机包含若干个Context

- Context：表示一个Web应用，一个上下文包含若干个wrapper

- Wrapper：表示一个独立的servlet，包装器作为容器中的最底层，不能包含子容器

最后执行请求的，是 `Wrapper` 下的 `servlet` ，其他的上层结构，是负责加载配置项，以及查找合适的 `Wrapper` 来 `invoke()`



## 服务器配置

### Server.xml配置文件详解

配置文件层级结构

- Server
  - Listener
  - GlobalNamingResources
  - service

#### Server标签

`<Server port="8005" shutdown="SHUTDOWN">`

- port：配置 Tomcat 监听**关闭服务器**的端口号
- shutdown：关闭服务器的指令字符串

#### Linstener标签

```xml
<!-- 用于以日志形式输出服务器、操作系统、JVM版本信息 -->
<Listener className="org.apache.catalina.startup.VersionLoggerListener" />

<!-- 用于加载和销毁APR, 如果找不到APR库, 则会输出日志, 不影响Tomcat启动 -->
<Listener className="org.apache.catalina.core.AprLifecycleListener" SSLEngine="on" />

<!-- 用于避免JRE内存泄漏问题 -->
<Listener className="org.apache.catalina.core.JreMemoryLeakPreventionListener" />

<!-- 用于加载和销毁全局命名服务 -->
<Listener className="org.apache.catalina.mbeans.GlobalResourcesLifecycleListener" />

<!-- 用于在Context停止时重建Executor中的线程, 以避免ThreadLocal相关内存泄漏 -->
<Listener className="org.apache.catalina.core.ThreadLocalLeakPreventionListener" />
```

#### GlobalNamingResources标签

全局命名服务：配置一些用户和角色的相关信息（tomcat-users.xml）

```xml
<GlobalNamingResources>
    <!-- Editable user database that can also be used by
         UserDatabaseRealm to authenticate users
    -->
    <Resource name="UserDatabase" auth="Container"
              type="org.apache.catalina.UserDatabase"
              description="User database that can be updated and saved"
              factory="org.apache.catalina.users.MemoryUserDatabaseFactory"
              pathname="conf/tomcat-users.xml" />
  </GlobalNamingResources>
```

#### Service标签

##### Executor标签

默认情况下，Service 并没有添加共享线程池的配置

```xml
<Executor name="tomcatThreadPool" 
          namePrefix="catalina-exec-"
          maxThreads="150" 
          minSpareThreads="4"
          maxIdleTime="6000"
          maxQueueSize="Integer.MAX_VALUE"
          prestartminSpareThreads="false"
          threadPriority="5"
          className="org.apache.catalina.core.StandardThreadExecutor"/>
```

- **name**：线程池名称
- **namePrefix**：创建的每个线程的名称的前缀
- **maxThreads**：线程池中最大线程数
- **minSpareThreads**：核心线程数
- **maxIdleTime**：线程最大空闲时间，单位毫秒
- **maxQueueSize**：线程池阻塞队列的最大任务排队数目
- **prestartminSpareThreads**：启动线程池时是否启动 minSpareThreads 配置数目的核心线程，默认不启动
- **threadPriority**：线程池中线程的优先级，可选1-10
- **className**：线程池实现类，上面配置的就是默认的实现类。如果要使用自己实现的线程池，那么需要实现 `org.apache.catalina.core.Executor` 接口



##### Connector标签

```xml
<Connector port="8080" 
           protocol="HTTP/1.1"
           connectionTimeout="20000"
           redirectPort="8443"
           URIEncoding="UTF-8"
           executor="tomcatThreadPool"
           maxConnections="1000"
           connectionTimeout="20000"
           compression="on"
           compressionMinSize="2048"
           disableUploadTimeout="true"/>

<Connector port="8009" protocol="AJP/1.3" redirectPort="8443" />
```

- **port**：端口号，用于创建服务器 Socket 监听的端口号，用于等待客户端的连接；如果配置为 0 ，那么Tomcat会随机选择一个端口来作为监听端口号
- **protocol**：当前连接器的协议版本。默认 HTTP/1.1 
- **connectionTimeout**：连接器连接后，等待的超时时间，默认毫秒。配置 -1 表示永远不超时
- **redirectPort**：当前的Connector不支持SSL请求（SSL是HTTPS协议的证书），当接收这种请求时，Catalina就会自动将这种请求重定向到这个端口
- **URIEncoding**：指定字符编码方式；Tomcat8.x版本默认UTF-8，7.x版本默认ISO-8859-1
- **executor**：指定使用的线程池，可自行配置（参考Executor标签）；默认使用 http-nio-8080-exec- 线程池