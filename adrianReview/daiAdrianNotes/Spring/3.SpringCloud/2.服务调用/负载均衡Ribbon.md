## 什么是Ribbon负载均衡

负载均衡是指将负载分摊到多个执行单元上，常见的负载均衡有两种方式：

- **<font color=orange>集中式的LB：</font>**独立进程单元，通过负载均衡策略，将请求转发到不同的执行单元上；如Nginx。集中式LB 是服务方的负载均衡
- **<font color=orange>进程内LB：</font>**将负载均衡逻辑以代码的形式封装到服务消费者的客户端上，服务消费者客户端维护了一份服务提供者的信息列表，通过负载均衡策略将请求分摊给多个服务提供者，从而达到负载均衡的目的。进程内的LB 是消费方的负载均衡



​		Ribbon是Netflix发布的负载均衡器，它有助于控制HTTP和TCP客户端的行为。为Ribbon配置服务提供者地址列表后，Ribbon就可以基于某种负债均衡算法，自动地帮助服务消费者去请求。Ribbon默认为我们提供了很多的负载均衡算法，同时也支持自定义的负载均衡算法

​		Ribbon 将负载均衡的逻辑封装到 Eureka Client 中，并且运行在客户端的进程里。Ribbon可以和`RestTemplate` 或者 Feign 结合使用



## Ribbon配置类

1. `RibbonLoadBalancerClient` 是处理负载均衡的请求的
2. 通过 `ILoadBalancer` 来保存和获取 Server 注册列表信息的
   - 具体使用的是实现类：`DynamicServerListLoadBalancer`
3. `DynamicServerListLoadBalancer` 类配置：
   - `IClientConfig` ：用于配置负载均衡的客户端
   - `IRule`：用于配置负载均衡的策略
   - `IPing`：用于向 Server 发送 “ping” 来判断该 server 是否有响应，从而判断server 是否可用
   - `ServerList`：定义获取所有server的注册列表信息的接口
   - `ServerListFilter`：该接口定义了可根据配置去过滤或者特性动态地获取符合条件的server列表的方法



## IRule负载均衡策略

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



### IRule默认的实现类：

（默认使用轮询）

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



<font color=blue>**使用自定义的负载均衡算法：**</font>



```java
@SpringBootApplication
@EnableEurekaClient
//在启动该微服务的时候就能去加载我们的自定义Ribbon配置类(自定义规则的负载均衡算法),从而使配置生效
//注意：这里传入的是 configuration, 指定需要用到负载均衡的微服务名及自定义算法的class对象
//注意：MyRuleConfig 不能放在和启动器同一个包下
@RibbonClient(name = "CLOUD-PROVIDER", configuration = MyRuleConfig.class)
public class CloudComsumerApplication {

	public static void main(String[] args) {
		SpringApplication.run(CloudComsumerApplication.class, args);
	}
}
```



```java
// MyRuleConfig 不能放在和启动器同一个包下
// 需要使用别的包来放置该配置类
@Configuration
public class MyRuleConfig {
    @Bean
    public IRule getMyRule(){
        return new MyRule();
    }
}
```





## Ribbon配置

### POM文件配置

```xml
<dependencies>
    <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    <!-- 这里 Ribbon 可以不用引入, 因为 netflix-eureka-client 会自动引入Ribbon -->
    <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-starter-netflix-ribbon</artifactId>
    </dependency>
</dependencies>
```



### 启动类和配置类

```java
@SpringBootApplication
@EnableEurekaClient
public class CloudComsumerApplication {
	public static void main(String[] args) {
		SpringApplication.run(CloudComsumerApplication.class, args);
	}
}
```



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



### 配置文件

配置文件直接使用 Eureka Client 的配置即可

额外的配置：

```yml
#ribbon的超时时间, ribbon的一些配置在通用配置 CommonClientConfigKey 类中
#这里是对所有的服务都有效的
ribbon:
  ReadTimeout: 3000
  ConnectTimeout: 3000
  MaxAutoRetries: 1                #同一台实例最大重试次数,不包括首次调用
  MaxAutoRetriesNextServer: 1      #重试负载均衡其他的实例最大重试次数,不包括首次调用
  OkToRetryOnAllOperations: false  #是否所有操作都重试
  ServerListRefreshInterval: 30000 #设置Ribbon缓存的时间,默认30s
```

Ribbon的缓存

- Ribbon从本地客户端获取服务注册列表信息。Ribbon本身还维护了缓存，以避免每个请求都需要从客户端获取注册列表信息
- Ribbon的注册列表信息的缓存时间是30秒，可以通过配置更改时间。所以此时也至少需要30秒的时间去刷新实例的缓存



### Ribbon重试机制

​		Eureka为了实现更高的服务可用性，牺牲了一定的一致性，极端情况下它宁愿接收故障实例也不愿丢掉健康实例（Eureka 的自我保护机制）

​		因此 Spring Cloud 整合了 Spring Retry 来增强 RestTemplate 的重试能力，当一次服务调用失败后，不会立即抛出一次，而是再次重试另一个服务

````yaml
spring:
  cloud:
    loadbalancer:
      retry:
        enabled: true # 开启Spring Cloud的重试功能
        
#这里的 user-service 就是你需要请求的服务实例的名称        
user-service: 
  ribbon:
    ConnectTimeout: 250            # Ribbon的连接超时时间
    ReadTimeout: 1000              # Ribbon的数据读取超时时间
    OkToRetryOnAllOperations: true # 是否对所有操作都进行重试
    MaxAutoRetriesNextServer: 1    # 切换实例的重试次数
    MaxAutoRetries: 1              # 对当前实例的重试次数
````



### Ribbon饥饿加载

​		 默认情况下 Ribbon 是懒加载的；即首次请求 Ribbon 相关类才会初始化，这会导致首次请求过慢的问题，你可以配置饥饿加载，让Ribbon在应用启动时就初始化 

```yaml
ribbon:
  eager-load:
	enabled: true
    # 多个服务用 , 分隔
    clients: microservice-provider-user
```

