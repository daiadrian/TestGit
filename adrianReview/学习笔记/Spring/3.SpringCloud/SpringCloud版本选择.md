## SpringCloud版本选择

springcloud官方网站：[SpringCloud官网](https://spring.io/projects/spring-cloud/)

![1.SpringCloud版本选择](.\images\1.SpringCloud版本选择.png)



更精确的版本选择需要查看 SpringCloud的文档

![2.SpringCloud文档](.\images\2.SpringCloud文档.png)

![3.选择最合适的SpringBoot版本](.\images\3.选择最合适的SpringBoot版本.png)



更准确的版本选择可以通过 Spring 官方提供的接口获取：`https://start.spring.io/actuator/info`



## Maven配置

1. `dependencyManagement`：锁定版本，且子模块继承之后不需要再写 groupId 和 版本号
2. `dependencyManagement` 和 `dependencies` 的区别：
   - `dependencyManagement` 是 Maven 用来提供一种管理依赖版本号的方式。通常会在一个项目的最顶层的父Pom文件中使用该元素。
   - 在Pom文件中使用了 `dependencyManagement`  元素能让所有在子项目中引用一个依赖而不用显示列出版本号。Maven 会沿着父子层次向上走，直到找到一个拥有 `dependencyManagement` 元素的项目，然后它就会使用该元素指定的版本号
   - 使用了`dependencyManagement` 就可以使多个子项目都引用同一样依赖，可以避免在每个使用的子项目中都去声明版本号。这样当想要升级或者切换版本时，就只需要在父Pom中更新即可。如果子项目需要另外的版本号，则在自己的Pom文件中声明版本号即可
   - `dependencyManagement` 只是声明了依赖，并不会实现引入，因此子项目中需要显示的声明需要使用到的依赖，才会引入相关Jar包
   - 如果不在子项目中声明依赖，是不会从父项目中继承依赖的；只有当子项目中写入该依赖，并且没有指定版本号，才会从父项目中继承该依赖，并且version和scope都读取自父Pom文件
   - 如果子项目中指定其他版本号，那么就会使用子项目中指定版本号的jar









