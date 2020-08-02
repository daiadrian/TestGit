## SpringBoot简介

> spring开发存在的一些不便：
>
> ​		比如开启spring的一些特性时（事务管理，springMVC），需要用到XML或者Java的显示配置；配置Servlet和过滤器等也是需要在web.xml或者Servlet初始化代码中进行显示配置
>
> ​		尽管spring加了很多组件扫描去减少配置量，但是还是无法避免一些配置

### springboot核心

1. <font color=red>自动配置</font>
   - 针对一些常用的应用功能，springboot能够自动提供相关配置；自动化配置springMVC
2. <font color=red>起步依赖</font>
   - 告诉springboot需要一些什么功能，就能引入需要的库；实现这个功能的是springboot中包含了pom文件
   - 例如引入`spring-boot-starter-web` 就会引入web相关的依赖



### Actuator监控

| HTTP方法 | 路径            | 作用                                                     |
| -------- | --------------- | -------------------------------------------------------- |
| GET      | /autoconfig     | 提供一份自动配置报告，描述哪些自动配置通过了，哪些没通过 |
| GET      | /configprops    | 描述配置属性如何注入Bean                                 |
| GET      | /beans          | 描述应用程序上下文所有的bean，以及他们之间的关系         |
| GET      | /dump           | 获取线程活动快照                                         |
| GET      | /env            | 获取全部环境属性                                         |
| GET      | /env/{name}     | 根据名称获取环境属性                                     |
| GET      | /health         | 报告应用程序的健康指标，由HealthIndicator实现类提供      |
| GET      | /info           | 获取应用程序定制信息，由info的配置属性提供               |
| GET      | /mappings       | 描述全部URI路径，以及它们和控制器的映射关系              |
| GET      | /metrics        | 报告各种应用程序度量信息，比如内存用量和HTTP请求计数     |
| GET      | /metrics/{name} | 获取指定名称的应用程序度量信息                           |
| **POST** | **/shutdown**   | 关闭应用程序；要求endpoints.shutdown.enabled设置为true   |
| GET      | /trace          | 提供基本的HTTP请求跟踪信息（时间戳，HTTP头等）           |

​		要引入Actuator功能，需要在POM文件加入该依赖 `spring-boot-starter-actuator`

​		默认 actuator 的根路径是 /actuator；即访问 /beans 需要访问的路径是 /actuator/beans；可以通过配置更改根路径：`management.endpoints.web.base-path=/actuator` 

#### 装配信息beans

/beans 得到的装配信息的解析：

1. bean：Bean的名称或者ID
2. resource：.class文件的物理位置，通常是一个URL，指向构建出的JAR文件
3. dependencies：当前bean注入的Bean ID列表
4. scope：bean的作用域
5. type：Bean的Java类型



#### 自动配置报告/autoconfig

​		自动配置报告会报告为什么有这个bean和没有这个bean