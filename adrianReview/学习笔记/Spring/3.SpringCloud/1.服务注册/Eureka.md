## 什么是Eureka	

​		Eureka是一种基于REST（Representational State Transfer）的服务，主要用于定位服务，以实现中间层服务器的负载平衡和故障转移；我们将此服务称为Eureka Server

​		Eureka还附带了一个基于Java的客户端组件Eureka Client，它使与服务的交互变得更加容易。客户端还有一个内置的负载均衡器，可以进行基本的循环负载均衡



## Eureka治理机制

### 服务注册

​		当客户端向服务注册中心注册时，客户端会提供自身的元数据，比如IP地址、端口、运行状况指标的URL、主页地址等信息



### 服务续约

​		客户端默认情况下会每隔30秒向注册中心发送一次心跳来进行服务的续约；通过心跳来告知注册中心，该服务仍然可用



### 服务下线

- 客户端在程序关闭时可以向注册中心发送下线请求。Eureka Server 会将该实例的信息从注册列表信息中删除

> 注意：<font color=red>服务下线不会自动调用</font>，需要在程序关闭时显示调用：`DiscoveryManager.getInstance().shutdownComponent();`



### 获取服务

- 客户端会从注册中心获取服务注册表信息，并且将其缓存到本地
- 客户端会使用服务注册列表的信息查找其他服务的信息，从而进行远程调用
- 该注册列表信息定时（默认每30秒）更新一次，客户端会自行整理跟本地缓存不相同的信息
- Eureka Server 缓存了所有服务注册列表信息，并将整个注册列表已经每个应用程序的信息进行了压缩。默认情况下，客户端和注册中心获取该信息的交互方式是 JSON 格式的



### 服务调用

​		服务消费者在获取服务清单后，通过服务名可以获得具体提供服务的实例名和该实例的元数据信息。在进行服务调用的时候，优先访问同处一个Zone中的服务提供方



### 失效剔除

​		有些时候，服务提供方并不一定会正常下线，可能因为内存溢出、网络故障等原因导致服务无法正常工作。Eureka Server 需要将这样的服务剔除出服务列表。因此它会开启一个定时任务，每隔60秒对所有失效的服务（超过90秒未响应）进行剔除

```yml
eureka:
  instance:
    #多久没收到心跳就剔除服务的时间配置, 默认90秒
    lease-expiration-duration-in-seconds: 90
  server:
    # 扫描失效服务的间隔时间（缺省为60*1000ms）
    eviction-interval-timer-in-ms: 1000    
```





## 注册延迟和缓存

1. <font color=orange>**客户端的注册延迟**</font>
   - 客户端启动后，不是立即向注册中心注册的，而是有一个延迟向服务端注册的时间。这个默认的延迟时间为40秒
2. <font color=orange>**注册中心的响应缓存**</font>
   - 注册中心维护 **<font color=blue>每30秒更新一次响应缓存</font>**，所以即使是刚注册的实例，也不会立即出现在服务注册列表中
   - 可以通过配置更改时间 `eureka.server.response-cache-update-interval-ms=30*1000` ，单位是毫秒
3. <font color=orange>**客户端的缓存**</font>
   - <font color=blue>客户端保存注册表信息的缓存，这个缓存**每30秒更新一次**</font>。所以客户端刷新本地缓存并发现其他新注册的实例可能需要至少30秒的时间
4. <font color=orange>**Ribbon的缓存**</font>
   - Ribbon从本地客户端获取服务注册列表信息。Ribbon本身还维护了缓存，以避免每个请求都需要从客户端获取注册列表信息
   - <font color=blue>Ribbon的注册列表信息的缓存时间是 30 秒</font>，可以通过配置更改时间。所以此时也至少需要30秒的时间去刷新实例的缓存；`ribbon.ServerListRefreshInterval=30000`



​		<font color=red>一个新实例的注册，**默认延迟40秒向注册中心进行注册**。即使注册了，也不能马上就被客户端进行调用，因为**客户端的注册列表信息的缓存也需要时间去进行刷新**</font>





## 自我保护机制

​		当有一个新的 Eureka Server 出现时，它会尝试从相邻的 Peer 节点（Eureka Server集群的其他节点）获取所有服务实例注册表信息。如果从相邻的 Peer 节点获取信息出现了故障，Eureka Server 就会尝试从其他的节点去获取实例注册表信息

​		如果 Server 能够成功获取所有的服务实例信息，则根据配置信息设置服务续约的阈值，在任何时间如果 Server 接收到的服务续约（即心跳）低于该阈值（阈值默认为15分钟内低于85%），则服务器开启自我保护模式。即不再剔除注册列表中的信息

​		这样做的好处是：**如果Eureka Server因为自身网络分区故障问题（延时、卡顿、拥挤）而导致客户端无法正常续约时，此时会保证客户端的注册信息不会被剔除，不再注销任何服务的实例**

​		这个模式的设计理念就是：宁可保留错误的服务注册信息，也不盲目注销任何可能健康的实例。这是一种应对网络异常的安全保护措施，保证了高可用性

```yml
eureka:
  server:
    # 关闭自我保护模式（缺省为打开）
    enable-self-preservation: false
    # 扫描失效服务的间隔时间（缺省为60*1000ms）
    eviction-interval-timer-in-ms: 1000
```





## Eureka配置

POM文件配置

```xml
<!-- eureka server: 提供服务发现与服务注册 -->
<dependencies>
    <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-starter-netflix-eureka-server</artifactId>
    </dependency>
</dependencies>

<!--eureka client 客户端 -->
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
</dependency>
```



### Server端配置

```yml
#配置服务端口
server:
  port: 8761

# 配置 Eureka Server 相关
##因为此服务作为 Eureka Server, 所以当前服务不需要注册到注册中心
##同样的也不需要获取 Eureka Server 上的注册列表信息
eureka:
  instance:
    hostname: localhost
    #上一次收到心跳后多少时间内没收到心跳就剔除服务的时间配置, 默认90秒
    lease-expiration-duration-in-seconds: 90
    # Eureka客户端向服务端发送心跳的时间间隔, 单位为秒 (默认是30秒)
    lease-renewal-interval-in-seconds: 30
  server:
    # 禁用自我保护,保证不可用服务被及时删除 (默认为打开)
    enable-self-preservation: false
    # 扫描失效服务的间隔时间(清理无效节点的时间间隔) (默认为60*1000ms) 
    eviction-interval-timer-in-ms: 1000  
  client:
    #是否注册到 Eureka Server
    register-with-eureka: false 
     #是否需要发现服务注册列表信息
    fetch-registry: false       
    #eureka 服务注册中心的地址, 集群方式用逗号分隔
    #service-url是一个Map
    #defaultZone就是Eureka服务所在的区域,通常是服务的url列表
    #默认的defaultZone是 (defaultZone:http://localhost:8761/eureka/)
    service-url:
      defaultZone:
        http://${eureka.instance.hostname}:${server.port}/eureka/
        # 集群配置: defaultZone中配置其他几台服务器的地址,逗号分隔
        #如: defaultZone: http://eureka8082.com:8082/eureka/,http://eureka8083.com:8083/eureka/
```



### Client端配置

```yml
# 建议不要用TAB键, 尽量使用空格键缩进
#配置服务端口
server:
  port: 8081

# 服务的名称, 这个必须配置, 用于服务的发现别名
spring:
  application:
    name: eureka-client
  cloud:
    loadbalancer:
      retry:
        enabled: true #开启Spring Cloud的重试功能  

# 配置 Eureka client 相关; 配置Eureka Sever的url列表
eureka:
  client:
    service-url:
      defaultZone:
        http://localhost:8761/eureka/
    registry-fetch-interval-seconds: 30 #指向注册中心获取注册实例列表的时间间隔,默认30s    
  instance:
    #客户端发送心跳时间的时间间隔, 默认30秒
    lease-renewal-interval-in-seconds: 30
    hostname: localhost
    #instance-id 是区分同一服务的不同实例的唯一标准;不能重复
    instance-id: ${spring.application.name}:${server.port}
    # 访问路径可以显示ip地址
    prefer-ip-address: true

#ribbon的超时时间, ribbon的一些配置在通用配置 CommonClientConfigKey 类中
ribbon:
  ReadTimeout: 3000
  ConnectTimeout: 3000
  MaxAutoRetries: 1                #同一台实例最大重试次数,不包括首次调用
  MaxAutoRetriesNextServer: 1      #重试负载均衡其他的实例最大重试次数,不包括首次调用
  OkToRetryOnAllOperations: false  #是否所有操作都重试
  ServerListRefreshInterval: 30000 #设置Ribbon缓存的时间,默认30s
```



### @EnableXX注解区别

Spring Cloud 中 Discovery Service 有许多种实现（Eureka、Consul、Zookeeper等等）

- `@EnableDiscoveryClient` 基于 spring-cloud-commons

- `@EnableEurekaClient` 基于 spring-cloud-netflix；该注解只适用于Eureka作为注册中心



### 身份验证配置

```yml
eureka:
  client:
    service-url:
      #在url上加上 ${user}:${password}@ 这样的参数,会经过HTTP进行基本的身份验证
      #其中 ${user} 为个人的用户配置
      defaultZone:
        http://${user}:${password}@${eureka.instance.hostname}:${server.port}/eureka/
```



