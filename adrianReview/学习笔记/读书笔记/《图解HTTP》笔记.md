《图解HTTP》笔记

## HTTP协议

### HTTP概述

一次Http的请求涉及的网络层级：

- 应用层（HTTP协议，DNS服务）
  - <font color=blue>DNS服务：</font>提供域名到IP地址的解析服务。即可以通过域名查找到IP地址或者通过IP地址反查到域名
- 传输层（TCP协议）
  - <font color=blue>TCP协议提供可靠的字节流服务</font>
    - 字节流服务是指为了方便传输，会把大块的数据分割成报文段（segment）为单位的数据包进行管理和传输
    - 可靠的传输就是TCP的三次握手能够将数据准确可靠的传给对方
- 网络层（IP协议）
  - IP协议重要的两个条件是：IP地址和MAC地址
  - IP间的通信依赖MAC地址
- 数据链路层



​		HTTP协议是用户客户端和服务端的通信。<font color=blue>HTTP是无状态的协议；无状态是指：</font>HTTP协议本身不对请求和响应之间的通信状态进行保存，也就是不保存请求报文和响应报文的信息

#### HTTP的方法

​		HTTP的方法是用来告知服务器，这次请求的意图。主要有：

- GET：获取资源
- POST：
- PUT：上传资源
- DELETE：删除资源
- HEAD：作用和GET方法类似，但是不返回响应实体的内容
- OPTIONS：查询服务端对于请求指定的资源支持的方法

#### 持久连接

​		持久连接是指：只要服务端或者客户端任意一端没有明确提出断开连接，则一直保持TCP的连接状态。这样的好处在于：<font color=red>减少了TCP连接的重复建立和断开所带来的网络开销，减轻服务端的负载</font>。HTTP/1.1默认所有连接都是持久连接

#### 管线化

​		因为持久连接的原因，可以使得多数请求能够以管线化的发送方式成为可能。没有管线化的时候，发送一次请求后需要等待服务端的响应才能进行下一次的请求，管线化能够做到同时**并行**发送多个请求



### URI和URL

- URI：统一资源标识符
- URL：统一资源定位符

URI标识某一互联网的资源，而URL表示的是这个资源的位置；URL是URI的子集



### 网关、代理和隧道

**代理**

​		代理是一种有转发功能的应用程序，位于服务器和客户端之间

​		使用代理服务器可以利用其缓存技术，减少网络带宽的流量。缓存代理会预先将资源的副本保存在代理服务器上，当客户端再次请求同样的内容时，代理服务器就会把缓存的资源副本直接返回给客户端；就不需要再去请求服务端了

**网关**

​		网关是转发其他服务器通信数据的服务器；可以将客户端的HTTP请求转换成其他协议的通信方式

**隧道**

​		隧道是指按要求建立起一条与其他服务器的通信线路，然后就可以使用SSL等加密手段进行通信。目的是确保客户端与服务器进行安全的通信



## HTTPS





## HTTP报文

​		用户HTTP协议交互的信息叫做HTTP报文，HTTP的报文主要分为请求报文和响应报文。一个报文主要组成是：

- 报文首部
  - 请求：
    - 请求行
      - 请求的URI和HTTP版本
    - 首部字段
      - 首部字段包含：请求首部字段，通用首部字段和实体首部字段
  - 响应：
    - 状态行
      - 状态码，原因短语和HTTP版本
    - 首部字段
- 请求行（一个空行，CR-LF）
- 报文主体

### 通用首部字段

> 如果首部字段重复了，那么会根据浏览器的不同有不同的处理逻辑；有可能是优先处理第一个同名字段，也有可能是处理最后一个字段

#### Cache-Control

`Cache-Control: no-cache` 

- 如果是请求带上这个字段，那么缓存服务器会向源服务器进行资源的有效期确认，如果缓存过期那么就必须将该请求转发到源服务器
- 如果是响应带上这个字段，那么缓存服务器将不会对该资源进行缓存

`Cache-Control: no-store`

- 不进行缓存，直接请求源服务器。服务端返回的响应也不会进行缓存

`Cache-Control: max-age=604800` （单位：秒）

- 表示缓存的最长时长。如果缓存服务器中的有效时间小于该值，那么设置该值作为缓存的最长时间

#### Connection

`Connection: Keep-Alive`

- 持久连接

#### Date

`Date: Fri, 18 Oct 2019 14:47:21 GMT`

- 表示创建这个HTTP报文的时间



### 请求首部字段

#### Accept

`Accept: application/json, text/javascript, */*; q=0.01`

- 用于通知服务器，用户代理能够处理的媒体类型及相对优先级
- 常用：
  - `application/json`
  - `text/html`
  - `image/gif`、`image/png`

#### Host

`Host: blog.csdn.net`

- 告知服务器，请求的资源所处的互联网主机名和端口号

#### Referer

`Referer: https://blog.csdn.net/Adrian_Dai`

- 告知服务器请求的原始资源URI；也就是这个请求的来源地址

#### User-Agent

`User-Agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/77.0.3865.120 Safari/537.36`

- 表示的是请求的浏览器信息和用户代理名称等信息

#### Range

`Range: bytes=5001-10000`

- 只需获取部分资源的范围请求。表示指定获取从5001字节到10000字节的资源



### 响应首部字段

#### Age

`Age: 600` 告知客户端，源服务器是多久前创建的响应，单位为秒

#### Location

`Location: http://www.dai.com` 

使用该字段能够将响应接收方引导到指定的URL上，通常配合 302 响应码一起使用，指定需要跳转的URL

#### Server

`Server: Apache/2.2.17 (Unix)`

告知客户端，当前服务端上安装的HTTP服务器应用程序的信息



### 实体首部字段

​		实体首部字段是包含在请求报文和响应报文中的实体部分所使用的字段

#### Content-Type

`COntent-Type: text/html; charset=UTF-8`

说明实体主体内对象的媒体类型和字符集

#### Allow

`Allow: GET,POST`

服务端通知客户端，服务端这边能够支持的HTTP方法

#### Content-Language

`Content-Language: zh-CN`

告知客户端，实体主体使用的语言

#### Content-Length

`Content-Length: 15000`

表明实体主体的大小，单位是字节；对实体主体进行内容编码传输时，不能再使用该首部字段



### 状态码

- <font color=red>200：</font>成功
- <font color=red>204：</font>请求成功，但是没有资源返回
- <font color=red>206：</font>指定返回 Content-Range 指定的范围数据



- <font color=red>301：</font>永久性重定向，指的是资源被分配了新的URI了
- <font color=red>302：</font>资源临时重定向



- <font color=red>400：</font>请求报文错误（Bad Request）
- <font color=red>401：</font>该资源需要先认证
- <font color=red>403：</font>访问被拒绝（Forbidden）
- <font color=red>404：</font>请求资源不存在（NOT FOUND）



- <font color=red>500：</font>服务器出现了错误
- <font color=red>503：</font>服务器正忙 
