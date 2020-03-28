# SpringCloud Gateway

## 简介

​		GateWay 旨在提供一种简单而有效的方式来对API进行路由，以及提供一些强大的过滤器功能（如熔断、限流和重试等等）

​		GateWay 基于 Filter 链的方式提供了网关的基本功能：安全、监控/指标、限流

​		GateWay 使用了 WebFlux 中的 Reactor-Netty 响应式编程组件，底层使用了 Netty 通讯框架



### 特性

1. 动态路由：能够匹配任何请求属性

2. 可以对路由指定 Predicate（断言）和 Filter（过滤器）

3. 集成Hystrix的断路器功能；

4. 请求限流功能

5. 支持路径重写



### 与Zuul的区别

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



