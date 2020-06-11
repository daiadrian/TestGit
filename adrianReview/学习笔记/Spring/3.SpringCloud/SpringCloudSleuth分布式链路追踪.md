# Sleuth分布式链路追踪

​		在微服务框架中，一个由客户端发起的请求在后端系统中会经过多个不同的服务节点调用来协同生产最后的请求结果，每一个前端请求都会形成一条复杂的分布式服务调用链路，链路中的任何一环出现高延时或错误都会导致整个请求最后的失败

​		Sleuth 配置了所需的一切。这包括将跟踪数据（跨度）报告到的位置，要保留（跟踪）多少个跟踪，是否发送了远程字段（行李）以及要跟踪哪些库。总的来说就是：`Spring Cloud Sleuth` 提供了一套完整的服务跟踪的解决方案，在分布式系统中提供链路追踪解决方案并兼容支持了 `zipkin`





## 实战

```xml
<!--包含了 sleuth + zipkin-->
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-zipkin</artifactId>
</dependency>
```



需要进行链路追踪的微服务都需要加上该配置信息

```yml
spring:
  zipkin:
    base-url: http://localhost:9411
    seluth:
      sampler:
        # 采样值介于 0-1 之间;  1 表示全部采集
        probability: 1
```



调用接口之后，打开 `http://localhost:9411` 即可使用`zipkin` 查看链路追踪信息