## SpringCloud配置Zookeeper

### 遇到导入的包与使用的ZK版本不一致的时候

```xml
<!--SpringBoot整合Zookeeper客户端-->
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-zookeeper-discovery</artifactId>
    <exclusions>
        <!-- 先排除自带的 zookeeper3.5.3 依赖 -->
        <exclusion>
        <groupId>org.apache.zookeeper</groupId>
        <artifactId>zookeeper</artifactId>
    </exclusion>
    </exclusions>
</dependency>
<!-- 添加 zookeeper3.4.9 版本 -->
<!-- 此处添加的依赖跟自己使用的ZK版本保持一致即可 -->
<dependency>
    <groupId>org.apache.zookeeper</groupId>
    <artifactId>zookeeper</artifactId>
    <version>3.4.14</version>
</dependency>
```



### 配置文件

此处的配置是在需要注册到注册中心的服务配置上

```yml
server:
  port: 8004
spring:
  application:
    # 服务别名, 注册到 zookeeper 注册中心的名称
    name: cloud-provider-payment
  cloud:
    zookeeper:
      # 这里配置注册中心的地址, 集群在后面加 逗号 
      connect-string: 127.0.0.1:2181
      #connect-string: 127.0.0.1:2181,127.0.0.1:2182,127.0.0.1:2183
```



启动类上加上 `@EnableDiscoveryClient` 注解即可



### 注册的节点是临时节点还是持久节点？

​		服务端注册到 ZK 上的节点是临时节点，因为 ZK 是保证 CP 的，一致性要求比较高；所以当服务端的心跳因为网络等原因没有达到 ZK 的话，一段时间后会被 ZK 剔除。当服务好了之后才会重新的在 ZK 生成新的临时节点