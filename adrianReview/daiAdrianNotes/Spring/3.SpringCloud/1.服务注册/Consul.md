## Consul

### 简介		

​		Consul 是一套开源的分布式服务发现和配置管理系统（由 HashiGorp 公司使用GO语言开发的）；它提供了微服务系统中的服务治理、配置中心、控制总线等功能，这些功能中的每一个都可以根据需要进行单独配置，也可以一起使用以构建全方位的服务网格，它提供了一种完整的服务网格解决方案

它具有很多优点：

1. 基于 raft 协议，比较简洁
2. 支持健康检查
3. 支持 HTTP 和 DNS 协议
4. 支持跨数据中心的 WAN 集群
5. 提供图形化的界面，跨平台的



### 特性

- 服务发现：
- 健康监测
- KV存储
- 多数据中心
- 可视化的Web界面



### Linux下的安装和使用

1. 首先在官网下载好 zip 包

2. 在 Liunx 上解压得到 `consul`  文件

3. 修改配置文件：`vi /etc/profile`

4. 增加内容

   ```
   export CONSUL_HOME=/opt/consul
   export PATH=$PATH:$JAVA_HOME/bin:$JRE_HOME/bin:$M2_HOME/bin:$CONSUL_HOME
   ```

5. 然后刷新配置文件：`source /etc/profile`

6. 开发模式开启consul：`consul agent -dev`



### 外部无法访问UI的处理方法

​		`consul agent -dev`  这是开发模式的启动命令 ，只能本机进行访问，当使用下面命令的时候就可以其他机器进行访问了：`consul agent -dev -client 0.0.0.0 -ui`
