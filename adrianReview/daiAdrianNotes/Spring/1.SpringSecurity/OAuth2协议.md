## OAuth2协议

​		OAuth是一个关于<font color=blue>授权的开放网络标准</font>，目前的版本是2.0

​		通过这个网络标准，一个 **<font color=blue>第三方应用（客户端）</font>** 可以获取 **<font color=blue>资源所有者（用户）</font>** 在 **<font color=blue>服务提供商（为用户提供服务的服务商）</font>** 保存的特定资源

在这个标准中：

- 第三方应用不能直接登录服务提供商
- 资源所有者只负责做是否授权以及授权哪些资源的决策
- 根据决策结果，<font color=red>第三方应用可以获得有**时效**、有**授权范围的令牌**，并通过令牌从服务供应商那里**获得特定的资源**</font>



## 常见的授权类型

### 1.授权码



![授权码模式](images\1.OAuth协议的授权码协议(主流协议).png)

授权码模式的主要流程：

- 第三方应用将用户导向认证服务器
- 用户同意进行授权
- 返回第三方应用并携带授权码
- 第三方应用向认证服务器申请令牌
- 认证服务器发放令牌
- 第三方引用使用令牌向资源服务器获取用户信息



### 2.客户端模式

### 3.更新令牌

### 4.密码模式