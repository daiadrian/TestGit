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



## Hystrix服务降级

​		服务降级是当服务提供方出现异常，宕机或者网络超时等问题时的一种优雅的处理方式，可以返回一些友好的提示给调用方，而不是报错信息

​		服务降级均可以在调用方和提供方进行设置，但是一般的业务要求会是在服务的调用方进行设置，这样可以应对提供方宕机时的降级处理情况



POM配置

```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-netflix-hystrix</artifactId>
</dependency>
```



配置文件配置

```yml
feign:
  hystrix:
    enabled: true
```



实际使用

1. **单独对某个方法进行降级**

```java
@Service
public class DeptmentServiceImpl implements DeptmentService {
    @Autowired
    private DeptmentMapper deptmentMapper;

    @Override
    @HystrixCommand(fallbackMethod = "saveFallbackMethod",
                    commandProperties = {
                        /**
                         *  设置线程超时时间3秒钟
                         */
                        @HystrixProperty(name = "execution.isolation.thread.timeoutInMilliseconds", value = "3000")
                    })
    public int saveDeptment(Department department) {
        return deptmentMapper.insert(department);
    }

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
                 * 请求次数
                 */
                @HystrixProperty(name = "circuitBreaker.requestVolumeThreshold", value = "10"),
                /**
                 * 时间窗口期/时间范文
                 */
                @HystrixProperty(name = "circuitBreaker.sleepWindowInMilliseconds", value = "10000"),
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

    public int saveFallbackMethod(Department department) {
        System.out.println("fall back");
        return -1;
    }
    public List<Department> getAllDepartmentFallback() {
        System.out.println("fall back");
        return null;
    }
}
```

2. **Feign 使用降级**

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



方式二：

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

​		服务熔断是当某个服务出错或者调用时间过久时出现的降级处理，当这个失败调用达到一定的阈值时，就会触发服务熔断，进而不再调用该服务，而是快速的返回 `fallback` 的响应信息

​		Hystrix实现了该熔断机制，它会监测微服务的调用情况，当失败的调用达到一定的阈值（<font color=red>默认是**5秒内20次调用失败**</font>），就会启动熔断机制



### 熔断的流程

1. Hystrix会监测微服务的调用情况，当失败的调用达到一定的阈值（<font color=red>默认是**5秒内20次调用失败**</font>），就会启动熔断机制
2. 熔断后的 `fallback` 响应会持续一段时间（因为此时的熔断器是open的状态）
3. 然后会检测该节点微服务的调用，这里有个内部时钟MTTR（即平均故障处理时间），当打开熔断器的时间达到了MTTR，则会进入半熔断阶段，这个阶段会放一部分请求过去
4. 这部分请求会根据规则调用当前服务，如果请求成功且符合规则，那么会认为当前服务恢复了正常（也就是熔断器关闭了 close）



总结：

​		默认情况下，**当 10s 内有超过20个请求且请求的失败率达到了或超过了 50% 时**，熔断器就会被打开，打开了熔断器之后，所有的请求都不会被转发到正常流程中。当一段时间之后 （**默认是5秒**），熔断器会变成半打开状态，会让其中的一个请求进行转发，如果请求正常，那么断路器会关闭，如果请求失败，那么会继续等待一段时间后再进行转发，重复此操作



### 熔断的重要参数

1. <font color=orange>**快照时间窗**</font>
   - 断路器确定是否打开需要统计一些请求和错误数据，而 **统计的时间范围就是快照时间窗**，默认为最近的10秒
2. <font color=orange>**请求总数阈值**</font>
   - 在快照时间窗内，必须满足请求总数阈值才有资格熔断。默认为20，即在10秒内，如果 Hystrix 命令的<font color=red>调用次数不足20次</font>（也就是 `fallback` 的次数），<font color=red>即使这10秒内的请求都是超时或者异常或者其他原因失败的</font>，都不会开启熔断器
3. <font color=orange>**错误百分比阈值**</font>
   - 当请求总数在快照时间窗内超过了阈值，比如发生了30次调用，如果在这30次调用中，有15次发生了超时异常，也就是超过了 50% 的错误百分比，在默认设定的 50% 阈值的情况下，这个时候熔断器会打开 





​		

