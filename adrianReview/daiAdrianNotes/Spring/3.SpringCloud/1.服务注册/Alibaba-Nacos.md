## Nacos简介

英文文档：`https://spring-cloud-alibaba-group.github.io/github-pages/greenwich/spring-cloud-alibaba.html#_spring_cloud_alibaba_nacos_discovery`

中文文档：`https://github.com/alibaba/spring-cloud-alibaba/wiki/Nacos-discovery`



​		Nacos 可以用于发现、配置和管理微服务。Nacos 提供了一组简单易用的特性集，可以快速实现动态服务发现、服务配置、服务元数据及流量管理

​		Nacos 可以更敏捷和容易地构建、交付和管理微服务平台。 Nacos 是构建以 “服务” 为中心的现代应用架构 （例如微服务范式、云原生范式） 的服务基础设施

### Nacos 的关键特性

1. **<font color=orange>服务发现和服务健康监测</font>**

   - Nacos 支持基于 DNS 和基于 RPC 的服务发现
   - Nacos 提供对服务的实时的健康检查，阻止向不健康的主机或服务实例发送请求
   - Nacos 支持传输层 （PING 或 TCP）和应用层 （如 HTTP、MySQL、用户自定义）的健康检查。 对于复杂的云环境和网络拓扑环境中（如 VPC、边缘网络等）服务的健康检查，Nacos 提供了 agent 上报模式和服务端主动检测2种健康检查模式
   - Nacos 还提供了统一的健康检查仪表盘，帮助您根据健康状态管理服务的可用性及流量

   

2. **<font color=orange>动态配置服务</font>**

   - 动态配置服务以中心化、外部化和动态化的方式管理所有环境的应用配置和服务配置

   - 动态配置消除了配置变更时重新部署应用和服务的需要，让配置管理变得更加高效和敏捷

   - 配置中心化管理让实现无状态服务变得更简单，让服务按需弹性扩展变得更容易

     > ​		Nacos 提供了一个简洁易用的 UI 方便管理所有的服务和应用的配置
     >
     > ​		Nacos 还提供包括配置版本跟踪、金丝雀发布、一键回滚配置以及客户端配置更新状态跟踪在内的一系列开箱即用的配置管理特性，可以更安全地在生产环境中管理配置变更和降低配置变更带来的风险

     

3. **<font color=orange>动态 DNS 服务</font>**

   - 动态 DNS 服务支持权重路由，以实现中间层负载均衡、更灵活的路由策略、流量控制以及数据中心内网的简单DNS解析服务
   - 动态DNS服务还能更容易地实现以 DNS 协议为基础的服务发现，以消除耦合到厂商私有服务发现 API 上的风险

   

3. **<font color=orange>服务及其元数据管理</font>**
   - Nacos 能让使用者从微服务平台建设的视角管理数据中心的所有服务及元数据
   - 包括管理服务的描述、生命周期、服务的静态依赖分析、服务的健康状态、服务的流量管理、路由及安全策略、服务的 SLA 以及最首要的 metrics 统计数据



### Nacos 基本概念

#### 命名空间

​		用于进行租户粒度的配置隔离。不同的命名空间下，可以存在相同的 Group 或 Data ID 的配置

​		Namespace 的常用场景之一是不同环境的配置的区分隔离，例如开发测试环境和生产环境的资源（如配置、服务）隔离等



#### 元信息

​		Nacos数据（如配置和服务）描述信息，如服务版本、权重、容灾策略、负载均衡策略、鉴权配置、各种自定义标签 (label)

​		从作用范围来看，分为服务级别的元信息、集群的元信息及实例的元信息



#### Data ID（配置集ID）

​		Nacos 中的某个配置集的 ID。配置集 ID 是组织划分配置的维度之一

​		Data ID 通常用于组织划分系统的配置集。一个系统或者应用可以包含多个配置集，每个配置集都可以被一个有意义的名称标识

​		Data ID 通常采用类 Java 包（如 com.taobao.tc.refund.log.level）的命名规则保证全局唯一性。此命名规则非强制



#### 配置分组

​		Nacos 中的一组配置集，是组织配置的维度之一。通过一个有意义的字符串（如 Buy 或 Trade ）对配置集进行分组，从而区分 Data ID 相同的配置集

​		当在 Nacos 上创建一个配置时，如果未填写配置分组的名称，则配置分组的名称默认采用 `DEFAULT_GROUP` 

​		配置分组的常见场景：不同的应用或组件使用了相同的配置类型，如 database_url 配置和 MQ_topic 配置



#### 权重

实例级别的配置。权重为浮点数。权重越大，分配给该实例的流量越大



#### 健康检查

​		以指定方式检查服务下挂载的实例 (Instance) 的健康度，从而确认该实例 （Instance） 是否能提供服务

​		根据检查结果，实例 (Instance) 会被判断为健康或不健康。对服务发起解析请求时，不健康的实例 (Instance) 不会返回给客户端



#### 健康保护阈值

​		为了防止因过多实例 (Instance) 不健康导致流量全部流向健康实例 (Instance) ，继而造成流量压力把健康实例 (Instance) 压垮并形成雪崩效应，应将健康保护阈值定义为一个 0 到 1 之间的浮点数

​		当域名健康实例 (Instance) 占总服务实例 (Instance) 的比例小于该值时，无论实例 (Instance) 是否健康，都会将这个实例 (Instance) 返回给客户端。这样做虽然损失了一部分流量，但是保证了集群的剩余健康实例 (Instance) 能正常工作





## Nacos实战

[参考 Nacos 官方文档](https://spring-cloud-alibaba-group.github.io/github-pages/greenwich/spring-cloud-alibaba.html)



### 服务注册中心

```xml
 <!-- alibaba 的服务发现和注册 -->
<dependency>
	<groupId>com.alibaba.cloud</groupId>
    <artifactId>spring-cloud-starter-alibaba-nacos-discovery</artifactId>
</dependency>
```

- Nacos 引入了 Ribbon 的依赖，默认是轮询方式的负载均衡



```yml
spring:
  application:
    name: NACOS-COMSUMER
  # nacos 服务注册中心的地址
  cloud:
    nacos:
      discovery:
        server-addr: localhost:8848
```



服务中心具体配置可以在 Nacos 后台进行配置，包括服务权重，服务下线和集群配置等功能操作





### 服务配置中心

#### Springboot 配置文件

Spring Boot 中有以下两种配置文件

- bootstrap （.yml 或者 .properties）
- application （.yml 或者 .properties）



两个配置文件的主要区别是：

1. boostrap 由父 ApplicationContext 加载，比 applicaton 优先加载

2. boostrap 里面的属性不能被覆盖



​		所以在使用配置中心时，需要在 bootstrap 配置文件中添加连接到配置中心的配置属性来加载外部配置中心的配置信息



#### 配置集格式

Nacos 配置管理 `dataId` 字段需要配置 `spring.application.name` 

在 Nacos Spring Cloud 中，`dataId` 的完整格式如下：

```plain
${prefix}-${spring.profile.active}.${file-extension}
```

- <font color=orange>**prefix**</font> 
  -  默认是 `spring.application.name` 的值，也可以通过配置项 `spring.cloud.nacos.config.prefix` 来配置
- <font color=orange>**spring.profile.active**</font> 
  - 即为当前环境对应的 profile
  - 注意：当 `spring.profile.active` 为空时，对应的连接符 `-` 也将不存在，dataId 的拼接格式变成 `${prefix}.${file-extension}`
- <font color=orange>**file-exetension**</font>
  - 为配置内容的数据格式，可以通过配置项 `spring.cloud.nacos.config.file-extension` 来配置
  - 目前只支持 `properties` 和 `yaml` 类型



#### 配置自动更新

可以通过 Spring Cloud 原生注解 `@RefreshScope` 实现配置自动更新：

```java
@RestController
@RequestMapping("/config")
@RefreshScope
public class ConfigController {

    @Value("${useLocalCache:false}")
    private boolean useLocalCache;

    @RequestMapping("/get")
    public boolean get() {
        return useLocalCache;
    }
}
```



### 分类配置方案



注意：**NameSpace 和 Group 配置必须放到 bootstrap.properties 文件中**



1. <font color=red>**NameSpace 命名空间**</font>

   - 用于进行租户（即注册到Nacos的服务）粒度的配置隔离
   - 不同的命名空间下，可以存在相同的 Group 或 Data ID 的配置
   - Namespace 的常用场景之一是不同环境的配置的区分隔离，例如开发测试环境和生产环境的资源（如配置、服务）隔离等

2. <font color=red>**Group 分组**</font>

   - 用于分组服务配置。可以用于不同的服务集群进行分组配置
   - 默认使用的是 DEFAULT_GROUP

3. **<font color=red>Data Id 配置</font>**

   - 通过自定义扩展的 Data Id 配置，既可以解决多个应用间配置共享的问题，又可以支持一个应用有多个配置文件

   ```properties
   spring.application.name=opensource-service-provider
   spring.cloud.nacos.config.server-addr=127.0.0.1:8848
   
   # config external configuration
   # 1、Data Id 在默认的组 DEFAULT_GROUP,不支持配置的动态刷新
   spring.cloud.nacos.config.extension-configs[0].data-id=ext-config-common01.properties
   
   # 2、Data Id 不在默认的组，不支持动态刷新
   spring.cloud.nacos.config.extension-configs[1].data-id=ext-config-common02.properties
   spring.cloud.nacos.config.extension-configs[1].group=GLOBALE_GROUP
   
   # 3、Data Id 既不在默认的组，也支持动态刷新
   spring.cloud.nacos.config.extension-configs[2].data-id=ext-config-common03.properties
   spring.cloud.nacos.config.extension-configs[2].group=REFRESH_GROUP
   spring.cloud.nacos.config.extension-configs[2].refresh=true
   ```

   - 通过 `spring.cloud.nacos.config.extension-configs[n].data-id` 的配置方式来支持多个 Data Id 的配置。
   - 通过 `spring.cloud.nacos.config.extension-configs[n].group` 的配置方式自定义 Data Id 所在的组，不明确配置的话，默认是 DEFAULT_GROUP
   - 通过 `spring.cloud.nacos.config.extension-configs[n].refresh` 的配置方式来控制该 Data Id 在配置变更时，是否支持应用中可动态刷新， 感知到最新的配置值。默认是不支持的





## Nacos集群和持久化配置

Nacos 默认自带的是嵌入式数据库 Apache derby 

要实现集群下的持久化配置，那么就要使用到统一的外部数据库，推荐 MySQL

- 首先需要执行 Nacos 自带的SQL文件 nacos-mysql.sql

- 然后需要在 config/application.properties 文件上加上 mysql 的相关配置信息

  ```properties
  ### Connect URL of DB:
  db.url.0=jdbc:mysql://127.0.0.1:3306/nacos?characterEncoding=utf8&connectTimeout=1000&socketTimeout=3000&autoReconnect=true
  db.user=root
  db.password=123456
  ```

- 然后需要配置 cluster.conf 配置文件，配置其他nacos服务器的地址和端口（nacos自带的文件，这里就是配置 Nacos 的集群）

  ```conf
  192.168.1.1:8848
  192.168.1.2:8848
  192.168.1.3:8848
  ```

  > 注意：
  >
  > **这个IP不能写 127.0.0.1. 必须是 Linux 命令 hostname -i 能够识别的 IP**





服务注册到集群的 Nacos 时， server-addr 配置可以将多个地址用逗号分隔即可

但是推荐使用 Nginx 给集群的 Nacos 进行反向代理，然后 server-addr 配置 Nginx 对应的地址即可