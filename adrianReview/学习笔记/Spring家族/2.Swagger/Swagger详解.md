### Swagger简介

​		swagger是一个功能强大的在线API文档的框架，提供在线文档的查阅和测试功能



### 引入swagger

了解一下springfox：

```txt
	Springfox是践行OAS的一个项目，它将Swagger融合进流行的Spring框架，根据OpenAPI规范，帮助开发者自动生成API文档。Springfox是由Marty Pitt创建的项目swagger-springmvc发展而来
	它其中有一个组件叫springfox-swagger2，springfox-swagger2是依赖OSA规范文档，也就是一个描述API的json文件，而这个组件的功能就是帮助我们自动生成这个json文件
	我们会用到的另外一个组件springfox-swagger-ui就是将这个json文件解析出来，用一种更友好的方式呈现出来
```



```xml
<!-- springfox-swagger -->
<dependency>
    <groupId>io.springfox</groupId>
    <artifactId>springfox-swagger2</artifactId>
    <version>2.9.2</version>
</dependency>
<dependency>
    <groupId>io.springfox</groupId>
    <artifactId>springfox-swagger-ui</artifactId>
    <version>2.9.2</version>
</dependency>
```



```java
@Configuration
@EnableSwagger2
public class SwaggerConfig {

    /**
     * Docket这个Bean包含了API文档的描述信息,已经包扫描的基本包名等信息
     * @return
     */
    @Bean
    public Docket createRestApi() {
        //指定规范，这里是SWAGGER_2
        return new Docket(DocumentationType.SWAGGER_2)
                /**
                 * 设定Api文档头信息,这个信息会展示在文档UI的头部位置
                 */
                .apiInfo(getApiInfo())
                .select()
                /**
                 * 添加过滤条件, 这里是限制扫描的包
                 */
                .apis(RequestHandlerSelectors.basePackage("com.dai.studyspringboot.controller"))
                /**
                 * 对所有路径进行监控
                 */
                .paths(PathSelectors.any())
                .build();
    }

    /**
     * 自定义API文档基本信息实体
     * @return
     */
    private ApiInfo getApiInfo() {
        return new ApiInfoBuilder()
                .title("使用Swagger构建Api文档")
                .description("我就是详细描述")
                .version("1.0")
                .build();
    }

}
```



项目启动后访问：http://localhost:9090/swagger-ui.html



### 注解详解

- <font color=red>@Api：</font>修饰整个类，用于描述控制器类

  ```java
  //value：表示说明,可以使用tags代替
  //tags：表示说明,也用于分组;
  //需要注意的是当tags有多个值,会对这个类中的所有方法生成多份swagger资源,且每份swagger资源中都包含类中所有的方法
  @Api(value = "swagger的使用示例", tags = {"示例接口"})
  ```

  

- <font color=red>@ApiOperation：</font>描述类方法，即接口

  ```java
  //value：用于方法描述
  //httpMethod：说明HTTP方法 (如果使用的@RequestMapping指定了method属性，则可以省略此属性)
  //response ：说明返回值类型 (无需指定，可自动检测到)
  //notes：用于简短说明，可在特殊情况下对接口进行补充说明，或提示说明
  @ApiOperation(value = "示例的方法", httpMethod = "GET", response = String.class, notes = "简短说明")
  ```

  

- <font color=red>@ApiIgnore：</font>使用该注解，表示Swagger忽略这个API

  - 可以用于类或者方法上，用在类上忽略整个控制器的所有API，用在具体方法上则忽略对应的方法

- <font color=red>@ApiParam：</font>单个参数的描述

- <font color=red>@ApiModel：</font>用对象来接收参数

- <font color=red>@ApiProperty：</font>用对象接收参数时，描述对象的一个字段

- <font color=red>@ApiResponse：</font>HTTP响应的一个描述

- <font color=red>@ApiResponses：</font>HTTP响应的整体描述

- <font color=red>@ApiError：</font>发生错误时返回的参数

- <font color=red>@ApiParamImplicit：</font>一个请求参数

- <font color=red>@ApiParamsImplict：</font>多个请求参数