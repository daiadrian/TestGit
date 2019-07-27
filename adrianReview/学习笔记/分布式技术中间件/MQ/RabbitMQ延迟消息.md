# 延迟消息

例子：实现下单后超过12分钟后自动取消订单的功能，可以使用延迟消息实现，即使用队列，将消息发送到队列中，指定过期时间，达到过期时间之后发送到消费方执行取消订单的功能。

### rabbitmq_delayed_message_exchange插件

插件下载地址：

http://www.rabbitmq.com/community-plugins.html

打开网址后，ctrl + f，搜索rabbitmq_delayed_message_exchange。 


千万记住，一定选好版本号，由于我使用的是RabbitMQ 3.7.4,因此对应的rabbitmq_delayed_message_exchange插件也必须选择3.7.x的。

下载完插件后，将其放置到RabbitMQ安装目录下的plugins目录下，并使用如下命令启动这个插件：

rabbitmq-plugins enable rabbitmq_delayed_message_exchange

如果启动成功会出现如下信息：

The following plugins have been enabled: 
rabbitmq_delayed_message_exchange

启动插件成功后，记得重启一下RabbitMQ，让其生效。

