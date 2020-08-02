## SpringCloud和Dubbo的比较

Dubbo致力于提供高性能和透明化的RPC远程服务调用方案，以及SOA服务治理解决方案，核心内容主要有：

- RPC 远程调用：封装长连接的NIO框架，如Netty、Mina等；并且采用多线程的模式
- 集群容错：提供基于接口方法的远程调用功能，并实现了负载均衡策略，失败容错策略等功能
- 服务发现：继承ZK组件，用于服务的发现和注册

| 微服务关注点   | SrpingCloud    | Dubbo |
| -------------- | -------------- | ----- |
| 服务注册和发现 | Eureka、Consul | Zk    |
| 负载均衡       | Ribbon         | 自带  |
| 网关           | Zuul           | —     |
| 容错           | Hystrix        | —     |
| 分布式链路追踪 | Sleuth         | —     |
| 通信方式       | HTTP、消息组件 | RPC   |
| 安全模块       | Security       | —     |
| 配置管理       | Config         | —     |



## SpringCloud和K8s的比较

​		Kubernetes是一个容器集群管理系统，为容器化的应用进程提供部署运行、维护、扩展、资源调度、服务发现等功能

K8s提供的功能：

- 自动包装：根据程序自身的资源需求和一些其他方面的需求来自动配置容器。K8s能够最大化的利用机器的工作负载，提高资源的利用率
- 自我修复：容器失败自动重启，当节点处于 “死机” 的状态，它会被替代并重新编排；当容器达到用户设定的无响应的阈值时，它会被剔除，并且不让其他容器调用它，直至它恢复服务
- 横向扩展：可以根据机器的CPU的使用率来调整容器的数量（只需要开发人员输入几个命令）
- 服务发现和负载均衡：K8s为容器提供了一个虚拟网络环境，每个容器拥有独立的IP地址和DNS名称，容器之间实现了负载均衡
- 自动部署和回滚：K8s支撑滚动更新模式，能逐步替换掉当前环境的应用程序和配置，同时监视应用程序运行状况，以确保不会同时杀死所有实例。如果出现问题，K8s支持回滚更改
- 配置管理：部署和更新应用程序的配置，不需要重新打包镜像，并且不需要在堆栈中暴露配置
- 存储编排：自动安装所选择的存储系统，无论是本地存储、公共云提供商还是网络存储系统

| 微服务关注点   | SrpingCloud    | K8s                     |
| -------------- | -------------- | ----------------------- |
| 服务注册和发现 | Eureka、Consul | Kubernetes Services     |
| 负载均衡       | Ribbon         | Kubernetes Services     |
| 网关           | Zuul           | Kubernetes Services     |
| 容错           | Hystrix        | Kubernetes Health Check |
| 分布式链路追踪 | Sleuth         | Open tracing            |





## SpringCloudAlibaba介绍

官网：
https://github.com/alibaba/spring-cloud-alibaba/blob/master/README-zh.md



### 能干什么

- <font color=orange>服务限流降级：</font>
  - 默认支持 Servlet、Feign、RestTemplate、Dubbo、和R ocketMQ 限流降级功能的接入，可以在运行时通过控制台实时修改限流降级骨子额，还支持查看限流降级 Metrics 控制
- <font color=orange>服务注册于发现：</font>
  - 适配 Spring Cloud 服务注册于发现标准，默认集成 Ribbon 支持
- <font color=orange>分布式配置管理：</font>
  - 支持分布式系统中的外部话配置，配置更改时自动刷新
- <font color=orange>消息驱动能力：</font>
  - 基于Spring Cloud Stream 为微服务应用构建消息驱动能力。
- <font color=orange>阿里云对象存储：</font>
  - 阿里云提供的海量、安全、低成本、高可靠的云存储服务
  - 支持在任何应用，任何时间、任何低调存储和访问任意类型的数据
- <font color=orange>分布式任务调度：</font>
  - 提供秒级、精准、高可靠、高可用的定时（基于 Cron 表达式）任务调度服务
  - 同时提供分布式的任务执行模型，如网格任务，网格任务支持海量任务均匀分配到所有 Worker (schedulerx-client) 执行