````xml
<!-- spring cloud parent -->
<properties>
    <spring-cloud.version>Greenwich.RELEASE</spring-cloud.version>
</properties>

<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-dependencies</artifactId>
            <version>${spring-cloud.version}</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>
````

## Eureka

### 什么是Eureka	

​	Eureka是一种基于REST（Representational State Transfer）的服务，主要用于定位服务，以实现中间层服务器的负载平衡和故障转移；我们将此服务称为Eureka Server。

​	Eureka还附带了一个基于Java的客户端组件Eureka Client，它使与服务的交互变得更加容易。客户端还有一个内置的负载均衡器，可以进行基本的循环负载均衡。

​	在Netflix，一个更复杂的负载均衡器包含Eureka基于流量，资源使用，错误条件等多种因素提供加权负载平衡，以提供卓越的弹性。

### Eureka治理机制

1. 服务提供者
   - **服务注册：**启动的时候会通过发送REST请求的方式将**自己注册到Eureka Server上**，同时带上了自身服务的一些元数据信息
   - **服务续约：**在注册完服务之后，**服务提供者会维护一个心跳**用来持续告诉Eureka Server:  "我还活着 ” 
   - **服务下线：**当服务实例进行正常的关闭操作时，它会**触发一个服务下线的REST请求**给Eureka Server, 告诉服务注册中心：“我要下线了 ”

2. 服务消费者
   - **获取服务：**当我们**启动服务消费者**的时候，它会发送一个REST请求给服务注册中心，来获取上面注册的服务清单
   - **服务调用：**服务消费者在获取服务清单后，通过**服务名**可以获得具体提供服务的实例名和该实例的元数据信息。在进行服务调用的时候，**优先访问同处一个Zone中的服务提供方**

3. Eureka Server(服务注册中心)
   - **失效剔除：**默认每隔一段时间（默认为60秒） 将当前清单中超时（默认为90秒）**没有续约的服务剔除出去**
   - **自我保护：**EurekaServer 在运行期间，会统计心跳失败的比例在15分钟之内是否低于85%(通常由于网络不稳定导致)。 Eureka Server会将当前的**实例注册信息保护起来**， 让这些实例不会过期，尽可能**保护这些注册信息**



> spring cloud中discovery service有许多种实现（eureka、consul、zookeeper等等）
>
> @EnableDiscoveryClient基于spring-cloud-commons
>
> @EnableEurekaClient基于spring-cloud-netflix
>
> ​	 其实用更简单的话来说，@EnableEurekaClient只适用于Eureka作为注册中心，如果是其他的注册中心，那么推荐使用@EnableDiscoveryClient

### Eureka配置使用

1. pom

   ```xml
   <!-- eureka server: 提供服务发现与服务注册 -->
   <dependencies>
       <dependency>
           <groupId>org.springframework.cloud</groupId>
           <artifactId>spring-cloud-starter-netflix-eureka-server</artifactId>
       </dependency>
   </dependencies>
   ```

2. properties

   ````yml
   spring:
     application:
       name: dh_eureka
     profiles: dev
   server:
     port: 7001
   eureka:
     instance:
       hostname: localhost
     client:
       fetch-registry: false
       register-with-eureka: false
       serviceUrl:
         defaultZone: http://localhost:7001/eureka
   ````

3. 启动类

   ````java
   @SpringBootApplication
   @EnableEurekaServer
   public class DaiEuerkaApplication {
       public static void main(String[] args) {
           SpringApplication.run(DaiEuerkaApplication.class, args);
       }
   }
   ````



## Ribbon

​	Spring Cloud Ribbon是基于Netflix Ribbon实现的一套**客户端负载均衡**的工具。Ribbon会自动帮助你基于某种规则（简单轮询、随机连接等），也可以实现自定义的负载均衡算法

### 负载均衡

- Load Balance，微服务或分布式集群中常用的一种应用
- 简单来说负载均衡就是将用户的请求ping平摊的分配到多个任务上，从而是系统达到HA（高可用）
- 两种负载均衡：
  1. 集中式LB：偏硬件，服务的消费方和提供方之间使用独立的LB设施，由该设施负责把访问请求以某种策略转发至服务的提供方。
  2. 进程内LB：骗软件， 将LB逻辑集成到消费方，消费方从服务注册中心指导哪些地址可用，再自己选择一个合适的服务器。

### Ribbon配置使用

1. pom

   ````xml
   <dependencies>
       <dependency>
           <groupId>org.springframework.cloud</groupId>
           <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
       </dependency>
       <dependency>
           <groupId>org.springframework.boot</groupId>
           <artifactId>spring-boot-starter-web</artifactId>
       </dependency>
       <dependency>
           <groupId>org.springframework.cloud</groupId>
           <artifactId>spring-cloud-starter-netflix-ribbon</artifactId>
       </dependency>
   </dependencies>
   ````

2. properties

   ````yml
   eureka:
     client:
       serviceUrl:
         defaultZone: http://localhost:7001/eureka/
   server:
     port: 8010
   spring:
     application:
       name: dai-ribbon
   ````

3. 启动类和配置类

   ````java
   @SpringBootApplication
   @EnableEurekaClient
   public class CloudComsumerApplication {
   	public static void main(String[] args) {
   		SpringApplication.run(CloudComsumerApplication.class, args);
   	}
   }
   ````

   ```java
   @Configuration
   public class ConfigBean {
       @Bean
       @LoadBalanced
       //加上这个注解开启Ribbon的负载均衡机制,默认是轮询方式访问对应微服务
       public RestTemplate restTemplate(){
           return new RestTemplate();
       }
   
       @Bean
       public IRule getMyRule(){
           //轮询算法
           new RoundRobinRule();
           //根据平均响应时间计算服务的权重。
           // 统计信息不足时会按照轮询，统计信息足够会按照响应的时间选择服务
           new WeightedResponseTimeRule();
           //正常时按照轮询选择服务
           // 若过程中有服务出现故障，在轮询一定次数后依然故障，则会跳过故障的服务继续轮询
           new RetryRule();
           //随机算法
           return new RandomRule();
       }
   }
   ```

4. 使用自定义的负载均衡算法

   ```java
   @SpringBootApplication
   @EnableEurekaClient
   //在启动该微服务的时候就能去加载我们的自定义Ribbon配置类(自定义规则的负载均衡算法),从而使配置生效
   //注意：这里传入的是 configuration
   //注意：MyRuleConfig不能放在和启动器同一个包下
   //指定需要用到负载均衡的微服务名及自定义算法的class对象
   @RibbonClient(name = "CLOUD-PROVIDER", configuration = MyRuleConfig.class)
   public class CloudComsumerApplication {
   
   	public static void main(String[] args) {
   		SpringApplication.run(CloudComsumerApplication.class, args);
   	}
   }
   ```

   ```java
   @Configuration
   public class MyRuleConfig {
       @Bean
       public IRule getMyRule(){
           return new MyRule();
       }
   }
   ```

   ````java
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
               // Shouldn't actually happen.. but must be transient or a bug.
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
       public void initWithNiwsConfig(IClientConfig clientConfig) {}
   }
   ````

## Feign

​	Feign是一个声明式的伪Http客户端，使用方法时定义一个接口并在上面添加注解即可。Feign支持可拔插式的编码器和解码器。Spring Cloud对Feign进行了封装，使其支持SpringMVC和HttpMessageConverters。Feign可以与Eureka和Ribbon组合使用以支持负载均衡。

​	<font color=red>Feign用于**微服务之间**的远程调用</font>，基于HHTP；类似于dubbo中服务间的RPC远程调用

- Feign 采用的是基于接口的注解；Feign通过接口方法调用REST服务，**在Eureka中查找对应的服务**
- Feign 整合了Ribbon，具有负载均衡的能力
- 整合了Hystrix，具有熔断的能力

### Feign的使用和配置

1. pom

   ````xml
   <dependency>
       <groupId>org.springframework.boot</groupId>
       <artifactId>spring-boot-starter-web</artifactId>
   </dependency>
   <dependency>
       <groupId>org.springframework.cloud</groupId>
       <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
   </dependency>
   <dependency>
       <groupId>org.springframework.cloud</groupId>
       <artifactId>spring-cloud-starter-netflix-ribbon</artifactId>
   </dependency>
   <!--feign-->
   <dependency>
       <groupId>org.springframework.cloud</groupId>
       <artifactId>spring-cloud-starter-openfeign</artifactId>
   </dependency>
   ````

2. properties

   ````yml
   server:
     port: 8011
   eureka:
     client:
       register-with-eureka: false
       serviceUrl:
         defaultZone: http://localhost:7001/eureka
   feign:
     hystrix:
   #   开启断路器  
       enabled: true
   management:
     endpoints:
       web:
         exposure:
           include: "*"
   ````

3. 启动类

   ```java
   @SpringBootApplication
   @EnableEurekaClient
   @EnableFeignClients(basePackages = "dai.cloud", defaultConfiguration = ConfigBean.class)
   public class CloudComsumerFeignApplication {
   
   	public static void main(String[] args) {
   		SpringApplication.run(CloudComsumerFeignApplication.class, args);
   	}
   }
   ```

   ```java
   @Configuration
   public class ConfigBean {
       @Bean
       @LoadBalanced
       public RestTemplate restTemplate(){
           return new RestTemplate();
       }
       @Bean
       public IRule getMyRule(){
           //轮询算法
           return new RoundRobinRule();
       }
   }
   ```

   ```java
   /**
    * Feign是面向接口编程的声明式web服务客户端
    */
   @FeignClient(name = "CLOUD-PROVIDER")
   public interface SellerService {
   
       @GetMapping(value = "/seller")
       public Seller getSeller();
   }
   ```

   


## Hystrix

​	Hystrix是一个用于处理分布式系统延迟和容错的开源库。分布式系统中，依赖避免不了调用失败，比如超时，异常等。Hystrix能保证在出现问题的时候，不会导致整体服务失败，避免级联故障，以提高分布式系统的弹性。

​	当系统中异常发生时，断路器给调用返回一个符合预期的，可处理的FallBack，这样就可以避免长时间无响应或抛出异常，使故障不能再系统中蔓延，造成雪崩。

### 服务熔断

#### 服务熔断是什么？

了解服务熔断，需要先知道微服务中的雪崩效应

##### 雪崩效应

​        假设微服务A调用微服务B和微服务C，微服务B和微服务C又调用其它的微服务，这就是所谓的“扇出”。如果扇出的链路上某个微服务的调用响应时间过长或者不可用，对微服务A的调用就会占用越来越多的系统资源，进而引起系统崩溃

##### 熔断机制

熔断机制是应对雪崩效应的一种微服务***链路保护机制***。(一般存在于服务端)

- 例如：高压电路中，如果某个地方的电压过高，熔断器就会熔断，对电路进行保护。

​        在微服务架构中，当扇出链路的某个微服务***不可用或者响应时间太长***时，会进行服务的降级，进而熔断该节点微服务的调用，**快速返回错误的响应信息**。当检测到该节点微服务调用响应正常后，恢复调用链路。

- Hystrix会监控微服务间调用的状况，当失败的调用到一定阈值，缺省是5秒内20次调用失败，就会启动熔断机制。

### 服务降级

#### 什么是服务降级

- 当系统整体资源快不够的时候，忍痛将部分服务暂时关闭，带渡过难关后，再重新开启。
- 降级处理时在***客户端***完成的，与服务端没有关系
- 所谓降级，一般是从 **整体负荷** 考虑，当某个服务熔断之后，服务器将不再被调用，此时客户端可以自己准备一个本地的FallBack回调，返回一个缺省值。这样做虽然服务水平下降，但好歹可用，比直接挂掉好

#### 服务降级与服务熔断的相同之处

- 目的很一致，都是从**可用性可靠性**着想，为防止系统的整体缓慢甚至崩溃，采用的技术手段；
- 最终表现类似，对于两者来说，最终让用户体验到的是某些功能暂时不可达或不可用；
- 粒度一般都是服务级别，当然也有不少更细粒度的做法，比如做到数据持久层（允许查询，不允许增删改）；
- 自治性要求很高，熔断模式一般都是服务基于策略的自动触发，降级虽说可人工干预，但在微服务架构下，完全靠人显然不可能，开关预置、配置中心都是必要手段；

#### 服务降级和服务熔断的区别

- 触发原因不太一样，服务熔断一般是某个服务（下游服务）**故障**引起，而服务降级一般是从***整体负荷考虑***； 
- 实现方式不太一样；服务降级具有**代码侵入性(由控制器完成/或自动降级)**，熔断一般称为**自我熔断**。
- 管理目标的层次不太一样，熔断其实是一个框架级的处理，每个微服务都需要（无层级之分），而降级一般需要对业务有层级之分（比如降级一般是从最外围服务开始） 

### 服务限流

#### 限流的目的

​        通过对**并发访问/请求**进行限速或者一个时间窗口内的的请求进行限速来保护系统，一旦达到限制速率则可以拒绝服务（定向到错误页或告知资源没有了）、排队或等待（比如秒杀、评论、下单）、降级（返回兜底数据或默认数据，如商品详情页库存默认有货）。

#### 限流的常见方式

- 限制总并发数（比如数据库连接池、线程池）
- 限制瞬时并发数（如nginx的limit_conn模块，用来限制瞬时并发连接数）
- 限制时间窗口内的平均速率（如Guava的RateLimiter、nginx的limit_req模块，限制每秒的平均速率）
- 限制远程接口调用速率
- 限制MQ的消费速率 等等

#### 限流算法

1. 令牌桶算法
2. 漏桶算法
3. 计数器算法
4. 滑动窗口算法

## Zuul GateWay

### Zuul简介

​	**Zuul的主要功能是路由转发和过滤器。路由功能是微服务的一部分**，比如／api/user转发到到user服务，/api/shop转发到到shop服务。zuul默认和Ribbon结合实现了负载均衡的功能。

​	Zuul是从设备和网站到Netflix流应用程序后端的所有请求的前门。(外部请求到达后端接口的过滤器)；作为边缘服务应用程序，Zuul旨在实现动态路由，监控，弹性和安全性。

>  在Spring Cloud微服务系统中，一种常见的负载均衡方式是，客户端的请求首先经过负载均衡（zuul、Ngnix），再到达服务网关（zuul集群），然后再到具体的服务，服务统一注册到高可用的服务注册中心集群

### Zuul使用一系列的过滤器，可带来以下功能

- 身份验证和安全性 - 识别每个资源的身份验证要求并拒绝不满足这些要求的请求。
- 洞察和监控 - 在边缘跟踪有意义的数据和统计数据，以便为我们提供准确的生产视图。
- 动态路由 - 根据需要动态地将请求路由到不同的后端群集。
- 压力测试 - 逐渐增加群集的流量以衡量性能。
- Load Shedding - 为每种类型的请求分配容量并删除超过限制的请求。
- 静态响应处理 - 直接在边缘构建一些响应，而不是将它们转发到内部集群
- 多区域弹性 - 跨AWS区域路由请求，以使我们的ELB使用多样化，并使我们的优势更接近我们的成员

### Zuul和Feign的区别

1. zuul作为整个应用的流量入口，接收所有的请求，并且将不同的请求转发至不同的处理微服务模块，其作用可视为nginx
2. feign则是将当前微服务的部分服务接口暴露出来，并且主要用于各个微服务之间的服务调用。两者的应用层次以及原理均不相同
3. zuul也含有hystrix和ribbon，基于http通讯的，可以直接代理服务就行。在它和服务间增加feign只会增加通讯消耗，没有特别的意义。**feign在服务互相调用**的时候用就行了，可以仿rpc通讯。
4. <font color=red>**Feign主要作客户端流控**</font>，Feign的负载均衡是基于Eureka实现的
5. <font color=red>**Zuul主要作服务端流控**</font>，并且Zuul的负载均衡结合Eureka实现易用性较好，并且Zuul一般用在对第三方提供访问接口

> 即是zuul是在provider端，feign是在consumer端。如果直接提供provider接口给外部调用则用zuul拦截，如果是微服务模块调用微服务模块则用feign