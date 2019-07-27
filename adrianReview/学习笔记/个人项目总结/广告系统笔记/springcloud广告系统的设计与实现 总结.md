springcloud广告系统的设计与实现 总结

广告系统包含

- 广告投放系统

  - 对接广告主，进行广告投放

  > 广告系统包含：
  >
  > - 用户账户
  > - 推广计划：可对应多个推广单元，推广计划即对某一类的品牌进行推广，确定推广的时间和费用等等
  > - 推广单元：与创意是多对多的关系；推广单元即对一些用户进行广告的限制，比如地域限制（例如只有广州的用户才能看到该品牌的广告），关键词限制（当客户请求中包含某些关键词才能看到这些广告的限制）等等
  > - 创意：广告的不同表现形式，比如图片，视频等等的形式。即广告创意
- 广告检索系统
- 广告计费系统（扣费系统）

  - 实时监测广告主投放广告的累计费用，达到广告主设定的时间段或者次数的投放时，由扣费系统来停止该广告的投放，以避免广告系统承担超时或超次数导致多出的广告费用
- 报表系统

使用的技术：

- JDK1.8
- MySQL5.7
- SpringCloud FinchLey
  - springboot的核心是简化Spring应用的搭建和开发过程，使用默认配置简化项目配置，没有冗余代码生成和XML配置的要求
- Kafka2.1



# 项目总结

### 实现接口统一返回参数

​	使用 `@ControllerAdvice` & 实现`ResponseBodyAdvice`接口 拦截Controller方法默认返回参数，统一处理返回值/响应体

````java
@ControllerAdvice
public class CommonResponseAdvice implements ResponseBodyAdvice<Object> {
 
    /**
      * 在响应返回之前可以做的事
      **/
    @Override
    public Object beforeBodyWrite(Object o, 
                                  MethodParameter methodParameter, 
                                  MediaType mediaType, 
                                  Class aClass, 
                                  ServerHttpRequest serverHttpRequest, 
                                  ServerHttpResponse serverHttpResponse) {
        
        CommonResponse<Object> response = new CommonResponse<>(200, "请求成功");
        if (null == o) {
            return response;
        } else if (o instanceof CommonResponse) {
            response = (CommonResponse<Object>) o;
        } else {
            response.setData(o);
        }

        return response;
    }
 
    /**
      * 判断是否应该对此响应进行拦截，拦截返回true
      **/
    @Override
    public boolean supports(MethodParameter methodParameter, 
                           Class<? extends HttpMessageConverter<?>> converterType) {
        //类上包含该注解即不拦截
        if (methodParameter.getDeclaringClass()
            			   .isAnnotationPresent(IgnoreResponseAdvice.class)) {
            return false;
        }
		//方法上包含该注解即不拦截
        if (methodParameter.getMethod()
            		       .isAnnotationPresent(IgnoreResponseAdvice.class)) {
            return false;
        }

        return true;
    }

````

````java
//该注解仅用于标识是否需要拦截响应进行处理
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface IgnoreResponseAdvice {
}
````

````java
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CommonResponse<T> implements Serializable {

    private String msg;
    private Integer status;
    private T data;
    public CommonResponse(Integer status, String msg) {
        this.status = status;
        this.msg = msg;
    }
}
````

### SpringMVC模块解析

![springMVC工作流程](.\springcloud广告系统的设计与实现 总结\SpringMVC模块解析.png)

DispatchServlet的三大功能：

- 捕获特定URL格式的Http请求
- 初始化DispatchServlet的上下文
- 初始化springmvc的各个组件，装备到DispatchServlet中，以便完成各个步骤



### 项目中不定义外键的原因

1. 外键占用存储空间
2. 外键与母表存在关联，一旦母表受到损坏，外键很难恢复
3. 在数据迁移，分库分表的情况下，外键不好进行维护



### MySQL Binlog

1. 什么是Binlog？

   二进制日志，记录对数据发生或潜在发生更改的SQL语句，并以二进制的形式保存在磁盘中

2. Binlog的作用

   - 主从复制
   - 恢复和审计