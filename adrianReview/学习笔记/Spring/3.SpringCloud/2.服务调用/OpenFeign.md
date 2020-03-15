## OpenFeign

### OpenFeign和Feign的区别

<font color=red>**Feign：**</font>

​		Feign 是 SpringCloud 组件中的一个轻量级的 RESTful 的 HTTP 服务客户端，Feign 内置了 Ribbon，用来做客户端的负载均衡，去调用服务注册中心的服务



<font color=red>**OpenFeign：**</font>

​		OpenFeign 是 SpringCloud 在 Feign 的基础上支持了 SpingMVC 注解，如 `@RequestMapping` 等等。OpenFeign 的 `@FeignClient`  注解可以解析 SpringMVC 的 `@RequestMapping` 注解下的接口，并通过动态代理的方式产生实现类，实现类中做负载均衡并调用其他服务



### 简介

​		Feign 是一个声明式 WebService 客户端，使用方法时定义一个接口并在上面添加注解即可。Feign 支持可拔插式的编码器和解码器。Spring Cloud 对 Feign 进行了封装，使其支持SpringMVC和HttpMessageConverters

​		Feign 是采用了 **声明式API接口** 的风格，将HTTP客户端绑定到其内部，使客户端调用过程变得简单。Feign是声明式、模板化的HTTP客户端，可以帮助我们更加便捷、优雅地调用HTTP API。<font color=red>Feign用于**微服务之间**的远程调用</font>

​		当 Feign 接口的方法被调用时，通过 <font color=red>动态代理</font> 来生成具体的 RequestTemplate 模板对象；然后根据 RequestTemplate 再生成 HTTP 请求的 Request 对象

​		Feign 客户端默认的网络请求框架是：`HttpURLConnection`；可以替换为 HttpClient 或者 OkHttp				

​		Feign 可以与 Eureka 和 Ribbon 组合使用以支持负载均衡（Feign 是通过 `LoadBalanceClient` 类来结合 Ribbon 做负载均衡的）；还整合了Hystrix，具有熔断的能力



### Feign的配置和使用

POM

```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-openfeign</artifactId>
</dependency>
```



配置文件

```yml
server:
  port: 9013

spring:
  application:
    name: department-consumer-9013
eureka:
  instance:
    instance-id: department-consumer-9013
    # 访问路径可以显示ip地址
    prefer-ip-address: true
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka/
# 设置feign客户端超时时间 ( OpenFeign 默认支持 Ribbon )
ribbon:
  # 指的是建立连接所用的时间,适用于网络状态正常的情况下,两端连接所用的时间
  ReadTimeout: 5000
  # 指的是建立连接后从服务器读取到可用资源所用的时间
  ConnectTimeout: 5000
```



服务类配置

```java
@Service
@FeignClient(name = "DEPARTMENT-PROVIDER")
@RequestMapping("/dept")
public interface DeptmentService {

    @PostMapping("/addDept.do")
    int saveDeptment(Department department);

    @GetMapping("/getAllDept.do")
    List<Department> getAllDepartment();

}
```

```java
@SpringBootApplication
@EnableEurekaClient
@EnableFeignClients
public class DepartmentConsumer9013 {

    public static void main(String[] args) {
        SpringApplication.run(DepartmentConsumer9013.class, args);
    }

}
```





### Feign超时控制

​		Feign 客户端默认的等待时间是 1 秒，超过 1 秒没有成功调用的时候，Feign 会直接返回 timeout 的超时异常报错

​		因为 Feign 是内置 Ribbon 的，所以可以通过配置 Ribbon 的超时时间来控制超时异常的问题

```yml
# 设置feign客户端超时时间 ( OpenFeign 默认支持 Ribbon )
ribbon:
  # 指的是建立连接所用的时间,适用于网络状态正常的情况下,两端连接所用的时间
  ReadTimeout: 5000
  # 指的是建立连接后从服务器读取到可用资源所用的时间
  ConnectTimeout: 5000
```



### Feign日志增强

​		Feign 提供了日志打印功能，可以通过配置来设置日志级别，从而了解 Feign 详细的 HTTP 调用过程

<font color=blue>**Feign 的日志级别：**</font>

- NONE：默认的，不显示任何日志
- BASIC：仅记录请求方法、URL、响应状态码和执行时间
- HEADERS：除了 BASIC 中的内容，还包括了请求和响应的头信息
- FULL：除了 HEADER 和 BASIC 中的内容，还包括了请求和响应的正文及元数据



配置方式一：

```java
/**
 * 这里是配置全局的Feign的日志
 **/
@Configuration
public class FeignConfig {

    /**
     * feignClient配置日志级别
     *
     * @return
     */
    @Bean
    public Logger.Level feignLoggerLevel() {
        // 请求和响应的头信息,请求和响应的正文及元数据
        return Logger.Level.FULL;
    }
}
```

```yml
# 配置文件配置
logging:
  level:
    # feign日志以什么级别监控哪个接口
    com.dai.service.DeptmentService: debug
    # 或者可以对整个包进行监控
    com.dai.service: debug
```



