Zookeeper知识点汇总



## 什么是Zookeeper？

​		Zookeeper是一个开源的**分布式的，为分布式应用提供协调服务**的Apache项目。

​		ZooKeeper是一个经典的**<font color=red>分布式数据一致性解决方案</font>**，致力于为分布式应用提供一个高性能、高可用，且具有**严格顺序访问**控制能力的分布式协调服务

​		分布式应用程序可以基于ZooKeeper实现数据发布与订阅、负载均衡、命名服务、分布式协调与通知、集群管理、Leader选举、分布式锁、分布式队列等功能。



### zookeeper的一些特点

1. 顺序一致性：从同一个client客户端发来的请求，会按其发送的顺序来执行
2. 原子性：一次数据处理要么全部成功，要么全部失败
3. 数据一致性：每个Server保存一份相同的数据，客户端无论连接到哪个Server，数据都是一致的
4. 实时性：在一定时间范围内，客户端能够读取到最新的数据



### Zookeeper的数据模型

​		ZK会维护一个具有层次关系的树状的数据结构，每个树节点称为一个ZNode。每个ZNode默认能够存储1MB的数据，每个ZNode都可以通过路径唯一标识

​		一个ZNode既能在它下面创建子节点，作为路径标识的一部分，同时该节点也能存储数据；主要存放分布式应用的配置信息和状态信息等

​		每个ZNode节点都有各自的版本号，当节点数据发生变化是，那该节点的版本号也会累加（乐观锁的机制）

**节点类型**

- 持久（Persistent）：客户端和服务器断开连接后，创建的节点不会被删除
- 短暂（Ephemeral）：客户端和服务器断开连接后，创建的节点会自动删除

> ​		创建ZNode节点的时候可以设置顺序标识，ZNode名称后会附加一个顺序号，这个顺序号是单调递增的计数器，并且是由父节点来维护的
>
> ​		注意：在分布式系统中，顺序号可以被用于所有事件的全局排序；客户端可以通过顺序号来推断事件的执行顺序



### Zookeeper的应用场景

1. **统一命名服务**

   > 在分布式环境下，对应用/服务进行统一的命名，会便于识别
   >
   > 对外只显示服务的名称，通过节点去访问对应IP的服务

2. **统一配置管理**

   > ​		集群中一般要求所有节点的配置信息是一致的，例如Kafka集群。并且对配置文件修改后，能够快速更新到各个节点上
   >
   > ​		可以将配置信息写入ZNode中，各个客户端监听该配置信息的状态，一旦ZNode中的数据发生改变，可以及时通知各个客户端将最新的配置信息更新到系统中

3. **统一集群管理**

   > ​		服务节点动态上下线，当ZK中注册的服务下线时，客户端能够实时的得到下线通知；这里可以通过ZK的监听器去监听节点的动态新增/删除

4. **分布式锁**

   > 

5. 软负载均衡

   > ZK记录节点上的服务，可以让访问数最少的服务器去处理最新的客户端请求



## ZK安装

> ​		注意：下面操作没有设置环境变量，如果设置的环境变量，那么可以在**全局环境**下直接使用zkServer.sh或者zkCli.sh
>
> 设置方法：
>
> ```shell
> vim /etc/profile
> export ZOOKEEPER_HOME=/opt/zookeeper
> export PATH=$PATH:$ZOOKEEPER_HOME/bin
> ```

### 单机模式

1. 解压tar.gz文件到指定目录下（/opt）

   ```shell
   tar -zxvf zookeeper-3.4.10.tar.gz
   ```

2. 复制conf下的zoo_sample.cfg为新文件zoo.cfg，并且在zookeeper的主目录下创建data文件夹，并在配置文件中设置data目录和dataLog目录

   ```shell
   cd /opt/zookeeper/conf
   cp zoo_sample.cfg zoo.cfg
   cd /opt/zookeeper
   mkdir data
   vim /opt/zookeeper/conf/zoo.cfg
   	dataDir=/opt/zookeeper/data
   	dataLogDir=/opt/zookeeper/dataLog
   ```

3. 启动zk

   ```shell
   #启动zk
   bin/zkServer.sh start
   #关闭zk
   bin/zkServer.sh stop
   
   ##查看zk的状态
   bin/zkServer.sh status
   
   ##查看zk进程是否启动
   jps
   	4020 Jps
   	4001 QuorumPeerMain
   ```



### 分布式部署

1. 在data目录下创建myid文件，在文件上添加ZK编号

   ```shell
   touch myid
   	1
   ##其他ZK的机子上需要添加不同的编号
   ```

2. 修改 zoo.cfg 配置文件

   ```shell
   vim zoo.cfg
   	#######################cluster##########################
       server.1=zk1:2888:3888
       server.2=zk2:2888:3888
       server.3=zk3:2888:3888
       
   #######################cluster##########################
   server.1=localhost:2881:3881
   server.2=localhost:2882:3882
   server.3=localhost:2883:3883
   ```

   配置文件解析：

   - server后面的数字就是 myid 文件制定的编号
   - zk1 是你服务器的 ip 地址
   - 2888 是zk集群的信息交换端口（不一定是2888，可自行指定）
   - 3888 是zk集群中Leader节点挂了之后重新选择Leader节点时进行通信的端口（同样可自行选择其他端口）



## 深入学习Zookeeper

### ZK配置文件

1. tickTime

   通信心跳数，Zookeeper**服务器与客户端**心跳时间，单位毫秒

   > ​		Zookeeper使用的基本时间，服务器之间或客户端与服务器之间维持心跳的时间间隔，也就是每个tickTime时间就会发送一个心跳，时间单位为毫秒
   >
   > ​		它用于心跳机制，并且设置最小的session（会话）超时时间为两倍心跳时间（session的最小超时时间是2*tickTime）

2. initLimit

   集群中主从服务器之间的**初始通信时限**

   > ​		集群中的Follower跟随者服务器与Leader领导者服务器之间初始连接时能容忍的最多心跳数（tickTime的数量），用它来限定集群中的Zookeeper服务器连接到Leader的时限

3. syncLimit

    集群中主从服务器之间的**同步通信时限**

   > ​		集群中Leader与Follower之间的最大响应时间单位，假如响应超过syncLimit * tickTime，Leader认为Follwer死掉，从服务器列表中删除Follwer

4. dataDir

   数据文件目录+数据持久化路径

5. dataLogDir

    日志文件目录，如果不配置则使用dataDir的目录进行日志的存放

6. clientPort 

   监听客户端连接的端口，默认是2181



### ZK集群

#### 选举机制中的基础概念

1. 服务器ID

   > 即myid文件中的编号；编号越大，权重越大

2. Zxid，数据ID

   > 服务器中存放的最大数据ID；值越大说明该数据越新，权重越大

3. Epoch：逻辑时钟

   > ​	投票的次数（轮数），同一轮投票过程中的逻辑时钟值是相同的
   >
   > ​	每投完一次票这个数据就会增加，然后与接收到的其它服务器返回的投票信息中的数值相比，根据不同的值做出不同的判断

4. Server状态：选举状态

   - LOOKING，竞选状态
   - FOLLOWING，随从状态，同步leader状态，参与投票
   - OBSERVING，观察状态,同步leader状态，不参与投票
   - LEADING，领导者状态



#### 选举简易流程

​	目前有5台服务器，每台服务器均没有数据，它们的编号分别是1,2,3,4,5,按编号依次启动，它们的选择举过程如下：

1. 服务器1启动，给自己投票，然后发投票信息，由于其它机器还没有启动所以它收不到反馈信息，服务器1的状态一直属于**Looking（竞选状态）**
2. 服务器2启动，给自己投票，同时与之前启动的服务器1交换结果；由于服务器2的编号比服务器1的大，所以服务器2胜出；但此时投票数没有大于半数，所以**两个服务器的状态依然是LOOKING**
3. 服务器3启动，给自己投票，同时与之前启动的服务器1和2交换信息，由于服务器3的编号最大，所以服务器3胜出，此时投票数正好**大于半数**，所以服务器3成为Leader，服务器1和2成为Follower，状态变成FOLLOWING
4. 服务器4启动，给自己投票，同时与之前启动的服务器1,2,3交换信息，尽管服务器4的编号大，但服务器3的状态已经是Leading，所以服务器4也是Follower
5. 服务器5启动，逻辑同服务器4



**几种情况的选举**

1. 一台宕机重启的机器加入已有环境，如果已有环境中已经存在Leader，那么该机器会变成Follwoer

2. 一台机器加入正在投票中的环境

   所有server都会接受优先级最高的投票，最高优先级最高的选票当选，选举结束

3. 当集群中多数机器宕机重启

   ​	存活的服务发现不满足多数派，改变状态为LOOKING，投票轮数+1，然后重新开始投票，会按照优先级的选举投票直至结束

   - 逻辑时钟小的选举结果被忽略，重新投票
   - 统一逻辑时钟后，数据 version 大的胜出
   - 数据 version 相同的情况下，server id 大的胜出

以上，只要有超过半数的机器存活，最终会完成投票



#### 选举机制（半数机制）

​		集群中**半数以上**机器存活，集群可用。所以Zookeeper适合安装奇数台服务器

​		Zookeeper虽然在配置文件中并没有指定Master和Slave。Zookeeper工作时只有一个节点为Leader，其他则为Follower，Leader是通过内部的选举机制**临时产生**的



### zkClient

````shell
#启动zk客户端
bin/zkCli.sh
##指定访问server
zkCli.sh -server 192.168.1.1:2181

###常用操作
create /dh "shuaige"
get /dh

##创建短暂节点
create -e /dh/fat "fat"
##创建带顺序号的持久节点
create -s /dh/handsome "handsome"
##修改节点的值
set /dh/fat "littlefat" 0
##删除节点
delete /dh/fat 0
##递归删除
rmr /dh
````

#### 常用操作命令

| 命令基本语法                | 功能描述                                                     |
| --------------------------- | ------------------------------------------------------------ |
| help                        | 显示所有操作命令                                             |
| ls path [watch]             | 使用 ls 命令来查看当前znode中所包含的内容                    |
| ls2 path [watch]            | 查看当前节点数据并能看到更新次数等数据（详细数据）           |
| create [选项]               | 普通创建一个zNode    -s ：含有序列    -e：临时（重启或者超时消失） |
| get path [watch]            | 获得节点的值                                                 |
| set path  data  [version]   | 设置（修改）节点的具体值，可根据版本号对节点的值进行修改（推荐使用版本号修改，乐观锁机制） |
| stat                        | 查看节点状态                                                 |
| delete path  data [version] | 删除节点，可根据版本号对节点进行删除（推荐使用版本号删除，乐观锁机制） |
| rmr                         | 递归删除节点                                                 |

#### Stat结构体

```shell
[zk: localhost:2181(CONNECTED) 1] ls2 /
[zookeeper]
cZxid = 0x0
ctime = Thu Jan 01 08:00:00 CST 1970
mZxid = 0x0
mtime = Thu Jan 01 08:00:00 CST 1970
pZxid = 0x0
cversion = -1
dataVersion = 0
aclVersion = 0
ephemeralOwner = 0x0
dataLength = 0
numChildren = 1
```

1. cZxid：创建节点的事务zxid

   ​		每次修改ZooKeeper状态都会收到一个zxid形式的时间戳，也就是ZooKeeper事务ID

   ​		事务ID是ZooKeeper中所有修改总的次序。每个修改都有唯一的zxid，如果zxid1小于zxid2，那么zxid1在zxid2之前发生

2. ctime：znode被创建的毫秒数(从1970年开始)

3. mzxid：znode最后更新的事务zxid

4. mtime：znode最后修改的毫秒数(从1970年开始)

5. pZxid：znode最后更新的子节点zxid

6. cversion：znode子节点变化版本号，znode子节点修改次数

7. dataversion：znode数据变化版本号

8. aclVersion：znode访问控制列表的变化版本号

9. ephemeralOwner：如果是临时节点，这个是znode拥有者的session id。如果不是临时节点则是0

10. <font color=red>dataLength：znode的数据长度</font>

11. <font color=red>numChildren：znode子节点数量</font>



#### watcher机制

​		watcher是zk中的监听器机制，父节点或者子节点的增删改操作都能够触发watcher事件

**事件类型**

1. 父节点创建：NodeCreated
2. 父节点数据修改：NodeDataChanged
3. 父节点删除：NodeDeleted
4. 创建了子节点：NodeChildrenChanged
5. 删除子节点：NodeChildrenChanged
6. 修改子节点不触发任何事件

**watcher机制的使用场景**

​		统一的配置管理，可以监听配置信息的节点，当配置信息的节点数据发生变化的时候触发客户端更新配置的操作



#### ACL权限控制

​		ACL（access control lists），可以针对节点设置读写等权限，可以保障数据的安全性；如果没有权限，则会抛出异常

zk的acl通过 [scheme​ : id : ​permissions] 的形式来构成权限的列表

- scheme：代表采用的某种权限机制
- id：代表允许访问的用户
- permissions：权限组合字符串（有crdwa）
  - c：CREATE，创建子节点
  - r：READ，获取节点/子节点
  - d：DELETE，删除子节点
  - w：WRITE，设置节点数据
  - a：ADMIN，设置权限

> 权限示例：
>
> world：world:anyone:[permissions]
>
> auth：auth:user:password:[permissions] 代表认证登录，需要注册的用户有操作权限即可
>
> digest：digest:username:BASE64(SHA1(password)):[permissions] 表示需要对密码进行加密才可以访问
>
> ip：ip:ip地址:[permissions]  可以限制指定ip才能访问该节点

**ACL的命令行操作**

1. getAcl：获取某个节点的acl权限信息

2. setAcl：设置某个节点的acl权限信息

   > 示例：（1和2是等价的）
   >
   > 1. setAcl /path auth:dai:dai:cdrwa
   >
   > 2. setAcl /path digest:dai:password:cdrwa
   >
   > 上面两个操作后需要进行addauth操作后才能够对 /path 进行操作
   >
   > 3. setAcl /path ip:192.168.1.1:cdrwa
   >
   > 设置ip后，只有指定ip的客户端才有权限去访问该节点

3. addauth：输入认证授权信息，注册时输入明文密码，在zk系统中，密码都是以加密的形式存在的

   > 参照2的示例： 执行 addauth digest:dai:dai  登录后能获取上面设置节点的操作权限  
   >
   > ​		注意：要使用 dai 用户前需要先注册 dai 用户才可以设置成功，注册用户同样是addAuth命令：addauth digest  dai:dai
   >
   > ​		注意：使用 digest 来设置权限时，查看加密后的password可以通过getAcl，比如：
   >
   > getAcl /dh
   >
   > ​	'digest，'dai:password（此处的password是加密后的显示）



## Java使用ZK

### 原生ZK的API

#### 引入POM

````xml
<!-- zookeeper -->
<dependency>
    <groupId>org.apache.zookeeper</groupId>
    <artifactId>zookeeper</artifactId>
    <!-- 版本与ZK版本一致 -->
    <version>3.4.11</version>
</dependency>
````

#### 连接ZK

````java
public class ZKDemo implements Watcher {
		
	final static Logger log = LoggerFactory.getLogger(ZKConnect.class);

	public static final String zkServerPath = "192.168.1.1:2181";
//	public static final String zkServerPath = "192.168.1.1:2181,192.168.1.2:2182,192.168.1.3:2183";
	public static final Integer timeout = 5000;
	
	public static void main(String[] args) throws Exception {
		/**
		 * 客户端和zk服务端链接是一个异步的过程
		 * 当连接成功后后，客户端会收的一个watch通知
		 * 
		 * 参数：
		 * connectString：连接服务器的ip字符串，
		 * 		比如: "192.168.1.1:2181,192.168.1.2:2181,192.168.1.3:2181"
		 * 		可以是一个ip，也可以是多个ip，一个ip代表单机，多个ip代表集群
		 * 		
		 * sessionTimeout：超时时间，心跳收不到了，那就超时
		 * 
		 * watcher：通知事件，如果有对应的事件触发，则会收到一个通知；如果不需要，那就设置为null
		 * 
		 * canBeReadOnly：可读，当这个物理机节点断开后，还是可以读到数据的，只是不能写，
		 * 					 此时数据被读取到的可能是旧数据，此处建议设置为false，不推荐使用
		 * 
		 * sessionId：会话的id
		 * 
		 * sessionPasswd：会话密码	
		 *          当会话丢失后，可以依据 sessionId 和 sessionPasswd 重新获取会话
		 * 
		 */
		ZooKeeper zk = new ZooKeeper(zkServerPath, timeout, new ZKDemo());
		log.warn("连接状态：{}", zk.getState());
        
        // 开始会话重连
        long sessionId = zk.getSessionId();
		byte[] sessionPassword = zk.getSessionPasswd();
		log.warn("开始会话重连...");
		ZooKeeper zkSession = new ZooKeeper(zkServerPath, 
											timeout, 
											new ZKDemo(), 
											sessionId, 
											sessionPassword);
		log.warn("重新连接状态zkSession：{}", zkSession.getState());
		new Thread().sleep(1000);
		log.warn("重新连接状态zkSession：{}", zkSession.getState());
	}

	@Override
	public void process(WatchedEvent event) {
		log.warn("接受到watch通知：{}", event);
	}
}
````

#### ZK的节点操作

````java
public class ZKNode implements Watcher {

	private ZooKeeper zookeeper = null;
	public static final String zkServerPath = "192.168.1.1:2181";
	public static final Integer timeout = 5000;
	
	public ZKNodeExist() {}
	
	public ZKNodeExist(String connectString) {
		try {
			zookeeper = new ZooKeeper(connectString, timeout, new ZKNode());
		} catch (IOException e) {
			e.printStackTrace();
			if (zookeeper != null) {
				try {
					zookeeper.close();
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
			}
		}
	}

	public static void main(String[] args) throws Exception {
		ZKNode zkServer = new ZKNode(zkServerPath);
		/**
		 *查询节点是否存在
		 * 参数：
		 * path：节点路径
		 * watch：watch
		 */
		Stat stat = zkServer.getZookeeper().exists("/dh-demo", true);
		if (stat != null) {
			System.out.println("查询的节点版本为dataVersion：" + stat.getVersion());
		} else {
			System.out.println("该节点不存在...");
		}
		/**
		  * 同步或者异步创建节点，都不支持子节点的递归创建，异步有一个callback函数
		  * 参数：
		  * path：创建的路径
		  * data：存储的数据的byte[]
		  * acl：控制权限策略
		  * 			Ids.OPEN_ACL_UNSAFE --> world:anyone:cdrwa
		  * 			CREATOR_ALL_ACL --> auth:user:password:cdrwa
		  * createMode：节点类型, 是一个枚举
		  * 			PERSISTENT：持久节点
		  * 			PERSISTENT_SEQUENTIAL：持久顺序节点
		  * 			EPHEMERAL：临时节点
		  * 			EPHEMERAL_SEQUENTIAL：临时顺序节点
		  */ 	       
        String ctx = "{'create':'success'}";
		zookeeper.create("/dh-path", "data".get, Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT, new CreateCallBack(), ctx);
             
        /**
         *获取一个节点的值
		 * 参数：
		 * path：节点路径
		 * watch：true或者false，注册一个watch事件
		 * stat：状态
		 */
        Stat stat = new Stat();
		byte[] resByte = zkServer.getZookeeper().getData("/dh", true, stat);
		String result = new String(resByte);
		System.out.println("当前值:" + result);
        
        /**
         *设置节点的数据
		 * 参数：
		 * path：节点路径
		 * data：数据
		 * version：数据状态
		 */
		stat  = zkServer.getZookeeper().setData("/dh-path", "data".getBytes(), stat.getVersion());
		System.out.println(status.getVersion());
        
        /**
         *删除节点(带回调的)
		 * 参数：
		 * path：节点路径
		 * version：数据状态
		 */
        String ctx = "{'delete':'success'}";
		zkServer.getZookeeper().delete("/dh-path", stat.getVersion(), new DeleteCallBack(), ctx);
	}
	
   /**
	 * 对节点的监听
	 *
	 * @param event
	 */
	@Override
	public void process(WatchedEvent event) {
		if (event.getType() == EventType.NodeCreated) {
			System.out.println("节点创建");
			countDown.countDown();
		} else if (event.getType() == EventType.NodeDataChanged) {
			System.out.println("节点数据改变");
			countDown.countDown();
		} else if (event.getType() == EventType.NodeDeleted) {
			System.out.println("节点删除");
			countDown.countDown();
		}
	}
	
	public ZooKeeper getZookeeper() {
		return zookeeper;
	}
	public void setZookeeper(ZooKeeper zookeeper) {
		this.zookeeper = zookeeper;
	}
}
````

**CallBack回调**

````java
//父节点的watcher机制回调
public class CreateCallBack implements StringCallback {
	@Override
	public void processResult(int rc, String path, Object ctx, String name) {
		System.out.println("创建节点: " + path);
		//ctx 就是create方法传入的ctx参数
		System.out.println((String)ctx);
	}
}
//子节点的watcher机制回调
public class ChildrenCallBack implements ChildrenCallback {
	@Override
	public void processResult(int rc, String path, Object ctx, List<String> children) {
		for (String s : children) {
			System.out.println(s);
		}
		System.out.println("ChildrenCallback:" + path);
		System.out.println((String)ctx);	
	}
}
````

#### ACL权限

````java
public class ZKNodeAcl implements Watcher {
	private ZooKeeper zookeeper = null;
	public static final String zkServerPath = "192.168.1.1:2181";
	public static final Integer timeout = 5000;
	public ZKNodeAcl() {}
	
	public ZKNodeAcl(String connectString) {
		try {
			zookeeper = new ZooKeeper(connectString, timeout, new ZKNodeAcl());
		} catch (IOException e) {
			e.printStackTrace();
			if (zookeeper != null) {
				try {
					zookeeper.close();
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
			}
		}
	}
	
	public void createZKNode(String path, byte[] data, List<ACL> acls) {
		String result = "";
		try {
			result = zookeeper.create(path, data, acls, CreateMode.PERSISTENT);
			System.out.println("创建节点：\t" + result + "\t成功...");
		} catch (KeeperException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} 
	}
	
	public static void main(String[] args) throws Exception {
		ZKNodeAcl zkServer = new ZKNodeAcl(zkServerPath);
		/**
		 * ======================  创建node start  ======================  
		 */
		// acl 任何人都可以访问
		zkServer.createZKNode("/dh-acl", "data".getBytes(), Ids.OPEN_ACL_UNSAFE);
		
		// 自定义用户认证访问
        /**
		 * DigestAuthenticationProvider.generateDigest(String str)
         * 这个方法是zk客户端提供的加密方式 BASE61(SHA1(password))
		 */
		List<ACL> acls = new ArrayList<ACL>();
		Id acl1 = new Id("digest", DigestAuthenticationProvider.generateDigest("dai1:123456"));
        
		Id acl2 = new Id("digest", DigestAuthenticationProvider.generateDigest("dai2:123456"));
		acls.add(new ACL(Perms.ALL, acl1));
		acls.add(new ACL(Perms.READ, acl2));
		acls.add(new ACL(Perms.DELETE | Perms.CREATE, acl2));
		zkServer.createZKNode("/dh-acl/test", "testdigest".getBytes(), acls);
		
		// 注册过的用户必须通过addAuthInfo才能操作节点，参考命令行 addauth
		zkServer.getZookeeper().addAuthInfo("digest", "dai1:123456".getBytes());
		zkServer.createZKNode("/dh-acl/test/childtest", "childtest".getBytes(), Ids.CREATOR_ALL_ACL);
		Stat stat = new Stat();
		byte[] data = zkServer.getZookeeper().getData("/dh-acl/test", false, stat);
		System.out.println(new String(data));
		zkServer.getZookeeper().setData("/dh-acl/test", "data".getBytes(), stat.getVersion());
		
		// ip方式的acl
		List<ACL> aclsIP = new ArrayList<ACL>();
		Id ipId = new Id("ip", "192.168.1.10");
		aclsIP.add(new ACL(Perms.ALL, ipId));
		zkServer.createZKNode("/dh-acl/iptest", "data".getBytes(), aclsIP);

		// 验证ip是否有权限
        zkServer.getZookeeper().getData("/dh-acl/test", false, stat);
		zkServer.getZookeeper().setData("/dh-acl/iptest", "setdata".getBytes(), stat.getVersion());
		byte[] data = zkServer.getZookeeper().getData("/aclimooc/iptest6", false, stat);
		System.out.println(new String(data));
		System.out.println(stat.getVersion());
	}

	public ZooKeeper getZookeeper() {
		return zookeeper;
	}
	public void setZookeeper(ZooKeeper zookeeper) {
		this.zookeeper = zookeeper;
	}
	@Override
	public void process(WatchedEvent event) {
	}
}
````

### Apache Curator

#### ZK连接及节点操作

````java
public class ZKCurator {

    private static String zkServerPath = "127.0.0.1:2181";

    public static void main(String[] args) throws Exception {
        /**
         * 同步创建zk示例，原生api是异步的
         *
         * curator链接zookeeper的策略:ExponentialBackoffRetry
         *              baseSleepTimeMs：初始sleep的时间
         *              maxRetries：最大重试次数
         *              maxSleepMs：最大重试时间
         */
		RetryPolicy retryPolicy1 = new ExponentialBackoffRetry(1000, 5);

        /**
         * curator链接zookeeper的策略:RetryNTimes
         *              n：重试的次数
         *              sleepMsBetweenRetries：每次重试间隔的时间
         */
        RetryPolicy retryPolicy = new RetryNTimes(3, 5000);

        /**
         * curator链接zookeeper的策略:RetryOneTime
         *              sleepMsBetweenRetry:每次重试间隔的时间
         */
		RetryPolicy retryPolicy2 = new RetryOneTime(3000);

        /**
         * curator链接zookeeper的策略:RetryUntilElapsed
         *              maxElapsedTimeMs:最大重试时间
         *              sleepMsBetweenRetries:每次重试间隔
         *              重试时间超过maxElapsedTimeMs,就不再重试
         */
		RetryPolicy retryPolicy3 = new RetryUntilElapsed(2000, 3000);

        CuratorFramework client = CuratorFrameworkFactory.builder()
                .connectString(zkServerPath)
                .sessionTimeoutMs(10000).retryPolicy(retryPolicy)
                .namespace("workspace").build();
        client.start();
        //判断链接是否成功
        boolean isZkCuratorStarted = client.isStarted();
        System.out.println("当前客户的状态：" + (isZkCuratorStarted ? "连接中" : "已关闭"));

        /**
         * 创建节点:
         *      creatingParentsIfNeeded : 开启递归的创建方式,不用一层一层的创建
         *      withMode  : 节点的类型
         *      withACL   : acl权限
         *      forPath   : 节点路径和数据
         *
         */
        String nodePath = "/dai";
        String str = client.create()
                            .creatingParentsIfNeeded()
                            .withMode(CreateMode.PERSISTENT)
                            .withACL(ZooDefs.Ids.OPEN_ACL_UNSAFE)
                            .forPath(nodePath, "data".getBytes());

        /**
         * 获取节点数据:
         *      storingStatIn : 把服务器端获取的状态数据存储到stat对象
         *
         */
        Stat stat = new Stat();
        byte[] data = client.getData()
                            .storingStatIn(stat)
                            .forPath(nodePath);
        System.out.println("节点" + nodePath + "的数据为: " + new String(data));
        System.out.println("该节点的版本号为: " + stat.getVersion());

        /**
         * 更新节点数据:
         *      withVersion : 数据版本
         *
         */
		client.setData()
                .withVersion(stat.getVersion())
                .forPath(nodePath, "update".getBytes());

        /**
         * 删除节点:
         *      guaranteed : 保障措施,只要客户端会话有效; 那么Curator会在后台持续进行删除操作,直到删除节点成功
         *      deletingChildrenIfNeeded : 递归删除,有子节点的情况下会将所有子节点也一并删除
         *      withVersion : 数据版本
         *
         */
        client.getData().storingStatIn(stat).forPath(nodePath);
		client.delete()
              .guaranteed()
              .deletingChildrenIfNeeded()
              .withVersion(stat.getVersion())
              .forPath(nodePath);

        /**
         * 查询子节点
         *
         */
		List<String> childNodes = client.getChildren().forPath(nodePath);
		System.out.println("开始打印子节点：");
		for (String child : childNodes) {
			System.out.println(child);
		}

        /**
         * 判断节点是否存在,如果不存在则为空
         */
		Stat statExist = client.checkExists().forPath(nodePath + "/exist");
		System.out.println(statExist);

        /**
         * watcher 事件:
         *      当使用usingWatcher的时候，监听只会触发一次，监听完毕后就销毁
         *
         */
		client.getData().usingWatcher(new MyCuratorWatcher()).forPath(nodePath);

        /**
         * watcher 事件：
         *
         *      NodeCache: 监听数据节点的变更，会触发事件
         *
         *
         */
		final NodeCache nodeCache = new NodeCache(client, nodePath);
        /**
         *  参数:
         *      buildInitial : 初始化的时候获取node的值并且缓存
         *          只有开启这个缓存后,下面的getCurrentData方法才能拿到数据
         */
		nodeCache.start(true);
		if (nodeCache.getCurrentData() != null) {
			System.out.println("节点初始化数据为：" + new String(nodeCache.getCurrentData().getData()));
		} else {
			System.out.println("节点初始化数据为空...");
		}
        /**
         * 添加节点数据监听器
         */
		nodeCache.getListenable()
                .addListener(new NodeCacheListener() {
			public void nodeChanged() throws Exception {
				if (nodeCache.getCurrentData() == null) {
					System.out.println("节点数据为空");
					return;
				}
				String data = new String(nodeCache.getCurrentData().getData());
				System.out.println("节点路径：" + nodeCache.getCurrentData().getPath() + "  数据：" + data);
			}
		});

        /**
         * 为子节点添加watcher事件
         *      PathChildrenCache: 监听数据节点的增删改，会触发事件
         *      cacheData: 是否设置缓存节点的数据状态
         *
         */
        final PathChildrenCache childrenCache = new PathChildrenCache(client, nodePath, true);
        /**
         * StartMode: 初始化方式
         *      POST_INITIALIZED_EVENT：异步初始化，初始化之后会触发事件
         *      NORMAL：异步初始化
         *      BUILD_INITIAL_CACHE：同步初始化
         */
        childrenCache.start(PathChildrenCache.StartMode.POST_INITIALIZED_EVENT);

        List<ChildData> childDataList = childrenCache.getCurrentData();
        System.out.println("当前数据节点的子节点数据列表：");
        for (ChildData child : childDataList) {
            String childData = new String(child.getData());
            System.out.println(childData);
        }
        /**
         * 添加节点数据监听器
         */
        childrenCache.getListenable()
                .addListener(new PathChildrenCacheListener() {
            public void childEvent(CuratorFramework client, PathChildrenCacheEvent event) throws Exception {
                if(event.getType().equals(PathChildrenCacheEvent.Type.INITIALIZED)){
                    System.out.println("子节点初始化ok...");
                }

                else if(event.getType().equals(PathChildrenCacheEvent.Type.CHILD_ADDED)){
                    String path = event.getData().getPath();
                    if (path.equals("dh-add")) {
                        System.out.println("添加子节点:" + event.getData().getPath());
                        System.out.println("子节点数据:" + new String(event.getData().getData()));
                    } else if (path.equals("/super/imooc/e")) {
                        System.out.println("添加不正确...");
                    }

                }else if(event.getType().equals(PathChildrenCacheEvent.Type.CHILD_REMOVED)){
                    System.out.println("删除子节点:" + event.getData().getPath());
                }else if(event.getType().equals(PathChildrenCacheEvent.Type.CHILD_UPDATED)){
                    System.out.println("修改子节点路径:" + event.getData().getPath());
                    System.out.println("修改子节点数据:" + new String(event.getData().getData()));
                }
            }
        });

        Thread.sleep(100000);

        //关闭zk客户端连接
        if (client != null) {
            client.close();
        }
        boolean isZkCuratorStarted2 = client.isStarted();
        System.out.println("当前客户的状态：" + (isZkCuratorStarted2 ? "连接中" : "已关闭"));
    }

    /**
     * watcher
     */
    public static class MyCuratorWatcher implements CuratorWatcher {
        @Override
        public void process(WatchedEvent watchedEvent) throws Exception {
            System.out.println("触发了watch 事件，节点路径：" + watchedEvent.getPath());
        }
    }
}
````

#### ACL权限

````java
public class ZKCurator {

    private static String zkServerPath = "127.0.0.1:2181";

    public static void main(String[] args) throws Exception {
        RetryPolicy retryPolicy = new RetryNTimes(3, 5000);
        CuratorFramework client = CuratorFrameworkFactory.builder()
                .connectString(zkServerPath)
                .sessionTimeoutMs(10000).retryPolicy(retryPolicy)
                .namespace("workspace").build();
        client.start();
        
        List<ACL> aclList = new ArrayList<>();
        /**
         * Id 构造参数:
         *         scheme:认证方式
         *                      world：默认方式，相当于全世界都能访问
         *                      auth：代表已经认证通过的用户
         *                      digest：即用户名:密码这种方式认证
         *                      ip：使用Ip地址认证
         */
        Id id1 = new Id("digest", DigestAuthenticationProvider.generateDigest("dai1:123456"));
        Id id2 = new Id("digest", DigestAuthenticationProvider.generateDigest("dai2:123456"));
        /**
         * ACL 构造参数:
         *          perms:五种权限:
         *                          CREATE: 能创建子节点
         *                          READ：能获取节点数据和列出其子节点
         *                          WRITE: 能设置节点数据
         *                          DELETE: 能删除子节点
         *                          ADMIN: 能设置权限
         *             Id:就是上面的Id类
         */
        aclList.add(new ACL(Perms.ALL, id1));
        aclList.add(new ACL(Perms.READ, id2));
        aclList.add(new ACL(Perms.CREATE | Perms.WRITE, id2));
        client.setACL().withACL(aclList).forPath("/dai/dh/hao");
        client.create()
                .creatingParentsIfNeeded()
                .withMode(CreateMode.PERSISTENT)
                /**
                 * withACL 第二个参数applyToParents :
                 *                      如果设置为true,那么会将这些acl权限加到创建的父节点上(递归创建)
                 */
                .withACL(aclList, true)
                .forPath("/dai/dh/hao", "data".getBytes());
        
        //关闭zk客户端连接
        if (client != null) {
            client.close();
        }
    }
````









