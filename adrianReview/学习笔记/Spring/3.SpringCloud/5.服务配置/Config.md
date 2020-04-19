# SpringCloud Config

## 简介

​		SpringCloud Config 为微服务架构提供了集中化的外部配置支持，配置服务器为各个不同的微服务应用的所有环境（dev，test，prod）提供一个中心化的外部配置

Config分为服务端和客户端两部分

1. 服务端：
   - 分布式配置中心，它是一个独立的微服务应用，用来连接配置服务器并为客户端提供获取配置信息，加密/解密 信息等访问接口
2. 客户端：
   - 通过指定的配置中心来管理应用资源，以及与业务相关的配置内容，并在**启动的时候从配置中心获取和加载配置信息**
   - 配置服务器默认采用 git 来存储配置信息，这样有助于对环境配置进行版本管理，并且可以通过 git 客户端工具来方便管理和访问配置内容



### **<font color=orange>统一配置中心的作用</font>**

- 集中管理配置文件
- 动态化的配置更新，管理不同环境的配置文件
- 运行期间的动态调整配置，服务会向配置中心统一拉取配置信息
- 配置发生变动时， 服务不需要重启即可感知到配置发生变化并应用新的配置
- 将配置信息以 REST 接口的形式暴露



### YML配置文件解析

- application.yml 是**应用级资源配置项**

- bootstarp.yml 是<font color=red>**系统级，优先级更高**</font>



​		Spring Cloud 会创建一个 “Bootstrap Context” ，作为Spring 应用的 “Application Context” 的父上下文， 初始化的时候，“Bootstrap Context" 负责从外部源加载配置属性并解析配置。这两个上下文共享一个从外部获取的 ”Environment“

​		”Bootstarp“ 属性有高优先级， 默认情况下，他们不会被本地配置覆盖。 ”Bootstrap context“ 有着不同的约定， 所以新增了一个 ”bootstarp.yml“ 文件，保证”Bootstrap Context “ 和 ”Application Context“ 配置的分离

​		所以要将客户端模块下的 application.yml 文件修改为 bootstrap.yml 这是很关键的。因为 bootstrap.yml 是比 application.yml 先加载的 。bootstrap.yml 优先级高于 application.yml





## 服务端实战

### pom依赖

```xml
<dependencies>
    <!--eureka client-->
    <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-actuator</artifactId>
    </dependency>
    <!-- config 依赖 -->
    <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-config-server</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
</dependencies>
```



### yml配置文件

```yml
server:
  port: 8844

spring:
  application:
    name: CONFIG-CENTER-8844
  cloud:
    config:
      server:
        git:
          #远程仓库地址
          uri: git@gitee.com:daiadrian6608/daiSpringCloudStudy.git
          #指定配置文件的目录
          search-paths:
            - daiSpringCloudStudy
           # 远程仓库用户名和密码
          username: daiadrian@sina.com
          password: ******
          #ssh私钥配置方式
          private-key: ******
          #此参数为 true 时,那么使用配置方式(private-key)的ssh验证, 否则使用文件方式的验证
          ignore-local-ssh-settings: true
      #指定分支
      label: master

eureka:
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka/
```



### 启动类

```java
@SpringBootApplication
@EnableEurekaClient
@EnableConfigServer
public class ConfigCenter8844 {
    public static void main(String[] args) {
        SpringApplication.run(ConfigCenter8844.class, args);
    }
}
```



## 客户端实战

### yml配置文件

```yml
server:
  port: 9015

spring:
  application:
    name: CONFIG-CLIENT-9015
  cloud:
    config:
      #分支名称
      label: master
      #配置文件名称 {application}-{profile}.yml 中 application 的名称
      name: config
      profile: dev
      #配置中心的地址
      uri: http://localhost:8844

eureka:
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka

# 暴露监控端点,只有暴露了才能够动态刷新配置文件
management:
  endpoints:
    web:
      exposure:
        include: "*"
```



### 控制层

```java
@RestController
@RequestMapping("/configClient")
//该注解可以开启配置项的刷新功能
@RefreshScope
public class ConfigClientController {

    @Value("config.client.hello")
    private String configHello;


    @GetMapping("/getConfigValue")
    public String helloWorld() {
        return configHello;
    }

}
```



### 动态刷新的操作

`curl -X POST "http://localhost:9015/actuator/refresh"` 

<font color=red>必须是**POST请求**才能更新变动的值</font>



