# 服务熔断

## 雪崩效应

​		假设微服务A调用微服务B和微服务C，微服务B和微服务C又调用其它的微服务，这就是所谓的 **“扇出”**。如果扇出的链路上某个微服务的调用响应时间过长或者不可用，对微服务A的调用就会占用越来越多的系统资源，进而引起系统崩溃

​		对于高流量的应用来说，一个模块的实例失败之后，但是这个实例仍然会接收新的流量，而且这个有问题的实例还有继续的调用其他模块或者被其他模块进行调用，这就会导致级联的故障，从而演变成服务雪崩



## 熔断机制

​		熔断机制是应对雪崩效应的一种微服务***链路保护机制*** ；<font color=red>**服务调用方**</font> 可以自己进行判断某些服务反应慢或者存在大量超时的情况时，能够主动熔断，防止整个系统被拖垮

​        在微服务架构中，当扇出链路的某个微服务***不可用或者响应时间太长*** 时，会进行服务的降级，进而熔断该节点微服务的调用，**快速返回错误的响应信息**。当检测到该节点微服务调用响应正常后，恢复调用链路。

​		Hystrix 会监控微服务间调用的状况，当失败的调用到一定阈值，缺省是5秒内20次调用失败，就会启动熔断机制



# 服务降级

## 简介

​		当系统整体资源快不够的时候，忍痛将部分服务暂时关闭，带渡过难关后，再重新开启

​		所谓降级，一般是从 **整体负荷** 考虑，当某个服务熔断之后，服务器将不再被调用，此时客户端可以自己准备一个本地的 `fallback` 回调，返回一个缺省值。这样做虽然服务水平下降，但好歹可用，比直接挂掉好

​		服务降级一般会在 **服务的调用方** 进行降级逻辑的代买实现；因为可以有效的处理服务提供方因为网络或者宕机的原因无法响应任何请求的情况下，服务调用方仍然可用（不管是服务提供方还是调用方，都可以设置服务降级，这个依照业务进行设置，但一般会在调用方设置）



## 发生服务降级的情况

1. 服务端程序运行异常，给调用方返回 `fallback`
2. 服务调用超时，给调用方返回 `fallback`
3. 服务熔断触发的服务降级
4. 线程池 / 信号量 打满也会导致服务降级



# 降级和熔断的异同

## 相同点

- 目的很一致，都是从**可用性可靠性** 着想，为防止系统的整体缓慢甚至崩溃，采用的技术手段
- 最终表现类似，对于两者来说，最终让用户体验到的是某些功能暂时不可达或不可用
- 粒度一般都是服务级别，当然也有不少更细粒度的做法，比如做到数据持久层（允许查询，不允许增删改）
- 自治性要求很高，熔断模式一般都是服务基于策略的自动触发，降级虽说可人工干预，但在微服务架构下，完全靠人显然不可能，开关预置、配置中心都是必要手段



## 区别

1. 触发原因不太一样
   - 服务熔断一般是某个服务（下游服务）**故障** 引起
   - 服务降级一般是从***整体负荷考虑***

2. 实现方式不太一样
   - 服务降级具有 **代码侵入性（由控制器完成/或自动降级）**
   - 熔断一般称为**自我熔断**

3. 管理目标的层次不太一样
   - 熔断其实是一个框架级的处理，每个微服务都需要（无层级之分）
   - 降级一般需要对业务有层级之分（比如降级一般是从最外围服务开始）



# Hystrix

## 简介

​		在分布式系统中，每个服务都可能会调用很多其他服务，被调用的那些服务就是 **依赖服务**，有的时候某些依赖服务出现故障也是很正常的

​		`Hystrix` 可以在分布式系统中对服务间的调用进行控制，加入一些 **调用延迟** 或者 **依赖故障** 的 **容错机制**。`Hystrix` 通过将依赖服务进行 **资源隔离**，进而阻止某个依赖服务出现故障时在整个系统所有的依赖服务调用中进行蔓延；同时`Hystrix`还提供故障时的 `fallback` 降级机制。提升分布式系统的可用性和稳定性

​		Hystrix 使用自己的线程池，和主应用服务器的线程池隔离，如果调用的时间过长，就会停止调用。不同的命令或者命令组能够被配置使用它们各自的线程池，可以隔离不同的服务

​		Hystrix 使用服务降级建议服务提供方和调用方都要设置好 `fallback`；因为 Hystrix 触发熔断之后，会利用  `fallback` 进行降级处理返回制定好的响应信息 



## Hystrix设计原则

1. 对依赖服务调用时出现的调用延迟和调用失败进行 **控制和容错保护**
   - 阻止任何一个依赖服务耗尽所有的资源，比如 tomcat 中的所有线程资源
   - 使用资源隔离技术，比如 `bulkhead`（舱壁隔离技术）、`swimlane`（泳道技术）、`circuit breaker`（断路技术）来限制任何一个依赖服务的故障的影响

2. 在复杂的分布式系统中，阻止某一个依赖服务的故障在整个系统中蔓延。比如某一个服务故障了，导致其它服务也跟着故障

3. 避免请求排队和积压，采用限流、`fail fast` （快速失败）和快速恢复来控制故障  

4. 提供 `fallback` 降级机制来应对故障

5. 支持近实时的监控、报警以及运维操作（Dashboard）
   - 保护依赖服务调用的所有故障情况，而不仅仅只是网络故障情况
   - 通过近实时的 统计/监控/报警 功能，来提高故障发现的速度
   - 通过近实时的属性和配置 **热修改** 功能，来提高故障处理和恢复的速度



## POM配置

```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-netflix-hystrix</artifactId>
</dependency>
```



## Hystrix服务降级

​		服务降级是当<font color=blue>**服务提供方出现异常，宕机或者网络超时**等问题</font>时的一种优雅的处理方式，可以返回一些友好的提示给调用方，而不是报错信息

​		服务降级可以在**调用方**和**提供方**进行设置，但是一般的业务都要求<font color=red>在服务的**调用方进行设置降级处理**，这样可以应对提供方宕机时的降级处理情况，然后**服务提供方配置服务熔断和服务降级**</font>



### 配置文件配置

```yml
feign:
  hystrix:
    enabled: true
```



### 单独对某个方法进行降级

```java
@Service
public class DeptmentServiceImpl implements DeptmentService {
    @Autowired
    private DeptmentMapper deptmentMapper;

    @Override
    @HystrixCommand(fallbackMethod = "saveFallbackMethod")
    public int saveDeptment(Department department) {
        return deptmentMapper.insert(department);
    }

    public int saveFallbackMethod(Department department) {
        System.out.println("fall back");
        return -1;
    }
}
```



### 使用统一的降级方法

```java
@RestController
@RequestMapping("/consumer/dept")
/**
 * 使用 @DefaultProperties 注解配置统一的降级方法
 * 然后必须在每个方法上加上@HystrixCommand注解
 **/
@DefaultProperties(defaultFallback = "defaultFallback")
public class DeptmentController {

    @Autowired
    private DeptmentService deptmentService;

    @PostMapping("/addDept.do")
    @HystrixCommand
    public CommonResult saveDeptment(Department department) {
        return deptmentService.saveDeptment(department);
    }

    @GetMapping("/getAllDept.do")
    @HystrixCommand
    public CommonResult getAllDept() {
        return deptmentService.getAllDepartment();
    }


    public CommonResult defaultFallback() {
        return CommonResult.getFallback();
    }

}
```



### Feign 使用降级

```java
/**
 * 使用这种方式, DeptmentService上不能有 @RequestMapping("/dept") 注解, 否则报错
 **/
@Service
@FeignClient(name = "DEPARTMENT-PROVIDER", fallback = DeptmentFallback.class)
public interface DeptmentService {

    @PostMapping("/dept/addDept.do")
    CommonResult saveDeptment(Department department);

    @GetMapping("/dept/getAllDept.do")
    CommonResult getAllDepartment();

}

@Component("departmentFallback")
public class DeptmentFallback implements DeptmentService {
    @Override
    public CommonResult saveDeptment(Department department) {
        return CommonResult.getFallback();
    }
    @Override
    public CommonResult getAllDepartment() {
        return CommonResult.getFallback();
    }
}
```

<font color=red>**方式二：**</font>

```java
@Service
@FeignClient(name = "DEPARTMENT-PROVIDER", fallbackFactory = DepartmentFallbackFactory.class)
@RequestMapping("/dept")
public interface DeptmentService {

    @PostMapping("/addDept.do")
    CommonResult saveDeptment(Department department);

    @GetMapping("/getAllDept.do")
    CommonResult getAllDepartment();

}

@Component("departmentFallbackFactory")
public class DepartmentFallbackFactory implements FallbackFactory<DeptmentService> {

    @Override
    public DeptmentService create(Throwable throwable) {
        return new DeptmentService() {
            @Override
            public CommonResult saveDeptment(Department department) {
                return CommonResult.getFallback();
            }

            @Override
            public CommonResult getAllDepartment() {
                return CommonResult.getFallback();
            }
        };
    }
}
```





## Hystrix服务熔断

​		<font color=blue>服务熔断是当某个**服务出错或者调用时间过久**时出现的降级处理，当这个**失败次数调用达到一定的阈值时**，就会触发服务熔断，进而不再调用该服务，而是快速的返回 `fallback` 的响应信息</font>

​		Hystrix实现了该熔断机制，它会监测微服务的调用情况，当失败的调用达到一定的阈值（<font color=red>默认是**5秒内20次调用失败**</font>），就会启动熔断机制



### 熔断的重要参数

1. <font color=orange>**快照时间窗**</font>
   - 断路器确定是否打开需要统计一些请求和错误数据，而 **统计的时间范围就是快照时间窗**，默认为最近的10秒
2. <font color=orange>**请求总数阈值**</font>
   - 在快照时间窗内，必须满足请求总数阈值才有资格熔断。默认为20，即在10秒内，如果 Hystrix 命令的<font color=red>调用次数不足20次</font>（也就是 `fallback` 的次数），<font color=red>即使这10秒内的请求都是超时或者异常或者其他原因失败的</font>，都不会开启熔断器
3. <font color=orange>**错误百分比阈值**</font>
   - 当请求总数在快照时间窗内超过了阈值，比如发生了30次调用，如果在这30次调用中，有15次发生了超时异常，也就是超过了 50% 的错误百分比，在默认设定的 50% 阈值的情况下，这个时候熔断器会打开 



### 熔断的流程

1. Hystrix会监测微服务的调用情况，当失败的调用达到一定的阈值（<font color=red>默认是**5秒内20次调用失败**</font>），就会启动熔断机制
2. 熔断后的 `fallback` 响应会持续一段时间（因为此时的熔断器是open的状态）
3. 然后会检测该节点微服务的调用，这里有个内部时钟MTTR（即平均故障处理时间），当打开熔断器的时间达到了MTTR，则会进入半熔断阶段，这个阶段会放一部分请求过去
4. 这部分请求会根据规则调用当前服务，如果请求成功且符合规则，那么会认为当前服务恢复了正常（也就是熔断器关闭了 close）



**<font color=orange>总结：</font>**

​		默认情况下，**当 10s 内有超过20个请求且请求的失败率达到了或超过了 50% 时**，熔断器就会被打开，打开了熔断器之后，所有的请求都不会被转发到正常流程中。当一段时间之后 （**默认是5秒**），<font color=blue>熔断器会变成半打开状态，会让**其中的一个请求进行转发**</font>，如果请求正常，那么断路器会关闭，如果请求失败，那么会继续等待一段时间后再进行转发，重复此操作



### 熔断配置

```yml
#开启熔断器
feign:
  hystrix:
    enabled: true
```



```java
@SpringBootApplication
@EnableEurekaClient
@MapperScan("com.dai.mapper")
//开启服务熔断器
@EnableCircuitBreaker
public class DeptmentProvider9004 {
    public static void main(String[] args) {
        SpringApplication.run(DeptmentProvider9004.class, args);
    }
}


@Service
public class DeptmentServiceImpl implements DeptmentService {

    @Autowired
    private DeptmentMapper deptmentMapper;
    /**
     * commandProperties里面的意思是：在10秒窗口期中有10次请求,且其中至少有6次是请求失败的,那么就开启断路器
     *
     * @param
     * @return
     */
    @HystrixCommand(
            fallbackMethod = "getAllDepartmentFallback",
            commandProperties = {
                /**
                 *   是否开启断路器
                 */
                @HystrixProperty(name = "circuitBreaker.enabled", value = "true"),
                /**
                 * 该属性用来设置在滚动时间(时间窗口期)中, 断路器熔断的最小请求数
                 */
                @HystrixProperty(name = "circuitBreaker.requestVolumeThreshold", value = "10"),
                /**
                 * 该属性用来设置当熔断器打开之后的休眠时间窗
                 */
                @HystrixProperty(name = "metrics.rollingStats.timeInMilliseconds", value = "10000"),
                /**
                 * 失败率达到多少后开启熔断器
                 */
                @HystrixProperty(name = "circuitBreaker.errorThresholdPercentage", value = "60")
            }
    )
    @Override
    public List<Department> getAllDepartment() {
        return deptmentMapper.selectList(new QueryWrapper<>());
    }

    public List<Department> getAllDepartmentFallback() {
        System.out.println("fall back");
        return null;
    }

}
```



## Hystrix工作流程

1. 创建 `HystrixCommand` （用在依赖的服务返回单个操作结果的时候）或 `HtstrixObserableCommand` （用在依赖的服务返回多个操作结果的时候） 对象

2. <font color=orange>**命令执行**</font> （也就是请求）

   - `HystrixCommand` 执行方式

     - `execute()` 同步执行，从依赖的服务返回一个单一的结果对象，或者是在发生错误的时候抛出异常
     - `queue()` 异步执行，直接返回一个 `Future` 对象，其中包含了服务执行结束时要返回的单一结果对象

   - `HtstrixObserableCommand`  执行方式

     - `observe()` 返回 `Observable` 对象，它代表了操作的多个结果，它是一个 Hot Observable （不论 “事件源” 是否有 “订阅者” ，都会在创建后对事件工作进行发布；所以对于 Hot Observable 的每一个订阅者都有可能是从事件源中途开始的，并且可能只是看到了整个操作的局部过程 ）
     - `toObservable` 返回 `Observable` 对象，但是返回的是一个 Cold Observable （没有订阅者的时候不会发布事件，而是进行等待，直到有订阅者之后才会发布事件；所以对于 Cold Observable 的订阅者，他可以保证从一开始就能看到整个操作的全部过程）

     

3. 若当前命令的请求缓存功能是被启用的，并且命中缓存了，那么缓存的结果会立即以 `Observable` 对象的形式返回

4. <font color=orange>**检查熔断器是否打开**</font>

   - 如果熔断器打开了，那么不执行命令了，而是转到 `fallback` （第8步）
   - 如果熔断器是关闭的，那么检查是否有可用的资源来执行命令

5. <font color=orange>**线程池 / 请求队列 / 信号量 是否被占满**</font>

   - 如果命令依赖服务的专有线程池和请求队列、或者信号量已经被占满，那么不再执行命令，转到 `fallback` （第8步）

6. Hystrix 会根据设置去采用不同的方式去请求依赖服务

   - `HystrixCommand.run()` 返回一个单一的结果，或者抛出异常
   - `HystrixObservableCommand.construct()` 返回一个 Observable 对象来发射多个结果，或者通过 `onError` 发送错误通知

7. Hystrix 会将 “成功”、“失败”、“异常”、“超时”等信息报告给断路器，而断路器会维护一组计数器来统计这些数据，断路器会使用这些统计数据来决定是否要将断路器打开（对某个依赖服务的请求进行熔断）

8. <font color=orange>**当命令执行失败时，Hystrix 会进入 fallback 尝试降级处理**</font>；发生降级的常见情况：

   - 熔断器是打开的状态
   - 当前命令的线程池、请求队列或者信号量被占满的时候
   - 抛出异常，请求超时

9. 当 Hystrix 命令执行成功之后，它会将处理结果直接返回或者是以 Observable 的形式返回

> ​		如果没有实现降级逻辑或者在降级逻辑的方法内抛出异常的时候，Hystrix依然会返回一个 Observable 对象 ，但是它没有任何数据，而是通过 onError 方法通知命令立即中断请求，并通过 onError 方法将失败的异常信息返回给调用者





## Hystrix常用配置

### HystrixProperty的配置

```java
@Override
@HystrixCommand(
    fallbackMethod = "getByIdFallback",
    commandProperties = {
            /**
             * 设置执行 HystrixCommand 的隔离策略, 参数可选两个：THREAD, SEMAPHORE
             *      THREAD表示使用线程池大小限制并发执行
             *      SEMAPHORE表示使用信号池计数限制并发执行
             */
            @HystrixProperty(name = "execution.isolation.strategy", value = "THREAD"),

            /**
             *  设置线程超时时间为3秒钟, 默认是 1 秒钟
             */
            @HystrixProperty(name = "execution.isolation.thread.timeoutInMilliseconds", value = "3000"),

            /**
             *   当选择使用 信号池 隔离策略时, 用来设置信号量的大小(即最大并发数) ; 默认是10
             */
            @HystrixProperty(name = "execution.isolation.semaphore.maxConcurrentRequests", value = "10"),

            /**
             *   是否启用超时时间, 默认启用
             */
            @HystrixProperty(name = "execution.timeout.enabled", value = "true"),

            /**
             *   执行超时时, 是否中断线程执行, 默认是true
             */
            @HystrixProperty(name = "execution.isolation.thread.interruptOnTimeout", value = "true"),

            /**
             *   执行取消的时候, 是否中断线程, 默认false
             */
            @HystrixProperty(name = "execution.isolation.thread.interruptOnFutureCancel", value = "false"),

            /**
             *   允许执行回调方法(即fallback方法)的最大并发线程数, 默认10
             */
            @HystrixProperty(name = "fallback.isolation.semaphore.maxConcurrentRequests", value = "10"),

            /**
             *   服务降级是否启用, 是否执行回调方法(即fallback方法)
             */
            @HystrixProperty(name = "fallback.enabled", value = "true"),

            /**
             *   是否开启断路器
             */
            @HystrixProperty(name = "circuitBreaker.enabled", value = "true"),

            /**
             * 该属性用来设置在滚动时间(时间窗口期)中, 断路器熔断的最小请求数; 默认20
             *      例如: 默认20的情况下, 如果滚动时间是10秒, 则在10秒内收到19个请求, 即使这19个请求都失败了, 也不会发生熔断
             *      即在滚动时间内要达到该值设定的最小请求数,并且还要达到失败率才会触发熔断
             */
            @HystrixProperty(name = "circuitBreaker.requestVolumeThreshold", value = "20"),

            /**
             * 该属性用来设置当熔断器打开之后的休眠时间窗, 默认是 5000ms, 即5秒
             *
             * 当触发熔断之后, 会经历休眠时间, 即5秒后, 会将熔断器设置成 半打开 的状态, 此时会尝试转发一个请求:
             *      1. 如果请求继续失败, 那么熔断器继续打开, 重复经历休眠时间
             *      2. 如果请求成功, 那么熔断器就会关闭
             */
            @HystrixProperty(name = "circuitBreaker.sleepWindowInMilliseconds", value = "5000"),

            /**
             * 失败率达到多少后开启熔断器; 默认 50%
             *     即滚动时间内请求数达到设定值, 且该设定值内的请求数错误率达到50%, 那么熔断器就会打开
             */
            @HystrixProperty(name = "circuitBreaker.errorThresholdPercentage", value = "50"),

            /**
             * 滚动时间窗, 该时间用于熔断器判断健康时需要收集信息的持续时间
             */
            @HystrixProperty(name = "metrics.rollingStats.timeInMilliseconds", value = "10000"),

            /**
             * 该属性用来设置滚动时间窗统计指标信息时划分的 "桶" 的数量,
             * 熔断器在收集指标的时候会根据设置的时间窗长度划分成多个 "桶" 来累计各度量值, 每个 "桶" 记录一段时间内的采集指标
             * (类似滑动窗口算法)
             *
             *      例如：10秒内拆分成10个"桶"来进行收集,  timeInMilliseconds必须能被numBuckets整除, 否则抛出异常
             */
            @HystrixProperty(name = "metrics.rollingStats.numBuckets", value = "10"),

            /**
             * 该属性用来设置对命令执行的延迟是否使用百分位数来跟踪和计算
             * 如果设置为false, 则所有统计都返回 -1
             */
            @HystrixProperty(name = "metrics.rollingPercentile.enabled", value = "true"),

            /**
             * 该属性用来设置百分位统计的滚动窗口的持续时间, 默认60000ms, 即60s
             */
            @HystrixProperty(name = "metrics.rollingPercentile.timeInMilliseconds", value = "60000"),

            /**
             * 该属性用来设置百分位统计滚动窗口中使用 "桶" 的数量
             */
            @HystrixProperty(name = "metrics.rollingPercentile.numBuckets", value = "6"),

            /**
             * 该属性用来设置百分位统计的执行过程中每个 "桶" 中保留的最大执行数.
             * 如果在滚动时间窗内发生超过该值的执行次数, 就从最初的位置开始重写.
             *
             *      例如: 设置了默认值100, 滑动窗口为10秒内, 若在10秒内一个 "桶" 中发生了500次执行, 那么该"桶" 中只保留最后的100次执行的统计
             *      另外如果增加该值大小, 那么将会增加内存的消耗, 并增加排序百分位数所需的计算时间
             */
            @HystrixProperty(name = "metrics.rollingPercentile.bucketSize", value = "100"),

            /**
             * 该属性用来设置采集影响断路器状态的健康快照 (请求的成功和错误的百分比) 的隔离等待时间, 默认500ms
             */
            @HystrixProperty(name = "metrics.healthSnapshot.intervalInMilliseconds", value = "500"),

            /**
             * 是否开启请求缓存
             */
            @HystrixProperty(name = "requestCache.enabled", value = "true"),

            /**
             * HystrixCommand 的执行和事件是否打印到 HystrixRequestLog 中
             */
            @HystrixProperty(name = "requestLog.enabled", value = "true"),

            /**
             * 断路器强制打开, 打开这个参数将始终打开熔断器
             */
            @HystrixProperty(name = "circuitBreaker.forceOpen", value = "false"),

            /**
             * 断路器强制关闭
             */
            @HystrixProperty(name = "circuitBreaker.forceClosed", value = "false")
    },
    threadPoolProperties = {
            /**
             * 执行Feign请求的线程池的核心线程数, 默认10, 就是最大并发量
             */
            @HystrixProperty(name = "coreSize", value = "10"),

            /**
             * 设置线程池的阻塞队列长度,
             * 当设置为 -1 时, 队列使用SynchronousQueue队列; 否则就使用 LinkedBlockingQueue
             */
            @HystrixProperty(name = "maxQueueSize", value = "-1"),

            /**
             * 设置队列拒绝阈值, 通过该参数可以使得即使队列没有达到最大值, 还能拒绝后续请求
             *      这个设置是为了使用LinkedBlockingQueue 无界队列时, 能够调整拒绝后续请求的队列大小
             */
            @HystrixProperty(name = "queueSizeRejectionThreshold", value = "5")
    }
)
public Department getById(String id) {
    return deptmentMapper.selectById(id);
}
```

- `execution.isolation.strategy` 

  - 设置执行 HystrixCommand 的隔离策略, 参数可选两个：THREAD, SEMAPHORE

  - THREAD表示使用线程池大小限制并发执行
  - SEMAPHORE表示使用信号池计数限制并发执行

  

- `execution.isolation.thread.timeoutInMilliseconds`

  - 设置线程超时时间为3秒钟, 默认是 1 秒钟

  

- `execution.isolation.semaphore.maxConcurrentRequests`

  - 当选择使用 信号池 隔离策略时, 用来设置信号量的大小(即最大并发数) ; 默认是10

  

- `execution.timeout.enabled`

  - 是否启用超时时间，默认启用

  

- `execution.isolation.thread.interruptOnTimeout`

  - 执行超时时，是否中断线程执行，默认是true

  

- `execution.isolation.thread.interruptOnFutureCancel`

  - 执行取消的时候，是否中断线程，默认false

  

- `fallback.isolation.semaphore.maxConcurrentRequests`

  - 允许执行回调方法(即fallback方法)的最大并发线程数, 默认10

  

- `fallback.enabled`

  - 服务降级是否启用, 是否执行回调方法(即fallback方法)

  

- `circuitBreaker.enabled`

  - 是否开启断路器

  

- `circuitBreaker.requestVolumeThreshold`

  - 该属性用来设置在滚动时间(时间窗口期)中, 断路器熔断的最小请求数; 默认20
  - 例如: 默认20的情况下, 如果滚动时间是10秒, 则在10秒内收到19个请求, 即使这19个请求都失败了, 也不会发生熔断
  - 即在滚动时间内要达到该值设定的最小请求数,并且还要达到失败率才会触发熔断

  

- `circuitBreaker.sleepWindowInMilliseconds`

  - 该属性用来设置当熔断器打开之后的休眠时间窗, 默认是 5000ms, 即5秒
  - 当触发熔断之后, 会经历休眠时间, 即5秒后, 会将熔断器设置成 半打开 的状态, 此时会尝试转发一个请求:
    1. 如果请求继续失败, 那么熔断器继续打开, 重复经历休眠时间
    2. 如果请求成功, 那么熔断器就会关闭

  

- `circuitBreaker.errorThresholdPercentage`

  - 失败率达到多少后开启熔断器; 默认 50%
  - 即滚动时间内请求数达到设定值, 且该设定值内的请求数错误率达到50%, 那么熔断器就会打开

  

- `metrics.rollingStats.timeInMilliseconds`

  - 滚动时间窗, 该时间用于熔断器判断健康时需要收集信息的持续时间

  

- `metrics.rollingStats.numBuckets`

  - 该属性用来设置滚动时间窗统计指标信息时划分的 "桶" 的数量，熔断器在收集指标的时候会根据设置的时间窗长度划分成多个 "桶" 来累计各度量值, 每个 "桶" 记录一段时间内的采集指标（类似滑动窗口算法）
                     *      例如：10秒内拆分成10个"桶"来进行收集,  timeInMilliseconds必须能被numBuckets整除, 否则抛出异常

  

- `metrics.rollingPercentile.enabled`

  - 该属性用来设置对命令执行的延迟是否使用百分位数来跟踪和计算
  - 如果设置为false，则所有统计都返回 -1

  

- `metrics.rollingPercentile.timeInMilliseconds`

  - 该属性用来设置百分位统计的滚动窗口的持续时间, 默认60000ms, 即60s

  

- `metrics.rollingPercentile.numBuckets`

  - 该属性用来设置百分位统计滚动窗口中使用 "桶" 的数量

  

- `metrics.rollingPercentile.bucketSize`

  - 该属性用来设置百分位统计的执行过程中每个 "桶" 中保留的最大执行数.
  - 如果在滚动时间窗内发生超过该值的执行次数, 就从最初的位置开始重写.
    - 例如: 设置了默认值100, 滑动窗口为10秒内, 若在10秒内一个 "桶" 中发生了500次执行, 那么该"桶" 中只保留最后的100次执行的统计。另外如果增加该值大小, 那么将会增加内存的消耗, 并增加排序百分位数所需的计算时间

  

- `metrics.healthSnapshot.intervalInMilliseconds`

  - 该属性用来设置采集影响断路器状态的健康快照 (请求的成功和错误的百分比) 的隔离等待时间, 默认500ms

  

- `requestCache.enabled`

  - 是否开启请求缓存

  

- `requestLog.enabled`

  - HystrixCommand 的执行和事件是否打印到 HystrixRequestLog 中

  

- `circuitBreaker.forceOpen`

  - 断路器强制打开, 打开这个参数将始终打开熔断器

- `circuitBreaker.forceClosed`

  - 断路器强制关闭



### 全局Hystrix配置

```yml
hystrix:
  execution:
    isolation:
      strategy: THREAD
      thread:
        timeoutInMilliseconds: 3000
```

​		配置和上述 `HystrixProperty` 中的配置一致，只是 `HystrixProperty` 作用的粒度更细，而这里作用于整个微服务的所有接口

​		配置参考 `HystrixCommandProperties.java` 类有详细的解释，对于这样的配置是没有代码提示的，为什么这样配置能够起作用：

```java
//1.构造函数
protected HystrixCommandProperties(HystrixCommandKey key) {
    this(key, new Setter(), "hystrix");
}
protected HystrixCommandProperties(HystrixCommandKey key, HystrixCommandProperties.Setter builder) {
    this(key, builder, "hystrix");
}

//2. 配置类中的主要干事的构造函数, 其中 propertyPrefix 就是上面的 hystrix
protected HystrixCommandProperties(HystrixCommandKey key, HystrixCommandProperties.Setter builder, String propertyPrefix) {
    this.key = key;
    this.circuitBreakerEnabled = getProperty(propertyPrefix, key, "circuitBreaker.enabled", builder.getCircuitBreakerEnabled(), default_circuitBreakerEnabled);

    //...省略其中的配置
}

//3. 获取配置文件的参数, 这里的拼接就能得出配置文件应该如何配置 hystrix 的全局配置了
private static HystrixProperty<Boolean> getProperty(String propertyPrefix, HystrixCommandKey key, String instanceProperty, Boolean builderOverrideValue, Boolean defaultValue) {
    return forBoolean()
        .add(propertyPrefix + ".command." + key.name() + "." + instanceProperty, builderOverrideValue)
        .add(propertyPrefix + ".command.default." + instanceProperty, defaultValue)
        .build();
}
```



# HystrixDashboard仪表盘

​		仪表盘提供了准实时的调用监控信息，Hystrix 会持续的记录所有通过 Hystrix 发起的请求的执行信息，并以统计报表和图形的形式展现给用户（包括每秒执行的请求数，其中成功了多少和失败了多少）



## 仪表盘的图形说明

![HystrixDashboard监控说明图](.\HystrixDashboard监控说明图.png)



1. <font color=orange>**实心圆**</font>
   - 颜色用来表示实例的健康程度，它的健康度从 绿色<黄色<橙色<红色 递减
   - 大小用来表示实例的请求流量变化，流量越大那么实心圆越大。可以通过这个实心圆的大小能够快速的在大量的实例中发现故障实例和高请求压力的实例
2. <font color=orange>**曲线**</font>
   - 用来记录 2 分钟内的流量的相对变化，可以使用它来观察到流量的上升和下降趋势
3. <font color=orange>**服务请求频率**</font>
   - 表示每秒的并发请求数
4. <font color=orange>**Hosts**</font>
   - 表示节点个数
5. <font color=orange>**6色数字**</font>
   - 绿色：请求成功数
   - 蓝色：熔断器熔断的次数
   - 青色：错误的请求次数
   - 橙色：请求超时的次数
   - 紫色：线程池拒绝线程的次数
   - 红色：请求失败/异常的次数





## POM配置

```xml
<!-- hystrix dashboard -->
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-netflix-hystrix-dashboard</artifactId>
</dependency>
<!--监控 (被监控的服务也一定要加这个) -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
```

需要在启动类上加上 `@EnableHystrixDashboard`



## SpringCloud版本升级导致的问题

```java
/**
*  此配置是为了服务监控而配置，与服务容错本省无关，spring-cloud 升级后的坑
*  ServletRegistrationBean 因为 spring boot 的默认路径不是 "/hystrix.stream"
*  只要在自己的项目里配置下文的 servlet 就可以了
* @return
*/
@Bean
public ServletRegistrationBean getServlet() {
    HystrixMetricsStreamServlet streamServlet = new HystrixMetricsStreamServlet();
    ServletRegistrationBean registrationBean = new ServletRegistrationBean(streamServlet);
    registrationBean.setLoadOnStartup(1);
    registrationBean.addUrlMappings("/hystrix.stream");
    registrationBean.setName("HystrixMetricsStreamServlet");
    return registrationBean;
}
```



**这里的内容配置在服务提供者**

**这里的内容配置在服务提供者**

**这里的内容配置在服务提供者**



