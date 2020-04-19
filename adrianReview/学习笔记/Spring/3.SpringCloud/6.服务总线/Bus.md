# SpringCloudBus

## 简介

​		Spring Cloud Bus 使用轻量级消息代理连接分布式系统的节点

​		这可以用于广播状态更改（例如配置更改）或其他管理指令。Bus 就像一个扩展的 Spring Boot 应用程序的分布式执行器，但也可以用作应用程序之间的通信渠道。Bus 提供了 AMQP 代理（RabbitMQ ）或者 kafka 作为传输



## 什么是消息总线

​		在微服务架构的系统中，通常会使用轻量级的消息代理来构建一个共用的消息主题， 并让系统同中所有的微服务实例都连接上来，由于<font color=red>该主题中产生的消息会被**所有实例监听和消费**</font>，所以它成为消息总线。在总线的各个实例，都可以方便地广播一些需要让其他链接在该主题上的实例都知道的消息

- 基本原理
  - Config Clinet 实例会监听MQ中同一个 Topic （默认创建了主题是 SpringCloudBus 的交换机） 
  - 当一个服务刷新数据的时候，会把这个信息放入到 Topic 汇总，这样其他监听同一个 Topic 服务就能得到通知，然后更新自身的配置





## 消息总线实战

### POM依赖

```xml
<!-- 引入 bus 消息总线 -->
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-bus-amqp</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>


<!-- bus 消息总线 kafka配置 -->
<!--<dependency>-->
<!--<groupId>org.springframework.cloud</groupId>-->
<!--<artifactId>spring-cloud-starter-bus-kafka</artifactId>-->
<!--</dependency>-->
```



### yml配置文件

```yml
#添加mq的配置信息 (config服务端和其他微服务均需配置)
rabbitmq:
  host: localhost
  port: 5672
  username: guest
  password: guest

# 暴露 bus 刷新配置的端点 (config服务端需要进行此项配置)
management:
  endpoints:
    web:
      exposure:
        include: 'bus-refresh'
```





### 广播刷新配置文件

微服务ID必须唯一

微服务ID必须唯一

微服务ID必须唯一



`curl -X POST  "http://localhost:8844/actuator/bus-refresh"`

使用POST请求 config-server 服务去发送 MQ 消息，从而使订阅该主体的微服务更新对应的配置信息





### 动态属性定点更新

> 只想更新某一个微服务的属性，其他微服务不更新

方式：（同样是请求 config-server 的微服务即可）

- `curl -X POST  "http://localhost:8844/actuator/bus-refresh/{destination}"`
- destination 的内容是想要更新的微服务的 `{spring.application.name}:{server.port}`