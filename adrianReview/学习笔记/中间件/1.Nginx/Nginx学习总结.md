Nginx学习总结

## Nginx简述

 	Nginx是一个开源且高性能的Web 服务器，同时是一个高效的反向代理服务器，它还是一个IMAP/POP3/SMTP代理服务器



### Nginx的优点

- 高并发响应性能非常好，官方Nginx处理静态文件并发 5W/s
- 反向代理性能非常强（可用于负载均衡）
- 内存和cpu占用率低（为Apache的1/5-1/10）
- 对后端服务有健康检查功能
- 配置代码简洁且容易上手



### Nginx的作用

- 静态的web资源服务器（html，图片，js，css，txt等静态资源）
- 结合FastCGI/uWSGI/SCGI等协议的反向代理动态资源请求
- http/https协议、imap4/pop3协议的反向代理
- tcp/udp协议的请求转发（反向代理）



### Nginx安装

````shell
vim /etc/yum.repos.d/nginx.repo
---
[nginx-stable]
name=nginx stable repo
baseurl=http://nginx.org/packages/centos/7/$basearch/
gpgcheck=0
enabled=1
---
yum install nginx
````



### Nginx基本操作命令

````shell
# stop   : 快速关机
# quit   : 优雅的关机
# reload : 重新加载配置文件
# reopen : 重新打开日志文件
nginx -s [参数]

##配置文件语法检查
nginx -t [配置文件路径]

#启动nginx
cd /usr/local/nginx
./nginx
##查看进程
ps -ef | grep nginx

##查看帮助选项
nginx -h

##查看版本和配置选项
nginx -V

##指定配置文件（默认是：/etc/nginx/nginx.conf）
nginx -c [文件路径]
````



## Nginx配置文件

### 主配置模块

1. **worker_processes [number|auto]**

   > worker进程的数量，通常为当前主机的cpu物理核心数
   >
   > 可以配置为 worker_processes auto;  自动的方式

2. **worker_priority [number]**

   > 指定worker进程的nice值，设定worker进程的优先级【-20~20】

3. **worker_rlimit_nofile [number]**

   > ​	这个指令是指当一个nginx进程打开的最多文件描述符数目，理论值应该是最多打开文件数（ulimit -n）与nginx进程数相除，但是nginx分配请求并不是那么均匀，所以最好与ulimit -n 的值保持一致
   > ​	现在在linux 2.6内核下开启文件打开数为65535，worker_rlimit_nofile就相应应该填写65535
   > ​	这是因为nginx调度时分配请求到进程并不是那么的均衡，所以假如填写10240，总并发量达到3-4万时就有进程可能超过10240了，这时会返回502错误

4. **include [file|mask]**

   > 指明包含进来的其他配置文件片段



### events事件驱动模块

````nginx
events{
    use epoll;
    worker_connections 65535;
}
````



1. **worker_connections [number]**

   > 每个worker进程所能够打开的最大并发连接数的数量
   >
   > 总最大并发数：work_processes * worker_connections

2. **accept_mutex [on|off]**

   > 处理新的连接请求的方法：
   >
   > - on指由各个worker轮流处理新的请求
   > - off指每个新请求的到达都会通知所有worker进程，但只能有一个进程可获得连接，配置成off会影响性能

3. **deamon [on|off]**

   > 是否以守护进程的方式运行nginx，默认是守护进程方式
   >
   > 测试开发的时候可以改成on的形式

4. **master_process [on|off]**

   > 是否以master/worker模式运行nginx，默认为on

5. **error_log file [level]**

   > 错误日志及其级别；默认crit
   >
   > level：【debug|info|notice|warn|error|crit】



### server虚拟主机模块

````nginx
server {  
    #监听端口 80  
    listen 80;   
    #监听域名dh.com;  
    server_name dh.com www.dh.com;
    access_log logs/sever.log combined;
    location / {
        # 站点根目录，即网站程序存放目录
        # 相对路径，相对nginx根目录。也可写成绝对路径  
        root /data/dh;  

        # 默认跳转到index.html页面  
        index index.html;
        #开启后会以主目录下的随机文件作为主页
        #random_index on;
    }  
}
````



1. **listen [PORT]**

   ````nginx
   # 设定此虚拟主机为默认的虚拟主机
   ## 会优先匹配虚拟主机
   listen 80 default_server;
   ````

   

2. **server_name [name...]**

   虚拟主机的名称。可以添加多个，空格分隔；还可以匹配正则表达式

   匹配优先级：

   - 精确匹配，daiadrian.github.io
   - 左侧*通配符， *.github.io
   - 右侧*通配符， daiadrian.github. *
   - 正则表达式，~^.*\\.github\\.io$
   - default_server

3. **tcp_nodelay [on|off]**

   > - off指延迟发送，即如果一个会话中有多个请求，会合并之后再发送
   > - on指不延迟发送，来一个请求就处理一个
   >
   > ​        一般为了用户体验最好设置成on，如果为了服务器的资源得到更好的使用可以设置为off

4. **server_tokens [on|off|build|string]**

   是否在响应报文的server首部显示nginx版本

   ````http
   HTTP/1.1 304 Not Modified
   Server: nginx/1.15.7
   Date: Thu, 09 May 2019 03:33:04 GMT
   Last-Modified: Tue, 09 Apr 2019 14:26:15 GMT
   
   设置为on，则在server会显示nginx版本
   推荐设置为off或者修改源码使其可用string选项
   （string是商业版才有）
   ````

5. **error_page code [=[response]] uri**

   定义错误页面，可以以指定的响应状态码进行响应

   ````nginx
   error_page 404 /404.html
   # =200 即如果是404的错误，响应的状态码返回给客户端是200的状态
   ## 可以指定响应状态码，可以防止360等流氓浏览器将404的页面强行转到360自带的404页面去
   error_page 404 =200 /404.html
   
   location = /404.html {
       alias /web/error;
   }
   ````

   

#### **location配置**

语法规则： location [=|~|~*|^~] /uri/ {… }

​	在一个server中location配置段可存在多个，用于实现从uri到文件系统的路径映射；nginx会根据用户请求的URI来检查定义的所有location，并找出一个最佳匹配

|  =   | 表示精确匹配                                  |
| :--: | --------------------------------------------- |
|  ~   | 表示对URI做**区分字符大小写**的正则匹配       |
|  ~*  | 表示对URI做**不区分字符大小写**的正则匹配     |
|  ^~  | 对URI的最左边部分做匹配检查，不区分字符大小写 |

- <font color=red>**匹配优先级由高到低**</font>：=，^~，~/~*，不带符号的

````nginx
location = / {
    [ configuration A ]
}

location / {
    [ configuration B ]
}

location /dh/ {
    [ configuration C ]
}

location ^~ /images/ {
    [ configuration D ]
}

# \. 是转义的 .
# $ 表示以（gif|jpg|jpeg|png）结尾
location ~* \.(gif|jpg|jpeg|png)$ {
    [ configuration E ]
}
````

1. `/`请求将匹配配置A 

2. `/index.html`请求将匹配配置B

3. `/dh/adrian.html`请求将匹配配置C

4. `/images/1.gif`请求将匹配配置D

   > ^~ 以左边 /images/ 做匹配检查，后面跟什么内容都可以
   >
   > ^~的优先级比 ~* 的优先级高，所以会先匹配D

5. `/dh/1.jpg`请求将匹配配置E

- **location中使用root和alias的区别**

  - root：设置web资源的路径映射，指明请求URL所对应的文档的目录路径，可用于http，server，location
  - alias：路径别名，仅能用于location上下文

  ````nginx
  location /dh/ {
      alias /web/data;
  }
  
  location /dh {
      root /web/data;
  }
  
  # 如果访问 http://www.dh.com/dh/index.html
  ## 如果是alias配置，那么会转到 /web/data/index.html
  ## 如果是root配置，那么转到 /web/data/dh/index.html
  ````



### 常用的配置

1. **keepalive_timeout timeout [header_timeout]**

   > **Default**：keepalive_timeout 75s;
   > **Context**：http, server, location

   设定保持连接的超时时长，0表示禁止长连接

2. **keepalive_requests number**

   > **Default**：keepalive_requests 100;
   > **Context**：http, server, location

   在一次长连接上所允许请求的资源最大数量

3. **keepalive_disable** `none` | `browser` ...;

   > **Default**：keepalive_disable msie6;
   > **Context**：http, server, location

   对哪种浏览器禁用长连接

4. **send_timeout** `time`;

   > **Default**：send_timeout 60s;
   > **Context**：http, server, location

   向客户端发送响应报文的超时时长，此处是指两次写操作之间的间隔时长，而非整个响应过程的传输时长

5. **client_body_buffer_size** `size`;

   > **Default**：client_body_buffer_size 8k|16k;
   > **Context**：http, server, location

   设置读取客户端请求体（body部分）的缓冲区大小。如果请求主体大于缓冲区，则将整个主体或仅其部分写入临时文件（位置是由client_body_temp_path指令决定）。32位平台上是8K，在其他64位平台上通常是16K

6. **client_body_temp_path** `path` [`level1` [`level2` [`level3`]]];

   > **Default**：client_body_temp_path client_body_temp;
   > **Context**：http, server, location

   设定存储客户端请求报文的body部分的临时存储路径及子目录结构和数量目录名为16进制的数字；

   例如：client_body_temp_path /var/tmp/client_ody 1 2 2;

   解释：

   1：1级目录占1位16进制，即2^4=16个目录0-f

   2：2级目录占2位16进制，即2^8=256个目录00-ff

   2：3级目录占2位16进制，即2^8=256个目录00-ff

7. **limit_rate** `rate`;

   > **Default**：limit_rate 0;
   > **Context**：http, server, location, if in location

   限制响应给客户端的传输速率。0表示无限制，单位是bytes/second

8. **limit_except** `method` ... { ... }

   > **Default**：-
   > **Context**：location

   限制客户端使用出来指定的请求方法之外的其他方法

   methid：GET、HEAD、POST、PUT、DELETE等等。。

   ````nginx
   #解释：
   ##除了GET之外的其他方法仅允许192.168.1.0/24网段使用
   limit_expect GET {
       allow 192.168.1.0/24;
       deny all;
   }
   ````

9. **directio** `size` | `off`;

   > **Default**：directio off;
   > **Context**：http, server, location

   当文件大于等于给定大小时，同步（直接）写磁盘，而非写缓存；例如directio 4m

10. **open_file_cache max=N [inactive=time];**

    > **Default**：open_file_cache off;
    > **Context**：http, server, location

    nginx可以缓存以下三种信息

    - 文件元数据：文件的描述符，文件大小和最近一次修改时间
    - 打开的目录结构
    - 没有找到的或者没有权限访问的文件的相关信息

    max=N：可缓存的缓存项上限，达到上限后会使用LRU算法实现管理

    inactive=time：缓存项的非活动时长，在此处指定的时长内未被命中的或命中的次数少于open_file_cache_min_uses指令锁指定的次数的缓存项即为非活动项，这些内容将被删除

    ````nginx
    ##缓存的配置示例
    open_file_cache          max=1000 inactive=20s;
    open_file_cache_valid    30s;
    open_file_cache_min_uses 2;
    open_file_cache_errors   on;
    ````

11. **open_file_cache_errors** `on` | `off`;

    > **Default**：open_file_cache_errors off;
    > **Context**：http, server, location

    是否缓存查找时发生错误的文件一类的信息

12. **open_file_cache_min_uses** `number`;

    > **Default**：open_file_cache_min_uses 1;
    > **Context**：http, server, location

    指定命中次数；缓存命中该次数的即为活动项

13. **open_file_cache_valid time;**

    > **Default**：open_file_cache_valid 60s;
    > **Context**：http, server, location

    缓存项有效性的检查频率

14. **基于IP的访问控制功能**

    > **Context**：http, server, location, limit_except

    ````nginx
    ##可配置IPv4和IPv6
    ###deny 禁止对应IP访问
    ###allow 允许对应IP访问
    location / {
        deny  192.168.1.1;
        allow 192.168.1.0/24;
        allow 10.1.1.0/16;
        allow 2001:0db8::/32;
        deny  all;
    }
    ````



#### gzip相关配置

使用gzip方法压缩响应数据，可以节约带宽

1. **gzip on|off**

   > **Default**：gzip off;
   > **Context**：http, server, location, if in location

   是否启用gzip压缩

2. **gzip_comp_level** `level`

   > **Default**：gzip_comp_level 1;
   > **Context**：http, server, location

   压缩比由低到高：1-9

3. **gzip_disable** `regex` ...;

   > **Default**：-
   > **Context**：http, server, location

   匹配到的客户端浏览器不执行压缩

4. **gzip_min_length length;**

   > **Default**：gzip_min_length 20;
   > **Context**：http, server, location

   启用压缩功能的响应报文大小阈值

5. **gzip_http_version** `1.0` | `1.1`;

   > **Default**：gzip_http_version 1.1;
   > **Context**：http, server, location

   设定启用压缩功能时，协议的最小版本

6. **gzip_buffers number size;**

   > **Default**：gzip_buffers 32 4k|16 8k;
   > **Context**：http, server, location

   支持实现压缩功能时缓冲区数量及每个缓存区的大小

7. **gzip_types mime-type ...;**

   > **Default**：gzip_types text/html;
   > **Context**：http, server, location

   指明仅对哪些类型的资源执行压缩操作；默认包含了text/html，不用再显示指定

   ````nginx
   gzip_type text/css application/javascript;
   ````

8. **gzip_vary** `on` | `off`;

   > **Default**：gzip_vary off;
   > **Context**：http, server, location

   如果启用压缩，是否在响应报文首部插入“Vary:Accept-Encoding”

9. **gzip_proxied off | expired | no-cache | no-store | private | no_last_modified | no_etag | auth | any ...;**

   > **Default**：gzip_proxied off;
   > **Context**：http, server, location

   nginx充当代理服务器时，对于后端服务器的响应报文，在何种条件下启用压缩功能。

   expered、no-cache、no-store、private：如果后端服务器的响应头的“Cache-Control”包含这四个参数的其中一个，则启用压缩功能



### 代理配置

1. **proxy_pass `URL`;**

   > **Default**：——
   > **Context**：`location`, `if in location`, `limit_except`

   ​	设置代理服务器的协议和地址以及应映射位置的可选URI。作为协议，可以指定“ `http`”或“ `https`”。地址可以指定为域名或IP地址，以及可选端口

   如：`proxy_pass http：//localhost：8000/uri/;`

2. **proxy_set_header** `*field*` `*value*`;

   > **Default**：proxy_set_header Host $proxy_host;
   > 		   proxy_set_header Connection close;
   > **Context**：`http`, `server`, `location`

   允许重新定义或者添加发往后端服务器的请求头

   <font color=red>重要的配置方式：</font>

   ```nginx
   http{
       proxy_set_header X-real-ip $remote_addr;
       proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
   }
   ```

3. **proxy_hide_header** `field`;

   > **Default**：——
   > **Context**：`http`，`server`，`location`

   ​	默认情况下，nginx不会从代理服务器对客户端的响应中传递标题字段`Date`，`Server`，`X-Pad`和`X-Accel -...`。该`proxy_hide_header`指令设置了不会传递的其他字段

   ​	<font color=blue>**即可以隐藏一些头信息不向客户端进行传递**</font>

4. **proxy_buffer** `size`;

   > **Default**：proxy_buffer 4k | 8k;
   > **Context**：`mail`， `server`

   ​	设置用于代理的缓冲区的大小。默认情况下，缓冲区大小等于一个内存页面。根据平台，它是4K或8K

5. **proxy_buffering** `on` | `off`;

   > **Default**：proxy_buffering on;
   > **Context**：`http`，`server`，`location`

   启用或禁用来自代理服务器的响应缓冲

   - 启用缓冲后，nginx会尽快从代理服务器接收响应，并将其保存到`proxy_buffer_size`和`proxy_buffers`指令设置的缓冲区中 。如果整个响应不适合内存，则可以将其中的一部分保存到磁盘上的**临时文件**中。写入临时文件由`proxy_max_temp_file_size`和 `proxy_temp_file_write_size`指令控制
   - 禁用缓冲时，响应会在收到响应时立即同步传递给客户端。nginx不会尝试从代理服务器读取整个响应。nginx一次可以从服务器接收的数据的大小由`proxy_buffer_size`指令设置

6. **proxy_redirect** default;
   **proxy_redirect** off;
   **proxy_redirect** *redirect* *replacement*;

   > **Default**：proxy_redirect `default`;
   > **Context**：`http`，`server`，`location`

   ​	如果需要修改从被代理服务器传来的应答头中的"Location"和"Refresh"字段，可以用这个指令设置

7. **proxy_connect_timeout** `time`;

   > **Default**：proxy_connect_timeout 60s;
   > **Context**：http`，`server`，`location

   定义与代理服务器建立连接的超时。应该注意，此超时通常**不会超过75秒**

   与此类似的有

   1. `proxy_send_timeout`：设置将请求传输到代理服务器的超时。**<font color=blue>仅在两个连续的写操作之间设置超时</font>**，而不是为整个请求的传输。如果代理服务器在此时间内未收到任何内容，则关闭连接
   2. `proxy_read_timout`：定义从代理服务器读取响应的超时。**<font color=blue>仅在两个连续的读操作之间设置超时</font>**，而不是为整个响应的传输。如果代理服务器在此时间内未传输任何内容，则关闭连接



#### 常用配置方式

```nginx
http{
    proxy_pass http://localhost:8080;
    proxy_redirect default;
    
    proxy_set_header Host $http_host;
    proxy_set_header X-Real-IP $remote_addr;
    proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
    
    proxy_connect_timeout 30;
    proxy_send_timeout 60;
    proxy_read_timeout 60;
    
    proxy_buffer_size 32k;
    proxy_buffering on;
    proxy_buffers 4 128k;
    proxy_busy_buffers_size 256k;
    proxy_max_temp_file_size 256k;
}
```



#### 正向代理配置

```nginx
http{
    # 指定谷歌公用的DNS解析服务器
    resolver 8.8.8.8;
    location / {
        proxy_pass http://$http_host$request_uri;
    }
}
```



#### 反向代理配置

```nginx
http{
    # location规则可随意配置
    location / {
        # 指所有的请求都转发到8080端口上
        proxy_pass http://127.0.0.1:8080;
        
        if ( $http_x_forward_for !~* "^116\.62\.103\.228" ) {
            return 403;
        }
    }
}
```



### upstream负载均衡

**upstream** `*name*` { ... }

> **Default**：——
> **Context**：http

​	定义一组服务器。服务器可以侦听不同的端口（可以混合侦听TCP和UNIX域套接字的服务器）

```nginx
http{
    # backend是虚拟服务的名字，可自行定义
    upstream backend {
        server backend1.example.com weight=5;
        server 127.0.0.1:8080 max_fails=3 fail_timeout=30s;
        server unix:/tmp/backend3;
        server backup1.example.com  backup;
	}
    server {
        listen 80;
        server_name dh
        
        location / {
            # 这里的proxy_pass的http://后面的部分要配置成 upstream的服务名称
        	proxy_pass http://dh;
    	}
    }
    
    upstream dh {
        server localhost:8088;
        server localhost:8089;
    }
}
```



#### 服务器在负载均衡调度的状态

| down         | 当前的server暂时不参与负载均衡            |
| ------------ | ----------------------------------------- |
| backup       | 预留的备份服务器                          |
| max_fails    | 允许请求失败的次数                        |
| fail_timeout | 经过max_fails的失败次数后，服务暂停的时间 |
| max_conns    | 限制最大的接收的连接数                    |

```nginx
upstream dh {
    # 负载均衡不会分配到 dh1.com 这台机子
    server dh1.com down;
    # 预留备份即其他参与负载均衡的机子都提供不了服务的时候
    # 预留的机子就会顶上，由预留机子提供服务直到其他机子其中一个恢复
    # 当其他提供服务的机子恢复后,该机子会自动变回预留状态,从而由其他机子提供服务
    server dh2.com backup;
    # 表示允许失败的次数是10次，十次调用失败后，需要等待30s再重新检查该机子
    server dh3.com max_fails=10 fail_timeout=30s;
    server dh4.com max_conns=1000;
}
```



#### 调度算法

| 轮询         | 按时间顺序逐一分配到不同的后端服务器                         |
| ------------ | ------------------------------------------------------------ |
| 加权轮询     | weight值越大，分配到的访问几率越高                           |
| ip_hash      | 每个请求按访问IP的hash结果分配，这样来自同一个IP的请求将会固定访问一个后端服务器 |
| url_hash     | 按照访问URL的hash结果来分配请求，这样每个URL定向到同一个后端服务器 |
| least_conn   | 最少连接数，哪个机器的连接数少就分发给哪个机器               |
| hash关键数值 | hash自定义的key来分发请求                                    |

```nginx
# 加权方式(weight默认是1)
upstream dh {
    server dh1.com down weight=8;
    server dh2.com backup weight=6;
    server dh3.com max_fails=10 fail_timeout=30s weight=1;
}

# ip_hash方式
upstream dh {
    ip_hash;
    
    server dh1.com down;
    server dh2.com backup;
    server dh3.com max_fails=10 fail_timeout=30s;
}

# url_hash方式 (其实也就是采用了hash 关键字的形式)
upstream dh {
    hash $request_uri;
    
    server dh1.com down;
    server dh2.com backup;
    server dh3.com max_fails=10 fail_timeout=30s;
}

#  least_conn方式
# 指定组应使用负载平衡方法，其中请求以最少数量的活动连接传递给服务器
#同时考虑服务器的权重。如果有多个这样的服务器，则使用加权循环平衡方法依次尝试它们
upstream dh {
    least_conn;
    
    server dh1.com down;
    server dh2.com backup;
    server dh3.com max_fails=10 fail_timeout=30s;
}
```





### 配置文件总览

```nginx
#定义Nginx运行的用户和用户组
user dh daigroup;

#nginx进程数，建议设置为等于CPU总核心数
worker_processes 8;
 
#全局错误日志定义类型，[ debug | info | notice | warn | error | crit ]
#默认是 crit 级别
error_log /usr/local/nginx/logs/error.log info;

#进程pid文件
pid /usr/local/nginx/logs/nginx.pid;

#指定进程可以打开的最大描述符：数目
#工作模式与连接数上限
#这个指令是指当一个nginx进程打开的最多文件描述符数目，理论值应该是最多打开文件数（ulimit -n）与nginx进程数相除，但是nginx分配请求并不是那么均匀，所以最好与ulimit -n 的值保持一致
#现在在linux 2.6内核下开启文件打开数为65535，worker_rlimit_nofile就相应应该填写65535
#这是因为nginx调度时分配请求到进程并不是那么的均衡，所以假如填写10240，总并发量达到3-4万时就有进程可能超过10240了，这时会返回502错误
worker_rlimit_nofile 65535;

events
{
    #参考事件模型，use [ kqueue | rtsig | epoll | /dev/poll | select | poll ]; 
    #epoll模型是Linux 2.6以上版本内核中的高性能网络I/O模型，linux建议epoll，如果跑在FreeBSD上面，就用kqueue模型。
    #补充说明：
    #与apache相类，nginx针对不同的操作系统，有不同的事件模型
    #A）标准事件模型
    #Select、poll属于标准事件模型，如果当前系统不存在更有效的方法，nginx会选择select或poll
    #B）高效事件模型
    #Kqueue：使用于FreeBSD 4.1+, OpenBSD 2.9+, NetBSD 2.0 和 MacOS X.使用双处理器的MacOS X系统使用kqueue可能会造成内核崩溃。
    #Epoll：使用于Linux内核2.6版本及以后的系统。
    #/dev/poll：使用于Solaris 7 11/99+，HP/UX 11.22+ (eventport)，IRIX 6.5.15+ 和 Tru64 UNIX 5.1A+。
    #Eventport：使用于Solaris 10。 为了防止出现内核崩溃的问题， 有必要安装安全补丁。
    use epoll;

    #单个进程最大连接数（最大连接数=连接数*进程数）
    #根据硬件调整，和前面工作进程配合起来用，尽量大，但是别把cpu跑到100%就行。每个进程允许的最多连接数，理论上每台nginx服务器的最大连接数为 worker_processes*worker_connections
    worker_connections 65535;

    #keepalive超时时间
    keepalive_timeout 60;

    #客户端请求头部的缓冲区大小。这个可以根据你的系统分页大小来设置，一般一个请求头的大小不会超过1k，不过由于一般系统分页都要大于1k，所以这里设置为分页大小。
    #分页大小可以用命令getconf PAGESIZE 取得。
    #[root@dh ~]# getconf PAGESIZE
    #4096
    #但也有client_header_buffer_size超过4k的情况，但是client_header_buffer_size该值必须设置为“系统分页大小”的整倍数。
    client_header_buffer_size 4k;

    #这个将为打开文件指定缓存，默认是没有启用的，max指定缓存数量，建议和打开文件数一致，inactive是指经过多长时间文件没被请求后删除缓存。
    open_file_cache max=65535 inactive=60s;

    #这个是指多长时间检查一次缓存的有效信息。
    #语法:open_file_cache_valid time 默认值:open_file_cache_valid 60 使用字段:http, server, location 这个指令指定了何时需要检查open_file_cache中缓存项目的有效信息.
    open_file_cache_valid 80s;

    #open_file_cache指令中的inactive参数时间内文件的最少使用次数，如果超过这个数字，文件描述符一直是在缓存中打开的，如上例，如果有一个文件在inactive时间内一次没被使用，它将被移除。
    #语法:open_file_cache_min_uses number 默认值:open_file_cache_min_uses 1 使用字段:http, server, location  这个指令指定了在open_file_cache指令无效的参数中一定的时间范围内可以使用的最小文件数,如果使用更大的值,文件描述符在cache中总是打开状态.
    open_file_cache_min_uses 1;
    
    #语法:open_file_cache_errors on | off 默认值:open_file_cache_errors off 使用字段:http, server, location 这个指令指定是否在搜索一个文件时记录cache错误.
    open_file_cache_errors on;
}
 
#设定http服务器，利用它的反向代理功能提供负载均衡支持
http
{
    #文件扩展名与文件类型映射表
    include mime.types;
    #默认文件类型
    default_type application/octet-stream;
    #默认编码
    charset utf-8;

    #服务器名字的hash表大小
    #保存服务器名字的hash表是由指令server_names_hash_max_size 和server_names_hash_bucket_size所控制的。参数hash bucket size总是等于hash表的大小，并且是一路处理器缓存大小的倍数。在减少了在内存中的存取次数后，使在处理器中加速查找hash表键值成为可能。如果hash bucket size等于一路处理器缓存的大小，那么在查找键的时候，最坏的情况下在内存中查找的次数为2。第一次是确定存储单元的地址，第二次是在存储单元中查找键 值。因此，如果Nginx给出需要增大hash max size 或 hash bucket size的提示，那么首要的是增大前一个参数的大小.
    server_names_hash_bucket_size 128;

    #客户端请求头部的缓冲区大小。这个可以根据你的系统分页大小来设置，一般一个请求的头部大小不会超过1k，不过由于一般系统分页都要大于1k，所以这里设置为分页大小。分页大小可以用命令getconf PAGESIZE取得。
    client_header_buffer_size 32k;

    #客户请求头缓冲大小。nginx默认会用client_header_buffer_size这个buffer来读取header值，如果header过大，它会使用large_client_header_buffers来读取。
    large_client_header_buffers 4 64k;

    #设定通过nginx上传文件的大小
    client_max_body_size 8m;

    #开启高效文件传输模式，sendfile指令指定nginx是否调用sendfile函数来输出文件，对于普通应用设为 on，如果用来进行下载等应用磁盘IO重负载应用，可设置为off，以平衡磁盘与网络I/O处理速度，降低系统的负载。注意：如果图片显示不正常把这个改成off。
    #sendfile指令指定 nginx 是否调用sendfile 函数（zero copy 方式）来输出文件，对于普通应用，必须设为on。如果用来进行下载等应用磁盘IO重负载应用，可设置为off，以平衡磁盘与网络IO处理速度，降低系统uptime。
    sendfile on;

    #开启目录列表访问，合适下载服务器，默认关闭。
    autoindex on;

    #此选项允许或禁止使用socke的TCP_CORK的选项，此选项仅在使用sendfile的时候使用
    tcp_nopush on;
     
    tcp_nodelay on;

    #长连接超时时间，单位是秒
    keepalive_timeout 120;

    #FastCGI相关参数是为了改善网站的性能：减少资源占用，提高访问速度。下面参数看字面意思都能理解。
    fastcgi_connect_timeout 300;
    fastcgi_send_timeout 300;
    fastcgi_read_timeout 300;
    fastcgi_buffer_size 64k;
    fastcgi_buffers 4 64k;
    fastcgi_busy_buffers_size 128k;
    fastcgi_temp_file_write_size 128k;

    #gzip模块设置
    gzip on; #开启gzip压缩输出
    gzip_min_length 1k;    #最小压缩文件大小
    gzip_buffers 4 16k;    #压缩缓冲区
    gzip_http_version 1.0;    #压缩版本（默认1.1，前端如果是squid2.5请使用1.0）
    gzip_comp_level 2;    #压缩等级
    gzip_types text/plain application/x-javascript text/css application/xml;    #压缩类型，默认就已经包含textml，所以下面就不用再写了，写上去也不会有问题，但是会有一个warn。
    gzip_vary on;

    #开启限制IP连接数的时候需要使用
    #limit_zone crawler $binary_remote_addr 10m;

    #负载均衡配置
    upstream www.dh.com {
     
        #upstream的负载均衡，weight是权重，可以根据机器配置定义权重。weigth参数表示权值，权值越高被分配到的几率越大。
        server 192.168.80.121:80 weight=3;
        server 192.168.80.122:80 weight=2;
        server 192.168.80.123:80 weight=3;

        #nginx的upstream目前支持4种方式的分配
        #1、轮询（默认）
        #每个请求按时间顺序逐一分配到不同的后端服务器，如果后端服务器down掉，能自动剔除。
        #2、weight
        #指定轮询几率，weight和访问比率成正比，用于后端服务器性能不均的情况。
        #例如：
        #upstream bakend {
        #    server 192.168.0.14 weight=10;
        #    server 192.168.0.15 weight=10;
        #}
        #2、ip_hash
        #每个请求按访问ip的hash结果分配，这样每个访客固定访问一个后端服务器，可以解决session的问题。
        #例如：
        #upstream bakend {
        #    ip_hash;
        #    server 192.168.0.14:88;
        #    server 192.168.0.15:80;
        #}
        #3、fair（第三方）
        #按后端服务器的响应时间来分配请求，响应时间短的优先分配。
        #upstream backend {
        #    server server1;
        #    server server2;
        #    fair;
        #}
        #4、url_hash（第三方）
        #按访问url的hash结果来分配请求，使每个url定向到同一个后端服务器，后端服务器为缓存时比较有效。
        #例：在upstream中加入hash语句，server语句中不能写入weight等其他的参数，hash_method是使用的hash算法
        #upstream backend {
        #    server squid1:3128;
        #    server squid2:3128;
        #    hash $request_uri;
        #    hash_method crc32;
        #}

        #tips:
        #upstream bakend{#定义负载均衡设备的Ip及设备状态}{
        #    ip_hash;
        #    server 127.0.0.1:9090 down;
        #    server 127.0.0.1:8080 weight=2;
        #    server 127.0.0.1:6060;
        #    server 127.0.0.1:7070 backup;
        #}
        #在需要使用负载均衡的server中增加 proxy_pass http://bakend/;

        #每个设备的状态设置为:
        #1.down表示单前的server暂时不参与负载
        #2.weight为weight越大，负载的权重就越大。
        #3.max_fails：允许请求失败的次数默认为1.当超过最大次数时，返回proxy_next_upstream模块定义的错误
        #4.fail_timeout:max_fails次失败后，暂停的时间。
        #5.backup： 其它所有的非backup机器down或者忙的时候，请求backup机器。所以这台机器压力会最轻。

        #nginx支持同时设置多组的负载均衡，用来给不用的server来使用。
        #client_body_in_file_only设置为On 可以讲client post过来的数据记录到文件中用来做debug
        #client_body_temp_path设置记录文件的目录 可以设置最多3层目录
        #location对URL进行匹配.可以进行重定向或者进行新的代理 负载均衡
    }
       
    #虚拟主机的配置
    server
    {
        #监听端口
        listen 80;

        #域名可以有多个，用空格隔开
        server_name www.dh.com dh.cn;
        index index.html index.htm index.php;
        root /data/dh;
         
        #图片缓存时间设置
        location ~ .*.(gif|jpg|jpeg|png|bmp|swf)$
        {
            #缓存时间
            expires 10d;
        }
         
        #JS和CSS缓存时间设置
        location ~ .*.(js|css)?$
        {
            expires 1h;
        }
         
        #日志格式设定
        #$remote_addr与$http_x_forwarded_for用以记录客户端的ip地址；
        #$remote_user：用来记录客户端用户名称；
        #$time_local： 用来记录访问时间与时区；
        #$request： 用来记录请求的url与http协议；
        #$status： 用来记录请求状态；成功是200，
        #$body_bytes_sent ：记录发送给客户端文件主体内容大小；
        #$http_referer：用来记录从那个页面链接访问过来的；
        #$http_user_agent：记录客户浏览器的相关信息；
        #通常web服务器放在反向代理的后面，这样就不能获取到客户的IP地址了，通过$remote_add拿到的IP地址是反向代理服务器的iP地址。反向代理服务器在转发请求的http头信息中，可以增加x_forwarded_for信息，用以记录原有客户端的IP地址和原来客户端的请求的服务器地址。
        log_format combined '$remote_addr - $remote_user [$time_local] "$request" '
        '$status $body_bytes_sent "$http_referer" '
        '"$http_user_agent" $http_x_forwarded_for';
         
        #定义本虚拟主机的访问日志
        access_log  /usr/local/nginx/logs/host.access.log  main;
        access_log  /usr/local/nginx/logs/host.access.404.log  log404;
         
        #对 "/" 启用反向代理
        location / {
            proxy_pass http://127.0.0.1:88;
            proxy_redirect off;
            proxy_set_header X-Real-IP $remote_addr;
             
            #后端的Web服务器可以通过X-Forwarded-For获取用户真实IP
            proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
             
            #以下是一些反向代理的配置，可选。
            proxy_set_header Host $host;

            #允许客户端请求的最大单文件字节数
            client_max_body_size 10m;

            #缓冲区代理缓冲用户端请求的最大字节数，
            #如果把它设置为比较大的数值，例如256k，那么，无论使用firefox还是IE浏览器，来提交任意小于256k的图片，都很正常。如果注释该指令，使用默认的client_body_buffer_size设置，也就是操作系统页面大小的两倍，8k或者16k，问题就出现了。
            #无论使用firefox4.0还是IE8.0，提交一个比较大，200k左右的图片，都返回500 Internal Server Error错误
            client_body_buffer_size 128k;

            #表示使nginx阻止HTTP应答代码为400或者更高的应答。
            proxy_intercept_errors on;

            #后端服务器连接的超时时间_发起握手等候响应超时时间
            #nginx跟后端服务器连接超时时间(代理连接超时)
            proxy_connect_timeout 90;

            #后端服务器数据回传时间(代理发送超时)
            #后端服务器数据回传时间_就是在规定时间之内后端服务器必须传完所有的数据
            proxy_send_timeout 90;

            #连接成功后，后端服务器响应时间(代理接收超时)
            #连接成功后_等候后端服务器响应时间_其实已经进入后端的排队之中等候处理（也可以说是后端服务器处理请求的时间）
            proxy_read_timeout 90;

            #设置代理服务器（nginx）保存用户头信息的缓冲区大小
            #设置从被代理服务器读取的第一部分应答的缓冲区大小，通常情况下这部分应答中包含一个小的应答头，默认情况下这个值的大小为指令proxy_buffers中指定的一个缓冲区的大小，不过可以将其设置为更小
            proxy_buffer_size 4k;

            #proxy_buffers缓冲区，网页平均在32k以下的设置
            #设置用于读取应答（来自被代理服务器）的缓冲区数目和大小，默认情况也为分页大小，根据操作系统的不同可能是4k或者8k
            proxy_buffers 4 32k;

            #高负荷下缓冲大小（proxy_buffers*2）
            proxy_busy_buffers_size 64k;

            #设置在写入proxy_temp_path时数据的大小，预防一个工作进程在传递文件时阻塞太长
            #设定缓存文件夹大小，大于这个值，将从upstream服务器传
            proxy_temp_file_write_size 64k;
        }
         
         
        #设定查看Nginx状态的地址
        location /NginxStatus {
            #开启查看Nginx状态模块
            stub_status on;
            access_log on;
            auth_basic "NginxStatus";
            auth_basic_user_file confpasswd;
            #htpasswd文件的内容可以用apache提供的htpasswd工具来产生。
        }
         
        #本地动静分离反向代理配置
        #所有jsp的页面均交由tomcat或resin处理
        location ~ .(jsp|jspx|do)?$ {
            proxy_set_header Host $host;
            proxy_set_header X-Real-IP $remote_addr;
            proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
            proxy_pass http://127.0.0.1:8080;
        }
         
        #所有静态文件由nginx直接读取不经过tomcat或resin
        location ~ .*.(htm|html|gif|jpg|jpeg|png|bmp|swf|ioc|rar|zip|txt|flv|mid|doc|ppt|
        pdf|xls|mp3|wma)$
        {
            #缓存时间
            expires 15d; 
        }
         
        location ~ .*.(js|css)?$
        {
            expires 1h;
        }
    }
}
```

