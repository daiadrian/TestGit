# SpringCloud Stream消息驱动

​		`Spring Cloud Stream` 是一个消息驱动微服务框架。应用程序通过 inputs 或者 output 来与 Spring Cloud Stream 中 binder 对象交互。通过配置来 binding （绑定），而 Spring Cloud Stream 的 binder 对象负责与消息中间件交互。通过使用 `Spring Integration` 来连接消息代理中间件以实现消息事件驱动

​		`Spring Cloud Stream` 为一些供应商的消息中间件（<font color=orange>**目前支持 RabbitMQ 、Kafka**</font>）提供了个性化的自动化配置实现，引用了发布 - 订阅模型、消费组和分区三个核心概念

​		总的来说就是：<font color=red>屏蔽消息中间件的差异，降低切换成本，统一的消息编程模型</font>



## Stream如何屏蔽中间件差异

​		在没有绑定器这个概念的情况下，我们的 Spring Boot 应用要直接与消息中间件进行信息交互的时候，由于各个消息中间件构建的初衷不同，他们的实现细节上会有较大的差异性

​		通过自定义绑定器作为中间层，完美的实现了应用程序与消息中间件细节之间的隔离。Stream 对消息中间件的进步一封装，可以做到代码层面对中间件的无感知，甚至于动态的切换中间件（RabbitMQ 切换为 kafka ）, 使得微服务开发的高度解耦， 服务可以关注更多自己的业务流程

​		通过定义绑定器作为中间层，完美实现了**应用程序与消息中间件细节之间的隔离**

​		通过向应用程序暴露统一的Channel 通道，使得应用程序不需要在考虑各种不同的消息中间件实现

​		**通过自定义绑定器 Binder 作为中间层， 实现了应用程序与消息中间件细节之间的隔离**



## 标准概念

1. Binder：可以方便的连接中间件，屏蔽差异
   - Binder 是应用消息中间件之间的封装， 目前实现了 Kafka 和 RabbitMQ 的Binder ；通过 Binder 可以很方便的连接中间件，可以动态的改变消息类型（对于 Kafka 的 topic , rabbitmQ 的 exchange）, 这些都可以通过配置文件来实现
2. Channel：通道，是队列 Queue 的一种抽象，在消息通讯系统中就是实现存储和转发的媒介，通过channel 队列进行配置
3. Source 和 Sink ：简单的理解为参照对象 `Spring Cloud Stream` 自身，从 Steam 发布消息就是输出，接受消息就是输入



### 常用注解

1. `@Input`：注解标识输入通道，通过该输入通道接收到的消息进入应用程序
2. `@Output`：注解表示输出通道，发布的消息将通过该通道离开应用程序
3. `@StramListener`：监听队列，用于消费者的队列的消息接受
4. `@EnableBinding`：指信道 channel 和 exchange 绑定在一起





## 实战

### 消息生产者端

```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-stream-rabbit</artifactId>
</dependency>
```



```yml
server:
  port: 8701

spring:
  application:
    name: stream-provider
  cloud:
    stream:
      binders:  # 在此处配置要绑定的 rabbitmq 的服务信息。
        defaultRabbit:  # 表示定义的名称，用于 binding 的整合
          type: rabbit  # 消息组件类型
          evironment:   # 设置rabbitmq 相关的环境信息
            spring:
              rabbitmq:
                host: 127.0.0.1
                port: 5672
                username: guest
                password: guest
      bindings: # 服务整合处理
        output: # 这个名字就是一个通道名称
          destination: studyExchange  # 表示要使用 Exchange 名称定义
          cnntext-type: application/json # 设置消息类型，本次为 JSON，文本则设置 "test/plan"
          binder: defaultRabbit # 设置要绑定的消息服务的具体位置

eureka:
  instance:
    # 访问路径可显示IP地址
    prefer-ip-address: true
    # Eureka 客户端向服务端发送心跳的时间间隔，单位为秒（默认30秒）
    lease-renewal-interval-in-seconds: 1
    # Eureka 服务端在收到最后一次心跳后等待时间上限，单位为秒 (默认90秒)，超时将剔除服务
    lease-expiration-duration-in-seconds: 2
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka/
```



```java
@Slf4j
@EnableBinding(Source.class)
public class MessageProviderImpl implements IMessageProvider {

    @Autowired
    private MessageChannel output;

    @Override
    public String send() {
        String serial = UUID.randomUUID().toString().replace("-", "");
        output.send(MessageBuilder.withPayload(serial).build());
        log.info(" =======> serial: {}", serial);
        return null;
    }
}
```





### 消费者端

```yml
server:
  port: 8801

spring:
  application:
    name: stream-consumer
  cloud:
    stream:
      binders:  # 在此处配置要绑定的 rabbitmq 的服务信息。
        defaultRabbit:  # 表示定义的名称，用于 binding 的整合
          type: rabbit  # 消息组件类型
          evironment:   # 设置rabbitmq 相关的环境信息
            spring:
              rabbitmq:
                host: 127.0.0.1
                port: 5672
                username: guest
                password: guest
      bindings: # 服务整合处理
        input: # 这个名字就是一个通道名称
          destination: studyExchange  # 表示要使用 Exchange 名称定义
          cnntext-type: application/json # 设置消息类型，本次为 JSON，文本则设置 "test/plan"
          binder: defaultRabbit # 设置要绑定的消息服务的具体位置

eureka:
  instance:
    # 访问路径可显示IP地址
    prefer-ip-address: true
    # Eureka 客户端向服务端发送心跳的时间间隔，单位为秒（默认30秒）
    lease-renewal-interval-in-seconds: 1
    # Eureka 服务端在收到最后一次心跳后等待时间上限，单位为秒 (默认90秒)，超时将剔除服务
    lease-expiration-duration-in-seconds: 2
    instance-id: stream-consumer-8801
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka/
```



```java
@Slf4j
@Component
@EnableBinding(Sink.class)
public class ReceiveMessageListenerController {

    @Value("${server.port}")
    private String serverPort;

    @StreamListener(Sink.INPUT)
    public void input(Message<String> message) {
        log.info("消费者:{}, ------> 接受到的消息:{}", this.serverPort, message);
    }
}
```



## 消息重复消费和持久化

​		微服务应用放置于同一个 group 中， 就能保证消息只会被其中一个应用消费一次。不同的小组是可以消费的，同一个组内会发生竞争关系，只有其中一个可以消费



```yml
# 其余内容省略，在消费者端设置分组信息
spring:
  cloud:
    stream:
      bindings: 
        input: 
          group: daiA #分组的名称, 在需要解决重复消费的其他服务上加上同名的分组即可
```

