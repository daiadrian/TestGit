# SpringCloud Gateway

## 简介

​		GateWay 旨在提供一种简单而有效的方式来对API进行路由，以及提供一些强大的过滤器功能（如熔断、限流和重试等等）

​		GateWay 基于 Filter 链的方式提供了网关的基本功能：安全、监控/指标、限流

​		GateWay 使用了 WebFlux 中的 Reactor-Netty 响应式编程组件，底层使用了 Netty 通讯框架



**<font color=orange>特性</font>**

1. 动态路由：能够匹配任何请求属性

2. 可以对路由指定 Predicate（断言）和 Filter（过滤器）

3. 集成Hystrix的断路器功能；

4. 请求限流功能

5. 支持路径重写



**<font color=orange>与Zuul的区别</font>**

​		Zuul 1.x 基于 Servlet2.5 使用阻塞架构，不支持任何长连接（如WebSocket），每次I/O操作都是从工作线程中选择一个执行，请求线程被阻塞到工作线程完成（与Nginx设计理念比较像）



## 核心概念

### 路由

​		路由是构建网关的基本模块；它由ID，目标URI，一系列的断言和过滤器组成，如果断言为 true 则匹配该路由

### 断言

​		开发人员可以匹配 HTTP 请求中的所有内容（如请求头和请求参数），如果请求与断言匹配则进行路由；跟 JDK8 中的 Predicate 的用法一致

### 过滤

​		**Filter**：可以使用过滤器在请求前后做其他事情（如鉴权，日志输出，协议转换，流量监控等）



## 工作原理

![gateway工作原理](.\1.gateway工作原理.png)

1. 客户端向 `Spring Cloud Gateway` 发出请求
2. 如果网关处理程序映射确定请求与路由匹配（这里的匹配规则就是断言 Predicate），则将其发送到网关 Web 处理程序。该处理程序通过特定于请求的过滤器链来运行请求
3. 过滤器由虚线分隔的原因是，<font color=red>过滤器可以在**发送代理请求之前和之后**运行逻辑</font>
4. 所有 “前置” 过滤器逻辑均被执行。然后发出代理请求
5. 发出代理请求后，当代理请求执行完之后，将运行 “后置” 过滤器逻辑



## 路由断言配置（留意*号）

### 配置方式

1. 简易配置

   ```yml
   # 这里配置了只有请求中的 Cookie 带有 name是mycookie 的, 且值是mycookievalue的才匹配放行
   spring:
     cloud:
       gateway:
         routes:
         - id: after_route
           uri: http://localhost:8080
           predicates:
           - Cookie=mycookie,mycookievalue
   ```

2. 详细配置

   ```yml
   spring:
     cloud:
       gateway:
         routes:
         - id: after_route
           uri: http://localhost:8080
           predicates:
           - name: Cookie
             args:
               name: mycookie
               regexp: mycookievalue
   ```



### After Predicate 

​		`After Predicate` 工厂有一个参数，一个 `datetime`（其是 Java `ZonedDateTime` ，这里的时区不是北京时间）

​		该断言匹配在指定日期时间之后发生的请求，即在这个时间之前的请求都不会匹配放行，只有时间到达指定的日期时间才会匹配请求

```yml
# 亚洲上海时间2020-3-28 21:20 分之后的任何请求都能匹配
spring:
  cloud:
    gateway:
      routes:
      - id: after_route
        uri: http://localhost:8080
        predicates:
        - After=2020-03-28T21:20:02.923+08:00[Asia/Shanghai]
```

```java
//ZonedDateTime 获取方式
ZoneDateTime time = ZoneDateTime.now();
```



### Before Predicate

​		和 `After Predicate` 配置一致，然后是在配置时间之前的请求都会匹配放行；但其之后的请求都不会匹配



### Between Predicate

​		`Between Predicate`工厂有两个参数，`datetime1` 和`datetime2` （这两个都 Java `ZonedDateTime`对象）。该断言匹配在 `datetime1` 之后和 `datetime2` 之前发生的请求 

```yml
spring:
  cloud:
    gateway:
      routes:
      - id: between_route
        uri: http://localhost:8080
        predicates:
        - Between=2020-03-28T21:20:02.923+08:00[Asia/Shanghai], 2020-03-28T21:28:02.923+08:00[Asia/Shanghai]
```



###  Cookie Predicate

​		`Cookie Predicate`工厂采用两个参数，Cookie 的 `name` 和 `regexp`（Java的正则表达式）。该断言匹配具有给定名称且其值与正则表达式匹配的 cookie 

```yml
spring:
  cloud:
    gateway:
      routes:
      - id: cookie_route
        uri: http://localhost:8080
        predicates:
        - Cookie=username, [0-9]*
```

匹配 cookie 中是纯整数的请求



### Header Predicate

​		`Header Predicate`工厂采用两个参数，Header 的`name` 和一个`regexp`（Java正则表达式）。该断言与具有给定名称且其值与正则表达式匹配的 Header 请求头

```yml
spring:
  cloud:
    gateway:
      routes:
      - id: header_route
        uri: http://localhost:8080
        predicates:
        - Header=X-Request-Id, \d+
```

​		如果请求中有名为 `X-Request-Id` 的请求头，且其值与 `\d+` 正则表达式匹配的（即其值为一个或多个数字），则此路由匹配



### Host Predicate

​		`Host Predicate`工厂需要一个参数：主机名的列表 `patterns` 。断言与`Host` 主机名匹配的请求

```yml
spring:
  cloud:
    gateway:
      routes:
      - id: host_route
        uri: http://localhost:8080
        predicates:
        - Host=**.dai.com,**.adrian.com
```

​		如果请求的请求头中的 Host 的值为：`search.dai.com`、`order.dai.com` 或者 `www.adrian.com`， 那么就会被断言匹配



### *Method Predicate

​		`Method Predicate`需要 `methods` 的参数，它是一个或多个参数：匹配 HTTP 请求的方法

```yml
spring:
  cloud:
    gateway:
      routes:
      - id: method_route
        uri: http://localhost:8080
        predicates:
        - Method=GET,POST
```

该断言匹配 GET 和 POST 请求方式的请求



### *Path Predicate

该断言匹配的是路径

```yml
spring:
  cloud:
    gateway:
      routes:
      - id: path_route
        uri: http://localhost:8080
        predicates:
        - Path=/red/**,/blue/**
```

匹配路径 `/red/1` 或 `/red/blue` 或 `/blue/2`



### Query Predicate

查询条件的断言配置，有两种配置方式（其中一种是可选的正则表达式）

```yml
spring:
  cloud:
    gateway:
      routes:
      - id: query_route
        uri: http://localhost:8080
        predicates:
        - Query=username
```

匹配请求参数中带有 `username` 的请求



```yml
spring:
  cloud:
    gateway:
      routes:
      - id: query_route
        uri: http://localhost:8080
        predicates:
        - Query=username, dai.
```

匹配请求参数中带有 `username` 的请求，并且该参数的值要满足后面的正则表达式内容



## 过滤器配置（内置过滤器，留意*）

​		内置过滤器比较少用到，参考 GateWay 官方文档使用 

`https://cloud.spring.io/spring-cloud-static/spring-cloud-gateway/2.2.2.RELEASE/reference/html/#gatewayfilter-factories`

### *SetPath Filter

```yml
spring:
  cloud:
    gateway:
      routes:
      - id: setpath_route
        uri: http://localhost:8080
        predicates:
        - Path=/red/**
        filters:
        - SetPath=/**
```

​		设置了 `SetPath` 过滤器，那么对于的请求路径 `/red/blue`，这会将路径设置为`/blue` **发出下游请求**之前的路径



## 自定义全局过滤器（重点）

<font color=red>针对 `2.2.2.RELEAS` 版本，不同版本可能不一致</font>

```java
@Component
@Slf4j
public class MyFilter implements GlobalFilter, Ordered {

    /**
     * 过滤器真正执行的方法
     * @param exchange
     * @param chain
     * @return
     */
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        log.info("======> 自行实现的过滤器, 请求进来了");
        ServerHttpRequest request = exchange.getRequest();
        String token = request.getQueryParams().getFirst("dai_token");
        if (StringUtils.isEmpty(token)) {
            log.info("------> 过滤没有携带 token 的请求 ");
            ServerHttpResponse response = exchange.getResponse();
            response.setStatusCode(HttpStatus.NOT_ACCEPTABLE);
            return exchange.getResponse().setComplete();
        }
        log.info("请求的 token 内容: " + token);
        return chain.filter(exchange);
    }

    /**
     * 过滤器的执行顺序 (执行的优先级)
     * 数值越小, 优先级越高
     *
     * @return
     */
    @Override
    public int getOrder() {
        return 0;
    }
}
```



## 开启服务名动态路由

```yml
server:
  port: 9527

spring:
  application:
    name: gateway-9527
  cloud:
    gateway:
      discovery:
        locator:
        #开启从注册中心动态创建路由的功能(默认是关闭的), 可以使用微服务名称进行路由
          enabled: true  
      routes:
        - id: department-consumer   #路由ID,唯一; 建议使用服务名称
          # lb 是loadbalance, 后续加的是注册到注册中心上的服务名称
          uri: lb://DEPARTMENT-CONSUMER  
          predicates:
            - Path=/consumer/dept/**     #断言, 路径匹配进行路由
            - Query=username
```



## HTTP超时配置

```yml
spring:
  application:
    name: gateway-9527
  cloud:
    gateway:
      # 全局的超时时间配置
      httpclient:
        # 连接超时时间, 以毫秒为单位
        connect-timeout: 1000
        # 响应超时时间 必须指定为 java.time.Duration
        response-timeout: 5s
      discovery:
        locator:
          enabled: true  #开启从注册中心动态创建路由的功能(默认是关闭的), 可以使用微服务名称进行路由
      routes:
        - id: department-consumer   #路由ID,唯一; 建议使用服务名称
          #uri: http://localhost:9014     #匹配后的路由地址
          uri: lb://DEPARTMENT-CONSUMER
          predicates:
            - Path=/consumer/dept/**     #断言, 路径匹配进行路由
            - Query=username
          metadata:
            # 对于指定路由的超时时间, 这里的时间都是毫秒为单位
            response-timeout: 200
            connect-timeout: 200
```



