# 微服务简介

## MVC模型

​		MVC模型分为 Model（数据访问层）、View（表示层）和 Controller（业务逻辑层）组成

​		MVC模型是一个单体应用的经典模型，单体架构按照这三层模型进行开发分层，MVC模型对业务逻辑层的业务场景没有进行划分，这三层构建部署成一个单体应用

​		这样的单体应用在业务场景越来越多越来越复杂的情况下，代码量会变得越来越大，可读性，可扩展性和可维护性会变得很低。且随着并发量的提升，单体应用会达到性能的瓶颈

## 集群模式

​		对于单体应用的瓶颈可以使用集群形式的部署单机应用来应对并发量，且为其添加负债均衡服务器（如Nginx），同时添加缓存服务器和文件服务器去减轻数据库访问压力，数据库也可以采用读写分离的形式应对高并发量的访问

​		对于单体应用的集群模式仍然会存在问题：对于代码的可读性，可维护性和可扩展性仍然很差；业务越来越复杂，对于持续交付（修改和添加代码，新人熟悉旧代码的时间和成本）就会变得越来越差；且面对海量的用户，数据库会变成瓶颈（可以使用分布式数据库，也就是分库分表解决）



## 微服务

[马丁福勒微服务论文](https://martinfowler.com/articles/microservices.html)

​		微服务这种架构风格就是把一组小服务演化成为一个单一的应用的一种方法。每个应用都运行在自己的进程中，并通过轻量级的机制保持通信，就像HTTP这样的API。这些服务要基于业务场景，并使用自动化布署工具进行独立的发布。可以有一个非常轻量级的集中式管理来协调这些服务，可以使用不同的语言来编写服务，也可以使用不同的数据存储

​		微服务架构风格是以服务构建应用。这些服务还可以被独立布署、独立扩展，每个服务也都提供了清晰的模块边界，甚至不同的服务都可以使用不同的编程语言来实现，也可以由不同的团队进行管理



### 微服务的特点

- <font color=blue>按业务划分成一个独立运行的程序，即服务单元</font>

  > ​		一个大的业务可以拆分成若干更小的业务；例如：微博的划分，微博内容（微博内容又可以分为点赞、评论等）、粉丝、关注等
  >
  > ​		按照业务拆分后的微服务都是独立部署的，是高度组件化的模块，并且有稳定的模块边界，服务于服务之间没有任何的耦合

- <font color=blue>服务之间通过HTTP等协议进行通信</font>

  - 常用的是使用 Restful API ，然后通过 JSON 做数据的交互

- <font color=blue>自动化部署</font>

  - docker + Jenkins + K8s 自动化部署；可以提高部署的效率

- <font color=blue>可以使用不同的编程语言开发，使用不同的存储技术</font>

- <font color=blue>服务集中化管理</font>

  - 微服务系统通过业务单元划分微服务，服务数量越多就越难管理，所以需要进行集中化的管理；如使用Eureka

- <font color=blue>微服务是一个分布式系统</font>

  > ​		分布式系统是集群方式部署的，由很多服务相互协调共同完成一个完整的应用系统；能够处理海量的用户请求。而且分布式服务可以部署在不同的机房或者地区
  >
  > ​		分布式系统比单体系统更加复杂，主要体现在：<font color=red>服务的独立性和服务的相互协调的可靠性、分布式事务（分布式下的一致性问题）、全局锁、全局唯一ID等</font>



### 雪崩问题及熔断机制

​		在分布式系统下，服务之间相互依赖，当一个服务出现了故障或者网络延迟问题，在高并发的情况下，会导致线程阻塞，在很短的时间内该服务的线程资源会被消耗殆尽，最终会导致该服务不可用。由于服务之间的互相依赖，可能会导致调用该服务的其他服务不可用，从而导致整个系统都不可用。这就是雪崩效应

​		为了防止雪崩事件的发生，分布式系统使用了熔断机制（常用的是 Hystrix 和 阿里的 Sentinel）

​		熔断机制指：在 服务A 请求 服务B 出现的失败请求次数超过设置的阈值后，此时 服务B 会开启熔断器，这个时候服务B不再执行任何业务逻辑，执行快速的失败直接返回请求失败的信息。服务A此时能够收到服务B的响应

​		同时熔断器还有个自我修复的机制：服务B熔断后，经过一段时间后，会半打开熔断器；半打开的熔断器会检查一部分请求是否正常（其他请求执行快速失败），检查的请求如果响应正常，这个时候就会判断服务B已经正常了，那么此时会关闭熔断器



### 微服务的优缺点

- 优点：
  1. 将复杂的业务拆分成若干小业务，使复杂问题简单化
  2. 服务和服务之间没有任何的耦合；可以根据业务进行再拆分服务，具有很强的横向扩展能力；如果某个服务并发量剧增，这个时候就可以适当的加多一些机器去部署该服务，从而增加系统的负载能力
  3. 每个微服务都是独立部署的，单个服务的修改或者部署对于其他服务是没有影响的，从而大大减少了测试和部署的时间



- 缺点：
  1. 因为服务和服务之间是互相依赖的，所以如果一个服务修改了一些内容对其他服务有影响的情况下，就会产生很多问题了。微服务之间的依赖会加大微服务系统的复杂性
  2. 分布式事务
  3. 服务的划分（微服务的拆分理念：领域驱动设计）
  4. 服务的部署：微服务系统需要对每个服务进行治理、监控和管理等，而每个服务有大量的配置，还需要考虑服务的启动顺序和启动时机等问题（容器编排、Jenkins流水线部署）



## SpringCloud

### 简介

​		SpringCloud是基于SpringBoot的。**<font color=red>旨在通过一系列的开发组件和框架帮助开发者快速搭建一个分布式的微服务系统</font>**

​		SpringCloud 提供了开发分布式微服务系统的一些常用组件，例如：服务注册和发现、配置中心、熔断器、智能路由、微代理、控制总线、全局锁、分布式会话等

>  		SpringBoot 简化了开发和部署的流程，简化了Spring复杂的配置和依赖管理，通过起步依赖和内置Servlet容器能够使开发者快速搭建一个Web工程



### 主要功能

#### 服务的注册和发现

​		由于一个微服务系统的服务粒度比较小，服务数量众多，服务之间的相互依赖成网状；所以就需要服务注册中心来统一管理微服务实例，方便查看每一个微服务实例的健康状态

​		服务注册是指向服务注册中心注册一个服务实例，服务提供者将自己的服务信息（服务名，IP地址等）告知服务注册中心。服务发现是指服务注册中心能够告知服务消费者所要消费的服务的实例信息

​		通常一个服务既是服务提供者，也是服务的消费者。一般使用HTTP协议或者消息组件的轻量级的通信机制来进行数据的交互

​		服务注册中心会提供服务的健康检查方案，检查被注册的服务是否可用。通常一个服务实例注册后，会定时向服务注册中心发送 “心跳” 包，以表示自己还处于可用状态。当服务停止提供 “心跳” 一段时间后，服务注册中心会认为该服务实例不可用，从而在服务的注册列表中将其剔除。如果后续该服务向注册中心发送 “心跳” ，那么注册中心会再次将其加入到注册列表中



#### 服务的负载均衡

​		为了保证服务的高可用，服务通常是集群化部署的。所以服务消费者调用服务提供者的时候，就需要用到负载均衡的机制了



#### 服务的容错

参考雪崩效应的问题

熔断机制除了能够有效防止雪崩效应的问题，同时还具备：

1. 将资源进行隔离。隔离出现问题的API，执行快速失败的逻辑
2. 服务的降级处理。大量请求短时间内达到服务提供方，此时打开熔断器，将服务降级处理，返回自定义好的内容（不执行复杂的业务逻辑），以免服务器因负载过高而出现故障
3. 自我修复能力。熔断器的自动启动和关闭



#### 服务网关

​		微服务系统通过将资源以 API 的形式暴露给外界来提供服务。API 接口资源通过由服务网关统一暴露（也称为API网关），内部服务不直接对外提供服务。这样能将内部的服务隐藏秋来，一定程度上保护了微服务系统的安全

​		API网关通常有请求转发的作用，另外也可能需要负责一定的安全验证，如判断某个请求是否合法等。通常到达服务网关层之前，会经过负载均衡层（Nginx双机热备，配置路由转发规则），将请求转发到网关层，然后在经过一系列的用户身份校验，权限判断，最终达到具体的服务。具体的服务会经过一定的业务逻辑运算和数据操作后将响应结果返回给用户

网关的好处：

1. 将所有服务的 API 接口资源统一聚合，保护接口防止被外界直接调用
2. 网关可以做一些用户身份验证、权限验证，防止非法请求操作 API 接口
3. 网关可以实现监控功能，实时日志输出，对请求进行记录
4. 可以做流量监控，在高流量的情况下，对服务进行降级



#### 服务配置的统一管理

​		随着实际开发的深入和周期迭代，每个服务都会有大量的配置文件，并且在不同环境是使用不同的配置文件的。所以在配置文件上的管理也是非常必要的



#### 服务链路追踪

​		微服务系统是一个分布式架构的系统，微服务系统按照业务划分服务单元。由于服务单元的数量很多且业务复杂，服务与服务之间的调用也可能非常复杂，一旦出现异常和错误，就会很难定位到问题。所以就需要分布式链路追踪，去跟进一个请求究竟有哪些服务进行了参与，包括参考的顺序等，从而使整个请求连续清晰可见，这样就很容易能够定位到问题

​		常用的分布式链路追踪组件有：谷歌的Dapper、推特的Zipkin、阿里的Eagleeye



### 常用组件

1. <font color=blue>服务注册中心和发现组件：Eureka</font>（同时也支持 Consul 和 Zookeeper）

2. <font color=blue>熔断组件：Hystrix</font>

   - Hystrix 是一个熔断组件，它除了提供一些基本的熔断器功能外，还能够实现服务降级、服务限流的功能。另外它还提供了熔断器的健康监测，以及健康数据的API接口
   - Hystrix Dashboard 组件提供了单个服务熔断器的健康状态数据的界面展示功能
   - Hystrix Turbine 组件提供了多个服务的熔断器的健康状态数据的界面展示功能

3. <font color=blue>客户端负载均衡组件：Ribbon</font>

   - 它通常和 Eureka、Zuul、Feign 和 RestTemplate 一起使用；可以根据不同的负载均衡策略将请求分配到不同的服务实例中

4. <font color=blue>声明式远程调用组件：Feign</font>

5. <font color=blue>路由网关：Zuul</font>

   - Zuul 具有智能路由和过滤的功能。其过滤功能是通过拦截请求来实现的，可以对用户的角色和权限进行判断，起到安全验证的作用。同时还可以输出实时的请求日志

   > Eureka、Hystrix、Ribbon和Zuul都是Netflix公司开源的组件，也被称为Spring Cloud Netflix

6. 统一配置管理 SpringCloud Config，通常和消息总线组件SpringCloud Bus（用于动态刷新服务配置）配合使用

7. <font color=blue>分布式链路追踪组件：SpringCloud Sleuth</font>

   - 它封装了Dapper、Zipkin 和 Kibana等组件，通过它可以知道服务之间的相互依赖关系，并实时观察链路的调用情况

8. <font color=blue>安全模块组件：SpringCloud Security</font>

   - 它是对 Spring Security 的封装，通常配合OAuth2一起使用，通过搭建授权服务，验证Token或者JWT的形式对整个微服务系统进行安全验证



### SpringCloud和Dubbo的比较

Dubbo致力于提供高性能和透明化的RPC远程服务调用方案，以及SOA服务治理解决方案，核心内容主要有：

- RPC 远程调用：封装长连接的NIO框架，如Netty、Mina等；并且采用多线程的模式
- 集群容错：提供基于接口方法的远程调用功能，并实现了负载均衡策略，失败容错策略等功能
- 服务发现：继承ZK组件，用于服务的发现和注册

| 微服务关注点   | SrpingCloud    | Dubbo |
| -------------- | -------------- | ----- |
| 服务注册和发现 | Eureka、Consul | Zk    |
| 负载均衡       | Ribbon         | 自带  |
| 网关           | Zuul           | —     |
| 容错           | Hystrix        | —     |
| 分布式链路追踪 | Sleuth         | —     |
| 通信方式       | HTTP、消息组件 | RPC   |
| 安全模块       | Security       | —     |
| 配置管理       | Config         | —     |



### SpringCloud和K8s的比较

​		Kubernetes是一个容器集群管理系统，为容器化的应用进程提供部署运行、维护、扩展、资源调度、服务发现等功能

K8s提供的功能：

- 自动包装：根据程序自身的资源需求和一些其他方面的需求来自动配置容器。K8s能够最大化的利用机器的工作负载，提高资源的利用率
- 自我修复：容器失败自动重启，当节点处于 “死机” 的状态，它会被替代并重新编排；当容器达到用户设定的无响应的阈值时，它会被剔除，并且不让其他容器调用它，直至它恢复服务
- 横向扩展：可以根据机器的CPU的使用率来调整容器的数量（只需要开发人员输入几个命令）
- 服务发现和负载均衡：K8s为容器提供了一个虚拟网络环境，每个容器拥有独立的IP地址和DNS名称，容器之间实现了负载均衡
- 自动部署和回滚：K8s支撑滚动更新模式，能逐步替换掉当前环境的应用程序和配置，同时监视应用程序运行状况，以确保不会同时杀死所有实例。如果出现问题，K8s支持回滚更改
- 配置管理：部署和更新应用程序的配置，不需要重新打包镜像，并且不需要在堆栈中暴露配置
- 存储编排：自动安装所选择的存储系统，无论是本地存储、公共云提供商还是网络存储系统

| 微服务关注点   | SrpingCloud    | K8s                     |
| -------------- | -------------- | ----------------------- |
| 服务注册和发现 | Eureka、Consul | Kubernetes Services     |
| 负载均衡       | Ribbon         | Kubernetes Services     |
| 网关           | Zuul           | Kubernetes Services     |
| 容错           | Hystrix        | Kubernetes Health Check |
| 分布式链路追踪 | Sleuth         | Open tracing            |



## SpringBoot

​		Springboot 和 SpringCloud 都是由 Pivotal 团队开发的 Spring 框架

​		SpringBoot 旨在简化配置，致力于快速开发。它提供了自动配置和起步依赖，使开发人员不需要配置各种XML文件，极大地提高开发的速度

Springboot 两大特点：自动配置和起步依赖

- 自动配置：在 pom 中引入一个组件，Springboot 能够自动的引入该组件默认配置的Bean
- 起步依赖：就是 `spring-boot-starter-xxx` 的 POM 文件配置



## Eureka-服务注册与发现

### 简介

Eureka 是用于服务注册和发现的组件。分为 Eureka Server 和 Eureka Client

- Eureka Server ：服务注册中心，提供服务注册和发现的功能

- Eureka Client：客户端，有两种形态，一种是服务的提供者，提供服务；一种是服务的消费者，消费服务

  > ​		服务的提供者注册到Eureka Server上，将服务名和IP地址等信息提交给服务注册中心；服务的消费者也同样的注册到服务注册中心，通过获取注册中心返回的服务注册列表去调用相应的服务



### 注册中心相关概念

1. <font color=blue>服务注册：</font>当客户端向服务注册中心注册时，客户端会提供自身的元数据，比如IP地址、端口、运行状况指标的URL、主页地址等信息

2. <font color=blue>服务续约：</font>

   - 客户端默认情况下会每隔30秒向注册中心发送一次心跳来进行服务的续约；通过心跳来告知注册中心，该服务仍然可用

3. <font color=blue>获取服务注册列表信息：</font>

   - 客户端会从注册中心获取服务注册表信息，并且将其缓存到本地
   - 客户端会使用服务注册列表的信息查找其他服务的信息，从而进行远程调用
   - 该注册列表信息定时（默认每30秒）更新一次，客户端会自行整理跟本地缓存不相同的信息
   - Eureka Server 缓存了所有服务注册列表信息，并将整个注册列表已经每个应用程序的信息进行了压缩。默认情况下，客户端和注册中心获取该信息的交互方式是 JSON 格式的

4. <font color=blue>服务下线：</font>客户端在程序关闭时**可以**向注册中心发送下线请求。Eureka Server 会将该实例的信息从注册列表信息中删除

   > 注意：服务下线不会自动调用，需要在程序关闭时显示调用：`DiscoveryManager.getInstance().shutdownComponent();`

5. <font color=blue>服务剔除：</font>

   - 默认情况下如果注册中心在90秒内没有收到客户端的心跳，那么注册中心就会将客户端从注册列表中删除



### 客户端获取服务实例

1. 客户端的注册延迟
   - 客户端启动后，不是立即向注册中心注册的，而是有一个延迟向服务端注册的时间。这个默认的延迟时间为40秒
2. 注册中心的响应缓存
   - 注册中心维护每30秒更新一次响应缓存，所以即使是刚注册的实例，也不会立即出现在服务注册列表中；可以通过配置更改时间 `eureka.server.response-cache-update-interval-ms=30*1000` ，单位是毫秒
3. 客户端的缓存
   - 客户端保存注册表信息的缓存。这个缓存每30秒更新一次。所以客户端刷新本地缓存并发现其他新注册的实例可能需要至少30秒的时间
4. Ribbon的缓存
   - Ribbon从本地客户端获取服务注册列表信息。Ribbon本身还维护了缓存，以避免每个请求都需要从客户端获取注册列表信息
   - Ribbon的注册列表信息的缓存时间是30秒，可以通过配置更改时间。所以此时也至少需要30秒的时间去刷新实例的缓存



​		所以：一个新实例的注册，默认延迟40秒向注册中心进行注册。即使注册了，也不能马上就被客户端进行调用，因为客户端的注册列表信息的缓存也需要时间去进行刷新



### Eureka的自我保护模式

​		当有一个新的 Eureka Server 出现时，它会尝试从相邻的 Peer 节点（可以理解为Eureka Server集群的其他节点）获取所有服务实例注册表信息。如果从相邻的 Peer 节点获取信息出现了故障，Eureka Server 就会尝试其他的节点

​		如果Server能够成功获取所有的服务实例信息，则根据配置信息设置服务续约的阈值，在任何时间如果Server接收到的服务续约（即心跳）低于该阈值（阈值默认为15分钟内低于85%），则服务器开启自我保护模式。即不再剔除注册列表中的信息

​		这样做的好处是：如果Eureka Server因为自身网络问题而导致客户端无法续约，那么客户端的注册信息不会被剔除，从而使得客户端能够被其他服务消费

​		默认是开启自我保护模式的，关闭的配置是：`eureka.server.enable-self-preservation=false`



### Eureka配置详解

POM文件相关：

```xml
<!-- eureka server: 提供服务发现与服务注册 -->
<dependencies>
    <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-starter-netflix-eureka-server</artifactId>
    </dependency>
</dependencies>
```



YML配置文件：

#### Server端配置

```yml
# 建议不要用TAB键, 尽量使用空格键缩进
#配置服务端口
server:
  port: 8761

# 配置 Eureka Server 相关
##因为此服务作为 Eureka Server, 所以当前服务不需要注册到注册中心
##同样的也不需要获取 Eureka Server 上的注册列表信息
eureka:
  instance:
    hostname: localhost
    #多久没收到心跳就剔除服务的时间配置, 默认90秒
    lease-expiration-duration-in-seconds: 90
  server:
    enable-self-preservation: false # 关闭自我保护模式(默认为打开)
    eviction-interval-timer-in-ms: 1000 # 扫描失效服务的间隔时间(默认为60*1000ms)  
  client:
    register-with-eureka: false #是否注册到 Eureka Server
    fetch-registry: false #是否需要发现服务注册列表信息
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



#### client端配置

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

#ribbon的超时时间, ribbon的一些配置在通用配置 CommonClientConfigKey 类中
ribbon:
  ReadTimeout: 3000
  ConnectTimeout: 3000
  MaxAutoRetries: 1 #同一台实例最大重试次数,不包括首次调用
  MaxAutoRetriesNextServer: 1 #重试负载均衡其他的实例最大重试次数,不包括首次调用
  OkToRetryOnAllOperations: false  #是否所有操作都重试
  ServerListRefreshInterval: 30000 #设置Ribbon缓存的时间,默认30s

```



注解配置：

​		spring cloud中 Discovery Service有许多种实现（eureka、consul、zookeeper等等）

- @EnableDiscoveryClient 基于 spring-cloud-commons

- @EnableEurekaClient 基于 spring-cloud-netflix；该注解只适用于Eureka作为注册中心





## Ribbon负载均衡

### 简介

负载均衡是指将负载分摊到多个执行单元上，常见的负载均衡有两种方式：

- 独立进程单元，通过负载均衡策略，将请求转发到不同的执行单元上；如Nginx
- 将负载均衡逻辑以代码的形式封装到服务消费者的客户端上，服务消费者客户端维护了一份服务提供者的信息列表，通过负载均衡策略将请求分摊给多个服务提供者，从而达到负载均衡的目的



​		Ribbon就是将负载均衡的逻辑封装到 Eureka Client 中，并且运行在客户端的进程里。Ribbon可以和RestTemplate或者Feign结合使用

### Ribbon配置类

1. `RibbonLoadBalancerClient` 是处理负载均衡的请求的
2. 通过 `ILoadBalancer` 来保存和获取 Server 注册列表信息的，具体使用的是实现类：`DynamicServerListLoadBalancer`
3. `DynamicServerListLoadBalancer` 类配置：
   - `IClientConfig` ：用于配置负载均衡的客户端
   - `IRule`：用于配置负载均衡的策略
   - `IPing`：用于向 Server 发送 “ping” 来判断该 server 是否有响应，从而判断server 是否可用
   - `ServerList`：定义获取所有server的注册列表信息的接口
   - `ServerListFilter`：该接口定义了可根据配置去过滤或者特性动态地获取符合条件的server列表的方法



### IRule负载均衡策略

```java
public interface IRule{
    /**
 	  * 根据key获取server实例
      */
    public Server choose(Object key);
    
    /**
 	  * 设置ILoadBalancer
      */
    public void setLoadBalancer(ILoadBalancer lb);
    
    /**
 	  * 获取ILoadBalancer
      */
    public ILoadBalancer getLoadBalancer();    
}
```



IRule默认的实现类：（默认使用轮询）

- `RoundRobinRule`：轮询
- `BestAvailableRule`：选择最小请求数
- `RandomRule`：随机策略
- `RetryRule`：根据轮询的策略，但是带有重试机制
- `WeightedResponseTimeRule`：根据响应时间去分配一个权重，根据权重去分配请求
- `ZoneAvoidanceRule`：根据server和zone区域和可用性来轮询选择



### 自定义负载均衡算法

> 自定义算法：每个服务调用3次后再进行轮询

```java
public class MyRule extends AbstractLoadBalancerRule {

    private static int total = 0;
    private static int currentIndex = 0;

    public Server choose(ILoadBalancer lb, Object key) {
        if (lb == null) {
            return null;
        }
        Server server = null;

        while (server == null) {
            if (Thread.interrupted()) {
                return null;
            }
            List<Server> upList = lb.getReachableServers();
            List<Server> allList = lb.getAllServers();

            int serverCount = allList.size();
            if (serverCount == 0) {
                return null;
            }

            int index = currentIndex;
            server = upList.get(index);

            if (server == null) {
                Thread.yield();
                continue;
            }

            if (total < 2) {
                //如果轮询小于三次,那么继续访问之前的server
                total++;
            } else {
                //当前下标+1
                currentIndex++;
                if (currentIndex >= serverCount) {
                    //重新开始轮询
                    currentIndex = 0;
                }
                //将访问次数置为0
                total = 0;
            }

            if (server.isAlive()) {
                return (server);
            }
            server = null;
            Thread.yield();
        }
        return server;
    }

    @Override
    public Server choose(Object key) {
        return choose(getLoadBalancer(), key);
    }

    @Override
    public void initWithNiwsConfig(IClientConfig clientConfig) {
        // TODO Auto-generated method stub
    }
}
```



自定义实现类的应用

```java
@SpringBootApplication
@EnableEurekaClient
@RibbonClient(name = "eureka-client", configuration = MyRule.class)
public class EurekaClientApplication {
    public static void main(String[] args) {
        SpringApplication.run(EurekaClientApplication.class,args);
    }
}
```



## Feign声明式调用

### 简介		

​		Feign 是采用了声明式API接口的风格，将HTTP客户端绑定到其内部，使客户端调用过程变得简单。Feign是声明式、模板化的HTTP客户端，可以帮助我们更加便捷、优雅地调用HTTP API

​		Feign通过处理注解生成Request模板，从而简化HTTP API的开发；在发送Request请求之前，Feign 通过处理注解的方式替换掉Request模板中的参数，生成真正的 Request ，并交给 Java Http 客户端去处理。开发者只需要关注Feign的注解模板开发，而不用去关注Http请求本身

- Feign 采用的是基于接口的注解；Feign通过接口方法调用REST服务，在Eureka中查找对应的服务
- Feign 整合了Ribbon，具有负载均衡的能力
- 整合了Hystrix，具有熔断的能力



### 小结

1. 当Feign接口的方法被调用时，通过JDK代理来生成具体的 RequestTemplate 模板对象；然后根据 RequestTemplate 再生成 HTTP 请求的 Request 对象
2. 客户端默认的网络请求框架是：`HttpURLConnection`；可以替换为 HttpClient 或者 OkHttp
3. Feign 是通过 `LoadBalanceClient` 类来结合 Ribbon 做负载均衡的



## Hystrix熔断器

### 简介

​		Hystrix是一个用于处理分布式系统延迟和容错的开源库。分布式系统中，避免不了服务间的调用失败，比如超时，异常等。Hystrix能保证在出现问题的时候，不会导致整体服务失败，**避免级联故障**，以提高分布式系统的弹性

​		当系统中异常发生时，断路器给调用返回一个符合预期的，可处理的FallBack，这样就可以避免长时间无响应或抛出异常，使故障不能再系统中蔓延，造成雪崩

​		熔断机制的原理是：<font color=red>**服务调用方**</font>可以自己进行判断某些服务反应慢或者存在大量超时的情况时，能够主动熔断，防止整个系统被拖垮（<font color=blue>注意：熔断器是在服务的调用者这边，而不是在服务的提供者中</font>）



### Hystrix的设计原则

1. 防止单个服务的故障耗尽整个服务的 Servlet 容器的线程资源（如Tomcat）
2. 快速失败机制，如果某个服务出现了故障，则调用该服务的请求快速失败，而不是线程等待
3. 提供回退方案（fallback），在请求发生故障时，提供设定好的回退方案
   - 回退方案也就是降级的处理，请求发生故障或者熔断器打开的时候，就可以执行 fallback 的快速失败
4. 使用熔断机制，防止故障扩散到其他服务
5. 提供熔断器的监控组件（Dashboard），可以实时监控熔断器的状态



