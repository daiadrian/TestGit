### BIO

​		使用BIO，数据准备和数据从内核空间拷贝到用户空间两个阶段都是阻塞的 

 		BIO最大的缺点：浪费资源，只能处理少量的连接，线程数随着连接数线性增加，连接越多线程越多，直到抗不住 

### AIO



### NIO

 NIO，New IO（不是No Blocking IO），内部是基于多路复用的IO模型

​		使用NIO则多条连接的数据准备阶段会阻塞在select上，数据从内核空间拷贝到用户空间依然是阻塞的

​		因为第一阶段并不是连接本身处于阻塞阶段，所以通常来说NIO也可以看作是**同步非阻塞IO**

#### Channel

 		Channel 是IO操作的一种连接。 它代表到实体的开放连接，这个实体可以是硬件，文件，网络套接字，或者程序组件；并且可以执行一个或多个不同的IO操作，例如，读或写

##### Channel和BIO的区别

- Channel 可以同时支持读和写，而 Stream 只能支持单向的读或写（所以分成InputStream和OutputStream）
- Channel 支持异步读写，Stream通常只支持同步
- 以 Channel 为中心，从 Channel中 读出数据到Buffer，从 Buffer 中往 Channel 写入数据



